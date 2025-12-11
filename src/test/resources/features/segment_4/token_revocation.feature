@not_implemented @vs-identity @segment_4 @j4.8 @b2b @security @enterprise @high @e2e @may_require_auth
Feature: Revogação de Tokens Ativos
  Como admin técnico de uma plataforma B2B
  Eu quero revogar tokens JWT específicos ou todos
  Para controle de segurança e resposta a incidentes

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando
    And que sou admin técnico de uma plataforma
    And que estou autenticado na plataforma

  Scenario: Revogação de token específico
    Given que identifico necessidade de revogar token específico
    When eu consulto tokens ativos de um usuário
    And seleciono um token específico para revogar
    When eu revogo o token específico
    Then o token deve ser marcado como "revoked_at"
    And a validação deve falhar imediatamente
    And o evento "auth.tokens.revoked" deve ser publicado
    When o usuário tenta usar o token revogado
    Then o acesso deve ser negado com status 401

  Scenario: Revogação de todos os tokens de um usuário
    Given que identifico necessidade de revogar todos os tokens
    When eu consulto tokens ativos de um usuário
    And revogo todos os tokens desse usuário
    Then todos os tokens devem ser marcados como "revoked_at"
    And todas as validações devem falhar imediatamente
    And o evento "auth.tokens.revoked.all" deve ser publicado
    And o usuário precisa fazer login novamente

  Scenario: Consulta de tokens ativos antes de revogar
    Given que tenho tokens emitidos
    When eu consulto tokens ativos de um usuário
    Then apenas tokens não revogados e não expirados devem ser retornados
    And os tokens devem estar filtrados por:
      | campo      | condição                    |
      | revoked_at | IS NULL                     |
      | expires_at | > NOW()                     |

