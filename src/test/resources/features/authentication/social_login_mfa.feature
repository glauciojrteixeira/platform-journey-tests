@implemented @vs-identity @segment_1 @j1.2 @authentication @social_login @mfa @critical @e2e
Feature: MFA Condicional no Login Social
  Como um usuário fazendo login social
  Eu quero que o sistema solicite OTP quando houver risco
  Para garantir segurança adicional

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando

  # Cenário 1: MFA obrigatório - Device novo
  Scenario: Login social requer OTP quando device é novo
    Given que tenho dados de teste únicos
    And que crio um usuário com esses dados
    And que o usuário tem credencial social para provider "GOOGLE"
    And que estou usando um device novo (nunca visto antes)
    When eu inicio login social com provider "GOOGLE" e redirect_uri "http://localhost:3000/auth/callback"
    And o provider retorna autorização bem-sucedida
    Then o login social deve retornar status "pending_otp"
    And eu devo receber um pendingOtpId no redirect
    And o motivo do OTP deve ser "NEW_DEVICE"
    When eu solicito OTP via "EMAIL" para "LOGIN"
    And eu recebo o código OTP
    And eu valido o OTP fornecido
    Then o login social deve ser completado
    And eu devo receber um JWT válido no redirect
    And o device deve ser registrado como "NEW"

  # Cenário 2: MFA obrigatório - País inesperado
  Scenario: Login social requer OTP quando país é inesperado
    Given que tenho dados de teste únicos
    And que crio um usuário com esses dados
    And que o usuário tem credencial social para provider "GOOGLE"
    And que o usuário sempre faz login do país "BR"
    And que estou fazendo login do país "MX" (inesperado)
    When eu inicio login social com provider "GOOGLE" e redirect_uri "http://localhost:3000/auth/callback"
    And o provider retorna autorização bem-sucedida
    Then o login social deve retornar status "pending_otp"
    And eu devo receber um pendingOtpId no redirect
    And o motivo do OTP deve ser "UNEXPECTED_COUNTRY"
    When eu valido o OTP fornecido
    Then o login social deve ser completado
    And eu devo receber um JWT válido no redirect

  # Cenário 3: MFA não obrigatório - Device confiável
  Scenario: Login social sem OTP quando device é confiável
    Given que tenho dados de teste únicos
    And que crio um usuário com esses dados
    And que o usuário tem credencial social para provider "GOOGLE"
    And que estou usando um device confiável (já usado antes)
    When eu inicio login social com provider "GOOGLE" e redirect_uri "http://localhost:3000/auth/callback"
    And o provider retorna autorização bem-sucedida
    Then o login social deve ser bem-sucedido
    And eu devo receber um JWT válido no redirect
    And nenhum OTP deve ser solicitado
    And o status não deve ser "pending_otp"

  # Cenário 4: MFA obrigatório - Múltiplos fatores de risco
  Scenario: Login social requer OTP quando há múltiplos fatores de risco
    Given que tenho dados de teste únicos
    And que crio um usuário com esses dados
    And que o usuário tem credencial social para provider "GOOGLE"
    And que estou usando um device novo
    And que estou fazendo login do país inesperado
    When eu inicio login social com provider "GOOGLE" e redirect_uri "http://localhost:3000/auth/callback"
    And o provider retorna autorização bem-sucedida
    Then o login social deve retornar status "pending_otp"
    And eu devo receber um pendingOtpId no redirect
    And o motivo do OTP deve conter "NEW_DEVICE" ou "UNEXPECTED_COUNTRY"
    When eu valido o OTP fornecido
    Then o login social deve ser completado
    And eu devo receber um JWT válido no redirect

  # Cenário 5: MFA falha com OTP inválido
  Scenario: MFA falha quando OTP é inválido
    Given que tenho dados de teste únicos
    And que crio um usuário com esses dados
    And que o usuário tem credencial social para provider "GOOGLE"
    And que estou usando um device novo
    When eu inicio login social com provider "GOOGLE" e redirect_uri "http://localhost:3000/auth/callback"
    And o provider retorna autorização bem-sucedida
    Then o login social deve retornar status "pending_otp"
    And eu devo receber um pendingOtpId no redirect
    When eu valido o OTP com código "000000" (inválido)
    Then o login social deve falhar com status 400
    And o erro deve ser "INVALID_OTP"
    And nenhum JWT deve ser emitido

  # Cenário 6: MFA falha com OTP expirado
  Scenario: MFA falha quando OTP expira
    Given que tenho dados de teste únicos
    And que crio um usuário com esses dados
    And que o usuário tem credencial social para provider "GOOGLE"
    And que estou usando um device novo
    When eu inicio login social com provider "GOOGLE" e redirect_uri "http://localhost:3000/auth/callback"
    And o provider retorna autorização bem-sucedida
    Then o login social deve retornar status "pending_otp"
    And eu devo receber um pendingOtpId no redirect
    And o OTP expira (TTL de 15 minutos)
    When eu valido o OTP após expiração
    Then o login social deve falhar com status 400
    And o erro deve ser "OTP_EXPIRED"
    And nenhum JWT deve ser emitido

