import React, { useState, useEffect } from 'react'
import { useKV } from '@github/spark/hooks'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Badge } from '@/components/ui/badge'
import { Progress } from '@/components/ui/progress'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { ScrollArea } from '@/components/ui/scroll-area'
import { Code, Robot, Send, History, Lightbulb, Wand, CheckCircle, XCircle } from '@phosphor-icons/react'
import { toast } from 'sonner'

interface AgenticCodeEditorProps {
  project: any
  onUpdateProject: (id: string, updates: any) => void
}

interface ChatMessage {
  id: string
  type: 'user' | 'assistant'
  content: string
  timestamp: number
  codeChanges?: {
    file: string
    before: string
    after: string
  }[]
}

interface ContextMemory {
  projectContext: string
  codePatterns: string[]
  userPreferences: string[]
  recentChanges: string[]
}

export function AgenticCodeEditor({ project, onUpdateProject }: AgenticCodeEditorProps) {
  const [messages, setMessages] = useKV<ChatMessage[]>(`agentic-chat-${project.id}`, [])
  const [contextMemory, setContextMemory] = useKV<ContextMemory>(`agentic-memory-${project.id}`, {
    projectContext: '',
    codePatterns: [],
    userPreferences: [],
    recentChanges: []
  })
  const [input, setInput] = useState('')
  const [isProcessing, setIsProcessing] = useState(false)
  const [suggestions, setSuggestions] = useState<string[]>([])
  const [selectedFiles, setSelectedFiles] = useState<string[]>([])

  // Initialize context when project loads
  useEffect(() => {
    if (project && !contextMemory.projectContext) {
      const context = `
        Project: ${project.name}
        Type: ${project.type}
        Description: ${project.description}
        Files: ${Object.keys(project.codebase?.files || {}).join(', ')}
      `
      setContextMemory(prev => ({ ...prev, projectContext: context }))
    }
  }, [project, contextMemory.projectContext, setContextMemory])

  // Generate intelligent suggestions based on context
  useEffect(() => {
    generateSuggestions()
  }, [project])

  const generateSuggestions = async () => {
    if (!project?.codebase?.files) return

    try {
      const filesList = Object.keys(project.codebase.files)
      const prompt = spark.llmPrompt`
        Based on this ${project.type} project with files: ${filesList.join(', ')}, 
        suggest 5 intelligent code improvements or features to implement.
        Focus on best practices, performance, and modern development patterns.
        Return as a JSON array of strings.
      `

      const response = await spark.llm(prompt, 'gpt-4o-mini', true)
      const suggestions = JSON.parse(response)
      setSuggestions(suggestions.slice(0, 5))
    } catch (error) {
      console.error('Failed to generate suggestions:', error)
    }
  }

  const sendMessage = async () => {
    if (!input.trim() || isProcessing) return

    const userMessage: ChatMessage = {
      id: Date.now().toString(),
      type: 'user',
      content: input,
      timestamp: Date.now()
    }

    setMessages(prev => [...prev, userMessage])
    setInput('')
    setIsProcessing(true)

    try {
      // Build comprehensive context
      const fileContents = Object.entries(project.codebase?.files || {})
        .map(([path, content]) => `// ${path}\n${content}`)
        .join('\n\n')

      const conversationHistory = messages.slice(-10).map(m => 
        `${m.type}: ${m.content}`
      ).join('\n')

      const prompt = spark.llmPrompt`
        You are an expert AI coding assistant with persistent memory and unlimited context.
        
        PROJECT CONTEXT:
        ${contextMemory.projectContext}
        
        CURRENT FILES:
        ${fileContents}
        
        RECENT CONVERSATION:
        ${conversationHistory}
        
        USER REQUEST: ${input}
        
        MEMORY CONTEXT:
        - Code patterns observed: ${contextMemory.codePatterns.join(', ')}
        - User preferences: ${contextMemory.userPreferences.join(', ')}
        - Recent changes: ${contextMemory.recentChanges.join(', ')}
        
        Please provide a comprehensive response that:
        1. Understands the full context of the project
        2. Suggests specific code changes if applicable
        3. Explains the reasoning behind recommendations
        4. Considers the existing codebase and patterns
        
        If making code changes, return them in this JSON format:
        {
          "response": "explanation of changes",
          "codeChanges": [
            {
              "file": "path/to/file",
              "before": "old code snippet",
              "after": "new code snippet",
              "fullContent": "complete new file content"
            }
          ],
          "memoryUpdates": {
            "patterns": ["new patterns learned"],
            "preferences": ["user preferences observed"],
            "changes": ["summary of this change"]
          }
        }
        
        If no code changes, just return: {"response": "your response", "codeChanges": []}
      `

      const response = await spark.llm(prompt, 'gpt-4o', true)
      const parsed = JSON.parse(response)

      const assistantMessage: ChatMessage = {
        id: (Date.now() + 1).toString(),
        type: 'assistant',
        content: parsed.response,
        timestamp: Date.now(),
        codeChanges: parsed.codeChanges || []
      }

      setMessages(prev => [...prev, assistantMessage])

      // Apply code changes if any
      if (parsed.codeChanges && parsed.codeChanges.length > 0) {
        const updatedFiles = { ...project.codebase.files }
        
        parsed.codeChanges.forEach((change: any) => {
          if (change.fullContent) {
            updatedFiles[change.file] = change.fullContent
          } else {
            // Partial replacement
            const currentContent = updatedFiles[change.file] || ''
            updatedFiles[change.file] = currentContent.replace(change.before, change.after)
          }
        })

        onUpdateProject(project.id, {
          codebase: {
            ...project.codebase,
            files: updatedFiles
          }
        })

        toast.success(`Updated ${parsed.codeChanges.length} file(s)`)
      }

      // Update memory with new learnings
      if (parsed.memoryUpdates) {
        setContextMemory(prev => ({
          ...prev,
          codePatterns: [...prev.codePatterns, ...(parsed.memoryUpdates.patterns || [])].slice(-20),
          userPreferences: [...prev.userPreferences, ...(parsed.memoryUpdates.preferences || [])].slice(-10),
          recentChanges: [...prev.recentChanges, ...(parsed.memoryUpdates.changes || [])].slice(-15)
        }))
      }

    } catch (error) {
      console.error('Failed to process message:', error)
      const errorMessage: ChatMessage = {
        id: (Date.now() + 2).toString(),
        type: 'assistant',
        content: 'Sorry, I encountered an error processing your request. Please try again.',
        timestamp: Date.now()
      }
      setMessages(prev => [...prev, errorMessage])
      toast.error('Failed to process your request')
    } finally {
      setIsProcessing(false)
    }
  }

  const applySuggestion = (suggestion: string) => {
    setInput(suggestion)
  }

  const clearConversation = () => {
    setMessages([])
    toast.success('Conversation cleared')
  }

  const exportConversation = () => {
    const conversationData = {
      project: project.name,
      timestamp: new Date().toISOString(),
      messages: messages,
      memory: contextMemory
    }
    
    const blob = new Blob([JSON.stringify(conversationData, null, 2)], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${project.name}-conversation.json`
    a.click()
    URL.revokeObjectURL(url)
  }

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Robot className="w-5 h-5" />
            Agentic Code Editor
          </CardTitle>
          <CardDescription>
            AI-powered code editing with natural language instructions and persistent memory
          </CardDescription>
        </CardHeader>
      </Card>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Chat Interface */}
        <Card className="lg:col-span-2">
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle className="text-lg">Conversation</CardTitle>
              <div className="flex items-center gap-2">
                <Button variant="outline" size="sm" onClick={clearConversation}>
                  <History className="w-4 h-4" />
                </Button>
                <Button variant="outline" size="sm" onClick={exportConversation}>
                  Export
                </Button>
              </div>
            </div>
          </CardHeader>
          <CardContent className="space-y-4">
            <ScrollArea className="h-96 pr-4">
              <div className="space-y-4">
                {messages.length === 0 ? (
                  <div className="text-center py-8 text-muted-foreground">
                    <Robot className="w-8 h-8 mx-auto mb-2 opacity-50" />
                    <p>Start a conversation with your AI coding assistant</p>
                    <p className="text-sm">I remember our previous conversations and project context</p>
                  </div>
                ) : (
                  messages.map(message => (
                    <div key={message.id} className={`space-y-2 ${message.type === 'user' ? 'ml-8' : 'mr-8'}`}>
                      <div className={`p-3 rounded-lg ${
                        message.type === 'user' 
                          ? 'bg-primary text-primary-foreground ml-auto' 
                          : 'bg-muted'
                      }`}>
                        <p className="text-sm whitespace-pre-wrap">{message.content}</p>
                        <p className="text-xs opacity-70 mt-2">
                          {new Date(message.timestamp).toLocaleTimeString()}
                        </p>
                      </div>
                      
                      {message.codeChanges && message.codeChanges.length > 0 && (
                        <div className="space-y-2">
                          {message.codeChanges.map((change, index) => (
                            <Alert key={index}>
                              <CheckCircle className="w-4 h-4" />
                              <AlertDescription>
                                <span className="font-medium">Modified:</span> {change.file}
                              </AlertDescription>
                            </Alert>
                          ))}
                        </div>
                      )}
                    </div>
                  ))
                )}
              </div>
            </ScrollArea>

            <div className="space-y-2">
              <div className="flex gap-2">
                <Input
                  value={input}
                  onChange={(e) => setInput(e.target.value)}
                  placeholder="Describe what you want to implement or ask a question..."
                  onKeyPress={(e) => e.key === 'Enter' && !e.shiftKey && sendMessage()}
                  disabled={isProcessing}
                />
                <Button onClick={sendMessage} disabled={isProcessing || !input.trim()}>
                  <Send className="w-4 h-4" />
                </Button>
              </div>
              
              {isProcessing && (
                <div className="flex items-center gap-2 text-sm text-muted-foreground">
                  <div className="animate-spin w-4 h-4 border-2 border-primary border-t-transparent rounded-full" />
                  Processing with full context...
                </div>
              )}
            </div>
          </CardContent>
        </Card>

        {/* Sidebar with suggestions and context */}
        <div className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle className="text-lg flex items-center gap-2">
                <Lightbulb className="w-5 h-5" />
                Smart Suggestions
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-2">
              {suggestions.map((suggestion, index) => (
                <Button
                  key={index}
                  variant="outline"
                  className="w-full text-left justify-start text-sm h-auto p-3"
                  onClick={() => applySuggestion(suggestion)}
                >
                  <Wand className="w-4 h-4 mr-2 flex-shrink-0" />
                  <span className="truncate">{suggestion}</span>
                </Button>
              ))}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="text-lg">Memory Context</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              <div>
                <h4 className="font-medium text-sm">Recent Changes</h4>
                <div className="space-y-1 mt-1">
                  {contextMemory.recentChanges.slice(-3).map((change, index) => (
                    <Badge key={index} variant="outline" className="text-xs">
                      {change}
                    </Badge>
                  ))}
                </div>
              </div>
              
              <div>
                <h4 className="font-medium text-sm">Code Patterns</h4>
                <div className="space-y-1 mt-1">
                  {contextMemory.codePatterns.slice(-3).map((pattern, index) => (
                    <Badge key={index} variant="secondary" className="text-xs">
                      {pattern}
                    </Badge>
                  ))}
                </div>
              </div>
              
              <div>
                <h4 className="font-medium text-sm">Preferences</h4>
                <div className="space-y-1 mt-1">
                  {contextMemory.userPreferences.slice(-3).map((pref, index) => (
                    <Badge key={index} variant="outline" className="text-xs">
                      {pref}
                    </Badge>
                  ))}
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="text-lg">Quick Actions</CardTitle>
            </CardHeader>
            <CardContent className="space-y-2">
              <Button
                variant="outline"
                size="sm"
                className="w-full"
                onClick={() => setInput('Review my code for best practices and suggest improvements')}
              >
                Code Review
              </Button>
              <Button
                variant="outline"
                size="sm"
                className="w-full"
                onClick={() => setInput('Add error handling and validation to my code')}
              >
                Add Error Handling
              </Button>
              <Button
                variant="outline"
                size="sm"
                className="w-full"
                onClick={() => setInput('Optimize performance and add caching where appropriate')}
              >
                Performance Optimization
              </Button>
              <Button
                variant="outline"
                size="sm"
                className="w-full"
                onClick={() => setInput('Add comprehensive documentation and comments')}
              >
                Add Documentation
              </Button>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
}