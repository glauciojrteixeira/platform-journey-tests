@not_implemented @vs-identity @segment_2 @j2.2 @security @mfa @high @e2e @otp_required
Feature: Ativação de MFA
  Como um arrematador profissional
  Eu quero ativar MFA na minha conta
  Para aumentar a segurança

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando
    And que estou autenticado na plataforma

  Scenario: Ativação de MFA bem-sucedida
    Given que acesso a área de segurança
    When eu solicito ativação de MFA
    And escolho método "OTP_APP"
    Then um OTP deve ser enviado para confirmação
    When eu valido o OTP informando "123456"
    Then o MFA deve ser ativado
    And o campo "mfa_enabled" deve ser atualizado para true
    And o evento "mfa.enabled" deve ser publicado
    And o próximo login exigirá MFA

  Scenario: Ativação de MFA via SMS
    Given que acesso a área de segurança
    When eu solicito ativação de MFA
    And escolho método "SMS"
    Then um OTP deve ser enviado via SMS
    When eu valido o OTP informando "123456"
    Then o MFA deve ser ativado com método SMS

  Scenario: Ativação de MFA falha com OTP inválido
    Given que acesso a área de segurança
    When eu solicito ativação de MFA
    And um OTP é enviado
    When eu valido o OTP informando "000000"
    Then a ativação deve falhar com status 401
    And o erro deve ser "OTP_INVALID"
    And o MFA não deve ser ativado

