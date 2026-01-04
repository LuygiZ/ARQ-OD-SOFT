# ODSOFT - Plano de Implementação CI/CD

## Student C - Nuno Oliveira

**Projeto:** LMS Microservices - CI/CD Pipeline
**Deadline:** 2026-01-04

---

# ÍNDICE

1. [Visão Geral](#1-visão-geral)
2. [Continuous Integration (CI)](#2-continuous-integration-ci)
3. [Provision and Hosting](#3-provision-and-hosting)
4. [Rollout/Deployment](#4-rolloutdeployment)
5. [Exposure/Release](#5-exposurerelease)
6. [Checklist de Implementação](#6-checklist-de-implementação)

---

# 1. VISÃO GERAL

## 1.1 Arquitetura Atual do Projeto

O projeto LMS já tem a seguinte estrutura de microserviços:

| Serviço | Porta | Descrição |
|---------|-------|-----------|
| genre-service | 8080 | Gestão de géneros |
| author-service | 8082 | Gestão de autores (CQRS) |
| book-command-service | 8083 | CQRS Write - Livros |
| book-query-service | 8085 | CQRS Read - Livros/Reviews |
| lending-service | 8086 | Empréstimos |
| reader-service | 8087 | Autenticação/Leitores |
| saga-orchestrator | 8084 | Transações distribuídas |

## 1.2 Requisitos Não-Funcionais

| Requisito | Descrição | Como Implementar |
|-----------|-----------|------------------|
| Performance +25% | Aumentar performance sob carga | Load tests + auto-scaling |
| Hardware parsimonioso | Usar recursos eficientemente | Auto-scaling baseado em carga |
| Releasability | Cada serviço deployável independentemente | Pipelines separadas por serviço |
| Independent deployment | Deploy de um serviço não afeta outros | Docker Swarm/Kubernetes |
| 3 Environments | Dev, Staging, Production | Docker Compose por ambiente |
| Auto-rollback | Rollback automático em falha de health check | Swarm health checks + rollback |
| Zero downtime | Updates sem interrupção | Rolling updates / Blue-Green |

## 1.3 Ambientes a Configurar

```
┌─────────────────────────────────────────────────────────────────┐
│                        ENVIRONMENTS                              │
├───────────────────┬───────────────────┬─────────────────────────┤
│       DEV         │      STAGING      │      PRODUCTION         │
├───────────────────┼───────────────────┼─────────────────────────┤
│ - Testes unitários│ - Load tests      │ - Health checks         │
│ - Smoke tests     │ - Smoke tests     │ - Auto-rollback         │
│ - Build images    │ - Integration     │ - Zero downtime         │
│ - Static analysis │ - CDC tests       │ - Gradual release       │
│ - Mutation tests  │ - Performance     │ - Kill switch           │
└───────────────────┴───────────────────┴─────────────────────────┘
```

---

# 2. CONTINUOUS INTEGRATION (CI)

## 2.1 Static Tests (Peso: 5%)

### O que fazer:
Configurar análise estática de código no pipeline.

### Ferramentas a usar:
- **Checkstyle** - Verificação de estilo Java
- **SpotBugs** - Deteção de bugs
- **SonarQube** (opcional) - Análise completa

### Ficheiros a criar/modificar:

**1. `checkstyle.xml` (raiz do projeto)**
```xml
<?xml version="1.0"?>
<!DOCTYPE module PUBLIC
    "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
    "https://checkstyle.org/dtds/configuration_1_3.dtd">
<module name="Checker">
    <module name="TreeWalker">
        <module name="JavadocMethod"/>
        <module name="JavadocType"/>
        <module name="ConstantName"/>
        <module name="LocalFinalVariableName"/>
        <module name="LocalVariableName"/>
        <module name="MemberName"/>
        <module name="MethodName"/>
        <module name="PackageName"/>
        <module name="ParameterName"/>
        <module name="TypeName"/>
    </module>
</module>
```

**2. Adicionar ao `pom.xml` de cada serviço:**
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-checkstyle-plugin</artifactId>
    <version>3.3.1</version>
    <configuration>
        <configLocation>checkstyle.xml</configLocation>
        <failOnViolation>true</failOnViolation>
    </configuration>
</plugin>
```

**3. Stage no Jenkinsfile:**
```groovy
stage('Static Analysis') {
    steps {
        sh 'mvn checkstyle:check'
        sh 'mvn spotbugs:check'
    }
    post {
        always {
            recordIssues tools: [checkStyle(), spotBugs()]
        }
    }
}
```

---

## 2.2 Unit Tests (SUT = classes, 2+ classes)

### O que fazer:
Garantir que os testes unitários existentes correm na pipeline e cobrem múltiplas classes.

### Estrutura de testes por serviço:

```
lending-service/
└── src/test/java/pt/psoft/lending/
    ├── model/
    │   ├── LendingEntityTest.java      ← Testa LendingEntity
    │   └── LendingNumberTest.java      ← Testa LendingNumber
    ├── services/
    │   └── LendingCommandServiceTest.java
    └── api/
        └── LendingCommandControllerTest.java
```

### Configuração Maven:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.2.5</version>
    <configuration>
        <includes>
            <include>**/*Test.java</include>
        </includes>
    </configuration>
</plugin>
```

### Stage no Jenkinsfile:
```groovy
stage('Unit Tests') {
    steps {
        sh 'mvn test'
    }
    post {
        always {
            junit '**/target/surefire-reports/*.xml'
        }
    }
}
```

---

## 2.3 Mutation Tests (Peso: parte do CI)

### O que fazer:
Configurar PIT (Pitest) para mutation testing nas classes de domínio.

### Adicionar ao `pom.xml`:
```xml
<plugin>
    <groupId>org.pitest</groupId>
    <artifactId>pitest-maven</artifactId>
    <version>1.15.3</version>
    <dependencies>
        <dependency>
            <groupId>org.pitest</groupId>
            <artifactId>pitest-junit5-plugin</artifactId>
            <version>1.2.1</version>
        </dependency>
    </dependencies>
    <configuration>
        <targetClasses>
            <param>pt.psoft.lending.model.*</param>
            <param>pt.psoft.bookcommand.model.*</param>
        </targetClasses>
        <targetTests>
            <param>pt.psoft.lending.model.*Test</param>
        </targetTests>
        <mutationThreshold>70</mutationThreshold>
        <outputFormats>
            <outputFormat>HTML</outputFormat>
            <outputFormat>XML</outputFormat>
        </outputFormats>
    </configuration>
</plugin>
```

### Stage no Jenkinsfile:
```groovy
stage('Mutation Tests') {
    steps {
        sh 'mvn org.pitest:pitest-maven:mutationCoverage'
    }
    post {
        always {
            publishHTML([
                reportDir: 'target/pit-reports',
                reportFiles: 'index.html',
                reportName: 'PIT Mutation Report'
            ])
        }
    }
}
```

---

## 2.4 Consumer-Driven Contract Tests (Peso: 60%)

### O que fazer:
Usar Pact para validar contratos entre serviços (especialmente eventos RabbitMQ).

### Contratos a testar:

| Provider | Consumer | Contrato |
|----------|----------|----------|
| lending-service | book-query-service | LendingReturnedEvent |
| book-command-service | book-query-service | BookCreatedEvent |
| book-command-service | book-query-service | BookUpdatedEvent |
| saga-orchestrator | book-command-service | CreateBookRequest |

### Dependências Pact no `pom.xml`:
```xml
<dependency>
    <groupId>au.com.dius.pact.provider</groupId>
    <artifactId>junit5</artifactId>
    <version>4.6.5</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>au.com.dius.pact.consumer</groupId>
    <artifactId>junit5</artifactId>
    <version>4.6.5</version>
    <scope>test</scope>
</dependency>
```

### Exemplo de teste Provider (lending-service):

**`LendingReturnedEventProviderPactTest.java`**
```java
@Provider("lending-service")
@Consumer("book-query-service")
@PactFolder("pacts")
class LendingReturnedEventProviderPactTest {

    @PactVerifyProvider("a lending returned event with review")
    MessageAndMetadata lendingReturnedWithReview() {
        LendingReturnedEvent event = new LendingReturnedEvent();
        event.setLendingNumber("2025/1");
        event.setBookId("978-0-13-468599-1");
        event.setComment("Great book!");
        event.setRating(8);
        event.setReturnDate(LocalDate.now());

        String json = JsonUtils.toJson(event);
        Map<String, Object> metadata = Map.of(
            "contentType", "application/json",
            "routing-key", "lending.lending.returned"
        );
        return new MessageAndMetadata(json.getBytes(), metadata);
    }

    @PactVerifyProvider("a lending returned event without review")
    MessageAndMetadata lendingReturnedWithoutReview() {
        LendingReturnedEvent event = new LendingReturnedEvent();
        event.setLendingNumber("2025/2");
        event.setBookId("978-0-13-468599-1");
        event.setComment(null);
        event.setRating(null);
        event.setReturnDate(LocalDate.now());

        String json = JsonUtils.toJson(event);
        return new MessageAndMetadata(json.getBytes(), Map.of());
    }
}
```

### Exemplo de teste Consumer (book-query-service):

**`LendingReturnedEventConsumerPactTest.java`**
```java
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "lending-service")
class LendingReturnedEventConsumerPactTest {

    @Pact(consumer = "book-query-service")
    MessagePact lendingReturnedWithReview(MessagePactBuilder builder) {
        return builder
            .expectsToReceive("a lending returned event with review")
            .withContent(new PactDslJsonBody()
                .stringType("lendingNumber", "2025/1")
                .stringType("bookId", "978-0-13-468599-1")
                .stringType("comment", "Great book!")
                .integerType("rating", 8)
                .date("returnDate", "yyyy-MM-dd"))
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "lendingReturnedWithReview")
    void testLendingReturnedWithReview(List<Message> messages) {
        assertThat(messages).hasSize(1);
        String json = new String(messages.get(0).contentsAsBytes());
        LendingReturnedEvent event = JsonUtils.fromJson(json, LendingReturnedEvent.class);

        assertThat(event.getLendingNumber()).isEqualTo("2025/1");
        assertThat(event.getRating()).isEqualTo(8);
    }
}
```

### Stage no Jenkinsfile:
```groovy
stage('CDC Tests - Consumer') {
    steps {
        dir('book-query-service') {
            sh 'mvn test -Dtest=*ConsumerPactTest'
        }
    }
    post {
        always {
            // Publish pact files to broker or archive
            archiveArtifacts artifacts: '**/target/pacts/*.json'
        }
    }
}

stage('CDC Tests - Provider') {
    steps {
        dir('lending-service') {
            sh 'mvn test -Dtest=*ProviderPactTest'
        }
    }
}
```

---

## 2.5 Container Image Build (Peso: 35%)

### O que fazer:
Construir imagens Docker para cada serviço com tags apropriadas.

### Estratégia de tagging:
```
<service-name>:<version>
<service-name>:<git-commit-sha>
<service-name>:latest
<service-name>:staging
<service-name>:production
```

### Dockerfile padrão (já existe, verificar):
```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8083
HEALTHCHECK --interval=30s --timeout=3s \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8083/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Stage no Jenkinsfile:
```groovy
stage('Build Docker Images') {
    steps {
        script {
            def services = ['genre-service', 'author-service', 'book-command-service',
                           'book-query-service', 'lending-service', 'saga-orchestrator']

            services.each { service ->
                dir(service) {
                    sh "docker build -t ${service}:${env.BUILD_NUMBER} ."
                    sh "docker tag ${service}:${env.BUILD_NUMBER} ${service}:latest"
                    sh "docker tag ${service}:${env.BUILD_NUMBER} ${service}:${env.GIT_COMMIT}"
                }
            }
        }
    }
}
```

---

# 3. PROVISION AND HOSTING

## 3.1 Infrastructure as Code (Peso: 70%)

### O que fazer:
Criar ficheiros de configuração para cada ambiente.

### Estrutura de ficheiros a criar:

```
infrastructure/
├── docker-compose.yml          # Ambiente DEV (já existe)
├── docker-compose.staging.yml  # Ambiente STAGING
├── docker-compose.prod.yml     # Ambiente PRODUCTION
├── docker-stack.yml            # Docker Swarm para PROD
├── init-databases.sql          # Inicialização BD (já existe)
└── scripts/
    ├── deploy-dev.sh
    ├── deploy-staging.sh
    ├── deploy-prod.sh
    ├── scale-services.sh
    └── rollback.sh
```

### `docker-compose.staging.yml`:
```yaml
version: '3.8'

networks:
  lms_staging:
    driver: bridge

services:
  traefik:
    image: traefik:v3.1
    command:
      - "--api.dashboard=true"
      - "--providers.docker=true"
      - "--entrypoints.web.address=:80"
    ports:
      - "8080:80"
      - "8091:8080"
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock:ro
    networks:
      - lms_staging

  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: ${DB_PASSWORD:-staging_password}
    volumes:
      - ./init-databases.sql:/docker-entrypoint-initdb.d/init.sql
    networks:
      - lms_staging
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      retries: 5

  # Services with staging tag
  book-command-service:
    image: ${REGISTRY}/book-command-service:staging
    deploy:
      replicas: 2
    environment:
      SPRING_PROFILES_ACTIVE: staging
      DB_HOST: postgres
    networks:
      - lms_staging
    labels:
      - "traefik.enable=true"
      - "traefik.http.routers.book-command-staging.rule=PathPrefix(`/api/books`)"

  # ... outros serviços
```

### `docker-stack.yml` (Docker Swarm para Produção):
```yaml
version: '3.8'

services:
  book-command-service:
    image: ${REGISTRY}/book-command-service:${VERSION:-latest}
    deploy:
      replicas: 2
      update_config:
        parallelism: 1
        delay: 10s
        failure_action: rollback
        order: start-first
      rollback_config:
        parallelism: 1
        delay: 10s
      restart_policy:
        condition: on-failure
        delay: 5s
        max_attempts: 3
    healthcheck:
      test: ["CMD", "wget", "--spider", "-q", "http://localhost:8083/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
    networks:
      - lms_prod

  book-query-service:
    image: ${REGISTRY}/book-query-service:${VERSION:-latest}
    deploy:
      replicas: 3  # Mais replicas para leitura
      update_config:
        parallelism: 1
        delay: 10s
        failure_action: rollback
      rollback_config:
        parallelism: 1
    healthcheck:
      test: ["CMD", "wget", "--spider", "-q", "http://localhost:8085/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
    networks:
      - lms_prod

networks:
  lms_prod:
    driver: overlay
```

---

## 3.2 Container Image Push (Peso: 30%)

### O que fazer:
Configurar push de imagens para um registry Docker.

### Opções de Registry:
1. **Docker Hub** (público/privado)
2. **GitHub Container Registry (ghcr.io)**
3. **DEI Registry** (se disponível)
4. **Harbor** (self-hosted)

### Stage no Jenkinsfile:
```groovy
stage('Push to Registry') {
    environment {
        REGISTRY = 'docker.io/your-username'  // ou ghcr.io/your-repo
        DOCKER_CREDENTIALS = credentials('docker-hub-credentials')
    }
    steps {
        sh 'echo $DOCKER_CREDENTIALS_PSW | docker login -u $DOCKER_CREDENTIALS_USR --password-stdin'

        script {
            def services = ['book-command-service', 'book-query-service', 'lending-service']
            services.each { service ->
                sh "docker tag ${service}:${env.BUILD_NUMBER} ${REGISTRY}/${service}:${env.BUILD_NUMBER}"
                sh "docker tag ${service}:${env.BUILD_NUMBER} ${REGISTRY}/${service}:latest"
                sh "docker push ${REGISTRY}/${service}:${env.BUILD_NUMBER}"
                sh "docker push ${REGISTRY}/${service}:latest"
            }
        }
    }
}
```

---

# 4. ROLLOUT/DEPLOYMENT

## 4.1 Deployment Automático de Service A (Peso: 10%)

### O que fazer:
Configurar deploy automático do serviço A (e.g., `book-query-service`) sem intervenção manual.

### Stage no Jenkinsfile:
```groovy
stage('Deploy Service A to Production') {
    when {
        branch 'main'
    }
    steps {
        script {
            // Deploy automático sem aprovação
            sshagent(['remote-server-credentials']) {
                sh '''
                    ssh user@remote-server "
                        cd /opt/lms &&
                        docker-compose -f docker-compose.prod.yml pull book-query-service &&
                        docker-compose -f docker-compose.prod.yml up -d book-query-service
                    "
                '''
            }
        }
    }
}
```

---

## 4.2 Deployment Manual de Service B com Notificação (Peso: 25%)

### O que fazer:
Configurar deploy do serviço B (e.g., `book-command-service`) com aprovação manual após notificação por email.

### Stage no Jenkinsfile:
```groovy
stage('Notify for Approval - Service B') {
    when {
        branch 'main'
    }
    steps {
        script {
            def stagingUrl = "http://staging.lms.dei.isep.ipp.pt/api/books"

            emailext(
                subject: "LMS Deploy Approval Required - book-command-service",
                body: """
                    <h2>Deploy Approval Required</h2>
                    <p>A new version of book-command-service is ready for production.</p>
                    <p><strong>Build:</strong> ${env.BUILD_NUMBER}</p>
                    <p><strong>Commit:</strong> ${env.GIT_COMMIT}</p>
                    <p><strong>Staging URL:</strong> <a href="${stagingUrl}">${stagingUrl}</a></p>
                    <p>Please test the staging environment and approve/reject the deployment:</p>
                    <p><a href="${env.BUILD_URL}input">Click here to Approve or Reject</a></p>
                """,
                to: 'team@example.com',
                mimeType: 'text/html'
            )
        }
    }
}

stage('Manual Approval - Service B') {
    when {
        branch 'main'
    }
    steps {
        timeout(time: 24, unit: 'HOURS') {
            input message: 'Deploy book-command-service to production?',
                  ok: 'Deploy',
                  submitter: 'admin,devops'
        }
    }
}

stage('Deploy Service B to Production') {
    when {
        branch 'main'
    }
    steps {
        sshagent(['remote-server-credentials']) {
            sh '''
                ssh user@remote-server "
                    cd /opt/lms &&
                    docker-compose -f docker-compose.prod.yml pull book-command-service &&
                    docker-compose -f docker-compose.prod.yml up -d book-command-service
                "
            '''
        }
    }
}
```

---

## 4.3 Deploy em Servidor Remoto (Peso: 25%)

### O que fazer:
Configurar deploy em servidor DEI ou cloud (AWS/Azure/GCP).

### Opções:
1. **Servidor DEI** - VM com Docker instalado
2. **AWS EC2** - Instância com Docker
3. **DigitalOcean** - Droplet
4. **Azure Container Instances**

### Script de deploy remoto (`scripts/deploy-prod.sh`):
```bash
#!/bin/bash

REMOTE_HOST="user@dei-server.isep.ipp.pt"
DEPLOY_DIR="/opt/lms"
REGISTRY="docker.io/your-username"
VERSION=${1:-latest}

echo "Deploying version: $VERSION"

ssh $REMOTE_HOST << EOF
    cd $DEPLOY_DIR

    # Pull new images
    export VERSION=$VERSION
    export REGISTRY=$REGISTRY
    docker-compose -f docker-compose.prod.yml pull

    # Deploy with zero downtime
    docker-compose -f docker-compose.prod.yml up -d --no-deps --scale book-command-service=2

    # Wait for health checks
    sleep 30

    # Verify deployment
    docker-compose -f docker-compose.prod.yml ps
EOF
```

---

## 4.4 Load Tests em Staging (Peso: 30%)

### O que fazer:
Configurar testes de carga com K6 ou JMeter no ambiente de staging.

### Criar ficheiro K6 (`load-tests/load-test.js`):
```javascript
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '1m', target: 50 },   // Ramp up to 50 users
        { duration: '3m', target: 50 },   // Stay at 50 users
        { duration: '1m', target: 100 },  // Ramp up to 100 users
        { duration: '3m', target: 100 },  // Stay at 100 users
        { duration: '1m', target: 0 },    // Ramp down
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'],  // 95% requests under 500ms
        http_req_failed: ['rate<0.01'],    // Less than 1% failure rate
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://staging.lms.local';

export default function () {
    // Test GET /api/books
    let res = http.get(`${BASE_URL}/api/books`);
    check(res, {
        'status is 200': (r) => r.status === 200,
        'response time < 500ms': (r) => r.timings.duration < 500,
    });

    sleep(1);

    // Test GET /api/books/{isbn}
    res = http.get(`${BASE_URL}/api/books/978-0-13-468599-1`);
    check(res, {
        'status is 200 or 404': (r) => r.status === 200 || r.status === 404,
    });

    sleep(1);
}

export function handleSummary(data) {
    return {
        'load-test-results.json': JSON.stringify(data),
    };
}
```

### Stage no Jenkinsfile:
```groovy
stage('Load Tests - Staging') {
    when {
        branch 'main'
    }
    steps {
        sh '''
            docker run --rm \
                -v ${WORKSPACE}/load-tests:/scripts \
                grafana/k6 run \
                -e BASE_URL=http://staging.lms.dei.isep.ipp.pt \
                --out json=/scripts/results.json \
                /scripts/load-test.js
        '''
    }
    post {
        always {
            archiveArtifacts artifacts: 'load-tests/results.json'
            // Parse results and decide on scaling
        }
    }
}
```

---

## 4.5 Scale Services via Scripts (Peso: 20%)

### O que fazer:
Criar scripts que escalam serviços baseado nos resultados dos load tests.

### Script (`scripts/scale-services.sh`):
```bash
#!/bin/bash

# Parse load test results
RESULTS_FILE=$1
P95_LATENCY=$(jq '.metrics.http_req_duration.values["p(95)"]' $RESULTS_FILE)
ERROR_RATE=$(jq '.metrics.http_req_failed.values.rate' $RESULTS_FILE)

echo "P95 Latency: $P95_LATENCY ms"
echo "Error Rate: $ERROR_RATE"

# Scaling thresholds
LATENCY_THRESHOLD=400
ERROR_THRESHOLD=0.005

# Scale up if needed
if (( $(echo "$P95_LATENCY > $LATENCY_THRESHOLD" | bc -l) )); then
    echo "High latency detected, scaling up book-query-service..."
    docker service scale lms_book-query-service=4
fi

if (( $(echo "$ERROR_RATE > $ERROR_THRESHOLD" | bc -l) )); then
    echo "High error rate detected, scaling up services..."
    docker service scale lms_book-command-service=3
    docker service scale lms_book-query-service=5
fi

# Report current scale
docker service ls
```

### Stage no Jenkinsfile:
```groovy
stage('Auto-Scale Based on Load Tests') {
    steps {
        sh 'chmod +x scripts/scale-services.sh'
        sh './scripts/scale-services.sh load-tests/results.json'
    }
}
```

---

## 4.6 Smoke Tests (Peso: 5%)

### O que fazer:
Criar smoke tests que verificam se os serviços estão a responder após deploy.

### Script (`scripts/smoke-tests.sh`):
```bash
#!/bin/bash

BASE_URL=${1:-http://localhost}
TIMEOUT=5

echo "Running smoke tests against $BASE_URL"

# Test genre-service
echo -n "Testing genre-service... "
if curl -s --max-time $TIMEOUT "$BASE_URL/api/genres" > /dev/null; then
    echo "OK"
else
    echo "FAILED"
    exit 1
fi

# Test book-query-service
echo -n "Testing book-query-service... "
if curl -s --max-time $TIMEOUT "$BASE_URL/api/books" > /dev/null; then
    echo "OK"
else
    echo "FAILED"
    exit 1
fi

# Test lending-service health
echo -n "Testing lending-service health... "
HEALTH=$(curl -s --max-time $TIMEOUT "$BASE_URL/api/lendings/actuator/health" | jq -r '.status')
if [ "$HEALTH" == "UP" ]; then
    echo "OK"
else
    echo "FAILED (status: $HEALTH)"
    exit 1
fi

echo "All smoke tests passed!"
```

### Stages no Jenkinsfile:
```groovy
stage('Smoke Tests - Dev') {
    steps {
        sh './scripts/smoke-tests.sh http://localhost'
    }
}

stage('Smoke Tests - Staging') {
    steps {
        sh './scripts/smoke-tests.sh http://staging.lms.dei.isep.ipp.pt'
    }
}
```

---

## 4.7 Health Checks em Produção (Peso: 5%)

### O que fazer:
Configurar health checks no Docker Swarm/Compose que triggam rollback automático.

### No `docker-stack.yml` (já parcialmente configurado):
```yaml
services:
  book-command-service:
    healthcheck:
      test: ["CMD", "wget", "--spider", "-q", "http://localhost:8083/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    deploy:
      update_config:
        failure_action: rollback  # Rollback automático em falha
```

### Verificar health no `application.yml`:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
      probes:
        enabled: true
  health:
    livenessState:
      enabled: true
    readinessState:
      enabled: true
```

---

## 4.8 Rollout Strategies (Peso: 30% - 1 por estudante)

### Estratégias disponíveis:

| Estratégia | Descrição | Quando Usar |
|------------|-----------|-------------|
| **Rolling Update** | Atualiza instâncias uma a uma | Default, baixo risco |
| **Blue-Green** | 2 ambientes, switch instantâneo | Zero downtime garantido |
| **Canary** | % pequena de tráfego para nova versão | Testar em produção |

### Implementação Rolling Update (Docker Swarm):
```yaml
deploy:
  update_config:
    parallelism: 1        # 1 container de cada vez
    delay: 10s            # 10s entre updates
    failure_action: rollback
    order: start-first    # Inicia novo antes de parar antigo
```

### Implementação Blue-Green (com Traefik):
```yaml
# docker-compose.blue-green.yml
services:
  book-query-blue:
    image: book-query-service:v1
    labels:
      - "traefik.http.routers.book-query.rule=PathPrefix(`/api/books`)"
      - "traefik.http.services.book-query-blue.loadbalancer.weight=100"

  book-query-green:
    image: book-query-service:v2
    labels:
      - "traefik.http.routers.book-query.rule=PathPrefix(`/api/books`)"
      - "traefik.http.services.book-query-green.loadbalancer.weight=0"
```

**Script para switch:**
```bash
#!/bin/bash
# Switch from blue to green
docker service update --label-add traefik.http.services.book-query-blue.loadbalancer.weight=0 book-query-blue
docker service update --label-add traefik.http.services.book-query-green.loadbalancer.weight=100 book-query-green
```

### Implementação Canary (com Traefik weights):
```yaml
services:
  book-query-stable:
    image: book-query-service:v1
    labels:
      - "traefik.http.services.book-query.loadbalancer.weight=90"  # 90% tráfego

  book-query-canary:
    image: book-query-service:v2
    labels:
      - "traefik.http.services.book-query.loadbalancer.weight=10"  # 10% tráfego
```

---

# 5. EXPOSURE/RELEASE

## 5.1 Release Gradual com Zero Downtime (Peso: 20%)

### O que fazer:
Configurar releases graduais que não interrompem o serviço.

### Técnicas:
1. **Rolling updates** - já configurado
2. **Connection draining** - esperar requests terminarem
3. **Health checks** - só expor quando saudável

### Configuração Traefik para zero downtime:
```yaml
# traefik.yml
entryPoints:
  web:
    address: ":80"
    transport:
      lifeCycle:
        graceTimeOut: 30s  # Espera 30s para terminar requests
```

---

## 5.2 Auto-Rollback em Falha de Testes (Peso: 30%)

### O que fazer:
Configurar rollback automático quando testes de release falham.

### Stage no Jenkinsfile:
```groovy
stage('Release Validation') {
    steps {
        script {
            try {
                // Run post-deployment tests
                sh './scripts/release-tests.sh'
            } catch (Exception e) {
                echo "Release tests failed, initiating rollback..."

                // Rollback
                sh '''
                    docker service update --rollback book-command-service
                    docker service update --rollback book-query-service
                '''

                // Notify team
                emailext(
                    subject: "ROLLBACK: LMS Deployment Failed",
                    body: "Release tests failed. Services have been rolled back.",
                    to: 'team@example.com'
                )

                error("Deployment rolled back due to failed release tests")
            }
        }
    }
}
```

### Script de testes de release (`scripts/release-tests.sh`):
```bash
#!/bin/bash

BASE_URL=${1:-http://production.lms.local}

# Test critical endpoints
echo "Testing critical endpoints..."

# Create a test book
RESPONSE=$(curl -s -X POST "$BASE_URL/api/books" \
    -H "Content-Type: application/json" \
    -d '{"title":"Test Book","description":"Test","genre":"Test","authorIds":[1]}')

if echo $RESPONSE | grep -q "isbn"; then
    echo "Book creation: OK"
else
    echo "Book creation: FAILED"
    exit 1
fi

# Query the book
ISBN=$(echo $RESPONSE | jq -r '.isbn')
QUERY_RESPONSE=$(curl -s "$BASE_URL/api/books/$ISBN")

if echo $QUERY_RESPONSE | grep -q "Test Book"; then
    echo "Book query: OK"
else
    echo "Book query: FAILED"
    exit 1
fi

# Cleanup
curl -s -X DELETE "$BASE_URL/api/books/$ISBN"

echo "All release tests passed!"
```

---

## 5.3 Dark Launch e Kill Switch (Peso: 20%)

### O que fazer:
Implementar feature flags para dark launch e kill switch.

### Opção 1: Usar Spring Cloud Config + Properties
```yaml
# application.yml
features:
  new-rating-algorithm: ${FEATURE_NEW_RATING:false}
  reviews-enabled: ${FEATURE_REVIEWS:true}
```

### Opção 2: Usar biblioteca de Feature Flags (FF4J ou Unleash)

**Dependência:**
```xml
<dependency>
    <groupId>org.ff4j</groupId>
    <artifactId>ff4j-spring-boot-starter</artifactId>
    <version>2.1</version>
</dependency>
```

**Uso no código:**
```java
@RestController
public class BookQueryController {

    @Autowired
    private FF4j ff4j;

    @GetMapping("/{isbn}/reviews")
    public ResponseEntity<?> getReviews(@PathVariable String isbn) {
        // Kill Switch - desliga funcionalidade rapidamente
        if (!ff4j.check("reviews-enabled")) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Reviews temporarily unavailable");
        }

        // Dark Launch - nova funcionalidade só para alguns users
        if (ff4j.check("new-review-format")) {
            return getNewFormatReviews(isbn);
        }

        return getStandardReviews(isbn);
    }
}
```

### Script Kill Switch (`scripts/kill-switch.sh`):
```bash
#!/bin/bash

FEATURE=$1
ACTION=$2  # enable or disable

case $ACTION in
    "disable")
        echo "Disabling feature: $FEATURE"
        curl -X POST "http://ff4j-server/api/ff4j/store/features/$FEATURE/disable"
        ;;
    "enable")
        echo "Enabling feature: $FEATURE"
        curl -X POST "http://ff4j-server/api/ff4j/store/features/$FEATURE/enable"
        ;;
esac
```

---

## 5.4 Release Strategies (Peso: 30% - 1 por estudante)

### Estratégias disponíveis:

| Estratégia | Descrição | Implementação |
|------------|-----------|---------------|
| **Internal/Beta Access** | Apenas users internos veem nova versão | Header routing |
| **Gradual Release** | % de tráfego aumenta progressivamente | Traefik weights |
| **A/B Testing** | Diferentes versões para diferentes users | Cookie/Header routing |

### Implementação Internal/Beta Access (Traefik):
```yaml
# Rota para beta testers (header X-Beta: true)
labels:
  - "traefik.http.routers.book-query-beta.rule=PathPrefix(`/api/books`) && Headers(`X-Beta`, `true`)"
  - "traefik.http.routers.book-query-beta.service=book-query-beta"
  - "traefik.http.routers.book-query-beta.priority=100"

# Rota normal
labels:
  - "traefik.http.routers.book-query.rule=PathPrefix(`/api/books`)"
  - "traefik.http.routers.book-query.service=book-query-stable"
  - "traefik.http.routers.book-query.priority=50"
```

### Implementação Gradual Release (Script):
```bash
#!/bin/bash
# gradual-release.sh

NEW_VERSION=$1
INCREMENTS=(10 25 50 75 100)

for PERCENT in "${INCREMENTS[@]}"; do
    echo "Setting new version traffic to ${PERCENT}%..."

    OLD_PERCENT=$((100 - PERCENT))

    # Update weights
    docker service update \
        --label-add "traefik.http.services.book-query-stable.loadbalancer.weight=$OLD_PERCENT" \
        book-query-stable

    docker service update \
        --label-add "traefik.http.services.book-query-new.loadbalancer.weight=$PERCENT" \
        book-query-new

    # Wait and monitor
    echo "Waiting 5 minutes to monitor..."
    sleep 300

    # Check error rate
    ERROR_RATE=$(curl -s http://prometheus:9090/api/v1/query?query=rate(http_requests_total{status=~"5.."}[5m]) | jq '.data.result[0].value[1]')

    if (( $(echo "$ERROR_RATE > 0.01" | bc -l) )); then
        echo "Error rate too high, rolling back..."
        ./scripts/rollback.sh
        exit 1
    fi
done

echo "Gradual release complete!"
```

### Implementação A/B Testing (Traefik com cookies):
```yaml
# A/B Testing - 50% users veem versão A, 50% versão B
labels:
  # Version A (cookie ab=A)
  - "traefik.http.routers.book-query-a.rule=PathPrefix(`/api/books`) && HeadersRegexp(`Cookie`, `ab=A`)"
  - "traefik.http.routers.book-query-a.service=book-query-v1"

  # Version B (cookie ab=B ou sem cookie)
  - "traefik.http.routers.book-query-b.rule=PathPrefix(`/api/books`)"
  - "traefik.http.routers.book-query-b.service=book-query-v2"

  # Middleware para set cookie se não existir
  - "traefik.http.middlewares.ab-cookie.headers.customresponseheaders.Set-Cookie=ab=${random(A,B)}; Path=/"
```

---

# 6. CHECKLIST DE IMPLEMENTAÇÃO

## Fase 1: CI Pipeline (Semana 1)

- [ ] Configurar Jenkins/GitLab CI
- [ ] Adicionar Checkstyle ao pom.xml
- [ ] Verificar testes unitários existentes
- [ ] Configurar PIT mutation testing
- [ ] Criar testes Pact (CDC)
- [ ] Configurar build de imagens Docker

## Fase 2: Provision & Hosting (Semana 2)

- [ ] Criar docker-compose.staging.yml
- [ ] Criar docker-compose.prod.yml
- [ ] Criar docker-stack.yml (Swarm)
- [ ] Configurar Docker registry
- [ ] Configurar push de imagens

## Fase 3: Deployment (Semana 3)

- [ ] Configurar deploy automático Service A
- [ ] Configurar notificação email Service B
- [ ] Configurar approval gate Service B
- [ ] Setup servidor remoto DEI
- [ ] Criar load tests K6
- [ ] Criar scripts de scaling
- [ ] Criar smoke tests
- [ ] Configurar health checks

## Fase 4: Release (Semana 4)

- [ ] Implementar rolling updates
- [ ] Configurar auto-rollback
- [ ] Implementar feature flags (FF4J ou env vars)
- [ ] Configurar dark launch
- [ ] Implementar kill switch
- [ ] Escolher e implementar release strategy (Beta/Gradual/A/B)

## Fase 5: Documentação (Final)

- [ ] Documentar arquitetura CI/CD
- [ ] Criar diagramas de pipeline
- [ ] Documentar decisões e justificações
- [ ] Preparar demo

---

# RESUMO FINAL

## Critérios e Evidências Necessárias

| # | Critério | Peso | Evidência Necessária |
|---|----------|------|---------------------|
| 1.1 | Static Tests | 5% | Logs Checkstyle/SpotBugs |
| 1.2 | Unit Tests | - | Relatórios JUnit |
| 1.3 | Mutation Tests | - | Relatório PIT |
| 1.4 | CDC Tests | 60% | Ficheiros Pact + logs |
| 1.5 | Container Build | 35% | Logs docker build |
| 2.1 | IaC | 70% | Dockerfiles, Compose, Stack |
| 2.2 | Image Push | 30% | Logs docker push |
| 3.1 | Auto Deploy A | 10% | Logs pipeline |
| 3.2 | Manual Deploy B | 25% | Email + approval gate |
| 3.3 | Remote Deploy | 25% | Logs servidor remoto |
| 3.4 | Load Tests | 30% | Relatório K6/JMeter |
| 3.5 | Auto-Scale | 20% | Logs scaling scripts |
| 3.6 | Smoke Tests | 5% | Logs smoke tests |
| 3.7 | Health Checks | 5% | Config + logs |
| 3.8 | Rollout Strategy | 30% | Config + logs |
| 4.1 | Zero Downtime | 20% | Logs sem interrupção |
| 4.2 | Auto-Rollback | 30% | Logs rollback |
| 4.3 | Dark/Kill Switch | 20% | Config + demo |
| 4.4 | Release Strategy | 30% | Config + demo |

## Ferramentas Recomendadas

| Categoria | Ferramenta |
|-----------|------------|
| CI/CD | Jenkins / GitLab CI / GitHub Actions |
| Static Analysis | Checkstyle, SpotBugs, SonarQube |
| Mutation Testing | PIT |
| CDC Testing | Pact |
| Load Testing | K6, JMeter, Gatling |
| Container Orchestration | Docker Swarm / Kubernetes |
| Feature Flags | FF4J, Unleash, LaunchDarkly |
| Monitoring | Prometheus + Grafana |
| Registry | Docker Hub, GHCR, Harbor |
