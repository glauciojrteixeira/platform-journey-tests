@implemented @vs-customer-communications @integration @edge-case @critical @messaging @e2e
Feature: Edge Cases - Integração VS-Customer-Communications
  Como um testador de integração
  Eu quero validar cenários extremos em integrações
  Para garantir resiliência e confiabilidade do sistema

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando

  @edge-case @concurrency @integration
  Scenario: Processar múltiplos eventos otp.sent simultaneamente
    Given que 5 eventos "otp.sent" foram publicados simultaneamente na fila "transactional.auth-otp-sent.queue"
    And que cada evento contém dados válidos diferentes
    When o Transactional Messaging Service processa os eventos
    Then todos os 5 eventos devem ser processados com sucesso
    And 5 emails devem ser enviados (simulados)
    And cada email deve conter o OTP correto
    And nenhum evento deve ser perdido ou duplicado
    And a ordem de processamento deve ser preservada

  @edge-case @partial-failure @integration
  Scenario: Falha parcial - Alguns eventos processados, outros não
    Given que 3 eventos "otp.sent" foram publicados na fila
    And que o primeiro evento contém dados válidos
    And que o segundo evento contém dados inválidos
    And que o terceiro evento contém dados válidos
    When o Transactional Messaging Service processa os eventos
    Then o primeiro evento deve ser processado com sucesso
    And o segundo evento deve ser rejeitado e movido para DLQ
    And o terceiro evento deve ser processado com sucesso
    And apenas 2 emails devem ser enviados (simulados)
    And o sistema deve continuar processando eventos válidos

  @edge-case @event-order @integration
  Scenario: Preservar ordem de eventos mesmo com retries
    Given que 2 eventos "otp.sent" foram publicados sequencialmente na fila
    And que o primeiro evento falha temporariamente (requer retry)
    And que o segundo evento é processado com sucesso
    When o sistema faz retry do primeiro evento
    Then o primeiro evento deve ser processado após o retry
    And a ordem lógica dos eventos deve ser preservada
    And ambos os emails devem ser enviados (simulados)

  @edge-case @data-consistency @integration
  Scenario: Consistência de dados entre Transactional Messaging e Delivery Tracker
    Given que o Transactional Messaging Service enviou uma mensagem OTP
    And que o evento "delivery.tracking.created.v1" foi publicado
    When o Delivery Tracker Service processa o evento
    Then os dados do tracking devem ser consistentes com os dados da mensagem:
      | campo              | origem                            |
      | messageId         | Transactional Messaging            |
      | channel            | Transactional Messaging            |
      | providerId         | Transactional Messaging            |
      | providerMessageId  | Transactional Messaging            |
    And não deve haver divergência entre os dados
    And consultas em ambos os serviços devem retornar dados consistentes

  @edge-case @rate-limiting @integration
  Scenario: Rate limiting no processamento de eventos
    Given que 100 eventos "otp.sent" foram publicados na fila simultaneamente
    When o Transactional Messaging Service processa os eventos
    Then o sistema deve respeitar o rate limit configurado
    And apenas o número permitido de emails deve ser enviado por minuto
    And os eventos restantes devem permanecer na fila para processamento posterior
    And o sistema deve proteger contra sobrecarga

  @edge-case @timeout @integration
  Scenario: Timeout no processamento de evento - Retry e DLQ
    Given que um evento "otp.sent" foi publicado na fila
    And que o processamento do evento excede o timeout configurado
    When o Transactional Messaging Service tenta processar o evento
    Then após timeout, o sistema deve fazer retry
    And se o retry também falhar por timeout, o evento deve ser movido para DLQ
    And o evento não deve ser perdido
    And um alerta deve ser gerado para monitoramento
