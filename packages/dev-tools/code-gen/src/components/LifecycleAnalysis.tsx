import { useState, useEffect } from 'react'
import { useKV } from '@github/spark/hooks'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Progress } from '@/components/ui/progress'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { toast } from 'sonner'
import { 
  GitBranch, 
  CheckCircle, 
  AlertTriangle, 
  Eye, 
  Target, 
  TrendingUp,
  MapPin,
  Route,
  Activity,
  BarChart3,
  Network,
  Clock
} from '@phosphor-icons/react'

interface NavigationEndpoint {
  id: string
  path: string
  component: string
  method: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH'
  implemented: boolean
  tested: boolean
  dependencies: string[]
  metadata: {
    description: string
    parameters?: string[]
    returns?: string
    lastValidated?: number
  }
}

interface NavigationFlow {
  id: string
  name: string
  description: string
  startPoint: string
  endPoint: string
  steps: NavigationStep[]
  completionRate: number
  lastAnalyzed: number
  issues: FlowIssue[]
}

interface NavigationStep {
  id: string
  component: string
  action: string
  endpoint?: string
  nextSteps: string[]
  implemented: boolean
  validationStatus: 'pending' | 'passed' | 'failed'
}

interface FlowIssue {
  id: string
  type: 'missing_endpoint' | 'broken_navigation' | 'incomplete_implementation' | 'validation_failed'
  severity: 'low' | 'medium' | 'high' | 'critical'
  description: string
  component: string
  endpoint?: string
  recommendation: string
}

interface LifecycleStage {
  id: string
  name: string
  description: string
  status: 'not_started' | 'in_progress' | 'completed' | 'failed'
  progress: number
  dependencies: string[]
  flows: string[]
  endpoints: string[]
  issues: number
  lastUpdated: number
}

interface Project {
  id: string
  name: string
  type: string
  codebase: {
    files: Record<string, string>
  }
}

interface LifecycleAnalysisProps {
  project: Project
  onUpdateProject: (projectId: string, updates: any) => void
}

export function LifecycleAnalysis({ project, onUpdateProject }: LifecycleAnalysisProps) {
  const [endpoints, setEndpoints] = useKV<NavigationEndpoint[]>(`lifecycle-endpoints-${project.id}`, [])
  const [flows, setFlows] = useKV<NavigationFlow[]>(`lifecycle-flows-${project.id}`, [])
  const [stages, setStages] = useKV<LifecycleStage[]>(`lifecycle-stages-${project.id}`, [])
  const [isAnalyzing, setIsAnalyzing] = useState(false)
  const [analysisResults, setAnalysisResults] = useState<any>(null)
  const [activeTab, setActiveTab] = useState('overview')

  useEffect(() => {
    initializeLifecycleTracking()
  }, [project.id])

  const initializeLifecycleTracking = async () => {
    if (stages.length === 0) {
      const defaultStages: LifecycleStage[] = [
        {
          id: 'planning',
          name: 'Planning & Design',
          description: 'Project planning, architecture design, and component mapping',
          status: 'completed',
          progress: 100,
          dependencies: [],
          flows: [],
          endpoints: [],
          issues: 0,
          lastUpdated: Date.now()
        },
        {
          id: 'development',
          name: 'Development',
          description: 'Component implementation and feature development',
          status: 'in_progress',
          progress: 65,
          dependencies: ['planning'],
          flows: [],
          endpoints: [],
          issues: 0,
          lastUpdated: Date.now()
        },
        {
          id: 'navigation',
          name: 'Navigation Implementation',
          description: 'Navigation flow implementation and endpoint creation',
          status: 'in_progress',
          progress: 45,
          dependencies: ['development'],
          flows: [],
          endpoints: [],
          issues: 0,
          lastUpdated: Date.now()
        },
        {
          id: 'testing',
          name: 'Testing & Validation',
          description: 'Flow validation, endpoint testing, and user journey verification',
          status: 'not_started',
          progress: 0,
          dependencies: ['navigation'],
          flows: [],
          endpoints: [],
          issues: 0,
          lastUpdated: Date.now()
        },
        {
          id: 'deployment',
          name: 'Deployment',
          description: 'Production deployment and final validation',
          status: 'not_started',
          progress: 0,
          dependencies: ['testing'],
          flows: [],
          endpoints: [],
          issues: 0,
          lastUpdated: Date.now()
        }
      ]
      setStages(defaultStages)
    }
  }

  const analyzeApplicationLifecycle = async () => {
    setIsAnalyzing(true)
    
    try {
      // Analyze project files for navigation patterns
      const fileAnalysis = analyzeProjectFiles(project.codebase.files)
      
      // Generate endpoints from analysis
      const detectedEndpoints = generateEndpointsFromAnalysis(fileAnalysis)
      setEndpoints(detectedEndpoints)
      
      // Generate navigation flows
      const detectedFlows = generateNavigationFlows(fileAnalysis, detectedEndpoints)
      setFlows(detectedFlows)
      
      // Update lifecycle stages based on analysis
      const updatedStages = updateLifecycleStages(detectedFlows, detectedEndpoints)
      setStages(updatedStages)
      
      // Generate comprehensive analysis results
      const results = {
        totalEndpoints: detectedEndpoints.length,
        implementedEndpoints: detectedEndpoints.filter(e => e.implemented).length,
        totalFlows: detectedFlows.length,
        completeFlows: detectedFlows.filter(f => f.completionRate === 100).length,
        criticalIssues: detectedFlows.reduce((acc, f) => acc + f.issues.filter(i => i.severity === 'critical').length, 0),
        overallCompleteness: calculateOverallCompleteness(updatedStages),
        recommendations: generateRecommendations(detectedFlows, detectedEndpoints)
      }
      
      setAnalysisResults(results)
      toast.success('Lifecycle analysis completed successfully')
      
    } catch (error) {
      console.error('Analysis failed:', error)
      toast.error('Failed to analyze application lifecycle')
    } finally {
      setIsAnalyzing(false)
    }
  }

  const analyzeProjectFiles = (files: Record<string, string>) => {
    const analysis = {
      components: [] as string[],
      routes: [] as string[],
      apiEndpoints: [] as string[],
      navigationPatterns: [] as string[],
      stateManagement: [] as string[],
      eventHandlers: [] as string[]
    }

    Object.entries(files).forEach(([filePath, content]) => {
      // Detect React components
      if (content.includes('function ') || content.includes('const ') && content.includes('=')) {
        const componentMatches = content.match(/(?:function|const)\s+([A-Z][a-zA-Z0-9]*)/g)
        if (componentMatches) {
          analysis.components.push(...componentMatches.map(m => m.split(' ').pop() || ''))
        }
      }

      // Detect routes and navigation
      if (content.includes('Route') || content.includes('navigate') || content.includes('href')) {
        const routeMatches = content.match(/(?:to|href)=["']([^"']+)["']/g)
        if (routeMatches) {
          analysis.routes.push(...routeMatches.map(m => m.split('=')[1].replace(/["']/g, '')))
        }
      }

      // Detect API endpoints
      if (content.includes('fetch') || content.includes('axios') || content.includes('api')) {
        const apiMatches = content.match(/(?:fetch|axios\.(?:get|post|put|delete))\s*\(\s*["']([^"']+)["']/g)
        if (apiMatches) {
          analysis.apiEndpoints.push(...apiMatches.map(m => m.split(/["']/)[1]))
        }
      }

      // Detect event handlers
      if (content.includes('onClick') || content.includes('onSubmit') || content.includes('onChange')) {
        const handlerMatches = content.match(/on[A-Z][a-zA-Z]*=/g)
        if (handlerMatches) {
          analysis.eventHandlers.push(...handlerMatches.map(m => m.replace('=', '')))
        }
      }
    })

    return analysis
  }

  const generateEndpointsFromAnalysis = (analysis: any): NavigationEndpoint[] => {
    const endpoints: NavigationEndpoint[] = []
    let endpointId = 1

    // Generate endpoints from detected routes
    analysis.routes.forEach((route: string) => {
      endpoints.push({
        id: `endpoint-${endpointId++}`,
        path: route,
        component: 'Unknown',
        method: 'GET',
        implemented: true,
        tested: false,
        dependencies: [],
        metadata: {
          description: `Route endpoint for ${route}`,
          lastValidated: Date.now()
        }
      })
    })

    // Generate endpoints from API calls
    analysis.apiEndpoints.forEach((endpoint: string) => {
      endpoints.push({
        id: `endpoint-${endpointId++}`,
        path: endpoint,
        component: 'API',
        method: 'GET',
        implemented: false,
        tested: false,
        dependencies: [],
        metadata: {
          description: `API endpoint ${endpoint}`,
          lastValidated: Date.now()
        }
      })
    })

    return endpoints
  }

  const generateNavigationFlows = (analysis: any, endpoints: NavigationEndpoint[]): NavigationFlow[] => {
    const flows: NavigationFlow[] = []
    
    // Generate primary user flows
    const primaryFlows = [
      {
        name: 'User Registration',
        description: 'Complete user registration and onboarding flow',
        startPoint: '/register',
        endPoint: '/dashboard'
      },
      {
        name: 'Project Creation',
        description: 'Create new project workflow',
        startPoint: '/projects',
        endPoint: '/project/[id]'
      },
      {
        name: 'Code Generation',
        description: 'AI-powered code generation workflow',
        startPoint: '/project/[id]/generate',
        endPoint: '/project/[id]/code'
      }
    ]

    primaryFlows.forEach((flowTemplate, index) => {
      const steps: NavigationStep[] = []
      const relatedEndpoints = endpoints.filter(e => 
        e.path.includes(flowTemplate.startPoint) || e.path.includes(flowTemplate.endPoint)
      )

      // Generate steps based on endpoints
      relatedEndpoints.forEach((endpoint, stepIndex) => {
        steps.push({
          id: `step-${index}-${stepIndex}`,
          component: endpoint.component,
          action: `Navigate to ${endpoint.path}`,
          endpoint: endpoint.id,
          nextSteps: stepIndex < relatedEndpoints.length - 1 ? [`step-${index}-${stepIndex + 1}`] : [],
          implemented: endpoint.implemented,
          validationStatus: endpoint.tested ? 'passed' : 'pending'
        })
      })

      const completionRate = steps.length > 0 ? 
        (steps.filter(s => s.implemented).length / steps.length) * 100 : 0

      const issues: FlowIssue[] = []
      steps.forEach(step => {
        if (!step.implemented) {
          issues.push({
            id: `issue-${step.id}`,
            type: 'incomplete_implementation',
            severity: 'high',
            description: `Step "${step.action}" is not implemented`,
            component: step.component,
            endpoint: step.endpoint,
            recommendation: `Implement ${step.action} in ${step.component}`
          })
        }
      })

      flows.push({
        id: `flow-${index}`,
        name: flowTemplate.name,
        description: flowTemplate.description,
        startPoint: flowTemplate.startPoint,
        endPoint: flowTemplate.endPoint,
        steps,
        completionRate,
        lastAnalyzed: Date.now(),
        issues
      })
    })

    return flows
  }

  const updateLifecycleStages = (flows: NavigationFlow[], endpoints: NavigationEndpoint[]): LifecycleStage[] => {
    return stages.map(stage => {
      let progress = stage.progress
      let status = stage.status
      let issues = 0

      switch (stage.id) {
        case 'navigation':
          progress = flows.length > 0 ? 
            flows.reduce((acc, f) => acc + f.completionRate, 0) / flows.length : 0
          status = progress === 100 ? 'completed' : progress > 0 ? 'in_progress' : 'not_started'
          issues = flows.reduce((acc, f) => acc + f.issues.length, 0)
          break
        case 'testing':
          const testedEndpoints = endpoints.filter(e => e.tested).length
          progress = endpoints.length > 0 ? (testedEndpoints / endpoints.length) * 100 : 0
          status = progress === 100 ? 'completed' : progress > 0 ? 'in_progress' : 'not_started'
          break
      }

      return {
        ...stage,
        progress,
        status,
        issues,
        lastUpdated: Date.now()
      }
    })
  }

  const calculateOverallCompleteness = (stages: LifecycleStage[]): number => {
    const totalProgress = stages.reduce((acc, stage) => acc + stage.progress, 0)
    return totalProgress / stages.length
  }

  const generateRecommendations = (flows: NavigationFlow[], endpoints: NavigationEndpoint[]): string[] => {
    const recommendations: string[] = []

    // Check for missing implementations
    const unimplementedEndpoints = endpoints.filter(e => !e.implemented)
    if (unimplementedEndpoints.length > 0) {
      recommendations.push(`Implement ${unimplementedEndpoints.length} missing endpoints`)
    }

    // Check for incomplete flows
    const incompleteFlows = flows.filter(f => f.completionRate < 100)
    if (incompleteFlows.length > 0) {
      recommendations.push(`Complete ${incompleteFlows.length} incomplete navigation flows`)
    }

    // Check for testing gaps
    const untestedEndpoints = endpoints.filter(e => !e.tested)
    if (untestedEndpoints.length > 0) {
      recommendations.push(`Add tests for ${untestedEndpoints.length} endpoints`)
    }

    return recommendations
  }

  const getStageStatusColor = (status: LifecycleStage['status']) => {
    switch (status) {
      case 'completed': return 'bg-green-500'
      case 'in_progress': return 'bg-blue-500'
      case 'failed': return 'bg-red-500'
      default: return 'bg-gray-400'
    }
  }

  const getIssueColor = (severity: FlowIssue['severity']) => {
    switch (severity) {
      case 'critical': return 'destructive'
      case 'high': return 'destructive'
      case 'medium': return 'default'
      case 'low': return 'secondary'
      default: return 'secondary'
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-semibold flex items-center gap-2">
            <Activity className="w-6 h-6" />
            Lifecycle Analysis
          </h2>
          <p className="text-muted-foreground">
            Comprehensive navigation flow tracking and endpoint validation
          </p>
        </div>
        <Button 
          onClick={analyzeApplicationLifecycle}
          disabled={isAnalyzing}
          className="flex items-center gap-2"
        >
          <TrendingUp className="w-4 h-4" />
          {isAnalyzing ? 'Analyzing...' : 'Analyze Lifecycle'}
        </Button>
      </div>

      {analysisResults && (
        <Alert>
          <CheckCircle className="w-4 h-4" />
          <AlertDescription>
            Analysis complete: {analysisResults.implementedEndpoints}/{analysisResults.totalEndpoints} endpoints implemented, 
            {analysisResults.completeFlows}/{analysisResults.totalFlows} flows complete, 
            {analysisResults.overallCompleteness.toFixed(1)}% overall completeness
          </AlertDescription>
        </Alert>
      )}

      <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-4">
        <TabsList className="grid w-full grid-cols-5">
          <TabsTrigger value="overview">Overview</TabsTrigger>
          <TabsTrigger value="stages">Stages</TabsTrigger>
          <TabsTrigger value="flows">Flows</TabsTrigger>
          <TabsTrigger value="endpoints">Endpoints</TabsTrigger>
          <TabsTrigger value="analytics">Analytics</TabsTrigger>
        </TabsList>

        <TabsContent value="overview" className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Total Stages</CardTitle>
                <GitBranch className="w-4 h-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{stages.length}</div>
                <p className="text-xs text-muted-foreground">
                  {stages.filter(s => s.status === 'completed').length} completed
                </p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Navigation Flows</CardTitle>
                <Route className="w-4 h-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{flows.length}</div>
                <p className="text-xs text-muted-foreground">
                  {flows.filter(f => f.completionRate === 100).length} complete
                </p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Endpoints</CardTitle>
                <MapPin className="w-4 h-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{endpoints.length}</div>
                <p className="text-xs text-muted-foreground">
                  {endpoints.filter(e => e.implemented).length} implemented
                </p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Issues</CardTitle>
                <AlertTriangle className="w-4 h-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">
                  {flows.reduce((acc, f) => acc + f.issues.length, 0)}
                </div>
                <p className="text-xs text-muted-foreground">
                  {flows.reduce((acc, f) => acc + f.issues.filter(i => i.severity === 'critical').length, 0)} critical
                </p>
              </CardContent>
            </Card>
          </div>

          {analysisResults && (
            <Card>
              <CardHeader>
                <CardTitle>Recommendations</CardTitle>
                <CardDescription>Suggested actions to improve completeness</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-2">
                  {analysisResults.recommendations.map((rec: string, index: number) => (
                    <div key={index} className="flex items-center gap-2">
                      <Target className="w-4 h-4 text-blue-500" />
                      <span className="text-sm">{rec}</span>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          )}
        </TabsContent>

        <TabsContent value="stages" className="space-y-4">
          <div className="space-y-4">
            {stages.map((stage) => (
              <Card key={stage.id}>
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <div className={`w-3 h-3 rounded-full ${getStageStatusColor(stage.status)}`} />
                      <CardTitle>{stage.name}</CardTitle>
                      {stage.issues > 0 && (
                        <Badge variant="destructive">{stage.issues} issues</Badge>
                      )}
                    </div>
                    <Badge variant="outline">{stage.status.replace('_', ' ')}</Badge>
                  </div>
                  <CardDescription>{stage.description}</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    <div>
                      <div className="flex justify-between text-sm mb-2">
                        <span>Progress</span>
                        <span>{stage.progress.toFixed(1)}%</span>
                      </div>
                      <Progress value={stage.progress} className="h-2" />
                    </div>
                    
                    {stage.dependencies.length > 0 && (
                      <div>
                        <h4 className="text-sm font-medium mb-2">Dependencies</h4>
                        <div className="flex flex-wrap gap-2">
                          {stage.dependencies.map(dep => (
                            <Badge key={dep} variant="secondary" className="text-xs">
                              {stages.find(s => s.id === dep)?.name || dep}
                            </Badge>
                          ))}
                        </div>
                      </div>
                    )}
                    
                    <div className="text-xs text-muted-foreground">
                      Last updated: {new Date(stage.lastUpdated).toLocaleString()}
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        </TabsContent>

        <TabsContent value="flows" className="space-y-4">
          <div className="space-y-4">
            {flows.map((flow) => (
              <Card key={flow.id}>
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <CardTitle>{flow.name}</CardTitle>
                    <Badge variant={flow.completionRate === 100 ? "default" : "secondary"}>
                      {flow.completionRate.toFixed(1)}% complete
                    </Badge>
                  </div>
                  <CardDescription>{flow.description}</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    <div className="flex items-center justify-between text-sm">
                      <span>From: <code className="bg-muted px-1 rounded">{flow.startPoint}</code></span>
                      <span>To: <code className="bg-muted px-1 rounded">{flow.endPoint}</code></span>
                    </div>
                    
                    <div>
                      <div className="flex justify-between text-sm mb-2">
                        <span>Completion</span>
                        <span>{flow.completionRate.toFixed(1)}%</span>
                      </div>
                      <Progress value={flow.completionRate} className="h-2" />
                    </div>
                    
                    {flow.steps.length > 0 && (
                      <div>
                        <h4 className="text-sm font-medium mb-2">Steps ({flow.steps.length})</h4>
                        <div className="space-y-2">
                          {flow.steps.slice(0, 3).map(step => (
                            <div key={step.id} className="flex items-center gap-2 text-sm">
                              <div className={`w-2 h-2 rounded-full ${
                                step.implemented ? 'bg-green-500' : 'bg-gray-300'
                              }`} />
                              <span>{step.action}</span>
                              <Badge variant="outline" className="text-xs">
                                {step.validationStatus}
                              </Badge>
                            </div>
                          ))}
                          {flow.steps.length > 3 && (
                            <div className="text-xs text-muted-foreground">
                              +{flow.steps.length - 3} more steps
                            </div>
                          )}
                        </div>
                      </div>
                    )}
                    
                    {flow.issues.length > 0 && (
                      <div>
                        <h4 className="text-sm font-medium mb-2">Issues ({flow.issues.length})</h4>
                        <div className="space-y-2">
                          {flow.issues.slice(0, 2).map(issue => (
                            <div key={issue.id} className="flex items-start gap-2">
                              <AlertTriangle className="w-4 h-4 text-orange-500 mt-0.5 flex-shrink-0" />
                              <div className="text-sm">
                                <Badge variant={getIssueColor(issue.severity)} className="text-xs mr-2">
                                  {issue.severity}
                                </Badge>
                                {issue.description}
                              </div>
                            </div>
                          ))}
                          {flow.issues.length > 2 && (
                            <div className="text-xs text-muted-foreground">
                              +{flow.issues.length - 2} more issues
                            </div>
                          )}
                        </div>
                      </div>
                    )}
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        </TabsContent>

        <TabsContent value="endpoints" className="space-y-4">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
            {endpoints.map((endpoint) => (
              <Card key={endpoint.id}>
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <CardTitle className="text-base">{endpoint.path}</CardTitle>
                    <div className="flex items-center gap-2">
                      <Badge variant="outline">{endpoint.method}</Badge>
                      <Badge variant={endpoint.implemented ? "default" : "secondary"}>
                        {endpoint.implemented ? 'Implemented' : 'Missing'}
                      </Badge>
                    </div>
                  </div>
                  <CardDescription>{endpoint.metadata.description}</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-3">
                    <div className="flex items-center justify-between text-sm">
                      <span>Component</span>
                      <code className="bg-muted px-2 py-1 rounded text-xs">{endpoint.component}</code>
                    </div>
                    
                    <div className="flex items-center gap-4 text-sm">
                      <div className="flex items-center gap-2">
                        <div className={`w-2 h-2 rounded-full ${
                          endpoint.implemented ? 'bg-green-500' : 'bg-red-500'
                        }`} />
                        <span>Implemented</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <div className={`w-2 h-2 rounded-full ${
                          endpoint.tested ? 'bg-green-500' : 'bg-gray-300'
                        }`} />
                        <span>Tested</span>
                      </div>
                    </div>
                    
                    {endpoint.dependencies.length > 0 && (
                      <div>
                        <h5 className="text-sm font-medium mb-1">Dependencies</h5>
                        <div className="flex flex-wrap gap-1">
                          {endpoint.dependencies.map(dep => (
                            <Badge key={dep} variant="outline" className="text-xs">{dep}</Badge>
                          ))}
                        </div>
                      </div>
                    )}
                    
                    {endpoint.metadata.lastValidated && (
                      <div className="text-xs text-muted-foreground">
                        Last validated: {new Date(endpoint.metadata.lastValidated).toLocaleString()}
                      </div>
                    )}
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        </TabsContent>

        <TabsContent value="analytics" className="space-y-4">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <BarChart3 className="w-5 h-5" />
                  Stage Progress
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {stages.map(stage => (
                    <div key={stage.id}>
                      <div className="flex justify-between text-sm mb-1">
                        <span>{stage.name}</span>
                        <span>{stage.progress.toFixed(1)}%</span>
                      </div>
                      <Progress value={stage.progress} className="h-2" />
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Network className="w-5 h-5" />
                  Flow Completeness
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {flows.map(flow => (
                    <div key={flow.id}>
                      <div className="flex justify-between text-sm mb-1">
                        <span>{flow.name}</span>
                        <span>{flow.completionRate.toFixed(1)}%</span>
                      </div>
                      <Progress value={flow.completionRate} className="h-2" />
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Eye className="w-5 h-5" />
                  Implementation Status
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  <div className="flex justify-between">
                    <span>Endpoints Implemented</span>
                    <span>{endpoints.filter(e => e.implemented).length}/{endpoints.length}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Endpoints Tested</span>
                    <span>{endpoints.filter(e => e.tested).length}/{endpoints.length}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Complete Flows</span>
                    <span>{flows.filter(f => f.completionRate === 100).length}/{flows.length}</span>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Clock className="w-5 h-5" />
                  Recent Activity
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-2">
                  {[...stages]
                    .sort((a, b) => b.lastUpdated - a.lastUpdated)
                    .slice(0, 5)
                    .map(stage => (
                      <div key={stage.id} className="flex justify-between text-sm">
                        <span>{stage.name}</span>
                        <span className="text-muted-foreground">
                          {new Date(stage.lastUpdated).toLocaleDateString()}
                        </span>
                      </div>
                    ))}
                </div>
              </CardContent>
            </Card>
          </div>
        </TabsContent>
      </Tabs>
    </div>
  )
}