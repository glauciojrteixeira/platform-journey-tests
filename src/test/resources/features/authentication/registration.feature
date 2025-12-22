@implemented @vs-identity @segment_1 @j1.1 @registration @critical @e2e
Feature: Registro e Onboarding de Comprador Ocasional
  Como um comprador ocasional
  Eu quero me registrar na plataforma
  Para poder fazer arremates

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando

  # Cenário básico de criação de identidade (consolidado de create_identity.feature)
  Scenario: Criação de identidade bem-sucedida
    Given que estou na tela de registro
    When eu informo:
      | campo           | valor                    |
      | nome            | João Silva               |
      | documentNumber  | {unique_cpf}             |
      | documentType    | CPF                      |
      | email           | joao.silva@example.com    |
      | telefone        | +5511999998888            |
    And eu envio os dados para criar identidade
    Then a identidade deve ser criada com sucesso
    And a identidade deve ser criada no Identity Service

  Scenario: Registro falha com documento duplicado
    Given que já existe um usuário com documento "{unique_cpf}" do tipo "CPF"
    When eu tento criar um novo usuário com o mesmo documento:
      | campo           | valor                    |
      | nome            | Maria Santos             |
      | documentNumber  | {same_document}          |
      | documentType    | CPF                      |
      | email           | maria.santos@example.com |
      | telefone        | +5511999997777           |
    And eu envio os dados para criar identidade
    Then o registro deve falhar com status 409
    And a mensagem de erro deve conter "Document already exists"
    And nenhuma identidade deve ser criada

  Scenario: Registro falha com email inválido
    Given que estou na tela de registro
    When eu informo dados com email inválido:
      | campo           | valor                    |
      | nome            | João Silva               |
      | documentNumber  | {unique_cpf}             |
      | documentType    | CPF                      |
      | email           | email-invalido           |
      | telefone        | +5511999998888           |
    And eu envio os dados para criar identidade
    # Nota: Como a API agora exige OTP antes de criar usuário, a falha ocorre na solicitação de OTP
    # quando o email é inválido, não na criação do usuário
    Then a solicitação de OTP deve falhar com status 400
    # A API retorna código de erro de validação genérico quando email é inválido
    And a mensagem de erro deve conter "Email must be valid"
    And nenhuma identidade deve ser criada

  # Cenário marcado como não implementado - OTP não está disponível
  @not_implemented @otp_required
  Scenario: Registro falha com OTP inválido
    Given que estou na tela de registro
    When eu informo dados válidos:
      | campo           | valor                    |
      | nome            | João Silva               |
      | documentNumber  | {unique_cpf}             |
      | documentType    | CPF                      |
      | email           | joao.silva@example.com   |
      | telefone        | +5511999998888           |
    And eu valido o reCAPTCHA
    And eu solicito OTP via WhatsApp
    And eu valido o OTP informando "000000"
    And eu envio os dados para criar identidade
    Then o registro deve falhar com status 401
    And o erro deve ser "OTP_INVALID"
    And a mensagem de erro deve conter "código OTP inválido"
    And nenhuma identidade deve ser criada

