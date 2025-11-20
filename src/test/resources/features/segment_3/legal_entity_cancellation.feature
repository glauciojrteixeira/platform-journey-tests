@not_implemented @segment_3 @j3.7 @b2b @admin @critical @e2e @may_require_auth @otp_required
Feature: Cancelamento de Entidade Jurídica
  Como um representante legal
  Eu quero cancelar a entidade jurídica
  Para encerrar operações corporativas

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando
    And que sou representante legal de uma entidade jurídica
    And que estou autenticado na plataforma

  Scenario: Cancelamento de entidade jurídica bem-sucedido
    Given que tenho uma entidade jurídica ativa
    And não há pendências que bloqueiem cancelamento
    When eu solicito cancelamento da entidade
    Then um OTP + MFA deve ser solicitado para confirmação
    When eu valido o código de confirmação
    And confirmo explicitamente o cancelamento
    Then a entidade deve ser cancelada
    And o campo "isActive" deve ser atualizado para false na LegalEntity
    And todos os usuários vinculados devem ser desativados ou desvinculados
    And todas as credenciais devem ser suspensas
    And todos os tokens devem ser revogados
    And o evento "legal-entity.cancelled" deve ser publicado
    And um email deve ser enviado para todos os usuários vinculados

  Scenario: Cancelamento falha se houver pendências
    Given que tenho uma entidade jurídica ativa
    And há pendências que bloqueiam cancelamento
    When eu tento cancelar a entidade
    Then a operação deve falhar com status 400
    And o erro deve indicar pendências que precisam ser resolvidas
    And a entidade deve permanecer ativa

  Scenario: Cancelamento requer confirmação explícita
    Given que tenho uma entidade jurídica ativa
    When eu solicito cancelamento
    But não confirmo explicitamente
    Then a operação deve falhar
    And a entidade deve permanecer ativa

