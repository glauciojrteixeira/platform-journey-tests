@not_implemented @segment_4 @j4.7 @b2b @audit @enterprise @medium @e2e @may_require_auth
Feature: Auditoria Completa de Acessos
  Como admin técnico de uma plataforma B2B
  Eu quero consultar auditoria completa
  Para compliance e investigação de incidentes

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando
    And que sou admin técnico de uma plataforma
    And que estou autenticado na plataforma

  Scenario: Consulta de auditoria de tokens
    Given que tenho histórico de tokens emitidos/revogados
    When eu consulto auditoria de tokens de um usuário
    Then a auditoria deve retornar lista completa:
      | campo       | descrição                    |
      | token_id    | ID único do token            |
      | emitido_em  | Quando foi emitido           |
      | expira_em   | Quando expira                |
      | revogado_em | Quando foi revogado (se aplicável)|
      | claims      | Claims incluídos             |
    And os tokens devem estar ordenados por data

  Scenario: Consulta de auditoria de acessos
    Given que tenho histórico de acessos
    When eu consulto auditoria de acessos de um usuário
    Then a auditoria deve retornar lista completa:
      | campo         | descrição                    |
      | tipo          | LOGIN, API_CALL, RESOURCE_ACCESS|
      | recurso       | Recurso acessado              |
      | ip_origem     | IP de origem                  |
      | timestamp     | Quando ocorreu               |
      | status        | Sucesso ou falha              |
    And os acessos devem estar ordenados por data

  Scenario: Consulta de auditoria de API keys
    Given que tenho histórico de uso de API keys
    When eu consulto auditoria de API keys de um usuário
    Then a auditoria deve retornar histórico completo:
      | campo         | descrição                    |
      | api_key_id    | ID da API key                |
      | usado_em      | Quando foi usada             |
      | recurso       | Recurso acessado             |
      | ip_origem     | IP de origem                 |
      | status        | Sucesso ou falha             |

  Scenario: Filtros e exportação de auditoria
    Given que tenho histórico de auditoria
    When eu aplico filtros:
      | campo      | valor                    |
      | data_inicio| 2025-01-01              |
      | data_fim   | 2025-01-31              |
      | tipo       | LOGIN                   |
      | status     | sucesso                 |
    Then apenas registros que atendem filtros devem ser retornados
    When eu exporto auditoria para relatório
    Then o relatório deve ser gerado em formato adequado para compliance

