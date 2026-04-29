import React, { useState, useEffect } from 'react'
import { useKV } from '@github/spark/hooks'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Progress } from '@/components/ui/progress'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { ScrollArea } from '@/components/ui/scroll-area'
import { Switch } from '@/components/ui/switch'
import { Textarea } from '@/components/ui/textarea'
import { Separator } from '@/components/ui/separator'
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { Rocket, Cloud, Server, Database, GitBranch, CheckCircle, XCircle, Clock, Settings, Play, Pause, RefreshCw, Eye, ArrowRight, Terminal, Shield, Zap, Globe } from '@phosphor-icons/react'
import { toast } from 'sonner'

interface DeploymentPipelineProps {
  project: any
  onUpdateProject: (id: string, updates: any) => void
}

interface DeploymentStep {
  id: string
  name: string
  status: 'pending' | 'running' | 'completed' | 'failed' | 'skipped'
  duration?: number
  logs?: string[]
  error?: string
  output?: string
}

interface EnvironmentConfig {
  id: string
  name: string
  type: 'development' | 'staging' | 'production'
  url?: string
  branch: string
  autoPromote: boolean
  requiresApproval: boolean
  variables: Record<string, string>
  secrets: Record<string, string>
  healthChecks: string[]
  rollbackEnabled: boolean
}

interface Pipeline {
  id: string
  name: string
  trigger: 'manual' | 'push' | 'pr' | 'schedule'
  environments: string[]
  steps: PipelineStep[]
  enabled: boolean
  lastRun?: number
  successRate: number
}

interface PipelineStep {
  id: string
  name: string
  type: 'build' | 'test' | 'security' | 'deploy' | 'verify' | 'promote'
  command?: string
  environment?: string
  condition?: string
  parallel?: boolean
  timeout: number
}

interface Deployment {
  id: string
  pipelineId: string
  environment: string
  status: 'pending' | 'building' | 'testing' | 'deploying' | 'verifying' | 'deployed' | 'failed' | 'rolled-back'
  progress: number
  steps: DeploymentStep[]
  startTime: number
  endTime?: number
  url?: string
  version: string
  triggeredBy: string
  approvals: Array<{
    user: string
    approved: boolean
    timestamp: number
    comment?: string
  }>
  rollbackAvailable: boolean
  healthStatus: 'healthy' | 'degraded' | 'unhealthy' | 'unknown'
  metrics?: {
    responseTime: number
    errorRate: number
    throughput: number
  }
}

const deploymentTargets = [
  { id: 'vercel', name: 'Vercel', description: 'Best for frontend apps', icon: Cloud, staging: true },
  { id: 'netlify', name: 'Netlify', description: 'JAMstack deployment', icon: Cloud, staging: true },
  { id: 'aws', name: 'AWS ECS', description: 'Container deployment', icon: Server, staging: true },
  { id: 'aws-lambda', name: 'AWS Lambda', description: 'Serverless functions', icon: Zap, staging: true },
  { id: 'gcp', name: 'Google Cloud Run', description: 'Container platform', icon: Cloud, staging: true },
  { id: 'azure', name: 'Azure Container', description: 'Microsoft cloud', icon: Cloud, staging: true },
  { id: 'heroku', name: 'Heroku', description: 'Simple app deployment', icon: Cloud, staging: true },
  { id: 'docker', name: 'Docker Registry', description: 'Container images', icon: Database, staging: false },
  { id: 'kubernetes', name: 'Kubernetes', description: 'Container orchestration', icon: Server, staging: true },
  { id: 'railway', name: 'Railway', description: 'Modern deployment', icon: Cloud, staging: true },
  { id: 'fly', name: 'Fly.io', description: 'Edge deployment', icon: Globe, staging: true }
]

const defaultEnvironments: EnvironmentConfig[] = [
  {
    id: 'development',
    name: 'Development',
    type: 'development',
    branch: 'develop',
    autoPromote: false,
    requiresApproval: false,
    variables: { NODE_ENV: 'development', DEBUG: 'true' },
    secrets: {},
    healthChecks: ['/health'],
    rollbackEnabled: true
  },
  {
    id: 'staging',
    name: 'Staging',
    type: 'staging',
    branch: 'staging',
    autoPromote: false,
    requiresApproval: false,
    variables: { NODE_ENV: 'staging' },
    secrets: {},
    healthChecks: ['/health', '/api/status'],
    rollbackEnabled: true
  },
  {
    id: 'production',
    name: 'Production',
    type: 'production',
    branch: 'main',
    autoPromote: false,
    requiresApproval: true,
    variables: { NODE_ENV: 'production' },
    secrets: {},
    healthChecks: ['/health', '/api/status', '/api/metrics'],
    rollbackEnabled: true
  }
]

const defaultPipelines: Pipeline[] = [
  {
    id: 'main',
    name: 'Main Deployment Pipeline',
    trigger: 'push',
    environments: ['development', 'staging', 'production'],
    enabled: true,
    successRate: 95,
    steps: [
      { id: 'build', name: 'Build Application', type: 'build', timeout: 300 },
      { id: 'unit-tests', name: 'Unit Tests', type: 'test', timeout: 180 },
      { id: 'security-scan', name: 'Security Scan', type: 'security', timeout: 120 },
      { id: 'deploy-dev', name: 'Deploy to Development', type: 'deploy', environment: 'development', timeout: 300 },
      { id: 'integration-tests', name: 'Integration Tests', type: 'test', environment: 'development', timeout: 240 },
      { id: 'promote-staging', name: 'Promote to Staging', type: 'promote', environment: 'staging', timeout: 180 },
      { id: 'e2e-tests', name: 'E2E Tests', type: 'test', environment: 'staging', timeout: 600 },
      { id: 'performance-tests', name: 'Performance Tests', type: 'test', environment: 'staging', timeout: 300 },
      { id: 'promote-prod', name: 'Promote to Production', type: 'promote', environment: 'production', condition: 'manual', timeout: 300 },
      { id: 'verify-prod', name: 'Verify Production', type: 'verify', environment: 'production', timeout: 180 }
    ]
  },
  {
    id: 'hotfix',
    name: 'Hotfix Pipeline',
    trigger: 'manual',
    environments: ['production'],
    enabled: true,
    successRate: 88,
    steps: [
      { id: 'build', name: 'Build Hotfix', type: 'build', timeout: 180 },
      { id: 'critical-tests', name: 'Critical Tests', type: 'test', timeout: 120 },
      { id: 'deploy-prod', name: 'Deploy to Production', type: 'deploy', environment: 'production', timeout: 240 },
      { id: 'verify', name: 'Verify Hotfix', type: 'verify', environment: 'production', timeout: 120 }
    ]
  }
]

export function DeploymentPipeline({ project, onUpdateProject }: DeploymentPipelineProps) {
  const [deployments, setDeployments] = useKV<Deployment[]>(`deployments-${project.id}`, [])
  const [environments, setEnvironments] = useKV<EnvironmentConfig[]>(`environments-${project.id}`, defaultEnvironments)
  const [pipelines, setPipelines] = useKV<Pipeline[]>(`pipelines-${project.id}`, defaultPipelines)
  const [selectedTarget, setSelectedTarget] = useState('')
  const [selectedPipeline, setSelectedPipeline] = useState('main')
  const [isDeploying, setIsDeploying] = useState(false)
  const [selectedEnvironment, setSelectedEnvironment] = useState<EnvironmentConfig | null>(null)
  const [showEnvironmentConfig, setShowEnvironmentConfig] = useState(false)
  const [showPipelineConfig, setShowPipelineConfig] = useState(false)
  const [newEnvVar, setNewEnvVar] = useState({ key: '', value: '' })
  const [activePipeline, setActivePipeline] = useState<Pipeline | null>(null)

  const generateAdvancedDeploymentSteps = (target: string, projectType: string, environment: string): DeploymentStep[] => {
    const baseSteps = [
      { id: 'pre-build', name: 'Pre-build Validation', status: 'pending' as const },
      { id: 'install-deps', name: 'Install Dependencies', status: 'pending' as const },
      { id: 'lint', name: 'Code Linting', status: 'pending' as const },
      { id: 'security-audit', name: 'Security Audit', status: 'pending' as const },
      { id: 'build', name: 'Build Application', status: 'pending' as const },
      { id: 'unit-tests', name: 'Unit Tests', status: 'pending' as const },
      { id: 'bundle-analysis', name: 'Bundle Analysis', status: 'pending' as const },
      { id: 'artifact-creation', name: 'Create Artifacts', status: 'pending' as const }
    ]

    const environmentSteps = environment === 'production' 
      ? [
          { id: 'backup', name: 'Create Backup', status: 'pending' as const },
          { id: 'blue-green-setup', name: 'Blue-Green Setup', status: 'pending' as const }
        ]
      : []

    const targetSpecificSteps: Record<string, DeploymentStep[]> = {
      vercel: [
        { id: 'vercel-config', name: 'Generate Vercel Config', status: 'pending' as const },
        { id: 'domain-setup', name: 'Configure Domain', status: 'pending' as const },
        { id: 'edge-functions', name: 'Deploy Edge Functions', status: 'pending' as const },
        { id: 'deploy-vercel', name: 'Deploy to Vercel', status: 'pending' as const },
        { id: 'preview-url', name: 'Generate Preview URL', status: 'pending' as const }
      ],
      netlify: [
        { id: 'netlify-config', name: 'Generate Netlify Config', status: 'pending' as const },
        { id: 'functions-deploy', name: 'Deploy Functions', status: 'pending' as const },
        { id: 'form-handlers', name: 'Setup Form Handlers', status: 'pending' as const },
        { id: 'deploy-netlify', name: 'Deploy to Netlify', status: 'pending' as const }
      ],
      aws: [
        { id: 'ecr-push', name: 'Push to ECR', status: 'pending' as const },
        { id: 'task-definition', name: 'Update Task Definition', status: 'pending' as const },
        { id: 'service-update', name: 'Update ECS Service', status: 'pending' as const },
        { id: 'load-balancer', name: 'Configure Load Balancer', status: 'pending' as const },
        { id: 'auto-scaling', name: 'Setup Auto Scaling', status: 'pending' as const }
      ],
      'aws-lambda': [
        { id: 'lambda-package', name: 'Package Lambda', status: 'pending' as const },
        { id: 'api-gateway', name: 'Configure API Gateway', status: 'pending' as const },
        { id: 'lambda-deploy', name: 'Deploy Lambda Functions', status: 'pending' as const },
        { id: 'cloudwatch', name: 'Setup CloudWatch', status: 'pending' as const }
      ],
      kubernetes: [
        { id: 'docker-build', name: 'Build Docker Image', status: 'pending' as const },
        { id: 'k8s-manifests', name: 'Generate K8s Manifests', status: 'pending' as const },
        { id: 'secrets-config', name: 'Configure Secrets', status: 'pending' as const },
        { id: 'rolling-deploy', name: 'Rolling Deployment', status: 'pending' as const },
        { id: 'service-mesh', name: 'Configure Service Mesh', status: 'pending' as const }
      ]
    }

    const postDeploySteps = [
      { id: 'health-check', name: 'Health Check', status: 'pending' as const },
      { id: 'smoke-tests', name: 'Smoke Tests', status: 'pending' as const },
      { id: 'performance-check', name: 'Performance Check', status: 'pending' as const },
      { id: 'monitoring-setup', name: 'Setup Monitoring', status: 'pending' as const },
      { id: 'notification', name: 'Send Notifications', status: 'pending' as const }
    ]

    return [...baseSteps, ...environmentSteps, ...(targetSpecificSteps[target] || []), ...postDeploySteps]
  }

  const runPipeline = async (pipelineId: string, targetEnvironment?: string) => {
    const pipeline = pipelines.find(p => p.id === pipelineId)
    if (!pipeline || !selectedTarget) {
      toast.error('Please select a pipeline and deployment target')
      return
    }

    setActivePipeline(pipeline)
    setIsDeploying(true)

    // Run pipeline for each environment or specific environment
    const envsToRun = targetEnvironment 
      ? [targetEnvironment] 
      : pipeline.environments

    for (const envId of envsToRun) {
      const environment = environments.find(e => e.id === envId)
      if (!environment) continue

      // Check if approval is required
      if (environment.requiresApproval && environment.type === 'production') {
        toast.info(`Deployment to ${environment.name} requires approval`)
        // In a real implementation, this would wait for approval
        // For demo, we'll simulate approval after 3 seconds
        await new Promise(resolve => setTimeout(resolve, 3000))
      }

      const steps = generateAdvancedDeploymentSteps(selectedTarget, project.type, envId)
      const version = `v${Date.now()}`
      
      const newDeployment: Deployment = {
        id: Date.now().toString(),
        pipelineId: pipeline.id,
        environment: envId,
        status: 'building',
        progress: 0,
        steps,
        startTime: Date.now(),
        version,
        triggeredBy: 'manual', // In real app, would get from auth context
        approvals: [],
        rollbackAvailable: environment.rollbackEnabled,
        healthStatus: 'unknown'
      }

      setDeployments(prev => [...prev, newDeployment])

      try {
        // Generate comprehensive deployment configuration
        const prompt = spark.llmPrompt`
          Generate comprehensive deployment configuration for a ${project.type} project.
          
          Target: ${selectedTarget}
          Environment: ${environment.name} (${environment.type})
          Pipeline: ${pipeline.name}
          Branch: ${environment.branch}
          
          Project Details:
          - Name: ${project.name}
          - Type: ${project.type}
          - Files: ${Object.keys(project.codebase?.files || {}).join(', ')}
          
          Environment Configuration:
          - Variables: ${JSON.stringify(environment.variables)}
          - Health Checks: ${environment.healthChecks.join(', ')}
          - Auto Promote: ${environment.autoPromote}
          - Requires Approval: ${environment.requiresApproval}
          
          Generate:
          1. Deployment configuration files (docker, k8s, serverless)
          2. CI/CD pipeline configuration (GitHub Actions, GitLab CI)
          3. Infrastructure as Code (Terraform, CloudFormation)
          4. Monitoring and alerting setup
          5. Security configurations
          6. Environment-specific build scripts
          7. Database migration scripts
          8. Load balancer and CDN configuration
          9. Backup and disaster recovery scripts
          10. Compliance and audit configurations
          
          Return as JSON with:
          {
            "files": { "path": "content" },
            "infrastructure": { ... },
            "monitoring": { ... },
            "security": { ... },
            "instructions": "deployment instructions"
          }
        `

        // Execute deployment steps with realistic timing and potential failures
        for (let i = 0; i < steps.length; i++) {
          const step = steps[i]
          
          // Update step to running
          setDeployments(prev => prev.map(dep => 
            dep.id === newDeployment.id
              ? {
                  ...dep,
                  steps: dep.steps.map(s => 
                    s.id === step.id ? { ...s, status: 'running' } : s
                  ),
                  progress: ((i) / steps.length) * 100
                }
              : dep
          ))

          // Simulate realistic step durations
          const baseDuration = getStepBaseDuration(step.id)
          const duration = baseDuration + Math.random() * 2000
          await new Promise(resolve => setTimeout(resolve, duration))

          // Determine step success with environment-specific failure rates
          const failureRate = environment.type === 'production' ? 0.02 : 0.05
          const stepStatus = Math.random() > failureRate ? 'completed' : 'failed'
          
          const stepOutput = generateStepOutput(step.id, stepStatus, selectedTarget)
          
          setDeployments(prev => prev.map(dep => 
            dep.id === newDeployment.id
              ? {
                  ...dep,
                  steps: dep.steps.map(s => 
                    s.id === step.id 
                      ? { 
                          ...s, 
                          status: stepStatus, 
                          duration: Math.floor(duration / 1000),
                          output: stepOutput,
                          error: stepStatus === 'failed' ? generateErrorMessage(step.id) : undefined
                        }
                      : s
                  ),
                  progress: ((i + 1) / steps.length) * 100
                }
              : dep
          ))

          if (stepStatus === 'failed') {
            // Mark deployment as failed and enable rollback
            setDeployments(prev => prev.map(dep => 
              dep.id === newDeployment.id
                ? { 
                    ...dep, 
                    status: 'failed', 
                    endTime: Date.now(),
                    healthStatus: 'unhealthy'
                  }
                : dep
            ))
            toast.error(`Deployment failed at step: ${step.name}`)
            setIsDeploying(false)
            setActivePipeline(null)
            return
          }
        }

        // Generate deployment configurations
        const response = await spark.llm(prompt, 'gpt-4o', true)
        const deploymentConfig = JSON.parse(response)

        // Add deployment files to project
        if (deploymentConfig.files) {
          const updatedFiles = { ...project.codebase.files }
          Object.entries(deploymentConfig.files).forEach(([path, content]: [string, any]) => {
            updatedFiles[path] = content
          })
          
          onUpdateProject(project.id, {
            codebase: {
              ...project.codebase,
              files: updatedFiles
            }
          })
        }

        // Generate deployment URL
        const deploymentUrl = generateDeploymentUrl(project.name, environment.type, selectedTarget, version)

        // Complete deployment with health metrics
        const healthStatus = await simulateHealthCheck(deploymentUrl)
        const metrics = generateMockMetrics()

        setDeployments(prev => prev.map(dep => 
          dep.id === newDeployment.id
            ? {
                ...dep,
                status: 'deployed',
                endTime: Date.now(),
                url: deploymentUrl,
                healthStatus,
                metrics
              }
            : dep
        ))

        // Update environment URL
        setEnvironments(prev => prev.map(env =>
          env.id === envId ? { ...env, url: deploymentUrl } : env
        ))

        // Auto-promote to next environment if configured
        if (environment.autoPromote && environment.type !== 'production') {
          const nextEnvIndex = envsToRun.indexOf(envId) + 1
          if (nextEnvIndex < envsToRun.length) {
            toast.info(`Auto-promoting to ${envsToRun[nextEnvIndex]}`)
            // Continue with next environment
          }
        }

        toast.success(`Successfully deployed to ${environment.name}!`)

      } catch (error) {
        console.error('Deployment failed:', error)
        setDeployments(prev => prev.map(dep => 
          dep.id === newDeployment.id
            ? { 
                ...dep, 
                status: 'failed', 
                endTime: Date.now(),
                healthStatus: 'unhealthy'
              }
            : dep
        ))
        toast.error(`Deployment to ${environment.name} failed`)
      }
    }

    // Update project status based on production deployment
    if (envsToRun.includes('production')) {
      onUpdateProject(project.id, {
        status: 'deployed'
      })
    }

    setIsDeploying(false)
    setActivePipeline(null)
  }

  // Utility functions for deployment simulation
  const getStepBaseDuration = (stepId: string): number => {
    const durations: Record<string, number> = {
      'pre-build': 1000,
      'install-deps': 3000,
      'lint': 2000,
      'security-audit': 4000,
      'build': 5000,
      'unit-tests': 3000,
      'bundle-analysis': 2000,
      'artifact-creation': 2000,
      'backup': 3000,
      'blue-green-setup': 4000,
      'deploy-vercel': 3000,
      'deploy-netlify': 3000,
      'ecr-push': 6000,
      'docker-build': 8000,
      'k8s-manifests': 2000,
      'health-check': 2000,
      'smoke-tests': 3000,
      'performance-check': 4000,
      'monitoring-setup': 2000,
      'notification': 1000
    }
    return durations[stepId] || 2000
  }

  const generateStepOutput = (stepId: string, status: string, target: string): string => {
    if (status === 'failed') return ''
    
    const outputs: Record<string, string> = {
      'build': `✓ Build completed successfully\n✓ Generated 15 assets\n✓ Bundle size: 2.3MB`,
      'unit-tests': `✓ 47 tests passed\n✓ Code coverage: 85%\n✓ 0 failing tests`,
      'security-audit': `✓ No high-severity vulnerabilities\n✓ 2 low-risk issues fixed\n✓ Security score: A+`,
      'deploy-vercel': `✓ Deployed to Vercel\n✓ Preview URL generated\n✓ Edge functions deployed`,
      'health-check': `✓ Health endpoint responding\n✓ Response time: 120ms\n✓ All services healthy`,
      'performance-check': `✓ Lighthouse score: 95/100\n✓ Core Web Vitals passed\n✓ Load time: 1.2s`
    }
    
    return outputs[stepId] || `✓ ${stepId.replace(/-/g, ' ')} completed successfully`
  }

  const generateErrorMessage = (stepId: string): string => {
    const errors: Record<string, string> = {
      'build': 'TypeScript compilation error in src/components/App.tsx',
      'unit-tests': '3 tests failed: authentication.test.ts',
      'security-audit': 'High-severity vulnerability detected in dependencies',
      'deploy-vercel': 'Deployment failed: Invalid build configuration',
      'health-check': 'Health endpoint returned 503 status',
      'performance-check': 'Performance budget exceeded: bundle too large'
    }
    
    return errors[stepId] || `${stepId.replace(/-/g, ' ')} failed with unknown error`
  }

  const generateDeploymentUrl = (projectName: string, environment: string, target: string, version: string): string => {
    const name = projectName.toLowerCase().replace(/\s+/g, '-')
    const subdomain = environment === 'production' ? name : `${name}-${environment}`
    
    const domains: Record<string, string> = {
      'vercel': 'vercel.app',
      'netlify': 'netlify.app',
      'heroku': 'herokuapp.com',
      'railway': 'railway.app',
      'fly': 'fly.dev'
    }
    
    return `https://${subdomain}.${domains[target] || 'example.com'}`
  }

  const simulateHealthCheck = async (url: string): Promise<'healthy' | 'degraded' | 'unhealthy'> => {
    // Simulate health check delay
    await new Promise(resolve => setTimeout(resolve, 1000))
    const random = Math.random()
    if (random > 0.9) return 'unhealthy'
    if (random > 0.7) return 'degraded'
    return 'healthy'
  }

  const generateMockMetrics = () => ({
    responseTime: Math.floor(Math.random() * 200) + 50,
    errorRate: Math.random() * 0.05,
    throughput: Math.floor(Math.random() * 1000) + 100
  })

  const rollbackDeployment = async (deploymentId: string) => {
    const deployment = deployments.find(d => d.id === deploymentId)
    if (!deployment || !deployment.rollbackAvailable) {
      toast.error('Rollback not available for this deployment')
      return
    }

    setDeployments(prev => prev.map(dep =>
      dep.id === deploymentId
        ? { ...dep, status: 'rolling-back' as any }
        : dep
    ))

    // Simulate rollback process
    await new Promise(resolve => setTimeout(resolve, 3000))

    setDeployments(prev => prev.map(dep =>
      dep.id === deploymentId
        ? { ...dep, status: 'rolled-back' as any, healthStatus: 'unknown' as any }
        : dep
    ))

    toast.success('Deployment rolled back successfully')
  }

  const updateEnvironmentVariable = (envId: string, key: string, value: string) => {
    setEnvironments(prev => prev.map(env =>
      env.id === envId
        ? {
            ...env,
            variables: { ...env.variables, [key]: value }
          }
        : env
    ))
  }

  const removeEnvironmentVariable = (envId: string, key: string) => {
    setEnvironments(prev => prev.map(env =>
      env.id === envId
        ? {
            ...env,
            variables: Object.fromEntries(
              Object.entries(env.variables).filter(([k]) => k !== key)
            )
          }
        : env
    ))
  }

  const getStepIcon = (status: string) => {
    switch (status) {
      case 'completed': return <CheckCircle className="w-4 h-4 text-green-500" />
      case 'failed': return <XCircle className="w-4 h-4 text-red-500" />
      case 'running': return <div className="w-4 h-4 border-2 border-blue-500 border-t-transparent rounded-full animate-spin" />
      case 'skipped': return <ArrowRight className="w-4 h-4 text-gray-400" />
      default: return <Clock className="w-4 h-4 text-gray-400" />
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'deployed': return 'bg-green-500'
      case 'deploying':
      case 'building':
      case 'testing':
      case 'verifying': return 'bg-blue-500'
      case 'failed': return 'bg-red-500'
      case 'rolled-back': return 'bg-orange-500'
      default: return 'bg-gray-500'
    }
  }

  const getHealthColor = (health: string) => {
    switch (health) {
      case 'healthy': return 'text-green-500'
      case 'degraded': return 'text-yellow-500'
      case 'unhealthy': return 'text-red-500'
      default: return 'text-gray-400'
    }
  }

  return (
    <div className="space-y-6">
      <Tabs defaultValue="deploy" className="space-y-6">
        <TabsList className="grid w-full grid-cols-4">
          <TabsTrigger value="deploy">Deploy</TabsTrigger>
          <TabsTrigger value="environments">Environments</TabsTrigger>
          <TabsTrigger value="pipelines">Pipelines</TabsTrigger>
          <TabsTrigger value="history">History</TabsTrigger>
        </TabsList>

        <TabsContent value="deploy" className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Rocket className="w-5 h-5" />
                Quick Deploy
              </CardTitle>
              <CardDescription>
                Deploy your project to any environment with automated CI/CD pipeline
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label>Deployment Target</Label>
                  <Select value={selectedTarget} onValueChange={setSelectedTarget}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select deployment target" />
                    </SelectTrigger>
                    <SelectContent>
                      {deploymentTargets.map((target) => (
                        <SelectItem key={target.id} value={target.id}>
                          <div className="flex items-center gap-2">
                            <target.icon className="w-4 h-4" />
                            <div>
                              <div className="font-medium">{target.name}</div>
                              <div className="text-xs text-muted-foreground">{target.description}</div>
                            </div>
                          </div>
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-2">
                  <Label>Pipeline</Label>
                  <Select value={selectedPipeline} onValueChange={setSelectedPipeline}>
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {pipelines.filter(p => p.enabled).map((pipeline) => (
                        <SelectItem key={pipeline.id} value={pipeline.id}>
                          <div className="flex items-center justify-between w-full">
                            <span>{pipeline.name}</span>
                            <Badge variant="outline" className="ml-2">
                              {pipeline.successRate}%
                            </Badge>
                          </div>
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              </div>

              <div className="grid grid-cols-3 gap-4">
                {environments.map((env) => (
                  <Card key={env.id} className="cursor-pointer hover:shadow-md transition-shadow">
                    <CardHeader className="pb-2">
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                          <div className={`w-3 h-3 rounded-full ${
                            env.type === 'production' ? 'bg-red-500' :
                            env.type === 'staging' ? 'bg-yellow-500' : 'bg-blue-500'
                          }`} />
                          <CardTitle className="text-sm">{env.name}</CardTitle>
                        </div>
                        {env.requiresApproval && <Shield className="w-4 h-4 text-amber-500" />}
                      </div>
                    </CardHeader>
                    <CardContent className="pt-0">
                      <div className="space-y-2 text-xs">
                        <div className="flex justify-between">
                          <span className="text-muted-foreground">Branch:</span>
                          <span className="font-mono">{env.branch}</span>
                        </div>
                        {env.url && (
                          <div className="flex justify-between">
                            <span className="text-muted-foreground">Status:</span>
                            <Badge variant="outline" className="text-xs">
                              <Globe className="w-3 h-3 mr-1" />
                              Live
                            </Badge>
                          </div>
                        )}
                      </div>
                      <Button
                        size="sm"
                        className="w-full mt-3"
                        disabled={isDeploying || !selectedTarget}
                        onClick={() => runPipeline(selectedPipeline, env.id)}
                      >
                        <Play className="w-3 h-3 mr-1" />
                        Deploy
                      </Button>
                    </CardContent>
                  </Card>
                ))}
              </div>

              <Separator />

              <div className="flex items-center justify-between">
                <div className="flex items-center gap-4">
                  <Button
                    onClick={() => runPipeline(selectedPipeline)}
                    disabled={isDeploying || !selectedTarget}
                    className="flex items-center gap-2"
                  >
                    <Rocket className="w-4 h-4" />
                    {isDeploying ? 'Running Pipeline...' : 'Run Full Pipeline'}
                  </Button>
                  
                  {activePipeline && (
                    <div className="flex items-center gap-2">
                      <div className="w-2 h-2 bg-blue-500 rounded-full animate-pulse" />
                      <span className="text-sm text-muted-foreground">
                        Running: {activePipeline.name}
                      </span>
                    </div>
                  )}
                </div>

                <div className="flex items-center gap-2">
                  <Button variant="outline" size="sm" onClick={() => setShowPipelineConfig(true)}>
                    <Settings className="w-4 h-4" />
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="environments" className="space-y-6">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="text-lg font-semibold">Environment Configuration</h3>
              <p className="text-sm text-muted-foreground">
                Configure deployment environments and their settings
              </p>
            </div>
            <Button onClick={() => setShowEnvironmentConfig(true)}>
              <Plus className="w-4 h-4 mr-2" />
              Add Environment
            </Button>
          </div>

          <div className="grid gap-6">
            {environments.map((env) => (
              <Card key={env.id}>
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <div className={`w-4 h-4 rounded-full ${
                        env.type === 'production' ? 'bg-red-500' :
                        env.type === 'staging' ? 'bg-yellow-500' : 'bg-blue-500'
                      }`} />
                      <div>
                        <CardTitle>{env.name}</CardTitle>
                        <CardDescription>
                          {env.type.charAt(0).toUpperCase() + env.type.slice(1)} Environment
                        </CardDescription>
                      </div>
                    </div>
                    <div className="flex items-center gap-2">
                      {env.url && (
                        <Button variant="outline" size="sm" asChild>
                          <a href={env.url} target="_blank" rel="noopener noreferrer">
                            <Eye className="w-4 h-4" />
                          </a>
                        </Button>
                      )}
                      <Button variant="outline" size="sm" onClick={() => {
                        setSelectedEnvironment(env)
                        setShowEnvironmentConfig(true)
                      }}>
                        <Settings className="w-4 h-4" />
                      </Button>
                    </div>
                  </div>
                </CardHeader>
                <CardContent>
                  <div className="grid grid-cols-2 gap-6">
                    <div className="space-y-3">
                      <div className="flex items-center justify-between text-sm">
                        <span className="text-muted-foreground">Branch:</span>
                        <span className="font-mono">{env.branch}</span>
                      </div>
                      <div className="flex items-center justify-between text-sm">
                        <span className="text-muted-foreground">Auto Promote:</span>
                        <Badge variant={env.autoPromote ? "default" : "secondary"}>
                          {env.autoPromote ? 'Enabled' : 'Disabled'}
                        </Badge>
                      </div>
                      <div className="flex items-center justify-between text-sm">
                        <span className="text-muted-foreground">Requires Approval:</span>
                        <Badge variant={env.requiresApproval ? "destructive" : "secondary"}>
                          {env.requiresApproval ? 'Required' : 'Not Required'}
                        </Badge>
                      </div>
                      <div className="flex items-center justify-between text-sm">
                        <span className="text-muted-foreground">Rollback:</span>
                        <Badge variant={env.rollbackEnabled ? "default" : "secondary"}>
                          {env.rollbackEnabled ? 'Enabled' : 'Disabled'}
                        </Badge>
                      </div>
                    </div>
                    <div className="space-y-3">
                      <div>
                        <h4 className="text-sm font-medium mb-2">Environment Variables</h4>
                        <div className="space-y-1">
                          {Object.entries(env.variables).map(([key, value]) => (
                            <div key={key} className="flex items-center justify-between text-xs bg-muted p-2 rounded">
                              <span className="font-mono">{key}</span>
                              <span className="font-mono text-muted-foreground">{value}</span>
                            </div>
                          ))}
                          {Object.keys(env.variables).length === 0 && (
                            <p className="text-xs text-muted-foreground">No variables configured</p>
                          )}
                        </div>
                      </div>
                      <div>
                        <h4 className="text-sm font-medium mb-2">Health Checks</h4>
                        <div className="space-y-1">
                          {env.healthChecks.map((check, index) => (
                            <div key={index} className="text-xs font-mono bg-muted p-2 rounded">
                              {check}
                            </div>
                          ))}
                        </div>
                      </div>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        </TabsContent>

        <TabsContent value="pipelines" className="space-y-6">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="text-lg font-semibold">CI/CD Pipelines</h3>
              <p className="text-sm text-muted-foreground">
                Configure automated deployment pipelines and workflows
              </p>
            </div>
          </div>

          <div className="grid gap-6">
            {pipelines.map((pipeline) => (
              <Card key={pipeline.id}>
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <div>
                      <CardTitle className="flex items-center gap-2">
                        <GitBranch className="w-5 h-5" />
                        {pipeline.name}
                      </CardTitle>
                      <CardDescription>
                        {pipeline.environments.length} environments • {pipeline.steps.length} steps
                      </CardDescription>
                    </div>
                    <div className="flex items-center gap-2">
                      <Badge variant={pipeline.enabled ? "default" : "secondary"}>
                        {pipeline.enabled ? 'Active' : 'Inactive'}
                      </Badge>
                      <Badge variant="outline">
                        {pipeline.successRate}% success
                      </Badge>
                      <Switch
                        checked={pipeline.enabled}
                        onCheckedChange={(enabled) => {
                          setPipelines(prev => prev.map(p =>
                            p.id === pipeline.id ? { ...p, enabled } : p
                          ))
                        }}
                      />
                    </div>
                  </div>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    <div className="flex items-center gap-2 text-sm">
                      <span className="text-muted-foreground">Trigger:</span>
                      <Badge variant="outline">{pipeline.trigger}</Badge>
                      <span className="text-muted-foreground">Environments:</span>
                      {pipeline.environments.map(envId => {
                        const env = environments.find(e => e.id === envId)
                        return env ? (
                          <Badge key={envId} variant="secondary">{env.name}</Badge>
                        ) : null
                      })}
                    </div>
                    
                    <div>
                      <h4 className="text-sm font-medium mb-2">Pipeline Steps</h4>
                      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-2">
                        {pipeline.steps.map((step, index) => (
                          <div key={step.id} className="flex items-center gap-2 p-2 bg-muted rounded text-sm">
                            <span className="text-muted-foreground">{index + 1}.</span>
                            <span>{step.name}</span>
                            <Badge variant="outline" className="text-xs ml-auto">
                              {step.type}
                            </Badge>
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        </TabsContent>

        <TabsContent value="history" className="space-y-6">
          {deployments.length > 0 ? (
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <h3 className="text-lg font-semibold">Deployment History</h3>
                <div className="flex items-center gap-2">
                  <Button variant="outline" size="sm">
                    <RefreshCw className="w-4 h-4" />
                  </Button>
                </div>
              </div>
              
              <ScrollArea className="h-96">
                <div className="space-y-4">
                  {deployments.slice().reverse().map((deployment) => (
                    <Card key={deployment.id} className="p-4">
                      <div className="space-y-4">
                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-3">
                            <div className={`w-3 h-3 rounded-full ${getStatusColor(deployment.status)}`} />
                            <div>
                              <h4 className="font-medium flex items-center gap-2">
                                {selectedTarget} → {environments.find(e => e.id === deployment.environment)?.name}
                                <Badge variant="outline" className="text-xs">
                                  {deployment.version}
                                </Badge>
                              </h4>
                              <p className="text-sm text-muted-foreground">
                                {new Date(deployment.startTime).toLocaleString()}
                                {deployment.endTime && (
                                  <span className="ml-2">
                                    • Duration: {Math.round((deployment.endTime - deployment.startTime) / 1000)}s
                                  </span>
                                )}
                              </p>
                            </div>
                          </div>
                          <div className="flex items-center gap-2">
                            <Badge variant="outline" className="flex items-center gap-1">
                              {getStepIcon(deployment.status as any)}
                              {deployment.status}
                            </Badge>
                            {deployment.healthStatus && deployment.healthStatus !== 'unknown' && (
                              <Badge variant="outline" className={`${getHealthColor(deployment.healthStatus)}`}>
                                {deployment.healthStatus}
                              </Badge>
                            )}
                            {deployment.url && (
                              <Button variant="outline" size="sm" asChild>
                                <a href={deployment.url} target="_blank" rel="noopener noreferrer">
                                  <Eye className="w-4 h-4" />
                                </a>
                              </Button>
                            )}
                            {deployment.rollbackAvailable && deployment.status === 'deployed' && (
                              <Button 
                                variant="outline" 
                                size="sm"
                                onClick={() => rollbackDeployment(deployment.id)}
                              >
                                <RefreshCw className="w-4 h-4" />
                              </Button>
                            )}
                          </div>
                        </div>

                        {deployment.metrics && (
                          <div className="grid grid-cols-3 gap-4 p-3 bg-muted rounded-lg">
                            <div className="text-center">
                              <div className="text-sm text-muted-foreground">Response Time</div>
                              <div className="font-medium">{deployment.metrics.responseTime}ms</div>
                            </div>
                            <div className="text-center">
                              <div className="text-sm text-muted-foreground">Error Rate</div>
                              <div className="font-medium">{(deployment.metrics.errorRate * 100).toFixed(2)}%</div>
                            </div>
                            <div className="text-center">
                              <div className="text-sm text-muted-foreground">Throughput</div>
                              <div className="font-medium">{deployment.metrics.throughput}/min</div>
                            </div>
                          </div>
                        )}

                        {(deployment.status === 'building' || deployment.status === 'deploying' || deployment.status === 'testing' || deployment.status === 'verifying') && (
                          <div className="space-y-2">
                            <Progress value={deployment.progress} className="h-2" />
                            <p className="text-sm text-muted-foreground">
                              {Math.round(deployment.progress)}% complete
                            </p>
                          </div>
                        )}

                        <div className="space-y-2">
                          {deployment.steps.map((step) => (
                            <div key={step.id} className="flex items-start justify-between p-2 rounded-md bg-muted/50">
                              <div className="flex items-start gap-3 flex-1">
                                <div className="mt-0.5">
                                  {getStepIcon(step.status)}
                                </div>
                                <div className="flex-1">
                                  <div className="flex items-center justify-between">
                                    <span className="text-sm font-medium">{step.name}</span>
                                    {step.duration && (
                                      <span className="text-xs text-muted-foreground">
                                        {step.duration}s
                                      </span>
                                    )}
                                  </div>
                                  {step.output && (
                                    <pre className="text-xs text-muted-foreground mt-1 whitespace-pre-wrap">
                                      {step.output}
                                    </pre>
                                  )}
                                  {step.error && (
                                    <div className="text-xs text-red-500 mt-1 p-2 bg-red-50 rounded">
                                      {step.error}
                                    </div>
                                  )}
                                </div>
                              </div>
                            </div>
                          ))}
                        </div>
                      </div>
                    </Card>
                  ))}
                </div>
              </ScrollArea>
            </div>
          ) : (
            <div className="text-center py-12">
              <Terminal className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
              <h3 className="text-lg font-semibold mb-2">No deployments yet</h3>
              <p className="text-muted-foreground mb-4">Run your first deployment to see the history</p>
            </div>
          )}
        </TabsContent>
      </Tabs>

      {/* Environment Configuration Dialog */}
      <Dialog open={showEnvironmentConfig} onOpenChange={setShowEnvironmentConfig}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>
              {selectedEnvironment ? 'Edit Environment' : 'Add Environment'}
            </DialogTitle>
            <DialogDescription>
              Configure environment settings, variables, and deployment options
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            {selectedEnvironment && (
              <div className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label>Branch</Label>
                    <Input 
                      value={selectedEnvironment.branch}
                      onChange={(e) => setSelectedEnvironment({
                        ...selectedEnvironment,
                        branch: e.target.value
                      })}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label>URL</Label>
                    <Input 
                      value={selectedEnvironment.url || ''}
                      onChange={(e) => setSelectedEnvironment({
                        ...selectedEnvironment,
                        url: e.target.value
                      })}
                      placeholder="https://example.com"
                    />
                  </div>
                </div>

                <div className="flex items-center justify-between">
                  <div className="space-y-1">
                    <Label>Auto Promote</Label>
                    <p className="text-xs text-muted-foreground">
                      Automatically promote successful deployments
                    </p>
                  </div>
                  <Switch 
                    checked={selectedEnvironment.autoPromote}
                    onCheckedChange={(checked) => setSelectedEnvironment({
                      ...selectedEnvironment,
                      autoPromote: checked
                    })}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="space-y-1">
                    <Label>Requires Approval</Label>
                    <p className="text-xs text-muted-foreground">
                      Require manual approval before deployment
                    </p>
                  </div>
                  <Switch 
                    checked={selectedEnvironment.requiresApproval}
                    onCheckedChange={(checked) => setSelectedEnvironment({
                      ...selectedEnvironment,
                      requiresApproval: checked
                    })}
                  />
                </div>

                <div className="space-y-2">
                  <Label>Environment Variables</Label>
                  <div className="space-y-2">
                    {Object.entries(selectedEnvironment.variables).map(([key, value]) => (
                      <div key={key} className="flex items-center gap-2">
                        <Input value={key} className="font-mono" disabled />
                        <Input 
                          value={value}
                          onChange={(e) => updateEnvironmentVariable(selectedEnvironment.id, key, e.target.value)}
                          className="font-mono"
                        />
                        <Button 
                          variant="outline" 
                          size="sm"
                          onClick={() => removeEnvironmentVariable(selectedEnvironment.id, key)}
                        >
                          ✕
                        </Button>
                      </div>
                    ))}
                    <div className="flex items-center gap-2">
                      <Input 
                        placeholder="KEY"
                        value={newEnvVar.key}
                        onChange={(e) => setNewEnvVar({ ...newEnvVar, key: e.target.value })}
                        className="font-mono"
                      />
                      <Input 
                        placeholder="value"
                        value={newEnvVar.value}
                        onChange={(e) => setNewEnvVar({ ...newEnvVar, value: e.target.value })}
                        className="font-mono"
                      />
                      <Button 
                        variant="outline" 
                        size="sm"
                        onClick={() => {
                          if (newEnvVar.key && newEnvVar.value) {
                            updateEnvironmentVariable(selectedEnvironment.id, newEnvVar.key, newEnvVar.value)
                            setNewEnvVar({ key: '', value: '' })
                          }
                        }}
                      >
                        Add
                      </Button>
                    </div>
                  </div>
                </div>

                <div className="space-y-2">
                  <Label>Health Check Endpoints</Label>
                  <Textarea 
                    value={selectedEnvironment.healthChecks.join('\n')}
                    onChange={(e) => setSelectedEnvironment({
                      ...selectedEnvironment,
                      healthChecks: e.target.value.split('\n').filter(Boolean)
                    })}
                    placeholder="/health&#10;/api/status&#10;/api/metrics"
                    className="font-mono"
                  />
                </div>
              </div>
            )}
          </div>
        </DialogContent>
      </Dialog>
    </div>
  )
}