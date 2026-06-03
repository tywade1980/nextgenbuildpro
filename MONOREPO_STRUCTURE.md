# NextGen BuildPro — Monorepo Structure

Consolidated from 37 GitHub repositories into 5 unified domain packages.

---

## Repository Map

```
nextgenbuildpro/                          ← ROOT = Group 1: Construction Platform
├── app/                                  ← Android app (Kotlin/Gradle)
│   └── src/main/java/com/nextgenbuildpro/
│       ├── agents/               AI agents (voice, CRM, estimating)
│       ├── apps/                 CallScreen, DialerApp, ConstructionPlatform
│       ├── bms/                  Building Management System
│       ├── clientengagement/     Client portal, progress updates
│       ├── core/                 Firebase, Firestore, API keys, learning
│       ├── mcp/                  Model Context Protocol server
│       ├── navigation/           Compose nav graph
│       ├── orchestrators/        CEO/CFO/COO/CTO/CSO/CHRO orchestrators
│       ├── pm/                   Project management (estimates, catalogue, labor)
│       ├── receptionist/         AI receptionist + call handling
│       ├── timeclock/            Time tracking
│       └── ui/                   Compose UI components + theme
├── services/                             ← TypeScript catalogue & pricing
├── models/                               ← TypeScript data models
├── seeds/                                ← Database seed scripts (2025 pricing)
├── tests/                                ← TypeScript tests
│
└── packages/                             ← Groups 2–5
    │
    ├── construction-platform/            ← GROUP 1 EXTRAS
    │   └── android/
    │       ├── ConstructionPlatformIntegration.kt  ← Unified domain model
    │       ├── domain/model/             ← ngbp-v2-0 clean-arch domain models
    │       ├── data/database/            ← Room DB (entities, DAOs, SeedData)
    │       ├── data/network/             ← Retrofit auth API
    │       ├── data/repository/          ← Repository implementations
    │       └── ui/screens/               ← Extra screens from ngbp-v2-0
    │
    ├── caroline-ai/                      ← GROUP 2: Caroline AI Executive Assistant
    │   ├── android/src/
    │   │   ├── CarolineAIApplication.kt  ← App entry point
    │   │   ├── CarolineServiceLocator.kt ← Service DI
    │   │   ├── llm/CarolineLLMService.kt ← Multi-provider LLM (xAI/OpenAI/Anthropic/OR)
    │   │   ├── llm/ApiKeyStore.kt        ← EncryptedSharedPreferences key storage
    │   │   ├── VoiceInterfaces.kt        ← STT/TTS contracts
    │   │   ├── APIManager.kt             ← Secure API key manager
    │   │   └── AutonomousEvolutionEngine.kt
    │   ├── server/                       ← Python FastAPI (caroline-server-v2)
    │   │   ├── main.py                   ← xAI Realtime WS proxy + LLM relay + TTS
    │   │   ├── modules/
    │   │   │   ├── llm_orchestrator.py   ← Multi-model routing (5 strategies)
    │   │   │   ├── real_voice_engines.py ← Groq + ElevenLabs voice
    │   │   │   ├── health_monitor.py     ← CPU/memory/disk monitoring
    │   │   │   └── neural_interface.py   ← Background scanner/weather/route threads
    │   │   └── .env.example
    │   └── config/                       ← Caroline "soul pack" (JSON)
    │       ├── identity.json             ← Agent identity definition
    │       ├── emotional_engine.json     ← Response weights + tone
    │       ├── instruction_core.json     ← Command trigger phrases
    │       └── presence_rules.json       ← Behavioral rules
    │
    ├── voice-telephony/                  ← GROUP 3: Voice & Telephony Platform
    │   ├── android/                      ← smart-incallservice (most complete)
    │   │   └── (Phi-3.5-mini ONNX, 8 agents, Room DB, Hilt DI)
    │   ├── android-screener/             ← telephony_agent (CallScreeningService)
    │   ├── server/                       ← nextgentele (Node.js SIP/WebRTC/IVR)
    │   │   ├── unified-gateway.js        ← Routes all voice services
    │   │   ├── src/services/             ← SIP, WebRTC, IVR, AI, carrier, agent
    │   │   ├── deepgram/                 ← Deepgram Voice Agent integration
    │   │   └── .env.example
    │   ├── web/                          ← voice-ai-app (React Native Expo)
    │   │   └── (Caroline/xAI Realtime, VAD, push-to-talk)
    │   ├── aria-voice/                   ← Aria-voice (Python FastAPI + React + Runpod)
    │   │   ├── backend/server.py         ← Multi-backend (Ollama/Runpod/OpenAI)
    │   │   ├── frontend/                 ← React UI (swarm, skills, conversation)
    │   │   └── runpod_container/         ← Self-hosted GPU (XTTS, Fish Speech, vLLM)
    │   └── web-dashboard/                ← next-genai (Next.js 15 dashboard)
    │       └── (OpenAI + OpenRouter + DALL-E 3 call analysis)
    │
    ├── ai-framework/                     ← GROUP 4: AI Agent Framework
    │   ├── android/
    │   │   ├── agents/                   ← nextgen_apk: BigDaddy, HRM, Hermes, MRM, EliteHuman
    │   │   ├── core/                     ← MainOrchestrator, AgentOrchestrator
    │   │   ├── env/                      ← LivingEnv (3D spatial agent positioning)
    │   │   └── shared/                   ← Data models, constants
    │   ├── python/
    │   │   ├── agents/
    │   │   │   ├── unified_agent_system.py  ← OpenClaw routing + NeuroRank + 6 agents
    │   │   │   ├── orchestrator/         ← multi_agent_orchestrator.py (xAI Grok)
    │   │   │   ├── server/               ← FastAPI REST (chat/voice/estimate endpoints)
    │   │   │   └── hrm_models/           ← PyTorch Hierarchical Reasoning Model
    │   │   ├── state/
    │   │   │   ├── wade_global_state.json ← Canonical agent registry + OpenClaw rules
    │   │   │   ├── hermes.py             ← Message routing engine
    │   │   │   ├── orchestrator.py       ← Caroline conversation layer
    │   │   │   ├── sync_wgs.py           ← GitHub-backed state sync
    │   │   │   └── agent_hooks/          ← caroline_bridge.py
    │   │   └── skills/                   ← manus-DRS-skills (18 Manus skills)
    │   │       ├── caroline-ai/          ← Bridge to Caroline + WGS sync
    │   │       ├── centauri-interlock/   ← Command_Router event bus
    │   │       ├── neurorank/            ← NeuroRank™ cognitive scoring
    │   │       ├── rsmeans-cost-estimator/ ← RSMeans construction data
    │   │       ├── runpod-connector/     ← RunPod GPU pod management
    │   │       ├── honcho-caroline/      ← Honcho persistent memory bridge
    │   │       └── (13 more skills)
    │   ├── backend/                      ← Spring Boot API (nextgen_apk)
    │   ├── mcp-server/                   ← MCP server (nextgen_apk)
    │   └── infra/                        ← Docker Compose (PostgreSQL+pgvector, Redis,
    │                                          ChromaDB, Prometheus, Grafana, MinIO, Elasticsearch)
    │
    └── dev-tools/                        ← GROUP 5: Developer Tools
        ├── hface-ide/                    ← Hface-IDE (VS Code extension, HF Hub)
        ├── kilocode-ide/                 ← Kilocode (VS Code extension fork)
        ├── code-gen/                     ← ai-full-stack-develo (24-tab AI meta-IDE)
        ├── model-tools/                  ← model-downloader (Python CLI + Flask + Android)
        ├── chatbot/                      ← nextjs-ai-chatbot (Next.js 15, xAI Grok)
        └── wcc-website/                  ← Wcc (Wade Custom Carpentry website + admin)
```

---

## Deduplication Decisions

| Feature | Source Repos | Chosen Canonical | Reason |
|---|---|---|---|
| Construction domain model | ngbp-v2-0, nextgen_apk, root | ConstructionPlatformIntegration.kt | Merges Room persistence + blueprint/quality/defect tracking |
| Android voice interfaces | caroline-alpha, caroline-android, smart-incallservice | packages/caroline-ai/android (STT/TTS) + packages/voice-telephony/android (full InCallService) | Different concerns |
| LLM multi-provider routing | caroline-alpha, unified-agentic-ai-foundation | CarolineLLMService.kt + unified_agent_system.py | Android vs Python contexts |
| Material pricing seed data | ngbp-v2-0/SeedData.kt | packages/construction-platform/android/data/database/SeedData.kt | 2025 US regional pricing (15+ materials) |
| Call screening | telephony_agent, smart-incallservice | smart-incallservice (Phi-3.5 ONNX, 8 agents, Room DB) | More complete |
| Voice WebSocket server | caroline-server-v2, nextgentele | caroline-server-v2 main.py (Python) + unified-gateway.js (Node.js routing) | Both preserved, gateway routes |
| Agent registry | wade-global-state, centauri-interlock | wade_global_state.json | Higher-level canonical registry |
| Caroline soul config | caroline-alpha, unified-agentic-ai-foundation | packages/caroline-ai/config/ | Single canonical identity/emotion JSON |
| VS Code extension | Hface-IDE, kilocode-src | Both preserved in packages/dev-tools/ | Different functionality |

---

## Security Notes

- All API keys use environment variables (`.env.example` in each package)
- Hardcoded Honcho API key removed from `packages/ai-framework/python/skills/honcho-caroline/scripts/honcho_caroline_node.py`
- Admin authentication in `wcc-website` is placeholder — replace `localStorage` auth before production
- `ai-filter-blocker` module NOT included (content filter bypass not appropriate for production)
- Google Services JSON contains placeholder values — replace for production builds
