package com.nulote.journey.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração centralizada para testes E2E.
 * Carrega propriedades do application.yml com prefixo "e2e".
 */
@Configuration
@ConfigurationProperties(prefix = "e2e")
public class E2EConfiguration {
    
    private String environment;
    private String defaultCountryCode = "BR"; // Default para Brasil (será convertido para lowercase no header)
    private Services services;
    private int timeout;
    private SimulateProvider simulateProvider = new SimulateProvider();
    private RateLimitRetry rateLimitRetry = new RateLimitRetry();
    
    public String getEnvironment() {
        return environment;
    }
    
    public void setEnvironment(String environment) {
        this.environment = environment;
    }
    
    public String getDefaultCountryCode() {
        return defaultCountryCode;
    }
    
    public void setDefaultCountryCode(String defaultCountryCode) {
        this.defaultCountryCode = defaultCountryCode;
    }
    
    /**
     * Retorna o código do país em lowercase para uso no header HTTP.
     * Conforme RFC 6648 e playbook, headers devem ser lowercase.
     * 
     * @return Código do país em lowercase (ex: "br", "ar", "cl")
     */
    public String getCountryCodeHeader() {
        return defaultCountryCode != null ? defaultCountryCode.toLowerCase() : "br";
    }
    
    public Services getServices() {
        return services;
    }
    
    public void setServices(Services services) {
        this.services = services;
    }
    
    public int getTimeout() {
        return timeout;
    }
    
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    
    public SimulateProvider getSimulateProvider() {
        return simulateProvider;
    }
    
    public void setSimulateProvider(SimulateProvider simulateProvider) {
        this.simulateProvider = simulateProvider;
    }
    
    public RateLimitRetry getRateLimitRetry() {
        return rateLimitRetry;
    }
    
    public void setRateLimitRetry(RateLimitRetry rateLimitRetry) {
        this.rateLimitRetry = rateLimitRetry;
    }
    
    /**
     * Determina se o header simulate-provider deve ser adicionado nas requisições.
     * 
     * Regras:
     * - Nunca simular em PROD (segurança)
     * - Respeitar configuração explícita (simulate-provider.enabled)
     * - Default: simular em ambientes não-PROD (local, sit, uat)
     * 
     * @return true se deve adicionar header simulate-provider: true
     */
    public boolean shouldSimulateProvider() {
        // Nunca simular em PROD (camada de segurança)
        if ("prod".equalsIgnoreCase(environment)) {
            return false;
        }
        
        // Respeitar configuração explícita
        if (simulateProvider.getEnabled() != null) {
            return simulateProvider.getEnabled();
        }
        
        // Default: simular em ambientes não-PROD (local, sit, uat)
        return true;
    }
    
    public static class Services {
        private String identityUrl;
        private String authUrl;
        private String profileUrl;
        private String transactionalMessagingUrl;
        private String deliveryTrackerUrl;
        private String auditComplianceUrl;
        
        public String getIdentityUrl() {
            return identityUrl;
        }
        
        public void setIdentityUrl(String identityUrl) {
            this.identityUrl = identityUrl;
        }
        
        public String getAuthUrl() {
            return authUrl;
        }
        
        public void setAuthUrl(String authUrl) {
            this.authUrl = authUrl;
        }
        
        public String getProfileUrl() {
            return profileUrl;
        }
        
        public void setProfileUrl(String profileUrl) {
            this.profileUrl = profileUrl;
        }
        
        public String getTransactionalMessagingUrl() {
            return transactionalMessagingUrl;
        }
        
        public void setTransactionalMessagingUrl(String transactionalMessagingUrl) {
            this.transactionalMessagingUrl = transactionalMessagingUrl;
        }
        
        public String getDeliveryTrackerUrl() {
            return deliveryTrackerUrl;
        }
        
        public void setDeliveryTrackerUrl(String deliveryTrackerUrl) {
            this.deliveryTrackerUrl = deliveryTrackerUrl;
        }
        
        public String getAuditComplianceUrl() {
            return auditComplianceUrl;
        }
        
        public void setAuditComplianceUrl(String auditComplianceUrl) {
            this.auditComplianceUrl = auditComplianceUrl;
        }
    }
    
    public static class SimulateProvider {
        private Boolean enabled;
        
        public Boolean getEnabled() {
            return enabled;
        }
        
        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }
    }
    
    public static class RateLimitRetry {
        private Integer maxAttempts;
        private Long initialDelayMs;
        private Boolean enabled;
        
        public Integer getMaxAttempts() {
            return maxAttempts != null ? maxAttempts : 3; // Default: 3 tentativas
        }
        
        public void setMaxAttempts(Integer maxAttempts) {
            this.maxAttempts = maxAttempts;
        }
        
        public Long getInitialDelayMs() {
            return initialDelayMs != null ? initialDelayMs : 2000L; // Default: 2 segundos
        }
        
        public void setInitialDelayMs(Long initialDelayMs) {
            this.initialDelayMs = initialDelayMs;
        }
        
        public Boolean getEnabled() {
            return enabled != null ? enabled : true; // Default: habilitado
        }
        
        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }
    }
}

