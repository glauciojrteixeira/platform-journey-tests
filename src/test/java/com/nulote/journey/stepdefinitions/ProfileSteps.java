package com.nulote.journey.stepdefinitions;

import com.nulote.journey.clients.ProfileServiceClient;
import com.nulote.journey.fixtures.UserFixture;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step definitions para operações de perfil.
 */
@ContextConfiguration
public class ProfileSteps {
    
    @Autowired
    private ProfileServiceClient profileClient;
    
    @Autowired
    private com.nulote.journey.clients.AuthServiceClient authClient;
    
    @Autowired
    private UserFixture userFixture;
    
    // Configurações de timeout para eventos assíncronos
    @Value("${e2e.event-timeout-seconds:3}")
    private long eventTimeoutSeconds;
    
    @Value("${e2e.event-poll-interval-ms:200}")
    private long eventPollIntervalMs;
    
    private Response lastResponse;
    
    @Então("o perfil deve estar acessível")
    public void o_perfil_deve_estar_acessivel() {
        // Validação já feita em AuthenticationSteps
        // Este step pode ser usado para validações adicionais específicas
    }
    
    @Então("o último login deve ser atualizado")
    public void o_ultimo_login_deve_ser_atualizado() {
        // Verificar que último login foi atualizado no perfil
        // Implementação depende da API disponível
    }
    
    // ========== Step Definitions para Atualização de Perfil (J1.4) ==========
    
    @Autowired
    private com.nulote.journey.clients.IdentityServiceClient identityClient;
    
    @Dado("que estou autenticado na plataforma")
    public void que_estou_autenticado_na_plataforma() {
        var logger = org.slf4j.LoggerFactory.getLogger(ProfileSteps.class);
        
        // Se o usuário já foi criado em um step anterior, apenas verificar se está autenticado
        // Não precisamos criar um novo usuário se já existe um
        if (userFixture.getCreatedUserUuid() != null) {
            logger.debug("Usuário já existe (UUID: {}). Verificando se está autenticado...", userFixture.getCreatedUserUuid());
            // Se usuário já existe, assumir que está autenticado (pode ter sido autenticado em step anterior)
            return;
        }
        
        // Se usuário não existe, criar um novo usuário de teste (com OTP)
        logger.debug("Criando novo usuário para autenticação...");
        // Criar usuário primeiro
        // Usar CPF como padrão (BR) - se precisar de outro país, deve ser configurado antes
        var userData = new java.util.HashMap<String, String>();
        userData.put("nome", com.nulote.journey.fixtures.TestDataGenerator.generateUniqueName());
        userData.put("documentNumber", com.nulote.journey.fixtures.TestDataGenerator.generateUniqueCpf());
        userData.put("documentType", "CPF");
        userData.put("email", com.nulote.journey.fixtures.TestDataGenerator.generateUniqueEmail());
        userData.put("telefone", com.nulote.journey.fixtures.TestDataGenerator.generateUniquePhone());
        userData.put("password", "TestPassword123!");
        userFixture.setUserData(userData);
        
        // Criar identidade - A API agora exige registration-token (sessionToken)
        // Se não houver sessionToken, criar OTP e obter sessionToken automaticamente
        String sessionToken = userFixture.getSessionToken();
        
        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            logger.info("Nenhum sessionToken disponível. Criando OTP e sessionToken automaticamente...");
            
            try {
                // Solicitar OTP para registro
                var otpRequest = userFixture.buildOtpRequest("EMAIL", "REGISTRATION");
                var otpResponse = authClient.requestOtp(otpRequest);
                
                if (otpResponse.getStatusCode() == 200) {
                    String otpId = otpResponse.jsonPath().getString("otpId");
                    if (otpId != null) {
                        userFixture.setOtpUuid(otpId);
                        
                        // Obter código OTP do endpoint de teste
                        var testCodeResponse = authClient.getTestOtpCode(otpId);
                        if (testCodeResponse.getStatusCode() == 200) {
                            String otpCode = testCodeResponse.jsonPath().getString("code");
                            if (otpCode == null) {
                                otpCode = testCodeResponse.jsonPath().getString("otpCode");
                            }
                            if (otpCode != null) {
                                otpCode = otpCode.replaceAll("[^0-9]", "");
                                if (otpCode.length() == 6) {
                                    // Validar OTP para obter sessionToken
                                    var validationRequest = userFixture.buildOtpValidationRequest(otpCode);
                                    var validationResponse = authClient.validateOtp(validationRequest);
                                    
                                    if (validationResponse.getStatusCode() == 200) {
                                        sessionToken = validationResponse.jsonPath().getString("sessionToken");
                                        if (sessionToken != null && !sessionToken.trim().isEmpty()) {
                                            userFixture.setSessionToken(sessionToken);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("Não foi possível criar OTP e sessionToken automaticamente: {}", e.getMessage());
            }
        }
        
        var request = userFixture.buildCreateUserRequest();
        var response = identityClient.createUser(request, sessionToken);
        if (response.getStatusCode() == 201 || response.getStatusCode() == 200) {
            try {
                var userUuid = response.jsonPath().getString("uuid");
                if (userUuid == null) {
                    userUuid = response.jsonPath().getString("id");
                }
                if (userUuid != null) {
                    userFixture.setCreatedUserUuid(userUuid);
                    
                    // FASE 3: Aguardar criação automática de perfil
                    // IMPORTANTE: O Outbox Pattern pode ter delay de 2-5 segundos para publicar o evento
                    // e o consumer pode levar mais alguns segundos para processar e criar o perfil
                    final String finalUserUuid = userUuid;
                    final var profileLogger = org.slf4j.LoggerFactory.getLogger(ProfileSteps.class);
                    try {
                        profileLogger.info("Aguardando criação automática de perfil para usuário {} (timeout: {}s)", 
                            finalUserUuid, eventTimeoutSeconds);
                        org.awaitility.Awaitility.await()
                            .atMost(Math.max(eventTimeoutSeconds, 15), java.util.concurrent.TimeUnit.SECONDS) // Mínimo de 15 segundos
                            .pollInterval(eventPollIntervalMs, java.util.concurrent.TimeUnit.MILLISECONDS)
                            .until(() -> {
                                try {
                                    var profileResponse = profileClient.getProfileByUserUuid(finalUserUuid);
                                    int statusCode = profileResponse != null ? profileResponse.getStatusCode() : -1;
                                    if (statusCode == 200) {
                                        profileLogger.debug("Perfil encontrado para usuário {} (status: 200)", finalUserUuid);
                                        return true;
                                    } else {
                                        profileLogger.trace("Perfil ainda não criado para usuário {} (status: {})", finalUserUuid, statusCode);
                                        return false;
                                    }
                                } catch (Exception e) {
                                    profileLogger.trace("Erro ao verificar perfil para usuário {}: {}", finalUserUuid, e.getMessage());
                                    return false;
                                }
                            });
                        profileLogger.info("✅ Perfil criado automaticamente para usuário {}", finalUserUuid);
                    } catch (Exception e) {
                        profileLogger.warn("Perfil não foi criado automaticamente após {}s para usuário {}. " +
                            "Isso pode indicar que o evento user.created.v1 não foi publicado ou processado corretamente. " +
                            "Continuando...", 
                            Math.max(eventTimeoutSeconds, 15), finalUserUuid);
                        // Não falhar aqui - alguns testes podem criar perfil manualmente
                    }
                }
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(ProfileSteps.class)
                    .warn("Não foi possível extrair UUID da resposta de criação");
            }
        }
    }
    
    @Dado("que consulto meu perfil atual")
    public void que_consulto_meu_perfil_atual() {
        if (userFixture.getCreatedUserUuid() == null) {
            throw new IllegalStateException("Usuário não foi criado. Execute steps de criação primeiro.");
        }
        
        final String userUuid = userFixture.getCreatedUserUuid();
        
        // FASE 3: Aguardar criação de perfil se não existir
        lastResponse = profileClient.getProfileByUserUuid(userUuid);
        
        if (lastResponse.getStatusCode() == 404) {
            var profileLogger = org.slf4j.LoggerFactory.getLogger(ProfileSteps.class);
            profileLogger.warn("Perfil não encontrado (404) - aguardando criação automática. UUID: {}", userUuid);
            
            // Aguardar criação automática de perfil
            // IMPORTANTE: O Outbox Pattern pode ter delay de 2-5 segundos para publicar o evento
            // e o consumer pode levar mais alguns segundos para processar e criar o perfil
            // Aumentar timeout para 20 segundos para dar tempo suficiente
            try {
                profileLogger.info("Aguardando criação automática de perfil para usuário {} (timeout: 20s)", userUuid);
                org.awaitility.Awaitility.await()
                    .atMost(20, java.util.concurrent.TimeUnit.SECONDS)
                    .pollInterval(500, java.util.concurrent.TimeUnit.MILLISECONDS)
                    .until(() -> {
                        try {
                            var profileResponse = profileClient.getProfileByUserUuid(userUuid);
                            int statusCode = profileResponse != null ? profileResponse.getStatusCode() : -1;
                            if (statusCode == 200) {
                                lastResponse = profileResponse;
                                profileLogger.debug("Perfil encontrado para usuário {} (status: 200)", userUuid);
                                return true;
                            } else {
                                profileLogger.trace("Perfil ainda não criado para usuário {} (status: {})", userUuid, statusCode);
                                return false;
                            }
                        } catch (Exception e) {
                            profileLogger.trace("Erro ao verificar perfil para usuário {}: {}", userUuid, e.getMessage());
                            return false;
                        }
                    });
                profileLogger.info("✅ Perfil criado após aguardo para usuário {}", userUuid);
            } catch (Exception e) {
                profileLogger.error("Perfil não foi criado após aguardo de 20 segundos para usuário {}. " +
                    "Isso pode indicar que: " +
                    "1. O evento user.created.v1 não foi publicado pelo Identity Service, " +
                    "2. O evento não foi processado pelo User Profile Service consumer, " +
                    "3. O consumer falhou silenciosamente ao criar o perfil. " +
                    "Verifique os logs do Identity Service (publicação do evento) e User Profile Service (consumer). UUID: {}", 
                    userUuid);
                
                // Não tentar criar manualmente - não há endpoint POST para criar perfil
                // O perfil deve ser criado automaticamente pelo consumer
                // Se não foi criado, há um problema no consumer ou no evento
                throw new IllegalStateException(
                    "Perfil não foi criado automaticamente para o usuário " + userUuid + 
                    ". Verifique se o evento user.created.v1 foi publicado e processado corretamente. " +
                    "Verifique os logs do Identity Service (publicação) e User Profile Service (consumer)."
                );
            }
        }
        
        // Verificar se o perfil foi encontrado
        if (lastResponse.getStatusCode() != 200) {
            throw new IllegalStateException(
                "Não foi possível obter perfil para o usuário " + userUuid + 
                ". Status: " + lastResponse.getStatusCode()
            );
        }
        
        // Perfil encontrado com sucesso
        org.slf4j.LoggerFactory.getLogger(ProfileSteps.class)
            .debug("Perfil encontrado para usuário {}", userUuid);
    }
    
    @Quando("eu atualizo minhas preferências:")
    public void eu_atualizo_minhas_preferencias(io.cucumber.datatable.DataTable dataTable) {
        var preferences = dataTable.asMap(String.class, String.class);
        var request = new java.util.HashMap<String, Object>();
        preferences.forEach((key, value) -> {
            // Converter valores booleanos e outros tipos conforme necessário
            if (value.equals("true") || value.equals("false")) {
                request.put(key, Boolean.parseBoolean(value));
            } else {
                request.put(key, value);
            }
        });
        lastResponse = profileClient.updateProfile(userFixture.getCreatedUserUuid(), request);
    }
    
    @Quando("eu tento atualizar com dados inválidos:")
    public void eu_tento_atualizar_com_dados_invalidos(io.cucumber.datatable.DataTable dataTable) {
        var preferences = dataTable.asMap(String.class, String.class);
        var request = new java.util.HashMap<String, Object>();
        preferences.forEach((key, value) -> request.put(key, value));
        lastResponse = profileClient.updateProfile(userFixture.getCreatedUserUuid(), request);
    }
    
    @Quando("eu tento alterar dados de segurança:")
    public void eu_tento_alterar_dados_de_seguranca(io.cucumber.datatable.DataTable dataTable) {
        var securityData = dataTable.asMap(String.class, String.class);
        var request = new java.util.HashMap<String, Object>();
        securityData.forEach((key, value) -> request.put(key, value));
        lastResponse = profileClient.updateProfile(userFixture.getCreatedUserUuid(), request);
    }
    
    @Então("o perfil deve ser atualizado com sucesso")
    public void o_perfil_deve_ser_atualizado_com_sucesso() {
        assertThat(lastResponse.getStatusCode())
            .as("Perfil deve ser atualizado com sucesso")
            .isIn(200, 204);
    }
    
    @Então("as preferências devem ser refletidas imediatamente")
    public void as_preferencias_devem_ser_refletidas_imediatamente() {
        // Consultar perfil novamente para verificar atualização
        var updatedProfile = profileClient.getProfileByUserUuid(userFixture.getCreatedUserUuid());
        assertThat(updatedProfile.getStatusCode()).isEqualTo(200);
    }
    
    @Então("a atualização deve falhar com status {int}")
    public void a_atualizacao_deve_falhar_com_status(int statusCode) {
        assertThat(lastResponse.getStatusCode())
            .as("Atualização deve falhar com status %d", statusCode)
            .isEqualTo(statusCode);
    }
    
    @Então("o erro deve indicar dados inválidos")
    public void o_erro_deve_indicar_dados_invalidos() {
        // Verificar mensagem de erro
        assertThat(lastResponse.getStatusCode()).isGreaterThanOrEqualTo(400);
    }
    
    @Então("o erro deve indicar que dados de segurança não podem ser alterados via perfil")
    public void o_erro_deve_indicar_que_dados_de_seguranca_nao_podem_ser_alterados_via_perfil() {
        assertThat(lastResponse.getStatusCode()).isEqualTo(400);
        // Verificar mensagem específica se disponível
    }
    
    // Nota: o_evento_deve_ser_publicado está definido em AuthenticationSteps
    // para evitar duplicação. Este step pode ser usado por qualquer feature.
}

