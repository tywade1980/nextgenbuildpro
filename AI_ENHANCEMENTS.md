# AI Enhancement Features - MLKit Gen AI, Gemini Nano & Web Search

## Overview

This document describes the newly integrated AI features for NextGen BuildPro, including on-device inference with Gemini Nano, cloud-based Gemini models, headless web searching, and enhanced MCP server capabilities.

---

## 1. Gemini AI Integration

### Features

- **Gemini Nano**: On-device inference (requires Google AICore)
- **Gemini Pro**: Cloud-based balanced performance
- **Gemini Pro Vision**: Cloud-based with vision capabilities
- **Gemini Ultra**: Most capable model for complex reasoning
- **Streaming Support**: Real-time token streaming
- **Multi-turn Conversations**: Context-aware chat history
- **Agent-specific Prompting**: Context enhancement per orchestrator type

### Setup

#### 1. Get Gemini API Key

Visit [Google AI Studio](https://aistudio.google.com/app/apikey) to obtain your API key.

#### 2. Add to local.properties

```properties
# Gemini API Key (for cloud inference)
gemini.api.key=YOUR_GEMINI_API_KEY
```

#### 3. Usage in Code

```kotlin
import com.nextgenbuildpro.ai.gemini.GeminiService

// Initialize service
val geminiService = GeminiService(context)
geminiService.initialize()

// Generate text with Gemini Pro
val result = geminiService.generateText(
    prompt = "Estimate labor hours for kitchen renovation",
    modelName = GeminiService.MODEL_PRO,
    agentType = AgentType.CFO_FINANCIAL_ORCHESTRATOR,
    temperature = 0.7f
)

// Generate streaming response
geminiService.generateTextStream(
    prompt = "List safety requirements for roofing",
    modelName = GeminiService.MODEL_PRO,
    agentType = AgentType.CSO_SAFETY_ORCHESTRATOR
).collect { chunk ->
    println("Token: $chunk")
}

// Multi-turn conversation
val history = listOf(
    "user" to "What's the average cost of drywall?",
    "model" to "Drywall typically costs $1.50-$3.00 per square foot installed."
)
val response = geminiService.generateTextWithHistory(
    history = history,
    newPrompt = "What about for a 2000 sq ft home?"
)
```

### Models Available

| Model | Type | Use Case | API Key Required |
|-------|------|----------|-----------------|
| gemini-nano | On-device | Privacy-sensitive, offline | No (requires AICore) |
| gemini-pro | Cloud | General purpose, balanced | Yes |
| gemini-pro-vision | Cloud | Image understanding | Yes |
| gemini-ultra | Cloud | Complex reasoning, analysis | Yes |

### Agent Context Enhancement

The GeminiService automatically enhances prompts based on agent type:

- **COO Operations**: Focuses on scheduling, resource allocation, field operations
- **CFO Financial**: Focuses on cost estimation, budgeting, financial analysis
- **CHRO Client/HR**: Focuses on customer satisfaction, team management
- **CTO Design**: Focuses on blueprints, technical specifications
- **CSO Safety**: Focuses on OSHA regulations, safety protocols

---

## 2. Headless Web Search Service

### Features

- **DuckDuckGo Integration**: Free, no API key required
- **Construction-specific Searches**: Optimized queries for construction industry
- **Price Extraction**: Automatic pricing data extraction from search results
- **Result Caching**: 24-hour cache to reduce API calls
- **Rate Limiting**: Built-in protection against excessive requests

### Setup

No API key required for DuckDuckGo. Service works out of the box.

### Usage

```kotlin
import com.nextgenbuildpro.ai.tools.HeadlessWebSearchService

val searchService = HeadlessWebSearchService()

// General web search
val result = searchService.search(
    query = "best practices for concrete pouring in cold weather",
    maxResults = 10
)

// Construction-specific search
val pricingResult = searchService.searchConstruction(
    materialOrTrade = "2x4 lumber",
    type = HeadlessWebSearchService.SearchType.PRICING,
    location = "California"
)

// Extract pricing from results
result.onSuccess { searchResult ->
    val prices = searchService.extractPricing(searchResult)
    prices.forEach { priceData ->
        println("Price: $${priceData.value} from ${priceData.source}")
        println("Confidence: ${priceData.confidence}")
    }
}

// Labor rate search
val laborResult = searchService.searchConstruction(
    materialOrTrade = "carpenter",
    type = HeadlessWebSearchService.SearchType.LABOR_RATE,
    location = "Texas"
)
```

### Search Types

- `PRICING`: Material price searches
- `LABOR_RATE`: Labor rate by trade and location
- `SPECIFICATIONS`: Technical specifications and standards
- `SAFETY`: Safety requirements and OSHA regulations

### Result Structure

```kotlin
data class SearchResult(
    val query: String,
    val results: List<SearchItem>,
    val timestamp: Long,
    val source: String // "DuckDuckGo", "SerpAPI", etc.
)

data class SearchItem(
    val title: String,
    val snippet: String,
    val url: String,
    val position: Int
)
```

---

## 3. MCP Server Enhancement

### Enhanced Capabilities

The MCP server now provides comprehensive web tools accessible to all AI agents:

#### Available Tools

1. **Web Search** (`web_search`)
   - General search, news, images, academic, construction-specific
   - Multi-engine support

2. **Web Scraper** (`web_scrape`)
   - HTML parsing, content extraction
   - Link and image extraction
   - Metadata extraction

3. **Data Harvester** (`web_harvest`)
   - Multi-source aggregation
   - Data consolidation
   - Format conversion

4. **Resource Downloader** (`web_download`)
   - File downloads
   - Document parsing
   - Batch operations

5. **Web Monitor** (`web_monitor`)
   - Change detection
   - Price monitoring
   - Availability alerts

6. **API Client** (`api_client`)
   - REST API calls
   - GraphQL queries
   - Authentication support

### Usage from Agents

```kotlin
import com.nextgenbuildpro.mcp.MCPServer
import com.nextgenbuildpro.mcp.tools.WebTools

// Get MCP server instance
val mcpServer = MCPServer.getInstance()

// Create connection
val connection = mcpServer.createConnection(agentId, agentType)

// Use web tools
val searchResults = WebTools.search(
    query = "construction cost database 2024",
    searchType = "general",
    maxResults = 10
)

// Scrape website
val scraped = WebTools.scrape(
    url = "https://example.com/pricing",
    extractionRules = mapOf(
        "price_selector" to ".price-class",
        "description_selector" to ".desc-class"
    )
)

// Make API request
val apiResponse = WebTools.apiRequest(
    endpoint = "https://api.example.com/data",
    method = "GET",
    headers = mapOf("Authorization" to "Bearer $token")
)
```

---

## 4. BYOK (Bring Your Own Key) Architecture

### Supported Services

The application supports BYOK for multiple AI and search services:

| Service | Key Property | Required | Purpose |
|---------|-------------|----------|---------|
| OpenRouter | `openrouter.api.key` | Yes | Multi-LLM access (GPT, Claude, etc.) |
| Gemini | `gemini.api.key` | Optional | On-device & cloud AI inference |
| Google | `google.api.key` | Optional | Maps, Custom Search |
| SerpAPI | `serpapi.key` | Optional | Premium web search |

### Configuration File

Add your API keys to `local.properties`:

```properties
# OpenRouter API Key (required for LLM functionality)
openrouter.api.key=sk-or-v1-YOUR_KEY

# Gemini API Key (optional - for on-device and cloud AI)
gemini.api.key=YOUR_GEMINI_KEY

# Google API Key (optional - for Maps and Custom Search)
google.api.key=YOUR_GOOGLE_KEY

# SerpAPI Key (optional - for premium search)
serpapi.key=YOUR_SERPAPI_KEY
```

### Accessing Keys in Code

```kotlin
import com.nextgenbuildpro.core.ApiKeyManager

// Initialize (done automatically in Application.onCreate)
ApiKeyManager.initialize(context)

// Get specific keys
val openRouterKey = ApiKeyManager.getOpenRouterApiKey()
val geminiKey = ApiKeyManager.getGeminiApiKey()
val googleKey = ApiKeyManager.getGoogleApiKey()

// Get custom key
val customKey = ApiKeyManager.getProperty("custom.service.key")
```

---

## 5. Integration Examples

### Example 1: Cost Database Agent with Web Search

```kotlin
class CostDatabaseAgent : SubAgent {
    private val geminiService = GeminiService(context)
    private val searchService = HeadlessWebSearchService()
    
    suspend fun getCurrentPricing(material: String): Result<PriceData> {
        // Search web for current pricing
        val searchResult = searchService.searchConstruction(
            materialOrTrade = material,
            type = HeadlessWebSearchService.SearchType.PRICING
        ).getOrNull() ?: return Result.failure(Exception("Search failed"))
        
        // Extract prices
        val prices = searchService.extractPricing(searchResult)
        
        // Use Gemini to analyze and validate prices
        val analysis = geminiService.generateText(
            prompt = """
                Analyze these prices for $material:
                ${prices.joinToString("\n") { "$${it.value} from ${it.source}" }}
                
                Provide:
                1. Average price
                2. Price range
                3. Regional variations
                4. Reliability assessment
            """.trimIndent(),
            modelName = GeminiService.MODEL_PRO,
            agentType = AgentType.CFO_FINANCIAL_ORCHESTRATOR
        ).getOrNull()
        
        // Return structured data
        return Result.success(PriceData(
            material = material,
            averagePrice = prices.map { it.value }.average(),
            sources = prices.map { it.source },
            aiAnalysis = analysis ?: ""
        ))
    }
}
```

### Example 2: Voice Assistant with Gemini Streaming

```kotlin
class VoiceAssistantAgent(
    private val context: Context,
    private val llmService: LLMService
) : SubAgent {
    private val geminiService = GeminiService(context)
    
    suspend fun processVoiceCommand(command: String): Flow<String> {
        // Use Gemini streaming for real-time responses
        return geminiService.generateTextStream(
            prompt = "Process this voice command for construction management: $command",
            modelName = GeminiService.MODEL_PRO,
            agentType = AgentType.CEO_PERSONAL_ASSISTANT
        )
    }
}
```

### Example 3: Safety Agent with Web Monitoring

```kotlin
class SafetyComplianceAgent : SubAgent {
    private val geminiService = GeminiService(context)
    
    suspend fun checkOSHACompliance(task: String): Result<ComplianceReport> {
        // Use Gemini to analyze safety requirements
        val safetyAnalysis = geminiService.generateText(
            prompt = """
                Analyze OSHA safety requirements for: $task
                
                Provide:
                1. Required PPE
                2. Safety protocols
                3. Training requirements
                4. Inspection checklist
            """.trimIndent(),
            modelName = GeminiService.MODEL_PRO,
            agentType = AgentType.CSO_SAFETY_ORCHESTRATOR
        ).getOrNull() ?: return Result.failure(Exception("Analysis failed"))
        
        // Search for latest OSHA regulations
        val searchService = HeadlessWebSearchService()
        val regulations = searchService.searchConstruction(
            materialOrTrade = task,
            type = HeadlessWebSearchService.SearchType.SAFETY
        )
        
        return Result.success(ComplianceReport(
            task = task,
            aiAnalysis = safetyAnalysis,
            latestRegulations = regulations.getOrNull()?.results ?: emptyList()
        ))
    }
}
```

---

## 6. Performance Considerations

### Caching

- **Search Results**: 24-hour cache for web searches
- **Gemini Models**: In-memory model cache
- **MCP Tools**: Result caching per tool

### Rate Limiting

- **DuckDuckGo**: No official limits, but use responsibly
- **Gemini API**: Per-key rate limits (check Google AI Studio)
- **OpenRouter**: Per-model rate limits

### Cost Optimization

1. **Use Gemini Nano** when possible (on-device, free)
2. **Cache aggressive**: Reduce API calls
3. **Prefer DuckDuckGo**: Free web search
4. **Model selection**: Use appropriate model for task
   - Nano: Simple queries, on-device
   - Pro: General purpose
   - Ultra: Complex reasoning only

---

## 7. Troubleshooting

### Gemini Nano Not Available

**Issue**: `isNanoAvailable()` returns false

**Solution**: 
- Ensure device has Google AICore installed
- Check Android version (AICore requires recent Android)
- Fall back to Gemini Pro (cloud)

### Web Search Returns Empty

**Issue**: Search returns no results

**Solution**:
- Check network connectivity
- Verify query formatting
- Try different search terms
- Check DuckDuckGo API status

### API Key Errors

**Issue**: "API key required for cloud models"

**Solution**:
- Verify key is set in `local.properties`
- Check key format (Gemini keys start with specific prefix)
- Ensure `ApiKeyManager.initialize()` was called
- Verify key has API access enabled

---

## 8. Future Enhancements

### Planned Features

1. **Google AI Studio Integration**: Direct API access
2. **SerpAPI Premium Search**: When API key provided
3. **Gemini Flash**: Fast, efficient model variant
4. **Vision Processing**: Image understanding with Gemini Pro Vision
5. **Multimodal Input**: Text + image prompts
6. **Fine-tuning**: Custom model training
7. **Batch Processing**: Bulk inference operations

### Contributing

To add new AI capabilities:

1. Extend `GeminiService` or create new service class
2. Add configuration to `ApiKeyManager`
3. Update `local.properties.template`
4. Add integration tests
5. Document in this file

---

## 9. References

- [Google AI Studio](https://aistudio.google.com/)
- [Gemini API Documentation](https://ai.google.dev/docs)
- [DuckDuckGo API](https://duckduckgo.com/api)
- [OpenRouter Documentation](https://openrouter.ai/docs)
- [MCP Protocol Specification](https://modelcontextprotocol.io/)

---

**Last Updated**: January 2025  
**Version**: 1.0  
**Maintainer**: NextGen BuildPro Team
