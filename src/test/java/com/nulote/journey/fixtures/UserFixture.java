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
        // CORREÇÃO: Garantir que userData seja sempre mutável
        // DataTables do Cucumber podem retornar Maps imutáveis
        if (userData != null) {
            // Criar uma cópia mutável do Map
            this.userData = new java.util.HashMap<>(userData);
        } else {
            this.userData = null;
        }
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
        // Para outros propósitos (PASSWORD_RECOVERY, LOGIN, etc.), userUuid é necessário
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
        
        // CORREÇÃO 2: Se purpose for LOGIN mas a API não aceitar, usar PASSWORD_RECOVERY como fallback
        // ou verificar se precisa de ajuste na API. Por enquanto, manter LOGIN mas garantir userUuid
        // Se a API realmente não aceitar LOGIN, o teste falhará e poderemos ajustar
        
        request.put("channel", channel);
        
        // CORREÇÃO 2: A API não aceita "LOGIN" como purpose válido
        // Apenas REGISTRATION e PASSWORD_RECOVERY são aceitos
        // Se purpose for LOGIN, usar PASSWORD_RECOVERY como fallback (ou marcar cenário como @not_implemented)
        String validPurpose = purpose;
        if ("LOGIN".equals(purpose)) {
            // A API não suporta LOGIN ainda - usar PASSWORD_RECOVERY como alternativa
            // ou marcar cenário como @not_implemented
            // Por enquanto, manter LOGIN para que o teste falhe e identifique o problema
            // O cenário deve estar marcado como @not_implemented
            validPurpose = purpose; // Manter para que erro seja claro
        }
        request.put("purpose", validPurpose);
        
        // CORREÇÃO 1: Sempre incluir email quando channel for EMAIL
        // Se userData não tiver email, gerar um único para evitar erros de validação
        if ("EMAIL".equals(channel)) {
            String email = null;
            var logger = org.slf4j.LoggerFactory.getLogger(UserFixture.class);
            
            // CORREÇÃO CRÍTICA: Garantir que userData seja sempre mutável
            // DataTables do Cucumber retornam UnmodifiableMap, então precisamos criar cópia mutável
            if (userData != null && !(userData instanceof java.util.HashMap)) {
                logger.debug("🔧 [TROUBLESHOOTING] userData não é mutável, criando cópia mutável");
                userData = new java.util.HashMap<>(userData);
                setUserData(userData);
                userData = getUserData(); // Obter referência atualizada
            }
            
            // Tentar obter email do userData
            if (userData != null) {
                Object emailObj = userData.get("email");
                if (emailObj != null) {
                    email = emailObj.toString().trim();
                    logger.debug("🔧 [TROUBLESHOOTING] Email obtido do userData: {}", email);
                }
            }
            
            // Se não encontrou email válido, gerar um novo
            if (email == null || email.isEmpty()) {
                email = com.nulote.journey.fixtures.TestDataGenerator.generateUniqueEmail();
                logger.debug("🔧 [TROUBLESHOOTING] Email gerado automaticamente: {}", email);
                
                // Armazenar no userData para uso futuro
                if (userData == null) {
                    userData = new java.util.HashMap<>();
                    setUserData(userData);
                    userData = getUserData(); // Obter referência atualizada
                }
                
                // Garantir que userData é mutável antes de fazer put
                if (!(userData instanceof java.util.HashMap)) {
                    logger.warn("⚠️ [TROUBLESHOOTING] userData ainda não é mutável após setUserData, forçando cópia");
                    userData = new java.util.HashMap<>(userData);
                    setUserData(userData);
                    userData = getUserData(); // Obter referência atualizada
                }
                
                userData.put("email", email);
                logger.debug("🔧 [TROUBLESHOOTING] Email armazenado no userData: {}", email);
            }
            
            // Garantir que email não seja null ou vazio
            if (email == null || email.trim().isEmpty()) {
                throw new IllegalStateException("Email não pode ser null ou vazio para channel EMAIL");
            }
            
            // Validar formato básico de email
            if (!email.contains("@") || !email.contains(".")) {
                logger.warn("⚠️ [TROUBLESHOOTING] Email gerado pode ter formato inválido: {}. Gerando novo...", email);
                email = com.nulote.journey.fixtures.TestDataGenerator.generateUniqueEmail();
                
                // Garantir que userData é mutável
                if (userData == null) {
                    userData = new java.util.HashMap<>();
                    setUserData(userData);
                    userData = getUserData();
                } else if (!(userData instanceof java.util.HashMap)) {
                    userData = new java.util.HashMap<>(userData);
                    setUserData(userData);
                    userData = getUserData();
                }
                
                userData.put("email", email);
                logger.debug("🔧 [TROUBLESHOOTING] Novo email armazenado após validação: {}", email);
            }
            
            // Sempre incluir email na requisição como String
            String emailTrimmed = email.trim();
            request.put("email", emailTrimmed);
            logger.info("✅ [TROUBLESHOOTING] Email incluído na requisição OTP (tipo: {}, valor: {})", 
                emailTrimmed.getClass().getSimpleName(), emailTrimmed);
            
            // Validação final: garantir que email está realmente no request
            if (!request.containsKey("email")) {
                logger.error("❌ [TROUBLESHOOTING] FALHA CRÍTICA: Email não encontrado no request após put()!");
                logger.error("❌ [TROUBLESHOOTING] Campos no request: {}", request.keySet());
                throw new IllegalStateException("FALHA CRÍTICA: Email não foi incluído no request após put()!");
            }
            
            Object emailInRequest = request.get("email");
            if (emailInRequest == null) {
                logger.error("❌ [TROUBLESHOOTING] FALHA CRÍTICA: Email está null no request!");
                logger.error("❌ [TROUBLESHOOTING] Request completo: {}", request);
                throw new IllegalStateException("FALHA CRÍTICA: Email está null no request!");
            }
            
            logger.debug("🔧 [TROUBLESHOOTING] Validação final: email confirmado no request - {}", emailInRequest);
        } else {
            var logger = org.slf4j.LoggerFactory.getLogger(UserFixture.class);
            logger.debug("🔧 [TROUBLESHOOTING] Channel não é EMAIL ({}), email não será incluído", channel);
        }
        
        // Para WHATSAPP, incluir phone se disponível
        if ("WHATSAPP".equals(channel)) {
            if (userData != null && userData.get("telefone") != null) {
                request.put("phone", userData.get("telefone"));
            } else if (userData != null && userData.get("phone") != null) {
                request.put("phone", userData.get("phone"));
            }
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

