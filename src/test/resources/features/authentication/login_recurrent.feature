@implemented @vs-identity @segment_1 @j1.3 @authentication @critical @e2e
Feature: Login Recorrente
  Como um comprador ocasional
  Eu quero fazer login recorrente na plataforma
  Para acessar meus arremates rapidamente

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando

  Scenario: Login recorrente com token válido
    Given que já estou autenticado na plataforma
    And meu token JWT ainda é válido
    When eu acesso a plataforma
    Then eu devo continuar autenticado sem precisar fazer login novamente
    And meu token deve ser renovado automaticamente se necessário

  Scenario: Login recorrente com token expirado
    Given que já estou autenticado na plataforma
    And meu token JWT expirou
    When eu acesso a plataforma
    Then o sistema deve solicitar reautenticação
    When eu faço login novamente
    Then eu devo receber um novo JWT válido

  Scenario: Login recorrente via login social
    Given que me registrei via login social
    When eu acesso a plataforma novamente
    And escolho login social
    Then o login deve ser rápido (sem reCAPTCHA)
    And eu devo receber um JWT válido

