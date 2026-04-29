import { useState, useEffect, useRef } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Slider } from '@/components/ui/slider'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Switch } from '@/components/ui/switch'
import { useRealTimePreview } from '@/hooks/useRealTimePreview'
import { 
  DevicePhoneMobile, 
  Play, 
  Pause, 
  ArrowClockwise, 
  GearSix,
  Eye,
  Code,
  Warning,
  Lightning,
  Circle
} from '@phosphor-icons/react'

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

interface AndroidEmulatorProps {
  project: Project
  onUpdateProject: (projectId: string, updates: Partial<Project>) => void
}

interface EmulatorState {
  isRunning: boolean
  orientation: 'portrait' | 'landscape'
  deviceModel: 'pixel6' | 'pixel7' | 'galaxy_s22'
  apiLevel: number
  scale: number
}

interface ComponentState {
  name: string
  type: 'Activity' | 'Fragment' | 'View' | 'Layout'
  properties: Record<string, any>
  children?: ComponentState[]
}

export function AndroidEmulator({ project, onUpdateProject }: AndroidEmulatorProps) {
  const [emulatorState, setEmulatorState] = useState<EmulatorState>({
    isRunning: false,
    orientation: 'portrait',
    deviceModel: 'pixel6',
    apiLevel: 33,
    scale: 0.8
  })
  
  const [selectedComponent, setSelectedComponent] = useState<ComponentState | null>(null)
  const [logs, setLogs] = useState<string[]>([])
  const [autoRefresh, setAutoRefresh] = useState(true)
  const canvasRef = useRef<HTMLCanvasElement>(null)

  // Real-time preview system
  const {
    isActive: previewActive,
    previewData,
    pendingChanges,
    lastUpdate,
    startPreview,
    stopPreview,
    clearChanges
  } = useRealTimePreview(project.codebase.files, (newPreviewData) => {
    addLog(`Preview updated: ${newPreviewData.layouts.length} layouts, ${newPreviewData.activities.length} activities`)
    if (autoRefresh && emulatorState.isRunning) {
      renderPreview()
    }
  })

  // Auto-start preview when emulator starts
  useEffect(() => {
    if (emulatorState.isRunning && !previewActive) {
      startPreview()
    } else if (!emulatorState.isRunning && previewActive) {
      stopPreview()
    }
  }, [emulatorState.isRunning, previewActive, startPreview, stopPreview])

  // Re-render when preview data changes
  useEffect(() => {
    if (previewData && emulatorState.isRunning) {
      renderPreview()
    }
  }, [previewData, emulatorState.isRunning])

  const parseAndroidFiles = () => {
    // This is now handled by the useRealTimePreview hook
    // Keep for backwards compatibility
    if (previewData) {
      addLog(`Loaded ${previewData.layouts.length} layouts and ${previewData.activities.length} activities`)
    }
  }

  const parseLayoutXML = (content: string, path: string): ComponentState[] => {
    const components: ComponentState[] = []
    
    // Simple XML parsing for demo - in production, use proper XML parser
    const viewMatches = content.match(/<(\w+)[\s\S]*?(?:\/?>|<\/\1>)/g) || []
    
    viewMatches.forEach((match, index) => {
      const tagMatch = match.match(/<(\w+)/)
      if (tagMatch) {
        const viewType = tagMatch[1]
        const properties: Record<string, any> = {}
        
        // Extract properties
        const propMatches = match.match(/(\w+:?\w+)="([^"]+)"/g) || []
        propMatches.forEach(prop => {
          const [key, value] = prop.split('=')
          properties[key] = value.replace(/"/g, '')
        })
        
        components.push({
          name: `${viewType}_${index}`,
          type: 'View',
          properties: {
            ...properties,
            layoutFile: path.split('/').pop(),
            viewType
          }
        })
      }
    })
    
    return components
  }

  const parseKotlinActivity = (content: string, path: string): ComponentState[] => {
    const components: ComponentState[] = []
    
    // Extract class name
    const classMatch = content.match(/class\s+(\w+)/)
    if (classMatch) {
      const className = classMatch[1]
      const isActivity = content.includes('Activity')
      const isFragment = content.includes('Fragment')
      
      components.push({
        name: className,
        type: isActivity ? 'Activity' : isFragment ? 'Fragment' : 'View',
        properties: {
          fileName: path.split('/').pop(),
          packageName: content.match(/package\s+([\w.]+)/)?.[1] || 'com.example'
        }
      })
    }
    
    return components
  }

  const addLog = (message: string) => {
    setLogs(prev => [...prev, `${new Date().toLocaleTimeString()}: ${message}`].slice(-50))
  }

  const toggleEmulator = () => {
    const newState = !emulatorState.isRunning
    setEmulatorState(prev => ({ ...prev, isRunning: newState }))
    addLog(newState ? 'Emulator started - Real-time preview active' : 'Emulator stopped')
    
    if (newState) {
      renderPreview()
    }
  }

  const renderPreview = () => {
    const canvas = canvasRef.current
    if (!canvas) return

    const ctx = canvas.getContext('2d')
    if (!ctx) return

    // Clear canvas
    ctx.fillStyle = '#f0f0f0'
    ctx.fillRect(0, 0, canvas.width, canvas.height)

    // Draw device frame
    ctx.strokeStyle = '#333'
    ctx.lineWidth = 4
    ctx.roundRect(10, 10, canvas.width - 20, canvas.height - 20, 20)
    ctx.stroke()

    // Draw status bar
    ctx.fillStyle = '#1976d2'
    ctx.fillRect(20, 20, canvas.width - 40, 40)
    ctx.fillStyle = 'white'
    ctx.font = '12px Arial'
    ctx.fillText('9:41 AM', 30, 38)
    ctx.fillText('100%', canvas.width - 60, 38)

    // Render components from preview data
    if (previewData && previewData.layouts.length > 0) {
      let yOffset = 80
      previewData.layouts.forEach((layout: any) => {
        if (layout.views) {
          layout.views.forEach((view: any) => {
            renderComponent(ctx, {
              name: view.id,
              type: 'View',
              properties: { ...view.properties, viewType: view.type }
            }, 30, yOffset, canvas.width - 60)
            yOffset += getComponentHeight({
              name: view.id,
              type: 'View',
              properties: { ...view.properties, viewType: view.type }
            }) + 10
          })
        }
      })
    } else {
      // Show empty state
      ctx.fillStyle = '#666'
      ctx.font = '16px Arial'
      ctx.textAlign = 'center'
      ctx.fillText('No UI components found', canvas.width / 2, canvas.height / 2)
      ctx.fillText('Add layout files to see preview', canvas.width / 2, canvas.height / 2 + 20)
      ctx.textAlign = 'left'
    }
  }

  const renderComponent = (
    ctx: CanvasRenderingContext2D, 
    component: ComponentState, 
    x: number, 
    y: number, 
    width: number
  ) => {
    const height = getComponentHeight(component)
    
    // Component background
    ctx.fillStyle = component === selectedComponent ? '#e3f2fd' : '#ffffff'
    ctx.fillRect(x, y, width, height)
    
    // Component border
    ctx.strokeStyle = component === selectedComponent ? '#2196f3' : '#e0e0e0'
    ctx.lineWidth = 1
    ctx.strokeRect(x, y, width, height)
    
    // Component content based on type
    ctx.fillStyle = '#333'
    ctx.font = '14px Arial'
    
    if (component.properties?.viewType === 'TextView') {
      const text = component.properties['android:text'] || component.name
      ctx.fillText(text, x + 10, y + height / 2)
    } else if (component.properties?.viewType === 'Button') {
      ctx.fillStyle = '#1976d2'
      ctx.fillRect(x + 5, y + 5, width - 10, height - 10)
      ctx.fillStyle = 'white'
      ctx.fillText(component.properties['android:text'] || 'Button', x + 10, y + height / 2)
    } else if (component.properties?.viewType === 'ImageView') {
      ctx.strokeStyle = '#666'
      ctx.strokeRect(x + 10, y + 10, width - 20, height - 20)
      ctx.fillText('📷', x + width / 2 - 10, y + height / 2)
    } else {
      ctx.fillText(`${component.type}: ${component.name}`, x + 10, y + height / 2)
    }
  }

  const getComponentHeight = (component: ComponentState): number => {
    switch (component.properties?.viewType) {
      case 'Button': return 48
      case 'TextView': return 32
      case 'ImageView': return 80
      default: return 40
    }
  }

  const handleComponentClick = (event: React.MouseEvent<HTMLCanvasElement>) => {
    const canvas = canvasRef.current
    if (!canvas || !emulatorState.isRunning || !previewData) return

    const rect = canvas.getBoundingClientRect()
    const x = event.clientX - rect.left
    const y = event.clientY - rect.top

    // Find clicked component from preview data
    let yOffset = 80
    const allViews = previewData.layouts.flatMap((layout: any) => 
      layout.views?.map((view: any) => ({
        ...view,
        layoutFile: layout.fileName
      })) || []
    )

    for (const view of allViews) {
      const component = {
        name: view.id,
        type: 'View' as const,
        properties: { ...view.properties, viewType: view.type, layoutFile: view.layoutFile }
      }
      const height = getComponentHeight(component)
      
      if (y >= yOffset && y <= yOffset + height) {
        setSelectedComponent(component)
        addLog(`Selected: ${view.type} from ${view.layoutFile}`)
        break
      }
      yOffset += height + 10
    }

    renderPreview()
  }

  const getDeviceSpecs = (model: string) => {
    switch (model) {
      case 'pixel6': return { width: 360, height: 780, name: 'Pixel 6' }
      case 'pixel7': return { width: 360, height: 800, name: 'Pixel 7' }
      case 'galaxy_s22': return { width: 384, height: 854, name: 'Galaxy S22' }
      default: return { width: 360, height: 780, name: 'Generic' }
    }
  }

  const deviceSpecs = getDeviceSpecs(emulatorState.deviceModel)

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-semibold flex items-center gap-2">
            <DevicePhoneMobile className="w-6 h-6" />
            Android Emulator
            {previewActive && (
              <Badge variant="default" className="flex items-center gap-1">
                <Circle className="w-2 h-2 fill-current animate-pulse" />
                Live
              </Badge>
            )}
          </h2>
          <p className="text-muted-foreground">Real-time preview of your Android app</p>
        </div>
        
        <div className="flex items-center gap-2">
          <div className="flex items-center gap-2">
            <Switch
              checked={autoRefresh}
              onCheckedChange={setAutoRefresh}
              id="auto-refresh"
            />
            <label htmlFor="auto-refresh" className="text-sm">Auto-refresh</label>
          </div>
          
          {pendingChanges.length > 0 && (
            <Badge variant="secondary" className="flex items-center gap-1">
              <Lightning className="w-3 h-3" />
              {pendingChanges.length} changes
            </Badge>
          )}
          
          <Badge variant={emulatorState.isRunning ? 'default' : 'secondary'}>
            {emulatorState.isRunning ? 'Running' : 'Stopped'}
          </Badge>
          
          <Button onClick={toggleEmulator} variant={emulatorState.isRunning ? 'destructive' : 'default'}>
            {emulatorState.isRunning ? <Pause className="w-4 h-4 mr-2" /> : <Play className="w-4 h-4 mr-2" />}
            {emulatorState.isRunning ? 'Stop' : 'Start'}
          </Button>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Emulator Display */}
        <Card className="lg:col-span-2">
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle className="flex items-center gap-2">
                <Eye className="w-5 h-5" />
                {deviceSpecs.name} - API {emulatorState.apiLevel}
              </CardTitle>
              <div className="flex items-center gap-2">
                <Button variant="outline" size="sm">
                  <ArrowClockwise className="w-4 h-4" />
                </Button>
                <Button variant="outline" size="sm">
                  <GearSix className="w-4 h-4" />
                </Button>
              </div>
            </div>
            <div className="flex items-center gap-4">
              <span className="text-sm text-muted-foreground">Scale:</span>
              <Slider
                value={[emulatorState.scale * 100]}
                onValueChange={([value]) => 
                  setEmulatorState(prev => ({ ...prev, scale: value / 100 }))
                }
                min={50}
                max={100}
                step={10}
                className="w-32"
              />
              <span className="text-sm">{Math.round(emulatorState.scale * 100)}%</span>
            </div>
          </CardHeader>
          <CardContent className="flex justify-center">
            <div 
              className="relative bg-gray-800 rounded-3xl p-4 shadow-2xl"
              style={{ 
                transform: `scale(${emulatorState.scale})`,
                transformOrigin: 'top center'
              }}
            >
              <canvas
                ref={canvasRef}
                width={deviceSpecs.width}
                height={deviceSpecs.height}
                className="bg-white rounded-2xl cursor-pointer"
                onClick={handleComponentClick}
              />
              
              {!emulatorState.isRunning && (
                <div className="absolute inset-0 flex items-center justify-center bg-black bg-opacity-50 rounded-2xl">
                  <div className="text-center text-white">
                    <DevicePhoneMobile className="w-12 h-12 mx-auto mb-2 opacity-50" />
                    <p className="text-sm">Emulator Stopped</p>
                  </div>
                </div>
              )}
            </div>
          </CardContent>
        </Card>

        {/* Controls and Info */}
        <div className="space-y-4">
          {/* Component Inspector */}
          <Card>
            <CardHeader>
              <CardTitle className="text-lg flex items-center gap-2">
                <Code className="w-5 h-5" />
                Component Inspector
              </CardTitle>
            </CardHeader>
            <CardContent>
              {selectedComponent ? (
                <div className="space-y-3">
                  <div>
                    <label className="text-sm font-medium">Name</label>
                    <p className="text-sm text-muted-foreground">{selectedComponent.name}</p>
                  </div>
                  <div>
                    <label className="text-sm font-medium">Type</label>
                    <Badge variant="outline">{selectedComponent.type}</Badge>
                  </div>
                  <div>
                    <label className="text-sm font-medium">Properties</label>
                    <div className="space-y-1 max-h-32 overflow-y-auto">
                      {Object.entries(selectedComponent.properties).map(([key, value]) => (
                        <div key={key} className="text-xs">
                          <span className="font-mono text-blue-600">{key}:</span>{' '}
                          <span className="text-gray-700">{String(value)}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              ) : (
                <p className="text-sm text-muted-foreground">Click on a component to inspect</p>
              )}
            </CardContent>
          </Card>

          {/* Components List */}
          <Card>
            <CardHeader>
              <CardTitle className="text-lg">UI Components</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-2 max-h-40 overflow-y-auto">
                {previewData && previewData.layouts.length > 0 ? (
                  previewData.layouts.flatMap((layout: any) => 
                    layout.views?.map((view: any, index: number) => {
                      const component = {
                        name: view.id,
                        type: 'View' as const,
                        properties: { ...view.properties, viewType: view.type, layoutFile: layout.fileName }
                      }
                      return (
                        <div
                          key={`${layout.fileName}_${index}`}
                          className={`p-2 rounded cursor-pointer transition-colors ${
                            selectedComponent?.name === component.name 
                              ? 'bg-primary/10 border border-primary' 
                              : 'bg-muted hover:bg-muted/80'
                          }`}
                          onClick={() => setSelectedComponent(component)}
                        >
                          <div className="flex items-center gap-2">
                            <Badge variant="secondary" className="text-xs">
                              {view.type}
                            </Badge>
                            <span className="text-sm font-medium">{view.id}</span>
                          </div>
                          <p className="text-xs text-muted-foreground mt-1">
                            from {layout.fileName}
                          </p>
                        </div>
                      )
                    }) || []
                  )
                ) : (
                  <div className="text-center py-4">
                    <Warning className="w-8 h-8 mx-auto mb-2 text-muted-foreground" />
                    <p className="text-sm text-muted-foreground">No UI components found</p>
                    <p className="text-xs text-muted-foreground mt-1">
                      Add layout XML files to see components
                    </p>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>

          {/* Logs */}
          <Card>
            <CardHeader>
              <CardTitle className="text-lg">Emulator Logs</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-1 max-h-32 overflow-y-auto font-mono text-xs">
                {logs.map((log, index) => (
                  <div key={index} className="text-muted-foreground">
                    {log}
                  </div>
                ))}
                {logs.length === 0 && (
                  <p className="text-muted-foreground">No logs yet</p>
                )}
              </div>
            </CardContent>
          </Card>
        </div>
      </div>

      {/* Device Settings */}
      <Card>
        <CardHeader>
          <CardTitle>Device Settings</CardTitle>
        </CardHeader>
        <CardContent>
          <Tabs defaultValue="device" className="w-full">
            <TabsList>
              <TabsTrigger value="device">Device</TabsTrigger>
              <TabsTrigger value="system">System</TabsTrigger>
              <TabsTrigger value="network">Network</TabsTrigger>
            </TabsList>
            
            <TabsContent value="device" className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                  <label className="text-sm font-medium">Device Model</label>
                  <select 
                    value={emulatorState.deviceModel}
                    onChange={(e) => setEmulatorState(prev => ({ 
                      ...prev, 
                      deviceModel: e.target.value as EmulatorState['deviceModel']
                    }))}
                    className="w-full mt-1 p-2 border rounded-md"
                  >
                    <option value="pixel6">Pixel 6</option>
                    <option value="pixel7">Pixel 7</option>
                    <option value="galaxy_s22">Galaxy S22</option>
                  </select>
                </div>
                
                <div>
                  <label className="text-sm font-medium">API Level</label>
                  <select 
                    value={emulatorState.apiLevel}
                    onChange={(e) => setEmulatorState(prev => ({ 
                      ...prev, 
                      apiLevel: parseInt(e.target.value)
                    }))}
                    className="w-full mt-1 p-2 border rounded-md"
                  >
                    <option value={33}>API 33 (Android 13)</option>
                    <option value={32}>API 32 (Android 12L)</option>
                    <option value={31}>API 31 (Android 12)</option>
                  </select>
                </div>
                
                <div>
                  <label className="text-sm font-medium">Orientation</label>
                  <select 
                    value={emulatorState.orientation}
                    onChange={(e) => setEmulatorState(prev => ({ 
                      ...prev, 
                      orientation: e.target.value as EmulatorState['orientation']
                    }))}
                    className="w-full mt-1 p-2 border rounded-md"
                  >
                    <option value="portrait">Portrait</option>
                    <option value="landscape">Landscape</option>
                  </select>
                </div>
              </div>
            </TabsContent>
            
            <TabsContent value="system" className="space-y-4">
              <p className="text-sm text-muted-foreground">System settings and performance options</p>
            </TabsContent>
            
            <TabsContent value="network" className="space-y-4">
              <p className="text-sm text-muted-foreground">Network simulation and proxy settings</p>
            </TabsContent>
          </Tabs>
        </CardContent>
      </Card>
    </div>
  )
}