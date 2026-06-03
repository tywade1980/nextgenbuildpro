import { useState, useEffect } from 'react'
import { useKV } from '@github/spark/hooks'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Progress } from '@/components/ui/progress'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { toast } from 'sonner'
import { 
  Route, 
  MapPin, 
  ArrowRight, 
  CheckCircle, 
  AlertTriangle,
  Play,
  RefreshCcw,
  Network,
  Target,
  Users,
  Clock
} from '@phosphor-icons/react'

interface NavigationNode {
  id: string
  component: string
  route: string
  type: 'page' | 'component' | 'api' | 'action'
  implemented: boolean
  tested: boolean
  connections: string[]
  metadata: {
    description: string
    userActions: string[]
    dependencies: string[]
    lastVisited?: number
  }
}

interface UserJourney {
  id: string
  name: string
  description: string
  persona: string
  steps: JourneyStep[]
  completeness: number
  criticalPath: boolean
  lastValidated: number
  issues: string[]
}

interface JourneyStep {
  id: string
  nodeId: string
  action: string
  expectedOutcome: string
  validationCriteria: string[]
  implemented: boolean
  tested: boolean
  blocked: boolean
  blockers?: string[]
}

interface NavigationGraph {
  nodes: NavigationNode[]
  edges: NavigationEdge[]
  orphanNodes: string[]
  deadEnds: string[]
  unreachableNodes: string[]
}

interface NavigationEdge {
  id: string
  from: string
  to: string
  trigger: string
  condition?: string
  implemented: boolean
}

interface Project {
  id: string
  name: string
  type: string
  codebase: {
    files: Record<string, string>
  }
}

interface NavigationFlowTrackerProps {
  project: Project
  onUpdateProject: (projectId: string, updates: any) => void
}

export function NavigationFlowTracker({ project, onUpdateProject }: NavigationFlowTrackerProps) {
  const [navigationGraph, setNavigationGraph] = useKV<NavigationGraph>(`nav-graph-${project.id}`, {
    nodes: [],
    edges: [],
    orphanNodes: [],
    deadEnds: [],
    unreachableNodes: []
  })
  const [userJourneys, setUserJourneys] = useKV<UserJourney[]>(`user-journeys-${project.id}`, [])
  const [isAnalyzing, setIsAnalyzing] = useState(false)
  const [selectedNode, setSelectedNode] = useState<NavigationNode | null>(null)
  const [flowValidation, setFlowValidation] = useState<any>(null)

  useEffect(() => {
    initializeDefaultJourneys()
  }, [project.id])

  const initializeDefaultJourneys = () => {
    if (userJourneys.length === 0) {
      const defaultJourneys: UserJourney[] = [
        {
          id: 'new-user-onboarding',
          name: 'New User Onboarding',
          description: 'First-time user registration and setup',
          persona: 'New Developer',
          steps: [
            {
              id: 'landing',
              nodeId: 'home',
              action: 'Visit landing page',
              expectedOutcome: 'See value proposition and call-to-action',
              validationCriteria: ['Page loads', 'CTA visible', 'Value prop clear'],
              implemented: true,
              tested: false,
              blocked: false
            },
            {
              id: 'register',
              nodeId: 'auth/register',
              action: 'Create account',
              expectedOutcome: 'Successfully register and verify email',
              validationCriteria: ['Form validation', 'Email sent', 'Account created'],
              implemented: false,
              tested: false,
              blocked: true,
              blockers: ['Email service not configured']
            }
          ],
          completeness: 50,
          criticalPath: true,
          lastValidated: Date.now(),
          issues: ['Registration endpoint missing', 'Email verification not implemented']
        },
        {
          id: 'project-creation',
          name: 'Project Creation Flow',
          description: 'Creating and configuring a new project',
          persona: 'Active Developer',
          steps: [
            {
              id: 'dashboard',
              nodeId: 'dashboard',
              action: 'Access dashboard',
              expectedOutcome: 'See project overview and create button',
              validationCriteria: ['Dashboard loads', 'Create button visible'],
              implemented: true,
              tested: true,
              blocked: false
            },
            {
              id: 'create-project',
              nodeId: 'projects/create',
              action: 'Fill project form',
              expectedOutcome: 'Project created with template',
              validationCriteria: ['Form validation', 'Template selection', 'Project saved'],
              implemented: true,
              tested: false,
              blocked: false
            }
          ],
          completeness: 75,
          criticalPath: true,
          lastValidated: Date.now(),
          issues: ['Template selection needs validation tests']
        }
      ]
      setUserJourneys(defaultJourneys)
    }
  }

  const analyzeNavigationFlow = async () => {
    setIsAnalyzing(true)
    
    try {
      // Analyze project files to build navigation graph
      const graph = await buildNavigationGraph(project.codebase.files)
      setNavigationGraph(graph)
      
      // Validate user journeys against the graph
      const validation = validateUserJourneys(userJourneys, graph)
      setFlowValidation(validation)
      
      // Update journey completeness based on analysis
      const updatedJourneys = updateJourneyCompleteness(userJourneys, graph)
      setUserJourneys(updatedJourneys)
      
      toast.success('Navigation flow analysis completed')
      
    } catch (error) {
      console.error('Navigation analysis failed:', error)
      toast.error('Failed to analyze navigation flow')
    } finally {
      setIsAnalyzing(false)
    }
  }

  const buildNavigationGraph = async (files: Record<string, string>): Promise<NavigationGraph> => {
    const nodes: NavigationNode[] = []
    const edges: NavigationEdge[] = []
    let nodeId = 1
    let edgeId = 1

    // Extract components and routes from files
    Object.entries(files).forEach(([filePath, content]) => {
      // Detect React components
      if (content.includes('function ') || content.includes('const ') && content.includes('export')) {
        const componentMatches = content.match(/(?:function|const)\s+([A-Z][a-zA-Z0-9]*)/g)
        if (componentMatches) {
          componentMatches.forEach(match => {
            const componentName = match.split(' ').pop() || ''
            const route = extractRouteFromComponent(content, componentName)
            
            nodes.push({
              id: `node-${nodeId++}`,
              component: componentName,
              route: route || `/${componentName.toLowerCase()}`,
              type: 'component',
              implemented: true,
              tested: false,
              connections: [],
              metadata: {
                description: `Component: ${componentName}`,
                userActions: extractUserActions(content),
                dependencies: extractDependencies(content)
              }
            })
          })
        }
      }

      // Detect navigation patterns and create edges
      const navigationPatterns = extractNavigationPatterns(content)
      navigationPatterns.forEach(pattern => {
        edges.push({
          id: `edge-${edgeId++}`,
          from: pattern.from,
          to: pattern.to,
          trigger: pattern.trigger,
          condition: pattern.condition,
          implemented: true
        })
      })
    })

    // Identify navigation issues
    const orphanNodes = findOrphanNodes(nodes, edges)
    const deadEnds = findDeadEnds(nodes, edges)
    const unreachableNodes = findUnreachableNodes(nodes, edges)

    return {
      nodes,
      edges,
      orphanNodes,
      deadEnds,
      unreachableNodes
    }
  }

  const extractRouteFromComponent = (content: string, componentName: string): string | null => {
    // Look for route patterns in the content
    const routeMatches = content.match(/(?:path|to|href)=["']([^"']+)["']/g)
    if (routeMatches && routeMatches.length > 0) {
      return routeMatches[0].split('=')[1].replace(/["']/g, '')
    }
    return null
  }

  const extractUserActions = (content: string): string[] => {
    const actions: string[] = []
    
    // Extract onClick handlers
    const clickHandlers = content.match(/onClick=\{[^}]+\}/g)
    if (clickHandlers) {
      actions.push(...clickHandlers.map(h => 'Click action'))
    }
    
    // Extract form submissions
    if (content.includes('onSubmit')) {
      actions.push('Form submission')
    }
    
    // Extract navigation
    if (content.includes('navigate') || content.includes('router.push')) {
      actions.push('Navigation')
    }
    
    return actions
  }

  const extractDependencies = (content: string): string[] => {
    const deps: string[] = []
    
    // Extract imports
    const importMatches = content.match(/import\s+.*\s+from\s+['"]([^'"]+)['"]/g)
    if (importMatches) {
      deps.push(...importMatches.map(imp => imp.split('from')[1].trim().replace(/['"`]/g, '')))
    }
    
    return deps.filter(dep => !dep.startsWith('.') && !dep.startsWith('@/'))
  }

  const extractNavigationPatterns = (content: string): any[] => {
    const patterns: any[] = []
    
    // Look for navigation calls
    const navMatches = content.match(/(?:navigate|router\.push|href)\s*\(\s*['"`]([^'"`]+)['"`]/g)
    if (navMatches) {
      navMatches.forEach(match => {
        const route = match.match(/['"`]([^'"`]+)['"`]/)?.[1]
        if (route) {
          patterns.push({
            from: 'current',
            to: route,
            trigger: 'navigation',
            condition: undefined
          })
        }
      })
    }
    
    return patterns
  }

  const findOrphanNodes = (nodes: NavigationNode[], edges: NavigationEdge[]): string[] => {
    return nodes
      .filter(node => !edges.some(edge => edge.to === node.id || edge.from === node.id))
      .map(node => node.id)
  }

  const findDeadEnds = (nodes: NavigationNode[], edges: NavigationEdge[]): string[] => {
    return nodes
      .filter(node => !edges.some(edge => edge.from === node.id))
      .map(node => node.id)
  }

  const findUnreachableNodes = (nodes: NavigationNode[], edges: NavigationEdge[]): string[] => {
    // Simple implementation - nodes with no incoming edges except entry points
    const entryPoints = ['home', 'index', 'landing']
    return nodes
      .filter(node => 
        !entryPoints.includes(node.route) && 
        !edges.some(edge => edge.to === node.id)
      )
      .map(node => node.id)
  }

  const validateUserJourneys = (journeys: UserJourney[], graph: NavigationGraph) => {
    const validation = {
      totalJourneys: journeys.length,
      validJourneys: 0,
      criticalPathIssues: 0,
      blockedSteps: 0,
      missingNodes: [] as string[],
      brokenPaths: [] as string[]
    }

    journeys.forEach(journey => {
      let journeyValid = true
      
      journey.steps.forEach(step => {
        const node = graph.nodes.find(n => n.id === step.nodeId || n.route === step.nodeId)
        
        if (!node) {
          validation.missingNodes.push(step.nodeId)
          journeyValid = false
        }
        
        if (step.blocked) {
          validation.blockedSteps++
        }
      })
      
      if (journeyValid) {
        validation.validJourneys++
      }
      
      if (journey.criticalPath && !journeyValid) {
        validation.criticalPathIssues++
      }
    })

    return validation
  }

  const updateJourneyCompleteness = (journeys: UserJourney[], graph: NavigationGraph): UserJourney[] => {
    return journeys.map(journey => {
      const implementedSteps = journey.steps.filter(step => {
        const node = graph.nodes.find(n => n.id === step.nodeId || n.route === step.nodeId)
        return node?.implemented && step.implemented
      }).length
      
      const completeness = (implementedSteps / journey.steps.length) * 100
      
      return {
        ...journey,
        completeness,
        lastValidated: Date.now()
      }
    })
  }

  const simulateUserJourney = async (journeyId: string) => {
    const journey = userJourneys.find(j => j.id === journeyId)
    if (!journey) return

    toast.info(`Simulating journey: ${journey.name}`)
    
    // Simulate each step with validation
    for (const step of journey.steps) {
      await new Promise(resolve => setTimeout(resolve, 1000)) // Simulate step execution
      
      const node = navigationGraph.nodes.find(n => n.id === step.nodeId || n.route === step.nodeId)
      
      if (node?.implemented) {
        toast.success(`✓ ${step.action}`)
      } else {
        toast.error(`✗ ${step.action} - Not implemented`)
        break
      }
    }
    
    toast.success(`Journey simulation completed`)
  }

  const getJourneyStatusColor = (completeness: number) => {
    if (completeness >= 90) return 'bg-green-500'
    if (completeness >= 70) return 'bg-yellow-500'
    if (completeness >= 50) return 'bg-orange-500'
    return 'bg-red-500'
  }

  const getNodeTypeIcon = (type: NavigationNode['type']) => {
    switch (type) {
      case 'page': return <Route className="w-4 h-4" />
      case 'component': return <Network className="w-4 h-4" />
      case 'api': return <MapPin className="w-4 h-4" />
      case 'action': return <Target className="w-4 h-4" />
      default: return <Network className="w-4 h-4" />
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-semibold flex items-center gap-2">
            <Route className="w-6 h-6" />
            Navigation Flow Tracker
          </h2>
          <p className="text-muted-foreground">
            Complete navigation flow analysis and user journey validation
          </p>
        </div>
        <Button 
          onClick={analyzeNavigationFlow}
          disabled={isAnalyzing}
          className="flex items-center gap-2"
        >
          <RefreshCcw className="w-4 h-4" />
          {isAnalyzing ? 'Analyzing...' : 'Analyze Flows'}
        </Button>
      </div>

      {flowValidation && (
        <Alert>
          <CheckCircle className="w-4 h-4" />
          <AlertDescription>
            Flow analysis complete: {flowValidation.validJourneys}/{flowValidation.totalJourneys} journeys valid, 
            {flowValidation.criticalPathIssues} critical path issues, 
            {flowValidation.blockedSteps} blocked steps
          </AlertDescription>
        </Alert>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Navigation Nodes</CardTitle>
            <Network className="w-4 h-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{navigationGraph.nodes.length}</div>
            <p className="text-xs text-muted-foreground">
              {navigationGraph.nodes.filter(n => n.implemented).length} implemented
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">User Journeys</CardTitle>
            <Users className="w-4 h-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{userJourneys.length}</div>
            <p className="text-xs text-muted-foreground">
              {userJourneys.filter(j => j.completeness >= 90).length} complete
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Issues Found</CardTitle>
            <AlertTriangle className="w-4 h-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {navigationGraph.orphanNodes.length + navigationGraph.deadEnds.length + navigationGraph.unreachableNodes.length}
            </div>
            <p className="text-xs text-muted-foreground">
              {navigationGraph.orphanNodes.length} orphan nodes
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Flow Completion</CardTitle>
            <Target className="w-4 h-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {userJourneys.length > 0 ? 
                Math.round(userJourneys.reduce((acc, j) => acc + j.completeness, 0) / userJourneys.length) : 0
              }%
            </div>
            <p className="text-xs text-muted-foreground">
              Average completeness
            </p>
          </CardContent>
        </Card>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle>User Journeys</CardTitle>
            <CardDescription>Critical user paths through the application</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {userJourneys.map((journey) => (
                <div key={journey.id} className="border rounded-lg p-4">
                  <div className="flex items-center justify-between mb-3">
                    <div className="flex items-center gap-3">
                      <div className={`w-3 h-3 rounded-full ${getJourneyStatusColor(journey.completeness)}`} />
                      <h4 className="font-medium">{journey.name}</h4>
                      {journey.criticalPath && (
                        <Badge variant="destructive" className="text-xs">Critical</Badge>
                      )}
                    </div>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => simulateUserJourney(journey.id)}
                      className="flex items-center gap-1"
                    >
                      <Play className="w-3 h-3" />
                      Test
                    </Button>
                  </div>
                  
                  <p className="text-sm text-muted-foreground mb-3">{journey.description}</p>
                  
                  <div className="space-y-2">
                    <div className="flex justify-between text-sm">
                      <span>Completion</span>
                      <span>{journey.completeness.toFixed(1)}%</span>
                    </div>
                    <Progress value={journey.completeness} className="h-2" />
                  </div>
                  
                  <div className="mt-3">
                    <h5 className="text-sm font-medium mb-2">Steps ({journey.steps.length})</h5>
                    <div className="space-y-2">
                      {journey.steps.slice(0, 3).map((step, index) => (
                        <div key={step.id} className="flex items-center gap-2 text-sm">
                          <div className={`w-6 h-6 rounded-full flex items-center justify-center text-xs ${
                            step.implemented ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'
                          }`}>
                            {index + 1}
                          </div>
                          <span className={step.blocked ? 'line-through text-muted-foreground' : ''}>
                            {step.action}
                          </span>
                          {step.blocked && (
                            <AlertTriangle className="w-4 h-4 text-orange-500" />
                          )}
                        </div>
                      ))}
                      {journey.steps.length > 3 && (
                        <div className="text-xs text-muted-foreground">
                          +{journey.steps.length - 3} more steps
                        </div>
                      )}
                    </div>
                  </div>
                  
                  {journey.issues.length > 0 && (
                    <div className="mt-3 p-2 bg-orange-50 rounded">
                      <h5 className="text-sm font-medium text-orange-800 mb-1">Issues</h5>
                      <div className="space-y-1">
                        {journey.issues.slice(0, 2).map((issue, index) => (
                          <div key={index} className="text-xs text-orange-700">{issue}</div>
                        ))}
                        {journey.issues.length > 2 && (
                          <div className="text-xs text-orange-600">+{journey.issues.length - 2} more</div>
                        )}
                      </div>
                    </div>
                  )}
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Navigation Graph</CardTitle>
            <CardDescription>Application structure and flow validation</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4 text-sm">
                <div>
                  <span className="text-muted-foreground">Total Nodes:</span>
                  <span className="ml-2 font-medium">{navigationGraph.nodes.length}</span>
                </div>
                <div>
                  <span className="text-muted-foreground">Connections:</span>
                  <span className="ml-2 font-medium">{navigationGraph.edges.length}</span>
                </div>
                <div>
                  <span className="text-muted-foreground">Orphan Nodes:</span>
                  <span className="ml-2 font-medium text-orange-600">{navigationGraph.orphanNodes.length}</span>
                </div>
                <div>
                  <span className="text-muted-foreground">Dead Ends:</span>
                  <span className="ml-2 font-medium text-red-600">{navigationGraph.deadEnds.length}</span>
                </div>
              </div>

              {navigationGraph.nodes.length > 0 && (
                <div>
                  <h4 className="font-medium mb-3">Navigation Nodes</h4>
                  <div className="space-y-2 max-h-64 overflow-y-auto">
                    {navigationGraph.nodes.map((node) => (
                      <div 
                        key={node.id} 
                        className={`p-3 border rounded cursor-pointer transition-colors ${
                          selectedNode?.id === node.id ? 'bg-blue-50 border-blue-200' : 'hover:bg-gray-50'
                        }`}
                        onClick={() => setSelectedNode(node)}
                      >
                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-2">
                            {getNodeTypeIcon(node.type)}
                            <span className="font-medium">{node.component}</span>
                          </div>
                          <div className="flex items-center gap-2">
                            <div className={`w-2 h-2 rounded-full ${
                              node.implemented ? 'bg-green-500' : 'bg-red-500'
                            }`} />
                            <Badge variant="outline" className="text-xs">{node.type}</Badge>
                          </div>
                        </div>
                        <div className="text-xs text-muted-foreground mt-1">
                          {node.route}
                        </div>
                        {node.metadata.userActions.length > 0 && (
                          <div className="text-xs text-blue-600 mt-1">
                            {node.metadata.userActions.length} user actions
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {(navigationGraph.orphanNodes.length > 0 || navigationGraph.deadEnds.length > 0) && (
                <Alert>
                  <AlertTriangle className="w-4 h-4" />
                  <AlertDescription>
                    Found navigation issues: {navigationGraph.orphanNodes.length} orphan nodes, 
                    {navigationGraph.deadEnds.length} dead ends, 
                    {navigationGraph.unreachableNodes.length} unreachable nodes
                  </AlertDescription>
                </Alert>
              )}
            </div>
          </CardContent>
        </Card>
      </div>

      {selectedNode && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              {getNodeTypeIcon(selectedNode.type)}
              {selectedNode.component}
            </CardTitle>
            <CardDescription>Node details and connections</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="space-y-4">
                <div>
                  <h4 className="font-medium mb-2">Node Information</h4>
                  <div className="space-y-2 text-sm">
                    <div className="flex justify-between">
                      <span>Route:</span>
                      <code className="bg-muted px-1 rounded">{selectedNode.route}</code>
                    </div>
                    <div className="flex justify-between">
                      <span>Type:</span>
                      <Badge variant="outline">{selectedNode.type}</Badge>
                    </div>
                    <div className="flex justify-between">
                      <span>Implemented:</span>
                      <Badge variant={selectedNode.implemented ? "default" : "secondary"}>
                        {selectedNode.implemented ? 'Yes' : 'No'}
                      </Badge>
                    </div>
                    <div className="flex justify-between">
                      <span>Tested:</span>
                      <Badge variant={selectedNode.tested ? "default" : "secondary"}>
                        {selectedNode.tested ? 'Yes' : 'No'}
                      </Badge>
                    </div>
                  </div>
                </div>

                {selectedNode.metadata.userActions.length > 0 && (
                  <div>
                    <h4 className="font-medium mb-2">User Actions</h4>
                    <div className="space-y-1">
                      {selectedNode.metadata.userActions.map((action, index) => (
                        <div key={index} className="text-sm flex items-center gap-2">
                          <Target className="w-3 h-3" />
                          {action}
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>

              <div className="space-y-4">
                {selectedNode.metadata.dependencies.length > 0 && (
                  <div>
                    <h4 className="font-medium mb-2">Dependencies</h4>
                    <div className="space-y-1">
                      {selectedNode.metadata.dependencies.slice(0, 5).map((dep, index) => (
                        <div key={index} className="text-sm">
                          <Badge variant="outline" className="text-xs">{dep}</Badge>
                        </div>
                      ))}
                      {selectedNode.metadata.dependencies.length > 5 && (
                        <div className="text-xs text-muted-foreground">
                          +{selectedNode.metadata.dependencies.length - 5} more
                        </div>
                      )}
                    </div>
                  </div>
                )}

                <div>
                  <h4 className="font-medium mb-2">Connections</h4>
                  <div className="text-sm text-muted-foreground">
                    {navigationGraph.edges.filter(edge => 
                      edge.from === selectedNode.id || edge.to === selectedNode.id
                    ).length} connections
                  </div>
                </div>

                {selectedNode.metadata.lastVisited && (
                  <div>
                    <h4 className="font-medium mb-2">Last Activity</h4>
                    <div className="text-sm text-muted-foreground flex items-center gap-2">
                      <Clock className="w-4 h-4" />
                      {new Date(selectedNode.metadata.lastVisited).toLocaleString()}
                    </div>
                  </div>
                )}
              </div>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  )
}