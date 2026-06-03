import { MicroserviceConfig } from './MicroserviceGenerator'

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

export class DockerGenerator {
  generateDockerfile(service: MicroserviceConfig): string {
    switch (service.framework) {
      case 'express':
      case 'nestjs':
        return this.generateNodeDockerfile(service)
      case 'fastapi':
      case 'django':
        return this.generatePythonDockerfile(service)
      case 'spring-boot':
        return this.generateJavaDockerfile(service)
      case 'go-gin':
        return this.generateGoDockerfile(service)
      default:
        return this.generateNodeDockerfile(service)
    }
  }

  private generateNodeDockerfile(service: MicroserviceConfig): string {
    return `# Use official Node.js runtime as base image
FROM node:18-alpine

# Set working directory
WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci --only=production && npm cache clean --force

# Copy application code
COPY . .

# Create non-root user
RUN addgroup -g 1001 -S nodejs && \\
    adduser -S nodejs -u 1001

# Create logs directory
RUN mkdir -p logs && chown nodejs:nodejs logs

# Switch to non-root user
USER nodejs

# Expose port
EXPOSE ${service.port}

# Health check
HEALTHCHECK --interval=${service.healthCheck.interval}s --timeout=3s --start-period=5s --retries=3 \\
  CMD node healthcheck.js

# Start the application
CMD ["npm", "start"]`
  }

  private generatePythonDockerfile(service: MicroserviceConfig): string {
    const isAsyncFramework = service.framework === 'fastapi'
    
    return `# Use official Python runtime as base image
FROM python:3.11-slim

# Set working directory
WORKDIR /app

# Set environment variables
ENV PYTHONDONTWRITEBYTECODE=1 \\
    PYTHONUNBUFFERED=1 \\
    PIP_NO_CACHE_DIR=1 \\
    PIP_DISABLE_PIP_VERSION_CHECK=1

# Install system dependencies
RUN apt-get update && apt-get install -y \\
    gcc \\
    && rm -rf /var/lib/apt/lists/*

# Copy requirements and install Python dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy application code
COPY . .

# Create non-root user
RUN adduser --disabled-password --gecos '' appuser && \\
    chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE ${service.port}

# Health check
HEALTHCHECK --interval=${service.healthCheck.interval}s --timeout=3s --start-period=5s --retries=3 \\
  CMD python healthcheck.py

# Start the application
${isAsyncFramework 
  ? `CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "${service.port}"]`
  : `CMD ["gunicorn", "--bind", "0.0.0.0:${service.port}", "--workers", "2", "${service.name.replace('-', '_')}.wsgi:application"]`
}`
  }

  private generateJavaDockerfile(service: MicroserviceConfig): string {
    return `# Use official OpenJDK runtime as base image
FROM openjdk:17-jdk-slim as build

# Set working directory
WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Production stage
FROM openjdk:17-jre-slim

# Set working directory
WORKDIR /app

# Copy the jar file from build stage
COPY --from=build /app/target/${service.name}-1.0.0.jar app.jar

# Create non-root user
RUN addgroup --system spring && adduser --system --group spring

# Switch to non-root user
USER spring:spring

# Expose port
EXPOSE ${service.port}

# Health check
HEALTHCHECK --interval=${service.healthCheck.interval}s --timeout=3s --start-period=5s --retries=3 \\
  CMD curl -f http://localhost:${service.port}/actuator/health || exit 1

# Start the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]`
  }

  private generateGoDockerfile(service: MicroserviceConfig): string {
    return `# Build stage
FROM golang:1.21-alpine AS builder

# Set working directory
WORKDIR /app

# Copy go mod files
COPY go.mod go.sum ./

# Download dependencies
RUN go mod download

# Copy source code
COPY . .

# Build the application
RUN CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -o main .

# Production stage
FROM alpine:latest

# Install ca-certificates for HTTPS requests
RUN apk --no-cache add ca-certificates curl

# Set working directory
WORKDIR /root/

# Copy the binary from builder stage
COPY --from=builder /app/main .

# Create non-root user
RUN adduser -D -s /bin/sh appuser

# Switch to non-root user
USER appuser

# Expose port
EXPOSE ${service.port}

# Health check
HEALTHCHECK --interval=${service.healthCheck.interval}s --timeout=3s --start-period=5s --retries=3 \\
  CMD curl -f http://localhost:${service.port}${service.healthCheck.path} || exit 1

# Start the application
CMD ["./main"]`
  }

  generateComposeService(service: MicroserviceConfig): string {
    return `version: '3.8'

services:
  ${service.name}:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: ${service.name}
    ports:
      - "\${${service.name.toUpperCase().replace('-', '_')}_PORT:-${service.port}}:${service.port}"
    environment:
      - NODE_ENV=\${NODE_ENV:-production}
      - PORT=${service.port}
      - SERVICE_NAME=${service.name}
${Object.entries(service.environment).map(([key, value]) => `      - ${key}=${value}`).join('\n')}
    networks:
      - app-network
    volumes:
      - ./logs:/app/logs
    restart: unless-stopped
    deploy:
      resources:
        limits:
          cpus: '${service.resources.cpu === '100m' ? '0.1' : service.resources.cpu}'
          memory: ${service.resources.memory}
        reservations:
          cpus: '0.05'
          memory: 64M
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:${service.port}${service.healthCheck.path}"]
      interval: ${service.healthCheck.interval}s
      timeout: 3s
      retries: 3
      start_period: 5s

networks:
  app-network:
    external: true`
  }

  generateMainCompose(mesh: ServiceMesh): string {
    const services = mesh.services.map(service => {
      const envVars = Object.entries(service.environment)
        .map(([key, value]) => `      - ${key}=${value}`)
        .join('\n')

      return `  ${service.name}:
    build:
      context: ./services/${service.name}
      dockerfile: Dockerfile
    container_name: ${service.name}
    ports:
      - "${service.port}:${service.port}"
    environment:
      - NODE_ENV=production
      - PORT=${service.port}
      - SERVICE_NAME=${service.name}
      - CONSUL_HOST=consul
      - CONSUL_PORT=8500
${envVars}
    networks:
      - ${mesh.networks[0]?.name || 'app-network'}
    volumes:
      - ./logs/${service.name}:/app/logs
    restart: unless-stopped
    depends_on:
      - consul
    deploy:
      resources:
        limits:
          cpus: '${service.resources.cpu === '100m' ? '0.1' : service.resources.cpu}'
          memory: ${service.resources.memory}
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:${service.port}${service.healthCheck.path}"]
      interval: ${service.healthCheck.interval}s
      timeout: 3s
      retries: 3
      start_period: 10s`
    }).join('\n\n')

    const serviceDiscovery = mesh.serviceDiscovery.enabled ? this.generateServiceDiscoveryService(mesh) : ''
    const gateway = mesh.gateway.enabled ? this.generateGatewayService(mesh) : ''
    const monitoring = mesh.monitoring.enabled ? this.generateMonitoringServices(mesh) : ''

    return `version: '3.8'

services:
${services}

${serviceDiscovery}

${gateway}

${monitoring}

networks:
${mesh.networks.map(network => `  ${network.name}:
    driver: ${network.driver}${network.subnet ? `\n    ipam:\n      config:\n        - subnet: ${network.subnet}` : ''}`).join('\n')}

volumes:
${mesh.volumes.map(volume => `  ${volume.name}:
    driver: ${volume.driver}`).join('\n')}
  consul-data:
  prometheus-data:
  grafana-data:`
  }

  private generateServiceDiscoveryService(mesh: ServiceMesh): string {
    switch (mesh.serviceDiscovery.type) {
      case 'consul':
        return `  consul:
    image: consul:1.15
    container_name: consul
    ports:
      - "8500:8500"
      - "8600:8600/udp"
    environment:
      - CONSUL_BIND_INTERFACE=eth0
    command: agent -server -ui -node=server-1 -bootstrap-expect=1 -client=0.0.0.0
    volumes:
      - consul-data:/consul/data
    networks:
      - ${mesh.networks[0]?.name || 'app-network'}`

      case 'eureka':
        return `  eureka:
    image: springcloud/eureka:latest
    container_name: eureka
    ports:
      - "8761:8761"
    environment:
      - EUREKA_INSTANCE_HOSTNAME=eureka
      - EUREKA_CLIENT_REGISTER_WITH_EUREKA=false
      - EUREKA_CLIENT_FETCH_REGISTRY=false
    networks:
      - ${mesh.networks[0]?.name || 'app-network'}`

      case 'etcd':
        return `  etcd:
    image: quay.io/coreos/etcd:v3.5.0
    container_name: etcd
    ports:
      - "2379:2379"
      - "2380:2380"
    environment:
      - ETCD_NAME=node1
      - ETCD_INITIAL_ADVERTISE_PEER_URLS=http://etcd:2380
      - ETCD_LISTEN_PEER_URLS=http://0.0.0.0:2380
      - ETCD_LISTEN_CLIENT_URLS=http://0.0.0.0:2379
      - ETCD_ADVERTISE_CLIENT_URLS=http://etcd:2379
      - ETCD_INITIAL_CLUSTER=node1=http://etcd:2380
    networks:
      - ${mesh.networks[0]?.name || 'app-network'}`

      case 'zookeeper':
        return `  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    container_name: zookeeper
    ports:
      - "2181:2181"
    environment:
      - ZOOKEEPER_CLIENT_PORT=2181
      - ZOOKEEPER_TICK_TIME=2000
    networks:
      - ${mesh.networks[0]?.name || 'app-network'}`

      default:
        return ''
    }
  }

  private generateGatewayService(mesh: ServiceMesh): string {
    return `  gateway:
    build:
      context: ./gateway
      dockerfile: Dockerfile
    container_name: api-gateway
    ports:
      - "${mesh.gateway.port}:80"
    depends_on:
${mesh.services.map(service => `      - ${service.name}`).join('\n')}
    networks:
      - ${mesh.networks[0]?.name || 'app-network'}
    restart: unless-stopped`
  }

  private generateMonitoringServices(mesh: ServiceMesh): string {
    let services = ''

    if (mesh.monitoring.prometheus) {
      services += `  prometheus:
    image: prom/prometheus:latest
    container_name: prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus-data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--web.console.libraries=/etc/prometheus/console_libraries'
      - '--web.console.templates=/etc/prometheus/consoles'
      - '--storage.tsdb.retention.time=200h'
      - '--web.enable-lifecycle'
    networks:
      - ${mesh.networks[0]?.name || 'app-network'}
    restart: unless-stopped

`
    }

    if (mesh.monitoring.grafana) {
      services += `  grafana:
    image: grafana/grafana:latest
    container_name: grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_USER=admin
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - grafana-data:/var/lib/grafana
      - ./monitoring/grafana/provisioning:/etc/grafana/provisioning
    networks:
      - ${mesh.networks[0]?.name || 'app-network'}
    restart: unless-stopped

`
    }

    if (mesh.monitoring.jaeger) {
      services += `  jaeger:
    image: jaegertracing/all-in-one:latest
    container_name: jaeger
    ports:
      - "16686:16686"
      - "14268:14268"
    environment:
      - COLLECTOR_OTLP_ENABLED=true
    networks:
      - ${mesh.networks[0]?.name || 'app-network'}
    restart: unless-stopped

`
    }

    return services
  }

  generateGatewayConfig(mesh: ServiceMesh): string {
    const upstreams = mesh.services.map(service => `
upstream ${service.name} {
    server ${service.name}:${service.port};
}`).join('\n')

    const locations = mesh.gateway.routes.map(route => `
    location ${route.path} {
        proxy_pass http://${route.service};
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # Add correlation ID
        proxy_set_header X-Correlation-ID $request_id;
        
        # Health check bypass
        if ($request_uri ~ "^${route.path}/health") {
            proxy_pass http://${route.service};
        }
    }`).join('\n')

    return `events {
    worker_connections 1024;
}

http {
    include       /etc/nginx/mime.types;
    default_type  application/octet-stream;

    # Logging format
    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent" "$http_x_forwarded_for" '
                    'rt=$request_time uct="$upstream_connect_time" '
                    'uht="$upstream_header_time" urt="$upstream_response_time"';

    access_log /var/log/nginx/access.log main;
    error_log /var/log/nginx/error.log;

    # Basic settings
    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;
    keepalive_timeout 65;
    types_hash_max_size 2048;

    # Rate limiting
    limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;

    # Upstream definitions
    ${upstreams}

    server {
        listen 80;
        server_name _;

        # Apply rate limiting
        limit_req zone=api burst=20 nodelay;

        # Health check endpoint
        location /health {
            access_log off;
            return 200 "healthy\\n";
            add_header Content-Type text/plain;
        }

        # Metrics endpoint
        location /metrics {
            stub_status on;
            access_log off;
            allow 127.0.0.1;
            allow 10.0.0.0/8;
            allow 172.16.0.0/12;
            allow 192.168.0.0/16;
            deny all;
        }

        # Service routes
        ${locations}

        # Default fallback
        location / {
            return 404 "Service not found";
        }
    }
}`
  }

  generateGatewayDockerfile(): string {
    return `FROM nginx:alpine

# Copy nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

# Create log directory
RUN mkdir -p /var/log/nginx

# Expose port
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \\
  CMD curl -f http://localhost/health || exit 1

# Start nginx
CMD ["nginx", "-g", "daemon off;"]`
  }

  generateMonitoringConfigs(mesh: ServiceMesh): Record<string, string> {
    const configs: Record<string, string> = {}

    if (mesh.monitoring.prometheus) {
      configs['prometheus.yml'] = `global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  # - "first_rules.yml"
  # - "second_rules.yml"

scrape_configs:
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

${mesh.services.map(service => `  - job_name: '${service.name}'
    static_configs:
      - targets: ['${service.name}:${service.port}']
    metrics_path: '/metrics'
    scrape_interval: 5s`).join('\n\n')}

  - job_name: 'nginx'
    static_configs:
      - targets: ['gateway:80']
    metrics_path: '/metrics'`
    }

    if (mesh.monitoring.grafana) {
      configs['grafana/provisioning/dashboards/dashboard.yml'] = `apiVersion: 1

providers:
  - name: 'default'
    orgId: 1
    folder: ''
    type: file
    disableDeletion: false
    updateIntervalSeconds: 10
    allowUiUpdates: true
    options:
      path: /etc/grafana/provisioning/dashboards`

      configs['grafana/provisioning/datasources/datasource.yml'] = `apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true`

      configs['grafana/provisioning/dashboards/microservices.json'] = JSON.stringify({
        dashboard: {
          id: null,
          title: "Microservices Dashboard",
          tags: ["microservices"],
          timezone: "browser",
          panels: [
            {
              id: 1,
              title: "Request Rate",
              type: "graph",
              targets: [
                {
                  expr: "rate(http_requests_total[5m])",
                  legendFormat: "{{service}}"
                }
              ]
            },
            {
              id: 2,
              title: "Response Time",
              type: "graph",
              targets: [
                {
                  expr: "http_request_duration_seconds",
                  legendFormat: "{{service}}"
                }
              ]
            }
          ],
          time: {
            from: "now-1h",
            to: "now"
          },
          refresh: "5s"
        }
      })
    }

    return configs
  }

  generateDeployScript(mesh: ServiceMesh): string {
    return `#!/bin/bash

# Microservices Deployment Script
# Generated for ${mesh.name}

set -e

echo "🚀 Deploying ${mesh.name} microservices..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker and try again."
    exit 1
fi

# Check if docker-compose is available
if ! command -v docker-compose &> /dev/null; then
    echo "❌ docker-compose is not installed. Please install it and try again."
    exit 1
fi

# Create necessary directories
echo "📁 Creating directories..."
mkdir -p logs
mkdir -p logs/nginx
${mesh.services.map(service => `mkdir -p logs/${service.name}`).join('\n')}

# Create networks if they don't exist
echo "🌐 Creating networks..."
${mesh.networks.map(network => `docker network create ${network.name} --driver ${network.driver} 2>/dev/null || true`).join('\n')}

# Pull latest images
echo "📦 Pulling images..."
docker-compose pull

# Build services
echo "🔨 Building services..."
docker-compose build --parallel

# Start services
echo "▶️  Starting services..."
docker-compose up -d

# Wait for services to be healthy
echo "⏳ Waiting for services to be healthy..."
sleep 10

# Check service health
echo "🏥 Checking service health..."
${mesh.services.map(service => `
echo "Checking ${service.name}..."
for i in {1..30}; do
    if curl -f http://localhost:${service.port}${service.healthCheck.path} > /dev/null 2>&1; then
        echo "✅ ${service.name} is healthy"
        break
    else
        echo "⏳ Waiting for ${service.name}... ($i/30)"
        sleep 2
    fi
    if [ $i -eq 30 ]; then
        echo "❌ ${service.name} failed to start"
        exit 1
    fi
done`).join('')}

echo "🎉 All services are running successfully!"
echo ""
echo "📊 Service endpoints:"
${mesh.services.map(service => `echo "  ${service.name}: http://localhost:${service.port}"`).join('\n')}

${mesh.gateway.enabled ? `echo "🌐 API Gateway: http://localhost:${mesh.gateway.port}"` : ''}
${mesh.monitoring.prometheus ? 'echo "📈 Prometheus: http://localhost:9090"' : ''}
${mesh.monitoring.grafana ? 'echo "📊 Grafana: http://localhost:3000 (admin/admin)"' : ''}
${mesh.serviceDiscovery.enabled ? `echo "🔍 ${mesh.serviceDiscovery.type}: http://localhost:${mesh.serviceDiscovery.type === 'consul' ? '8500' : '8761'}"` : ''}

echo ""
echo "🔧 To view logs: docker-compose logs -f [service-name]"
echo "🛑 To stop all services: ./scripts/stop.sh"
echo "📊 To view service status: docker-compose ps"`
  }

  generateStartScript(mesh: ServiceMesh): string {
    return `#!/bin/bash

# Start ${mesh.name} microservices

set -e

echo "▶️  Starting ${mesh.name} microservices..."

# Start all services
docker-compose up -d

echo "✅ Services started successfully!"
echo "📊 View status: docker-compose ps"
echo "📋 View logs: docker-compose logs -f"`
  }

  generateStopScript(mesh: ServiceMesh): string {
    return `#!/bin/bash

# Stop ${mesh.name} microservices

set -e

echo "🛑 Stopping ${mesh.name} microservices..."

# Stop all services
docker-compose down

echo "✅ All services stopped successfully!"
echo "🧹 To remove volumes: docker-compose down -v"
echo "🗑️  To remove images: docker-compose down --rmi all"`
  }

  generateKubernetesManifests(mesh: ServiceMesh): Record<string, string> {
    const manifests: Record<string, string> = {}

    // Namespace
    manifests['namespace.yaml'] = `apiVersion: v1
kind: Namespace
metadata:
  name: ${mesh.name}
  labels:
    name: ${mesh.name}`

    // ConfigMap for service discovery
    if (mesh.serviceDiscovery.enabled) {
      manifests['configmap.yaml'] = `apiVersion: v1
kind: ConfigMap
metadata:
  name: ${mesh.name}-config
  namespace: ${mesh.name}
data:
  SERVICE_DISCOVERY_TYPE: "${mesh.serviceDiscovery.type}"
  CONSUL_HOST: "consul.${mesh.name}.svc.cluster.local"
  CONSUL_PORT: "8500"`
    }

    // Services and Deployments
    mesh.services.forEach(service => {
      manifests[`${service.name}-deployment.yaml`] = `apiVersion: apps/v1
kind: Deployment
metadata:
  name: ${service.name}
  namespace: ${mesh.name}
  labels:
    app: ${service.name}
    version: v1
spec:
  replicas: ${service.scaling.min}
  selector:
    matchLabels:
      app: ${service.name}
  template:
    metadata:
      labels:
        app: ${service.name}
        version: v1
    spec:
      containers:
      - name: ${service.name}
        image: ${service.name}:latest
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: ${service.port}
        env:
        - name: NODE_ENV
          value: "production"
        - name: PORT
          value: "${service.port}"
        - name: SERVICE_NAME
          value: "${service.name}"
${Object.entries(service.environment).map(([key, value]) => `        - name: ${key}
          value: "${value}"`).join('\n')}
        resources:
          limits:
            cpu: ${service.resources.cpu}
            memory: ${service.resources.memory}
          requests:
            cpu: "50m"
            memory: "64Mi"
        livenessProbe:
          httpGet:
            path: ${service.healthCheck.path}
            port: ${service.port}
          initialDelaySeconds: 10
          periodSeconds: ${service.healthCheck.interval}
        readinessProbe:
          httpGet:
            path: ${service.healthCheck.path}
            port: ${service.port}
          initialDelaySeconds: 5
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: ${service.name}
  namespace: ${mesh.name}
  labels:
    app: ${service.name}
spec:
  selector:
    app: ${service.name}
  ports:
  - port: ${service.port}
    targetPort: ${service.port}
    protocol: TCP
  type: ClusterIP`

      // HPA for each service
      manifests[`${service.name}-hpa.yaml`] = `apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: ${service.name}-hpa
  namespace: ${mesh.name}
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: ${service.name}
  minReplicas: ${service.scaling.min}
  maxReplicas: ${service.scaling.max}
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: ${service.scaling.targetCpu}`
    })

    // Service Discovery (Consul)
    if (mesh.serviceDiscovery.enabled && mesh.serviceDiscovery.type === 'consul') {
      manifests['consul.yaml'] = `apiVersion: apps/v1
kind: Deployment
metadata:
  name: consul
  namespace: ${mesh.name}
spec:
  replicas: 1
  selector:
    matchLabels:
      app: consul
  template:
    metadata:
      labels:
        app: consul
    spec:
      containers:
      - name: consul
        image: consul:1.15
        ports:
        - containerPort: 8500
        - containerPort: 8600
        command:
        - consul
        - agent
        - -server
        - -ui
        - -node=server-1
        - -bootstrap-expect=1
        - -client=0.0.0.0
        - -bind=0.0.0.0
---
apiVersion: v1
kind: Service
metadata:
  name: consul
  namespace: ${mesh.name}
spec:
  selector:
    app: consul
  ports:
  - name: http
    port: 8500
    targetPort: 8500
  - name: dns
    port: 8600
    targetPort: 8600
    protocol: UDP
  type: ClusterIP`
    }

    // Gateway (Nginx Ingress)
    if (mesh.gateway.enabled) {
      manifests['gateway-ingress.yaml'] = `apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: ${mesh.name}-gateway
  namespace: ${mesh.name}
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /$2
    nginx.ingress.kubernetes.io/use-regex: "true"
    nginx.ingress.kubernetes.io/rate-limit: "100"
spec:
  ingressClassName: nginx
  rules:
  - host: ${mesh.name}.local
    http:
      paths:
${mesh.gateway.routes.map(route => `      - path: ${route.path}(/|$)(.*)
        pathType: Prefix
        backend:
          service:
            name: ${route.service}
            port:
              number: ${route.port}`).join('\n')}
  - host: localhost
    http:
      paths:
${mesh.gateway.routes.map(route => `      - path: ${route.path}(/|$)(.*)
        pathType: Prefix
        backend:
          service:
            name: ${route.service}
            port:
              number: ${route.port}`).join('\n')}`
    }

    // Monitoring (Prometheus)
    if (mesh.monitoring.prometheus) {
      manifests['prometheus.yaml'] = `apiVersion: apps/v1
kind: Deployment
metadata:
  name: prometheus
  namespace: ${mesh.name}
spec:
  replicas: 1
  selector:
    matchLabels:
      app: prometheus
  template:
    metadata:
      labels:
        app: prometheus
    spec:
      containers:
      - name: prometheus
        image: prom/prometheus:latest
        ports:
        - containerPort: 9090
        volumeMounts:
        - name: config-volume
          mountPath: /etc/prometheus
      volumes:
      - name: config-volume
        configMap:
          name: prometheus-config
---
apiVersion: v1
kind: Service
metadata:
  name: prometheus
  namespace: ${mesh.name}
spec:
  selector:
    app: prometheus
  ports:
  - port: 9090
    targetPort: 9090
  type: ClusterIP
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: prometheus-config
  namespace: ${mesh.name}
data:
  prometheus.yml: |
    global:
      scrape_interval: 15s
    scrape_configs:
    - job_name: 'kubernetes-pods'
      kubernetes_sd_configs:
      - role: pod
        namespaces:
          names:
          - ${mesh.name}
      relabel_configs:
      - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_scrape]
        action: keep
        regex: true`
    }

    return manifests
  }
}