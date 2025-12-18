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
 * Cliente HTTP para comunicação com o Audit Compliance Service (VS-Customer-Communications).
 */
@Component
public class AuditComplianceServiceClient {
    
    @Autowired
    private E2EConfiguration config;
    
    private String getBaseUrl() {
        // Por padrão, usar porta padrão (a definir conforme implementação)
        return config.getServices().getAuditComplianceUrl() != null 
            ? config.getServices().getAuditComplianceUrl()
            : "http://localhost:8090";
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
     * Consulta um log de auditoria pelo auditId.
     * 
     * @param auditId UUID do log de auditoria
     * @return Resposta HTTP
     */
    public Response getAuditLog(String auditId) {
        RequestSpecification spec = RestAssured.given()
            .baseUri(getBaseUrl())
            .contentType(ContentType.JSON)
            .header("request-trace-id", getRequestTraceId());
        spec = addRequiredHeaders(spec);
        
        return spec.when()
            .get("/api/v1/audit/{auditId}", auditId)
            .then()
            .extract()
            .response();
    }
    
    /**
     * Lista logs de auditoria por userId.
     * 
     * @param userId UUID do usuário
     * @return Resposta HTTP
     */
    public Response getAuditLogsByUser(String userId) {
        RequestSpecification spec = RestAssured.given()
            .baseUri(getBaseUrl())
            .contentType(ContentType.JSON)
            .header("request-trace-id", getRequestTraceId());
        spec = addRequiredHeaders(spec);
        
        return spec.when()
            .get("/api/v1/audit/user/{userId}", userId)
            .then()
            .extract()
            .response();
    }
    
    /**
     * Lista logs de auditoria por messageId.
     * 
     * @param messageId UUID da mensagem
     * @return Resposta HTTP
     */
    public Response getAuditLogsByMessage(String messageId) {
        RequestSpecification spec = RestAssured.given()
            .baseUri(getBaseUrl())
            .contentType(ContentType.JSON)
            .header("request-trace-id", getRequestTraceId());
        spec = addRequiredHeaders(spec);
        
        return spec.when()
            .get("/api/v1/audit/message/{messageId}", messageId)
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
