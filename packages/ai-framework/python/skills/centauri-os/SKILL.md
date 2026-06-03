---
name: centauri-os
description: "Centauri OS — Wade's custom Android-based operating system with Caroline AI as its core agent. Use for: designing or developing Centauri OS features, defining voice command interfaces, integrating Caroline into Android system services, building the AI-Enhanced CodeAssist app, and planning the Centauri OS architecture."
---

# Centauri OS Skill

Centauri OS is Wade's custom Android-based operating system with the Caroline AI as its core agent. It represents the convergence of all his AI and mobile development work into a single, deeply integrated platform.

## Core Architecture

| Layer | Component | Technology |
|---|---|---|
| AI Core | Caroline AI | Dolphin Mistral on RunPod |
| Voice Interface | STT/TTS | Faster-Whisper + ElevenLabs |
| OS Base | Android | Custom AOSP build |
| App Layer | Expo/React Native | SDK 54 |
| Memory | Long-term context | Wade Global State |
| Security | Encrypted comms | TLS + local-first |

## Core Workflows

### 1. Voice Command Interface Design

Define and implement voice commands for the OS:

- **Wake word**: "Hey Caroline" triggers the AI assistant.
- **System commands**: "Caroline, call [client name]", "Caroline, what's my schedule today?"
- **Construction commands**: "Caroline, start the timer for tile work", "Caroline, add 2 hours to the bathroom project."

### 2. AI-Enhanced CodeAssist

The CodeAssist app is a key component of Centauri OS:

- **Voice I/O**: Dictate code and hear explanations read back.
- **Knowledge Graph**: Interconnected information about Wade's codebase.
- **Autonomous Evolution**: The AI learns from coding sessions and improves its suggestions.

Repository: `tywade1980/ai-agent-android`

### 3. System Integration Points

| Integration | Description |
|---|---|
| Phone calls | Caroline intercepts and screens calls via the telephony skill |
| Calendar | Caroline reads and writes to Google Calendar |
| Location | Caroline infers work context from location data |
| Camera | Caroline analyzes job site photos for progress tracking |
| Notifications | Caroline filters and prioritizes notifications |

## Key References

- **Full Design Proposal**: See `references/centauri_os_design.md` for the complete architecture document.
- **Voice Command Spec**: See `references/voice_commands.md` for the full command vocabulary.

## Best Practices

- **Local-first**: Prioritize on-device processing to minimize latency and protect privacy.
- **Offline capable**: Core functions must work without internet (RunPod may be offline on job sites).
- **Battery aware**: Voice processing is power-intensive; implement smart wake word detection.
- **Caroline continuity**: All OS interactions feed back into Caroline's personality evolution.
