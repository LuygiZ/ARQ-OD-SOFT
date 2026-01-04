# ODSOFT - Jenkins Setup Guide

## Student C - Nuno Oliveira
## LMS Microservices - Lending Service

---

## 1. Instalar Jenkins

### Opção A: Docker (Recomendado)

```bash
# Criar network
docker network create jenkins

# Correr Jenkins
docker run -d \
  --name jenkins \
  --network jenkins \
  -p 8080:8080 \
  -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  jenkins/jenkins:lts-jdk21

# Ver password inicial
docker logs jenkins 2>&1 | grep -A 2 "initial"
```

### Opção B: Windows Installer

1. Download: https://www.jenkins.io/download/
2. Instalar o ficheiro `.msi`
3. Aceder a http://localhost:8080
4. Usar password de `C:\ProgramData\Jenkins\.jenkins\secrets\initialAdminPassword`

---

## 2. Configurar Jenkins

### 2.1 Plugins Necessários

Ir a **Manage Jenkins > Manage Plugins > Available** e instalar:

- **Pipeline** (se não estiver instalado)
- **Git**
- **GitHub Integration**
- **Docker Pipeline**
- **Docker**
- **Email Extension**
- **JUnit**
- **JaCoCo**
- **HTML Publisher**
- **Checkstyle** (ou Warnings Next Generation)

### 2.2 Configurar Ferramentas

Ir a **Manage Jenkins > Global Tool Configuration**:

#### Maven
- Name: `Maven 3.9.12`
- Install automatically: ✓
- Version: 3.9.12

#### JDK
- Name: `JDK21`
- Install automatically: ✓
- Version: 21

#### Git
- Name: `Default`
- Path: `git` (ou caminho completo no Windows)

### 2.3 Configurar Docker

Se usares Docker:

```bash
# Dar permissões ao Jenkins para usar Docker
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins
```

No Windows, garantir que Docker Desktop está a correr.

### 2.4 Configurar Email (Criterion 3.2)

Ir a **Manage Jenkins > Configure System**:

1. **Extended E-mail Notification**:
   - SMTP Server: `smtp.gmail.com`
   - Port: 465
   - Use SSL: ✓
   - Credentials: (criar com email e app password do Gmail)
   - Default Recipients: `nuno.oliveira@student.isep.ipp.pt`

2. **E-mail Notification** (simples):
   - SMTP Server: `smtp.gmail.com`
   - Default user e-mail suffix: `@student.isep.ipp.pt`
   - Use SSL: ✓

---

## 3. Criar Pipeline Job

### 3.1 Novo Job

1. **New Item** > Nome: `LMS-Microservices-StudentC`
2. Selecionar **Pipeline**
3. Click **OK**

### 3.2 Configurar Pipeline

Na configuração do job:

#### General
- [x] GitHub project
  - Project url: `https://github.com/LuygiZ/ARQ-OD-SOFT`

#### Build Triggers
- [x] GitHub hook trigger for GITScm polling
- [x] Poll SCM: `H/5 * * * *`

#### Pipeline
- Definition: **Pipeline script from SCM**
- SCM: **Git**
- Repository URL: `https://github.com/LuygiZ/ARQ-OD-SOFT.git`
- Branch: `*/main`
- Script Path: `Docs/student-c/odsoft/Jenkinsfile`

### 3.3 Guardar e Executar

1. Click **Save**
2. Click **Build with Parameters**
3. Selecionar:
   - Environment: `dev`
   - SKIP_TESTS: `false`
   - ENABLE_DARK_LAUNCH: `false`
   - RELEASE_PERCENTAGE: `10`
4. Click **Build**

---

## 4. Verificar Pipeline

### 4.1 Logs

- Ir ao job > último build > **Console Output**
- Verificar cada stage

### 4.2 Stages Esperados

```
✓ 1. Build & Compile
✓ 1.1 Static Analysis (Checkstyle)
✓ 1.2 Unit Tests
✓ 1.3 Mutation Tests (PIT)
✓ 1.4 Contract Tests (Pact CDC)
✓ 1.5 Package & Build Containers
✓ 2.2 Push to Docker Registry
✓ 3.1 Deploy to DEV (Auto)
✓ 3.6a Smoke Tests DEV
✓ 3.4 Load Tests
✓ 3.5 Auto-Scale Services
✓ 3. Deploy to STAGING
✓ 3.6b Smoke Tests STAGING
✓ 3.2 Manual Approval (WAIT)
✓ 3.8 Deploy to PRODUCTION (Blue-Green)
✓ 3.7 Health Checks PRODUCTION
✓ 4.3 Dark Launch Configuration
✓ 4.4 Gradual Release
```

---

## 5. Troubleshooting

### Maven não encontrado

```groovy
// No Jenkinsfile, verificar:
tools {
    maven 'Maven 3.9.12'  // Nome deve corresponder ao configurado
    jdk 'JDK21'
}
```

### Docker não funciona

```bash
# Linux
sudo chmod 666 /var/run/docker.sock

# Windows
# Verificar se Docker Desktop está a correr
# Settings > General > "Expose daemon on tcp://localhost:2375"
```

### Email não envia

1. Verificar credenciais
2. Para Gmail, criar App Password:
   - https://myaccount.google.com/apppasswords
3. Testar em **Manage Jenkins > Configure System > Test configuration**

### Tests falham

```bash
# Correr localmente primeiro
mvn test -pl lending-service

# Se PIT falhar
mvn org.pitest:pitest-maven:mutationCoverage -pl lending-service -DmutationThreshold=80
```

---

## 6. Endpoints para Testar

Após deploy bem-sucedido:

| Environment | URL |
|-------------|-----|
| DEV | http://localhost:8086/swagger-ui/index.html |
| STAGING | http://localhost:8186/swagger-ui/index.html |
| PRODUCTION | http://localhost:8286/swagger-ui/index.html |

### Health Checks

```bash
curl http://localhost:8086/actuator/health
curl http://localhost:8186/actuator/health
curl http://localhost:8286/actuator/health
```

---

## 7. Critérios ODSOFT Cobertos

| Critério | Descrição | Stage |
|----------|-----------|-------|
| 1.1 | Static Tests (Checkstyle) | 1.1 |
| 1.2 | Unit Tests | 1.2 |
| 1.3 | Mutation Tests (PIT) | 1.3 |
| 1.4 | CDC Tests (Pact) | 1.4 |
| 1.5 | Container Build | 1.5 |
| 2.1 | Infrastructure as Code | deploy functions |
| 2.2 | Docker Push | 2.2 |
| 3.1 | Auto Deploy Service A | 3.1 |
| 3.2 | Manual Approval + Email | 3.2 |
| 3.3 | Deploy Remote (Docker) | 3.x |
| 3.4 | Load Tests | 3.4 |
| 3.5 | Auto-Scale | 3.5 |
| 3.6 | Smoke Tests | 3.6a, 3.6b |
| 3.7 | Health Checks | 3.7 |
| 3.8 | Rollout Strategy (Blue-Green) | 3.8 |
| 4.1 | Zero Downtime | 3.8 |
| 4.2 | Auto Rollback | post failure |
| 4.3 | Dark Launch / Kill Switch | 4.3 |
| 4.4 | Gradual Release | 4.4 |

---

## 8. Ficheiros Criados

```
Docs/student-c/odsoft/
├── Jenkinsfile           # Pipeline principal
├── checkstyle.xml        # Configuração Checkstyle
├── feature-flags.json    # Dark Launch / Kill Switch
├── JENKINS-SETUP.md      # Este documento
├── jmeter/
│   └── load-test.jmx     # Testes de carga
└── scripts/
    ├── scale-services.sh # Auto-scaling
    └── rollback.sh       # Rollback automático
```

---

**ODSOFT 2025/2026 - Student C - Nuno Oliveira**
