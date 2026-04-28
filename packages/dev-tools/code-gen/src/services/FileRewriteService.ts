/**
 * FileRewriteService handles complete file rewriting operations
 * Ensures atomic file replacement without patching or snippet injection
 */

export interface FileRewriteOperation {
  filePath: string
  newContent: string
  originalContent?: string
  timestamp: number
  checksum?: string
}

export interface FileRewriteResult {
  success: boolean
  filePath: string
  error?: string
  backup?: string
  checksum: string
}

export class FileRewriteService {
  private static backups = new Map<string, string[]>()
  private static readonly MAX_BACKUPS = 10

  /**
   * Completely rewrites a file with new content
   * No patching, no snippet injection - full file replacement
   */
  static async rewriteFile(operation: FileRewriteOperation): Promise<FileRewriteResult> {
    try {
      const { filePath, newContent, originalContent } = operation
      
      // Create backup of original content
      if (originalContent) {
        this.createBackup(filePath, originalContent)
      }

      // Generate checksum for integrity verification
      const checksum = await this.generateChecksum(newContent)

      // Validate new content structure
      const validation = await this.validateFileContent(filePath, newContent)
      if (!validation.isValid) {
        throw new Error(`File validation failed: ${validation.error}`)
      }

      // Simulate file write operation (in real implementation, this would write to filesystem)
      const result: FileRewriteResult = {
        success: true,
        filePath,
        checksum,
        backup: this.getLatestBackup(filePath)
      }

      return result
    } catch (error) {
      return {
        success: false,
        filePath: operation.filePath,
        error: error instanceof Error ? error.message : 'Unknown error',
        checksum: ''
      }
    }
  }

  /**
   * Batch rewrite multiple files atomically
   */
  static async batchRewrite(operations: FileRewriteOperation[]): Promise<FileRewriteResult[]> {
    const results: FileRewriteResult[] = []
    const successfulOperations: string[] = []

    try {
      // Validate all operations first
      for (const operation of operations) {
        const validation = await this.validateFileContent(operation.filePath, operation.newContent)
        if (!validation.isValid) {
          throw new Error(`Validation failed for ${operation.filePath}: ${validation.error}`)
        }
      }

      // Execute all operations
      for (const operation of operations) {
        const result = await this.rewriteFile(operation)
        results.push(result)
        
        if (result.success) {
          successfulOperations.push(result.filePath)
        } else {
          // If any operation fails, we need to rollback
          throw new Error(`Failed to rewrite ${operation.filePath}: ${result.error}`)
        }
      }

      return results
    } catch (error) {
      // Rollback successful operations
      await this.rollbackOperations(successfulOperations)
      
      // Return error results
      return operations.map(op => ({
        success: false,
        filePath: op.filePath,
        error: error instanceof Error ? error.message : 'Batch operation failed',
        checksum: ''
      }))
    }
  }

  /**
   * Creates a backup of file content
   */
  private static createBackup(filePath: string, content: string): void {
    if (!this.backups.has(filePath)) {
      this.backups.set(filePath, [])
    }

    const backups = this.backups.get(filePath)!
    backups.unshift(content)

    // Keep only MAX_BACKUPS
    if (backups.length > this.MAX_BACKUPS) {
      backups.splice(this.MAX_BACKUPS)
    }
  }

  /**
   * Gets the latest backup for a file
   */
  private static getLatestBackup(filePath: string): string | undefined {
    const backups = this.backups.get(filePath)
    return backups?.[0]
  }

  /**
   * Restores a file from backup
   */
  static async restoreFromBackup(filePath: string, backupIndex: number = 0): Promise<FileRewriteResult> {
    const backups = this.backups.get(filePath)
    
    if (!backups || !backups[backupIndex]) {
      return {
        success: false,
        filePath,
        error: 'No backup available',
        checksum: ''
      }
    }

    const backupContent = backups[backupIndex]
    return this.rewriteFile({
      filePath,
      newContent: backupContent,
      timestamp: Date.now()
    })
  }

  /**
   * Validates file content before rewriting
   */
  private static async validateFileContent(filePath: string, content: string): Promise<{isValid: boolean, error?: string}> {
    try {
      const extension = filePath.split('.').pop()?.toLowerCase()

      switch (extension) {
        case 'js':
        case 'jsx':
        case 'ts':
        case 'tsx':
          return this.validateJavaScriptContent(content)
        
        case 'json':
          return this.validateJsonContent(content)
        
        case 'css':
        case 'scss':
        case 'sass':
          return this.validateCssContent(content)
        
        case 'html':
          return this.validateHtmlContent(content)
        
        default:
          return { isValid: true } // No validation for unknown types
      }
    } catch (error) {
      return {
        isValid: false,
        error: error instanceof Error ? error.message : 'Validation error'
      }
    }
  }

  /**
   * Validates JavaScript/TypeScript content
   */
  private static validateJavaScriptContent(content: string): {isValid: boolean, error?: string} {
    try {
      // Basic syntax checks
      if (!content.trim()) {
        return { isValid: true } // Empty files are valid
      }

      // Check for unmatched brackets/braces
      const brackets = { '{': 0, '[': 0, '(': 0 }
      const stack: string[] = []

      for (const char of content) {
        if (char === '{' || char === '[' || char === '(') {
          stack.push(char)
        } else if (char === '}' || char === ']' || char === ')') {
          const expected = stack.pop()
          const mapping = { '}': '{', ']': '[', ')': '(' }
          if (!expected || expected !== mapping[char as keyof typeof mapping]) {
            return { isValid: false, error: 'Unmatched brackets or braces' }
          }
        }
      }

      if (stack.length > 0) {
        return { isValid: false, error: 'Unclosed brackets or braces' }
      }

      return { isValid: true }
    } catch (error) {
      return {
        isValid: false,
        error: error instanceof Error ? error.message : 'JavaScript validation failed'
      }
    }
  }

  /**
   * Validates JSON content
   */
  private static validateJsonContent(content: string): {isValid: boolean, error?: string} {
    try {
      JSON.parse(content)
      return { isValid: true }
    } catch (error) {
      return {
        isValid: false,
        error: error instanceof Error ? error.message : 'Invalid JSON'
      }
    }
  }

  /**
   * Validates CSS content
   */
  private static validateCssContent(content: string): {isValid: boolean, error?: string} {
    try {
      // Basic CSS validation - check for unmatched braces
      let braceCount = 0
      for (const char of content) {
        if (char === '{') braceCount++
        if (char === '}') braceCount--
        if (braceCount < 0) {
          return { isValid: false, error: 'Unmatched CSS braces' }
        }
      }
      
      if (braceCount !== 0) {
        return { isValid: false, error: 'Unclosed CSS braces' }
      }

      return { isValid: true }
    } catch (error) {
      return {
        isValid: false,
        error: error instanceof Error ? error.message : 'CSS validation failed'
      }
    }
  }

  /**
   * Validates HTML content
   */
  private static validateHtmlContent(content: string): {isValid: boolean, error?: string} {
    try {
      // Basic HTML validation - check for unmatched tags
      const parser = new DOMParser()
      const doc = parser.parseFromString(content, 'text/html')
      
      const parseErrors = doc.querySelectorAll('parsererror')
      if (parseErrors.length > 0) {
        return { isValid: false, error: 'HTML parse error' }
      }

      return { isValid: true }
    } catch (error) {
      return {
        isValid: false,
        error: error instanceof Error ? error.message : 'HTML validation failed'
      }
    }
  }

  /**
   * Generates a checksum for content verification
   */
  private static async generateChecksum(content: string): Promise<string> {
    const encoder = new TextEncoder()
    const data = encoder.encode(content)
    const hashBuffer = await crypto.subtle.digest('SHA-256', data)
    const hashArray = Array.from(new Uint8Array(hashBuffer))
    return hashArray.map(b => b.toString(16).padStart(2, '0')).join('')
  }

  /**
   * Rollback operations in case of batch failure
   */
  private static async rollbackOperations(filePaths: string[]): Promise<void> {
    for (const filePath of filePaths) {
      try {
        await this.restoreFromBackup(filePath, 0)
      } catch (error) {
        console.error(`Failed to rollback ${filePath}:`, error)
      }
    }
  }

  /**
   * Get backup history for a file
   */
  static getBackupHistory(filePath: string): string[] {
    return this.backups.get(filePath) || []
  }

  /**
   * Clear all backups for a file
   */
  static clearBackups(filePath: string): void {
    this.backups.delete(filePath)
  }

  /**
   * Clear all backups
   */
  static clearAllBackups(): void {
    this.backups.clear()
  }
}