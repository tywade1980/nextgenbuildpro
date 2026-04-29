import { useState } from 'react'
import { useKV } from '@github/spark/hooks'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Badge } from '@/components/ui/badge'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Switch } from '@/components/ui/switch'
import { Progress } from '@/components/ui/progress'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { toast } from 'sonner'
import { 
  Network, 
  Shield, 
  Lock, 
  Globe, 
  Server, 
  Gauge, 
  Clock, 
  AlertTriangle, 
  CheckCircle, 
  Plus, 
  Trash2, 
  Settings,
  Eye,
  FileText,
  Play,
  Copy
} from '@phosphor-icons/react'

interface NetworkPolicy {
  id: string
  name: string
  description: string
  type: 'access' | 'security' | 'traffic' | 'circuit-breaker' | 'rate-limit' | 'retry'
  enabled: boolean
  source: {
    services: string[]
    namespaces: string[]
    labels: Record<string, string>
  }
  destination: {
    services: string[]
    namespaces: string[]
    labels: Record<string, string>
  }
  rules: PolicyRule[]
  metadata: {
    created: number
    lastModified: number
    appliedServices: number
    violations: number
  }
}

interface PolicyRule {
  id: string
  action: 'allow' | 'deny' | 'rate-limit' | 'circuit-break' | 'retry'
  conditions: {
    method?: string[]
    path?: string
    headers?: Record<string, string>
    protocol?: 'http' | 'grpc' | 'tcp'
    port?: number[]
  }
  parameters?: {
    rateLimit?: {
      requests: number
      window: string
      burst?: number
    }
    circuitBreaker?: {
      failureThreshold: number
      timeout: string
      halfOpenRequests: number
    }
    retry?: {
      attempts: number
      backoff: string
      timeout: string
    }
    security?: {
      mtls: boolean
      encryption: boolean
      authentication: string[]
    }
  }
}

interface TrafficMetrics {
  serviceId: string
  serviceName: string
  requests: {
    total: number
    success: number
    errors: number
    latency: {
      p50: number
      p95: number
      p99: number
    }
  }
  connections: {
    active: number
    failed: number
  }
  policies: {
    applied: number
    violations: number
  }
}

interface ServiceMeshPoliciesProps {
  projectId: string
  services: Array<{
    id: string
    name: string
    namespace: string
    labels: Record<string, string>
  }>
  onPolicyUpdate?: (policies: NetworkPolicy[]) => void
}

export function ServiceMeshPolicies({ projectId, services, onPolicyUpdate }: ServiceMeshPoliciesProps) {
  const [policies, setPolicies] = useKV<NetworkPolicy[]>(`service-mesh-policies-${projectId}`, [])
  const [selectedPolicy, setSelectedPolicy] = useState<NetworkPolicy | null>(null)
  const [activeTab, setActiveTab] = useState('policies')
  const [showCreateDialog, setShowCreateDialog] = useState(false)
  const [isDeploying, setIsDeploying] = useState(false)

  // Mock traffic metrics
  const [trafficMetrics] = useState<TrafficMetrics[]>([
    {
      serviceId: 'auth-service',
      serviceName: 'Authentication Service',
      requests: { total: 15420, success: 15398, errors: 22, latency: { p50: 45, p95: 120, p99: 250 } },
      connections: { active: 156, failed: 3 },
      policies: { applied: 5, violations: 1 }
    },
    {
      serviceId: 'user-service',
      serviceName: 'User Management',
      requests: { total: 8930, success: 8925, errors: 5, latency: { p50: 32, p95: 89, p99: 180 } },
      connections: { active: 89, failed: 0 },
      policies: { applied: 3, violations: 0 }
    },
    {
      serviceId: 'payment-service',
      serviceName: 'Payment Gateway',
      requests: { total: 2340, success: 2338, errors: 2, latency: { p50: 78, p95: 200, p99: 450 } },
      connections: { active: 34, failed: 1 },
      policies: { applied: 7, violations: 0 }
    }
  ])

  const createPolicy = (type: NetworkPolicy['type']) => {
    const newPolicy: NetworkPolicy = {
      id: Date.now().toString(),
      name: `${type}-policy-${policies.length + 1}`,
      description: `Auto-generated ${type} policy`,
      type,
      enabled: true,
      source: { services: [], namespaces: [], labels: {} },
      destination: { services: [], namespaces: [], labels: {} },
      rules: [],
      metadata: {
        created: Date.now(),
        lastModified: Date.now(),
        appliedServices: 0,
        violations: 0
      }
    }

    setPolicies(current => [...current, newPolicy])
    setSelectedPolicy(newPolicy)
    setShowCreateDialog(false)
    toast.success(`${type} policy created successfully`)
  }

  const updatePolicy = (policyId: string, updates: Partial<NetworkPolicy>) => {
    setPolicies(current =>
      current.map(policy =>
        policy.id === policyId
          ? { ...policy, ...updates, metadata: { ...policy.metadata, lastModified: Date.now() } }
          : policy
      )
    )
    
    if (selectedPolicy?.id === policyId) {
      setSelectedPolicy(prev => prev ? { ...prev, ...updates } : null)
    }
  }

  const addPolicyRule = (policyId: string) => {
    const newRule: PolicyRule = {
      id: Date.now().toString(),
      action: 'allow',
      conditions: { protocol: 'http' },
      parameters: {}
    }

    updatePolicy(policyId, {
      rules: [...(selectedPolicy?.rules || []), newRule]
    })
  }

  const updatePolicyRule = (policyId: string, ruleId: string, updates: Partial<PolicyRule>) => {
    const policy = policies.find(p => p.id === policyId)
    if (!policy) return

    const updatedRules = policy.rules.map(rule =>
      rule.id === ruleId ? { ...rule, ...updates } : rule
    )

    updatePolicy(policyId, { rules: updatedRules })
  }

  const deletePolicyRule = (policyId: string, ruleId: string) => {
    const policy = policies.find(p => p.id === policyId)
    if (!policy) return

    const updatedRules = policy.rules.filter(rule => rule.id !== ruleId)
    updatePolicy(policyId, { rules: updatedRules })
  }

  const deployPolicies = async () => {
    setIsDeploying(true)
    try {
      // Simulate deployment
      await new Promise(resolve => setTimeout(resolve, 2000))
      
      // Update metrics
      setPolicies(current =>
        current.map(policy => ({
          ...policy,
          metadata: {
            ...policy.metadata,
            appliedServices: Math.floor(Math.random() * services.length) + 1
          }
        }))
      )
      
      toast.success('Service mesh policies deployed successfully')
      onPolicyUpdate?.(policies)
    } catch (error) {
      toast.error('Failed to deploy policies')
    } finally {
      setIsDeploying(false)
    }
  }

  const generateIstioConfig = (policy: NetworkPolicy) => {
    const config = {
      apiVersion: 'networking.istio.io/v1beta1',
      kind: 'VirtualService',
      metadata: {
        name: policy.name,
        namespace: 'default'
      },
      spec: {
        hosts: policy.destination.services,
        http: policy.rules.map(rule => ({
          match: [
            {
              method: { exact: rule.conditions.method?.[0] },
              uri: { prefix: rule.conditions.path }
            }
          ],
          route: [{ destination: { host: policy.destination.services[0] } }],
          fault: rule.action === 'circuit-break' ? {
            abort: {
              percentage: { value: policy.rules[0].parameters?.circuitBreaker?.failureThreshold || 50 },
              httpStatus: 503
            }
          } : undefined,
          timeout: rule.parameters?.retry?.timeout || '30s'
        }))
      }
    }
    
    return JSON.stringify(config, null, 2)
  }

  const getSuccessRate = (metrics: TrafficMetrics) => {
    return metrics.requests.total > 0 
      ? Math.round((metrics.requests.success / metrics.requests.total) * 100)
      : 0
  }

  const getPolicyIcon = (type: NetworkPolicy['type']) => {
    switch (type) {
      case 'access': return <Shield className="w-4 h-4" />
      case 'security': return <Lock className="w-4 h-4" />
      case 'traffic': return <Network className="w-4 h-4" />
      case 'circuit-breaker': return <AlertTriangle className="w-4 h-4" />
      case 'rate-limit': return <Gauge className="w-4 h-4" />
      case 'retry': return <Clock className="w-4 h-4" />
      default: return <Settings className="w-4 h-4" />
    }
  }

  const getPolicyColor = (type: NetworkPolicy['type']) => {
    switch (type) {
      case 'access': return 'bg-blue-500'
      case 'security': return 'bg-red-500'
      case 'traffic': return 'bg-green-500'
      case 'circuit-breaker': return 'bg-orange-500'
      case 'rate-limit': return 'bg-purple-500'
      case 'retry': return 'bg-yellow-500'
      default: return 'bg-gray-500'
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-semibold flex items-center gap-2">
            <Network className="w-6 h-6" />
            Service Mesh Policies
          </h2>
          <p className="text-muted-foreground">
            Manage network policies, security rules, and traffic management for your microservices
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Button
            onClick={deployPolicies}
            disabled={isDeploying || policies.length === 0}
            className="flex items-center gap-2"
          >
            <Play className="w-4 h-4" />
            {isDeploying ? 'Deploying...' : 'Deploy Policies'}
          </Button>
          <Dialog open={showCreateDialog} onOpenChange={setShowCreateDialog}>
            <DialogTrigger asChild>
              <Button className="flex items-center gap-2">
                <Plus className="w-4 h-4" />
                Create Policy
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Create Network Policy</DialogTitle>
                <DialogDescription>
                  Choose the type of policy you want to create
                </DialogDescription>
              </DialogHeader>
              <div className="grid grid-cols-2 gap-4">
                {[
                  { type: 'access' as const, title: 'Access Control', desc: 'Control service-to-service access' },
                  { type: 'security' as const, title: 'Security', desc: 'mTLS and encryption policies' },
                  { type: 'traffic' as const, title: 'Traffic Management', desc: 'Load balancing and routing' },
                  { type: 'circuit-breaker' as const, title: 'Circuit Breaker', desc: 'Fault tolerance patterns' },
                  { type: 'rate-limit' as const, title: 'Rate Limiting', desc: 'Request throttling policies' },
                  { type: 'retry' as const, title: 'Retry Policy', desc: 'Automatic retry configuration' }
                ].map(({ type, title, desc }) => (
                  <Card key={type} className="cursor-pointer hover:shadow-md transition-shadow" onClick={() => createPolicy(type)}>
                    <CardHeader className="pb-2">
                      <div className="flex items-center gap-2">
                        {getPolicyIcon(type)}
                        <CardTitle className="text-sm">{title}</CardTitle>
                      </div>
                    </CardHeader>
                    <CardContent>
                      <p className="text-xs text-muted-foreground">{desc}</p>
                    </CardContent>
                  </Card>
                ))}
              </div>
            </DialogContent>
          </Dialog>
        </div>
      </div>

      {isDeploying && (
        <Alert>
          <Play className="w-4 h-4" />
          <AlertDescription>
            Deploying service mesh policies to cluster...
            <Progress value={66} className="mt-2" />
          </AlertDescription>
        </Alert>
      )}

      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList>
          <TabsTrigger value="policies">Policies</TabsTrigger>
          <TabsTrigger value="traffic">Traffic Metrics</TabsTrigger>
          <TabsTrigger value="security">Security Status</TabsTrigger>
          <TabsTrigger value="config">Configuration</TabsTrigger>
        </TabsList>

        <TabsContent value="policies" className="space-y-4">
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div className="lg:col-span-1 space-y-4">
              <Card>
                <CardHeader>
                  <CardTitle className="text-lg">Active Policies</CardTitle>
                  <CardDescription>
                    {policies.filter(p => p.enabled).length} of {policies.length} policies enabled
                  </CardDescription>
                </CardHeader>
                <CardContent className="space-y-3">
                  {policies.map(policy => (
                    <div
                      key={policy.id}
                      className={`p-3 rounded-lg border cursor-pointer transition-colors ${
                        selectedPolicy?.id === policy.id ? 'bg-accent' : 'hover:bg-muted'
                      }`}
                      onClick={() => setSelectedPolicy(policy)}
                    >
                      <div className="flex items-center justify-between mb-2">
                        <div className="flex items-center gap-2">
                          <div className={`w-2 h-2 rounded-full ${policy.enabled ? 'bg-green-500' : 'bg-gray-400'}`} />
                          {getPolicyIcon(policy.type)}
                          <span className="font-medium text-sm">{policy.name}</span>
                        </div>
                        <Badge variant="outline" className="text-xs">
                          {policy.type}
                        </Badge>
                      </div>
                      <p className="text-xs text-muted-foreground mb-2">{policy.description}</p>
                      <div className="flex items-center justify-between text-xs">
                        <span className="text-muted-foreground">
                          {policy.rules.length} rules
                        </span>
                        <span className="text-muted-foreground">
                          {policy.metadata.appliedServices} services
                        </span>
                      </div>
                    </div>
                  ))}
                  
                  {policies.length === 0 && (
                    <div className="text-center py-8">
                      <Network className="w-8 h-8 text-muted-foreground mx-auto mb-2" />
                      <p className="text-sm text-muted-foreground">No policies created yet</p>
                    </div>
                  )}
                </CardContent>
              </Card>
            </div>

            <div className="lg:col-span-2">
              {selectedPolicy ? (
                <Card>
                  <CardHeader>
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        {getPolicyIcon(selectedPolicy.type)}
                        <CardTitle>{selectedPolicy.name}</CardTitle>
                        <Badge variant="outline">{selectedPolicy.type}</Badge>
                      </div>
                      <div className="flex items-center gap-2">
                        <Switch
                          checked={selectedPolicy.enabled}
                          onCheckedChange={(enabled) => updatePolicy(selectedPolicy.id, { enabled })}
                        />
                        <Button variant="outline" size="sm">
                          <Settings className="w-4 h-4" />
                        </Button>
                      </div>
                    </div>
                    <CardDescription>{selectedPolicy.description}</CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-6">
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <Label className="text-sm font-medium">Source Services</Label>
                        <div className="mt-1 space-y-1">
                          {selectedPolicy.source.services.map((service, idx) => (
                            <Badge key={idx} variant="secondary" className="mr-1">
                              {service}
                            </Badge>
                          ))}
                          {selectedPolicy.source.services.length === 0 && (
                            <p className="text-xs text-muted-foreground">All services</p>
                          )}
                        </div>
                      </div>
                      <div>
                        <Label className="text-sm font-medium">Destination Services</Label>
                        <div className="mt-1 space-y-1">
                          {selectedPolicy.destination.services.map((service, idx) => (
                            <Badge key={idx} variant="secondary" className="mr-1">
                              {service}
                            </Badge>
                          ))}
                          {selectedPolicy.destination.services.length === 0 && (
                            <p className="text-xs text-muted-foreground">All services</p>
                          )}
                        </div>
                      </div>
                    </div>

                    <div>
                      <div className="flex items-center justify-between mb-3">
                        <Label className="text-sm font-medium">Policy Rules</Label>
                        <Button size="sm" onClick={() => addPolicyRule(selectedPolicy.id)}>
                          <Plus className="w-4 h-4 mr-1" />
                          Add Rule
                        </Button>
                      </div>
                      <div className="space-y-3">
                        {selectedPolicy.rules.map(rule => (
                          <Card key={rule.id} className="p-4">
                            <div className="flex items-start justify-between">
                              <div className="space-y-2 flex-1">
                                <div className="flex items-center gap-2">
                                  <Badge variant={rule.action === 'allow' ? 'default' : 'destructive'}>
                                    {rule.action}
                                  </Badge>
                                  <span className="text-sm font-medium">
                                    {rule.conditions.method?.[0] || 'ANY'} {rule.conditions.path || '/*'}
                                  </span>
                                </div>
                                <div className="text-xs text-muted-foreground">
                                  Protocol: {rule.conditions.protocol?.toUpperCase() || 'ANY'}
                                  {rule.conditions.port && ` • Port: ${rule.conditions.port.join(', ')}`}
                                </div>
                                {rule.parameters?.rateLimit && (
                                  <div className="text-xs text-blue-600">
                                    Rate Limit: {rule.parameters.rateLimit.requests}/{rule.parameters.rateLimit.window}
                                  </div>
                                )}
                              </div>
                              <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => deletePolicyRule(selectedPolicy.id, rule.id)}
                              >
                                <Trash2 className="w-4 h-4" />
                              </Button>
                            </div>
                          </Card>
                        ))}
                        
                        {selectedPolicy.rules.length === 0 && (
                          <div className="text-center py-4 text-sm text-muted-foreground">
                            No rules configured. Add a rule to get started.
                          </div>
                        )}
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ) : (
                <Card>
                  <CardContent className="flex items-center justify-center py-12">
                    <div className="text-center">
                      <Eye className="w-8 h-8 text-muted-foreground mx-auto mb-2" />
                      <p className="text-muted-foreground">Select a policy to view details</p>
                    </div>
                  </CardContent>
                </Card>
              )}
            </div>
          </div>
        </TabsContent>

        <TabsContent value="traffic" className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {trafficMetrics.map(metrics => (
              <Card key={metrics.serviceId}>
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <CardTitle className="text-lg flex items-center gap-2">
                      <Server className="w-5 h-5" />
                      {metrics.serviceName}
                    </CardTitle>
                    <Badge variant={getSuccessRate(metrics) > 95 ? 'default' : 'destructive'}>
                      {getSuccessRate(metrics)}%
                    </Badge>
                  </div>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="grid grid-cols-2 gap-4 text-sm">
                    <div>
                      <div className="text-muted-foreground">Total Requests</div>
                      <div className="text-2xl font-bold">{metrics.requests.total.toLocaleString()}</div>
                    </div>
                    <div>
                      <div className="text-muted-foreground">Error Rate</div>
                      <div className="text-2xl font-bold text-red-500">
                        {((metrics.requests.errors / metrics.requests.total) * 100).toFixed(2)}%
                      </div>
                    </div>
                    <div>
                      <div className="text-muted-foreground">P95 Latency</div>
                      <div className="text-lg font-semibold">{metrics.requests.latency.p95}ms</div>
                    </div>
                    <div>
                      <div className="text-muted-foreground">Active Connections</div>
                      <div className="text-lg font-semibold">{metrics.connections.active}</div>
                    </div>
                  </div>
                  
                  <div className="space-y-2">
                    <div className="flex justify-between text-sm">
                      <span>Applied Policies</span>
                      <span className="font-medium">{metrics.policies.applied}</span>
                    </div>
                    <div className="flex justify-between text-sm">
                      <span>Policy Violations</span>
                      <span className={`font-medium ${metrics.policies.violations > 0 ? 'text-red-500' : 'text-green-500'}`}>
                        {metrics.policies.violations}
                      </span>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        </TabsContent>

        <TabsContent value="security" className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Lock className="w-5 h-5" />
                  Security Overview
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center justify-between">
                  <span>mTLS Encryption</span>
                  <div className="flex items-center gap-2">
                    <CheckCircle className="w-4 h-4 text-green-500" />
                    <span className="text-sm text-green-600">Enabled</span>
                  </div>
                </div>
                <div className="flex items-center justify-between">
                  <span>Certificate Rotation</span>
                  <div className="flex items-center gap-2">
                    <CheckCircle className="w-4 h-4 text-green-500" />
                    <span className="text-sm text-green-600">Automatic</span>
                  </div>
                </div>
                <div className="flex items-center justify-between">
                  <span>Zero Trust Network</span>
                  <div className="flex items-center gap-2">
                    <CheckCircle className="w-4 h-4 text-green-500" />
                    <span className="text-sm text-green-600">Active</span>
                  </div>
                </div>
                <div className="flex items-center justify-between">
                  <span>Policy Violations</span>
                  <div className="flex items-center gap-2">
                    <AlertTriangle className="w-4 h-4 text-yellow-500" />
                    <span className="text-sm text-yellow-600">2 Active</span>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Shield className="w-5 h-5" />
                  Access Control
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="space-y-2">
                  <div className="flex justify-between text-sm">
                    <span>Allowed Connections</span>
                    <span className="font-medium text-green-600">1,247</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span>Denied Connections</span>
                    <span className="font-medium text-red-600">23</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span>Rate Limited</span>
                    <span className="font-medium text-yellow-600">156</span>
                  </div>
                </div>
                
                <Progress value={98} className="h-2" />
                <p className="text-xs text-muted-foreground">98% of requests allowed</p>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        <TabsContent value="config" className="space-y-4">
          {selectedPolicy && (
            <Card>
              <CardHeader>
                <div className="flex items-center justify-between">
                  <CardTitle className="flex items-center gap-2">
                    <FileText className="w-5 h-5" />
                    Istio Configuration
                  </CardTitle>
                  <Button variant="outline" size="sm">
                    <Copy className="w-4 h-4 mr-2" />
                    Copy Config
                  </Button>
                </div>
                <CardDescription>
                  Generated Istio VirtualService configuration for {selectedPolicy.name}
                </CardDescription>
              </CardHeader>
              <CardContent>
                <Textarea
                  value={generateIstioConfig(selectedPolicy)}
                  readOnly
                  className="min-h-96 font-mono text-sm"
                />
              </CardContent>
            </Card>
          )}
          
          {!selectedPolicy && (
            <Card>
              <CardContent className="flex items-center justify-center py-12">
                <div className="text-center">
                  <FileText className="w-8 h-8 text-muted-foreground mx-auto mb-2" />
                  <p className="text-muted-foreground">Select a policy to view its configuration</p>
                </div>
              </CardContent>
            </Card>
          )}
        </TabsContent>
      </Tabs>
    </div>
  )
}