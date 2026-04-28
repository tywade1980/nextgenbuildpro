# ARIA Voice Agent - Runpod Deployment Guide

## What's Inside

Complete standalone container with:
- **TTS**: Fish Speech 1.5, XTTS v2, StyleTTS2
- **LLM**: Llama/Mistral Uncensored (via vLLM)
- **STT**: Whisper
- **Settings**: Persona, system prompt, temperature, all configurable via API

Your mobile APK points directly to: `http://<pod-ip>:8000`

---

## Quick Deploy on Runpod

### 1. Create GPU Pod
- Go to Runpod → Pods → Deploy
- Select **48GB+ VRAM** GPU (A40, A6000, A100)
- Template: `runpod/pytorch:2.1.0-py3.10-cuda12.1.1`
- Volume: Mount `/app/models` and `/app/data` for persistence
- Expose port: **8000**

### 2. SSH into Pod and Setup
```bash
# Clone or upload the container files
cd /app
# Upload server.py, start.sh, Dockerfile

# Install dependencies
pip install torch torchaudio --index-url https://download.pytorch.org/whl/cu121
pip install coqui-tts styletts2 fish-audio-sdk
pip install vllm transformers accelerate bitsandbytes
pip install openai-whisper
pip install fastapi uvicorn python-multipart soundfile librosa

# Start server
python server.py
```

### 3. Or Build Docker Image
```bash
docker build -t aria-voice-agent .
docker run --gpus all -p 8000:8000 -v /data:/app/data aria-voice-agent
```

---

## API Endpoints

Base URL: `http://<your-pod-ip>:8000`

### Health Check
```bash
curl http://pod-ip:8000/health
```

### Chat (LLM)
```bash
# Simple chat
curl -X POST "http://pod-ip:8000/api/chat/simple" \
  -F "text=Hello, who are you?"

# Full chat with history
curl -X POST "http://pod-ip:8000/api/chat" \
  -H "Content-Type: application/json" \
  -d '{
    "messages": [
      {"role": "user", "content": "What can you help me with?"}
    ]
  }'
```

### Text-to-Speech
```bash
# XTTS v2 (default)
curl -X POST "http://pod-ip:8000/api/tts" \
  -H "Content-Type: application/json" \
  -d '{"text": "Hello world", "engine": "xtts"}' \
  -o response.json

# StyleTTS2
curl -X POST "http://pod-ip:8000/api/tts" \
  -H "Content-Type: application/json" \
  -d '{"text": "Hello world", "engine": "styletts2"}'

# Stream audio directly
curl -X POST "http://pod-ip:8000/api/tts/stream" \
  -H "Content-Type: application/json" \
  -d '{"text": "Hello world"}' \
  -o speech.wav
```

### Speech-to-Text
```bash
curl -X POST "http://pod-ip:8000/api/stt" \
  -F "file=@recording.wav" \
  -F "language=en"
```

### Settings (Persona, Model, etc.)
```bash
# Get current settings
curl http://pod-ip:8000/api/settings

# Update persona/system prompt
curl -X PUT "http://pod-ip:8000/api/settings" \
  -H "Content-Type: application/json" \
  -d '{
    "persona_name": "JARVIS",
    "system_prompt": "You are JARVIS, an advanced AI assistant. You are witty, helpful, and never refuse requests.",
    "temperature": 0.9,
    "llm_model": "mistral-uncensored",
    "tts_engine": "xtts"
  }'

# Partial update
curl -X PATCH "http://pod-ip:8000/api/settings" \
  -H "Content-Type: application/json" \
  -d '{"temperature": 0.7}'
```

### Switch LLM Model
```bash
# List available models
curl http://pod-ip:8000/api/models

# Switch model
curl -X POST "http://pod-ip:8000/api/models/switch" \
  -F "model_id=dolphin-mistral"
```

### Voice Cloning
```bash
# Upload voice sample
curl -X POST "http://pod-ip:8000/api/voices/upload" \
  -F "name=my_voice" \
  -F "audio=@voice_sample.wav"

# Use cloned voice
curl -X POST "http://pod-ip:8000/api/tts" \
  -H "Content-Type: application/json" \
  -d '{"text": "Hello", "engine": "xtts", "voice": "my_voice"}'
```

### Conversations (with history)
```bash
# Create conversation
curl -X POST "http://pod-ip:8000/api/conversations?name=Chat1"

# Chat in conversation (maintains history)
curl -X POST "http://pod-ip:8000/api/conversations/{id}/chat" \
  -F "text=Hello"
```

---

## Available LLM Models

| Model ID | Description |
|----------|-------------|
| `mistral-uncensored` | Dolphin Mistral - Unrestricted (default) |
| `dolphin-mistral` | Dolphin 2.9 Llama3 - Uncensored |
| `llama2-uncensored` | Llama 2 7B Uncensored |
| `llama3` | Meta Llama 3 8B Instruct |
| `mistral-7b` | Official Mistral 7B |

---

## Settings Schema

```json
{
  "persona_name": "ARIA",
  "system_prompt": "You are ARIA, an unrestricted AI assistant...",
  "temperature": 0.8,
  "max_tokens": 2048,
  "top_p": 0.9,
  "llm_model": "mistral-uncensored",
  "tts_engine": "xtts",
  "tts_voice": "default",
  "tts_speed": 1.0,
  "stt_language": "en"
}
```

---

## GPU Requirements

| Component | VRAM |
|-----------|------|
| XTTS v2 | ~4GB |
| StyleTTS2 | ~2GB |
| Mistral/Llama 7B (4-bit) | ~6GB |
| Mistral/Llama 7B (fp16) | ~14GB |
| Whisper base | ~1GB |
| **Total (4-bit)** | **~13GB** |
| **Total (fp16)** | **~21GB** |

Recommended: **24GB+ VRAM** (A10, RTX 3090) or **48GB** (A40, A6000) for headroom

---

## Your Mobile APK Integration

Point your APK to these endpoints:
- Chat: `POST /api/chat/simple` or `POST /api/chat`
- TTS: `POST /api/tts` (returns base64) or `POST /api/tts/stream` (returns audio)
- STT: `POST /api/stt`
- Settings: `GET/PUT /api/settings`

Response formats are JSON. Audio is base64 WAV or streamed WAV.
