"""
Wade Ecosystem — Orchestrator REST API Server (The Brain)
=========================================================
LLM:   Grok (xAI)      — via XAI_API_KEY env variable
Voice: ElevenLabs TTS  — via ELEVENLABS_API_KEY env variable
STT:   Deepgram        — via DEEPGRAM_API_KEY env variable

This FastAPI server is the central nervous system of the Wade Ecosystem.
It exposes the multi-agent orchestrator as a REST API callable from:
  - Caroline Android Super App  (ConstructionViewModel.kt)
  - Voice Pipeline              (VoiceEngine.kt)
  - Telephony Receptionist      (caroline_receptionist.py)
  - Any web dashboard or MCP tool

Run locally:
    uvicorn orchestrator_server:app --host 0.0.0.0 --port 8000 --reload

Deploy to RunPod / Railway / Render with env vars:
    XAI_API_KEY, ELEVENLABS_API_KEY, DEEPGRAM_API_KEY
"""

import os, sys, json, asyncio, re
from datetime import datetime
from typing import Optional, List, Dict, Any

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import StreamingResponse
from pydantic import BaseModel
import uvicorn
import httpx

# ── Import the orchestrator engine ──────────────────────────────────────────
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from orchestrator.multi_agent_orchestrator import (
    agents, process_query, register_agent,
    add_tool_to_agent, list_agents, run_parallel_tasks
)

# ── API Keys ─────────────────────────────────────────────────────────────────
XAI_API_KEY         = os.environ.get("XAI_API_KEY", "")
ELEVENLABS_API_KEY  = os.environ.get("ELEVENLABS_API_KEY", "")
DEEPGRAM_API_KEY    = os.environ.get("DEEPGRAM_API_KEY", "")

# ElevenLabs voice ID for Caroline — "Rachel" is warm and professional
# Override with ELEVENLABS_VOICE_ID env var to use a custom cloned voice
ELEVENLABS_VOICE_ID = os.environ.get("ELEVENLABS_VOICE_ID", "21m00Tcm4TlvDq8ikWAM")
ELEVENLABS_MODEL    = os.environ.get("ELEVENLABS_MODEL", "eleven_turbo_v2_5")

# ─────────────────────────────────────────────────────────────────────────────
# App Setup
# ─────────────────────────────────────────────────────────────────────────────

app = FastAPI(
    title="Wade Ecosystem — Orchestrator API",
    description="The brain of the Caroline AI platform. Powered by Grok (xAI) + ElevenLabs.",
    version="2.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Per-session conversation history (in-memory; swap for Redis in production)
conversation_store: Dict[str, List[Dict]] = {}

# ─────────────────────────────────────────────────────────────────────────────
# Request / Response Models
# ─────────────────────────────────────────────────────────────────────────────

class ChatRequest(BaseModel):
    agent: str = "Orchestrator"
    message: str
    session_id: str = "default"

class ChatResponse(BaseModel):
    agent: str
    message: str
    response: str
    session_id: str
    timestamp: str

class VoiceTextRequest(BaseModel):
    """Receives a Deepgram STT transcript and returns text + optional audio URL."""
    transcript: str
    session_id: str = "voice_default"
    agent: str = "Orchestrator"
    return_audio: bool = True   # If True, synthesize ElevenLabs TTS and return audio bytes

class EstimateRequest(BaseModel):
    client_name: str
    project_type: str
    notes: str = ""

class LogHoursRequest(BaseModel):
    task_name: str
    hours: float
    progress_pct: int
    notes: str = ""

class AddToolRequest(BaseModel):
    agent_name: str
    tool_name: str
    tool_code: str

class ParallelTaskRequest(BaseModel):
    tasks: List[Dict[str, str]]

# ─────────────────────────────────────────────────────────────────────────────
# Status & Health
# ─────────────────────────────────────────────────────────────────────────────

@app.get("/")
async def root():
    return {
        "system": "Wade Ecosystem — Orchestrator API",
        "version": "2.0.0",
        "llm": "Grok (xAI) — grok-3-mini",
        "tts": "ElevenLabs — eleven_turbo_v2_5",
        "stt": "Deepgram — nova-3",
        "status": "operational",
        "agents": list(agents.keys()),
        "keys_loaded": {
            "xai": bool(XAI_API_KEY),
            "elevenlabs": bool(ELEVENLABS_API_KEY),
            "deepgram": bool(DEEPGRAM_API_KEY)
        },
        "timestamp": datetime.now().isoformat()
    }

@app.get("/health")
async def health():
    return {
        "status": "healthy",
        "agents_loaded": len(agents),
        "agent_names": list(agents.keys()),
        "timestamp": datetime.now().isoformat()
    }

@app.get("/agents")
async def get_agents():
    return json.loads(list_agents())

# ─────────────────────────────────────────────────────────────────────────────
# Core Chat Endpoint — Android app, web clients, MCP tools
# ─────────────────────────────────────────────────────────────────────────────

@app.post("/chat", response_model=ChatResponse)
async def chat(request: ChatRequest):
    """
    Primary chat endpoint. Routes message to the specified Grok-powered agent.
    Maintains per-session conversation history for multi-turn dialogue.
    """
    if request.agent not in agents:
        raise HTTPException(
            status_code=404,
            detail=f"Agent '{request.agent}' not found. Available: {list(agents.keys())}"
        )

    history = conversation_store.get(request.session_id, [])
    history.append({"role": "user", "content": request.message})

    loop = asyncio.get_event_loop()
    response_text = await loop.run_in_executor(
        None, lambda: process_query(request.agent, history)
    )

    history.append({"role": "assistant", "content": response_text})
    conversation_store[request.session_id] = history[-20:]   # keep last 20 turns

    return ChatResponse(
        agent=request.agent,
        message=request.message,
        response=response_text,
        session_id=request.session_id,
        timestamp=datetime.now().isoformat()
    )

# ─────────────────────────────────────────────────────────────────────────────
# Voice Pipeline Endpoint
# Flow: Deepgram STT transcript → Grok Agent → ElevenLabs TTS audio
# ─────────────────────────────────────────────────────────────────────────────

@app.post("/voice/text")
async def voice_text_input(request: VoiceTextRequest):
    """
    Receives a Deepgram STT transcript, routes it through the Grok-powered
    orchestrator, and returns the response text. If return_audio=True,
    also synthesizes ElevenLabs TTS and returns the audio as a streaming MP3.
    """
    transcript = request.transcript.strip()
    if not transcript:
        raise HTTPException(status_code=400, detail="Empty transcript.")

    agent_name = _route_voice_intent(transcript)
    history = conversation_store.get(request.session_id, [])
    history.append({"role": "user", "content": transcript})

    loop = asyncio.get_event_loop()
    response_text = await loop.run_in_executor(
        None, lambda: process_query(agent_name, history)
    )

    history.append({"role": "assistant", "content": response_text})
    conversation_store[request.session_id] = history[-20:]

    voice_text = _trim_for_voice(response_text)

    return {
        "transcript": transcript,
        "agent_used": agent_name,
        "response_text": response_text,
        "voice_text": voice_text,
        "session_id": request.session_id,
        "timestamp": datetime.now().isoformat()
    }

@app.post("/voice/synthesize")
async def synthesize_speech(text: str, voice_id: str = ELEVENLABS_VOICE_ID):
    """
    Synthesizes speech from text using ElevenLabs eleven_turbo_v2_5.
    Returns a streaming MP3 audio response.
    Called by VoiceEngine.kt after receiving the response_text from /voice/text.
    """
    if not ELEVENLABS_API_KEY:
        raise HTTPException(status_code=503, detail="ElevenLabs API key not configured.")
    if not text.strip():
        raise HTTPException(status_code=400, detail="Empty text for synthesis.")

    voice_text = _trim_for_voice(text)

    async def generate_audio():
        url = f"https://api.elevenlabs.io/v1/text-to-speech/{voice_id}/stream"
        headers = {
            "xi-api-key": ELEVENLABS_API_KEY,
            "Content-Type": "application/json",
            "Accept": "audio/mpeg"
        }
        payload = {
            "text": voice_text,
            "model_id": ELEVENLABS_MODEL,
            "voice_settings": {
                "stability": 0.5,
                "similarity_boost": 0.75,
                "style": 0.0,
                "use_speaker_boost": True
            }
        }
        async with httpx.AsyncClient(timeout=30.0) as client:
            async with client.stream("POST", url, headers=headers, json=payload) as response:
                if response.status_code != 200:
                    error = await response.aread()
                    raise HTTPException(
                        status_code=response.status_code,
                        detail=f"ElevenLabs error: {error.decode()}"
                    )
                async for chunk in response.aiter_bytes(chunk_size=4096):
                    yield chunk

    return StreamingResponse(
        generate_audio(),
        media_type="audio/mpeg",
        headers={"X-Voice-Text": voice_text[:100]}
    )

@app.post("/voice/full_pipeline")
async def full_voice_pipeline(request: VoiceTextRequest):
    """
    Complete voice pipeline in one call:
    Deepgram transcript → Grok agent → ElevenLabs TTS audio stream.
    This is the single endpoint VoiceEngine.kt calls for the full loop.
    """
    transcript = request.transcript.strip()
    if not transcript:
        raise HTTPException(status_code=400, detail="Empty transcript.")

    agent_name = _route_voice_intent(transcript)
    history = conversation_store.get(request.session_id, [])
    history.append({"role": "user", "content": transcript})

    loop = asyncio.get_event_loop()
    response_text = await loop.run_in_executor(
        None, lambda: process_query(agent_name, history)
    )

    history.append({"role": "assistant", "content": response_text})
    conversation_store[request.session_id] = history[-20:]

    voice_text = _trim_for_voice(response_text)

    if not ELEVENLABS_API_KEY or not request.return_audio:
        return {
            "response_text": response_text,
            "voice_text": voice_text,
            "agent_used": agent_name,
            "audio": None
        }

    # Stream ElevenLabs TTS audio directly
    async def generate():
        url = f"https://api.elevenlabs.io/v1/text-to-speech/{ELEVENLABS_VOICE_ID}/stream"
        headers = {"xi-api-key": ELEVENLABS_API_KEY, "Content-Type": "application/json", "Accept": "audio/mpeg"}
        payload = {
            "text": voice_text,
            "model_id": ELEVENLABS_MODEL,
            "voice_settings": {"stability": 0.5, "similarity_boost": 0.75, "use_speaker_boost": True}
        }
        async with httpx.AsyncClient(timeout=30.0) as client:
            async with client.stream("POST", url, headers=headers, json=payload) as resp:
                async for chunk in resp.aiter_bytes(4096):
                    yield chunk

    return StreamingResponse(
        generate(),
        media_type="audio/mpeg",
        headers={
            "X-Agent-Used": agent_name,
            "X-Response-Text": response_text[:200],
            "X-Voice-Text": voice_text[:100]
        }
    )

# ─────────────────────────────────────────────────────────────────────────────
# WCC Pro Convenience Endpoints
# ─────────────────────────────────────────────────────────────────────────────

@app.post("/wcc/estimate")
async def create_estimate(request: EstimateRequest):
    prompt = f"Create an estimate for {request.client_name} — {request.project_type}. Notes: {request.notes}"
    loop = asyncio.get_event_loop()
    response = await loop.run_in_executor(None, lambda: process_query("WCC_Pro", [{"role": "user", "content": prompt}]))
    return {"response": response, "timestamp": datetime.now().isoformat()}

@app.post("/wcc/log_hours")
async def log_hours(request: LogHoursRequest):
    prompt = f"Log {request.hours} hours on '{request.task_name}' at {request.progress_pct}% complete. Notes: {request.notes}"
    loop = asyncio.get_event_loop()
    response = await loop.run_in_executor(None, lambda: process_query("WCC_Pro", [{"role": "user", "content": prompt}]))
    return {"response": response, "timestamp": datetime.now().isoformat()}

@app.get("/wcc/pricebook/{search_term}")
async def search_pricebook(search_term: str):
    prompt = f"Search the pricebook for: {search_term}"
    loop = asyncio.get_event_loop()
    response = await loop.run_in_executor(None, lambda: process_query("WCC_Pro", [{"role": "user", "content": prompt}]))
    return {"response": response, "search_term": search_term}

@app.get("/wcc/briefing")
async def get_briefing():
    prompt = "Give me a concise voice-ready project status briefing. List each active project, its current phase, and progress percentage. Keep it under 150 words."
    loop = asyncio.get_event_loop()
    response = await loop.run_in_executor(None, lambda: process_query("WCC_Pro", [{"role": "user", "content": prompt}]))
    return {"briefing": response, "timestamp": datetime.now().isoformat()}

# ─────────────────────────────────────────────────────────────────────────────
# Dynamic Tool Registration
# ─────────────────────────────────────────────────────────────────────────────

@app.post("/tools/add")
async def add_tool(request: AddToolRequest):
    result = add_tool_to_agent(request.agent_name, request.tool_name, request.tool_code)
    return {"result": result, "timestamp": datetime.now().isoformat()}

@app.post("/tasks/parallel")
async def parallel_tasks(request: ParallelTaskRequest):
    loop = asyncio.get_event_loop()
    result = await loop.run_in_executor(None, lambda: run_parallel_tasks(request.tasks))
    return json.loads(result)

# ─────────────────────────────────────────────────────────────────────────────
# Session Management
# ─────────────────────────────────────────────────────────────────────────────

@app.delete("/session/{session_id}")
async def clear_session(session_id: str):
    conversation_store.pop(session_id, None)
    return {"status": "cleared", "session_id": session_id}

@app.get("/sessions")
async def list_sessions():
    return {sid: len(msgs) for sid, msgs in conversation_store.items()}

# ─────────────────────────────────────────────────────────────────────────────
# Private Helpers
# ─────────────────────────────────────────────────────────────────────────────

def _route_voice_intent(transcript: str) -> str:
    lower = transcript.lower()
    if any(k in lower for k in ["estimate", "proposal", "log hours", "client", "project",
                                  "pricebook", "material", "briefing", "tile", "cabinet",
                                  "flooring", "bathroom", "kitchen", "trim", "carpentry",
                                  "demo", "drywall", "paint", "punch", "schedule"]):
        return "WCC_Pro"
    if any(k in lower for k in ["research", "price check", "rsmeans", "building code",
                                  "home depot", "lowes", "ferguson", "supplier"]):
        return "Researcher"
    if any(k in lower for k in ["build tool", "create tool", "new capability", "autotool",
                                  "generate tool", "add tool"]):
        return "AutoTooler"
    return "Orchestrator"

def _trim_for_voice(text: str) -> str:
    """Strip markdown and trim to voice-friendly length for ElevenLabs TTS."""
    text = re.sub(r'#{1,6}\s+', '', text)
    text = re.sub(r'\*{1,2}([^*]+)\*{1,2}', r'\1', text)
    text = re.sub(r'`[^`]+`', '', text)
    text = re.sub(r'\|[^\n]+\|', '', text)
    text = re.sub(r'[-]{3,}', '', text)
    text = re.sub(r'\n{2,}', ' ', text)
    text = re.sub(r'\s{2,}', ' ', text)
    text = text.strip()
    if len(text) > 450:
        text = text[:447] + "..."
    return text

# ─────────────────────────────────────────────────────────────────────────────
# Entry Point
# ─────────────────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    port = int(os.environ.get("PORT", 8000))
    host = os.environ.get("HOST", "0.0.0.0")
    print(f"\n{'='*60}")
    print(f"  Wade Ecosystem — Orchestrator Server v2.0")
    print(f"  LLM:   Grok (xAI) — grok-3-mini")
    print(f"  TTS:   ElevenLabs — {ELEVENLABS_MODEL}")
    print(f"  STT:   Deepgram — nova-3")
    print(f"  Host:  http://{host}:{port}")
    print(f"  Agents: {list(agents.keys())}")
    print(f"{'='*60}\n")
    uvicorn.run("orchestrator_server:app", host=host, port=port, reload=True)
