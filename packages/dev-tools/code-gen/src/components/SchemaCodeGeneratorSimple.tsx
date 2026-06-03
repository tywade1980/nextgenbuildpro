/**
 * Simple Schema Code Generator Component - Generate projects from database schemas
 */

import { useState } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Badge } from '@/components/ui/badge'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Database, Code2, Zap, FileText, Package } from '@phosphor-icons/react'
import { toast } from 'sonner'

interface SchemaCodeGeneratorProps {
  onCreateProject: (project: any) => void
}

interface DatabaseField {
  name: string
  type: string
  required: boolean
  primaryKey?: boolean
  foreignKey?: string
}

interface DatabaseTable {
  name: string
  fields: DatabaseField[]
}

export function SchemaCodeGenerator({ onCreateProject }: SchemaCodeGeneratorProps) {
  const [projectName, setProjectName] = useState('')
  const [projectDescription, setProjectDescription] = useState('')
  const [framework, setFramework] = useState<'express' | 'fastapi' | 'nestjs'>('express')
  const [database, setDatabase] = useState<'postgresql' | 'mysql' | 'mongodb'>('postgresql')
  const [schemaText, setSchemaText] = useState('')
  const [tables, setTables] = useState<DatabaseTable[]>([])
  const [isGenerating, setIsGenerating] = useState(false)

  const parseSchema = () => {
    try {
      const lines = schemaText.split('\n').filter(line => line.trim())
      const parsedTables: DatabaseTable[] = []
      let currentTable: DatabaseTable | null = null

      for (const line of lines) {
        const trimmed = line.trim()
        
        // Table definition
        if (trimmed.startsWith('table ') || trimmed.startsWith('CREATE TABLE')) {
          const tableName = trimmed.replace(/table\s+|CREATE TABLE\s+/i, '').replace(/[{(].*/, '').trim()
          currentTable = { name: tableName, fields: [] }
          parsedTables.push(currentTable)
        }
        // Field definition
        else if (currentTable && trimmed.includes(':') || trimmed.includes(' ')) {
          const parts = trimmed.split(/[:\s]+/)
          if (parts.length >= 2) {
            const field: DatabaseField = {
              name: parts[0].replace(/[,;]/, ''),
              type: parts[1].replace(/[,;]/, ''),
              required: trimmed.includes('NOT NULL') || trimmed.includes('required'),
              primaryKey: trimmed.includes('PRIMARY KEY') || trimmed.includes('pk'),
              foreignKey: trimmed.includes('REFERENCES') ? 'foreign' : undefined
            }
            currentTable.fields.push(field)
          }
        }
      }

      setTables(parsedTables)
      toast.success(`Parsed ${parsedTables.length} tables`)
    } catch (error) {
      toast.error('Failed to parse schema')
    }
  }

  const generateProject = async () => {
    if (!projectName || tables.length === 0) {
      toast.error('Please provide project name and valid schema')
      return
    }

    setIsGenerating(true)
    
    try {
      // Generate code structure based on schema
      const generatedFiles: Record<string, string> = {}
      
      // Package.json
      generatedFiles['package.json'] = JSON.stringify({
        name: projectName.toLowerCase().replace(/\s+/g, '-'),
        version: '1.0.0',
        description: projectDescription,
        main: 'src/index.js',
        scripts: {
          start: 'node src/index.js',
          dev: 'nodemon src/index.js',
          test: 'jest'
        },
        dependencies: {
          express: '^4.18.2',
          cors: '^2.8.5',
          dotenv: '^16.0.3',
          ...(database === 'postgresql' && { pg: '^8.8.0' }),
          ...(database === 'mysql' && { mysql2: '^3.2.0' }),
          ...(database === 'mongodb' && { mongoose: '^6.10.0' })
        },
        devDependencies: {
          nodemon: '^2.0.20',
          jest: '^29.4.0'
        }
      }, null, 2)

      // Main server file
      generatedFiles['src/index.js'] = `const express = require('express');
const cors = require('cors');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(express.json());

// Routes
${tables.map(table => `const ${table.name}Routes = require('./routes/${table.name}');
app.use('/api/${table.name.toLowerCase()}', ${table.name}Routes);`).join('\n')}

app.get('/', (req, res) => {
  res.json({ message: '${projectName} API is running!' });
});

app.listen(PORT, () => {
  console.log(\`Server running on port \${PORT}\`);
});

module.exports = app;`

      // Generate routes for each table
      tables.forEach(table => {
        generatedFiles[`src/routes/${table.name}.js`] = `const express = require('express');
const router = express.Router();
const ${table.name}Controller = require('../controllers/${table.name}Controller');

// CRUD routes
router.get('/', ${table.name}Controller.getAll);
router.get('/:id', ${table.name}Controller.getById);
router.post('/', ${table.name}Controller.create);
router.put('/:id', ${table.name}Controller.update);
router.delete('/:id', ${table.name}Controller.delete);

module.exports = router;`

        // Generate controllers
        generatedFiles[`src/controllers/${table.name}Controller.js`] = `const ${table.name}Model = require('../models/${table.name}');

class ${table.name}Controller {
  static async getAll(req, res) {
    try {
      const items = await ${table.name}Model.findAll();
      res.json(items);
    } catch (error) {
      res.status(500).json({ error: error.message });
    }
  }

  static async getById(req, res) {
    try {
      const item = await ${table.name}Model.findById(req.params.id);
      if (!item) {
        return res.status(404).json({ error: '${table.name} not found' });
      }
      res.json(item);
    } catch (error) {
      res.status(500).json({ error: error.message });
    }
  }

  static async create(req, res) {
    try {
      const item = await ${table.name}Model.create(req.body);
      res.status(201).json(item);
    } catch (error) {
      res.status(400).json({ error: error.message });
    }
  }

  static async update(req, res) {
    try {
      const item = await ${table.name}Model.update(req.params.id, req.body);
      if (!item) {
        return res.status(404).json({ error: '${table.name} not found' });
      }
      res.json(item);
    } catch (error) {
      res.status(400).json({ error: error.message });
    }
  }

  static async delete(req, res) {
    try {
      const deleted = await ${table.name}Model.delete(req.params.id);
      if (!deleted) {
        return res.status(404).json({ error: '${table.name} not found' });
      }
      res.status(204).send();
    } catch (error) {
      res.status(500).json({ error: error.message });
    }
  }
}

module.exports = ${table.name}Controller;`

        // Generate models
        generatedFiles[`src/models/${table.name}.js`] = `class ${table.name} {
  constructor(data) {
${table.fields.map(field => `    this.${field.name} = data.${field.name};`).join('\n')}
  }

  static async findAll() {
    // Implement database query
    return [];
  }

  static async findById(id) {
    // Implement database query
    return null;
  }

  static async create(data) {
    // Implement database insert
    return new ${table.name}(data);
  }

  static async update(id, data) {
    // Implement database update
    return null;
  }

  static async delete(id) {
    // Implement database delete
    return false;
  }

  validate() {
    const errors = [];
${table.fields.filter(f => f.required).map(field => 
  `    if (!this.${field.name}) errors.push('${field.name} is required');`
).join('\n')}
    return errors;
  }
}

module.exports = ${table.name};`
      })

      // Environment file
      generatedFiles['.env'] = `PORT=3000
NODE_ENV=development
${database === 'postgresql' ? 'DATABASE_URL=postgresql://username:password@localhost:5432/database' : ''}
${database === 'mysql' ? 'DATABASE_URL=mysql://username:password@localhost:3306/database' : ''}
${database === 'mongodb' ? 'MONGODB_URI=mongodb://localhost:27017/database' : ''}
JWT_SECRET=your-secret-key`

      // README
      generatedFiles['README.md'] = `# ${projectName}

${projectDescription}

## Generated from Schema

This project was automatically generated from a database schema.

### Tables:
${tables.map(table => `- **${table.name}**: ${table.fields.length} fields`).join('\n')}

### Setup

1. Install dependencies:
   \`\`\`
   npm install
   \`\`\`

2. Set up environment variables in \`.env\`

3. Start the server:
   \`\`\`
   npm run dev
   \`\`\`

### API Endpoints

${tables.map(table => `#### ${table.name}
- GET /api/${table.name.toLowerCase()} - Get all ${table.name.toLowerCase()}
- GET /api/${table.name.toLowerCase()}/:id - Get ${table.name.toLowerCase()} by ID
- POST /api/${table.name.toLowerCase()} - Create new ${table.name.toLowerCase()}
- PUT /api/${table.name.toLowerCase()}/:id - Update ${table.name.toLowerCase()}
- DELETE /api/${table.name.toLowerCase()}/:id - Delete ${table.name.toLowerCase()}`).join('\n\n')}
`

      const project = {
        name: projectName,
        description: projectDescription,
        type: framework as any,
        status: 'development' as const,
        codebase: {
          files: generatedFiles,
          dependencies: Object.keys(JSON.parse(generatedFiles['package.json']).dependencies || {})
        }
      }

      onCreateProject(project)
      toast.success('Backend project generated from schema!')
      
      // Reset form
      setProjectName('')
      setProjectDescription('')
      setSchemaText('')
      setTables([])
      
    } catch (error) {
      toast.error('Failed to generate project')
      console.error(error)
    } finally {
      setIsGenerating(false)
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <Database className="w-8 h-8 text-primary" />
        <div>
          <h2 className="text-2xl font-bold">Schema Code Generator</h2>
          <p className="text-muted-foreground">Generate backend projects from database schemas</p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Code2 className="w-5 h-5" />
              Project Configuration
            </CardTitle>
            <CardDescription>
              Configure your backend project settings
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="project-name">Project Name</Label>
              <Input
                id="project-name"
                value={projectName}
                onChange={(e) => setProjectName(e.target.value)}
                placeholder="My Backend API"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="project-description">Description</Label>
              <Textarea
                id="project-description"
                value={projectDescription}
                onChange={(e) => setProjectDescription(e.target.value)}
                placeholder="Generated backend API from database schema"
                rows={3}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="framework">Backend Framework</Label>
              <Select value={framework} onValueChange={(value: any) => setFramework(value)}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="express">Express.js</SelectItem>
                  <SelectItem value="fastapi">FastAPI</SelectItem>
                  <SelectItem value="nestjs">NestJS</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="database">Database</Label>
              <Select value={database} onValueChange={(value: any) => setDatabase(value)}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="postgresql">PostgreSQL</SelectItem>
                  <SelectItem value="mysql">MySQL</SelectItem>
                  <SelectItem value="mongodb">MongoDB</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <FileText className="w-5 h-5" />
              Database Schema
            </CardTitle>
            <CardDescription>
              Enter your database schema (SQL, JSON, or simple format)
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <Textarea
              value={schemaText}
              onChange={(e) => setSchemaText(e.target.value)}
              placeholder={`Example schema:

table User {
  id: integer pk
  name: string required
  email: string required
  created_at: timestamp
}

table Post {
  id: integer pk
  title: string required
  content: text
  user_id: integer fk
  published: boolean
}`}
              rows={12}
              className="font-mono text-sm"
            />
            
            <div className="flex gap-2">
              <Button onClick={parseSchema} variant="outline" size="sm">
                <Zap className="w-4 h-4 mr-2" />
                Parse Schema
              </Button>
              <Badge variant="secondary">{tables.length} tables parsed</Badge>
            </div>
          </CardContent>
        </Card>
      </div>

      {tables.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Package className="w-5 h-5" />
              Parsed Tables ({tables.length})
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {tables.map((table, index) => (
                <Card key={index} className="border-dashed">
                  <CardHeader className="pb-2">
                    <CardTitle className="text-lg">{table.name}</CardTitle>
                    <CardDescription>{table.fields.length} fields</CardDescription>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-1">
                      {table.fields.slice(0, 4).map((field, i) => (
                        <div key={i} className="flex items-center justify-between text-sm">
                          <span className={field.primaryKey ? 'font-semibold' : ''}>
                            {field.name}
                          </span>
                          <div className="flex gap-1">
                            <Badge variant="outline" className="text-xs">
                              {field.type}
                            </Badge>
                            {field.required && (
                              <Badge variant="secondary" className="text-xs">req</Badge>
                            )}
                            {field.primaryKey && (
                              <Badge variant="default" className="text-xs">pk</Badge>
                            )}
                          </div>
                        </div>
                      ))}
                      {table.fields.length > 4 && (
                        <div className="text-xs text-muted-foreground">
                          +{table.fields.length - 4} more fields
                        </div>
                      )}
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          {isGenerating && (
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <div className="w-4 h-4 border-2 border-primary border-t-transparent rounded-full animate-spin" />
              Generating backend code...
            </div>
          )}
        </div>
        
        <Button 
          onClick={generateProject}
          disabled={!projectName || tables.length === 0 || isGenerating}
          size="lg"
        >
          <Code2 className="w-4 h-4 mr-2" />
          Generate Backend Project
        </Button>
      </div>

      {tables.length === 0 && schemaText && (
        <Alert>
          <AlertTriangle className="w-4 h-4" />
          <AlertDescription>
            Click "Parse Schema" to analyze your database schema and generate code.
          </AlertDescription>
        </Alert>
      )}
    </div>
  )
}