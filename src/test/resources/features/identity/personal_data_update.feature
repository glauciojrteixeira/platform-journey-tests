@segment_1 @j1.5 @identity @critical @e2e
Feature: Alteração de Dados Pessoais
  Como um comprador ocasional
  Eu quero alterar meus dados pessoais críticos
  Para manter minhas informações atualizadas

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando
    And que estou autenticado na plataforma

  @not_implemented @otp_required
  Scenario: Alteração de email bem-sucedida com validação OTP
    Given que consulto meus dados atuais
    When eu solicito alteração de email para "novo.email@example.com"
    Then um OTP deve ser enviado para o novo email
    When eu valido o OTP informando "123456"
    Then o email deve ser atualizado com sucesso
    And o evento "identity.updated" deve ser publicado
    And o Auth Service deve sincronizar a cópia local via evento

  @not_implemented @otp_required
  Scenario: Alteração de telefone bem-sucedida com validação OTP
    Given que consulto meus dados atuais
    When eu solicito alteração de telefone para "+5511999998888"
    Then um OTP deve ser enviado para o novo telefone
    When eu valido o OTP informando "123456"
    Then o telefone deve ser atualizado com sucesso
    And o evento "identity.updated" deve ser publicado

  Scenario: Alteração de email falha com email já existente
    Given que já existe um usuário com email "existente@example.com"
    When eu tento alterar meu email para "existente@example.com"
    Then a alteração de identidade deve falhar com status 409
    And o erro de identidade deve ser "EMAIL_ALREADY_EXISTS"

  Scenario: Tentativa de alterar CPF
    Given que consulto meus dados atuais
    When eu tento alterar meu CPF para "98765432100"
    Then a alteração de identidade deve falhar com status 400
    And o erro deve indicar que CPF não pode ser alterado
    And a mensagem de erro de identidade deve conter "CPF é imutável"

  @not_implemented @otp_required
  Scenario: Alteração de email falha com OTP inválido
    Given que consulto meus dados atuais
    When eu solicito alteração de email para "novo.email@example.com"
    And um OTP é enviado
    When eu valido o OTP informando "000000"
    Then a alteração deve falhar com status 401
    And o erro deve ser "OTP_INVALID"
    And o email não deve ser alterado

