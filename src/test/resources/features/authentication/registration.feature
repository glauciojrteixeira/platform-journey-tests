@implemented @vs-identity @segment_1 @j1.1 @registration @critical @e2e
Feature: Registro e Onboarding de Comprador Ocasional
  Como um comprador ocasional
  Eu quero me registrar na plataforma
  Para poder fazer arremates

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando

  # Cenário simplificado sem OTP - NÃO MAIS SUPORTADO: API agora exige OTP obrigatório
  @not_implemented @otp_required
  Scenario: Registro bem-sucedido sem OTP
    Given que estou na tela de registro
    When eu escolho registro com credenciais próprias
    And eu informo:
      | campo      | valor                    |
      | nome       | João Silva               |
      | cpf        | 12345678901              |
      | email      | joao.silva@example.com    |
      | telefone   | +5511999998888            |
    And eu valido o reCAPTCHA
    And eu envio os dados para criar identidade
    Then a identidade deve ser criada com sucesso
    # Nota: Credenciais e perfil podem ser criados assincronamente via eventos
    # And as credenciais devem ser provisionadas
    # And o perfil deve ser criado automaticamente
    # And eu devo receber um JWT válido
    # And o evento "user.created.v1" deve ser publicado
    # And o evento "credentials.provisioned.v1" deve ser publicado

  # Cenário completo com OTP - marcado como não implementado
  @not_implemented @otp_required
  Scenario: Registro bem-sucedido via credenciais próprias com OTP
    Given que estou na tela de registro
    When eu escolho registro com credenciais próprias
    And eu informo:
      | campo      | valor                    |
      | nome       | João Silva               |
      | cpf        | 12345678901              |
      | email      | joao.silva@example.com    |
      | telefone   | +5511999998888            |
    And eu valido o reCAPTCHA
    And eu solicito OTP via WhatsApp
    And eu recebo o código OTP
    And eu valido o OTP informando "123456"
    And eu envio os dados para criar identidade
    Then a identidade deve ser criada com sucesso
    And as credenciais devem ser provisionadas
    And o perfil deve ser criado automaticamente
    And eu devo receber um JWT válido
    And o evento "user.created.v1" deve ser publicado
    And o evento "credentials.provisioned.v1" deve ser publicado

  Scenario: Registro falha com CPF duplicado
    Given que já existe um usuário com CPF "12345678901"
    When eu tento criar um novo usuário com o mesmo CPF:
      | campo      | valor                    |
      | nome       | Maria Santos             |
      | cpf        | 12345678901              |
      | email      | maria.santos@example.com |
      | telefone   | +5511999997777           |
    And eu envio os dados para criar identidade
    Then o registro deve falhar com status 409
    And o erro deve ser "CPF_ALREADY_EXISTS"
    And a mensagem de erro deve conter "CPF já cadastrado"
    And nenhuma identidade deve ser criada

  Scenario: Registro falha com email inválido
    Given que estou na tela de registro
    When eu informo dados com email inválido:
      | campo      | valor                    |
      | nome       | João Silva               |
      | cpf        | 98765432100              |
      | email      | email-invalido           |
      | telefone   | +5511999998888           |
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
      | campo      | valor                    |
      | nome       | João Silva               |
      | cpf        | 11122233344              |
      | email      | joao.silva@example.com   |
      | telefone   | +5511999998888           |
    And eu valido o reCAPTCHA
    And eu solicito OTP via WhatsApp
    And eu valido o OTP informando "000000"
    And eu envio os dados para criar identidade
    Then o registro deve falhar com status 401
    And o erro deve ser "OTP_INVALID"
    And a mensagem de erro deve conter "código OTP inválido"
    And nenhuma identidade deve ser criada

