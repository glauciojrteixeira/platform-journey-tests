@implemented @vs-identity @cross-vs @vs-customer-communications @segment_1 @j1.3 @b2c @otp @password-recovery @critical @api @messaging @integration @event-driven @e2e
Feature: Envio de OTP via Email - Fluxo Cross-VS Completo (PASSWORD_RECOVERY)
  Como um usuário
  Eu quero receber OTP via email para recuperação de senha
  Para redefinir minha senha de forma segura

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando
    And que tenho dados de teste únicos
    And que crio um usuário com esses dados

  @otp_request @cross-vs-email @password-recovery
  Scenario: Envio de OTP via Email - Fluxo Cross-VS Completo (PASSWORD_RECOVERY)
    Given que existe um usuário com email do usuário criado
    When eu solicito OTP via "EMAIL" para "PASSWORD_RECOVERY"
    Then a solicitação de OTP deve retornar status 200
    And o código OTP deve estar presente na resposta
    # Validação Cross-VS - Publicação de Evento
    And o evento "otp.sent" deve ser publicado
    And o evento "otp.sent" deve conter o header "simulate-provider" com valor "true"
    # Validação Cross-VS - Consumo e Processamento
    And o Transactional Messaging Service (VS-Customer-Communications) deve consumir o evento da fila "transactional.auth-otp-sent.queue"
    And o SendOtpUseCase deve ser executado com sucesso
    And o template de recuperação de senha deve ser aplicado
    And o email deve ser enviado via SendGrid (simulado com header "simulate-provider: true")
    And a mensagem deve ser persistida no banco com status "SENT"
    # Validação Cross-VS - Integração com Delivery Tracker
    And o evento "delivery.tracking.created.v1" deve ser publicado no RabbitMQ (exchange "delivery-tracker.events")
    And o Delivery Tracker Service deve consumir o evento e criar tracking inicial
    And o Delivery Tracker Service deve registrar o envio
