package com.nulote.journey.utils;

import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper para integração com Allure Report.
 * Facilita a adição de screenshots, logs e respostas HTTP aos relatórios Allure.
 */
public class AllureHelper {
    
    private static final Logger log = LoggerFactory.getLogger(AllureHelper.class);
    
    /**
     * Adiciona screenshot ao relatório Allure
     * 
     * @param screenshot Array de bytes da imagem PNG
     * @return Array de bytes (para compatibilidade com @Attachment)
     */
    @Attachment(value = "Screenshot", type = "image/png")
    public static byte[] attachScreenshot(byte[] screenshot) {
        return screenshot;
    }
    
    /**
     * Adiciona texto ao relatório Allure
     * 
     * @param text Texto a ser anexado
     * @return Texto anexado (para compatibilidade com @Attachment)
     */
    @Attachment(value = "Log", type = "text/plain")
    public static String attachText(String text) {
        return text;
    }
    
    /**
     * Adiciona JSON ao relatório Allure
     * 
     * @param json String JSON a ser anexada
     * @return JSON anexado (para compatibilidade com @Attachment)
     */
    @Attachment(value = "Response JSON", type = "application/json")
    public static String attachJson(String json) {
        return json;
    }
    
    /**
     * Adiciona resposta HTTP completa ao relatório Allure
     * Inclui URL, status code, headers e body
     * 
     * @param response Resposta HTTP do RestAssured
     * @param stepName Nome do step para contexto
     */
    public static void attachHttpResponse(Response response, String stepName) {
        try {
            if (response == null) {
                log.warn("Resposta HTTP nula para step: {}", stepName);
                return;
            }
            
            // Adicionar status code
            Allure.addAttachment("Status Code", 
                String.valueOf(response.getStatusCode()));
            
            // Adicionar URL da requisição (se disponível)
            try {
                String requestUrl = response.getDetailedCookies().toString();
                Allure.addAttachment("Request Info", requestUrl);
            } catch (Exception e) {
                log.debug("Não foi possível extrair URL da requisição: {}", e.getMessage());
            }
            
            // Adicionar headers da resposta
            try {
                Allure.addAttachment("Response Headers", 
                    response.getHeaders().toString());
            } catch (Exception e) {
                log.debug("Erro ao anexar headers: {}", e.getMessage());
            }
            
            // Adicionar body JSON
            try {
                String body = response.getBody().asString();
                if (body != null && !body.isEmpty()) {
                    attachJson(body);
                }
            } catch (Exception e) {
                log.debug("Erro ao anexar body JSON: {}", e.getMessage());
            }
            
            log.debug("Resposta HTTP anexada ao Allure para step: {}", stepName);
            
        } catch (Exception e) {
            log.warn("Erro ao anexar resposta HTTP ao Allure: {}", e.getMessage());
        }
    }
    
    /**
     * Adiciona log estruturado ao Allure
     * 
     * @param message Mensagem do log
     * @param level Nível do log (INFO, DEBUG, ERROR, etc.)
     */
    public static void attachLog(String message, String level) {
        String logEntry = String.format("[%s] %s", level, message);
        attachText(logEntry);
        Allure.step(logEntry);
    }
    
    /**
     * Marca step no Allure com descrição
     * Útil para marcar etapas importantes nos testes
     * 
     * @param stepDescription Descrição do step
     */
    @Step("{stepDescription}")
    public static void step(String stepDescription) {
        log.info("Allure Step: {}", stepDescription);
    }
    
    /**
     * Adiciona descrição detalhada a um step do Allure
     * 
     * @param description Descrição detalhada
     */
    public static void addDescription(String description) {
        // Usar addAttachment para adicionar descrição como texto
        Allure.addAttachment("Description", description);
    }
    
    /**
     * Adiciona link externo ao relatório Allure usando attachment
     * 
     * @param name Nome do link
     * @param url URL do link
     */
    public static void addLink(String name, String url) {
        // Usar addAttachment para adicionar link como texto
        Allure.addAttachment(name, url);
    }
}

