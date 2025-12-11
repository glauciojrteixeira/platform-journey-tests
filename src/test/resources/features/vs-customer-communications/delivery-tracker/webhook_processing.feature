@implemented @vs-customer-communications @integration @delivery-tracker @critical @messaging @event-driven @e2e
Feature: Delivery Tracker Service - Integração com Transactional Messaging e Webhooks
  Como um testador de integração
  Eu quero validar o rastreamento de entrega de mensagens
  Para garantir que o status de entrega é atualizado corretamente

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando

  @delivery-tracker @transactional-messaging @integration
  Scenario: Delivery Tracker recebe evento de tracking criado pelo Transactional Messaging
    Given que o Transactional Messaging Service enviou uma mensagem OTP com sucesso
    And que o evento "delivery.tracking.created.v1" foi publicado no exchange "delivery-tracker.events"
    And que o evento contém:
      | campo              | valor                              |
      | messageId         | {messageId}                        |
      | channel            | EMAIL                              |
      | subfluxo           | TRANSACTIONAL                      |
      | serviceName        | transactional-messaging-service   |
      | providerId         | sendgrid                           |
      | providerMessageId  | sg-xxx                             |
    And que o evento está na fila "delivery-tracker.delivery-tracking-created.queue"
    When o Delivery Tracker Service consome o evento da fila
    Then o evento deve ser processado com sucesso
    And um registro de tracking deve ser criado no banco de dados
    And o tracking deve conter:
      | campo              | valor                              |
      | messageId         | {messageId}                        |
      | status             | SENT                               |
      | channel            | EMAIL                              |
      | providerId         | sendgrid                           |
      | providerMessageId  | sg-xxx                             |
    And o tracking deve estar pronto para receber webhooks do provider

  @delivery-tracker @webhook @integration
  Scenario: Delivery Tracker recebe webhook do SendGrid e atualiza status
    Given que existe um registro de tracking para mensagem com "providerMessageId" "sg-xxx"
    And que o tracking tem status inicial "SENT"
    When o SendGrid envia webhook HTTP para o endpoint do Delivery Tracker com:
      | campo              | valor            |
      | providerMessageId  | sg-xxx           |
      | event              | delivered        |
      | timestamp          | {timestamp}      |
    And os headers obrigatórios estão presentes
    Then o webhook deve ser recebido com sucesso (status 200)
    And o webhook deve ser normalizado
    And o evento "callback.received" deve ser publicado no RabbitMQ (exchange "delivery-tracker.delivery-callbacks")
    And o Delivery Tracker Service deve consumir o evento da fila "delivery-tracker.delivery-callbacks.queue"
    And o status do tracking deve ser atualizado para "DELIVERED"
    And o campo "deliveredAt" deve ser preenchido
    And o registro de tracking deve ser atualizado no banco de dados

  @delivery-tracker @webhook @validation @integration
  Scenario: Rejeitar webhook do SendGrid com assinatura inválida
    Given que existe um registro de tracking para mensagem com "providerMessageId" "sg-xxx"
    When o SendGrid envia webhook HTTP com assinatura inválida ou ausente
    Then o webhook deve ser rejeitado (status 401 ou 403)
    And o status do tracking não deve ser atualizado
    And um log de segurança deve ser registrado
    And o evento não deve ser publicado no RabbitMQ

  @delivery-tracker @webhook @validation @integration
  Scenario: Rejeitar webhook do SendGrid com providerMessageId inexistente
    Given que não existe um registro de tracking para "providerMessageId" "sg-inexistente"
    When o SendGrid envia webhook HTTP com:
      | campo              | valor            |
      | providerMessageId  | sg-inexistente   |
      | event              | delivered        |
    Then o webhook deve ser rejeitado (status 404)
    And um log de erro deve ser registrado
    And o evento não deve ser publicado no RabbitMQ

  @delivery-tracker @webhook @events @integration
  Scenario: Processar múltiplos webhooks do SendGrid para mesma mensagem (delivered, opened, clicked)
    Given que existe um registro de tracking para mensagem com "providerMessageId" "sg-xxx"
    And que o tracking tem status inicial "SENT"
    When o SendGrid envia webhook "delivered" para o Delivery Tracker
    Then o status do tracking deve ser atualizado para "DELIVERED"
    When o SendGrid envia webhook "opened" para o Delivery Tracker
    Then o status do tracking deve ser atualizado para "OPENED"
    And o campo "openedAt" deve ser preenchido
    When o SendGrid envia webhook "clicked" para o Delivery Tracker
    Then o status do tracking deve ser atualizado para "CLICKED"
    And o campo "clickedAt" deve ser preenchido
    And todos os eventos devem ser registrados no histórico do tracking
