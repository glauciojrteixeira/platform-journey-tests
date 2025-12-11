@implemented @vs-identity @segment_1 @j1.4 @profile @medium @e2e
Feature: Atualização de Perfil
  Como um comprador ocasional
  Eu quero atualizar minhas preferências de perfil
  Para personalizar minha experiência na plataforma

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando
    And que estou autenticado na plataforma

  Scenario: Atualização de preferências bem-sucedida
    Given que consulto meu perfil atual
    When eu atualizo minhas preferências:
      | campo         | valor           |
      | idioma        | pt-BR           |
      | notificacoes  | true            |
      | tema          | dark            |
    Then o perfil deve ser atualizado com sucesso
    And as preferências devem ser refletidas imediatamente
    And o evento "profile.updated" deve ser publicado

  Scenario: Atualização de perfil falha com dados inválidos
    Given que consulto meu perfil atual
    When eu tento atualizar com dados inválidos:
      | campo         | valor           |
      | idioma        | idioma-invalido |
    Then a atualização deve falhar com status 400
    And o erro deve indicar dados inválidos

  Scenario: Tentativa de alterar dados de segurança via perfil
    Given que consulto meu perfil atual
    When eu tento alterar dados de segurança:
      | campo  | valor              |
      | email  | novo@email.com     |
      | cpf    | 12345678901        |
    Then a atualização deve falhar com status 400
    And o erro deve indicar que dados de segurança não podem ser alterados via perfil

