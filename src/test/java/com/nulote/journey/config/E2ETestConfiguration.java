package com.nulote.journey.config;

import com.nulote.platform_journey_tests.PlatformJourneyTestsApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Configuração base para testes E2E com Cucumber e Spring Boot.
 * Esta classe é usada pelo Cucumber para inicializar o contexto Spring.
 */
@CucumberContextConfiguration
@SpringBootTest(classes = PlatformJourneyTestsApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("e2e-test")
public class E2ETestConfiguration {
    // Configuração base para testes E2E
}

