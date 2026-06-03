/**
 * LLM Configuration Component
 * Allows users to configure OpenRouter API key and model selection
 */

import { useState, useEffect } from 'react'
import { useKV } from '@github/spark/hooks'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Switch } from '@/components/ui/switch'
import { Separator } from '@/components/ui/separator'
import { Key, TestTube, CheckCircle, XCircle, Settings, Zap, ExternalLink } from '@phosphor-icons/react'
import { toast } from 'sonner'
import { llmService } from '@/services/LLMService'

interface LLMConfigurationProps {
  onConfigurationChange?: (configured: boolean) => void
}

interface ModelOption {
  id: string
  name: string
  description?: string
  provider?: string
  cost?: string
}

export function LLMConfiguration({ onConfigurationChange }: LLMConfigurationProps) {
  const [apiKey, setApiKey] = useKV<string>('openrouter-api-key', '')
  const [selectedModel, setSelectedModel] = useKV<string>('openrouter-default-model', 'openai/gpt-4o')
  const [testingConnection, setTestingConnection] = useState(false)
  const [connectionStatus, setConnectionStatus] = useState<'idle' | 'testing' | 'success' | 'failed'>('idle')
  const [availableModels, setAvailableModels] = useState<ModelOption[]>([])
  const [loadingModels, setLoadingModels] = useState(false)
  const [useOpenRouter, setUseOpenRouter] = useKV<boolean>('use-openrouter', false)
  const [showApiKey, setShowApiKey] = useState(false)

  const popularModels: ModelOption[] = [
    {
      id: 'openai/gpt-4o',
      name: 'GPT-4o',
      description: 'Latest GPT-4o model with improved performance',
      provider: 'OpenAI',
      cost: 'Medium'
    },
    {
      id: 'openai/gpt-4-turbo',
      name: 'GPT-4 Turbo',
      description: 'Fast and capable GPT-4 variant',
      provider: 'OpenAI',
      cost: 'Medium'
    },
    {
      id: 'anthropic/claude-3.5-sonnet',
      name: 'Claude 3.5 Sonnet',
      description: 'Anthropic\'s most capable model',
      provider: 'Anthropic',
      cost: 'Medium'
    },
    {
      id: 'meta-llama/llama-3.1-405b-instruct',
      name: 'Llama 3.1 405B',
      description: 'Meta\'s largest open-source model',
      provider: 'Meta',
      cost: 'High'
    },
    {
      id: 'google/gemini-pro-1.5',
      name: 'Gemini Pro 1.5',
      description: 'Google\'s multimodal AI model',
      provider: 'Google',
      cost: 'Low'
    }
  ]

  useEffect(() => {
    if (apiKey && useOpenRouter) {
      configureOpenRouter()
    }
    onConfigurationChange?.(Boolean(apiKey && useOpenRouter))
  }, [apiKey, useOpenRouter])

  const configureOpenRouter = async () => {
    if (!apiKey) return

    try {
      llmService.setOpenRouterKey(apiKey, selectedModel)
      toast.success('OpenRouter configured successfully')
    } catch (error) {
      toast.error('Failed to configure OpenRouter')
      console.error('OpenRouter configuration error:', error)
    }
  }

  const testConnection = async () => {
    if (!apiKey) {
      toast.error('Please enter your OpenRouter API key first')
      return
    }

    setTestingConnection(true)
    setConnectionStatus('testing')

    try {
      await llmService.setOpenRouterKey(apiKey, selectedModel)
      const success = await llmService.testConnection()
      
      if (success) {
        setConnectionStatus('success')
        toast.success('OpenRouter connection successful!')
        setUseOpenRouter(true)
      } else {
        setConnectionStatus('failed')
        toast.error('OpenRouter connection test failed')
      }
    } catch (error) {
      setConnectionStatus('failed')
      toast.error(`Connection failed: ${error.message}`)
    } finally {
      setTestingConnection(false)
    }
  }

  const loadAvailableModels = async () => {
    if (!apiKey) return

    setLoadingModels(true)
    try {
      await llmService.setOpenRouterKey(apiKey, selectedModel)
      const models = await llmService.getAvailableModels()
      setAvailableModels(models)
      toast.success(`Loaded ${models.length} available models`)
    } catch (error) {
      toast.error('Failed to load models')
      console.error('Failed to load models:', error)
      setAvailableModels(popularModels)
    } finally {
      setLoadingModels(false)
    }
  }

  const getCurrentStatus = () => {
    const status = llmService.getStatus()
    return status
  }

  const status = getCurrentStatus()

  return (
    <div className="space-y-4">
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Key className="h-5 w-5" />
            LLM Configuration
          </CardTitle>
          <CardDescription>
            Configure your AI language model provider. Use OpenRouter to access multiple LLM providers with your own API key.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          {/* Current Status */}
          <Alert>
            <Settings className="h-4 w-4" />
            <AlertDescription>
              <div className="flex items-center justify-between">
                <span>Current Provider: <strong>{status.provider}</strong></span>
                <Badge variant={status.available ? 'default' : 'secondary'}>
                  {status.available ? 'Available' : 'Not Available'}
                </Badge>
              </div>
            </AlertDescription>
          </Alert>

          <Tabs defaultValue="setup" className="w-full">
            <TabsList className="grid w-full grid-cols-3">
              <TabsTrigger value="setup">Setup</TabsTrigger>
              <TabsTrigger value="models">Models</TabsTrigger>
              <TabsTrigger value="testing">Testing</TabsTrigger>
            </TabsList>

            <TabsContent value="setup" className="space-y-4">
              <div className="space-y-4">
                <div className="flex items-center space-x-2">
                  <Switch
                    id="use-openrouter"
                    checked={useOpenRouter}
                    onCheckedChange={setUseOpenRouter}
                  />
                  <Label htmlFor="use-openrouter">Use OpenRouter (Recommended)</Label>
                </div>

                {useOpenRouter && (
                  <>
                    <Separator />
                    
                    <div className="space-y-2">
                      <Label htmlFor="api-key">OpenRouter API Key</Label>
                      <div className="flex gap-2">
                        <Input
                          id="api-key"
                          type={showApiKey ? 'text' : 'password'}
                          placeholder="sk-or-v1-..."
                          value={apiKey}
                          onChange={(e) => setApiKey(e.target.value)}
                          className="flex-1"
                        />
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => setShowApiKey(!showApiKey)}
                        >
                          {showApiKey ? 'Hide' : 'Show'}
                        </Button>
                      </div>
                      <p className="text-sm text-muted-foreground">
                        Get your API key from{' '}
                        <a 
                          href="https://openrouter.ai/keys" 
                          target="_blank" 
                          rel="noopener noreferrer"
                          className="inline-flex items-center gap-1 text-blue-600 hover:underline"
                        >
                          OpenRouter <ExternalLink className="h-3 w-3" />
                        </a>
                      </p>
                    </div>

                    <div className="space-y-2">
                      <Label htmlFor="default-model">Default Model</Label>
                      <Select value={selectedModel} onValueChange={setSelectedModel}>
                        <SelectTrigger>
                          <SelectValue placeholder="Select a model" />
                        </SelectTrigger>
                        <SelectContent>
                          {popularModels.map((model) => (
                            <SelectItem key={model.id} value={model.id}>
                              <div className="flex items-center gap-2">
                                <span>{model.name}</span>
                                <Badge variant="outline" className="text-xs">
                                  {model.provider}
                                </Badge>
                              </div>
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                      <p className="text-sm text-muted-foreground">
                        Choose the default model for AI operations. You can change this later.
                      </p>
                    </div>
                  </>
                )}

                {!useOpenRouter && (
                  <Alert>
                    <Zap className="h-4 w-4" />
                    <AlertDescription>
                      Using Spark as LLM provider. This requires the Spark runtime to be available.
                      For better control and reliability, consider using OpenRouter.
                    </AlertDescription>
                  </Alert>
                )}
              </div>
            </TabsContent>

            <TabsContent value="models" className="space-y-4">
              <div className="flex items-center justify-between">
                <h3 className="text-lg font-medium">Available Models</h3>
                <Button
                  onClick={loadAvailableModels}
                  disabled={!apiKey || loadingModels}
                  size="sm"
                >
                  {loadingModels ? 'Loading...' : 'Refresh Models'}
                </Button>
              </div>

              <div className="grid gap-3">
                {(availableModels.length > 0 ? availableModels : popularModels).map((model) => (
                  <Card key={model.id} className="p-3">
                    <div className="flex items-center justify-between">
                      <div>
                        <div className="flex items-center gap-2">
                          <h4 className="font-medium">{model.name}</h4>
                          {model.provider && (
                            <Badge variant="outline" className="text-xs">
                              {model.provider}
                            </Badge>
                          )}
                          {model.cost && (
                            <Badge variant="secondary" className="text-xs">
                              {model.cost} Cost
                            </Badge>
                          )}
                        </div>
                        {model.description && (
                          <p className="text-sm text-muted-foreground">{model.description}</p>
                        )}
                        <p className="text-xs text-muted-foreground font-mono">{model.id}</p>
                      </div>
                      <Button
                        variant={selectedModel === model.id ? 'default' : 'outline'}
                        size="sm"
                        onClick={() => setSelectedModel(model.id)}
                      >
                        {selectedModel === model.id ? 'Selected' : 'Select'}
                      </Button>
                    </div>
                  </Card>
                ))}
              </div>
            </TabsContent>

            <TabsContent value="testing" className="space-y-4">
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <h3 className="text-lg font-medium">Connection Test</h3>
                  <Button
                    onClick={testConnection}
                    disabled={!apiKey || testingConnection}
                    className="flex items-center gap-2"
                  >
                    <TestTube className="h-4 w-4" />
                    {testingConnection ? 'Testing...' : 'Test Connection'}
                  </Button>
                </div>

                {connectionStatus !== 'idle' && (
                  <Alert>
                    {connectionStatus === 'success' ? (
                      <CheckCircle className="h-4 w-4" />
                    ) : connectionStatus === 'failed' ? (
                      <XCircle className="h-4 w-4" />
                    ) : (
                      <TestTube className="h-4 w-4" />
                    )}
                    <AlertDescription>
                      {connectionStatus === 'testing' && 'Testing connection to OpenRouter...'}
                      {connectionStatus === 'success' && 'Connection successful! OpenRouter is ready to use.'}
                      {connectionStatus === 'failed' && 'Connection failed. Please check your API key and try again.'}
                    </AlertDescription>
                  </Alert>
                )}

                <div className="space-y-2">
                  <Label>Configuration Summary</Label>
                  <div className="bg-muted p-3 rounded-md">
                    <div className="space-y-1 text-sm">
                      <div>Provider: <strong>{useOpenRouter ? 'OpenRouter' : 'Spark'}</strong></div>
                      {useOpenRouter && (
                        <>
                          <div>API Key: <strong>{apiKey ? '••••••••' : 'Not set'}</strong></div>
                          <div>Default Model: <strong>{selectedModel}</strong></div>
                        </>
                      )}
                      <div>Status: <strong>{status.available ? 'Ready' : 'Not Ready'}</strong></div>
                    </div>
                  </div>
                </div>
              </div>
            </TabsContent>
          </Tabs>
        </CardContent>
      </Card>
    </div>
  )
}