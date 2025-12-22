# Resumo da Investigação - Problema de documentType

**Data**: 2025-12-22  
**Status**: 🔍 **Investigação Completa - Próximo Passo Identificado**

---

## ✅ Descobertas

### 1. Serialização está Funcionando ✅
- Teste unitário confirmou que todos os tipos de documento são serializados corretamente
- JSON gerado está correto: `{"documentType":"CPF",...}`
- Campos null são incluídos: `{"documentType":null,...}`

### 2. Processamento está Funcionando (parcialmente) ✅
- Logs mostram que `documentType` está sendo capturado corretamente do DataTable
- Normalização para uppercase está funcionando
- Request está sendo construído com `documentType` correto

### 3. Problema Identificado ❌
- Backend está rejeitando o request mesmo quando `documentType` está correto
- Erro: `"documentType: Document type must be one of: CPF, CNPJ, CUIT, DNI, RUT, CI, SSN"`
- Isso sugere que o backend está recebendo `documentType` como null, vazio, ou valor inválido

---

## 🔍 Análise dos Logs

### Logs que vimos:
```
🔍 [DEBUG] DataTable recebido - documentType original: 'CPF'
✅ [DEBUG] DocumentType normalizado de 'CPF' para 'CPF'
🔍 [UserFixture] Request final - documentType: 'CPF'
🔍 [UserFixture] Request completo: {documentType=CPF, ...}
```

### O que NÃO vimos:
- Logs para outros tipos de documento (CNPJ, CUIT, DNI, RUT, CI, SSN)
- Logs do request body no IdentityServiceClient (que adicionamos)
- Erro específico do backend mostrando o valor recebido

---

## 💡 Hipótese Principal

**O problema pode ser que o RestAssured está omitindo o campo `documentType` quando ele é null em algum momento, OU o backend está recebendo o request de forma diferente.**

Possibilidades:
1. O `documentType` está sendo definido como null em algum ponto após ser normalizado
2. O RestAssured está omitindo o campo na serialização (mas o teste unitário mostrou que não)
3. O backend está validando antes de receber o campo (improvável)
4. Há algum problema na forma como o request está sendo enviado

---

## 🎯 Próxima Ação Recomendada

**Adicionar logging no IdentityServiceClient para ver o request body ANTES de ser enviado:**

Já adicionamos o logging, mas precisamos executar os testes novamente para ver esses logs. O problema é que os testes estão demorando muito.

**Solução Rápida**: Executar apenas UM teste específico do Scenario Outline e verificar os logs completos.

---

## 📝 Correções Já Implementadas

1. ✅ Logging detalhado em todos os pontos críticos
2. ✅ Teste unitário de serialização
3. ✅ Validação de documentType no UserFixture
4. ✅ Logging no IdentityServiceClient (adicionado, mas não testado ainda)

---

## 🚀 Como Proceder

1. **Executar um teste específico** (não todos):
   ```bash
   # Executar apenas o primeiro exemplo do Scenario Outline
   mvn test -Dtest=CucumberTestRunner -Dcucumber.filter.tags="@document-country-validation" 2>&1 | grep -E "(🔍|✅|⚠️|IdentityClient|Request body|FAILURE)" | head -50
   ```

2. **Verificar os logs do IdentityServiceClient** para ver o request body antes da serialização

3. **Comparar o request body** com o que o backend está recebendo

---

## ⚠️ Observação Importante

Os testes estão demorando porque:
- Cada teste cria OTP (2-3 segundos)
- Aguarda eventos RabbitMQ (5 segundos de timeout)
- Execução sequencial

**Para acelerar**: Execute apenas um teste por vez ou reduza os timeouts temporariamente.

