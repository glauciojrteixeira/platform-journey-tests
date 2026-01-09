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
    private String jwtToken;
    private String providerEmail; // Email do provider para account linking (quando corresponde ao email do usuário existente)
    
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
     * Define o JWT token obtido após autenticação
     * 
     * @param jwtToken Token JWT
     */
    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }
    
    /**
     * Retorna o JWT token
     * 
     * @return JWT token ou null se ainda não foi obtido
     */
    public String getJwtToken() {
        return jwtToken;
    }
    
    /**
     * Define o email do provider para account linking (quando corresponde ao email do usuário existente)
     * 
     * @param providerEmail Email do provider
     */
    public void setProviderEmail(String providerEmail) {
        this.providerEmail = providerEmail;
    }
    
    /**
     * Retorna o email do provider
     * 
     * @return Email do provider ou null se ainda não foi configurado
     */
    public String getProviderEmail() {
        return providerEmail;
    }
    
    /**
     * Constrói um objeto de requisição para criar usuário no Identity Service.
     * 
     * @return Objeto de requisição (Map para ser usado com RestAssured)
     */
    public Map<String, Object> buildCreateUserRequest() {
        var request = new java.util.HashMap<String, Object>();
        request.put("name", userData.get("nome") != null ? userData.get("nome") : userData.get("name"));
        
        // Usar documentNumber e documentType (formato atual)
        // Para testes de validação, permitir valores null - o backend deve validar
        Object documentNumberObj = userData.get("documentNumber");
        Object documentTypeObj = userData.get("documentType");
        
        // Normalizar documentType para uppercase (backend espera uppercase: CPF, CNPJ, etc.)
        // IMPORTANTE: Sempre normalizar, mesmo que já tenha sido normalizado antes
        // Isso garante que valores do Examples ou qualquer outro lugar sejam sempre uppercase
        String documentType = null;
        var logger = org.slf4j.LoggerFactory.getLogger(UserFixture.class);
        
        logger.info("🔍 [UserFixture] buildCreateUserRequest - documentTypeObj: '{}' (tipo: {})", 
            documentTypeObj, documentTypeObj != null ? documentTypeObj.getClass().getSimpleName() : "null");
        logger.info("🔍 [UserFixture] userData completo: {}", userData);
        
        if (documentTypeObj != null) {
            String docTypeStr = documentTypeObj.toString();
            logger.info("🔍 [UserFixture] DocumentType original (toString): '{}' (tipo: {})", docTypeStr, documentTypeObj.getClass().getSimpleName());
            
            if (docTypeStr != null && !docTypeStr.trim().isEmpty() && !docTypeStr.trim().equals("null")) {
                documentType = docTypeStr.trim().toUpperCase();
                logger.info("✅ [UserFixture] DocumentType após trim e uppercase: '{}'", documentType);
                
                // Validar que o documentType está na lista aceita pelo backend
                // Backend aceita: CPF, CNPJ, CUIT, DNI, RUT, CI, SSN
                String[] validTypes = {"CPF", "CNPJ", "CUIT", "DNI", "RUT", "CI", "SSN"};
                boolean isValid = false;
                for (String validType : validTypes) {
                    if (validType.equals(documentType)) {
                        isValid = true;
                        break;
                    }
                }
                
                if (!isValid) {
                    logger.warn("⚠️ DocumentType '{}' não está na lista de tipos aceitos pelo backend: CPF, CNPJ, CUIT, DNI, RUT, CI, SSN", documentType);
                    logger.warn("⚠️ Isso pode causar erro de validação no backend. Verifique o feature file.");
                }
                
                // Se ficou vazio após trim e uppercase, usar null
                if (documentType.isEmpty()) {
                    logger.warn("DocumentType ficou vazio após trim e uppercase, usando null");
                    documentType = null;
                }
            } else {
                logger.debug("DocumentType é null, vazio ou 'null' após toString, usando null");
                documentType = null;
            }
        } else {
            logger.debug("DocumentTypeObj é null, usando null para documentType");
            documentType = null;
        }
        
        // Adicionar documentNumber e documentType (podem ser null para testes de validação)
        request.put("documentNumber", documentNumberObj);
        // CORREÇÃO CRÍTICA: NÃO adicionar documentType ao request se for null
        // Se adicionarmos null, o RestAssured pode omitir, mas o backend pode inferir CPF quando o campo não está presente
        // Para testes de validação que esperam falha quando documentType é null, NÃO incluir o campo no request
        if (documentType != null && !documentType.trim().isEmpty()) {
            request.put("documentType", documentType);
            logger.info("✅ [UserFixture] documentType adicionado ao request: '{}'", documentType);
        } else {
            // NÃO adicionar documentType ao request quando for null
            // Isso permite que o backend valide e retorne erro apropriado
            logger.info("ℹ️ [UserFixture] documentType é null - NÃO adicionando ao request (teste de validação)");
        }
        
        // Adicionar outros campos
        request.put("email", userData.get("email"));
        request.put("phone", userData.get("telefone") != null ? userData.get("telefone") : userData.get("phone"));
        request.put("role", userData.getOrDefault("role", "INDIVIDUAL"));
        request.put("relationship", userData.getOrDefault("relationship", "B2C"));
        if (userData.containsKey("position")) {
            request.put("position", userData.get("position"));
        }
        
        // Log final para debug - verificar se documentType ainda está presente
        logger.info("🔍 [UserFixture] Request final - documentType: '{}' (documentTypeObj: '{}')", documentType, documentTypeObj);
        logger.info("🔍 [UserFixture] Request completo: {}", request);
        
        // VERIFICAÇÃO FINAL CRÍTICA: Garantir que documentType está presente no request
        Object finalDocumentTypeInRequest = request.get("documentType");
        logger.info("🔍 [UserFixture] documentType no request após adicionar todos os campos: '{}' (tipo: {})", 
            finalDocumentTypeInRequest, 
            finalDocumentTypeInRequest != null ? finalDocumentTypeInRequest.getClass().getSimpleName() : "null");
        
        // Se documentType não está presente ou é null quando deveria ter valor, adicionar novamente
        // Isso garante que mesmo se algo sobrescreveu o valor, ele será restaurado
        if (documentType != null && finalDocumentTypeInRequest == null) {
            logger.warn("⚠️ [UserFixture] documentType estava null no request mas deveria ser '{}'. Restaurando...", documentType);
            request.put("documentType", documentType);
        } else if (documentType != null && !documentType.equals(finalDocumentTypeInRequest)) {
            logger.warn("⚠️ [UserFixture] documentType no request ('{}') difere do esperado ('{}'). Corrigindo...", 
                finalDocumentTypeInRequest, documentType);
            request.put("documentType", documentType);
        }
        
        // Verificação final absoluta
        Object verifiedDocumentType = request.get("documentType");
        logger.info("✅ [UserFixture] VERIFICAÇÃO FINAL - documentType no request: '{}'", verifiedDocumentType);
        
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
        // A API usa username (que pode ser email ou documentNumber)
        String username = userData.get("email") != null ? userData.get("email") : userData.get("documentNumber");
        request.put("username", username);
        request.put("password", userData.getOrDefault("password", "TestPassword123!"));
        return request;
    }
}

