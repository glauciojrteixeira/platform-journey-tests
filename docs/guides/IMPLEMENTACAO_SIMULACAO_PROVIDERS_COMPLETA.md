# ✅ Implementação Completa: Suporte ao Header `simulate-provider` no Platform Journey Tests

**Data:** 2025-01-27  
**Status:** ✅ **IMPLEMENTAÇÃO COMPLETA**

---

## 📋 Resumo

Implementação completa do suporte ao header `simulate-provider` no `platform-journey-tests`. O header é adicionado automaticamente em todas as requisições que geram mensagens transacionais (OTP e criação de usuário), permitindo simular o envio de mensagens aos providers em ambientes não-PROD.

---

## ✅ Componentes Implementados

### 1. E2EConfiguration.java

**Localização:** `src/main/java/com/nulote/journey/config/E2EConfiguration.java`

**Mudanças:**
- ✅ Adicionado campo `simulateProvider` (classe interna `SimulateProvider`)
- ✅ Adicionado método `shouldSimulateProvider()` com lógica:
  - Nunca simular em PROD (segurança)
  - Respeitar configuração explícita (`simulate-provider.enabled`)
  - Default: simular em ambientes não-PROD (local, sit, uat)

**Código:**
```java
public boolean shouldSimulateProvider() {
    // Nunca simular em PROD
    if ("prod".equalsIgnoreCase(environment)) {
        return false;
    }
    
    // Respeitar configuração explícita
    if (simulateProvider.getEnabled() != null) {
        return simulateProvider.getEnabled();
    }
    
    // Default: simular em ambientes não-PROD
    return true;
}
```

---

### 2. Configurações YAML

#### application.yml
- ✅ Adicionada propriedade `e2e.simulate-provider.enabled: true` (default)

#### application-local.yml
- ✅ Adicionada propriedade `e2e.simulate-provider.enabled: true`

#### application-sit.yml
- ✅ Adicionada propriedade `e2e.simulate-provider.enabled: true`

#### application-uat.yml
- ✅ Adicionada propriedade `e2e.simulate-provider.enabled: true`

---

### 3. AuthServiceClient.java

**Localização:** `src/test/java/com/nulote/journey/clients/AuthServiceClient.java`

**Mudanças:**
- ✅ Adicionado método privado `addSimulateProviderHeader()`
- ✅ Atualizado método `requestOtp()` para adicionar header automaticamente
- ✅ Import adicionado: `io.restassured.specification.RequestSpecification`

**Código:**
```java
private RequestSpecification addSimulateProviderHeader(RequestSpecification spec) {
    if (config.shouldSimulateProvider()) {
        spec = spec.header("simulate-provider", "true");
    }
    return spec;
}

public Response requestOtp(Object request) {
    RequestSpecification spec = RestAssured.given()
        .baseUri(getBaseUrl())
        .contentType(ContentType.JSON)
        .header("request-trace-id", getRequestTraceId());
    
    spec = addSimulateProviderHeader(spec);
    
    return spec.body(request)
        .when()
        .post("/api/v1/auth/otp/request")
        .then()
        .extract()
        .response();
}
```

---

### 4. IdentityServiceClient.java

**Localização:** `src/test/java/com/nulote/journey/clients/IdentityServiceClient.java`

**Mudanças:**
- ✅ Adicionado método privado `addSimulateProviderHeader()`
- ✅ Atualizado método `createUser()` para adicionar header automaticamente
- ✅ Import adicionado: `io.restassured.specification.RequestSpecification`

**Código:**
```java
private RequestSpecification addSimulateProviderHeader(RequestSpecification spec) {
    if (config.shouldSimulateProvider()) {
        spec = spec.header("simulate-provider", "true");
    }
    return spec;
}

public Response createUser(Object request) {
    RequestSpecification spec = RestAssured.given()
        .baseUri(getBaseUrl())
        .contentType(ContentType.JSON)
        .header("request-trace-id", getRequestTraceId());
    
    spec = addSimulateProviderHeader(spec);
    
    return spec.body(request)
        .when()
        .post("/api/v1/identity/users")
        .then()
        .extract()
        .response();
}
```

---

## 🔄 Fluxo de Dados Implementado

```
Teste E2E → AuthServiceClient.requestOtp()
    ↓
E2EConfiguration.shouldSimulateProvider() → true (em local/sit/uat)
    ↓
addSimulateProviderHeader() → adiciona header "simulate-provider: true"
    ↓
HTTP Request → POST /api/v1/auth/otp/request
    Header: simulate-provider: true
    ↓
Auth Service → processa e propaga flag
    ↓
RabbitMQ → mensagem com header simulate-provider: true
    ↓
Transactional Messaging Service → simula envio (não envia ao provider)
```

---

## 📊 Arquivos Modificados

1. `src/main/java/com/nulote/journey/config/E2EConfiguration.java`
2. `src/main/resources/application.yml`
3. `src/main/resources/application-local.yml`
4. `src/main/resources/application-sit.yml`
5. `src/main/resources/application-uat.yml`
6. `src/test/java/com/nulote/journey/clients/AuthServiceClient.java`
7. `src/test/java/com/nulote/journey/clients/IdentityServiceClient.java`

---

## ✅ Compatibilidade

- ✅ **Zero mudanças nos testes existentes:** Header é adicionado automaticamente
- ✅ **Configurável:** Pode ser desabilitado via propriedade `e2e.simulate-provider.enabled=false`
- ✅ **Seguro:** Nunca simula em PROD (validação no `E2EConfiguration`)

---

## 🧪 Validação

### Compilação

- ✅ **Status:** BUILD SUCCESS
- ✅ **Arquivos compilados:** 2 source files
- ✅ **Tempo:** 11.953s
- ✅ **Sem erros de compilação**

### Próximos Passos (Validação)

- [ ] Executar testes E2E em ambiente local
- [ ] Verificar que header é adicionado nas requisições
- [ ] Validar que simulação funciona corretamente
- [ ] Verificar logs dos serviços (Auth Service e Transactional Messaging Service)

---

## 📝 Exemplos de Uso

### Execução Normal (Com Simulação Automática)

```bash
# Local - Simulação automática habilitada
mvn test -Dspring.profiles.active=local

# SIT - Simulação automática habilitada
mvn test -Dspring.profiles.active=sit

# UAT - Simulação automática habilitada
mvn test -Dspring.profiles.active=uat
```

### Desabilitar Simulação (Para Testes Reais)

```bash
# Via variável de ambiente
export E2E_SIMULATE_PROVIDER_ENABLED=false
mvn test -Dspring.profiles.active=local

# Ou modificar application-local.yml temporariamente
e2e:
  simulate-provider:
    enabled: false
```

---

## 🔐 Segurança

- ✅ **Validação de ambiente:** Nunca simula em PROD
- ✅ **Configuração explícita:** Pode ser desabilitada
- ✅ **Logs:** Registrar quando simulação está habilitada (futuro)

---

## 📚 Documentação Relacionada

- [PLANO_NORMALIZACAO_SIMULACAO_PROVIDERS.md](./PLANO_NORMALIZACAO_SIMULACAO_PROVIDERS.md) - Plano detalhado
- [RESUMO_EXECUTIVO_NORMALIZACAO_SIMULACAO.md](./RESUMO_EXECUTIVO_NORMALIZACAO_SIMULACAO.md) - Resumo executivo

---

**Status:** ✅ **IMPLEMENTAÇÃO COMPLETA - PRONTO PARA TESTES**

