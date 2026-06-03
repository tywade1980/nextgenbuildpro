import { OpenRouter } from 'openrouter-client';
import { AIModel, AIModelType } from '@/types';

export interface OpenRouterModel {
  id: string;
  name: string;
  description: string;
  context_length: number;
  pricing: {
    prompt: string;
    completion: string;
  };
  architecture: {
    modality: string;
    tokenizer: string;
  };
}

export class OpenRouterService {
  private client: OpenRouter | null = null;
  private apiKey: string | null = null;

  constructor(apiKey?: string) {
    this.apiKey = apiKey || process.env.OPENROUTER_API_KEY || null;
    if (this.apiKey) {
      this.client = new OpenRouter(this.apiKey);
    }
  }

  isConfigured(): boolean {
    return this.client !== null && this.apiKey !== null;
  }

  async getAvailableModels(): Promise<OpenRouterModel[]> {
    if (!this.client) {
      throw new Error('OpenRouter client not configured. Please provide an API key.');
    }

    try {
      const response = await fetch('https://openrouter.ai/api/v1/models', {
        headers: {
          'Authorization': `Bearer ${this.apiKey}`,
          'Content-Type': 'application/json'
        }
      });

      if (!response.ok) {
        throw new Error(`OpenRouter API error: ${response.status} ${response.statusText}`);
      }

      const data = await response.json();
      return data.data || [];
    } catch (error) {
      console.error('Error fetching OpenRouter models:', error);
      throw error;
    }
  }

  async generateResponse(
    modelId: string, 
    prompt: string, 
    options: Record<string, unknown> = {}
  ): Promise<string> {
    if (!this.client) {
      throw new Error('OpenRouter client not configured');
    }

    try {
      const response = await this.client.chat([
        { role: 'user', content: prompt }
      ], {
        model: modelId,
        temperature: (options.temperature as number) || 0.7,
        max_tokens: (options.max_tokens as number) || 1000,
        ...options
      });

      if (response.success) {
        return response.data.choices[0]?.message?.content || 'No response generated';
      } else {
        if ('errorCode' in response) {
          throw new Error(`OpenRouter API error: ${response.errorCode} - ${response.errorMessage}`);
        } else {
          throw new Error(`OpenRouter error: ${response.error}`);
        }
      }
    } catch (error) {
      console.error('Error generating OpenRouter response:', error);
      throw error;
    }
  }

  convertToAIModel(openRouterModel: OpenRouterModel): Partial<AIModel> {
    return {
      name: openRouterModel.name,
      type: this.determineModelType(openRouterModel),
      provider: 'openrouter',
      modelId: openRouterModel.id,
      capabilities: this.determineCapabilities(openRouterModel),
      configuration: {
        temperature: 0.7,
        maxTokens: Math.min(openRouterModel.context_length, 4000),
        systemPrompt: 'You are a construction industry expert assistant.',
        pricing: openRouterModel.pricing,
        contextLength: openRouterModel.context_length
      }
    };
  }

  private determineModelType(model: OpenRouterModel): AIModelType {
    const modelId = model.id.toLowerCase();
    const name = model.name.toLowerCase();
    
    if (modelId.includes('whisper') || name.includes('speech') || name.includes('audio')) {
      return 'speech-to-text';
    }
    
    if (modelId.includes('embedding') || name.includes('embedding')) {
      return 'embedding';
    }
    
    if (modelId.includes('classification') || name.includes('classification')) {
      return 'classification';
    }
    
    // Default to LLM for most models
    return 'llm';
  }

  private determineCapabilities(model: OpenRouterModel): string[] {
    const capabilities = ['text-generation'];
    const modelId = model.id.toLowerCase();
    const name = model.name.toLowerCase();
    
    // Add construction-specific capabilities
    capabilities.push('construction-advice', 'project-planning');
    
    if (modelId.includes('gpt') || modelId.includes('claude') || modelId.includes('llama')) {
      capabilities.push('code-analysis', 'document-analysis');
    }
    
    if (model.architecture.modality === 'multimodal' || modelId.includes('vision')) {
      capabilities.push('image-analysis', 'blueprint-analysis');
    }
    
    if (modelId.includes('code') || name.includes('code')) {
      capabilities.push('code-generation', 'code-review');
    }
    
    return capabilities;
  }
}

export const openRouterService = new OpenRouterService();