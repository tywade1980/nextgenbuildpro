import OpenAI from 'openai';

// Construction-themed prompts and styles
const constructionTopics = [
  'modern skyscraper construction site',
  'bridge building project',
  'residential house construction',
  'road construction and infrastructure',
  'commercial building development',
  'renovation and remodeling',
  'heavy machinery and equipment',
  'architectural blueprints and planning',
  'construction workers and safety',
  'concrete and steel framework',
  'construction materials and tools',
  'urban development project',
  'sustainable building construction',
  'industrial construction facility',
  'construction site aerial view'
];

const constructionStyles = [
  'photorealistic',
  'architectural rendering',
  'technical drawing style',
  'blueprint aesthetic',
  'industrial photography',
  'modern minimalist',
  'detailed engineering view',
  'construction site documentary',
  'architectural visualization',
  'technical illustration',
  'professional photography',
  'CAD-style rendering',
  'construction infographic style',
  'dramatic lighting perspective',
  'detailed cross-section view'
];

const timeOfDay = [
  'golden hour lighting',
  'bright daylight',
  'early morning',
  'sunset lighting',
  'dramatic shadows',
  'overcast lighting',
  'artificial lighting'
];

const perspectives = [
  'aerial drone view',
  'ground level perspective',
  'worker eye level',
  'architectural elevation view',
  'close-up detail shot',
  'wide panoramic view',
  'cross-section view',
  'isometric perspective'
];

export interface ImageGenerationOptions {
  topic?: string;
  style?: string;
  lighting?: string;
  perspective?: string;
  customPrompt?: string;
  size?: '1024x1024' | '1792x1024' | '1024x1792';
  quality?: 'standard' | 'hd';
}

export class ConstructionImageGenerator {
  private openai: OpenAI | null = null;

  constructor() {
    if (process.env.OPENAI_API_KEY) {
      this.openai = new OpenAI({
        apiKey: process.env.OPENAI_API_KEY,
      });
    }
  }

  private getRandomElement<T>(array: T[]): T {
    return array[Math.floor(Math.random() * array.length)];
  }

  public generatePrompt(options: ImageGenerationOptions = {}): string {
    const topic = options.topic || this.getRandomElement(constructionTopics);
    const style = options.style || this.getRandomElement(constructionStyles);
    const lighting = options.lighting || this.getRandomElement(timeOfDay);
    const perspective = options.perspective || this.getRandomElement(perspectives);

    if (options.customPrompt) {
      return `${options.customPrompt}, ${style}, ${lighting}, ${perspective}, construction theme, high quality, professional`;
    }

    return `${topic}, ${style}, ${lighting}, ${perspective}, construction industry, high quality, professional, detailed`;
  }

  public async generateImage(options: ImageGenerationOptions = {}): Promise<{
    url: string;
    prompt: string;
    error?: string;
  }> {
    if (!this.openai) {
      return {
        url: '',
        prompt: '',
        error: 'OpenAI API key not configured'
      };
    }

    try {
      const prompt = this.generatePrompt(options);
      
      const response = await this.openai.images.generate({
        model: "dall-e-3",
        prompt: prompt,
        n: 1,
        size: options.size || "1024x1024",
        quality: options.quality || "standard",
      });

      const imageUrl = response.data?.[0]?.url;
      
      if (!imageUrl) {
        throw new Error('No image URL returned from OpenAI');
      }

      return {
        url: imageUrl,
        prompt: prompt
      };
    } catch (error) {
      console.error('Image generation error:', error);
      return {
        url: '',
        prompt: '',
        error: error instanceof Error ? error.message : 'Unknown error occurred'
      };
    }
  }

  public getAvailableOptions() {
    return {
      topics: constructionTopics,
      styles: constructionStyles,
      lighting: timeOfDay,
      perspectives: perspectives
    };
  }
}

// Image modification utilities
export interface ImageModificationOptions {
  brightness?: number; // -100 to 100
  contrast?: number; // -100 to 100
  saturation?: number; // -100 to 100
  sepia?: boolean;
  grayscale?: boolean;
  blur?: number; // 0 to 10
}

export class ImageModifier {
  public static applyFilters(imageUrl: string, options: ImageModificationOptions): string {
    // Create CSS filter string
    const filters = [];
    
    if (options.brightness !== undefined) {
      filters.push(`brightness(${100 + options.brightness}%)`);
    }
    
    if (options.contrast !== undefined) {
      filters.push(`contrast(${100 + options.contrast}%)`);
    }
    
    if (options.saturation !== undefined) {
      filters.push(`saturate(${100 + options.saturation}%)`);
    }
    
    if (options.sepia) {
      filters.push('sepia(100%)');
    }
    
    if (options.grayscale) {
      filters.push('grayscale(100%)');
    }
    
    if (options.blur !== undefined && options.blur > 0) {
      filters.push(`blur(${options.blur}px)`);
    }
    
    return filters.length > 0 ? filters.join(' ') : 'none';
  }
}