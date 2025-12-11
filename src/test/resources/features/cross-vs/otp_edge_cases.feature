@implemented @vs-identity @cross-vs @vs-customer-communications @segment_1 @edge-case @critical @api @messaging @e2e
Feature: Edge Cases - Fluxos Cross-VS (VS-Identity → VS-Customer-Communications)
  Como um testador E2E
  Eu quero validar cenários extremos em fluxos cross-VS
  Para garantir resiliência e confiabilidade do sistema

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando
    And que tenho dados de teste únicos

  @edge-case @concurrency @cross-vs
  Scenario: Múltiplos OTPs simultâneos - Processamento assíncrono correto
    Given que estou na tela de registro
    When eu solicito OTP via "EMAIL" para "REGISTRATION"
    And aguardo 1 segundo
    And eu solicito OTP via "EMAIL" para "REGISTRATION"
    And aguardo 1 segundo
    And eu solicito OTP via "EMAIL" para "REGISTRATION"
    Then todas as solicitações de OTP devem retornar status 200
    And 3 eventos "otp.sent" devem ser publicados no RabbitMQ
    And o Transactional Messaging Service deve processar todos os 3 eventos
    And 3 emails devem ser enviados (simulados)
    And cada email deve conter um OTP diferente
    And nenhum evento deve ser perdido ou duplicado

  @edge-case @failure @partial @cross-vs
  Scenario: Falha no Transactional Messaging Service - Evento deve ir para DLQ
    Given que estou na tela de registro
    And que o Transactional Messaging Service está indisponível
    When eu solicito OTP via "EMAIL" para "REGISTRATION"
    Then a solicitação de OTP deve retornar status 200
    And o evento "otp.sent" deve ser publicado no RabbitMQ
    And o evento deve ficar na fila "transactional.auth-otp-sent.queue"
    And após TTL configurado, o evento deve ser movido para DLQ "transactional.auth-otp-sent.queue.dlq"
    And o evento não deve ser perdido

  @edge-case @timeout @cross-vs
  Scenario: Timeout no envio de email - Retry automático
    Given que estou na tela de registro
    And que o SendGrid está com latência alta (timeout simulado)
    When eu solicito OTP via "EMAIL" para "REGISTRATION"
    Then a solicitação de OTP deve retornar status 200
    And o evento "otp.sent" deve ser publicado no RabbitMQ
    And o Transactional Messaging Service deve tentar processar o evento
    And após timeout, o sistema deve fazer retry automático
    And após sucesso, o email deve ser enviado (simulado)
    And a mensagem deve ser persistida com status "SENT"

  @edge-case @event-order @cross-vs
  Scenario: Múltiplos eventos OTP - Ordem de processamento preservada
    Given que estou na tela de registro
    When eu solicito OTP via "EMAIL" para "REGISTRATION"
    And aguardo 3 segundos
    And eu solicito OTP via "EMAIL" para "REGISTRATION"
    Then 2 eventos "otp.sent" devem ser publicados no RabbitMQ
    And o Transactional Messaging Service deve processar os eventos na ordem de publicação
    And o primeiro OTP enviado deve ser o primeiro a ser processado
    And o segundo OTP enviado deve ser o segundo a ser processado

  @edge-case @data-consistency @cross-vs
  Scenario: Consistência de dados entre VS-Identity e VS-Customer-Communications
    Given que estou na tela de registro
    When eu solicito OTP via "EMAIL" para "REGISTRATION"
    Then a solicitação de OTP deve retornar status 200
    And o evento "otp.sent" publicado deve conter dados consistentes:
      | campo     | origem                    |
      | userEmail | dados do usuário          |
      | channel   | EMAIL                     |
      | purpose   | REGISTRATION              |
    And o Transactional Messaging Service deve processar com os mesmos dados
    And a mensagem persistida deve conter os mesmos dados do evento
    And não deve haver divergência entre os dados do evento e da mensagem

  @edge-case @idempotency @cross-vs
  Scenario: Idempotência no processamento de eventos OTP
    Given que estou na tela de registro
    When eu solicito OTP via "EMAIL" para "REGISTRATION"
    Then a solicitação de OTP deve retornar status 200
    And o evento "otp.sent" deve ser publicado no RabbitMQ
    When o Transactional Messaging Service processa o evento pela primeira vez
    Then o email deve ser enviado (simulado)
    And a mensagem deve ser persistida
    When o mesmo evento é processado novamente (replay)
    Then o sistema deve detectar que o evento já foi processado
    And o email não deve ser enviado novamente
    And a mensagem não deve ser duplicada no banco

  @edge-case @rate-limiting @cross-vs
  Scenario: Rate limiting no envio de OTP - Múltiplas requisições
    Given que estou na tela de registro
    When eu solicito OTP via "EMAIL" para "REGISTRATION"
    And eu solicito OTP via "EMAIL" para "REGISTRATION"
    And eu solicito OTP via "EMAIL" para "REGISTRATION"
    And eu solicito OTP via "EMAIL" para "REGISTRATION"
    And eu solicito OTP via "EMAIL" para "REGISTRATION"
    And eu solicito OTP via "EMAIL" para "REGISTRATION"
    And eu solicito OTP via "EMAIL" para "REGISTRATION"
    And eu solicito OTP via "EMAIL" para "REGISTRATION"
    And eu solicito OTP via "EMAIL" para "REGISTRATION"
    And eu solicito OTP via "EMAIL" para "REGISTRATION"
    Then algumas solicitações de OTP devem retornar status 429
    And o número de eventos "otp.sent" publicados deve respeitar o rate limit
    And o Transactional Messaging Service deve processar apenas os eventos permitidos
    And o sistema deve proteger contra abuso de envio de OTP
