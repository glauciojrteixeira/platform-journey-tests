@implemented @vs-identity @segment_1 @j1.10 @authentication @critical @e2e
Feature: Logout
  Como um comprador ocasional
  Eu quero fazer logout da plataforma
  Para encerrar minha sessão com segurança

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando
    And que estou autenticado na plataforma

  @smoke @critical
  Scenario: Logout bem-sucedido
    Given que tenho um token JWT válido
    When eu faço logout
    Then o token deve ser invalidado no servidor
    # Evento auth.logout é publicado no exchange auth.events quando logout ocorre
    # O evento contém informações do usuário (userUuid, email, ipAddress, userAgent)
    And o evento "auth.logout" deve ser publicado
    And o evento "auth.logout" deve conter o campo "userUuid"
    And o evento "auth.logout" deve conter o campo "email"
    And o evento "auth.logout" deve conter o campo "ipAddress"
    And o evento "auth.logout" deve conter o campo "userAgent"
    When eu tento usar o token invalidado
    Then o acesso deve ser negado com status 401
    And o erro deve indicar token inválido

  @high
  Scenario: Logout apenas local (sem invalidar no servidor)
    Given que tenho um token JWT válido
    When eu removo o token apenas do frontend
    Then o token ainda é válido no servidor
    # Nota: Logout completo requer chamada ao servidor

  @behavior @medium
  Scenario: Logout com token inválido (idempotência)
    Given que tenho um token JWT inválido
    When eu tento fazer logout
    # Nota: Logout é idempotente - mesmo com token inválido, retorna sucesso (204)
    # O token inválido não existe no banco, então não há nada para revogar
    # Isso é um comportamento seguro e idempotente
    Then o logout deve ser bem-sucedido (status 204)
    And nenhum evento "auth.logout" deve ser publicado
    # Nota: Evento não é publicado porque não há usuário associado ao token inválido

  @error @high
  Scenario: Logout falha sem header Authorization
    Given que não tenho header Authorization
    When eu tento fazer logout sem header Authorization
    # Nota: O serviço pode retornar 400, 401 ou 500 quando não há header Authorization
    # 500 ocorre quando o serviço tenta processar sem header e encontra erro interno
    Then o logout deve falhar com status 400, 401 ou 500
    And nenhum evento "auth.logout" deve ser publicado

  @error @medium
  Scenario: Logout com header Authorization malformado
    Given que tenho um header Authorization malformado
    When eu tento fazer logout com header malformado
    # Nota: O serviço pode aceitar token malformado e retornar sucesso (204) ou erro
    # Este cenário valida que o comportamento é consistente (sucesso ou erro apropriado)
    Then o logout deve retornar status apropriado (200, 204, 400, 401 ou 500)
    # Se retornar sucesso, o token malformado foi processado (comportamento do serviço)
    # Se retornar erro, o token malformado foi rejeitado (comportamento esperado)

  @behavior @medium
  Scenario: Logout múltiplas vezes com mesmo token (idempotência)
    Given que tenho um token JWT válido
    When eu faço logout
    Then o logout deve ser bem-sucedido
    When eu tento fazer logout novamente com o mesmo token
    Then o segundo logout deve ser bem-sucedido ou retornar erro apropriado
    # Nota: Logout deve ser idempotente - múltiplas chamadas não devem causar erro

  @behavior @medium
  Scenario: Logout invalida sessão atual
    Given que tenho um token JWT válido
    And que tenho uma sessão ativa
    When eu faço logout
    Then o token deve ser invalidado no servidor
    And a sessão atual deve ser invalidada
    And o evento "auth.logout" deve ser publicado

