# Análise: Bloqueio de IP nos Testes E2E

## 🔍 Identificação do Problema

### Microserviço e Endpoint
- **Microserviço**: **Auth Service** (porta 8080)
- **Endpoint**: `POST /api/v1/auth/login`
- **Código de Erro**: `AU-A-BUS010`
- **Mensagem**: "IP address is blocked"

### Erro Completo
```json
{
  "errorCode": "AU-A-BUS010",
  "message": "IP address is blocked",
  "cause": "IP address '138.68.11.125' is blocked: IP address blocked due to 58 failed login attempts",
  "action": "Please try again after the block period expires or contact support",
  "httpStatus": 403,
  "severity": "HIGH",
  "retryable": false
}
```

---

## 🛡️ Mecanismo de Proteção

### Dois Tipos de Bloqueio

#### 1. **AU-A-BUS009**: Bloqueio de Conta (Account Locked)
- **O que bloqueia**: Conta específica (email/username)
- **Quando**: Após X tentativas falhadas para a mesma conta
- **Escopo**: Apenas aquela conta específica

#### 2. **AU-A-BUS010**: Bloqueio de IP (IP Blocked) ⚠️
- **O que bloqueia**: Endereço IP completo
- **Quando**: Após X tentativas falhadas de QUALQUER conta vindo daquele IP
- **Escopo**: Todas as contas tentando fazer login daquele IP

### Por que é um Problema nos Testes E2E?

1. **Múltiplos testes executam sequencialmente**
2. **Alguns testes falham intencionalmente** (testes de erro como "Login falha com credenciais inválidas")
3. **O IP acumula tentativas falhadas** de todos os testes
4. **Após um limite (ex: 50-60 tentativas), o IP é bloqueado**
5. **Testes subsequentes falham** mesmo com credenciais válidas

---

## ✅ É Comportamento Esperado?

### Sim, mas...

**Em Produção**: ✅ **SIM, é comportamento esperado e desejado**
- Protege contra ataques de força bruta
- Bloqueia IPs suspeitos automaticamente
- É uma camada importante de segurança

**Em Ambiente de Testes**: ⚠️ **Pode ser problemático**
- Testes E2E fazem muitas tentativas de login
- Testes de erro intencionalmente falham
- O bloqueio pode impedir a execução completa dos testes

---

## 🔧 Soluções Possíveis

### Opção 1: Whitelist de IPs para Ambiente de Teste (Recomendado)
**Configurar no Auth Service para não bloquear IPs de teste**

```yaml
# application-test.yml ou application-local.yml
security:
  brute-force:
    ip-whitelist:
      - "138.68.11.125"  # IP do ambiente de teste
      - "127.0.0.1"      # Localhost
      - "0.0.0.0/0"      # Todos (apenas para testes locais)
```

**Vantagens**:
- ✅ Mantém proteção em produção
- ✅ Permite testes sem bloqueio
- ✅ Configuração por ambiente

**Desvantagens**:
- ⚠️ Requer acesso à configuração do Auth Service
- ⚠️ Precisa manter lista atualizada

---

### Opção 2: Aumentar Limite de Tentativas para Ambiente de Teste
**Aumentar o threshold de bloqueio apenas em ambiente de teste**

```yaml
# application-test.yml
security:
  brute-force:
    max-failed-attempts-per-ip: 1000  # Muito alto para testes
    ip-block-window-minutes: 5         # Janela curta
    ip-block-duration-minutes: 1       # Bloqueio curto
```

**Vantagens**:
- ✅ Mantém proteção (mas mais permissiva)
- ✅ Permite mais tentativas antes de bloquear

**Desvantagens**:
- ⚠️ Ainda pode bloquear se muitos testes falharem
- ⚠️ Não resolve completamente o problema

---

### Opção 3: Limpar Bloqueios Antes de Executar Testes
**Criar endpoint administrativo para limpar bloqueios de IP**

```java
@RestController
@RequestMapping("/admin")
public class BruteForceAdminController {
    
    @PostMapping("/ip-blocks/clear")
    public void clearIpBlocks() {
        bruteForceDetectionService.clearIpBlocks();
    }
    
    @PostMapping("/ip-blocks/clear/{ip}")
    public void clearIpBlock(@PathVariable String ip) {
        bruteForceDetectionService.clearIpBlock(ip);
    }
}
```

**Uso nos Testes**:
```java
@BeforeAll
public static void setup() {
    // Limpar bloqueios antes de executar testes
    adminClient.clearIpBlocks();
}
```

**Vantagens**:
- ✅ Resolve o problema completamente
- ✅ Não afeta produção
- ✅ Pode ser usado em qualquer ambiente

**Desvantagens**:
- ⚠️ Requer implementação no Auth Service
- ⚠️ Precisa de endpoint administrativo (segurança!)

---

### Opção 4: Aguardar Desbloqueio Automático
**Aguardar o período de bloqueio expirar**

```java
// No teste, verificar se IP está bloqueado
if (response.getStatusCode() == 403 && 
    response.jsonPath().getString("errorCode").equals("AU-A-BUS010")) {
    
    // Aguardar período de bloqueio
    long blockDurationMinutes = extractBlockDuration(response);
    await().atMost(blockDurationMinutes + 1, MINUTES)
        .until(() -> {
            // Tentar login novamente
            return tryLogin() != 403;
        });
}
```

**Vantagens**:
- ✅ Não requer mudanças no backend
- ✅ Funciona com implementação atual

**Desvantagens**:
- ❌ Aumenta tempo de execução dos testes
- ❌ Não resolve o problema, apenas contorna

---

### Opção 5: Usar Múltiplos IPs (Load Balancer/VPN)
**Distribuir requisições entre múltiplos IPs**

**Vantagens**:
- ✅ Não requer mudanças no código
- ✅ Simula ambiente real

**Desvantagens**:
- ❌ Complexidade de infraestrutura
- ❌ Pode não ser viável em todos os ambientes

---

## 🎯 Recomendação

### Para Ambiente de Teste Local/SIT/UAT:

**Combinação de Opção 1 + Opção 3**:

1. **Whitelist de IPs de teste** no Auth Service
2. **Endpoint administrativo** para limpar bloqueios (se necessário)
3. **Configuração por ambiente** (teste vs produção)

### Implementação Sugerida:

```yaml
# application-test.yml (Auth Service)
security:
  brute-force:
    # Whitelist de IPs de teste
    ip-whitelist:
      - "127.0.0.1"
      - "localhost"
      - "${TEST_IP_WHITELIST:}"  # Variável de ambiente
    
    # Limites mais altos para testes
    max-failed-attempts-per-ip: 500
    ip-block-window-minutes: 10
    ip-block-duration-minutes: 1  # Bloqueio curto
```

```java
// Auth Service - BruteForceDetectionService
public boolean isIpBlocked(String ipAddress) {
    // Verificar whitelist primeiro
    if (ipWhitelist.contains(ipAddress)) {
        return false;  // IP de teste, não bloquear
    }
    
    // Lógica normal de bloqueio
    // ...
}
```

---

## 📊 Impacto nos Testes

### Testes Afetados pelo Bloqueio de IP:

1. ✅ Login bem-sucedido após registro
2. ✅ Login falha com credenciais inválidas
3. ✅ Login falha com usuário não encontrado
4. ✅ Login recorrente com token expirado/válido
5. ✅ Primeiro acesso após registro
6. ✅ Primeiro login após registro
7. ✅ Logout (precisa estar autenticado primeiro)
8. ✅ Alteração de senha (precisa estar autenticado)

**Total**: ~10-15 testes afetados

---

## 🔄 Próximos Passos

1. **Verificar configuração atual** do Auth Service para bloqueio de IP
2. **Implementar whitelist** de IPs para ambiente de teste
3. **Criar endpoint administrativo** (se necessário) para limpar bloqueios
4. **Documentar** a configuração necessária para cada ambiente
5. **Atualizar testes** para lidar com bloqueio de IP (se necessário)

---

## 📝 Notas

- O bloqueio de IP é uma **proteção de segurança importante** e deve ser mantido em produção
- Em ambiente de teste, precisamos de uma **configuração mais permissiva** ou **whitelist**
- A solução deve ser **configurável por ambiente** (teste vs produção)
- Testes E2E devem ser **idempotentes** e não depender de estado anterior (bloqueios)
