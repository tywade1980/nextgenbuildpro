# Technical State

This document outlines Wade's current technical environment, including hardware, software, and key scripts.

## Hardware

-   **Primary Mobile**: Android Phone
-   **Primary Computer**: Windows Laptop (often has a low battery)
-   **Secondary Computer**: Old Mac Desktop

## Remote Server (RunPod)

-   **Pod ID**: `dmed1ybt9cju4h`
-   **Service**: GPU server used for hosting the Caroline AI.
-   **Setup**: The server requires a `setup.sh` script to be run to install dependencies and start the server on port 8000. An `start.sh` script is available for auto-starting the pod.
-   **Storage**: All files are saved to `/workspace/` for persistence across pod restarts.

## Caroline AI Stack

-   **Language Model**: Dolphin Mistral (24B Venice Edition), running on Ollama.
-   **Text-to-Speech (TTS)**: ElevenLabs voice (ID: `wvVfSeWpAEhEqciDp1gK`).
-   **Speech-to-Text (STT)**: Faster-Whisper.
-   **Frontend**: An Expo app (SDK 54) is built and functional on his Android phone. The source code is on GitHub: `github.com/tywade1980/voice-ai-app`.
-   **Avatar Animation**: SadTalker is installed but currently paused.

## Key Scripts

-   `setup.sh`: A complete server installation script for Ollama, FastAPI, and voice endpoints.
-   `start.sh`: An auto-start script for the RunPod container.
-   `telegram_voice_bot.py`: A backup Telegram voice bot implementation.
