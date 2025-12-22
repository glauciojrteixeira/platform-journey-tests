# Correções de Validação de Dados

**Data**: 2025-12-22  
**Status**: ✅ **Correções Implementadas**

---

## 📋 Problemas Identificados

### 1. Tipos de Documento Inválidos (7 falhas)

**Erro**: `"Document type must be one of: CPF, CNPJ, CUIT, DNI, RUT, CI, SSN"`

**Cenários Afetados**:
- Example #1.1 até #1.7 (Scenario Outline: Documentos devem ser validados de acordo com o país configurado)

**Causa Raiz**: 
- O `documentType` do Scenario Outline estava sendo processado incorretamente
- Quando o Cucumber substitui `<document_type>` do Examples, o valor pode não estar sendo normalizado para uppercase
- O `documentType` pode estar sendo enviado como null ou com valor inválido

**Solução Implementada**:
1. ✅ Adicionada normalização adicional de `documentType` no método `eu_informo()`
2. ✅ Adicionada validação para inferir `documentType` do país configurado quando ausente
3. ✅ Adicionada validação no `UserFixture.buildCreateUserRequest()` para garantir uppercase
4. ✅ Adicionado logging detalhado para diagnosticar problemas de `documentType`
5. ✅ Adicionada validação de tipos aceitos pelo backend com mensagens de erro claras

**Arquivos Modificados**:
- `src/test/java/com/nulote/journey/stepdefinitions/AuthenticationSteps.java`
  - Método `eu_informo()`: Adicionada inferência de `documentType` do país configurado
  - Adicionada validação de tipos aceitos pelo backend
  - Melhorado logging para diagnóstico
  
- `src/test/java/com/nulote/journey/fixtures/UserFixture.java`
  - Método `buildCreateUserRequest()`: Adicionada validação de tipos aceitos
  - Melhorada normalização de `documentType` para uppercase
  - Adicionado tratamento para valores "null" como string

---

### 2. Header registration-token Ausente (1 falha)

**Erro**: `"registration-token header is required for user registration"`

**Cenário Afetado**:
- Criar usuário B2C com RUT válido terminando em K (Chile)

**Causa Raiz**: 
- O código já tinha lógica para criar OTP e obter `sessionToken` automaticamente
- Mas pode haver casos onde o email não está presente ou o OTP não é criado corretamente

**Solução Implementada**:
1. ✅ Adicionada validação explícita de email antes de solicitar OTP
2. ✅ Adicionado logging detalhado para diagnosticar problemas de OTP
3. ✅ Melhorada mensagem de erro quando OTP não pode ser criado
4. ✅ Adicionada validação do request de OTP antes de enviar

**Arquivos Modificados**:
- `src/test/java/com/nulote/journey/stepdefinitions/AuthenticationSteps.java`
  - Método `eu_envio_os_dados_para_criar_identidade()`: Adicionada validação de email antes de criar OTP
  - Melhorado logging e tratamento de erros

---

## 🔧 Detalhes Técnicos das Correções

### Correção 1: Normalização de documentType

**Antes**:
```java
if (documentType != null && !documentType.trim().isEmpty() && !documentType.startsWith("{")) {
    documentType = documentType.toUpperCase().trim();
    userData.put("documentType", documentType);
}
```

**Depois**:
```java
// Normalização inicial
if (documentType != null && !documentType.trim().isEmpty() && !documentType.startsWith("{")) {
    documentType = documentType.toUpperCase().trim();
    userData.put("documentType", documentType);
}

// Inferência do país se documentType ainda estiver ausente
if (normalizedDocumentType == null || normalizedDocumentType.trim().isEmpty()) {
    if (config != null) {
        String countryCode = config.getDefaultCountryCode();
        String inferredType = null;
        switch (countryCode) {
            case "BR": inferredType = "CPF"; break;
            case "AR": inferredType = "CUIT"; break;
            case "CL": inferredType = "RUT"; break;
            case "BO": inferredType = "CI"; break;
            case "US": inferredType = "SSN"; break;
        }
        if (inferredType != null) {
            userData.put("documentType", inferredType);
        }
    }
}

// Normalização final com validação
String finalDocumentType = userData.get("documentType");
if (finalDocumentType != null && !finalDocumentType.trim().isEmpty() && !finalDocumentType.startsWith("{")) {
    finalDocumentType = finalDocumentType.toUpperCase().trim();
    userData.put("documentType", finalDocumentType);
    
    // Validar que está na lista aceita
    String[] validTypes = {"CPF", "CNPJ", "CUIT", "DNI", "RUT", "CI", "SSN"};
    boolean isValid = false;
    for (String validType : validTypes) {
        if (validType.equals(finalDocumentType)) {
            isValid = true;
            break;
        }
    }
    if (!isValid) {
        logger.error("❌ DocumentType '{}' não está na lista de tipos aceitos", finalDocumentType);
    }
}
```

### Correção 2: Validação de Email antes de OTP

**Antes**:
```java
var otpRequest = userFixture.buildOtpRequest("EMAIL", "REGISTRATION");
var otpResponse = authClient.requestOtp(otpRequest);
```

**Depois**:
```java
// Garantir que email está presente antes de solicitar OTP
var userData = userFixture.getUserData();
if (userData == null || userData.get("email") == null || userData.get("email").trim().isEmpty()) {
    logger.error("❌ Email não está presente no userData. Não é possível criar OTP.");
    throw new IllegalStateException("Email não está presente no userData. Não é possível criar OTP para registro.");
}

String email = userData.get("email");
logger.debug("Solicitando OTP para email: {}", email);

var otpRequest = userFixture.buildOtpRequest("EMAIL", "REGISTRATION");
logger.debug("OTP Request: {}", otpRequest);
var otpResponse = authClient.requestOtp(otpRequest);
```

---

## ✅ Resultados Esperados

Após essas correções, esperamos que:

1. ✅ **Todos os 7 testes do Scenario Outline passem** - O `documentType` será sempre normalizado corretamente
2. ✅ **O teste do RUT com K passe** - O `registration-token` será sempre enviado corretamente
3. ✅ **Logs mais informativos** - Facilitarão o diagnóstico de problemas futuros

---

## 🧪 Como Testar

Execute os testes E2E novamente:

```bash
cd nulote-backend/platform-journey-tests
mvn clean test
```

Ou execute apenas os testes afetados:

```bash
mvn test -Dtest=CucumberTestRunner -Dcucumber.filter.tags="@document-country-validation or @document-rut-k"
```

---

## 📝 Notas Adicionais

1. **Normalização em Múltiplos Pontos**: A normalização de `documentType` foi implementada em três pontos:
   - No método `eu_informo()` (normalização inicial)
   - No método `eu_informo()` (inferência do país se ausente)
   - No método `buildCreateUserRequest()` (normalização final antes de enviar)

2. **Validação de Tipos Aceitos**: Adicionada validação que verifica se o `documentType` está na lista aceita pelo backend antes de enviar a requisição. Isso ajuda a identificar problemas mais cedo.

3. **Logging Detalhado**: Adicionado logging em pontos críticos para facilitar o diagnóstico de problemas:
   - Log do `documentType` antes e depois da normalização
   - Log do email antes de solicitar OTP
   - Log do request de OTP
   - Mensagens de erro claras quando validações falham

---

## 🔍 Próximos Passos

Se os testes ainda falharem após essas correções:

1. Verificar os logs detalhados para identificar o valor exato do `documentType` sendo enviado
2. Verificar se o email está sendo gerado corretamente nos testes
3. Verificar se há algum problema na substituição de placeholders do Cucumber

