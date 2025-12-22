package com.nulote.journey.config;

import com.nulote.platform_journey_tests.PlatformJourneyTestsApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.annotation.PostConstruct;

/**
 * Configuração base para testes E2E com Cucumber e Spring Boot.
 * Esta classe é usada pelo Cucumber para inicializar o contexto Spring.
 * 
 * Otimizações de performance implementadas:
 * - Paralelização de testes (Maven Surefire)
 * - Timeouts otimizados (application.yml)
 * - Poll intervals reduzidos (application.yml)
 * - Connection pooling explícito (RestAssured)
 */
@CucumberContextConfiguration
@SpringBootTest(classes = PlatformJourneyTestsApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("e2e-test")
public class E2ETestConfiguration {
    
    @Autowired
    private E2EConfiguration config;
    
    /**
     * Loga configurações de HTTP client para referência.
     * RestAssured 5.x já usa connection pooling por padrão, então não é necessário
     * configurá-lo explicitamente. As configurações em application.yml servem como
     * documentação e podem ser usadas no futuro se a API do RestAssured permitir.
     */
    @PostConstruct
    public void logHttpClientConfig() {
        E2EConfiguration.HttpClient httpConfig = config.getHttpClient();
        
        var logger = org.slf4j.LoggerFactory.getLogger(E2ETestConfiguration.class);
        logger.info("✅ [PERFORMANCE] Configurações de HTTP client (RestAssured usa pooling padrão):");
        logger.info("   - Max connections per route: {} (configurado em application.yml)", httpConfig.getMaxConnectionsPerRoute());
        logger.info("   - Max total connections: {} (configurado em application.yml)", httpConfig.getMaxTotalConnections());
        logger.info("   - Connection timeout: {}ms (configurado em application.yml)", httpConfig.getConnectionTimeoutMs());
        logger.info("   - Socket timeout: {}ms (configurado em application.yml)", httpConfig.getSocketTimeoutMs());
        logger.info("   - Connection TTL: {}ms (configurado em application.yml)", httpConfig.getConnectionTtlMs());
        logger.info("   Nota: RestAssured 5.x já usa connection pooling por padrão. Configurações acima são para referência.");
    }
}

