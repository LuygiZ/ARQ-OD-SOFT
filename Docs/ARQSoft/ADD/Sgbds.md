# Escolha e Estratégia de SGBDs (Sistemas de Gestão de Bases de Dados)

---

## 1. Design Objective
Definir a estratégia de utilização de Sistemas de Gestão de Bases de Dados (SGBDs) no projeto, permitindo flexibilidade e adaptabilidade às necessidades de diferentes módulos ou tipos de dados. O objetivo é suportar tanto bases de dados relacionais (SQL) quanto não-relacionais (NoSQL).

---

## 2. Constraints
- Necessidade de persistência de dados para diversas entidades do domínio.
- Requisito de flexibilidade para escolher o SGBD mais adequado para cada caso de uso.
- Compatibilidade com a arquitetura modular e a Onion Architecture do sistema.

---

## 3. Concerns
- **Complexidade de Gestão**: Gerir múltiplos tipos de SGBDs pode aumentar a complexidade de desenvolvimento e manutenção.
- **Consistência de Dados**: Garantir a consistência dos dados em diferentes SGBDs, especialmente em cenários de integração.
- **Curva de Aprendizagem**: A equipa pode precisar de conhecimento em diferentes tecnologias de persistência.
- **Performance**: Otimizar o acesso a dados para diferentes SGBDs.

---

## 4. Quality Attribute Scenario

| Element | Statement                                                                                |
|---|------------------------------------------------------------------------------------------|
| Stimulus | Um novo módulo ou funcionalidade requer persistência de dados com características específicas (ex: alta escalabilidade, flexibilidade de esquema). |
| Stimulus source | Equipa de desenvolvimento.                                                               |
| Environment | Sistema em fase de desenvolvimento ou evolução.                                          |
| Artifact | Camada de infraestrutura (módulos de persistência).                                      |
| Response | O sistema permite a integração de um novo SGBD (SQL ou NoSQL) com impacto mínimo na arquitetura existente. |
| Response measure | A integração de um novo SGBD é concluída em menos de 2 dias de trabalho.             |

---

## 5. System-to-be

O sistema será projetado para ser agnóstico ao SGBD a nível da camada de domínio, utilizando o padrão Repository para abstrair os detalhes de persistência. A camada de infraestrutura conterá implementações específicas para diferentes SGBDs (ex: JPA para SQL, Spring Data MongoDB para NoSQL).

### Táticas aplicadas
- **Abstract Common Services**: O `Repository Pattern` abstrai a persistência, tornando o domínio independente do SGBD.
- **Encapsular**: A lógica de acesso a cada SGBD é encapsulada nas suas respetivas implementações de repositório.
- **Defer Binding**: A escolha do SGBD para um determinado módulo pode ser configurada em tempo de deployment.

### Padrões utilizados
- **Repository Pattern**: Abstrai a fonte de dados, permitindo que o domínio trabalhe com coleções de objetos sem se preocupar com a persistência subjacente.
- **Dependency Injection**: Para injetar a implementação correta do repositório (SQL ou NoSQL) em tempo de execução.

### Arquitetura de referência aplicada
- **Onion Architecture**: A camada de domínio é independente da camada de infraestrutura (onde residem os SGBDs).
- **Modular Monolith**: Diferentes módulos podem usar diferentes SGBDs, mas dentro de uma aplicação monolítica.

### Alternativas de Arquiteturas
- **Single SGBD (ex: apenas SQL)**: Rejeitado por limitar a flexibilidade e não otimizar a persistência para todos os tipos de dados e casos de uso.

---

## 6. Technical Memo

- **Problem**:
    - A necessidade de suportar diferentes requisitos de persistência de dados, que podem ser melhor atendidos por SGBDs relacionais ou não-relacionais, dependendo do contexto.

- **Resumo da Solução**:
    - Adotar uma estratégia de persistência poliglota, onde o domínio é agnóstico ao SGBD através do `Repository Pattern`. A camada de infraestrutura implementa repositórios específicos para SQL (ex: PostgreSQL) e NoSQL (ex: MongoDB), configuráveis via `application.properties`.

- **Fatores**:
    - Flexibilidade para escolher a melhor ferramenta para o trabalho (SQL para dados estruturados, NoSQL para dados flexíveis/escaláveis).
    - Otimização de performance para diferentes tipos de acesso a dados.
    - Preparação para futuras necessidades de escalabilidade e diversidade de dados.

- **Solução**:
    - Implementar interfaces de repositório no domínio e múltiplas implementações na infraestrutura. A injeção de dependências garante que a implementação correta seja usada. Ex: `BookRepository` com `JpaBookRepository` e `MongoBookRepository`.

- **Motivação**:
    - Esta abordagem oferece alta flexibilidade, permite otimizar a persistência para diferentes cenários e facilita a evolução do sistema sem estar preso a uma única tecnologia de base de dados.

- **Alternativas**:
    - **Usar apenas um SGBD relacional**: Rejeitado porque nem todos os dados se encaixam bem num modelo relacional e pode levar a soluções subótimas para dados flexíveis ou de alta volume.
    - **Usar apenas um SGBD NoSQL**: Rejeitado por não ser ideal para dados com relações complexas e transações ACID rigorosas.

- **Questões pendentes**: Será necessário definir uma estratégia para lidar com transações distribuídas, se aplicável, entre diferentes SGBDs.