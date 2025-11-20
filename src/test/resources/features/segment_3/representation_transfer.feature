@not_implemented @segment_3 @j3.6 @b2b @admin @critical @e2e @may_require_auth @otp_required
Feature: Transferência de Representação Legal
  Como um representante legal atual
  Eu quero transferir minha função para outro usuário
  Para delegar responsabilidades

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando
    And que sou representante legal de uma entidade jurídica
    And que estou autenticado na plataforma

  Scenario: Transferência de representação bem-sucedida
    Given que tenho outro usuário com role ADMIN na minha PJ
    When eu solicito transferência de representação para esse usuário
    Then um OTP + MFA deve ser solicitado para confirmação
    When eu valido o código de confirmação
    And o novo representante também confirma
    Then a representação deve ser transferida
    And o campo "legal_representative_user_uuid" deve ser atualizado
    And o evento "legal-entity.representation.transferred" deve ser publicado
    And um email deve ser enviado para ambos os usuários
    And o novo representante assume a função

  Scenario: Transferência falha se novo usuário não for ADMIN
    Given que tenho um usuário com role "OPERATOR" na minha PJ
    When eu tento transferir representação para esse usuário
    Then a operação deve falhar com status 400
    And o erro deve indicar que novo representante deve ter role ADMIN

  Scenario: Transferência requer confirmação de ambos os usuários
    Given que tenho outro usuário ADMIN na minha PJ
    When eu solicito transferência de representação
    And confirmo com OTP/MFA
    But o novo representante não confirma
    Then a transferência não deve ser concluída
    And a representação deve permanecer com o representante atual

