# NextGen BuildPro - AI OS Copilot Instructions

This document provides essential information for GitHub Copilot coding agents working with the NextGen BuildPro repository. Following these instructions will reduce build failures, minimize exploration time, and improve code quality.

## Repository Overview

**NextGen BuildPro** is an advanced Android application built in Kotlin that implements a Multi-Agent AI Operating System specifically designed for the construction industry. The project combines specialized AI agents with human intelligence to create an adaptive construction management platform.

### Project Specifications
- **Platform**: Android (Jetpack Compose UI)
- **Language**: Kotlin 1.9.20
- **Build System**: Gradle 8.11.1 with Kotlin DSL
- **Target SDK**: API 34 (Android 14)
- **Minimum SDK**: API 26 (Android 8.0)
- **JVM Target**: 1.8
- **Architecture**: Multi-Agent AI System with 5 core agents
- **Repository Size**: ~187 Kotlin files across multiple modules
- **Main Entry Point**: `MainActivity.kt` - Initializes ModuleManager and automation systems

### Core AI Agents Architecture
1. **MRM (Master Resource Manager)** - Resource allocation and system optimization
2. **HermesBrain** - Communication hub and coordination
3. **BigDaddyAgent** - Executive decision-making and strategic oversight
4. **HRM Model** - Human resource management and optimization
5. **EliteHuman** - Human excellence and creativity amplification

## Build and Development Instructions

### Prerequisites (ALWAYS Required)
- Android Studio Arctic Fox (2020.3.1) or later
- JDK 17 or later
- Android SDK with API level 34
- Kotlin 1.9.20 or later

### Build Process

**CRITICAL**: This is an Android project that requires Android SDK and proper development environment setup.

**In Full Development Environment**:
1. **Always start with clean**:
   ```bash
   ./gradlew clean
   ```

2. **Build the project** (requires Android SDK):
   ```bash
   ./gradlew build
   ```

3. **For release builds**:
   ```bash
   ./gradlew assembleRelease
   ```

**In Limited/Sandbox Environments**:
- Direct compilation may fail due to missing Android SDK
- Focus on Kotlin source code analysis and modification
- Use static analysis tools instead of compilation for validation
- Test logical changes without full build when SDK unavailable

### Common Build Issues and Solutions

**Issue 1**: Plugin not found errors for `com.android.application:com.android.application.gradle.plugin:8.1.4`
- **Cause**: Android Gradle Plugin version 8.1.4 may not be available in current repositories
- **Root Cause**: This is a known issue in sandbox/CI environments without proper Android SDK setup
- **Immediate Solution**: This error indicates the project requires a full Android development environment
- **Workaround**: In environments without Android SDK, focus on Kotlin code changes that don't require compilation
- **Full Solution**: Use Android Studio or ensure Android SDK is properly installed and configured

**Issue 2**: Kotlin compilation errors
- **Cause**: Version mismatch between Kotlin compiler and libraries
- **Solution**: Check that Kotlin version is 1.9.20 in both root and app build.gradle.kts

**Issue 3**: Compose compilation failures
- **Cause**: Compose compiler version mismatch
- **Solution**: Ensure kotlinCompilerExtensionVersion is "1.5.4" in app/build.gradle.kts

### Testing Strategy

**Unit Tests** (Standard Android test structure):
```bash
./gradlew test
```
- Located in `app/src/test/java/com/nextgenbuildpro/`
- Basic unit tests available (ExampleUnitTest.kt)
- Limited unit test coverage currently

**Integration Tests** (requires Android SDK):
```bash
./gradlew connectedAndroidTest
```
- Located in `app/src/androidTest/java/com/nextgenbuildpro/`

**AI Agent Specific Tests** (Custom test implementations):
- Located in `app/src/main/java/com/nextgenbuildpro/pm/test/`
- These are integration tests disguised as main source files
- Key test files:
  - `AssemblyCatalogueIntegrationTest.kt` - Tests assembly catalog functionality
  - `HierarchicalCatalogueTest.kt` - Tests project hierarchy system
  - `AutomationDebugger.kt` - Debug utility for automation testing

**Testing Notes**:
- Limited formal unit test coverage (only 4 test files in proper test directories)
- Main testing logic is embedded in the `pm/test/` directory within main source
- Tests require Firebase configuration and proper Android environment to run
- Integration tests in main source can be run programmatically through debug utilities

## Project Architecture and Layout

### Directory Structure
```
app/src/main/java/com/nextgenbuildpro/
├── agents/                     # Core AI agents
│   ├── MRM.kt                 # Master Resource Manager
│   ├── HermesBrain.kt         # Communication hub
│   ├── BigDaddyAgent.kt       # Executive intelligence
│   ├── HRMModel.kt            # Human resource management  
│   └── EliteHuman.kt          # Human excellence framework
├── apps/                      # Main applications
│   ├── CallScreenService.kt   # Call management
│   ├── DialerApp.kt          # Smart dialing
│   └── ConstructionPlatform.kt # Construction management
├── core/                      # Core services and orchestration
│   ├── MainOrchestrator.kt    # Central orchestration
│   ├── services/              # Core services
│   └── api/                   # API integrations
├── crm/                       # CRM module
│   ├── ai/CrmAIAssistant.kt   # CRM AI functionality
│   └── repository/            # CRM data layer
├── pm/                        # Project management
│   ├── data/                  # Data models and repositories
│   ├── services/              # PM services
│   └── test/                  # PM integration tests
├── ui/                        # UI components and screens
├── shared/                    # Shared types and utilities
└── env/LivingEnv.kt          # Living Environment Mesh
```

### Key Configuration Files
- `build.gradle.kts` (root) - Project-level Gradle configuration
- `app/build.gradle.kts` - App-level Gradle configuration  
- `gradle.properties` - Gradle optimization settings
- `google-services.json` - Firebase configuration (required)
- `buildgradle.txt` - Legacy build configuration reference

### Critical Dependencies
- **Jetpack Compose** - Modern Android UI toolkit
- **Firebase** - Backend services (Firestore, Storage, Analytics)
- **Kotlin Coroutines** - Asynchronous programming
- **Material Design 3** - UI components
- **Navigation Compose** - Navigation framework

## Key Development Patterns

### AI Agent Implementation
All AI agents implement the `LearningAgent` interface from `shared/Types.kt`:
```kotlin
interface LearningAgent {
    val agentType: AgentType
    val capabilities: List<AgentCapability>
    val status: StateFlow<SystemStatus>
    suspend fun processRequest(request: AgentRequest): AgentResponse
    suspend fun learnFromInteraction(interaction: String, outcome: String)
}
```

**Agent Implementation Example** (BigDaddyAgent pattern):
- Uses `MutableStateFlow` for reactive state management
- Implements `Mutex` for thread-safe operations
- Maintains internal knowledge bases and decision history
- Provides executive-level decision making capabilities

### Service Architecture
Core services implement `NextGenService` interface:
```kotlin
interface NextGenService {
    val serviceName: String
    val isRunning: StateFlow<Boolean>
    suspend fun start(): Result<Unit>
    suspend fun stop(): Result<Unit>
    suspend fun getHealthStatus(): ServiceHealth
}
```

### Module Management
- `ModuleManager.initialize(applicationContext)` in MainActivity bootstraps the system
- `AutomationDebugger` provides comprehensive testing capabilities
- Services are initialized asynchronously in background coroutines

### State Management
- Use `StateFlow` for reactive state management
- Implement proper coroutine scoping with `viewModelScope`
- Follow unidirectional data flow patterns
- Use `Mutex` for thread-safe operations in agents

## Common Workflows and Validation

### Making Changes to AI Agents
1. Modify agent implementation in `agents/` directory
2. Update corresponding tests in `pm/test/`
3. Run unit tests: `./gradlew test`
4. Test agent interactions through `AutomationDebugger.kt`
5. Verify integration with `MainOrchestrator.kt`

### UI Development
1. Create composables in appropriate `ui/` subdirectories
2. Follow Material Design 3 guidelines
3. Use existing design system components
4. Test on different screen sizes
5. Verify accessibility compliance

### Adding New Features
1. Create feature branch
2. Implement data layer first (repositories, models)
3. Add service layer logic
4. Create UI components
5. Add to navigation structure
6. Write integration tests
7. Update documentation

## Troubleshooting Guide

### Gradle Sync Issues
- Clear Gradle cache: `./gradlew clean --refresh-dependencies`
- Invalidate Android Studio caches and restart
- Check internet connectivity for dependency downloads
- Verify Android SDK installation and versions

### Firebase Configuration
- Ensure `google-services.json` is in `app/` directory
- Verify Firebase project configuration matches applicationId
- Check Firebase rules allow read/write access for development

### Agent Compilation Errors
- Verify all agents implement required interfaces
- Check coroutine dependency versions match
- Ensure proper error handling in agent interactions
- Use `AutomationDebugger` to test agent functionality

### Memory Issues
- The project has optimized Gradle settings in `gradle.properties`
- Use `-Xmx2048m` JVM args for large builds
- Enable Gradle daemon and build cache
- Use parallel execution for faster builds

## Trust These Instructions

**IMPORTANT**: The information in this document has been verified against the actual codebase. Only perform additional exploration if:
- The instructions are incomplete for your specific task
- You encounter errors not covered in the troubleshooting section
- You need to understand specific implementation details not documented here

This instruction set is designed to minimize the time you spend exploring the codebase and maximize your productivity in implementing features that align with the NextGen BuildPro AI architecture.