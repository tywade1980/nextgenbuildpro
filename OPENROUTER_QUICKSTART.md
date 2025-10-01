# OpenRouter LLM Integration - Quick Start Guide

This guide will help you quickly integrate OpenRouter API for LLM access in NextGen BuildPro.

## 🚀 Quick Setup (5 minutes)

### Step 1: Get Your API Key

1. Go to [https://openrouter.ai](https://openrouter.ai)
2. Sign up or log in
3. Navigate to [API Keys](https://openrouter.ai/keys)
4. Click "Create Key"
5. Copy your API key (starts with `sk-or-v1-`)

### Step 2: Add Credits

1. Go to [Credits](https://openrouter.ai/credits)
2. Add at least $5 to start (recommended: $10-20 for development)
3. OpenRouter charges per token used

### Step 3: Configure Your Project

1. **Create `local.properties` file** in project root:
   ```bash
   cd /path/to/nextgenbuildpro
   cp local.properties.template local.properties
   ```

2. **Add your API key** to `local.properties`:
   ```properties
   openrouter.api.key=sk-or-v1-your-actual-key-here
   ```

3. **Verify `.gitignore`** excludes `local.properties`:
   ```bash
   grep "local.properties" .gitignore
   ```
   Should show: `local.properties`

### Step 4: Test the Integration

Use the example code to verify everything works:

```kotlin
import com.nextgenbuildpro.ai.llm.OpenRouterService
import com.nextgenbuildpro.shared.AgentType

// Initialize the service
val openRouterService = OpenRouterService(firestoreService)

// Test with a simple prompt
val result = openRouterService.generateResponse(
    prompt = "What are the key phases of a construction project?",
    context = null,
    agentType = AgentType.COO_OPERATIONS_ORCHESTRATOR
)

result.onSuccess { response ->
    println("✅ Success: ${response.content}")
    println("Tokens used: ${response.tokenUsage.totalTokens}")
}

result.onFailure { error ->
    println("❌ Error: ${error.message}")
}
```

## 📚 What You Can Do

### 1. Simple LLM Queries
```kotlin
val response = openRouterService.generateResponse(
    prompt = "Estimate cost for 1000 sq ft concrete slab",
    agentType = AgentType.CFO_FINANCIAL_ORCHESTRATOR
)
```

### 2. Multi-Agent Coordination
```kotlin
val request = MultiAgentCoordinationRequest(
    requestingAgent = AgentType.ORCHESTRATOR,
    targetAgents = listOf(
        AgentType.COO_OPERATIONS_ORCHESTRATOR,
        AgentType.CFO_FINANCIAL_ORCHESTRATOR
    ),
    task = "Plan new project",
    context = "Commercial building, $5M budget"
)

val response = openRouterService.generateCoordinationResponse(request)
```

### 3. Conversations with Memory
```kotlin
val context = LLMContext(
    conversationId = UUID.randomUUID().toString(),
    systemPrompt = "You are a construction expert",
    previousMessages = listOf(
        LLMMessage("user", "What's the timeline?"),
        LLMMessage("assistant", "Typically 12-18 months...")
    )
)

val response = openRouterService.generateResponse(
    prompt = "What about costs?",
    context = context,
    agentType = AgentType.CFO_FINANCIAL_ORCHESTRATOR
)
```

## 🎯 Choosing the Right Model

OpenRouter provides access to 100+ models. Here are our recommendations:

| Use Case | Model | Why |
|----------|-------|-----|
| **Complex Reasoning** | `openai/o1-preview` | Best for strategic planning, optimization |
| **Agent Coordination** | `anthropic/claude-3-opus` | Excellent for multi-agent workflows |
| **Fast Queries** | `openai/gpt-3.5-turbo` | Quick responses, cost-effective |
| **Code Generation** | `anthropic/claude-3-sonnet` | Great for technical tasks |
| **Budget-Friendly** | `meta-llama/llama-3-8b-instruct` | Low cost, good quality |

The OpenRouterService **automatically selects the best model** based on your task:

```kotlin
// Automatically uses reasoning model for complex tasks
"analyze and optimize the project budget across 10 sites"

// Automatically uses fast model for simple queries
"what is the project deadline?"
```

## 💰 Cost Management

### Typical Costs (as of 2024)

- **GPT-3.5 Turbo**: $0.0005 per 1K tokens (~$0.01 per request)
- **GPT-4**: $0.03 per 1K tokens (~$0.60 per request)
- **Claude 3 Opus**: $0.015 per 1K tokens (~$0.30 per request)
- **Llama 3 8B**: $0.0001 per 1K tokens (~$0.002 per request)

### Tips to Reduce Costs

1. **Use cheaper models for simple tasks**:
   ```kotlin
   client.chatCompletion(
       messages = messages,
       model = "openai/gpt-3.5-turbo"  // vs expensive gpt-4
   )
   ```

2. **Limit response length**:
   ```kotlin
   client.chatCompletion(
       messages = messages,
       maxTokens = 500  // Instead of 2048
   )
   ```

3. **Cache frequent responses** in Firestore

4. **Set budget alerts** at [OpenRouter Dashboard](https://openrouter.ai/settings)

## 🔒 Security Best Practices

### ✅ DO
- Store API key in `local.properties` (excluded from git)
- Use different keys for dev/staging/production
- Rotate keys regularly (monthly recommended)
- Set rate limits in OpenRouter dashboard
- Monitor usage for anomalies

### ❌ DON'T
- Commit API keys to version control
- Share API keys via email/chat
- Use production keys in development
- Hardcode keys in source code

## 🐛 Troubleshooting

### "OpenRouter API key not configured"

**Solution**: Add key to `local.properties`:
```properties
openrouter.api.key=sk-or-v1-your-key-here
```

### "HTTP 401 Unauthorized"

**Cause**: Invalid or expired API key

**Solution**:
1. Check your key at [https://openrouter.ai/keys](https://openrouter.ai/keys)
2. Generate new key if needed
3. Update `local.properties`

### "HTTP 402 Payment Required"

**Cause**: Insufficient credits

**Solution**: Add credits at [https://openrouter.ai/credits](https://openrouter.ai/credits)

### "HTTP 429 Too Many Requests"

**Cause**: Rate limit exceeded

**Solution**:
1. Implement exponential backoff
2. Reduce request frequency
3. Cache responses

### Connection Timeout

**Solutions**:
- Use faster models (gpt-3.5-turbo vs gpt-4)
- Increase timeout in `OpenRouterClient`
- Check network connectivity

## 📖 More Resources

- **Comprehensive Guide**: [app/src/main/java/com/nextgenbuildpro/ai/llm/README.md](app/src/main/java/com/nextgenbuildpro/ai/llm/README.md)
- **API Key Management**: [app/src/main/java/com/nextgenbuildpro/core/README.md](app/src/main/java/com/nextgenbuildpro/core/README.md)
- **Code Examples**: [app/src/main/java/com/nextgenbuildpro/examples/OpenRouterExample.kt](app/src/main/java/com/nextgenbuildpro/examples/OpenRouterExample.kt)
- **OpenRouter Docs**: [https://openrouter.ai/docs](https://openrouter.ai/docs)
- **Model List**: [https://openrouter.ai/models](https://openrouter.ai/models)

## 🎓 Example Usage

See complete working examples in `OpenRouterExample.kt`:

```kotlin
import com.nextgenbuildpro.examples.OpenRouterExample

// Run all examples (requires API key and credits)
OpenRouterExample.runAllExamples(firestoreService)

// Or run individual examples
OpenRouterExample.example1_BasicResponse(firestoreService)
OpenRouterExample.example2_MultiAgentCoordination(firestoreService)
OpenRouterExample.example3_ConversationWithContext(firestoreService)
```

## ✅ Checklist

Before deploying to production:

- [ ] API key configured in `local.properties`
- [ ] API key NOT in version control
- [ ] Credits added to OpenRouter account
- [ ] Budget alerts configured
- [ ] Rate limits set appropriately
- [ ] Error handling implemented
- [ ] Usage monitoring in place
- [ ] Different keys for dev/prod
- [ ] Team members have their own keys
- [ ] Documentation updated for team

## 🆘 Getting Help

- **OpenRouter Issues**: [OpenRouter Support](https://openrouter.ai/support)
- **Integration Issues**: Open an issue on GitHub
- **Security Concerns**: Review [Security Best Practices](#-security-best-practices)

---

**Ready to build?** Start with `OpenRouterExample.kt` to see it in action! 🚀
