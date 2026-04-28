/**
 * Backend Code Generator - Generates complete backend code from database schemas
 */

import { DatabaseSchema } from './DatabaseService'

export interface BackendFramework {
  name: 'express' | 'fastify' | 'nestjs' | 'koa' | 'django' | 'flask' | 'spring-boot' | 'laravel'
  language: 'javascript' | 'typescript' | 'python' | 'java' | 'php'
}

export interface GeneratedBackendCode {
  framework: BackendFramework
  schemas: DatabaseSchema[]
  files: {
    [filePath: string]: string
  }
  dependencies: string[]
  dockerConfig?: string
  readme: string
}

export interface CodeGenerationOptions {
  framework: BackendFramework
  features: {
    authentication?: boolean
    authorization?: boolean
    validation?: boolean
    logging?: boolean
    cors?: boolean
    swagger?: boolean
    testing?: boolean
    dockerization?: boolean
    errorHandling?: boolean
    pagination?: boolean
    caching?: boolean
    fileUpload?: boolean
  }
  database: {
    provider: string
    url?: string
  }
  apiPrefix?: string
  port?: number
}

export class BackendCodeGenerator {
  
  /**
   * Generate complete backend code from schemas
   */
  static generateBackend(
    schemas: DatabaseSchema[], 
    options: CodeGenerationOptions
  ): GeneratedBackendCode {
    const generator = new BackendCodeGenerator()
    
    switch (options.framework.name) {
      case 'express':
        return generator.generateExpressBackend(schemas, options)
      case 'fastify':
        return generator.generateFastifyBackend(schemas, options)
      case 'nestjs':
        return generator.generateNestJSBackend(schemas, options)
      case 'django':
        return generator.generateDjangoBackend(schemas, options)
      case 'flask':
        return generator.generateFlaskBackend(schemas, options)
      case 'spring-boot':
        return generator.generateSpringBootBackend(schemas, options)
      case 'laravel':
        return generator.generateLaravelBackend(schemas, options)
      default:
        throw new Error(`Unsupported framework: ${options.framework.name}`)
    }
  }

  /**
   * Generate Express.js backend
   */
  private generateExpressBackend(schemas: DatabaseSchema[], options: CodeGenerationOptions): GeneratedBackendCode {
    const files: { [key: string]: string } = {}
    const dependencies = this.getExpressDependencies(options)

    // Package.json
    files['package.json'] = this.generatePackageJson('express-backend', dependencies, options)

    // Main server file
    files['src/app.js'] = this.generateExpressApp(schemas, options)
    files['src/server.js'] = this.generateExpressServer(options)

    // Database configuration
    files['src/config/database.js'] = this.generateDatabaseConfig(options.database)

    // Models
    schemas.forEach(schema => {
      files[`src/models/${schema.tableName}.js`] = this.generateExpressModel(schema, options)
    })

    // Controllers
    schemas.forEach(schema => {
      files[`src/controllers/${schema.tableName}Controller.js`] = this.generateExpressController(schema, options)
    })

    // Routes
    schemas.forEach(schema => {
      files[`src/routes/${schema.tableName}Routes.js`] = this.generateExpressRoutes(schema, options)
    })
    files['src/routes/index.js'] = this.generateExpressRouteIndex(schemas, options)

    // Middleware
    if (options.features.authentication) {
      files['src/middleware/auth.js'] = this.generateAuthMiddleware(options)
    }
    if (options.features.validation) {
      files['src/middleware/validation.js'] = this.generateValidationMiddleware(options)
    }
    if (options.features.errorHandling) {
      files['src/middleware/errorHandler.js'] = this.generateErrorHandlerMiddleware(options)
    }

    // Services
    schemas.forEach(schema => {
      files[`src/services/${schema.tableName}Service.js`] = this.generateExpressService(schema, options)
    })

    // Utilities
    files['src/utils/logger.js'] = this.generateLogger(options)
    files['src/utils/response.js'] = this.generateResponseHelper(options)

    // Environment configuration
    files['.env.example'] = this.generateEnvExample(options)

    // Docker configuration
    if (options.features.dockerization) {
      files['Dockerfile'] = this.generateDockerfile(options)
      files['docker-compose.yml'] = this.generateDockerCompose(options)
    }

    // Testing
    if (options.features.testing) {
      schemas.forEach(schema => {
        files[`tests/${schema.tableName}.test.js`] = this.generateExpressTests(schema, options)
      })
      files['tests/setup.js'] = this.generateTestSetup(options)
    }

    // Swagger documentation
    if (options.features.swagger) {
      files['src/docs/swagger.js'] = this.generateSwaggerConfig(schemas, options)
    }

    // Migration files
    schemas.forEach((schema, index) => {
      const timestamp = Date.now() + index
      files[`src/migrations/${timestamp}_create_${schema.tableName}.js`] = this.generateMigration(schema, options)
    })

    return {
      framework: options.framework,
      schemas,
      files,
      dependencies,
      dockerConfig: options.features.dockerization ? files['docker-compose.yml'] : undefined,
      readme: this.generateReadme(schemas, options)
    }
  }

  /**
   * Generate NestJS backend
   */
  private generateNestJSBackend(schemas: DatabaseSchema[], options: CodeGenerationOptions): GeneratedBackendCode {
    const files: { [key: string]: string } = {}
    const dependencies = this.getNestJSDependencies(options)

    // Package.json and tsconfig
    files['package.json'] = this.generatePackageJson('nestjs-backend', dependencies, options)
    files['tsconfig.json'] = this.generateTsConfig()
    files['nest-cli.json'] = this.generateNestCliConfig()

    // Main application files
    files['src/main.ts'] = this.generateNestJSMain(options)
    files['src/app.module.ts'] = this.generateNestJSAppModule(schemas, options)

    // Database module
    files['src/database/database.module.ts'] = this.generateNestJSDatabaseModule(options)

    // Entities (TypeORM)
    schemas.forEach(schema => {
      files[`src/entities/${schema.tableName}.entity.ts`] = this.generateNestJSEntity(schema, options)
    })

    // DTOs
    schemas.forEach(schema => {
      files[`src/dto/create-${schema.tableName}.dto.ts`] = this.generateNestJSCreateDTO(schema, options)
      files[`src/dto/update-${schema.tableName}.dto.ts`] = this.generateNestJSUpdateDTO(schema, options)
    })

    // Services
    schemas.forEach(schema => {
      files[`src/${schema.tableName}/${schema.tableName}.service.ts`] = this.generateNestJSService(schema, options)
    })

    // Controllers
    schemas.forEach(schema => {
      files[`src/${schema.tableName}/${schema.tableName}.controller.ts`] = this.generateNestJSController(schema, options)
    })

    // Modules
    schemas.forEach(schema => {
      files[`src/${schema.tableName}/${schema.tableName}.module.ts`] = this.generateNestJSModule(schema, options)
    })

    // Guards and decorators
    if (options.features.authentication) {
      files['src/auth/auth.guard.ts'] = this.generateNestJSAuthGuard(options)
      files['src/auth/auth.module.ts'] = this.generateNestJSAuthModule(options)
      files['src/decorators/auth.decorator.ts'] = this.generateNestJSAuthDecorator(options)
    }

    return {
      framework: options.framework,
      schemas,
      files,
      dependencies,
      readme: this.generateReadme(schemas, options)
    }
  }

  /**
   * Generate Django backend
   */
  private generateDjangoBackend(schemas: DatabaseSchema[], options: CodeGenerationOptions): GeneratedBackendCode {
    const files: { [key: string]: string } = {}
    const dependencies = this.getDjangoDependencies(options)

    // Requirements and settings
    files['requirements.txt'] = dependencies.join('\n')
    files['manage.py'] = this.generateDjangoManage()
    
    // Django settings
    files['backend/settings.py'] = this.generateDjangoSettings(options)
    files['backend/urls.py'] = this.generateDjangoUrls(schemas, options)
    files['backend/wsgi.py'] = this.generateDjangoWSGI()

    // App structure
    const appName = 'api'
    files[`${appName}/__init__.py`] = ''
    files[`${appName}/apps.py`] = this.generateDjangoApps(appName)
    files[`${appName}/admin.py`] = this.generateDjangoAdmin(schemas)
    files[`${appName}/urls.py`] = this.generateDjangoAppUrls(schemas, options)

    // Models
    files[`${appName}/models.py`] = this.generateDjangoModels(schemas, options)

    // Serializers
    files[`${appName}/serializers.py`] = this.generateDjangoSerializers(schemas, options)

    // Views
    files[`${appName}/views.py`] = this.generateDjangoViews(schemas, options)

    // Migrations
    files[`${appName}/migrations/__init__.py`] = ''
    schemas.forEach((schema, index) => {
      files[`${appName}/migrations/000${index + 1}_initial_${schema.tableName}.py`] = 
        this.generateDjangoMigration(schema, options)
    })

    return {
      framework: options.framework,
      schemas,
      files,
      dependencies,
      readme: this.generateReadme(schemas, options)
    }
  }

  /**
   * Generate Flask backend
   */
  private generateFlaskBackend(schemas: DatabaseSchema[], options: CodeGenerationOptions): GeneratedBackendCode {
    const files: { [key: string]: string } = {}
    const dependencies = this.getFlaskDependencies(options)

    files['requirements.txt'] = dependencies.join('\n')
    files['app.py'] = this.generateFlaskApp(schemas, options)
    files['config.py'] = this.generateFlaskConfig(options)

    // Models
    files['models.py'] = this.generateFlaskModels(schemas, options)

    // Resources/Views
    schemas.forEach(schema => {
      files[`resources/${schema.tableName}_resource.py`] = this.generateFlaskResource(schema, options)
    })

    return {
      framework: options.framework,
      schemas,
      files,
      dependencies,
      readme: this.generateReadme(schemas, options)
    }
  }

  /**
   * Generate Spring Boot backend
   */
  private generateSpringBootBackend(schemas: DatabaseSchema[], options: CodeGenerationOptions): GeneratedBackendCode {
    const files: { [key: string]: string } = {}
    const dependencies = this.getSpringBootDependencies(options)

    // Maven configuration
    files['pom.xml'] = this.generateSpringBootPom(dependencies, options)
    
    // Application properties
    files['src/main/resources/application.yml'] = this.generateSpringBootProperties(options)

    // Main application class
    files['src/main/java/com/example/backend/BackendApplication.java'] = this.generateSpringBootMain()

    // Entities
    schemas.forEach(schema => {
      files[`src/main/java/com/example/backend/entity/${this.capitalize(schema.tableName)}.java`] = 
        this.generateSpringBootEntity(schema, options)
    })

    // Repositories
    schemas.forEach(schema => {
      files[`src/main/java/com/example/backend/repository/${this.capitalize(schema.tableName)}Repository.java`] = 
        this.generateSpringBootRepository(schema, options)
    })

    // Services
    schemas.forEach(schema => {
      files[`src/main/java/com/example/backend/service/${this.capitalize(schema.tableName)}Service.java`] = 
        this.generateSpringBootService(schema, options)
    })

    // Controllers
    schemas.forEach(schema => {
      files[`src/main/java/com/example/backend/controller/${this.capitalize(schema.tableName)}Controller.java`] = 
        this.generateSpringBootController(schema, options)
    })

    return {
      framework: options.framework,
      schemas,
      files,
      dependencies,
      readme: this.generateReadme(schemas, options)
    }
  }

  /**
   * Generate Laravel backend
   */
  private generateLaravelBackend(schemas: DatabaseSchema[], options: CodeGenerationOptions): GeneratedBackendCode {
    const files: { [key: string]: string } = {}
    const dependencies = this.getLaravelDependencies(options)

    // Composer configuration
    files['composer.json'] = this.generateLaravelComposer(dependencies, options)

    // Models
    schemas.forEach(schema => {
      files[`app/Models/${this.capitalize(schema.tableName)}.php`] = this.generateLaravelModel(schema, options)
    })

    // Controllers
    schemas.forEach(schema => {
      files[`app/Http/Controllers/${this.capitalize(schema.tableName)}Controller.php`] = 
        this.generateLaravelController(schema, options)
    })

    // Migrations
    schemas.forEach((schema, index) => {
      const timestamp = new Date().toISOString().replace(/[-:]/g, '').split('.')[0]
      files[`database/migrations/${timestamp}_create_${schema.tableName}_table.php`] = 
        this.generateLaravelMigration(schema, options)
    })

    // Routes
    files['routes/api.php'] = this.generateLaravelRoutes(schemas, options)

    return {
      framework: options.framework,
      schemas,
      files,
      dependencies,
      readme: this.generateReadme(schemas, options)
    }
  }

  // Utility methods for code generation

  private generateFastifyBackend(schemas: DatabaseSchema[], options: CodeGenerationOptions): GeneratedBackendCode {
    // Similar to Express but with Fastify-specific patterns
    const files: { [key: string]: string } = {}
    const dependencies = this.getFastifyDependencies(options)

    files['package.json'] = this.generatePackageJson('fastify-backend', dependencies, options)
    files['src/app.js'] = this.generateFastifyApp(schemas, options)
    
    return {
      framework: options.framework,
      schemas,
      files,
      dependencies,
      readme: this.generateReadme(schemas, options)
    }
  }

  // Helper methods for different frameworks

  private getExpressDependencies(options: CodeGenerationOptions): string[] {
    const deps = ['express', 'dotenv', 'cors']
    
    if (options.database.provider === 'postgresql') deps.push('pg')
    if (options.database.provider === 'mysql') deps.push('mysql2')
    if (options.database.provider === 'mongodb') deps.push('mongoose')
    if (options.features.validation) deps.push('joi', 'express-validator')
    if (options.features.authentication) deps.push('jsonwebtoken', 'bcryptjs')
    if (options.features.swagger) deps.push('swagger-ui-express', 'swagger-jsdoc')
    if (options.features.testing) deps.push('jest', 'supertest')
    if (options.features.logging) deps.push('winston')

    return deps
  }

  private getNestJSDependencies(options: CodeGenerationOptions): string[] {
    const deps = [
      '@nestjs/core', '@nestjs/common', '@nestjs/platform-express',
      '@nestjs/typeorm', 'typeorm', 'reflect-metadata', 'rxjs'
    ]

    if (options.database.provider === 'postgresql') deps.push('pg')
    if (options.database.provider === 'mysql') deps.push('mysql2')
    if (options.features.authentication) deps.push('@nestjs/jwt', '@nestjs/passport', 'passport', 'passport-jwt')
    if (options.features.swagger) deps.push('@nestjs/swagger', 'swagger-ui-express')
    if (options.features.validation) deps.push('class-validator', 'class-transformer')

    return deps
  }

  private getDjangoDependencies(options: CodeGenerationOptions): string[] {
    const deps = ['Django>=4.2.0', 'djangorestframework']

    if (options.database.provider === 'postgresql') deps.push('psycopg2-binary')
    if (options.database.provider === 'mysql') deps.push('mysqlclient')
    if (options.features.cors) deps.push('django-cors-headers')
    if (options.features.authentication) deps.push('djangorestframework-simplejwt')

    return deps
  }

  private getFlaskDependencies(options: CodeGenerationOptions): string[] {
    const deps = ['Flask', 'Flask-RESTful', 'Flask-SQLAlchemy']

    if (options.database.provider === 'postgresql') deps.push('psycopg2-binary')
    if (options.database.provider === 'mysql') deps.push('PyMySQL')
    if (options.features.cors) deps.push('Flask-CORS')
    if (options.features.authentication) deps.push('Flask-JWT-Extended')

    return deps
  }

  private getSpringBootDependencies(options: CodeGenerationOptions): string[] {
    const deps = ['spring-boot-starter-web', 'spring-boot-starter-data-jpa']

    if (options.database.provider === 'postgresql') deps.push('postgresql')
    if (options.database.provider === 'mysql') deps.push('mysql-connector-java')
    if (options.features.validation) deps.push('spring-boot-starter-validation')
    if (options.features.authentication) deps.push('spring-boot-starter-security')

    return deps
  }

  private getLaravelDependencies(options: CodeGenerationOptions): string[] {
    return ['laravel/framework', 'guzzlehttp/guzzle']
  }

  private getFastifyDependencies(options: CodeGenerationOptions): string[] {
    const deps = ['fastify', '@fastify/cors', 'dotenv']
    
    if (options.database.provider === 'postgresql') deps.push('pg')
    if (options.features.swagger) deps.push('@fastify/swagger')
    if (options.features.authentication) deps.push('@fastify/jwt')
    
    return deps
  }

  private generatePackageJson(name: string, dependencies: string[], options: CodeGenerationOptions): string {
    return JSON.stringify({
      name,
      version: "1.0.0",
      description: "Generated backend API",
      main: options.framework.name === 'nestjs' ? 'dist/main.js' : 'src/server.js',
      scripts: {
        start: options.framework.name === 'nestjs' ? 'node dist/main' : 'node src/server.js',
        dev: options.framework.name === 'nestjs' ? 'nest start --watch' : 'nodemon src/server.js',
        build: options.framework.name === 'nestjs' ? 'nest build' : '',
        test: options.features.testing ? 'jest' : 'echo "No tests specified"'
      },
      dependencies: dependencies.reduce((acc, dep) => {
        acc[dep] = 'latest'
        return acc
      }, {} as Record<string, string>),
      devDependencies: {
        nodemon: 'latest',
        ...(options.framework.language === 'typescript' ? {
          typescript: 'latest',
          '@types/node': 'latest'
        } : {})
      }
    }, null, 2)
  }

  private generateExpressApp(schemas: DatabaseSchema[], options: CodeGenerationOptions): string {
    return `const express = require('express')
const cors = require('cors')
const routes = require('./routes')
${options.features.errorHandling ? "const errorHandler = require('./middleware/errorHandler')" : ''}

const app = express()

// Middleware
app.use(cors())
app.use(express.json())
app.use(express.urlencoded({ extended: true }))

// Routes
app.use('${options.apiPrefix || '/api'}', routes)

// Error handling
${options.features.errorHandling ? 'app.use(errorHandler)' : ''}

module.exports = app`
  }

  private generateExpressServer(options: CodeGenerationOptions): string {
    return `require('dotenv').config()
const app = require('./app')

const PORT = process.env.PORT || ${options.port || 3000}

app.listen(PORT, () => {
  console.log(\`Server running on port \${PORT}\`)
})`
  }

  private generateExpressModel(schema: DatabaseSchema, options: CodeGenerationOptions): string {
    const fields = schema.columns.map(col => 
      `  ${col.name}: { type: '${col.type}', required: ${!col.nullable} }`
    ).join(',\n')

    return `const db = require('../config/database')

class ${this.capitalize(schema.tableName)} {
  constructor(data) {
${schema.columns.map(col => `    this.${col.name} = data.${col.name}`).join('\n')}
  }

  static async findAll() {
    const query = 'SELECT * FROM ${schema.tableName}'
    const result = await db.query(query)
    return result.rows.map(row => new ${this.capitalize(schema.tableName)}(row))
  }

  static async findById(id) {
    const query = 'SELECT * FROM ${schema.tableName} WHERE id = $1'
    const result = await db.query(query, [id])
    return result.rows[0] ? new ${this.capitalize(schema.tableName)}(result.rows[0]) : null
  }

  async save() {
    const columns = [${schema.columns.map(col => `'${col.name}'`).join(', ')}]
    const values = [${schema.columns.map(col => `this.${col.name}`).join(', ')}]
    const placeholders = columns.map((_, i) => \`$\${i + 1}\`).join(', ')
    
    const query = \`INSERT INTO ${schema.tableName} (\${columns.join(', ')}) VALUES (\${placeholders}) RETURNING *\`
    const result = await db.query(query, values)
    return new ${this.capitalize(schema.tableName)}(result.rows[0])
  }

  async update() {
    const setClause = [${schema.columns.filter(col => !col.primaryKey).map(col => `'${col.name} = $' + (${schema.columns.filter(c => !c.primaryKey).indexOf(col) + 1})`).join(', ')}].join(', ')
    const values = [${schema.columns.filter(col => !col.primaryKey).map(col => `this.${col.name}`).join(', ')}, this.id]
    
    const query = \`UPDATE ${schema.tableName} SET \${setClause} WHERE id = $\${values.length} RETURNING *\`
    const result = await db.query(query, values)
    return new ${this.capitalize(schema.tableName)}(result.rows[0])
  }

  async delete() {
    const query = 'DELETE FROM ${schema.tableName} WHERE id = $1'
    await db.query(query, [this.id])
  }
}

module.exports = ${this.capitalize(schema.tableName)}`
  }

  private generateExpressController(schema: DatabaseSchema, options: CodeGenerationOptions): string {
    const modelName = this.capitalize(schema.tableName)
    
    return `const ${modelName} = require('../models/${schema.tableName}')
const { successResponse, errorResponse } = require('../utils/response')

class ${modelName}Controller {
  // GET /${schema.tableName}
  static async getAll(req, res) {
    try {
      const items = await ${modelName}.findAll()
      return successResponse(res, 'Retrieved successfully', items)
    } catch (error) {
      return errorResponse(res, 'Failed to retrieve ${schema.tableName}', error.message)
    }
  }

  // GET /${schema.tableName}/:id
  static async getById(req, res) {
    try {
      const item = await ${modelName}.findById(req.params.id)
      if (!item) {
        return errorResponse(res, '${modelName} not found', null, 404)
      }
      return successResponse(res, 'Retrieved successfully', item)
    } catch (error) {
      return errorResponse(res, 'Failed to retrieve ${schema.tableName}', error.message)
    }
  }

  // POST /${schema.tableName}
  static async create(req, res) {
    try {
      const item = new ${modelName}(req.body)
      const savedItem = await item.save()
      return successResponse(res, 'Created successfully', savedItem, 201)
    } catch (error) {
      return errorResponse(res, 'Failed to create ${schema.tableName}', error.message)
    }
  }

  // PUT /${schema.tableName}/:id
  static async update(req, res) {
    try {
      const item = await ${modelName}.findById(req.params.id)
      if (!item) {
        return errorResponse(res, '${modelName} not found', null, 404)
      }
      
      Object.assign(item, req.body)
      const updatedItem = await item.update()
      return successResponse(res, 'Updated successfully', updatedItem)
    } catch (error) {
      return errorResponse(res, 'Failed to update ${schema.tableName}', error.message)
    }
  }

  // DELETE /${schema.tableName}/:id
  static async delete(req, res) {
    try {
      const item = await ${modelName}.findById(req.params.id)
      if (!item) {
        return errorResponse(res, '${modelName} not found', null, 404)
      }
      
      await item.delete()
      return successResponse(res, 'Deleted successfully')
    } catch (error) {
      return errorResponse(res, 'Failed to delete ${schema.tableName}', error.message)
    }
  }
}

module.exports = ${modelName}Controller`
  }

  private generateExpressRoutes(schema: DatabaseSchema, options: CodeGenerationOptions): string {
    const controllerName = `${this.capitalize(schema.tableName)}Controller`
    
    return `const express = require('express')
const ${controllerName} = require('../controllers/${schema.tableName}Controller')
${options.features.authentication ? "const auth = require('../middleware/auth')" : ''}
${options.features.validation ? "const validate = require('../middleware/validation')" : ''}

const router = express.Router()

// ${schema.tableName} routes
router.get('/', ${controllerName}.getAll)
router.get('/:id', ${controllerName}.getById)
router.post('/', ${options.features.authentication ? 'auth, ' : ''}${controllerName}.create)
router.put('/:id', ${options.features.authentication ? 'auth, ' : ''}${controllerName}.update)
router.delete('/:id', ${options.features.authentication ? 'auth, ' : ''}${controllerName}.delete)

module.exports = router`
  }

  private generateExpressRouteIndex(schemas: DatabaseSchema[], options: CodeGenerationOptions): string {
    const routes = schemas.map(schema => 
      `router.use('/${schema.tableName}', require('./${schema.tableName}Routes'))`
    ).join('\n')

    return `const express = require('express')
const router = express.Router()

// API Routes
${routes}

module.exports = router`
  }

  private generateDatabaseConfig(database: { provider: string; url?: string }): string {
    return `const { Pool } = require('pg')

const pool = new Pool({
  connectionString: process.env.DATABASE_URL || '${database.url || 'postgresql://localhost:5432/database'}',
  ssl: process.env.NODE_ENV === 'production' ? { rejectUnauthorized: false } : false
})

module.exports = {
  query: (text, params) => pool.query(text, params),
  pool
}`
  }

  private generateEnvExample(options: CodeGenerationOptions): string {
    return `# Server Configuration
PORT=${options.port || 3000}
NODE_ENV=development

# Database Configuration
DATABASE_URL=${options.database.url || 'postgresql://localhost:5432/database'}

# Authentication
JWT_SECRET=your-jwt-secret-key

# API Configuration
API_PREFIX=${options.apiPrefix || '/api'}`
  }

  private generateReadme(schemas: DatabaseSchema[], options: CodeGenerationOptions): string {
    const endpoints = schemas.map(schema => `
### ${this.capitalize(schema.tableName)} Endpoints

- \`GET ${options.apiPrefix || '/api'}/${schema.tableName}\` - Get all ${schema.tableName}
- \`GET ${options.apiPrefix || '/api'}/${schema.tableName}/:id\` - Get ${schema.tableName} by ID
- \`POST ${options.apiPrefix || '/api'}/${schema.tableName}\` - Create new ${schema.tableName}
- \`PUT ${options.apiPrefix || '/api'}/${schema.tableName}/:id\` - Update ${schema.tableName}
- \`DELETE ${options.apiPrefix || '/api'}/${schema.tableName}/:id\` - Delete ${schema.tableName}`
    ).join('\n')

    return `# Generated ${options.framework.name.toUpperCase()} Backend API

This backend API was automatically generated from your database schemas.

## Features

${Object.entries(options.features).filter(([_, enabled]) => enabled).map(([feature]) => `- ✅ ${feature}`).join('\n')}

## Installation

1. Install dependencies:
   \`\`\`bash
   ${options.framework.language === 'python' ? 'pip install -r requirements.txt' : 'npm install'}
   \`\`\`

2. Configure environment variables:
   \`\`\`bash
   cp .env.example .env
   # Edit .env with your configuration
   \`\`\`

3. Run database migrations:
   \`\`\`bash
   # Database migration commands
   \`\`\`

4. Start the server:
   \`\`\`bash
   ${options.framework.language === 'python' ? 'python manage.py runserver' : 'npm run dev'}
   \`\`\`

## API Endpoints

${endpoints}

## Database Schema

${schemas.map(schema => `
### ${schema.tableName}
${schema.columns.map(col => `- **${col.name}**: ${col.type}${col.primaryKey ? ' (Primary Key)' : ''}${!col.nullable ? ' (Required)' : ''}`).join('\n')}
`).join('\n')}

## Project Structure

\`\`\`
${this.generateProjectStructure(options.framework)}
\`\`\`

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

MIT License`
  }

  private generateProjectStructure(framework: BackendFramework): string {
    switch (framework.name) {
      case 'express':
        return `src/
├── controllers/     # Route controllers
├── models/         # Data models
├── routes/         # API routes
├── middleware/     # Custom middleware
├── services/       # Business logic
├── utils/          # Utility functions
├── config/         # Configuration files
├── migrations/     # Database migrations
└── app.js          # Express application`
      
      case 'nestjs':
        return `src/
├── entities/       # TypeORM entities
├── dto/           # Data transfer objects
├── auth/          # Authentication module
├── decorators/    # Custom decorators
├── guards/        # Route guards
└── main.ts        # Application entry point`
      
      case 'django':
        return `backend/
├── settings.py    # Django settings
├── urls.py        # URL configuration
├── wsgi.py        # WSGI configuration
api/
├── models.py      # Database models
├── views.py       # API views
├── serializers.py # Data serializers
├── urls.py        # API URLs
└── migrations/    # Database migrations`
      
      default:
        return 'Standard project structure'
    }
  }

  // Additional helper methods for specific frameworks...

  private generateNestJSMain(options: CodeGenerationOptions): string {
    return `import { NestFactory } from '@nestjs/core'
import { AppModule } from './app.module'
import { ValidationPipe } from '@nestjs/common'
${options.features.swagger ? "import { DocumentBuilder, SwaggerModule } from '@nestjs/swagger'" : ''}

async function bootstrap() {
  const app = await NestFactory.create(AppModule)
  
  app.useGlobalPipes(new ValidationPipe())
  app.enableCors()
  
  ${options.features.swagger ? `
  const config = new DocumentBuilder()
    .setTitle('Generated API')
    .setDescription('Auto-generated API documentation')
    .setVersion('1.0')
    .addBearerAuth()
    .build()
  const document = SwaggerModule.createDocument(app, config)
  SwaggerModule.setup('api/docs', app, document)
  ` : ''}
  
  await app.listen(${options.port || 3000})
}
bootstrap()`
  }

  private generateTsConfig(): string {
    return JSON.stringify({
      compilerOptions: {
        module: "commonjs",
        declaration: true,
        removeComments: true,
        emitDecoratorMetadata: true,
        experimentalDecorators: true,
        allowSyntheticDefaultImports: true,
        target: "es2017",
        sourceMap: true,
        outDir: "./dist",
        baseUrl: "./",
        incremental: true,
        skipLibCheck: true,
        strictNullChecks: false,
        noImplicitAny: false,
        strictBindCallApply: false,
        forceConsistentCasingInFileNames: false,
        noFallthroughCasesInSwitch: false
      }
    }, null, 2)
  }

  private capitalize(str: string): string {
    return str.charAt(0).toUpperCase() + str.slice(1)
  }

  // Additional generation methods would continue here...
  // Due to length constraints, I'm showing the core structure and key methods

  private generateNestJSEntity(schema: DatabaseSchema, options: CodeGenerationOptions): string {
    const imports = ['Entity', 'Column', 'PrimaryGeneratedColumn']
    const columns = schema.columns.map(col => {
      if (col.primaryKey) {
        return `  @PrimaryGeneratedColumn()
  ${col.name}: number`
      }
      return `  @Column(${col.nullable ? '{ nullable: true }' : ''})
  ${col.name}: ${this.mapTypeScriptType(col.type)}`
    }).join('\n\n')

    return `import { ${imports.join(', ')} } from 'typeorm'

@Entity('${schema.tableName}')
export class ${this.capitalize(schema.tableName)} {
${columns}
}`
  }

  private mapTypeScriptType(dbType: string): string {
    switch (dbType) {
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

  // Placeholder methods for other framework-specific generators
  private generateFastifyApp(schemas: DatabaseSchema[], options: CodeGenerationOptions): string { return '' }
  private generateNestJSAppModule(schemas: DatabaseSchema[], options: CodeGenerationOptions): string { return '' }
  private generateNestJSDatabaseModule(options: CodeGenerationOptions): string { return '' }
  private generateNestJSCreateDTO(schema: DatabaseSchema, options: CodeGenerationOptions): string { return '' }
  private generateNestJSUpdateDTO(schema: DatabaseSchema, options: CodeGenerationOptions): string { return '' }
  private generateNestJSService(schema: DatabaseSchema, options: CodeGenerationOptions): string { return '' }
  private generateNestJSController(schema: DatabaseSchema, options: CodeGenerationOptions): string { return '' }
  private generateNestJSModule(schema: DatabaseSchema, options: CodeGenerationOptions): string { return '' }
  private generateNestJSAuthGuard(options: CodeGenerationOptions): string { return '' }
  private generateNestJSAuthModule(options: CodeGenerationOptions): string { return '' }
  private generateNestJSAuthDecorator(options: CodeGenerationOptions): string { return '' }
  private generateNestCliConfig(): string { return '' }
  private generateDjangoManage(): string { return '' }
  private generateDjangoSettings(options: CodeGenerationOptions): string { return '' }
  private generateDjangoUrls(schemas: DatabaseSchema[], options: CodeGenerationOptions): string { return '' }
  private generateDjangoWSGI(): string { return '' }
  private generateDjangoApps(appName: string): string { return '' }
  private generateDjangoAdmin(schemas: DatabaseSchema[]): string { return '' }
  private generateDjangoAppUrls(schemas: DatabaseSchema[], options: CodeGenerationOptions): string { return '' }
  private generateDjangoModels(schemas: DatabaseSchema[], options: CodeGenerationOptions): string { return '' }
  private generateDjangoSerializers(schemas: DatabaseSchema[], options: CodeGenerationOptions): string { return '' }
  private generateDjangoViews(schemas: DatabaseSchema[], options: CodeGenerationOptions): string { return '' }
  private generateDjangoMigration(schema: DatabaseSchema, options: CodeGenerationOptions): string { return '' }
  private generateFlaskApp(schemas: DatabaseSchema[], options: CodeGenerationOptions): string { return '' }
  private generateFlaskConfig(options: CodeGenerationOptions): string { return '' }
  private generateFlaskModels(schemas: DatabaseSchema[], options: CodeGenerationOptions): string { return '' }
  private generateFlaskResource(schema: DatabaseSchema, options: CodeGenerationOptions): string { return '' }
  private generateSpringBootPom(dependencies: string[], options: CodeGenerationOptions): string { return '' }
  private generateSpringBootProperties(options: CodeGenerationOptions): string { return '' }
  private generateSpringBootMain(): string { return '' }
  private generateSpringBootEntity(schema: DatabaseSchema, options: CodeGenerationOptions): string { return '' }
  private generateSpringBootRepository(schema: DatabaseSchema, options: CodeGenerationOptions): string { return '' }
  private generateSpringBootService(schema: DatabaseSchema, options: CodeGenerationOptions): string { return '' }
  private generateSpringBootController(schema: DatabaseSchema, options: CodeGenerationOptions): string { return '' }
  private generateLaravelComposer(dependencies: string[], options: CodeGenerationOptions): string { return '' }
  private generateLaravelModel(schema: DatabaseSchema, options: CodeGenerationOptions): string { return '' }
  private generateLaravelController(schema: DatabaseSchema, options: CodeGenerationOptions): string { return '' }
  private generateLaravelMigration(schema: DatabaseSchema, options: CodeGenerationOptions): string { return '' }
  private generateLaravelRoutes(schemas: DatabaseSchema[], options: CodeGenerationOptions): string { return '' }
  private generateExpressService(schema: DatabaseSchema, options: CodeGenerationOptions): string { return '' }
  private generateLogger(options: CodeGenerationOptions): string { return '' }
  private generateResponseHelper(options: CodeGenerationOptions): string { return '' }
  private generateDockerfile(options: CodeGenerationOptions): string { return '' }
  private generateDockerCompose(options: CodeGenerationOptions): string { return '' }
  private generateExpressTests(schema: DatabaseSchema, options: CodeGenerationOptions): string { return '' }
  private generateTestSetup(options: CodeGenerationOptions): string { return '' }
  private generateSwaggerConfig(schemas: DatabaseSchema[], options: CodeGenerationOptions): string { return '' }
  private generateMigration(schema: DatabaseSchema, options: CodeGenerationOptions): string { return '' }
  private generateAuthMiddleware(options: CodeGenerationOptions): string { return '' }
  private generateValidationMiddleware(options: CodeGenerationOptions): string { return '' }
  private generateErrorHandlerMiddleware(options: CodeGenerationOptions): string { return '' }
}