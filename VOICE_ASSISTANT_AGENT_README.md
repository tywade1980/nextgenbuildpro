# Voice Assistant Agent - Technical Documentation

## Overview

The **VoiceAssistantAgent** is a specialized SubAgent in the NextGen BuildPro AI architecture that combines voice interaction capabilities with external Large Language Model (LLM) integration. It serves as a voice-enabled interface for construction management tasks, allowing users to interact naturally using speech while leveraging the power of external AI models for intelligent responses.

## Architecture Position

### Agent Hierarchy
```
CEO Personal Assistant (Orchestrator)
└── VoiceAssistantAgent (SubAgent)
    ├── Voice Recognition (MCP Tool)
    ├── Voice Synthesis (MCP Tool)
    └── External LLM Query (MCP Tool)
```

### Key Properties
- **Agent Type**: `SubAgent`
- **Department Head**: `CEO_PERSONAL_ASSISTANT`
- **Specialization**: Voice-enabled AI assistance with external data retrieval
- **ML Model**: GPT-4 via OpenRouter (Agent Workflow model)

## Core Features

### 1. Voice Input (Speech-to-Text)
- **Technology**: Android SpeechRecognizer API
- **Capabilities**:
  - Real-time speech recognition
  - Construction vocabulary optimization
  - Bilingual support foundation (English/Spanish)
  - Noise filtering for construction sites
  - Partial result streaming

### 2. Voice Output (Text-to-Speech)
- **Technology**: Android TextToSpeech API
- **Capabilities**:
  - Natural voice synthesis
  - Customizable speech parameters
  - Queue management for responses
  - Utterance progress tracking

### 3. External LLM Integration
- **Provider**: OpenRouter (Multi-LLM Gateway)
- **Supported Models**:
  - GPT-4 (Agent Workflow)
  - Claude-3 Opus (Agent Workflow)
  - GPT-3.5 Turbo (Fast Inference)
  - o1-preview (Complex Reasoning)
  - Meta Llama (Cost-Effective)

### 4. Conversation Management
- **Context Retention**: Maintains up to 10 message pairs
- **Conversation ID Tracking**: Persistent session management
- **History Storage**: Firebase integration for conversation logging
- **State Management**: Real-time conversation state tracking

## API Integration

### OpenRouter Configuration
```kotlin
APIIntegration(
    apiId = "openrouter_api",
    apiName = "OpenRouter API",
    endpoint = "https://openrouter.ai/api/v1",
    authType = APIAuthType.API_KEY,
    rateLimits = APIRateLimits(
        requestsPerMinute = 60,
        requestsPerDay = 10000
    )
)
```

### Authentication
- **Method**: Bearer Token (API Key)
- **Configuration**: Via `local.properties` file
- **Key**: `openrouter.api.key`

## Task Types

The VoiceAssistantAgent processes three main task types:

### 1. Voice Query
**Purpose**: Natural language questions requiring LLM intelligence

**Example**:
```kotlin
NextGenTask(
    title = "Project Planning Query",
    description = "What should I do first for a new construction project?",
    type = "voice_query",
    parameters = mapOf("use_llm" to true)
)
```

**Response**: Intelligent answer from external LLM with construction expertise

### 2. Voice Command
**Purpose**: Actionable commands for system operations

**Example**:
```kotlin
NextGenTask(
    title = "Schedule Safety Inspection",
    description = "Schedule a safety inspection for tomorrow",
    type = "voice_command",
    parameters = mapOf(
        "voice_input" to "Schedule safety inspection tomorrow morning",
        "use_llm" to true
    )
)
```

**Response**: Confirmation of action with details

### 3. External Data Retrieval
**Purpose**: Fetch information from external sources via LLM

**Example**:
```kotlin
NextGenTask(
    title = "Material Price Query",
    description = "Get current lumber prices",
    type = "llm_data_retrieval",
    parameters = mapOf("query" to "What are current lumber prices?")
)
```

**Response**: Current data retrieved and formatted for internal use

## Conversation States

The agent maintains the following states:

| State | Description | User Action |
|-------|-------------|-------------|
| `Idle` | Agent not initialized | Initialize agent |
| `Ready` | Listening for input | Start voice command |
| `Listening` | Recording voice input | Speak command |
| `Processing` | Analyzing input with LLM | Wait for response |
| `Speaking` | Outputting voice response | Listen to response |
| `Error` | Error occurred | Retry or check logs |

## Usage Examples

### Initialize the Agent
```kotlin
val voiceAssistant = VoiceAssistantAgent(
    context = applicationContext,
    llmService = openRouterService
)

val initResult = voiceAssistant.initialize()
if (initResult.isSuccess) {
    println("Voice Assistant ready")
}
```

### Start Voice Interaction
```kotlin
// Start listening
voiceAssistant.startListening()

// Voice input is automatically processed when user stops speaking
// Response is spoken back automatically
```

### Programmatic Query
```kotlin
val task = NextGenTask(
    title = "Cost Estimate",
    description = "Estimate cost for 2000 sq ft deck",
    type = "voice_query",
    assignedAgent = AgentType.SUB_AGENT,
    priority = Priority.HIGH,
    parameters = mapOf(
        "voice_input" to "What's the estimated cost for a 2000 square foot deck?",
        "use_llm" to true
    )
)

val result = voiceAssistant.executeSpecializedTask(task)
result.onSuccess { completedTask ->
    val response = completedTask.result?.get("response")
    println("Response: $response")
}
```

### Stop Listening
```kotlin
voiceAssistant.stopListening()
```

### Shutdown Agent
```kotlin
voiceAssistant.shutdown()
```

## System Prompt

The agent uses a construction-specific system prompt:

```
You are an intelligent construction management assistant integrated into NextGen BuildPro.

Your role is to help construction professionals with:
- Project management and scheduling
- Cost estimation and budgeting
- Safety protocols and compliance
- Resource allocation and tracking
- Client communication
- Document management

Provide clear, concise, and actionable responses. Always prioritize safety and compliance.
When discussing costs, be detailed and transparent. When scheduling, consider dependencies and weather.

You are speaking to a user via voice interface, so keep responses conversational and easy to understand.
```

## MCP Tools

### 1. Voice Recognition Tool
- **Capabilities**: 
  - Speech recognition
  - Language detection
  - Noise filtering
  
### 2. Voice Synthesis Tool
- **Capabilities**:
  - Speech synthesis
  - Voice customization
  - Audio playback

### 3. External LLM Query Tool
- **Capabilities**:
  - LLM query execution
  - Context management
  - Response processing

## Error Handling

### Speech Recognition Errors
```kotlin
when (error) {
    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
    SpeechRecognizer.ERROR_NETWORK -> "Network error"
    SpeechRecognizer.ERROR_NO_MATCH -> "No speech match"
    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
    // ... other error types
}
```

### LLM API Errors
- **Network failures**: Graceful fallback with informative message
- **API rate limits**: Automatic retry with exponential backoff
- **Invalid responses**: Local fallback processing

## Performance Considerations

### Response Times
- **Local commands**: < 100ms
- **LLM queries**: 2-5 seconds (network dependent)
- **Voice recognition**: Real-time streaming

### Token Usage Optimization
- Context window management (last 10 messages)
- Appropriate model selection based on query complexity
- Conversation history pruning

### Resource Management
- TTS engine initialization on startup
- SpeechRecognizer cleanup on shutdown
- Coroutine lifecycle management
- Memory-efficient conversation storage

## Testing

### Unit Tests
Location: `app/src/test/java/com/nextgenbuildpro/agents/personal_assistant/VoiceAssistantAgentTest.kt`

**Test Coverage**:
- Agent initialization
- ML model configuration
- MCP tools setup
- API integration configuration
- Agent properties validation
- Conversation state management

### Run Tests
```bash
./gradlew :app:testDebugUnitTest --tests "*VoiceAssistantAgentTest*"
```

## Integration with Existing Systems

### OpenRouterService Integration
```kotlin
// VoiceAssistantAgent uses the existing LLMService interface
private suspend fun queryExternalLLM(input: String, parameters: Map<String, Any>): String {
    val context = LLMContext(
        conversationId = currentConversationId ?: UUID.randomUUID().toString(),
        systemPrompt = buildSystemPrompt(),
        previousMessages = conversationHistory.takeLast(MAX_CONVERSATION_HISTORY),
        metadata = parameters
    )
    
    val result = llmService.generateResponse(
        prompt = input,
        context = context,
        agentType = agentType
    )
    
    return result.getOrNull()?.content ?: "Error response"
}
```

### Firebase Integration
- Conversation history stored in Firestore
- Automatic logging of interactions
- Analytics tracking for usage patterns

## Security Considerations

### API Key Management
- API keys stored in `local.properties` (not version controlled)
- Runtime key validation before API calls
- Secure key retrieval via `ApiKeyManager`

### Data Privacy
- Voice data processed locally (not sent to external servers)
- LLM queries logged for audit purposes
- User consent required for external API calls

### Permission Requirements
```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.INTERNET" />
```

## Future Enhancements

### Planned Features
1. **Offline Mode**: Local LLM for offline voice commands
2. **Multi-Language**: Full Spanish language support
3. **Voice Customization**: Custom voice profiles
4. **Advanced Context**: Project-specific context awareness
5. **Emotion Detection**: Analyze voice tone for urgency
6. **Background Noise Cancellation**: Enhanced audio processing

### Integration Points
- Integration with other C-Suite orchestrators
- Direct task creation from voice commands
- Voice-activated safety protocols
- Real-time project status queries

## Troubleshooting

### Common Issues

**Issue**: "TTS not ready"
- **Cause**: TextToSpeech engine not initialized
- **Solution**: Ensure device has TTS engine installed

**Issue**: "Speech recognizer not available"
- **Cause**: Device doesn't support speech recognition
- **Solution**: Use text input fallback

**Issue**: "LLM query failed"
- **Cause**: Network connectivity or API key issues
- **Solution**: Check internet connection and API key configuration

### Logging
Enable debug logging:
```kotlin
Log.d("VoiceAssistantAgent", "Debug message")
```

### Debugging
Monitor conversation state:
```kotlin
voiceAssistant.conversationState.collect { state ->
    when (state) {
        is ConversationState.Error -> handleError(state.message)
        is ConversationState.Processing -> showLoading()
        // ... other states
    }
}
```

## License

Proprietary - NextGen BuildPro

## Support

For issues or questions:
1. Check logs in Android Studio
2. Review this documentation
3. Contact the development team
4. Submit an issue in the project repository

---

**Version**: 1.0.0  
**Last Updated**: 2025-11-01  
**Author**: NextGen BuildPro Development Team
