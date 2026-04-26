# NextGen BuildPro — Unified Platform

This repository is organized as a **5-group monorepo**. Each `packages/` subdirectory is one self-contained unified codebase consolidating all related repos.

---

## Group Map

| Group | Package | Platforms | Source Repos |
|---|---|---|---|
| 1 | Construction Platform | Android + TypeScript | nextgenbuildpro, constructprobms, Constructpro, Bms, ngbp-v2-0, next-gen-apk, nextgen_apk |
| 2 | Caroline AI | Android + Web + Python | caroline-alpha, caroline-android, caroline-app-v2, caroline-server-v2, caroline-ai-command-center |
| 3 | Voice & Telephony | Android + Web + Server | next-genai, telephony_agent, telephony, smart-incallservice, voice-ai-app, nextgentele, Aria-voice, DG-voice, csr.ai |
| 4 | AI Framework | Python + Android + Shared | wade-global-state, unified-brain, unified-agentic-ai-foundation, centauri-os, ai-agent-android, expert-panel, Curled-waddle, manus-DRS-skills, manus-skill-share-test |
| 5 | Developer Tools | TypeScript + Python | Hface-IDE, ai-full-stack-develo, model-downloader, fluffy-sniffle, nextjs-ai-chatbot, Wcc, manus-master-archive |

---

## Root = Group 1 (Construction Platform — Android + Web)

The repo root IS the Construction Platform:
- `app/` — Android/Kotlin app (Gradle project)
- `services/` — TypeScript catalogue & pricing services
- `models/` — TypeScript data models
- `seeds/` — Database seed scripts

Groups 2–5 live under `packages/<group>/`.

---

## Quick Start Per Group

### Construction Platform (root)
```bash
# Android
./gradlew assembleDebug
# TypeScript
pnpm install && pnpm build
```

### Caroline AI
```bash
cd packages/caroline-ai
# Android
./gradlew assembleDebug
# Web dashboard
cd web && npm install && npm run dev
# Python server
cd server && pip install -r requirements.txt && python main.py
```

### Voice & Telephony
```bash
cd packages/voice-telephony
# Android
./gradlew assembleDebug
# Web
cd web && npm install && npm run dev
```

### AI Framework
```bash
cd packages/ai-framework
# Python
pip install -r requirements.txt
python -m ai_framework.main
# Android module
./gradlew assembleDebug
```

### Developer Tools
```bash
cd packages/dev-tools
# HFace IDE
cd hface-ide && npm install && npm run dev
# Model tools
cd model-tools && pip install -r requirements.txt
```
