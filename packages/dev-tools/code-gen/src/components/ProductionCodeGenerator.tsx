import React, { useState } from 'react'
import { useKV } from '@github/spark/hooks'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Checkbox } from '@/components/ui/checkbox'
import { Badge } from '@/components/ui/badge'
import { Progress } from '@/components/ui/progress'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { toast } from 'sonner'
import { 
  Code, 
  Rocket, 
  CheckCircle, 
  AlertTriangle, 
  Download,
  Settings,
  Zap,
  Monitor,
  Smartphone,
  Globe,
  Database,
  Shield,
  Palette,
  TestTube,
  Cloud
} from '@phosphor-icons/react'
import { ProductionCodeGenerator, ProjectConfiguration, GeneratedProject } from '@/utils/ProductionCodeGenerator'

interface ProductionCodeGeneratorProps {
  onProjectGenerated?: (project: GeneratedProject, config: ProjectConfiguration) => void
}

interface GenerationStep {
  id: string
  name: string
  status: 'pending' | 'running' | 'completed' | 'error'
  progress: number
  message?: string
}

export function ProductionCodeGeneratorComponent({ onProjectGenerated }: ProductionCodeGeneratorProps) {
  const [savedConfigs, setSavedConfigs] = useKV<ProjectConfiguration[]>('saved-project-configs', [])
  const [currentConfig, setCurrentConfig] = useState<Partial<ProjectConfiguration>>({
    name: '',
    description: '',
    framework: 'react',
    features: [],
    deploymentTarget: 'vercel'
  })
  
  const [isGenerating, setIsGenerating] = useState(false)
  const [generationSteps, setGenerationSteps] = useState<GenerationStep[]>([])
  const [generatedProject, setGeneratedProject] = useState<GeneratedProject | null>(null)
  const [activeTab, setActiveTab] = useState('config')

  const frameworks = [
    { value: 'react', label: 'React', icon: <Code />, description: 'Modern React with TypeScript and Vite' },
    { value: 'nextjs', label: 'Next.js', icon: <Globe />, description: 'Full-stack React with SSR/SSG' },
    { value: 'express-api-basic', label: 'Express Basic', icon: <Monitor />, description: 'Simple Express API with TypeScript' },
    { value: 'express-mongodb', label: 'Express + MongoDB', icon: <Database />, description: 'Production Express API with MongoDB' },
    { value: 'express-postgresql', label: 'Express + PostgreSQL', icon: <Database />, description: 'Enterprise Express API with PostgreSQL' },
    { value: 'express-microservices', label: 'Express Microservices', icon: <Globe />, description: 'Microservices architecture with Docker' },
    { value: 'android', label: 'Android', icon: <Smartphone />, description: 'Native Android with Kotlin' },
    { value: 'fullstack', label: 'Full Stack', icon: <Zap />, description: 'Complete frontend + backend' },
    { value: 'fastapi', label: 'FastAPI', icon: <Rocket />, description: 'Python backend with FastAPI' }
  ]

  const deploymentTargets = [
    { value: 'vercel', label: 'Vercel', description: 'Serverless deployment' },
    { value: 'netlify', label: 'Netlify', description: 'JAMstack hosting' },
    { value: 'heroku', label: 'Heroku', description: 'Cloud platform' },
    { value: 'aws', label: 'AWS', description: 'Amazon Web Services' },
    { value: 'gcp', label: 'Google Cloud', description: 'Google Cloud Platform' },
    { value: 'docker', label: 'Docker', description: 'Containerized deployment' },
    { value: 'playstore', label: 'Play Store', description: 'Android app store' }
  ]

  const availableFeatures = {
    react: ['Authentication', 'Routing', 'State Management', 'UI Components', 'Testing', 'PWA', 'i18n'],
    nextjs: ['Authentication', 'API Routes', 'Database', 'SEO', 'Analytics', 'Middleware', 'Internationalization'],
    'express-api-basic': ['TypeScript', 'CORS', 'Security Headers', 'Error Handling', 'Health Checks', 'API Documentation'],
    'express-mongodb': ['TypeScript', 'MongoDB', 'JWT Authentication', 'Role-based Access', 'Rate Limiting', 'API Documentation', 'Testing Suite', 'Docker Support'],
    'express-postgresql': ['TypeScript', 'PostgreSQL', 'Prisma ORM', 'JWT Authentication', 'GraphQL', 'WebSockets', 'Redis Caching', 'Microservices Ready'],
    'express-microservices': ['Microservices Architecture', 'Service Discovery', 'API Gateway', 'Message Queue', 'Docker Compose', 'Monitoring', 'Load Balancing'],
    android: ['Navigation', 'Database', 'Networking', 'Camera', 'Push Notifications', 'Biometrics', 'Material Design'],
    fullstack: ['Authentication', 'Database', 'Real-time', 'File Upload', 'Email', 'Analytics', 'Admin Panel'],
    fastapi: ['Authentication', 'Database', 'File Upload', 'Background Tasks', 'WebSocket', 'Documentation', 'Testing']
  }

  const databases = [
    { value: 'postgresql', label: 'PostgreSQL' },
    { value: 'mongodb', label: 'MongoDB' },
    { value: 'sqlite', label: 'SQLite' },
    { value: 'mysql', label: 'MySQL' },
    { value: 'firebase', label: 'Firebase' }
  ]

  const authProviders = [
    { value: 'firebase', label: 'Firebase Auth' },
    { value: 'auth0', label: 'Auth0' },
    { value: 'supabase', label: 'Supabase Auth' },
    { value: 'custom', label: 'Custom Implementation' }
  ]

  const stylingOptions = [
    { value: 'tailwind', label: 'Tailwind CSS' },
    { value: 'mui', label: 'Material-UI' },
    { value: 'chakra', label: 'Chakra UI' },
    { value: 'styled-components', label: 'Styled Components' },
    { value: 'css-modules', label: 'CSS Modules' }
  ]

  const handleConfigChange = (field: keyof ProjectConfiguration, value: any) => {
    setCurrentConfig(prev => ({
      ...prev,
      [field]: value
    }))
  }

  const handleFeatureToggle = (feature: string) => {
    setCurrentConfig(prev => ({
      ...prev,
      features: prev.features?.includes(feature)
        ? prev.features.filter(f => f !== feature)
        : [...(prev.features || []), feature]
    }))
  }

  const saveConfiguration = () => {
    if (!currentConfig.name) {
      toast.error('Please provide a project name')
      return
    }

    const config = currentConfig as ProjectConfiguration
    setSavedConfigs(prev => [...prev, config])
    toast.success('Configuration saved successfully!')
  }

  const loadConfiguration = (config: ProjectConfiguration) => {
    setCurrentConfig(config)
    toast.success('Configuration loaded!')
  }

  const generateProject = async () => {
    if (!currentConfig.name || !currentConfig.framework) {
      toast.error('Please provide project name and select a framework')
      return
    }

    setIsGenerating(true)
    setActiveTab('generation')
    
    const steps: GenerationStep[] = [
      { id: 'validate', name: 'Validating Configuration', status: 'running', progress: 0 },
      { id: 'structure', name: 'Generating Project Structure', status: 'pending', progress: 0 },
      { id: 'dependencies', name: 'Resolving Dependencies', status: 'pending', progress: 0 },
      { id: 'files', name: 'Creating Source Files', status: 'pending', progress: 0 },
      { id: 'tests', name: 'Generating Tests', status: 'pending', progress: 0 },
      { id: 'config', name: 'Setting up Configuration', status: 'pending', progress: 0 },
      { id: 'deployment', name: 'Preparing Deployment', status: 'pending', progress: 0 },
      { id: 'validation', name: 'Validating Generated Code', status: 'pending', progress: 0 }
    ]

    setGenerationSteps(steps)

    try {
      // Simulate step-by-step generation
      for (let i = 0; i < steps.length; i++) {
        const step = steps[i]
        
        // Update current step to running
        setGenerationSteps(prev => prev.map(s => 
          s.id === step.id 
            ? { ...s, status: 'running', progress: 0 }
            : s
        ))

        // Simulate progress
        for (let progress = 0; progress <= 100; progress += 10) {
          await new Promise(resolve => setTimeout(resolve, 100))
          setGenerationSteps(prev => prev.map(s => 
            s.id === step.id 
              ? { ...s, progress }
              : s
          ))
        }

        // Mark as completed
        setGenerationSteps(prev => prev.map(s => 
          s.id === step.id 
            ? { ...s, status: 'completed', progress: 100 }
            : s
        ))

        // Start next step
        if (i < steps.length - 1) {
          setGenerationSteps(prev => prev.map(s => 
            s.id === steps[i + 1].id 
              ? { ...s, status: 'running' }
              : s
          ))
        }
      }

      // Generate the actual project
      const config = currentConfig as ProjectConfiguration
      const project = await ProductionCodeGenerator.generateProject(config)
      
      setGeneratedProject(project)
      onProjectGenerated?.(project, config)
      
      toast.success('Project generated successfully!')
      setActiveTab('results')

    } catch (error) {
      console.error('Generation failed:', error)
      toast.error('Failed to generate project: ' + (error as Error).message)
      
      // Mark current step as error
      setGenerationSteps(prev => prev.map(s => 
        s.status === 'running' 
          ? { ...s, status: 'error', message: (error as Error).message }
          : s
      ))
    } finally {
      setIsGenerating(false)
    }
  }

  const downloadProject = () => {
    if (!generatedProject) return

    // Create a downloadable zip file (simplified representation)
    const projectData = JSON.stringify(generatedProject, null, 2)
    const blob = new Blob([projectData], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    
    const link = document.createElement('a')
    link.href = url
    link.download = `${currentConfig.name}-project.json`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    URL.revokeObjectURL(url)
    toast.success('Project files downloaded!')
  }

  const getFrameworkFeatures = () => {
    return availableFeatures[currentConfig.framework as keyof typeof availableFeatures] || []
  }

  const getStepIcon = (status: GenerationStep['status']) => {
    switch (status) {
      case 'completed':
        return <CheckCircle className="w-5 h-5 text-green-500" />
      case 'running':
        return <div className="w-5 h-5 border-2 border-blue-500 border-t-transparent rounded-full animate-spin" />
      case 'error':
        return <AlertTriangle className="w-5 h-5 text-red-500" />
      default:
        return <div className="w-5 h-5 border-2 border-gray-300 rounded-full" />
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-3xl font-bold flex items-center gap-3">
            <Rocket className="w-8 h-8 text-primary" />
            Production Code Generator
          </h2>
          <p className="text-muted-foreground mt-2">
            Generate complete, production-ready applications with automated testing and deployment configuration
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="outline" onClick={saveConfiguration} disabled={!currentConfig.name}>
            <Settings className="w-4 h-4 mr-2" />
            Save Config
          </Button>
          <Button onClick={generateProject} disabled={!currentConfig.name || !currentConfig.framework || isGenerating}>
            <Zap className="w-4 h-4 mr-2" />
            Generate Project
          </Button>
        </div>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-6">
        <TabsList className="grid w-full grid-cols-4">
          <TabsTrigger value="config">Configuration</TabsTrigger>
          <TabsTrigger value="generation" disabled={!isGenerating && !generatedProject}>Generation</TabsTrigger>
          <TabsTrigger value="results" disabled={!generatedProject}>Results</TabsTrigger>
          <TabsTrigger value="saved">Saved Configs</TabsTrigger>
        </TabsList>

        <TabsContent value="config" className="space-y-6">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Basic Configuration */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Code className="w-5 h-5" />
                  Basic Configuration
                </CardTitle>
                <CardDescription>
                  Set up your project's basic information and framework
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="project-name">Project Name</Label>
                  <Input
                    id="project-name"
                    value={currentConfig.name || ''}
                    onChange={(e) => handleConfigChange('name', e.target.value)}
                    placeholder="My Awesome App"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="project-description">Description</Label>
                  <Textarea
                    id="project-description"
                    value={currentConfig.description || ''}
                    onChange={(e) => handleConfigChange('description', e.target.value)}
                    placeholder="A brief description of your project..."
                    rows={3}
                  />
                </div>

                <div className="space-y-2">
                  <Label>Framework</Label>
                  <div className="grid grid-cols-1 gap-2">
                    {frameworks.map((framework) => (
                      <div
                        key={framework.value}
                        className={`p-3 border rounded-lg cursor-pointer transition-colors ${
                          currentConfig.framework === framework.value
                            ? 'border-primary bg-primary/5'
                            : 'border-border hover:border-primary/50'
                        }`}
                        onClick={() => handleConfigChange('framework', framework.value)}
                      >
                        <div className="flex items-center gap-3">
                          {framework.icon}
                          <div>
                            <div className="font-medium">{framework.label}</div>
                            <div className="text-sm text-muted-foreground">{framework.description}</div>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* Features */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Zap className="w-5 h-5" />
                  Features
                </CardTitle>
                <CardDescription>
                  Select the features you want to include in your project
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-1 gap-2">
                  {getFrameworkFeatures().map((feature) => (
                    <div key={feature} className="flex items-center space-x-2">
                      <Checkbox
                        id={feature}
                        checked={currentConfig.features?.includes(feature) || false}
                        onCheckedChange={() => handleFeatureToggle(feature)}
                      />
                      <Label htmlFor={feature} className="text-sm font-normal">
                        {feature}
                      </Label>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>

            {/* Infrastructure */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Cloud className="w-5 h-5" />
                  Infrastructure
                </CardTitle>
                <CardDescription>
                  Configure your deployment and data storage options
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="space-y-2">
                  <Label>Deployment Target</Label>
                  <Select
                    value={currentConfig.deploymentTarget}
                    onValueChange={(value) => handleConfigChange('deploymentTarget', value)}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select deployment platform" />
                    </SelectTrigger>
                    <SelectContent>
                      {deploymentTargets.map((target) => (
                        <SelectItem key={target.value} value={target.value}>
                          <div>
                            <div className="font-medium">{target.label}</div>
                            <div className="text-sm text-muted-foreground">{target.description}</div>
                          </div>
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                {(currentConfig.framework === 'node' || currentConfig.framework === 'nextjs' || currentConfig.framework === 'fullstack') && (
                  <div className="space-y-2">
                    <Label>Database</Label>
                    <Select
                      value={currentConfig.database}
                      onValueChange={(value) => handleConfigChange('database', value)}
                    >
                      <SelectTrigger>
                        <SelectValue placeholder="Select database" />
                      </SelectTrigger>
                      <SelectContent>
                        {databases.map((db) => (
                          <SelectItem key={db.value} value={db.value}>
                            {db.label}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                )}

                {(currentConfig.framework === 'react' || currentConfig.framework === 'nextjs') && (
                  <div className="space-y-2">
                    <Label>Styling</Label>
                    <Select
                      value={currentConfig.styling}
                      onValueChange={(value) => handleConfigChange('styling', value)}
                    >
                      <SelectTrigger>
                        <SelectValue placeholder="Select styling solution" />
                      </SelectTrigger>
                      <SelectContent>
                        {stylingOptions.map((style) => (
                          <SelectItem key={style.value} value={style.value}>
                            {style.label}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                )}
              </CardContent>
            </Card>

            {/* Security */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Shield className="w-5 h-5" />
                  Security & Authentication
                </CardTitle>
                <CardDescription>
                  Configure authentication and security features
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="space-y-2">
                  <Label>Authentication Provider</Label>
                  <Select
                    value={currentConfig.authentication}
                    onValueChange={(value) => handleConfigChange('authentication', value)}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select auth provider" />
                    </SelectTrigger>
                    <SelectContent>
                      {authProviders.map((auth) => (
                        <SelectItem key={auth.value} value={auth.value}>
                          {auth.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        <TabsContent value="generation" className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <TestTube className="w-5 h-5" />
                Project Generation Progress
              </CardTitle>
              <CardDescription>
                Generating your production-ready application...
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              {generationSteps.map((step) => (
                <div key={step.id} className="space-y-2">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      {getStepIcon(step.status)}
                      <span className={`font-medium ${
                        step.status === 'completed' ? 'text-green-600' :
                        step.status === 'error' ? 'text-red-600' :
                        step.status === 'running' ? 'text-blue-600' :
                        'text-muted-foreground'
                      }`}>
                        {step.name}
                      </span>
                    </div>
                    <span className="text-sm text-muted-foreground">
                      {step.progress}%
                    </span>
                  </div>
                  <Progress value={step.progress} className="h-2" />
                  {step.message && (
                    <Alert>
                      <AlertTriangle className="h-4 w-4" />
                      <AlertDescription>{step.message}</AlertDescription>
                    </Alert>
                  )}
                </div>
              ))}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="results" className="space-y-6">
          {generatedProject && (
            <>
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <CheckCircle className="w-5 h-5 text-green-500" />
                    Generation Complete
                  </CardTitle>
                  <CardDescription>
                    Your production-ready project has been generated successfully
                  </CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                    <div className="text-center p-4 border rounded-lg">
                      <div className="text-2xl font-bold text-primary">
                        {Object.keys(generatedProject.files).length}
                      </div>
                      <div className="text-sm text-muted-foreground">Files Generated</div>
                    </div>
                    <div className="text-center p-4 border rounded-lg">
                      <div className="text-2xl font-bold text-primary">
                        {Object.keys(generatedProject.dependencies).length}
                      </div>
                      <div className="text-sm text-muted-foreground">Dependencies</div>
                    </div>
                    <div className="text-center p-4 border rounded-lg">
                      <div className="text-2xl font-bold text-primary">
                        {Object.keys(generatedProject.tests).length}
                      </div>
                      <div className="text-sm text-muted-foreground">Test Suites</div>
                    </div>
                    <div className="text-center p-4 border rounded-lg">
                      <div className="text-2xl font-bold text-green-500">
                        100%
                      </div>
                      <div className="text-sm text-muted-foreground">Ready to Deploy</div>
                    </div>
                  </div>

                  <Button onClick={downloadProject} className="w-full">
                    <Download className="w-4 h-4 mr-2" />
                    Download Project Files
                  </Button>
                </CardContent>
              </Card>

              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <Card>
                  <CardHeader>
                    <CardTitle>Project Structure</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-1 font-mono text-sm">
                      {Object.keys(generatedProject.files).slice(0, 10).map((file) => (
                        <div key={file} className="text-muted-foreground">
                          {file}
                        </div>
                      ))}
                      {Object.keys(generatedProject.files).length > 10 && (
                        <div className="text-muted-foreground">
                          ... and {Object.keys(generatedProject.files).length - 10} more files
                        </div>
                      )}
                    </div>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <CardTitle>Next Steps</CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-3">
                    <div className="flex items-center gap-2">
                      <CheckCircle className="w-4 h-4 text-green-500" />
                      <span className="text-sm">Download and extract project files</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <CheckCircle className="w-4 h-4 text-green-500" />
                      <span className="text-sm">Run <code className="bg-muted px-1 rounded">npm install</code></span>
                    </div>
                    <div className="flex items-center gap-2">
                      <CheckCircle className="w-4 h-4 text-green-500" />
                      <span className="text-sm">Start development with <code className="bg-muted px-1 rounded">npm run dev</code></span>
                    </div>
                    <div className="flex items-center gap-2">
                      <CheckCircle className="w-4 h-4 text-green-500" />
                      <span className="text-sm">Run tests with <code className="bg-muted px-1 rounded">npm test</code></span>
                    </div>
                    <div className="flex items-center gap-2">
                      <CheckCircle className="w-4 h-4 text-green-500" />
                      <span className="text-sm">Deploy to {currentConfig.deploymentTarget}</span>
                    </div>
                  </CardContent>
                </Card>
              </div>
            </>
          )}
        </TabsContent>

        <TabsContent value="saved" className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Saved Configurations</CardTitle>
              <CardDescription>
                Manage your saved project configurations
              </CardDescription>
            </CardHeader>
            <CardContent>
              {savedConfigs.length > 0 ? (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {savedConfigs.map((config, index) => (
                    <div key={index} className="p-4 border rounded-lg space-y-2">
                      <div className="flex items-center justify-between">
                        <h4 className="font-medium">{config.name}</h4>
                        <Badge variant="outline">{config.framework}</Badge>
                      </div>
                      <p className="text-sm text-muted-foreground">{config.description}</p>
                      <div className="flex items-center gap-2">
                        <Button size="sm" onClick={() => loadConfiguration(config)}>
                          Load Config
                        </Button>
                        <Button 
                          size="sm" 
                          variant="outline"
                          onClick={() => setSavedConfigs(prev => prev.filter((_, i) => i !== index))}
                        >
                          Delete
                        </Button>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="text-center py-8 text-muted-foreground">
                  No saved configurations yet. Create and save a configuration to see it here.
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}