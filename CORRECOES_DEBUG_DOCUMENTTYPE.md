# Correções e Debugging de documentType

**Data**: 2025-12-22  
**Status**: 🔍 **Debugging em Andamento**

---

## 📋 Problemas Identificados

### 1. documentType do Scenario Outline não está sendo capturado (7 falhas)

**Erro**: `"Document type must be one of: CPF, CNPJ, CUIT, DNI, RUT, CI, SSN"`

**Cenários Afetados**:
- Example #1.1 até #1.7 (Scenario Outline: Documentos devem ser validados de acordo com o país configurado)

**Causa Suspeita**: 
- O Cucumber substitui `<document_type>` do Examples ANTES de passar para o step definition
- O valor pode estar sendo processado incorretamente ou não está sendo capturado do DataTable

### 2. registration-token ausente (1 falha)

**Erro**: `"registration-token header is required for user registration"`

**Cenário Afetado**:
- Criar usuário B2C com RUT válido terminando em K (Chile)

### 3. Teste de validação passando quando deveria falhar (1 falha)

**Erro**: Teste "Criar usuário sem documentType deve falhar" está criando usuário com CPF quando não deveria

**Causa**: A inferência automática de documentType do país estava sendo aplicada mesmo em testes de validação

---

## 🔧 Correções Implementadas

### 1. Logging Detalhado Adicionado

Adicionado logging em pontos críticos para diagnosticar o problema:

**Em `AuthenticationSteps.eu_informo()`**:
- Log do DataTable recebido
- Log do documentType antes e depois da normalização
- Log após processar placeholders
- Log quando documentType é preservado ou definido como null

**Em `UserFixture.buildCreateUserRequest()`**:
- Log do documentTypeObj recebido
- Log do userData completo
- Log do documentType após processamento
- Log do request final completo

### 2. Removida Inferência Automática do País

**Antes**: O código inferia documentType do país quando ausente, o que quebrava testes de validação

**Depois**: O documentType só é usado se estiver explicitamente presente no DataTable ou Examples

```java
// ANTES (removido):
if (normalizedDocumentType == null || normalizedDocumentType.trim().isEmpty()) {
    // Inferir do país
    if (config != null) {
        String countryCode = config.getDefaultCountryCode();
        String inferredType = null;
        switch (countryCode) {
            case "BR": inferredType = "CPF"; break;
            // ...
        }
        if (inferredType != null) {
            userData.put("documentType", inferredType);
        }
    }
}

// DEPOIS:
if (normalizedDocumentType == null || normalizedDocumentType.trim().isEmpty()) {
    // NÃO inferir - deixar null para que o backend valide
    userData.put("documentType", null);
    logger.warn("⚠️ DocumentType está ausente no DataTable - mantendo como null");
}
```

### 3. Melhorada Validação de Email antes de OTP

Adicionada validação explícita de email antes de solicitar OTP para garantir que o registration-token seja criado corretamente.

---

## 🧪 Como Diagnosticar

Execute os testes novamente e verifique os logs:

```bash
cd nulote-backend/platform-journey-tests
mvn clean test -Dtest=CucumberTestRunner -Dcucumber.filter.tags="@document-country-validation"
```

Procure por logs com prefixo `🔍 [DEBUG]` e `✅ [DEBUG]` para ver:
1. O que está sendo recebido no DataTable
2. Como o documentType está sendo processado
3. O que está sendo enviado no request final

---

## 🔍 Próximos Passos

1. **Executar testes com logging** para ver o que está acontecendo
2. **Verificar logs** para identificar onde o documentType está sendo perdido
3. **Ajustar código** baseado nos logs obtidos

---

## 📝 Notas Técnicas

### Como o Cucumber Processa Scenario Outline

Quando o Cucumber processa um Scenario Outline:

1. **Substituição de Placeholders**: O Cucumber substitui `<document_type>` do Examples ANTES de passar para o step definition
2. **DataTable**: O DataTable recebido no step definition já tem os valores substituídos
3. **Exemplo**: Se o Examples tem `| CPF |`, o DataTable recebido terá `documentType: "CPF"`

### Possíveis Problemas

1. **Valor vazio**: O Cucumber pode estar substituindo com string vazia
2. **Espaços em branco**: O valor pode ter espaços extras
3. **Case sensitivity**: O valor pode estar em lowercase
4. **Null vs String "null"**: O valor pode ser a string "null" em vez de null

Os logs adicionados devem revelar qual é o problema real.

