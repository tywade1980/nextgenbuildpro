/**
 * OpenRouter Service - Provides LLM integration through OpenRouter API
 * Replaces spark.llm with OpenRouter backend while maintaining compatibility
 */

export interface OpenRouterConfig {
  apiKey: string
  baseUrl?: string
  defaultModel?: string
  timeout?: number
  maxRetries?: number
}

export interface OpenRouterMessage {
  role: 'system' | 'user' | 'assistant'
  content: string
}

export interface OpenRouterRequest {
  model: string
  messages: OpenRouterMessage[]
  max_tokens?: number
  temperature?: number
  top_p?: number
  stream?: boolean
  response_format?: { type: string }
}

export interface OpenRouterResponse {
  id: string
  object: string
  created: number
  model: string
  choices: Array<{
    index: number
    message: {
      role: string
      content: string
    }
    finish_reason: string
  }>
  usage: {
    prompt_tokens: number
    completion_tokens: number
    total_tokens: number
  }
}

export class OpenRouterService {
  private config: OpenRouterConfig
  private static instance: OpenRouterService

  constructor(config: OpenRouterConfig) {
    this.config = {
      baseUrl: 'https://openrouter.ai/api/v1',
      defaultModel: 'openai/gpt-4o',
      timeout: 30000,
      maxRetries: 3,
      ...config
    }
  }

  public static getInstance(config?: OpenRouterConfig): OpenRouterService {
    if (!OpenRouterService.instance) {
      if (!config) {
        throw new Error('OpenRouter configuration required for first initialization')
      }
      OpenRouterService.instance = new OpenRouterService(config)
    }
    return OpenRouterService.instance
  }

  public static configure(config: OpenRouterConfig): void {
    OpenRouterService.instance = new OpenRouterService(config)
  }

  /**
   * Make a completion request to OpenRouter
   */
  public async complete(
    prompt: string, 
    model?: string, 
    options?: {
      temperature?: number
      maxTokens?: number
      systemPrompt?: string
      returnJson?: boolean
    }
  ): Promise<string> {
    const messages: OpenRouterMessage[] = []
    
    if (options?.systemPrompt) {
      messages.push({
        role: 'system',
        content: options.systemPrompt
      })
    }
    
    messages.push({
      role: 'user',
      content: prompt
    })

    const request: OpenRouterRequest = {
      model: model || this.config.defaultModel || 'openai/gpt-4o',
      messages,
      max_tokens: options?.maxTokens || 4000,
      temperature: options?.temperature || 0.7
    }

    if (options?.returnJson) {
      request.response_format = { type: 'json_object' }
    }

    return await this.makeRequest(request)
  }

  /**
   * Compatible with spark.llm signature
   */
  public async llm(
    prompt: string, 
    model?: string, 
    returnJson?: boolean
  ): Promise<string> {
    return await this.complete(prompt, model, { returnJson })
  }

  /**
   * Make HTTP request to OpenRouter API
   */
  private async makeRequest(request: OpenRouterRequest, retryCount = 0): Promise<string> {
    try {
      const response = await fetch(`${this.config.baseUrl}/chat/completions`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${this.config.apiKey}`,
          'HTTP-Referer': window.location.origin,
          'X-Title': 'AI Development Platform'
        },
        body: JSON.stringify(request),
        signal: AbortSignal.timeout(this.config.timeout || 30000)
      })

      if (!response.ok) {
        throw new Error(`OpenRouter API error: ${response.status} ${response.statusText}`)
      }

      const data: OpenRouterResponse = await response.json()
      
      if (!data.choices || data.choices.length === 0) {
        throw new Error('No response choices returned from OpenRouter')
      }

      return data.choices[0].message.content

    } catch (error) {
      if (retryCount < (this.config.maxRetries || 3)) {
        await new Promise(resolve => setTimeout(resolve, Math.pow(2, retryCount) * 1000))
        return this.makeRequest(request, retryCount + 1)
      }
      
      throw new Error(`OpenRouter request failed after ${retryCount + 1} attempts: ${error.message}`)
    }
  }

  /**
   * Get available models from OpenRouter
   */
  public async getModels(): Promise<Array<{ id: string; name: string; description?: string }>> {
    try {
      const response = await fetch(`${this.config.baseUrl}/models`, {
        headers: {
          'Authorization': `Bearer ${this.config.apiKey}`
        }
      })

      if (!response.ok) {
        throw new Error(`Failed to fetch models: ${response.status}`)
      }

      const data = await response.json()
      return data.data || []
    } catch (error) {
      console.error('Failed to fetch OpenRouter models:', error)
      return [
        { id: 'openai/gpt-4o', name: 'GPT-4o', description: 'OpenAI GPT-4o' },
        { id: 'openai/gpt-4', name: 'GPT-4', description: 'OpenAI GPT-4' },
        { id: 'anthropic/claude-3.5-sonnet', name: 'Claude 3.5 Sonnet', description: 'Anthropic Claude 3.5 Sonnet' },
        { id: 'meta-llama/llama-3.1-405b-instruct', name: 'Llama 3.1 405B', description: 'Meta Llama 3.1 405B Instruct' }
      ]
    }
  }

  /**
   * Test the API connection
   */
  public async testConnection(): Promise<boolean> {
    try {
      const response = await this.complete('Test connection - respond with "OK"', undefined, {
        maxTokens: 10,
        temperature: 0
      })
      return response.toLowerCase().includes('ok')
    } catch (error) {
      console.error('OpenRouter connection test failed:', error)
      return false
    }
  }
}

// Create global instance accessor
let globalOpenRouterService: OpenRouterService | null = null

export function getOpenRouterService(): OpenRouterService {
  if (!globalOpenRouterService) {
    throw new Error('OpenRouter service not initialized. Call initializeOpenRouter first.')
  }
  return globalOpenRouterService
}

export function initializeOpenRouter(config: OpenRouterConfig): OpenRouterService {
  globalOpenRouterService = new OpenRouterService(config)
  return globalOpenRouterService
}

// Configuration management
export interface LLMConfig {
  provider: 'openrouter' | 'spark'
  openrouter?: OpenRouterConfig
  defaultModel?: string
}

export function getConfigFromEnvironment(): LLMConfig {
  // Try to get from environment variables (for local development)
  const openRouterApiKey = (typeof import.meta !== 'undefined' && import.meta.env?.VITE_OPENROUTER_API_KEY) ||
                           (typeof window !== 'undefined' && (window as any).OPENROUTER_API_KEY)

  if (openRouterApiKey) {
    return {
      provider: 'openrouter',
      openrouter: {
        apiKey: openRouterApiKey,
        defaultModel: (typeof import.meta !== 'undefined' && import.meta.env?.VITE_OPENROUTER_DEFAULT_MODEL) || 
                     'openai/gpt-4o'
      }
    }
  }

  // Fallback to spark (existing behavior)
  return {
    provider: 'spark',
    defaultModel: 'gpt-4o'
  }
}