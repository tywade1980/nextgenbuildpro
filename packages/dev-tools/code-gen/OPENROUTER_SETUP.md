# OpenRouter LLM Integration Setup

This guide explains how to configure OpenRouter as your LLM provider for the AI Development Platform.

## What is OpenRouter?

OpenRouter is a unified API that provides access to multiple LLM providers including OpenAI, Anthropic, Meta, Google, and many others. It offers:

- **Multiple Models**: Access GPT-4o, Claude 3.5 Sonnet, Llama 3.1, Gemini Pro, and 200+ other models
- **Competitive Pricing**: Often lower costs than direct provider APIs
- **Unified Interface**: Same API format for all providers
- **Pay-per-use**: No monthly subscriptions, pay only for what you use
- **Rate Limiting**: Built-in rate limiting and reliability features

## Setup Instructions

### 1. Get Your OpenRouter API Key

1. Visit [OpenRouter](https://openrouter.ai)
2. Sign up for a free account
3. Add credits to your account (minimum $5 recommended)
4. Go to [API Keys](https://openrouter.ai/keys) and create a new key
5. Copy your key (starts with `sk-or-v1-...`)

### 2. Configure in the Application

1. **Access LLM Configuration**:
   - Open the AI Development Platform
   - Click on the **"LLM Config"** tab in the navigation bar

2. **Enable OpenRouter**:
   - Toggle the **"Use OpenRouter (Recommended)"** switch
   - This will show the configuration options

3. **Enter API Key**:
   - Paste your OpenRouter API key in the **"OpenRouter API Key"** field
   - Click "Show" to verify the key if needed

4. **Select Default Model**:
   - Choose from popular models like:
     - **GPT-4o** (OpenAI) - Latest and most capable
     - **Claude 3.5 Sonnet** (Anthropic) - Excellent for coding
     - **Llama 3.1 405B** (Meta) - Open-source powerhouse
     - **Gemini Pro 1.5** (Google) - Cost-effective option

5. **Test Connection**:
   - Go to the **"Testing"** tab
   - Click **"Test Connection"** to verify your setup
   - You should see a success message if everything is configured correctly

### 3. Environment Variables (Optional)

For development environments, you can set environment variables:

```bash
# .env file
VITE_OPENROUTER_API_KEY=sk-or-v1-your-key-here
VITE_OPENROUTER_DEFAULT_MODEL=openai/gpt-4o
```

The application will automatically detect and use these variables.

## Model Recommendations

### For Code Generation:
- **GPT-4o** - Best overall performance for complex code generation
- **Claude 3.5 Sonnet** - Excellent at understanding context and generating clean code
- **GPT-4 Turbo** - Fast and reliable for most coding tasks

### For Cost-Effective Usage:
- **Gemini Pro 1.5** - Google's model with competitive pricing
- **Llama 3.1 8B** - Open-source model for simple tasks
- **Claude 3 Haiku** - Anthropic's fastest model

### For Large Context:
- **Claude 3.5 Sonnet** - 200K context window
- **GPT-4o** - 128K context window
- **Gemini Pro 1.5** - 1M+ context window

## Usage Examples

Once configured, the platform will automatically use OpenRouter for:

- **AI Assistant**: Natural language conversations about your projects
- **Code Generation**: Creating complete applications from descriptions
- **Template Generation**: Building framework-specific project templates
- **Backend Integration**: Generating API endpoints and database schemas
- **Testing**: Creating automated test suites
- **Documentation**: Generating API documentation

## Troubleshooting

### Connection Test Fails
- Verify your API key is correct and starts with `sk-or-v1-`
- Check that you have credits in your OpenRouter account
- Ensure your internet connection is stable

### Model Not Available
- Some models require special access or higher credit limits
- Try switching to a more commonly available model like GPT-4o
- Check the OpenRouter website for model availability

### Slow Responses
- Some larger models (like Llama 3.1 405B) may take longer to respond
- Consider switching to faster models like GPT-4 Turbo for interactive use
- Check OpenRouter's status page for any service issues

## Fallback Behavior

The platform is designed with automatic fallback:

1. **Primary**: OpenRouter (if configured)
2. **Fallback**: Spark LLM service (if available)
3. **Error**: Clear error message if no service is available

This ensures your development workflow continues even if one service has issues.

## Cost Management

- **Monitor Usage**: Check your OpenRouter dashboard regularly
- **Set Limits**: Configure spending limits in your OpenRouter account
- **Choose Efficient Models**: Use smaller models for simple tasks
- **Batch Requests**: The platform automatically optimizes API calls

## Security Notes

- **API Key Storage**: Keys are stored locally in your browser's storage
- **No Server Storage**: Your API keys never leave your local environment
- **HTTPS Only**: All API calls are made over secure HTTPS connections
- **Revocable**: You can revoke and regenerate API keys anytime

## Getting Help

- **OpenRouter Support**: [OpenRouter Discord](https://discord.gg/openrouter)
- **Documentation**: [OpenRouter Docs](https://openrouter.ai/docs)
- **Model Pricing**: [OpenRouter Models](https://openrouter.ai/models)

## Advanced Configuration

For advanced users, you can customize the OpenRouter configuration by modifying the `LLMService` configuration:

```typescript
import { llmService } from '@/services/LLMService'

// Configure custom settings
llmService.configure({
  provider: 'openrouter',
  openrouter: {
    apiKey: 'your-key-here',
    defaultModel: 'anthropic/claude-3.5-sonnet',
    baseUrl: 'https://openrouter.ai/api/v1', // custom endpoint
    timeout: 60000, // 60 second timeout
    maxRetries: 5 // retry failed requests
  }
})
```

This gives you full control over the LLM integration behavior.