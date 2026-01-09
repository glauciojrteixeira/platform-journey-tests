# Social Login via Relay (Clientes Externos)
# 
# Este feature testa o login social através do módulo RELAY, simulando clientes externos.
# Conforme playbook 001.00, clientes externos devem passar pelo relay que fornece:
# - Rate Limiting
# - Circuit Breaker
# - Cache
# - Propagação de country-code
#
# Tags:
# - @relay: Testa através do relay (simula cliente externo)
# - @social-login: Feature de login social
# - @oauth2: Usa OAuth2 Authorization Code Flow

@relay @social-login @oauth2
Feature: Login Social via Relay (Clientes Externos)
  Como um cliente externo
  Eu quero fazer login social através do relay
  Para que eu tenha proteção de rate limiting, circuit breaker e cache

  Background:
    Dado que o provider "GOOGLE" está configurado
    E que o redirect_uri "http://localhost:3000/callback" está na whitelist

  @smoke @happy-path
  Scenario: Login social bem-sucedido via relay com usuário novo
    Quando eu inicio login social via relay com provider "GOOGLE" e redirect_uri "http://localhost:3000/callback"
    Então o login social via relay deve ser bem-sucedido
    E eu devo receber um redirect para o provider OAuth2

  @smoke @happy-path
  Scenario: Login social bem-sucedido via relay com usuário existente
    Dado que eu tenho uma conta existente com credencial social "GOOGLE"
    Quando eu inicio login social via relay com provider "GOOGLE" e redirect_uri "http://localhost:3000/callback"
    Então o login social via relay deve ser bem-sucedido
    E eu devo receber um JWT válido no redirect

  @mfa @security
  Scenario: Login social via relay com device novo requer OTP
    Dado que eu tenho uma conta existente com credencial social "GOOGLE"
    E que estou usando um device novo (nunca visto antes)
    Quando eu inicio login social via relay com provider "GOOGLE" e redirect_uri "http://localhost:3000/callback"
    E eu processar o callback OAuth2 via relay com code válido
    Então o login social via relay deve retornar status "pending_otp"
    E eu devo receber um pendingOtpId
    Quando eu valido o OTP via relay com código válido
    Então eu devo receber um JWT válido

  @error-handling
  Scenario: Login social via relay falha com redirect_uri inválido
    Quando eu inicio login social via relay com provider "GOOGLE" e redirect_uri "http://evil.com/callback"
    Então o login social via relay deve falhar com status 400
    E a mensagem de erro deve conter "Redirect URI is not allowed"

  @error-handling
  Scenario: Login social via relay falha com provider inválido
    Quando eu inicio login social via relay com provider "INVALID" e redirect_uri "http://localhost:3000/callback"
    Então o login social via relay deve falhar com status 400
    E a mensagem de erro deve conter "unsupported_provider"

  @rate-limiting @relay-specific
  Scenario: Rate limiting via relay funciona corretamente
    Dado que eu faço 100 requisições consecutivas via relay
    Quando eu faço mais uma requisição via relay
    Então a requisição via relay deve falhar com status 429
    E a mensagem deve indicar rate limit excedido

  @cache @relay-specific
  Scenario: Cache via relay funciona para requisições GET
    Quando eu inicio login social via relay com provider "GOOGLE" e redirect_uri "http://localhost:3000/callback"
    E eu faço a mesma requisição novamente via relay
    Então a segunda requisição via relay deve usar cache
    E o tempo de resposta deve ser menor

