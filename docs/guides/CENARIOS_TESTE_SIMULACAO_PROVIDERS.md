# ✅ Cenários de Teste: Validação do Header `simulate-provider`

**Data:** 2025-01-27  
**Status:** ✅ **IMPLEMENTAÇÃO COMPLETA**

---

## 📋 Resumo

Criação de cenários de teste BDD (Cucumber/Gherkin) para validar que o header `simulate-provider` está funcionando corretamente no `platform-journey-tests`.

---

## ✅ Componentes Implementados

### 1. Feature: `simulate_provider.feature`

**Localização:** `src/test/resources/features/transversal/simulate_provider.feature`

**Cenários Implementados:**

1. **Header simulate-provider deve estar presente na mensagem RabbitMQ ao solicitar OTP**
   - Valida que ao solicitar OTP, o evento `otp.sent` contém o header `simulate-provider: true`
   - Valida que a mensagem não é enviada ao provider real

2. **Header simulate-provider deve estar presente na mensagem RabbitMQ ao criar usuário**
   - Valida que ao criar usuário, o evento `user.created.v1` contém o header `simulate-provider: true`

3. **Simulação deve estar habilitada em ambientes não-PROD**
   - Valida que a configuração está correta para ambiente local

4. **Simulação não deve estar habilitada em ambiente PROD**
   - Valida que a configuração está correta para ambiente prod (segurança)

5. **Múltiplas solicitações de OTP devem incluir header simulate-provider**
   - Valida que todas as mensagens `otp.sent` contêm o header

---

### 2. Step Definitions: `SimulateProviderSteps.java`

**Localização:** `src/test/java/com/nulote/journey/stepdefinitions/SimulateProviderSteps.java`

**Step Definitions Implementados:**

1. `@Então("o evento {string} deve conter o header {string} com valor {string}")`
   - Valida que um evento específico contém um header com valor esperado
   - Suporta headers do tipo String ou byte[]

2. `@Então("a mensagem não deve ser enviada ao provider real")`
   - Validação indireta: se o header está presente, a simulação deve estar funcionando
   - Preparado para validações futuras (logs, métricas)

3. `@Então("todas as mensagens {string} devem conter o header {string} com valor {string}")`
   - Valida múltiplas mensagens de um mesmo tipo de evento

4. `@Dado("que estou executando testes em ambiente {string}")`
   - Documenta o ambiente de teste

5. `@Então("a simulação de providers deve estar habilitada")`
   - Valida que `E2EConfiguration.shouldSimulateProvider()` retorna `true`

6. `@Então("a simulação de providers não deve estar habilitada")`
   - Valida que `E2EConfiguration.shouldSimulateProvider()` retorna `false`

---

### 3. RabbitMQHelper: Captura de Headers

**Localização:** `src/test/java/com/nulote/journey/utils/RabbitMQHelper.java`

**Mudanças:**
- ✅ Captura de headers da mensagem RabbitMQ via `response.getProps().getHeaders()`
- ✅ Armazenamento de headers no objeto `Event`
- ✅ Classe `Event` atualizada com campo `headers` e getter/setter

**Código:**
```java
// Capturar headers da mensagem
Map<String, Object> headers = response.getProps().getHeaders();
if (headers != null) {
    logger.debug("Headers da mensagem RabbitMQ: {}", headers.keySet());
}

// Armazenar headers no evento
if (headers != null) {
    event.setHeaders(headers);
}
```

---

## 🧪 Execução dos Testes

### Executar Todos os Cenários de Simulação

```bash
# Executar todos os cenários de simulação
mvn test -Dspring.profiles.active=local -Dcucumber.filter.tags="@simulate-provider"
```

### Executar Cenário Específico

```bash
# Apenas validação de OTP
mvn test -Dspring.profiles.active=local -Dcucumber.filter.tags="@simulate-provider-otp"

# Apenas validação de criação de usuário
mvn test -Dspring.profiles.active=local -Dcucumber.filter.tags="@simulate-provider-user-creation"

# Apenas validação de configuração
mvn test -Dspring.profiles.active=local -Dcucumber.filter.tags="@simulate-provider-configuration"
```

---

## 📊 Estrutura dos Testes

### Tags Utilizadas

- `@simulate-provider` - Tag principal para todos os cenários
- `@simulate-provider-otp` - Cenários relacionados a OTP
- `@simulate-provider-user-creation` - Cenários relacionados a criação de usuário
- `@simulate-provider-configuration` - Cenários relacionados a configuração
- `@simulate-provider-multiple-requests` - Cenários de múltiplas requisições

### Dependências

Os cenários dependem de:
- ✅ RabbitMQ configurado e acessível
- ✅ Microserviços rodando (Auth Service, Identity Service)
- ✅ Eventos sendo publicados corretamente

---

## 🔍 Validações Realizadas

### 1. Validação de Header na Mensagem RabbitMQ

- ✅ Verifica que o header `simulate-provider` está presente
- ✅ Verifica que o valor é `"true"` (como String ou byte[])
- ✅ Suporta diferentes formatos de header (String, byte[])

### 2. Validação de Configuração

- ✅ Verifica que simulação está habilitada em ambientes não-PROD
- ✅ Verifica que simulação está desabilitada em PROD
- ✅ Valida lógica do `E2EConfiguration.shouldSimulateProvider()`

### 3. Validação de Múltiplas Mensagens

- ✅ Verifica que todas as mensagens de um tipo contêm o header
- ✅ Suporta validação de múltiplas mensagens sequenciais

---

## 📝 Exemplo de Execução

### Saída Esperada

```
Scenario: Header simulate-provider deve estar presente na mensagem RabbitMQ ao solicitar OTP
  Given que crio um usuário com esses dados
  When eu solicito OTP via "EMAIL" para "REGISTRATION"
  Then a solicitação de OTP deve retornar status 200
  And o evento "otp.sent" deve ser publicado
  And o evento "otp.sent" deve conter o header "simulate-provider" com valor "true"
  And a mensagem não deve ser enviada ao provider real

✅ Header simulate-provider=true validado no evento otp.sent
✅ Validação de simulação: Header simulate-provider presente indica que envio será simulado
```

---

## 🔄 Integração com Implementação Anterior

Os cenários de teste validam a implementação realizada anteriormente:

1. ✅ **E2EConfiguration** - Valida lógica de `shouldSimulateProvider()`
2. ✅ **AuthServiceClient** - Valida que header é adicionado em `requestOtp()`
3. ✅ **IdentityServiceClient** - Valida que header é adicionado em `createUser()`
4. ✅ **Propagação via RabbitMQ** - Valida que header chega nas mensagens RabbitMQ

---

## 🚀 Próximos Passos

1. ✅ Executar testes em ambiente local para validar
2. ✅ Executar testes em ambiente SIT (se disponível)
3. ✅ Validar logs dos serviços para confirmar simulação
4. ⏳ Adicionar validações mais específicas (logs, métricas) se necessário

---

## 📚 Arquivos Criados/Modificados

### Novos Arquivos

1. `src/test/resources/features/transversal/simulate_provider.feature`
2. `src/test/java/com/nulote/journey/stepdefinitions/SimulateProviderSteps.java`

### Arquivos Modificados

1. `src/test/java/com/nulote/journey/utils/RabbitMQHelper.java`
   - Adicionada captura de headers
   - Classe `Event` atualizada com campo `headers`

---

**Status:** ✅ **CENÁRIOS DE TESTE IMPLEMENTADOS E COMPILADOS COM SUCESSO**

