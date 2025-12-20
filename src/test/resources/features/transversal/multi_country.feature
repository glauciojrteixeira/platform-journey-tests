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

  @multi-country-isolation @multi-country-idempotency
  Scenario: Dados devem ser isolados por país - Idempotência por país
    Given que o país padrão está configurado como "BR"
    And que crio um usuário com esses dados:
      | campo      | valor                    |
      | nome       | João Silva BR            |
      | cpf        | {unique_cpf}             |
      | email      | {unique_email}           |
      | telefone   | {unique_phone}           |
    Then a identidade deve ser criada com sucesso
    And o evento "user.created.v1" deve ser publicado
    And o evento "user.created.v1" deve conter o header "country-code" com valor "br"
    When eu tento criar um usuário com os mesmos dados no país "BR"
    Then o registro deve falhar com status 409
    And o erro deve indicar que o CPF já existe no país "BR"
    # NOTA: CPF é único globalmente (não por país), pois é específico do Brasil.
    # Outros países usam documentos diferentes (CUIT para AR, RUT para CL, etc.),
    # mas o backend ainda não suporta esses documentos.
    # Quando o backend suportar documentos de outros países, este teste deve ser atualizado
    # para validar isolamento por país usando documentos apropriados para cada país.

  @multi-country-propagation @vs-customer-communications
  Scenario: countryCode deve ser propagado entre microserviços (transactional-messaging → delivery-tracker → audit-compliance)
    Given que o país padrão está configurado como "BR"
    And que crio um usuário com esses dados
    Then a identidade deve ser criada com sucesso
    When eu solicito OTP via "EMAIL" para "REGISTRATION"
    Then a solicitação de OTP deve retornar status 200
    And o evento "otp.sent" deve ser publicado
    And o evento "otp.sent" deve conter o header "country-code" com valor "br"
    And o Transactional Messaging Service deve processar o evento "otp.sent" com countryCode "br"
    And o evento "delivery.tracking.created.v1" deve ser publicado
    And o evento "delivery.tracking.created.v1" deve conter o campo "countryCode" com valor "BR"
    And o Delivery Tracker Service deve persistir o tracking com countryCode "BR"
    And o evento "audit.events" deve ser publicado
    And o evento "audit.events" deve conter o header "country-code" com valor "br"
    And o Audit Compliance Service deve persistir o log de auditoria com countryCode "BR"

  @multi-country-multiple-countries
  Scenario: Sistema deve suportar múltiplos países simultaneamente
    Given que o país padrão está configurado como "BR"
    And que crio um usuário com esses dados:
      | campo      | valor                    |
      | nome       | João Silva BR            |
      | cpf        | {unique_cpf_br}          |
      | email      | {unique_email_br}        |
      | telefone   | {unique_phone_br}        |
    Then a identidade deve ser criada com sucesso
    And o evento "user.created.v1" deve conter o header "country-code" com valor "br"
    When eu configuro o país padrão como "AR"
    And que crio um usuário com esses dados:
      | campo      | valor                    |
      | nome       | Juan Pérez AR            |
      | cpf        | {unique_cpf_ar}          |
      | email      | {unique_email_ar}        |
      | telefone   | {unique_phone_ar}        |
    Then a identidade deve ser criada com sucesso
    And o evento "user.created.v1" deve conter o header "country-code" com valor "ar"
    When eu configuro o país padrão como "CL"
    And que crio um usuário com esses dados:
      | campo      | valor                    |
      | nome       | Carlos González CL        |
      | cpf        | {unique_cpf_cl}          |
      | email      | {unique_email_cl}        |
      | telefone   | {unique_phone_cl}        |
    Then a identidade deve ser criada com sucesso
    And o evento "user.created.v1" deve conter o header "country-code" com valor "cl"
    # Validação: Cada país processa independentemente

  @multi-country-all-supported-countries
  Scenario Outline: Sistema deve funcionar corretamente para todos os países suportados
    Given que o país padrão está configurado como "<country_code>"
    And que crio um usuário com esses dados
    Then a identidade deve ser criada com sucesso
    And o evento "user.created.v1" deve ser publicado
    And o evento "user.created.v1" deve conter o header "country-code" com valor "<country_code_lowercase>"
    And o RabbitMQ deve estar conectado ao virtual host "/<country_code_lowercase>"
    
    Examples:
      | country_code | country_code_lowercase |
      | BR           | br                     |
      | AR           | ar                     |
      | CL           | cl                     |
      | BO           | bo                     |
      | US           | us                     |

  @multi-country-delivery-tracker @vs-customer-communications
  Scenario: Delivery Tracker deve validar countryCode obrigatório e lançar exceção non-retryable quando ausente
    Given que o país padrão está configurado como "BR"
    And que crio um usuário com esses dados
    Then a identidade deve ser criada com sucesso
    When eu solicito OTP via "EMAIL" para "REGISTRATION"
    Then a solicitação de OTP deve retornar status 200
    And o evento "otp.sent" deve ser publicado
    # Simular evento sem countryCode (cenário de erro)
    When um evento "delivery.tracking.created.v1" é publicado sem o campo "countryCode"
    Then o Delivery Tracker Consumer deve lançar uma exceção non-retryable
    And a mensagem deve ser enviada para o parking lot
    And a mensagem não deve ser reenviada para a fila principal

  @multi-country-audit-compliance @vs-customer-communications
  Scenario: Audit Compliance deve validar countryCode obrigatório e lançar exceção non-retryable quando ausente
    Given que o país padrão está configurado como "BR"
    And que crio um usuário com esses dados
    Then a identidade deve ser criada com sucesso
    When eu solicito OTP via "EMAIL" para "REGISTRATION"
    Then a solicitação de OTP deve retornar status 200
    And o evento "otp.sent" deve ser publicado
    # Simular evento sem countryCode no header (cenário de erro)
    When um evento "audit.events" é publicado sem o header "country-code"
    Then o Audit Compliance Consumer deve lançar uma exceção non-retryable
    And a mensagem deve ser enviada para o parking lot
    And a mensagem não deve ser reenviada para a fila principal

  @multi-country-query-isolation
  Scenario: Consultas devem retornar apenas dados do país configurado
    Given que o país padrão está configurado como "BR"
    And que crio um usuário com esses dados:
      | campo      | valor                    |
      | nome       | João Silva BR            |
      | cpf        | {unique_cpf_br}          |
      | email      | {unique_email_br}        |
      | telefone   | {unique_phone_br}        |
    Then a identidade deve ser criada com sucesso
    And o usuário deve ser consultável no país "BR"
    When eu configuro o país padrão como "AR"
    And que crio um usuário com esses dados:
      | campo      | valor                    |
      | nome       | Juan Pérez AR            |
      | cpf        | {unique_cpf_ar}          |
      | email      | {unique_email_ar}        |
      | telefone   | {unique_phone_ar}        |
    Then a identidade deve ser criada com sucesso
    And o usuário deve ser consultável no país "AR"
    When eu consulto usuários no país "BR"
    Then apenas o usuário do país "BR" deve ser retornado
    When eu consulto usuários no país "AR"
    Then apenas o usuário do país "AR" deve ser retornado
