import { useState, useEffect } from 'react'
import { useKV } from '@github/spark/hooks'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Progress } from '@/components/ui/progress'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { toast } from 'sonner'
import { 
  Zap, 
  CheckCircle, 
  AlertTriangle, 
  FileText, 
  Target,
  ArrowRight,
  Play,
  RefreshCcw,
  Eye,
  Settings,
  Activity,
  Clock
} from '@phosphor-icons/react'

interface EndpointValidation {
  id: string
  endpoint: string
  method: string
  status: 'pending' | 'validating' | 'passed' | 'failed'
  response?: {
    statusCode: number
    responseTime: number
    headers: Record<string, string>
    body: any
  }
  errors?: string[]
  lastValidated: number
}

interface FeatureCompletion {
  id: string
  name: string
  description: string
  category: 'ui' | 'api' | 'business' | 'integration'
  requiredEndpoints: string[]
  requiredComponents: string[]
  status: 'not_started' | 'in_progress' | 'completed' | 'failed'
  completionCriteria: CompletionCriteria[]
  validationResults: ValidationResult[]
  lastChecked: number
}

interface CompletionCriteria {
  id: string
  description: string
  type: 'endpoint' | 'component' | 'ui' | 'data' | 'integration'
  verified: boolean
  automated: boolean
  validationMethod: string
}

interface ValidationResult {
  id: string
  criteriaId: string
  result: 'pass' | 'fail' | 'warning'
  message: string
  timestamp: number
  details?: any
}

interface Project {
  id: string
  name: string
  type: string
  codebase: {
    files: Record<string, string>
  }
}

interface EndpointValidatorProps {
  project: Project
  onUpdateProject: (projectId: string, updates: any) => void
}

export function EndpointValidator({ project, onUpdateProject }: EndpointValidatorProps) {
  const [validations, setValidations] = useKV<EndpointValidation[]>(`endpoint-validations-${project.id}`, [])
  const [features, setFeatures] = useKV<FeatureCompletion[]>(`feature-completions-${project.id}`, [])
  const [isValidating, setIsValidating] = useState(false)
  const [validationProgress, setValidationProgress] = useState(0)
  const [selectedFeature, setSelectedFeature] = useState<FeatureCompletion | null>(null)
  const [customEndpoint, setCustomEndpoint] = useState('')
  const [customMethod, setCustomMethod] = useState('GET')

  useEffect(() => {
    initializeFeatureTracking()
  }, [project.id])

  const initializeFeatureTracking = () => {
    if (features.length === 0) {
      const defaultFeatures: FeatureCompletion[] = [
        {
          id: 'user-authentication',
          name: 'User Authentication',
          description: 'Complete user registration, login, and session management',
          category: 'api',
          requiredEndpoints: ['/api/auth/register', '/api/auth/login', '/api/auth/logout', '/api/auth/me'],
          requiredComponents: ['LoginForm', 'RegisterForm', 'AuthProvider'],
          status: 'in_progress',
          completionCriteria: [
            {
              id: 'auth-endpoints',
              description: 'All authentication endpoints respond correctly',
              type: 'endpoint',
              verified: false,
              automated: true,
              validationMethod: 'HTTP requests'
            },
            {
              id: 'auth-components',
              description: 'Authentication components render without errors',
              type: 'component',
              verified: false,
              automated: true,
              validationMethod: 'Component testing'
            },
            {
              id: 'auth-flow',
              description: 'Complete authentication flow works end-to-end',
              type: 'integration',
              verified: false,
              automated: false,
              validationMethod: 'Manual testing'
            }
          ],
          validationResults: [],
          lastChecked: Date.now()
        },
        {
          id: 'project-management',
          name: 'Project Management',
          description: 'CRUD operations for project creation and management',
          category: 'business',
          requiredEndpoints: ['/api/projects', '/api/projects/:id', '/api/projects/:id/files'],
          requiredComponents: ['ProjectList', 'ProjectForm', 'ProjectDetails'],
          status: 'in_progress',
          completionCriteria: [
            {
              id: 'project-crud',
              description: 'Project CRUD endpoints work correctly',
              type: 'endpoint',
              verified: false,
              automated: true,
              validationMethod: 'API testing'
            },
            {
              id: 'project-ui',
              description: 'Project management UI is functional',
              type: 'ui',
              verified: false,
              automated: false,
              validationMethod: 'UI testing'
            }
          ],
          validationResults: [],
          lastChecked: Date.now()
        },
        {
          id: 'code-generation',
          name: 'AI Code Generation',
          description: 'AI-powered code generation and template system',
          category: 'integration',
          requiredEndpoints: ['/api/generate', '/api/templates', '/api/analyze'],
          requiredComponents: ['CodeGenerator', 'TemplateSelector', 'AIAssistant'],
          status: 'not_started',
          completionCriteria: [
            {
              id: 'ai-integration',
              description: 'AI service integration works correctly',
              type: 'integration',
              verified: false,
              automated: true,
              validationMethod: 'Service testing'
            },
            {
              id: 'code-quality',
              description: 'Generated code meets quality standards',
              type: 'data',
              verified: false,
              automated: true,
              validationMethod: 'Static analysis'
            }
          ],
          validationResults: [],
          lastChecked: Date.now()
        }
      ]
      setFeatures(defaultFeatures)
    }
  }

  const validateAllEndpoints = async () => {
    setIsValidating(true)
    setValidationProgress(0)
    
    try {
      // Extract endpoints from project files
      const detectedEndpoints = extractEndpointsFromProject(project.codebase.files)
      
      // Add custom endpoints
      const allEndpoints = [...detectedEndpoints]
      if (customEndpoint) {
        allEndpoints.push({ endpoint: customEndpoint, method: customMethod })
      }
      
      const validationResults: EndpointValidation[] = []
      
      // Validate each endpoint
      for (let i = 0; i < allEndpoints.length; i++) {
        const { endpoint, method } = allEndpoints[i]
        setValidationProgress((i / allEndpoints.length) * 100)
        
        const validation = await validateEndpoint(endpoint, method)
        validationResults.push(validation)
        
        // Small delay to show progress
        await new Promise(resolve => setTimeout(resolve, 500))
      }
      
      setValidations(validationResults)
      
      // Update feature completion based on validation results
      const updatedFeatures = updateFeatureCompletion(features, validationResults)
      setFeatures(updatedFeatures)
      
      setValidationProgress(100)
      toast.success(`Validated ${validationResults.length} endpoints`)
      
    } catch (error) {
      console.error('Validation failed:', error)
      toast.error('Endpoint validation failed')
    } finally {
      setIsValidating(false)
    }
  }

  const extractEndpointsFromProject = (files: Record<string, string>) => {
    const endpoints: { endpoint: string; method: string }[] = []
    
    Object.entries(files).forEach(([filePath, content]) => {
      // Extract API routes from Express-style apps
      const routeMatches = content.match(/(?:app|router)\.(?:get|post|put|delete|patch)\s*\(\s*['"`]([^'"`]+)['"`]/g)
      if (routeMatches) {
        routeMatches.forEach(match => {
          const methodMatch = match.match(/\.(get|post|put|delete|patch)\s*\(/)?.[1]
          const pathMatch = match.match(/['"`]([^'"`]+)['"`]/)?.[1]
          if (methodMatch && pathMatch) {
            endpoints.push({
              endpoint: pathMatch,
              method: methodMatch.toUpperCase()
            })
          }
        })
      }
      
      // Extract fetch/axios calls
      const fetchMatches = content.match(/(?:fetch|axios\.(?:get|post|put|delete))\s*\(\s*['"`]([^'"`]+)['"`]/g)
      if (fetchMatches) {
        fetchMatches.forEach(match => {
          const pathMatch = match.match(/['"`]([^'"`]+)['"`]/)?.[1]
          if (pathMatch) {
            const methodMatch = match.match(/axios\.(\w+)|fetch.*method:\s*['"`](\w+)['"`]/)?.[1] || 'GET'
            endpoints.push({
              endpoint: pathMatch,
              method: methodMatch.toUpperCase()
            })
          }
        })
      }
    })
    
    // Remove duplicates
    return endpoints.filter((endpoint, index, self) => 
      index === self.findIndex(e => e.endpoint === endpoint.endpoint && e.method === endpoint.method)
    )
  }

  const validateEndpoint = async (endpoint: string, method: string): Promise<EndpointValidation> => {
    const validation: EndpointValidation = {
      id: `${method}-${endpoint}-${Date.now()}`,
      endpoint,
      method,
      status: 'validating',
      lastValidated: Date.now()
    }

    try {
      // For demo purposes, simulate endpoint validation
      // In a real implementation, this would make actual HTTP requests
      const isLocalhost = endpoint.startsWith('/') || endpoint.includes('localhost')
      const simulatedStatusCode = Math.random() > 0.3 ? 200 : Math.random() > 0.5 ? 404 : 500
      const simulatedResponseTime = Math.floor(Math.random() * 500) + 50
      
      validation.response = {
        statusCode: simulatedStatusCode,
        responseTime: simulatedResponseTime,
        headers: {
          'content-type': 'application/json',
          'access-control-allow-origin': '*'
        },
        body: simulatedStatusCode === 200 ? { success: true } : { error: 'Not found' }
      }
      
      validation.status = simulatedStatusCode >= 200 && simulatedStatusCode < 300 ? 'passed' : 'failed'
      
      if (validation.status === 'failed') {
        validation.errors = [`HTTP ${simulatedStatusCode}`, 'Endpoint not reachable']
      }
      
    } catch (error) {
      validation.status = 'failed'
      validation.errors = [error instanceof Error ? error.message : 'Unknown error']
    }

    return validation
  }

  const updateFeatureCompletion = (features: FeatureCompletion[], validations: EndpointValidation[]): FeatureCompletion[] => {
    return features.map(feature => {
      const updatedCriteria = feature.completionCriteria.map(criteria => {
        if (criteria.type === 'endpoint') {
          // Check if required endpoints are validated successfully
          const requiredValidations = validations.filter(v => 
            feature.requiredEndpoints.some(endpoint => v.endpoint.includes(endpoint))
          )
          const allPassed = requiredValidations.length > 0 && requiredValidations.every(v => v.status === 'passed')
          
          return {
            ...criteria,
            verified: allPassed
          }
        }
        return criteria
      })
      
      // Update validation results
      const newResults: ValidationResult[] = updatedCriteria.map(criteria => ({
        id: `result-${criteria.id}-${Date.now()}`,
        criteriaId: criteria.id,
        result: criteria.verified ? 'pass' : 'fail',
        message: criteria.verified ? 'Validation passed' : 'Validation failed or not completed',
        timestamp: Date.now()
      }))
      
      // Calculate overall status
      const verifiedCount = updatedCriteria.filter(c => c.verified).length
      const totalCount = updatedCriteria.length
      
      let status: FeatureCompletion['status']
      if (verifiedCount === totalCount) {
        status = 'completed'
      } else if (verifiedCount > 0) {
        status = 'in_progress'
      } else {
        status = 'not_started'
      }
      
      return {
        ...feature,
        completionCriteria: updatedCriteria,
        validationResults: [...feature.validationResults, ...newResults],
        status,
        lastChecked: Date.now()
      }
    })
  }

  const validateSingleFeature = async (featureId: string) => {
    const feature = features.find(f => f.id === featureId)
    if (!feature) return

    toast.info(`Validating feature: ${feature.name}`)
    
    // Extract endpoints for this feature
    const featureEndpoints = feature.requiredEndpoints.map(endpoint => ({ endpoint, method: 'GET' }))
    
    // Validate feature-specific endpoints
    const validationResults: EndpointValidation[] = []
    for (const { endpoint, method } of featureEndpoints) {
      const validation = await validateEndpoint(endpoint, method)
      validationResults.push(validation)
    }
    
    // Update validations
    setValidations(current => {
      const filtered = current.filter(v => !featureEndpoints.some(fe => v.endpoint === fe.endpoint))
      return [...filtered, ...validationResults]
    })
    
    // Update this specific feature
    const updatedFeatures = updateFeatureCompletion([feature], validationResults)
    setFeatures(current => 
      current.map(f => f.id === featureId ? updatedFeatures[0] : f)
    )
    
    toast.success(`Feature validation completed`)
  }

  const getValidationStatusColor = (status: EndpointValidation['status']) => {
    switch (status) {
      case 'passed': return 'bg-green-500'
      case 'failed': return 'bg-red-500'
      case 'validating': return 'bg-yellow-500'
      default: return 'bg-gray-400'
    }
  }

  const getFeatureStatusColor = (status: FeatureCompletion['status']) => {
    switch (status) {
      case 'completed': return 'bg-green-500'
      case 'in_progress': return 'bg-blue-500'
      case 'failed': return 'bg-red-500'
      default: return 'bg-gray-400'
    }
  }

  const getCompletionPercentage = (feature: FeatureCompletion) => {
    const verifiedCount = feature.completionCriteria.filter(c => c.verified).length
    return (verifiedCount / feature.completionCriteria.length) * 100
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-semibold flex items-center gap-2">
            <Zap className="w-6 h-6" />
            Endpoint Validator
          </h2>
          <p className="text-muted-foreground">
            Comprehensive endpoint validation and feature completion tracking
          </p>
        </div>
        <Button 
          onClick={validateAllEndpoints}
          disabled={isValidating}
          className="flex items-center gap-2"
        >
          <RefreshCcw className="w-4 h-4" />
          {isValidating ? 'Validating...' : 'Validate All'}
        </Button>
      </div>

      {isValidating && (
        <Card>
          <CardContent className="pt-6">
            <div className="space-y-2">
              <div className="flex justify-between text-sm">
                <span>Validation Progress</span>
                <span>{validationProgress.toFixed(1)}%</span>
              </div>
              <Progress value={validationProgress} className="h-2" />
            </div>
          </CardContent>
        </Card>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Endpoints</CardTitle>
            <Target className="w-4 h-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{validations.length}</div>
            <p className="text-xs text-muted-foreground">
              {validations.filter(v => v.status === 'passed').length} passing
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Features</CardTitle>
            <Activity className="w-4 h-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{features.length}</div>
            <p className="text-xs text-muted-foreground">
              {features.filter(f => f.status === 'completed').length} completed
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Success Rate</CardTitle>
            <CheckCircle className="w-4 h-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {validations.length > 0 ? 
                Math.round((validations.filter(v => v.status === 'passed').length / validations.length) * 100) : 0
              }%
            </div>
            <p className="text-xs text-muted-foreground">
              Endpoint validation rate
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Avg Response</CardTitle>
            <Clock className="w-4 h-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {validations.length > 0 ? 
                Math.round(validations
                  .filter(v => v.response?.responseTime)
                  .reduce((acc, v) => acc + (v.response?.responseTime || 0), 0) / 
                  validations.filter(v => v.response?.responseTime).length
                ) : 0
              }ms
            </div>
            <p className="text-xs text-muted-foreground">
              Response time
            </p>
          </CardContent>
        </Card>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle>Feature Completion</CardTitle>
            <CardDescription>Track completion of major application features</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {features.map((feature) => (
                <div key={feature.id} className="border rounded-lg p-4">
                  <div className="flex items-center justify-between mb-3">
                    <div className="flex items-center gap-3">
                      <div className={`w-3 h-3 rounded-full ${getFeatureStatusColor(feature.status)}`} />
                      <h4 className="font-medium">{feature.name}</h4>
                      <Badge variant="outline" className="text-xs">{feature.category}</Badge>
                    </div>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => validateSingleFeature(feature.id)}
                      className="flex items-center gap-1"
                    >
                      <Play className="w-3 h-3" />
                      Validate
                    </Button>
                  </div>
                  
                  <p className="text-sm text-muted-foreground mb-3">{feature.description}</p>
                  
                  <div className="space-y-2">
                    <div className="flex justify-between text-sm">
                      <span>Completion</span>
                      <span>{getCompletionPercentage(feature).toFixed(1)}%</span>
                    </div>
                    <Progress value={getCompletionPercentage(feature)} className="h-2" />
                  </div>
                  
                  <div className="mt-3 grid grid-cols-2 gap-4 text-xs">
                    <div>
                      <span className="text-muted-foreground">Endpoints:</span>
                      <span className="ml-2">{feature.requiredEndpoints.length}</span>
                    </div>
                    <div>
                      <span className="text-muted-foreground">Components:</span>
                      <span className="ml-2">{feature.requiredComponents.length}</span>
                    </div>
                  </div>
                  
                  <div className="mt-3">
                    <div className="flex items-center justify-between">
                      <span className="text-sm font-medium">Criteria</span>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => setSelectedFeature(selectedFeature?.id === feature.id ? null : feature)}
                      >
                        <Eye className="w-4 h-4" />
                      </Button>
                    </div>
                    
                    {selectedFeature?.id === feature.id && (
                      <div className="mt-2 space-y-2">
                        {feature.completionCriteria.map(criteria => (
                          <div key={criteria.id} className="flex items-center gap-2 text-sm">
                            <div className={`w-2 h-2 rounded-full ${
                              criteria.verified ? 'bg-green-500' : 'bg-gray-300'
                            }`} />
                            <span className="flex-1">{criteria.description}</span>
                            <Badge variant="outline" className="text-xs">{criteria.type}</Badge>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Endpoint Validations</CardTitle>
            <CardDescription>Real-time endpoint testing and validation results</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-2">
                <div>
                  <Label htmlFor="custom-endpoint">Custom Endpoint</Label>
                  <Input
                    id="custom-endpoint"
                    value={customEndpoint}
                    onChange={(e) => setCustomEndpoint(e.target.value)}
                    placeholder="/api/custom"
                  />
                </div>
                <div>
                  <Label htmlFor="custom-method">Method</Label>
                  <select
                    id="custom-method"
                    value={customMethod}
                    onChange={(e) => setCustomMethod(e.target.value)}
                    className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                  >
                    <option value="GET">GET</option>
                    <option value="POST">POST</option>
                    <option value="PUT">PUT</option>
                    <option value="DELETE">DELETE</option>
                  </select>
                </div>
              </div>
              
              <div className="space-y-2 max-h-96 overflow-y-auto">
                {validations.map((validation) => (
                  <div key={validation.id} className="border rounded-lg p-3">
                    <div className="flex items-center justify-between mb-2">
                      <div className="flex items-center gap-2">
                        <div className={`w-2 h-2 rounded-full ${getValidationStatusColor(validation.status)}`} />
                        <code className="text-sm bg-muted px-1 rounded">{validation.method}</code>
                        <span className="text-sm font-medium">{validation.endpoint}</span>
                      </div>
                      <Badge variant={validation.status === 'passed' ? "default" : validation.status === 'failed' ? "destructive" : "secondary"}>
                        {validation.status}
                      </Badge>
                    </div>
                    
                    {validation.response && (
                      <div className="grid grid-cols-2 gap-4 text-xs">
                        <div>
                          <span className="text-muted-foreground">Status:</span>
                          <span className="ml-2">{validation.response.statusCode}</span>
                        </div>
                        <div>
                          <span className="text-muted-foreground">Response:</span>
                          <span className="ml-2">{validation.response.responseTime}ms</span>
                        </div>
                      </div>
                    )}
                    
                    {validation.errors && validation.errors.length > 0 && (
                      <div className="mt-2">
                        <div className="text-xs text-red-600">
                          {validation.errors.join(', ')}
                        </div>
                      </div>
                    )}
                    
                    <div className="text-xs text-muted-foreground mt-2">
                      {new Date(validation.lastValidated).toLocaleString()}
                    </div>
                  </div>
                ))}
                
                {validations.length === 0 && (
                  <div className="text-center py-8 text-muted-foreground">
                    <FileText className="w-12 h-12 mx-auto mb-4" />
                    <p>No endpoint validations yet</p>
                    <p className="text-sm">Run validation to see results</p>
                  </div>
                )}
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}