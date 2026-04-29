import { useState, useEffect } from 'react'
import { useKV } from '@github/spark/hooks'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { Badge } from '@/components/ui/badge'
import { ScrollArea } from '@/components/ui/scroll-area'
import { Separator } from '@/components/ui/separator'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Robot, Send, Code, Lightbulb, MessageSquare, Play, Download, Zap } from '@phosphor-icons/react'
import { toast } from 'sonner'
import { codeGenerationEngine, GenerationRequest, GeneratedApplication } from '@/services/CodeGenerationEngine'

interface Project {
  id: string
  name: string
  description: string
  type: 'react' | 'node' | 'fullstack' | 'android' | 'nextjs' | 'express' | 'fastapi'
  status: 'development' | 'testing' | 'deploying' | 'deployed'
  lastModified: number
  codebase: {
    files: Record<string, string>
    dependencies: string[]
  }
}

interface AIAssistantProps {
  project: Project
  onUpdateProject: (projectId: string, updates: Partial<Project>) => void
}

interface ChatMessage {
  id: string
  type: 'user' | 'assistant'
  content: string
  timestamp: number
  generatedApp?: GeneratedApplication
  codeBlocks?: Array<{
    language: string
    code: string
    filename?: string
  }>
}

export function AIAssistant({ project, onUpdateProject }: AIAssistantProps) {
  const [sessionId] = useState(() => `session_${Date.now()}`)
  const [messages, setMessages] = useKV<ChatMessage[]>(`ai_chat_${project.id}`, [
    {
      id: '1',
      type: 'assistant',
      content: `Hello! I'm your AI development assistant with unlimited context memory. I can generate complete, working applications from natural language descriptions.\n\n🚀 **What I can do:**\n• Generate complete full-stack applications\n• Create working backend APIs with database integration\n• Build responsive frontends with modern frameworks\n• Generate Android apps with Kotlin\n• Write comprehensive tests and documentation\n• Deploy applications to production\n• Maintain context across all our conversations\n\nDescribe what you want to build, and I'll create a complete, production-ready application for you!`,
      timestamp: Date.now()
    }
  ])
  const [input, setInput] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [features, setFeatures] = useState<string[]>([])
  const [newFeature, setNewFeature] = useState('')
  const [selectedDatabase, setSelectedDatabase] = useState<'postgresql' | 'mongodb' | 'mysql' | 'sqlite'>('postgresql')
  const [needsAuth, setNeedsAuth] = useState(false)
  const [deploymentTarget, setDeploymentTarget] = useState<'docker' | 'vercel' | 'aws' | 'gcp'>('docker')

  const handleSendMessage = async () => {
    if (!input.trim()) return

    const userMessage: ChatMessage = {
      id: Date.now().toString(),
      type: 'user',
      content: input,
      timestamp: Date.now()
    }

    setMessages(prev => [...prev, userMessage])
    const currentInput = input
    setInput('')
    setIsLoading(true)

    try {
      // Check if this is a request to generate a new application
      const isAppGenerationRequest = currentInput.toLowerCase().includes('build') || 
                                   currentInput.toLowerCase().includes('create') ||
                                   currentInput.toLowerCase().includes('generate') ||
                                   currentInput.toLowerCase().includes('app')

      if (isAppGenerationRequest) {
        // Generate complete application
        const generationRequest: GenerationRequest = {
          description: currentInput,
          projectType: project.type,
          features: features.length > 0 ? features : [currentInput],
          database: selectedDatabase,
          authentication: needsAuth,
          deployment: deploymentTarget
        }

        const generatedApp = await codeGenerationEngine.generateApplication(generationRequest, sessionId)

        const assistantMessage: ChatMessage = {
          id: (Date.now() + 1).toString(),
          type: 'assistant',
          content: `🎉 **Application Generated Successfully!**\n\nI've created a complete, production-ready ${project.type} application for: "${currentInput}"\n\n**Generated Components:**\n• ${Object.keys(generatedApp.files).length} source files\n• ${Object.keys(generatedApp.dependencies).length} production dependencies\n• ${Object.keys(generatedApp.tests).length} test files\n• Complete database schema${generatedApp.database ? ' with migrations' : ''}\n• Deployment configuration\n• Comprehensive documentation\n\n**Features Implemented:**\n${features.map(f => `• ${f}`).join('\n')}\n\nClick "Apply to Project" to integrate this code into your project, or "Download" to get all files as a package.`,
          timestamp: Date.now(),
          generatedApp
        }

        setMessages(prev => [...prev, assistantMessage])
        toast.success('Complete application generated!')
      } else {
        // Handle general development questions with AI
        const contextPrompt = spark.llmPrompt`
        User question about their ${project.type} project "${project.name}": ${currentInput}
        
        Project files: ${Object.keys(project.codebase.files).join(', ')}
        Dependencies: ${project.codebase.dependencies.join(', ')}
        
        Provide helpful, specific advice or code suggestions. If code is needed, provide working examples.
        Be concise but comprehensive.`

        const aiResponse = await spark.llm(contextPrompt, 'gpt-4o')

        const assistantMessage: ChatMessage = {
          id: (Date.now() + 1).toString(),
          type: 'assistant',
          content: aiResponse,
          timestamp: Date.now()
        }

        setMessages(prev => [...prev, assistantMessage])
        toast.success('AI assistant responded')
      }
    } catch (error) {
      toast.error('Failed to get AI response')
      console.error('AI Assistant error:', error)
      
      const errorMessage: ChatMessage = {
        id: (Date.now() + 1).toString(),
        type: 'assistant',
        content: 'I apologize, but I encountered an error while processing your request. Please try again or rephrase your question.',
        timestamp: Date.now()
      }
      setMessages(prev => [...prev, errorMessage])
    } finally {
      setIsLoading(false)
    }
  }

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSendMessage()
    }
  }

  const applyGeneratedAppToProject = (generatedApp: GeneratedApplication) => {
    // Apply all generated files to the project
    onUpdateProject(project.id, {
      codebase: {
        files: generatedApp.files,
        dependencies: Object.keys(generatedApp.dependencies)
      },
      status: 'development'
    })

    toast.success(`Applied ${Object.keys(generatedApp.files).length} files to project`)
  }

  const downloadGeneratedApp = (generatedApp: GeneratedApplication) => {
    // Create a downloadable package of all files
    const zipContent = {
      ...generatedApp.files,
      'package.json': JSON.stringify({
        name: project.name,
        version: '1.0.0',
        description: project.description,
        dependencies: generatedApp.dependencies,
        devDependencies: generatedApp.devDependencies,
        scripts: generatedApp.scripts
      }, null, 2),
      '.env.example': Object.entries(generatedApp.environment)
        .map(([key, value]) => `${key}=${value}`)
        .join('\n'),
      'README.md': generatedApp.documentation
    }

    // In a real implementation, this would create and download a zip file
    console.log('Generated app files:', zipContent)
    toast.success('Generated app package prepared for download')
  }

  const addFeature = () => {
    if (newFeature.trim() && !features.includes(newFeature.trim())) {
      setFeatures(prev => [...prev, newFeature.trim()])
      setNewFeature('')
    }
  }

  const removeFeature = (feature: string) => {
    setFeatures(prev => prev.filter(f => f !== feature))
  }

  const quickSuggestions = [
    'Build a complete e-commerce platform with shopping cart and payments',
    'Create a social media app with real-time chat',
    'Generate a project management dashboard',
    'Build a blog platform with admin panel',
    'Create a REST API with authentication and database',
    'Generate a mobile-first web application',
    'Build a real-time analytics dashboard',
    'Create a file sharing application'
  ]

  return (
    <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 h-[700px]">
      <Card className="lg:col-span-2 flex flex-col">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Robot className="w-5 h-5" />
            AI Development Assistant
            <Badge variant="outline" className="ml-2">
              <Zap className="w-3 h-3 mr-1" />
              Unlimited Context
            </Badge>
          </CardTitle>
          <CardDescription>
            Generate complete applications with persistent memory across sessions
          </CardDescription>
        </CardHeader>
        <CardContent className="flex-1 flex flex-col">
          <ScrollArea className="flex-1 mb-4 p-4 border rounded-lg">
            <div className="space-y-4">
              {messages.map((message) => (
                <div key={message.id} className={`flex ${message.type === 'user' ? 'justify-end' : 'justify-start'}`}>
                  <div className={`max-w-[80%] p-3 rounded-lg ${
                    message.type === 'user' 
                      ? 'bg-primary text-primary-foreground' 
                      : 'bg-muted'
                  }`}>
                    <div className="whitespace-pre-wrap text-sm">{message.content}</div>
                    
                    {message.generatedApp && (
                      <div className="mt-3 space-y-2">
                        <div className="bg-background border rounded p-3">
                          <div className="flex items-center justify-between mb-2">
                            <div className="flex items-center gap-2">
                              <Code className="w-4 h-4" />
                              <span className="text-xs font-semibold">Generated Application</span>
                            </div>
                            <div className="flex gap-2">
                              <Button 
                                size="sm" 
                                variant="outline"
                                onClick={() => downloadGeneratedApp(message.generatedApp!)}
                              >
                                <Download className="w-3 h-3 mr-1" />
                                Download
                              </Button>
                              <Button 
                                size="sm"
                                onClick={() => applyGeneratedAppToProject(message.generatedApp!)}
                              >
                                <Play className="w-3 h-3 mr-1" />
                                Apply to Project
                              </Button>
                            </div>
                          </div>
                          <div className="text-xs space-y-1">
                            <div className="flex justify-between">
                              <span className="text-muted-foreground">Files:</span>
                              <span>{Object.keys(message.generatedApp.files).length}</span>
                            </div>
                            <div className="flex justify-between">
                              <span className="text-muted-foreground">Dependencies:</span>
                              <span>{Object.keys(message.generatedApp.dependencies).length}</span>
                            </div>
                            <div className="flex justify-between">
                              <span className="text-muted-foreground">Tests:</span>
                              <span>{Object.keys(message.generatedApp.tests).length}</span>
                            </div>
                          </div>
                        </div>
                      </div>
                    )}

                    {message.codeBlocks && (
                      <div className="mt-3 space-y-2">
                        {message.codeBlocks.map((block, index) => (
                          <div key={index} className="bg-background border rounded p-3">
                            <div className="flex items-center justify-between mb-2">
                              <div className="flex items-center gap-2">
                                <Code className="w-4 h-4" />
                                <span className="text-xs font-mono">{block.filename || `${block.language} code`}</span>
                              </div>
                            </div>
                            <pre className="text-xs font-mono overflow-x-auto bg-muted p-2 rounded">
                              <code>{block.code}</code>
                            </pre>
                          </div>
                        ))}
                      </div>
                    )}
                    
                    <div className="text-xs opacity-70 mt-2">
                      {new Date(message.timestamp).toLocaleTimeString()}
                    </div>
                  </div>
                </div>
              ))}
              {isLoading && (
                <div className="flex justify-start">
                  <div className="bg-muted p-3 rounded-lg">
                    <div className="flex items-center gap-2">
                      <div className="w-2 h-2 bg-current rounded-full animate-pulse" />
                      <div className="w-2 h-2 bg-current rounded-full animate-pulse animation-delay-200" />
                      <div className="w-2 h-2 bg-current rounded-full animate-pulse animation-delay-400" />
                      <span className="text-sm text-muted-foreground ml-2">Generating application...</span>
                    </div>
                  </div>
                </div>
              )}
            </div>
          </ScrollArea>
          
          <div className="flex gap-2">
            <Textarea
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyPress={handleKeyPress}
              placeholder="Describe the complete application you want to build..."
              className="flex-1"
              rows={3}
              disabled={isLoading}
            />
            <Button 
              onClick={handleSendMessage} 
              disabled={!input.trim() || isLoading}
              size="sm"
            >
              <Send className="w-4 h-4" />
            </Button>
          </div>
        </CardContent>
      </Card>

      <div className="space-y-4">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Lightbulb className="w-5 h-5" />
              Quick Suggestions
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="space-y-2">
              <h4 className="font-medium text-sm">Application Ideas</h4>
              <div className="space-y-1">
                {quickSuggestions.slice(0, 4).map((suggestion) => (
                  <Button
                    key={suggestion}
                    variant="outline"
                    size="sm"
                    className="w-full justify-start text-xs h-auto p-2 whitespace-normal"
                    onClick={() => setInput(suggestion)}
                  >
                    {suggestion}
                  </Button>
                ))}
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-sm">Application Configuration</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="space-y-2">
              <label className="text-xs font-medium">Features</label>
              <div className="flex gap-1">
                <Input
                  value={newFeature}
                  onChange={(e) => setNewFeature(e.target.value)}
                  placeholder="Add feature..."
                  className="text-xs"
                  onKeyPress={(e) => e.key === 'Enter' && addFeature()}
                />
                <Button size="sm" onClick={addFeature}>+</Button>
              </div>
              <div className="flex flex-wrap gap-1">
                {features.map((feature) => (
                  <Badge key={feature} variant="secondary" className="text-xs">
                    {feature}
                    <button 
                      onClick={() => removeFeature(feature)}
                      className="ml-1 hover:bg-destructive hover:text-destructive-foreground rounded-sm px-1"
                    >
                      ×
                    </button>
                  </Badge>
                ))}
              </div>
            </div>

            <div className="space-y-2">
              <label className="text-xs font-medium">Database</label>
              <Select value={selectedDatabase} onValueChange={(value: any) => setSelectedDatabase(value)}>
                <SelectTrigger className="text-xs">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="postgresql">PostgreSQL</SelectItem>
                  <SelectItem value="mongodb">MongoDB</SelectItem>
                  <SelectItem value="mysql">MySQL</SelectItem>
                  <SelectItem value="sqlite">SQLite</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <label className="text-xs font-medium">Deployment</label>
              <Select value={deploymentTarget} onValueChange={(value: any) => setDeploymentTarget(value)}>
                <SelectTrigger className="text-xs">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="docker">Docker</SelectItem>
                  <SelectItem value="vercel">Vercel</SelectItem>
                  <SelectItem value="aws">AWS</SelectItem>
                  <SelectItem value="gcp">Google Cloud</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="flex items-center space-x-2">
              <input
                id="auth"
                type="checkbox"
                checked={needsAuth}
                onChange={(e) => setNeedsAuth(e.target.checked)}
                className="rounded border border-input"
              />
              <label htmlFor="auth" className="text-xs font-medium">
                Include Authentication
              </label>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}