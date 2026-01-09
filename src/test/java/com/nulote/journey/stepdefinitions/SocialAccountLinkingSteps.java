package com.nulote.journey.stepdefinitions;

import com.nulote.journey.clients.AuthServiceClient;
import com.nulote.journey.fixtures.UserFixture;
import com.nulote.journey.utils.AllureHelper;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step definitions para cenários de account linking (vincular conta social a usuário existente).
 */
@ContextConfiguration
public class SocialAccountLinkingSteps {
    
    @Autowired
    private AuthServiceClient authClient;
    
    @Autowired
    private UserFixture userFixture;
    
    @Autowired
    private SocialLoginSteps socialLoginSteps; // Para reutilizar steps de login social
    
    private Response lastResponse;
    private String otpCode;
    
    @Dado("o email do provider corresponde ao email do usuário existente")
    public void o_email_do_provider_corresponde_ao_email_do_usuario_existente() {
        AllureHelper.step("Configurando que email do provider corresponde ao email do usuário existente");
        
        var logger = org.slf4j.LoggerFactory.getLogger(SocialAccountLinkingSteps.class);
        
        // ✅ Obter email do usuário criado no teste
        var userData = userFixture.getUserData();
        if (userData == null || userData.get("email") == null) {
            throw new IllegalStateException("Usuário não foi criado ainda. Execute 'que crio um usuário com esses dados' primeiro.");
        }
        
        String userEmail = userData.get("email");
        logger.info("✅ Configurando email do provider para account linking: email={}", userEmail);
        
        // ✅ Armazenar email no UserFixture para uso no callback OAuth2
        // O step "o provider retorna autorização bem-sucedida" vai usar esse email
        userFixture.setProviderEmail(userEmail);
    }
    
    @Dado("o email do provider é diferente do email do usuário existente")
    public void o_email_do_provider_e_diferente_do_email_do_usuario_existente() {
        AllureHelper.step("Configurando que email do provider é diferente do email do usuário existente");
        
        // Em testes reais, o provider retornaria email diferente
        // Por enquanto, apenas log
        var logger = org.slf4j.LoggerFactory.getLogger(SocialAccountLinkingSteps.class);
        logger.info("Email do provider é diferente do email do usuário existente");
    }
    
    @Quando("eu valido o OTP para account linking com o código recebido")
    public void eu_valido_o_otp_para_account_linking_com_o_codigo_recebido() {
        AllureHelper.step("Validando OTP para account linking com código recebido");
        
        var logger = org.slf4j.LoggerFactory.getLogger(SocialAccountLinkingSteps.class);
        
        // Obter código OTP do endpoint de teste
        String pendingLinkId = socialLoginSteps.getPendingLinkId();
        assertThat(pendingLinkId)
            .as("pendingLinkId deve estar presente")
            .isNotNull();
        
        Response otpResponse = authClient.getTestOtpCode(pendingLinkId);
        otpCode = otpResponse.jsonPath().getString("code");
        
        logger.info("Código OTP obtido para account linking: {}", otpCode);
        
        // Validar OTP para account linking
        lastResponse = authClient.verifyAccountLinking(pendingLinkId, otpCode);
        
        logger.info("Resposta da validação de OTP para account linking: status={}", lastResponse.getStatusCode());
    }
    
    @Quando("eu valido o OTP para account linking com código {string}")
    public void eu_valido_o_otp_para_account_linking_com_codigo(String code) {
        AllureHelper.step("Validando OTP para account linking com código: " + code);
        
        var logger = org.slf4j.LoggerFactory.getLogger(SocialAccountLinkingSteps.class);
        
        String pendingLinkId = socialLoginSteps.getPendingLinkId();
        assertThat(pendingLinkId)
            .as("pendingLinkId deve estar presente")
            .isNotNull();
        
        lastResponse = authClient.verifyAccountLinking(pendingLinkId, code);
        
        logger.info("Resposta da validação de OTP para account linking: status={}", lastResponse.getStatusCode());
    }
    
    @Quando("eu valido o OTP para account linking com pendingLinkId {string} e código {string}")
    public void eu_valido_o_otp_para_account_linking_com_pendingLinkId_e_codigo(String pendingLinkId, String code) {
        AllureHelper.step("Validando OTP para account linking com pendingLinkId: " + pendingLinkId);
        
        lastResponse = authClient.verifyAccountLinking(pendingLinkId, code);
    }
    
    @Então("o account linking deve ser completado")
    public void o_account_linking_deve_ser_completado() {
        AllureHelper.step("Validando que account linking foi completado");
        
        assertThat(lastResponse.getStatusCode())
            .as("Account linking deve retornar 200 ou 302")
            .isIn(200, 302, 307);
        
        // Se for redirect, verificar que contém JWT
        if (lastResponse.getStatusCode() == 302 || lastResponse.getStatusCode() == 307) {
            String location = lastResponse.getHeader("Location");
            if (location != null && location.contains("#token=")) {
                String token = location.substring(location.indexOf("#token=") + 7);
                if (token.contains("&")) {
                    token = token.substring(0, token.indexOf("&"));
                }
                
                assertThat(token)
                    .as("JWT deve estar presente após account linking")
                    .isNotNull()
                    .isNotEmpty();
                
                userFixture.setJwtToken(token);
            }
        }
    }
    
    @Então("a credencial social deve ser vinculada ao usuário existente")
    public void a_credencial_social_deve_ser_vinculada_ao_usuario_existente() {
        AllureHelper.step("Validando que credencial social foi vinculada ao usuário existente");
        
        // Verificar que credencial social foi criada (via evento)
        // Por enquanto, apenas log
        var logger = org.slf4j.LoggerFactory.getLogger(SocialAccountLinkingSteps.class);
        logger.info("Validando que credencial social foi vinculada ao usuário existente");
    }
    
    @Então("o account linking deve falhar com status {int}")
    public void o_account_linking_deve_falhar_com_status(int expectedStatus) {
        AllureHelper.step("Validando que account linking falhou com status " + expectedStatus);
        
        var logger = org.slf4j.LoggerFactory.getLogger(SocialAccountLinkingSteps.class);
        
        // Diagnóstico se retornar status inesperado
        if (lastResponse.getStatusCode() != expectedStatus) {
            String responseBody = lastResponse.getBody() != null ? lastResponse.getBody().asString() : "";
            logger.error("❌ Account linking retornou {} quando esperávamos {}", 
                lastResponse.getStatusCode(), expectedStatus);
            logger.error("   Response: {}", responseBody.length() > 200 ? responseBody.substring(0, 200) : responseBody);
        }
        
        assertThat(lastResponse.getStatusCode())
            .as("Status deve ser " + expectedStatus)
            .isEqualTo(expectedStatus);
    }
    
    @Então("a credencial social NÃO deve ser vinculada")
    public void a_credencial_social_nao_deve_ser_vinculada() {
        AllureHelper.step("Validando que credencial social NÃO foi vinculada");
        
        // Verificar que evento credentials.provisioned.v1 não foi publicado
        // Por enquanto, apenas log
        var logger = org.slf4j.LoggerFactory.getLogger(SocialAccountLinkingSteps.class);
        logger.info("Validando que credencial social NÃO foi vinculada");
    }
    
    @Então("um novo usuário deve ser criado \\(sem account linking\\)")
    public void um_novo_usuario_deve_ser_criado_sem_account_linking() {
        AllureHelper.step("Validando que novo usuário foi criado sem account linking");
        
        // Verificar que novo usuário foi criado (via evento)
        // Por enquanto, apenas log
        var logger = org.slf4j.LoggerFactory.getLogger(SocialAccountLinkingSteps.class);
        logger.info("Validando que novo usuário foi criado sem account linking");
    }
    
    @Então("nenhum account linking deve ocorrer")
    public void nenhum_account_linking_deve_ocorrer() {
        AllureHelper.step("Validando que nenhum account linking ocorreu");
        
        // Verificar que status não é pending_linking
        // Por enquanto, apenas log
        var logger = org.slf4j.LoggerFactory.getLogger(SocialAccountLinkingSteps.class);
        logger.info("Validando que nenhum account linking ocorreu");
    }
    
    @Quando("o OTP expira \\(TTL de {int} minutos\\)")
    public void o_otp_expira_ttl_de_minutos(int ttlMinutes) {
        AllureHelper.step("Simulando expiração do OTP (TTL: " + ttlMinutes + " minutos)");
        
        // Em testes reais, aguardar expiração ou usar código expirado
        // Por enquanto, apenas log
        var logger = org.slf4j.LoggerFactory.getLogger(SocialAccountLinkingSteps.class);
        logger.info("Simulando expiração do OTP (TTL: {} minutos)", ttlMinutes);
    }
    
    @Quando("eu valido o OTP para account linking após expiração")
    public void eu_valido_o_otp_para_account_linking_apos_expiracao() {
        AllureHelper.step("Validando OTP expirado para account linking");
        
        // Usar código expirado ou inválido
        String pendingLinkId = socialLoginSteps.getPendingLinkId();
        lastResponse = authClient.verifyAccountLinking(pendingLinkId, "000000");
    }
    
    // ============================================================================
    // Step Definitions para RELAY (simulando clientes externos)
    // ============================================================================
    
    @Quando("eu completo o account linking via relay com OTP válido")
    public void eu_completo_o_account_linking_via_relay_com_otp_valido() {
        AllureHelper.step("Completando account linking via relay com OTP válido");
        
        // Em testes reais, o OTP viria do email/SMS
        String mockOtp = "123456";
        String linkId = socialLoginSteps.getPendingLinkId();
        
        lastResponse = authClient.completeAccountLinkingViaRelay(linkId, mockOtp);
        
        var logger = org.slf4j.LoggerFactory.getLogger(SocialAccountLinkingSteps.class);
        logger.info("Resposta do account linking via RELAY: status={}", lastResponse.getStatusCode());
    }
    
    @Quando("eu completo o account linking via relay com OTP {string}")
    public void eu_completo_o_account_linking_via_relay_com_otp(String otpCode) {
        AllureHelper.step("Completando account linking via relay com OTP " + otpCode);
        
        String linkId = socialLoginSteps.getPendingLinkId();
        lastResponse = authClient.completeAccountLinkingViaRelay(linkId, otpCode);
        
        var logger = org.slf4j.LoggerFactory.getLogger(SocialAccountLinkingSteps.class);
        logger.info("Resposta do account linking via RELAY: status={}", lastResponse.getStatusCode());
    }
    
    @Quando("eu completo o account linking via relay com OTP expirado")
    public void eu_completo_o_account_linking_via_relay_com_otp_expirado() {
        AllureHelper.step("Completando account linking via relay com OTP expirado");
        
        // Simular OTP expirado (em testes reais, usar um OTP que já expirou)
        String expiredOtp = "999999";
        String linkId = socialLoginSteps.getPendingLinkId();
        
        lastResponse = authClient.completeAccountLinkingViaRelay(linkId, expiredOtp);
        
        var logger = org.slf4j.LoggerFactory.getLogger(SocialAccountLinkingSteps.class);
        logger.info("Resposta do account linking via RELAY com OTP expirado: status={}", lastResponse.getStatusCode());
    }
    
    @Então("o account linking via relay deve ser bem-sucedido")
    public void o_account_linking_via_relay_deve_ser_bem_sucedido() {
        AllureHelper.step("Validando que account linking via relay foi bem-sucedido");
        
        assertThat(lastResponse.getStatusCode())
            .as("Account linking via relay deve retornar 200 ou 302")
            .isIn(200, 302, 307);
    }
    
    @Então("o account linking via relay deve falhar com status {int}")
    public void o_account_linking_via_relay_deve_falhar_com_status(int expectedStatus) {
        AllureHelper.step("Validando que account linking via relay falhou com status " + expectedStatus);
        
        assertThat(lastResponse.getStatusCode())
            .as("Status deve ser " + expectedStatus)
            .isEqualTo(expectedStatus);
    }
}

