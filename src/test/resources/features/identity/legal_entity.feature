# Entidade jurídica pode requerer autenticação para alguns endpoints
@implemented @segment_3 @j3.1 @legal_entity @b2b @high @e2e @partial
Feature: Registro de Entidade Jurídica para Revendedores
  Como representante legal de uma empresa
  Eu quero registrar minha empresa na plataforma
  Para gerenciar múltiplos usuários

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando

  # Cenário parcial - alguns passos podem requerer autenticação
  @partial @may_require_auth
  Scenario: Registro completo de PJ com representante legal
    Given que sou representante legal de uma empresa
    When eu informo os dados da empresa:
      | campo            | valor                    |
      | cnpj             | 12345678000190           |
      | razao_social     | Empresa LTDA             |
      | nome_fantasia    | Minha Empresa            |
      | email_corporativo| contato@empresa.com      |
      | telefone         | +5511888887777           |
    And eu valido o CNPJ via serviço externo
    And eu informo meus dados pessoais:
      | campo  | valor                    |
      | nome   | Maria Santos            |
      | cpf    | 98765432100             |
      | email  | maria@empresa.com       |
      | phone  | +5511999996666          |
    And eu envio a requisição de registro
    Then a entidade jurídica deve ser criada
    And o representante legal deve ser vinculado como ADMIN
    And as credenciais do representante devem ser criadas
    And o perfil corporativo deve ser criado
    And eu devo receber um JWT com escopo B2B
    And o evento "legal-entity.created" deve ser publicado
    And o evento "entity.linked" deve ser publicado

