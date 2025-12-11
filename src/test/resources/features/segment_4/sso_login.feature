@not_implemented @vs-identity @segment_4 @j4.3 @b2b @sso @enterprise @critical @e2e
Feature: Login via SSO B2B Enterprise
  Como um usuário técnico de uma plataforma parceira
  Eu quero fazer login via SSO corporativo
  Para acessar recursos usando autenticação corporativa

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando
    And que tenho uma plataforma com SSO configurado

  Scenario: Login via SSO SAML bem-sucedido
    Given que tenho SSO SAML configurado
    When eu acesso "Login SSO"
    Then o sistema deve redirecionar para provedor SSO
    When eu autentico no sistema corporativo (Active Directory)
    Then o provedor SSO deve retornar assertion SAML
    And o sistema deve validar assertion
    And deve mapear claims SSO para usuário interno
    And deve criar sessão SSO
    And deve emitir JWT interno com claims mapeados
    And o usuário deve ser redirecionado para aplicação autenticado

  Scenario: Login via SSO OAuth2 bem-sucedido
    Given que tenho SSO OAuth2 configurado
    When eu acesso "Login SSO"
    Then o sistema deve redirecionar para provedor OAuth2
    When eu autentico no sistema corporativo
    Then o provedor OAuth2 deve retornar token
    And o sistema deve validar token
    And deve mapear claims para usuário interno
    And deve criar sessão SSO
    And deve emitir JWT interno
    And o usuário deve ser autenticado

  Scenario: Login SSO falha com assertion inválida
    Given que tenho SSO SAML configurado
    When eu acesso "Login SSO"
    And o provedor SSO retorna assertion inválida
    Then o login deve falhar com status 401
    And o erro deve indicar que assertion é inválida
    And nenhum JWT deve ser emitido

  Scenario: Login SSO cria usuário automaticamente se não existir
    Given que tenho SSO configurado
    And que usuário não existe no sistema
    And que criação automática está habilitada
    When eu faço login via SSO
    Then o sistema deve criar usuário automaticamente
    And deve vincular à entidade jurídica
    And deve emitir JWT válido

