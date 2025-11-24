# 🚀 Guia Rápido de Início

## ⏱️ Setup em 10 Minutos

Este guia vai ajudar-te a configurar e executar a pipeline rapidamente.

---

## ✅ Checklist Pré-requisitos

Antes de começar, verifica que tens:

- [ ] Docker Desktop instalado e a correr
- [ ] Container Jenkins a correr
- [ ] Container Redis (`fervent_benz`) a correr
- [ ] Projeto Maven com código-fonte
- [ ] Git inicializado no projeto

---

## 📝 Passo 1: Preparar o Projeto (3 min)

### 1.1. Copiar ficheiros

```bash
cd /caminho/para/teu/projeto

# Copiar Jenkinsfile
cp Jenkinsfile-Final Jenkinsfile

# Copiar configuração Redis
cp RedisConfig.java src/main/java/com/library/config/

# Copiar exemplo de serviço com cache
cp BookService.java src/main/java/com/library/service/
```

### 1.2. Atualizar pom.xml

Adiciona estes plugins ao `pom.xml`:

```xml
<build>
    <plugins>
        <!-- Surefire -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.2.2</version>
        </plugin>

        <!-- Failsafe -->
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
        </plugin>

        <!-- JaCoCo -->
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
            </executions>
        </plugin>

        <!-- PIT -->
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
        </plugin>
    </plugins>
</build>
```

### 1.3. Adicionar Spring Boot Actuator

No `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

No `application.properties`:

```properties
# Actuator
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=always
```

### 1.4. Criar profiles de ambiente

**application-dev.properties:**
```properties
spring.profiles.active=dev

# Redis
spring.cache.type=redis
spring.data.redis.host=${SPRING_REDIS_HOST:localhost}
spring.data.redis.port=${SPRING_REDIS_PORT:6379}

# H2 Database
spring.datasource.url=jdbc:h2:mem:library_dev
spring.h2.console.enabled=true

# Logging
logging.level.root=INFO
logging.level.com.library=DEBUG
```

**application-staging.properties:**
```properties
spring.profiles.active=staging

spring.cache.type=redis
spring.data.redis.host=${SPRING_REDIS_HOST:localhost}
spring.data.redis.port=${SPRING_REDIS_PORT:6379}

spring.datasource.url=jdbc:h2:mem:library_staging

logging.level.root=INFO
```

**application-prod.properties:**
```properties
spring.profiles.active=prod

spring.cache.type=redis
spring.data.redis.host=${SPRING_REDIS_HOST:localhost}
spring.data.redis.port=${SPRING_REDIS_PORT:6379}

spring.datasource.url=jdbc:h2:mem:library_prod

logging.level.root=WARN
```

---

## ⚙️ Passo 2: Configurar Jenkins (5 min)

### 2.1. Configurar Tools

1. Acede ao Jenkins (porta que usas)
2. **Manage Jenkins** → **Global Tool Configuration**

**Maven:**
- Add Maven
- Name: `Maven 3.9.11` (tem que ser exatamente este nome!)
- Install automatically: ✓
- Version: 3.9.11
- Save

**JDK (se não estiver):**
- Add JDK
- Name: `JDK-17`
- Install automatically: ✓
- Version: OpenJDK 17
- Save

### 2.2. Instalar Plugins

**Manage Jenkins** → **Manage Plugins** → **Available**

Procura e instala:
- [ ] Docker Pipeline
- [ ] JaCoCo
- [ ] HTML Publisher
- [ ] JUnit (normalmente já instalado)

Clica **Install without restart**

### 2.3. Criar Pipeline Job

1. **New Item**
2. Nome: `library-management-pipeline`
3. Tipo: **Pipeline**
4. OK

**Configuração:**

**General:**
- ✓ Discard old builds: 10

**This project is parameterized:**
- Add Parameter → Choice Parameter
    - Name: `Environment`
    - Choices:
      ```
      docker
      local
      ```
    - Description: `Escolhe o ambiente`

- Add Parameter → Choice Parameter
    - Name: `DeploymentTarget`
    - Choices:
      ```
      dev
      staging
      production
      ```
    - Description: `Escolhe onde fazer deployment`

**Pipeline:**
- Definition: **Pipeline script from SCM** (se tiveres Git)
    - SCM: Git
    - Repository URL: `https://github.com/teu-user/teu-repo.git`
    - Branch: `*/main`
    - Script Path: `Jenkinsfile`

**OU** se não tiveres Git:
- Definition: **Pipeline script**
- Script: (cola o conteúdo do `Jenkinsfile-Final`)

**Save**

---

## 🎯 Passo 3: Primeira Execução (2 min)

### 3.1. Executar Pipeline

1. Vai para o job `library-management-pipeline`
2. Clica **Build with Parameters**
3. Seleciona:
    - Environment: `docker`
    - DeploymentTarget: `dev`
4. Clica **Build**

### 3.2. Acompanhar Execução

- Ver progresso em tempo real: Clica no build → **Console Output**
- Ver stages: Pipeline visualization na página do build

**Tempo estimado:** 5-10 minutos (primeira vez, download de dependências)

### 3.3. Verificar Resultado

Depois do build completar com sucesso:

```bash
# Verificar container
docker ps | grep library-app-dev

# Health check
curl http://localhost:8080/actuator/health

# Ver logs
docker logs library-app-dev
```

---

## 📊 Passo 4: Ver Reports

No Jenkins, na página do build:

1. **JaCoCo Coverage Report** - Cobertura de código
2. **PIT Mutation Testing Report** - Mutation tests
3. **Test Results** - Resultados dos testes

---

## 🎉 Sucesso!

Se chegaste aqui, tens:

- ✅ Pipeline configurada
- ✅ Build executado com sucesso
- ✅ Aplicação deployada em DEV
- ✅ Reports disponíveis
- ✅ Redis integrado

---

## 🔄 Próximas Execuções

### Deploy para STAGING

1. **Build with Parameters**
2. Environment: `docker`
3. DeploymentTarget: `staging`
4. Build
5. **Aguarda aprovação manual** - clica "Proceed" quando aparecer
6. Aplicação deployada na porta 8081

### Deploy para PRODUCTION

1. **Build with Parameters**
2. Environment: `docker`
3. DeploymentTarget: `production`
4. Build
5. **Aguarda aprovação ADMIN** - apenas admins podem aprovar
6. Aplicação deployada na porta 8082

---

## 🔧 Comandos Úteis

### Verificar Redis

```bash
# Conectar ao Redis
docker exec -it fervent_benz redis-cli

# Comandos úteis:
PING                    # Testa conexão
KEYS *                  # Lista todas as keys
DBSIZE                 # Número de keys
INFO stats             # Estatísticas
GET <key>              # Ver valor de uma key
```

### Verificar Aplicações

```bash
# Health checks
curl http://localhost:8080/actuator/health  # DEV
curl http://localhost:8081/actuator/health  # STAGING
curl http://localhost:8082/actuator/health  # PROD

# Ver logs
docker logs library-app-dev
docker logs -f library-app-staging  # Follow logs
docker logs --tail 50 library-app-prod  # Últimas 50 linhas
```

### Gestão de Containers

```bash
# Listar containers
docker ps

# Parar container
docker stop library-app-dev

# Remover container
docker rm library-app-dev

# Parar todos os containers da aplicação
docker stop library-app-dev library-app-staging library-app-prod
```

### Maven Local

```bash
# Executar testes
mvn test

# Ver cobertura
mvn jacoco:report
open target/site/jacoco/index.html

# Mutation tests
mvn org.pitest:pitest-maven:mutationCoverage
open target/pit-reports/index.html

# Build completo
mvn clean install
```

---

## 🐛 Problemas Comuns

### Pipeline não inicia

**Verifica:**
```bash
# Docker Desktop está a correr?
docker ps

# Jenkins está acessível?
curl http://localhost:PORTA_JENKINS

# Maven configurado corretamente?
# Jenkins → Global Tool Configuration → Maven
```

### Redis não conecta

**Verifica:**
```bash
# Container Redis está running?
docker ps | grep redis

# Testa conexão
docker exec fervent_benz redis-cli ping
```

**Se não responder:**
```bash
docker restart fervent_benz
```

### Tests falham

**Debug:**
```bash
# Ver detalhes dos testes
cat target/surefire-reports/*.txt

# Executar um teste específico
mvn test -Dtest=NomeDoTeste -X
```

### Port already in use

**Solução:**
```bash
# Ver o que está a usar a porta
lsof -i :8080

# Parar o container
docker stop library-app-dev
```

---

## 📚 Documentação Completa

Para mais detalhes, consulta:

- **[PIPELINE-DOCUMENTATION.md](PIPELINE-DOCUMENTATION.md)** - Documentação completa de todas as stages
- **[CHEATSHEET.md](CHEATSHEET.md)** - Comandos úteis
- **[INDEX.md](INDEX.md)** - Índice de todos os ficheiros

---

## ✨ Dicas

1. **Primeira execução é lenta** - Maven vai fazer download de dependências
2. **Usa DEV para testes rápidos** - Deployment automático
3. **STAGING para validação** - Antes de ir para produção
4. **PRODUCTION só quando tudo OK** - Tem aprovação admin
5. **Verifica logs** - `docker logs` é teu amigo
6. **Health checks são essenciais** - Usa `/actuator/health`

---

## 🎯 Checklist Final

Depois da primeira execução bem-sucedida:

- [ ] Pipeline executou sem erros
- [ ] Todos os testes passaram
- [ ] Coverage > 80%
- [ ] Mutation tests executaram
- [ ] Docker image criada
- [ ] Container DEV a correr
- [ ] Health check retorna 200
- [ ] Redis conectado
- [ ] Reports disponíveis no Jenkins

Se tudo está ✓, estás pronto para desenvolver! 🎉

---

**Boa sorte com o projeto ODSOFT 2025-2026! 🚀**

---

## 💡 Preciso de Ajuda?

1. Consulta **PIPELINE-DOCUMENTATION.md** secção "Troubleshooting"
2. Verifica logs do Jenkins e containers
3. Testa comandos manualmente
4. Usa o **CHEATSHEET.md** para comandos rápidos