# Análise de Logs dos Testes E2E - Arquitetura Multi-Country

**Data**: 2025-12-22  
**Contexto**: Após implementação da arquitetura multi-country com virtual hosts `/br` (VS-Identity) e `/shared` (VS-CustomerCommunications)

---

## 📊 Resumo Executivo

### Status da Execução
- **Total de testes**: 202
- **Falhas**: 7
- **Erros**: 9
- **Pulados**: 100
- **Taxa de sucesso**: ~86% (175/202)

### Problemas Identificados

#### 🔴 **CRÍTICO: Incompatibilidade de Virtual Hosts**

**Problema Principal**: Os testes E2E estão configurados para usar apenas o virtual host `/br`, mas os eventos da VS Customer Communications são publicados no virtual host `/shared`.

**Impacto**: 9 erros relacionados a eventos que não são encontrados:
- `otp.sent` (publicado em `/shared`, testes procuram em `/br`)
- `delivery.tracking.created.v1` (publicado em `/shared`, testes procuram em `/br`)
- `callback.received` (publicado em `/shared`, testes procuram em `/br`)

---

## 🔍 Análise Detalhada

### 1. Erros de Timeout em Eventos RabbitMQ

#### Eventos Afetados

| Evento | Exchange | Fila Esperada | VHost Correto | VHost Teste | Status |
|--------|----------|---------------|---------------|-------------|--------|
| `otp.sent` | `auth.events` | `transactional.auth-otp-sent.queue` | `/shared` | `/br` | ❌ |
| `delivery.tracking.created.v1` | `delivery-tracker.events` | `delivery-tracker.delivery-tracking-created.queue` | `/shared` | `/br` | ❌ |
| `callback.received` | `delivery-tracker.delivery-callbacks` | `delivery-tracker.delivery-callbacks.queue` | `/shared` | `/br` | ❌ |

#### Cenários de Teste Afetados

1. **Múltiplos OTPs simultâneos - Processamento assíncrono correto**
   - Erro: Timeout ao aguardar eventos `otp.sent`
   - Causa: Eventos publicados em `/shared`, testes procuram em `/br`

2. **Falha no Transactional Messaging Service - Evento deve ir para DLQ**
   - Erro: Timeout ao aguardar evento `otp.sent`
   - Causa: Eventos publicados em `/shared`, testes procuram em `/br`

3. **Timeout no envio de email - Retry automático**
   - Erro: Timeout ao aguardar evento `otp.sent`
   - Causa: Eventos publicados em `/shared`, testes procuram em `/br`

4. **Múltiplos eventos OTP - Ordem de processamento preservada**
   - Erro: Timeout ao aguardar eventos `otp.sent`
   - Causa: Eventos publicados em `/shared`, testes procuram em `/br`

5. **Idempotência no processamento de eventos OTP**
   - Erro: Timeout ao aguardar evento `otp.sent`
   - Causa: Eventos publicados em `/shared`, testes procuram em `/br`

6. **Envio de OTP via Email - Fluxo Cross-VS Completo (PASSWORD_RECOVERY)**
   - Erro: Timeout ao aguardar evento `delivery.tracking.created.v1`
   - Causa: Eventos publicados em `/shared`, testes procuram em `/br`

7. **Envio de OTP via Email - Fluxo Cross-VS Completo (REGISTRATION)**
   - Erro: Timeout ao aguardar evento `delivery.tracking.created.v1`
   - Causa: Eventos publicados em `/shared`, testes procuram em `/br`

8. **Delivery Tracker recebe webhook do SendGrid e atualiza status**
   - Erro: Timeout ao aguardar evento `callback.received`
   - Causa: Eventos publicados em `/shared`, testes procuram em `/br`

9. **Consumir evento otp.sent e processar envio de OTP via Email**
   - Erro: Timeout ao aguardar evento `delivery.tracking.created.v1`
   - Causa: Eventos publicados em `/shared`, testes procuram em `/br`

---

### 2. Falhas de Validação (Não relacionadas a Multi-Country)

#### Documentos Multi-Country (7 falhas)
- **Problema**: Testes esperando status 200/201, mas recebendo 400
- **Causa**: Validação de `documentType` não aceita alguns tipos de documento
- **Erro**: `"Document type must be one of: CPF, CNPJ, CUIT, DNI, RUT, CI, SSN"`
- **Status**: Não relacionado à arquitetura multi-country

---

## 🔧 Análise Técnica

### Configuração Atual dos Testes E2E

**Arquivo**: `src/main/resources/application-local.yml`

```yaml
rabbitmq:
  virtual-host: ${RABBITMQ_VIRTUAL_HOST:/br}  # Default: "/br"
```

**Problema**: Configuração única para todos os eventos, mas agora temos:
- VS-Identity: eventos em `/br`
- VS-CustomerCommunications: eventos em `/shared`

### Código Atual do RabbitMQHelper

**Arquivo**: `src/test/java/com/nulote/journey/utils/RabbitMQHelper.java`

```java
@Value("${rabbitmq.virtual-host:/}")
private String virtualHost;

private String determineVirtualHost() {
    // Se virtual host foi configurado explicitamente, usar
    if (virtualHost != null && !virtualHost.isEmpty() && !virtualHost.equals("/")) {
        return virtualHost;  // Sempre retorna "/br" para todos os eventos
    }
    // ...
}
```

**Problema**: Usa um único virtual host para todas as conexões, não considera o tipo de evento.

---

## 💡 Solução Proposta

### Opção 1: Múltiplas Conexões por Virtual Host (Recomendada)

Modificar `RabbitMQHelper` para manter conexões separadas para cada virtual host:

```java
private Map<String, Connection> connections = new HashMap<>();
private Map<String, Channel> channels = new HashMap<>();

private String determineVirtualHostForEvent(String eventType) {
    // Eventos VS-Identity -> /br
    if (isVSIdentityEvent(eventType)) {
        return "/br";
    }
    // Eventos VS-CustomerCommunications -> /shared
    if (isVSCustomerCommunicationsEvent(eventType)) {
        return "/shared";
    }
    // Fallback para configuração padrão
    return determineVirtualHost();
}

private boolean isVSIdentityEvent(String eventType) {
    return eventType.equals("user.created.v1") ||
           eventType.equals("credentials.provisioned.v1") ||
           eventType.equals("otp.validated");
}

private boolean isVSCustomerCommunicationsEvent(String eventType) {
    return eventType.equals("otp.sent") ||
           eventType.equals("delivery.tracking.created.v1") ||
           eventType.equals("callback.received") ||
           eventType.equals("welcome.message.sent");
}
```

### Opção 2: Configuração Dinâmica por Evento

Adicionar mapeamento de eventos para virtual hosts:

```yaml
rabbitmq:
  virtual-host: ${RABBITMQ_VIRTUAL_HOST:/br}
  event-vhost-mapping:
    # VS-Identity events
    user.created.v1: /br
    credentials.provisioned.v1: /br
    otp.validated: /br
    # VS-CustomerCommunications events
    otp.sent: /shared
    delivery.tracking.created.v1: /shared
    callback.received: /shared
    welcome.message.sent: /shared
```

### Opção 3: Variável de Ambiente por Tipo de Teste

Permitir sobrescrever virtual host por tipo de teste:

```bash
# Para testes VS-Identity
RABBITMQ_VIRTUAL_HOST=/br mvn test -Dcucumber.filter.tags="@vs-identity"

# Para testes VS-CustomerCommunications
RABBITMQ_VIRTUAL_HOST=/shared mvn test -Dcucumber.filter.tags="@vs-customer-communications"

# Para testes Cross-VS (requer ambos)
# Usar múltiplas conexões (Opção 1)
```

---

## 📋 Plano de Ação

### Fase 1: Implementação Imediata (Opção 1)

1. **Modificar `RabbitMQHelper`**:
   - Adicionar método `determineVirtualHostForEvent(String eventType)`
   - Manter conexões separadas por virtual host
   - Atualizar método `connect()` para suportar múltiplas conexões

2. **Atualizar mapeamento de eventos**:
   - Identificar todos os eventos VS-Identity → `/br`
   - Identificar todos os eventos VS-CustomerCommunications → `/shared`

3. **Testes**:
   - Executar testes VS-Identity (devem continuar funcionando)
   - Executar testes VS-CustomerCommunications (devem passar)
   - Executar testes Cross-VS (devem passar)

### Fase 2: Documentação

1. **Atualizar `TROUBLESHOOTING.md`**:
   - Adicionar seção sobre virtual hosts multi-country
   - Documentar mapeamento de eventos para virtual hosts

2. **Atualizar `README.md`**:
   - Documentar configuração de virtual hosts
   - Adicionar exemplos de execução por VS

### Fase 3: Validação

1. **Executar suite completa de testes**:
   ```bash
   mvn test -Dspring.profiles.active=local
   ```

2. **Validar eventos em ambos os virtual hosts**:
   ```bash
   # Verificar eventos em /br
   docker exec rabbitmq-br rabbitmqctl list_queues -p /br
   
   # Verificar eventos em /shared
   docker exec rabbitmq-br rabbitmqctl list_queues -p /shared
   ```

---

## 🎯 Prioridades

1. **🔴 ALTA**: Implementar suporte a múltiplos virtual hosts no `RabbitMQHelper`
2. **🟡 MÉDIA**: Atualizar documentação
3. **🟢 BAIXA**: Otimizar conexões (pooling, reutilização)

---

## 📝 Notas Adicionais

### Eventos VS-Identity (vhost `/br`)
- `user.created.v1` → `auth.user-created.queue`
- `credentials.provisioned.v1` → `identity.credentials-provisioned.queue`
- `otp.validated` → `auth.otp-validated.queue`

### Eventos VS-CustomerCommunications (vhost `/shared`)
- `otp.sent` → `transactional.auth-otp-sent.queue`
- `welcome.message.sent` → `transactional.auth-welcome-message-sent.queue`
- `delivery.tracking.created.v1` → `delivery-tracker.delivery-tracking-created.queue`
- `callback.received` → `delivery-tracker.delivery-callbacks.queue`

### Eventos Cross-VS
- Eventos publicados em ambos os vhosts (dual publishing):
  - `otp.sent` → publicado em `/br` e `/shared`
  - `welcome.message.sent` → publicado em `/br` e `/shared`

---

**Próximo Passo**: Implementar suporte a múltiplos virtual hosts no `RabbitMQHelper` conforme Opção 1.

