# Wade Ecosystem — Unified Android App

This is the single unified Android application that absorbs all Wade Ecosystem repositories.
All non-Android code has been converted to Kotlin and integrated into `com.nextgenbuildpro`.

## What came from where

### Originally Android (Kotlin)
| Source Repo | Package in unified app | What it does |
|---|---|---|
| `nextgenbuildpro` | `com.nextgenbuildpro.*` | Base: BMS, CRM, PM, field tools, agents |
| `smart-incallservice` | `.receptionist.*`, `.telecom.*` | AI InCallService, call screening |
| `unified-agentic-ai-foundation/caroline_superapp` | `.core.*`, `.voice.*` | VoiceEngine, inference, neural core |
| `model-downloader/model_downloader_app` | `.modeldownloader.*` | On-device model management |

### Converted from Python → Kotlin
| Source Repo | Original files | Package in unified app | What it does |
|---|---|---|---|
| `wade-global-state` | `hermes.py` | `.hermes.HermesRouter` | Multi-agent message bus & router |
| `wade-global-state` | `hermes.py` | `.hermes.OpenClawChunker` | NLP request → domain task chunker |
| `wade-global-state` | `hermes.py` | `.hermes.AuditAgent` | Result verification & synthesis |
| `wade-global-state` | `hermes.py` | `.hermes.AgentDispatcher` | Per-agent execution dispatcher |
| `wade-global-state` | `wade_global_state.json` | `.orchestrator.WadeGlobalState` | Global state data model (Room-backed) |
| `wade-global-state` | `orchestrator.py` | `.orchestrator.CarolineOrchestrator` | Caroline conversation layer |
| `unified-brain` | `unified_brain_api.py` | `.brain.UnifiedBrainService` | vLLM + Fish Speech API client |
| `unified-brain` | `system_prompts.py` | `.brain.SystemPrompts` | Persona prompt library |
| `unified-brain` | `voice_profiles.py` | `.brain.VoiceProfiles` | TTS voice profile definitions |

### Converted from React Native / TypeScript → Compose
| Source Repo | Original files | Package in unified app | What it does |
|---|---|---|---|
| `voice-ai-app` | `App.tsx` | `.voice.XAIRealtimeClient` | xAI WebSocket client (grok-voice) |
| `voice-ai-app` | `App.tsx` | `.voice.CarolineVoiceViewModel` | Voice UI state management |
| `voice-ai-app` | `App.tsx` | `.voice.ui.CarolineVoiceScreen` | Caroline orb UI in Compose |

### Backend services (referenced, not embedded)
| Repo | Role | How the app connects |
|---|---|---|
| `caroline-ai-command-center` | Web dashboard | REST API via `UnifiedBrainService` |
| `wcc` | Next.js web app | WebView or API only |
| `playwright-mcp` | Browser automation | MCP tool call from agents |
| `expert-panel` | MCP expert routing | Called via Hermes `web_agent` |

## Architecture

```
User
  │
  ▼
CarolineVoiceScreen (Compose)        ← voice-ai-app/App.tsx
  │  xAI WebSocket (grok-voice)
  ▼
CarolineOrchestrator                 ← orchestrator.py
  │
  ▼
HermesRouter                         ← hermes.py
  │  OpenClaw chunking
  ├──► memory_agent  → WadeGlobalState (Room)
  ├──► web_agent     → OkHttp / xAI realtime
  ├──► bms_agent     → NextGenBuildPro estimator
  ├──► crm_agent     → CRM data layer
  ├──► receptionist  → ReceptionistInCallService
  └──► neurorank     → NeuroRank™ scorer
         │
         ▼
     AuditAgent  →  synthesized response
         │
         ▼
UnifiedBrainService                  ← unified_brain_api.py
  ├──► vLLM endpoint (local or RunPod)
  └──► Fish Speech TTS endpoint
```

## Build
```bash
./gradlew assembleDebug
```

## Key packages
- `com.nextgenbuildpro.hermes` — Hermes router + OpenClaw
- `com.nextgenbuildpro.orchestrator` — Caroline orchestrator + WGS
- `com.nextgenbuildpro.brain` — Unified Brain (LLM + TTS)
- `com.nextgenbuildpro.voice` — xAI realtime client + Compose UI
- `com.nextgenbuildpro.receptionist` — AI InCallService
- `com.nextgenbuildpro.bms` — Building Management System
- `com.nextgenbuildpro.crm` — CRM agent
