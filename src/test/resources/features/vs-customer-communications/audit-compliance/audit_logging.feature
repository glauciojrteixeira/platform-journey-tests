@not_implemented @vs-customer-communications @integration @audit-compliance @high @messaging @event-driven @e2e
Feature: Audit Compliance Service - Integração (Quando Implementado)
  Como um testador de integração
  Eu quero validar o registro de logs de auditoria
  Para garantir conformidade e rastreabilidade

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando

  @audit-compliance @transactional-messaging @not_implemented
  Scenario: Audit Compliance recebe evento MESSAGE_SENT do Transactional Messaging
    Given que o Transactional Messaging Service enviou uma mensagem OTP com sucesso
    And que o evento "MESSAGE_SENT" foi publicado no exchange "audit-events-exchange"
    And que o evento contém:
      | campo              | valor                              |
      | eventType          | MESSAGE_SENT                       |
      | messageId         | {messageId}                        |
      | userId             | {userId}                           |
      | channel            | EMAIL                              |
      | messageType        | OTP                                |
      | serviceName        | transactional-messaging-service   |
      | subfluxo           | TRANSACTIONAL                      |
    And que o evento está na fila "audit-events"
    When o Audit Compliance Service consome o evento da fila
    Then o evento deve ser processado com sucesso
    And um log de auditoria deve ser criado no banco de dados
    And o log deve ser imutável
    And o log deve conter todos os dados do evento
    And o log deve estar disponível para consulta de compliance

  @audit-compliance @delivery-tracker @not_implemented
  Scenario: Audit Compliance recebe evento MESSAGE_DELIVERED do Delivery Tracker
    Given que o Delivery Tracker atualizou o status de uma mensagem para "DELIVERED"
    And que o evento "MESSAGE_DELIVERED" foi publicado no exchange "audit-events-exchange"
    And que o evento contém:
      | campo              | valor                              |
      | eventType          | MESSAGE_DELIVERED                  |
      | messageId         | {messageId}                        |
      | userId             | {userId}                           |
      | channel            | EMAIL                              |
      | serviceName        | delivery-tracker-service           |
    And que o evento está na fila "audit-events"
    When o Audit Compliance Service consome o evento da fila
    Then o evento deve ser processado com sucesso
    And um log de auditoria deve ser criado no banco de dados
    And o log deve registrar a entrega da mensagem
    And o log deve estar disponível para consulta de compliance
