# Análise do Problema de documentType

**Data**: 2025-12-22  
**Status**: 🔍 **Investigação em Andamento**

---

## 📊 Observações dos Logs

### ✅ O que está funcionando:

1. **Processamento do documentType**: Os logs mostram que o `documentType` está sendo capturado corretamente do DataTable:
   ```
   🔍 [DEBUG] DataTable recebido - documentType original: 'CPF'
   ✅ [DEBUG] DocumentType normalizado de 'CPF' para 'CPF'
   ✅ [DEBUG] DocumentType preservado do DataTable/Examples: 'CPF'
   ```

2. **Construção do Request**: O request está sendo construído corretamente:
   ```
   🔍 [UserFixture] Request final - documentType: 'CPF' (documentTypeObj: 'CPF')
   🔍 [UserFixture] Request completo: {documentType=CPF, documentNumber=..., name=...}
   ```

### ❌ O problema:

O backend está retornando erro mesmo quando o `documentType` está sendo enviado corretamente:
```
"documentType: Document type must be one of: CPF, CNPJ, CUIT, DNI, RUT, CI, SSN"
```

---

## 🔍 Hipóteses

### Hipótese 1: RestAssured omitindo campos null
**Possibilidade**: O RestAssured pode estar omitindo o campo `documentType` quando ele é `null` na serialização JSON, fazendo com que o backend receba o campo ausente (não null, mas ausente).

**Evidência**: Os logs mostram que o `documentType` está correto no Map, mas o backend rejeita.

**Correção aplicada**: Adicionado log final para verificar se o `documentType` ainda está presente no request após adicionar todos os campos.

### Hipótese 2: Problema no Scenario Outline
**Possibilidade**: O Cucumber pode não estar substituindo corretamente os valores do Examples (`<document_type>`) para todos os tipos de documento (CNPJ, CUIT, DNI, RUT, CI, SSN).

**Evidência**: Os logs mostram apenas CPF sendo processado, não vemos logs para outros tipos.

**Ação necessária**: Executar testes específicos para cada tipo de documento e verificar os logs.

### Hipótese 3: Serialização JSON
**Possibilidade**: O RestAssured pode estar serializando o Map de forma que o `documentType` não está sendo incluído no JSON final, mesmo que esteja no Map.

**Evidência**: O request Map tem o `documentType`, mas o backend não recebe.

**Correção aplicada**: Adicionado logging no `IdentityServiceClient` para ver o request body antes da serialização.

---

## 🔧 Correções Implementadas

### 1. Logging Detalhado
- ✅ Log do DataTable recebido
- ✅ Log do documentType em cada etapa de processamento
- ✅ Log do request final completo
- ✅ Log do request body antes da serialização no IdentityServiceClient

### 2. Validação Adicional
- ✅ Verificação se documentType ainda está presente no request após adicionar todos os campos
- ✅ Log final para confirmar o valor do documentType no request

---

## 🧪 Próximos Passos

1. **Executar testes específicos** para cada tipo de documento do Scenario Outline:
   ```bash
   mvn test -Dtest=CucumberTestRunner -Dcucumber.filter.tags="@document-country-validation"
   ```

2. **Verificar logs específicos** para cada tipo:
   - Procurar por logs de CNPJ, CUIT, DNI, RUT, CI, SSN
   - Verificar se o documentType está sendo capturado corretamente para cada tipo

3. **Verificar serialização JSON**:
   - Ver os logs do IdentityServiceClient mostrando o request body
   - Confirmar se o documentType está presente no JSON

---

## 📝 Notas Técnicas

### Como o RestAssured Serializa Maps

O RestAssured usa Jackson por padrão para serializar Maps em JSON. Por padrão:
- Campos `null` podem ser omitidos dependendo da configuração
- Campos vazios são incluídos
- Strings são incluídas normalmente

### Possível Solução

Se o problema for que campos `null` estão sendo omitidos, podemos:
1. Configurar o RestAssured para incluir campos null
2. Usar um ObjectMapper customizado
3. Garantir que o documentType nunca seja null quando deveria ter um valor

---

## ⚠️ Observação Importante

Os testes estão demorando muito porque:
1. Cada teste precisa criar OTP e validar
2. Cada teste precisa aguardar eventos RabbitMQ
3. Os testes são executados sequencialmente

Para acelerar a investigação, podemos:
- Executar apenas um teste específico por vez
- Reduzir timeouts de RabbitMQ temporariamente
- Adicionar mais logging para identificar o problema rapidamente

