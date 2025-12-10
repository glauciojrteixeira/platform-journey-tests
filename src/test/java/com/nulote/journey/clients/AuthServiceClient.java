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
 * Cliente HTTP para comunicação com o Auth Service.
 */
@Component
public class AuthServiceClient {
    
    @Autowired
    private E2EConfiguration config;
    
    private String getBaseUrl() {
        return config.getServices().getAuthUrl();
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
    
    public Response login(Object request) {
        RequestSpecification spec = RestAssured.given()
            .baseUri(getBaseUrl())
            .contentType(ContentType.JSON)
            .header("request-trace-id", getRequestTraceId());
        spec = addRequiredHeaders(spec);
        return spec.body(request)
            .when()
            .post("/api/v1/auth/login")
            .then()
            .extract()
            .response();
    }
    
    public Response requestOtp(Object request) {
        RequestSpecification spec = RestAssured.given()
            .baseUri(getBaseUrl())
            .contentType(ContentType.JSON)
            .header("request-trace-id", getRequestTraceId());
        
        spec = addRequiredHeaders(spec);
        spec = addSimulateProviderHeader(spec);
        
        return spec.body(request)
            .when()
            .post("/api/v1/auth/otp/request")
            .then()
            .extract()
            .response();
    }
    
    /**
     * Solicita OTP sem simulação (envio real ao provider)
     * 
     * @param request Dados da requisição OTP
     * @return Resposta HTTP
     */
    public Response requestOtpWithoutSimulation(Object request) {
        RequestSpecification spec = RestAssured.given()
            .baseUri(getBaseUrl())
            .contentType(ContentType.JSON)
            .header("request-trace-id", getRequestTraceId());
        spec = addRequiredHeaders(spec);
        // NÃO adicionar simulate-provider header
        
        return spec.body(request)
            .when()
            .post("/api/v1/auth/otp/request")
            .then()
            .extract()
            .response();
    }
    
    public Response validateOtp(Object request) {
        RequestSpecification spec = RestAssured.given()
            .baseUri(getBaseUrl())
            .contentType(ContentType.JSON)
            .header("request-trace-id", getRequestTraceId());
        spec = addRequiredHeaders(spec);
        return spec.body(request)
            .when()
            .post("/api/v1/auth/otp/validate")
            .then()
            .extract()
            .response();
    }
    
    /**
     * Verifica se usuário existe no Auth Service (indica que credenciais foram provisionadas).
     * Nota: Não há endpoint específico para credenciais. A existência do usuário no Auth Service
     * indica que as credenciais foram provisionadas via evento assíncrono.
     * 
     * @param userUuid UUID do usuário
     * @return Resposta HTTP (200 se usuário existe, 404 se não existe)
     */
    public Response getCredentialsByUserUuid(String userUuid) {
        RequestSpecification spec = RestAssured.given()
            .baseUri(getBaseUrl())
            .header("request-trace-id", getRequestTraceId());
        spec = addRequiredHeaders(spec);
        return spec.pathParam("uuid", userUuid)
            .when()
            .get("/api/v1/auth/users/{uuid}")
            .then()
            .extract()
            .response();
    }
    
    public Response validateToken(String token) {
        RequestSpecification spec = RestAssured.given()
            .baseUri(getBaseUrl())
            .contentType(ContentType.JSON)
            .header("request-trace-id", getRequestTraceId())
            .header("Authorization", "Bearer " + token);
        spec = addRequiredHeaders(spec);
        return spec.when()
            .post("/api/v1/auth/token/validate")
            .then()
            .extract()
            .response();
    }
    
    public Response logout(String token) {
        RequestSpecification spec = RestAssured.given()
            .baseUri(getBaseUrl())
            .contentType(ContentType.JSON)
            .header("request-trace-id", getRequestTraceId())
            .header("Authorization", "Bearer " + token);
        spec = addRequiredHeaders(spec);
        return spec.when()
            .post("/api/v1/auth/logout")
            .then()
            .extract()
            .response();
    }
    
    public Response changePassword(Object request, String token) {
        RequestSpecification spec = RestAssured.given()
            .baseUri(getBaseUrl())
            .contentType(ContentType.JSON)
            .header("request-trace-id", getRequestTraceId())
            .header("Authorization", "Bearer " + token);
        spec = addRequiredHeaders(spec);
        return spec.body(request)
            .when()
            .post("/api/v1/auth/password/change")
            .then()
            .extract()
            .response();
    }
    
    public Response revokeAllTokens(String userUuid, String token) {
        RequestSpecification spec = RestAssured.given()
            .baseUri(getBaseUrl())
            .contentType(ContentType.JSON)
            .header("request-trace-id", getRequestTraceId())
            .header("Authorization", "Bearer " + token);
        spec = addRequiredHeaders(spec);
        return spec.pathParam("userUuid", userUuid)
            .when()
            .post("/api/v1/auth/tokens/revoke-all/{userUuid}")
            .then()
            .extract()
            .response();
    }
    
    public Response recoverPassword(Object request) {
        RequestSpecification spec = RestAssured.given()
            .baseUri(getBaseUrl())
            .contentType(ContentType.JSON)
            .header("request-trace-id", getRequestTraceId());
        spec = addRequiredHeaders(spec);
        spec = addSimulateProviderHeader(spec); // Adicionar simulate-provider para garantir que código de teste seja salvo
        return spec.body(request)
            .when()
            .post("/api/v1/auth/password/recover")
            .then()
            .extract()
            .response();
    }
    
    public Response resetPassword(Object request) {
        RequestSpecification spec = RestAssured.given()
            .baseUri(getBaseUrl())
            .contentType(ContentType.JSON)
            .header("request-trace-id", getRequestTraceId());
        spec = addRequiredHeaders(spec);
        return spec.body(request)
            .when()
            .post("/api/v1/auth/password/reset")
            .then()
            .extract()
            .response();
    }
    
    /**
     * NOTA ARQUITETURAL: Este método existe apenas para casos específicos onde o Auth Service
     * precisa atualizar dados próprios (ex: role, position). Para dados de identidade (name, email, phone),
     * a atualização deve ser feita no Identity Service (fonte de verdade), que emite evento identity.updated
     * para sincronizar a cópia denormalizada no Auth Service.
     * 
     * @param uuid UUID do usuário
     * @param request Dados para atualização (apenas campos próprios do Auth Service)
     * @return Resposta HTTP
     */
    public Response updateUser(String uuid, Object request) {
        RequestSpecification spec = RestAssured.given()
            .baseUri(getBaseUrl())
            .contentType(ContentType.JSON)
            .header("request-trace-id", getRequestTraceId());
        spec = addRequiredHeaders(spec);
        return spec.pathParam("uuid", uuid)
            .body(request)
            .when()
            .put("/api/v1/auth/users/{uuid}")
            .then()
            .extract()
            .response();
    }
    
    /**
     * Obtém o código OTP do endpoint de teste quando simulate-provider está ativo.
     * Este endpoint é usado apenas para facilitar testes E2E.
     * 
     * @param otpId UUID do OTP
     * @return Resposta HTTP contendo o código OTP
     */
    public Response getTestOtpCode(String otpId) {
        RequestSpecification spec = RestAssured.given()
            .baseUri(getBaseUrl())
            .header("request-trace-id", getRequestTraceId());
        spec = addRequiredHeaders(spec);
        spec = addSimulateProviderHeader(spec); // Requerido para acessar o endpoint de teste
        return spec.pathParam("otpId", otpId)
            .when()
            .get("/api/v1/auth/otp/{otpId}/test-code")
            .then()
            .extract()
            .response();
    }
}

