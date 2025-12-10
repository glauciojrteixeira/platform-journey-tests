package com.nulote.journey.fixtures;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Fixture para construção de dados de usuário em testes E2E.
 * Facilita a criação de objetos de requisição e mantém estado durante execução de cenários.
 */
@Component
public class UserFixture {
    
    private Map<String, String> userData;
    private String createdUserUuid;
    private String otpCode;
    private String sessionToken;
    
    /**
     * Define os dados do usuário a partir de uma DataTable do Cucumber
     * 
     * @param userData Mapa com dados do usuário
     */
    public void setUserData(Map<String, String> userData) {
        this.userData = userData;
    }
    
    /**
     * Retorna os dados do usuário
     * 
     * @return Mapa com dados do usuário
     */
    public Map<String, String> getUserData() {
        return userData;
    }
    
    /**
     * Define o UUID do usuário criado
     * 
     * @param userUuid UUID do usuário
     */
    public void setCreatedUserUuid(String userUuid) {
        this.createdUserUuid = userUuid;
    }
    
    /**
     * Retorna o UUID do usuário criado
     * 
     * @return UUID do usuário ou null se ainda não foi criado
     */
    public String getCreatedUserUuid() {
        return createdUserUuid;
    }
    
    /**
     * Define o código OTP recebido
     * 
     * @param otpCode Código OTP
     */
    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }
    
    /**
     * Retorna o código OTP
     * 
     * @return Código OTP ou null se ainda não foi recebido
     */
    public String getOtpCode() {
        return otpCode;
    }
    
    /**
     * Define o sessionToken obtido após validação de OTP
     * 
     * @param sessionToken Token de sessão
     */
    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
    
    /**
     * Retorna o sessionToken
     * 
     * @return SessionToken ou null se ainda não foi obtido
     */
    public String getSessionToken() {
        return sessionToken;
    }
    
    /**
     * Constrói um objeto de requisição para criar usuário no Identity Service
     * 
     * @return Objeto de requisição (Map para ser usado com RestAssured)
     */
    public Map<String, Object> buildCreateUserRequest() {
        var request = new java.util.HashMap<String, Object>();
        request.put("name", userData.get("nome") != null ? userData.get("nome") : userData.get("name"));
        request.put("cpf", userData.get("cpf"));
        request.put("email", userData.get("email"));
        request.put("phone", userData.get("telefone") != null ? userData.get("telefone") : userData.get("phone"));
        request.put("role", userData.getOrDefault("role", "INDIVIDUAL"));
        request.put("relationship", userData.getOrDefault("relationship", "B2C"));
        if (userData.containsKey("position")) {
            request.put("position", userData.get("position"));
        }
        return request;
    }
    
    private String otpUuid;
    
    /**
     * Define o UUID do OTP criado
     * 
     * @param otpUuid UUID do OTP
     */
    public void setOtpUuid(String otpUuid) {
        this.otpUuid = otpUuid;
    }
    
    /**
     * Retorna o UUID do OTP
     * 
     * @return UUID do OTP ou null se ainda não foi criado
     */
    public String getOtpUuid() {
        return otpUuid;
    }
    
    /**
     * Constrói um objeto de requisição para solicitar OTP
     * 
     * @param channel Canal de envio (EMAIL, WHATSAPP)
     * @param purpose Propósito do OTP (REGISTRATION, PASSWORD_RECOVERY, etc.)
     * @return Objeto de requisição (Map para ser usado com RestAssured)
     */
    public Map<String, Object> buildOtpRequest(String channel, String purpose) {
        var request = new java.util.HashMap<String, Object>();
        // Para REGISTRATION, userUuid pode ser null (usuário ainda não existe)
        // Para outros propósitos (PASSWORD_RECOVERY, etc.), userUuid é necessário
        if (!"REGISTRATION".equals(purpose)) {
            String userUuid = createdUserUuid != null ? createdUserUuid : (userData != null ? userData.get("userUuid") : null);
            if (userUuid == null) {
                throw new IllegalStateException("User UUID is required to request OTP for purpose: " + purpose + ". Create user first or set userUuid in userData.");
            }
            request.put("userUuid", userUuid);
        } else {
            // Para REGISTRATION, userUuid é null
            request.put("userUuid", null);
        }
        request.put("channel", channel);
        request.put("purpose", purpose);
        // Adicionar email se disponível (para REGISTRATION)
        if (userData != null && userData.get("email") != null) {
            request.put("email", userData.get("email"));
        }
        return request;
    }
    
    /**
     * Constrói um objeto de requisição para validar OTP
     * 
     * @param code Código OTP a ser validado
     * @return Objeto de requisição (Map para ser usado com RestAssured)
     */
    public Map<String, Object> buildOtpValidationRequest(String code) {
        var request = new java.util.HashMap<String, Object>();
        String otpId = otpUuid != null ? otpUuid : userData.get("otpUuid");
        if (otpId == null) {
            throw new IllegalStateException("OTP UUID is required to validate OTP. Request OTP first or set otpUuid.");
        }
        request.put("otpUuid", otpId);
        request.put("code", code);
        return request;
    }
    
    /**
     * Constrói um objeto de requisição para login
     * 
     * @return Objeto de requisição (Map para ser usado com RestAssured)
     */
    public Map<String, String> buildLoginRequest() {
        var request = new java.util.HashMap<String, String>();
        // A API usa username (que pode ser email ou CPF)
        String username = userData.get("email") != null ? userData.get("email") : userData.get("cpf");
        request.put("username", username);
        request.put("password", userData.getOrDefault("password", "TestPassword123!"));
        return request;
    }
}

