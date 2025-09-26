# NextGen BuildPro - AI OS Copilot Instructions

This document provides essential information for GitHub Copilot coding agents working with the NextGen BuildPro repository. Following these instructions will reduce build failures, minimize exploration time, and improve code quality.

## Repository Overview

**NextGen BuildPro** is an Android application built in Kotlin that implements a Multi-Agent AI Operating System for the construction industry. The project combines specialized AI agents with human intelligence to create an adaptive construction management platform.

### Key Specifications
- **Platform**: Android (Jetpack Compose UI), Kotlin 2.0.21, Gradle 8.11.1
- **Target SDK**: API 34 (Android 14), **Min SDK**: API 26
- **Repository Size**: 197 Kotlin files across multiple modules
- **Architecture**: Multi-Agent AI System with 5 core agents
- **Frontend**: TypeScript/React Native hybrid with npm scripts

### Core AI Agents
1. **MRM (Master Resource Manager)** - Resource allocation and optimization
2. **HermesBrain** - Communication hub and coordination
3. **BigDaddyAgent** - Executive decision-making and strategic oversight
4. **HRM Model** - Human resource management
5. **EliteHuman** - Human excellence and creativity amplification

## Build and Development

### Prerequisites (ALWAYS Required)
- Android Studio Arctic Fox (2020.3.1) or later
- JDK 17 or later, Android SDK with API level 34, Kotlin 2.0.21+
- Node.js 16+ and npm for frontend development

### Build Process

**In Full Development Environment**:
```bash
./gradlew clean && ./gradlew build
```

**CRITICAL ISSUE**: Plugin not found errors for `org.jetbrains.kotlin.plugin.compose:2.0.21`
- **Cause**: Kotlin Compose Plugin requires proper Android SDK setup and compatible versions
- **Solution**: Use Android Studio or ensure Android SDK is properly installed
- **Sandbox Workaround**: Focus on Kotlin code analysis/modification without compilation

### Frontend Development

**Setup Requirements**:
```bash
npm install  # Install all dependencies first
```

**TypeScript/React Native Build**: 
```bash
npm run build    # Compile TypeScript
npm run dev      # Watch mode for development
npm run lint     # ESLint validation
npm run test     # Jest testing
```

**Catalogue Management**:
```bash
npm run seed:catalogue    # Seed construction catalogue
npm run seed:verify      # Verify seeded data
npm run seed:demo        # Run seeding demo
```

**Note**: Frontend dependencies need to be installed with `npm install` before running scripts.

### Testing Strategy

**Unit Tests**: `./gradlew test` (3 proper test files in `app/src/test/`)
- `LLMServiceTest.kt` - LLM service functionality  
- `CatalogueSeederTest.kt` - Catalogue seeding system
- `ExampleUnitTest.kt` - Basic unit test template

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
├── env/LivingEnv.kt          # Living Environment Mesh
├── bms/                       # Building Management System
├── clientengagement/          # Client engagement features
├── debug/                     # Debug utilities and monitoring
├── features/                  # Feature-specific modules
├── navigation/                # Navigation components
├── receptionist/              # AI receptionist service
├── service/                   # Service layer components
├── timeclock/                 # Time tracking functionality
└── util/                      # Utility functions
```

### Frontend Structure
```
├── EstimateEditor.js          # Construction estimate editor
├── package.json               # npm dependencies and scripts
├── tsconfig.json              # TypeScript configuration
├── jest.config.js             # Jest testing configuration
├── seeds/                     # Database seeding scripts
└── examples/                  # Code examples and demos
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
interface LearningAgent : NextGenAgent {
    suspend fun learn(data: LearningData): Result<Unit>
    suspend fun getKnowledgeBase(): Map<String, Any>
    suspend fun updateModel(parameters: Map<String, Any>): Result<Unit>
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
    suspend fun stop(): Result<Unit>
    suspend fun restart(): Result<Unit>
    suspend fun getHealthStatus(): ServiceHealth
}
```

### Critical Dependencies
- **Jetpack Compose** - UI toolkit
- **Firebase** - Backend (Firestore, Storage, Analytics)  
- **Kotlin Coroutines** - Asynchronous programming
- **Material Design 3** - UI components
- **React Native** - Hybrid mobile framework for cross-platform features
- **TypeScript** - Type-safe JavaScript for frontend development

## Code Style and Conventions

### Kotlin Conventions
- Use Kotlin coroutines for asynchronous operations
- Follow Material Design 3 patterns for UI components
- Implement `StateFlow` for reactive state management
- Use `Result<T>` for error handling in service methods
- Maintain consistent naming: Services end with `Service`, Agents end with `Agent`

### File Organization
- Place shared interfaces in `shared/Types.kt`
- Group related functionality in feature modules
- Keep test files in `app/src/test/` for unit tests
- Use `app/src/main/.../test/` for integration tests

### Error Handling
- Use `Result<T>` pattern for methods that can fail
- Log errors with appropriate tags: `Log.e(TAG, "message", exception)`
- Provide meaningful error messages for user-facing errors

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
- **Kotlin version conflicts**: Project uses Kotlin 2.0.21, ensure compatibility

### Frontend Issues
- **TypeScript compilation**: Run `npm run build` to check for type errors
- **Linting**: Use `npm run lint` to check code style
- **Testing**: Run `npm run test` for Jest testing
- **Dependencies**: Run `npm install` first - all dependencies need to be installed
- **Missing packages**: Check `package.json` for required TypeScript types and tools

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