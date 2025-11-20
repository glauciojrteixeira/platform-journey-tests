@not_implemented @segment_3 @j3.3 @b2b @roles @high @e2e @may_require_auth
Feature: Alteração de Role de Usuário
  Como um admin de uma entidade jurídica
  Eu quero alterar roles/permissões de usuários vinculados
  Para gerenciar acessos corporativos

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando
    And que sou admin de uma entidade jurídica
    And que estou autenticado na plataforma

  Scenario: Alteração de role bem-sucedida
    Given que tenho usuários vinculados à minha PJ
    When eu consulto lista de usuários vinculados
    And seleciono um usuário com role "OPERATOR"
    When eu altero o role para "FINANCIAL":
      | user_uuid | uuid-do-usuario        |
      | novo_role | FINANCIAL              |
    Then o role deve ser alterado com sucesso
    And o evento "identity.role.changed" deve ser publicado
    And o Auth Service deve atualizar cópia local via evento
    And o próximo token JWT emitido terá novo role

  Scenario: Alteração de role falha se não for admin
    Given que sou um usuário com role "OPERATOR"
    When eu tento alterar role de outro usuário
    Then a operação deve falhar com status 403
    And o erro deve indicar que apenas ADMIN pode alterar roles

  Scenario: Alteração de role falha ao tentar remover último admin
    Given que sou o único admin da PJ
    When eu tento alterar meu próprio role para "OPERATOR"
    Then a operação deve falhar com status 400
    And o erro deve indicar que não pode remover último admin
    And meu role deve permanecer como ADMIN

  Scenario: Usuário precisa fazer logout/login para obter novo role
    Given que tenho um usuário com role "OPERATOR"
    And esse usuário está logado
    When eu altero o role para "FINANCIAL"
    Then o role no Identity Service deve ser atualizado
    But o token JWT atual ainda terá role antigo
    When o usuário faz logout e login novamente
    Then o novo token JWT deve ter role "FINANCIAL"

