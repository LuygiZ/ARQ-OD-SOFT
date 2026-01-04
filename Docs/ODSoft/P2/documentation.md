# Documentação de ODSOFT - Projeto 2

Este documento foca-se na organização, automação e processos de DevOps implementados para suportar a arquitetura distribuída do Projeto 2.

## Análise à Pipeline (CI/CD)

A pipeline foi desenhada para suportar deployabilidade independente e integrações contínuas.

### Stages da Pipeline
1.  **Checkout**: Garante que cada microserviço obtém a sua versão mais recente.
2.  **Clean and Compile**: Compilação isolada de cada módulo Maven.
3.  **Unit Tests (Maven Surefire)**: Validação lógica isolada.
4.  **Contract Tests (Pact)**: Garante que as alterações no `reader-service` não quebram o contrato com o `user-service` antes do deployment.
5.  **Mutation Tests (PIT)**: Avalia a robustez dos testes (ver Análise Crítica abaixo).
6.  **Package & Docker Build**: Criação de imagens Docker individuais para cada serviço.
7.  **Deploy (Kubernetes)**: Implantação nos ambientes de Dev/Staging.

### Análise Crítica da Pipeline (Mutation Testing)

#### Primeiro teste de mutação (Baseline)
![BaseProjectMutationTestsResult.png](../../../ODSoft/P1/assets/BaseProjectMutationTestsResult.png)
*   **Cobertura**: 22% (Baixa). Mutações sobreviventes indicam testes frágeis.

#### Segundo teste de mutação (Projeto Atual)
![ARQSOFTProjectMutationTestsResults_1.png](../../../ODSoft/P1/assets/ProjectMutationTestsResults_1.png)
*   **Melhoria**: Aumento para 57% de mutações mortas e 76% de cobertura de linhas.
*   **Impacto**: Maior confiança na refatorização para microserviços, garantindo que a lógica de domínio distribuída permanece correta.

---

## Cumprimento dos Requisitos ODSOFT (Projeto 2)

Esta secção detalha como a pipeline e infraestrutura respondem aos objetivos de **independência, deployabilidade e automação**.

### Independência e Deployability
*   **Contexto**: "The LMS project will evolve... composed by several applications."
*   **Solução**:
    *   Cada serviço (`user`, `reader`) possui o seu próprio **Dockerfile**.
    *   A pipeline Jenkins suporta builds seletivos, permitindo que alterações num único serviço desencadeiem apenas o deploy desse serviço (Independent Deployability).

### Ambientes e Zero Downtime
*   **Ambientes (Dev/Staging/Prod)**:
    *   A pipeline suporta parametrização (`Environment` choice: docker vs kubernetes), permitindo promoção de código entre ambientes.
*   **Zero Downtime**:
    *   Uso de estratégia **RollingUpdate** no Kubernetes.
    *   Sondas de **Readiness** garantem que o tráfego só é enviado para novas versões saudáveis.
*   **Auto-Rollback**:
    *   Se as sondas de **Liveness** falharem após um deploy, o Kubernetes reverte automaticamente para a versão anterior (ReplicaSet estável).

### Funcionalidades DevOps (Critérios Funcionais)
*   **3+ Serviços**: O sistema, anteriormente monolítico, agora executa como uma frota de serviços: `user-service`, `reader-service`, e `book-service`, orquestrados em K8s.
*   **Monitorização/Gate**:
    *   Implementação de "Manual Gates" no Jenkins para aprovação de deploy em Produção.
