@not_implemented @segment_4 @j4.4 @b2b @api_keys @enterprise @high @e2e @may_require_auth
Feature: Geração e Gestão de API Keys
  Como um usuário técnico de uma plataforma parceira
  Eu quero gerar e gerenciar API keys
  Para integrações programáticas

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando
    And que sou usuário técnico (role TECHNICAL)
    And que estou autenticado na plataforma

  Scenario: Geração de API key bem-sucedida
    Given que acesso área de API Keys
    When eu solicito geração de API key:
      | campo      | valor                    |
      | scope      | read,write               |
      | expires_at | 2026-12-31T23:59:59Z    |
    Then a API key deve ser gerada
    And o formato deve ser "pk_live_xxxxxxxxxxxxx" ou similar
    And o hash SHA-256 deve ser calculado e armazenado
    And o prefixo (8 caracteres) deve ser armazenado
    And o valor completo deve ser retornado apenas uma vez
    And o evento "api-key.generated" deve ser publicado

  Scenario: Geração de API key falha se não for role TECHNICAL
    Given que sou usuário com role "OPERATOR"
    When eu tento gerar API key
    Then a operação deve falhar com status 403
    And o erro deve indicar que role TECHNICAL é necessário

  Scenario: Listagem de API keys ativas
    Given que tenho múltiplas API keys geradas
    When eu consulto lista de API keys
    Then a lista deve retornar informações de cada key:
      | campo         | descrição                    |
      | prefixo       | 8 primeiros caracteres      |
      | scope         | Permissões                  |
      | ultimo_uso    | Timestamp do último uso     |
      | expira_em     | Data de expiração           |
    And o valor completo não deve ser retornado (segurança)

  Scenario: Validação de API key bem-sucedida
    Given que tenho uma API key válida
    When eu uso a API key em uma requisição
    Then a validação deve ser bem-sucedida
    And o acesso deve ser permitido conforme scope
    And o uso deve ser registrado para auditoria

  Scenario: Validação de API key falha com key revogada
    Given que tenho uma API key que foi revogada
    When eu tento usar a API key revogada
    Then a validação deve falhar com status 401
    And o erro deve indicar que key foi revogada

  Scenario: Revogação de API key
    Given que tenho uma API key ativa
    When eu revogo a API key
    Then a key deve ser marcada como "revoked_at"
    And a validação deve falhar imediatamente
    And o histórico deve ser mantido para auditoria
    And o evento "api-key.revoked" deve ser publicado

  Scenario: Renovação de API key antes de expirar
    Given que tenho uma API key que expira em 30 dias
    When eu renovo a API key
    Then uma nova API key deve ser gerada
    And a key antiga ainda funciona por período de transição (7 dias)
    And a key nova já funciona imediatamente
    And permite migração gradual

