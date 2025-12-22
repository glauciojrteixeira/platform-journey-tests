@implemented @vs-identity @segment_1 @identity @multi-country @documents @critical @e2e
Feature: Suporte Multi-País para Documentos de Identificação
  Como um usuário de diferentes países
  Eu quero criar minha identidade usando o documento de identificação do meu país
  Para ter acesso aos serviços da plataforma

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando
    And que tenho dados de teste únicos

  # ========== Cenários de Criação com Diferentes Tipos de Documentos ==========

  @document-cpf @br @b2c
  Scenario: Criar usuário B2C com CPF válido (Brasil)
    Given que o país padrão está configurado como "BR"
    When eu informo:
      | campo           | valor                    |
      | nome            | João Silva               |
      | documentNumber  | {unique_cpf}             |
      | documentType    | CPF                      |
      | email           | {unique_email}            |
      | telefone        | {unique_phone}            |
      | relationship   | B2C                      |
    And eu envio os dados para criar identidade
    Then a identidade deve ser criada com sucesso
    And a resposta deve conter "documentNumber" e "documentType"
    And o "documentType" deve ser "CPF"
    And o evento "user.created.v1" deve ser publicado
    And o evento "user.created.v1" deve conter "documentNumber" e "documentType"

  @document-cnpj @br @b2b
  Scenario: Criar usuário B2B com CNPJ válido (Brasil)
    Given que o país padrão está configurado como "BR"
    When eu informo:
      | campo           | valor                    |
      | nome            | Maria Santos             |
      | documentNumber  | {unique_cnpj}             |
      | documentType    | CNPJ                     |
      | email           | {unique_email}            |
      | telefone        | {unique_phone}            |
      | relationship    | B2B                      |
      | role            | ADMIN                    |
      | position        | Gerente                  |
    And eu envio os dados para criar identidade
    Then a identidade deve ser criada com sucesso
    And a resposta deve conter "documentNumber" e "documentType"
    And o "documentType" deve ser "CNPJ"

  @document-cuit @ar @b2c
  Scenario: Criar usuário B2C com CUIT válido (Argentina)
    Given que o país padrão está configurado como "AR"
    When eu informo:
      | campo           | valor                    |
      | nome            | Juan Pérez               |
      | documentNumber  | {unique_cuit}             |
      | documentType    | CUIT                     |
      | email           | {unique_email}            |
      | telefone        | {unique_phone}            |
      | relationship    | B2C                      |
    And eu envio os dados para criar identidade
    Then a identidade deve ser criada com sucesso
    And a resposta deve conter "documentNumber" e "documentType"
    And o "documentType" deve ser "CUIT"
    And o evento "user.created.v1" deve conter o header "country-code" com valor "ar"

  @document-dni @ar @b2c
  Scenario: Criar usuário B2C com DNI válido (Argentina)
    Given que o país padrão está configurado como "AR"
    When eu informo:
      | campo           | valor                    |
      | nome            | Carlos González           |
      | documentNumber  | {unique_dni}              |
      | documentType    | DNI                      |
      | email           | {unique_email}            |
      | telefone        | {unique_phone}            |
      | relationship    | B2C                      |
    And eu envio os dados para criar identidade
    Then a identidade deve ser criada com sucesso
    And a resposta deve conter "documentNumber" e "documentType"
    And o "documentType" deve ser "DNI"

  @document-rut @cl @b2c
  Scenario: Criar usuário B2C com RUT válido (Chile)
    Given que o país padrão está configurado como "CL"
    When eu informo:
      | campo           | valor                    |
      | nome            | Pedro Martínez           |
      | documentNumber  | {unique_rut}              |
      | documentType    | RUT                      |
      | email           | {unique_email}            |
      | telefone        | {unique_phone}            |
      | relationship    | B2C                      |
    And eu envio os dados para criar identidade
    Then a identidade deve ser criada com sucesso
    And a resposta deve conter "documentNumber" e "documentType"
    And o "documentType" deve ser "RUT"
    And o evento "user.created.v1" deve conter o header "country-code" com valor "cl"

  @document-rut-k @cl @b2c
  Scenario: Criar usuário B2C com RUT válido terminando em K (Chile)
    Given que o país padrão está configurado como "CL"
    When eu informo:
      | campo           | valor                    |
      | nome            | Ana López                |
      | documentNumber  | 10000013-K               |
      | documentType    | RUT                      |
      | email           | {unique_email}            |
      | telefone        | {unique_phone}            |
      | relationship    | B2C                      |
    And eu envio os dados para criar identidade
    Then a identidade deve ser criada com sucesso
    And a resposta deve conter "documentNumber" e "documentType"
    And o "documentType" deve ser "RUT"

  @document-ci @bo @b2c
  Scenario: Criar usuário B2C com CI válido (Bolívia)
    Given que o país padrão está configurado como "BO"
    When eu informo:
      | campo           | valor                    |
      | nome            | Luis Fernández           |
      | documentNumber  | {unique_ci}               |
      | documentType    | CI                       |
      | email           | {unique_email}            |
      | telefone        | {unique_phone}            |
      | relationship    | B2C                      |
    And eu envio os dados para criar identidade
    Then a identidade deve ser criada com sucesso
    And a resposta deve conter "documentNumber" e "documentType"
    And o "documentType" deve ser "CI"
    And o evento "user.created.v1" deve conter o header "country-code" com valor "bo"

  @document-ssn @us @b2c
  Scenario: Criar usuário B2C com SSN válido (EUA)
    Given que o país padrão está configurado como "US"
    When eu informo:
      | campo           | valor                    |
      | nome            | John Smith               |
      | documentNumber  | {unique_ssn}              |
      | documentType    | SSN                      |
      | email           | {unique_email}            |
      | telefone        | {unique_phone}            |
      | relationship    | B2C                      |
    And eu envio os dados para criar identidade
    Then a identidade deve ser criada com sucesso
    And a resposta deve conter "documentNumber" e "documentType"
    And o "documentType" deve ser "SSN"
    And o evento "user.created.v1" deve conter o header "country-code" com valor "us"

  # ========== Cenários de Validação de Documentos Inválidos ==========

  @document-validation-invalid-cpf @br @b2c
  Scenario: Criar usuário com CPF inválido deve falhar
    Given que o país padrão está configurado como "BR"
    When eu informo:
      | campo           | valor                    |
      | nome            | João Silva               |
      | documentNumber  | 12345678900              |
      | documentType    | CPF                      |
      | email           | {unique_email}            |
      | telefone        | {unique_phone}            |
      | relationship    | B2C                      |
    And eu envio os dados para criar identidade
    Then a criação deve falhar com status 400
    And a mensagem de erro deve conter "Invalid CPF"

  @document-validation-invalid-cnpj @br @b2b
  Scenario: Criar usuário com CNPJ inválido deve falhar
    Given que o país padrão está configurado como "BR"
    When eu informo:
      | campo           | valor                    |
      | nome            | Maria Santos             |
      | documentNumber  | 12345678000190           |
      | documentType    | CNPJ                     |
      | email           | {unique_email}            |
      | telefone        | {unique_phone}            |
      | relationship    | B2B                      |
      | role            | ADMIN                    |
      | position        | Gerente                  |
    And eu envio os dados para criar identidade
    Then a criação deve falhar com status 400
    And a mensagem de erro deve conter "Invalid CNPJ"

  @document-validation-invalid-cuit @ar @b2c
  Scenario: Criar usuário com CUIT inválido deve falhar
    Given que o país padrão está configurado como "AR"
    When eu informo:
      | campo           | valor                    |
      | nome            | Juan Pérez               |
      | documentNumber  | 20123456780              |
      | documentType    | CUIT                     |
      | email           | {unique_email}            |
      | telefone        | {unique_phone}            |
      | relationship    | B2C                      |
    And eu envio os dados para criar identidade
    Then a criação deve falhar com status 400
    And a mensagem de erro deve conter "Invalid CUIT"

  @document-validation-invalid-rut @cl @b2c
  Scenario: Criar usuário com RUT inválido deve falhar
    Given que o país padrão está configurado como "CL"
    When eu informo:
      | campo           | valor                    |
      | nome            | Pedro Martínez           |
      | documentNumber  | 12345678-0               |
      | documentType    | RUT                      |
      | email           | {unique_email}            |
      | telefone        | {unique_phone}            |
      | relationship    | B2C                      |
    And eu envio os dados para criar identidade
    Then a criação deve falhar com status 400
    And a mensagem de erro deve conter "Invalid RUT"

  @document-validation-unsupported-type
  Scenario: Criar usuário com tipo de documento não suportado deve falhar
    Given que o país padrão está configurado como "BR"
    When eu informo:
      | campo           | valor                    |
      | nome            | João Silva               |
      | documentNumber  | 12345678901              |
      | documentType    | PASSPORT                 |
      | email           | {unique_email}            |
      | telefone        | {unique_phone}            |
      | relationship    | B2C                      |
    And eu envio os dados para criar identidade
    Then a criação deve falhar com status 400
    And a mensagem de erro deve conter "not supported"

  @document-validation-null-document-number
  Scenario: Criar usuário sem documentNumber deve falhar
    Given que o país padrão está configurado como "BR"
    When eu informo:
      | campo           | valor                    |
      | nome            | João Silva               |
      | documentType    | CPF                      |
      | email           | {unique_email}            |
      | telefone        | {unique_phone}            |
      | relationship    | B2C                      |
    And eu envio os dados para criar identidade
    Then a criação deve falhar com status 400
    And a mensagem de erro deve conter "Document number cannot be null"

  @document-validation-null-document-type
  Scenario: Criar usuário sem documentType deve falhar
    Given que o país padrão está configurado como "BR"
    When eu informo:
      | campo           | valor                    |
      | nome            | João Silva               |
      | documentNumber  | {unique_cpf}             |
      | email           | {unique_email}            |
      | telefone        | {unique_phone}            |
      | relationship    | B2C                      |
    And eu envio os dados para criar identidade
    Then a criação deve falhar com status 400
    And a mensagem de erro deve conter "Document type cannot be null"

  # ========== Cenários de Documentos Duplicados ==========

  @document-duplicate-cpf @br @b2c
  Scenario: Criar usuário com CPF duplicado deve falhar
    Given que o país padrão está configurado como "BR"
    And que já existe um usuário com documento "{unique_cpf}" do tipo "CPF"
    When eu informo:
      | campo           | valor                    |
      | nome            | João Silva Duplicado      |
      | documentNumber  | {same_document}         |
      | documentType    | CPF                      |
      | email           | {unique_email}            |
      | telefone        | {unique_phone}            |
      | relationship    | B2C                      |
    And eu envio os dados para criar identidade
    Then a criação deve falhar com status 409
    And a mensagem de erro deve conter "Document already exists"

  @document-duplicate-cnpj @br @b2b
  Scenario: Criar usuário com CNPJ duplicado deve falhar
    Given que o país padrão está configurado como "BR"
    And que já existe um usuário com documento "{unique_cnpj}" do tipo "CNPJ"
    When eu informo:
      | campo           | valor                    |
      | nome            | Maria Santos Duplicado   |
      | documentNumber  | {same_document}          |
      | documentType    | CNPJ                     |
      | email           | {unique_email}            |
      | telefone        | {unique_phone}            |
      | relationship    | B2B                      |
      | role            | ADMIN                    |
      | position        | Gerente                  |
    And eu envio os dados para criar identidade
    Then a criação deve falhar com status 409
    And a mensagem de erro deve conter "Document already exists"

  @document-duplicate-cuit @ar @b2c
  Scenario: Criar usuário com CUIT duplicado deve falhar
    Given que o país padrão está configurado como "AR"
    And que já existe um usuário com documento "{unique_cuit}" do tipo "CUIT"
    When eu informo:
      | campo           | valor                    |
      | nome            | Juan Pérez Duplicado     |
      | documentNumber  | {same_document}          |
      | documentType    | CUIT                     |
      | email           | {unique_email}            |
      | telefone        | {unique_phone}            |
      | relationship    | B2C                      |
    And eu envio os dados para criar identidade
    Then a criação deve falhar com status 409
    And a mensagem de erro deve conter "Document already exists"

  # ========== Cenários de Imutabilidade de Documentos ==========

  @document-immutability @br @b2c
  Scenario: Tentar alterar documentNumber deve falhar
    Given que o país padrão está configurado como "BR"
    And que crio um usuário com esses dados:
      | campo           | valor                    |
      | nome            | João Silva               |
      | documentNumber  | {unique_cpf}             |
      | documentType    | CPF                      |
      | email           | {unique_email}            |
      | telefone        | {unique_phone}            |
      | relationship    | B2C                      |
    And a identidade deve ser criada com sucesso
    When eu tento alterar meu documentNumber para "{unique_cpf_2}"
    Then a alteração de identidade deve falhar com status 400
    And a mensagem de erro de identidade deve conter "Document is immutable"

  @document-immutability-type @br @b2c
  Scenario: Tentar alterar documentType deve falhar
    Given que o país padrão está configurado como "BR"
    And que crio um usuário com esses dados:
      | campo           | valor                    |
      | nome            | João Silva               |
      | documentNumber  | {unique_cpf}             |
      | documentType    | CPF                      |
      | email           | {unique_email}            |
      | telefone        | {unique_phone}            |
      | relationship    | B2C                      |
    And a identidade deve ser criada com sucesso
    When eu tento alterar meu documentType para "CNPJ"
    Then a alteração de identidade deve falhar com status 400
    And a mensagem de erro de identidade deve conter "Document is immutable"

  # ========== Cenários de Compatibilidade Retroativa ==========

  @document-backward-compatibility @br @b2c
  Scenario: Eventos com formato antigo (cpf) devem ser processados corretamente
    Given que o país padrão está configurado como "BR"
    When um evento "user.created.v1" é publicado com formato antigo contendo "cpf"
    Then o Auth Service deve processar o evento corretamente
    And o usuário deve ser criado no Auth Service com "documentNumber" e "documentType" derivados do "cpf"

  # ========== Cenários de Validação por País ==========

  @document-country-validation @br @ar @cl
  Scenario Outline: Documentos devem ser validados de acordo com o país configurado
    Given que o país padrão está configurado como "<country>"
    When eu informo:
      | campo           | valor                    |
      | nome            | Test User                |
      | documentNumber  | "<document>"             |
      | documentType    | "<document_type>"         |
      | email           | {unique_email}            |
      | telefone        | {unique_phone}            |
      | relationship    | B2C                      |
    And eu envio os dados para criar identidade
    Then a identidade deve ser criada com sucesso
    And o "documentType" deve ser "<document_type>"
    And o evento "user.created.v1" deve conter o header "country-code" com valor "<country_lowercase>"
    
    Examples:
      | country | country_lowercase | document_type | document        |
      | BR      | br               | CPF           | {unique_cpf}    |
      | BR      | br               | CNPJ          | {unique_cnpj}   |
      | AR      | ar               | CUIT          | {unique_cuit}   |
      | AR      | ar               | DNI           | {unique_dni}    |
      | CL      | cl               | RUT           | {unique_rut}    |
      | BO      | bo               | CI            | {unique_ci}     |
      | US      | us               | SSN           | {unique_ssn}    |

  # ========== Cenários de Integração com Auth Service ==========

  @document-auth-service-integration @br @b2c
  Scenario: Auth Service deve receber eventos com documentNumber e documentType
    Given que o país padrão está configurado como "BR"
    When eu informo:
      | campo           | valor                    |
      | nome            | João Silva               |
      | documentNumber  | {unique_cpf}             |
      | documentType    | CPF                      |
      | email           | {unique_email}            |
      | telefone        | {unique_phone}            |
      | relationship    | B2C                      |
    And eu envio os dados para criar identidade
    Then a identidade deve ser criada com sucesso
    And o evento "user.created.v1" deve ser publicado
    And o evento "user.created.v1" deve conter "documentNumber" e "documentType"
    And o Auth Service deve consumir o evento "user.created.v1"
    And o usuário deve ser criado no Auth Service com "documentNumber" e "documentType" corretos
