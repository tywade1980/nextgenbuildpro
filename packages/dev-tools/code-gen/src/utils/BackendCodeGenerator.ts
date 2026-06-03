/**
 * Backend Code Generator - Generates backend API code and database schemas
 */

import { DatabaseSchema } from '@/services/DatabaseService'
import { APIEndpoint } from '@/services/APIService'

export interface BackendTemplate {
  id: string
  name: string
  description: string
  framework: 'express' | 'fastify' | 'koa' | 'nestjs' | 'django' | 'fastapi' | 'spring' | 'gin'
  database: 'postgresql' | 'mysql' | 'mongodb' | 'sqlite'
  auth: 'jwt' | 'oauth' | 'session' | 'none'
  features: ('crud' | 'auth' | 'realtime' | 'fileupload' | 'email' | 'payments')[]
}

export class BackendCodeGenerator {
  
  /**
   * Generate Express.js backend code
   */
  static generateExpressBackend(
    schemas: DatabaseSchema[],
    endpoints: APIEndpoint[],
    config: {
      database: string
      auth: string
      features: string[]
    }
  ): { files: Record<string, string>; dependencies: string[] } {
    const files: Record<string, string> = {}
    const dependencies = [
      'express',
      'cors',
      'helmet',
      'morgan',
      'dotenv',
      'express-rate-limit'
    ]

    // Package.json
    files['package.json'] = JSON.stringify({
      name: 'backend-api',
      version: '1.0.0',
      description: 'Generated backend API',
      main: 'src/index.js',
      scripts: {
        start: 'node src/index.js',
        dev: 'nodemon src/index.js',
        test: 'jest'
      },
      dependencies: this.getExpressDependencies(config, dependencies),
      devDependencies: {
        nodemon: '^2.0.22',
        jest: '^29.5.0',
        supertest: '^6.3.3'
      }
    }, null, 2)

    // Main server file
    files['src/index.js'] = this.generateExpressServer(config)

    // Database configuration
    files['src/config/database.js'] = this.generateDatabaseConfig(config.database)

    // Models
    schemas.forEach(schema => {
      files[`src/models/${schema.tableName}.js`] = this.generateExpressModel(schema, config.database)
    })

    // Routes
    schemas.forEach(schema => {
      files[`src/routes/${schema.tableName}.js`] = this.generateExpressRoutes(schema)
    })

    // Middleware
    files['src/middleware/auth.js'] = this.generateAuthMiddleware(config.auth)
    files['src/middleware/validation.js'] = this.generateValidationMiddleware()
    files['src/middleware/errorHandler.js'] = this.generateErrorHandler()

    // Controllers
    schemas.forEach(schema => {
      files[`src/controllers/${schema.tableName}Controller.js`] = this.generateExpressController(schema)
    })

    // Services
    schemas.forEach(schema => {
      files[`src/services/${schema.tableName}Service.js`] = this.generateExpressService(schema)
    })

    // Tests
    schemas.forEach(schema => {
      files[`src/tests/${schema.tableName}.test.js`] = this.generateExpressTests(schema)
    })

    // Environment config
    files['.env.example'] = this.generateEnvExample(config)
    files['.gitignore'] = this.generateGitignore()
    files['README.md'] = this.generateReadme(schemas, endpoints)

    // Docker support
    files['Dockerfile'] = this.generateDockerfile()
    files['docker-compose.yml'] = this.generateDockerCompose(config.database)

    return { files, dependencies }
  }

  /**
   * Generate FastAPI Python backend
   */
  static generateFastAPIBackend(
    schemas: DatabaseSchema[],
    endpoints: APIEndpoint[],
    config: any
  ): { files: Record<string, string>; dependencies: string[] } {
    const files: Record<string, string> = {}
    const dependencies = [
      'fastapi',
      'uvicorn',
      'sqlalchemy',
      'pydantic',
      'python-multipart',
      'python-jose',
      'passlib',
      'bcrypt'
    ]

    // Requirements.txt
    files['requirements.txt'] = this.getFastAPIDependencies(config, dependencies).join('\n')

    // Main app file
    files['main.py'] = this.generateFastAPIApp(config)

    // Database models
    files['models/__init__.py'] = ''
    schemas.forEach(schema => {
      files[`models/${schema.tableName}.py`] = this.generateFastAPIModel(schema)
    })

    // Pydantic schemas
    files['schemas/__init__.py'] = ''
    schemas.forEach(schema => {
      files[`schemas/${schema.tableName}.py`] = this.generatePydanticSchema(schema)
    })

    // API routes
    files['routers/__init__.py'] = ''
    schemas.forEach(schema => {
      files[`routers/${schema.tableName}.py`] = this.generateFastAPIRoutes(schema)
    })

    // Database connection
    files['database.py'] = this.generateFastAPIDatabase(config.database)

    // Authentication
    files['auth.py'] = this.generateFastAPIAuth(config.auth)

    return { files, dependencies }
  }

  /**
   * Generate NestJS TypeScript backend
   */
  static generateNestJSBackend(
    schemas: DatabaseSchema[],
    endpoints: APIEndpoint[],
    config: any
  ): { files: Record<string, string>; dependencies: string[] } {
    const files: Record<string, string> = {}
    const dependencies = [
      '@nestjs/core',
      '@nestjs/common',
      '@nestjs/platform-express',
      '@nestjs/typeorm',
      'typeorm',
      'reflect-metadata',
      'rxjs'
    ]

    // Package.json
    files['package.json'] = JSON.stringify({
      name: 'nestjs-backend',
      version: '1.0.0',
      description: 'Generated NestJS backend',
      main: 'dist/main.js',
      scripts: {
        build: 'nest build',
        start: 'nest start',
        'start:dev': 'nest start --watch',
        'start:prod': 'node dist/main'
      },
      dependencies: this.getNestJSDependencies(config, dependencies),
      devDependencies: {
        '@nestjs/cli': '^10.0.0',
        '@nestjs/schematics': '^10.0.0',
        '@nestjs/testing': '^10.0.0',
        typescript: '^5.0.0'
      }
    }, null, 2)

    // Main application file
    files['src/main.ts'] = this.generateNestJSMain()

    // App module
    files['src/app.module.ts'] = this.generateNestJSAppModule(schemas)

    // Entities
    schemas.forEach(schema => {
      files[`src/entities/${schema.tableName}.entity.ts`] = this.generateNestJSEntity(schema)
    })

    // DTOs
    schemas.forEach(schema => {
      files[`src/dto/${schema.tableName}.dto.ts`] = this.generateNestJSDTO(schema)
    })

    // Controllers
    schemas.forEach(schema => {
      files[`src/controllers/${schema.tableName}.controller.ts`] = this.generateNestJSController(schema)
    })

    // Services
    schemas.forEach(schema => {
      files[`src/services/${schema.tableName}.service.ts`] = this.generateNestJSService(schema)
    })

    // Modules
    schemas.forEach(schema => {
      files[`src/modules/${schema.tableName}.module.ts`] = this.generateNestJSModule(schema)
    })

    return { files, dependencies }
  }

  // Express.js helper methods
  private static generateExpressServer(config: any): string {
    return `
const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');
const rateLimit = require('express-rate-limit');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(helmet());
app.use(cors());
app.use(morgan('combined'));
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true }));

// Rate limiting
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100 // limit each IP to 100 requests per windowMs
});
app.use(limiter);

// Database connection
const db = require('./config/database');
db.connect();

// Routes
${config.schemas ? config.schemas.map((schema: any) => 
  `app.use('/api/${schema.tableName}', require('./routes/${schema.tableName}'));`
).join('\n') : ''}

// Error handling middleware
app.use(require('./middleware/errorHandler'));

// Health check
app.get('/health', (req, res) => {
  res.json({ status: 'OK', timestamp: new Date().toISOString() });
});

app.listen(PORT, () => {
  console.log(\`Server running on port \${PORT}\`);
});

module.exports = app;
`.trim()
  }

  private static generateDatabaseConfig(database: string): string {
    switch (database) {
      case 'postgresql':
        return `
const { Pool } = require('pg');

const pool = new Pool({
  user: process.env.DB_USER,
  host: process.env.DB_HOST,
  database: process.env.DB_NAME,
  password: process.env.DB_PASSWORD,
  port: process.env.DB_PORT || 5432,
});

module.exports = {
  connect: async () => {
    try {
      await pool.connect();
      console.log('Connected to PostgreSQL database');
    } catch (error) {
      console.error('Database connection error:', error);
      process.exit(1);
    }
  },
  query: (text, params) => pool.query(text, params),
  end: () => pool.end()
};
`.trim()

      case 'mysql':
        return `
const mysql = require('mysql2/promise');

const pool = mysql.createPool({
  host: process.env.DB_HOST,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
  database: process.env.DB_NAME,
  port: process.env.DB_PORT || 3306,
  waitForConnections: true,
  connectionLimit: 10,
  queueLimit: 0
});

module.exports = {
  connect: async () => {
    try {
      await pool.getConnection();
      console.log('Connected to MySQL database');
    } catch (error) {
      console.error('Database connection error:', error);
      process.exit(1);
    }
  },
  query: async (sql, params) => {
    const [rows] = await pool.execute(sql, params);
    return { rows };
  },
  end: () => pool.end()
};
`.trim()

      default:
        return '// Database configuration placeholder'
    }
  }

  private static generateExpressModel(schema: DatabaseSchema, database: string): string {
    const className = schema.tableName.charAt(0).toUpperCase() + schema.tableName.slice(1)
    
    return `
const db = require('../config/database');

class ${className} {
  constructor(data) {
    ${schema.columns.map(col => `this.${col.name} = data.${col.name};`).join('\n    ')}
  }

  static async findAll() {
    const query = 'SELECT * FROM ${schema.tableName}';
    const result = await db.query(query);
    return result.rows.map(row => new ${className}(row));
  }

  static async findById(id) {
    const query = 'SELECT * FROM ${schema.tableName} WHERE id = $1';
    const result = await db.query(query, [id]);
    return result.rows[0] ? new ${className}(result.rows[0]) : null;
  }

  async save() {
    const columns = [${schema.columns.map(col => `'${col.name}'`).join(', ')}];
    const values = [${schema.columns.map(col => `this.${col.name}`).join(', ')}];
    const placeholders = values.map((_, index) => \`$\${index + 1}\`).join(', ');
    
    const query = \`INSERT INTO ${schema.tableName} (\${columns.join(', ')}) VALUES (\${placeholders}) RETURNING *\`;
    const result = await db.query(query, values);
    return new ${className}(result.rows[0]);
  }

  async update() {
    const setParts = [${schema.columns.filter(col => !col.primaryKey).map(col => `'${col.name} = $' + (${schema.columns.filter(c => !c.primaryKey).indexOf(col) + 1})`).join(', ')}];
    const values = [${schema.columns.filter(col => !col.primaryKey).map(col => `this.${col.name}`).join(', ')}, this.id];
    
    const query = \`UPDATE ${schema.tableName} SET \${setParts.join(', ')} WHERE id = $\${values.length} RETURNING *\`;
    const result = await db.query(query, values);
    return new ${className}(result.rows[0]);
  }

  static async delete(id) {
    const query = 'DELETE FROM ${schema.tableName} WHERE id = $1';
    const result = await db.query(query, [id]);
    return result.rowCount > 0;
  }
}

module.exports = ${className};
`.trim()
  }

  private static generateExpressRoutes(schema: DatabaseSchema): string {
    const modelName = schema.tableName.charAt(0).toUpperCase() + schema.tableName.slice(1)
    
    return `
const express = require('express');
const router = express.Router();
const ${modelName}Controller = require('../controllers/${schema.tableName}Controller');
const auth = require('../middleware/auth');
const validation = require('../middleware/validation');

// GET all ${schema.tableName}
router.get('/', ${modelName}Controller.getAll);

// GET ${schema.tableName} by ID
router.get('/:id', ${modelName}Controller.getById);

// POST create new ${schema.tableName}
router.post('/', auth, validation.validate${modelName}, ${modelName}Controller.create);

// PUT update ${schema.tableName}
router.put('/:id', auth, validation.validate${modelName}, ${modelName}Controller.update);

// DELETE ${schema.tableName}
router.delete('/:id', auth, ${modelName}Controller.delete);

module.exports = router;
`.trim()
  }

  private static generateExpressController(schema: DatabaseSchema): string {
    const modelName = schema.tableName.charAt(0).toUpperCase() + schema.tableName.slice(1)
    
    return `
const ${modelName}Service = require('../services/${schema.tableName}Service');

class ${modelName}Controller {
  static async getAll(req, res, next) {
    try {
      const items = await ${modelName}Service.getAll();
      res.json(items);
    } catch (error) {
      next(error);
    }
  }

  static async getById(req, res, next) {
    try {
      const item = await ${modelName}Service.getById(req.params.id);
      if (!item) {
        return res.status(404).json({ error: '${modelName} not found' });
      }
      res.json(item);
    } catch (error) {
      next(error);
    }
  }

  static async create(req, res, next) {
    try {
      const item = await ${modelName}Service.create(req.body);
      res.status(201).json(item);
    } catch (error) {
      next(error);
    }
  }

  static async update(req, res, next) {
    try {
      const item = await ${modelName}Service.update(req.params.id, req.body);
      if (!item) {
        return res.status(404).json({ error: '${modelName} not found' });
      }
      res.json(item);
    } catch (error) {
      next(error);
    }
  }

  static async delete(req, res, next) {
    try {
      const deleted = await ${modelName}Service.delete(req.params.id);
      if (!deleted) {
        return res.status(404).json({ error: '${modelName} not found' });
      }
      res.status(204).send();
    } catch (error) {
      next(error);
    }
  }
}

module.exports = ${modelName}Controller;
`.trim()
  }

  private static generateExpressService(schema: DatabaseSchema): string {
    const modelName = schema.tableName.charAt(0).toUpperCase() + schema.tableName.slice(1)
    
    return `
const ${modelName} = require('../models/${schema.tableName}');

class ${modelName}Service {
  static async getAll() {
    return await ${modelName}.findAll();
  }

  static async getById(id) {
    return await ${modelName}.findById(id);
  }

  static async create(data) {
    const item = new ${modelName}(data);
    return await item.save();
  }

  static async update(id, data) {
    const item = await ${modelName}.findById(id);
    if (!item) {
      return null;
    }
    
    Object.assign(item, data);
    return await item.update();
  }

  static async delete(id) {
    return await ${modelName}.delete(id);
  }
}

module.exports = ${modelName}Service;
`.trim()
  }

  private static generateAuthMiddleware(authType: string): string {
    if (authType === 'jwt') {
      return `
const jwt = require('jsonwebtoken');

const auth = (req, res, next) => {
  const token = req.header('Authorization')?.replace('Bearer ', '');
  
  if (!token) {
    return res.status(401).json({ error: 'Access denied. No token provided.' });
  }

  try {
    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    req.user = decoded;
    next();
  } catch (error) {
    res.status(400).json({ error: 'Invalid token.' });
  }
};

module.exports = auth;
`.trim()
    }
    
    return `
// Auth middleware placeholder
const auth = (req, res, next) => {
  // Implement your authentication logic here
  next();
};

module.exports = auth;
`.trim()
  }

  private static generateValidationMiddleware(): string {
    return `
const validation = {
  // Add validation functions for each model
};

module.exports = validation;
`.trim()
  }

  private static generateErrorHandler(): string {
    return `
const errorHandler = (error, req, res, next) => {
  console.error('Error:', error);

  if (error.name === 'ValidationError') {
    return res.status(400).json({
      error: 'Validation Error',
      details: error.message
    });
  }

  if (error.name === 'CastError') {
    return res.status(400).json({
      error: 'Invalid ID format'
    });
  }

  res.status(500).json({
    error: 'Internal Server Error'
  });
};

module.exports = errorHandler;
`.trim()
  }

  private static generateExpressTests(schema: DatabaseSchema): string {
    return `
const request = require('supertest');
const app = require('../index');

describe('${schema.tableName} API', () => {
  test('GET /${schema.tableName} should return all items', async () => {
    const response = await request(app)
      .get('/api/${schema.tableName}')
      .expect(200);
    
    expect(Array.isArray(response.body)).toBe(true);
  });

  test('POST /${schema.tableName} should create new item', async () => {
    const newItem = {
      ${schema.columns.filter(col => !col.primaryKey).slice(0, 2).map(col => 
        `${col.name}: '${col.type === 'string' ? 'test value' : col.type === 'number' ? '123' : 'true'}'`
      ).join(',\n      ')}
    };

    const response = await request(app)
      .post('/api/${schema.tableName}')
      .send(newItem)
      .expect(201);
    
    expect(response.body).toHaveProperty('id');
  });
});
`.trim()
  }

  private static generateEnvExample(config: any): string {
    return `
# Database Configuration
DB_HOST=localhost
DB_PORT=${config.database === 'postgresql' ? '5432' : '3306'}
DB_NAME=your_database
DB_USER=your_username
DB_PASSWORD=your_password

# JWT Secret (if using JWT auth)
JWT_SECRET=your_super_secret_jwt_key

# Server Configuration
PORT=3000
NODE_ENV=development

# API Keys (if needed)
API_KEY=your_api_key
`.trim()
  }

  private static generateGitignore(): string {
    return `
node_modules/
.env
.env.local
.env.production
logs/
*.log
npm-debug.log*
yarn-debug.log*
yarn-error.log*
.DS_Store
coverage/
.nyc_output/
dist/
build/
`.trim()
  }

  private static generateReadme(schemas: DatabaseSchema[], endpoints: APIEndpoint[]): string {
    return `
# Backend API

Generated backend API with the following features:

## Database Tables
${schemas.map(schema => `- ${schema.tableName} (${schema.columns.length} columns)`).join('\n')}

## API Endpoints
${endpoints.map(endpoint => `- ${endpoint.method} ${endpoint.url} - ${endpoint.name}`).join('\n')}

## Getting Started

1. Install dependencies:
   \`\`\`bash
   npm install
   \`\`\`

2. Copy environment variables:
   \`\`\`bash
   cp .env.example .env
   \`\`\`

3. Update the .env file with your database credentials

4. Run the development server:
   \`\`\`bash
   npm run dev
   \`\`\`

## API Documentation

The API follows REST conventions:

- GET /api/{resource} - Get all items
- GET /api/{resource}/:id - Get item by ID
- POST /api/{resource} - Create new item
- PUT /api/{resource}/:id - Update item
- DELETE /api/{resource}/:id - Delete item

## Testing

Run tests with:
\`\`\`bash
npm test
\`\`\`
`.trim()
  }

  private static generateDockerfile(): string {
    return `
FROM node:18-alpine

WORKDIR /app

COPY package*.json ./
RUN npm ci --only=production

COPY . .

EXPOSE 3000

CMD ["npm", "start"]
`.trim()
  }

  private static generateDockerCompose(database: string): string {
    const dbService = database === 'postgresql' 
      ? `
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: your_database
      POSTGRES_USER: your_username
      POSTGRES_PASSWORD: your_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data`
      : `
  mysql:
    image: mysql:8
    environment:
      MYSQL_DATABASE: your_database
      MYSQL_USER: your_username
      MYSQL_PASSWORD: your_password
      MYSQL_ROOT_PASSWORD: root_password
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql`

    return `
version: '3.8'

services:
  app:
    build: .
    ports:
      - "3000:3000"
    environment:
      - NODE_ENV=production
    depends_on:
      - ${database === 'postgresql' ? 'postgres' : 'mysql'}
${dbService}

volumes:
  ${database === 'postgresql' ? 'postgres' : 'mysql'}_data:
`.trim()
  }

  // Dependency helper methods
  private static getExpressDependencies(config: any, base: string[]): Record<string, string> {
    const deps: Record<string, string> = {}
    
    base.forEach(dep => {
      deps[dep] = 'latest'
    })

    if (config.database === 'postgresql') {
      deps['pg'] = 'latest'
    } else if (config.database === 'mysql') {
      deps['mysql2'] = 'latest'
    }

    if (config.auth === 'jwt') {
      deps['jsonwebtoken'] = 'latest'
      deps['bcryptjs'] = 'latest'
    }

    if (config.features.includes('fileupload')) {
      deps['multer'] = 'latest'
    }

    if (config.features.includes('email')) {
      deps['nodemailer'] = 'latest'
    }

    return deps
  }

  private static getFastAPIDependencies(config: any, base: string[]): string[] {
    const deps = [...base]

    if (config.database === 'postgresql') {
      deps.push('psycopg2-binary')
    } else if (config.database === 'mysql') {
      deps.push('pymysql')
    }

    return deps
  }

  private static getNestJSDependencies(config: any, base: string[]): Record<string, string> {
    const deps: Record<string, string> = {}
    
    base.forEach(dep => {
      deps[dep] = 'latest'
    })

    if (config.database === 'postgresql') {
      deps['pg'] = 'latest'
      deps['@types/pg'] = 'latest'
    }

    return deps
  }

  // FastAPI generators (simplified)
  private static generateFastAPIApp(config: any): string {
    return `
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI(title="Generated API", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/")
async def root():
    return {"message": "API is running"}

@app.get("/health")
async def health_check():
    return {"status": "OK"}
`.trim()
  }

  private static generateFastAPIModel(schema: DatabaseSchema): string {
    return `
from sqlalchemy import Column, Integer, String, Boolean, DateTime
from sqlalchemy.ext.declarative import declarative_base

Base = declarative_base()

class ${schema.tableName.charAt(0).toUpperCase() + schema.tableName.slice(1)}(Base):
    __tablename__ = "${schema.tableName}"
    
    ${schema.columns.map(col => {
      const sqlType = col.type === 'string' ? 'String' : col.type === 'number' ? 'Integer' : 'String'
      return `${col.name} = Column(${sqlType}${col.primaryKey ? ', primary_key=True' : ''})`
    }).join('\n    ')}
`.trim()
  }

  private static generatePydanticSchema(schema: DatabaseSchema): string {
    return `
from pydantic import BaseModel
from typing import Optional

class ${schema.tableName.charAt(0).toUpperCase() + schema.tableName.slice(1)}Base(BaseModel):
    ${schema.columns.filter(col => !col.primaryKey).map(col => {
      const pyType = col.type === 'string' ? 'str' : col.type === 'number' ? 'int' : 'str'
      return `${col.name}: ${col.nullable ? `Optional[${pyType}]` : pyType}`
    }).join('\n    ')}

class ${schema.tableName.charAt(0).toUpperCase() + schema.tableName.slice(1)}Create(${schema.tableName.charAt(0).toUpperCase() + schema.tableName.slice(1)}Base):
    pass

class ${schema.tableName.charAt(0).toUpperCase() + schema.tableName.slice(1)}(${schema.tableName.charAt(0).toUpperCase() + schema.tableName.slice(1)}Base):
    id: int
    
    class Config:
        orm_mode = True
`.trim()
  }

  private static generateFastAPIRoutes(schema: DatabaseSchema): string {
    return `
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List

router = APIRouter(prefix="/${schema.tableName}", tags=["${schema.tableName}"])

@router.get("/", response_model=List[${schema.tableName.charAt(0).toUpperCase() + schema.tableName.slice(1)}])
def get_${schema.tableName}():
    # Implementation here
    return []

@router.post("/", response_model=${schema.tableName.charAt(0).toUpperCase() + schema.tableName.slice(1)})
def create_${schema.tableName.slice(0, -1)}():
    # Implementation here
    pass
`.trim()
  }

  private static generateFastAPIDatabase(database: string): string {
    return `
from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
import os

DATABASE_URL = os.getenv("DATABASE_URL", "sqlite:///./test.db")

engine = create_engine(DATABASE_URL)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base = declarative_base()

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
`.trim()
  }

  private static generateFastAPIAuth(authType: string): string {
    return `
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials

security = HTTPBearer()

def verify_token(credentials: HTTPAuthorizationCredentials = Depends(security)):
    # Implement token verification
    if not credentials.credentials:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid authentication credentials",
            headers={"WWW-Authenticate": "Bearer"},
        )
    return credentials.credentials
`.trim()
  }

  // NestJS generators (simplified)
  private static generateNestJSMain(): string {
    return `
import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);
  app.enableCors();
  await app.listen(3000);
}
bootstrap();
`.trim()
  }

  private static generateNestJSAppModule(schemas: DatabaseSchema[]): string {
    return `
import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
${schemas.map(schema => `import { ${schema.tableName.charAt(0).toUpperCase() + schema.tableName.slice(1)}Module } from './modules/${schema.tableName}.module';`).join('\n')}

@Module({
  imports: [
    TypeOrmModule.forRoot({
      type: 'postgres',
      host: process.env.DB_HOST,
      port: parseInt(process.env.DB_PORT) || 5432,
      username: process.env.DB_USER,
      password: process.env.DB_PASSWORD,
      database: process.env.DB_NAME,
      autoLoadEntities: true,
      synchronize: true,
    }),
    ${schemas.map(schema => `${schema.tableName.charAt(0).toUpperCase() + schema.tableName.slice(1)}Module`).join(',\n    ')}
  ],
})
export class AppModule {}
`.trim()
  }

  private static generateNestJSEntity(schema: DatabaseSchema): string {
    return `
import { Entity, Column, PrimaryGeneratedColumn } from 'typeorm';

@Entity('${schema.tableName}')
export class ${schema.tableName.charAt(0).toUpperCase() + schema.tableName.slice(1)} {
  @PrimaryGeneratedColumn()
  id: number;

  ${schema.columns.filter(col => !col.primaryKey).map(col => {
    const tsType = col.type === 'string' ? 'string' : col.type === 'number' ? 'number' : 'string'
    return `@Column(${col.nullable ? '{ nullable: true }' : ''})\n  ${col.name}: ${tsType};`
  }).join('\n\n  ')}
}
`.trim()
  }

  private static generateNestJSDTO(schema: DatabaseSchema): string {
    return `
export class Create${schema.tableName.charAt(0).toUpperCase() + schema.tableName.slice(1)}Dto {
  ${schema.columns.filter(col => !col.primaryKey).map(col => {
    const tsType = col.type === 'string' ? 'string' : col.type === 'number' ? 'number' : 'string'
    return `${col.name}: ${tsType};`
  }).join('\n  ')}
}

export class Update${schema.tableName.charAt(0).toUpperCase() + schema.tableName.slice(1)}Dto {
  ${schema.columns.filter(col => !col.primaryKey).map(col => {
    const tsType = col.type === 'string' ? 'string' : col.type === 'number' ? 'number' : 'string'
    return `${col.name}?: ${tsType};`
  }).join('\n  ')}
}
`.trim()
  }

  private static generateNestJSController(schema: DatabaseSchema): string {
    const className = schema.tableName.charAt(0).toUpperCase() + schema.tableName.slice(1)
    return `
import { Controller, Get, Post, Body, Patch, Param, Delete } from '@nestjs/common';
import { ${className}Service } from '../services/${schema.tableName}.service';
import { Create${className}Dto, Update${className}Dto } from '../dto/${schema.tableName}.dto';

@Controller('${schema.tableName}')
export class ${className}Controller {
  constructor(private readonly ${schema.tableName}Service: ${className}Service) {}

  @Post()
  create(@Body() create${className}Dto: Create${className}Dto) {
    return this.${schema.tableName}Service.create(create${className}Dto);
  }

  @Get()
  findAll() {
    return this.${schema.tableName}Service.findAll();
  }

  @Get(':id')
  findOne(@Param('id') id: string) {
    return this.${schema.tableName}Service.findOne(+id);
  }

  @Patch(':id')
  update(@Param('id') id: string, @Body() update${className}Dto: Update${className}Dto) {
    return this.${schema.tableName}Service.update(+id, update${className}Dto);
  }

  @Delete(':id')
  remove(@Param('id') id: string) {
    return this.${schema.tableName}Service.remove(+id);
  }
}
`.trim()
  }

  private static generateNestJSService(schema: DatabaseSchema): string {
    const className = schema.tableName.charAt(0).toUpperCase() + schema.tableName.slice(1)
    return `
import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { ${className} } from '../entities/${schema.tableName}.entity';
import { Create${className}Dto, Update${className}Dto } from '../dto/${schema.tableName}.dto';

@Injectable()
export class ${className}Service {
  constructor(
    @InjectRepository(${className})
    private ${schema.tableName}Repository: Repository<${className}>,
  ) {}

  create(create${className}Dto: Create${className}Dto) {
    return this.${schema.tableName}Repository.save(create${className}Dto);
  }

  findAll() {
    return this.${schema.tableName}Repository.find();
  }

  findOne(id: number) {
    return this.${schema.tableName}Repository.findOne({ where: { id } });
  }

  update(id: number, update${className}Dto: Update${className}Dto) {
    return this.${schema.tableName}Repository.update(id, update${className}Dto);
  }

  remove(id: number) {
    return this.${schema.tableName}Repository.delete(id);
  }
}
`.trim()
  }

  private static generateNestJSModule(schema: DatabaseSchema): string {
    const className = schema.tableName.charAt(0).toUpperCase() + schema.tableName.slice(1)
    return `
import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { ${className}Service } from '../services/${schema.tableName}.service';
import { ${className}Controller } from '../controllers/${schema.tableName}.controller';
import { ${className} } from '../entities/${schema.tableName}.entity';

@Module({
  imports: [TypeOrmModule.forFeature([${className}])],
  controllers: [${className}Controller],
  providers: [${className}Service],
})
export class ${className}Module {}
`.trim()
  }
}