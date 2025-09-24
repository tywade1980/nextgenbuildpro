# NextGen BuildPro - AI OS Copilot Instructions

This document provides essential information for GitHub Copilot coding agents working with the NextGen BuildPro repository. Following these instructions will reduce build failures, minimize exploration time, and improve code quality.

## Repository Overview

**NextGen BuildPro** is an Android application built in Kotlin that implements a Multi-Agent AI Operating System for the construction industry. The project combines specialized AI agents with human intelligence to create an adaptive construction management platform.

### Key Specifications
- **Platform**: Android (Jetpack Compose UI), Kotlin 1.9.20, Gradle 8.11.1
- **Target SDK**: API 34 (Android 14), **Min SDK**: API 26
- **Repository Size**: 187 Kotlin files across multiple modules
- **Architecture**: Multi-Agent AI System with 5 core agents

### Core AI Agents
1. **MRM (Master Resource Manager)** - Resource allocation and optimization
2. **HermesBrain** - Communication hub and coordination
3. **BigDaddyAgent** - Executive decision-making and strategic oversight
4. **HRM Model** - Human resource management
5. **EliteHuman** - Human excellence and creativity amplification

## Build and Development

### Prerequisites (ALWAYS Required)
- Android Studio Arctic Fox (2020.3.1) or later
- JDK 17 or later, Android SDK with API level 34, Kotlin 1.9.20+

### Build Process

**In Full Development Environment**:
```bash
./gradlew clean && ./gradlew build
```

**CRITICAL ISSUE**: Plugin not found errors for `com.android.application:8.1.4`
- **Cause**: Android Gradle Plugin requires proper Android SDK setup
- **Solution**: Use Android Studio or ensure Android SDK is properly installed
- **Sandbox Workaround**: Focus on Kotlin code analysis/modification without compilation

### Testing Strategy

**Unit Tests**: `./gradlew test` (limited coverage - only 4 test files in proper test directories)

**AI Agent Tests**: Located in `app/src/main/java/com/nextgenbuildpro/pm/test/`
- `AssemblyCatalogueIntegrationTest.kt` - Assembly catalog functionality  
- `HierarchicalCatalogueTest.kt` - Project hierarchy system
- `AutomationDebugger.kt` - Debug utility for automation testing

**Note**: Main testing logic is embedded in main source, requires Firebase config to run.

## Project Architecture

### Directory Structure
```
app/src/main/java/com/nextgenbuildpro/
├── agents/                     # Core AI agents (MRM, HermesBrain, BigDaddy, etc.)
├── apps/                      # Main applications (CallScreen, Dialer, Construction)
├── core/                      # MainOrchestrator, services, API integrations
├── crm/                       # CRM module with AI assistant
├── pm/                        # Project management and test utilities
├── ui/                        # UI components and screens
├── shared/                    # Shared types and utilities
└── env/LivingEnv.kt          # Living Environment Mesh
```

### Key Files
- **MainActivity.kt** - Entry point, initializes ModuleManager and automation
- **MainOrchestrator.kt** - Central orchestration engine
- **BigDaddyAgent.kt** - Example AI agent implementation
- **google-services.json** - Firebase configuration (required)
- **buildgradle.txt** - Legacy build configuration reference

## Key Development Patterns

### AI Agent Implementation
All agents implement `LearningAgent` interface from `shared/Types.kt`:
```kotlin
interface LearningAgent {
    val agentType: AgentType
    val capabilities: List<AgentCapability>
    val status: StateFlow<SystemStatus>
    suspend fun processRequest(request: AgentRequest): AgentResponse
}
```

**Pattern**: Use `MutableStateFlow` for state, `Mutex` for thread safety, maintain knowledge bases

### Service Architecture
Services implement `NextGenService` interface:
```kotlin
interface NextGenService {
    val serviceName: String
    val isRunning: StateFlow<Boolean>
    suspend fun start(): Result<Unit>
    suspend fun getHealthStatus(): ServiceHealth
}
```

### Critical Dependencies
- **Jetpack Compose** - UI toolkit
- **Firebase** - Backend (Firestore, Storage, Analytics)  
- **Kotlin Coroutines** - Asynchronous programming
- **Material Design 3** - UI components

## Common Workflows

### Making Changes to AI Agents
1. Modify agent in `agents/` directory
2. Update tests in `pm/test/` if applicable
3. Test through `AutomationDebugger.kt`
4. Verify integration with `MainOrchestrator.kt`

### Adding New Features
1. Implement data layer (repositories, models)
2. Add service layer logic
3. Create UI components (follow Material Design 3)
4. Add to navigation structure
5. Use existing design system components

## Troubleshooting

### Gradle/Build Issues
- **Plugin errors**: Requires Android SDK - use Android Studio environment
- **Firebase errors**: Verify `google-services.json` in `app/` directory  
- **Memory issues**: Project uses optimized Gradle settings in `gradle.properties`
- **Clean builds**: Use `./gradlew clean --refresh-dependencies`

### Development Environment
- **Android Studio**: Recommended for full development
- **Sandbox/CI**: Focus on Kotlin source modification without compilation
- **Firebase**: Required for full functionality, check project configuration

## Trust These Instructions

**IMPORTANT**: This information has been verified against the actual codebase. Only explore further if:
- Instructions are incomplete for your specific task
- You encounter errors not covered in troubleshooting
- You need specific implementation details not documented

This instruction set minimizes exploration time and maximizes productivity within the NextGen BuildPro AI architecture.