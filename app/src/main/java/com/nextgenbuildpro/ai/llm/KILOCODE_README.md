# Kilocode Integration - Large Context LLM Service

## Overview

Kilocode is integrated into NextGen BuildPro as a specialized LLM provider offering **1 million+ token context windows**. This enables comprehensive codebase analysis, system-wide refactoring, and architectural reviews that would be impossible with standard LLM services.

## Key Features

### 1. Large Context Window
- **1M+ tokens** vs 128K for GPT-4 or 200K for Claude
- Analyze entire codebases in a single request
- Maintain full system context for complex operations
- No need to split large files or repositories

### 2. Specialized Models

#### `kilocode-refactor-v1`
- **Purpose**: Code refactoring and optimization
- **Use Cases**: 
  - Large-scale code refactoring
  - Identifying code duplication across projects
  - Standardizing patterns across codebases
  - Optimizing complex systems

#### `kilocode-analysis-v1`
- **Purpose**: Comprehensive code review
- **Use Cases**:
  - Code quality assessment
  - Security vulnerability detection
  - Performance bottleneck identification
  - Best practices validation

#### `kilocode-architecture-v1`
- **Purpose**: System architecture planning
- **Use Cases**:
  - Architecture review and recommendations
  - Microservices decomposition planning
  - Dependency analysis
  - System design optimization

#### `kilocode-1m` (General)
- **Purpose**: General purpose with maximum context
- **Use Cases**:
  - Any task requiring large context
  - Multi-file code understanding
  - Complex question answering

## Integration Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    NextGen BuildPro                         │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────────────┐        ┌──────────────────┐         │
│  │   LLMService     │◄───────│  KilocodeService │         │
│  │   Interface      │        │    (1M context)  │         │
│  └──────────────────┘        └──────────────────┘         │
│           ▲                           │                     │
│           │                           │                     │
│           │                  ┌────────▼────────┐           │
│  ┌────────┴─────────┐        │ KilocodeClient  │           │
│  │ OpenRouterService│        │  (HTTP Client)  │           │
│  │   (Fallback)     │        └─────────────────┘           │
│  └──────────────────┘                 │                     │
│                                       │                     │
└───────────────────────────────────────┼─────────────────────┘
                                        │
                                        ▼
                          ┌──────────────────────────┐
                          │   Kilocode API Cloud     │
                          │  (1M+ Token Context)     │
                          └──────────────────────────┘
```

## Usage Examples

### Basic Response Generation

```kotlin
val kilocodeService = KilocodeService(firestoreService)

val response = kilocodeService.generateResponse(
    prompt = "Analyze the architecture of our construction management system",
    context = LLMContext(
        conversationId = "analysis-001",
        systemPrompt = "You are an expert system architect"
    ),
    agentType = AgentType.CTO_DESIGN_ORCHESTRATOR
)
```

### Comprehensive Codebase Analysis

```kotlin
// Collect all Kotlin files
val codebaseFiles = mutableMapOf<String, String>()
File("app/src/main/java").walkTopDown()
    .filter { it.extension == "kt" }
    .forEach { file ->
        codebaseFiles[file.path] = file.readText()
    }

// Analyze with Kilocode
val analysis = kilocodeService.analyzeCodebase(
    codebaseContent = codebaseFiles,
    analysisType = "refactor",
    focusAreas = listOf(
        "Code duplication",
        "Large files (1000+ lines)",
        "Cyclomatic complexity",
        "Architecture patterns"
    )
)

if (analysis.isSuccess) {
    val result = analysis.getOrNull()!!
    println("Analyzed ${result.fileCount} files (${result.totalLines} lines)")
    println("Tokens used: ${result.tokenUsage}")
    println("\nFindings:\n${result.findings}")
}
```

### Multi-Agent Coordination

```kotlin
val coordinationRequest = MultiAgentCoordinationRequest(
    requestingAgent = AgentType.ORCHESTRATOR,
    targetAgents = listOf(
        AgentType.CFO_FINANCIAL_ORCHESTRATOR,
        AgentType.COO_OPERATIONS_ORCHESTRATOR,
        AgentType.CTO_DESIGN_ORCHESTRATOR
    ),
    task = "Refactor cost estimation system",
    context = "Current system has code duplication across 15 files",
    priority = "HIGH"
)

val coordinationResponse = kilocodeService.generateCoordinationResponse(
    coordinationRequest
)
```

### Intelligent Model Selection

KilocodeService automatically selects the appropriate backend:

```kotlin
// Small context (< 32K tokens) → OpenRouter fallback
val smallResponse = kilocodeService.generateResponse(
    prompt = "What is the project status?",  // Small query
    agentType = AgentType.COO_OPERATIONS_ORCHESTRATOR
)

// Large context (≥ 32K tokens) → Kilocode
val largeResponse = kilocodeService.generateResponse(
    prompt = "Analyze all 200+ files and suggest refactoring",
    context = LLMContext(
        conversationId = "large-analysis",
        systemPrompt = "System architect",
        previousMessages = largeConversationHistory  // Large context
    ),
    agentType = AgentType.CTO_DESIGN_ORCHESTRATOR
)

// Force Kilocode regardless of size
val forcedKilocode = kilocodeService.generateResponse(
    prompt = "Review this file",
    context = LLMContext(
        conversationId = "forced",
        systemPrompt = "Code reviewer",
        metadata = mapOf("force_kilocode" to true)
    ),
    agentType = AgentType.CTO_DESIGN_ORCHESTRATOR
)
```

## Configuration

### Environment Variables

```bash
# Required for Kilocode API access
export KILOCODE_API_KEY="your_kilocode_api_key_here"

# Optional: Custom API endpoint
export KILOCODE_API_ENDPOINT="https://api.kilocode.ai/v1"
```

### Gradle Configuration

No additional Gradle dependencies required - uses standard Android HTTP libraries.

### Firebase Configuration

Kilocode conversations and analyses are stored in Firestore:

- **Collection**: `kilocode_conversations`
- **Collection**: `kilocode_analyses`

## Performance Characteristics

### Context Size Handling

| Context Size | Recommended Provider | Processing Time | Cost Factor |
|-------------|---------------------|-----------------|-------------|
| < 8K tokens | OpenRouter (GPT-3.5) | ~1-2s | 1x |
| 8K-32K tokens | OpenRouter (GPT-4) | ~3-5s | 10x |
| 32K-128K tokens | OpenRouter (Claude) | ~5-10s | 25x |
| 128K-1M tokens | **Kilocode** | ~10-30s | 50x |

### Token Estimation

Rough estimation: **~4 characters per token**

```kotlin
private fun estimateTokenCount(messages: List<ChatMessage>): Int {
    return messages.sumOf { it.content.length / 4 }
}
```

## Best Practices

### 1. Use Kilocode for Large-Scale Analysis

✅ **Good Use Cases**:
- Analyzing entire repositories (100+ files)
- System-wide refactoring planning
- Comprehensive security audits
- Cross-project architecture reviews

❌ **Poor Use Cases**:
- Simple queries ("What's the project status?")
- Single-file analysis
- Real-time chat interactions
- Frequent small requests

### 2. Leverage Fallback Strategy

```kotlin
val kilocodeService = KilocodeService(
    firestoreService = firestoreService,
    kilocodeClient = KilocodeClient(),
    openRouterFallback = openRouterService  // Automatic fallback
)
```

### 3. Monitor Token Usage

```kotlin
val response = kilocodeService.generateResponse(...)
if (response.isSuccess) {
    val llmResponse = response.getOrNull()!!
    val tokens = llmResponse.tokenUsage.totalTokens
    
    Log.d("TokenUsage", "Used $tokens tokens (${tokens / 1000}K)")
    
    // Alert if approaching 1M limit
    if (tokens > 900_000) {
        Log.w("TokenUsage", "Approaching 1M token limit!")
    }
}
```

### 4. Structure Large Analyses

For extremely large codebases (>1M tokens), split by domain:

```kotlin
// Analyze by module
val frontendAnalysis = kilocodeService.analyzeCodebase(
    codebaseContent = frontendFiles,
    analysisType = "refactor",
    focusAreas = listOf("TypeScript patterns", "React components")
)

val backendAnalysis = kilocodeService.analyzeCodebase(
    codebaseContent = backendFiles,
    analysisType = "refactor",
    focusAreas = listOf("Kotlin patterns", "Android architecture")
)
```

## Error Handling

### Automatic Fallback

```kotlin
try {
    val response = kilocodeService.generateResponse(...)
    // Automatically falls back to OpenRouter on Kilocode failure
} catch (e: Exception) {
    Log.e(TAG, "Both Kilocode and fallback failed", e)
}
```

### Manual Retry Logic

```kotlin
suspend fun generateWithRetry(
    prompt: String,
    maxRetries: Int = 3
): Result<LLMResponse> {
    repeat(maxRetries) { attempt ->
        val result = kilocodeService.generateResponse(prompt, ...)
        if (result.isSuccess) return result
        
        Log.w(TAG, "Attempt ${attempt + 1} failed, retrying...")
        delay(1000L * (attempt + 1)) // Exponential backoff
    }
    return Result.failure(Exception("Max retries exceeded"))
}
```

## Comparison with Other Providers

| Feature | GPT-4 | Claude 3 | Gemini Pro 1.5 | **Kilocode** |
|---------|-------|----------|----------------|-------------|
| Context Window | 128K | 200K | 1M | **1M+** |
| Code Understanding | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | **⭐⭐⭐⭐⭐** |
| Refactoring | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ | **⭐⭐⭐⭐⭐** |
| Architecture Review | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | **⭐⭐⭐⭐⭐** |
| Speed (small) | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |
| Speed (large) | ⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ | **⭐⭐⭐⭐** |
| Cost (per 1M tokens) | $30 | $15 | $7 | **$5-10** |

## Troubleshooting

### Issue: "Kilocode API key not configured"

**Solution**: Set environment variable:
```bash
export KILOCODE_API_KEY="your_key"
```

### Issue: Timeout on large requests

**Solution**: Increase timeout in KilocodeClient:
```kotlin
private const val DEFAULT_TIMEOUT = 180_000 // 3 minutes
```

### Issue: Out of memory with very large codebases

**Solution**: Process in chunks:
```kotlin
val chunks = codebaseFiles.chunked(50)  // 50 files per request
val analyses = chunks.map { chunk ->
    kilocodeService.analyzeCodebase(chunk.toMap(), ...)
}
```

## Future Enhancements

- [ ] Streaming support for real-time analysis feedback
- [ ] Parallel processing for multi-module analysis
- [ ] Incremental analysis (only changed files)
- [ ] Integration with CI/CD pipelines
- [ ] Visual architecture diagram generation
- [ ] Automated PR comment generation with suggestions

## References

- [Kilocode API Documentation](https://docs.kilocode.ai)
- [NextGen BuildPro LLM Architecture](./README.md)
- [OpenRouter Integration](./README.md#openrouter-integration)
- [Agent Coordination Protocol](../../orchestrators/README.md)

---

**Maintained by**: NextGen BuildPro Team  
**Last Updated**: 2024  
**Status**: Production Ready ✅
