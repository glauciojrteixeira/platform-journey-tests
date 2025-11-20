@not_implemented @transversal @jt.1 @authentication @token @medium @e2e
Feature: Refresh Token
  Como um usuário autenticado
  Eu quero renovar meu JWT sem reautenticação completa
  Para manter sessão ativa sem interrupção

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando
    And que estou autenticado na plataforma

  Scenario: Renovação de token bem-sucedida
    Given que tenho um token JWT válido próximo do vencimento
    When o sistema detecta e solicita renovação
    And eu solicito renovação do token
    Then o token atual deve ser verificado
    And o usuário deve estar ativo (isActive = true)
    And um novo token JWT deve ser emitido
    And o novo token deve ter novos claims atualizados
    And a expiração deve ser atualizada
    And o usuário permanece autenticado sem interrupção

  Scenario: Renovação falha com token inválido
    Given que tenho um token JWT inválido
    When eu tento renovar o token
    Then a renovação deve falhar com status 401
    And o erro deve indicar que token é inválido
    And será necessário fazer login novamente

  Scenario: Renovação falha se usuário está inativo
    Given que tenho um token JWT válido
    And minha conta foi desativada
    When eu tento renovar o token
    Then a renovação deve falhar com status 403
    And o erro deve indicar que conta está inativa

  Scenario: Token antigo pode ser mantido por período de grace
    Given que tenho um token JWT válido
    When eu renovo o token
    Then o token antigo pode ser mantido por período de grace (5 minutos)
    # Nota: Alternativamente, o token antigo pode ser invalidado imediatamente
    # Comportamento depende da política de segurança configurada

