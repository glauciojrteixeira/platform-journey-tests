package com.nulote.journey.clients;

import com.nulote.journey.config.E2EConfiguration;
import com.nulote.journey.fixtures.ExecutionContext;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Cliente HTTP para comunicação com o Profile Service.
 */
@Component
public class ProfileServiceClient {
    
    @Autowired
    private E2EConfiguration config;
    
    private String getBaseUrl() {
        return config.getServices().getProfileUrl();
    }
    
    private String getRequestTraceId() {
        return ExecutionContext.getExecutionId();
    }
    
    public Response getProfileByUserUuid(String userUuid) {
        return RestAssured.given()
            .baseUri(getBaseUrl())
            .header("request-trace-id", getRequestTraceId())
            .pathParam("userUuid", userUuid)
            .when()
            .get("/api/v1/profile/user/{userUuid}")
            .then()
            .extract()
            .response();
    }
    
    public Response updateProfile(String userUuid, Object request) {
        // Primeiro buscar o perfil para obter o UUID do perfil
        Response profileResponse = getProfileByUserUuid(userUuid);
        if (profileResponse.getStatusCode() != 200) {
            return profileResponse; // Retornar erro se perfil não existe
        }
        
        // Extrair UUID do perfil da resposta
        String profileUuid = profileResponse.jsonPath().getString("uuid");
        if (profileUuid == null) {
            profileUuid = profileResponse.jsonPath().getString("id");
        }
        
        return RestAssured.given()
            .baseUri(getBaseUrl())
            .contentType(io.restassured.http.ContentType.JSON)
            .header("request-trace-id", getRequestTraceId())
            .pathParam("uuid", profileUuid)
            .body(request)
            .when()
            .put("/api/v1/profile/{uuid}")
            .then()
            .extract()
            .response();
    }
    
    /**
     * Cria um perfil para o usuário (fallback quando perfil não é criado automaticamente).
     * 
     * @param userUuid UUID do usuário
     * @param request Dados do perfil a serem criados (deve conter userUuid, language, notifications, validationChannel, relationship)
     * @return Resposta HTTP
     */
    public Response createProfile(String userUuid, Object request) {
        return RestAssured.given()
            .baseUri(getBaseUrl())
            .contentType(io.restassured.http.ContentType.JSON)
            .header("request-trace-id", getRequestTraceId())
            .body(request)
            .when()
            .post("/api/v1/profile")
            .then()
            .extract()
            .response();
    }
}

