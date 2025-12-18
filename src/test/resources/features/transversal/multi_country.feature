@implemented @vs-identity @transversal @multi-country @critical @e2e
Feature: Suporte Multi-Country
  Como um testador E2E
  Eu quero validar que o sistema funciona corretamente com diferentes países
  Para garantir que a arquitetura multi-country está funcionando conforme esperado

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando
    And que tenho dados de teste únicos

  @multi-country-default
  Scenario: Sistema deve usar país padrão (BR) quando não especificado
    Given que o país padrão está configurado como "BR"
    And que crio um usuário com esses dados
    Then a identidade deve ser criada com sucesso
    And o evento "user.created.v1" deve ser publicado
    And o evento "user.created.v1" deve conter o header "country-code" com valor "br"
    And o RabbitMQ deve estar conectado ao virtual host "/br"

  @multi-country-header-validation
  Scenario: Header country-code deve estar presente em todos os eventos críticos
    Given que crio um usuário com esses dados
    When eu solicito OTP via "EMAIL" para "REGISTRATION"
    Then a solicitação de OTP deve retornar status 200
    And o evento "otp.sent" deve ser publicado
    And o evento "otp.sent" deve conter o header "country-code" com valor "br"
    And o evento "user.created.v1" deve conter o header "country-code" com valor "br"

  @multi-country-rabbitmq-vhost
  Scenario: RabbitMQ deve usar virtual host baseado no país configurado
    Given que o país padrão está configurado como "BR"
    Then o RabbitMQ deve estar conectado ao virtual host "/br"
    
  @multi-country-header-lowercase
  Scenario: Header country-code deve estar em lowercase conforme RFC 6648
    Given que o país padrão está configurado como "BR"
    And que crio um usuário com esses dados
    Then a identidade deve ser criada com sucesso
    And o evento "user.created.v1" deve ser publicado
    And o evento "user.created.v1" deve conter o header "country-code" com valor "br"
    # Validação: header deve ser lowercase mesmo que configuração seja uppercase
    And o header "country-code" do evento "user.created.v1" deve estar em lowercase
