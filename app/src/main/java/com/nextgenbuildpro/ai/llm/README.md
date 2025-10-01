# LLM Service - OpenRouter Integration

This module provides Large Language Model (LLM) capabilities for NextGen BuildPro's multi-agent AI system through OpenRouter's unified API.

## Overview

OpenRouter (https://openrouter.ai) provides a single API to access multiple LLM providers:
- OpenAI (GPT-4, GPT-3.5-turbo, o1-preview)
- Anthropic (Claude 3 Opus, Sonnet, Haiku)
- Meta (Llama 3, Llama 2)
- Google (Gemini Pro)
- Mistral AI
- And 100+ more models

## Architecture

### Components

1. **OpenRouterClient** - Low-level HTTP client for OpenRouter API
2. **OpenRouterService** - High-level service implementing LLMService interface
3. **LLMServiceImpl** - Original mock implementation (now superseded by OpenRouterService)

### Integration with Multi-Agent System

The OpenRouter service integrates seamlessly with NextGen BuildPro's agent architecture:

```kotlin
// Each agent type has a specialized system prompt
AgentType.ORCHESTRATOR -> "You are the Main Orchestrator..."
AgentType.COO_OPERATIONS_ORCHESTRATOR -> "You manage project operations..."
AgentType.CFO_FINANCIAL_ORCHESTRATOR -> "You handle financial analysis..."
// etc.
```

## Setup

### 1. Get OpenRouter API Key

1. Visit [https://openrouter.ai](https://openrouter.ai)
2. Sign up for an account
3. Navigate to [https://openrouter.ai/keys](https://openrouter.ai/keys)
4. Create a new API key
5. Add credits at [https://openrouter.ai/credits](https://openrouter.ai/credits)

### 2. Configure API Key

Add your API key to `local.properties` in the project root:

```properties
openrouter.api.key=sk-or-v1-your-actual-key-here
```

**IMPORTANT**: Never commit this file to version control!

### 3. Initialize the Service

```kotlin
// In your application or module initialization
val firestoreService = FirestoreServiceImpl()
val openRouterService = OpenRouterService(firestoreService)

// Or use dependency injection
@Provides
fun provideOpenRouterService(
    firestoreService: FirestoreService
): LLMService = OpenRouterService(firestoreService)
```

## Usage Examples

### Basic Chat Completion

```kotlin
val openRouterService = OpenRouterService(firestoreService)

val result = openRouterService.generateResponse(
    prompt = "How should I allocate resources for this construction project?",
    context = null,
    agentType = AgentType.COO_OPERATIONS_ORCHESTRATOR
)

result.onSuccess { response ->
    Log.d(TAG, "LLM Response: ${response.content}")
    Log.d(TAG, "Tokens used: ${response.tokenUsage.totalTokens}")
}

result.onFailure { error ->
    Log.e(TAG, "LLM Error: ${error.message}")
}
```

### Multi-Agent Coordination

```kotlin
val request = MultiAgentCoordinationRequest(
    requestingAgent = AgentType.ORCHESTRATOR,
    targetAgents = listOf(
        AgentType.COO_OPERATIONS_ORCHESTRATOR,
        AgentType.CFO_FINANCIAL_ORCHESTRATOR,
        AgentType.CTO_DESIGN_ORCHESTRATOR
    ),
    task = "Plan and budget new commercial building project",
    context = "50,000 sq ft office building, downtown location, 18-month timeline",
    priority = "HIGH"
)

val result = openRouterService.generateCoordinationResponse(request)

result.onSuccess { response ->
    Log.d(TAG, "Coordination Plan:\n${response.plan}")
    
    response.agentAssignments.forEach { (agent, task) ->
        Log.d(TAG, "$agent: $task")
    }
    
    Log.d(TAG, "Estimated Duration: ${response.estimatedDuration} minutes")
}
```

### Conversation with Context

```kotlin
val conversationId = UUID.randomUUID().toString()

val context = LLMContext(
    conversationId = conversationId,
    systemPrompt = "You are a construction project expert",
    previousMessages = listOf(
        LLMMessage("user", "What's the current project status?"),
        LLMMessage("assistant", "The project is 60% complete...")
    )
)

val result = openRouterService.generateResponse(
    prompt = "What should we prioritize next week?",
    context = context,
    agentType = AgentType.COO_OPERATIONS_ORCHESTRATOR
)
```

### Direct OpenRouter Client Usage

For advanced use cases, you can use the OpenRouterClient directly:

```kotlin
val client = OpenRouterClient()

val messages = listOf(
    ChatMessage("system", "You are a helpful assistant"),
    ChatMessage("user", "Estimate the cost of a 10,000 sq ft concrete slab")
)

val result = client.chatCompletion(
    messages = messages,
    model = "anthropic/claude-3-opus",
    temperature = 0.7,
    maxTokens = 2048
)

result.onSuccess { response ->
    Log.d(TAG, "Response: ${response.content}")
}
```

## Model Selection

The OpenRouterService automatically selects appropriate models based on task complexity:

### Reasoning Tasks (Complex Analysis)
```kotlin
// Automatically uses: openai/o1-preview
"analyze the budget variance for Q1"
"optimize resource allocation across 5 projects"
"develop a strategic plan for market expansion"
```

### Agent Coordination (Multi-Agent Workflows)
```kotlin
// Automatically uses: anthropic/claude-3-opus
"coordinate between finance and operations teams"
"orchestrate the project kickoff process"
```

### Code Generation
```kotlin
// Automatically uses: anthropic/claude-3-sonnet
"write a script to calculate material quantities"
"generate code for the invoice processing system"
```

### Fast Inference (Simple Queries)
```kotlin
// Automatically uses: openai/gpt-3.5-turbo
"What is the project deadline?"
"List the active contractors"
```

### Manual Model Selection

You can also manually specify models:

```kotlin
val client = OpenRouterClient()

// Use a specific model
client.chatCompletion(
    messages = messages,
    model = "openai/gpt-4-turbo-preview",  // Specific model
    temperature = 0.7,
    maxTokens = 4096
)
```

## Available Models

### OpenAI
- `openai/o1-preview` - Advanced reasoning (expensive)
- `openai/gpt-4-turbo-preview` - High quality, multimodal
- `openai/gpt-4` - High quality, reliable
- `openai/gpt-3.5-turbo` - Fast, cost-effective

### Anthropic
- `anthropic/claude-3-opus` - Best quality, long context (200K tokens)
- `anthropic/claude-3-sonnet` - Balanced performance/cost
- `anthropic/claude-3-haiku` - Fast, affordable

### Meta
- `meta-llama/llama-3-70b-instruct` - High quality open model
- `meta-llama/llama-3-8b-instruct` - Fast, affordable

### Google
- `google/gemini-pro-1.5` - Long context, multimodal
- `google/gemini-pro` - Balanced performance

### Mistral
- `mistralai/mistral-large` - High quality
- `mistralai/mixtral-8x7b-instruct` - Efficient, good quality

See full list at [https://openrouter.ai/models](https://openrouter.ai/models)

## Cost Management

### Understanding Costs

OpenRouter charges per token:
- **Input tokens**: The prompt you send
- **Output tokens**: The response generated
- Different models have different rates

Example costs (approximate):
- GPT-3.5-turbo: $0.0005 per 1K tokens
- GPT-4: $0.03 per 1K tokens
- Claude 3 Opus: $0.015 per 1K tokens
- Llama 3 8B: $0.0001 per 1K tokens

### Cost Optimization Tips

1. **Use appropriate models**:
   ```kotlin
   // Don't use GPT-4 for simple tasks
   // ❌ Expensive
   client.chatCompletion(messages, model = "openai/gpt-4")
   
   // ✅ Cost-effective
   client.chatCompletion(messages, model = "openai/gpt-3.5-turbo")
   ```

2. **Limit max_tokens**:
   ```kotlin
   // Set reasonable token limits
   client.chatCompletion(
       messages = messages,
       maxTokens = 500  // Limit response length
   )
   ```

3. **Cache frequently used responses**:
   ```kotlin
   // Store and reuse common responses in Firestore
   openRouterService.storeConversation(conversation)
   ```

4. **Monitor usage**:
   - Check usage at [https://openrouter.ai/activity](https://openrouter.ai/activity)
   - Set up budget alerts in your OpenRouter dashboard
   - Track token usage in your application logs

## Error Handling

The service provides comprehensive error handling:

```kotlin
val result = openRouterService.generateResponse(prompt, context, agentType)

result.onSuccess { response ->
    // Handle successful response
    processResponse(response)
}

result.onFailure { error ->
    when (error) {
        is IllegalStateException -> {
            // API key not configured
            Log.e(TAG, "OpenRouter API key not found. Check local.properties")
            showConfigurationError()
        }
        is java.net.SocketTimeoutException -> {
            // Network timeout
            Log.e(TAG, "Request timed out. Retrying...")
            retryRequest()
        }
        else -> {
            // Other errors
            Log.e(TAG, "LLM Error: ${error.message}", error)
            showGenericError()
        }
    }
}
```

## Testing

### Unit Tests

Run the LLM service tests:

```bash
./gradlew test --tests "com.nextgenbuildpro.ai.llm.LLMServiceTest"
```

### Mock vs Real Service

For testing without API calls, use the mock implementation:

```kotlin
@Test
fun testWithMockService() {
    val mockService = LLMServiceImpl(mockFirestoreService)
    // Test without making actual API calls
}
```

For integration testing with real API:

```kotlin
@Test
fun testWithRealOpenRouter() {
    val realService = OpenRouterService(realFirestoreService)
    // Makes actual API calls - consumes credits
}
```

## Security Best Practices

1. **Never commit API keys**:
   - ✅ Use `local.properties` (excluded from git)
   - ❌ Never hardcode keys in source code

2. **Rotate keys regularly**:
   - Generate new keys monthly
   - Revoke old keys after rotation

3. **Use environment-specific keys**:
   - Development: Low-limit key
   - Staging: Medium-limit key
   - Production: High-limit key with restrictions

4. **Monitor for anomalies**:
   - Set up alerts for unusual usage
   - Review activity logs regularly

5. **Implement rate limiting**:
   ```kotlin
   // Add application-level rate limiting
   if (requestCount > MAX_REQUESTS_PER_MINUTE) {
       throw RateLimitException("Too many requests")
   }
   ```

## Troubleshooting

### "OpenRouter API key not configured"

**Cause**: API key not found in `local.properties`

**Solution**:
1. Create/edit `local.properties` in project root
2. Add: `openrouter.api.key=sk-or-v1-your-key-here`
3. Restart the application

### "HTTP 401 Unauthorized"

**Cause**: Invalid or expired API key

**Solution**:
1. Check your API key at [https://openrouter.ai/keys](https://openrouter.ai/keys)
2. Generate a new key if needed
3. Update `local.properties`

### "HTTP 402 Payment Required"

**Cause**: Insufficient credits

**Solution**:
1. Add credits at [https://openrouter.ai/credits](https://openrouter.ai/credits)
2. Check your balance in the OpenRouter dashboard

### "HTTP 429 Too Many Requests"

**Cause**: Rate limit exceeded

**Solution**:
1. Implement exponential backoff
2. Reduce request frequency
3. Cache responses when possible

### Connection Timeout

**Cause**: Network issues or slow model

**Solution**:
1. Increase timeout value in OpenRouterClient
2. Use faster models for time-sensitive operations
3. Check network connectivity

## Additional Resources

- [OpenRouter Documentation](https://openrouter.ai/docs)
- [OpenRouter Models](https://openrouter.ai/models)
- [OpenRouter Pricing](https://openrouter.ai/docs#pricing)
- [NextGen BuildPro API Key Management](../../../core/README.md)
- [Multi-Agent System Architecture](../../../../AGENT_ARCHITECTURE.md)

## Support

For issues specific to:
- **OpenRouter API**: Contact OpenRouter support at https://openrouter.ai/support
- **NextGen BuildPro Integration**: Open an issue in the GitHub repository
- **API Key Configuration**: See [API Key Management README](../../../core/README.md)
