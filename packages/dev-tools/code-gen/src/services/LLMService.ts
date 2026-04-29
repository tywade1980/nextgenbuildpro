/**
 * Unified LLM Service - Provides a consistent interface for LLM operations
 * Supports both OpenRouter and Spark backends with automatic fallback
 */

import { OpenRouterService, getOpenRouterService, initializeOpenRouter, getConfigFromEnvironment, type LLMConfig } from './OpenRouterService'

declare global {
  interface Window {
    spark?: {
      llm: (prompt: string, model?: string, returnJson?: boolean) => Promise<string>
      llmPrompt: (strings: TemplateStringsArray, ...values: any[]) => string
    }
  }
}

export interface LLMPromptTemplate {
  (strings: TemplateStringsArray, ...values: any[]): string
}

export class LLMService {
  private static instance: LLMService
  private config: LLMConfig
  private openRouterService: OpenRouterService | null = null

  constructor() {
    this.config = getConfigFromEnvironment()
    this.initialize()
  }

  public static getInstance(): LLMService {
    if (!LLMService.instance) {
      LLMService.instance = new LLMService()
    }
    return LLMService.instance
  }

  /**
   * Initialize the LLM service based on configuration
   */
  private initialize(): void {
    if (this.config.provider === 'openrouter' && this.config.openrouter) {
      try {
        this.openRouterService = initializeOpenRouter(this.config.openrouter)
        console.log('✅ OpenRouter LLM service initialized')
      } catch (error) {
        console.warn('⚠️ Failed to initialize OpenRouter, falling back to Spark:', error)
        this.config.provider = 'spark'
      }
    }
  }

  /**
   * Reconfigure the LLM service
   */
  public configure(config: LLMConfig): void {
    this.config = config
    this.initialize()
  }

  /**
   * Set OpenRouter API key at runtime
   */
  public setOpenRouterKey(apiKey: string, defaultModel?: string): void {
    try {
      this.openRouterService = initializeOpenRouter({
        apiKey,
        defaultModel: defaultModel || 'openai/gpt-4o'
      })
      this.config = {
        provider: 'openrouter',
        openrouter: {
          apiKey,
          defaultModel: defaultModel || 'openai/gpt-4o'
        }
      }
      console.log('✅ OpenRouter API key configured successfully')
    } catch (error) {
      console.error('❌ Failed to configure OpenRouter API key:', error)
      throw error
    }
  }

  /**
   * Main LLM completion method with automatic provider selection
   */
  public async llm(prompt: string, model?: string, returnJson?: boolean): Promise<string> {
    const resolvedModel = model || this.config.defaultModel || 'gpt-4o'

    // Try OpenRouter first if configured
    if (this.config.provider === 'openrouter' && this.openRouterService) {
      try {
        return await this.openRouterService.llm(prompt, resolvedModel, returnJson)
      } catch (error) {
        console.warn('OpenRouter request failed, falling back to Spark:', error)
        // Fall through to Spark fallback
      }
    }

    // Fallback to Spark if available
    if (typeof window !== 'undefined' && window.spark?.llm) {
      try {
        return await window.spark.llm(prompt, resolvedModel, returnJson)
      } catch (error) {
        console.error('Spark LLM request failed:', error)
        throw new Error(`Both OpenRouter and Spark LLM requests failed. Last error: ${error.message}`)
      }
    }

    // If neither service is available
    throw new Error('No LLM service available. Please configure OpenRouter API key or ensure Spark is running.')
  }

  /**
   * Template literal function compatible with spark.llmPrompt
   */
  public llmPrompt: LLMPromptTemplate = (strings: TemplateStringsArray, ...values: any[]): string => {
    let result = strings[0]
    for (let i = 1; i < strings.length; i++) {
      result += String(values[i - 1]) + strings[i]
    }
    return result
  }

  /**
   * Get current provider status
   */
  public getStatus(): {
    provider: string
    configured: boolean
    available: boolean
    models?: string[]
  } {
    if (this.config.provider === 'openrouter' && this.openRouterService) {
      return {
        provider: 'OpenRouter',
        configured: true,
        available: true
      }
    }

    if (typeof window !== 'undefined' && window.spark?.llm) {
      return {
        provider: 'Spark',
        configured: true,
        available: true
      }
    }

    return {
      provider: 'None',
      configured: false,
      available: false
    }
  }

  /**
   * Test the current LLM configuration
   */
  public async testConnection(): Promise<boolean> {
    try {
      const response = await this.llm('Respond with exactly: "LLM_TEST_OK"', undefined, false)
      return response.includes('LLM_TEST_OK')
    } catch (error) {
      console.error('LLM connection test failed:', error)
      return false
    }
  }

  /**
   * Get available models from the current provider
   */
  public async getAvailableModels(): Promise<Array<{ id: string; name: string; description?: string }>> {
    if (this.config.provider === 'openrouter' && this.openRouterService) {
      return await this.openRouterService.getModels()
    }

    // Default models for Spark
    return [
      { id: 'gpt-4o', name: 'GPT-4o', description: 'OpenAI GPT-4o via Spark' },
      { id: 'gpt-4', name: 'GPT-4', description: 'OpenAI GPT-4 via Spark' },
      { id: 'claude-3-sonnet', name: 'Claude 3 Sonnet', description: 'Anthropic Claude 3 Sonnet via Spark' }
    ]
  }
}

// Create global instance
const globalLLMService = LLMService.getInstance()

// Export functions that match spark.llm API
export const llm = globalLLMService.llm.bind(globalLLMService)
export const llmPrompt = globalLLMService.llmPrompt.bind(globalLLMService)

// Export service instance
export const llmService = globalLLMService

// Provide backward compatibility by creating global spark-like object
if (typeof window !== 'undefined') {
  // Enhance existing spark object or create new one
  if (!window.spark) {
    (window as any).spark = {}
  }

  // Override spark.llm and spark.llmPrompt with our service
  window.spark.llm = llm
  window.spark.llmPrompt = llmPrompt
}

export default LLMService