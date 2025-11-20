@segment_1 @j1.7 @password @security @high @e2e
Feature: Alteração de Senha
  Como um comprador ocasional
  Eu quero alterar minha senha
  Para manter minha conta segura

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando
    And que estou autenticado na plataforma

  Scenario: Alteração de senha bem-sucedida
    Given que tenho uma senha atual válida
    When eu altero minha senha:
      | senha_atual | TestPassword123! |
      | senha_nova  | SenhaNova456@    |
    Then a senha deve ser alterada com sucesso
    And o evento "credential.updated" deve ser publicado
    When eu faço logout
    And eu faço login com a nova senha:
      | username | usuario@example.com |
      | password | SenhaNova456@       |
    Then o login deve ser bem-sucedido

  Scenario: Alteração de senha falha com senha atual incorreta
    Given que tenho uma senha atual válida
    When eu tento alterar minha senha com senha atual incorreta:
      | senha_atual | SenhaErrada123! |
      | senha_nova  | SenhaNova456@   |
    Then a alteração deve falhar com status 401
    And o erro deve ser "INVALID_CURRENT_PASSWORD"

  Scenario: Alteração de senha falha com senha nova não atende complexidade
    Given que tenho uma senha atual válida
    When eu tento alterar minha senha com senha fraca:
      | senha_atual | TestPassword123! |
      | senha_nova  | 12345            |
    Then a alteração deve falhar com status 400
    And o erro deve indicar que senha não atende critérios de complexidade

  @not_implemented @otp_required
  Scenario: Alteração de senha com confirmação OTP (recomendado)
    Given que tenho uma senha atual válida
    When eu solicito alteração de senha
    Then um OTP deve ser enviado para meu email/telefone
    When eu valido o OTP informando "123456"
    And eu informo nova senha "SenhaNova456@"
    Then a senha deve ser alterada com sucesso

