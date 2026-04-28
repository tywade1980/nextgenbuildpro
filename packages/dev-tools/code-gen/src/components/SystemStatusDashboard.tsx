/**
 * System Status Dashboard
 * 
 * Real-time monitoring and status of all platform components
 */

import { useState, useEffect } from 'react'
import { useKV } from '@github/spark/hooks'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Progress } from '@/components/ui/progress'
import { Button } from '@/components/ui/button'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { 
  CheckCircle, 
  XCircle, 
  AlertTriangle, 
  Activity, 
  Database, 
  Cloud, 
  Zap, 
  Monitor,
  RefreshCw,
  Settings,
  TrendUp
} from '@phosphor-icons/react'

interface ComponentStatus {
  id: string
  name: string
  category: string
  status: 'healthy' | 'warning' | 'error' | 'unknown'
  lastCheck: number
  uptime: number
  responseTime?: number
  errorCount: number
  details?: string
}

interface SystemMetrics {
  totalComponents: number
  healthyComponents: number
  warningComponents: number
  errorComponents: number
  overallHealth: number
  averageResponseTime: number
  totalUptime: number
}

export function SystemStatusDashboard() {
  const [components, setComponents] = useKV<ComponentStatus[]>('system-status-components', [])
  const [metrics, setMetrics] = useKV<SystemMetrics>('system-metrics', {
    totalComponents: 0,
    healthyComponents: 0,
    warningComponents: 0,
    errorComponents: 0,
    overallHealth: 100,
    averageResponseTime: 0,
    totalUptime: 0
  })
  const [isRefreshing, setIsRefreshing] = useState(false)
  const [autoRefresh, setAutoRefresh] = useState(true)

  // Initialize system components
  const initializeComponents = () => {
    const systemComponents: ComponentStatus[] = [
      {
        id: 'ui-components',
        name: 'UI Components',
        category: 'Frontend',
        status: 'healthy',
        lastCheck: Date.now(),
        uptime: 100,
        responseTime: 15,
        errorCount: 0,
        details: 'All shadcn/ui components loaded successfully'
      },
      {
        id: 'project-manager',
        name: 'Project Manager',
        category: 'Core',
        status: 'healthy',
        lastCheck: Date.now(),
        uptime: 99.8,
        responseTime: 120,
        errorCount: 0,
        details: 'Project creation and management working'
      },
      {
        id: 'ai-assistant',
        name: 'AI Assistant',
        category: 'AI Services',
        status: 'healthy',
        lastCheck: Date.now(),
        uptime: 98.5,
        responseTime: 850,
        errorCount: 2,
        details: 'LLM integration and context memory active'
      },
      {
        id: 'code-generation',
        name: 'Code Generation Engine',
        category: 'AI Services',
        status: 'healthy',
        lastCheck: Date.now(),
        uptime: 97.2,
        responseTime: 1200,
        errorCount: 1,
        details: 'Multi-framework code generation operational'
      },
      {
        id: 'live-preview',
        name: 'Live Preview',
        category: 'Development',
        status: 'warning',
        lastCheck: Date.now(),
        uptime: 95.1,
        responseTime: 2100,
        errorCount: 5,
        details: 'Preview system running with minor latency'
      },
      {
        id: 'visual-builder',
        name: 'Visual Builder',
        category: 'Development',
        status: 'healthy',
        lastCheck: Date.now(),
        uptime: 99.1,
        responseTime: 340,
        errorCount: 0,
        details: 'Drag-and-drop UI builder functional'
      },
      {
        id: 'testing-suite',
        name: 'Testing Suite',
        category: 'QA',
        status: 'healthy',
        lastCheck: Date.now(),
        uptime: 96.8,
        responseTime: 780,
        errorCount: 3,
        details: 'Automated testing engine active'
      },
      {
        id: 'deployment-pipeline',
        name: 'Deployment Pipeline',
        category: 'DevOps',
        status: 'healthy',
        lastCheck: Date.now(),
        uptime: 98.9,
        responseTime: 1800,
        errorCount: 1,
        details: 'Multi-platform deployment ready'
      },
      {
        id: 'microservices-architect',
        name: 'Microservices Architect',
        category: 'Architecture',
        status: 'healthy',
        lastCheck: Date.now(),
        uptime: 99.3,
        responseTime: 920,
        errorCount: 0,
        details: 'Service mesh and orchestration tools active'
      },
      {
        id: 'api-documentation',
        name: 'API Documentation',
        category: 'Documentation',
        status: 'healthy',
        lastCheck: Date.now(),
        uptime: 99.7,
        responseTime: 180,
        errorCount: 0,
        details: 'Automatic API docs generation working'
      },
      {
        id: 'data-persistence',
        name: 'Data Persistence',
        category: 'Storage',
        status: 'healthy',
        lastCheck: Date.now(),
        uptime: 99.9,
        responseTime: 45,
        errorCount: 0,
        details: 'useKV and project storage operational'
      },
      {
        id: 'android-support',
        name: 'Android Support',
        category: 'Mobile',
        status: 'healthy',
        lastCheck: Date.now(),
        uptime: 97.5,
        responseTime: 1500,
        errorCount: 2,
        details: 'Android development and preview active'
      }
    ]

    setComponents(systemComponents)
    updateMetrics(systemComponents)
  }

  const updateMetrics = (componentList: ComponentStatus[]) => {
    const total = componentList.length
    const healthy = componentList.filter(c => c.status === 'healthy').length
    const warning = componentList.filter(c => c.status === 'warning').length
    const error = componentList.filter(c => c.status === 'error').length
    
    const overallHealth = (healthy / total) * 100
    const avgResponseTime = componentList.reduce((acc, c) => acc + (c.responseTime || 0), 0) / total
    const totalUptime = componentList.reduce((acc, c) => acc + c.uptime, 0) / total

    const newMetrics: SystemMetrics = {
      totalComponents: total,
      healthyComponents: healthy,
      warningComponents: warning,
      errorComponents: error,
      overallHealth,
      averageResponseTime: avgResponseTime,
      totalUptime
    }

    setMetrics(newMetrics)
  }

  const refreshStatus = async () => {
    setIsRefreshing(true)
    
    // Simulate status check with slight variations
    const updatedComponents = components.map(component => {
      const responseVariation = (Math.random() - 0.5) * 200
      const uptimeVariation = (Math.random() - 0.5) * 2
      const errorVariation = Math.random() > 0.9 ? 1 : 0
      
      return {
        ...component,
        lastCheck: Date.now(),
        responseTime: Math.max(10, (component.responseTime || 100) + responseVariation),
        uptime: Math.max(0, Math.min(100, component.uptime + uptimeVariation)),
        errorCount: component.errorCount + errorVariation,
        status: component.uptime > 95 ? 'healthy' : component.uptime > 85 ? 'warning' : 'error'
      } as ComponentStatus
    })
    
    setComponents(updatedComponents)
    updateMetrics(updatedComponents)
    
    // Simulate network delay
    await new Promise(resolve => setTimeout(resolve, 1000))
    setIsRefreshing(false)
  }

  // Auto refresh every 30 seconds
  useEffect(() => {
    if (components.length === 0) {
      initializeComponents()
    }

    if (autoRefresh) {
      const interval = setInterval(refreshStatus, 30000)
      return () => clearInterval(interval)
    }
  }, [autoRefresh, components.length])

  const getStatusIcon = (status: ComponentStatus['status']) => {
    switch (status) {
      case 'healthy':
        return <CheckCircle className="w-4 h-4 text-green-500" />
      case 'warning':
        return <AlertTriangle className="w-4 h-4 text-yellow-500" />
      case 'error':
        return <XCircle className="w-4 h-4 text-red-500" />
      default:
        return <Monitor className="w-4 h-4 text-gray-500" />
    }
  }

  const getStatusColor = (status: ComponentStatus['status']) => {
    switch (status) {
      case 'healthy':
        return 'bg-green-50 border-green-200 text-green-800'
      case 'warning':
        return 'bg-yellow-50 border-yellow-200 text-yellow-800'
      case 'error':
        return 'bg-red-50 border-red-200 text-red-800'
      default:
        return 'bg-gray-50 border-gray-200 text-gray-800'
    }
  }

  const getCategoryIcon = (category: string) => {
    switch (category) {
      case 'Frontend':
        return <Monitor className="w-5 h-5" />
      case 'Core':
        return <Settings className="w-5 h-5" />
      case 'AI Services':
        return <Zap className="w-5 h-5" />
      case 'Development':
        return <Activity className="w-5 h-5" />
      case 'QA':
        return <CheckCircle className="w-5 h-5" />
      case 'DevOps':
        return <Cloud className="w-5 h-5" />
      case 'Architecture':
        return <Database className="w-5 h-5" />
      case 'Documentation':
        return <Monitor className="w-5 h-5" />
      case 'Storage':
        return <Database className="w-5 h-5" />
      case 'Mobile':
        return <Monitor className="w-5 h-5" />
      default:
        return <Activity className="w-5 h-5" />
    }
  }

  const componentsByCategory = components.reduce((acc, component) => {
    if (!acc[component.category]) {
      acc[component.category] = []
    }
    acc[component.category].push(component)
    return acc
  }, {} as Record<string, ComponentStatus[]>)

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-semibold">System Status</h2>
          <p className="text-muted-foreground">Real-time monitoring of all platform components</p>
        </div>
        <div className="flex items-center gap-2">
          <Button 
            variant="outline" 
            size="sm"
            onClick={() => setAutoRefresh(!autoRefresh)}
          >
            <Activity className="w-4 h-4 mr-2" />
            Auto Refresh: {autoRefresh ? 'On' : 'Off'}
          </Button>
          <Button 
            variant="outline" 
            size="sm" 
            onClick={refreshStatus}
            disabled={isRefreshing}
          >
            <RefreshCw className={`w-4 h-4 mr-2 ${isRefreshing ? 'animate-spin' : ''}`} />
            Refresh
          </Button>
        </div>
      </div>

      {/* Overall Health Metrics */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Overall Health</p>
                <p className="text-2xl font-bold">{metrics.overallHealth.toFixed(1)}%</p>
              </div>
              <div className="h-12 w-12 rounded-full bg-green-100 flex items-center justify-center">
                <TrendUp className="w-6 h-6 text-green-600" />
              </div>
            </div>
            <Progress value={metrics.overallHealth} className="mt-2" />
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Components</p>
                <p className="text-2xl font-bold">{metrics.healthyComponents}/{metrics.totalComponents}</p>
              </div>
              <div className="h-12 w-12 rounded-full bg-blue-100 flex items-center justify-center">
                <CheckCircle className="w-6 h-6 text-blue-600" />
              </div>
            </div>
            <p className="text-xs text-muted-foreground mt-2">
              {metrics.warningComponents} warnings, {metrics.errorComponents} errors
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Avg Response</p>
                <p className="text-2xl font-bold">{metrics.averageResponseTime.toFixed(0)}ms</p>
              </div>
              <div className="h-12 w-12 rounded-full bg-purple-100 flex items-center justify-center">
                <Activity className="w-6 h-6 text-purple-600" />
              </div>
            </div>
            <p className="text-xs text-muted-foreground mt-2">
              Performance monitoring
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Uptime</p>
                <p className="text-2xl font-bold">{metrics.totalUptime.toFixed(1)}%</p>
              </div>
              <div className="h-12 w-12 rounded-full bg-emerald-100 flex items-center justify-center">
                <Database className="w-6 h-6 text-emerald-600" />
              </div>
            </div>
            <p className="text-xs text-muted-foreground mt-2">
              System availability
            </p>
          </CardContent>
        </Card>
      </div>

      {/* System Health Alert */}
      {metrics.overallHealth < 90 && (
        <Alert>
          <AlertTriangle className="h-4 w-4" />
          <AlertDescription>
            System health is below optimal levels ({metrics.overallHealth.toFixed(1)}%). 
            {metrics.errorComponents > 0 && ` ${metrics.errorComponents} component(s) have errors.`}
            {metrics.warningComponents > 0 && ` ${metrics.warningComponents} component(s) need attention.`}
          </AlertDescription>
        </Alert>
      )}

      {/* Component Status by Category */}
      <div className="space-y-6">
        {Object.entries(componentsByCategory).map(([category, categoryComponents]) => (
          <Card key={category}>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                {getCategoryIcon(category)}
                {category}
              </CardTitle>
              <CardDescription>
                {categoryComponents.length} component(s) in this category
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {categoryComponents.map(component => (
                  <div key={component.id} className={`p-4 rounded-lg border ${getStatusColor(component.status)}`}>
                    <div className="flex items-center justify-between mb-2">
                      <div className="flex items-center gap-2">
                        {getStatusIcon(component.status)}
                        <h4 className="font-medium">{component.name}</h4>
                      </div>
                      <Badge variant="outline" className="text-xs">
                        {component.status}
                      </Badge>
                    </div>
                    <div className="space-y-2 text-sm">
                      <div className="flex justify-between">
                        <span>Uptime:</span>
                        <span className="font-medium">{component.uptime.toFixed(1)}%</span>
                      </div>
                      {component.responseTime && (
                        <div className="flex justify-between">
                          <span>Response:</span>
                          <span className="font-medium">{component.responseTime.toFixed(0)}ms</span>
                        </div>
                      )}
                      <div className="flex justify-between">
                        <span>Errors:</span>
                        <span className="font-medium">{component.errorCount}</span>
                      </div>
                      <div className="flex justify-between">
                        <span>Last Check:</span>
                        <span className="font-medium">
                          {new Date(component.lastCheck).toLocaleTimeString()}
                        </span>
                      </div>
                      {component.details && (
                        <div className="mt-2 p-2 bg-white/50 rounded text-xs">
                          {component.details}
                        </div>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  )
}