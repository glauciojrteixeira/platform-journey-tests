# Social Account Linking via Relay (Clientes Externos)
#
# Este feature testa o account linking através do módulo RELAY, simulando clientes externos.
# Conforme playbook 001.00, clientes externos devem passar pelo relay.

@relay @social-login @account-linking
Feature: Account Linking via Relay (Clientes Externos)
  Como um cliente externo
  Eu quero fazer linking de conta social através do relay
  Para que minha conta social seja vinculada à minha conta existente

  Background:
    Dado que o provider "GOOGLE" está configurado
    E que o redirect_uri "http://localhost:3000/callback" está na whitelist
    E que eu tenho uma conta existente sem credencial social

  @smoke @happy-path
  Scenario: Account linking bem-sucedido via relay com OTP válido
    Quando eu inicio login social via relay com provider "GOOGLE" e redirect_uri "http://localhost:3000/callback"
    E eu processar o callback OAuth2 via relay com code válido
    E o email do provider corresponde ao email da minha conta
    Então o login social via relay deve retornar status "pending_linking"
    E eu devo receber um pendingLinkId
    Quando eu completo o account linking via relay com OTP válido
    Então o account linking via relay deve ser bem-sucedido
    E eu devo receber um JWT válido

  @error-handling
  Scenario: Account linking via relay falha com OTP inválido
    Dado que eu tenho um pendingLinkId válido
    Quando eu completo o account linking via relay com OTP "000000"
    Então o account linking via relay deve falhar com status 400
    E a mensagem de erro deve conter "Invalid OTP"

  @error-handling
  Scenario: Account linking via relay falha com OTP expirado
    Dado que eu tenho um pendingLinkId válido
    E que o OTP expirou
    Quando eu completo o account linking via relay com OTP expirado
    Então o account linking via relay deve falhar com status 400
    E a mensagem de erro deve conter "OTP expired"

