@implemented @vs-identity @cross-vs @vs-customer-communications @segment_1 @j1.1 @b2c @otp @registration @critical @api @messaging @integration @event-driven @e2e
Feature: Envio de OTP via Email - Fluxo Cross-VS Completo (REGISTRATION)
  Como um usuário
  Eu quero receber OTP via email durante o registro
  Para validar minha identidade de forma segura

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando
    And que tenho dados de teste únicos

  @otp_request @cross-vs-email
  Scenario: Envio de OTP via Email - Fluxo Cross-VS Completo (REGISTRATION)
    Given que estou na tela de registro
    When eu solicito OTP via "EMAIL" para "REGISTRATION"
    Then a solicitação de OTP deve retornar status 200
    And o código OTP deve estar presente na resposta
    # Validação Cross-VS - Publicação de Evento
    And o evento "otp.sent" deve ser publicado
    And o evento "otp.sent" deve conter o header "simulate-provider" com valor "true"
    # Validação Cross-VS - Consumo e Processamento
    And o Transactional Messaging Service (VS-Customer-Communications) deve consumir o evento da fila "transactional.auth-otp-sent.queue"
    And o SendOtpUseCase deve ser executado com sucesso
    And o template de email OTP deve ser aplicado corretamente
    And o email deve ser enviado via SendGrid (simulado com header "simulate-provider: true")
    And a mensagem deve ser persistida no banco com status "SENT"
    # Validação Cross-VS - Integração com Delivery Tracker
    And o evento "delivery.tracking.created.v1" deve ser publicado no RabbitMQ (exchange "delivery-tracker.events")
    And o Delivery Tracker Service deve consumir o evento e criar tracking inicial
    And o tracking deve conter:
      | campo              | valor                              |
      | channel            | EMAIL                             |
      | subfluxo           | TRANSACTIONAL                     |
      | serviceName        | transactional-messaging-service   |
