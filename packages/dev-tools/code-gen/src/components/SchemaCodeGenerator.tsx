/**
 * Schema Code Generator Component - Generate backend code from database schemas
 */

import { useState, useEffect } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Badge } from '@/components/ui/badge'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { ScrollArea } from '@/components/ui/scroll-area'
import { Switch } from '@/components/ui/switch'
import { Progress } from '@/components/ui/progress'
import { 
  Code2, 
  Database, 
  Download, 
  FileText, 
  Zap,
  Settings,
  CheckCircle,
  AlertTriangle,
  Play,
  Copy,
  Folder,
  Package,
  Braces,
  Server,
  Shield,
  TestTube,
  Container,
  Eye,
  Trash2
} from '@phosphor-icons/react'
import { useKV } from '@github/spark/hooks'
import { toast } from 'sonner'
import { BackendCodeGenerator, BackendFramework, CodeGenerationOptions, GeneratedBackendCode } from '@/services/BackendCodeGenerator'
import { DatabaseSchema } from '@/services/DatabaseService'

interface SchemaCodeGeneratorProps {
  schemas: DatabaseSchema[]
  onCodeGenerated: (code: GeneratedBackendCode) => void
}

export function SchemaCodeGenerator({ schemas, onCodeGenerated }: SchemaCodeGeneratorProps) {
  const [selectedFramework, setSelectedFramework] = useState<BackendFramework>({
    name: 'express',
    language: 'javascript'
  })
  const [generationOptions, setGenerationOptions] = useKV<CodeGenerationOptions>('code-generation-options', {
    framework: selectedFramework,
    features: {
      authentication: true,
      authorization: false,
      validation: true,
      logging: true,
      cors: true,
      swagger: true,
      testing: false,
      dockerization: false,
      errorHandling: true,
      pagination: true,
      caching: false,
      fileUpload: false
    },
    database: {
      provider: 'postgresql'
    },
    apiPrefix: '/api',
    port: 3000
  })
  
  const [generatedCode, setGeneratedCode] = useState<GeneratedBackendCode | null>(null)
  const [generating, setGenerating] = useState(false)
  const [progress, setProgress] = useState(0)
  const [activeTab, setActiveTab] = useState('config')
  const [selectedFile, setSelectedFile] = useState<string | null>(null)
  const [showPreview, setShowPreview] = useState(false)

  const frameworks: { value: BackendFramework; label: string; description: string }[] = [
    {
      value: { name: 'express', language: 'javascript' },
      label: 'Express.js',
      description: 'Fast, unopinionated web framework for Node.js'
    },
    {
      value: { name: 'express', language: 'typescript' },
      label: 'Express.js + TypeScript',
      description: 'Express with TypeScript for type safety'
    },
    {
      value: { name: 'fastify', language: 'javascript' },
      label: 'Fastify',
      description: 'Fast and low overhead web framework'
    },
    {
      value: { name: 'nestjs', language: 'typescript' },
      label: 'NestJS',
      description: 'Progressive Node.js framework with decorators'
    },
    {
      value: { name: 'django', language: 'python' },
      label: 'Django REST',
      description: 'High-level Python web framework'
    },
    {
      value: { name: 'flask', language: 'python' },
      label: 'Flask',
      description: 'Lightweight WSGI web application framework'
    },
    {
      value: { name: 'spring-boot', language: 'java' },
      label: 'Spring Boot',
      description: 'Java framework for building REST APIs'
    },
    {
      value: { name: 'laravel', language: 'php' },
      label: 'Laravel',
      description: 'PHP framework with elegant syntax'
    }
  ]

  const databaseProviders = [
    { value: 'postgresql', label: 'PostgreSQL' },
    { value: 'mysql', label: 'MySQL' },
    { value: 'sqlite', label: 'SQLite' },
    { value: 'mongodb', label: 'MongoDB' },
    { value: 'firebase', label: 'Firebase' },
    { value: 'supabase', label: 'Supabase' }
  ]

  useEffect(() => {
    setGenerationOptions(prev => ({
      ...prev,
      framework: selectedFramework
    }))
  }, [selectedFramework, setGenerationOptions])

  const handleFeatureToggle = (feature: keyof CodeGenerationOptions['features'], enabled: boolean) => {
    setGenerationOptions(prev => ({
      ...prev,
      features: {
        ...prev.features,
        [feature]: enabled
      }
    }))
  }

  const generateBackendCode = async () => {
    if (!schemas || schemas.length === 0) {
      toast.error('No schemas available for code generation')
      return
    }

    setGenerating(true)
    setProgress(0)
    setActiveTab('generation')

    try {
      // Simulate progress steps
      const steps = [
        'Analyzing schemas...',
        'Generating models...',
        'Creating controllers...',
        'Setting up routes...',
        'Adding middleware...',
        'Configuring database...',
        'Finalizing code...'
      ]

      for (let i = 0; i < steps.length; i++) {
        setProgress((i / steps.length) * 100)
        toast.info(steps[i])
        await new Promise(resolve => setTimeout(resolve, 500))
      }

      const code = BackendCodeGenerator.generateBackend(schemas, generationOptions)
      setGeneratedCode(code)
      onCodeGenerated(code)
      setProgress(100)
      setActiveTab('preview')
      
      toast.success(`Backend code generated successfully! ${Object.keys(code.files).length} files created.`)
    } catch (error) {
      toast.error(`Code generation failed: ${error}`)
    } finally {
      setGenerating(false)
    }
  }

  const downloadCode = () => {
    if (!generatedCode) return

    // Create a zip-like structure simulation
    const codeBundle = {
      framework: generatedCode.framework.name,
      files: generatedCode.files,
      dependencies: generatedCode.dependencies,
      readme: generatedCode.readme
    }

    const dataStr = JSON.stringify(codeBundle, null, 2)
    const dataBlob = new Blob([dataStr], { type: 'application/json' })
    const url = URL.createObjectURL(dataBlob)
    
    const link = document.createElement('a')
    link.href = url
    link.download = `${generatedCode.framework.name}-backend-${Date.now()}.json`
    link.click()
    
    URL.revokeObjectURL(url)
    toast.success('Backend code package downloaded')
  }

  const copyFileContent = (content: string) => {
    navigator.clipboard.writeText(content)
    toast.success('File content copied to clipboard')
  }

  const getFileIcon = (filename: string) => {
    const ext = filename.split('.').pop()?.toLowerCase()
    switch (ext) {
      case 'js':
      case 'ts':
        return <Code2 className="w-4 h-4" />
      case 'json':
        return <Braces className="w-4 h-4" />
      case 'py':
        return <FileText className="w-4 h-4" />
      case 'java':
        return <Package className="w-4 h-4" />
      case 'php':
        return <Server className="w-4 h-4" />
      default:
        return <FileText className="w-4 h-4" />
    }
  }

  const getLanguage = (filename: string) => {
    const ext = filename.split('.').pop()?.toLowerCase()
    switch (ext) {
      case 'js': return 'javascript'
      case 'ts': return 'typescript'
      case 'py': return 'python'
      case 'java': return 'java'
      case 'php': return 'php'
      case 'json': return 'json'
      case 'yml':
      case 'yaml': return 'yaml'
      case 'sql': return 'sql'
      case 'md': return 'markdown'
      default: return 'text'
    }
  }

  const FrameworkSelection = () => (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Zap className="w-5 h-5" />
          Framework Selection
        </CardTitle>
        <CardDescription>Choose your backend framework and language</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {frameworks.map((framework) => (
            <Card
              key={`${framework.value.name}-${framework.value.language}`}
              className={`cursor-pointer transition-colors ${
                selectedFramework.name === framework.value.name && 
                selectedFramework.language === framework.value.language
                  ? 'ring-2 ring-primary'
                  : 'hover:bg-muted/50'
              }`}
              onClick={() => setSelectedFramework(framework.value)}
            >
              <CardContent className="p-4">
                <div className="flex items-center justify-between mb-2">
                  <h4 className="font-medium">{framework.label}</h4>
                  <Badge variant="outline">{framework.value.language}</Badge>
                </div>
                <p className="text-sm text-muted-foreground">{framework.description}</p>
              </CardContent>
            </Card>
          ))}
        </div>
      </CardContent>
    </Card>
  )

  const DatabaseConfiguration = () => (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Database className="w-5 h-5" />
          Database Configuration
        </CardTitle>
        <CardDescription>Configure database connection settings</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <div>
          <Label htmlFor="dbProvider">Database Provider</Label>
          <Select
            value={generationOptions.database.provider}
            onValueChange={(value) => setGenerationOptions(prev => ({
              ...prev,
              database: { ...prev.database, provider: value }
            }))}
          >
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {databaseProviders.map(provider => (
                <SelectItem key={provider.value} value={provider.value}>
                  {provider.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        <div>
          <Label htmlFor="dbUrl">Database URL (Optional)</Label>
          <Input
            value={generationOptions.database.url || ''}
            onChange={(e) => setGenerationOptions(prev => ({
              ...prev,
              database: { ...prev.database, url: e.target.value }
            }))}
            placeholder="postgresql://localhost:5432/database"
          />
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <Label htmlFor="apiPrefix">API Prefix</Label>
            <Input
              value={generationOptions.apiPrefix}
              onChange={(e) => setGenerationOptions(prev => ({
                ...prev,
                apiPrefix: e.target.value
              }))}
              placeholder="/api"
            />
          </div>
          <div>
            <Label htmlFor="port">Port</Label>
            <Input
              type="number"
              value={generationOptions.port}
              onChange={(e) => setGenerationOptions(prev => ({
                ...prev,
                port: parseInt(e.target.value) || 3000
              }))}
              placeholder="3000"
            />
          </div>
        </div>
      </CardContent>
    </Card>
  )

  const FeatureConfiguration = () => (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Settings className="w-5 h-5" />
          Feature Configuration
        </CardTitle>
        <CardDescription>Enable or disable backend features</CardDescription>
      </CardHeader>
      <CardContent>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {Object.entries(generationOptions.features).map(([feature, enabled]) => {
            const icons: Record<string, any> = {
              authentication: Shield,
              authorization: Shield,
              validation: CheckCircle,
              logging: FileText,
              cors: Server,
              swagger: FileText,
              testing: TestTube,
              dockerization: Container,
              errorHandling: AlertTriangle,
              pagination: Database,
              caching: Zap,
              fileUpload: Download
            }
            
            const Icon = icons[feature] || Settings

            return (
              <div key={feature} className="flex items-center justify-between p-3 border rounded-lg">
                <div className="flex items-center gap-2">
                  <Icon className="w-4 h-4" />
                  <span className="text-sm font-medium">
                    {feature.charAt(0).toUpperCase() + feature.slice(1).replace(/([A-Z])/g, ' $1')}
                  </span>
                </div>
                <Switch
                  checked={enabled || false}
                  onCheckedChange={(checked) => handleFeatureToggle(feature as keyof CodeGenerationOptions['features'], checked)}
                />
              </div>
            )
          })}
        </div>
      </CardContent>
    </Card>
  )

  const SchemaOverview = () => (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Database className="w-5 h-5" />
          Database Schemas
        </CardTitle>
        <CardDescription>
          {schemas.length} table{schemas.length !== 1 ? 's' : ''} will be processed
        </CardDescription>
      </CardHeader>
      <CardContent>
        <div className="space-y-3">
          {schemas.map((schema, index) => (
            <div key={index} className="flex items-center justify-between p-3 bg-muted rounded-lg">
              <div>
                <h4 className="font-medium">{schema.tableName}</h4>
                <p className="text-sm text-muted-foreground">
                  {schema.columns.length} columns
                </p>
              </div>
              <Badge variant="outline">{schema.columns.length}</Badge>
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  )

  const GenerationProgress = () => (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Zap className="w-5 h-5" />
          Code Generation
        </CardTitle>
        <CardDescription>
          {generating ? 'Generating backend code...' : 'Ready to generate'}
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <Progress value={progress} className="w-full" />
        
        <div className="flex items-center justify-between">
          <span className="text-sm text-muted-foreground">
            {progress}% complete
          </span>
          {generatedCode && (
            <Badge variant="default" className="flex items-center gap-1">
              <CheckCircle className="w-3 h-3" />
              Generated
            </Badge>
          )}
        </div>

        <div className="flex gap-2">
          <Button
            onClick={generateBackendCode}
            disabled={generating || schemas.length === 0}
            className="flex-1"
          >
            <Play className="w-4 h-4 mr-2" />
            {generating ? 'Generating...' : 'Generate Backend Code'}
          </Button>
          
          {generatedCode && (
            <Button variant="outline" onClick={downloadCode}>
              <Download className="w-4 h-4 mr-2" />
              Download
            </Button>
          )}
        </div>
      </CardContent>
    </Card>
  )

  const CodePreview = () => {
    if (!generatedCode) return null

    const fileList = Object.keys(generatedCode.files).sort()
    const currentFile = selectedFile || fileList[0]
    const currentContent = generatedCode.files[currentFile] || ''

    return (
      <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
        <Card className="lg:col-span-1">
          <CardHeader>
            <CardTitle className="text-base flex items-center gap-2">
              <Folder className="w-4 h-4" />
              Files ({fileList.length})
            </CardTitle>
          </CardHeader>
          <CardContent>
            <ScrollArea className="h-96">
              <div className="space-y-1">
                {fileList.map((filename) => (
                  <div
                    key={filename}
                    className={`flex items-center gap-2 p-2 rounded cursor-pointer text-sm ${
                      selectedFile === filename
                        ? 'bg-primary text-primary-foreground'
                        : 'hover:bg-muted'
                    }`}
                    onClick={() => setSelectedFile(filename)}
                  >
                    {getFileIcon(filename)}
                    <span className="truncate">{filename}</span>
                  </div>
                ))}
              </div>
            </ScrollArea>
          </CardContent>
        </Card>

        <Card className="lg:col-span-3">
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle className="text-base flex items-center gap-2">
                {getFileIcon(currentFile)}
                {currentFile}
              </CardTitle>
              <div className="flex gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => copyFileContent(currentContent)}
                >
                  <Copy className="w-4 h-4" />
                </Button>
                <Dialog open={showPreview} onOpenChange={setShowPreview}>
                  <DialogTrigger asChild>
                    <Button variant="outline" size="sm">
                      <Eye className="w-4 h-4" />
                    </Button>
                  </DialogTrigger>
                  <DialogContent className="max-w-4xl max-h-[80vh]">
                    <DialogHeader>
                      <DialogTitle>{currentFile}</DialogTitle>
                    </DialogHeader>
                    <ScrollArea className="h-96">
                      <pre className="text-xs bg-muted p-4 rounded overflow-x-auto">
                        <code>{currentContent}</code>
                      </pre>
                    </ScrollArea>
                  </DialogContent>
                </Dialog>
              </div>
            </div>
          </CardHeader>
          <CardContent>
            <ScrollArea className="h-96">
              <pre className="text-xs bg-muted p-4 rounded overflow-x-auto">
                <code>{currentContent}</code>
              </pre>
            </ScrollArea>
          </CardContent>
        </Card>
      </div>
    )
  }

  if (schemas.length === 0) {
    return (
      <Alert>
        <Database className="w-4 h-4" />
        <AlertDescription>
          No database schemas available. Please create database schemas first to generate backend code.
        </AlertDescription>
      </Alert>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-semibold">Backend Code Generator</h2>
          <p className="text-muted-foreground">
            Generate complete backend APIs from your database schemas
          </p>
        </div>
        <Badge variant="outline" className="flex items-center gap-1">
          <Code2 className="w-3 h-3" />
          {selectedFramework.name} + {selectedFramework.language}
        </Badge>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="grid w-full grid-cols-4">
          <TabsTrigger value="config">Configuration</TabsTrigger>
          <TabsTrigger value="schemas">Schemas</TabsTrigger>
          <TabsTrigger value="generation">Generation</TabsTrigger>
          <TabsTrigger value="preview" disabled={!generatedCode}>
            Preview
          </TabsTrigger>
        </TabsList>

        <TabsContent value="config" className="space-y-6">
          <FrameworkSelection />
          <DatabaseConfiguration />
          <FeatureConfiguration />
        </TabsContent>

        <TabsContent value="schemas" className="space-y-6">
          <SchemaOverview />
        </TabsContent>

        <TabsContent value="generation" className="space-y-6">
          <GenerationProgress />
        </TabsContent>

        <TabsContent value="preview" className="space-y-6">
          <CodePreview />
        </TabsContent>
      </Tabs>
    </div>
  )
}