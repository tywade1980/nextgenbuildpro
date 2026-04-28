/**
 * API Documentation Generator - Generates comprehensive API documentation
 */

import { APIEndpoint } from '@/services/APIService'
import { DatabaseSchema } from '@/services/DatabaseService'

export interface APIDocumentationConfig {
  title: string
  version: string
  description: string
  baseUrl: string
  contactEmail?: string
  termsOfService?: string
  license?: {
    name: string
    url: string
  }
}

export class APIDocumentationGenerator {
  
  /**
   * Generate OpenAPI/Swagger documentation
   */
  static generateOpenAPISpec(
    config: APIDocumentationConfig,
    endpoints: APIEndpoint[],
    schemas: DatabaseSchema[]
  ): any {
    const spec = {
      openapi: '3.0.3',
      info: {
        title: config.title,
        version: config.version,
        description: config.description,
        contact: config.contactEmail ? { email: config.contactEmail } : undefined,
        termsOfService: config.termsOfService,
        license: config.license
      },
      servers: [
        {
          url: config.baseUrl,
          description: 'Main server'
        }
      ],
      paths: {},
      components: {
        schemas: {},
        securitySchemes: {
          bearerAuth: {
            type: 'http',
            scheme: 'bearer',
            bearerFormat: 'JWT'
          },
          apiKey: {
            type: 'apiKey',
            in: 'header',
            name: 'X-API-Key'
          }
        }
      }
    }

    // Generate schema definitions
    schemas.forEach(schema => {
      const schemaName = schema.tableName.charAt(0).toUpperCase() + schema.tableName.slice(1)
      
      spec.components.schemas[schemaName] = {
        type: 'object',
        properties: {},
        required: schema.columns.filter(col => !col.nullable).map(col => col.name)
      }

      schema.columns.forEach(column => {
        spec.components.schemas[schemaName].properties[column.name] = {
          type: this.mapToOpenAPIType(column.type),
          description: `${column.name} field`,
          ...(column.primaryKey && { readOnly: true }),
          ...(column.defaultValue && { default: column.defaultValue })
        }
      })

      // Create input schema (without ID and read-only fields)
      spec.components.schemas[`${schemaName}Input`] = {
        type: 'object',
        properties: {},
        required: schema.columns.filter(col => !col.nullable && !col.primaryKey).map(col => col.name)
      }

      schema.columns.filter(col => !col.primaryKey).forEach(column => {
        spec.components.schemas[`${schemaName}Input`].properties[column.name] = {
          type: this.mapToOpenAPIType(column.type),
          description: `${column.name} field`
        }
      })
    })

    // Generate paths from endpoints
    endpoints.forEach(endpoint => {
      const path = endpoint.url.replace(config.baseUrl, '')
      
      if (!spec.paths[path]) {
        spec.paths[path] = {}
      }

      spec.paths[path][endpoint.method.toLowerCase()] = {
        summary: endpoint.name,
        description: `${endpoint.method} ${path}`,
        parameters: this.generateParameters(endpoint),
        requestBody: this.generateRequestBody(endpoint),
        responses: this.generateResponses(endpoint),
        security: endpoint.auth ? [{ bearerAuth: [] }] : []
      }
    })

    return spec
  }

  /**
   * Generate Markdown documentation
   */
  static generateMarkdownDocs(
    config: APIDocumentationConfig,
    endpoints: APIEndpoint[],
    schemas: DatabaseSchema[]
  ): string {
    let doc = `# ${config.title}\n\n`
    doc += `Version: ${config.version}\n\n`
    doc += `${config.description}\n\n`
    
    if (config.baseUrl) {
      doc += `**Base URL:** \`${config.baseUrl}\`\n\n`
    }

    // Table of Contents
    doc += '## Table of Contents\n\n'
    doc += '- [Authentication](#authentication)\n'
    doc += '- [Endpoints](#endpoints)\n'
    schemas.forEach(schema => {
      doc += `  - [${schema.tableName}](#${schema.tableName.toLowerCase()})\n`
    })
    doc += '- [Schemas](#schemas)\n'
    doc += '- [Error Codes](#error-codes)\n\n'

    // Authentication
    doc += '## Authentication\n\n'
    doc += 'This API uses Bearer token authentication. Include your token in the Authorization header:\n\n'
    doc += '```\nAuthorization: Bearer YOUR_TOKEN_HERE\n```\n\n'

    // Endpoints
    doc += '## Endpoints\n\n'
    
    const groupedEndpoints = this.groupEndpointsByResource(endpoints)
    Object.entries(groupedEndpoints).forEach(([resource, resourceEndpoints]) => {
      doc += `### ${resource}\n\n`
      
      resourceEndpoints.forEach(endpoint => {
        doc += `#### ${endpoint.name}\n\n`
        doc += `**${endpoint.method}** \`${endpoint.url}\`\n\n`
        
        if (endpoint.auth) {
          doc += '🔒 **Requires Authentication**\n\n'
        }

        // Parameters
        if (endpoint.params && Object.keys(endpoint.params).length > 0) {
          doc += '**Parameters:**\n\n'
          doc += '| Name | Type | Required | Description |\n'
          doc += '|------|------|----------|-------------|\n'
          Object.entries(endpoint.params).forEach(([name, value]) => {
            doc += `| ${name} | ${typeof value} | Yes | Parameter description |\n`
          })
          doc += '\n'
        }

        // Request Body
        if (endpoint.body && endpoint.method !== 'GET') {
          doc += '**Request Body:**\n\n'
          doc += '```json\n'
          doc += JSON.stringify(endpoint.body, null, 2)
          doc += '\n```\n\n'
        }

        // Response Examples
        doc += '**Response:**\n\n'
        doc += '```json\n'
        if (endpoint.method === 'GET') {
          doc += this.generateSampleResponse(resource, true)
        } else {
          doc += this.generateSampleResponse(resource, false)
        }
        doc += '\n```\n\n'

        // cURL Example
        doc += '**Example Request:**\n\n'
        doc += '```bash\n'
        doc += this.generateCurlExample(endpoint, config.baseUrl)
        doc += '\n```\n\n'

        doc += '---\n\n'
      })
    })

    // Schemas
    doc += '## Schemas\n\n'
    schemas.forEach(schema => {
      const schemaName = schema.tableName.charAt(0).toUpperCase() + schema.tableName.slice(1)
      doc += `### ${schemaName}\n\n`
      
      doc += '| Field | Type | Required | Description |\n'
      doc += '|-------|------|----------|-------------|\n'
      schema.columns.forEach(column => {
        const required = !column.nullable ? 'Yes' : 'No'
        doc += `| ${column.name} | ${column.type} | ${required} | ${column.name} field |\n`
      })
      doc += '\n'
    })

    // Error Codes
    doc += '## Error Codes\n\n'
    doc += '| Code | Description |\n'
    doc += '|------|-------------|\n'
    doc += '| 200 | Success |\n'
    doc += '| 201 | Created |\n'
    doc += '| 400 | Bad Request |\n'
    doc += '| 401 | Unauthorized |\n'
    doc += '| 403 | Forbidden |\n'
    doc += '| 404 | Not Found |\n'
    doc += '| 422 | Validation Error |\n'
    doc += '| 500 | Internal Server Error |\n\n'

    return doc
  }

  /**
   * Generate Postman collection
   */
  static generatePostmanCollection(
    config: APIDocumentationConfig,
    endpoints: APIEndpoint[]
  ): any {
    const collection = {
      info: {
        name: config.title,
        description: config.description,
        version: config.version,
        schema: 'https://schema.getpostman.com/json/collection/v2.1.0/collection.json'
      },
      auth: {
        type: 'bearer',
        bearer: [
          {
            key: 'token',
            value: '{{auth_token}}',
            type: 'string'
          }
        ]
      },
      variable: [
        {
          key: 'base_url',
          value: config.baseUrl,
          type: 'string'
        },
        {
          key: 'auth_token',
          value: 'your_token_here',
          type: 'string'
        }
      ],
      item: []
    }

    const groupedEndpoints = this.groupEndpointsByResource(endpoints)
    Object.entries(groupedEndpoints).forEach(([resource, resourceEndpoints]) => {
      const folder = {
        name: resource,
        item: []
      }

      resourceEndpoints.forEach(endpoint => {
        const request = {
          name: endpoint.name,
          request: {
            method: endpoint.method,
            header: this.generatePostmanHeaders(endpoint),
            url: {
              raw: `{{base_url}}${endpoint.url.replace(config.baseUrl, '')}`,
              host: ['{{base_url}}'],
              path: endpoint.url.replace(config.baseUrl, '').split('/').filter(p => p)
            },
            auth: endpoint.auth ? {
              type: 'bearer',
              bearer: [
                {
                  key: 'token',
                  value: '{{auth_token}}',
                  type: 'string'
                }
              ]
            } : undefined
          },
          response: []
        }

        if (endpoint.body && endpoint.method !== 'GET') {
          request.request.body = {
            mode: 'raw',
            raw: JSON.stringify(endpoint.body, null, 2),
            options: {
              raw: {
                language: 'json'
              }
            }
          }
        }

        folder.item.push(request)
      })

      collection.item.push(folder)
    })

    return collection
  }

  /**
   * Generate TypeScript client SDK
   */
  static generateTypeScriptSDK(
    config: APIDocumentationConfig,
    endpoints: APIEndpoint[],
    schemas: DatabaseSchema[]
  ): string {
    let sdk = `/**\n * ${config.title} TypeScript SDK\n * Version: ${config.version}\n */\n\n`

    // Types
    sdk += '// Types\n'
    schemas.forEach(schema => {
      const schemaName = schema.tableName.charAt(0).toUpperCase() + schema.tableName.slice(1)
      sdk += `export interface ${schemaName} {\n`
      schema.columns.forEach(column => {
        const tsType = this.mapToTypeScriptType(column.type)
        const optional = column.nullable ? '?' : ''
        sdk += `  ${column.name}${optional}: ${tsType};\n`
      })
      sdk += '}\n\n'

      // Create type (without ID)
      sdk += `export interface ${schemaName}Create {\n`
      schema.columns.filter(col => !col.primaryKey).forEach(column => {
        const tsType = this.mapToTypeScriptType(column.type)
        const optional = column.nullable ? '?' : ''
        sdk += `  ${column.name}${optional}: ${tsType};\n`
      })
      sdk += '}\n\n'
    })

    // API Response types
    sdk += 'export interface APIResponse<T> {\n'
    sdk += '  data: T;\n'
    sdk += '  status: number;\n'
    sdk += '  message?: string;\n'
    sdk += '}\n\n'

    sdk += 'export interface APIError {\n'
    sdk += '  message: string;\n'
    sdk += '  status: number;\n'
    sdk += '  errors?: Record<string, string[]>;\n'
    sdk += '}\n\n'

    // Client class
    sdk += `export class ${config.title.replace(/\s+/g, '')}Client {\n`
    sdk += '  private baseURL: string;\n'
    sdk += '  private token?: string;\n\n'

    sdk += '  constructor(baseURL: string, token?: string) {\n'
    sdk += '    this.baseURL = baseURL;\n'
    sdk += '    this.token = token;\n'
    sdk += '  }\n\n'

    sdk += '  setToken(token: string): void {\n'
    sdk += '    this.token = token;\n'
    sdk += '  }\n\n'

    sdk += '  private async request<T>(endpoint: string, options: RequestInit = {}): Promise<APIResponse<T>> {\n'
    sdk += '    const url = `${this.baseURL}${endpoint}`;\n'
    sdk += '    const headers: Record<string, string> = {\n'
    sdk += "      'Content-Type': 'application/json',\n"
    sdk += '      ...options.headers as Record<string, string>\n'
    sdk += '    };\n\n'

    sdk += '    if (this.token) {\n'
    sdk += "      headers['Authorization'] = `Bearer ${this.token}`;\n"
    sdk += '    }\n\n'

    sdk += '    const response = await fetch(url, {\n'
    sdk += '      ...options,\n'
    sdk += '      headers\n'
    sdk += '    });\n\n'

    sdk += '    const data = await response.json();\n\n'

    sdk += '    if (!response.ok) {\n'
    sdk += '      throw { message: data.message || "Request failed", status: response.status, errors: data.errors } as APIError;\n'
    sdk += '    }\n\n'

    sdk += '    return { data, status: response.status, message: data.message };\n'
    sdk += '  }\n\n'

    // Generate methods for each schema
    schemas.forEach(schema => {
      const resourceName = schema.tableName
      const className = schema.tableName.charAt(0).toUpperCase() + schema.tableName.slice(1)

      // Get all
      sdk += `  async get${className}s(): Promise<APIResponse<${className}[]>> {\n`
      sdk += `    return this.request<${className}[]>('/${resourceName}');\n`
      sdk += '  }\n\n'

      // Get by ID
      sdk += `  async get${className}(id: number): Promise<APIResponse<${className}>> {\n`
      sdk += `    return this.request<${className}>(\`/${resourceName}/\${id}\`);\n`
      sdk += '  }\n\n'

      // Create
      sdk += `  async create${className}(data: ${className}Create): Promise<APIResponse<${className}>> {\n`
      sdk += `    return this.request<${className}>('/${resourceName}', {\n`
      sdk += "      method: 'POST',\n"
      sdk += '      body: JSON.stringify(data)\n'
      sdk += '    });\n'
      sdk += '  }\n\n'

      // Update
      sdk += `  async update${className}(id: number, data: Partial<${className}Create>): Promise<APIResponse<${className}>> {\n`
      sdk += `    return this.request<${className}>(\`/${resourceName}/\${id}\`, {\n`
      sdk += "      method: 'PUT',\n"
      sdk += '      body: JSON.stringify(data)\n'
      sdk += '    });\n'
      sdk += '  }\n\n'

      // Delete
      sdk += `  async delete${className}(id: number): Promise<APIResponse<void>> {\n`
      sdk += `    return this.request<void>(\`/${resourceName}/\${id}\`, {\n`
      sdk += "      method: 'DELETE'\n"
      sdk += '    });\n'
      sdk += '  }\n\n'
    })

    sdk += '}\n\n'

    // Export default instance
    sdk += `export default ${config.title.replace(/\s+/g, '')}Client;\n`

    return sdk
  }

  // Helper methods
  private static mapToOpenAPIType(type: string): string {
    switch (type) {
      case 'string':
      case 'text':
        return 'string'
      case 'number':
        return 'integer'
      case 'boolean':
        return 'boolean'
      case 'date':
        return 'string'
      case 'json':
        return 'object'
      default:
        return 'string'
    }
  }

  private static mapToTypeScriptType(type: string): string {
    switch (type) {
      case 'string':
      case 'text':
        return 'string'
      case 'number':
        return 'number'
      case 'boolean':
        return 'boolean'
      case 'date':
        return 'Date'
      case 'json':
        return 'any'
      default:
        return 'string'
    }
  }

  private static groupEndpointsByResource(endpoints: APIEndpoint[]): Record<string, APIEndpoint[]> {
    const grouped: Record<string, APIEndpoint[]> = {}
    
    endpoints.forEach(endpoint => {
      const pathParts = endpoint.url.split('/').filter(p => p)
      const resource = pathParts[pathParts.length - 1]?.replace(/\{.*\}/, '') || 'general'
      
      if (!grouped[resource]) {
        grouped[resource] = []
      }
      grouped[resource].push(endpoint)
    })

    return grouped
  }

  private static generateParameters(endpoint: APIEndpoint): any[] {
    const parameters = []

    if (endpoint.params) {
      Object.entries(endpoint.params).forEach(([name, value]) => {
        parameters.push({
          name,
          in: 'query',
          required: true,
          schema: {
            type: typeof value === 'number' ? 'integer' : 'string'
          }
        })
      })
    }

    // Extract path parameters
    const pathParams = endpoint.url.match(/\{([^}]+)\}/g)
    if (pathParams) {
      pathParams.forEach(param => {
        const paramName = param.slice(1, -1)
        parameters.push({
          name: paramName,
          in: 'path',
          required: true,
          schema: {
            type: 'string'
          }
        })
      })
    }

    return parameters
  }

  private static generateRequestBody(endpoint: APIEndpoint): any {
    if (endpoint.method === 'GET' || !endpoint.body) {
      return undefined
    }

    return {
      required: true,
      content: {
        'application/json': {
          schema: {
            type: 'object'
          }
        }
      }
    }
  }

  private static generateResponses(endpoint: APIEndpoint): any {
    return {
      '200': {
        description: 'Success',
        content: {
          'application/json': {
            schema: {
              type: 'object'
            }
          }
        }
      },
      '400': {
        description: 'Bad Request'
      },
      '401': {
        description: 'Unauthorized'
      },
      '404': {
        description: 'Not Found'
      },
      '500': {
        description: 'Internal Server Error'
      }
    }
  }

  private static generateSampleResponse(resource: string, isArray: boolean): string {
    const sampleObject = {
      id: 1,
      name: 'Sample Name',
      email: 'sample@example.com',
      createdAt: '2023-01-01T00:00:00Z',
      updatedAt: '2023-01-01T00:00:00Z'
    }

    if (isArray) {
      return JSON.stringify([sampleObject], null, 2)
    }

    return JSON.stringify(sampleObject, null, 2)
  }

  private static generateCurlExample(endpoint: APIEndpoint, baseUrl: string): string {
    let curl = `curl -X ${endpoint.method} "${baseUrl}${endpoint.url}"`

    if (endpoint.auth) {
      curl += ' \\\n  -H "Authorization: Bearer YOUR_TOKEN"'
    }

    curl += ' \\\n  -H "Content-Type: application/json"'

    if (endpoint.body && endpoint.method !== 'GET') {
      curl += ' \\\n  -d \'' + JSON.stringify(endpoint.body) + '\''
    }

    return curl
  }

  private static generatePostmanHeaders(endpoint: APIEndpoint): any[] {
    const headers = [
      {
        key: 'Content-Type',
        value: 'application/json'
      }
    ]

    if (endpoint.headers) {
      Object.entries(endpoint.headers).forEach(([key, value]) => {
        headers.push({ key, value })
      })
    }

    return headers
  }
}