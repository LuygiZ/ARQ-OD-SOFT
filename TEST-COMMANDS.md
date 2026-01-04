# Quick Test Commands

## Immediate Testing (Copy & Paste)

### 1. Build Everything
```bash
# Build shared kernel + all services
mvn clean install -DskipTests
```

### 2. Run Static Analysis
```bash
# Checkstyle
mvn checkstyle:check

# View report
start target/checkstyle-result.xml  # Windows
# or
open target/checkstyle-result.xml   # Mac/Linux
```

### 3. Run All Tests
```bash
# Unit tests
mvn test

# Integration tests
mvn failsafe:integration-test failsafe:verify

# Mutation tests (takes 5-10 minutes)
mvn org.pitest:pitest-maven:mutationCoverage

# View mutation report
start target/pit-reports/index.html  # Windows
# or
open target/pit-reports/index.html   # Mac/Linux
```

### 4. Build Docker Images
```bash
# Using helper script
bash scripts/build-all.sh dev localhost:5000 1.0.0

# Or manually for one service
cd genre-service
docker build -t genre-service:dev .
cd ..
```

### 5. Start Development Environment
```bash
# Start all services + infrastructure
docker-compose -f docker-compose-dev.yml up -d

# Wait for startup (important!)
sleep 60

# Check running containers
docker-compose -f docker-compose-dev.yml ps

# View logs
docker-compose -f docker-compose-dev.yml logs -f genre-service
```

### 6. Verify Services Are Running
```bash
# Run smoke tests
bash scripts/testing/smoke-test.sh dev http://localhost

# Or test individually
curl http://localhost:8080/actuator/health  # genre-service
curl http://localhost:8082/actuator/health  # author-service
curl http://localhost:8083/actuator/health  # book-command
curl http://localhost:8085/actuator/health  # book-query
curl http://localhost:8086/actuator/health  # lending
curl http://localhost:8087/actuator/health  # reader
curl http://localhost:8084/actuator/health  # saga
```

### 7. Access Swagger UIs
```bash
# Open in browser (Windows)
start http://localhost:8080/swagger-ui/index.html  # genre
start http://localhost:8082/swagger-ui/index.html  # author
start http://localhost:8083/swagger-ui/index.html  # book-command
start http://localhost:8085/swagger-ui/index.html  # book-query

# Or for Mac/Linux
open http://localhost:8080/swagger-ui/index.html
```

### 8. Test Staging Environment
```bash
# Build images
bash scripts/build-all.sh staging localhost:5000 staging

# Start staging
docker-compose -f docker-compose-staging.yml up -d

# Wait for startup
sleep 90

# Smoke tests
bash scripts/testing/smoke-test.sh staging http://localhost

# Load tests
bash scripts/testing/load-test.sh http://localhost:8180/api/genres 50 1000

# View Traefik dashboard
start http://localhost:8190  # Windows
# or
open http://localhost:8190   # Mac/Linux
```

### 9. Test Production (Docker Swarm)
```bash
# Initialize Swarm (one-time)
docker swarm init

# Deploy
export VERSION=1.0.0
export DOCKER_REGISTRY=localhost:5000
docker stack deploy -c docker-swarm-stack.yml lms

# Check services
docker stack services lms
docker stack ps lms

# View logs
docker service logs lms_genre-service -f
```

### 10. Test Deployment Strategies

#### Canary Deployment
```bash
bash scripts/deployment/canary-deploy.sh genre-service v1.1.0 1 3
```

#### Blue-Green Deployment
```bash
bash scripts/deployment/blue-green-deploy.sh author-service v1.1.0 3
```

#### Auto-Scaling
```bash
# Monitor and auto-scale (runs continuously)
bash scripts/deployment/auto-scale.sh genre-service 2 5 70 80
```

## Cleanup Commands

### Stop Development
```bash
docker-compose -f docker-compose-dev.yml down
```

### Stop Staging
```bash
docker-compose -f docker-compose-staging.yml down
```

### Stop Production
```bash
docker stack rm lms
```

### Clean Everything
```bash
# Stop all containers
docker stop $(docker ps -aq)

# Remove all containers
docker rm $(docker ps -aq)

# Remove all volumes
docker volume prune -f

# Remove all networks
docker network prune -f

# Remove all images
docker rmi $(docker images -q) -f
```

## Evidence Collection Commands

### Screenshots for Report

#### 1. Pipeline Logs
```bash
# If you setup Jenkins, capture console output
# Otherwise show mvn test output
mvn test > test-output.txt
```

#### 2. Test Reports
```bash
# JUnit reports
ls -la target/surefire-reports/

# JaCoCo coverage
start target/site/jacoco/index.html

# PIT mutation
start target/pit-reports/index.html

# Checkstyle
start target/checkstyle-result.xml
```

#### 3. Docker Images
```bash
# List all built images
docker images | grep -E "genre|author|book|lending|reader|saga"

# Or on Windows
docker images | findstr "genre author book lending reader saga"
```

#### 4. Running Services
```bash
# Development
docker-compose -f docker-compose-dev.yml ps

# Staging
docker-compose -f docker-compose-staging.yml ps

# Production
docker stack services lms
docker stack ps lms
```

#### 5. Load Test Results
```bash
# Results are saved in load-test-results/
ls -la load-test-results/

# View latest
cat load-test-results/load-test-*.txt | tail -50
```

## Common Issues & Fixes

### Issue: Port already in use
```bash
# Windows - kill process on port 8080
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8080 | xargs kill -9
```

### Issue: Docker out of memory
```bash
# Increase Docker memory in Docker Desktop settings
# Or prune unused data
docker system prune -af --volumes
```

### Issue: Services not starting
```bash
# Check logs
docker-compose -f docker-compose-dev.yml logs

# Restart specific service
docker-compose -f docker-compose-dev.yml restart genre-service

# Rebuild and restart
docker-compose -f docker-compose-dev.yml up -d --build genre-service
```

### Issue: Maven build fails
```bash
# Clean and rebuild
mvn clean install -DskipTests -U

# Skip problematic modules
mvn clean install -DskipTests -pl '!book-command-service'
```

## Performance Benchmarks

### Expected Results (Reference)

#### Unit Tests
- Execution time: 2-5 minutes
- Success rate: 100%
- Coverage: >70%

#### Mutation Tests
- Execution time: 5-10 minutes per service
- Mutation score: >60%

#### Load Tests (Staging - 2 replicas)
- Throughput: 100-150 req/sec
- P95 latency: <500ms
- Failed requests: 0%

#### Startup Times
- Development: 60 seconds
- Staging: 90 seconds
- Production: 120 seconds

## Quick Reference URLs

### Development
- Genre: http://localhost:8080/swagger-ui/index.html
- Author: http://localhost:8082/swagger-ui/index.html
- Book Command: http://localhost:8083/swagger-ui/index.html
- Book Query: http://localhost:8085/swagger-ui/index.html
- Lending: http://localhost:8086/swagger-ui/index.html
- Reader: http://localhost:8087/swagger-ui/index.html
- Saga: http://localhost:8084/swagger-ui/index.html
- RabbitMQ: http://localhost:15672 (guest/guest)

### Staging
- API Gateway: http://localhost:8180
- Traefik Dashboard: http://localhost:8190

### Production
- API Gateway: http://localhost:80
- Traefik Dashboard: http://localhost:8080

## Video Demo Script

### Part 1: CI/CD Pipeline (5 minutes)
1. Show file structure
2. Run `mvn clean install`
3. Show test reports
4. Show mutation reports
5. Build Docker images

### Part 2: Deployment (5 minutes)
1. Start dev environment
2. Run smoke tests
3. Show Swagger UIs
4. Deploy to staging
5. Run load tests

### Part 3: Advanced Deployments (5 minutes)
1. Initialize Swarm
2. Show canary deployment
3. Show blue-green deployment
4. Demonstrate rollback
5. Show auto-scaling

### Part 4: Monitoring (2 minutes)
1. Show health checks
2. Show service replicas
3. Show logs
4. Show Traefik dashboard

## Final Checklist

Before submitting:
- [ ] All services build successfully
- [ ] Unit tests pass (>70% coverage)
- [ ] Mutation tests pass (>60% score)
- [ ] Checkstyle passes
- [ ] Docker images built
- [ ] Development environment works
- [ ] Staging environment works
- [ ] Production deployment works
- [ ] All 3 deployment strategies tested
- [ ] Smoke tests pass
- [ ] Load tests executed
- [ ] Screenshots captured
- [ ] Documentation complete
- [ ] Video demo recorded (optional)

---

**Tip:** Copy these commands into a script file for easy execution!
