@vs-identity @segment_1 @j1.9 @identity @lgpd @medium @e2e
Feature: Reativação de Conta
  Como um usuário com conta desativada
  Eu quero reativar minha conta
  Para voltar a usar a plataforma

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando

  @not_implemented @otp_required
  Scenario: Reativação de conta bem-sucedida
    Given que tenho uma conta desativada
    When eu tento fazer login
    Then o sistema deve detectar conta desativada
    And deve oferecer opção de reativação
    When eu solicito reativação da conta
    Then um OTP deve ser enviado para meu email/telefone cadastrado
    When eu valido o OTP informando "123456"
    Then a conta deve ser reativada
    And o evento "user.reactivated" deve ser publicado
    And as credenciais devem ser reativadas
    When eu faço login
    Then o login deve ser bem-sucedido
    And eu devo receber um JWT válido

  @not_implemented @otp_required
  Scenario: Reativação de conta falha com OTP inválido
    Given que tenho uma conta desativada
    When eu solicito reativação da conta
    And um OTP é enviado
    When eu valido o OTP informando "000000"
    Then a reativação deve falhar com status 401
    And o erro deve ser "OTP_INVALID"
    And a conta deve permanecer desativada

  @not_implemented
  Scenario: Histórico é preservado após reativação
    Given que tenho uma conta desativada com histórico
    When eu reativo minha conta
    Then o histórico anterior deve ser preservado
    And os dados devem estar acessíveis novamente

