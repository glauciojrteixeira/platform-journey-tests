package com.nulote.journey.runners;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResources;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.FILTER_TAGS_PROPERTY_NAME;

/**
 * Runner principal para executar todos os testes Cucumber.
 * Configura plugins de relatório e localização das features e step definitions.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResources({
    @org.junit.platform.suite.api.SelectClasspathResource("features")
})
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, 
    value = "pretty, " +
            "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm, " +
            "html:target/cucumber-reports/cucumber.html, " +
            "json:target/cucumber-reports/cucumber.json")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, 
    value = "com.nulote.journey.stepdefinitions,com.nulote.journey.config")
// Tags padrão podem ser definidas aqui (serão sobrescritas por linha de comando)
// Por padrão, executa apenas testes implementados (pula @not_implemented)
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, 
    value = "@e2e and not @not_implemented") // Tag padrão se nenhuma for especificada
public class CucumberTestRunner {
    // Runner principal para executar todos os testes Cucumber
}

