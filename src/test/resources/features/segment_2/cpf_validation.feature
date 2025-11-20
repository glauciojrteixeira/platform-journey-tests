@not_implemented @segment_2 @j2.1 @identity @validation @high @e2e
Feature: Registro com Validação de CPF
  Como um arrematador profissional
  Eu quero me registrar com validação de CPF
  Para ter acesso a funcionalidades profissionais

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando

  Scenario: Registro com validação de CPF bem-sucedida
    Given que estou na tela de registro profissional
    When eu informo meus dados:
      | campo      | valor                    |
      | nome       | João Silva               |
      | cpf        | 12345678901              |
      | email      | joao.silva@example.com   |
      | telefone   | +5511999998888           |
    And eu envio os dados para criar identidade
    Then a identidade deve ser criada com sucesso
    When eu solicito validação de CPF
    Then o CPF deve ser validado via serviço externo
    And a conta deve ser habilitada completamente
    And o perfil deve ser marcado como "validado profissionalmente"
    And o evento "identity.cpf.validated" deve ser publicado

  Scenario: Registro com CPF inválido
    Given que estou na tela de registro profissional
    When eu informo dados com CPF inválido:
      | campo      | valor                    |
      | nome       | João Silva               |
      | cpf        | 00000000000              |
      | email      | joao.silva@example.com   |
      | telefone   | +5511999998888           |
    And eu envio os dados para criar identidade
    Then a identidade deve ser criada
    When eu solicito validação de CPF
    Then a validação deve falhar
    And a conta deve ter restrições até correção do CPF
    And o erro deve indicar CPF inválido

