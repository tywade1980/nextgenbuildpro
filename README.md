# NextGen BuildPro - Construction Management Platform

> ⚠️ **DEVELOPMENT STATUS**: This project is currently in active development and is NOT production-ready. The application is a work in progress with many features still being implemented and debugged.

[![Platform](https://img.shields.io/badge/platform-android-green.svg)](https://developer.android.com)
[![Status](https://img.shields.io/badge/status-in%20development-yellow.svg)](#)
[![License](https://img.shields.io/badge/license-proprietary-red.svg)](#license)

## What This Project Is

NextGen BuildPro is an **Android construction management application** being developed to help construction professionals manage their business operations through a mobile platform.

### Core Purpose
This is a **construction CRM and project management tool** that aims to provide:
- Lead and client management
- Project estimation and bidding
- Project tracking and organization
- Calendar and scheduling
- Document and file management

## Current Implementation Status

### ✅ Implemented Features
- **Lead Management**: Basic CRUD screens for tracking potential clients
- **Estimate Editor**: Create and edit construction estimates with line items
- **Project Views**: List and detail screens for viewing projects
- **Calendar Integration**: Basic calendar UI for scheduling
- **Settings**: User preferences, permissions, notifications
- **Construction UI Theme**: Glove-friendly, high-contrast design optimized for outdoor use

### 🚧 Known Issues
- **Build Errors**: Application currently has compilation errors that prevent deployment
- **Backend Integration**: Firebase integration is incomplete
- **Data Persistence**: Not all CRUD operations are fully functional
- **Testing**: Limited test coverage and validation

### 📋 Planned Features
- AI-assisted cost estimation
- Voice command integration  
- Advanced analytics
- Multi-user collaboration
- Offline support

## Technical Architecture

### Technology Stack
- **Platform**: Android (API 28+)
- **Language**: Kotlin 2.0.21
- **UI Framework**: Jetpack Compose with Material Design 3
- **Backend**: Firebase (Firestore, Storage, Analytics)
- **Build System**: Gradle 8.13

### Project Structure
```
app/src/main/java/com/nextgenbuildpro/
├── features/          # Feature-specific UI screens
│   ├── home/         # Home dashboard
│   ├── leads/        # Lead management
│   ├── estimates/    # Estimate creation
│   ├── projects/     # Project management
│   ├── calendar/     # Scheduling
│   └── settings/     # App settings
├── ui/               # Shared UI components and theme
├── core/             # Core services and utilities
├── data/             # Data models and repositories
└── navigation/       # Navigation configuration
```

### Key Dependencies
- androidx.compose (UI toolkit)
- Firebase SDK (backend services)
- Kotlin Coroutines (async operations)
- Material Design 3 (UI components)

## Future Vision: AI Orchestration System

This project has long-term ambitions to incorporate AI agents for automating construction management tasks. The codebase includes experimental AI orchestrator classes that are not yet integrated into the main application:

- `orchestrators/` - Experimental AI agent coordination (not currently functional)
- `agents/` - Placeholder agent implementations
- `ai/llm/` - LLM service integrations (incomplete)

**Note**: These AI features are conceptual and not part of the current working application.

## Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 17 or later
- Android SDK with API level 35
- Kotlin 2.0.21+

### Building the Project

⚠️ **Current Build Status**: The project has compilation errors and requires fixes before it can be built successfully.

1. Clone the repository:
   ```bash
   git clone https://github.com/tywade1980/nextgenbuildpro.git
   cd nextgenbuildpro
   ```

2. Open in Android Studio

3. Sync Gradle files

4. Address compilation errors (see Issues section)

5. Build and run:
   ```bash
   ./gradlew assembleDebug
   ```

### Firebase Configuration
The app requires a `google-services.json` file in the `app/` directory for Firebase integration. This file contains your Firebase project configuration.

## Known Build Issues

The project currently has several compilation errors that prevent successful builds:
- Missing properties in data classes
- Unresolved references in orchestrator files
- Type mismatches in some service implementations
- Missing parameters in function calls

These issues are being actively addressed.

## Development Roadmap

**Phase 1 (Current)**: Fix critical build errors and establish stable foundation
**Phase 2**: Complete basic CRUD operations for all entities  
**Phase 3**: Implement full Firebase integration
**Phase 4**: Add offline support and data synchronization
**Phase 5**: Introduce AI-assisted features (experimental)

## Contributing

This is a personal project under active development. Contributions are not currently being accepted as the codebase is unstable.

## License

Proprietary - All rights reserved

## Contact

**Developer**: Tyler Wade
- **GitHub**: [@tywade1980](https://github.com/tywade1980)

---

**Disclaimer**: This is a development project. Features described as "implemented" may be partially complete or non-functional. Performance metrics, user counts, and feature completeness claims found in other documentation files are aspirational and do not reflect current reality.
