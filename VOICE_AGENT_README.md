# Voice Agent with Full LLM and Web Tools Integration

## Overview

The Enhanced Voice Command Agent provides natural language processing capabilities with full access to web search, scraping, data harvesting, and API integration tools through OpenRouter's multi-LLM system.

## Features

### 🎤 Natural Language Understanding
- **LLM-Powered Intent Recognition**: Uses OpenRouter LLMs (GPT-4, Claude, etc.) to understand complex user requests
- **Multi-Language Support**: Spanish and English voice commands
- **Context Awareness**: Maintains conversation history for coherent multi-turn interactions
- **Dynamic Planning**: Automatically determines required tools and execution steps

### 🔍 Web Tools
The agent has access to comprehensive web capabilities:

#### 1. Web Search
```kotlin
// Automatically triggered by queries like:
// "search for construction material prices"
// "buscar precios de materiales"
// "find information about OSHA regulations"
```
- Uses DuckDuckGo API for privacy-friendly searches
- Returns ranked results with snippets
- Supports construction-specific queries

#### 2. Web Scraping
```kotlin
// Triggered by requests like:
// "get content from this website: https://..."
// "extract information from the page"
```
- Parses HTML content
- Extracts text, links, images, and metadata
- Handles relative URLs and redirects

#### 3. Data Harvesting
```kotlin
// For requests like:
// "compare prices from these suppliers: url1, url2, url3"
// "aggregate data from multiple sources"
```
- Collects data from multiple URLs
- Consolidates information
- Handles partial failures gracefully

#### 4. API Client
```kotlin
// For API interactions:
// "call this API endpoint"
// "make a POST request with this data"
```
- Supports REST and GraphQL
- Handles authentication
- Manages headers and request bodies

### 🤖 Multi-LLM System
Uses different models for different task types:

- **Reasoning Tasks**: `openai/o1-preview` for complex planning and analysis
- **Agent Coordination**: `anthropic/claude-3-opus` for orchestration
- **Fast Queries**: `openai/gpt-3.5-turbo` for simple requests
- **Code Generation**: `anthropic/claude-3-sonnet` for technical tasks

## Architecture

```
User Voice Input
    ↓
VoiceCommandAgent (Entry Point)
    ↓
    ├─ Simple requests → Pattern Matching (fast, no LLM)
    └─ Complex requests → EnhancedVoiceCommandAgent
        ↓
        ├─ LLM Planning (OpenRouter)
        ├─ Tool Selection (WebTools)
        ├─ Execution (Multi-step)
        └─ Response Generation (LLM)
```

## Usage Examples

### Simple Voice Commands (Pattern Matching)
```kotlin
val agent = VoiceCommandAgent(llmService = null)
agent.initialize()

val task = NextGenTask(
    description = "create project new house",
    priority = Priority.MEDIUM,
    metadata = mapOf("voice_input" to "create project new house")
)

val result = agent.processTask(task)
// Fast response using pattern matching
```

### Complex Web-Based Requests (LLM + Tools)
```kotlin
val agent = VoiceCommandAgent(llmService = openRouterService)
agent.initialize()

val task = NextGenTask(
    description = "search for current lumber prices and compare with last month",
    priority = Priority.MEDIUM,
    metadata = mapOf(
        "voice_input" to "search for current lumber prices and compare with last month"
    )
)

val result = agent.processTask(task)
// Uses LLM to plan, web search to gather data, and generates comprehensive response
```

### Multi-Step Data Harvesting
```kotlin
val task = NextGenTask(
    description = "compare HVAC equipment prices from these suppliers",
    priority = Priority.HIGH,
    metadata = mapOf(
        "voice_input" to "compare HVAC equipment prices from homedepot.com, lowes.com, and ferguson.com"
    )
)

val result = agent.processTask(task)
// Agent will:
// 1. Plan execution (identify need for web scraping)
// 2. Scrape each supplier website
// 3. Extract pricing data
// 4. Consolidate and compare
// 5. Generate summary response
```

## Configuration

### 1. OpenRouter API Key
Add to `local.properties`:
```properties
openrouter.api.key=sk-or-v1-your-key-here
```

### 2. Initialize with LLM Service
```kotlin
// In your application or orchestrator
val firestoreService = FirestoreService(context)
val openRouterService = OpenRouterService(firestoreService)
val voiceAgent = VoiceCommandAgent(llmService = openRouterService)

voiceAgent.initialize()
```

### 3. Add to Orchestrator
```kotlin
// In CEOPersonalAssistantOrchestrator
override val subAgents: List<SubAgent> = listOf(
    EnhancedVoiceCommandAgent(llmService)
)
```

## Tool Capabilities

### Available MCP Tools
```kotlin
val tools = WebTools.getAvailableTools()
// Returns:
// - web_search: Search the web
// - web_scrape: Extract content from pages
// - web_harvest: Aggregate from multiple sources
// - web_download: Download resources
// - web_monitor: Monitor for changes
// - api_client: Make API calls
```

### Tool Selection Logic
The agent automatically selects tools based on the user's request:

```kotlin
// Web search triggers
"search", "find", "look up", "get information", "buscar"

// Scraping triggers  
"extract", "get content", "scrape", "obtener contenido"

// Harvesting triggers
"compare", "aggregate", "collect from multiple", "comparar"

// API triggers
"call API", "make request", "POST", "GET"
```

## Integration with Existing System

### 1. Backward Compatible
The original `VoiceCommandAgent` remains functional with simple pattern matching. Enhanced capabilities are only used when:
- LLM service is configured
- Request matches complex patterns
- User explicitly requests web operations

### 2. MCP Integration
All web tools are registered with the MCP server:
```kotlin
MCPServer.getInstance().start() // Automatically registers web tools
```

### 3. Multi-Agent Coordination
The enhanced agent can coordinate with other department agents:
```kotlin
// Example: Voice command triggers CFO for pricing
"search for material costs and create estimate"
// → VoiceAgent searches web
// → Passes data to CFO Financial Orchestrator
// → CFO creates estimate
```

## Performance

### Response Times
- **Pattern Matching**: < 100ms (no LLM)
- **LLM Planning**: 1-3 seconds
- **Web Search**: 2-5 seconds
- **Web Scraping**: 3-10 seconds per page
- **Multi-source Harvesting**: 5-30 seconds

### Cost Optimization
- Uses cheaper models (GPT-3.5-turbo) for simple queries
- Caches common responses
- Limits web operations to necessary data
- Incremental processing for large harvests

## Security

### API Key Management
- Keys stored in `local.properties` (not committed)
- Accessed via `ApiKeyManager`
- Encrypted in memory when possible

### Web Operations
- Rate limiting on requests
- User-Agent identification
- Timeout protection
- Error handling for malicious sites

### Data Privacy
- Uses privacy-friendly search (DuckDuckGo)
- No tracking or analytics
- Conversation history stored locally or in private Firestore

## Testing

### Unit Tests
```bash
./gradlew test
# Tests located in:
# app/src/test/java/com/nextgenbuildpro/agents/personal_assistant/
```

### Integration Tests
```kotlin
@Test
fun testWebSearchIntegration() {
    val agent = EnhancedVoiceCommandAgent(llmService)
    val task = NextGenTask(
        description = "search for construction safety guidelines",
        priority = Priority.MEDIUM
    )
    
    val result = agent.processTask(task)
    assertTrue(result.isSuccess)
}
```

### Manual Testing
```kotlin
// In MainActivity or debug activity
val agent = VoiceCommandAgent(openRouterService)
agent.initialize()

// Test various requests
val testRequests = listOf(
    "search for lumber prices",
    "scrape this website: https://example.com",
    "compare prices from multiple suppliers",
    "create new project residential house"
)

testRequests.forEach { request ->
    val task = NextGenTask(
        description = request,
        metadata = mapOf("voice_input" to request)
    )
    val result = agent.processTask(task)
    Log.d("Test", "Result: ${result.getOrNull()?.result}")
}
```

## Troubleshooting

### "OpenRouter API key not configured"
**Solution**: Add key to `local.properties`

### "Search failed: timeout"
**Solution**: 
- Check internet connection
- Increase timeout in WebTools
- Try simpler query

### "Failed to parse execution plan"
**Solution**:
- LLM returned invalid JSON
- Agent falls back to pattern matching
- Check OpenRouter service status

### Agent always uses pattern matching
**Cause**: LLM service not provided or request not complex enough

**Solution**:
```kotlin
// Ensure LLM service is provided
val agent = VoiceCommandAgent(llmService = openRouterService)

// Or use enhanced agent directly
val enhancedAgent = EnhancedVoiceCommandAgent(openRouterService)
```

## Future Enhancements

### Planned Features
- [ ] **Advanced Scraping**: JavaScript rendering, form submission
- [ ] **Data Export**: CSV, JSON, PDF generation
- [ ] **Scheduled Monitoring**: Periodic checks with notifications
- [ ] **Custom Search Engines**: Google Custom Search, Bing API
- [ ] **Vision Tools**: Image recognition, diagram parsing
- [ ] **Audio Processing**: Transcription, analysis
- [ ] **Video Tools**: YouTube transcription, video analysis
- [ ] **Document Processing**: PDF parsing, OCR

### Extensibility
Add new tools to `WebTools`:
```kotlin
object WebTools {
    fun getAvailableTools(): List<MCPTool> = listOf(
        // Existing tools...
        MCPTool(
            toolId = "custom_tool",
            toolName = "Custom Tool",
            description = "Your custom capability",
            capabilities = listOf("capability1", "capability2")
        )
    )
    
    suspend fun executeCustomTool(params: Map<String, Any>): Result<CustomResult> {
        // Implementation
    }
}
```

## Support

### Documentation
- Main README: `/README.md`
- OpenRouter Guide: `/OPENROUTER_QUICKSTART.md`
- MCP Configuration: `/MCP_CONFIG_README.md`

### Code Location
- **WebTools**: `app/src/main/java/com/nextgenbuildpro/mcp/tools/WebTools.kt`
- **EnhancedAgent**: `app/src/main/java/com/nextgenbuildpro/agents/personal_assistant/EnhancedVoiceCommandAgent.kt`
- **VoiceCommandAgent**: `app/src/main/java/com/nextgenbuildpro/agents/personal_assistant/VoiceCommandAgent.kt`

### Contributing
1. Add new tools to `WebTools` object
2. Update `getAvailableTools()` list
3. Add execution method for tool
4. Document capabilities in this README
5. Add tests for new functionality

## License

Part of NextGen BuildPro - Construction Management AI System
