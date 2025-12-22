# System As-Is

Este documento descreve a arquitetura e o estado atual da aplicação Spring Boot para gestão de uma biblioteca.

## 1. Resumo

A aplicação é uma API RESTful desenvolvida com **Spring Boot** que serve como backend para um sistema de gestão de biblioteca. Permite a gestão de autores, livros, leitores e empréstimos, incluindo funcionalidades de segurança e documentação de API.

## 2. Tecnologias Principais

- **Framework:** Spring Boot 3.2.5
- **Linguagem:** Java 17
- **Build Tool:** Apache Maven
- **Base de Dados:** H2 (configurada para correr em modo servidor TCP, com os dados no ficheiro `~/psoft-g1`)
- **Persistência:** Spring Data JPA / Hibernate
- **API:** Spring Web (REST)
- **Segurança:** Spring Security com autenticação baseada em JWT (usando chaves RSA).
- **Documentação da API:** SpringDoc (OpenAPI 3) com Swagger UI.
- **Outras Bibliotecas:** Lombok, MapStruct, OWASP Java HTML Sanitizer.

## 3. Arquitetura e Funcionalidades

A aplicação segue uma arquitetura em camadas, com uma clara separação de responsabilidades.

- **Domínio:** O modelo de negócio é centrado em agregados como `Author`, `Book`, `Reader`, e `Lending`, o que sugere uma abordagem de Domain-Driven Design (DDD).
- **API REST:** Expõe endpoints para operações CRUD (Create, Read, Update, Delete) sobre os principais recursos do sistema. A documentação da API está disponível via Swagger UI.
- **Segurança:** O acesso à API é protegido. A autenticação é feita através de tokens JWT, garantindo que apenas utilizadores autorizados possam realizar operações.
- **Gestão de Ficheiros:** O sistema suporta o upload de ficheiros (ex: fotos de perfil, capas de livros), que são armazenados no diretório `uploads-psoft-g1/`.
- **Configuração:** A configuração principal (`application.properties`) define o perfil `bootstrap` como ativo e configura a ligação à base de dados, segurança e comportamento da aplicação. O schema da base de dados é atualizado automaticamente (`ddl-auto: update`).

## 4. Como Executar

1.  **Pré-requisito:** Ter o Java 17 e o Maven instalados.
2.  **Execução:** Utilizar o Maven Wrapper na raiz do projeto:
    ```bash
    ./mvnw spring-boot:run
    ```
3.  **Acesso à API:**
    - **API Base URL:** `http://localhost:8080`
    - **Swagger UI (Documentação):** `http://localhost:8080/swagger-ui`
    - **H2 Console (Base de Dados):** `http://localhost:8080/h2-console`
