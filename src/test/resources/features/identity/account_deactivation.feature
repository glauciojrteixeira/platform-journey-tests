@segment_1 @j1.8 @identity @lgpd @critical @e2e
Feature: Desativação de Conta
  Como um comprador ocasional
  Eu quero desativar minha conta
  Para cumprir com LGPD e remover meu acesso

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando
    And que estou autenticado na plataforma

  @not_implemented @otp_required
  Scenario: Desativação de conta bem-sucedida com confirmação OTP
    Given que tenho uma conta ativa
    When eu solicito desativação da conta
    Then um OTP deve ser enviado para confirmação
    When eu valido o OTP informando "123456"
    Then a conta deve ser desativada (soft delete)
    And todas as credenciais devem ser suspensas
    And todos os tokens ativos devem ser revogados
    And o evento "user.deactivated" deve ser publicado
    When eu tento fazer login novamente
    Then o login deve falhar com status 403
    And o erro deve indicar que conta está desativada

  @not_implemented @otp_required
  Scenario: Desativação de conta falha sem confirmação OTP
    Given que tenho uma conta ativa
    When eu tento desativar sem confirmar OTP
    Then a desativação deve falhar com status 401
    And a conta deve permanecer ativa

  Scenario: Dados são mantidos após desativação (LGPD)
    Given que tenho uma conta ativa com dados
    When eu desativo minha conta
    Then os dados devem ser mantidos por período de retenção
    And a conta deve poder ser reativada posteriormente
    And os dados não devem ser deletados imediatamente

