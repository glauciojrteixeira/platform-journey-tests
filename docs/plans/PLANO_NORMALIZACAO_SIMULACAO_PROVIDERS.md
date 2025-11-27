# Plano de Normalização: Suporte ao Header `simulate-provider` no Platform Journey Tests

**Versão:** 1.0  
**Data:** 2025-01-27  
**Objetivo:** Normalizar o `platform-journey-tests` para suportar o header `simulate-provider` nos testes E2E

---

## 📋 Sumário

1. [Visão Geral](#visão-geral)
2. [Análise de Impacto](#análise-de-impacto)
3. [Estratégia de Implementação](#estratégia-de-implementação)
4. [Plano de Implementação](#plano-de-implementação)
5. [Configuração por Ambiente](#configuração-por-ambiente)
6. [Testes e Validação](#testes-e-validação)
7. [Checklist de Implementação](#checklist-de-implementação)

---

## 🎯 Visão Geral

### Objetivo

Atualizar o `platform-journey-tests` para suportar o header `simulate-provider` que permite simular o envio de mensagens aos providers em ambientes não-PROD (SIT, UAT, Local), evitando custos e permitindo testes de performance sem impacto financeiro.

### Contexto

- **Header:** `simulate-provider` (valores: `"true"`, `"1"`, `"false"`, `"0"` ou ausente)
- **Comportamento:** Em ambientes não-PROD, quando `true`, simula envio (não envia ao provider)
- **Segurança:** Em PROD, o header é ignorado (sempre envia ao provider)

### Fluxo de Dados

```
Teste E2E → AuthServiceClient.requestOtp() → Auth Service
   ↓
Header: simulate-provider: true (se ambiente não-PROD)
   ↓
Auth Service → RabbitMQ → Transactional Messaging Service
   ↓
Simula envio (não envia ao provider real)
```

---

## 📊 Análise de Impacto

### Endpoints Afetados

| Endpoint | Método | Cliente | Impacto | Prioridade |
|----------|--------|---------|---------|------------|
| `/api/v1/auth/otp/request` | POST | `AuthServiceClient` | 🔴 **ALTO** | 🔴 **ALTA** |
| `/api/v1/identity/users` | POST | `IdentityServiceClient` | 🟡 **MÉDIO** | 🟡 **MÉDIA** |

### Justificativa

1. **`/api/v1/auth/otp/request`** (ALTA):
   - Gera evento `otp.sent` que dispara envio de mensagens transacionais
   - Testes E2E executam múltiplas vezes → múltiplos envios → custos
   - Simulação reduz custos significativamente

2. **`/api/v1/identity/users`** (MÉDIA):
   - Gera evento `user.created.v1` que pode gerar notificações futuras
   - Impacto menor no momento, mas preparação para futuro

---

## 🔧 Estratégia de Implementação

### Abordagem: Configuração por Ambiente

**Decisão:** O header `simulate-provider` será adicionado **automaticamente** baseado no ambiente de execução:

- **Local:** ✅ Sempre adiciona `simulate-provider: true`
- **SIT:** ✅ Sempre adiciona `simulate-provider: true`
- **UAT:** ✅ Sempre adiciona `simulate-provider: true`
- **PROD:** ❌ Nunca adiciona (não deve executar testes em PROD)

### Vantagens

1. ✅ **Automático:** Não requer mudanças nos testes existentes
2. ✅ **Seguro:** Nunca simula em PROD (se testes forem executados por engano)
3. ✅ **Consistente:** Todos os testes usam simulação em ambientes não-PROD
4. ✅ **Configurável:** Pode ser desabilitado via configuração se necessário

---

## 📅 Plano de Implementação

### Fase 1: Configuração (1 dia)

#### 1.1. Atualizar `E2EConfiguration.java`

**Localização:** `src/main/java/com/nulote/journey/config/E2EConfiguration.java`

**Mudanças:**
- Adicionar propriedade `e2e.simulate-provider.enabled` (boolean)
- Adicionar método `shouldSimulateProvider()` que retorna:
  - `true` se ambiente é `local`, `sit` ou `uat`
  - `false` se ambiente é `prod` ou propriedade está desabilitada

**Código:**
```java
@ConfigurationProperties(prefix = "e2e")
public class E2EConfiguration {
    // ... campos existentes ...
    
    private SimulateProvider simulateProvider = new SimulateProvider();
    
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
    
    public static class SimulateProvider {
        private Boolean enabled;
        
        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    }
}
```

#### 1.2. Atualizar `application.yml` (todos os ambientes)

**Arquivos:**
- `src/main/resources/application.yml`
- `src/main/resources/application-local.yml`
- `src/main/resources/application-sit.yml`
- `src/main/resources/application-uat.yml`

**Mudanças:**
- Adicionar configuração `e2e.simulate-provider.enabled` (opcional, default: `true` para não-PROD)

**Código:**
```yaml
e2e:
  environment: local  # ou sit, uat
  simulate-provider:
    enabled: true  # Default: true para local/sit/uat, false para prod
  # ... outras configurações ...
```

---

### Fase 2: Atualizar Clients (1 dia)

#### 2.1. Atualizar `AuthServiceClient.java`

**Localização:** `src/test/java/com/nulote/journey/clients/AuthServiceClient.java`

**Mudanças:**
- Injetar `E2EConfiguration`
- Adicionar método privado `addSimulateProviderHeader()` que adiciona header se necessário
- Atualizar método `requestOtp()` para usar o novo método

**Código:**
```java
@Component
public class AuthServiceClient {
    
    @Autowired
    private E2EConfiguration config;
    
    // ... métodos existentes ...
    
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
}
```

#### 2.2. Atualizar `IdentityServiceClient.java`

**Localização:** `src/test/java/com/nulote/journey/clients/IdentityServiceClient.java`

**Mudanças:**
- Mesmas mudanças do `AuthServiceClient`
- Atualizar método `createUser()` para usar o novo método

**Código:**
```java
@Component
public class IdentityServiceClient {
    
    @Autowired
    private E2EConfiguration config;
    
    // ... métodos existentes ...
    
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
}
```

---

### Fase 3: Documentação e Testes (1 dia)

#### 3.1. Atualizar Documentação

**Arquivos:**
- `README.md` - Adicionar seção sobre simulação de providers
- `CONFIGURATION_SUMMARY.md` - Documentar nova configuração

#### 3.2. Testes de Validação

- ✅ Testar que header é adicionado em ambiente `local`
- ✅ Testar que header é adicionado em ambiente `sit`
- ✅ Testar que header é adicionado em ambiente `uat`
- ✅ Testar que header NÃO é adicionado se `simulate-provider.enabled=false`
- ✅ Validar que testes E2E continuam funcionando normalmente

---

## ⚙️ Configuração por Ambiente

### Local (application-local.yml)

```yaml
e2e:
  environment: local
  simulate-provider:
    enabled: true  # Sempre simular em local
  services:
    identity-url: http://localhost:8084
    auth-url: http://localhost:8080
    profile-url: http://localhost:8088
```

### SIT (application-sit.yml)

```yaml
e2e:
  environment: sit
  simulate-provider:
    enabled: true  # Sempre simular em SIT
  services:
    identity-url: ${SIT_IDENTITY_URL:http://identity-service.sit.example.com}
    auth-url: ${SIT_AUTH_URL:http://auth-service.sit.example.com}
    profile-url: ${SIT_PROFILE_URL:http://profile-service.sit.example.com}
  timeout: 60000
```

### UAT (application-uat.yml)

```yaml
e2e:
  environment: uat
  simulate-provider:
    enabled: true  # Sempre simular em UAT
  services:
    identity-url: ${UAT_IDENTITY_URL:http://identity-service.uat.example.com}
    auth-url: ${UAT_AUTH_URL:http://auth-service.uat.example.com}
    profile-url: ${UAT_PROFILE_URL:http://profile-service.uat.example.com}
  timeout: 90000
```

### PROD (application-prod.yml) - Se Existir

```yaml
e2e:
  environment: prod
  simulate-provider:
    enabled: false  # NUNCA simular em PROD
  # ... outras configurações ...
```

> ⚠️ **IMPORTANTE:** Testes E2E **NÃO devem ser executados em PROD**. Esta configuração é apenas uma camada adicional de segurança.

---

## 🧪 Testes e Validação

### Testes Unitários

#### AuthServiceClientTest

```java
@Test
void shouldAddSimulateProviderHeaderWhenEnabled() {
    // Given
    when(config.shouldSimulateProvider()).thenReturn(true);
    
    // When
    Response response = authClient.requestOtp(request);
    
    // Then
    // Verificar que header foi adicionado (via mock ou spy)
    verify(requestSpec).header("simulate-provider", "true");
}

@Test
void shouldNotAddSimulateProviderHeaderWhenDisabled() {
    // Given
    when(config.shouldSimulateProvider()).thenReturn(false);
    
    // When
    Response response = authClient.requestOtp(request);
    
    // Then
    // Verificar que header NÃO foi adicionado
    verify(requestSpec, never()).header("simulate-provider", anyString());
}
```

### Testes de Integração

#### Validar Header em Requisições Reais

```java
@Test
@SpringBootTest
class AuthServiceClientIntegrationTest {
    
    @Autowired
    private AuthServiceClient authClient;
    
    @Test
    void shouldIncludeSimulateProviderHeaderInRequest() {
        // Executar requisição real
        Response response = authClient.requestOtp(request);
        
        // Verificar logs ou interceptar requisição HTTP
        // Header deve estar presente em ambientes não-PROD
    }
}
```

### Validação Manual

#### Teste via Logs

1. Executar teste E2E em ambiente local
2. Verificar logs do Auth Service:
   ```
   DEBUG OtpController - OTP request received for user: ..., simulate=true
   ```
3. Verificar logs do Transactional Messaging Service:
   ```
   WARN SendGridEmailAdapter - SIMULATED: Email would be sent to...
   ```

---

## ✅ Checklist de Implementação

### Fase 1: Configuração
- [ ] Atualizar `E2EConfiguration.java` com método `shouldSimulateProvider()`
- [ ] Adicionar propriedade `simulate-provider.enabled` em `application.yml`
- [ ] Adicionar propriedade em `application-local.yml`
- [ ] Adicionar propriedade em `application-sit.yml`
- [ ] Adicionar propriedade em `application-uat.yml`
- [ ] Validar que configuração é carregada corretamente

### Fase 2: Clients
- [ ] Atualizar `AuthServiceClient.requestOtp()` para adicionar header
- [ ] Atualizar `IdentityServiceClient.createUser()` para adicionar header
- [ ] Adicionar método `addSimulateProviderHeader()` em ambos os clients
- [ ] Validar que header é adicionado corretamente

### Fase 3: Testes
- [ ] Criar testes unitários para `shouldSimulateProvider()`
- [ ] Criar testes unitários para `addSimulateProviderHeader()`
- [ ] Validar que testes E2E existentes continuam funcionando
- [ ] Executar suite completa de testes em ambiente local
- [ ] Executar suite completa de testes em ambiente SIT (se disponível)

### Fase 4: Documentação
- [ ] Atualizar `README.md` com seção sobre simulação
- [ ] Atualizar `CONFIGURATION_SUMMARY.md`
- [ ] Adicionar exemplos de uso
- [ ] Documentar comportamento por ambiente

### Fase 5: Validação Final
- [ ] Validar que header é propagado corretamente
- [ ] Validar que simulação funciona em ambientes não-PROD
- [ ] Validar que simulação é ignorada em PROD (se aplicável)
- [ ] Verificar logs de simulação nos serviços
- [ ] Confirmar que custos são reduzidos (não há envios reais)

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

# Via application-local.yml (temporário)
e2e:
  simulate-provider:
    enabled: false
```

---

## 🔐 Considerações de Segurança

### Validação de Ambiente

- ✅ **Nunca simular em PROD:** Lógica de validação no `E2EConfiguration`
- ✅ **Configuração explícita:** Pode ser desabilitada via propriedade
- ✅ **Logs:** Registrar quando simulação está habilitada

### Comportamento Esperado

| Ambiente | `simulate-provider.enabled` | Comportamento |
|----------|----------------------------|---------------|
| **local** | `true` (default) | ✅ Adiciona header |
| **sit** | `true` (default) | ✅ Adiciona header |
| **uat** | `true` (default) | ✅ Adiciona header |
| **prod** | `false` (forçado) | ❌ Nunca adiciona header |
| **qualquer** | `false` (explícito) | ❌ Não adiciona header |

---

## 📊 Benefícios

### Redução de Custos

- ✅ **Testes E2E executam múltiplas vezes** → múltiplos envios de OTP
- ✅ **Simulação elimina custos** de envio real aos providers
- ✅ **Permite execução frequente** sem preocupação com custos

### Melhoria de Performance

- ✅ **Testes mais rápidos** (não espera envio real)
- ✅ **Menos dependências externas** (não depende de providers reais)
- ✅ **Execução mais confiável** (não falha por problemas de rede com providers)

### Facilidade de Testes

- ✅ **Testes podem ser executados sem credenciais de providers**
- ✅ **Não requer configuração de providers externos**
- ✅ **Permite testes de carga sem custos**

---

## 🔗 Referências

- [Estratégia de Simulação de Providers](../VS-CustomerCommunications/transactional-messaging-service/ESTRATEGIA_SIMULACAO_PROVIDERS.md)
- [Plano de Implementação VS Identity](../VS-Identity/PLANO_IMPLEMENTACAO_SIMULACAO_PROVIDERS.md)
- [Documentação RabbitMQ - Headers](https://www.rabbitmq.com/headers.html)

---

## 📅 Timeline

| Fase | Atividades | Duração |
|------|------------|---------|
| **Fase 1** | Configuração | 1 dia |
| **Fase 2** | Atualizar Clients | 1 dia |
| **Fase 3** | Documentação e Testes | 1 dia |
| **TOTAL** | | **3 dias úteis** |

---

**Próximos Passos:** Iniciar Fase 1 (Configuração) e criar branch de feature.

