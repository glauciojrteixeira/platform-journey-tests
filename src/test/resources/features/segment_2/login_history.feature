@not_implemented @segment_2 @j2.4 @audit @history @medium @e2e
Feature: Histórico de Logins e Acessos
  Como um arrematador profissional
  Eu quero consultar meu histórico de acessos
  Para monitorar a segurança da minha conta

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando
    And que estou autenticado na plataforma

  Scenario: Consulta de histórico de logins bem-sucedida
    Given que tenho histórico de logins anteriores
    When eu consulto meu histórico de acessos
    Then o histórico deve retornar lista de logins com:
      | campo              | descrição                    |
      | data_hora          | Timestamp do login            |
      | ip_origem          | IP de origem                 |
      | dispositivo        | Dispositivo/navegador        |
      | status             | Sucesso ou falha             |
      | metodo             | Credencial/social/MFA        |
    And os logins devem estar ordenados por data (mais recente primeiro)

  Scenario: Filtro de histórico por data
    Given que tenho histórico de logins anteriores
    When eu consulto histórico filtrando por data:
      | data_inicio | 2025-01-01 |
      | data_fim    | 2025-01-31 |
    Then apenas logins no período devem ser retornados

  Scenario: Filtro de histórico por status
    Given que tenho histórico de logins anteriores
    When eu consulto histórico filtrando por status "falha"
    Then apenas logins com falha devem ser retornados
    And isso permite identificar tentativas suspeitas

