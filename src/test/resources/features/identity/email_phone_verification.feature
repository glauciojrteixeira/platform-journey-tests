@not_implemented @segment_1 @j1.11 @identity @verification @medium @e2e @otp_required
Feature: Verificação de Email e Telefone
  Como um comprador ocasional
  Eu quero verificar meu email e telefone cadastrados
  Para aumentar a confiabilidade da minha conta

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando
    And que estou autenticado na plataforma

  Scenario: Verificação de email bem-sucedida
    Given que tenho um email cadastrado não verificado
    When eu solicito verificação de email
    Then um OTP deve ser enviado para meu email
    When eu valido o OTP informando "123456"
    Then o campo "email_verified" deve ser atualizado para true
    And o evento "identity.email.verified" deve ser publicado

  Scenario: Verificação de telefone bem-sucedida
    Given que tenho um telefone cadastrado não verificado
    When eu solicito verificação de telefone
    Then um OTP deve ser enviado para meu telefone via WhatsApp
    When eu valido o OTP informando "123456"
    Then o campo "phone_verified" deve ser atualizado para true
    And o evento "identity.phone.verified" deve ser publicado

  Scenario: Verificação falha com OTP inválido
    Given que tenho um email cadastrado não verificado
    When eu solicito verificação de email
    And um OTP é enviado
    When eu valido o OTP informando "000000"
    Then a verificação deve falhar com status 401
    And o erro deve ser "OTP_INVALID"
    And o email não deve ser marcado como verificado

