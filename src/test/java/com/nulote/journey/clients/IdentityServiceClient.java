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
 * Cliente HTTP para comunicação com o Identity Service.
 */
@Component
public class IdentityServiceClient {
    
    @Autowired
    private E2EConfiguration config;
    
    private String getBaseUrl() {
        return config.getServices().getIdentityUrl();
    }
    
    private String getRequestTraceId() {
        return ExecutionContext.getExecutionId();
    }
    
    /**
     * Adiciona os headers obrigatórios de correlação e governança.
     * 
     * @param spec RequestSpecification do RestAssured
     * @return RequestSpecification com headers adicionados
     */
    private RequestSpecification addRequiredHeaders(RequestSpecification spec) {
        spec = spec.header("request-caller", "e2e-tests")
                   .header("request-origin", "direct");
        return spec;
    }
    
    /**
     * Adiciona o header simulate-provider se a simulação estiver habilitada.
     * 
     * @param spec RequestSpecification do RestAssured
     * @return RequestSpecification com header adicionado (se necessário)
     */
    private RequestSpecification addSimulateProviderHeader(RequestSpecification spec) {
        if (config.shouldSimulateProvider()) {
            spec = spec.header("simulate-provider", "true");
        }
        return spec;
    }
    
    /**
     * Cria usuário. Se sessionToken estiver disponível, será usado automaticamente.
     * 
     * @param request Dados do usuário
     * @param sessionToken Token de sessão opcional (obtido após validação de OTP)
     * @return Resposta HTTP
     */
    public Response createUser(Object request, String sessionToken) {
        // Se sessionToken está disponível, usar método específico
        if (sessionToken != null && !sessionToken.trim().isEmpty()) {
            return createUserWithSessionToken(request, sessionToken);
        }
        
        // Caso contrário, criar sem sessionToken (para compatibilidade com testes antigos)
        // NOTA: A API agora exige registration-token, então isso provavelmente falhará
        // Mas mantemos para não quebrar código existente que não usa OTP
        RequestSpecification spec = RestAssured.given()
            .baseUri(getBaseUrl())
            .contentType(ContentType.JSON)
            .header("request-trace-id", getRequestTraceId());
        
        spec = addRequiredHeaders(spec);
        spec = addSimulateProviderHeader(spec);
        
        return spec.body(request)
            .when()
            .post("/api/v1/identity/users")
            .then()
            .extract()
            .response();
    }
    
    /**
     * Cria usuário sem sessionToken (método de compatibilidade).
     * NOTA: A API agora exige registration-token, então este método provavelmente falhará.
     * Use createUser(request, sessionToken) quando possível.
     * 
     * @param request Dados do usuário
     * @return Resposta HTTP
     */
    public Response createUser(Object request) {
        return createUser(request, null);
    }
    
    /**
     * Cria usuário com sessionToken (para registro via OTP)
     * 
     * @param request Dados do usuário
     * @param sessionToken Token de sessão obtido após validação de OTP
     * @return Resposta HTTP
     */
    public Response createUserWithSessionToken(Object request, String sessionToken) {
        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            throw new IllegalArgumentException("SessionToken não pode ser null ou vazio");
        }
        
        var logger = org.slf4j.LoggerFactory.getLogger(IdentityServiceClient.class);
        logger.info("Creating user with sessionToken: {}... (length: {})", 
            sessionToken.length() > 8 ? sessionToken.substring(0, 8) : sessionToken,
            sessionToken.length());
        
        // Construir todos os headers explicitamente - IMPORTANTE: adicionar registration-token ANTES de addRequiredHeaders
        RequestSpecification spec = RestAssured.given()
            .baseUri(getBaseUrl())
            .contentType(ContentType.JSON)
            .header("registration-token", sessionToken)  // Adicionar PRIMEIRO
            .header("request-trace-id", getRequestTraceId());
        
        spec = addRequiredHeaders(spec);
        spec = addSimulateProviderHeader(spec);
        
        // Verificar se o header ainda está presente (debug)
        logger.debug("Request URL: {}/api/v1/identity/users", getBaseUrl());
        logger.debug("SessionToken being sent: {}...", sessionToken.length() > 8 ? sessionToken.substring(0, 8) : sessionToken);
        
        Response response = spec.body(request)
            .when()
            .post("/api/v1/identity/users")
            .then()
            .extract()
            .response();
        
        // Log da resposta para debug
        if (response.getStatusCode() != 200 && response.getStatusCode() != 201) {
            logger.warn("Failed to create user. Status: {}, Body: {}", 
                response.getStatusCode(), 
                response.getBody() != null ? response.getBody().asString() : "null");
        }
        
        return response;
    }
    
    public Response getUserByUuid(String uuid) {
        RequestSpecification spec = RestAssured.given()
            .baseUri(getBaseUrl())
            .header("request-trace-id", getRequestTraceId());
        spec = addRequiredHeaders(spec);
        return spec.when()
            .get("/api/v1/identity/users/{uuid}", uuid)
            .then()
            .extract()
            .response();
    }
    
    public Response findUserByEmail(String email) {
        RequestSpecification spec = RestAssured.given()
            .baseUri(getBaseUrl())
            .header("request-trace-id", getRequestTraceId());
        spec = addRequiredHeaders(spec);
        return spec.queryParam("email", email)
            .when()
            .get("/api/v1/identity/users/search")
            .then()
            .extract()
            .response();
    }
    
    public Response updateUser(String uuid, Object request) {
        RequestSpecification spec = RestAssured.given()
            .baseUri(getBaseUrl())
            .contentType(ContentType.JSON)
            .header("request-trace-id", getRequestTraceId());
        spec = addRequiredHeaders(spec);
        return spec.body(request)
            .when()
            .put("/api/v1/identity/users/{uuid}", uuid)
            .then()
            .extract()
            .response();
    }
    
    public Response deactivateUser(String uuid) {
        RequestSpecification spec = RestAssured.given()
            .baseUri(getBaseUrl())
            .header("request-trace-id", getRequestTraceId());
        spec = addRequiredHeaders(spec);
        return spec.when()
            .delete("/api/v1/identity/users/{uuid}", uuid)
            .then()
            .extract()
            .response();
    }
    
    public Response reactivateUser(String uuid) {
        RequestSpecification spec = RestAssured.given()
            .baseUri(getBaseUrl())
            .header("request-trace-id", getRequestTraceId());
        spec = addRequiredHeaders(spec);
        return spec.when()
            .post("/api/v1/identity/users/{uuid}/reactivate", uuid)
            .then()
            .extract()
            .response();
    }
}

