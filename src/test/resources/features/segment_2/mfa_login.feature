@not_implemented @segment_2 @j2.3 @authentication @mfa @critical @e2e @otp_required
Feature: Login com MFA
  Como um arrematador profissional com MFA ativado
  Eu quero fazer login com código MFA
  Para acessar minha conta com segurança reforçada

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando
    And que tenho uma conta com MFA ativado

  Scenario: Login bem-sucedido com MFA
    Given que tenho credenciais válidas
    When eu faço login com minhas credenciais
    Then o sistema deve detectar MFA ativo
    And deve retornar "mfa_required: true"
    When eu solicito código MFA
    Then um código MFA deve ser enviado
    When eu valido o código MFA informando "123456"
    Then o login deve ser bem-sucedido
    And eu devo receber um JWT válido completo

  Scenario: Login falha com código MFA inválido
    Given que tenho credenciais válidas
    When eu faço login com minhas credenciais
    And o sistema solicita código MFA
    When eu valido o código MFA informando "000000"
    Then o login deve falhar com status 401
    And o erro deve ser "MFA_INVALID"
    And nenhum JWT deve ser emitido

  Scenario: Login falha sem código MFA quando MFA está ativo
    Given que tenho credenciais válidas
    When eu faço login com minhas credenciais
    And o sistema solicita código MFA
    When eu tento prosseguir sem informar código MFA
    Then o login deve falhar com status 401
    And o erro deve indicar que MFA é obrigatório

