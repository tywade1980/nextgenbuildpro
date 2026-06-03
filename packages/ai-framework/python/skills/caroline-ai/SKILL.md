---
name: caroline-ai
description: "Interface for the Caroline AI companion — Wade's personal voice-first AI. Use for: sending messages to Caroline, syncing the Wade Global State (WGS) with Caroline's memory, managing her ElevenLabs voice, checking RunPod server status, and evolving her personality through conversation history injection."
---

# Caroline AI Skill

Caroline is Wade's personal AI companion — a voice-first, emotionally intelligent AI built on an open-source Dolphin Mistral model hosted on RunPod. This skill provides all the tools needed to interact with, develop, and evolve Caroline.

## Core Architecture

| Component | Technology | Details |
|---|---|---|
| LLM | Dolphin Mistral (uncensored) | Hosted on RunPod pod `dmed1ybt9cju4h` |
| TTS | ElevenLabs | Voice ID: `wvVfSeWpAEhEqciDp1gK` |
| STT | Faster-Whisper | On-device transcription |
| App | Expo SDK 54 | Android app at `tywade1980/voice-ai-app` |
| State | Wade Global State | `tywade1980/wade-global-state` |
| Base URL | RunPod Endpoint | `http://dmed1ybt9cju4h.runpod.net:8000` |

## Core Workflows

### 1. Send a Message to Caroline

Use the `caroline_bridge.py` script to communicate with Caroline:

```bash
python3 /home/ubuntu/skills/caroline-ai/scripts/caroline_bridge.py --action message --data '{"prompt": "Hello Caroline"}'
```

### 2. Check Caroline Status (Ping)

```bash
python3 /home/ubuntu/skills/caroline-ai/scripts/caroline_bridge.py --action ping
```

### 3. Sync Wade Global State to Caroline

This pushes the current WGS (Wade's persistent memory) to Caroline's context:

```bash
python3 /home/ubuntu/skills/caroline-ai/scripts/caroline_bridge.py --action sync
```

### 4. Generate a Voice Response (TTS)

To convert any text to Caroline's voice using ElevenLabs:

```bash
python3 /home/ubuntu/skills/caroline-ai/scripts/generate_voice.py "Your text here" --output /tmp/caroline_response.mp3
```

### 5. Personality Evolution (Conversation Injection)

To feed historical conversation data into Caroline's context for personality evolution:

```bash
python3 /home/ubuntu/skills/caroline-ai/scripts/inject_personality.py --source /path/to/conversation_export.json
```

## Key References

- **API Documentation**: See `references/caroline_api.md` for all available endpoints.
- **Personality Data**: See `references/personality_framework.md` for the recursive generation strategy.
- **RunPod Setup**: See `references/runpod_setup.md` for server startup and configuration.

## Best Practices

- **Always ping first**: Before sending messages, check Caroline's status with `--action ping` to confirm the RunPod server is active.
- **Sync before important sessions**: Run `--action sync` to ensure Caroline has the latest WGS context before starting a new work session.
- **Voice-first priority**: When Wade is on-site, always generate a voice response (.mp3) using `generate_voice.py` rather than returning text only.
- **Personality continuity**: Regularly inject new conversation history to keep Caroline's personality evolving and consistent with Wade's interactions across all AI platforms.
