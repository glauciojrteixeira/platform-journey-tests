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
     * Verifica se estamos em ambiente local ou de teste.
     * Em ambientes locais/testes, o rate limit é mais permissivo (100 req/hora),
     * então não devemos usar o delay da API que é configurado para PROD (5 req/hora).
     * 
     * @return true se estamos em ambiente local ou teste
     */
    private boolean isLocalOrTestEnvironment() {
        String env = config.getEnvironment();
        return env != null && (env.equalsIgnoreCase("local") || 
                              env.equalsIgnoreCase("test") || 
                              env.equalsIgnoreCase("dev"));
    }
    
    /**
     * Adiciona os headers obrigatórios de correlação e governança.
     * Inclui o header country-code para suporte multi-country (conforme refatoração).
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
        var logger = org.slf4j.LoggerFactory.getLogger(AuthServiceClient.class);
        logger.debug("🌍 [MULTI-COUNTRY] Header 'country-code: {}' adicionado à requisição", countryCode);
        
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
            var logger = org.slf4j.LoggerFactory.getLogger(AuthServiceClient.class);
            logger.debug("✅ [SIMULATE-PROVIDER] Header 'simulate-provider: true' adicionado à requisição (ambiente: {})", 
                config.getEnvironment());
        } else {
            var logger = org.slf4j.LoggerFactory.getLogger(AuthServiceClient.class);
            logger.debug("⚠️ [SIMULATE-PROVIDER] Header 'simulate-provider' NÃO adicionado (ambiente: {}, shouldSimulate: {})", 
                config.getEnvironment(), config.shouldSimulateProvider());
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
        // Usar configuração de retry do E2EConfiguration
        if (config.getRateLimitRetry().getEnabled()) {
            return requestOtpWithRetry(
                request, 
                config.getRateLimitRetry().getMaxAttempts(),
                config.getRateLimitRetry().getInitialDelayMs()
            );
        } else {
            // Se retry estiver desabilitado, fazer requisição simples
            return requestOtpSimple(request);
        }
    }
    
    /**
     * Solicita OTP sem retry (requisição simples).
     * 
     * @param request Dados da requisição OTP
     * @return Resposta HTTP
     */
    private Response requestOtpSimple(Object request) {
        RequestSpecification spec = RestAssured.given()
            .baseUri(getBaseUrl())
            .contentType(ContentType.JSON)
            .header("request-trace-id", getRequestTraceId());
        
        spec = addRequiredHeaders(spec);
        spec = addSimulateProviderHeader(spec);
        
        // Log detalhado do request para debug
        var logger = org.slf4j.LoggerFactory.getLogger(AuthServiceClient.class);
        logger.info("🔧 [TROUBLESHOOTING] Preparando requisição OTP para {}", getBaseUrl() + "/api/v1/auth/otp/request");
        
        if (request instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> requestMap = (java.util.Map<String, Object>) request;
            
            logger.info("🔧 [TROUBLESHOOTING] Request OTP - Total de campos: {}", requestMap.size());
            logger.info("🔧 [TROUBLESHOOTING] Request OTP - Campos: {}", requestMap.keySet());
            
            // Validação detalhada de email
            if (requestMap.containsKey("email")) {
                Object emailObj = requestMap.get("email");
                if (emailObj != null) {
                    String emailStr = emailObj.toString().trim();
                    logger.info("✅ [TROUBLESHOOTING] Email presente no request: {} (tipo: {})", 
                        emailStr, emailObj.getClass().getSimpleName());
                    
                    // Validação de formato básico
                    if (!emailStr.contains("@") || !emailStr.contains(".")) {
                        logger.warn("⚠️ [TROUBLESHOOTING] Email pode ter formato inválido: {}", emailStr);
                    }
                } else {
                    logger.error("❌ [TROUBLESHOOTING] Email está presente mas é NULL!");
                }
            } else {
                logger.error("❌ [TROUBLESHOOTING] Email NÃO encontrado no request!");
                logger.error("❌ [TROUBLESHOOTING] Campos disponíveis: {}", requestMap.keySet());
                logger.error("❌ [TROUBLESHOOTING] Request completo: {}", requestMap);
            }
            
            // Log de outros campos importantes
            if (requestMap.containsKey("channel")) {
                logger.debug("🔧 [TROUBLESHOOTING] Channel: {}", requestMap.get("channel"));
            }
            if (requestMap.containsKey("purpose")) {
                logger.debug("🔧 [TROUBLESHOOTING] Purpose: {}", requestMap.get("purpose"));
            }
            if (requestMap.containsKey("userUuid")) {
                logger.debug("🔧 [TROUBLESHOOTING] UserUuid: {}", requestMap.get("userUuid"));
            }
        } else {
            logger.warn("⚠️ [TROUBLESHOOTING] Request não é um Map, tipo: {}", 
                request != null ? request.getClass().getName() : "null");
        }
        
        return spec.body(request)
            .when()
            .post("/api/v1/auth/otp/request")
            .then()
            .extract()
            .response();
    }
    
    /**
     * Solicita OTP com retry automático para rate limiting (429).
     * 
     * @param request Dados da requisição OTP
     * @param maxRetries Número máximo de tentativas
     * @param initialDelayMs Delay inicial em milissegundos (backoff exponencial)
     * @return Resposta HTTP
     */
    public Response requestOtpWithRetry(Object request, int maxRetries, long initialDelayMs) {
        var logger = org.slf4j.LoggerFactory.getLogger(AuthServiceClient.class);
        Response response = null;
        int attempt = 0;
        
        while (attempt < maxRetries) {
            attempt++;
            
            RequestSpecification spec = RestAssured.given()
                .baseUri(getBaseUrl())
                .contentType(ContentType.JSON)
                .header("request-trace-id", getRequestTraceId());
            
            spec = addRequiredHeaders(spec);
            spec = addSimulateProviderHeader(spec);
            
            // Log detalhado do request para debug (apenas na primeira tentativa)
            if (attempt == 1) {
                logger.info("🔧 [TROUBLESHOOTING] Preparando requisição OTP para {}", getBaseUrl() + "/api/v1/auth/otp/request");
                
                if (request instanceof java.util.Map) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> requestMap = (java.util.Map<String, Object>) request;
                    
                    logger.info("🔧 [TROUBLESHOOTING] Request OTP - Total de campos: {}", requestMap.size());
                    logger.info("🔧 [TROUBLESHOOTING] Request OTP - Campos: {}", requestMap.keySet());
                    
                    // Validação detalhada de email
                    if (requestMap.containsKey("email")) {
                        Object emailObj = requestMap.get("email");
                        if (emailObj != null) {
                            String emailStr = emailObj.toString().trim();
                            logger.info("✅ [TROUBLESHOOTING] Email presente no request: {} (tipo: {})", 
                                emailStr, emailObj.getClass().getSimpleName());
                            
                            // Validação de formato básico
                            if (!emailStr.contains("@") || !emailStr.contains(".")) {
                                logger.warn("⚠️ [TROUBLESHOOTING] Email pode ter formato inválido: {}", emailStr);
                            }
                        } else {
                            logger.error("❌ [TROUBLESHOOTING] Email está presente mas é NULL!");
                        }
                    } else {
                        logger.error("❌ [TROUBLESHOOTING] Email NÃO encontrado no request!");
                        logger.error("❌ [TROUBLESHOOTING] Campos disponíveis: {}", requestMap.keySet());
                        logger.error("❌ [TROUBLESHOOTING] Request completo: {}", requestMap);
                    }
                    
                    // Log de outros campos importantes
                    if (requestMap.containsKey("channel")) {
                        logger.debug("🔧 [TROUBLESHOOTING] Channel: {}", requestMap.get("channel"));
                    }
                    if (requestMap.containsKey("purpose")) {
                        logger.debug("🔧 [TROUBLESHOOTING] Purpose: {}", requestMap.get("purpose"));
                    }
                    if (requestMap.containsKey("userUuid")) {
                        logger.debug("🔧 [TROUBLESHOOTING] UserUuid: {}", requestMap.get("userUuid"));
                    }
                } else {
                    logger.warn("⚠️ [TROUBLESHOOTING] Request não é um Map, tipo: {}", 
                        request != null ? request.getClass().getName() : "null");
                }
            }
            
            response = spec.body(request)
                .when()
                .post("/api/v1/auth/otp/request")
                .then()
                .extract()
                .response();
            
            int statusCode = response.getStatusCode();
            
            // Se não for rate limiting (429), retornar imediatamente
            if (statusCode != 429) {
                if (attempt > 1) {
                    logger.info("✅ [TROUBLESHOOTING] Requisição OTP bem-sucedida após {} tentativa(s)", attempt);
                }
                return response;
            }
            
            // Rate limiting (429) detectado
            if (attempt < maxRetries) {
                long delayMs = initialDelayMs * (long) Math.pow(2, attempt - 1); // Backoff exponencial padrão
                
                // Determinar se estamos em ambiente local/teste (rate limit mais permissivo)
                boolean isLocalOrTestEnvironment = isLocalOrTestEnvironment();
                
                // Em ambiente local/teste, usar backoff exponencial padrão (não usar delay da API)
                // A API retorna delay de PROD (10 minutos), mas em local temos rate limit mais alto (100 req/hora)
                if (isLocalOrTestEnvironment) {
                    logger.info("🔄 [TROUBLESHOOTING] Rate limiting (429) detectado em ambiente local/teste. " +
                        "Usando backoff exponencial padrão ({}ms) ao invés do delay da API (configurado para PROD).", delayMs);
                    logger.info("🔄 [TROUBLESHOOTING] Em ambiente local, o rate limit é mais permissivo (100 req/hora). " +
                        "O delay da API (PT10M) é para PROD (5 req/hora) e não se aplica aqui.");
                } else {
                    // Em ambiente PROD/SIT/UAT, tentar usar delay da API se disponível
                    try {
                        String responseBody = response.getBody() != null ? response.getBody().asString() : null;
                        if (responseBody != null && responseBody.contains("retryPolicy")) {
                            // Tentar extrair initialDelay ou maxDelay do retryPolicy
                            String initialDelayStr = response.jsonPath().getString("retryPolicy.initialDelay");
                            String maxDelayStr = response.jsonPath().getString("retryPolicy.maxDelay");
                            
                            // Preferir maxDelay se disponível, senão usar initialDelay
                            String delayStr = maxDelayStr != null ? maxDelayStr : initialDelayStr;
                            
                            if (delayStr != null && !delayStr.isEmpty()) {
                                try {
                                    // Parsear formato ISO 8601 (ex: PT10M = 10 minutos, PT5S = 5 segundos)
                                    java.time.Duration duration = java.time.Duration.parse(delayStr);
                                    long durationMs = duration.toMillis();
                                    
                                    // Limitar delay máximo a 5 minutos para testes (evitar esperar 10 minutos)
                                    long maxDelayForTests = 5 * 60 * 1000; // 5 minutos
                                    if (durationMs > maxDelayForTests) {
                                        logger.warn("⚠️ [TROUBLESHOOTING] Delay sugerido pela API ({}) é muito longo para testes. Limitando a {}ms (5 minutos)", 
                                            delayStr, maxDelayForTests);
                                        delayMs = maxDelayForTests;
                                    } else {
                                        delayMs = durationMs;
                                    }
                                    
                                    logger.info("🔄 [TROUBLESHOOTING] Rate limiting (429) detectado. Usando delay sugerido pela API: {} ({}ms)", 
                                        delayStr, delayMs);
                                } catch (Exception e) {
                                    logger.debug("Não foi possível parsear delay da API ({}). Usando backoff exponencial: {}ms", 
                                        delayStr, delayMs);
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.debug("Erro ao extrair delay da resposta: {}. Usando backoff exponencial: {}ms", 
                            e.getMessage(), delayMs);
                    }
                    
                    // Tentar extrair Retry-After do header se disponível (sobrescreve delay da API se menor)
                    String retryAfterHeader = response.getHeader("Retry-After");
                    if (retryAfterHeader != null && !retryAfterHeader.isEmpty()) {
                        try {
                            int retryAfterSeconds = Integer.parseInt(retryAfterHeader);
                            long retryAfterMs = retryAfterSeconds * 1000L;
                            
                            // Usar o menor entre Retry-After e delay da API (mas limitar a 5 minutos)
                            long maxDelayForTests = 5 * 60 * 1000; // 5 minutos
                            if (retryAfterMs < delayMs && retryAfterMs <= maxDelayForTests) {
                                delayMs = retryAfterMs;
                                logger.info("🔄 [TROUBLESHOOTING] Usando Retry-After header: {}s (menor que delay da API)", retryAfterSeconds);
                            }
                        } catch (NumberFormatException e) {
                            logger.debug("Retry-After header inválido: {}. Mantendo delay calculado: {}ms", 
                                retryAfterHeader, delayMs);
                        }
                    }
                    
                    // Se delay for muito longo (> 5 minutos), logar aviso e usar máximo de 5 minutos
                    long maxDelayForTests = 5 * 60 * 1000; // 5 minutos
                    if (delayMs > maxDelayForTests) {
                        logger.warn("⚠️ [TROUBLESHOOTING] Delay calculado ({}ms) é muito longo para testes. Limitando a {}ms (5 minutos)", 
                            delayMs, maxDelayForTests);
                        delayMs = maxDelayForTests;
                    }
                }
                
                logger.info("🔄 [TROUBLESHOOTING] Tentativa {}/{} falhou com rate limiting (429). Aguardando {}ms ({}s) antes de retry...", 
                    attempt, maxRetries, delayMs, delayMs / 1000);
                
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error("❌ [TROUBLESHOOTING] Delay interrompido durante retry de rate limiting");
                    return response; // Retornar resposta atual
                }
            } else {
                logger.error("❌ [TROUBLESHOOTING] Rate limiting (429) persistiu após {} tentativas. Retornando última resposta.", maxRetries);
                logger.error("❌ [TROUBLESHOOTING] O rate limit pode exigir aguardar mais tempo (ex: 10 minutos).");
                logger.error("❌ [TROUBLESHOOTING] Considere:");
                logger.error("   - Aguardar o rate limit resetar antes de executar os testes");
                logger.error("   - Executar os testes em ambiente com rate limit mais alto");
                logger.error("   - Adicionar delays maiores entre requisições OTP nos cenários");
            }
        }
        
        return response; // Retornar última resposta (429 ou outra)
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
    
    public Response refreshToken(Object request) {
        RequestSpecification spec = RestAssured.given()
            .baseUri(getBaseUrl())
            .contentType(ContentType.JSON)
            .header("request-trace-id", getRequestTraceId());
        spec = addRequiredHeaders(spec);
        return spec.body(request)
            .when()
            .post("/api/v1/auth/token/refresh")
            .then()
            .extract()
            .response();
    }
    
    public Response logout(String token) {
        RequestSpecification spec = RestAssured.given()
            .baseUri(getBaseUrl())
            .contentType(ContentType.JSON)
            .header("request-trace-id", getRequestTraceId());
        
        // Adicionar header Authorization apenas se token não for null
        if (token != null) {
            spec = spec.header("Authorization", "Bearer " + token);
        }
        // Se token for null, não adicionar header Authorization (para testar cenário de erro)
        
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
        spec = addSimulateProviderHeader(spec); // Adicionar simulate-provider para evitar envio real ao provider
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

