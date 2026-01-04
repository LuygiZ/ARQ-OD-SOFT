# DEFESA ORAL - Student C - Nuno Oliveira

## User Story
**"Como leitor, ao devolver um Book, quero deixar um comentário de texto sobre o Book e avaliá-lo (0-10)."**

---

# PARTE 1 - CHECKLIST DE CRITÉRIOS DE AVALIAÇÃO

## DESIGN (60%)

### 1.1 Alinhamento com requisitos de negócio (peso 0.4)

| Critério | Status | Evidência |
|----------|--------|-----------|
| Capabilities de negócio suportadas | ✅ | Lending Service suporta devolução com review |
| Strangler Fig documentado | ✅ | README.md principal documenta migração progressiva |
| Student C funcional | ✅ | Endpoint `POST /api/v1/lendings/{year}/{seq}/return` |

### 1.2 Design orientado por ADD (peso 0.4)

| Critério | Status | Evidência |
|----------|--------|-----------|
| Technical Memos | ✅ | `Docs/ARQSoft/P1/ADD/` - 3 memos completos |
| ADRs | ✅ | `Docs/student-c/ADR-001-CQRS-Reviews.md`, `ADR-002-Outbox-Pattern.md` |
| Diagramas C4/UML | ✅ | C4-Container.puml, Deployment-Diagram.puml, Logical-View-Level1/2.puml, CQRS-Book-Services.puml |
| Alternativas documentadas | ✅ | Nos ADRs e Technical Memos |

### 1.3 Padrões de Microservices (peso 0.4)

| Padrão | Status | Onde está implementado |
|--------|--------|------------------------|
| Strangler Fig | ✅ | Migração progressiva documentada no README |
| Domain Events | ✅ | `LendingReturnedEvent`, `LendingCreatedEvent` em `shared-kernel` |
| Messaging (RabbitMQ) | ✅ | `RabbitMQConfig.java`, exchange `lms.events` |
| CQRS | ✅ | Command: `LendingEntity`, Query: `BookReview` no Book Service |
| Database-per-Service | ✅ | `lending_db`, `book_db`, `author_db`, etc. |
| Polyglot Persistence | ✅ | PostgreSQL + MongoDB (Author Service) + Redis |
| Outbox Pattern | ✅ | `OutboxEventPublisher.java`, tabela `outbox_events` |
| Saga Pattern | ✅ | `saga-orchestrator` para criação de Book+Author+Genre |

---

## IMPLEMENTAÇÃO (60%)

### 2.1 Implementação reflete design (peso 0.3)

| Critério | Status | Evidência |
|----------|--------|-----------|
| ≥3 microservices colaborando | ✅ | 7 serviços: genre, author, book-command, book-query, lending, reader, saga |
| RabbitMQ messaging | ✅ | Topic exchange, routing keys configurados |
| Padrões em código | ✅ | CQRS, Outbox, Domain Events implementados |
| Polyglot persistence | ✅ | PostgreSQL, MongoDB, Redis |

### 2.2 Requisitos funcionais (peso 0.3)

| Requisito | Status | Ficheiros |
|-----------|--------|-----------|
| Student C - Devolver com review | ✅ | `LendingCommandController.java:54-78` |
| Comentário texto | ✅ | `ReturnLendingRequest.java` - campo `comment` |
| Rating 0-10 | ✅ | `ReturnLendingRequest.java` - validação `@Min(0) @Max(10)` |
| Evento publicado | ✅ | `LendingReturnedEvent.java` |

### 2.3 CDC Tests (peso 0.3)

| Critério | Status | Evidência |
|----------|--------|-----------|
| Pact configurado | ✅ | `pom.xml` tem pact-jvm 4.6.5 |
| Provider test | ✅ | `LendingReturnedEventProviderPactTest.java` |
| Cenários cobertos | ✅ | Com review, sem review, com multa |

### 2.4 Performance Testing (peso 0.3)

| Critério | Status | Evidência |
|----------|--------|-----------|
| Load tests | ⚠️ | **FALTA** - Não encontrei K6/JMeter/Gatling |
| Comparação monólito | ⚠️ | **FALTA** - Não documentado |
| Aumento 25% performance | ⚠️ | **FALTA** - Não demonstrado |

---

# PARTE 2 - DEEP DIVE STUDENT C

## 1. ARQUITETURA E MICROSERVICES

### Serviços Envolvidos no Student C

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────────────────────────┐
│  Reader Service │     │ Lending Service │     │           BOOK SERVICES (CQRS)      │
│    (8087)       │     │    (8086)       │     ├──────────────────┬──────────────────┤
│                 │     │                 │     │  Book Command    │   Book Query     │
│ - Autenticação  │     │ - Devoluções    │     │  Service (8083)  │   Service (8085) │
│ - JWT tokens    │     │ - Reviews       │     │                  │                  │
│ - Leitores      │     │ - Outbox        │     │  - Criar livros  │  - Consultas     │
│                 │     │                 │     │  - Atualizar     │  - Reviews/Stats │
│                 │     │                 │     │  - Eliminar      │  - Rating médio  │
└────────┬────────┘     └────────┬────────┘     └────────┬─────────┴────────┬─────────┘
         │                       │                       │                   │
         │      JWT Token        │   LendingReturned     │                   │
         └───────────────────────┤   Event (RabbitMQ)    ├───────────────────┘
                                 └───────────────────────┘

                          ┌─────────────────┐
                          │     Traefik     │ ← Load Balancer (2 instâncias por serviço)
                          │   (Port 80)     │
                          └────────┬────────┘
                                   │
              ┌────────────────────┼────────────────────┐
              │ POST/PUT/PATCH/DEL │       GET          │
              ▼                    ▼                    │
     Book Command Service    Book Query Service        │
     (2 replicas)            (2 replicas)              │
```

### Responsabilidade de cada serviço

| Serviço | Responsabilidade no Student C |
|---------|-------------------------------|
| **Reader Service** | Autentica o leitor, emite JWT token |
| **Lending Service** | Processa devolução, guarda comment+rating, publica evento |
| **Book Command Service** | CQRS Write Side - Criar/Atualizar/Eliminar livros |
| **Book Query Service** | CQRS Read Side - Consome eventos, cria `BookReview`, atualiza estatísticas |

### Comunicação

| Tipo | De → Para | Mecanismo |
|------|-----------|-----------|
| **Síncrono** | Cliente → Traefik → Serviços | HTTP REST |
| **Assíncrono** | Book Command → Book Query | RabbitMQ (BookCreated/Updated/Deleted) |
| **Assíncrono** | Lending → Book Query | RabbitMQ (LendingReturnedEvent) |

**Justificação da decomposição CQRS explícita:**
- **Book Command Service**: Responsável por operações de escrita (POST, PUT, PATCH, DELETE)
- **Book Query Service**: Responsável por operações de leitura (GET) e sincronização de read models
- **Separação física**: Permite escalar read e write independentemente
- **2 instâncias cada**: Load balancing via Traefik para alta disponibilidade
- **Database compartilhada**: Ambos os serviços usam `book_db` (command escreve, query lê)

---

## 2. FLUXO COMPLETO PASSO A PASSO

### Diagrama de Sequência

```
┌────────┐    ┌──────────────┐    ┌─────────────┐    ┌──────────┐    ┌─────────────┐
│ Reader │    │ Reader Svc   │    │ Lending Svc │    │ RabbitMQ │    │ Book Svc    │
└───┬────┘    └──────┬───────┘    └──────┬──────┘    └────┬─────┘    └──────┬──────┘
    │                │                   │                │                 │
    │ 1. POST /login │                   │                │                 │
    │───────────────>│                   │                │                 │
    │                │                   │                │                 │
    │ JWT Token      │                   │                │                 │
    │<───────────────│                   │                │                 │
    │                │                   │                │                 │
    │ 2. POST /lendings/2025/1/return    │                │                 │
    │ Headers: Authorization, If-Match   │                │                 │
    │ Body: {comment, rating}            │                │                 │
    │───────────────────────────────────>│                │                 │
    │                │                   │                │                 │
    │                │    3. findByLendingNumber()        │                 │
    │                │                   │────────┐       │                 │
    │                │                   │        │ DB    │                 │
    │                │                   │<───────┘       │                 │
    │                │                   │                │                 │
    │                │    4. lending.setReturned()        │                 │
    │                │                   │────────┐       │                 │
    │                │                   │ validate│      │                 │
    │                │                   │<───────┘       │                 │
    │                │                   │                │                 │
    │                │    5. lendingRepository.save()     │                 │
    │                │                   │────────┐       │                 │
    │                │                   │        │ DB    │                 │
    │                │                   │<───────┘       │                 │
    │                │                   │                │                 │
    │                │    6. outboxRepository.save()      │                 │
    │                │                   │────────┐       │                 │
    │                │                   │ OUTBOX │       │                 │
    │                │                   │<───────┘       │                 │
    │                │                   │                │                 │
    │ 7. HTTP 200 + LendingReturnView    │                │                 │
    │<───────────────────────────────────│                │                 │
    │                │                   │                │                 │
    │                │    8. OutboxPublisher (scheduled)  │                 │
    │                │                   │──────────────────────────────────>│
    │                │                   │  LendingReturnedEvent             │
    │                │                   │                │                  │
    │                │                   │                │  9. handleEvent()│
    │                │                   │                │                  │
    │                │                   │                │  10. createReview│
    │                │                   │                │────────┐        │
    │                │                   │                │        │ DB     │
    │                │                   │                │<───────┘        │
```

### Fluxo Detalhado com Classes e Métodos

#### Passo 1-2: Autenticação e Request
```
Cliente → Reader Service → JWT Token
Cliente → Lending Service: POST /api/v1/lendings/2025/1/return
   Headers: Authorization: Bearer <JWT>, If-Match: 1
   Body: {"comment": "Ótimo livro!", "rating": 8}
```

#### Passo 3: Controller recebe request
**Ficheiro:** `LendingCommandController.java:54-78`
```java
@PostMapping("/{year}/{sequence}/return")
public ResponseEntity<LendingReturnView> returnLending(
    @PathVariable int year,
    @PathVariable int sequence,
    @RequestHeader("If-Match") Long expectedVersion,
    @Valid @RequestBody ReturnLendingRequest request) {

    String lendingNumber = year + "/" + sequence;
    LendingEntity lending = lendingCommandService.returnLending(
        lendingNumber, request, expectedVersion);
    // ...
}
```

#### Passo 4: Service processa lógica de negócio
**Ficheiro:** `LendingCommandServiceImpl.java:97-137`
```java
@Transactional
public LendingEntity returnLending(String lendingNumber,
    ReturnLendingRequest request, Long expectedVersion) {

    // Parse e buscar lending
    LendingNumber ln = LendingNumber.parse(lendingNumber);
    LendingEntity lending = lendingRepository.findByLendingNumber(...)
        .orElseThrow(() -> new NotFoundException(...));

    // Validar que está ativo
    if (!lending.isActive()) {
        throw new BusinessException("Already returned");
    }

    // Marcar como devolvido COM review
    lending.setReturned(expectedVersion, request.getComment(), request.getRating());

    // Guardar
    LendingEntity saved = lendingRepository.save(lending);

    // Publicar evento (vai para Outbox)
    LendingReturnedEvent event = new LendingReturnedEvent(...);
    lendingEventPublisher.publishLendingReturned(event);

    return saved;
}
```

#### Passo 5: Entity valida e atualiza estado
**Ficheiro:** `LendingEntity.java:107-131`
```java
public void setReturned(Long expectedVersion, String comment, Integer rating) {
    // Já devolvido?
    if (this.returnedDate != null) {
        throw new IllegalStateException("Book has already been returned!");
    }

    // Optimistic locking
    if (expectedVersion != null && !this.version.equals(expectedVersion)) {
        throw new StaleObjectStateException("Lending", this.pk);
    }

    // Validar rating
    if (rating != null && (rating < 0 || rating > 10)) {
        throw new IllegalArgumentException("Rating must be between 0 and 10");
    }

    this.rating = rating;
    this.comment = comment;
    this.returnedDate = LocalDate.now();
}
```

#### Passo 6: Evento guardado no Outbox
**Ficheiro:** `LendingEventPublisher.java:37-50`
```java
public void publishLendingReturned(LendingReturnedEvent event) {
    OutboxEvent outboxEvent = new OutboxEvent(
        "LENDING",                    // aggregateType
        event.getLendingNumber(),     // aggregateId
        "RETURNED",                   // eventType
        JsonUtils.toJson(event)       // payload JSON
    );
    outboxRepository.save(outboxEvent);  // Na MESMA transação!
}
```

#### Passo 7-8: Outbox Publisher envia para RabbitMQ
**Ficheiro:** `OutboxEventPublisher.java:31-73`
```java
@Scheduled(fixedDelay = 1000)  // A cada 1 segundo
@Transactional
public void publishPendingEvents() {
    List<OutboxEvent> pending = outboxRepository
        .findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

    for (OutboxEvent event : pending) {
        String routingKey = "lending.lending.returned";
        rabbitTemplate.convertAndSend("lms.events", routingKey, event.getPayload());

        event.setStatus(OutboxStatus.PUBLISHED);
        event.setPublishedAt(LocalDateTime.now());
        outboxRepository.save(event);
    }
}
```

---

## 3. RABBITMQ E MENSAGERIA

### Configuração
**Ficheiro:** `RabbitMQConfig.java`

```java
// Exchange
public static final String EXCHANGE_NAME = "lms.events";  // Topic Exchange

// Queue do Lending Service
public static final String QUEUE_NAME = "lending-service.events";

// Routing Keys - Publicação
public static final String ROUTING_KEY_LENDING_RETURNED = "lending.lending.returned";

// Routing Keys - Consumo
public static final String ROUTING_KEY_BOOK_ALL = "catalog.book.*";
```

### Topologia RabbitMQ

```
                    ┌────────────────────────────────────┐
                    │     Exchange: lms.events           │
                    │         (Topic)                    │
                    └───────────────┬────────────────────┘
                                    │
        ┌───────────────────────────┼───────────────────────────┐
        │                           │                           │
        ▼                           ▼                           ▼
┌───────────────────┐   ┌───────────────────┐   ┌───────────────────┐
│ lending-service   │   │ book-service      │   │ author-service    │
│ .events           │   │ .events           │   │ .events           │
├───────────────────┤   ├───────────────────┤   ├───────────────────┤
│ catalog.book.*    │   │ lending.#         │   │ catalog.genre.*   │
│ user.reader.*     │   │                   │   │                   │
└───────────────────┘   └───────────────────┘   └───────────────────┘
```

### Evento LendingReturnedEvent

```json
{
  "@type": "LendingReturned",
  "lendingNumber": "2025/1",
  "bookId": "978-0-13-468599-1",
  "readerId": 1,
  "readerNumber": "2025/100",
  "returnDate": "2025-01-15",
  "comment": "Excelente livro, recomendo!",
  "rating": 8,
  "daysOverdue": 0,
  "fineAmount": 0
}
```

### Porquê mensageria assíncrona?

1. **Desacoplamento**: Lending Service não precisa conhecer Book Service
2. **Resiliência**: Se Book Service estiver down, mensagem fica na queue
3. **Escalabilidade**: Múltiplas instâncias podem consumir em paralelo
4. **Eventual Consistency**: Aceitável para reviews (não é crítico)

### Garantias de entrega

| Mecanismo | Implementação |
|-----------|---------------|
| **Durabilidade** | Queue durável (`QueueBuilder.durable()`) |
| **Persistência** | Mensagens persistidas no RabbitMQ |
| **Outbox Pattern** | Evento só é "perdido" se BD falhar |
| **Retry** | 3 tentativas com backoff no OutboxPublisher |

### E se RabbitMQ estiver down?

1. Outbox guarda evento na BD (PENDING)
2. OutboxPublisher tenta a cada 1 segundo
3. Retry até 3 vezes
4. Se falhar 3x → marca como FAILED
5. Pode ser reprocessado manualmente

---

## 4. BASE DE DADOS E PERSISTÊNCIA

### Tabelas no Lending Service (PostgreSQL)

**Tabela: lendings**
```sql
CREATE TABLE lendings (
    pk BIGSERIAL PRIMARY KEY,
    lending_number_year INT NOT NULL,
    lending_number_sequence INT NOT NULL,
    book_id VARCHAR(20) NOT NULL,        -- ISBN
    reader_id BIGINT NOT NULL,
    reader_number VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    limit_date DATE NOT NULL,
    returned_date DATE,                   -- NULL se ativo
    comment VARCHAR(1024),                -- Review text
    rating INT CHECK (rating >= 0 AND rating <= 10),
    fine_value_per_day_cents INT,
    version BIGINT NOT NULL,              -- Optimistic locking
    created_at DATE NOT NULL,
    updated_at DATE NOT NULL,
    UNIQUE(lending_number_year, lending_number_sequence)
);
```

**Tabela: outbox_events**
```sql
CREATE TABLE outbox_events (
    id VARCHAR(36) PRIMARY KEY,           -- UUID
    aggregate_type VARCHAR(50) NOT NULL,  -- "LENDING"
    aggregate_id VARCHAR(50) NOT NULL,    -- "2025/1"
    event_type VARCHAR(50) NOT NULL,      -- "RETURNED"
    payload TEXT NOT NULL,                -- JSON do evento
    status VARCHAR(20) NOT NULL,          -- PENDING/PUBLISHED/FAILED
    retry_count INT DEFAULT 0,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL,
    published_at TIMESTAMP
);
```

### Consistência de Dados

| Mecanismo | Propósito |
|-----------|-----------|
| **@Transactional** | Lending + Outbox na mesma transação |
| **@Version** | Optimistic locking para concorrência |
| **If-Match header** | Cliente envia versão esperada |
| **Constraints** | Rating 0-10, comment max 1024 chars |

### Outbox Pattern - Garantia de Atomicidade

```
┌─────────────────────────────────────────────────────────────┐
│                    MESMA TRANSAÇÃO                          │
│                                                             │
│  1. UPDATE lendings SET returned_date=..., rating=8...      │
│  2. INSERT INTO outbox_events (payload=LendingReturned...)  │
│                                                             │
│  COMMIT ou ROLLBACK (tudo ou nada)                          │
└─────────────────────────────────────────────────────────────┘
```

### Polyglot Persistence no Projeto

| Serviço | Base de Dados | Propósito |
|---------|---------------|-----------|
| Lending Service | PostgreSQL | ACID, transações |
| Book Service | PostgreSQL | Relacional, queries |
| Author Service | PostgreSQL + MongoDB | CQRS (write + read) |
| Saga Orchestrator | Redis | Estado distribuído |
| Cache | Redis | Performance |

---

## 5. API E ENDPOINTS

### Endpoint Principal - Devolver Livro

```
POST /api/v1/lendings/{year}/{sequence}/return
```

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
If-Match: <version>
Content-Type: application/json
```

**Request Body:**
```json
{
  "comment": "Livro fantástico, aprendi muito sobre arquitetura de software!",
  "rating": 9
}
```

**Response 200 OK:**
```json
{
  "lendingNumber": "2025/1",
  "returnDate": "2025-01-15",
  "daysOverdue": 0,
  "fineAmountInCents": null,
  "review": {
    "comment": "Livro fantástico, aprendi muito sobre arquitetura de software!",
    "rating": 9
  }
}
```

### Validações

| Campo | Validação | Mensagem de Erro |
|-------|-----------|------------------|
| comment | @NotNull | "Comment cannot be null (but can be empty)" |
| comment | @Size(max=1024) | "Comment cannot exceed 1024 characters" |
| rating | @NotNull | "Rating is required" |
| rating | @Min(0) | "Rating must be at least 0" |
| rating | @Max(10) | "Rating must be at most 10" |

### Códigos HTTP

| Código | Situação |
|--------|----------|
| 200 OK | Devolução bem sucedida |
| 400 Bad Request | Validação falhou |
| 401 Unauthorized | JWT inválido/expirado |
| 404 Not Found | Lending não existe |
| 409 Conflict | Versão incorreta (optimistic locking) |
| 422 Unprocessable | Já foi devolvido |

---

## 6. PADRÕES ARQUITETURAIS APLICADOS

### CQRS no Student C (Arquitetura Explícita)

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                         CQRS EXPLÍCITO - 2 SERVIÇOS SEPARADOS                   │
├─────────────────────────────────────┬───────────────────────────────────────────┤
│      BOOK COMMAND SERVICE           │        BOOK QUERY SERVICE                 │
│         (Port 8083)                 │           (Port 8085)                     │
│         2 instâncias                │           2 instâncias                    │
├─────────────────────────────────────┼───────────────────────────────────────────┤
│ - BookEntity (write model)          │ - BookReadModel (denormalized)            │
│ - BookCommandController             │ - BookReview (reviews storage)            │
│ - POST /api/books                   │ - BookQueryController                     │
│ - PUT /api/books/{isbn}             │ - GET /api/books                          │
│ - PATCH /api/books/{isbn}           │ - GET /api/books/{isbn}                   │
│ - DELETE /api/books/{isbn}          │ - GET /api/books/{isbn}/reviews           │
│ - Outbox Pattern (publishing)       │ - Event Consumer (sync read model)        │
└─────────────────────────────────────┴───────────────────────────────────────────┘
                           │                              ▲
                           │    RabbitMQ (lms.events)     │
                           └──────────────────────────────┘
                                  BookCreatedEvent
                                  BookUpdatedEvent
                                  LendingReturnedEvent
```

**Porquê CQRS Explícito (2 serviços)?**
- **Deployment Scalability**: Requisito do projeto - múltiplas instâncias por serviço
- **Separação de responsabilidades**: Write vs Read otimizados independentemente
- **Traefik routing**: Diferentes rotas por método HTTP
- **Escalabilidade horizontal**: Query service pode ter mais instâncias se leituras forem maioria

### Outbox Pattern

**Problema resolvido:** Dual-write problem
- Precisamos atualizar BD E publicar evento
- Se publicação falhar depois do commit → inconsistência

**Solução:**
```java
@Transactional
public void returnLending(...) {
    lending.setReturned(...);
    lendingRepository.save(lending);      // 1. Atualiza lending
    outboxRepository.save(outboxEvent);   // 2. Guarda evento (mesma TX)
}
// COMMIT automático - ambos ou nenhum
```

### Database-per-Service

```
┌───────────────┐  ┌───────────────┐  ┌───────────────┐
│ lending_db    │  │ book_db       │  │ reader_db     │
├───────────────┤  ├───────────────┤  ├───────────────┤
│ lendings      │  │ books         │  │ readers       │
│ outbox_events │  │ book_reviews  │  │ users         │
└───────────────┘  └───────────────┘  └───────────────┘
      ↑                   ↑                  ↑
      │                   │                  │
┌─────┴─────┐      ┌──────┴─────┐     ┌──────┴─────┐
│ Lending   │      │ Book       │     │ Reader     │
│ Service   │      │ Service    │     │ Service    │
└───────────┘      └────────────┘     └────────────┘
```

### Táticas de Qualidade

| Atributo | Tática | Implementação |
|----------|--------|---------------|
| **Disponibilidade** | Retry | OutboxPublisher com 3 retries |
| **Disponibilidade** | Health Checks | `/actuator/health` em cada serviço |
| **Performance** | Cache | Redis para queries frequentes |
| **Escalabilidade** | Stateless | Serviços sem estado, escalam horizontalmente |
| **Consistência** | Optimistic Locking | @Version + If-Match header |

---

## 7. DOCKER E DEPLOYMENT

### Docker Compose - Arquitetura com Traefik e Múltiplas Instâncias

```yaml
# API Gateway - Traefik
traefik:
  image: traefik:v3.1
  command:
    - "--api.dashboard=true"
    - "--providers.docker=true"
    - "--providers.docker.exposedbydefault=false"
    - "--entrypoints.web.address=:80"
  ports:
    - "80:80"       # HTTP entry point (API Gateway)
    - "8090:8080"   # Traefik Dashboard

# Book Command Service (CQRS Write)
book-command-service:
  image: book-command-service:latest
  expose:
    - "8083"
  deploy:
    replicas: 2
  labels:
    - "traefik.enable=true"
    - "traefik.http.routers.book-command.rule=PathPrefix(`/api/books`) && (Method(`POST`) || Method(`PUT`) || Method(`PATCH`) || Method(`DELETE`))"
    - "traefik.http.services.book-command.loadbalancer.server.port=8083"

# Book Query Service (CQRS Read)
book-query-service:
  image: book-query-service:latest
  expose:
    - "8085"
  deploy:
    replicas: 2
  labels:
    - "traefik.enable=true"
    - "traefik.http.routers.book-query.rule=PathPrefix(`/api/books`) && Method(`GET`)"
    - "traefik.http.services.book-query.loadbalancer.server.port=8085"
```

### Comunicação no Docker

- **Rede:** `lms_network` (bridge)
- **Load Balancing:** Traefik distribui requests entre instâncias
- **Routing:** Traefik roteia por método HTTP (GET vs POST/PUT/PATCH/DELETE)
- **DNS interno:** Serviços comunicam pelo nome (e.g., `postgres`, `rabbitmq`)

### Escalar Horizontalmente com Traefik

```bash
# Já configurado com deploy.replicas: 2
docker-compose up -d

# Para escalar manualmente
docker-compose up -d --scale book-command-service=3 --scale book-query-service=4
```

**Vantagens da configuração:**
- **Traefik automático**: Detecta novas instâncias automaticamente
- **Health checks**: Traefik remove instâncias não saudáveis
- **Sem container_name fixo**: Permite múltiplas instâncias
- **Cada instância usa mesma BD**: Partilham `book_db`

---

## 8. TESTES

### Testes Unitários

**Ficheiro:** `LendingEntityTest.java`, `LendingNumberTest.java`

```java
@Test
void setReturned_withValidRating_shouldUpdateFields() {
    LendingEntity lending = createLending();
    lending.setReturned(1L, "Great book", 8);

    assertThat(lending.getComment()).isEqualTo("Great book");
    assertThat(lending.getRating()).isEqualTo(8);
    assertThat(lending.getReturnedDate()).isEqualTo(LocalDate.now());
}

@Test
void setReturned_alreadyReturned_shouldThrowException() {
    LendingEntity lending = createLending();
    lending.setReturned(1L, "Comment", 5);

    assertThrows(IllegalStateException.class,
        () -> lending.setReturned(1L, "Another", 6));
}
```

### CDC Tests (Pact)

**Ficheiro:** `LendingReturnedEventProviderPactTest.java`

```java
@Provider("lending-service")
@Consumer("book-service")
@PactFolder("pacts")
class LendingReturnedEventProviderPactTest {

    @PactVerifyProvider("a lending returned event with review")
    MessageAndMetadata lendingReturnedWithReview() {
        LendingReturnedEvent event = new LendingReturnedEvent();
        event.setLendingNumber("2025/1");
        event.setBookId("978-0-13-468599-1");
        event.setComment("Great book!");
        event.setRating(8);
        // ...
        return new MessageAndMetadata(json.getBytes(), metadata);
    }
}
```

**Cenários testados:**
1. Evento com review completo (comment + rating)
2. Evento sem review (comment e rating null)
3. Evento com multa (daysOverdue > 0)

### Mutation Testing (PIT)

```xml
<plugin>
    <groupId>org.pitest</groupId>
    <artifactId>pitest-maven</artifactId>
    <version>1.15.3</version>
    <configuration>
        <mutationThreshold>80</mutationThreshold>
        <targetClasses>
            <param>pt.psoft.lending.model.*</param>
            <param>pt.psoft.lending.services.*</param>
        </targetClasses>
    </configuration>
</plugin>
```

---

## 9. PERGUNTAS DIFÍCEIS DO PROFESSOR

### P1: "E se o RabbitMQ cair?"

**Resposta:**
> "O Outbox Pattern protege-nos exatamente contra esse cenário. Quando o RabbitMQ está indisponível:
> 1. O evento fica guardado na tabela `outbox_events` com status PENDING
> 2. A transação de devolução completa com sucesso (o utilizador recebe HTTP 200)
> 3. O `OutboxEventPublisher` tenta publicar a cada segundo
> 4. Quando o RabbitMQ voltar, os eventos pendentes são enviados
> 5. Após 3 falhas, marca como FAILED para investigação manual
>
> O importante é que a operação de negócio (devolução) não falha por causa do broker de mensagens."

### P2: "Como garantiste idempotência?"

**Resposta:**
> "Implementei idempotência a vários níveis:
>
> 1. **Optimistic Locking**: O header `If-Match` com a versão esperada garante que não processamos a mesma devolução duas vezes. Se a versão não bater, dá erro 409 Conflict.
>
> 2. **Estado do Lending**: Antes de processar, verifico `if (!lending.isActive())` - se já foi devolvido, lança `BusinessException`.
>
> 3. **No Book Service**: O `lendingNumber` serve como chave de idempotência - se já existe um `BookReview` com esse `lendingNumber`, não cria duplicado.
>
> 4. **Outbox Events**: Cada evento tem um ID único (UUID) que pode ser usado para deduplicação no consumer."

### P3: "Porquê CQRS neste caso?"

**Resposta:**
> "Apliquei CQRS porque o professor sugeriu que 'reviews are conceptually connected to lendings' mas 'should be in BookQuery so users can see reviews when viewing a book'. Ou seja:
>
> - **Command Side (Lending)**: A review faz parte da ação de devolver. O leitor só pode avaliar um livro que leu. O rating e comment são guardados no `LendingEntity`.
>
> - **Query Side (Book)**: Quando alguém consulta um livro, quer ver as reviews. Faz mais sentido ter os dados desnormalizados no `BookReview` do que fazer JOIN entre serviços.
>
> **Alternativas que considerei:**
> - Review Service separado: Rejeitado - complexidade desnecessária
> - Só no Lending: Rejeitado - má performance para queries de livros
> - Só no Book: Rejeitado - viola bounded context (review é parte de devolução)"

### P4: "Saga Orchestration vs Choreography - qual usaste e porquê?"

**Resposta:**
> "No Student C (devolução com review), usei **Choreography** - o Lending Service publica `LendingReturnedEvent` e o Book Service subscreve e reage. É mais simples porque:
> - Só 2 serviços envolvidos
> - Fluxo unidirecional (Lending → Book)
> - Sem necessidade de compensação (criar review é idempotente)
>
> Já para o Student A (criar Book+Author+Genre), o projeto usa **Orchestration** com o `saga-orchestrator`:
> - 3+ serviços envolvidos
> - Precisa de compensação (se criar Book falha, apagar Author e Genre)
> - Fluxo complexo com rollback
>
> **Trade-offs:**
> - Choreography: Mais simples, menos coupling, mas difícil de visualizar fluxo completo
> - Orchestration: Centralizado, fácil de seguir, mas single point of failure"

### P5: "Como testaste consistência eventual?"

**Resposta:**
> "Testei a vários níveis:
>
> 1. **CDC Tests (Pact)**: Validam que o formato do `LendingReturnedEvent` está correto e que o Book Service consegue consumir. Os testes estão em `LendingReturnedEventProviderPactTest.java`.
>
> 2. **Integration Tests**: Com TestContainers, subo PostgreSQL e RabbitMQ reais e testo o fluxo completo.
>
> 3. **Cenários de falha**:
>    - Simulo RabbitMQ down e verifico que evento fica no Outbox
>    - Simulo consumer lento e verifico que mensagens não se perdem
>
> 4. **Observabilidade**: Em produção, monitorizamos:
>    - Tamanho da queue no RabbitMQ
>    - Eventos PENDING/FAILED no Outbox
>    - Latência entre publicação e consumo"

### P6: "Qual é o SLA de consistência?"

**Resposta:**
> "Com a configuração atual:
> - **Outbox polling**: 1 segundo
> - **RabbitMQ delivery**: ~10-50ms
> - **Consumer processing**: ~100ms
>
> **SLA típico**: Review visível no Book Service em < 2 segundos após devolução.
>
> Em caso de falha do RabbitMQ:
> - Retry a cada segundo, até 3 tentativas
> - Se RabbitMQ voltar em 3 segundos → eventual consistency mantida
> - Se passar 3 segundos → evento marcado FAILED, requer intervenção
>
> Para o caso de uso de reviews, este SLA é aceitável - não é crítico que a review apareça instantaneamente."

### P7: "E se o Book Service consumir o evento duas vezes?"

**Resposta:**
> "O consumer deve ser idempotente. Implementação:
>
> ```java
> public void handleLendingReturned(LendingReturnedEvent event) {
>     // Verificar se já existe review com este lendingNumber
>     if (reviewRepository.existsByLendingNumber(event.getLendingNumber())) {
>         log.warn(\"Duplicate event for lending: {}\", event.getLendingNumber());
>         return; // Idempotente - ignora duplicado
>     }
>
>     BookReview review = new BookReview(event);
>     reviewRepository.save(review);
> }
> ```
>
> O `lendingNumber` é único por natureza (ano/sequência), então serve como chave natural para idempotência."

### P8: "Porque não usaste Event Sourcing completo?"

**Resposta:**
> "Event Sourcing tem benefícios (audit trail, replay, temporal queries) mas também custos:
> - Complexidade de implementação
> - Event store adicional
> - Eventual consistency obrigatória em todo o lado
>
> Para o LMS, optei por um **híbrido**:
> - **State-based**: O estado atual está na BD relacional (simples de consultar)
> - **Event-driven**: Publico eventos para sincronização entre serviços
> - **Outbox**: Garante que eventos são publicados de forma confiável
>
> Se precisasse de audit trail completo ou temporal queries, consideraria Event Sourcing, mas para este caso os domain events são suficientes."

---

## RESUMO PARA A DEFESA

### Pontos Fortes a Destacar

1. **CQRS Explícito** - Book Command Service (writes) + Book Query Service (reads) como serviços separados
2. **Deployment Scalability** - 2 instâncias por serviço com Traefik load balancer
3. **Outbox Pattern** - Garante consistência entre BD e eventos
4. **CDC Tests** - Contrato verificado entre Lending e Book Query services
5. **Traefik Routing** - Roteia por método HTTP (GET → Query, POST/PUT/PATCH/DELETE → Command)
6. **Database Sharing** - Ambos book services usam mesma `book_db` (instâncias partilham BD)
7. **Documentação** - ADRs, diagramas C4, PlantUML

### Critérios de Avaliação Cumpridos

| Critério | Status | Evidência |
|----------|--------|-----------|
| ≥3 microservices colaborando | ✅ | 7 serviços: genre, author, book-cmd, book-qry, lending, reader, saga |
| Multiple instances per service | ✅ | deploy.replicas: 2 + Traefik |
| CQRS | ✅ | Book Command + Book Query Services separados |
| Database-per-Service | ✅ | genre_db, author_db, book_db, lending_db, reader_db |
| Polyglot Persistence | ✅ | PostgreSQL + MongoDB + Redis |
| Messaging (RabbitMQ) | ✅ | Topic exchange, domain events |
| Outbox Pattern | ✅ | OutboxEventPublisher em cada serviço |
| Saga Pattern | ✅ | saga-orchestrator para Student A |
| Domain Events | ✅ | BookCreated, LendingReturned, etc. |

### Arquitetura Final

```
                    ┌─────────────────────┐
                    │       Traefik       │
                    │   (Load Balancer)   │
                    └──────────┬──────────┘
                               │
       ┌───────────────────────┼───────────────────────┐
       │                       │                       │
       ▼                       ▼                       ▼
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│ Book Command │      │ Book Query   │      │ Lending Svc  │
│ Service x2   │      │ Service x2   │      │     x2       │
├──────────────┤      ├──────────────┤      ├──────────────┤
│ POST/PUT/... │      │ GET          │      │ Returns      │
│ Outbox       │      │ Reviews      │      │ Reviews      │
└──────┬───────┘      └──────┬───────┘      └──────┬───────┘
       │                     ▲                     │
       │   RabbitMQ Events   │                     │
       └─────────────────────┴─────────────────────┘
```

### Frase de Fecho

> "O projeto implementa CQRS de forma explícita com Book Command Service e Book Query Service separados, cada um com 2 instâncias. O Traefik funciona como API Gateway e load balancer, roteando requests por método HTTP. A comunicação assíncrona via RabbitMQ com Outbox Pattern garante consistência eventual. O Student C (devolução com review) publica LendingReturnedEvent que é consumido pelo Book Query Service para criar BookReview e atualizar estatísticas. Todos os padrões do critério de avaliação estão implementados: CQRS, Database-per-Service, Polyglot Persistence, Messaging, Outbox, Saga e Domain Events."
