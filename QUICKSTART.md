# LMS Microservices - Quick Start Guide

## Prerequisites
- Docker Desktop / Docker Engine
- Java 21
- Maven 3.9+
- Git

## Quick Setup (5 minutes)

### 1. Clone and Build
```bash
# Clone repository
git clone <your-repo-url>
cd ARQ-OD-SOFT

# Build all services
mvn clean install -DskipTests
```

### 2. Run Development Environment
```bash
# Start all services with Docker Compose
docker-compose -f docker-compose-dev.yml up -d

# Wait 60 seconds for services to start
sleep 60

# Verify all services are running
bash scripts/testing/smoke-test.sh dev http://localhost
```

### 3. Access Services
- **Genre Service:** http://localhost:8080/swagger-ui/index.html
- **Author Service:** http://localhost:8082/swagger-ui/index.html
- **Book Command:** http://localhost:8083/swagger-ui/index.html
- **Book Query:** http://localhost:8085/swagger-ui/index.html
- **Lending Service:** http://localhost:8086/swagger-ui/index.html
- **Reader Service:** http://localhost:8087/swagger-ui/index.html
- **Saga Orchestrator:** http://localhost:8084/swagger-ui/index.html

### 4. Access Infrastructure
- **PostgreSQL:** localhost:5432 (user: postgres, password: password)
- **MongoDB:** localhost:27017 (user: admin, password: admin123)
- **Redis:** localhost:6379
- **RabbitMQ Management:** http://localhost:15672 (user: guest, password: guest)

## Common Tasks

### Run All Tests
```bash
# Unit + Integration + Mutation + CDC
mvn clean verify

# View coverage report
open target/site/jacoco/index.html

# View mutation report
open target/pit-reports/index.html
```

### Build Docker Images
```bash
# Build all services
for service in genre-service author-service book-command-service book-query-service lending-service reader-service saga-orchestrator; do
    cd $service
    docker build -t $service:dev .
    cd ..
done
```

### Deploy to Staging
```bash
# Build and push images (if using registry)
docker-compose -f docker-compose-staging.yml build

# Start staging environment
docker-compose -f docker-compose-staging.yml up -d

# Run smoke tests
bash scripts/testing/smoke-test.sh staging http://localhost

# Run load tests
bash scripts/testing/load-test.sh http://localhost:8180/api/genres 50 1000
```

### Deploy to Production (Docker Swarm)
```bash
# Initialize Swarm (one-time)
docker swarm init

# Deploy stack
export VERSION=1.0.0
export DOCKER_REGISTRY=localhost:5000
docker stack deploy -c docker-swarm-stack.yml lms

# Verify deployment
docker stack services lms
docker stack ps lms

# Monitor logs
docker service logs lms_genre-service -f
```

### Deployment Strategies

#### Rolling Update
```bash
# Default strategy - automatic
docker stack deploy -c docker-swarm-stack.yml lms
```

#### Blue-Green
```bash
bash scripts/deployment/blue-green-deploy.sh author-service v1.2.0 3
```

#### Canary
```bash
bash scripts/deployment/canary-deploy.sh genre-service v1.2.0 1 3
```

### Auto-Scaling
```bash
# Monitor and auto-scale based on CPU/Memory
bash scripts/deployment/auto-scale.sh genre-service 2 5 70 80
```

### Rollback
```bash
# Automatic rollback on health check failure
bash scripts/deployment/rollback.sh genre-service 10 3

# Manual rollback via Docker
docker service rollback genre-service
```

## Troubleshooting

### Services won't start
```bash
# Check logs
docker-compose -f docker-compose-dev.yml logs -f

# Restart specific service
docker-compose -f docker-compose-dev.yml restart genre-service

# Rebuild and restart
docker-compose -f docker-compose-dev.yml up -d --build genre-service
```

### Database connection issues
```bash
# Check PostgreSQL is running
docker ps | grep postgres

# Test connection
docker exec -it postgres_dev psql -U postgres -c "SELECT 1"

# View logs
docker logs postgres_dev
```

### Clean restart
```bash
# Stop everything
docker-compose -f docker-compose-dev.yml down -v

# Remove all containers and volumes
docker system prune -af --volumes

# Start fresh
docker-compose -f docker-compose-dev.yml up -d
```

## CI/CD Pipeline

### Jenkins Setup
1. Create new Pipeline job
2. Point to `Jenkinsfile-microservices`
3. Configure parameters:
   - DEPLOYMENT_STRATEGY: rolling/blue-green/canary
   - ENVIRONMENT: dev/staging/prod
   - DOCKER_REGISTRY: your-registry:5000
   - NOTIFICATION_EMAIL: your-email@example.com

### Manual Pipeline Run
```bash
# Trigger Jenkins build
curl -X POST http://jenkins:8080/job/lms-microservices/build \
  --user admin:token \
  --data-urlencode json='{"parameter": [{"name":"ENVIRONMENT", "value":"staging"}]}'
```

## Performance Benchmarks

### Expected Performance (Staging - 2 replicas)
- **Throughput:** 100-150 req/sec
- **Response Time (P95):** < 500ms
- **Failed Requests:** 0%
- **CPU Usage:** 40-60%
- **Memory Usage:** 300-400MB per instance

### Scaling Thresholds
- **Scale UP:** CPU > 70% OR Memory > 80%
- **Scale DOWN:** CPU < 35% AND Memory < 40%
- **Min Replicas:** 2
- **Max Replicas:** 5

## Project Structure
```
ARQ-OD-SOFT/
├── genre-service/          # Genre management service
├── author-service/         # Author management (CQRS)
├── book-command-service/   # Book writes (CQRS)
├── book-query-service/     # Book reads (CQRS)
├── lending-service/        # Lending management
├── reader-service/         # Reader & auth
├── saga-orchestrator/      # Saga coordination
├── shared-kernel/          # Shared DTOs and utilities
├── infrastructure/         # Database init scripts
├── scripts/
│   ├── deployment/        # Deployment scripts
│   └── testing/          # Test scripts
├── Docs/                  # Documentation
├── docker-compose-dev.yml
├── docker-compose-staging.yml
├── docker-swarm-stack.yml
├── Jenkinsfile-microservices
├── checkstyle.xml
└── pom.xml               # Parent POM

```

## Next Steps
1. Review [DEPLOYMENT-GUIDE.md](Docs/DEPLOYMENT-GUIDE.md) for detailed documentation
2. Configure Jenkins pipeline
3. Set up Docker registry
4. Configure SonarQube
5. Set up monitoring (Prometheus + Grafana recommended)
6. Configure email notifications in Jenkins

## Support
- Full documentation: [Docs/DEPLOYMENT-GUIDE.md](Docs/DEPLOYMENT-GUIDE.md)
- Issues: GitHub Issues
- Contact: devops@example.com
