# Exemplo de Código Completo - Implementação Híbrida

## 📁 Estrutura de Arquivos

```
auth-service/
├── src/main/java/com/projeto2026/auth_service/
│   ├── domain/services/
│   │   ├── BruteForceDetectionService.java
│   │   └── impl/
│   │       └── BruteForceDetectionServiceImpl.java
│   └── infrastructure/
│       └── controllers/
│           └── AuthenticationController.java
└── src/main/resources/
    ├── application.yml
    ├── application-local.yml
    ├── application-sit.yml
    ├── application-uat.yml
    └── application-prod.yml
```

---

## 🔧 Código Completo

### 1. Interface BruteForceDetectionService

```java
package com.projeto2026.auth_service.domain.services;

import java.util.List;

/**
 * Serviço para detecção e prevenção de ataques de força bruta.
 */
public interface BruteForceDetectionService {
    
    /**
     * Verifica se a conta está bloqueada.
     * 
     * @param email Email do usuário
     * @return true se conta está bloqueada, false caso contrário
     */
    boolean isAccountLocked(String email);
    
    /**
     * Verifica se o IP está bloqueado.
     * 
     * @param ipAddress Endereço IP
     * @return true se IP está bloqueado, false caso contrário
     */
    boolean isIpBlocked(String ipAddress);
    
    /**
     * Verifica se o IP está na whitelist.
     * 
     * @param ipAddress Endereço IP
     * @return true se IP está na whitelist, false caso contrário
     */
    boolean isIpWhitelisted(String ipAddress);
    
    /**
     * Registra uma tentativa falhada.
     * 
     * @param email Email do usuário
     * @param ipAddress Endereço IP
     */
    void recordFailedAttempt(String email, String ipAddress);
    
    /**
     * Registra uma tentativa bem-sucedida.
     * 
     * @param email Email do usuário
     * @param ipAddress Endereço IP
     */
    void recordSuccessfulAttempt(String email, String ipAddress);
    
    /**
     * Conta tentativas falhadas por email dentro da janela de tempo.
     * 
     * @param email Email do usuário
     * @return Número de tentativas falhadas
     */
    long countFailedAttempts(String email);
    
    /**
     * Conta tentativas falhadas por IP dentro da janela de tempo.
     * 
     * @param ipAddress Endereço IP
     * @return Número de tentativas falhadas
     */
    long countFailedAttemptsByIp(String ipAddress);
    
    /**
     * Desbloqueia uma conta manualmente.
     * 
     * @param email Email do usuário
     */
    void unlockAccount(String email);
    
    /**
     * Desbloqueia um IP manualmente.
     * 
     * @param ipAddress Endereço IP
     */
    void unblockIp(String ipAddress);
}
```

---

### 2. Implementação BruteForceDetectionServiceImpl

```java
package com.projeto2026.auth_service.domain.services.impl;

import com.projeto2026.auth_service.domain.services.BruteForceDetectionService;
import com.projeto2026.auth_service.infrastructure.repositories.mongodb.AuthFailedMetricsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class BruteForceDetectionServiceImpl implements BruteForceDetectionService {
    
    private static final Logger logger = LoggerFactory.getLogger(BruteForceDetectionServiceImpl.class);
    
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
        if (ipAddress == null || ipAddress.trim().isEmpty() || ipWhitelist == null || ipWhitelist.isEmpty()) {
            return false;
        }
        
        String normalizedIp = ipAddress.trim().toLowerCase();
        boolean whitelisted = ipWhitelist.contains(normalizedIp);
        
        if (whitelisted) {
            logger.debug("✅ IP {} is whitelisted", ipAddress);
        }
        
        return whitelisted;
    }
    
    @Override
    public boolean isIpBlocked(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            return false;
        }
        
        // ✅ PRIORIDADE 1: Verificar whitelist primeiro
        if (isIpWhitelisted(ipAddress)) {
            logger.debug("✅ IP {} is whitelisted, skipping block check", ipAddress);
            return false;  // IP whitelisted nunca é bloqueado
        }
        
        // ✅ PRIORIDADE 2: Aplicar lógica normal de bloqueio (com parâmetros ajustados por ambiente)
        Date windowStart = new Date(System.currentTimeMillis() - 
            TimeUnit.MINUTES.toMillis(windowMinutes));
        
        long failedCount = metricsRepository.countByIpAddressAndOccurredAtAfter(
            ipAddress, windowStart);
        
        boolean isBlocked = failedCount >= maxAttemptsPerIp;
        
        if (isBlocked) {
            logger.warn("🚫 IP {} is blocked: {} failed attempts in last {} minutes (limit: {})", 
                ipAddress, failedCount, windowMinutes, maxAttemptsPerIp);
        } else {
            logger.debug("✅ IP {} is not blocked: {} failed attempts in last {} minutes (limit: {})", 
                ipAddress, failedCount, windowMinutes, maxAttemptsPerIp);
        }
        
        return isBlocked;
    }
    
    @Override
    public boolean isAccountLocked(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        Date windowStart = new Date(System.currentTimeMillis() - 
            TimeUnit.MINUTES.toMillis(windowMinutes));
        
        long failedCount = metricsRepository.countByEmailAndOccurredAtAfter(email, windowStart);
        
        boolean isLocked = failedCount >= maxAttemptsPerEmail;
        
        if (isLocked) {
            logger.warn("🚫 Account {} is locked: {} failed attempts in last {} minutes (limit: {})", 
                email, failedCount, windowMinutes, maxAttemptsPerEmail);
        }
        
        return isLocked;
    }
    
    @Override
    public void recordFailedAttempt(String email, String ipAddress) {
        // O evento auth.failed já é publicado e salvo no MongoDB
        // Este método pode ser usado para lógica adicional (ex: cache, alertas)
        logger.debug("📝 Recorded failed attempt for email={}, ip={}", email, ipAddress);
    }
    
    @Override
    public void recordSuccessfulAttempt(String email, String ipAddress) {
        // Login bem-sucedido não reseta tentativas falhadas no MongoDB
        // (mantém histórico para análise)
        logger.debug("✅ Recorded successful attempt for email={}, ip={}", email, ipAddress);
    }
    
    @Override
    public long countFailedAttempts(String email) {
        if (email == null || email.trim().isEmpty()) {
            return 0;
        }
        
        Date windowStart = new Date(System.currentTimeMillis() - 
            TimeUnit.MINUTES.toMillis(windowMinutes));
        
        return metricsRepository.countByEmailAndOccurredAtAfter(email, windowStart);
    }
    
    @Override
    public long countFailedAttemptsByIp(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            return 0;
        }
        
        Date windowStart = new Date(System.currentTimeMillis() - 
            TimeUnit.MINUTES.toMillis(windowMinutes));
        
        return metricsRepository.countByIpAddressAndOccurredAtAfter(ipAddress, windowStart);
    }
    
    @Override
    public void unlockAccount(String email) {
        // Implementação para desbloquear conta manualmente
        // (pode limpar métricas ou marcar como desbloqueada)
        logger.info("🔓 Manually unlocking account: {}", email);
        // TODO: Implementar lógica de desbloqueio manual
    }
    
    @Override
    public void unblockIp(String ipAddress) {
        // Implementação para desbloquear IP manualmente
        // (pode limpar métricas ou marcar como desbloqueado)
        logger.info("🔓 Manually unblocking IP: {}", ipAddress);
        // TODO: Implementar lógica de desbloqueio manual
    }
}
```

---

### 3. Exemplo de Uso no AuthenticationServiceImpl

```java
// AuthenticationServiceImpl.java (exemplo de integração)
@Override
public JwtToken authenticate(AuthenticationRequest request) {
    String ipAddress = RequestHelper.extractClientIp(httpRequest);
    String userAgent = RequestHelper.extractUserAgent(httpRequest);
    
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
    
    // ✅ Verificar bloqueio de conta
    if (bruteForceDetectionService.isAccountLocked(request.username())) {
        long failedCount = bruteForceDetectionService.countFailedAttempts(request.username());
        publishAuthFailedEvent(request.username(), "ACCOUNT_LOCKED", ipAddress, userAgent);
        throw new AccountLockedException(
            request.username(),
            String.format("Account '%s' is locked: Account locked due to %d failed login attempts within the last %d minutes. Please try again after the lockout period expires.",
                request.username(), failedCount, bruteForceConfig.getWindowMinutes())
        );
    }
    
    // ... resto da lógica de autenticação ...
    
    try {
        // Tentar autenticar
        // ...
        
        // ✅ Registrar sucesso
        bruteForceDetectionService.recordSuccessfulAttempt(request.username(), ipAddress);
        return jwtToken;
        
    } catch (InvalidCredentialsException e) {
        // ✅ Registrar falha
        bruteForceDetectionService.recordFailedAttempt(request.username(), ipAddress);
        throw e;
    }
}
```

---

## 📝 Configuração

### 1. application.yml (Único arquivo)

```yaml
security:
  brute-force:
    enabled: ${SECURITY_BRUTE_FORCE_ENABLED:true}
    
    # Whitelist de IPs (string separada por vírgula)
    # Formato: "127.0.0.1,localhost,::1"
    # Em produção: deixar vazio ou não definir a variável
    ip-whitelist: ${SECURITY_BRUTE_FORCE_IP_WHITELIST:127.0.0.1,localhost,::1}
    
    # Parâmetros de bloqueio (configuráveis por ambiente)
    max-attempts-per-email: ${SECURITY_BRUTE_FORCE_MAX_ATTEMPTS_PER_EMAIL:3}
    max-attempts-per-ip: ${SECURITY_BRUTE_FORCE_MAX_ATTEMPTS_PER_IP:200}  # Alto para local/teste
    window-minutes: ${SECURITY_BRUTE_FORCE_WINDOW_MINUTES:10}              # Curto para local/teste
    lockout-duration-minutes: ${SECURITY_BRUTE_FORCE_LOCKOUT_DURATION_MINUTES:2}  # Curto para local/teste
    window-type: ${SECURITY_BRUTE_FORCE_WINDOW_TYPE:SLIDING}
```

### 2. env.example (Template)

**Arquivo**: `auth-service/env.example`

```bash
# Brute Force Detection
SECURITY_BRUTE_FORCE_ENABLED=true
SECURITY_BRUTE_FORCE_IP_WHITELIST=127.0.0.1,localhost,::1
SECURITY_BRUTE_FORCE_MAX_ATTEMPTS_PER_EMAIL=3
SECURITY_BRUTE_FORCE_MAX_ATTEMPTS_PER_IP=200
SECURITY_BRUTE_FORCE_WINDOW_MINUTES=10
SECURITY_BRUTE_FORCE_LOCKOUT_DURATION_MINUTES=2
SECURITY_BRUTE_FORCE_WINDOW_TYPE=SLIDING
```

### 3. .env (Configuração por Ambiente)

**⚠️ IMPORTANTE**: Copiar `env.example` para `.env` e configurar conforme o ambiente.

**Local** (`auth-service/.env`):
```bash
SECURITY_BRUTE_FORCE_IP_WHITELIST=127.0.0.1,localhost,::1
SECURITY_BRUTE_FORCE_MAX_ATTEMPTS_PER_IP=200
SECURITY_BRUTE_FORCE_WINDOW_MINUTES=10
SECURITY_BRUTE_FORCE_LOCKOUT_DURATION_MINUTES=2
```

**SIT** (`auth-service/.env`):
```bash
SECURITY_BRUTE_FORCE_IP_WHITELIST=127.0.0.1,localhost,::1,138.68.11.125
SECURITY_BRUTE_FORCE_MAX_ATTEMPTS_PER_IP=200
SECURITY_BRUTE_FORCE_WINDOW_MINUTES=10
SECURITY_BRUTE_FORCE_LOCKOUT_DURATION_MINUTES=2
```

**UAT** (`auth-service/.env`):
```bash
SECURITY_BRUTE_FORCE_IP_WHITELIST=138.68.11.125
SECURITY_BRUTE_FORCE_MAX_ATTEMPTS_PER_IP=100
SECURITY_BRUTE_FORCE_WINDOW_MINUTES=15
SECURITY_BRUTE_FORCE_LOCKOUT_DURATION_MINUTES=5
```

**PROD** (`auth-service/.env`):
```bash
SECURITY_BRUTE_FORCE_IP_WHITELIST=
SECURITY_BRUTE_FORCE_MAX_ATTEMPTS_PER_IP=10
SECURITY_BRUTE_FORCE_WINDOW_MINUTES=30
SECURITY_BRUTE_FORCE_LOCKOUT_DURATION_MINUTES=15
```

**Nota**: O arquivo `.env` é carregado automaticamente pelo `docker-compose.yml` e as variáveis são injetadas nos containers Docker.

---

## ✅ Validação

Após implementar, verificar logs:

```
✅ IP Whitelist configured with 3 IPs: [127.0.0.1, localhost, ::1]
🔒 Brute Force Detection configured: maxAttemptsPerEmail=3, maxAttemptsPerIp=200, windowMinutes=10, lockoutDurationMinutes=2
✅ IP 127.0.0.1 is whitelisted, skipping block check
```
