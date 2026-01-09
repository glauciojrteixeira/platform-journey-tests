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
    
    @Autowired(required = false)
    private com.nulote.journey.fixtures.UserFixture userFixture;
    
    private String getBaseUrl() {
        return config.getServices().getIdentityUrl();
    }
    
    private String getRequestTraceId() {
        return ExecutionContext.getExecutionId();
    }
    
    /**
     * Adiciona os headers obrigatórios de correlação e governança.
     * Inclui o header country-code para suporte multi-country (conforme refatoração).
     * 
     * Conforme playbook 006.00 - HTTP-HEADERS-CORRELATION.md:
     * - request-caller: Header obrigatório validado pelo CorrelationFilter
     * - request-origin: Header obrigatório validado pelo CorrelationFilter
     * - country-code: Header para suporte multi-country (lowercase conforme RFC 6648)
     * 
     * @param spec RequestSpecification do RestAssured
     * @return RequestSpecification com headers adicionados
     */
    private RequestSpecification addRequiredHeaders(RequestSpecification spec) {
        String countryCode = config.getCountryCodeHeader();
        spec = spec.header("request-caller", "e2e-tests")
                   .header("request-origin", "direct")
                   .header("country-code", countryCode); // Multi-country: header lowercase conforme RFC 6648
        
        // Logging para debug (apenas em nível debug para não poluir logs)
        var logger = org.slf4j.LoggerFactory.getLogger(IdentityServiceClient.class);
        logger.debug("🌍 [MULTI-COUNTRY] Header 'country-code: {}' adicionado à requisição", countryCode);
        
        return spec;
    }
    
    /**
     * Adiciona o header Authorization com JWT token se disponível.
     * 
     * @param spec RequestSpecification do RestAssured
     * @return RequestSpecification com header Authorization adicionado (se token disponível)
     */
    private RequestSpecification addAuthHeader(RequestSpecification spec) {
        if (userFixture != null) {
            String jwtToken = userFixture.getJwtToken();
            if (jwtToken != null && !jwtToken.trim().isEmpty()) {
                spec = spec.header("Authorization", "Bearer " + jwtToken);
                var logger = org.slf4j.LoggerFactory.getLogger(IdentityServiceClient.class);
                logger.debug("🔐 [AUTH] Header 'Authorization: Bearer ...' adicionado à requisição");
            } else {
                var logger = org.slf4j.LoggerFactory.getLogger(IdentityServiceClient.class);
                logger.debug("⚠️ [AUTH] JWT token não disponível - requisição será feita sem autenticação");
            }
        }
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
            var logger = org.slf4j.LoggerFactory.getLogger(IdentityServiceClient.class);
            logger.debug("✅ [SIMULATE-PROVIDER] Header 'simulate-provider: true' adicionado à requisição (ambiente: {})", 
                config.getEnvironment());
        } else {
            var logger = org.slf4j.LoggerFactory.getLogger(IdentityServiceClient.class);
            logger.debug("⚠️ [SIMULATE-PROVIDER] Header 'simulate-provider' NÃO adicionado (ambiente: {}, shouldSimulate: {})", 
                config.getEnvironment(), config.shouldSimulateProvider());
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
        
        // Log do request body para debug
        try {
            if (request instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> requestMap = (java.util.Map<String, Object>) request;
                logger.info("🔍 [IdentityClient] Request body antes de serializar: {}", requestMap);
                logger.info("🔍 [IdentityClient] documentType no request: '{}' (tipo: {})", 
                    requestMap.get("documentType"), 
                    requestMap.get("documentType") != null ? requestMap.get("documentType").getClass().getSimpleName() : "null");
            }
        } catch (Exception e) {
            logger.warn("⚠️ Erro ao logar request body: {}", e.getMessage());
        }
        
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
        spec = addAuthHeader(spec); // Adicionar autenticação JWT se disponível
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
        var logger = org.slf4j.LoggerFactory.getLogger(IdentityServiceClient.class);
        logger.info("🔄 [UPDATE] Preparando atualização de usuário: uuid={}, request={}", uuid, request);
        
        RequestSpecification spec = RestAssured.given()
            .baseUri(getBaseUrl())
            .contentType(ContentType.JSON)
            .header("request-trace-id", getRequestTraceId());
        spec = addRequiredHeaders(spec);
        spec = addAuthHeader(spec); // Adicionar autenticação JWT se disponível
        
        // Verificar se JWT foi adicionado
        if (userFixture != null) {
            String jwtToken = userFixture.getJwtToken();
            if (jwtToken != null && !jwtToken.trim().isEmpty()) {
                logger.info("✅ [UPDATE] JWT disponível para autenticação (length: {})", jwtToken.length());
            } else {
                logger.warn("⚠️ [UPDATE] JWT não disponível - requisição será feita sem autenticação");
            }
        } else {
            logger.warn("⚠️ [UPDATE] UserFixture não disponível - requisição será feita sem autenticação");
        }
        
        Response response = spec.body(request)
            .when()
            .put("/api/v1/identity/users/{uuid}", uuid)
            .then()
            .extract()
            .response();
        
        int statusCode = response.getStatusCode();
        String responseBody = response.getBody() != null ? response.getBody().asString() : "null";
        logger.info("📥 [UPDATE] Resposta do identity-service: status={}, body={}", statusCode, 
            responseBody.length() > 300 ? responseBody.substring(0, 300) + "..." : responseBody);
        
        if (statusCode == 500) {
            logger.error("❌ [UPDATE] Erro 500 ao atualizar usuário: uuid={}, body={}", uuid, responseBody);
        } else if (statusCode == 401 || statusCode == 403) {
            logger.error("❌ [UPDATE] Erro de autenticação/autorização: status={}, body={}", statusCode, responseBody);
        }
        
        return response;
    }
    
    public Response deactivateUser(String uuid) {
        RequestSpecification spec = RestAssured.given()
            .baseUri(getBaseUrl())
            .header("request-trace-id", getRequestTraceId());
        spec = addRequiredHeaders(spec);
        spec = addAuthHeader(spec); // Adicionar autenticação JWT se disponível
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
    
    /**
     * Cria entidade jurídica (Legal Entity).
     * 
     * @param request Dados da entidade jurídica (documentNumber, documentType, corporateName, tradeName, corporateEmail, phone)
     * @return Resposta HTTP
     */
    public Response createLegalEntity(Object request) {
        var logger = org.slf4j.LoggerFactory.getLogger(IdentityServiceClient.class);
        logger.info("Creating legal entity...");
        
        RequestSpecification spec = RestAssured.given()
            .baseUri(getBaseUrl())
            .contentType(ContentType.JSON)
            .header("request-trace-id", getRequestTraceId());
        
        spec = addRequiredHeaders(spec);
        spec = addSimulateProviderHeader(spec);
        
        // Log do request body para debug
        try {
            if (request instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> requestMap = (java.util.Map<String, Object>) request;
                logger.info("🔍 [IdentityClient] Legal entity request body: {}", requestMap);
                logger.info("🔍 [IdentityClient] documentType no request: '{}'", 
                    requestMap.get("documentType"));
            }
        } catch (Exception e) {
            logger.warn("⚠️ Erro ao logar request body: {}", e.getMessage());
        }
        
        Response response = spec.body(request)
            .when()
            .post("/api/v1/identity/legal-entities")
            .then()
            .extract()
            .response();
        
        // Log da resposta para debug
        if (response.getStatusCode() != 200 && response.getStatusCode() != 201) {
            logger.warn("Failed to create legal entity. Status: {}, Body: {}", 
                response.getStatusCode(), 
                response.getBody() != null ? response.getBody().asString() : "null");
        } else {
            logger.info("✅ Legal entity created successfully. Status: {}", response.getStatusCode());
        }
        
        return response;
    }
    
    /**
     * Busca entidade jurídica por UUID.
     * 
     * @param uuid UUID da entidade jurídica
     * @return Resposta HTTP
     */
    public Response getLegalEntityByUuid(String uuid) {
        RequestSpecification spec = RestAssured.given()
            .baseUri(getBaseUrl())
            .header("request-trace-id", getRequestTraceId());
        spec = addRequiredHeaders(spec);
        return spec.when()
            .get("/api/v1/identity/legal-entities/{uuid}", uuid)
            .then()
            .extract()
            .response();
    }
}

