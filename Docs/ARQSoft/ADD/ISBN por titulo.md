# ISBN por Título

---

## 1. Design Objective
Implementar a funcionalidade de obter o ISBN de um livro a partir do seu título. O objetivo é permitir a integração com serviços externos para enriquecer os dados dos livros ou validar informações.

---

## 2. Constraints
- A pesquisa deve ser eficiente e retornar resultados relevantes.
- Deve ser capaz de lidar com títulos parciais ou variações.
- Dependência de um serviço externo (API de livros).

---

## 3. Concerns
- **Disponibilidade do Serviço Externo**: O que acontece se a API externa estiver indisponível?
- **Limites de Rate**: Como gerir os limites de requisições da API externa?
- **Qualidade dos Dados**: A precisão dos resultados retornados pela API externa.
- **Performance**: O tempo de resposta da pesquisa.

---

## 4. Quality Attribute Scenario

| Element | Statement                                                                                |
|---|------------------------------------------------------------------------------------------|
| Stimulus | Um utilizador solicita o ISBN de um livro através do seu título.                         |
| Stimulus source | Serviço de aplicação (ex: UI ou outra API interna).                                      |
| Environment | Sistema em operação normal, com conectividade à internet para aceder a APIs externas.    |
| Artifact | Módulo de gestão de livros, serviço de integração com APIs externas.                     |
| Response | O sistema retorna o ISBN correspondente ao título, ou uma indicação de que não foi encontrado. |
| Response measure | O ISBN é retornado em menos de 500ms para 95% das requisições.                   |

---

## 5. System-to-be

A funcionalidade será implementada num serviço dedicado dentro do módulo de gestão de livros, que fará a ponte com uma API externa (ex: Google Books API, Open Library API).

### Táticas aplicadas
- **Encapsular**: A lógica de integração com a API externa será encapsulada num serviço.
- **Abstract Common Services**: O serviço de pesquisa de ISBN pode ser reutilizado por diferentes partes da aplicação.
- **Monitorar**: Monitorização da disponibilidade e performance da API externa.

### Padrões utilizados
- **Adapter Pattern**: Para adaptar a interface da API externa à interface interna do sistema.
- **Circuit Breaker Pattern**: Para lidar com falhas temporárias da API externa.

### Arquitetura de referência aplicada
- **Modular Monolith**: A funcionalidade é isolada no módulo de livros.
- **Onion Architecture**: A camada de infraestrutura lida com a comunicação externa.

### Alternativas de Arquiteturas
- **Microserviços**: Um microserviço dedicado para integração com APIs de livros, para maior escalabilidade e isolamento de falhas.

---

## 6. Technical Memo

- **Problem**:
    - Necessidade de obter ISBNs de livros de forma programática, usando apenas o título, para enriquecimento de dados ou validação.

- **Resumo da Solução**:
    - Implementar um serviço de integração que utiliza uma API externa de livros. Este serviço usará o `Adapter Pattern` para normalizar a comunicação e o `Circuit Breaker Pattern` para resiliência.

- **Fatores**:
    - A disponibilidade de APIs externas robustas.
    - A necessidade de manter os dados dos livros atualizados e consistentes.
    - A complexidade de gerir diferentes formatos de dados de várias fontes.

- **Solução**:
    - Um serviço `IsbnLookupService` no módulo de livros, que utiliza um `ExternalBookApiAdapter`. Este adaptador traduz as requisições internas para o formato da API externa e vice-versa. O `Circuit Breaker` previne sobrecargas em caso de falha da API externa.

- **Motivação**:
    - Centraliza a lógica de integração, facilita a troca de APIs externas no futuro e melhora a resiliência do sistema a falhas externas.

- **Alternativas**:
    - **Manutenção manual dos ISBNs**: Rejeitado por ser ineficiente e propenso a erros.
    - **Web scraping**: Rejeitado por ser frágil e violar termos de serviço.

- **Questões pendentes**:
    - Qual API externa será utilizada (ex: Google Books, Open Library)?
    - Como será feita a autenticação/autorização com a API externa?
    - Como lidar com múltiplos resultados para o mesmo título?