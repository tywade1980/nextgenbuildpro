import { useState, useEffect, useRef } from 'react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Progress } from '@/components/ui/progress'
import { ScrollArea } from '@/components/ui/scroll-area'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Play, Square, RefreshCw, ExternalLink, Monitor, Terminal, Activity, Clock } from '@phosphor-icons/react'
import { toast } from 'sonner'
import { codeExecutionEngine, ExecutionResult } from '@/services/CodeExecutionEngine'

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

interface LivePreviewProps {
  project: Project
  onUpdateProject: (projectId: string, updates: Partial<Project>) => void
}

export function LivePreview({ project, onUpdateProject }: LivePreviewProps) {
  const [isRunning, setIsRunning] = useState(false)
  const [executionResult, setExecutionResult] = useState<ExecutionResult | null>(null)
  const [logs, setLogs] = useState<Array<{ timestamp: number; level: string; message: string }>>([])
  const [performance, setPerformance] = useState({
    startupTime: 0,
    memoryUsage: 0,
    bundleSize: 0
  })
  const iframeRef = useRef<HTMLIFrameElement>(null)
  const [activeTab, setActiveTab] = useState('preview')

  const startApplication = async () => {
    if (!project.codebase.files || Object.keys(project.codebase.files).length === 0) {
      toast.error('No code to execute. Generate or write some code first.')
      return
    }

    setIsRunning(true)
    toast.info('Starting application...')

    try {
      const result = await codeExecutionEngine.executeApplication(
        project.id,
        project.codebase.files,
        project.type
      )

      setExecutionResult(result)
      
      if (result.success) {
        // Load preview in iframe
        if (result.output && iframeRef.current) {
          if (result.output.startsWith('http') || result.output.startsWith('blob:')) {
            iframeRef.current.src = result.output
          }
        }

        if (result.performance) {
          setPerformance(result.performance)
        }

        // Get live logs
        const appLogs = codeExecutionEngine.getApplicationLogs(project.id)
        setLogs(appLogs)

        toast.success('Application started successfully!')
        onUpdateProject(project.id, { status: 'testing' })
      } else {
        toast.error(result.error || 'Failed to start application')
        setLogs(result.logs.map(log => ({
          timestamp: Date.now(),
          level: 'error',
          message: log
        })))
      }
    } catch (error) {
      console.error('Execution error:', error)
      toast.error('Failed to start application')
      setExecutionResult({
        success: false,
        error: error instanceof Error ? error.message : 'Unknown error',
        logs: []
      })
    }
  }

  const stopApplication = () => {
    codeExecutionEngine.stopApplication(project.id)
    setIsRunning(false)
    setExecutionResult(null)
    setLogs([])
    
    if (iframeRef.current) {
      iframeRef.current.src = 'about:blank'
    }
    
    toast.info('Application stopped')
    onUpdateProject(project.id, { status: 'development' })
  }

  const restartApplication = async () => {
    stopApplication()
    setTimeout(() => {
      startApplication()
    }, 1000)
  }

  const openInNewTab = () => {
    if (executionResult?.success && executionResult.output) {
      window.open(executionResult.output, '_blank')
    }
  }

  useEffect(() => {
    // Cleanup on unmount
    return () => {
      if (isRunning) {
        codeExecutionEngine.stopApplication(project.id)
      }
    }
  }, [project.id, isRunning])

  useEffect(() => {
    // Update logs periodically when app is running
    if (isRunning) {
      const interval = setInterval(() => {
        const appLogs = codeExecutionEngine.getApplicationLogs(project.id)
        setLogs(appLogs)
      }, 2000)

      return () => clearInterval(interval)
    }
  }, [isRunning, project.id])

  const getStatusColor = () => {
    if (!isRunning) return 'bg-gray-500'
    if (executionResult?.success) return 'bg-green-500'
    return 'bg-red-500'
  }

  const getStatusText = () => {
    if (!isRunning) return 'Stopped'
    if (executionResult?.success) return 'Running'
    return 'Error'
  }

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="flex items-center gap-2">
                <Monitor className="w-5 h-5" />
                Live Preview
                <div className={`w-2 h-2 rounded-full ${getStatusColor()}`} />
              </CardTitle>
              <CardDescription>
                Run and test your {project.type} application in real-time
              </CardDescription>
            </div>
            <div className="flex items-center gap-2">
              <Badge variant="outline">
                {getStatusText()}
              </Badge>
              {isRunning ? (
                <>
                  <Button size="sm" variant="outline" onClick={restartApplication}>
                    <RefreshCw className="w-4 h-4" />
                  </Button>
                  <Button size="sm" variant="outline" onClick={stopApplication}>
                    <Square className="w-4 h-4" />
                  </Button>
                  {executionResult?.output && (
                    <Button size="sm" variant="outline" onClick={openInNewTab}>
                      <ExternalLink className="w-4 h-4" />
                    </Button>
                  )}
                </>
              ) : (
                <Button size="sm" onClick={startApplication}>
                  <Play className="w-4 h-4 mr-2" />
                  Start App
                </Button>
              )}
            </div>
          </div>
        </CardHeader>
      </Card>

      <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-4">
        <TabsList className="grid w-full grid-cols-4">
          <TabsTrigger value="preview">Preview</TabsTrigger>
          <TabsTrigger value="logs">Logs</TabsTrigger>
          <TabsTrigger value="performance">Performance</TabsTrigger>
          <TabsTrigger value="config">Config</TabsTrigger>
        </TabsList>

        <TabsContent value="preview" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle className="text-lg">Application Preview</CardTitle>
              <CardDescription>
                Live preview of your running application
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="border rounded-lg overflow-hidden bg-background">
                {isRunning && executionResult?.success ? (
                  <iframe
                    ref={iframeRef}
                    className="w-full h-96 border-0"
                    title="Application Preview"
                    sandbox="allow-scripts allow-same-origin allow-forms"
                  />
                ) : isRunning ? (
                  <div className="h-96 flex items-center justify-center bg-muted">
                    <div className="text-center space-y-2">
                      <div className="w-8 h-8 border-2 border-primary border-t-transparent rounded-full animate-spin mx-auto" />
                      <p className="text-sm text-muted-foreground">Starting application...</p>
                    </div>
                  </div>
                ) : (
                  <div className="h-96 flex items-center justify-center bg-muted">
                    <div className="text-center space-y-4">
                      <Monitor className="w-12 h-12 mx-auto text-muted-foreground" />
                      <div>
                        <h3 className="font-medium">No Application Running</h3>
                        <p className="text-sm text-muted-foreground">
                          Click "Start App" to preview your application
                        </p>
                      </div>
                      <Button onClick={startApplication}>
                        <Play className="w-4 h-4 mr-2" />
                        Start Preview
                      </Button>
                    </div>
                  </div>
                )}
              </div>
              
              {executionResult && !executionResult.success && (
                <div className="mt-4 p-4 bg-destructive/10 border border-destructive/20 rounded-lg">
                  <h4 className="font-medium text-destructive mb-2">Execution Error</h4>
                  <p className="text-sm text-destructive/80">{executionResult.error}</p>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="logs" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Terminal className="w-5 h-5" />
                Application Logs
              </CardTitle>
              <CardDescription>
                Real-time logs from your running application
              </CardDescription>
            </CardHeader>
            <CardContent>
              <ScrollArea className="h-96 bg-background border rounded-lg p-4">
                {logs.length > 0 ? (
                  <div className="space-y-1 font-mono text-sm">
                    {logs.map((log, index) => (
                      <div key={index} className="flex gap-3">
                        <span className="text-muted-foreground text-xs w-20 flex-shrink-0">
                          {new Date(log.timestamp).toLocaleTimeString()}
                        </span>
                        <span className={`text-xs uppercase w-12 flex-shrink-0 ${
                          log.level === 'error' ? 'text-destructive' :
                          log.level === 'warn' ? 'text-yellow-600' :
                          'text-muted-foreground'
                        }`}>
                          {log.level}
                        </span>
                        <span className="text-foreground">{log.message}</span>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="flex items-center justify-center h-full text-muted-foreground">
                    <div className="text-center">
                      <Terminal className="w-8 h-8 mx-auto mb-2 opacity-50" />
                      <p>No logs available</p>
                      <p className="text-xs">Start the application to see logs</p>
                    </div>
                  </div>
                )}
              </ScrollArea>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="performance" className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <Card>
              <CardContent className="p-6">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm font-medium text-muted-foreground">Startup Time</p>
                    <p className="text-2xl font-bold">{performance.startupTime}ms</p>
                  </div>
                  <Clock className="w-8 h-8 text-muted-foreground" />
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardContent className="p-6">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm font-medium text-muted-foreground">Memory Usage</p>
                    <p className="text-2xl font-bold">{performance.memoryUsage}MB</p>
                  </div>
                  <Activity className="w-8 h-8 text-muted-foreground" />
                </div>
                <Progress value={performance.memoryUsage} className="mt-2" />
              </CardContent>
            </Card>

            <Card>
              <CardContent className="p-6">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm font-medium text-muted-foreground">Bundle Size</p>
                    <p className="text-2xl font-bold">
                      {performance.bundleSize ? `${Math.round(performance.bundleSize / 1024)}KB` : '0KB'}
                    </p>
                  </div>
                  <Monitor className="w-8 h-8 text-muted-foreground" />
                </div>
              </CardContent>
            </Card>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>Performance Metrics</CardTitle>
              <CardDescription>
                Detailed performance analysis of your application
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <div className="flex justify-between items-center">
                  <span className="text-sm font-medium">Load Time</span>
                  <span className="text-sm text-muted-foreground">
                    {performance.startupTime < 1000 ? 'Excellent' : 
                     performance.startupTime < 3000 ? 'Good' : 'Needs Improvement'}
                  </span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-sm font-medium">Memory Efficiency</span>
                  <span className="text-sm text-muted-foreground">
                    {performance.memoryUsage < 50 ? 'Excellent' : 
                     performance.memoryUsage < 100 ? 'Good' : 'High Usage'}
                  </span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-sm font-medium">Bundle Optimization</span>
                  <span className="text-sm text-muted-foreground">
                    {performance.bundleSize && performance.bundleSize < 500000 ? 'Optimized' : 'Could be smaller'}
                  </span>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="config" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Runtime Configuration</CardTitle>
              <CardDescription>
                Configure how your application runs in the preview environment
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-sm font-medium mb-2 block">Project Type</label>
                  <Badge variant="secondary">{project.type}</Badge>
                </div>
                <div>
                  <label className="text-sm font-medium mb-2 block">Status</label>
                  <Badge variant="outline">{project.status}</Badge>
                </div>
              </div>
              
              <div>
                <label className="text-sm font-medium mb-2 block">Files Count</label>
                <p className="text-sm text-muted-foreground">
                  {Object.keys(project.codebase.files || {}).length} files in project
                </p>
              </div>
              
              <div>
                <label className="text-sm font-medium mb-2 block">Dependencies</label>
                <p className="text-sm text-muted-foreground">
                  {project.codebase.dependencies?.length || 0} dependencies installed
                </p>
              </div>
              
              <div>
                <label className="text-sm font-medium mb-2 block">Last Modified</label>
                <p className="text-sm text-muted-foreground">
                  {new Date(project.lastModified).toLocaleString()}
                </p>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}