# Implementação de Suporte a Múltiplos Virtual Hosts

**Data**: 2025-12-22  
**Arquivo Modificado**: `src/test/java/com/nulote/journey/utils/RabbitMQHelper.java`

---

## 📋 Resumo

Implementado suporte a múltiplos virtual hosts no `RabbitMQHelper` para suportar a arquitetura multi-country:
- **VS-Identity**: eventos no virtual host `/br`
- **VS-CustomerCommunications**: eventos no virtual host `/shared`

---

## 🔧 Alterações Implementadas

### 1. Múltiplas Conexões por Virtual Host

**Antes**: Uma única conexão e canal para todos os eventos.

**Depois**: Map de conexões e canais por virtual host:

```java
private Map<String, Connection> connections = new HashMap<>();
private Map<String, Channel> channels = new HashMap<>();
```

### 2. Método para Determinar Virtual Host por Evento

Adicionado método `determineVirtualHostForEvent(String eventType)` que mapeia eventos para seus virtual hosts corretos:

- **VS-Identity** (`/br`):
  - `user.created.v1`
  - `credentials.provisioned.v1`
  - `otp.validated`
  - `auth.logout`

- **VS-CustomerCommunications** (`/shared`):
  - `otp.sent`
  - `welcome.message.sent`
  - `delivery.tracking.created.v1`
  - `callback.received`

### 3. Conexão Lazy por Virtual Host

Modificado método `connect()` para aceitar virtual host como parâmetro:

```java
private void connect(String vhost) throws IOException, TimeoutException {
    // Estabelece conexão específica para o virtual host
    // Armazena em connections e channels maps
}
```

### 4. Consumo de Mensagens com Virtual Host Correto

Método `consumeMessage()` agora:
1. Determina o virtual host correto baseado no tipo de evento
2. Obtém ou cria conexão para aquele virtual host
3. Consome mensagem usando o canal correto

### 5. Informações de Fila em Múltiplos Virtual Hosts

Método `getQueueInfo()` agora tenta encontrar a fila em ambos os virtual hosts:
1. Primeiro tenta em `/shared` (VS-CustomerCommunications)
2. Depois tenta em `/br` (VS-Identity)
3. Por último tenta no virtual host padrão da configuração

### 6. Fechamento de Todas as Conexões

Método `close()` agora fecha todas as conexões e canais de todos os virtual hosts.

---

## 🎯 Benefícios

1. **Suporte Completo à Arquitetura Multi-Country**
   - Eventos VS-Identity consumidos do vhost `/br`
   - Eventos VS-CustomerCommunications consumidos do vhost `/shared`

2. **Conexões Eficientes**
   - Conexões são criadas apenas quando necessário (lazy)
   - Reutilização de conexões existentes
   - Gerenciamento automático de múltiplas conexões

3. **Compatibilidade Retroativa**
   - Método `connect()` sem parâmetros mantido para compatibilidade
   - Fallback para virtual host padrão quando evento não está mapeado

4. **Logs Informativos**
   - Logs indicam qual virtual host está sendo usado
   - Facilita troubleshooting e debugging

---

## 📝 Exemplo de Uso

```java
// Consumir evento VS-Identity (automaticamente usa /br)
Event event = rabbitMQHelper.consumeMessage("user.created.v1");

// Consumir evento VS-CustomerCommunications (automaticamente usa /shared)
Event otpEvent = rabbitMQHelper.consumeMessage("otp.sent");

// Obter informações de fila (tenta em ambos os vhosts)
QueueInfo info = rabbitMQHelper.getQueueInfo("transactional.auth-otp-sent.queue");
```

---

## ✅ Testes Afetados

Os seguintes testes devem passar após esta implementação:

1. ✅ Múltiplos OTPs simultâneos - Processamento assíncrono correto
2. ✅ Falha no Transactional Messaging Service - Evento deve ir para DLQ
3. ✅ Timeout no envio de email - Retry automático
4. ✅ Múltiplos eventos OTP - Ordem de processamento preservada
5. ✅ Idempotência no processamento de eventos OTP
6. ✅ Envio de OTP via Email - Fluxo Cross-VS Completo (PASSWORD_RECOVERY)
7. ✅ Envio de OTP via Email - Fluxo Cross-VS Completo (REGISTRATION)
8. ✅ Delivery Tracker recebe webhook do SendGrid e atualiza status
9. ✅ Consumir evento otp.sent e processar envio de OTP via Email

---

## 🔍 Validação

Para validar a implementação:

```bash
# Executar testes E2E
mvn test -Dspring.profiles.active=local

# Verificar logs para confirmar uso correto dos virtual hosts
grep "MULTI-COUNTRY" target/surefire-reports/*.txt
```

---

## 📚 Referências

- `ANALISE_LOGS_MULTI_COUNTRY.md` - Análise dos problemas identificados
- `playbooks/architecture-playbook/001.00 - MULTI-COUNTRY-ARCHITECTURE-STRATEGY.md` - Estratégia de arquitetura

