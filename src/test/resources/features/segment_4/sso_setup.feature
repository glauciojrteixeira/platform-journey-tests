@not_implemented @vs-identity @segment_4 @j4.2 @b2b @sso @enterprise @medium @e2e @may_require_auth
Feature: Configuração Inicial de SSO
  Como admin técnico de uma plataforma B2B
  Eu quero configurar SSO corporativo
  Para permitir login via sistema corporativo

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando
    And que tenho uma entidade jurídica registrada
    And que estou autenticado como admin técnico

  Scenario: Configuração de SSO SAML bem-sucedida
    Given que tenho metadados SSO SAML válidos
    When eu configuro SSO informando:
      | campo            | valor                           |
      | sso_metadata_url | https://idp.empresa.com/metadata|
      | sso_enabled      | true                            |
    Then o sistema deve baixar metadados da URL fornecida
    And deve validar formato SAML XML
    And deve extrair certificados e endpoints
    And deve validar certificados SSL
    When eu testo a configuração SSO
    Then o teste deve ser bem-sucedido
    And o SSO deve ser habilitado
    And o evento "sso.configured" deve ser publicado
    And usuários podem usar SSO para login

  Scenario: Configuração de SSO OAuth2 bem-sucedida
    Given que tenho configuração OAuth2 válida
    When eu configuro SSO informando:
      | campo            | valor                           |
      | sso_metadata_url | https://oauth.empresa.com/.well-known/openid-configuration|
      | sso_enabled      | true                            |
    Then o sistema deve validar configuração OAuth2
    And deve registrar redirect_uri
    And deve validar certificados
    When eu testo a configuração SSO
    Then o teste deve ser bem-sucedido
    And o SSO deve ser habilitado

  Scenario: Configuração SSO falha com metadados inválidos
    Given que tenho metadados SSO inválidos
    When eu tento configurar SSO com metadados inválidos:
      | campo            | valor                           |
      | sso_metadata_url | https://url-invalida.com        |
    Then a configuração deve falhar com status 400
    And o erro deve indicar que metadados são inválidos
    And o SSO não deve ser habilitado

  Scenario: Configuração SSO falha com certificados expirados
    Given que tenho metadados SSO com certificados expirados
    When eu tento configurar SSO
    Then a configuração deve falhar com status 400
    And o erro deve indicar que certificados estão expirados
    And o SSO não deve ser habilitado

