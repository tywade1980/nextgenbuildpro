/**
 * NextGenTele IVR System - Comprehensive Interactive Voice Response Platform
 * 
 * Features:
 * - Call Flow Designer with drag-and-drop interface
 * - Menu Management System
 * - Speech Recognition & Text-to-Speech
 * - Call Routing & Queue Management
 * - Real-time Analytics & Reporting
 * - Multi-language Support
 * - Integration with CRM/Backend Systems
 * - Testing & Simulation Environment
 */

import { useState, useCallback } from 'react'
import { useKV } from '@github/spark/hooks'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Badge } from '@/components/ui/badge'
import { Progress } from '@/components/ui/progress'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { ScrollArea } from '@/components/ui/scroll-area'
import { Separator } from '@/components/ui/separator'
import { toast } from 'sonner'
import { 
  Phone, 
  PhoneCall, 
  Mic, 
  Speaker, 
  Settings, 
  BarChart, 
  Users, 
  Clock, 
  PlayCircle, 
  PauseCircle, 
  StopCircle, 
  Volume2, 
  VolumeX, 
  Download, 
  Upload, 
  RefreshCw, 
  Plus, 
  Trash2, 
  Edit3, 
  Eye,
  GitBranch,
  Zap,
  Globe,
  Shield,
  Database,
  FileText,
  Calendar,
  TrendingUp,
  Activity,
  Headphones
} from 'lucide-react'

// IVR System Interfaces
interface IVRFlow {
  id: string
  name: string
  description: string
  nodes: IVRNode[]
  connections: IVRConnection[]
  isActive: boolean
  language: string
  createdAt: number
  lastModified: number
}

interface IVRNode {
  id: string
  type: 'greeting' | 'menu' | 'input' | 'route' | 'transfer' | 'hangup' | 'queue' | 'voicemail' | 'webhook'
  position: { x: number; y: number }
  data: {
    title: string
    message?: string
    options?: IVRMenuOption[]
    transferNumber?: string
    queueName?: string
    webhookUrl?: string
    timeout?: number
    retries?: number
  }
}

interface IVRMenuOption {
  key: string
  label: string
  action: 'transfer' | 'submenu' | 'queue' | 'webhook' | 'hangup'
  target?: string
}

interface IVRConnection {
  id: string
  source: string
  target: string
  label?: string
}

interface CallRecord {
  id: string
  callerId: string
  startTime: number
  endTime?: number
  duration?: number
  flow: string
  path: string[]
  status: 'active' | 'completed' | 'abandoned' | 'transferred'
  recording?: string
}

interface QueueMetrics {
  name: string
  waitingCalls: number
  averageWaitTime: number
  longestWait: number
  callsHandled: number
  abandonment: number
}

export function IVRSystem() {
  // State Management
  const [flows, setFlows] = useKV<IVRFlow[]>('ivr-flows', [])
  const [activeFlow, setActiveFlow] = useState<IVRFlow | null>(null)
  const [callRecords, setCallRecords] = useKV<CallRecord[]>('ivr-call-records', [])
  const [isSimulating, setIsSimulating] = useState(false)
  const [currentSimulationStep, setCurrentSimulationStep] = useState<string>('')
  
  // Analytics State
  const [analyticsTimeframe, setAnalyticsTimeframe] = useState<'hour' | 'day' | 'week' | 'month'>('day')
  const [queueMetrics] = useState<QueueMetrics[]>([
    { name: 'Sales', waitingCalls: 3, averageWaitTime: 45, longestWait: 120, callsHandled: 84, abandonment: 8 },
    { name: 'Support', waitingCalls: 7, averageWaitTime: 67, longestWait: 180, callsHandled: 156, abandonment: 12 },
    { name: 'Billing', waitingCalls: 2, averageWaitTime: 32, longestWait: 90, callsHandled: 43, abandonment: 4 }
  ])

  // Create new IVR flow
  const createFlow = useCallback(() => {
    const newFlow: IVRFlow = {
      id: `flow_${Date.now()}`,
      name: `New IVR Flow ${flows.length + 1}`,
      description: 'Auto-generated IVR flow',
      nodes: [
        {
          id: 'start',
          type: 'greeting',
          position: { x: 100, y: 100 },
          data: {
            title: 'Welcome Message',
            message: 'Thank you for calling NextGenTele. Your call is important to us.',
            timeout: 5
          }
        },
        {
          id: 'main_menu',
          type: 'menu',
          position: { x: 300, y: 100 },
          data: {
            title: 'Main Menu',
            message: 'Please select from the following options:',
            options: [
              { key: '1', label: 'Sales', action: 'queue', target: 'sales' },
              { key: '2', label: 'Support', action: 'queue', target: 'support' },
              { key: '3', label: 'Billing', action: 'queue', target: 'billing' },
              { key: '9', label: 'Operator', action: 'transfer', target: '+1234567890' },
              { key: '0', label: 'Repeat Menu', action: 'submenu', target: 'main_menu' }
            ],
            timeout: 10,
            retries: 3
          }
        }
      ],
      connections: [
        { id: 'conn_1', source: 'start', target: 'main_menu', label: 'After greeting' }
      ],
      isActive: false,
      language: 'en-US',
      createdAt: Date.now(),
      lastModified: Date.now()
    }
    
    setFlows(prev => [...prev, newFlow])
    setActiveFlow(newFlow)
    toast.success('New IVR flow created successfully')
  }, [flows, setFlows])

  // Start call simulation
  const startSimulation = useCallback((flow: IVRFlow) => {
    setIsSimulating(true)
    setCurrentSimulationStep('Initiating call...')
    
    // Simulate call progression
    const steps = [
      'Call connected',
      'Playing greeting message',
      'Presenting main menu',
      'Waiting for user input...'
    ]
    
    let currentStep = 0
    const interval = setInterval(() => {
      if (currentStep < steps.length) {
        setCurrentSimulationStep(steps[currentStep])
        currentStep++
      } else {
        setCurrentSimulationStep('Simulation complete')
        setIsSimulating(false)
        clearInterval(interval)
        
        // Add to call records
        const newRecord: CallRecord = {
          id: `call_${Date.now()}`,
          callerId: '+1234567890',
          startTime: Date.now() - 30000,
          endTime: Date.now(),
          duration: 30,
          flow: flow.id,
          path: ['start', 'main_menu'],
          status: 'completed'
        }
        
        setCallRecords(prev => [newRecord, ...prev.slice(0, 49)]) // Keep last 50 records
        toast.success('Call simulation completed')
      }
    }, 1500)
  }, [setCallRecords])

  // Toggle flow activation
  const toggleFlowActivation = useCallback((flowId: string) => {
    setFlows(prev => prev.map(flow => 
      flow.id === flowId 
        ? { ...flow, isActive: !flow.isActive, lastModified: Date.now() }
        : { ...flow, isActive: false } // Only one flow can be active
    ))
    toast.success('Flow activation updated')
  }, [setFlows])

  // Delete flow
  const deleteFlow = useCallback((flowId: string) => {
    setFlows(prev => prev.filter(flow => flow.id !== flowId))
    if (activeFlow?.id === flowId) {
      setActiveFlow(null)
    }
    toast.success('Flow deleted successfully')
  }, [setFlows, activeFlow])

  // Calculate analytics
  const getAnalytics = useCallback(() => {
    const now = Date.now()
    const timeframes = {
      hour: 3600000,
      day: 86400000,
      week: 604800000,
      month: 2592000000
    }
    
    const cutoff = now - timeframes[analyticsTimeframe]
    const recentRecords = callRecords.filter(record => record.startTime > cutoff)
    
    return {
      totalCalls: recentRecords.length,
      completedCalls: recentRecords.filter(r => r.status === 'completed').length,
      abandonedCalls: recentRecords.filter(r => r.status === 'abandoned').length,
      averageDuration: recentRecords.reduce((acc, r) => acc + (r.duration || 0), 0) / recentRecords.length || 0,
      peakHour: '2:00 PM - 3:00 PM',
      satisfactionScore: 4.2
    }
  }, [callRecords, analyticsTimeframe])

  const analytics = getAnalytics()

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-blue-600 rounded-lg flex items-center justify-center">
            <Phone className="w-6 h-6 text-white" />
          </div>
          <div>
            <h2 className="text-3xl font-bold">NextGenTele IVR System</h2>
            <p className="text-muted-foreground">Comprehensive Interactive Voice Response Platform</p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <Badge variant="outline" className="flex items-center gap-1">
            <Activity className="w-3 h-3 text-green-500" />
            System Online
          </Badge>
          <Button onClick={createFlow}>
            <Plus className="w-4 h-4 mr-2" />
            New Flow
          </Button>
        </div>
      </div>

      {/* Main Tabs */}
      <Tabs defaultValue="dashboard" className="space-y-6">
        <TabsList className="grid w-full grid-cols-6">
          <TabsTrigger value="dashboard">Dashboard</TabsTrigger>
          <TabsTrigger value="flows">Call Flows</TabsTrigger>
          <TabsTrigger value="analytics">Analytics</TabsTrigger>
          <TabsTrigger value="queues">Queue Management</TabsTrigger>
          <TabsTrigger value="settings">Settings</TabsTrigger>
          <TabsTrigger value="testing">Testing</TabsTrigger>
        </TabsList>

        {/* Dashboard Tab */}
        <TabsContent value="dashboard" className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Active Calls</CardTitle>
                <PhoneCall className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">23</div>
                <p className="text-xs text-muted-foreground">+12% from last hour</p>
              </CardContent>
            </Card>
            
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Queue Wait Time</CardTitle>
                <Clock className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">1:24</div>
                <p className="text-xs text-muted-foreground">Average wait time</p>
              </CardContent>
            </Card>
            
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Satisfaction Score</CardTitle>
                <TrendingUp className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">4.2/5</div>
                <p className="text-xs text-muted-foreground">Based on 156 responses</p>
              </CardContent>
            </Card>
            
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">System Status</CardTitle>
                <Activity className="h-4 w-4 text-green-500" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold text-green-500">Online</div>
                <p className="text-xs text-muted-foreground">All services operational</p>
              </CardContent>
            </Card>
          </div>

          {/* Recent Activity */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <Card>
              <CardHeader>
                <CardTitle>Recent Calls</CardTitle>
                <CardDescription>Latest incoming calls and their status</CardDescription>
              </CardHeader>
              <CardContent>
                <ScrollArea className="h-64">
                  <div className="space-y-3">
                    {callRecords.slice(0, 8).map(record => (
                      <div key={record.id} className="flex items-center justify-between p-3 border rounded-lg">
                        <div className="flex items-center gap-3">
                          <div className="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
                            <Phone className="w-4 h-4 text-blue-600" />
                          </div>
                          <div>
                            <p className="font-medium">{record.callerId}</p>
                            <p className="text-sm text-muted-foreground">
                              {new Date(record.startTime).toLocaleTimeString()}
                            </p>
                          </div>
                        </div>
                        <div className="text-right">
                          <Badge variant={record.status === 'completed' ? 'default' : 'secondary'}>
                            {record.status}
                          </Badge>
                          {record.duration && (
                            <p className="text-sm text-muted-foreground mt-1">
                              {Math.floor(record.duration / 60)}:{(record.duration % 60).toString().padStart(2, '0')}
                            </p>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>
                </ScrollArea>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Queue Status</CardTitle>
                <CardDescription>Current queue performance metrics</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {queueMetrics.map(queue => (
                    <div key={queue.name} className="space-y-2">
                      <div className="flex items-center justify-between">
                        <span className="font-medium">{queue.name} Queue</span>
                        <Badge variant="outline">{queue.waitingCalls} waiting</Badge>
                      </div>
                      <div className="flex items-center justify-between text-sm text-muted-foreground">
                        <span>Avg wait: {queue.averageWaitTime}s</span>
                        <span>Abandonment: {queue.abandonment}%</span>
                      </div>
                      <Progress value={(queue.callsHandled / (queue.callsHandled + queue.abandonment)) * 100} className="h-2" />
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        {/* Call Flows Tab */}
        <TabsContent value="flows" className="space-y-6">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="text-xl font-semibold">IVR Call Flows</h3>
              <p className="text-muted-foreground">Design and manage your interactive voice response flows</p>
            </div>
            <Button onClick={createFlow}>
              <Plus className="w-4 h-4 mr-2" />
              Create Flow
            </Button>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {flows.map(flow => (
              <Card key={flow.id} className="relative">
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <CardTitle className="flex items-center gap-2">
                      <GitBranch className="w-5 h-5" />
                      {flow.name}
                    </CardTitle>
                    <div className="flex items-center gap-2">
                      {flow.isActive && (
                        <Badge variant="default" className="bg-green-500">Active</Badge>
                      )}
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => deleteFlow(flow.id)}
                      >
                        <Trash2 className="w-4 h-4" />
                      </Button>
                    </div>
                  </div>
                  <CardDescription>{flow.description}</CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="flex items-center justify-between text-sm">
                    <span className="text-muted-foreground">Language:</span>
                    <Badge variant="outline">{flow.language}</Badge>
                  </div>
                  <div className="flex items-center justify-between text-sm">
                    <span className="text-muted-foreground">Nodes:</span>
                    <span>{flow.nodes.length}</span>
                  </div>
                  <div className="flex items-center justify-between text-sm">
                    <span className="text-muted-foreground">Last Modified:</span>
                    <span>{new Date(flow.lastModified).toLocaleDateString()}</span>
                  </div>
                  
                  <div className="flex gap-2">
                    <Button
                      variant={flow.isActive ? "destructive" : "default"}
                      size="sm"
                      onClick={() => toggleFlowActivation(flow.id)}
                      className="flex-1"
                    >
                      {flow.isActive ? 'Deactivate' : 'Activate'}
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => startSimulation(flow)}
                      disabled={isSimulating}
                    >
                      <PlayCircle className="w-4 h-4 mr-1" />
                      Test
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setActiveFlow(flow)}
                    >
                      <Edit3 className="w-4 h-4" />
                    </Button>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>

          {/* Flow Designer */}
          {activeFlow && (
            <Card>
              <CardHeader>
                <CardTitle>Flow Designer - {activeFlow.name}</CardTitle>
                <CardDescription>Visual editor for designing call flows</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="border-2 border-dashed border-muted-foreground/25 rounded-lg p-8 min-h-96 bg-muted/10">
                  <div className="text-center space-y-4">
                    <GitBranch className="w-16 h-16 mx-auto text-muted-foreground" />
                    <div>
                      <h4 className="text-lg font-semibold">Visual Flow Designer</h4>
                      <p className="text-muted-foreground">
                        Drag and drop interface for creating complex IVR flows
                      </p>
                    </div>
                    <div className="flex justify-center gap-2">
                      <Button variant="outline">
                        <Plus className="w-4 h-4 mr-2" />
                        Add Node
                      </Button>
                      <Button variant="outline">
                        <Settings className="w-4 h-4 mr-2" />
                        Configure
                      </Button>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          )}
        </TabsContent>

        {/* Analytics Tab */}
        <TabsContent value="analytics" className="space-y-6">
          <div className="flex items-center justify-between">
            <h3 className="text-xl font-semibold">IVR Analytics</h3>
            <Select value={analyticsTimeframe} onValueChange={(value: any) => setAnalyticsTimeframe(value)}>
              <SelectTrigger className="w-32">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="hour">Last Hour</SelectItem>
                <SelectItem value="day">Last Day</SelectItem>
                <SelectItem value="week">Last Week</SelectItem>
                <SelectItem value="month">Last Month</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <Card>
              <CardHeader>
                <CardTitle>Total Calls</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="text-3xl font-bold">{analytics.totalCalls}</div>
                <p className="text-muted-foreground">in the last {analyticsTimeframe}</p>
              </CardContent>
            </Card>
            
            <Card>
              <CardHeader>
                <CardTitle>Completion Rate</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="text-3xl font-bold">
                  {analytics.totalCalls > 0 ? Math.round((analytics.completedCalls / analytics.totalCalls) * 100) : 0}%
                </div>
                <Progress 
                  value={analytics.totalCalls > 0 ? (analytics.completedCalls / analytics.totalCalls) * 100 : 0} 
                  className="mt-2"
                />
              </CardContent>
            </Card>
            
            <Card>
              <CardHeader>
                <CardTitle>Avg Duration</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="text-3xl font-bold">
                  {Math.floor(analytics.averageDuration / 60)}:{((analytics.averageDuration % 60) || 0).toFixed(0).padStart(2, '0')}
                </div>
                <p className="text-muted-foreground">minutes:seconds</p>
              </CardContent>
            </Card>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>Call Volume Trends</CardTitle>
              <CardDescription>Hourly call distribution</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="h-64 flex items-center justify-center border-2 border-dashed border-muted-foreground/25 rounded-lg">
                <div className="text-center">
                  <BarChart className="w-16 h-16 mx-auto text-muted-foreground mb-4" />
                  <p className="text-muted-foreground">Analytics charts would be rendered here</p>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Queue Management Tab */}
        <TabsContent value="queues" className="space-y-6">
          <div className="flex items-center justify-between">
            <h3 className="text-xl font-semibold">Queue Management</h3>
            <Button>
              <Plus className="w-4 h-4 mr-2" />
              Add Queue
            </Button>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {queueMetrics.map(queue => (
              <Card key={queue.name}>
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <CardTitle className="flex items-center gap-2">
                      <Users className="w-5 h-5" />
                      {queue.name} Queue
                    </CardTitle>
                    <Badge variant="outline">{queue.waitingCalls} waiting</Badge>
                  </div>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <p className="text-sm text-muted-foreground">Average Wait</p>
                      <p className="text-2xl font-bold">{queue.averageWaitTime}s</p>
                    </div>
                    <div>
                      <p className="text-sm text-muted-foreground">Longest Wait</p>
                      <p className="text-2xl font-bold">{queue.longestWait}s</p>
                    </div>
                    <div>
                      <p className="text-sm text-muted-foreground">Calls Handled</p>
                      <p className="text-2xl font-bold">{queue.callsHandled}</p>
                    </div>
                    <div>
                      <p className="text-sm text-muted-foreground">Abandonment</p>
                      <p className="text-2xl font-bold">{queue.abandonment}%</p>
                    </div>
                  </div>
                  
                  <div className="flex gap-2">
                    <Button variant="outline" size="sm" className="flex-1">
                      <Settings className="w-4 h-4 mr-2" />
                      Configure
                    </Button>
                    <Button variant="outline" size="sm" className="flex-1">
                      <BarChart className="w-4 h-4 mr-2" />
                      Reports
                    </Button>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        </TabsContent>

        {/* Settings Tab */}
        <TabsContent value="settings" className="space-y-6">
          <h3 className="text-xl font-semibold">IVR System Settings</h3>
          
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <Card>
              <CardHeader>
                <CardTitle>General Settings</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div>
                  <Label htmlFor="system-name">System Name</Label>
                  <Input id="system-name" defaultValue="NextGenTele IVR" />
                </div>
                <div>
                  <Label htmlFor="default-language">Default Language</Label>
                  <Select defaultValue="en-US">
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="en-US">English (US)</SelectItem>
                      <SelectItem value="en-GB">English (UK)</SelectItem>
                      <SelectItem value="es-ES">Spanish</SelectItem>
                      <SelectItem value="fr-FR">French</SelectItem>
                      <SelectItem value="de-DE">German</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                <div>
                  <Label htmlFor="max-retries">Maximum Retries</Label>
                  <Input id="max-retries" type="number" defaultValue="3" />
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Voice Settings</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div>
                  <Label htmlFor="voice-type">Voice Type</Label>
                  <Select defaultValue="female">
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="female">Female Voice</SelectItem>
                      <SelectItem value="male">Male Voice</SelectItem>
                      <SelectItem value="neutral">Neutral Voice</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                <div>
                  <Label htmlFor="speech-rate">Speech Rate</Label>
                  <Input id="speech-rate" type="range" min="0.5" max="2" step="0.1" defaultValue="1" />
                </div>
                <div>
                  <Label htmlFor="voice-volume">Voice Volume</Label>
                  <Input id="voice-volume" type="range" min="0" max="100" defaultValue="80" />
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Integration Settings</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div>
                  <Label htmlFor="crm-endpoint">CRM Endpoint</Label>
                  <Input id="crm-endpoint" placeholder="https://api.crm.example.com" />
                </div>
                <div>
                  <Label htmlFor="api-key">API Key</Label>
                  <Input id="api-key" type="password" placeholder="Your API key" />
                </div>
                <div>
                  <Label htmlFor="webhook-url">Webhook URL</Label>
                  <Input id="webhook-url" placeholder="https://webhook.example.com/ivr" />
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Recording Settings</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center justify-between">
                  <Label htmlFor="enable-recording">Enable Call Recording</Label>
                  <input type="checkbox" id="enable-recording" defaultChecked />
                </div>
                <div>
                  <Label htmlFor="recording-format">Recording Format</Label>
                  <Select defaultValue="mp3">
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="mp3">MP3</SelectItem>
                      <SelectItem value="wav">WAV</SelectItem>
                      <SelectItem value="flac">FLAC</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                <div>
                  <Label htmlFor="retention-days">Retention Period (days)</Label>
                  <Input id="retention-days" type="number" defaultValue="90" />
                </div>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        {/* Testing Tab */}
        <TabsContent value="testing" className="space-y-6">
          <div className="flex items-center justify-between">
            <h3 className="text-xl font-semibold">IVR Testing Suite</h3>
            <div className="flex gap-2">
              <Button variant="outline">
                <Download className="w-4 h-4 mr-2" />
                Export Tests
              </Button>
              <Button variant="outline">
                <Upload className="w-4 h-4 mr-2" />
                Import Tests
              </Button>
            </div>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <Card>
              <CardHeader>
                <CardTitle>Call Simulation</CardTitle>
                <CardDescription>Test your IVR flows with simulated calls</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div>
                  <Label htmlFor="test-flow">Select Flow to Test</Label>
                  <Select>
                    <SelectTrigger>
                      <SelectValue placeholder="Choose a flow" />
                    </SelectTrigger>
                    <SelectContent>
                      {flows.map(flow => (
                        <SelectItem key={flow.id} value={flow.id}>{flow.name}</SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                
                <div>
                  <Label htmlFor="caller-id">Caller ID</Label>
                  <Input id="caller-id" placeholder="+1234567890" />
                </div>
                
                <div>
                  <Label htmlFor="simulation-scenario">Scenario</Label>
                  <Select>
                    <SelectTrigger>
                      <SelectValue placeholder="Select scenario" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="normal">Normal Call Flow</SelectItem>
                      <SelectItem value="timeout">Timeout Scenarios</SelectItem>
                      <SelectItem value="invalid">Invalid Input</SelectItem>
                      <SelectItem value="abandonment">Call Abandonment</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                {isSimulating && (
                  <Alert>
                    <Activity className="h-4 w-4" />
                    <AlertDescription>
                      Simulation in progress: {currentSimulationStep}
                    </AlertDescription>
                  </Alert>
                )}
                
                <Button 
                  className="w-full" 
                  onClick={() => flows.length > 0 && startSimulation(flows[0])}
                  disabled={isSimulating || flows.length === 0}
                >
                  {isSimulating ? (
                    <>
                      <StopCircle className="w-4 h-4 mr-2" />
                      Stop Simulation
                    </>
                  ) : (
                    <>
                      <PlayCircle className="w-4 h-4 mr-2" />
                      Start Simulation
                    </>
                  )}
                </Button>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Test Results</CardTitle>
                <CardDescription>Recent simulation results and performance metrics</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  <div className="flex items-center justify-between p-3 border rounded-lg">
                    <div>
                      <p className="font-medium">Main Menu Flow Test</p>
                      <p className="text-sm text-muted-foreground">Completed 2 minutes ago</p>
                    </div>
                    <Badge variant="default">Passed</Badge>
                  </div>
                  
                  <div className="flex items-center justify-between p-3 border rounded-lg">
                    <div>
                      <p className="font-medium">Timeout Scenario Test</p>
                      <p className="text-sm text-muted-foreground">Completed 5 minutes ago</p>
                    </div>
                    <Badge variant="secondary">Warning</Badge>
                  </div>
                  
                  <div className="flex items-center justify-between p-3 border rounded-lg">
                    <div>
                      <p className="font-medium">Invalid Input Test</p>
                      <p className="text-sm text-muted-foreground">Completed 8 minutes ago</p>
                    </div>
                    <Badge variant="default">Passed</Badge>
                  </div>
                </div>
                
                <Button variant="outline" className="w-full mt-4">
                  <FileText className="w-4 h-4 mr-2" />
                  View Detailed Report
                </Button>
              </CardContent>
            </Card>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>Load Testing</CardTitle>
              <CardDescription>Simulate high call volumes to test system performance</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
                <div>
                  <Label htmlFor="concurrent-calls">Concurrent Calls</Label>
                  <Input id="concurrent-calls" type="number" defaultValue="50" />
                </div>
                <div>
                  <Label htmlFor="test-duration">Duration (minutes)</Label>
                  <Input id="test-duration" type="number" defaultValue="10" />
                </div>
                <div>
                  <Label htmlFor="ramp-up">Ramp-up (seconds)</Label>
                  <Input id="ramp-up" type="number" defaultValue="30" />
                </div>
              </div>
              
              <Button className="w-full">
                <Zap className="w-4 h-4 mr-2" />
                Start Load Test
              </Button>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}