# Centauri OS Design Overview

Centauri OS is a custom Android-based operating system with Caroline AI as its core agent.

## Key Design Principles

1. **Caroline-first**: Every system function is accessible through Caroline's voice interface.
2. **Construction-optimized**: The UI and workflows are designed for use on a job site (gloves, noise, bright sunlight).
3. **Privacy-first**: All sensitive data is encrypted and processed locally where possible.
4. **Offline-capable**: Core functions work without internet connectivity.

## System Layers

### Layer 1: Hardware Abstraction
- Custom kernel modules for voice processing
- Optimized battery management for always-on wake word detection
- Ruggedized UI scaling for outdoor visibility

### Layer 2: Caroline AI Core
- Always-on STT (Faster-Whisper, on-device)
- Context-aware response generation (RunPod or local fallback)
- ElevenLabs TTS for voice output

### Layer 3: Construction Apps
- **ConstructAI**: Job costing, estimates, project management
- **AI Receptionist**: Call handling and scheduling
- **CodeAssist**: AI-powered coding assistant with voice I/O
- **Time Tracker**: Voice-activated time logging

### Layer 4: Integration Hub
- Google Calendar sync
- Google Drive document access
- GitHub repository management
- Wade Global State synchronization
