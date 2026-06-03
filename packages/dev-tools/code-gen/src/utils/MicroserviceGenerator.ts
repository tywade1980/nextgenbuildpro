export interface MicroserviceConfig {
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

export class MicroserviceGenerator {
  async generateService(config: MicroserviceConfig): Promise<Record<string, string>> {
    const files: Record<string, string> = {}
    
    switch (config.framework) {
      case 'express':
        return this.generateExpressService(config)
      case 'fastapi':
        return this.generateFastAPIService(config)
      case 'spring-boot':
        return this.generateSpringBootService(config)
      case 'go-gin':
        return this.generateGoGinService(config)
      case 'nestjs':
        return this.generateNestJSService(config)
      case 'django':
        return this.generateDjangoService(config)
      default:
        return this.generateExpressService(config)
    }
  }

  private generateExpressService(config: MicroserviceConfig): Record<string, string> {
    const files: Record<string, string> = {}

    // package.json
    files['package.json'] = JSON.stringify({
      name: config.name,
      version: '1.0.0',
      description: config.description,
      main: 'src/index.js',
      scripts: {
        start: 'node src/index.js',
        dev: 'nodemon src/index.js',
        test: 'jest',
        'test:watch': 'jest --watch'
      },
      dependencies: {
        express: '^4.18.2',
        cors: '^2.8.5',
        helmet: '^7.0.0',
        'express-rate-limit': '^6.7.0',
        'express-validator': '^7.0.1',
        dotenv: '^16.3.1',
        winston: '^3.10.0',
        'express-prometheus-middleware': '^1.2.0',
        ...this.getDependenciesForType(config.type)
      },
      devDependencies: {
        nodemon: '^3.0.1',
        jest: '^29.6.1',
        supertest: '^6.3.3',
        '@types/jest': '^29.5.3'
      }
    }, null, 2)

    // Main application file
    files['src/index.js'] = `const express = require('express')
const cors = require('cors')
const helmet = require('helmet')
const rateLimit = require('express-rate-limit')
const prometheus = require('express-prometheus-middleware')
require('dotenv').config()

const logger = require('./utils/logger')
const routes = require('./routes')
const { errorHandler, notFound } = require('./middleware/errorHandler')
const healthCheck = require('./middleware/healthCheck')

const app = express()
const PORT = process.env.PORT || ${config.port}

// Security middleware
app.use(helmet())
app.use(cors())

// Rate limiting
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100 // limit each IP to 100 requests per windowMs
})
app.use(limiter)

// Prometheus metrics
app.use(prometheus({
  metricsPath: '/metrics',
  collectDefaultMetrics: true,
  requestDurationBuckets: [0.1, 0.5, 1, 1.5]
}))

// Body parsing
app.use(express.json({ limit: '10mb' }))
app.use(express.urlencoded({ extended: true }))

// Health check
app.use('${config.healthCheck.path}', healthCheck)

// Routes
app.use('/api', routes)

// Error handling
app.use(notFound)
app.use(errorHandler)

// Graceful shutdown
process.on('SIGTERM', () => {
  logger.info('SIGTERM received, shutting down gracefully')
  server.close(() => {
    logger.info('Process terminated')
    process.exit(0)
  })
})

const server = app.listen(PORT, () => {
  logger.info(\`\${config.name} service running on port \${PORT}\`)
})

module.exports = app`

    // Routes
    files['src/routes/index.js'] = `const express = require('express')
const router = express.Router()

${config.endpoints.map(endpoint => `
// ${endpoint.description}
router.${endpoint.method.toLowerCase()}('${endpoint.path}', async (req, res) => {
  try {
    // TODO: Implement ${endpoint.description}
    res.json({ 
      message: '${endpoint.description}',
      service: '${config.name}',
      timestamp: new Date().toISOString()
    })
  } catch (error) {
    res.status(500).json({ error: error.message })
  }
})
`).join('')}

module.exports = router`

    // Health check middleware
    files['src/middleware/healthCheck.js'] = `const healthCheck = (req, res) => {
  const healthData = {
    service: '${config.name}',
    status: 'healthy',
    timestamp: new Date().toISOString(),
    uptime: process.uptime(),
    version: process.env.npm_package_version || '1.0.0',
    environment: process.env.NODE_ENV || 'development'
  }
  
  res.status(200).json(healthData)
}

module.exports = healthCheck`

    // Error handling middleware
    files['src/middleware/errorHandler.js'] = `const logger = require('../utils/logger')

const errorHandler = (err, req, res, next) => {
  logger.error(err.stack)

  if (err.name === 'ValidationError') {
    return res.status(400).json({
      error: 'Validation Error',
      details: err.message
    })
  }

  if (err.name === 'UnauthorizedError') {
    return res.status(401).json({
      error: 'Unauthorized',
      message: 'Invalid token'
    })
  }

  res.status(500).json({
    error: 'Internal Server Error',
    message: process.env.NODE_ENV === 'production' ? 'Something went wrong' : err.message
  })
}

const notFound = (req, res) => {
  res.status(404).json({
    error: 'Not Found',
    message: \`Route \${req.originalUrl} not found\`
  })
}

module.exports = { errorHandler, notFound }`

    // Logger utility
    files['src/utils/logger.js'] = `const winston = require('winston')

const logger = winston.createLogger({
  level: process.env.LOG_LEVEL || 'info',
  format: winston.format.combine(
    winston.format.timestamp(),
    winston.format.errors({ stack: true }),
    winston.format.json()
  ),
  defaultMeta: { service: '${config.name}' },
  transports: [
    new winston.transports.File({ filename: 'logs/error.log', level: 'error' }),
    new winston.transports.File({ filename: 'logs/combined.log' })
  ]
})

if (process.env.NODE_ENV !== 'production') {
  logger.add(new winston.transports.Console({
    format: winston.format.simple()
  }))
}

module.exports = logger`

    // Environment variables template
    files['.env.template'] = `NODE_ENV=development
PORT=${config.port}
LOG_LEVEL=info

# Service Discovery
SERVICE_NAME=${config.name}
CONSUL_HOST=consul
CONSUL_PORT=8500

${Object.entries(config.environment).map(([key, value]) => `${key}=${value}`).join('\n')}`

    // Test file
    files['src/__tests__/app.test.js'] = `const request = require('supertest')
const app = require('../index')

describe('${config.name} Service', () => {
  test('Health check should return 200', async () => {
    const response = await request(app)
      .get('${config.healthCheck.path}')
      .expect(200)
    
    expect(response.body.service).toBe('${config.name}')
    expect(response.body.status).toBe('healthy')
  })

${config.endpoints.map(endpoint => `
  test('${endpoint.method} ${endpoint.path} should work', async () => {
    const response = await request(app)
      .${endpoint.method.toLowerCase()}('/api${endpoint.path}')
      .expect(200)
    
    expect(response.body.service).toBe('${config.name}')
  })
`).join('')}
})`

    return files
  }

  private generateFastAPIService(config: MicroserviceConfig): Record<string, string> {
    const files: Record<string, string> = {}

    // requirements.txt
    files['requirements.txt'] = `fastapi==0.104.1
uvicorn[standard]==0.24.0
pydantic==2.5.0
python-multipart==0.0.6
python-jose[cryptography]==3.3.0
passlib[bcrypt]==1.7.4
prometheus-fastapi-instrumentator==6.1.0
${this.getPythonDependenciesForType(config.type).join('\n')}`

    // Main application
    files['main.py'] = `from fastapi import FastAPI, HTTPException, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.middleware.trustedhost import TrustedHostMiddleware
from prometheus_fastapi_instrumentator import Instrumentator
import uvicorn
import logging
import os
from datetime import datetime

from routers import api_router
from middleware.logging import setup_logging

# Setup logging
setup_logging()
logger = logging.getLogger(__name__)

# Create FastAPI app
app = FastAPI(
    title="${config.name}",
    description="${config.description}",
    version="1.0.0"
)

# Security middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.add_middleware(
    TrustedHostMiddleware,
    allowed_hosts=["*"]
)

# Prometheus metrics
Instrumentator().instrument(app).expose(app)

# Include routers
app.include_router(api_router, prefix="/api")

@app.get("${config.healthCheck.path}")
async def health_check():
    return {
        "service": "${config.name}",
        "status": "healthy",
        "timestamp": datetime.utcnow().isoformat(),
        "version": "1.0.0"
    }

@app.get("/")
async def root():
    return {"message": "${config.name} service is running"}

if __name__ == "__main__":
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=${config.port},
        reload=True
    )`

    // Router
    files['routers/__init__.py'] = ''
    files['routers/api.py'] = `from fastapi import APIRouter, HTTPException, Depends
from datetime import datetime
import logging

logger = logging.getLogger(__name__)
router = APIRouter()

${config.endpoints.map(endpoint => `
@router.${endpoint.method.toLowerCase()}("${endpoint.path}")
async def ${endpoint.path.replace('/', '_').replace('{', '').replace('}', '')}_endpoint():
    """${endpoint.description}"""
    try:
        return {
            "message": "${endpoint.description}",
            "service": "${config.name}",
            "timestamp": datetime.utcnow().isoformat()
        }
    except Exception as e:
        logger.error(f"Error in ${endpoint.path}: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))
`).join('')}

# Export router
api_router = router`

    // Logging middleware
    files['middleware/__init__.py'] = ''
    files['middleware/logging.py'] = `import logging
import sys
from datetime import datetime

def setup_logging():
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
        handlers=[
            logging.StreamHandler(sys.stdout),
            logging.FileHandler('app.log')
        ]
    )`

    return files
  }

  private generateSpringBootService(config: MicroserviceConfig): Record<string, string> {
    const files: Record<string, string> = {}

    // pom.xml
    files['pom.xml'] = `<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    
    <groupId>com.microservice</groupId>
    <artifactId>${config.name}</artifactId>
    <version>1.0.0</version>
    <name>${config.name}</name>
    <description>${config.description}</description>
    
    <properties>
        <java.version>17</java.version>
        <spring-cloud.version>2023.0.0</spring-cloud.version>
    </properties>
    
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>`

    // Main application class
    files['src/main/java/com/microservice/Application.java'] = `package com.microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}`

    // Controller
    files['src/main/java/com/microservice/controller/ApiController.java'] = `package com.microservice.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api")
public class ApiController {
    
${config.endpoints.map(endpoint => `
    @${endpoint.method.charAt(0) + endpoint.method.slice(1).toLowerCase()}Mapping("${endpoint.path}")
    public ResponseEntity<Map<String, Object>> ${endpoint.path.replace('/', '_').replace('{', '').replace('}', '')}Endpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "${endpoint.description}");
        response.put("service", "${config.name}");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
`).join('')}
}`

    // Application properties
    files['src/main/resources/application.yml'] = `server:
  port: ${config.port}

spring:
  application:
    name: ${config.name}

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true

logging:
  level:
    com.microservice: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/application.log`

    return files
  }

  private generateGoGinService(config: MicroserviceConfig): Record<string, string> {
    const files: Record<string, string> = {}

    // go.mod
    files['go.mod'] = `module ${config.name}

go 1.21

require (
    github.com/gin-gonic/gin v1.9.1
    github.com/prometheus/client_golang v1.17.0
    github.com/sirupsen/logrus v1.9.3
)`

    // main.go
    files['main.go'] = `package main

import (
    "fmt"
    "log"
    "net/http"
    "os"
    "time"

    "github.com/gin-gonic/gin"
    "github.com/prometheus/client_golang/prometheus/promhttp"
    "github.com/sirupsen/logrus"
)

func main() {
    // Setup logging
    logrus.SetFormatter(&logrus.JSONFormatter{})
    logrus.SetOutput(os.Stdout)
    logrus.SetLevel(logrus.InfoLevel)

    // Create Gin router
    r := gin.Default()

    // Middleware
    r.Use(gin.Recovery())
    r.Use(gin.Logger())

    // Prometheus metrics
    r.GET("/metrics", gin.WrapH(promhttp.Handler()))

    // Health check
    r.GET("${config.healthCheck.path}", healthCheck)

    // API routes
    api := r.Group("/api")
    setupRoutes(api)

    // Start server
    port := os.Getenv("PORT")
    if port == "" {
        port = "${config.port}"
    }

    logrus.WithFields(logrus.Fields{
        "service": "${config.name}",
        "port":    port,
    }).Info("Starting server")

    log.Fatal(r.Run(":" + port))
}

func healthCheck(c *gin.Context) {
    c.JSON(http.StatusOK, gin.H{
        "service":   "${config.name}",
        "status":    "healthy",
        "timestamp": time.Now().UTC(),
        "version":   "1.0.0",
    })
}

func setupRoutes(api *gin.RouterGroup) {
${config.endpoints.map(endpoint => `
    api.${endpoint.method}("${endpoint.path}", func(c *gin.Context) {
        logrus.WithFields(logrus.Fields{
            "method": "${endpoint.method}",
            "path":   "${endpoint.path}",
        }).Info("${endpoint.description}")

        c.JSON(http.StatusOK, gin.H{
            "message":   "${endpoint.description}",
            "service":   "${config.name}",
            "timestamp": time.Now().UTC(),
        })
    })
`).join('')}
}`

    return files
  }

  private generateNestJSService(config: MicroserviceConfig): Record<string, string> {
    const files: Record<string, string> = {}

    // package.json
    files['package.json'] = JSON.stringify({
      name: config.name,
      version: '1.0.0',
      description: config.description,
      scripts: {
        build: 'nest build',
        format: 'prettier --write "src/**/*.ts" "test/**/*.ts"',
        start: 'nest start',
        'start:dev': 'nest start --watch',
        'start:debug': 'nest start --debug --watch',
        'start:prod': 'node dist/main',
        lint: 'eslint "{src,apps,libs,test}/**/*.ts" --fix',
        test: 'jest',
        'test:watch': 'jest --watch',
        'test:cov': 'jest --coverage',
        'test:debug': 'node --inspect-brk -r tsconfig-paths/register -r ts-node/register node_modules/.bin/jest --runInBand',
        'test:e2e': 'jest --config ./test/jest-e2e.json'
      },
      dependencies: {
        '@nestjs/common': '^10.0.0',
        '@nestjs/core': '^10.0.0',
        '@nestjs/platform-express': '^10.0.0',
        '@nestjs/terminus': '^10.0.0',
        '@prometheus-io/client': '^1.0.0',
        'class-validator': '^0.14.0',
        'class-transformer': '^0.5.1',
        'reflect-metadata': '^0.1.13',
        rxjs: '^7.8.1'
      },
      devDependencies: {
        '@nestjs/cli': '^10.0.0',
        '@nestjs/schematics': '^10.0.0',
        '@nestjs/testing': '^10.0.0',
        '@types/express': '^4.17.17',
        '@types/jest': '^29.5.2',
        '@types/node': '^20.3.1',
        '@typescript-eslint/eslint-plugin': '^6.0.0',
        '@typescript-eslint/parser': '^6.0.0',
        eslint: '^8.42.0',
        'eslint-config-prettier': '^9.0.0',
        'eslint-plugin-prettier': '^5.0.0',
        jest: '^29.5.0',
        prettier: '^3.0.0',
        'source-map-support': '^0.5.21',
        'ts-jest': '^29.1.0',
        'ts-loader': '^9.4.3',
        'ts-node': '^10.9.1',
        tsconfig: '^7.0.0',
        typescript: '^5.1.3'
      }
    }, null, 2)

    // Main application
    files['src/main.ts'] = `import { NestFactory } from '@nestjs/core'
import { ValidationPipe } from '@nestjs/common'
import { AppModule } from './app.module'

async function bootstrap() {
  const app = await NestFactory.create(AppModule)
  
  app.useGlobalPipes(new ValidationPipe())
  app.enableCors()
  
  const port = process.env.PORT || ${config.port}
  await app.listen(port)
  
  console.log(\`${config.name} service is running on port \${port}\`)
}

bootstrap()`

    // App module
    files['src/app.module.ts'] = `import { Module } from '@nestjs/common'
import { TerminusModule } from '@nestjs/terminus'
import { AppController } from './app.controller'
import { AppService } from './app.service'
import { HealthController } from './health/health.controller'

@Module({
  imports: [TerminusModule],
  controllers: [AppController, HealthController],
  providers: [AppService],
})
export class AppModule {}`

    // App controller
    files['src/app.controller.ts'] = `import { Controller, Get, Post, Put, Delete, Patch } from '@nestjs/common'
import { AppService } from './app.service'

@Controller('api')
export class AppController {
  constructor(private readonly appService: AppService) {}

${config.endpoints.map(endpoint => `
  @${endpoint.method.charAt(0) + endpoint.method.slice(1).toLowerCase()}('${endpoint.path}')
  ${endpoint.path.replace('/', '_').replace('{', '').replace('}', '')}() {
    return this.appService.handleRequest('${endpoint.description}')
  }
`).join('')}
}`

    // App service
    files['src/app.service.ts'] = `import { Injectable } from '@nestjs/common'

@Injectable()
export class AppService {
  handleRequest(description: string) {
    return {
      message: description,
      service: '${config.name}',
      timestamp: new Date().toISOString(),
    }
  }
}`

    // Health controller
    files['src/health/health.controller.ts'] = `import { Controller, Get } from '@nestjs/common'
import { HealthCheckService, HealthCheck } from '@nestjs/terminus'

@Controller('${config.healthCheck.path}')
export class HealthController {
  constructor(private health: HealthCheckService) {}

  @Get()
  @HealthCheck()
  check() {
    return {
      service: '${config.name}',
      status: 'healthy',
      timestamp: new Date().toISOString(),
      version: '1.0.0'
    }
  }
}`

    return files
  }

  private generateDjangoService(config: MicroserviceConfig): Record<string, string> {
    const files: Record<string, string> = {}

    // requirements.txt
    files['requirements.txt'] = `Django==4.2.7
djangorestframework==3.14.0
django-cors-headers==4.3.1
django-prometheus==2.3.1
gunicorn==21.2.0
${this.getPythonDependenciesForType(config.type).join('\n')}`

    // manage.py
    files['manage.py'] = `#!/usr/bin/env python
import os
import sys

if __name__ == '__main__':
    os.environ.setdefault('DJANGO_SETTINGS_MODULE', '${config.name.replace('-', '_')}.settings')
    try:
        from django.core.management import execute_from_command_line
    except ImportError as exc:
        raise ImportError(
            "Couldn't import Django. Are you sure it's installed and "
            "available on your PYTHONPATH environment variable? Did you "
            "forget to activate a virtual environment?"
        ) from exc
    execute_from_command_line(sys.argv)`

    // Django settings
    files[`${config.name.replace('-', '_')}/settings.py`] = `import os
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent.parent

SECRET_KEY = os.environ.get('SECRET_KEY', 'your-secret-key-here')
DEBUG = os.environ.get('DEBUG', 'False') == 'True'
ALLOWED_HOSTS = ['*']

INSTALLED_APPS = [
    'django.contrib.admin',
    'django.contrib.auth',
    'django.contrib.contenttypes',
    'django.contrib.sessions',
    'django.contrib.messages',
    'django.contrib.staticfiles',
    'rest_framework',
    'corsheaders',
    'django_prometheus',
    'api',
]

MIDDLEWARE = [
    'django_prometheus.middleware.PrometheusBeforeMiddleware',
    'django.middleware.security.SecurityMiddleware',
    'corsheaders.middleware.CorsMiddleware',
    'django.contrib.sessions.middleware.SessionMiddleware',
    'django.middleware.common.CommonMiddleware',
    'django.middleware.csrf.CsrfViewMiddleware',
    'django.contrib.auth.middleware.AuthenticationMiddleware',
    'django.contrib.messages.middleware.MessageMiddleware',
    'django.middleware.clickjacking.XFrameOptionsMiddleware',
    'django_prometheus.middleware.PrometheusAfterMiddleware',
]

ROOT_URLCONF = '${config.name.replace('-', '_')}.urls'

TEMPLATES = [
    {
        'BACKEND': 'django.template.backends.django.DjangoTemplates',
        'DIRS': [],
        'APP_DIRS': True,
        'OPTIONS': {
            'context_processors': [
                'django.template.context_processors.debug',
                'django.template.context_processors.request',
                'django.contrib.auth.context_processors.auth',
                'django.contrib.messages.context_processors.messages',
            ],
        },
    },
]

WSGI_APPLICATION = '${config.name.replace('-', '_')}.wsgi.application'

REST_FRAMEWORK = {
    'DEFAULT_PERMISSION_CLASSES': [
        'rest_framework.permissions.AllowAny',
    ],
    'DEFAULT_RENDERER_CLASSES': [
        'rest_framework.renderers.JSONRenderer',
    ],
}

CORS_ALLOW_ALL_ORIGINS = True

LANGUAGE_CODE = 'en-us'
TIME_ZONE = 'UTC'
USE_I18N = True
USE_TZ = True

STATIC_URL = '/static/'
DEFAULT_AUTO_FIELD = 'django.db.models.BigAutoField'

LOGGING = {
    'version': 1,
    'disable_existing_loggers': False,
    'handlers': {
        'console': {
            'class': 'logging.StreamHandler',
        },
    },
    'root': {
        'handlers': ['console'],
    },
}`

    // URL configuration
    files[`${config.name.replace('-', '_')}/urls.py`] = `from django.contrib import admin
from django.urls import path, include

urlpatterns = [
    path('admin/', admin.site.urls),
    path('api/', include('api.urls')),
    path('${config.healthCheck.path}/', include('api.urls')),
    path('', include('django_prometheus.urls')),
]`

    // API views
    files['api/views.py'] = `from rest_framework.decorators import api_view
from rest_framework.response import Response
from rest_framework import status
from datetime import datetime

@api_view(['GET'])
def health_check(request):
    return Response({
        'service': '${config.name}',
        'status': 'healthy',
        'timestamp': datetime.utcnow().isoformat(),
        'version': '1.0.0'
    })

${config.endpoints.map(endpoint => `
@api_view(['${endpoint.method}'])
def ${endpoint.path.replace('/', '_').replace('{', '').replace('}', '')}_view(request):
    """${endpoint.description}"""
    return Response({
        'message': '${endpoint.description}',
        'service': '${config.name}',
        'timestamp': datetime.utcnow().isoformat()
    })
`).join('')}`

    // API URLs
    files['api/urls.py'] = `from django.urls import path
from . import views

urlpatterns = [
    path('health/', views.health_check, name='health_check'),
${config.endpoints.map(endpoint => `
    path('${endpoint.path.startsWith('/') ? endpoint.path.slice(1) : endpoint.path}', views.${endpoint.path.replace('/', '_').replace('{', '').replace('}', '')}_view, name='${endpoint.path.replace('/', '_')}'),
`).join('')}
]`

    return files
  }

  private getDependenciesForType(type: string): Record<string, string> {
    switch (type) {
      case 'database':
        return {
          mongoose: '^7.5.0',
          pg: '^8.11.3',
          'pg-hstore': '^2.3.4'
        }
      case 'cache':
        return {
          redis: '^4.6.7',
          'node-cache': '^5.1.2'
        }
      case 'queue':
        return {
          bull: '^4.11.3',
          amqplib: '^0.10.3'
        }
      case 'auth':
        return {
          jsonwebtoken: '^9.0.2',
          bcryptjs: '^2.4.3',
          passport: '^0.6.0'
        }
      default:
        return {}
    }
  }

  private getPythonDependenciesForType(type: string): string[] {
    switch (type) {
      case 'database':
        return ['sqlalchemy==2.0.23', 'psycopg2-binary==2.9.9', 'pymongo==4.6.0']
      case 'cache':
        return ['redis==5.0.1', 'aioredis==2.0.1']
      case 'queue':
        return ['celery==5.3.4', 'kombu==5.3.4']
      case 'auth':
        return ['pyjwt==2.8.0', 'bcrypt==4.1.2']
      default:
        return []
    }
  }
}