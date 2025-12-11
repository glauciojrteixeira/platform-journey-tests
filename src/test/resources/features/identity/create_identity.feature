@implemented @vs-identity @segment_1 @j1.1 @identity @critical @e2e
Feature: Criação de Identidade
  Como um novo usuário
  Eu quero criar minha identidade na plataforma
  Para ter acesso aos serviços

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando

  Scenario: Criação de identidade bem-sucedida
    Given que estou na tela de registro
    When eu informo:
      | campo      | valor                    |
      | nome       | João Silva               |
      | cpf        | 12345678901              |
      | email      | joao.silva@example.com    |
      | telefone   | +5511999998888            |
    And eu envio os dados para criar identidade
    Then a identidade deve ser criada com sucesso
    And a identidade deve ser criada no Identity Service

