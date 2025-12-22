@not_implemented @vs-identity @segment_4 @j4.1 @b2b @enterprise @high @e2e @may_require_auth
Feature: Registro e Validação Completa de Plataforma B2B
  Como representante técnico de uma plataforma parceira
  Eu quero registrar minha plataforma com todas as validações
  Para ter acesso a integrações técnicas

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando

  Scenario: Registro completo de plataforma B2B bem-sucedido
    Given que sou representante técnico de uma plataforma
    When eu registro a entidade jurídica:
      | campo            | valor                    |
      | cnpj             | 12345678000190           |
      | razao_social     | Plataforma LTDA          |
      | nome_fantasia    | Minha Plataforma         |
      | email_corporativo| contato@plataforma.com   |
      | telefone         | +5511888887777           |
    Then o CNPJ deve ser validado via Receita Federal
    When eu valido o contrato de parceria:
      | numero_contrato | CONTRATO-2025-001        |
    Then o contrato deve ser validado contra base de contratos aprovados
    When eu valido o domínio técnico:
      | dominio_tecnico | api.plataforma.com       |
    Then o domínio deve ser validado (DNS, certificado)
    When eu crio usuário técnico com role TECHNICAL:
      | campo           | valor                    |
      | nome            | Admin Técnico            |
      | documentNumber  | {unique_cpf}             |
      | documentType    | CPF                      |
      | email           | admin@plataforma.com     |
      | phone           | +5511999996666           |
      | role            | TECHNICAL                |
    Then o usuário técnico deve ser criado
    And o MFA deve ser obrigatório e ativado automaticamente
    And o evento "legal-entity.platform.registered" deve ser publicado

  Scenario: Registro falha sem contrato de parceria válido
    Given que sou representante técnico de uma plataforma
    When eu registro a entidade jurídica
    And o CNPJ é validado
    When eu tento validar contrato inexistente:
      | numero_contrato | CONTRATO-INVALIDO        |
    Then a validação deve falhar com status 404
    And o erro deve indicar que contrato não encontrado ou não aprovado
    And o registro não deve prosseguir

  Scenario: Registro falha com domínio técnico inválido
    Given que sou representante técnico de uma plataforma
    When eu registro a entidade jurídica
    And o CNPJ é validado
    And o contrato é validado
    When eu tento validar domínio técnico inválido:
      | dominio_tecnico | dominio-invalido.com     |
    Then a validação deve falhar com status 400
    And o erro deve indicar que domínio não pode ser validado
    And o registro não deve prosseguir

