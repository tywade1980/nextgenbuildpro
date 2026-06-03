import { useKV } from '@github/spark/hooks'

export interface ConversationMemory {
  id: string
  projectId: string
  sessionId: string
  messages: ChatMessage[]
  context: ProjectContext
  learnings: Learning[]
  codePatterns: CodePattern[]
  userPreferences: UserPreferences
  timestamp: number
}

export interface ChatMessage {
  id: string
  role: 'user' | 'assistant' | 'system'
  content: string
  timestamp: number
  projectId: string
  sessionId: string
  metadata?: {
    action?: string
    files_affected?: string[]
    success?: boolean
    confidence?: number
  }
}

export interface ProjectContext {
  name: string
  type: string
  description: string
  dependencies: string[]
  fileStructure: Record<string, FileMetadata>
  recentChanges: Change[]
  objectives: string[]
  constraints: string[]
}

export interface FileMetadata {
  language: string
  size: number
  lastModified: number
  dependencies: string[]
  exports: string[]
  imports: string[]
  complexity: number
}

export interface Learning {
  id: string
  type: 'pattern' | 'preference' | 'solution' | 'bug_fix' | 'optimization'
  description: string
  context: string
  confidence: number
  timestamp: number
  applications: number
}

export interface CodePattern {
  id: string
  name: string
  language: string
  pattern: string
  usage: string
  examples: string[]
  frequency: number
  effectiveness: number
  timestamp: number
}

export interface UserPreferences {
  codingStyle: {
    indentation: 'tabs' | 'spaces'
    indentSize: number
    semicolons: boolean
    quotes: 'single' | 'double'
    trailingComma: boolean
  }
  frameworks: {
    preferred: string[]
    avoided: string[]
  }
  architecture: {
    patterns: string[]
    principles: string[]
  }
  testing: {
    frameworks: string[]
    coverage: number
    types: string[]
  }
}

export interface Change {
  id: string
  type: 'create' | 'modify' | 'delete' | 'rename'
  files: string[]
  description: string
  impact: 'low' | 'medium' | 'high'
  timestamp: number
  author: 'user' | 'ai'
}

export class AgenticMemoryManager {
  private static instance: AgenticMemoryManager
  private memoryStore: Map<string, ConversationMemory> = new Map()
  
  static getInstance(): AgenticMemoryManager {
    if (!AgenticMemoryManager.instance) {
      AgenticMemoryManager.instance = new AgenticMemoryManager()
    }
    return AgenticMemoryManager.instance
  }

  async loadMemory(projectId: string): Promise<ConversationMemory | null> {
    try {
      const memory = await spark.kv.get<ConversationMemory>(`agent-memory-${projectId}`)
      if (memory) {
        this.memoryStore.set(projectId, memory)
        return memory
      }
      return null
    } catch (error) {
      console.error('Failed to load memory:', error)
      return null
    }
  }

  async saveMemory(projectId: string, memory: ConversationMemory): Promise<void> {
    try {
      await spark.kv.set(`agent-memory-${projectId}`, memory)
      this.memoryStore.set(projectId, memory)
    } catch (error) {
      console.error('Failed to save memory:', error)
      throw error
    }
  }

  async addMessage(projectId: string, message: ChatMessage): Promise<void> {
    const memory = await this.getOrCreateMemory(projectId)
    memory.messages.push(message)
    
    // Keep only last 1000 messages for performance
    if (memory.messages.length > 1000) {
      memory.messages = memory.messages.slice(-1000)
    }
    
    await this.saveMemory(projectId, memory)
  }

  async addLearning(projectId: string, learning: Learning): Promise<void> {
    const memory = await this.getOrCreateMemory(projectId)
    
    // Check for similar learnings and update confidence
    const existingLearning = memory.learnings.find(l => 
      l.type === learning.type && 
      l.description.toLowerCase().includes(learning.description.toLowerCase().substring(0, 50))
    )
    
    if (existingLearning) {
      existingLearning.confidence = Math.min(1.0, existingLearning.confidence + 0.1)
      existingLearning.applications += 1
    } else {
      memory.learnings.push(learning)
    }
    
    // Keep only top 100 learnings by confidence
    memory.learnings.sort((a, b) => b.confidence - a.confidence)
    memory.learnings = memory.learnings.slice(0, 100)
    
    await this.saveMemory(projectId, memory)
  }

  async addCodePattern(projectId: string, pattern: CodePattern): Promise<void> {
    const memory = await this.getOrCreateMemory(projectId)
    
    const existingPattern = memory.codePatterns.find(p => 
      p.name === pattern.name && p.language === pattern.language
    )
    
    if (existingPattern) {
      existingPattern.frequency += 1
      existingPattern.effectiveness = (existingPattern.effectiveness + pattern.effectiveness) / 2
    } else {
      memory.codePatterns.push(pattern)
    }
    
    // Keep only top 50 patterns by frequency
    memory.codePatterns.sort((a, b) => b.frequency - a.frequency)
    memory.codePatterns = memory.codePatterns.slice(0, 50)
    
    await this.saveMemory(projectId, memory)
  }

  async updateProjectContext(projectId: string, context: Partial<ProjectContext>): Promise<void> {
    const memory = await this.getOrCreateMemory(projectId)
    memory.context = { ...memory.context, ...context }
    await this.saveMemory(projectId, memory)
  }

  async getRelevantContext(projectId: string, query: string): Promise<{
    relevantMessages: ChatMessage[]
    relevantLearnings: Learning[]
    relevantPatterns: CodePattern[]
    projectContext: ProjectContext
  }> {
    const memory = await this.getOrCreateMemory(projectId)
    
    // Use simple keyword matching for relevance (could be enhanced with embeddings)
    const queryWords = query.toLowerCase().split(' ')
    
    const relevantMessages = memory.messages
      .filter(m => queryWords.some(word => m.content.toLowerCase().includes(word)))
      .slice(-20) // Last 20 relevant messages
    
    const relevantLearnings = memory.learnings
      .filter(l => queryWords.some(word => 
        l.description.toLowerCase().includes(word) || 
        l.context.toLowerCase().includes(word)
      ))
      .slice(0, 10) // Top 10 relevant learnings
    
    const relevantPatterns = memory.codePatterns
      .filter(p => queryWords.some(word => 
        p.name.toLowerCase().includes(word) || 
        p.usage.toLowerCase().includes(word)
      ))
      .slice(0, 5) // Top 5 relevant patterns
    
    return {
      relevantMessages,
      relevantLearnings,
      relevantPatterns,
      projectContext: memory.context
    }
  }

  async buildUnlimitedContext(projectId: string, currentFiles: Record<string, string>): Promise<string> {
    const memory = await this.getOrCreateMemory(projectId)
    
    // Update project context with current files
    const fileStructure: Record<string, FileMetadata> = {}
    Object.entries(currentFiles).forEach(([filename, content]) => {
      fileStructure[filename] = this.analyzeFile(filename, content)
    })
    
    await this.updateProjectContext(projectId, { fileStructure })
    
    // Build comprehensive context
    const context = {
      project: memory.context,
      conversationHistory: memory.messages.slice(-50), // Last 50 messages
      recentLearnings: memory.learnings.slice(0, 20), // Top 20 learnings
      codePatterns: memory.codePatterns.slice(0, 15), // Top 15 patterns
      userPreferences: memory.userPreferences,
      currentFiles: Object.entries(currentFiles).map(([path, content]) => ({
        path,
        content: content.substring(0, 10000), // Limit content size for context
        metadata: fileStructure[path]
      }))
    }
    
    return JSON.stringify(context, null, 2)
  }

  private analyzeFile(filename: string, content: string): FileMetadata {
    const lines = content.split('\n')
    const language = this.getLanguageFromFilename(filename)
    
    // Extract imports and exports (basic analysis)
    const imports = lines
      .filter(line => line.trim().startsWith('import'))
      .map(line => line.trim())
    
    const exports = lines
      .filter(line => line.trim().startsWith('export'))
      .map(line => line.trim())
    
    // Calculate complexity (simple metric)
    const complexity = this.calculateComplexity(content, language)
    
    return {
      language,
      size: content.length,
      lastModified: Date.now(),
      dependencies: this.extractDependencies(imports),
      exports,
      imports,
      complexity
    }
  }

  private getLanguageFromFilename(filename: string): string {
    const extension = filename.split('.').pop()?.toLowerCase()
    const languageMap: Record<string, string> = {
      'js': 'javascript',
      'jsx': 'javascript',
      'ts': 'typescript',
      'tsx': 'typescript',
      'py': 'python',
      'java': 'java',
      'kt': 'kotlin',
      'css': 'css',
      'html': 'html',
      'json': 'json',
      'md': 'markdown'
    }
    return languageMap[extension || ''] || 'text'
  }

  private extractDependencies(imports: string[]): string[] {
    return imports
      .map(imp => {
        const match = imp.match(/from\s+['"]([^'"]+)['"]/)
        return match ? match[1] : null
      })
      .filter(Boolean) as string[]
  }

  private calculateComplexity(content: string, language: string): number {
    // Simple complexity calculation based on keywords and nesting
    let complexity = 0
    const lines = content.split('\n')
    
    const complexityKeywords = ['if', 'for', 'while', 'switch', 'try', 'catch', 'function', 'class']
    
    lines.forEach(line => {
      complexityKeywords.forEach(keyword => {
        if (line.includes(keyword)) {
          complexity += 1
        }
      })
      
      // Count nesting level
      const indentation = line.match(/^\s*/)?.[0].length || 0
      complexity += indentation / 4 // Assuming 4-space indentation
    })
    
    return Math.min(complexity / lines.length, 10) // Normalize to 0-10 scale
  }

  private async getOrCreateMemory(projectId: string): Promise<ConversationMemory> {
    let memory = this.memoryStore.get(projectId)
    
    if (!memory) {
      memory = await this.loadMemory(projectId)
    }
    
    if (!memory) {
      memory = {
        id: Date.now().toString(),
        projectId,
        sessionId: Date.now().toString(),
        messages: [],
        context: {
          name: '',
          type: '',
          description: '',
          dependencies: [],
          fileStructure: {},
          recentChanges: [],
          objectives: [],
          constraints: []
        },
        learnings: [],
        codePatterns: [],
        userPreferences: {
          codingStyle: {
            indentation: 'spaces',
            indentSize: 2,
            semicolons: true,
            quotes: 'single',
            trailingComma: true
          },
          frameworks: {
            preferred: [],
            avoided: []
          },
          architecture: {
            patterns: [],
            principles: []
          },
          testing: {
            frameworks: [],
            coverage: 80,
            types: []
          }
        },
        timestamp: Date.now()
      }
      
      await this.saveMemory(projectId, memory)
    }
    
    this.memoryStore.set(projectId, memory)
    return memory
  }

  async exportMemory(projectId: string): Promise<string> {
    const memory = await this.getOrCreateMemory(projectId)
    return JSON.stringify(memory, null, 2)
  }

  async importMemory(projectId: string, memoryData: string): Promise<void> {
    try {
      const memory = JSON.parse(memoryData) as ConversationMemory
      memory.projectId = projectId // Ensure correct project ID
      await this.saveMemory(projectId, memory)
    } catch (error) {
      throw new Error('Invalid memory data format')
    }
  }

  async clearMemory(projectId: string): Promise<void> {
    await spark.kv.delete(`agent-memory-${projectId}`)
    this.memoryStore.delete(projectId)
  }

  async getAllProjectMemories(): Promise<string[]> {
    const keys = await spark.kv.keys()
    return keys.filter(key => key.startsWith('agent-memory-'))
  }
}

// Export singleton instance
export const memoryManager = AgenticMemoryManager.getInstance()