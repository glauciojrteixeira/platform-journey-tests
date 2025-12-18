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
 * Cliente HTTP para comunicação com o Delivery Tracker Service (VS-Customer-Communications).
 */
@Component
public class DeliveryTrackerServiceClient {
    
    @Autowired
    private E2EConfiguration config;
    
    private String getBaseUrl() {
        // Por padrão, usar porta 8083 conforme documentação
        return config.getServices().getDeliveryTrackerUrl() != null 
            ? config.getServices().getDeliveryTrackerUrl()
            : "http://localhost:8083";
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
     * Consulta o status de entrega de uma mensagem pelo messageId.
     * 
     * @param messageId UUID da mensagem
     * @return Resposta HTTP
     */
    public Response getDeliveryStatus(String messageId) {
        RequestSpecification spec = RestAssured.given()
            .baseUri(getBaseUrl())
            .contentType(ContentType.JSON)
            .header("request-trace-id", getRequestTraceId());
        spec = addRequiredHeaders(spec);
        
        return spec.when()
            .get("/api/v1/delivery/{messageId}", messageId)
            .then()
            .extract()
            .response();
    }
    
    /**
     * Lista entregas por status.
     * 
     * @param status Status da entrega (SENT, DELIVERED, OPENED, CLICKED, FAILED, etc.)
     * @return Resposta HTTP
     */
    public Response getDeliveriesByStatus(String status) {
        RequestSpecification spec = RestAssured.given()
            .baseUri(getBaseUrl())
            .contentType(ContentType.JSON)
            .header("request-trace-id", getRequestTraceId());
        spec = addRequiredHeaders(spec);
        
        return spec.when()
            .get("/api/v1/delivery/status/{status}", status)
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
