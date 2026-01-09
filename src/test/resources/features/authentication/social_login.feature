@implemented @vs-identity @segment_1 @j1.2 @authentication @social_login @critical @e2e
Feature: Login Social (OAuth2) para Compradores Ocasionais
  Como um comprador ocasional
  Eu quero fazer login usando minha conta Google
  Para acessar a plataforma de forma rápida e segura

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando

  # Cenário 1: Login social bem-sucedido - Usuário novo (primeira vez)
  Scenario: Login social bem-sucedido com usuário novo
    Given que não existe usuário com email "novo.usuario.social@example.com"
    When eu inicio login social com provider "GOOGLE" e redirect_uri "http://localhost:3000/auth/callback"
    And o provider retorna autorização bem-sucedida
    Then o login social deve ser bem-sucedido
    And eu devo receber um JWT válido no redirect
    And um novo usuário deve ser criado sem documento
    And uma credencial social deve ser criada para o provider "GOOGLE"
    And o evento "user.created.v1" deve ser publicado
    And o evento "credentials.provisioned.v1" deve ser publicado

  # Cenário 2: Login social bem-sucedido - Usuário existente com credencial social
  Scenario: Login social bem-sucedido com usuário existente que já tem credencial social
    Given que tenho dados de teste únicos
    And que crio um usuário com esses dados
    And que o usuário tem credencial social para provider "GOOGLE"
    When eu inicio login social com provider "GOOGLE" e redirect_uri "http://localhost:3000/auth/callback"
    And o provider retorna autorização bem-sucedida
    Then o login social deve ser bem-sucedido
    And eu devo receber um JWT válido no redirect
    And nenhum novo usuário deve ser criado
    And o evento "user.created.v1" não deve ser publicado

  # Cenário 3: Login social com device novo - OTP obrigatório
  Scenario: Login social com device novo requer OTP
    Given que tenho dados de teste únicos
    And que crio um usuário com esses dados
    And que o usuário tem credencial social para provider "GOOGLE"
    And que estou usando um device novo
    When eu inicio login social com provider "GOOGLE" e redirect_uri "http://localhost:3000/auth/callback"
    And o provider retorna autorização bem-sucedida
    Then o login social deve retornar status "pending_otp"
    And eu devo receber um pendingOtpId no redirect
    When eu valido o OTP fornecido
    Then o login social deve ser completado
    And eu devo receber um JWT válido no redirect

  # Cenário 4: Login social com redirect_uri inválido
  Scenario: Login social falha com redirect_uri inválido
    When eu inicio login social com provider "GOOGLE" e redirect_uri "https://evil.com/callback"
    Then o login social deve falhar com status 400
    And o erro deve ser "INVALID_REDIRECT_URI"
    And a mensagem de erro deve conter "redirect_uri não permitido"

  # Cenário 5: Login social com provider inválido
  Scenario: Login social falha com provider inválido
    When eu inicio login social com provider "INVALID_PROVIDER" e redirect_uri "http://localhost:3000/auth/callback"
    Then o login social deve falhar com status 400
    And o erro deve ser "INVALID_PROVIDER"
    And a mensagem de erro deve conter "provider não suportado"

  # Cenário 6: Callback com state inválido
  Scenario: Callback OAuth2 falha com state inválido
    Given que eu iniciei login social com provider "GOOGLE"
    When o callback OAuth2 é recebido com state "invalid_state"
    Then o callback deve falhar com status 400
    And o erro deve ser "INVALID_STATE"
    And a mensagem de erro deve conter "state inválido ou expirado"

  # Cenário 7: Callback com code inválido
  Scenario: Callback OAuth2 falha com code inválido
    Given que eu iniciei login social com provider "GOOGLE"
    When o callback OAuth2 é recebido com code "invalid_code"
    Then o callback deve falhar com status 400
    And o erro deve ser "INVALID_AUTHORIZATION_CODE"
    And a mensagem de erro deve conter "código de autorização inválido"

  # Cenário 8: Provider retorna erro (usuário cancelou)
  Scenario: Login social falha quando usuário cancela no provider
    Given que eu iniciei login social com provider "GOOGLE"
    When o provider retorna erro "access_denied"
    Then o callback deve retornar status "error"
    And o código de erro deve ser "USER_CANCELLED"
    And o redirect deve conter o código de erro no fragment

  # Cenário 9: Login social com state expirado
  Scenario: Login social falha com state expirado
    Given que eu iniciei login social com provider "GOOGLE"
    And o state expira (TTL de 10 minutos)
    When o callback OAuth2 é recebido após expiração
    Then o callback deve falhar com status 400
    And o erro deve ser "INVALID_STATE"
    And a mensagem de erro deve conter "state expirado"

