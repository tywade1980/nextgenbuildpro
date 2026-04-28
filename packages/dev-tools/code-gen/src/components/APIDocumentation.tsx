import React, { useState } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Label } from '@/components/ui/label'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { ScrollArea } from '@/components/ui/scroll-area'
import { 
  Code, 
  Database, 
  Shield, 
  Globe, 
  FileText, 
  Play, 
  Terminal,
  Copy,
  CheckCircle,
  ExternalLink,
  Rocket
} from '@phosphor-icons/react'
import { frameworkTemplates, FrameworkTemplate } from '@/templates/framework-templates'
import { toast } from 'sonner'

interface APIDocumentationProps {
  project?: any
}

export const APIDocumentation: React.FC<APIDocumentationProps> = ({ project }) => {
  const [copiedCode, setCopiedCode] = useState<string | null>(null)
  const [activeEndpoint, setActiveEndpoint] = useState<string>('auth')

  const copyToClipboard = async (text: string, id: string) => {
    try {
      await navigator.clipboard.writeText(text)
      setCopiedCode(id)
      setTimeout(() => setCopiedCode(null), 2000)
      toast.success('Code copied to clipboard!')
    } catch (error) {
      toast.error('Failed to copy code')
    }
  }

  const getExpressTemplate = (): FrameworkTemplate | null => {
    if (!project?.template) return null
    return frameworkTemplates.find(t => t.id === project.template) || null
  }

  const template = getExpressTemplate()
  if (!template || !template.id.includes('express')) {
    return (
      <Alert>
        <AlertDescription>
          API documentation is only available for Express projects.
        </AlertDescription>
      </Alert>
    )
  }

  const apiEndpoints = {
    auth: {
      title: 'Authentication',
      icon: <Shield className="w-5 h-5" />,
      endpoints: [
        {
          method: 'POST',
          path: '/api/auth/register',
          description: 'Register a new user',
          request: {
            body: {
              name: 'string',
              email: 'string',
              password: 'string'
            }
          },
          response: {
            success: true,
            message: 'User registered successfully',
            data: {
              user: { id: 'string', name: 'string', email: 'string' },
              token: 'string'
            }
          }
        },
        {
          method: 'POST',
          path: '/api/auth/login',
          description: 'Login user',
          request: {
            body: {
              email: 'string',
              password: 'string'
            }
          },
          response: {
            success: true,
            message: 'Login successful',
            data: {
              user: { id: 'string', name: 'string', email: 'string' },
              token: 'string'
            }
          }
        },
        {
          method: 'GET',
          path: '/api/auth/me',
          description: 'Get current user profile',
          headers: {
            Authorization: 'Bearer <token>'
          },
          response: {
            success: true,
            data: {
              user: { id: 'string', name: 'string', email: 'string', role: 'string' }
            }
          }
        }
      ]
    },
    users: {
      title: 'Users',
      icon: <Database className="w-5 h-5" />,
      endpoints: [
        {
          method: 'GET',
          path: '/api/users',
          description: 'Get all users (Admin only)',
          headers: {
            Authorization: 'Bearer <token>'
          },
          query: {
            page: 'number (optional)',
            limit: 'number (optional)',
          },
          response: {
            success: true,
            data: {
              users: [],
              pagination: {
                page: 1,
                limit: 10,
                total: 0,
                totalPages: 0
              }
            }
          }
        },
        {
          method: 'GET',
          path: '/api/users/:id',
          description: 'Get user by ID',
          headers: {
            Authorization: 'Bearer <token>'
          },
          response: {
            success: true,
            data: {
              user: { id: 'string', name: 'string', email: 'string' }
            }
          }
        },
        {
          method: 'PUT',
          path: '/api/users/:id',
          description: 'Update user',
          headers: {
            Authorization: 'Bearer <token>'
          },
          request: {
            body: {
              name: 'string (optional)',
              email: 'string (optional)'
            }
          },
          response: {
            success: true,
            message: 'User updated successfully',
            data: {
              user: { id: 'string', name: 'string', email: 'string' }
            }
          }
        }
      ]
    },
    products: {
      title: 'Products',
      icon: <Globe className="w-5 h-5" />,
      endpoints: [
        {
          method: 'GET',
          path: '/api/products',
          description: 'Get all products',
          query: {
            page: 'number (optional)',
            limit: 'number (optional)',
            category: 'string (optional)',
            search: 'string (optional)'
          },
          response: {
            success: true,
            data: {
              products: [],
              pagination: {
                page: 1,
                limit: 10,
                total: 0,
                totalPages: 0
              }
            }
          }
        },
        {
          method: 'POST',
          path: '/api/products',
          description: 'Create a new product',
          headers: {
            Authorization: 'Bearer <token>'
          },
          request: {
            body: {
              name: 'string',
              description: 'string',
              price: 'number',
              category: 'string',
              stock: 'number (optional)'
            }
          },
          response: {
            success: true,
            message: 'Product created successfully',
            data: {
              product: { id: 'string', name: 'string', price: 0 }
            }
          }
        }
      ]
    }
  }

  const curlExamples = {
    register: `curl -X POST http://localhost:3000/api/auth/register \\
  -H "Content-Type: application/json" \\
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "password123"
  }'`,
    login: `curl -X POST http://localhost:3000/api/auth/login \\
  -H "Content-Type: application/json" \\
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'`,
    getUsers: `curl -X GET http://localhost:3000/api/users \\
  -H "Authorization: Bearer YOUR_JWT_TOKEN"`,
    createProduct: `curl -X POST http://localhost:3000/api/products \\
  -H "Content-Type: application/json" \\
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \\
  -d '{
    "name": "Awesome Product",
    "description": "A great product",
    "price": 29.99,
    "category": "electronics",
    "stock": 100
  }'`
  }

  const envExample = `# Server Configuration
NODE_ENV=development
PORT=3000

# Database
${template.id.includes('mongodb') ? 'MONGODB_URI=mongodb://localhost:27017/express-api' : ''}
${template.id.includes('postgresql') ? 'DATABASE_URL=postgresql://username:password@localhost:5432/database' : ''}

# JWT Configuration
JWT_SECRET=your-super-secret-jwt-key-here
JWT_EXPIRES_IN=7d

# CORS Configuration
FRONTEND_URL=http://localhost:3000`

  const dockerRunExample = template.id.includes('microservices') 
    ? 'docker-compose -f docker-compose.microservices.yml up -d'
    : 'docker-compose up -d'

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-semibold">API Documentation</h2>
          <p className="text-muted-foreground">
            Complete API reference for your {template.name} project
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Badge variant="outline" className="flex items-center gap-1">
            <Database className="w-3 h-3" />
            {template.name}
          </Badge>
          <Button variant="outline" size="sm" asChild>
            <a href="http://localhost:3000/api-docs" target="_blank" rel="noopener noreferrer">
              <ExternalLink className="w-4 h-4 mr-2" />
              Swagger UI
            </a>
          </Button>
        </div>
      </div>

      <Tabs value={activeEndpoint} onValueChange={setActiveEndpoint} className="w-full">
        <TabsList className="grid w-full grid-cols-4">
          <TabsTrigger value="auth">Authentication</TabsTrigger>
          <TabsTrigger value="users">Users</TabsTrigger>
          <TabsTrigger value="products">Products</TabsTrigger>
          <TabsTrigger value="setup">Setup</TabsTrigger>
        </TabsList>

        {Object.entries(apiEndpoints).map(([key, section]) => (
          <TabsContent key={key} value={key} className="space-y-4">
            <div className="flex items-center gap-2 mb-4">
              {section.icon}
              <h3 className="text-xl font-semibold">{section.title} API</h3>
            </div>

            <div className="space-y-4">
              {section.endpoints.map((endpoint, index) => (
                <Card key={index}>
                  <CardHeader>
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-3">
                        <Badge 
                          variant={endpoint.method === 'GET' ? 'secondary' : 
                                  endpoint.method === 'POST' ? 'default' : 
                                  endpoint.method === 'PUT' ? 'outline' : 'destructive'}
                          className="font-mono"
                        >
                          {endpoint.method}
                        </Badge>
                        <code className="text-sm font-mono bg-muted px-2 py-1 rounded">
                          {endpoint.path}
                        </code>
                      </div>
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => copyToClipboard(
                          JSON.stringify(endpoint.response, null, 2),
                          `${key}-${index}-response`
                        )}
                      >
                        {copiedCode === `${key}-${index}-response` ? (
                          <CheckCircle className="w-4 h-4" />
                        ) : (
                          <Copy className="w-4 h-4" />
                        )}
                      </Button>
                    </div>
                    <CardDescription>{endpoint.description}</CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    {endpoint.headers && (
                      <div>
                        <h4 className="font-medium mb-2">Headers</h4>
                        <div className="bg-muted p-3 rounded-lg">
                          <pre className="text-sm">
                            {JSON.stringify(endpoint.headers, null, 2)}
                          </pre>
                        </div>
                      </div>
                    )}

                    {endpoint.query && (
                      <div>
                        <h4 className="font-medium mb-2">Query Parameters</h4>
                        <div className="bg-muted p-3 rounded-lg">
                          <pre className="text-sm">
                            {JSON.stringify(endpoint.query, null, 2)}
                          </pre>
                        </div>
                      </div>
                    )}

                    {endpoint.request && (
                      <div>
                        <h4 className="font-medium mb-2">Request Body</h4>
                        <div className="bg-muted p-3 rounded-lg">
                          <pre className="text-sm">
                            {JSON.stringify(endpoint.request.body, null, 2)}
                          </pre>
                        </div>
                      </div>
                    )}

                    <div>
                      <h4 className="font-medium mb-2">Response</h4>
                      <div className="bg-muted p-3 rounded-lg">
                        <pre className="text-sm">
                          {JSON.stringify(endpoint.response, null, 2)}
                        </pre>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          </TabsContent>
        ))}

        <TabsContent value="setup" className="space-y-6">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Terminal className="w-5 h-5" />
                  Environment Setup
                </CardTitle>
                <CardDescription>
                  Configure your environment variables
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  <div className="flex items-center justify-between">
                    <Label>Environment Variables</Label>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => copyToClipboard(envExample, 'env')}
                    >
                      {copiedCode === 'env' ? (
                        <CheckCircle className="w-4 h-4" />
                      ) : (
                        <Copy className="w-4 h-4" />
                      )}
                    </Button>
                  </div>
                  <div className="bg-muted p-3 rounded-lg">
                    <pre className="text-sm whitespace-pre-wrap">
                      {envExample}
                    </pre>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Rocket className="w-5 h-5" />
                  Quick Start
                </CardTitle>
                <CardDescription>
                  Get your API running in minutes
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div className="space-y-2">
                    <h4 className="font-medium">1. Install Dependencies</h4>
                    <div className="bg-muted p-2 rounded text-sm font-mono">
                      npm install
                    </div>
                  </div>
                  
                  <div className="space-y-2">
                    <h4 className="font-medium">2. Setup Environment</h4>
                    <div className="bg-muted p-2 rounded text-sm font-mono">
                      cp .env.example .env
                    </div>
                  </div>
                  
                  {template.id.includes('mongodb') && (
                    <div className="space-y-2">
                      <h4 className="font-medium">3. Start Database</h4>
                      <div className="bg-muted p-2 rounded text-sm font-mono">
                        mongod
                      </div>
                    </div>
                  )}
                  
                  {template.id.includes('postgresql') && (
                    <div className="space-y-2">
                      <h4 className="font-medium">3. Run Database Migrations</h4>
                      <div className="bg-muted p-2 rounded text-sm font-mono">
                        npm run db:migrate
                      </div>
                    </div>
                  )}
                  
                  <div className="space-y-2">
                    <h4 className="font-medium">4. Start Development Server</h4>
                    <div className="bg-muted p-2 rounded text-sm font-mono">
                      npm run dev
                    </div>
                  </div>

                  {template.id.includes('docker') && (
                    <div className="space-y-2">
                      <h4 className="font-medium">Or use Docker</h4>
                      <div className="bg-muted p-2 rounded text-sm font-mono">
                        {dockerRunExample}
                      </div>
                    </div>
                  )}
                </div>
              </CardContent>
            </Card>
          </div>

          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Code className="w-5 h-5" />
                Example API Calls
              </CardTitle>
              <CardDescription>
                Test your API with these curl examples
              </CardDescription>
            </CardHeader>
            <CardContent>
              <Tabs defaultValue="register" className="w-full">
                <TabsList className="grid w-full grid-cols-4">
                  <TabsTrigger value="register">Register</TabsTrigger>
                  <TabsTrigger value="login">Login</TabsTrigger>
                  <TabsTrigger value="users">Get Users</TabsTrigger>
                  <TabsTrigger value="products">Create Product</TabsTrigger>
                </TabsList>

                {Object.entries(curlExamples).map(([key, command]) => (
                  <TabsContent key={key} value={key}>
                    <div className="space-y-3">
                      <div className="flex items-center justify-between">
                        <h4 className="font-medium capitalize">{key.replace(/([A-Z])/g, ' $1')}</h4>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => copyToClipboard(command, key)}
                        >
                          {copiedCode === key ? (
                            <CheckCircle className="w-4 h-4" />
                          ) : (
                            <Copy className="w-4 h-4" />
                          )}
                        </Button>
                      </div>
                      <div className="bg-muted p-3 rounded-lg">
                        <pre className="text-sm whitespace-pre-wrap">
                          {command}
                        </pre>
                      </div>
                    </div>
                  </TabsContent>
                ))}
              </Tabs>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}