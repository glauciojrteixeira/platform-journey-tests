@not_implemented @vs-identity @segment_2 @j2.1 @identity @validation @high @e2e
Feature: Registro com Validação de CPF
  Como um arrematador profissional
  Eu quero me registrar com validação de CPF
  Para ter acesso a funcionalidades profissionais

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando

  Scenario: Registro com validação de documento bem-sucedida
    Given que estou na tela de registro profissional
    When eu informo meus dados:
      | campo           | valor                    |
      | nome            | João Silva               |
      | documentNumber  | {unique_cpf}             |
      | documentType    | CPF                      |
      | email           | joao.silva@example.com   |
      | telefone        | +5511999998888           |
    And eu envio os dados para criar identidade
    Then a identidade deve ser criada com sucesso
    When eu solicito validação de documento
    Then o documento deve ser validado via serviço externo
    And a conta deve ser habilitada completamente
    And o perfil deve ser marcado como "validado profissionalmente"
    And o evento "identity.document.validated" deve ser publicado

  Scenario: Registro com documento inválido
    Given que estou na tela de registro profissional
    When eu informo dados com documento inválido:
      | campo           | valor                    |
      | nome            | João Silva               |
      | documentNumber  | 00000000000              |
      | documentType    | CPF                      |
      | email           | joao.silva@example.com   |
      | telefone        | +5511999998888           |
    And eu envio os dados para criar identidade
    Then a identidade deve ser criada
    When eu solicito validação de documento
    Then a validação deve falhar
    And a conta deve ter restrições até correção do documento
    And o erro deve indicar documento inválido

