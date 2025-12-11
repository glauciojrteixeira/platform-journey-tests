@not_implemented @vs-identity @segment_4 @j4.5 @b2b @sso @enterprise @low @e2e @may_require_auth
Feature: Rotação de Certificados SSO
  Como admin técnico de uma plataforma B2B
  Eu quero atualizar certificados SSO quando expiram
  Para manter SSO funcionando sem interrupção

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando
    And que tenho uma plataforma com SSO configurado
    And que estou autenticado como admin técnico

  Scenario: Rotação de certificados bem-sucedida
    Given que tenho certificados SSO próximos do vencimento (30 dias)
    When o sistema detecta e notifica sobre vencimento
    And eu atualizo metadados SSO com novos certificados:
      | campo            | valor                           |
      | sso_metadata_url | https://idp.empresa.com/metadata-novo|
    Then o sistema deve baixar novos metadados
    And deve validar novos certificados
    And deve configurar período de transição
    And certificados antigos ainda funcionam por 7 dias
    And certificados novos já funcionam imediatamente
    And o evento "sso.certificates.rotated" deve ser publicado
    And o SSO continua funcionando durante transição

  Scenario: Rotação falha com certificados inválidos
    Given que tenho certificados SSO próximos do vencimento
    When eu tento atualizar com certificados inválidos
    Then a atualização deve falhar com status 400
    And o erro deve indicar que certificados são inválidos
    And os certificados antigos devem continuar funcionando

