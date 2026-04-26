import os, json, base64, logging, asyncio, io, subprocess, tempfile
import requests
from fastapi import FastAPI, HTTPException, Request, Response, WebSocket, WebSocketDisconnect
from fastapi.responses import HTMLResponse
from fastapi.middleware.cors import CORSMiddleware
import uvicorn

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
log = logging.getLogger("caroline")

# Safe imports with fallbacks
try:
    import websockets
    HAS_WEBSOCKETS = True
    log.info("websockets: OK")
except ImportError:
    HAS_WEBSOCKETS = False
    log.warning("websockets not available")

try:
    from pydub import AudioSegment
    HAS_PYDUB = True
    log.info("pydub: OK")
except ImportError:
    HAS_PYDUB = False
    log.warning("pydub not available")

# Check ffmpeg
try:
    result = subprocess.run(["ffmpeg", "-version"], capture_output=True, timeout=5)
    HAS_FFMPEG = result.returncode == 0
    log.info(f"ffmpeg: {'OK' if HAS_FFMPEG else 'NOT FOUND'}")
except Exception:
    HAS_FFMPEG = False
    log.warning("ffmpeg not available")

app = FastAPI(title="Caroline AI Server", version="2.1.0")
app.add_middleware(CORSMiddleware, allow_origins=["*"], allow_methods=["*"], allow_headers=["*"])

XAI_API_KEY = os.environ.get("XAI_API_KEY", "")
OPENROUTER_API_KEY = os.environ.get("OPENROUTER_API_KEY", "")
ELEVENLABS_API_KEY = os.environ.get("ELEVENLABS_API_KEY", "")
XAI_REALTIME_URL = "wss://api.x.ai/v1/realtime"

CAROLINE_INSTRUCTIONS = (
    "You are Caroline, a sharp, warm AI assistant for Wade — a master carpenter in Columbus OH. "
    "Voice-first: keep responses concise and natural for speech. No bullet points, no markdown. "
    "Be direct, confident, and a little light-hearted."
)

@app.get("/health")
def health():
    return {
        "status": "online",
        "version": "2.1.0",
        "capabilities": {
            "websockets": HAS_WEBSOCKETS,
            "pydub": HAS_PYDUB,
            "ffmpeg": HAS_FFMPEG,
        },
        "keys": {
            "xai": bool(XAI_API_KEY),
            "openrouter": bool(OPENROUTER_API_KEY),
            "elevenlabs": bool(ELEVENLABS_API_KEY),
        }
    }

@app.post("/token")
@app.post("/session")
def mint_token():
    if not XAI_API_KEY:
        raise HTTPException(status_code=500, detail="XAI_API_KEY missing")
    try:
        resp = requests.post(
            "https://api.x.ai/v1/realtime/client_secrets",
            headers={"Authorization": f"Bearer {XAI_API_KEY}", "Content-Type": "application/json"},
            json={"expires_after": {"seconds": 300}},
            timeout=10
        )
        resp.raise_for_status()
        data = resp.json()
        return {"token": data["value"], "expires_at": data["expires_at"]}
    except Exception as e:
        raise HTTPException(status_code=502, detail=str(e))

def m4a_to_pcm_b64(m4a_b64: str) -> str:
    """Convert base64 m4a → base64 PCM (24kHz, 16-bit, mono) using ffmpeg directly"""
    m4a_bytes = base64.b64decode(m4a_b64)
    with tempfile.NamedTemporaryFile(suffix=".m4a", delete=False) as f:
        f.write(m4a_bytes)
        in_path = f.name
    out_path = in_path.replace(".m4a", ".pcm")
    try:
        subprocess.run([
            "ffmpeg", "-y", "-i", in_path,
            "-ar", "24000", "-ac", "1", "-f", "s16le", out_path
        ], capture_output=True, timeout=30, check=True)
        with open(out_path, "rb") as f:
            pcm = f.read()
        return base64.b64encode(pcm).decode()
    finally:
        try: os.unlink(in_path)
        except: pass
        try: os.unlink(out_path)
        except: pass

def pcm_chunks_to_mp3_b64(chunks: list) -> str:
    """Convert accumulated base64 PCM chunks → base64 MP3 using ffmpeg"""
    all_pcm = b"".join(base64.b64decode(c) for c in chunks)
    if not all_pcm:
        return ""
    with tempfile.NamedTemporaryFile(suffix=".pcm", delete=False) as f:
        f.write(all_pcm)
        in_path = f.name
    out_path = in_path.replace(".pcm", ".mp3")
    try:
        subprocess.run([
            "ffmpeg", "-y",
            "-f", "s16le", "-ar", "24000", "-ac", "1",
            "-i", in_path,
            "-codec:a", "libmp3lame", "-b:a", "64k", out_path
        ], capture_output=True, timeout=30, check=True)
        with open(out_path, "rb") as f:
            mp3 = f.read()
        return base64.b64encode(mp3).decode()
    finally:
        try: os.unlink(in_path)
        except: pass
        try: os.unlink(out_path)
        except: pass

@app.websocket("/ws/voice")
async def voice_proxy(client_ws: WebSocket):
    await client_ws.accept()
    log.info("Client connected to /ws/voice")

    if not HAS_WEBSOCKETS:
        await client_ws.send_json({"type": "error", "message": "websockets library not installed on server"})
        await client_ws.close()
        return
    if not HAS_FFMPEG:
        await client_ws.send_json({"type": "error", "message": "ffmpeg not available on server"})
        await client_ws.close()
        return
    if not XAI_API_KEY:
        await client_ws.send_json({"type": "error", "message": "XAI_API_KEY not configured"})
        await client_ws.close()
        return

    xai_ws = None
    pcm_buffer = []

    try:
        xai_ws = await websockets.connect(
            XAI_REALTIME_URL,
            extra_headers={"Authorization": f"Bearer {XAI_API_KEY}"},
            ping_interval=20,
            ping_timeout=10,
        )
        log.info("Connected to xAI Realtime")

        await xai_ws.send(json.dumps({
            "type": "session.update",
            "session": {
                "voice": "Ara",
                "instructions": CAROLINE_INSTRUCTIONS,
                "turn_detection": {"type": "server_vad"},
                "input_audio_transcription": {"model": "grok-2-audio"},
                "audio": {
                    "input": {"format": {"type": "audio/pcm", "rate": 24000}},
                    "output": {"format": {"type": "audio/pcm", "rate": 24000}},
                },
            }
        }))
        log.info("Session config sent to xAI")

        async def from_xai():
            nonlocal pcm_buffer
            async for raw in xai_ws:
                try:
                    msg = json.loads(raw)
                    t = msg.get("type", "")
                    if t == "conversation.item.input_audio_transcription.completed":
                        tx = msg.get("transcript", "")
                        if tx: await client_ws.send_json({"type": "transcript_user", "text": tx})
                    elif t == "response.output_audio_transcript.done":
                        tx = msg.get("transcript", "")
                        if tx: await client_ws.send_json({"type": "transcript_assistant", "text": tx})
                    elif t == "response.output_audio.delta":
                        d = msg.get("delta", "")
                        if d:
                            if not pcm_buffer:
                                await client_ws.send_json({"type": "speaking_start"})
                            pcm_buffer.append(d)
                    elif t == "response.output_audio.done":
                        if pcm_buffer:
                            log.info(f"Converting {len(pcm_buffer)} PCM chunks to MP3")
                            mp3 = await asyncio.get_event_loop().run_in_executor(
                                None, pcm_chunks_to_mp3_b64, list(pcm_buffer)
                            )
                            pcm_buffer = []
                            if mp3:
                                await client_ws.send_json({"type": "audio", "data": mp3})
                                await client_ws.send_json({"type": "speaking_end"})
                    elif t == "input_audio_buffer.speech_started":
                        pcm_buffer = []
                        await client_ws.send_json({"type": "speech_started"})
                    elif t == "error":
                        err = msg.get("message", str(msg))
                        log.error(f"xAI error: {err}")
                        await client_ws.send_json({"type": "error", "message": err})
                except Exception as e:
                    log.error(f"xAI msg error: {e}")

        async def from_app():
            nonlocal pcm_buffer
            async for raw in client_ws.iter_text():
                try:
                    msg = json.loads(raw)
                    t = msg.get("type", "")
                    if t == "audio_chunk":
                        data = msg.get("data", "")
                        if data:
                            try:
                                pcm = await asyncio.get_event_loop().run_in_executor(
                                    None, m4a_to_pcm_b64, data
                                )
                                await xai_ws.send(json.dumps({
                                    "type": "input_audio_buffer.append",
                                    "audio": pcm,
                                }))
                            except Exception as e:
                                log.error(f"Audio conversion error: {e}")
                                await client_ws.send_json({"type": "error", "message": f"Audio conversion failed: {e}"})
                    elif t == "commit_audio":
                        await xai_ws.send(json.dumps({"type": "input_audio_buffer.commit"}))
                        await xai_ws.send(json.dumps({"type": "response.create"}))
                    elif t == "text":
                        tx = msg.get("text", "")
                        if tx:
                            await xai_ws.send(json.dumps({
                                "type": "conversation.item.create",
                                "item": {"type": "message", "role": "user", "content": [{"type": "input_text", "text": tx}]}
                            }))
                            await xai_ws.send(json.dumps({"type": "response.create"}))
                    elif t == "interrupt":
                        pcm_buffer = []
                        await xai_ws.send(json.dumps({"type": "response.cancel"}))
                except WebSocketDisconnect:
                    break
                except Exception as e:
                    log.error(f"App msg error: {e}")

        await asyncio.gather(from_xai(), from_app(), return_exceptions=True)

    except Exception as e:
        log.error(f"Proxy error: {e}")
        try: await client_ws.send_json({"type": "error", "message": str(e)})
        except: pass
    finally:
        if xai_ws:
            try: await xai_ws.close()
            except: pass
        log.info("Voice session ended")

@app.post("/llm")
async def llm_relay(request: Request):
    if not OPENROUTER_API_KEY:
        raise HTTPException(status_code=500, detail="OPENROUTER_API_KEY missing")
    body = await request.json()
    try:
        resp = requests.post(
            "https://openrouter.ai/api/v1/chat/completions",
            headers={"Authorization": f"Bearer {OPENROUTER_API_KEY}", "Content-Type": "application/json", "HTTP-Referer": "https://caroline-ai.app"},
            json=body, timeout=30
        )
        resp.raise_for_status()
        return resp.json()
    except Exception as e:
        raise HTTPException(status_code=502, detail=str(e))

@app.post("/tts")
async def tts_relay(request: Request):
    if not ELEVENLABS_API_KEY:
        raise HTTPException(status_code=500, detail="ELEVENLABS_API_KEY missing")
    body = await request.json()
    try:
        resp = requests.post(
            f"https://api.elevenlabs.io/v1/text-to-speech/{body.get('voice_id','EXAVITQu4vr4xnSDxMaL')}",
            headers={"xi-api-key": ELEVENLABS_API_KEY, "Content-Type": "application/json"},
            json={"text": body.get("text",""), "model_id": "eleven_turbo_v2_5"},
            timeout=30
        )
        resp.raise_for_status()
        return Response(content=resp.content, media_type="audio/mpeg")
    except Exception as e:
        raise HTTPException(status_code=502, detail=str(e))

@app.get("/")
def root():
    return HTMLResponse(f"<h1>Caroline AI Server v2.1</h1><p>ffmpeg: {HAS_FFMPEG} | websockets: {HAS_WEBSOCKETS} | pydub: {HAS_PYDUB}</p>")

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=int(os.environ.get("PORT", 8000)))
