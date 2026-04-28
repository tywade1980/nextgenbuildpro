# ARIA - Voice Agent Orchestrator PRD

## Original Problem Statement
Build a deployable conversational agent using Fish Speech 1.5, XTTS v2, MOSS TTSD (substituted with OpenAI TTS), and Mistral open source model (with Runpod placeholder). Features include:
- Proactive skill creator for app connectors
- Agent swarm orchestrator
- Default dialer with call handling (placeholder until carrier permissions)

## User Choices
- **TTS/STT**: OpenAI via Emergent LLM Key
- **LLM**: GPT-5.2 via Emergent LLM Key (Runpod endpoint configurable)
- **Telephony**: Placeholder for carrier integration
- **Connectors**: Plugin-based system
- **UI**: Web dashboard

## Architecture

### Backend (FastAPI)
- `/api/chat` - Conversational AI with GPT-5.2
- `/api/tts` - Text-to-Speech (OpenAI)
- `/api/stt` - Speech-to-Text (Whisper)
- `/api/agents` - Agent CRUD operations
- `/api/skills` - Skills/Connectors management
- `/api/calls` - Call handling (placeholder)
- `/api/settings` - Configuration management

### Frontend (React)
- **ConversationPage** - Voice orb + chat interface
- **SwarmPage** - Agent management dashboard
- **SkillsPage** - Plugin marketplace
- **DialerPage** - Call handling UI (placeholder)
- **SettingsPage** - API configuration

### Database (MongoDB)
- `sessions` - Conversation sessions
- `messages` - Chat messages
- `agents` - AI agents
- `skills` - Installed plugins
- `calls` - Call logs
- `settings` - Global configuration

## User Personas
1. **Power User** - Manages multiple AI agents, creates custom skills
2. **Developer** - Extends ARIA with new connectors
3. **Personal Assistant User** - Voice-first interaction

## What's Been Implemented (Jan 2026)
- [x] Voice conversation interface with VoiceOrb
- [x] GPT-5.2 chat integration via Emergent
- [x] OpenAI TTS (9 voices) with speed control
- [x] OpenAI Whisper STT for voice input
- [x] Agent swarm management (CRUD + status)
- [x] Skills plugin system with marketplace
- [x] Call dialer placeholder
- [x] Settings management
- [x] Cyber-command dark theme UI
- [x] Mobile responsive design

## Prioritized Backlog

### P0 (Critical)
- Carrier telephony integration (Twilio/Vonage)
- Runpod Mistral endpoint connection

### P1 (High)
- Multi-agent orchestration (task routing)
- Real-time audio streaming for calls
- Agent-to-agent communication

### P2 (Medium)
- OAuth for skill connectors (Slack, Discord, etc.)
- Conversation memory/RAG integration
- Voice cloning support (Fish Speech/XTTS)

### P3 (Nice to Have)
- Agent analytics dashboard
- Skill versioning and updates
- Conversation export/import

## Next Tasks
1. Configure Runpod endpoint for Mistral inference
2. Integrate Twilio for call handling when carrier access available
3. Implement skill connector OAuth flows
4. Add multi-agent task routing logic
