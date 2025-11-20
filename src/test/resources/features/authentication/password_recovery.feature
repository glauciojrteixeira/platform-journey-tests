@implemented @segment_1 @j1.6 @password @recovery @medium @e2e
Feature: Recuperação de Senha
  Como um usuário que esqueceu a senha
  Eu quero recuperar minha senha
  Para poder acessar minha conta novamente

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando
    And que tenho dados de teste únicos
    And que crio um usuário com esses dados

  @not_implemented @otp_service_missing
  Scenario: Recuperação de senha bem-sucedida com OTP
    Given que esqueci minha senha
    When eu solicito recuperação de senha para o email do usuário criado
    And eu recebo o código OTP
    And eu valido o OTP recebido via WhatsApp
    Then eu devo conseguir redefinir minha senha
    And o evento "otp.validated" deve ser publicado

