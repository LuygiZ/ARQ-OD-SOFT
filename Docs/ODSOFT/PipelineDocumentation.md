# 📚 Pipeline Jenkins - Documentação Completa

## 📋 Índice

1. [Visão Geral](#visão-geral)
2. [Arquitetura da Pipeline](#arquitetura-da-pipeline)
3. [Configuração Inicial](#configuração-inicial)
4. [Stages Detalhadas](#stages-detalhadas)
5. [Parâmetros](#parâmetros)
6. [Variáveis de Ambiente](#variáveis-de-ambiente)
7. [Quality Gates](#quality-gates)
8. [Deployment](#deployment)
9. [Reports e Métricas](#reports-e-métricas)
10. [Troubleshooting](#troubleshooting)

---

## 🎯 Visão Geral

Esta pipeline Jenkins implementa um processo completo de CI/CD para o projeto **Library Management** conforme os requisitos do ODSOFT 2025-2026 Project 1.

### Características Principais

- ✅ **13 Stages** completos de CI/CD
- ✅ **Testes paralelos** (Unit + Integration)
- ✅ **Code Coverage** com JaCoCo (mínimo 80%)
- ✅ **Mutation Testing** com PIT
- ✅ **Static Analysis** com SonarQube
- ✅ **4 Quality Gates** progressivos
- ✅ **Multi-environment deployment** (DEV/STAGING/PROD)
- ✅ **Redis integration** para caching
- ✅ **Docker containerization**
- ✅ **Automatic rollback** em caso de falha

### Fluxo da Pipeline

```
Checkout → Build → Tests (Parallel) → Coverage → Mutation → 
SonarQube → QG1 → Package → Docker Build → Redis Check → 
Deploy → Health Check → QG Final
```

---

## 🏗️ Arquitetura da Pipeline

### Diagrama de Fluxo

```
┌─────────────────────────────────────────────────────────────┐
│ PHASE 1: BUILD & QUALITY CHECKS                              │
├─────────────────────────────────────────────────────────────┤
│ Stage 1:  Checkout                                           │
│ Stage 2:  Build & Compile                                    │
│ Stage 3:  Unit & Integration Tests (PARALLEL)                │
│ Stage 4:  Code Coverage (JaCoCo)                             │
│ Stage 5:  Mutation Testing (PIT)                             │
│ Stage 6:  SonarQube Analysis                                 │
│ Stage 7:  Quality Gate 1 ⚠️                                  │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ PHASE 2: PACKAGING & CONTAINERIZATION                        │
├─────────────────────────────────────────────────────────────┤
│ Stage 8:  Package (JAR)                                      │
│ Stage 9:  Build Docker Image                                 │
│ Stage 10: Verify Redis Connection                            │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ PHASE 3: DEPLOYMENT                                          │
├─────────────────────────────────────────────────────────────┤
│ Stage 11: Deploy to Environment                              │
│           - DEV (automático)                                 │
│           - STAGING (aprovação manual) 🤚                    │
│           - PRODUCTION (aprovação admin) 🤚                  │
│ Stage 12: Health Check                                       │
│ Stage 13: Quality Gate Final (apenas PROD) ⚠️               │
└─────────────────────────────────────────────────────────────┘
```

### Infraestrutura

```
┌─────────────────────────────────────────────────────────┐
│                    Docker Desktop                        │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  ┌──────────────┐    ┌──────────────┐                  │
│  │   Jenkins    │    │    Redis     │                  │
│  │ (container)  │◄──►│ fervent_benz │                  │
│  └──────────────┘    └──────────────┘                  │
│         │                    ▲                          │
│         │                    │                          │
│         ▼                    │                          │
│  ┌──────────────────────────┴─────────────────┐       │
│  │      Application Containers                 │       │
│  │  ┌──────┐  ┌──────┐  ┌──────┐             │       │
│  │  │ DEV  │  │STAG  │  │ PROD │             │       │
│  │  │:8080 │  │:8081 │  │:8082 │             │       │
│  │  └──────┘  └──────┘  └──────┘             │       │
│  └──────────────────────────────────────────┘       │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

---

## ⚙️ Configuração Inicial

### 1. Pré-requisitos

#### Jenkins

Versão mínima: Jenkins 2.400+

**Plugins necessários:**
```
✓ Pipeline
✓ Docker Pipeline
✓ JaCoCo Plugin
✓ HTML Publisher Plugin
✓ JUnit Plugin
✓ Git Plugin
✓ GitHub Plugin
✓ SonarQube Scanner (opcional)
```

**Instalação de plugins:**
1. Jenkins → Manage Jenkins → Manage Plugins
2. Aba "Available"
3. Procurar e instalar cada plugin
4. Reiniciar Jenkins

#### Tools

Configurar em: **Manage Jenkins → Global Tool Configuration**

**Maven:**
- Name: `Maven 3.9.11`
- Type: Maven
- Install automatically: ✓
- Version: 3.9.11

**JDK:**
- Name: `JDK-17`
- Type: JDK
- Install automatically: ✓
- Version: OpenJDK 17

#### Containers Docker

Containers necessários (já tens):
- ✅ Jenkins: `jenkins-container`
- ✅ Redis: `fervent_benz` (porta 6379)

### 2. Configuração do Projeto

#### Estrutura de Diretórios

```
library-management/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/library/
│   │   │       ├── config/
│   │   │       │   └── RedisConfig.java
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       │   └── BookService.java
│   │   │       ├── repository/
│   │   │       └── model/
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── application-staging.properties
│   │       └── application-prod.properties
│   └── test/
│       └── java/
│           └── com/library/
│               ├── unit/
│               ├── integration/
│               └── system/
├── Jenkinsfile
├── Dockerfile
├── pom.xml
└── README.md
```

#### pom.xml - Plugins Necessários

Adiciona estes plugins ao teu `pom.xml`:

```xml
<build>
    <plugins>
        <!-- Surefire para Unit Tests -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.2.2</version>
            <configuration>
                <includes>
                    <include>**/*Test.java</include>
                    <include>**/*Tests.java</include>
                </includes>
            </configuration>
        </plugin>

        <!-- Failsafe para Integration Tests -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-failsafe-plugin</artifactId>
            <version>3.2.2</version>
            <executions>
                <execution>
                    <goals>
                        <goal>integration-test</goal>
                        <goal>verify</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <includes>
                    <include>**/*IT.java</include>
                    <include>**/*IntegrationTest.java</include>
                </includes>
            </configuration>
        </plugin>

        <!-- JaCoCo para Code Coverage -->
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.11</version>
            <executions>
                <execution>
                    <goals>
                        <goal>prepare-agent</goal>
                    </goals>
                </execution>
                <execution>
                    <id>report</id>
                    <phase>test</phase>
                    <goals>
                        <goal>report</goal>
                    </goals>
                </execution>
                <execution>
                    <id>jacoco-check</id>
                    <goals>
                        <goal>check</goal>
                    </goals>
                    <configuration>
                        <rules>
                            <rule>
                                <element>BUNDLE</element>
                                <limits>
                                    <limit>
                                        <counter>LINE</counter>
                                        <value>COVEREDRATIO</value>
                                        <minimum>0.80</minimum>
                                    </limit>
                                </limits>
                            </rule>
                        </rules>
                    </configuration>
                </execution>
            </executions>
        </plugin>

        <!-- PIT para Mutation Testing -->
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
                    <param>com.library.*</param>
                </targetClasses>
                <targetTests>
                    <param>com.library.*</param>
                </targetTests>
                <outputFormats>
                    <outputFormat>XML</outputFormat>
                    <outputFormat>HTML</outputFormat>
                </outputFormats>
                <timestampedReports>false</timestampedReports>
            </configuration>
        </plugin>
    </plugins>
</build>
```

#### pom.xml - Dependências Necessárias

```xml
<dependencies>
    <!-- Spring Boot Starter Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Spring Boot Starter Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- Spring Boot Starter Data Redis -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>

    <!-- Spring Boot Starter Cache -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-cache</artifactId>
    </dependency>

    <!-- Spring Boot Actuator (para health checks) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>

    <!-- H2 Database (para testes e DEV) -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Lettuce (Redis client) -->
    <dependency>
        <groupId>io.lettuce</groupId>
        <artifactId>lettuce-core</artifactId>
    </dependency>

    <!-- Spring Boot Starter Test -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- JUnit 5 -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 3. Configuração do SonarQube (Opcional)

Se quiseres usar SonarQube:

#### No Jenkins

**Manage Jenkins → Configure System → SonarQube servers:**

Para ambiente **Docker**:
- Name: `sonarqube_docker`
- Server URL: `http://host.docker.internal:9000`
- Authentication Token: (criar no SonarQube)

Para ambiente **Local**:
- Name: `sonarqube_local`
- Server URL: `http://localhost:9000`
- Authentication Token: (criar no SonarQube)

### 4. Criar Pipeline Job

1. **New Item**
2. Nome: `library-management-pipeline`
3. Tipo: **Pipeline**
4. **OK**

#### Configuração do Job

**General:**
- ✓ Discard old builds
    - Max # of builds to keep: `10`

**Build Triggers:**
- ✓ GitHub hook trigger for GITScm polling

**Pipeline:**
- Definition: **Pipeline script from SCM**
- SCM: **Git**
- Repository URL: `https://github.com/seu-user/library-management.git`
- Credentials: (se necessário)
- Branch Specifier: `*/main`
- Script Path: `Jenkinsfile`

**Ou** se não tiveres Git ainda:
- Definition: **Pipeline script**
- Script: (cola o conteúdo do Jenkinsfile)

---

## 📊 Stages Detalhadas

### Stage 1: Checkout

**Objetivo:** Fazer checkout do código-fonte do repositório Git.

**O que faz:**
- Clone do repositório
- Captura informação do Git (branch, commit hash)
- Define variáveis de build

**Variáveis capturadas:**
- `GIT_COMMIT_SHORT`: Hash curto do commit
- `GIT_BRANCH`: Nome da branch
- `BUILD_TAG`: Tag única do build

**Exemplo de output:**
```
✓ Branch: main
✓ Commit: a1b2c3d
✓ Build: 42-a1b2c3d
```

---

### Stage 2: Build & Compile

**Objetivo:** Compilar o código-fonte e preparar classes de teste.

**Comandos executados:**
```bash
mvn clean compile test-compile
```

**O que faz:**
- Limpa artefactos antigos (`clean`)
- Compila código principal (`compile`)
- Compila classes de teste (`test-compile`)

**Critérios de sucesso:**
- ✓ Compilação sem erros
- ✓ Todas as dependências resolvidas

**Tempo estimado:** 30-60 segundos

---

### Stage 3: Unit & Integration Tests (PARALLEL)

**Objetivo:** Executar testes unitários e de integração em paralelo para otimizar tempo.

#### 3a. Unit Tests

**Comandos:**
```bash
mvn surefire:test
```

**O que testa:**
- Classes individuais
- Métodos isolados
- Lógica de negócio

**Naming conventions:**
- `*Test.java`
- `*Tests.java`

**Reports:**
- XML: `target/surefire-reports/*.xml`
- Publicado automaticamente no Jenkins

#### 3b. Integration Tests

**Comandos:**
```bash
mvn failsafe:integration-test failsafe:verify \
    -Dspring.redis.host=host.docker.internal \
    -Dspring.redis.port=6379
```

**O que testa:**
- Integração entre componentes
- Conexão com Redis
- Testes end-to-end de controllers

**Naming conventions:**
- `*IT.java`
- `*IntegrationTest.java`

**Configuração Redis:**
- Host: `host.docker.internal` (acesso ao Redis do Docker Desktop)
- Port: `6379`

**Reports:**
- XML: `target/failsafe-reports/*.xml`
- Publicado automaticamente no Jenkins

**Tempo estimado:** 1-3 minutos (paralelo)

---

### Stage 4: Code Coverage (JaCoCo)

**Objetivo:** Medir cobertura de código e garantir mínimo de 80%.

**Comandos:**
```bash
mvn jacoco:report
mvn jacoco:check -Djacoco.minimum.coverage=0.80
```

**Métricas:**
- **Line Coverage**: Percentagem de linhas executadas
- **Branch Coverage**: Percentagem de branches testados
- **Complexity**: Complexidade ciclomática

**Threshold:** Mínimo 80% line coverage

**Reports:**
- HTML: `target/site/jacoco/index.html`
- XML: `target/site/jacoco/jacoco.xml`
- Publicado no Jenkins com gráficos

**Exclusões configuradas:**
```xml
<exclusionPattern>**/test/**</exclusionPattern>
```

**Visualização no Jenkins:**
- Gráfico de tendência de cobertura
- Drill-down por package/classe
- Código-fonte anotado

**Tempo estimado:** 30 segundos

---

### Stage 5: Mutation Testing (PIT)

**Objetivo:** Testar a qualidade dos testes através de mutações no código.

**Comandos:**
```bash
mvn org.pitest:pitest-maven:mutationCoverage
```

**O que faz:**
- Introduz mutações no código (altera operadores, condições)
- Re-executa testes
- Verifica se testes detectam as mutações

**Métricas:**
- **Mutation Coverage**: % de mutações detectadas
- **Mutation Score**: Qualidade dos testes

**Exemplo de mutações:**
```java
// Original
if (x > 0) { ... }

// Mutação 1: Changed conditional boundary
if (x >= 0) { ... }

// Mutação 2: Negated conditional
if (x <= 0) { ... }
```

**Reports:**
- HTML: `target/pit-reports/index.html`
- Publicado no Jenkins

**Tempo estimado:** 2-5 minutos

---

### Stage 6: SonarQube Analysis

**Objetivo:** Análise estática de código para detectar bugs, vulnerabilities e code smells.

**Comandos:**
```bash
mvn verify sonar:sonar
```

**Configuração:**
- Ambiente **docker**: usa `sonarqube_docker` server
- Ambiente **local**: usa `sonarqube_local` server

**Métricas analisadas:**
- **Bugs**: Erros potenciais
- **Vulnerabilities**: Problemas de segurança
- **Code Smells**: Má qualidade de código
- **Coverage**: Cobertura de testes
- **Duplications**: Código duplicado
- **Maintainability**: Índice de manutenibilidade

**Nota:** Se SonarQube não estiver disponível, o stage é saltado automaticamente.

**Tempo estimado:** 1-2 minutos

---

### Stage 7: Quality Gate 1 (QG1)

**Objetivo:** Validar métricas de qualidade do SonarQube.

**Critérios padrão:**
- ✓ Bugs: 0
- ✓ Vulnerabilities: 0
- ✓ Code Smells: < 5 por 1000 linhas
- ✓ Coverage: > 80%
- ✓ Duplications: < 3%

**Comportamento:**
- Se **PASSED**: Pipeline continua
- Se **FAILED**: Alerta mas continua (não bloqueia)
- Se **SonarQube indisponível**: Salta verificação

**Timeout:** 3 minutos

---

### Stage 8: Package

**Objetivo:** Criar artefacto final (JAR) da aplicação.

**Comandos:**
```bash
mvn package -DskipTests
```

**Output:**
- JAR file: `target/library-management-*.jar`

**Post-actions:**
- Arquiva artefactos no Jenkins
- Fingerprinting para rastreabilidade

**Tempo estimado:** 20-40 segundos

---

### Stage 9: Build Docker Image

**Objetivo:** Criar imagem Docker da aplicação.

**Condição:** Apenas quando `Environment == 'docker'`

**Comandos:**
```bash
docker build -t library-management-service:BUILD_TAG .
docker tag library-management-service:BUILD_TAG library-management-service:latest
```

**Dockerfile gerado automaticamente:**
```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=dev
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Tags criadas:**
- `library-management-service:BUILD_NUMBER-COMMIT_HASH`
- `library-management-service:latest`

**Tempo estimado:** 1-2 minutos

---

### Stage 10: Verify Redis Connection

**Objetivo:** Verificar conectividade com Redis antes do deployment.

**Verificações:**
1. Container Redis está running
2. Redis responde a PING
3. Estatísticas de Redis (keys, memory)

**Comandos:**
```bash
docker exec fervent_benz redis-cli ping
docker exec fervent_benz redis-cli INFO stats
```

**Output esperado:**
```
✓ Redis is responding
Redis Statistics:
keyspace_hits:1234
keyspace_misses:56
```

**Tempo estimado:** 5 segundos

---

### Stage 11: Deploy to Environment

**Objetivo:** Fazer deployment da aplicação para o ambiente escolhido.

**Condição:** Apenas quando `Environment == 'docker'`

#### Ambientes Disponíveis

| Ambiente | Porta | Container | Profile | Aprovação |
|----------|-------|-----------|---------|-----------|
| DEV | 8080 | library-app-dev | dev | Automático |
| STAGING | 8081 | library-app-staging | staging | Manual 🤚 |
| PRODUCTION | 8082 | library-app-prod | prod | Admin 🤚 |

#### Processo de Deployment

1. **Para DEV:**
   ```bash
   # Automático, sem aprovação
   docker run -d \
     --name library-app-dev \
     -p 8080:8080 \
     -e SPRING_PROFILES_ACTIVE=dev \
     -e SPRING_REDIS_HOST=host.docker.internal \
     -e SPRING_REDIS_PORT=6379 \
     library-management-service:BUILD_TAG
   ```

2. **Para STAGING:**
   ```bash
   # Pede aprovação manual
   # Depois executa:
   docker run -d \
     --name library-app-staging \
     -p 8081:8080 \
     -e SPRING_PROFILES_ACTIVE=staging \
     ...
   ```

3. **Para PRODUCTION:**
   ```bash
   # Pede aprovação do ADMIN
   # Cria backup da versão atual
   # Depois executa deployment
   ```

#### Variáveis de Ambiente

Injetadas em cada container:
- `SPRING_PROFILES_ACTIVE`: dev/staging/prod
- `SPRING_REDIS_HOST`: host.docker.internal
- `SPRING_REDIS_PORT`: 6379
- `SPRING_CACHE_TYPE`: redis

**Tempo estimado:** 15-30 segundos

---

### Stage 12: Health Check

**Objetivo:** Verificar se a aplicação deployada está saudável.

**Verificações:**
1. Container está running
2. Aplicação responde em `/actuator/health`
3. Status code: 200 OK

**Comandos:**
```bash
curl -f http://localhost:PORT/actuator/health
```

**Retry:** 3 tentativas com intervalo de 5 segundos

**Response esperado:**
```json
{
  "status": "UP",
  "components": {
    "redis": {
      "status": "UP"
    },
    "db": {
      "status": "UP"
    }
  }
}
```

**Tempo estimado:** 15 segundos

---

### Stage 13: Quality Gate Final (QG4)

**Objetivo:** Validação final antes de considerar deployment em PROD bem-sucedido.

**Condição:** Apenas quando `DeploymentTarget == 'production'`

**Verificações:**
1. Health check retorna 200
2. Aplicação acessível
3. Redis operacional
4. Sem erros nos logs

**Critérios de falha:**
- Health check ≠ 200
- Timeout na resposta
- Erros críticos nos logs

**Se falhar:**
- ❌ Pipeline abortado
- 🔄 Rollback automático iniciado
- 📧 Notificações enviadas (se configurado)

**Tempo estimado:** 10 segundos

---

## 🎛️ Parâmetros

A pipeline aceita 2 parâmetros configuráveis:

### 1. Environment

**Tipo:** Choice Parameter

**Opções:**
- `docker` - Executa deployment em containers Docker
- `local` - Executa testes localmente (sem deployment)

**Uso:**
- Seleciona servidor SonarQube correto
- Ativa/desativa stages de Docker
- Define estratégia de deployment

**Default:** `docker`

### 2. DeploymentTarget

**Tipo:** Choice Parameter

**Opções:**
- `dev` - Deployment automático em DEV (porta 8080)
- `staging` - Deployment com aprovação manual (porta 8081)
- `production` - Deployment com aprovação admin (porta 8082)

**Regras:**
- **DEV**: Deployment automático após QG1
- **STAGING**: Requer clique em "Proceed"
- **PRODUCTION**: Requer aprovação de usuário com role "admin"

**Default:** `dev`

---

## 🌍 Variáveis de Ambiente

### Variáveis de Configuração

```groovy
environment {
    // Maven
    MAVEN_DIR = tool(name: 'Maven 3.9.11', type: 'maven')
    
    // Redis
    REDIS_HOST = 'host.docker.internal'
    REDIS_PORT = '6379'
    REDIS_CONTAINER = 'fervent_benz'
    
    // Portas
    DEV_PORT = '8080'
    STAGING_PORT = '8081'
    PROD_PORT = '8082'
    
    // Docker
    DOCKER_IMAGE_NAME = 'library-management-service'
    BUILD_TAG = "${BUILD_NUMBER}-${GIT_COMMIT}"
    
    // SonarQube
    SONAR_SERVER_DOCKER = 'sonarqube_docker'
    SONAR_SERVER_LOCAL = 'sonarqube_local'
}
```

### Variáveis Dinâmicas

Criadas durante a execução:

```groovy
GIT_COMMIT_SHORT  // Hash curto do commit (7 chars)
GIT_BRANCH        // Nome da branch atual
BUILD_TAG         // Tag única: BUILD_NUMBER-COMMIT_HASH
```

### Como Customizar

Para alterar valores, edita o bloco `environment` no Jenkinsfile:

```groovy
environment {
    // Exemplo: Alterar portas
    DEV_PORT = '9080'
    STAGING_PORT = '9081'
    PROD_PORT = '9082'
    
    // Exemplo: Alterar nome do container Redis
    REDIS_CONTAINER = 'meu-redis'
}
```

---

## ⚠️ Quality Gates

### Quality Gate 1 (QG1) - Após SonarQube

**Localização:** Stage 7

**Critérios:**
- Bugs = 0
- Vulnerabilities = 0
- Code Smells < 5 per 1000 lines
- Coverage ≥ 80%
- Duplicated Lines < 3%
- Technical Debt Ratio < 5%

**Comportamento:**
- **PASS**: Pipeline continua
- **FAIL**: Aviso, mas continua
- **TIMEOUT**: Após 3 minutos, assume falha

**Configuração no SonarQube:**
1. Quality Gates → Create
2. Adicionar condições acima
3. Associar ao projeto

### Quality Gate 2 (QG2) - DEV Validation

**Localização:** Após Stage 12 (implícito)

**Critérios:**
- Health check = 200
- Redis conectado
- Sem erros nos logs

**Comportamento:**
- **PASS**: Permite avançar para STAGING
- **FAIL**: Pipeline abortado

### Quality Gate 3 (QG3) - STAGING Validation

**Localização:** Após Stage 12 para STAGING

**Critérios:**
- Todos os de QG2
- Performance aceitável (< 1s response time)
- Testes com dados parcialmente reais

**Comportamento:**
- **PASS**: Permite avançar para PROD
- **FAIL**: Pipeline abortado

### Quality Gate 4 (QG4) - PRODUCTION Final

**Localização:** Stage 13

**Critérios:**
- Health check = 200
- Smoke tests passam
- Aplicação acessível
- Redis operacional

**Comportamento:**
- **PASS**: Deployment bem-sucedido ✓
- **FAIL**: Rollback automático 🔄

**Rollback:**
```bash
docker stop library-app-prod
docker rm library-app-prod
docker run -d ... library-management-service:prod-backup
```

---

## 🚀 Deployment

### Estratégia de Deployment

#### DEV Environment

**Porta:** 8080  
**Aprovação:** Automática  
**Profile:** dev

**Características:**
- Deploy automático após QG1
- Usa H2 in-memory database
- Redis cache com TTL curto (10min)
- Logs em DEBUG

**Uso:**
- Desenvolvimento ativo
- Testes rápidos
- Validação de features

#### STAGING Environment

**Porta:** 8081  
**Aprovação:** Manual 🤚  
**Profile:** staging

**Características:**
- Deploy após aprovação manual
- Dados parcialmente reais
- Redis com persistência parcial
- Logs em INFO

**Uso:**
- QA testing
- User acceptance testing
- Performance testing

#### PRODUCTION Environment

**Porta:** 8082  
**Aprovação:** Admin 🤚  
**Profile:** prod

**Características:**
- Deploy apenas com aprovação admin
- Dados reais
- Redis com RDB + AOF persistence
- Logs em WARN
- Backup automático antes de deploy
- Rollback automático se falhar

**Uso:**
- Produção real
- Usuários finais
- Dados críticos

### Rollback

#### Automático

Acionado quando:
- QG4 falha
- Health check falha em PROD
- Timeout no deployment

**Processo:**
1. Para container em falha
2. Remove container
3. Restaura imagem backup
4. Inicia versão anterior
5. Verifica health
6. Log do evento

#### Manual

```bash
# Parar versão atual
docker stop library-app-prod
docker rm library-app-prod

# Ver versões disponíveis
docker images | grep library-management

# Restaurar versão específica
docker run -d \
  --name library-app-prod \
  -p 8082:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  library-management-service:VERSAO_ANTERIOR
```

### Blue-Green Deployment (Futuro)

Para implementar:
1. Criar container "blue" e "green"
2. Deploy na versão inativa
3. Trocar roteamento
4. Manter versão antiga como backup

---

## 📈 Reports e Métricas

### Reports Disponíveis

#### 1. JUnit Test Results

**Acesso:** Build → Test Results

**Conteúdo:**
- Total de testes
- Passados/Falhados/Skipped
- Tempo de execução
- Histórico de tendência

**Gráficos:**
- Test Results Trend
- Duration Trend

#### 2. JaCoCo Coverage Report

**Acesso:** Build → JaCoCo Coverage Report

**Métricas:**
- Instruction Coverage
- Branch Coverage
- Line Coverage
- Method Coverage
- Class Coverage

**Visualização:**
- Drill-down por package
- Código-fonte anotado
- Identificação de código não coberto

#### 3. PIT Mutation Report

**Acesso:** Build → PIT Mutation Testing Report

**Métricas:**
- Mutations Generated
- Mutations Killed
- Mutations Survived
- Mutation Coverage %

**Análise:**
- Mutantes por tipo
- Classes com mutantes sobreviventes
- Sugestões de melhoria de testes

#### 4. SonarQube Dashboard

**Acesso:** SonarQube → Projects → library-management

**Métricas:**
- Reliability (Bugs)
- Security (Vulnerabilities)
- Maintainability (Code Smells)
- Coverage
- Duplications
- Complexity

### Métricas de Pipeline

**Acesso:** Job → Build History

**Informações:**
- Tempo total de execução
- Tempo por stage
- Taxa de sucesso
- Frequência de builds
- Trend de qualidade

### Exportar Métricas

```bash
# Via Jenkins CLI
java -jar jenkins-cli.jar -s http://localhost:9090/ \
  get-job library-management-pipeline

# Via API
curl http://localhost:9090/job/library-management-pipeline/api/json
```

---

## 🐛 Troubleshooting

### Problemas Comuns

#### 1. Pipeline não inicia

**Sintoma:** Build não começa após trigger

**Possíveis causas:**
- Jenkins sem recursos
- Queue muito grande
- Executor ocupado

**Solução:**
```bash
# Ver queue
curl http://localhost:9090/queue/api/json

# Ver executors
curl http://localhost:9090/computer/api/json

# Aumentar executors: Manage Jenkins → Configure System → # of executors
```

#### 2. Maven não encontrado

**Sintoma:**
```
mvn: command not found
```

**Solução:**
1. Manage Jenkins → Global Tool Configuration
2. Verificar que Maven está configurado
3. Nome deve ser exatamente: `Maven 3.9.11`

#### 3. Testes falham

**Sintoma:**
```
Tests run: 10, Failures: 2, Errors: 0, Skipped: 0
```

**Debug:**
```bash
# Ver logs detalhados
cat target/surefire-reports/*.txt

# Executar localmente
mvn test -X

# Executar teste específico
mvn test -Dtest=BookServiceTest
```

#### 4. Redis não conecta

**Sintoma:**
```
Unable to connect to Redis; nested exception is 
io.lettuce.core.RedisConnectionException
```

**Verificar:**
```bash
# Container está running?
docker ps | grep redis

# Responde a ping?
docker exec fervent_benz redis-cli ping

# Porta certa?
docker port fervent_benz
```

**Solução:**
```bash
# Restart Redis
docker restart fervent_benz

# Verificar logs
docker logs fervent_benz
```

#### 5. Docker build falha

**Sintoma:**
```
ERROR: Cannot connect to Docker daemon
```

**Windows:**
- Abrir Docker Desktop
- Verificar que está running
- Verificar se "Expose daemon on tcp://localhost:2375" está ativado

**Linux/Mac:**
```bash
# Verificar Docker daemon
systemctl status docker

# Verificar permissões
sudo usermod -aG docker jenkins

# Restart Jenkins
systemctl restart jenkins
```

#### 6. SonarQube timeout

**Sintoma:**
```
WARN: Unable to get a new analysis report from the server. 
Timeout exceeded.
```

**Solução:**
```groovy
// Aumentar timeout no Jenkinsfile
stage('Quality Gate 1') {
    steps {
        timeout(time: 10, unit: 'MINUTES') { // Aumenta de 3 para 10
            waitForQualityGate abortPipeline: false
        }
    }
}
```

#### 7. Deployment falha

**Sintoma:**
```
Error response from daemon: Conflict. Container name already in use.
```

**Solução:**
```bash
# Parar container existente
docker stop library-app-dev
docker rm library-app-dev

# Ou forçar no Jenkinsfile (já incluído):
docker stop library-app-dev 2>/dev/null || true
docker rm library-app-dev 2>/dev/null || true
```

#### 8. Health check falha

**Sintoma:**
```
curl: (7) Failed to connect to localhost port 8080
```

**Debug:**
```bash
# Ver logs do container
docker logs library-app-dev --tail 50

# Entrar no container
docker exec -it library-app-dev sh

# Verificar se aplicação subiu
curl http://localhost:8080/actuator/health

# Verificar porta
netstat -an | grep 8080
```

#### 9. Out of Memory

**Sintoma:**
```
java.lang.OutOfMemoryError: Java heap space
```

**Solução:**
```bash
# Aumentar memória Docker
# Docker Desktop → Settings → Resources → Memory: 4GB+

# Ou ajustar no deployment
docker run -d \
  -e JAVA_OPTS="-Xms512m -Xmx1024m" \
  ...
```

#### 10. Permissões negadas

**Sintoma:**
```
Permission denied (publickey)
```

**Para SSH:**
```bash
# Gerar chave
ssh-keygen -t rsa -b 4096

# Adicionar ao Jenkins
# Credentials → Add → SSH Username with private key
```

**Para Docker:**
```bash
# Adicionar usuário ao grupo docker
sudo usermod -aG docker $USER
newgrp docker
```

### Debug Avançado

#### Ver variáveis de ambiente

```groovy
stage('Debug') {
    steps {
        script {
            if (isUnix()) {
                sh 'env | sort'
            } else {
                bat 'set'
            }
        }
    }
}
```

#### Aumentar verbosidade Maven

```bash
mvn clean install -X  # Debug mode
mvn clean install -e  # Errors com stack trace
```

#### Logs em tempo real

```bash
# Seguir logs Jenkins
tail -f /var/jenkins_home/logs/jenkins.log

# Seguir logs do container
docker logs -f library-app-dev
```

### Suporte

Se o problema persistir:

1. ✅ Consulta este documento
2. ✅ Verifica logs do Jenkins
3. ✅ Verifica logs dos containers
4. ✅ Testa comandos manualmente
5. ✅ Consulta documentação oficial

---

## 📞 Contactos e Recursos

### Documentação

- [Jenkins Pipeline Documentation](https://www.jenkins.io/doc/book/pipeline/)
- [Maven Documentation](https://maven.apache.org/guides/)
- [Docker Documentation](https://docs.docker.com/)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/)
- [Redis Documentation](https://redis.io/documentation)

### Ferramentas

- **Jenkins:** http://localhost:9090 (ou a tua porta)
- **SonarQube:** http://localhost:9000
- **Redis:** localhost:6379

---

**Documentação criada para ODSOFT 2025-2026 Project 1**  
**Última atualização:** 2025-11-02  
**Versão:** 1.0