@not_implemented @segment_3 @j3.5 @b2b @admin @high @e2e @may_require_auth @otp_required
Feature: Remoção de Usuário da PJ
  Como um admin de uma entidade jurídica
  Eu quero remover usuário da minha empresa
  Para desvincular acesso corporativo

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando
    And que sou admin de uma entidade jurídica
    And que estou autenticado na plataforma

  Scenario: Remoção de usuário bem-sucedida
    Given que tenho usuários vinculados à minha PJ
    When eu consulto lista de usuários vinculados
    And seleciono um usuário para remover
    And confirmo a remoção com OTP/MFA
    Then o usuário deve ser desvinculado da PJ
    And o campo "legal_entity_uuid" deve ser atualizado para null
    And o evento "entity.user.unlinked" deve ser publicado
    And o usuário perde acesso a recursos corporativos
    And se não tiver outra vinculação, conta pode ser convertida para B2C

  Scenario: Remoção falha ao tentar remover último admin
    Given que sou o único admin da PJ
    When eu tento remover outro admin (que não existe)
    # Nota: Também falha se tentar remover a si mesmo
    Then a operação deve falhar com status 400
    And o erro deve indicar que não pode remover último admin

  Scenario: Remoção falha ao tentar remover representante legal sem transferir
    Given que tenho um representante legal vinculado
    When eu tento remover o representante legal sem transferir representação antes
    Then a operação deve falhar com status 400
    And o erro deve indicar que representação deve ser transferida primeiro

  Scenario: Remoção requer confirmação OTP/MFA
    Given que tenho usuários vinculados à minha PJ
    When eu tento remover usuário sem confirmar OTP/MFA
    Then a operação deve falhar com status 401
    And o erro deve indicar que confirmação é obrigatória

