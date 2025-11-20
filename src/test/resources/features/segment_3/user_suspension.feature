@not_implemented @segment_3 @j3.4 @b2b @admin @high @e2e @may_require_auth
Feature: Suspensão de Usuário
  Como um admin de uma entidade jurídica
  Eu quero suspender acesso de usuários vinculados
  Para controlar acessos corporativos

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando
    And que sou admin de uma entidade jurídica
    And que estou autenticado na plataforma

  Scenario: Suspensão de usuário bem-sucedida
    Given que tenho usuários vinculados à minha PJ
    When eu consulto lista de usuários vinculados
    And seleciono um usuário ativo
    When eu suspendo o usuário com justificativa "Suspeita de atividade suspeita"
    Then o usuário deve ser suspenso
    And o campo "isActive" deve ser atualizado para false
    And todas as credenciais devem ser suspensas
    And todos os tokens ativos devem ser revogados
    And o evento "user.suspended" deve ser publicado
    And um email deve ser enviado ao usuário suspendido
    When o usuário suspendido tenta fazer login
    Then o login deve falhar com status 403
    And o erro deve indicar que conta está suspensa

  Scenario: Suspensão falha sem justificativa
    Given que tenho usuários vinculados à minha PJ
    When eu tento suspender usuário sem justificativa
    Then a operação deve falhar com status 400
    And o erro deve indicar que justificativa é obrigatória

  Scenario: Admin pode reativar usuário suspenso
    Given que tenho um usuário suspenso
    When eu reativo o usuário
    Then o usuário deve ser reativado
    And o campo "isActive" deve ser atualizado para true
    And as credenciais devem ser reativadas
    And o evento "user.reactivated" deve ser publicado

