@implemented @segment_1 @j1.11 @otp @critical @e2e @bu-identity
Feature: Geração e Validação de OTP
  Como um usuário
  Eu quero solicitar e validar códigos OTP
  Para realizar operações que requerem verificação

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando
    And que tenho dados de teste únicos
    And que crio um usuário com esses dados

  @otp_request
  Scenario: Solicitação de OTP via EMAIL para REGISTRATION bem-sucedida
    When eu solicito OTP via "EMAIL" para "REGISTRATION"
    Then a solicitação de OTP deve retornar status 200
    And o evento "otp.sent" deve ser publicado
    And o código OTP deve estar presente na resposta

  @otp_request @not_implemented @otp_service_missing
  Scenario: Solicitação de OTP via WHATSAPP para REGISTRATION bem-sucedida
    Given que o usuário criado tem telefone configurado
    When eu solicito OTP via "WHATSAPP" para "REGISTRATION"
    Then a solicitação de OTP deve retornar status 200
    And o evento "otp.sent" deve ser publicado
    And o código OTP deve estar presente na resposta

  @otp_validation @not_implemented @otp_service_missing
  Scenario: Validação de OTP bem-sucedida
    When eu solicito OTP via "EMAIL" para "REGISTRATION"
    And eu recebo o código OTP
    And eu valido o OTP recebido
    Then a validação de OTP deve retornar status 200
    And o evento "otp.validated" deve ser publicado

  @otp_validation @not_implemented @otp_service_missing
  Scenario: Validação de OTP falha com código inválido
    When eu solicito OTP via "EMAIL" para "REGISTRATION"
    And eu recebo o código OTP
    And eu valido o OTP informando "000000"
    Then a validação de OTP deve retornar status 401
    And o evento "otp.validated" não deve ser publicado

  @otp_rate_limiting
  Scenario: Rate limiting impede múltiplas solicitações de OTP
    When eu solicito OTP via "EMAIL" para "REGISTRATION"
    And eu solicito OTP via "EMAIL" para "REGISTRATION"
    And eu solicito OTP via "EMAIL" para "REGISTRATION"
    And eu solicito OTP via "EMAIL" para "REGISTRATION"
    And eu solicito OTP via "EMAIL" para "REGISTRATION"
    And eu solicito OTP via "EMAIL" para "REGISTRATION"
    Then a última solicitação de OTP deve retornar status 429

