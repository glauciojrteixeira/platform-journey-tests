@implemented @segment_1 @j1.1 @registration @otp @critical @e2e @vs-identity
Feature: Fluxo Completo de Registro com OTP
  Como um comprador ocasional
  Eu quero me registrar na plataforma usando OTP
  Para poder fazer arremates com segurança

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando
    And que tenho dados de teste únicos

  @simulate-provider @otp_request @otp_validation
  Scenario: Registro completo com OTP via EMAIL (com simulação)
    Given que estou na tela de registro
    When eu informo:
      | campo           | valor                    |
      | nome            | João Silva               |
      | documentNumber  | {unique_cpf}             |
      | documentType    | CPF                      |
      | email           | joao.silva@example.com   |
      | telefone        | +5511999998888           |
    And eu solicito OTP via "EMAIL" para "REGISTRATION"
    Then a solicitação de OTP deve retornar status 200
    And o código OTP deve estar presente na resposta
    And o evento "otp.sent" deve ser publicado
    When eu recebo o código OTP
    And eu valido o OTP recebido
    Then a validação de OTP deve retornar status 200
    And o evento "otp.validated" deve ser publicado
    And eu devo receber um sessionToken válido
    When eu envio os dados para criar identidade com o sessionToken
    Then a identidade deve ser criada com sucesso
    And as credenciais devem ser provisionadas
    And o evento "user.created.v1" deve ser publicado
    And o evento "credentials.provisioned.v1" deve ser publicado

  @simulate-provider @password_recovery
  Scenario: Recuperação de senha completa (com simulação)
    Given que crio um usuário com esses dados
    And que esqueci minha senha
    When eu solicito recuperação de senha para o email do usuário criado
    Then a solicitação de recuperação de senha deve retornar status 200
    And o evento "otp.sent" deve ser publicado
    When eu recebo o código OTP
    And eu valido o OTP recebido via WhatsApp
    Then a validação de OTP deve retornar status 200
    When eu redefino minha senha com o OTP validado
    Then a senha deve ser redefinida com sucesso
    And o evento "otp.validated" deve ser publicado

  @simulate-provider @first_access
  Scenario: Primeiro acesso após registro (com simulação)
    Given que crio um usuário com esses dados
    And que as credenciais foram provisionadas
    When eu faço login com minhas credenciais
    Then o login deve ser bem-sucedido
    And eu devo receber um JWT válido
    And o sistema deve solicitar alteração de senha obrigatória
