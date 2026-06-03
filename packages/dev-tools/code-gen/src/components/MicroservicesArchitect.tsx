import { useState } from 'react'
import { useKV } from '@github/spark/hooks'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Badge } from '@/components/ui/badge'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { toast } from 'sonner'
import { 
  Container, 
  Network, 
  Globe, 
  Database, 
  Shield, 
  Monitor, 
  GitBranch, 
  Plus, 
  Trash2, 
  Play, 
  Stop,
  Settings,
  Box,
  Link as LinkIcon,
  CloudArrowUp,
  Code
} from '@phosphor-icons/react'
import { MicroserviceGenerator } from '@/utils/MicroserviceGenerator'
import { DockerGenerator } from '@/utils/DockerGenerator'
import { ServiceDiscoveryManager } from '@/utils/ServiceDiscoveryManager'

interface MicroserviceConfig {
  id: string
  name: string
  description: string
  type: 'api' | 'database' | 'cache' | 'queue' | 'gateway' | 'auth' | 'monitoring'
  framework: 'express' | 'fastapi' | 'spring-boot' | 'go-gin' | 'nestjs' | 'django'
  port: number
  endpoints: Array<{
    path: string
    method: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH'
    description: string
  }>
  dependencies: string[]
  environment: Record<string, string>
  healthCheck: {
    path: string
    interval: number
  }
  resources: {
    cpu: string
    memory: string
  }
  scaling: {
    min: number
    max: number
    targetCpu: number
  }
}

interface ServiceMesh {
  id: string
  name: string
  services: MicroserviceConfig[]
  networks: Array<{
    name: string
    driver: string
    subnet?: string
  }>
  volumes: Array<{
    name: string
    driver: string
    mountPath: string
  }>
  gateway: {
    enabled: boolean
    port: number
    routes: Array<{
      path: string
      service: string
      port: number
    }>
  }
  monitoring: {
    enabled: boolean
    prometheus: boolean
    grafana: boolean
    jaeger: boolean
  }
  serviceDiscovery: {
    type: 'consul' | 'eureka' | 'etcd' | 'zookeeper'
    enabled: boolean
  }
}

interface MicroservicesArchitectProps {
  project: any
  onUpdateProject: (projectId: string, updates: any) => void
}

export function MicroservicesArchitect({ project, onUpdateProject }: MicroservicesArchitectProps) {
  const [serviceMeshes, setServiceMeshes] = useKV<ServiceMesh[]>('microservices-meshes', [])
  const [selectedMesh, setSelectedMesh] = useState<ServiceMesh | null>(null)
  const [isCreatingService, setIsCreatingService] = useState(false)
  const [isCreatingMesh, setIsCreatingMesh] = useState(false)
  const [activeTab, setActiveTab] = useState('overview')
  const [generatingFiles, setGeneratingFiles] = useState(false)

  const [newService, setNewService] = useState<Partial<MicroserviceConfig>>({
    type: 'api',
    framework: 'express',
    port: 3000,
    endpoints: [],
    dependencies: [],
    environment: {},
    healthCheck: { path: '/health', interval: 30 },
    resources: { cpu: '100m', memory: '128Mi' },
    scaling: { min: 1, max: 5, targetCpu: 70 }
  })

  const [newMesh, setNewMesh] = useState<Partial<ServiceMesh>>({
    services: [],
    networks: [{ name: 'app-network', driver: 'bridge' }],
    volumes: [],
    gateway: { enabled: true, port: 80, routes: [] },
    monitoring: { enabled: true, prometheus: true, grafana: true, jaeger: false },
    serviceDiscovery: { type: 'consul', enabled: true }
  })

  const createServiceMesh = async () => {
    if (!newMesh.name) {
      toast.error('Service mesh name is required')
      return
    }

    const mesh: ServiceMesh = {
      id: Date.now().toString(),
      name: newMesh.name,
      services: [],
      networks: newMesh.networks || [],
      volumes: newMesh.volumes || [],
      gateway: newMesh.gateway || { enabled: true, port: 80, routes: [] },
      monitoring: newMesh.monitoring || { enabled: true, prometheus: true, grafana: true, jaeger: false },
      serviceDiscovery: newMesh.serviceDiscovery || { type: 'consul', enabled: true }
    }

    setServiceMeshes(current => [...current, mesh])
    setSelectedMesh(mesh)
    setIsCreatingMesh(false)
    setNewMesh({
      services: [],
      networks: [{ name: 'app-network', driver: 'bridge' }],
      volumes: [],
      gateway: { enabled: true, port: 80, routes: [] },
      monitoring: { enabled: true, prometheus: true, grafana: true, jaeger: false },
      serviceDiscovery: { type: 'consul', enabled: true }
    })
    toast.success('Service mesh created successfully')
  }

  const addMicroservice = async () => {
    if (!selectedMesh || !newService.name) {
      toast.error('Service mesh and service name are required')
      return
    }

    const service: MicroserviceConfig = {
      id: Date.now().toString(),
      name: newService.name,
      description: newService.description || '',
      type: newService.type || 'api',
      framework: newService.framework || 'express',
      port: newService.port || 3000,
      endpoints: newService.endpoints || [],
      dependencies: newService.dependencies || [],
      environment: newService.environment || {},
      healthCheck: newService.healthCheck || { path: '/health', interval: 30 },
      resources: newService.resources || { cpu: '100m', memory: '128Mi' },
      scaling: newService.scaling || { min: 1, max: 5, targetCpu: 70 }
    }

    const updatedMesh = {
      ...selectedMesh,
      services: [...selectedMesh.services, service]
    }

    setServiceMeshes(current =>
      current.map(mesh => mesh.id === selectedMesh.id ? updatedMesh : mesh)
    )
    setSelectedMesh(updatedMesh)
    setIsCreatingService(false)
    setNewService({
      type: 'api',
      framework: 'express',
      port: 3000,
      endpoints: [],
      dependencies: [],
      environment: {},
      healthCheck: { path: '/health', interval: 30 },
      resources: { cpu: '100m', memory: '128Mi' },
      scaling: { min: 1, max: 5, targetCpu: 70 }
    })
    toast.success('Microservice added successfully')
  }

  const generateMicroservicesCode = async () => {
    if (!selectedMesh || selectedMesh.services.length === 0) {
      toast.error('No services to generate')
      return
    }

    setGeneratingFiles(true)
    try {
      const generator = new MicroserviceGenerator()
      const dockerGen = new DockerGenerator()
      const discoveryManager = new ServiceDiscoveryManager()

      // Generate microservice code for each service
      const generatedFiles: Record<string, string> = {}

      for (const service of selectedMesh.services) {
        const serviceFiles = await generator.generateService(service)
        Object.entries(serviceFiles).forEach(([path, content]) => {
          generatedFiles[`services/${service.name}/${path}`] = content
        })

        // Generate Dockerfile for each service
        const dockerfile = dockerGen.generateDockerfile(service)
        generatedFiles[`services/${service.name}/Dockerfile`] = dockerfile

        // Generate docker-compose service definition
        const composeService = dockerGen.generateComposeService(service)
        generatedFiles[`services/${service.name}/docker-compose.yml`] = composeService
      }

      // Generate main docker-compose.yml
      const mainCompose = dockerGen.generateMainCompose(selectedMesh)
      generatedFiles['docker-compose.yml'] = mainCompose

      // Generate service discovery configuration
      if (selectedMesh.serviceDiscovery.enabled) {
        const discoveryConfig = discoveryManager.generateConfiguration(selectedMesh)
        generatedFiles[`config/${selectedMesh.serviceDiscovery.type}.yml`] = discoveryConfig
      }

      // Generate API Gateway configuration
      if (selectedMesh.gateway.enabled) {
        const gatewayConfig = dockerGen.generateGatewayConfig(selectedMesh)
        generatedFiles['gateway/nginx.conf'] = gatewayConfig
        generatedFiles['gateway/Dockerfile'] = dockerGen.generateGatewayDockerfile()
      }

      // Generate monitoring configuration
      if (selectedMesh.monitoring.enabled) {
        const monitoringConfigs = dockerGen.generateMonitoringConfigs(selectedMesh)
        Object.entries(monitoringConfigs).forEach(([path, content]) => {
          generatedFiles[`monitoring/${path}`] = content
        })
      }

      // Generate deployment scripts
      generatedFiles['scripts/deploy.sh'] = dockerGen.generateDeployScript(selectedMesh)
      generatedFiles['scripts/start.sh'] = dockerGen.generateStartScript(selectedMesh)
      generatedFiles['scripts/stop.sh'] = dockerGen.generateStopScript(selectedMesh)

      // Generate Kubernetes manifests
      const k8sManifests = dockerGen.generateKubernetesManifests(selectedMesh)
      Object.entries(k8sManifests).forEach(([path, content]) => {
        generatedFiles[`k8s/${path}`] = content
      })

      // Update project with generated files
      onUpdateProject(project.id, {
        codebase: {
          ...project.codebase,
          files: {
            ...project.codebase.files,
            ...generatedFiles
          }
        }
      })

      toast.success(`Generated microservices architecture with ${selectedMesh.services.length} services`)
    } catch (error) {
      console.error('Error generating microservices:', error)
      toast.error('Failed to generate microservices code')
    } finally {
      setGeneratingFiles(false)
    }
  }

  const removeService = (serviceId: string) => {
    if (!selectedMesh) return

    const updatedMesh = {
      ...selectedMesh,
      services: selectedMesh.services.filter(s => s.id !== serviceId)
    }

    setServiceMeshes(current =>
      current.map(mesh => mesh.id === selectedMesh.id ? updatedMesh : mesh)
    )
    setSelectedMesh(updatedMesh)
    toast.success('Service removed')
  }

  const getServiceTypeIcon = (type: MicroserviceConfig['type']) => {
    switch (type) {
      case 'api': return <Globe className="w-4 h-4" />
      case 'database': return <Database className="w-4 h-4" />
      case 'cache': return <Box className="w-4 h-4" />
      case 'queue': return <LinkIcon className="w-4 h-4" />
      case 'gateway': return <Network className="w-4 h-4" />
      case 'auth': return <Shield className="w-4 h-4" />
      case 'monitoring': return <Monitor className="w-4 h-4" />
      default: return <Container className="w-4 h-4" />
    }
  }

  const getServiceTypeColor = (type: MicroserviceConfig['type']) => {
    switch (type) {
      case 'api': return 'bg-blue-500'
      case 'database': return 'bg-green-500'
      case 'cache': return 'bg-orange-500'
      case 'queue': return 'bg-purple-500'
      case 'gateway': return 'bg-cyan-500'
      case 'auth': return 'bg-red-500'
      case 'monitoring': return 'bg-yellow-500'
      default: return 'bg-gray-500'
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-semibold flex items-center gap-2">
            <Container className="w-6 h-6" />
            Microservices Architecture
          </h2>
          <p className="text-muted-foreground">Design and deploy containerized microservices with service discovery</p>
        </div>
        <div className="flex items-center gap-2">
          <Dialog open={isCreatingMesh} onOpenChange={setIsCreatingMesh}>
            <DialogTrigger asChild>
              <Button>
                <Plus className="w-4 h-4 mr-2" />
                New Service Mesh
              </Button>
            </DialogTrigger>
            <DialogContent className="max-w-2xl">
              <DialogHeader>
                <DialogTitle>Create Service Mesh</DialogTitle>
                <DialogDescription>Configure a new microservices mesh</DialogDescription>
              </DialogHeader>
              <div className="space-y-4">
                <div>
                  <Label htmlFor="mesh-name">Mesh Name</Label>
                  <Input
                    id="mesh-name"
                    value={newMesh.name || ''}
                    onChange={(e) => setNewMesh(prev => ({ ...prev, name: e.target.value }))}
                    placeholder="my-service-mesh"
                  />
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <Label>Service Discovery</Label>
                    <Select
                      value={newMesh.serviceDiscovery?.type}
                      onValueChange={(value) => setNewMesh(prev => ({
                        ...prev,
                        serviceDiscovery: { ...prev.serviceDiscovery!, type: value as any }
                      }))}
                    >
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="consul">Consul</SelectItem>
                        <SelectItem value="eureka">Eureka</SelectItem>
                        <SelectItem value="etcd">etcd</SelectItem>
                        <SelectItem value="zookeeper">Zookeeper</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                  <div>
                    <Label>Gateway Port</Label>
                    <Input
                      type="number"
                      value={newMesh.gateway?.port}
                      onChange={(e) => setNewMesh(prev => ({
                        ...prev,
                        gateway: { ...prev.gateway!, port: parseInt(e.target.value) }
                      }))}
                    />
                  </div>
                </div>
                <div className="flex justify-end gap-2">
                  <Button variant="outline" onClick={() => setIsCreatingMesh(false)}>
                    Cancel
                  </Button>
                  <Button onClick={createServiceMesh}>Create Mesh</Button>
                </div>
              </div>
            </DialogContent>
          </Dialog>
        </div>
      </div>

      {serviceMeshes.length === 0 ? (
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-12">
            <Container className="w-12 h-12 text-muted-foreground mb-4" />
            <h3 className="text-lg font-semibold mb-2">No Service Meshes</h3>
            <p className="text-muted-foreground text-center mb-4">
              Create your first service mesh to start building microservices
            </p>
            <Button onClick={() => setIsCreatingMesh(true)}>
              <Plus className="w-4 h-4 mr-2" />
              Create Service Mesh
            </Button>
          </CardContent>
        </Card>
      ) : (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {serviceMeshes.map(mesh => (
              <Card 
                key={mesh.id} 
                className={`cursor-pointer transition-all ${selectedMesh?.id === mesh.id ? 'ring-2 ring-primary' : ''}`}
                onClick={() => setSelectedMesh(mesh)}
              >
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <Network className="w-5 h-5" />
                    {mesh.name}
                  </CardTitle>
                  <CardDescription>
                    {mesh.services.length} services configured
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center gap-2 text-sm">
                    <Badge variant="outline">
                      {mesh.serviceDiscovery.type}
                    </Badge>
                    {mesh.monitoring.enabled && (
                      <Badge variant="outline">Monitoring</Badge>
                    )}
                    {mesh.gateway.enabled && (
                      <Badge variant="outline">Gateway</Badge>
                    )}
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>

          {selectedMesh && (
            <Card>
              <CardHeader>
                <div className="flex items-center justify-between">
                  <div>
                    <CardTitle className="flex items-center gap-2">
                      <Network className="w-5 h-5" />
                      {selectedMesh.name}
                    </CardTitle>
                    <CardDescription>
                      Manage microservices and configuration
                    </CardDescription>
                  </div>
                  <div className="flex items-center gap-2">
                    <Button 
                      onClick={generateMicroservicesCode}
                      disabled={generatingFiles || selectedMesh.services.length === 0}
                    >
                      {generatingFiles ? (
                        <>
                          <Settings className="w-4 h-4 mr-2 animate-spin" />
                          Generating...
                        </>
                      ) : (
                        <>
                          <Code className="w-4 h-4 mr-2" />
                          Generate Code
                        </>
                      )}
                    </Button>
                    <Dialog open={isCreatingService} onOpenChange={setIsCreatingService}>
                      <DialogTrigger asChild>
                        <Button>
                          <Plus className="w-4 h-4 mr-2" />
                          Add Service
                        </Button>
                      </DialogTrigger>
                      <DialogContent className="max-w-4xl max-h-[80vh] overflow-y-auto">
                        <DialogHeader>
                          <DialogTitle>Add Microservice</DialogTitle>
                          <DialogDescription>Configure a new microservice</DialogDescription>
                        </DialogHeader>
                        <div className="space-y-4">
                          <div className="grid grid-cols-2 gap-4">
                            <div>
                              <Label htmlFor="service-name">Service Name</Label>
                              <Input
                                id="service-name"
                                value={newService.name || ''}
                                onChange={(e) => setNewService(prev => ({ ...prev, name: e.target.value }))}
                                placeholder="user-service"
                              />
                            </div>
                            <div>
                              <Label htmlFor="service-port">Port</Label>
                              <Input
                                id="service-port"
                                type="number"
                                value={newService.port}
                                onChange={(e) => setNewService(prev => ({ ...prev, port: parseInt(e.target.value) }))}
                              />
                            </div>
                          </div>
                          <div>
                            <Label htmlFor="service-description">Description</Label>
                            <Textarea
                              id="service-description"
                              value={newService.description || ''}
                              onChange={(e) => setNewService(prev => ({ ...prev, description: e.target.value }))}
                              placeholder="User management service"
                            />
                          </div>
                          <div className="grid grid-cols-3 gap-4">
                            <div>
                              <Label>Service Type</Label>
                              <Select
                                value={newService.type}
                                onValueChange={(value) => setNewService(prev => ({ ...prev, type: value as any }))}
                              >
                                <SelectTrigger>
                                  <SelectValue />
                                </SelectTrigger>
                                <SelectContent>
                                  <SelectItem value="api">API Service</SelectItem>
                                  <SelectItem value="database">Database</SelectItem>
                                  <SelectItem value="cache">Cache</SelectItem>
                                  <SelectItem value="queue">Message Queue</SelectItem>
                                  <SelectItem value="gateway">Gateway</SelectItem>
                                  <SelectItem value="auth">Authentication</SelectItem>
                                  <SelectItem value="monitoring">Monitoring</SelectItem>
                                </SelectContent>
                              </Select>
                            </div>
                            <div>
                              <Label>Framework</Label>
                              <Select
                                value={newService.framework}
                                onValueChange={(value) => setNewService(prev => ({ ...prev, framework: value as any }))}
                              >
                                <SelectTrigger>
                                  <SelectValue />
                                </SelectTrigger>
                                <SelectContent>
                                  <SelectItem value="express">Express.js</SelectItem>
                                  <SelectItem value="fastapi">FastAPI</SelectItem>
                                  <SelectItem value="spring-boot">Spring Boot</SelectItem>
                                  <SelectItem value="go-gin">Go Gin</SelectItem>
                                  <SelectItem value="nestjs">NestJS</SelectItem>
                                  <SelectItem value="django">Django</SelectItem>
                                </SelectContent>
                              </Select>
                            </div>
                            <div>
                              <Label>Health Check Path</Label>
                              <Input
                                value={newService.healthCheck?.path}
                                onChange={(e) => setNewService(prev => ({
                                  ...prev,
                                  healthCheck: { ...prev.healthCheck!, path: e.target.value }
                                }))}
                              />
                            </div>
                          </div>
                          <div className="grid grid-cols-2 gap-4">
                            <div>
                              <Label>CPU Limit</Label>
                              <Input
                                value={newService.resources?.cpu}
                                onChange={(e) => setNewService(prev => ({
                                  ...prev,
                                  resources: { ...prev.resources!, cpu: e.target.value }
                                }))}
                                placeholder="100m"
                              />
                            </div>
                            <div>
                              <Label>Memory Limit</Label>
                              <Input
                                value={newService.resources?.memory}
                                onChange={(e) => setNewService(prev => ({
                                  ...prev,
                                  resources: { ...prev.resources!, memory: e.target.value }
                                }))}
                                placeholder="128Mi"
                              />
                            </div>
                          </div>
                          <div className="flex justify-end gap-2">
                            <Button variant="outline" onClick={() => setIsCreatingService(false)}>
                              Cancel
                            </Button>
                            <Button onClick={addMicroservice}>Add Service</Button>
                          </div>
                        </div>
                      </DialogContent>
                    </Dialog>
                  </div>
                </div>
              </CardHeader>
              <CardContent>
                <Tabs value={activeTab} onValueChange={setActiveTab}>
                  <TabsList>
                    <TabsTrigger value="services">Services</TabsTrigger>
                    <TabsTrigger value="network">Network</TabsTrigger>
                    <TabsTrigger value="monitoring">Monitoring</TabsTrigger>
                    <TabsTrigger value="discovery">Discovery</TabsTrigger>
                  </TabsList>

                  <TabsContent value="services" className="space-y-4">
                    {selectedMesh.services.length === 0 ? (
                      <div className="text-center py-8">
                        <Container className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
                        <p className="text-muted-foreground">No services configured</p>
                      </div>
                    ) : (
                      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                        {selectedMesh.services.map(service => (
                          <Card key={service.id}>
                            <CardHeader className="pb-3">
                              <div className="flex items-start justify-between">
                                <div className="flex items-center gap-2">
                                  <div className={`w-3 h-3 rounded-full ${getServiceTypeColor(service.type)}`} />
                                  <CardTitle className="text-lg">{service.name}</CardTitle>
                                </div>
                                <Button
                                  variant="ghost"
                                  size="sm"
                                  onClick={() => removeService(service.id)}
                                >
                                  <Trash2 className="w-4 h-4" />
                                </Button>
                              </div>
                              <CardDescription>{service.description}</CardDescription>
                            </CardHeader>
                            <CardContent className="space-y-2">
                              <div className="flex items-center justify-between text-sm">
                                <span className="text-muted-foreground">Type</span>
                                <Badge variant="outline" className="flex items-center gap-1">
                                  {getServiceTypeIcon(service.type)}
                                  {service.type}
                                </Badge>
                              </div>
                              <div className="flex items-center justify-between text-sm">
                                <span className="text-muted-foreground">Framework</span>
                                <Badge variant="secondary">{service.framework}</Badge>
                              </div>
                              <div className="flex items-center justify-between text-sm">
                                <span className="text-muted-foreground">Port</span>
                                <span>{service.port}</span>
                              </div>
                              <div className="flex items-center justify-between text-sm">
                                <span className="text-muted-foreground">Endpoints</span>
                                <span>{service.endpoints.length}</span>
                              </div>
                            </CardContent>
                          </Card>
                        ))}
                      </div>
                    )}
                  </TabsContent>

                  <TabsContent value="network" className="space-y-4">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      <Card>
                        <CardHeader>
                          <CardTitle className="flex items-center gap-2">
                            <Network className="w-5 h-5" />
                            Networks
                          </CardTitle>
                        </CardHeader>
                        <CardContent className="space-y-2">
                          {selectedMesh.networks.map((network, index) => (
                            <div key={index} className="flex items-center justify-between p-2 bg-muted rounded">
                              <span className="font-mono text-sm">{network.name}</span>
                              <Badge variant="outline">{network.driver}</Badge>
                            </div>
                          ))}
                        </CardContent>
                      </Card>

                      <Card>
                        <CardHeader>
                          <CardTitle className="flex items-center gap-2">
                            <Network className="w-5 h-5" />
                            Gateway Routes
                          </CardTitle>
                        </CardHeader>
                        <CardContent className="space-y-2">
                          {selectedMesh.gateway.routes.map((route, index) => (
                            <div key={index} className="p-2 bg-muted rounded">
                              <div className="text-sm font-mono">{route.path}</div>
                              <div className="text-xs text-muted-foreground">
                                → {route.service}:{route.port}
                              </div>
                            </div>
                          ))}
                        </CardContent>
                      </Card>
                    </div>
                  </TabsContent>

                  <TabsContent value="monitoring" className="space-y-4">
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                      <Card>
                        <CardHeader>
                          <CardTitle className="flex items-center gap-2">
                            <Monitor className="w-5 h-5" />
                            Prometheus
                          </CardTitle>
                        </CardHeader>
                        <CardContent>
                          <Badge variant={selectedMesh.monitoring.prometheus ? "default" : "secondary"}>
                            {selectedMesh.monitoring.prometheus ? "Enabled" : "Disabled"}
                          </Badge>
                        </CardContent>
                      </Card>

                      <Card>
                        <CardHeader>
                          <CardTitle className="flex items-center gap-2">
                            <Monitor className="w-5 h-5" />
                            Grafana
                          </CardTitle>
                        </CardHeader>
                        <CardContent>
                          <Badge variant={selectedMesh.monitoring.grafana ? "default" : "secondary"}>
                            {selectedMesh.monitoring.grafana ? "Enabled" : "Disabled"}
                          </Badge>
                        </CardContent>
                      </Card>

                      <Card>
                        <CardHeader>
                          <CardTitle className="flex items-center gap-2">
                            <GitBranch className="w-5 h-5" />
                            Jaeger
                          </CardTitle>
                        </CardHeader>
                        <CardContent>
                          <Badge variant={selectedMesh.monitoring.jaeger ? "default" : "secondary"}>
                            {selectedMesh.monitoring.jaeger ? "Enabled" : "Disabled"}
                          </Badge>
                        </CardContent>
                      </Card>
                    </div>
                  </TabsContent>

                  <TabsContent value="discovery" className="space-y-4">
                    <Card>
                      <CardHeader>
                        <CardTitle className="flex items-center gap-2">
                          <Globe className="w-5 h-5" />
                          Service Discovery
                        </CardTitle>
                      </CardHeader>
                      <CardContent className="space-y-4">
                        <div className="flex items-center justify-between">
                          <span>Provider</span>
                          <Badge variant="outline">{selectedMesh.serviceDiscovery.type}</Badge>
                        </div>
                        <div className="flex items-center justify-between">
                          <span>Status</span>
                          <Badge variant={selectedMesh.serviceDiscovery.enabled ? "default" : "secondary"}>
                            {selectedMesh.serviceDiscovery.enabled ? "Enabled" : "Disabled"}
                          </Badge>
                        </div>
                        <Alert>
                          <Globe className="w-4 h-4" />
                          <AlertDescription>
                            Service discovery automatically registers and discovers services in the mesh.
                            Services will be accessible by name across the network.
                          </AlertDescription>
                        </Alert>
                      </CardContent>
                    </Card>
                  </TabsContent>
                </Tabs>
              </CardContent>
            </Card>
          )}
        </>
      )}
    </div>
  )
}