@implemented @vs-identity @segment_1 @j1.2 @authentication @social_login @account_linking @critical @e2e
Feature: Account Linking - Vincular Conta Social a Usuário Existente
  Como um usuário que já tem conta na plataforma
  Eu quero vincular minha conta Google à minha conta existente
  Para poder fazer login social no futuro

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando

  # Cenário 1: Account linking bem-sucedido - Email já existe, OTP válido
  Scenario: Account linking bem-sucedido com OTP válido
    Given que tenho dados de teste únicos
    And que crio um usuário com esses dados
    And que o usuário NÃO tem credencial social para provider "GOOGLE"
    And o email do provider corresponde ao email do usuário existente
    When eu inicio login social com provider "GOOGLE" e redirect_uri "http://localhost:3000/auth/callback"
    And o provider retorna autorização bem-sucedida
    Then o login social deve retornar status "pending_linking"
    And eu devo receber um pendingLinkId no redirect
    When eu solicito OTP via "EMAIL" para "ACCOUNT_LINKING"
    And eu recebo o código OTP
    And eu valido o OTP para account linking com o código recebido
    Then o account linking deve ser completado
    And eu devo receber um JWT válido no redirect
    And a credencial social deve ser vinculada ao usuário existente
    And o evento "credentials.provisioned.v1" deve ser publicado

  # Cenário 2: Account linking falha com OTP inválido
  Scenario: Account linking falha com OTP inválido
    Given que tenho dados de teste únicos
    And que crio um usuário com esses dados
    And que o usuário NÃO tem credencial social para provider "GOOGLE"
    And o email do provider corresponde ao email do usuário existente
    When eu inicio login social com provider "GOOGLE" e redirect_uri "http://localhost:3000/auth/callback"
    And o provider retorna autorização bem-sucedida
    Then o login social deve retornar status "pending_linking"
    And eu devo receber um pendingLinkId no redirect
    When eu valido o OTP para account linking com código "000000"
    Then o account linking deve falhar com status 400
    And o erro deve ser "INVALID_OTP"
    And a mensagem de erro deve conter "código OTP inválido"
    And a credencial social NÃO deve ser vinculada

  # Cenário 3: Account linking falha com OTP expirado
  Scenario: Account linking falha com OTP expirado
    Given que tenho dados de teste únicos
    And que crio um usuário com esses dados
    And que o usuário NÃO tem credencial social para provider "GOOGLE"
    And o email do provider corresponde ao email do usuário existente
    When eu inicio login social com provider "GOOGLE" e redirect_uri "http://localhost:3000/auth/callback"
    And o provider retorna autorização bem-sucedida
    Then o login social deve retornar status "pending_linking"
    And eu devo receber um pendingLinkId no redirect
    And o OTP expira (TTL de 15 minutos)
    When eu valido o OTP para account linking após expiração
    Then o account linking deve falhar com status 400
    And o erro deve ser "OTP_EXPIRED"
    And a mensagem de erro deve conter "código OTP expirado"

  # Cenário 4: Account linking falha com pendingLinkId inválido
  Scenario: Account linking falha com pendingLinkId inválido
    When eu valido o OTP para account linking com pendingLinkId "invalid-uuid" e código "123456"
    Then o account linking deve falhar com status 404
    And o erro deve ser "PENDING_LINK_NOT_FOUND"
    And a mensagem de erro deve conter "pending account link não encontrado"

  # Cenário 5: Account linking com email diferente (não deve vincular)
  Scenario: Account linking não ocorre quando email do provider é diferente
    Given que tenho dados de teste únicos
    And que crio um usuário com esses dados
    And que o usuário NÃO tem credencial social para provider "GOOGLE"
    And o email do provider é diferente do email do usuário existente
    When eu inicio login social com provider "GOOGLE" e redirect_uri "http://localhost:3000/auth/callback"
    And o provider retorna autorização bem-sucedida
    Then um novo usuário deve ser criado (sem account linking)
    And eu devo receber um JWT válido no redirect
    And nenhum account linking deve ocorrer

