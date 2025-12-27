# Implementação: Abordagem Híbrida para Bloqueio de IP

## 📋 Resumo

Este documento contém todas as mudanças necessárias para implementar a abordagem híbrida (whitelist + parâmetros ajustados) no **Auth Service**.

---

## 🔧 Mudanças Necessárias

### 1. Adicionar Suporte a Whitelist no BruteForceDetectionService

#### 1.1. Atualizar Interface (se necessário)

```java
// BruteForceDetectionService.java
public interface BruteForceDetectionService {
    // ... métodos existentes ...
    
    /**
     * Verifica se o IP está na whitelist.
     * 
     * @param ipAddress Endereço IP
     * @return true se IP está na whitelist, false caso contrário
     */
    boolean isIpWhitelisted(String ipAddress);
}
```

#### 1.2. Atualizar Implementação

```java
// BruteForceDetectionServiceImpl.java
@Service
public class BruteForceDetectionServiceImpl implements BruteForceDetectionService {
    
    private final AuthFailedMetricsRepository metricsRepository;
    
    @Value("${security.brute-force.max-attempts-per-email:3}")
    private int maxAttemptsPerEmail;
    
    @Value("${security.brute-force.max-attempts-per-ip:10}")
    private int maxAttemptsPerIp;
    
    @Value("${security.brute-force.window-minutes:30}")
    private int windowMinutes;
    
    @Value("${security.brute-force.lockout-duration-minutes:15}")
    private int lockoutDurationMinutes;
    
    // ✅ NOVO: Whitelist de IPs (string separada por vírgula)
    @Value("${security.brute-force.ip-whitelist:}")
    private String ipWhitelistString;
    
    private List<String> ipWhitelist;
    
    private static final Logger logger = LoggerFactory.getLogger(BruteForceDetectionServiceImpl.class);
    
    public BruteForceDetectionServiceImpl(AuthFailedMetricsRepository metricsRepository) {
        this.metricsRepository = metricsRepository;
    }
    
    @PostConstruct
    public void init() {
        // Processar whitelist de string (separada por vírgula) para lista
        if (ipWhitelistString != null && !ipWhitelistString.trim().isEmpty()) {
            ipWhitelist = Arrays.stream(ipWhitelistString.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(ip -> !ip.isEmpty())
                .collect(Collectors.toList());
            
            logger.info("✅ IP Whitelist configured with {} IPs: {}", 
                ipWhitelist.size(), ipWhitelist);
        } else {
            ipWhitelist = Collections.emptyList();
            logger.info("ℹ️ IP Whitelist is empty (no whitelist configured)");
        }
        
        logger.info("🔒 Brute Force Detection configured: maxAttemptsPerEmail={}, maxAttemptsPerIp={}, windowMinutes={}, lockoutDurationMinutes={}",
            maxAttemptsPerEmail, maxAttemptsPerIp, windowMinutes, lockoutDurationMinutes);
    }
    
    @Override
    public boolean isIpWhitelisted(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty() || ipWhitelist == null) {
            return false;
        }
        
        String normalizedIp = ipAddress.trim().toLowerCase();
        return ipWhitelist.contains(normalizedIp);
    }
    
    @Override
    public boolean isIpBlocked(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            return false;
        }
        
        // ✅ NOVO: Verificar whitelist primeiro (prioridade)
        if (isIpWhitelisted(ipAddress)) {
            logger.debug("IP {} is whitelisted, skipping block check", ipAddress);
            return false;  // IP whitelisted nunca é bloqueado
        }
        
        // Lógica normal de bloqueio (com parâmetros ajustados por ambiente)
        Date windowStart = new Date(System.currentTimeMillis() - 
            TimeUnit.MINUTES.toMillis(windowMinutes));
        
        long failedCount = metricsRepository.countByIpAddressAndOccurredAtAfter(
            ipAddress, windowStart);
        
        boolean isBlocked = failedCount >= maxAttemptsPerIp;
        
        if (isBlocked) {
            logger.warn("IP {} is blocked: {} failed attempts in last {} minutes (limit: {})", 
                ipAddress, failedCount, windowMinutes, maxAttemptsPerIp);
        }
        
        return isBlocked;
    }
    
    // ... outros métodos existentes ...
}
```

**Imports necessários**:
```java
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Date;
import java.util.concurrent.TimeUnit;
```

---

### 2. Atualizar Configuração YAML (Único application.yml)

**⚠️ IMPORTANTE**: VS Identity e Customer Communications usam **um único `application.yml`** com variáveis de ambiente, não múltiplos arquivos `application-*.yml`.

#### 2.1. Adicionar Configuração no `application.yml`

```yaml
security:
  brute-force:
    enabled: ${SECURITY_BRUTE_FORCE_ENABLED:true}
    
    # Whitelist de IPs (configurável via variável de ambiente)
    # Formato: lista separada por vírgula (ex: "127.0.0.1,localhost,::1")
    # Em produção: deixar vazio ou não definir a variável
    ip-whitelist: ${SECURITY_BRUTE_FORCE_IP_WHITELIST:127.0.0.1,localhost,::1}
    
    # Parâmetros de bloqueio (configuráveis por ambiente)
    max-attempts-per-email: ${SECURITY_BRUTE_FORCE_MAX_ATTEMPTS_PER_EMAIL:3}
    max-attempts-per-ip: ${SECURITY_BRUTE_FORCE_MAX_ATTEMPTS_PER_IP:200}  # Alto para local/teste
    window-minutes: ${SECURITY_BRUTE_FORCE_WINDOW_MINUTES:10}              # Curto para local/teste
    lockout-duration-minutes: ${SECURITY_BRUTE_FORCE_LOCKOUT_DURATION_MINUTES:2}  # Curto para local/teste
    window-type: ${SECURITY_BRUTE_FORCE_WINDOW_TYPE:SLIDING}
```

**⚠️ Nota sobre whitelist**: Como Spring Boot não suporta listas diretamente via variável de ambiente, a whitelist é configurada como uma **string separada por vírgula** (ex: `"127.0.0.1,localhost,::1"`). O código processa essa string no método `@PostConstruct init()` para converter em uma lista. Veja seção 1.2 para implementação completa.

#### 2.2. Variáveis de Ambiente por Ambiente

**⚠️ IMPORTANTE**: As variáveis de ambiente são injetadas nos containers Docker através do arquivo `.env`. Cada serviço possui um arquivo `env.example` que deve ser copiado para `.env` e configurado.

**Passos para configurar**:

1. **Copiar `env.example` para `.env`**:
```bash
cd auth-service
cp env.example .env
```

2. **Adicionar variáveis de brute-force no `.env`**:

**Local (desenvolvimento)** - `auth-service/.env`:
```bash
# Brute Force Detection - Configuração para ambiente local/teste
SECURITY_BRUTE_FORCE_ENABLED=true
SECURITY_BRUTE_FORCE_IP_WHITELIST=127.0.0.1,localhost,::1
SECURITY_BRUTE_FORCE_MAX_ATTEMPTS_PER_EMAIL=3
SECURITY_BRUTE_FORCE_MAX_ATTEMPTS_PER_IP=200
SECURITY_BRUTE_FORCE_WINDOW_MINUTES=10
SECURITY_BRUTE_FORCE_LOCKOUT_DURATION_MINUTES=2
SECURITY_BRUTE_FORCE_WINDOW_TYPE=SLIDING
```

**SIT (ambiente de teste)** - `auth-service/.env`:
```bash
# Brute Force Detection - Configuração para SIT
SECURITY_BRUTE_FORCE_ENABLED=true
SECURITY_BRUTE_FORCE_IP_WHITELIST=127.0.0.1,localhost,::1,138.68.11.125
SECURITY_BRUTE_FORCE_MAX_ATTEMPTS_PER_EMAIL=3
SECURITY_BRUTE_FORCE_MAX_ATTEMPTS_PER_IP=200
SECURITY_BRUTE_FORCE_WINDOW_MINUTES=10
SECURITY_BRUTE_FORCE_LOCKOUT_DURATION_MINUTES=2
SECURITY_BRUTE_FORCE_WINDOW_TYPE=SLIDING
```

**UAT (ambiente de teste)** - `auth-service/.env`:
```bash
# Brute Force Detection - Configuração para UAT (mais conservador)
SECURITY_BRUTE_FORCE_ENABLED=true
SECURITY_BRUTE_FORCE_IP_WHITELIST=138.68.11.125
SECURITY_BRUTE_FORCE_MAX_ATTEMPTS_PER_EMAIL=3
SECURITY_BRUTE_FORCE_MAX_ATTEMPTS_PER_IP=100
SECURITY_BRUTE_FORCE_WINDOW_MINUTES=15
SECURITY_BRUTE_FORCE_LOCKOUT_DURATION_MINUTES=5
SECURITY_BRUTE_FORCE_WINDOW_TYPE=SLIDING
```

**PROD (produção)** - `auth-service/.env`:
```bash
# Brute Force Detection - Configuração para PROD (seguro)
SECURITY_BRUTE_FORCE_ENABLED=true
SECURITY_BRUTE_FORCE_IP_WHITELIST=
SECURITY_BRUTE_FORCE_MAX_ATTEMPTS_PER_EMAIL=3
SECURITY_BRUTE_FORCE_MAX_ATTEMPTS_PER_IP=10
SECURITY_BRUTE_FORCE_WINDOW_MINUTES=30
SECURITY_BRUTE_FORCE_LOCKOUT_DURATION_MINUTES=15
SECURITY_BRUTE_FORCE_WINDOW_TYPE=SLIDING
```

**Nota**: O arquivo `.env` é carregado automaticamente pelo `docker-compose.yml` e as variáveis são injetadas nos containers. Ver `CONFIGURACAO_ENV.md` para documentação completa.

---

### 3. Atualizar AuthenticationServiceImpl (se necessário)

Verificar se o método `authenticate()` já chama `isIpBlocked()`. Se não, adicionar:

```java
// AuthenticationServiceImpl.java
@Override
public JwtToken authenticate(AuthenticationRequest request) {
    String ipAddress = RequestHelper.extractClientIp(httpRequest);
    
    // ✅ Verificar bloqueio de IP ANTES de tentar autenticar
    if (bruteForceDetectionService.isIpBlocked(ipAddress)) {
        long failedCount = bruteForceDetectionService.countFailedAttemptsByIp(ipAddress);
        publishAuthFailedEvent(request.username(), "IP_BLOCKED", ipAddress, userAgent);
        throw new IpBlockedException(
            ipAddress,
            String.format("IP address '%s' is blocked: IP address blocked due to %d failed login attempts within the last %d minutes. Please try again after the block period expires or contact support.",
                ipAddress, failedCount, bruteForceConfig.getWindowMinutes())
        );
    }
    
    // ... resto da lógica de autenticação ...
}
```

---

## 📝 Checklist de Implementação

### Fase 1: Código
- [ ] Adicionar campo `ipWhitelist` no `BruteForceDetectionServiceImpl`
- [ ] Adicionar método `isIpWhitelisted()`
- [ ] Atualizar método `isIpBlocked()` para verificar whitelist primeiro
- [ ] Adicionar `@PostConstruct` para normalizar whitelist
- [ ] Adicionar logs para debug

### Fase 2: Configuração
- [ ] Atualizar `application.yml` com configurações de brute-force usando variáveis de ambiente
- [ ] Adicionar variáveis de brute-force no `env.example` do Auth Service
- [ ] Documentar variáveis de ambiente necessárias
- [ ] Criar exemplos de `.env` para cada ambiente (local, SIT, UAT, PROD)

### Fase 3: Testes
- [ ] Testar whitelist com localhost
- [ ] Testar bloqueio com IP não whitelisted
- [ ] Testar parâmetros ajustados em ambiente local
- [ ] Verificar que produção mantém proteção

### Fase 4: Documentação
- [ ] Adicionar seção de brute-force no `env.example` do Auth Service
- [ ] Documentar variáveis de ambiente no README do Auth Service
- [ ] Atualizar documentação de configuração com exemplos de `.env`

---

## 🧪 Testes Recomendados

### Teste 1: Whitelist Funciona
```bash
# Tentar fazer login com IP whitelisted (localhost)
# Deve permitir muitas tentativas sem bloquear
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test@example.com","password":"wrong"}'
# Repetir 100 vezes - não deve bloquear
```

### Teste 2: IP Não Whitelisted é Bloqueado
```bash
# Usar IP não whitelisted
# Deve bloquear após max-attempts-per-ip tentativas
# Em local: após 200 tentativas
# Em prod: após 10 tentativas
```

### Teste 3: Parâmetros Ajustados Funcionam
```bash
# Em ambiente local, fazer 150 tentativas falhadas
# Não deve bloquear (limite é 200)
# Em produção, fazer 15 tentativas falhadas
# Deve bloquear (limite é 10)
```

---

## 🔍 Validação

Após implementar, validar:

1. **Logs mostram whitelist carregada**:
   ```
   IP Whitelist configured with 3 IPs: [127.0.0.1, localhost, ::1]
   ```

2. **IP whitelisted não é bloqueado**:
   ```
   IP 127.0.0.1 is whitelisted, skipping block check
   ```

3. **IP não whitelisted é bloqueado corretamente**:
   ```
   IP 138.68.11.125 is blocked: 201 failed attempts in last 10 minutes (limit: 200)
   ```

4. **Parâmetros por ambiente funcionam**:
   - Local: limite 200
   - SIT: limite 200
   - UAT: limite 100
   - Prod: limite 10

---

## 📚 Referências

- Documento de comparação: `COMPARACAO_WHITELIST_VS_RATE_LIMIT.md`
- Análise do problema: `BLOQUEIO_IP_ANALISE.md`
- Análise de erros: `ANALISE_ERROS_TESTES.md`

---

## ⚠️ Notas Importantes

1. **Variáveis de Ambiente**: Configure `SIT_TEST_IP` e `UAT_TEST_IP` nos respectivos ambientes
2. **Segurança**: Nunca adicionar IPs de produção na whitelist
3. **Logs**: Manter logs de whitelist em nível INFO para auditoria
4. **Normalização**: Sempre normalizar IPs (trim, lowercase) antes de comparar
5. **Fallback**: Parâmetros ajustados servem como fallback para IPs não whitelisted

---

## 🚀 Próximos Passos Após Implementação

1. Executar testes E2E para validar que bloqueio de IP não ocorre mais
2. Monitorar logs para verificar whitelist funcionando
3. Ajustar parâmetros se necessário (baseado em execuções reais)
4. Documentar IPs whitelisted em cada ambiente
