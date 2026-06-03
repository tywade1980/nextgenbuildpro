// Mock Prisma client since we can't use the real one in this environment
export const prisma = {
  aIModel: {
    findMany: async () => {
      // Mock AI models data
      return [
        {
          id: '1',
          name: 'GPT-4 Turbo',
          type: 'llm',
          provider: 'openai',
          modelId: 'gpt-4-turbo-preview',
          isActive: true,
          configuration: {
            temperature: 0.7,
            max_tokens: 2000,
            top_p: 1,
          },
        },
        {
          id: '2',
          name: 'Claude 3 Haiku',
          type: 'llm',
          provider: 'anthropic',
          modelId: 'claude-3-haiku-20240307',
          isActive: true,
          configuration: {
            temperature: 0.5,
            max_tokens: 1500,
          },
        },
        {
          id: '3',
          name: 'Whisper',
          type: 'speech',
          provider: 'openai',
          modelId: 'whisper-1',
          isActive: true,
          configuration: {
            language: 'en',
          },
        },
      ];
    },
    create: async (data: Record<string, unknown>) => {
      return { id: 'mock-id', ...data };
    }
  },
  client: {
    findMany: async () => [],
    create: async (data: Record<string, unknown>) => data
  },
  project: {
    findMany: async () => [],
    create: async (data: Record<string, unknown>) => data
  },
  call: {
    findMany: async () => [],
    create: async (data: Record<string, unknown>) => data
  },
  $disconnect: async () => {}
};