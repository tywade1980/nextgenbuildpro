interface ServiceMesh {
  id: string
  name: string
  services: Array<{
    id: string
    name: string
    type: string
    port: number
  }>
  serviceDiscovery: {
    type: 'consul' | 'eureka' | 'etcd' | 'zookeeper'
    enabled: boolean
  }
}

export class ServiceDiscoveryManager {
  generateConfiguration(mesh: ServiceMesh): string {
    switch (mesh.serviceDiscovery.type) {
      case 'consul':
        return this.generateConsulConfig(mesh)
      case 'eureka':
        return this.generateEurekaConfig(mesh)
      case 'etcd':
        return this.generateEtcdConfig(mesh)
      case 'zookeeper':
        return this.generateZookeeperConfig(mesh)
      default:
        return this.generateConsulConfig(mesh)
    }
  }

  private generateConsulConfig(mesh: ServiceMesh): string {
    return `{
  "datacenter": "dc1",
  "data_dir": "/consul/data",
  "log_level": "INFO",
  "node_name": "consul-server",
  "server": true,
  "bootstrap_expect": 1,
  "ui_config": {
    "enabled": true
  },
  "bind_addr": "0.0.0.0",
  "client_addr": "0.0.0.0",
  "ports": {
    "grpc": 8502
  },
  "connect": {
    "enabled": true
  },
  "acl": {
    "enabled": false,
    "default_policy": "allow"
  },
  "services": [
${mesh.services.map(service => `    {
      "id": "${service.id}",
      "name": "${service.name}",
      "port": ${service.port},
      "check": {
        "http": "http://${service.name}:${service.port}/health",
        "interval": "10s"
      },
      "tags": ["${service.type}", "microservice"]
    }`).join(',\n')}
  ]
}`
  }

  private generateEurekaConfig(mesh: ServiceMesh): string {
    return `server:
  port: 8761

eureka:
  instance:
    hostname: eureka-server
  client:
    registerWithEureka: false
    fetchRegistry: false
    service-url:
      defaultZone: http://eureka-server:8761/eureka/
  server:
    enable-self-preservation: false

spring:
  application:
    name: eureka-server

logging:
  level:
    com.netflix.eureka: INFO
    com.netflix.discovery: INFO

# Service configurations for registration
services:
${mesh.services.map(service => `  ${service.name}:
    service-url:
      defaultZone: http://eureka-server:8761/eureka/
    instance:
      hostname: ${service.name}
      port: ${service.port}
      health-check-url-path: /health
      status-page-url-path: /info
      metadata-map:
        type: ${service.type}
        version: 1.0.0`).join('\n')}
`
  }

  private generateEtcdConfig(mesh: ServiceMesh): string {
    return `# etcd configuration
name: etcd-server
data-dir: /etcd-data
listen-client-urls: http://0.0.0.0:2379
advertise-client-urls: http://etcd:2379
listen-peer-urls: http://0.0.0.0:2380
initial-advertise-peer-urls: http://etcd:2380
initial-cluster: etcd-server=http://etcd:2380
initial-cluster-state: new
initial-cluster-token: etcd-cluster

# Service registry structure
# Services will be registered under /services/{service-name}
# Example:
# /services/${mesh.services[0]?.name}/config -> {"host": "${mesh.services[0]?.name}", "port": ${mesh.services[0]?.port}}

# Auto-registration script for services:
# etcdctl put /services/{service-name}/config '{"host": "{service-name}", "port": {port}, "health": "/health"}'

# Service discovery endpoints:
${mesh.services.map(service => `# /services/${service.name}/config
# /services/${service.name}/health`).join('\n')}
`
  }

  private generateZookeeperConfig(mesh: ServiceMesh): string {
    return `# Zookeeper configuration
tickTime=2000
dataDir=/var/lib/zookeeper
clientPort=2181
initLimit=10
syncLimit=5

# Service registry configuration
# Services will be registered under /services
# Path structure: /services/{service-name}/{instance-id}

# Service node structure for each service:
${mesh.services.map(service => `# /services/${service.name}
#   ├── config ({"port": ${service.port}, "type": "${service.type}"})
#   ├── instances
#   │   └── ${service.name}-1 ({"host": "${service.name}", "port": ${service.port}, "status": "UP"})
#   └── health
#       └── endpoint ("/health")`).join('\n')}

# Auto-discovery configuration
autopurge.snapRetainCount=3
autopurge.purgeInterval=1

# Service monitoring
4lw.commands.whitelist=*

# Health check paths
${mesh.services.map(service => `# ${service.name}: /health`).join('\n')}
`
  }

  generateServiceRegistrationScript(service: any, discoveryType: string): string {
    switch (discoveryType) {
      case 'consul':
        return this.generateConsulRegistration(service)
      case 'eureka':
        return this.generateEurekaRegistration(service)
      case 'etcd':
        return this.generateEtcdRegistration(service)
      case 'zookeeper':
        return this.generateZookeeperRegistration(service)
      default:
        return this.generateConsulRegistration(service)
    }
  }

  private generateConsulRegistration(service: any): string {
    return `#!/bin/bash

# Consul service registration script for ${service.name}

CONSUL_HOST=\${CONSUL_HOST:-consul}
CONSUL_PORT=\${CONSUL_PORT:-8500}
SERVICE_NAME=${service.name}
SERVICE_PORT=${service.port}
SERVICE_HOST=${service.name}

# Wait for Consul to be available
echo "Waiting for Consul to be available..."
while ! curl -s http://\$CONSUL_HOST:\$CONSUL_PORT/v1/status/leader > /dev/null; do
  echo "Waiting for Consul..."
  sleep 2
done

# Register service
echo "Registering service \$SERVICE_NAME with Consul..."
curl -X PUT http://\$CONSUL_HOST:\$CONSUL_PORT/v1/agent/service/register \\
  -d '{
    "ID": "'\$SERVICE_NAME'-'\$(hostname)'",
    "Name": "'\$SERVICE_NAME'",
    "Tags": ["${service.type}", "microservice", "v1.0.0"],
    "Address": "'\$SERVICE_HOST'",
    "Port": '\$SERVICE_PORT',
    "Check": {
      "HTTP": "http://'\$SERVICE_HOST':'\$SERVICE_PORT'/health",
      "Interval": "10s",
      "Timeout": "3s"
    }
  }'

echo "Service registered successfully!"

# Deregister on exit
trap 'curl -X PUT http://\$CONSUL_HOST:\$CONSUL_PORT/v1/agent/service/deregister/'\$SERVICE_NAME'-'\$(hostname)'; echo "Service deregistered"' EXIT

# Keep script running
while true; do
  sleep 30
done`
  }

  private generateEurekaRegistration(service: any): string {
    return `#!/bin/bash

# Eureka service registration script for ${service.name}

EUREKA_HOST=\${EUREKA_HOST:-eureka}
EUREKA_PORT=\${EUREKA_PORT:-8761}
SERVICE_NAME=${service.name}
SERVICE_PORT=${service.port}
SERVICE_HOST=${service.name}
INSTANCE_ID=${service.name}-\$(hostname)

# Wait for Eureka to be available
echo "Waiting for Eureka to be available..."
while ! curl -s http://\$EUREKA_HOST:\$EUREKA_PORT/eureka/apps > /dev/null; do
  echo "Waiting for Eureka..."
  sleep 2
done

# Register service
echo "Registering service \$SERVICE_NAME with Eureka..."
curl -X POST http://\$EUREKA_HOST:\$EUREKA_PORT/eureka/apps/\$SERVICE_NAME \\
  -H "Content-Type: application/json" \\
  -d '{
    "instance": {
      "instanceId": "'\$INSTANCE_ID'",
      "hostName": "'\$SERVICE_HOST'",
      "app": "'\$SERVICE_NAME'",
      "ipAddr": "'\$SERVICE_HOST'",
      "status": "UP",
      "overriddenStatus": "UNKNOWN",
      "port": {
        "$": '\$SERVICE_PORT',
        "@enabled": "true"
      },
      "securePort": {
        "$": 443,
        "@enabled": "false"
      },
      "countryId": 1,
      "dataCenterInfo": {
        "@class": "com.netflix.appinfo.InstanceInfo\$DefaultDataCenterInfo",
        "name": "MyOwn"
      },
      "healthCheckUrl": "http://'\$SERVICE_HOST':'\$SERVICE_PORT'/health",
      "statusPageUrl": "http://'\$SERVICE_HOST':'\$SERVICE_PORT'/info",
      "homePageUrl": "http://'\$SERVICE_HOST':'\$SERVICE_PORT'/"
    }
  }'

echo "Service registered successfully!"

# Send heartbeat and deregister on exit
trap 'curl -X DELETE http://\$EUREKA_HOST:\$EUREKA_PORT/eureka/apps/'\$SERVICE_NAME'/'\$INSTANCE_ID'; echo "Service deregistered"' EXIT

# Send heartbeats
while true; do
  curl -X PUT http://\$EUREKA_HOST:\$EUREKA_PORT/eureka/apps/\$SERVICE_NAME/\$INSTANCE_ID
  sleep 30
done`
  }

  private generateEtcdRegistration(service: any): string {
    return `#!/bin/bash

# etcd service registration script for ${service.name}

ETCD_HOST=\${ETCD_HOST:-etcd}
ETCD_PORT=\${ETCD_PORT:-2379}
SERVICE_NAME=${service.name}
SERVICE_PORT=${service.port}
SERVICE_HOST=${service.name}
TTL=30

# Wait for etcd to be available
echo "Waiting for etcd to be available..."
while ! curl -s http://\$ETCD_HOST:\$ETCD_PORT/health > /dev/null; do
  echo "Waiting for etcd..."
  sleep 2
done

# Register service
echo "Registering service \$SERVICE_NAME with etcd..."

# Service configuration
etcdctl --endpoints=http://\$ETCD_HOST:\$ETCD_PORT put /services/\$SERVICE_NAME/config \\
  '{"host": "'\$SERVICE_HOST'", "port": '\$SERVICE_PORT', "type": "${service.type}", "health": "/health"}'

# Service instance
INSTANCE_KEY="/services/\$SERVICE_NAME/instances/\$(hostname)"
etcdctl --endpoints=http://\$ETCD_HOST:\$ETCD_PORT put \$INSTANCE_KEY \\
  '{"host": "'\$SERVICE_HOST'", "port": '\$SERVICE_PORT', "status": "UP", "timestamp": "'\$(date -u +%Y-%m-%dT%H:%M:%SZ)'"}'

echo "Service registered successfully!"

# Cleanup on exit
trap 'etcdctl --endpoints=http://\$ETCD_HOST:\$ETCD_PORT del '\$INSTANCE_KEY'; echo "Service deregistered"' EXIT

# Keep service alive with periodic updates
while true; do
  etcdctl --endpoints=http://\$ETCD_HOST:\$ETCD_PORT put \$INSTANCE_KEY \\
    '{"host": "'\$SERVICE_HOST'", "port": '\$SERVICE_PORT', "status": "UP", "timestamp": "'\$(date -u +%Y-%m-%dT%H:%M:%SZ)'"}' --lease=\$TTL
  sleep 20
done`
  }

  private generateZookeeperRegistration(service: any): string {
    return `#!/bin/bash

# Zookeeper service registration script for ${service.name}

ZK_HOST=\${ZK_HOST:-zookeeper}
ZK_PORT=\${ZK_PORT:-2181}
SERVICE_NAME=${service.name}
SERVICE_PORT=${service.port}
SERVICE_HOST=${service.name}

# Install zkCli if not available (for Alpine)
if ! command -v zkCli.sh &> /dev/null; then
    echo "Installing Zookeeper client..."
    apk add --no-cache openjdk11-jre
    wget -q https://archive.apache.org/dist/zookeeper/zookeeper-3.7.0/apache-zookeeper-3.7.0-bin.tar.gz
    tar -xzf apache-zookeeper-3.7.0-bin.tar.gz
    mv apache-zookeeper-3.7.0-bin /opt/zookeeper
    export PATH=\$PATH:/opt/zookeeper/bin
fi

# Wait for Zookeeper to be available
echo "Waiting for Zookeeper to be available..."
while ! echo ruok | nc \$ZK_HOST \$ZK_PORT | grep -q imok; do
  echo "Waiting for Zookeeper..."
  sleep 2
done

# Create service paths
echo "Creating service paths..."
zkCli.sh -server \$ZK_HOST:\$ZK_PORT <<EOF
create /services
create /services/\$SERVICE_NAME
create /services/\$SERVICE_NAME/config
create /services/\$SERVICE_NAME/instances
create /services/\$SERVICE_NAME/health
quit
EOF

# Register service configuration
echo "Registering service configuration..."
zkCli.sh -server \$ZK_HOST:\$ZK_PORT <<EOF
set /services/\$SERVICE_NAME/config '{"port": \$SERVICE_PORT, "type": "${service.type}", "version": "1.0.0"}'
quit
EOF

# Register service instance
INSTANCE_PATH="/services/\$SERVICE_NAME/instances/\$(hostname)"
echo "Registering service instance..."
zkCli.sh -server \$ZK_HOST:\$ZK_PORT <<EOF
create \$INSTANCE_PATH '{"host": "\$SERVICE_HOST", "port": \$SERVICE_PORT, "status": "UP", "timestamp": "\$(date -u +%Y-%m-%dT%H:%M:%SZ)"}'
quit
EOF

# Set health check endpoint
zkCli.sh -server \$ZK_HOST:\$ZK_PORT <<EOF
set /services/\$SERVICE_NAME/health '/health'
quit
EOF

echo "Service registered successfully!"

# Cleanup on exit
trap 'zkCli.sh -server \$ZK_HOST:\$ZK_PORT delete '\$INSTANCE_PATH'; echo "Service deregistered"' EXIT

# Keep service alive with periodic updates
while true; do
  zkCli.sh -server \$ZK_HOST:\$ZK_PORT <<EOF
set \$INSTANCE_PATH '{"host": "\$SERVICE_HOST", "port": \$SERVICE_PORT, "status": "UP", "timestamp": "\$(date -u +%Y-%m-%dT%H:%M:%SZ)"}'
quit
EOF
  sleep 30
done`
  }

  generateServiceDiscoveryClient(discoveryType: string, language: string): string {
    switch (language) {
      case 'javascript':
        return this.generateJSDiscoveryClient(discoveryType)
      case 'python':
        return this.generatePythonDiscoveryClient(discoveryType)
      case 'java':
        return this.generateJavaDiscoveryClient(discoveryType)
      case 'go':
        return this.generateGoDiscoveryClient(discoveryType)
      default:
        return this.generateJSDiscoveryClient(discoveryType)
    }
  }

  private generateJSDiscoveryClient(discoveryType: string): string {
    switch (discoveryType) {
      case 'consul':
        return `const axios = require('axios')

class ConsulServiceDiscovery {
  constructor(consulHost = 'consul', consulPort = 8500) {
    this.consulUrl = \`http://\${consulHost}:\${consulPort}\`
  }

  async discoverService(serviceName) {
    try {
      const response = await axios.get(\`\${this.consulUrl}/v1/health/service/\${serviceName}?passing=true\`)
      const services = response.data
      
      if (services.length === 0) {
        throw new Error(\`No healthy instances found for service: \${serviceName}\`)
      }
      
      // Return random healthy instance
      const randomIndex = Math.floor(Math.random() * services.length)
      const service = services[randomIndex].Service
      
      return {
        host: service.Address || service.ID.split('-')[0],
        port: service.Port,
        id: service.ID
      }
    } catch (error) {
      console.error('Service discovery failed:', error.message)
      throw error
    }
  }

  async getAllServices() {
    try {
      const response = await axios.get(\`\${this.consulUrl}/v1/catalog/services\`)
      return Object.keys(response.data)
    } catch (error) {
      console.error('Failed to get services:', error.message)
      throw error
    }
  }

  async getServiceHealth(serviceName) {
    try {
      const response = await axios.get(\`\${this.consulUrl}/v1/health/service/\${serviceName}\`)
      return response.data.map(service => ({
        id: service.Service.ID,
        status: service.Checks.every(check => check.Status === 'passing') ? 'healthy' : 'unhealthy',
        address: service.Service.Address,
        port: service.Service.Port
      }))
    } catch (error) {
      console.error('Failed to get service health:', error.message)
      throw error
    }
  }
}

module.exports = ConsulServiceDiscovery`

      case 'eureka':
        return `const axios = require('axios')

class EurekaServiceDiscovery {
  constructor(eurekaHost = 'eureka', eurekaPort = 8761) {
    this.eurekaUrl = \`http://\${eurekaHost}:\${eurekaPort}/eureka\`
  }

  async discoverService(serviceName) {
    try {
      const response = await axios.get(\`\${this.eurekaUrl}/apps/\${serviceName}\`, {
        headers: { 'Accept': 'application/json' }
      })
      
      const instances = response.data.application.instance
      const healthyInstances = instances.filter(instance => instance.status === 'UP')
      
      if (healthyInstances.length === 0) {
        throw new Error(\`No healthy instances found for service: \${serviceName}\`)
      }
      
      // Return random healthy instance
      const randomIndex = Math.floor(Math.random() * healthyInstances.length)
      const instance = healthyInstances[randomIndex]
      
      return {
        host: instance.hostName,
        port: instance.port['$'],
        id: instance.instanceId
      }
    } catch (error) {
      console.error('Service discovery failed:', error.message)
      throw error
    }
  }

  async getAllServices() {
    try {
      const response = await axios.get(\`\${this.eurekaUrl}/apps\`, {
        headers: { 'Accept': 'application/json' }
      })
      return response.data.applications.application.map(app => app.name)
    } catch (error) {
      console.error('Failed to get services:', error.message)
      throw error
    }
  }
}

module.exports = EurekaServiceDiscovery`

      default:
        return this.generateJSDiscoveryClient('consul')
    }
  }

  private generatePythonDiscoveryClient(discoveryType: string): string {
    switch (discoveryType) {
      case 'consul':
        return `import requests
import random
import json

class ConsulServiceDiscovery:
    def __init__(self, consul_host='consul', consul_port=8500):
        self.consul_url = f"http://{consul_host}:{consul_port}"
    
    def discover_service(self, service_name):
        try:
            response = requests.get(f"{self.consul_url}/v1/health/service/{service_name}?passing=true")
            response.raise_for_status()
            services = response.json()
            
            if not services:
                raise Exception(f"No healthy instances found for service: {service_name}")
            
            # Return random healthy instance
            service = random.choice(services)['Service']
            
            return {
                'host': service.get('Address', service['ID'].split('-')[0]),
                'port': service['Port'],
                'id': service['ID']
            }
        except Exception as e:
            print(f"Service discovery failed: {e}")
            raise
    
    def get_all_services(self):
        try:
            response = requests.get(f"{self.consul_url}/v1/catalog/services")
            response.raise_for_status()
            return list(response.json().keys())
        except Exception as e:
            print(f"Failed to get services: {e}")
            raise
    
    def get_service_health(self, service_name):
        try:
            response = requests.get(f"{self.consul_url}/v1/health/service/{service_name}")
            response.raise_for_status()
            services = response.json()
            
            return [
                {
                    'id': service['Service']['ID'],
                    'status': 'healthy' if all(check['Status'] == 'passing' for check in service['Checks']) else 'unhealthy',
                    'address': service['Service']['Address'],
                    'port': service['Service']['Port']
                }
                for service in services
            ]
        except Exception as e:
            print(f"Failed to get service health: {e}")
            raise`

      default:
        return this.generatePythonDiscoveryClient('consul')
    }
  }

  private generateJavaDiscoveryClient(discoveryType: string): string {
    return `// Service Discovery Client for ${discoveryType}
// Add to your Spring Boot application

@Component
public class ServiceDiscoveryClient {
    
    @Value("\${consul.host:consul}")
    private String consulHost;
    
    @Value("\${consul.port:8500}")
    private int consulPort;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    public ServiceInstance discoverService(String serviceName) {
        try {
            String url = String.format("http://%s:%d/v1/health/service/%s?passing=true", 
                consulHost, consulPort, serviceName);
            
            ResponseEntity<ConsulService[]> response = restTemplate.getForEntity(url, ConsulService[].class);
            ConsulService[] services = response.getBody();
            
            if (services == null || services.length == 0) {
                throw new ServiceDiscoveryException("No healthy instances found for service: " + serviceName);
            }
            
            // Return random healthy instance
            ConsulService service = services[new Random().nextInt(services.length)];
            
            return new ServiceInstance(
                service.getService().getAddress(),
                service.getService().getPort(),
                service.getService().getId()
            );
        } catch (Exception e) {
            throw new ServiceDiscoveryException("Service discovery failed", e);
        }
    }
    
    public List<String> getAllServices() {
        try {
            String url = String.format("http://%s:%d/v1/catalog/services", consulHost, consulPort);
            ResponseEntity<Map<String, String[]>> response = restTemplate.getForEntity(url, 
                new ParameterizedTypeReference<Map<String, String[]>>() {});
            
            return new ArrayList<>(response.getBody().keySet());
        } catch (Exception e) {
            throw new ServiceDiscoveryException("Failed to get services", e);
        }
    }
}

// Data classes
public class ServiceInstance {
    private String host;
    private int port;
    private String id;
    
    // constructors, getters, setters
}

public class ServiceDiscoveryException extends RuntimeException {
    public ServiceDiscoveryException(String message) {
        super(message);
    }
    
    public ServiceDiscoveryException(String message, Throwable cause) {
        super(message, cause);
    }
}`
  }

  private generateGoDiscoveryClient(discoveryType: string): string {
    return `package discovery

import (
    "encoding/json"
    "fmt"
    "math/rand"
    "net/http"
    "time"
)

type ServiceDiscovery struct {
    consulHost string
    consulPort int
    client     *http.Client
}

type ServiceInstance struct {
    Host string \`json:"host"\`
    Port int    \`json:"port"\`
    ID   string \`json:"id"\`
}

type ConsulService struct {
    Service struct {
        ID      string \`json:"ID"\`
        Address string \`json:"Address"\`
        Port    int    \`json:"Port"\`
    } \`json:"Service"\`
    Checks []struct {
        Status string \`json:"Status"\`
    } \`json:"Checks"\`
}

func NewServiceDiscovery(consulHost string, consulPort int) *ServiceDiscovery {
    return &ServiceDiscovery{
        consulHost: consulHost,
        consulPort: consulPort,
        client:     &http.Client{Timeout: 10 * time.Second},
    }
}

func (sd *ServiceDiscovery) DiscoverService(serviceName string) (*ServiceInstance, error) {
    url := fmt.Sprintf("http://%s:%d/v1/health/service/%s?passing=true", 
        sd.consulHost, sd.consulPort, serviceName)
    
    resp, err := sd.client.Get(url)
    if err != nil {
        return nil, fmt.Errorf("service discovery failed: %w", err)
    }
    defer resp.Body.Close()
    
    var services []ConsulService
    if err := json.NewDecoder(resp.Body).Decode(&services); err != nil {
        return nil, fmt.Errorf("failed to decode response: %w", err)
    }
    
    if len(services) == 0 {
        return nil, fmt.Errorf("no healthy instances found for service: %s", serviceName)
    }
    
    // Return random healthy instance
    service := services[rand.Intn(len(services))]
    
    host := service.Service.Address
    if host == "" {
        host = serviceName // fallback to service name
    }
    
    return &ServiceInstance{
        Host: host,
        Port: service.Service.Port,
        ID:   service.Service.ID,
    }, nil
}

func (sd *ServiceDiscovery) GetAllServices() ([]string, error) {
    url := fmt.Sprintf("http://%s:%d/v1/catalog/services", sd.consulHost, sd.consulPort)
    
    resp, err := sd.client.Get(url)
    if err != nil {
        return nil, fmt.Errorf("failed to get services: %w", err)
    }
    defer resp.Body.Close()
    
    var services map[string][]string
    if err := json.NewDecoder(resp.Body).Decode(&services); err != nil {
        return nil, fmt.Errorf("failed to decode response: %w", err)
    }
    
    var serviceNames []string
    for name := range services {
        serviceNames = append(serviceNames, name)
    }
    
    return serviceNames, nil
}`
  }
}