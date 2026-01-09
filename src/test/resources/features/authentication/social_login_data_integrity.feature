@implemented @vs-identity @segment_1 @j1.2 @authentication @social_login @data_integrity @critical @e2e
Feature: Integridade de Dados no Login Social (Fallback e Eventos RabbitMQ)
  Como um desenvolvedor/testador
  Eu quero garantir que a integridade dos dados é mantida mesmo quando o fallback é usado
  Para validar que o OTP sempre referencia o UUID correto do identity-service

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando

  # Cenário 1: Integridade de dados quando fallback é usado (evento RabbitMQ atrasado)
  # Este cenário valida que mesmo quando o usuário é criado via fallback no auth-service
  # (antes do evento RabbitMQ ser processado), o OTP sempre referencia o UUID correto
  # do identity-service (fonte de verdade)
  Scenario: Integridade de dados mantida quando fallback cria usuário antes do evento RabbitMQ
    Given que não existe usuário com email "teste.integridade.fallback@example.com"
    When eu inicio login social com provider "GOOGLE" e redirect_uri "http://localhost:3000/auth/callback"
    And o provider retorna autorização bem-sucedida
    And que estou usando um device novo (nunca visto antes)
    Then o login social deve retornar status "pending_otp"
    And eu devo receber um pendingOtpId no redirect
    And o usuário deve ser criado no identity-service com UUID válido
    And o usuário deve existir no auth-service (via fallback ou evento)
    And o OTP deve ter user_uuid igual ao UUID do identity-service
    And o evento "user.created.v1" deve ser publicado (eventual consistency)
    When o evento RabbitMQ "user.created.v1" é processado
    Then o usuário no auth-service não deve ser duplicado (idempotência)
    And o OTP deve continuar válido após processamento do evento
    When eu valido o OTP fornecido
    Then o login social deve ser completado
    And eu devo receber um JWT válido no redirect
    And o JWT deve conter o userId correto (mesmo UUID do identity-service)

  # Cenário 2: Integridade de dados quando evento RabbitMQ chega antes (fluxo normal)
  # Este cenário valida que quando o evento RabbitMQ chega a tempo, não há necessidade
  # de fallback, mas a integridade ainda é mantida
  Scenario: Integridade de dados mantida quando evento RabbitMQ chega antes do OTP
    Given que não existe usuário com email "teste.integridade.normal@example.com"
    When eu inicio login social com provider "GOOGLE" e redirect_uri "http://localhost:3000/auth/callback"
    And o provider retorna autorização bem-sucedida
    And o evento RabbitMQ "user.created.v1" é processado antes de criar OTP
    And que estou usando um device novo (nunca visto antes)
    Then o login social deve retornar status "pending_otp"
    And eu devo receber um pendingOtpId no redirect
    And o usuário deve ser criado no identity-service com UUID válido
    And o usuário deve existir no auth-service (via evento RabbitMQ)
    And o OTP deve ter user_uuid igual ao UUID do identity-service
    When eu valido o OTP fornecido
    Then o login social deve ser completado
    And eu devo receber um JWT válido no redirect
    And o JWT deve conter o userId correto (mesmo UUID do identity-service)

  # Cenário 3: Validação de idempotência do evento RabbitMQ após fallback
  # Este cenário valida que o evento RabbitMQ é idempotente e não cria duplicatas
  # mesmo quando o usuário já foi criado via fallback
  Scenario: Evento RabbitMQ é idempotente após criação via fallback
    Given que não existe usuário com email "teste.idempotencia@example.com"
    When eu inicio login social com provider "GOOGLE" e redirect_uri "http://localhost:3000/auth/callback"
    And o provider retorna autorização bem-sucedida
    And que estou usando um device novo (nunca visto antes)
    Then o login social deve retornar status "pending_otp"
    And o usuário deve existir no auth-service (via fallback)
    And o OTP deve ter user_uuid igual ao UUID do identity-service
    When o evento RabbitMQ "user.created.v1" é processado múltiplas vezes
    Then o usuário no auth-service não deve ser duplicado
    And deve existir apenas um usuário com o UUID do identity-service
    And o OTP deve continuar válido
    And o OTP deve continuar referenciando o mesmo user_uuid

