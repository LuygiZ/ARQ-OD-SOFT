# 📚 LMS - Library Management System (Microservices)

Sistema de Gestão de Biblioteca reengenhado de arquitetura monolítica para microserviços usando **Attribute-Driven Design (ADD)**.

## 🎯 Objetivos do Projeto

Transformação arquitetural do LMS seguindo metodologia ADD para alcançar:
- ✅ **Escalabilidade independente** de cada domínio
- ✅ **Database-per-Service** com polyglot persistence
- ✅ **Event-Driven Architecture** para comunicação assíncrona
- ✅ **Consistência eventual** com Saga Pattern e Outbox Pattern
- ✅ **CQRS** para otimização de leitura/escrita (Book Service)
- ✅ **Observabilidade** com health checks e métricas

---

## 🏗️ Arquitetura

### 📊 Microserviços

| Serviço | Status | Porta | Descrição | Database | Padrões |
|---------|--------|-------|-----------|----------|---------|
| **Genre Service** | ✅ Completo | 8081 | Gestão de Géneros Literários | PostgreSQL (genre_db) | Outbox, Domain Events, Caching |
| **Author Service** | 🔄 Planeado | 8082 | Gestão de Autores | PostgreSQL (author_db) | Outbox, Domain Events |
| **Book Service** | 🔄 Planeado | 8083 | Gestão de Livros | PostgreSQL (book_db + replicas) | CQRS, Event Sourcing |
| **Reader Service** | 🔄 Planeado | 8085 | Gestão de Leitores | PostgreSQL (reader_db) | Outbox, Domain Events |
| **Lending Service** | 🔄 Planeado | 8086 | Gestão de Empréstimos | PostgreSQL (lending_db) | Saga Pattern, Outbox |
| **User Service** | 🔄 Planeado | 8087 | Autenticação & Autorização | PostgreSQL (user_db) | OAuth2, JWT |
| **Saga Orchestrator** | 🔄 Planeado | 8084 | Coordenação de Transações Distribuídas | Redis (state) | Saga Orchestration |

### 🛠️ Infraestrutura

| Componente | Versão | Porta(s) | Descrição |
|------------|--------|----------|-----------|
| **PostgreSQL** | 15-alpine | 5432 | Database-per-Service + Read Replicas |
| **Redis** | 7-alpine | 6379 | L2 Cache + Distributed Lock + Saga State |
| **RabbitMQ** | 3-management | 5672, 15672 | Message Broker (Events & Commands) |
| **Shared Kernel** | 1.0.0 | - | Domain Events, DTOs, Exceptions, Base Classes |

### 🔄 Padrões Arquiteturais Implementados

#### ✅ Strangler Fig Pattern
- Migração progressiva do monólito para microserviços
- Roteamento por domínio (Catalog, Lending, Users)

#### ✅ Database-per-Service
- Cada microserviço possui sua própria base de dados
- Autonomia total sobre schema e tecnologia de persistência

#### ✅ Outbox Pattern
- Garantia de consistência entre DB write e event publishing
- Eliminação de dual-write problems
- Retry automático com backoff exponencial

#### ✅ Domain Events
- Comunicação assíncrona entre bounded contexts
- Event-driven architecture com RabbitMQ
- Routing key pattern: `catalog.{aggregate}.{eventType}`

#### ✅ CQRS (Command Query Responsibility Segregation)
- Separação de modelos de leitura e escrita (Book Service)
- Read replicas para queries
- Event Sourcing para auditoria completa

#### ✅ Saga Pattern (Orchestration)
- Coordenação de transações distribuídas
- Compensating transactions para rollback
- State machine com Redis

#### ✅ Caching Strategy
- Redis como L2 distributed cache
- Cache-aside pattern
- TTL de 1 hora por padrão
- Invalidação por domain events

---

## 🚀 Quick Start

### 📋 Pré-requisitos

#### Software Necessário
- **Docker** 24.0+ e **Docker Compose** 2.20+
- **Java JDK** 21
- **Maven** 3.9+
- **Git**

#### Verificar Instalação
```bash
docker --version
docker-compose --version
java -version
mvn -version
```

---

## 🔨 Build & Deploy

### 1️⃣ Build do Shared Kernel

```bash
cd shared-kernel
mvn clean install
```

**Output esperado:**
```
[INFO] Installing /path/to/lms-shared-kernel-1.0.0.jar to ~/.m2/repository/pt/psoft/lms-shared-kernel/1.0.0/
[INFO] BUILD SUCCESS
```

---

### 2️⃣ Build do Genre Service

```bash
cd ../genre-service
mvn clean package

# Build da Docker image
docker build -t genre-service:latest .
```

**Output esperado:**
```
[INFO] Building jar: /path/to/genre-service-1.0.0.jar
[INFO] BUILD SUCCESS

Successfully tagged genre-service:latest
```

---

### 3️⃣ Deploy da Infraestrutura Completa

```bash
cd ../infrastructure

# Iniciar todos os serviços
docker-compose up -d

# Ver logs em tempo real
docker-compose logs -f

# Ver apenas logs do genre-service
docker-compose logs -f genre-service
```

**Ordem de inicialização (automática via depends_on):**
1. PostgreSQL (healthcheck: pg_isready)
2. Redis (healthcheck: redis-cli ping)
3. RabbitMQ (healthcheck: rabbitmqctl status)
4. Genre Service (aguarda infraestrutura healthy)

---

### 4️⃣ Verificar Status

```bash
# Status de todos os containers
docker-compose ps

# Saúde dos serviços
docker-compose ps | grep healthy
```

**Output esperado:**
```
NAME              STATUS          PORTS
postgres_lms      Up (healthy)    0.0.0.0:5432->5432/tcp
redis_lms         Up (healthy)    0.0.0.0:6379->6379/tcp
rabbitmq_lms      Up (healthy)    0.0.0.0:5672->5672/tcp, 0.0.0.0:15672->15672/tcp
genre-service     Up (healthy)    0.0.0.0:8081->8080/tcp
```

---

## 🧪 Testes e Validação

### Health Checks

```bash
# PowerShell (usando curl.exe nativo)
curl.exe http://localhost:8081/actuator/health

# Ou Invoke-WebRequest
Invoke-WebRequest -Uri http://localhost:8081/actuator/health -UseBasicParsing
```

**Response esperada:**
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "diskSpace": {"status": "UP"},
    "ping": {"status": "UP"},
    "rabbit": {"status": "UP"},
    "redis": {"status": "UP"}
  }
}
```

---

### CRUD Operations

#### 1. Criar Géneros (POST)

```bash
# PowerShell
curl.exe -X POST http://localhost:8081/api/genres `
  -H "Content-Type: application/json" `
  -d "{\"name\":\"Ficção Científica\"}"

curl.exe -X POST http://localhost:8081/api/genres `
  -H "Content-Type: application/json" `
  -d "{\"name\":\"Romance\"}"

curl.exe -X POST http://localhost:8081/api/genres `
  -H "Content-Type: application/json" `
  -d "{\"name\":\"Thriller\"}"
```

**Response esperada:**
```json
{
  "id": 1,
  "name": "Ficção Científica"
}
```

#### 2. Listar Todos os Géneros (GET)

```bash
curl.exe http://localhost:8081/api/genres
```

**Response esperada:**
```json
[
  {"id": 1, "name": "Ficção Científica"},
  {"id": 2, "name": "Romance"},
  {"id": 3, "name": "Thriller"}
]
```

#### 3. Buscar por ID (GET)

```bash
curl.exe http://localhost:8081/api/genres/1
```

#### 4. Buscar por Nome (GET)

```bash
curl.exe "http://localhost:8081/api/genres/search?name=Romance"
```

#### 5. Atualizar Género (PUT)

```bash
curl.exe -X PUT http://localhost:8081/api/genres/1 `
  -H "Content-Type: application/json" `
  -d "{\"name\":\"Sci-Fi\"}"
```

#### 6. Eliminar Género (DELETE)

```bash
curl.exe -X DELETE http://localhost:8081/api/genres/1
```

---

### Verificar Outbox Pattern

**Ver eventos na base de dados:**
```bash
docker exec -it postgres_lms psql -U postgres -d genre_db -c "SELECT id, event_type, status, aggregate_id, created_at FROM outbox_events ORDER BY created_at DESC LIMIT 10;"
```

**Output esperado:**
```
 id | event_type | status    | aggregate_id | created_at
----+------------+-----------+--------------+----------------------------
  3 | CREATED    | PUBLISHED | 3            | 2025-12-24 18:50:00.123456
  2 | CREATED    | PUBLISHED | 2            | 2025-12-24 18:49:30.654321
  1 | CREATED    | PUBLISHED | 1            | 2025-12-24 18:49:00.987654
```

**Estados possíveis:**
- `PENDING`: Aguardando publicação
- `PUBLISHED`: Publicado com sucesso
- `FAILED`: Falha após 3 tentativas

---

### Verificar Eventos no RabbitMQ

**Via UI (Recomendado):**
1. Abrir http://localhost:15672
2. Login: `guest` / `guest`
3. Ir para **Exchanges** → `lms.events`
4. Ver **Bindings** e **Message rates**
5. Ir para **Queues** → `genre-service.events`
6. Ver **Messages** (ready/unacked)

**Via CLI:**
```bash
docker exec -it rabbitmq_lms rabbitmqctl list_exchanges
docker exec -it rabbitmq_lms rabbitmqctl list_queues
docker exec -it rabbitmq_lms rabbitmqctl list_bindings
```

---

### Verificar Cache Redis

```bash
# Ver todas as keys
docker exec -it redis_lms redis-cli KEYS "*"

# Ver conteúdo de uma key (genres)
docker exec -it redis_lms redis-cli GET "genres::all"

# Ver TTL de uma key
docker exec -it redis_lms redis-cli TTL "genres::all"

# Limpar todo o cache
docker exec -it redis_lms redis-cli FLUSHALL
```

---

## 🌐 Acessos aos Serviços

### APIs REST

| Endpoint | URL | Autenticação |
|----------|-----|--------------|
| Genre Service API | http://localhost:8081/api/genres | Nenhuma (dev) |
| Swagger UI | http://localhost:8081/swagger-ui.html | Nenhuma |
| Health Check | http://localhost:8081/actuator/health | Nenhuma |
| Metrics | http://localhost:8081/actuator/metrics | Nenhuma |

### Infraestrutura

| Interface | URL | Credenciais |
|-----------|-----|-------------|
| RabbitMQ Management | http://localhost:15672 | guest / guest |
| PostgreSQL | localhost:5432 | postgres / password |
| Redis | localhost:6379 | (sem password) |

### Swagger UI - Genre Service

Aceder via browser: http://localhost:8081/swagger-ui.html

**Endpoints disponíveis:**
- `GET /api/genres` - Listar todos
- `GET /api/genres/{id}` - Buscar por ID
- `GET /api/genres/search?name=X` - Buscar por nome
- `POST /api/genres` - Criar género
- `PUT /api/genres/{id}` - Atualizar género
- `DELETE /api/genres/{id}` - Eliminar género

---

## 🔧 Troubleshooting

### Container não inicia

```bash
# Ver logs detalhados
docker-compose logs genre-service

# Verificar status
docker-compose ps

# Restart de um serviço específico
docker-compose restart genre-service

# Rebuild completo
docker-compose down
docker-compose build --no-cache
docker-compose up -d
```

---

### Erro: "Not a managed type: OutboxEvent"

**Solução:** Adicionar `@EntityScan` no `@SpringBootApplication`:

```java
@EntityScan(basePackages = {
    "pt.psoft.genre.model",
    "pt.psoft.shared.messaging"
})
```

---

### Erro: "UnknownHostException: rabbitmq/redis"

**Causa:** Containers não estão na mesma Docker network.

**Solução:**
```bash
# Verificar network
docker network inspect lms_network

# Se necessário, recriar
docker-compose down
docker network rm lms_network
docker-compose up -d
```

---

### Health Check falha (503)

**Causa:** Dependências (PostgreSQL, Redis, RabbitMQ) não estão healthy.

**Diagnóstico:**
```bash
# Verificar saúde da infraestrutura
docker-compose ps

# Ver logs de cada componente
docker-compose logs postgres
docker-compose logs redis
docker-compose logs rabbitmq
```

**Solução:** Aguardar até todos os serviços ficarem `healthy` (pode demorar 30-60 segundos).

---

### OutboxEvents ficam PENDING

**Causas possíveis:**
1. RabbitMQ não está acessível
2. Scheduler não está ativo
3. Erro na serialização JSON

**Diagnóstico:**
```bash
# Ver logs do publisher
docker-compose logs genre-service | grep -i "outbox\|rabbit"

# Verificar RabbitMQ
docker exec -it rabbitmq_lms rabbitmqctl status

# Ver eventos PENDING
docker exec -it postgres_lms psql -U postgres -d genre_db -c "SELECT * FROM outbox_events WHERE status='PENDING';"
```

---

### Cache não funciona

**Verificar configuração:**
```bash
# Ver se Redis está UP
docker exec -it redis_lms redis-cli PING
# Deve retornar: PONG

# Ver keys no Redis
docker exec -it redis_lms redis-cli KEYS "*"

# Verificar TTL
docker exec -it redis_lms redis-cli TTL "genres::all"
```

**Forçar invalidação:**
```bash
# Limpar cache
docker exec -it redis_lms redis-cli FLUSHDB

# Fazer request que popula cache
curl.exe http://localhost:8081/api/genres
```

---

## 🔄 Comandos Úteis

### Docker Compose

```bash
# Iniciar todos os serviços
docker-compose up -d

# Parar todos os serviços
docker-compose down

# Parar e remover volumes (CUIDADO: perde dados)
docker-compose down -v

# Ver logs em tempo real
docker-compose logs -f

# Ver logs de um serviço específico
docker-compose logs -f genre-service

# Rebuild de um serviço
docker-compose build genre-service
docker-compose up -d genre-service

# Ver status
docker-compose ps

# Restart de um serviço
docker-compose restart genre-service

# Escalar serviço (horizontal scaling)
docker-compose up -d --scale genre-service=3
```

---

### PostgreSQL

```bash
# Conectar ao PostgreSQL
docker exec -it postgres_lms psql -U postgres

# Listar databases
docker exec -it postgres_lms psql -U postgres -c "\l"

# Conectar a uma database específica
docker exec -it postgres_lms psql -U postgres -d genre_db

# Ver tabelas
docker exec -it postgres_lms psql -U postgres -d genre_db -c "\dt"

# Executar query
docker exec -it postgres_lms psql -U postgres -d genre_db -c "SELECT * FROM genres;"

# Backup de uma database
docker exec postgres_lms pg_dump -U postgres genre_db > genre_db_backup.sql

# Restore de uma database
docker exec -i postgres_lms psql -U postgres genre_db < genre_db_backup.sql
```

---

### Redis

```bash
# Conectar ao Redis CLI
docker exec -it redis_lms redis-cli

# Ver todas as keys
docker exec -it redis_lms redis-cli KEYS "*"

# Ver valor de uma key
docker exec -it redis_lms redis-cli GET "genres::all"

# Eliminar uma key
docker exec -it redis_lms redis-cli DEL "genres::all"

# Limpar toda a cache
docker exec -it redis_lms redis-cli FLUSHALL

# Ver info do Redis
docker exec -it redis_lms redis-cli INFO

# Ver memória usada
docker exec -it redis_lms redis-cli INFO memory
```

---

### RabbitMQ

```bash
# Ver status
docker exec -it rabbitmq_lms rabbitmqctl status

# Listar exchanges
docker exec -it rabbitmq_lms rabbitmqctl list_exchanges

# Listar queues
docker exec -it rabbitmq_lms rabbitmqctl list_queues

# Listar bindings
docker exec -it rabbitmq_lms rabbitmqctl list_bindings

# Purge de uma queue
docker exec -it rabbitmq_lms rabbitmqctl purge_queue genre-service.events
```

---

## 📈 Próximos Passos

### 🔄 Em Desenvolvimento

1. **Author Service** - Similar ao Genre Service
    - Outbox Pattern
    - Domain Events
    - Redis caching

2. **Book Service** - Com CQRS
    - Command Model (Write)
    - Query Model (Read Replicas)
    - Event Sourcing

3. **Saga Orchestrator** - Para FR-1
    - State machine com Redis
    - Compensating transactions
    - Coordenação de Create Book + Author + Genre

### 📋 Backlog

- [ ] API Gateway (Traefik/Kong)
- [ ] Monitoring (Prometheus + Grafana)
- [ ] Distributed Tracing (Jaeger/Zipkin)
- [ ] Circuit Breaker (Resilience4j)
- [ ] Rate Limiting
- [ ] API Versioning
- [ ] Integration Tests
- [ ] Load Testing (K6/JMeter)
- [ ] Kubernetes deployment
- [ ] CI/CD Pipeline

---

## 📚 Documentação Adicional

### Documentos de Arquitetura

- **ADD (Attribute-Driven Design)**: Ver `Docs/ADD.pdf`
- **Domain Model**: Ver `Docs/DomainModel.md`
- **Event Catalog**: Ver `Docs/EventCatalog.md`
- **API Contracts**: Ver `Docs/APIContracts.md`

### Recursos Externos

- [Spring Boot 3 Documentation](https://docs.spring.io/spring-boot/docs/3.2.0/reference/html/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [RabbitMQ Tutorials](https://www.rabbitmq.com/tutorials)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Redis Documentation](https://redis.io/docs/)
- [Outbox Pattern](https://microservices.io/patterns/data/transactional-outbox.html)
- [Saga Pattern](https://microservices.io/patterns/data/saga.html)
- [CQRS Pattern](https://martinfowler.com/bliki/CQRS.html)

---

## 👥 Equipa

**Projeto Académico - ISEP 2024/2025**
- Curso: Mestrado em Engenharia Informática
- UCs: ARQSOFT (Arquitetura de Software) + ODSOFT (Organização e Desenvolvimento de Software)

---

## 📝 Licença

Este projeto é um trabalho académico desenvolvido no âmbito do Mestrado em Engenharia Informática do ISEP.

---

## 🎯 Requisitos Funcionais Implementados

### ✅ FR-1: Create Book with Author and Genre (via Saga)
**Status:** Parcialmente implementado
- ✅ Genre Service operacional
- 🔄 Author Service - em desenvolvimento
- 🔄 Book Service - em desenvolvimento
- 🔄 Saga Orchestrator - em desenvolvimento

**Fluxo esperado:**
1. API Gateway recebe `POST /api/books` com author e genre
2. Saga Orchestrator inicia transaction
3. Valida/Cria Genre (Genre Service)
4. Valida/Cria Author (Author Service)
5. Cria Book (Book Service)
6. Se falha → Compensating transactions
7. Retorna resultado agregado

---

## 🔐 Segurança (Planeado)

- **Autenticação**: OAuth2 + JWT
- **Autorização**: Role-based (Admin, Librarian, Reader)
- **API Gateway**: Rate limiting, CORS, SSL/TLS
- **Secrets Management**: Docker secrets / Vault
- **Audit Log**: Event Sourcing

---

