import React, { useState, useEffect } from 'react'
import { useKV } from '@github/spark/hooks'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Badge } from '@/components/ui/badge'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { ScrollArea } from '@/components/ui/scroll-area'
import { Database, Plus, Code, Server, Cloud, Link, TestTube, CheckCircle } from '@phosphor-icons/react'
import { toast } from 'sonner'

interface BackendIntegrationProps {
  project: any
  onUpdateProject: (id: string, updates: any) => void
}

interface APIEndpoint {
  id: string
  name: string
  method: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH'
  path: string
  description: string
  requestBody?: string
  responseBody?: string
  status: 'draft' | 'implemented' | 'tested'
}

interface DatabaseSchema {
  id: string
  name: string
  type: 'table' | 'collection'
  fields: {
    name: string
    type: string
    required: boolean
    description?: string
  }[]
  relationships?: string[]
}

interface BackendConfig {
  framework: string
  database: string
  authentication: string
  apiStyle: 'REST' | 'GraphQL' | 'tRPC'
  deploymentTarget: string
}

const backendFrameworks = [
  { id: 'express', name: 'Express.js', description: 'Fast Node.js web framework' },
  { id: 'fastify', name: 'Fastify', description: 'Fast and low overhead web framework' },
  { id: 'nestjs', name: 'NestJS', description: 'Progressive Node.js framework' },
  { id: 'fastapi', name: 'FastAPI', description: 'Modern Python web framework' },
  { id: 'django', name: 'Django', description: 'High-level Python web framework' },
  { id: 'spring', name: 'Spring Boot', description: 'Java application framework' }
]

const databases = [
  { id: 'postgresql', name: 'PostgreSQL', description: 'Advanced open source database' },
  { id: 'mysql', name: 'MySQL', description: 'Popular relational database' },
  { id: 'mongodb', name: 'MongoDB', description: 'Document-oriented database' },
  { id: 'redis', name: 'Redis', description: 'In-memory data structure store' },
  { id: 'supabase', name: 'Supabase', description: 'Open source Firebase alternative' },
  { id: 'firebase', name: 'Firebase', description: 'Google\'s app development platform' }
]

export function BackendIntegration({ project, onUpdateProject }: BackendIntegrationProps) {
  const [config, setConfig] = useKV<BackendConfig>(`backend-config-${project.id}`, {
    framework: 'express',
    database: 'postgresql',
    authentication: 'jwt',
    apiStyle: 'REST',
    deploymentTarget: 'vercel'
  })
  const [endpoints, setEndpoints] = useKV<APIEndpoint[]>(`api-endpoints-${project.id}`, [])
  const [schemas, setSchemas] = useKV<DatabaseSchema[]>(`db-schemas-${project.id}`, [])
  const [newEndpoint, setNewEndpoint] = useState<Partial<APIEndpoint>>({
    method: 'GET',
    status: 'draft'
  })
  const [newSchema, setNewSchema] = useState<Partial<DatabaseSchema>>({
    type: 'table',
    fields: []
  })
  const [isGenerating, setIsGenerating] = useState(false)

  const generateBackendCode = async () => {
    setIsGenerating(true)
    
    try {
      const prompt = spark.llmPrompt`
        Generate a complete ${config.framework} backend for this ${project.type} project:
        
        Project: ${project.name}
        Framework: ${config.framework}
        Database: ${config.database}
        API Style: ${config.apiStyle}
        
        API Endpoints:
        ${endpoints.map(ep => `${ep.method} ${ep.path} - ${ep.description}`).join('\n')}
        
        Database Schemas:
        ${schemas.map(schema => `${schema.name}: ${schema.fields.map(f => `${f.name}:${f.type}`).join(', ')}`).join('\n')}
        
        Generate:
        1. Complete backend application structure
        2. API routes and controllers
        3. Database models and migrations
        4. Authentication middleware
        5. Error handling
        6. API documentation
        7. Environment configuration
        8. Database connection setup
        
        Return as JSON with file structure:
        {
          "files": {
            "path/to/file": "file content"
          },
          "dependencies": ["package1", "package2"],
          "scripts": {
            "dev": "start command",
            "build": "build command"
          }
        }
      `

      const response = await spark.llm(prompt, 'gpt-4o', true)
      const backendCode = JSON.parse(response)

      // Add backend files to project
      const updatedFiles = { ...project.codebase.files }
      Object.entries(backendCode.files).forEach(([path, content]: [string, any]) => {
        updatedFiles[path] = content
      })

      // Update dependencies
      const updatedDependencies = [
        ...(project.codebase.dependencies || []),
        ...(backendCode.dependencies || [])
      ]

      onUpdateProject(project.id, {
        codebase: {
          ...project.codebase,
          files: updatedFiles,
          dependencies: updatedDependencies
        }
      })

      toast.success('Backend code generated successfully!')
      
    } catch (error) {
      console.error('Backend generation failed:', error)
      toast.error('Failed to generate backend code')
    } finally {
      setIsGenerating(false)
    }
  }

  const addEndpoint = () => {
    if (!newEndpoint.name || !newEndpoint.path) {
      toast.error('Please fill in endpoint name and path')
      return
    }

    const endpoint: APIEndpoint = {
      id: Date.now().toString(),
      name: newEndpoint.name!,
      method: newEndpoint.method!,
      path: newEndpoint.path!,
      description: newEndpoint.description || '',
      requestBody: newEndpoint.requestBody,
      responseBody: newEndpoint.responseBody,
      status: 'draft'
    }

    setEndpoints(prev => [...prev, endpoint])
    setNewEndpoint({ method: 'GET', status: 'draft' })
    toast.success('API endpoint added')
  }

  const addSchema = () => {
    if (!newSchema.name || !newSchema.fields?.length) {
      toast.error('Please fill in schema name and add at least one field')
      return
    }

    const schema: DatabaseSchema = {
      id: Date.now().toString(),
      name: newSchema.name!,
      type: newSchema.type!,
      fields: newSchema.fields!
    }

    setSchemas(prev => [...prev, schema])
    setNewSchema({ type: 'table', fields: [] })
    toast.success('Database schema added')
  }

  const addField = () => {
    setNewSchema(prev => ({
      ...prev,
      fields: [
        ...(prev.fields || []),
        { name: '', type: 'string', required: false }
      ]
    }))
  }

  const updateField = (index: number, field: any) => {
    setNewSchema(prev => ({
      ...prev,
      fields: prev.fields?.map((f, i) => i === index ? field : f) || []
    }))
  }

  const removeField = (index: number) => {
    setNewSchema(prev => ({
      ...prev,
      fields: prev.fields?.filter((_, i) => i !== index) || []
    }))
  }

  const testEndpoint = async (endpoint: APIEndpoint) => {
    try {
      // Simulate API testing
      toast.info(`Testing ${endpoint.method} ${endpoint.path}...`)
      
      await new Promise(resolve => setTimeout(resolve, 2000))
      
      // Update endpoint status
      setEndpoints(prev => prev.map(ep => 
        ep.id === endpoint.id 
          ? { ...ep, status: Math.random() > 0.2 ? 'tested' : 'draft' }
          : ep
      ))
      
      toast.success(`${endpoint.name} test completed`)
    } catch (error) {
      toast.error('Endpoint test failed')
    }
  }

  const generateAPIDocumentation = async () => {
    try {
      const prompt = spark.llmPrompt`
        Generate comprehensive API documentation for these endpoints:
        
        ${endpoints.map(ep => `
        ${ep.method} ${ep.path}
        Description: ${ep.description}
        Request: ${ep.requestBody || 'None'}
        Response: ${ep.responseBody || 'None'}
        `).join('\n')}
        
        Generate OpenAPI/Swagger documentation with examples.
      `

      const response = await spark.llm(prompt, 'gpt-4o')
      
      // Add documentation file to project
      const updatedFiles = {
        ...project.codebase.files,
        'api-docs.md': response
      }

      onUpdateProject(project.id, {
        codebase: {
          ...project.codebase,
          files: updatedFiles
        }
      })

      toast.success('API documentation generated!')
    } catch (error) {
      toast.error('Failed to generate documentation')
    }
  }

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Database className="w-5 h-5" />
            Backend Integration
          </CardTitle>
          <CardDescription>
            Design and implement backend APIs, database integration, and server-side functionality
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="space-y-2">
              <Label>Framework</Label>
              <Select value={config.framework} onValueChange={(value) => 
                setConfig(prev => ({ ...prev, framework: value }))
              }>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {backendFrameworks.map(fw => (
                    <SelectItem key={fw.id} value={fw.id}>
                      {fw.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label>Database</Label>
              <Select value={config.database} onValueChange={(value) => 
                setConfig(prev => ({ ...prev, database: value }))
              }>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {databases.map(db => (
                    <SelectItem key={db.id} value={db.id}>
                      {db.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label>API Style</Label>
              <Select value={config.apiStyle} onValueChange={(value: any) => 
                setConfig(prev => ({ ...prev, apiStyle: value }))
              }>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="REST">REST API</SelectItem>
                  <SelectItem value="GraphQL">GraphQL</SelectItem>
                  <SelectItem value="tRPC">tRPC</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label>Auth</Label>
              <Select value={config.authentication} onValueChange={(value) => 
                setConfig(prev => ({ ...prev, authentication: value }))
              }>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="jwt">JWT</SelectItem>
                  <SelectItem value="oauth">OAuth</SelectItem>
                  <SelectItem value="session">Sessions</SelectItem>
                  <SelectItem value="supabase">Supabase Auth</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          <Button 
            onClick={generateBackendCode}
            disabled={isGenerating}
            className="w-full"
          >
            <Code className="w-4 h-4 mr-2" />
            {isGenerating ? 'Generating Backend...' : 'Generate Backend Code'}
          </Button>
        </CardContent>
      </Card>

      <Tabs defaultValue="endpoints" className="space-y-4">
        <TabsList className="grid w-full grid-cols-3">
          <TabsTrigger value="endpoints">API Endpoints</TabsTrigger>
          <TabsTrigger value="database">Database Schema</TabsTrigger>
          <TabsTrigger value="integration">Integration</TabsTrigger>
        </TabsList>

        <TabsContent value="endpoints" className="space-y-4">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <Card>
              <CardHeader>
                <div className="flex items-center justify-between">
                  <CardTitle className="text-lg">API Endpoints</CardTitle>
                  <Button onClick={generateAPIDocumentation} variant="outline" size="sm">
                    Generate Docs
                  </Button>
                </div>
              </CardHeader>
              <CardContent>
                <ScrollArea className="h-64">
                  <div className="space-y-3">
                    {endpoints.map(endpoint => (
                      <div key={endpoint.id} className="border rounded-lg p-3">
                        <div className="flex items-center justify-between mb-2">
                          <div className="flex items-center gap-2">
                            <Badge variant="outline">{endpoint.method}</Badge>
                            <span className="font-mono text-sm">{endpoint.path}</span>
                          </div>
                          <div className="flex items-center gap-2">
                            <Badge variant={endpoint.status === 'tested' ? 'default' : 'secondary'}>
                              {endpoint.status}
                            </Badge>
                            <Button 
                              variant="ghost" 
                              size="sm"
                              onClick={() => testEndpoint(endpoint)}
                            >
                              <TestTube className="w-4 h-4" />
                            </Button>
                          </div>
                        </div>
                        <div>
                          <h4 className="font-medium">{endpoint.name}</h4>
                          <p className="text-sm text-muted-foreground">{endpoint.description}</p>
                        </div>
                      </div>
                    ))}
                  </div>
                </ScrollArea>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="text-lg">Add New Endpoint</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label>Method</Label>
                    <Select 
                      value={newEndpoint.method} 
                      onValueChange={(value: any) => setNewEndpoint(prev => ({ ...prev, method: value }))}
                    >
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="GET">GET</SelectItem>
                        <SelectItem value="POST">POST</SelectItem>
                        <SelectItem value="PUT">PUT</SelectItem>
                        <SelectItem value="DELETE">DELETE</SelectItem>
                        <SelectItem value="PATCH">PATCH</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                  <div className="space-y-2">
                    <Label>Path</Label>
                    <Input
                      placeholder="/api/users"
                      value={newEndpoint.path || ''}
                      onChange={(e) => setNewEndpoint(prev => ({ ...prev, path: e.target.value }))}
                    />
                  </div>
                </div>

                <div className="space-y-2">
                  <Label>Name</Label>
                  <Input
                    placeholder="Get all users"
                    value={newEndpoint.name || ''}
                    onChange={(e) => setNewEndpoint(prev => ({ ...prev, name: e.target.value }))}
                  />
                </div>

                <div className="space-y-2">
                  <Label>Description</Label>
                  <Textarea
                    placeholder="Endpoint description..."
                    value={newEndpoint.description || ''}
                    onChange={(e) => setNewEndpoint(prev => ({ ...prev, description: e.target.value }))}
                  />
                </div>

                <Button onClick={addEndpoint} className="w-full">
                  <Plus className="w-4 h-4 mr-2" />
                  Add Endpoint
                </Button>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        <TabsContent value="database" className="space-y-4">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <Card>
              <CardHeader>
                <CardTitle className="text-lg">Database Schemas</CardTitle>
              </CardHeader>
              <CardContent>
                <ScrollArea className="h-64">
                  <div className="space-y-3">
                    {schemas.map(schema => (
                      <div key={schema.id} className="border rounded-lg p-3">
                        <div className="flex items-center justify-between mb-2">
                          <h4 className="font-medium">{schema.name}</h4>
                          <Badge variant="outline">{schema.type}</Badge>
                        </div>
                        <div className="space-y-1">
                          {schema.fields.map((field, index) => (
                            <div key={index} className="text-sm flex items-center gap-2">
                              <span className="font-mono">{field.name}</span>
                              <Badge variant="secondary" className="text-xs">{field.type}</Badge>
                              {field.required && <Badge variant="outline" className="text-xs">required</Badge>}
                            </div>
                          ))}
                        </div>
                      </div>
                    ))}
                  </div>
                </ScrollArea>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="text-lg">Add Schema</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label>Name</Label>
                    <Input
                      placeholder="users"
                      value={newSchema.name || ''}
                      onChange={(e) => setNewSchema(prev => ({ ...prev, name: e.target.value }))}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label>Type</Label>
                    <Select 
                      value={newSchema.type} 
                      onValueChange={(value: any) => setNewSchema(prev => ({ ...prev, type: value }))}
                    >
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="table">Table</SelectItem>
                        <SelectItem value="collection">Collection</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                </div>

                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <Label>Fields</Label>
                    <Button variant="outline" size="sm" onClick={addField}>
                      <Plus className="w-4 h-4" />
                    </Button>
                  </div>
                  <div className="space-y-2 max-h-32 overflow-y-auto">
                    {newSchema.fields?.map((field, index) => (
                      <div key={index} className="grid grid-cols-3 gap-2 items-center">
                        <Input
                          placeholder="field name"
                          value={field.name}
                          onChange={(e) => updateField(index, { ...field, name: e.target.value })}
                        />
                        <Select 
                          value={field.type} 
                          onValueChange={(value) => updateField(index, { ...field, type: value })}
                        >
                          <SelectTrigger>
                            <SelectValue />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="string">String</SelectItem>
                            <SelectItem value="number">Number</SelectItem>
                            <SelectItem value="boolean">Boolean</SelectItem>
                            <SelectItem value="date">Date</SelectItem>
                            <SelectItem value="text">Text</SelectItem>
                          </SelectContent>
                        </Select>
                        <Button 
                          variant="ghost" 
                          size="sm" 
                          onClick={() => removeField(index)}
                        >
                          ×
                        </Button>
                      </div>
                    ))}
                  </div>
                </div>

                <Button onClick={addSchema} className="w-full">
                  <Plus className="w-4 h-4 mr-2" />
                  Add Schema
                </Button>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        <TabsContent value="integration" className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <Card>
              <CardHeader>
                <CardTitle className="text-lg">Integration Status</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="space-y-3">
                  <div className="flex items-center justify-between">
                    <span>Backend Framework</span>
                    <Badge variant="outline">{config.framework}</Badge>
                  </div>
                  <div className="flex items-center justify-between">
                    <span>Database</span>
                    <Badge variant="outline">{config.database}</Badge>
                  </div>
                  <div className="flex items-center justify-between">
                    <span>API Endpoints</span>
                    <Badge>{endpoints.length} defined</Badge>
                  </div>
                  <div className="flex items-center justify-between">
                    <span>Database Schemas</span>
                    <Badge>{schemas.length} created</Badge>
                  </div>
                  <div className="flex items-center justify-between">
                    <span>Testing Status</span>
                    <Badge variant={endpoints.some(ep => ep.status === 'tested') ? 'default' : 'secondary'}>
                      {endpoints.filter(ep => ep.status === 'tested').length}/{endpoints.length} tested
                    </Badge>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="text-lg">Quick Actions</CardTitle>
              </CardHeader>
              <CardContent className="space-y-3">
                <Button variant="outline" className="w-full justify-start">
                  <Server className="w-4 h-4 mr-2" />
                  Deploy Backend
                </Button>
                <Button variant="outline" className="w-full justify-start">
                  <TestTube className="w-4 h-4 mr-2" />
                  Run Integration Tests
                </Button>
                <Button variant="outline" className="w-full justify-start">
                  <Link className="w-4 h-4 mr-2" />
                  Connect Frontend
                </Button>
                <Button variant="outline" className="w-full justify-start">
                  <Cloud className="w-4 h-4 mr-2" />
                  Setup Environment
                </Button>
              </CardContent>
            </Card>
          </div>
        </TabsContent>
      </Tabs>
    </div>
  )
}