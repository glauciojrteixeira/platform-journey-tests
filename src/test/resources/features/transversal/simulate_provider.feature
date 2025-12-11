@implemented @vs-identity @transversal @segment_1 @simulate-provider @critical @e2e
Feature: Simulação de Providers
  Como um testador E2E
  Eu quero validar que o header simulate-provider está funcionando corretamente
  Para garantir que mensagens não são enviadas aos providers em ambientes não-PROD

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando
    And que tenho dados de teste únicos

  @simulate-provider-otp
  Scenario: Header simulate-provider deve estar presente na mensagem RabbitMQ ao solicitar OTP
    Given que crio um usuário com esses dados
    When eu solicito OTP via "EMAIL" para "REGISTRATION"
    Then a solicitação de OTP deve retornar status 200
    And o evento "otp.sent" deve ser publicado
    And o evento "otp.sent" deve conter o header "simulate-provider" com valor "true"
    And a mensagem não deve ser enviada ao provider real

  @simulate-provider-user-creation
  Scenario: Header simulate-provider deve estar presente na mensagem RabbitMQ ao criar usuário
    Given que crio um usuário com esses dados
    Then a identidade deve ser criada com sucesso
    And o evento "user.created.v1" deve ser publicado
    And o evento "user.created.v1" deve conter o header "simulate-provider" com valor "true"

  @simulate-provider-configuration
  Scenario: Simulação deve estar habilitada em ambientes não-PROD
    Given que estou executando testes em ambiente "local"
    Then a simulação de providers deve estar habilitada
    
  @simulate-provider-configuration
  Scenario: Simulação não deve estar habilitada em ambiente PROD
    Given que estou executando testes em ambiente "prod"
    Then a simulação de providers não deve estar habilitada

  @simulate-provider-multiple-requests
  Scenario: Múltiplas solicitações de OTP devem incluir header simulate-provider
    Given que crio um usuário com esses dados
    When eu solicito OTP via "EMAIL" para "REGISTRATION"
    And eu solicito OTP via "EMAIL" para "REGISTRATION"
    And eu solicito OTP via "EMAIL" para "REGISTRATION"
    Then todas as mensagens "otp.sent" devem conter o header "simulate-provider" com valor "true"

