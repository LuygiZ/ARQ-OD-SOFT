# LMS - Library Management System (Microservices)

Sistema de Gestão de Biblioteca reengenhado de monólito para arquitetura de microserviços.

## 🏗️ Arquitetura

### Microserviços

| Serviço | Porta | Descrição | Database |
|---------|-------|-----------|----------|
| **eureka** | 8761 | Service Registry | - |
| **genre-service** | 8081 | Gestão de Géneros | PostgreSQL (genre_db) |
| **author-service** | 8082 | Gestão de Autores | PostgreSQL (author_db) |
| **book-service** | 8083 | Gestão de Livros (CQRS) | PostgreSQL (book_db) + Replicas |
| **saga-orchestrator** | 8084 | Process API (Saga Pattern) | Redis |
| **reader-service** | 8085 | Gestão de Leitores | PostgreSQL (reader_db) |
| **lending-service** | 8086 | Gestão de Empréstimos | PostgreSQL (lending_db) |
| **user-service** | 8087 | Autenticação & Utilizadores | PostgreSQL (user_db) |

### Infraestrutura

- **PostgreSQL**: Database-per-Service + Read Replicas (CQRS)
- **Redis**: L2 Cache + Saga State
- **RabbitMQ**: Message Broker (Events & Commands)
- **Traefik**: API Gateway
- **Prometheus + Grafana**: Monitoring

## 🚀 Quick Start

### Pré-requisitos

- Docker & Docker Compose
- Java 21
- Maven 3.9+

### Build & Deploy
```bash
# 1. Build shared-kernel
cd shared-kernel
mvn clean install

# 2. Build todos os microserviços
cd ..
mvn clean package

# 3. Criar network
docker network create lms_network
```

### Acessos

- **Eureka Dashboard**: http://localhost:8761
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)
- **Traefik Dashboard**: http://localhost:8080
- **API Gateway**: http://localhost/api/...

## 📚 Padrões Implementados

- ✅ Strangler Fig (migração progressiva)
- ✅ Database-per-Service
- ✅ Polyglot Persistence
- ✅ Saga Pattern (Orchestration)
- ✅ Outbox Pattern
- ✅ Domain Events
- ✅ CQRS (Book Service)
- ✅ RabbitMQ Message Broker

## 📖 Documentação

Ver `Docs/ADD.pdf` para arquitetura completa.