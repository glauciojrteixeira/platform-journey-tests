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
    
    public Response createUser(Object request) {
        RequestSpecification spec = RestAssured.given()
            .baseUri(getBaseUrl())
            .contentType(ContentType.JSON)
            .header("request-trace-id", getRequestTraceId());
        
        spec = addSimulateProviderHeader(spec);
        
        return spec.body(request)
            .when()
            .post("/api/v1/identity/users")
            .then()
            .extract()
            .response();
    }
    
    public Response getUserByUuid(String uuid) {
        return RestAssured.given()
            .baseUri(getBaseUrl())
            .header("request-trace-id", getRequestTraceId())
            .when()
            .get("/api/v1/identity/users/{uuid}", uuid)
            .then()
            .extract()
            .response();
    }
    
    public Response findUserByEmail(String email) {
        return RestAssured.given()
            .baseUri(getBaseUrl())
            .header("request-trace-id", getRequestTraceId())
            .queryParam("email", email)
            .when()
            .get("/api/v1/identity/users/search")
            .then()
            .extract()
            .response();
    }
    
    public Response updateUser(String uuid, Object request) {
        return RestAssured.given()
            .baseUri(getBaseUrl())
            .contentType(ContentType.JSON)
            .header("request-trace-id", getRequestTraceId())
            .body(request)
            .when()
            .put("/api/v1/identity/users/{uuid}", uuid)
            .then()
            .extract()
            .response();
    }
    
    public Response deactivateUser(String uuid) {
        return RestAssured.given()
            .baseUri(getBaseUrl())
            .header("request-trace-id", getRequestTraceId())
            .when()
            .delete("/api/v1/identity/users/{uuid}", uuid)
            .then()
            .extract()
            .response();
    }
    
    public Response reactivateUser(String uuid) {
        return RestAssured.given()
            .baseUri(getBaseUrl())
            .header("request-trace-id", getRequestTraceId())
            .when()
            .post("/api/v1/identity/users/{uuid}/reactivate", uuid)
            .then()
            .extract()
            .response();
    }
}

