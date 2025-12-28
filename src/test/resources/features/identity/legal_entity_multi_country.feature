@implemented @vs-identity @segment_3 @j3.1 @legal_entity @multi-country @b2b @high @e2e
Feature: Registro de Entidade Jurídica Multi-Country
  Como representante legal de uma empresa em diferentes países
  Eu quero registrar minha empresa usando o documento de identificação do meu país
  Para gerenciar múltiplos usuários em diferentes mercados

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando
    And que tenho dados de teste únicos

  # ========== Cenários de Criação com Diferentes Tipos de Documentos PJ ==========

  @legal-entity-cnpj @br @b2b
  Scenario: Criar entidade jurídica com CNPJ válido (Brasil)
    Given que o país padrão está configurado como "BR"
    When eu informo os dados da entidade jurídica:
      | campo            | valor                    |
      | documentNumber   | {unique_cnpj}             |
      | documentType     | CNPJ                      |
      | corporateName    | Empresa Brasileira LTDA   |
      | tradeName        | Empresa BR                |
      | corporateEmail   | {unique_email}            |
      | phone            | {unique_phone}            |
    And eu envio a requisição para criar entidade jurídica
    Then a entidade jurídica deve ser criada com sucesso
    And a resposta deve conter "documentNumber" e "documentType"
    And o "documentType" deve ser "CNPJ"
    And o "documentNumber" deve ser igual ao informado
    And o evento "legal-entity.created" deve ser publicado
    And o evento "legal-entity.created" deve conter "documentNumber" e "documentType"

  @legal-entity-cuit @ar @b2b
  Scenario: Criar entidade jurídica com CUIT válido (Argentina)
    Given que o país padrão está configurado como "AR"
    When eu informo os dados da entidade jurídica:
      | campo            | valor                    |
      | documentNumber   | {unique_cuit}             |
      | documentType     | CUIT                      |
      | corporateName    | Empresa Argentina S.A.    |
      | tradeName        | Empresa AR                |
      | corporateEmail   | {unique_email}            |
      | phone            | {unique_phone}            |
    And eu envio a requisição para criar entidade jurídica
    Then a entidade jurídica deve ser criada com sucesso
    And a resposta deve conter "documentNumber" e "documentType"
    And o "documentType" deve ser "CUIT"
    And o "documentNumber" deve ser igual ao informado
    And o evento "legal-entity.created" deve ser publicado
    And o evento "legal-entity.created" deve conter o header "country-code" com valor "ar"

  @legal-entity-rut @cl @b2b
  Scenario: Criar entidade jurídica com RUT válido (Chile)
    Given que o país padrão está configurado como "CL"
    When eu informo os dados da entidade jurídica:
      | campo            | valor                    |
      | documentNumber   | {unique_rut}              |
      | documentType     | RUT                      |
      | corporateName    | Empresa Chilena S.A.     |
      | tradeName        | Empresa CL               |
      | corporateEmail   | {unique_email}            |
      | phone            | {unique_phone}            |
    And eu envio a requisição para criar entidade jurídica
    Then a entidade jurídica deve ser criada com sucesso
    And a resposta deve conter "documentNumber" e "documentType"
    And o "documentType" deve ser "RUT"
    And o "documentNumber" deve ser igual ao informado
    And o evento "legal-entity.created" deve ser publicado
    And o evento "legal-entity.created" deve conter o header "country-code" com valor "cl"

  @legal-entity-nit @bo @b2b
  Scenario: Criar entidade jurídica com NIT válido (Bolívia)
    Given que o país padrão está configurado como "BO"
    When eu informo os dados da entidade jurídica:
      | campo            | valor                    |
      | documentNumber   | {unique_nit}              |
      | documentType     | NIT                      |
      | corporateName    | Empresa Boliviana S.A.    |
      | tradeName        | Empresa BO               |
      | corporateEmail   | {unique_email}            |
      | phone            | {unique_phone}            |
    And eu envio a requisição para criar entidade jurídica
    Then a entidade jurídica deve ser criada com sucesso
    And a resposta deve conter "documentNumber" e "documentType"
    And o "documentType" deve ser "NIT"
    And o "documentNumber" deve ser igual ao informado
    And o evento "legal-entity.created" deve ser publicado
    And o evento "legal-entity.created" deve conter o header "country-code" com valor "bo"

  @legal-entity-ein @us @b2b
  Scenario: Criar entidade jurídica com EIN válido (Estados Unidos)
    Given que o país padrão está configurado como "US"
    When eu informo os dados da entidade jurídica:
      | campo            | valor                    |
      | documentNumber   | {unique_ein}              |
      | documentType     | EIN                      |
      | corporateName    | US Company Inc.          |
      | tradeName        | US Company               |
      | corporateEmail   | {unique_email}            |
      | phone            | {unique_phone}            |
    And eu envio a requisição para criar entidade jurídica
    Then a entidade jurídica deve ser criada com sucesso
    And a resposta deve conter "documentNumber" e "documentType"
    And o "documentType" deve ser "EIN"
    And o "documentNumber" deve ser igual ao informado
    And o evento "legal-entity.created" deve ser publicado
    And o evento "legal-entity.created" deve conter o header "country-code" com valor "us"

  # ========== Cenários de Validação de Documentos Inválidos ==========

  @legal-entity-validation-invalid-cnpj @br @b2b
  Scenario: Criar entidade jurídica com CNPJ inválido deve falhar
    Given que o país padrão está configurado como "BR"
    When eu informo os dados da entidade jurídica:
      | campo            | valor                    |
      | documentNumber   | 12345678000190           |
      | documentType     | CNPJ                     |
      | corporateName    | Empresa Teste LTDA       |
      | tradeName        | Empresa Teste            |
      | corporateEmail   | {unique_email}            |
      | phone            | {unique_phone}            |
    And eu envio a requisição para criar entidade jurídica
    Then a criação deve falhar com status 400
    And a mensagem de erro deve conter "Invalid CNPJ"

  @legal-entity-validation-invalid-cuit @ar @b2b
  Scenario: Criar entidade jurídica com CUIT inválido deve falhar
    Given que o país padrão está configurado como "AR"
    When eu informo os dados da entidade jurídica:
      | campo            | valor                    |
      | documentNumber   | 20123456780              |
      | documentType     | CUIT                     |
      | corporateName    | Empresa Teste S.A.       |
      | tradeName        | Empresa Teste            |
      | corporateEmail   | {unique_email}            |
      | phone            | {unique_phone}            |
    And eu envio a requisição para criar entidade jurídica
    Then a criação deve falhar com status 400
    And a mensagem de erro deve conter "Invalid CUIT"

  @legal-entity-validation-invalid-rut @cl @b2b
  Scenario: Criar entidade jurídica com RUT inválido deve falhar
    Given que o país padrão está configurado como "CL"
    When eu informo os dados da entidade jurídica:
      | campo            | valor                    |
      | documentNumber   | 12345678-0               |
      | documentType     | RUT                      |
      | corporateName    | Empresa Teste S.A.       |
      | tradeName        | Empresa Teste            |
      | corporateEmail   | {unique_email}            |
      | phone            | {unique_phone}            |
    And eu envio a requisição para criar entidade jurídica
    Then a criação deve falhar com status 400
    And a mensagem de erro deve conter "Invalid RUT"

  @legal-entity-validation-unsupported-type
  Scenario: Criar entidade jurídica com tipo de documento não suportado deve falhar
    Given que o país padrão está configurado como "BR"
    When eu informo os dados da entidade jurídica:
      | campo            | valor                    |
      | documentNumber   | 12345678000190           |
      | documentType     | PASSPORT                 |
      | corporateName    | Empresa Teste LTDA       |
      | tradeName        | Empresa Teste            |
      | corporateEmail   | {unique_email}            |
      | phone            | {unique_phone}            |
    And eu envio a requisição para criar entidade jurídica
    Then a criação deve falhar com status 400
    And a mensagem de erro deve conter "not supported"

  @legal-entity-validation-null-document-number
  Scenario: Criar entidade jurídica sem documentNumber deve falhar
    Given que o país padrão está configurado como "BR"
    When eu informo os dados da entidade jurídica:
      | campo            | valor                    |
      | documentType     | CNPJ                     |
      | corporateName    | Empresa Teste LTDA       |
      | tradeName        | Empresa Teste            |
      | corporateEmail   | {unique_email}            |
      | phone            | {unique_phone}            |
    And eu envio a requisição para criar entidade jurídica
    Then a criação deve falhar com status 400
    And a mensagem de erro deve conter "Document number cannot be null"

  @legal-entity-validation-null-document-type
  Scenario: Criar entidade jurídica sem documentType deve falhar
    Given que o país padrão está configurado como "BR"
    When eu informo os dados da entidade jurídica:
      | campo            | valor                    |
      | documentNumber   | {unique_cnpj}             |
      | corporateName    | Empresa Teste LTDA       |
      | tradeName        | Empresa Teste            |
      | corporateEmail   | {unique_email}            |
      | phone            | {unique_phone}            |
    And eu envio a requisição para criar entidade jurídica
    Then a criação deve falhar com status 400
    And a mensagem de erro deve conter "Document type cannot be null"

  # ========== Cenários de Documentos Duplicados ==========

  @legal-entity-duplicate-cnpj @br @b2b
  Scenario: Criar entidade jurídica com CNPJ duplicado deve falhar
    Given que o país padrão está configurado como "BR"
    And que já existe uma entidade jurídica com documento "{unique_cnpj}" do tipo "CNPJ"
    When eu informo os dados da entidade jurídica:
      | campo            | valor                    |
      | documentNumber   | {same_document}          |
      | documentType     | CNPJ                     |
      | corporateName    | Empresa Duplicada LTDA   |
      | tradeName        | Empresa Duplicada        |
      | corporateEmail   | {unique_email}            |
      | phone            | {unique_phone}            |
    And eu envio a requisição para criar entidade jurídica
    Then a criação deve falhar com status 409
    And a mensagem de erro deve conter "already exists"

  @legal-entity-duplicate-cuit @ar @b2b
  Scenario: Criar entidade jurídica com CUIT duplicado deve falhar
    Given que o país padrão está configurado como "AR"
    And que já existe uma entidade jurídica com documento "{unique_cuit}" do tipo "CUIT"
    When eu informo os dados da entidade jurídica:
      | campo            | valor                    |
      | documentNumber   | {same_document}          |
      | documentType     | CUIT                     |
      | corporateName    | Empresa Duplicada S.A.   |
      | tradeName        | Empresa Duplicada        |
      | corporateEmail   | {unique_email}            |
      | phone            | {unique_phone}            |
    And eu envio a requisição para criar entidade jurídica
    Then a criação deve falhar com status 409
    And a mensagem de erro deve conter "already exists"

  # ========== Cenários de Validação por País ==========

  @legal-entity-country-validation @br @ar @cl @bo @us
  Scenario Outline: Entidades jurídicas devem ser validadas de acordo com o país configurado
    Given que o país padrão está configurado como "<country>"
    When eu informo os dados da entidade jurídica:
      | campo            | valor                    |
      | documentNumber   | "<document>"             |
      | documentType     | "<document_type>"        |
      | corporateName    | Empresa Teste           |
      | tradeName        | Empresa Teste            |
      | corporateEmail   | {unique_email}            |
      | phone            | {unique_phone}            |
    And eu envio a requisição para criar entidade jurídica
    Then a entidade jurídica deve ser criada com sucesso
    And o "documentType" deve ser "<document_type>"
    And o evento "legal-entity.created" deve conter o header "country-code" com valor "<country_lowercase>"
    
    Examples:
      | country | country_lowercase | document_type | document        |
      | BR      | br               | CNPJ          | {unique_cnpj}   |
      | AR      | ar               | CUIT          | {unique_cuit}   |
      | CL      | cl               | RUT           | {unique_rut}    |
      | BO      | bo               | NIT           | {unique_nit}    |
      | US      | us               | EIN           | {unique_ein}    |

