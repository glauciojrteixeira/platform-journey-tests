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
    
    /**
     * Adiciona os headers obrigatórios de correlação e governança.
     * Inclui o header country-code para suporte multi-country (conforme refatoração).
     * 
     * @param spec RequestSpecification do RestAssured
     * @return RequestSpecification com headers adicionados
     */
    private io.restassured.specification.RequestSpecification addRequiredHeaders(io.restassured.specification.RequestSpecification spec) {
        spec = spec.header("request-caller", "e2e-tests")
                   .header("request-origin", "direct")
                   .header("country-code", config.getCountryCodeHeader()); // Multi-country: header lowercase conforme RFC 6648
        return spec;
    }
    
    public Response getProfileByUserUuid(String userUuid) {
        io.restassured.specification.RequestSpecification spec = RestAssured.given()
            .baseUri(getBaseUrl())
            .header("request-trace-id", getRequestTraceId());
        spec = addRequiredHeaders(spec);
        return spec.pathParam("userUuid", userUuid)
            .when()
            .get("/api/v1/profile/user/{userUuid}")
            .then()
            .extract()
            .response();
    }
    
    public Response updateProfile(String userUuid, Object request) {
        // Aguardar até 5 segundos pela criação do perfil (pode ser assíncrono)
        Response profileResponse = null;
        int maxAttempts = 10;
        int attempt = 0;
        
        while (attempt < maxAttempts) {
            profileResponse = getProfileByUserUuid(userUuid);
            if (profileResponse.getStatusCode() == 200) {
                break; // Perfil encontrado
            }
            
            // Se for 404, aguardar um pouco e tentar novamente (perfil pode estar sendo criado)
            if (profileResponse.getStatusCode() == 404) {
                try {
                    Thread.sleep(500); // Aguardar 500ms antes de tentar novamente
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                attempt++;
            } else {
                // Outro erro (não 404) - retornar imediatamente
                return profileResponse;
            }
        }
        
        // Se após todas as tentativas o perfil ainda não existe, retornar 404
        if (profileResponse == null || profileResponse.getStatusCode() != 200) {
            return profileResponse != null ? profileResponse : 
                RestAssured.given()
                    .baseUri(getBaseUrl())
                    .when()
                    .get("/api/v1/profile/user/" + userUuid)
                    .then()
                    .extract()
                    .response();
        }
        
        // Extrair UUID do perfil da resposta
        String profileUuid = profileResponse.jsonPath().getString("uuid");
        if (profileUuid == null) {
            profileUuid = profileResponse.jsonPath().getString("id");
        }
        
        if (profileUuid == null) {
            // Se não conseguiu extrair UUID, retornar erro
            return RestAssured.given()
                .baseUri(getBaseUrl())
                .when()
                .get("/api/v1/profile/user/" + userUuid)
                .then()
                .statusCode(404)
                .extract()
                .response();
        }
        
        io.restassured.specification.RequestSpecification spec = RestAssured.given()
            .baseUri(getBaseUrl())
            .contentType(io.restassured.http.ContentType.JSON)
            .header("request-trace-id", getRequestTraceId());
        spec = addRequiredHeaders(spec);
        return spec.pathParam("uuid", profileUuid)
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
        io.restassured.specification.RequestSpecification spec = RestAssured.given()
            .baseUri(getBaseUrl())
            .contentType(io.restassured.http.ContentType.JSON)
            .header("request-trace-id", getRequestTraceId());
        spec = addRequiredHeaders(spec);
        return spec.body(request)
            .when()
            .post("/api/v1/profile")
            .then()
            .extract()
            .response();
    }
}

