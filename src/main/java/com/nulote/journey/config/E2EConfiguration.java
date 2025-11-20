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
    private Services services;
    private int timeout;
    private SimulateProvider simulateProvider = new SimulateProvider();
    
    public String getEnvironment() {
        return environment;
    }
    
    public void setEnvironment(String environment) {
        this.environment = environment;
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
}

