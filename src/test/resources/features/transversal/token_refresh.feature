@vs-identity @transversal @segment_1 @jt.1 @authentication @token @medium @e2e
Feature: Refresh Token
  Como um usuário autenticado
  Eu quero renovar meu JWT usando refresh token
  Para manter sessão ativa sem interrupção

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando

  @implemented
  Scenario: Renovação de token bem-sucedida com refresh token válido
    Given que tenho dados de teste únicos
    And que crio um usuário com esses dados
    When eu faço login com minhas credenciais
    And eu recebo um JWT válido com refresh token
    When eu solicito renovação do token usando o refresh token
    Then a renovação deve ser bem-sucedida com status 200
    And eu devo receber um novo JWT válido
    And o novo token deve ter tipo "Bearer"
    And o novo token deve ter data de expiração futura
    And a mensagem deve indicar "Token refreshed successfully"
    And o refresh token antigo deve ser revogado

  @implemented
  Scenario: Renovação falha com refresh token nulo
    When eu tento renovar o token com refresh token nulo
    Then a renovação deve falhar com status 400
    And o erro deve ser "AU-A-BUS006"
    And a mensagem de erro deve indicar que refresh token é obrigatório

  @implemented
  Scenario: Renovação falha com refresh token vazio
    When eu tento renovar o token com refresh token vazio
    Then a renovação deve falhar com status 400
    And o erro deve ser "AU-A-BUS006"
    And a mensagem de erro deve indicar que refresh token é obrigatório

  @implemented
  Scenario: Renovação falha com refresh token inválido (formato incorreto)
    When eu tento renovar o token com refresh token "token-invalido-sem-formato-jwt"
    Then a renovação deve falhar com status 401
    And o erro deve ser "AU-A-BUS006"
    And a mensagem de erro deve indicar que formato do token é inválido

  @implemented
  Scenario: Renovação falha com refresh token expirado
    Given que tenho dados de teste únicos
    And que crio um usuário com esses dados
    When eu faço login com minhas credenciais
    And eu recebo um refresh token que foi expirado
    When eu tento renovar o token usando o refresh token expirado
    Then a renovação deve falhar com status 401
    And o erro deve ser "AU-A-BUS006"
    And a mensagem de erro deve indicar que refresh token expirou

  @implemented
  Scenario: Renovação falha com refresh token revogado
    Given que tenho dados de teste únicos
    And que crio um usuário com esses dados
    When eu faço login com minhas credenciais
    And eu recebo um refresh token válido
    And o refresh token foi revogado
    When eu tento renovar o token usando o refresh token revogado
    Then a renovação deve falhar com status 401
    And o erro deve ser "AU-A-BUS006"
    And a mensagem de erro deve indicar que refresh token foi revogado

  @not_implemented @partial @requires_event_sync
  Scenario: Renovação falha se usuário está inativo
    Given que tenho dados de teste únicos
    And que crio um usuário com esses dados
    When eu faço login com minhas credenciais
    And eu recebo um refresh token válido
    And minha conta foi desativada
    # Nota: Este cenário requer sincronização via eventos RabbitMQ entre Identity Service e Auth Service
    # A desativação no Identity Service deve ser propagada para o Auth Service antes do refresh token ser usado
    # Em ambiente de testes, pode haver delay na sincronização que impede a validação adequada
    # O Auth Service verifica user.isActive() mas depende de sincronização via eventos
    # Por isso este cenário está marcado como @not_implemented até que a sincronização seja garantida
    When eu tento renovar o token usando o refresh token
    Then a renovação deve falhar com status 401
    And o erro deve ser "AU-A-BUS006"
    And a mensagem de erro deve indicar que refresh token é inválido

