# Gerador de Id's

---

## 1. Design Objective
Implementar um mecanismo de base de dados para gerar identificadores únicos de entidades do sistema.
O objetivo é garantir unicidade e compatibilidade com o modelo de domínio e com o serviço REST existente (LMS).
---

## 2. Constraints


- O requisito deve ser implementado com base no serviço REST já existente (LMS);
- Garantir que é permitido alterar o Formato de Geração de Id;
- Formato em base65 de um número gerado aleatoriamente com 6 digitos;
- Codificação que se baseia numa timestamp, com 6 digitos aleatórios de sufixo;

---

## 3. Concerns

- **Validação de Dados**: Garantir que os dados de entrada (DTO) são válidos.
- **Desempenho**: A geração deve ser rápida para não impactar a performance da criação de entidades.
- **Escalabilidade**: O sistema deve permitir a adição de novos formatos de ID com mínimo impacto.
- **Manutenção**: A lógica de geração deve ser centralizada e isolada.
- **Concorrência**: Evitar colisões de IDs quando múltiplos pedidos ocorrem simultaneamente.

---

## 4. Quality Attribute Scenario

| Element | Statement                                                                                |
|---|------------------------------------------------------------------------------------------|
| Stimulus | Um serviço de aplicação solicita a criação de uma nova entidade e requer um ID único.    |
| Stimulus source | Pedido proveniente de um utilizador autenticado via API REST                             |
| Environment | Sistema em operação normal, com base de dados e serviço de geração de IDs disponíveis.   |
| Artifact | Camada de aplicação, domínio e infraestrutura.                                           |
| Response | O sistema gera um ID único, no formato definido, e o devolve ao serviço que o solicitou. |
| Response measure | O ID é gerado em menos de 10ms e a probabilidade de colisão é inferior a 0.0001%.      |

---

## 5. System-to-be

O sistema será composto por um serviço de geração de ID no módulo `idmanagement`, integrado na arquitetura modular do sistema. Este serviço será responsável por gerar, validar e devolver o ID.

### Táticas aplicadas

- **Encapsular**: A lógica de geração de ID está contida num módulo específico.
- **Abstract Common Services**: O serviço de ID é um serviço comum que pode ser usado por outros módulos.
- **Restringir Dependências**: O módulo de ID não depende de módulos de negócio.
- **Defer Binding**: A escolha do algoritmo de geração de ID é configurável e pode ser alterada em tempo de execução ou deployment.

### Padrões utilizados

- **Strategy Pattern**: Cada forma de gerar ID é uma estratégia diferente, permitindo trocar o algoritmo dinamicamente.
- **Factory Pattern**: Usada para criar o gerador de ID apropriado com base na configuração do sistema.

### Arquitetura de referência aplicada

- **Modular Monolith**: A funcionalidade é isolada no módulo `idmanagement`.
- **Onion Architecture**: As dependências fluem para o centro; a lógica de negócio não depende de frameworks externos.

### Alternativas de Arquiteturas

**Clean Architecture**: Uma alternativa que também garantiria uma forte separação de responsabilidades, isolando a lógica de geração de ID das dependências externas, de forma semelhante à Onion Architecture.

---

## 6. Technical Memo

- **Problem**:
    - É necessário um mecanismo consistente, configurável e desacoplado para gerar identificadores únicos para as entidades do domínio.

- **Resumo da Solução**:
    - Isolar a lógica de geração de IDs num módulo (`idmanagement`) que implementa os padrões Strategy e Factory. Uma fábrica seleciona a estratégia de geração (ex: timestamp, aleatória) com base num ficheiro de configuração.

- **Fatores**:
    - Necessidade de IDs únicos e não sequenciais.
    - Requisito de poder alterar o formato de geração de IDs sem impactar o resto da aplicação.
    - Facilidade de manutenção e teste da lógica de geração.

- **Solução**:
    - Ao usar **Encapsular** num módulo dedicado, garantimos que a responsabilidade da geração de IDs está num só sítio. O **Strategy Pattern** permite que cada algoritmo de geração seja uma classe separada, e o **Factory Pattern** escolhe qual usar, aplicando **Defer Binding**.

- **Motivação**:
    - Esta solução centraliza a lógica, facilita a sua substituição e evolução, e desacopla a geração de IDs do código de negócio que a consome, aumentando a manutenibilidade e testabilidade.

- **Alternativas**:
    - **IDs auto-incrementados da BD**: Rejeitado por serem previsíveis e exporem métricas de negócio.
    - **UUIDs standard**: Rejeitado para permitir um maior controlo sobre o formato e comprimento do ID.

- **Questões pendentes**:
    - A configuração da estratégia de geração de
    - ID será gerida via ficheiro de propriedades ou por uma tabela na base de dados? (Decisão: Ficheiro de propriedades para simplicidade).