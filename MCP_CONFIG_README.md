# MCP Configuration for NextGen BuildPro

This document explains the Model Context Protocol (MCP) configuration file for the NextGen BuildPro repository.

## Overview

The `mcp-config.json` file provides structured metadata about the NextGen BuildPro codebase to help AI assistants and automated tools understand the project structure, key components, and development patterns.

## Key Features

### 1. Repository Structure Recognition
- **Hybrid Architecture**: Recognizes both Android/Kotlin and TypeScript/JavaScript components
- **Multi-Agent AI System**: Identifies the 5 core AI agents and their capabilities
- **Firebase Integration**: Maps backend services and configuration files

### 2. Intelligent File Prioritization
The configuration prioritizes important files for context:
- Core orchestration files (`MainOrchestrator.kt`, `Types.kt`)
- AI agent implementations (`agents/*.kt`)
- Data schema and services (`CatalogueSchema.ts`, `CatalogueDataService.ts`)
- Configuration files (`package.json`, `build.gradle.kts`)

### 3. Semantic Understanding
Categorizes files by their purpose:
- **Agent Files**: AI agent implementations with learning capabilities
- **Core Files**: System orchestration and services
- **Catalogue Files**: Construction project management components
- **Frontend Files**: TypeScript/JavaScript seeding and data logic

## Configuration Sections

### Repository Metadata
```json
{
  "type": "hybrid",
  "primaryLanguages": ["kotlin", "typescript", "javascript"],
  "framework": "android",
  "architecture": "multi-agent-ai",
  "domain": "construction-management"
}
```

### Context Window Management
- **Max Tokens**: 128,000 (optimized for large codebase analysis)
- **Priority Files**: 50 most important files get priority inclusion
- **Smart Exclusions**: Excludes build artifacts, dependencies, and temporary files

### Specialized Agent Recognition
Maps departmental orchestrators with their capabilities:
- **CRM Orchestrator**: Contact management and client engagement
- **Personal Assistant Orchestrator**: Voice command processing and task assistance
- **Project Management Orchestrator**: Project planning and resource coordination
- **Design Department Orchestrator**: Design workflow coordination
- **Analytics Orchestrator**: Data analysis and reporting

### Build System Integration
Supports both build systems:
- **Android**: Gradle 8.11.1 with Kotlin 1.9.20
- **Frontend**: npm with TypeScript 5.9.2

## Usage Examples

### For AI Code Assistants
The configuration helps AI understand:
- Which files are most important for context
- How the multi-agent system is structured
- Build and test commands for different components
- Integration patterns (Firebase, Android, TypeScript)

### For Development Tools
Tools can use this configuration to:
- Prioritize file indexing
- Understand project dependencies
- Map architectural relationships
- Generate appropriate documentation

### For Code Analysis
The semantic rules help identify:
- Agent coordination patterns
- Data flow between Android and frontend
- Firebase integration points
- Construction domain-specific logic

## File Patterns

### Included Files
- All Kotlin source files (`**/*.kt`)
- TypeScript/JavaScript files (`**/*.ts`, `**/*.js`)
- Configuration files (`**/*.json`, `**/*.gradle*`)
- Documentation (`**/*.md`)
- Android resources (`**/*.xml`)

### Excluded Files
- Build artifacts (`**/build/**`, `**/dist/**`)
- Dependencies (`**/node_modules/**`, `**/.gradle/**`)
- IDE files (`**/.idea/**`, `**/.kotlin/**`)
- Binary files (`**/*.apk`, `**/*.class`)
- Research and example directories

## Integration Points

### Firebase Configuration
```json
{
  "projectId": "nextgenbuildpro",
  "storageBucket": "gs://nextgenbuildpro.firebasestorage.app",
  "configFile": "google-services.json",
  "webConfig": "firebase.ts"
}
```

### AI System Architecture
```json
{
  "framework": "custom-multi-agent",
  "learningCapability": true,
  "orchestrated": true
}
```

## Contextual Hints

The configuration provides guidance for common modification scenarios:

- **Modifying Agents**: Consider impact on MainOrchestrator coordination
- **Updating Core**: Ensure compatibility with all AI agents
- **Changing Catalogue**: Update both Kotlin models and TypeScript schemas
- **Build Changes**: Test both Android gradle and npm builds

## Testing Integration

Maps test locations and commands:
- **Android Tests**: `app/src/test/`, `app/src/androidTest/`
- **Frontend Tests**: `tests/`
- **AI Agent Tests**: `app/src/main/java/com/nextgenbuildpro/pm/test/`
- **Seeding Verification**: `seeds/verifyCatalogue.ts`

## Best Practices

1. **Keep Configuration Updated**: Update when adding new agents or major architectural changes
2. **Maintain Priority Files**: Ensure the most important files remain in priority patterns
3. **Test Integration**: Verify build commands work after configuration changes
4. **Document Changes**: Update this README when modifying the configuration

## Version History

- **v1.0.0**: Initial MCP configuration for NextGen BuildPro
  - Multi-agent AI system recognition
  - Hybrid Android/TypeScript support
  - Firebase integration mapping
  - Construction domain semantics

## Support

For questions about this MCP configuration:
- Check the main README.md for project overview
- Review individual component documentation in relevant directories
- Consult the AI agent implementations in `app/src/main/java/com/nextgenbuildpro/agents/`