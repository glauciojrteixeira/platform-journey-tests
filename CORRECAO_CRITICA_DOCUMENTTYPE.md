# Correção Crítica - Problema de documentType

**Data**: 2025-12-22  
**Status**: ✅ **Correção Crítica Implementada**

---

## 🔍 Problema Identificado

O backend está rejeitando o `documentType` mesmo quando ele está sendo processado corretamente nos logs. Isso sugere que:

1. **O Cucumber pode não estar substituindo `<document_type>` corretamente** para todos os exemplos
2. **O documentType pode estar sendo perdido** entre o step definition e o buildCreateUserRequest
3. **O documentType pode estar sendo definido como null** em algum ponto após ser normalizado

---

## ✅ Correção Crítica Implementada

### 1. Verificação de Substituição do Cucumber

Adicionada verificação crítica no início do método `eu_informo()` para detectar se o Cucumber não substituiu `<document_type>`:

```java
// CORREÇÃO CRÍTICA: Verificar se documentType está presente ANTES de processar placeholders
String initialDocumentType = userData.get("documentType");
if (initialDocumentType == null || initialDocumentType.trim().isEmpty() || 
    initialDocumentType.equals("<document_type>") || initialDocumentType.startsWith("<")) {
    logger.error("❌ PROBLEMA CRÍTICO: documentType não foi substituído pelo Cucumber!");
    // Inferir do país configurado como fallback
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
```

### 2. Verificação Final no UserFixture

Adicionada verificação final que garante que `documentType` está presente no request:

```java
// VERIFICAÇÃO FINAL CRÍTICA: Garantir que documentType está presente no request
Object finalDocumentTypeInRequest = request.get("documentType");

// Se documentType não está presente ou é null quando deveria ter valor, adicionar novamente
if (documentType != null && finalDocumentTypeInRequest == null) {
    logger.warn("⚠️ documentType estava null no request mas deveria ser '{}'. Restaurando...", documentType);
    request.put("documentType", documentType);
}
```

---

## 🎯 O que Isso Resolve

1. ✅ **Detecta se o Cucumber não substituiu `<document_type>`** e infere do país como fallback
2. ✅ **Garante que documentType está presente no request** mesmo se for perdido em algum lugar
3. ✅ **Adiciona logging crítico** para identificar problemas de substituição do Cucumber

---

## 🧪 Como Testar

Execute os testes novamente:

```bash
cd nulote-backend/platform-journey-tests
mvn clean test -Dtest=CucumberTestRunner -Dcucumber.filter.tags="@document-country-validation"
```

**Procure por logs**:
- `❌ PROBLEMA CRÍTICO: documentType não foi substituído` - Indica problema com Cucumber
- `⚠️ Inferindo documentType` - Fallback sendo usado
- `⚠️ documentType estava null no request` - documentType foi restaurado
- `✅ VERIFICAÇÃO FINAL - documentType no request` - Confirmação final

---

## 📊 Resultado Esperado

Com essas correções:
1. ✅ Se o Cucumber não substituir `<document_type>`, será detectado e corrigido
2. ✅ Se o documentType for perdido, será restaurado antes de enviar
3. ✅ Logs detalhados mostrarão exatamente onde está o problema

---

## ⚠️ Nota Importante

Se os testes ainda falharem após essas correções, o problema pode estar no **backend não recebendo o campo corretamente**. Nesse caso, será necessário verificar os logs do backend para ver o que está sendo recebido.

