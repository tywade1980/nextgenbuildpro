# Telephony Agent Application

## Overview

This project implements an **Autonomous Call Screening and AI Receptionist** system for Android.  
The application leverages the `CallScreeningService` API and adopts the default dialer role to
autonomously screen incoming calls. Once a call is cleared, it is handed off to an on‑device
language model which provides real‑time customer service, schedules follow‑ups, and
integrates with existing business systems. Calls are recorded and transcribed locally,
allowing for immediate human intervention and continuous learning.  
To protect privacy and guarantee availability, all inference runs on the device using a
quantised Phi‑3 Mini model via ONNX Runtime.

## Technical Specifications

### Programming Languages

- **Kotlin 2.2.20** for application and build scripts—selected for its modern, concise and
  type‑safe nature, excellent Android tooling, and tight integration with the Telephony
  framework【792882792094335†L2055-L2073】.
- **C++ 17** for native AI processing—used to integrate ONNX Runtime and deliver
  high‑performance inference【792882792094335†L2074-L2079】.
- **Kotlin DSL** for Gradle build configuration.

### Platform Requirements

- **Minimum SDK:** 29 (Android 10) to implement `CallScreeningService` without requiring
  a full dialer【792882792094335†L2108-L2113】.
- **Target SDK:** 35 (Android 15) to ensure Play store compliance【792882792094335†L2108-L2113】.
- **Coroutines** are used throughout the app for asynchronous audio processing and AI
  inference【792882792094335†L2115-L2117】.

### Frameworks and Libraries

- **Android Telecom Framework** (API 29+) for core telephony operations, call
  connection and in‑call management【792882792094335†L2121-L2137】.
- **Jetpack Compose 1.7.6** for building reactive UIs【792882792094335†L2138-L2145】.
- **Android KTX 1.15.0** and **Lifecycle 2.8.7** for concise Kotlin extensions and
  lifecycle‑aware services【792882792094335†L2149-L2176】.
- **ONNX Runtime Mobile 1.20.1** and **ONNX Runtime GenAI 0.5.1** for executing the
  Phi‑3 Mini model on device【792882792094335†L2184-L2202】.
- **Phi‑3 Mini model (4 K context)**—a 3.8 billion parameter language model int4
  quantised to ~1.8 GB for deployment on client devices【792882792094335†L2207-L2221】.
- **Retrofit 2.11.0**, **OkHttp 4.12.0** and **Gson 2.11.0** for secure network
  communication and JSON serialization【792882792094335†L2287-L2304】.
- **Room 2.6.1** with SQLCipher for local persistence【792882792094335†L2317-L2323】.
- **Media3** (Exoplayer 1.5.0, Session 1.5.0) for audio capture and streaming【792882792094335†L2357-L2366】.
- **Google Cloud Speech 4.44.0** and **grpc-okhttp 1.68.1** for real‑time
  transcription【792882792094335†L2357-L2366】.
- **Deepgram API** (via Retrofit) for an alternative speech‑to‑text provider.
- **ElevenLabs API** for high‑quality text‑to‑speech responses.

### Open Source Dependencies

The system depends on a number of open source libraries to implement telephony,
AI inference, networking, database and real‑time audio. Important dependencies
include Android telephony libraries (`androidx.core:core-telecom`), lifecycle
services, WorkManager, biometric authentication, audio codecs (Opus, FLAC,
Zstd) and concurrency via Kotlin Coroutines【792882792094335†L2342-L2367】.  
The complete dependency list can be found in `app/build.gradle.kts`.

### Third‑Party Services

This application is designed to integrate with various external services:

- **CRM** platforms (Salesforce, HubSpot, Microsoft Dynamics) for
  customer data synchronization【792882792094335†L2388-L2444】.
- **Project management** tools (Asana) for automatic task creation【792882792094335†L2388-L2444】.
- **Communication** tools (Slack, Microsoft Teams) for sending team
  notifications【792882792094335†L2388-L2444】.
- **Speech and AI** services for transcription and fallback processing when
  offline models are unavailable【792882792094335†L2445-L2474】.
- **Monitoring** via Firebase Crashlytics and custom analytics【792882792094335†L2490-L2513】.

## Project Structure

```
telephony_agent_app/
├── settings.gradle.kts       — Project settings and repository configuration.
├── build.gradle.kts          — Top‑level Gradle configuration.
└── app/
    ├── build.gradle.kts      — Module configuration and dependency declarations.
    └── src/main/
        ├── AndroidManifest.xml   — Declares services, activities and permissions.
        ├── java/com/example/telephonyagent/
        │   ├── MainActivity.kt      — Entry point and Compose UI scaffold.
        │   ├── CallScreener.kt      — Implements `CallScreeningService` to filter incoming calls.
        │   ├── ReceptionInCallService.kt — Implements `InCallService`; hands off calls to AI.
        │   └── AiProcessor.kt       — Wrapper around ONNX Runtime to run the Phi‑3 model.
        │   ├── PreferencesManager.kt — Stores user preferences such as call screening thresholds.
        │   ├── SettingsScreen.kt    — Compose screen that allows users to adjust call screening sensitivity.
        │   ├── CallHistoryScreen.kt — Placeholder screen displaying recent call history.
        ├── res/                     — UI resources (themes, layouts, values).
        └── assets/models/           — Place Phi‑3 model and tokenizer files here.
```

## Setup and Build

1. **Install the Android SDK and NDK** for API 35 using Android Studio or the
   command line. Accept all required licenses.
2. **Clone this repository** and place your downloaded **Phi‑3 Mini** model and
   **GenAI tokenizer** files into `app/src/main/assets/models/`. You can
   download the model using the HuggingFace CLI:

   ```sh
   huggingface-cli download microsoft/Phi-3-mini-4k-instruct-onnx \
     --include cpu_and_mobile/cpu-int4-rtn-block-32-acc-level-4/*
   ```【792882792094335†L2334-L2340】

3. **Obtain API keys** for Deepgram (speech‑to‑text) and ElevenLabs
   (text‑to‑speech). Store them securely in a `local.properties` file or an
   encrypted keystore. The code includes Retrofit interfaces for these services.
4. **Build and run** on a device running Android 10 or higher:

   ```sh
   ./gradlew clean installDebug
   ```

   Make sure the device has telephony capabilities and that it is set as the
   default dialer or granted the `ROLE_DIALER` role through Settings.

## Security and Privacy

- The application operates **entirely on‑device** by default. It falls back to
  cloud transcription only when user‑downloaded models are unavailable.
- All call recordings and transcripts are stored locally using an encrypted
  Room database (SQLCipher) to protect sensitive information【792882792094335†L2317-L2323】.
- Telephony permissions—including `CALL_SCREENING`, `READ_CALL_LOG`,
  `ANSWER_PHONE_CALLS`, `RECORD_AUDIO` and `FOREGROUND_SERVICE`—are
  declared in the manifest and requested at runtime.

## Contributing

This repository contains a minimal MVP scaffold. Contributions are welcome to:
- Implement full AI call handling using the Phi‑3 model and GenAI tokenizer.
- Integrate CRM and project management APIs.
- Improve the user interface and call controls.
- Extend analytics and monitoring capabilities.

## License

This project is licensed under the MIT License. See `LICENSE` for full details.