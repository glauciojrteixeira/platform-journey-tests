package com.nulote.journey.clients;

import com.nulote.journey.config.E2EConfiguration;
import com.nulote.journey.fixtures.ExecutionContext;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Cliente HTTP para comunicação com o Transactional Messaging Service (VS-Customer-Communications).
 */
@Component
public class TransactionalMessagingServiceClient {
    
    @Autowired
    private E2EConfiguration config;
    
    private String getBaseUrl() {
        // Por padrão, usar porta 8188 conforme documentação
        return config.getServices().getTransactionalMessagingUrl() != null 
            ? config.getServices().getTransactionalMessagingUrl()
            : "http://localhost:8188";
    }
    
    private String getRequestTraceId() {
        return ExecutionContext.getExecutionId();
    }
    
    /**
     * Adiciona os headers obrigatórios de correlação e governança.
     * Inclui o header country-code para suporte multi-country (conforme refatoração).
     */
    private RequestSpecification addRequiredHeaders(RequestSpecification spec) {
        spec = spec.header("request-caller", "e2e-tests")
                   .header("request-origin", "direct")
                   .header("country-code", config.getCountryCodeHeader()); // Multi-country: header lowercase conforme RFC 6648
        return spec;
    }
    
    /**
     * Consulta o status de uma mensagem pelo messageId.
     * 
     * @param messageId UUID da mensagem
     * @return Resposta HTTP
     */
    public Response getMessageStatus(String messageId) {
        RequestSpecification spec = RestAssured.given()
            .baseUri(getBaseUrl())
            .contentType(ContentType.JSON)
            .header("request-trace-id", getRequestTraceId());
        spec = addRequiredHeaders(spec);
        
        return spec.when()
            .get("/api/v1/messages/{messageId}", messageId)
            .then()
            .extract()
            .response();
    }
    
    /**
     * Lista mensagens por userId.
     * 
     * @param userId UUID do usuário
     * @return Resposta HTTP
     */
    public Response getMessagesByUser(String userId) {
        RequestSpecification spec = RestAssured.given()
            .baseUri(getBaseUrl())
            .contentType(ContentType.JSON)
            .header("request-trace-id", getRequestTraceId());
        spec = addRequiredHeaders(spec);
        
        return spec.when()
            .get("/api/v1/messages/user/{userId}", userId)
            .then()
            .extract()
            .response();
    }
    
    /**
     * Health check do serviço.
     * 
     * @return Resposta HTTP
     */
    public Response healthCheck() {
        RequestSpecification spec = RestAssured.given()
            .baseUri(getBaseUrl());
        
        return spec.when()
            .get("/api/v1/health")
            .then()
            .extract()
            .response();
    }
}
