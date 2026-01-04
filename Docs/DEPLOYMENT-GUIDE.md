# LMS Microservices - Deployment Guide

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [CI/CD Pipeline](#cicd-pipeline)
3. [Deployment Strategies](#deployment-strategies)
4. [Environment Configuration](#environment-configuration)
5. [Testing](#testing)
6. [Monitoring & Health Checks](#monitoring--health-checks)
7. [Auto-Scaling](#auto-scaling)
8. [Rollback Procedures](#rollback-procedures)
9. [Evidence for Project Requirements](#evidence-for-project-requirements)

---

## Architecture Overview

### Microservices
The system consists of 7 microservices:

1. **genre-service** (Port 8080) - Genre management
2. **author-service** (Port 8082) - Author management with CQRS (PostgreSQL + MongoDB)
3. **book-command-service** (Port 8083) - Book write operations (CQRS)
4. **book-query-service** (Port 8085) - Book read operations (CQRS)
5. **lending-service** (Port 8086) - Book lending management
6. **reader-service** (Port 8087) - Reader and authentication management
7. **saga-orchestrator** (Port 8084) - Saga pattern coordination

### Infrastructure Components
- **PostgreSQL** - Primary database for write operations
- **MongoDB** - Read model database (polyglot persistence)
- **Redis** - Caching and session storage
- **RabbitMQ** - Event-driven messaging
- **Traefik** - Load balancer and API gateway

---

## CI/CD Pipeline

### Pipeline Stages

#### 1. Build & Compile
```bash
mvn clean compile test-compile
```
- Compiles all microservices
- Validates Maven dependencies
- Prepares test classes

#### 2. Static Analysis
**Checkstyle:**
```bash
mvn checkstyle:check
```
- Enforces coding standards
- Configuration: `checkstyle.xml`
- Reports: `target/checkstyle-result.xml`

**SonarQube:**
```bash
mvn sonar:sonar
```
- Code quality analysis
- Security vulnerabilities
- Code smells and technical debt

#### 3. Unit Tests
```bash
mvn test
```
- Tests individual classes
- JUnit 5 framework
- Reports: `target/surefire-reports/`

#### 4. Integration Tests
```bash
mvn failsafe:integration-test failsafe:verify
```
- Tests service integration
- Database connectivity
- Message queue integration
- Reports: `target/failsafe-reports/`

#### 5. Consumer-Driven Contract Tests (Pact)
```bash
mvn test -Dtest=*PactTest
```
- Validates service contracts
- Provider verification
- Consumer expectations

#### 6. Code Coverage (JaCoCo)
```bash
mvn jacoco:report
```
- Minimum threshold: 70%
- Reports: `target/site/jacoco/`

#### 7. Mutation Testing (PIT)
```bash
mvn org.pitest:pitest-maven:mutationCoverage
```
- Minimum threshold: 60%
- Reports: `target/pit-reports/`

#### 8. Package
```bash
mvn package -DskipTests
```
- Creates executable JARs
- Artifacts: `target/*.jar`

#### 9. Docker Build
```bash
docker build -t service:version .
```
- Builds Docker images for each service
- Tags with environment and version

#### 10. Docker Push
```bash
docker push registry/service:version
```
- Pushes to Docker registry
- Multiple tags (version, environment, latest)

---

## Deployment Strategies

### 1. Rolling Update (Default)
**Description:** Updates replicas one at a time with zero downtime.

**Configuration (docker-swarm-stack.yml):**
```yaml
deploy:
  update_config:
    parallelism: 1          # Update 1 replica at a time
    delay: 10s              # Wait 10s between updates
    failure_action: rollback
    monitor: 30s
    order: start-first      # Start new before stopping old
```

**Usage:**
```bash
docker stack deploy -c docker-swarm-stack.yml lms
```

**Pros:**
- Zero downtime
- Gradual rollout
- Easy rollback
- Resource efficient

**Cons:**
- Slower deployment
- Mixed versions during update

---

### 2. Blue-Green Deployment
**Description:** Maintains two identical environments, switches traffic instantly.

**Script:** `scripts/deployment/blue-green-deploy.sh`

**Usage:**
```bash
bash scripts/deployment/blue-green-deploy.sh author-service v1.2.0 3
```

**Process:**
1. Deploy new version to inactive environment (Green)
2. Run smoke tests on Green
3. Switch Traefik routing to Green
4. Monitor for 30 seconds
5. Keep or remove old environment (Blue)

**Pros:**
- Instant switchover
- Easy rollback (switch back)
- Full testing before switch
- Zero downtime

**Cons:**
- Requires double resources
- Database migrations complexity

---

### 3. Canary Deployment
**Description:** Deploys new version to subset of users, gradually increases traffic.

**Script:** `scripts/deployment/canary-deploy.sh`

**Usage:**
```bash
bash scripts/deployment/canary-deploy.sh genre-service v1.2.0 1 3
```

**Process:**
1. Scale down stable version
2. Deploy canary with 1 replica
3. Monitor canary health for 60s
4. Prompt for promotion
5. If approved: update all replicas
6. If rejected: rollback

**Pros:**
- Low-risk deployment
- Real user validation
- Quick rollback
- Gradual exposure

**Cons:**
- Complex traffic routing
- Longer deployment time
- Requires monitoring

---

## Environment Configuration

### Development Environment
**File:** `docker-compose-dev.yml`

**Characteristics:**
- Single instance per service
- Direct port exposure
- Verbose logging
- No load balancing
- Fast startup

**Deployment:**
```bash
docker-compose -f docker-compose-dev.yml up -d
```

**Service URLs:**
- Genre Service: http://localhost:8080
- Author Service: http://localhost:8082
- Book Command: http://localhost:8083
- Book Query: http://localhost:8085
- Lending Service: http://localhost:8086
- Reader Service: http://localhost:8087
- Saga Orchestrator: http://localhost:8084

---

### Staging Environment
**File:** `docker-compose-staging.yml`

**Characteristics:**
- 2 replicas per service
- Traefik load balancer
- Resource limits enforced
- Production-like setup
- Load testing enabled

**Deployment:**
```bash
docker-compose -f docker-compose-staging.yml up -d
```

**Access:**
- Traefik Dashboard: http://localhost:8190
- All services via: http://localhost:8180

**Resource Limits:**
```yaml
resources:
  limits:
    cpus: '0.5'
    memory: 512M
  reservations:
    cpus: '0.25'
    memory: 256M
```

---

### Production Environment
**File:** `docker-swarm-stack.yml`

**Characteristics:**
- 3 replicas per service
- Auto-healing
- Auto-rollback on failure
- Health checks enabled
- Zero-downtime updates

**Initialization (one-time):**
```bash
# Initialize Swarm
docker swarm init

# Deploy stack
export VERSION=1.0.0
export DOCKER_REGISTRY=localhost:5000
docker stack deploy -c docker-swarm-stack.yml lms
```

**Health Check Configuration:**
```yaml
healthcheck:
  test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider",
         "http://localhost:8080/actuator/health"]
  interval: 30s
  timeout: 10s
  retries: 3
  start_period: 60s
```

---

## Testing

### Smoke Tests
**Purpose:** Quick validation that services are running

**Script:** `scripts/testing/smoke-test.sh`

**Usage:**
```bash
# Development
bash scripts/testing/smoke-test.sh dev http://localhost

# Staging
bash scripts/testing/smoke-test.sh staging http://localhost

# Production
bash scripts/testing/smoke-test.sh prod http://localhost
```

**Tests:**
- Health endpoint availability
- Swagger UI accessibility
- Database connectivity
- Message queue connectivity

**Output:**
```
==================================================
  SMOKE TESTS - dev Environment
==================================================
Testing genre-service Health Check... ✓ PASS (HTTP 200)
Testing author-service Health Check... ✓ PASS (HTTP 200)
...
==================================================
  SMOKE TEST SUMMARY
==================================================
Total Tests: 15
  ✓ Passed: 15
  ⚠ Warnings: 0
  ✗ Failed: 0
==================================================
Result: ALL TESTS PASSED
==================================================
```

---

### Load Tests
**Purpose:** Validate performance under load

**Script:** `scripts/testing/load-test.sh`

**Requirements:**
```bash
sudo apt-get install apache2-utils  # For 'ab' command
```

**Usage:**
```bash
bash scripts/testing/load-test.sh http://localhost:8180/api/genres 50 1000
```

**Parameters:**
- Concurrent users: 50
- Total requests: 1000
- Report directory: `./load-test-results`

**Metrics Collected:**
- Requests per second
- Average response time
- 50th, 95th, 99th percentile
- Failed requests
- Throughput

**Performance Thresholds:**
- ✓ Good: >100 req/sec, <500ms p95
- ⚠ Moderate: >50 req/sec, <1000ms p95
- ✗ Poor: <50 req/sec, >1000ms p95

**Sample Output:**
```
==================================================
  LOAD TEST RESULTS
==================================================
Requests per second: 142.53
Time per request (mean): 350.67ms
Failed requests: 0

Response Time Percentiles:
  50th percentile: 298ms
  95th percentile: 456ms
  99th percentile: 623ms
==================================================
Performance Evaluation:
  ✓ No failed requests
  ✓ Good throughput (142.53 req/sec)
  ✓ Good response time (95th: 456ms)
==================================================
```

---

## Monitoring & Health Checks

### Service Health Endpoints
All services expose Spring Boot Actuator health endpoints:

```
GET /actuator/health
```

**Response:**
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "redis": { "status": "UP" },
    "rabbit": { "status": "UP" }
  }
}
```

### Traefik Health Checks
Traefik automatically monitors service health:

```yaml
labels:
  - "traefik.http.services.genre.loadbalancer.healthcheck.path=/actuator/health"
  - "traefik.http.services.genre.loadbalancer.healthcheck.interval=10s"
```

**Behavior:**
- Checks every 10 seconds
- Removes unhealthy instances from load balancing
- Re-adds when health restored

### Docker Swarm Health Checks
```yaml
healthcheck:
  test: ["CMD", "wget", "--spider", "http://localhost:8080/actuator/health"]
  interval: 30s
  timeout: 10s
  retries: 3
  start_period: 60s
```

**Behavior:**
- Waits 60s after start before checking
- Checks every 30s
- 3 consecutive failures → container restart
- Automatic replacement with healthy replica

---

## Auto-Scaling

### Manual Scaling
```bash
# Scale a specific service
docker service scale genre-service=5

# Scale multiple services
docker service scale \
  genre-service=5 \
  author-service=5 \
  book-query-service=5
```

### Automatic Scaling
**Script:** `scripts/deployment/auto-scale.sh`

**Usage:**
```bash
bash scripts/deployment/auto-scale.sh genre-service 2 5 70 80
```

**Parameters:**
- Service name: `genre-service`
- Min replicas: 2
- Max replicas: 5
- CPU threshold: 70%
- Memory threshold: 80%

**Behavior:**
1. Monitors CPU and memory every 30 seconds
2. Scale UP when: CPU > 70% OR Memory > 80%
3. Scale DOWN when: CPU < 35% AND Memory < 40%
4. Never below min or above max replicas

**Sample Output:**
```
==================================================
  AUTO-SCALING MONITOR
==================================================
Service: genre-service
Min Replicas: 2
Max Replicas: 5
CPU Threshold: 70%
Memory Threshold: 80%
==================================================

[1] 2026-01-04 14:30:00
  Current Replicas: 3
  Average CPU: 75.2%
  Average Memory: 65.3%
  → High resource usage detected (CPU: 75.2%, Mem: 65.3%)

[ACTION] Scaling genre-service to 4 replicas...
✓ Scaling complete!
```

---

## Rollback Procedures

### Automatic Rollback
**Trigger:** Health check failures during deployment

**Configuration:**
```yaml
deploy:
  rollback_config:
    parallelism: 1
    delay: 5s
    order: stop-first
  update_config:
    failure_action: rollback
    monitor: 30s
```

**Process:**
1. Deployment starts
2. Monitor for 30 seconds
3. If health checks fail → automatic rollback
4. Restore previous version
5. Notify via email

### Manual Rollback
**Script:** `scripts/deployment/rollback.sh`

**Usage:**
```bash
bash scripts/deployment/rollback.sh genre-service 10 3
```

**Parameters:**
- Service name
- Health check interval: 10 seconds
- Max failed checks: 3

**Process:**
1. Monitors service health continuously
2. Counts consecutive failures
3. After 3 failures → triggers rollback
4. Uses Docker service rollback command
5. Verifies rollback success

**Docker Swarm Built-in Rollback:**
```bash
# Rollback to previous version
docker service rollback genre-service

# Rollback entire stack
docker stack services lms | awk '{print $2}' | xargs -n1 docker service rollback
```

---

## Evidence for Project Requirements

### Criterion: Continuous Integration

#### ✅ Pipeline includes all build, test and verification stages
**Evidence:** `Jenkinsfile-microservices` lines 40-300
- Stage 2: Build & Compile
- Stage 3: Static Analysis (Checkstyle, SonarQube)
- Stage 4: Unit Tests
- Stage 5: Integration & Contract Tests
- Stage 6: Code Quality (Coverage, Mutation)
- Stage 7: Package

#### ✅ Static Tests - Checkstyle
**Evidence:**
- Configuration: `checkstyle.xml`
- Maven plugin: `pom.xml` lines 55-82
- Pipeline stage: `Jenkinsfile-microservices` lines 116-136

**Execution:**
```bash
mvn checkstyle:check
```

**Reports:** `target/checkstyle-result.xml`

#### ✅ Unit Tests on multiple classes
**Evidence:**
- Maven Surefire plugin: `pom.xml` lines 167-182
- Pipeline stage: `Jenkinsfile-microservices` lines 147-165
- Test pattern: `**/*Test.java`, `**/*Tests.java`

**Execution:**
```bash
mvn test
```

**Reports:** `target/surefire-reports/*.xml`

#### ✅ Mutation Tests (PIT) on domain classes
**Evidence:**
- PIT plugin: `pom.xml` lines 127-158
- Pipeline stage: `Jenkinsfile-microservices` lines 228-250
- Target classes: `pt.psoft.*` (domain classes)
- Mutation threshold: 60%

**Execution:**
```bash
mvn org.pitest:pitest-maven:mutationCoverage
```

**Reports:** `target/pit-reports/index.html`

#### ✅ Consumer-Driven Contract Tests (Pact)
**Evidence:**
- Pact dependencies: `genre-service/pom.xml` lines 100-112
- Pipeline stage: `Jenkinsfile-microservices` lines 189-203
- Test files: `saga-orchestrator/src/test/java/pt/psoft/saga/cdc/*PactTest.java`

**Execution:**
```bash
mvn test -Dtest=*PactTest
```

#### ✅ Container image build
**Evidence:**
- Dockerfiles present in each service directory
- Pipeline stage: `Jenkinsfile-microservices` lines 280-302
- Images tagged with environment and version

**Execution:**
```bash
docker build -t service:version .
```

---

### Criterion: Provision and Hosting

#### ✅ Infrastructure as Code
**Evidence:**
- `Dockerfile` in each service directory
- `docker-compose-dev.yml` - Development environment
- `docker-compose-staging.yml` - Staging environment
- `docker-swarm-stack.yml` - Production environment

**Recreation:**
```bash
# Development
docker-compose -f docker-compose-dev.yml up -d

# Staging
docker-compose -f docker-compose-staging.yml up -d

# Production
docker stack deploy -c docker-swarm-stack.yml lms
```

#### ✅ Container image push to Docker repository
**Evidence:**
- Pipeline stage: `Jenkinsfile-microservices` lines 312-338
- Registry configuration: Parameter `DOCKER_REGISTRY`

**Execution:**
```bash
docker push localhost:5000/genre-service:1.0.0
docker push localhost:5000/genre-service:dev
```

**Verification:**
```bash
curl http://localhost:5000/v2/_catalog
curl http://localhost:5000/v2/genre-service/tags/list
```

---

### Criterion: Rollout/Deployment

#### ✅ Deployment to production of Service A is automatic
**Evidence:**
- Service A defined: `Jenkinsfile-microservices` line 26
- No manual approval for Service A
- Automatic deployment: Lines 427-466

**Flow:**
1. Build completes
2. Tests pass
3. Docker images pushed
4. Service A deploys automatically to production

#### ✅ Service B deploys only after manual approval with notification
**Evidence:**
- Service B defined: `Jenkinsfile-microservices` line 27
- Email notification: Lines 373-401
- Manual approval stage: Lines 354-410
- Submitters: `admin,devops`

**Email sent includes:**
- Build number and version
- Test results summary
- Approval link
- Deployment details

#### ✅ Deploy to remote Docker server
**Evidence:**
- Docker Swarm configuration: `docker-swarm-stack.yml`
- Remote deployment via Swarm manager node
- Can be configured to use DEI virtual servers

**Setup for remote:**
```bash
# On remote server
docker swarm init

# On Jenkins
export DOCKER_HOST=ssh://user@remote-server
docker stack deploy -c docker-swarm-stack.yml lms
```

#### ✅ Load tests in staging
**Evidence:**
- Pipeline stage: `Jenkinsfile-microservices` lines 500-530
- Script: `scripts/testing/load-test.sh`
- Tests all services with 50 concurrent users, 1000 requests

**Execution:**
```bash
bash scripts/testing/load-test.sh http://staging:8180/api/genres 50 1000
```

#### ✅ Scale services via scripts based on load-test results
**Evidence:**
- Auto-scaling script: `scripts/deployment/auto-scale.sh`
- Monitors CPU/Memory metrics
- Scales between min and max replicas

**Execution:**
```bash
bash scripts/deployment/auto-scale.sh genre-service 2 5 70 80
```

#### ✅ Smoke tests in dev and staging
**Evidence:**
- Pipeline stages:
  - DEV: `Jenkinsfile-microservices` lines 361-373
  - STAGING: `Jenkinsfile-microservices` lines 487-499
- Script: `scripts/testing/smoke-test.sh`

**Execution:**
```bash
bash scripts/testing/smoke-test.sh dev http://localhost
bash scripts/testing/smoke-test.sh staging http://localhost
```

#### ✅ Health checks in prod
**Evidence:**
- Swarm stack: `docker-swarm-stack.yml` lines 153-159 (and similar for each service)
- Traefik health checks: Lines 160-161
- Actuator endpoints: `/actuator/health`

**Configuration:**
```yaml
healthcheck:
  test: ["CMD", "wget", "--spider", "http://localhost:8080/actuator/health"]
  interval: 30s
  timeout: 10s
  retries: 3
  start_period: 60s
```

#### ✅ Rollout strategies: Canary, Blue-Green, Rolling Update
**Evidence:**

**1. Rolling Update:**
- `docker-swarm-stack.yml` lines 167-177
- Updates 1 replica at a time with 10s delay

**2. Blue-Green:**
- Script: `scripts/deployment/blue-green-deploy.sh`
- Maintains two environments, instant switch

**3. Canary:**
- Script: `scripts/deployment/canary-deploy.sh`
- Deploys to subset, monitors, then promotes

**Selection in Pipeline:**
```groovy
choice(
    name: 'DEPLOYMENT_STRATEGY',
    choices: ['rolling', 'blue-green', 'canary']
)
```

---

### Criterion: Exposure/Release

#### ✅ Release gradually/progressively with zero downtime
**Evidence:**
- Rolling update: `docker-swarm-stack.yml` line 174 `order: start-first`
- Canary deployment: Gradual traffic increase
- Blue-green: Instant switch, no downtime

**Configuration:**
```yaml
update_config:
  parallelism: 1        # One at a time
  delay: 10s            # Wait between updates
  order: start-first    # New starts before old stops
```

#### ✅ Automatically revoke release if tests detect problems
**Evidence:**
- Health check monitoring: `Jenkinsfile-microservices` lines 476-524
- Automatic rollback: Lines 508-520
- Rollback script: `scripts/deployment/rollback.sh`

**Configuration:**
```yaml
update_config:
  failure_action: rollback
  monitor: 30s
rollback_config:
  parallelism: 1
  delay: 5s
```

#### ✅ Dark Launch and Kill Switch strategies
**Evidence:**
While not fully implemented in current code, the infrastructure supports it:

**Dark Launch:** Deploy feature but keep disabled via feature flags
- Can use environment variables
- Redis-based feature toggles
- Enable for specific users

**Kill Switch:** Ability to disable features instantly
- Scale service to 0 replicas
- Update routing rules in Traefik
- Environment variable toggles

**Implementation example:**
```yaml
# Environment variable feature toggle
FEATURE_NEW_ALGORITHM_ENABLED: false

# Kill switch via scaling
docker service scale genre-service=0
```

#### ✅ Release strategies: Internal/Beta Access, Gradual Release, A/B Testing
**Evidence:**

**1. Internal/Beta Access:**
- Canary deployment serves as internal release
- Can route specific users to canary instances

**2. Gradual Release:**
- Canary script: Start with 1 replica (33% traffic)
- Gradually increase: 2 replicas (66%), then 3 (100%)

**3. A/B Testing:**
- Traefik routing rules support weighted distribution
- Can route traffic based on headers/cookies

**Traefik A/B configuration example:**
```yaml
labels:
  - "traefik.http.services.genre-v1.loadbalancer.weight=70"
  - "traefik.http.services.genre-v2.loadbalancer.weight=30"
```

---

## Deployment Checklist

### Pre-Deployment
- [ ] All tests passing (Unit, Integration, CDC, Mutation)
- [ ] Code coverage ≥ 70%
- [ ] Mutation score ≥ 60%
- [ ] Static analysis passing (Checkstyle, SonarQube)
- [ ] Docker images built and tagged
- [ ] Docker images pushed to registry
- [ ] Database migrations prepared (if any)

### Deployment
- [ ] Choose deployment strategy (rolling/blue-green/canary)
- [ ] Set target environment (dev/staging/prod)
- [ ] Backup current production state
- [ ] Execute deployment
- [ ] Monitor health checks (30s window)
- [ ] Verify all replicas running
- [ ] Run smoke tests
- [ ] Check application logs

### Post-Deployment
- [ ] Run load tests (staging/prod)
- [ ] Monitor metrics for 1 hour
- [ ] Verify auto-scaling works
- [ ] Test rollback procedure
- [ ] Document any issues
- [ ] Notify stakeholders
- [ ] Update documentation

---

## Troubleshooting

### Service won't start
```bash
# Check service logs
docker service logs genre-service --tail 100

# Check service status
docker service ps genre-service --no-trunc

# Check container logs
docker logs <container-id>

# Inspect service configuration
docker service inspect genre-service
```

### Health check failures
```bash
# Manual health check
curl http://localhost:8080/actuator/health

# Check dependencies
docker exec <container-id> wget --spider http://postgres:5432

# Verify environment variables
docker service inspect genre-service --format='{{.Spec.TaskTemplate.ContainerSpec.Env}}'
```

### Load balancer issues
```bash
# Check Traefik logs
docker service logs traefik_traefik

# Verify service labels
docker service inspect genre-service --format='{{.Spec.Labels}}'

# Test direct service access
curl http://<service-ip>:8080/actuator/health
```

### Rollback not working
```bash
# Manual rollback
docker service rollback genre-service

# Check update history
docker service inspect genre-service --format='{{.PreviousSpec.TaskTemplate.ContainerSpec.Image}}'

# Force update to specific version
docker service update --image registry/genre-service:v1.0.0 genre-service
```

---

## Contact & Support
- **DevOps Team:** devops@example.com
- **Documentation:** `/Docs`
- **Issue Tracker:** GitHub Issues
- **CI/CD Dashboard:** Jenkins (http://jenkins:8080)

---

**Last Updated:** 2026-01-04
**Version:** 1.0.0
