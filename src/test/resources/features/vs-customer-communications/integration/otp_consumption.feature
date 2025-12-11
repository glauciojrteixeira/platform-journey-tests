@implemented @vs-customer-communications @integration @vs-identity @critical @messaging @event-driven @e2e
Feature: Integração - Consumo de Eventos Cross-VS (VS-Identity → VS-Customer-Communications)
  Como um testador de integração
  Eu quero validar o consumo de eventos otp.sent pelo Transactional Messaging Service
  Para garantir que a integração entre VSs funciona corretamente

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando

  @otp @email @integration
  Scenario: Consumir evento otp.sent e processar envio de OTP via Email
    Given que um evento "otp.sent" foi publicado no exchange "auth.events" (VS-Identity)
    And que o evento contém dados válidos:
      | campo     | valor            |
      | userEmail | teste@example.com|
      | otpCode   | 123456           |
      | channel   | EMAIL            |
      | purpose   | REGISTRATION     |
    And que o evento está na fila "transactional.auth-otp-sent.queue"
    When o Transactional Messaging Service consome o evento da fila
    Then o evento deve ser processado com sucesso
    And o SendOtpUseCase deve ser executado
    And o template de email OTP deve ser aplicado corretamente
    And o template deve conter o código OTP "123456"
    And o email deve ser enviado via SendGrid (simulado com header "simulate-provider: true")
    And a mensagem deve ser persistida no banco com:
      | campo              | valor                              |
      | status             | SENT                               |
      | channel            | EMAIL                              |
      | recipient          | teste@example.com                  |
      | messageType        | OTP                                |
      | subfluxo           | TRANSACTIONAL                      |
    And a mensagem deve conter o "providerMessageId" retornado pelo provider
    And o evento "delivery.tracking.created.v1" deve ser publicado no RabbitMQ (exchange "delivery-tracker.events")

  @otp @whatsapp @integration @not_implemented
  Scenario: Consumir evento otp.sent e processar envio de OTP via WhatsApp
    Given que um evento "otp.sent" foi publicado no exchange "auth.events" (VS-Identity)
    And que o evento contém dados válidos:
      | campo     | valor            |
      | userPhone | +5511999998888   |
      | otpCode   | 654321           |
      | channel   | WHATSAPP         |
      | purpose   | REGISTRATION     |
    And que o evento está na fila "transactional.auth-otp-sent.queue"
    When o Transactional Messaging Service consome o evento da fila
    Then o evento deve ser processado com sucesso
    And o SendOtpUseCase deve ser executado
    And o template de WhatsApp OTP deve ser aplicado corretamente
    And o template deve conter o código OTP "654321"
    And a mensagem WhatsApp deve ser enviada via Meta Business API (simulado)
    And a mensagem deve ser persistida no banco com status "SENT"
    And o evento "delivery.tracking.created.v1" deve ser publicado no RabbitMQ

  @otp @validation @integration
  Scenario: Rejeitar evento otp.sent com dados inválidos (email ausente)
    Given que um evento "otp.sent" foi publicado no exchange "auth.events"
    And que o evento contém dados inválidos:
      | campo     | valor            |
      | channel   | EMAIL            |
      | otpCode   | 123456           |
      | purpose   | REGISTRATION    |
      # userEmail ausente
    And que o evento está na fila "transactional.auth-otp-sent.queue"
    When o Transactional Messaging Service consome o evento da fila
    Then o evento deve ser rejeitado
    And o email não deve ser enviado
    And a mensagem não deve ser persistida
    And o evento deve ser movido para DLQ ou Parking Lot
    And um log de erro deve ser registrado

  @otp @validation @integration
  Scenario: Rejeitar evento otp.sent com canal inválido
    Given que um evento "otp.sent" foi publicado no exchange "auth.events"
    And que o evento contém dados inválidos:
      | campo     | valor            |
      | userEmail | teste@example.com|
      | channel   | INVALID_CHANNEL  |  # Canal inválido
      | otpCode   | 123456           |
      | purpose   | REGISTRATION    |
    And que o evento está na fila "transactional.auth-otp-sent.queue"
    When o Transactional Messaging Service consome o evento da fila
    Then o evento deve ser rejeitado
    And o email não deve ser enviado
    And o evento deve ser movido para DLQ ou Parking Lot

  @otp @idempotency @integration
  Scenario: Idempotência no processamento de evento otp.sent (evento duplicado)
    Given que um evento "otp.sent" foi processado anteriormente com sucesso
    And que o evento contém:
      | campo     | valor            |
      | eventId   | {eventId}        |
      | userEmail | teste@example.com|
      | otpCode   | 123456           |
      | channel   | EMAIL            |
    And que o mesmo evento é publicado novamente na fila "transactional.auth-otp-sent.queue"
    When o Transactional Messaging Service consome o evento duplicado
    Then o sistema deve detectar que o evento já foi processado
    And o email não deve ser enviado novamente
    And a mensagem não deve ser duplicada no banco
    And o evento deve ser marcado como já processado

  @otp @retry @integration
  Scenario: Retry automático após falha temporária no envio de email
    Given que um evento "otp.sent" foi publicado no exchange "auth.events"
    And que o evento contém dados válidos:
      | campo     | valor            |
      | userEmail | teste@example.com|
      | otpCode   | 123456           |
      | channel   | EMAIL            |
    And que o SendGrid está temporariamente indisponível (timeout)
    When o Transactional Messaging Service consome o evento da fila
    Then o sistema deve tentar enviar o email
    And após falha, o sistema deve fazer retry automático
    And após sucesso, o email deve ser enviado (simulado)
    And a mensagem deve ser persistida com status "SENT"
    And o número de tentativas deve ser registrado

  @otp @dlq @integration
  Scenario: Evento otp.sent movido para DLQ após falhas repetidas
    Given que um evento "otp.sent" foi publicado no exchange "auth.events"
    And que o evento contém dados válidos mas o provider está permanentemente indisponível
    And que o evento está na fila "transactional.auth-otp-sent.queue"
    When o Transactional Messaging Service tenta processar o evento
    And todas as tentativas de retry falham
    Then após exceder o número máximo de tentativas
    And o evento deve ser movido para DLQ "transactional.auth-otp-sent.queue.dlq"
    And o evento não deve ser perdido
    And um alerta deve ser gerado para monitoramento
