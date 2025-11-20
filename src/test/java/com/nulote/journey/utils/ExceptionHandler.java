package com.nulote.journey.utils;

import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * Helper para tratamento padronizado de exceções em operações HTTP.
 */
public class ExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(ExceptionHandler.class);
    
    /**
     * Executa uma operação HTTP com tratamento de exceções padronizado
     * 
     * @param operation Operação HTTP a ser executada
     * @param operationName Nome da operação para logging
     * @return Response da operação
     * @throws RuntimeException Se a operação falhar
     */
    public static Response executeHttpOperation(
            Supplier<Response> operation, 
            String operationName) {
        
        try {
            log.debug("Executando operação HTTP: {}", operationName);
            Response response = operation.get();
            log.debug("Operação concluída: {} - Status: {}", 
                operationName, response.getStatusCode());
            return response;
            
        } catch (Exception e) {
            // Tratamento genérico de exceções
            String exceptionType = e.getClass().getSimpleName();
            String message = e.getMessage();
            
            // Identificar tipo de erro baseado na exceção
            if (e instanceof java.net.ConnectException || 
                e instanceof java.net.SocketTimeoutException ||
                e instanceof java.net.UnknownHostException ||
                message != null && (message.contains("Connection") || message.contains("timeout"))) {
                // Erro de conexão/rede
                log.error("Erro de conexão em {}: {}", operationName, message, e);
                throw new RuntimeException(
                    String.format("Falha de conexão em %s: %s", operationName, message), e);
            } else if (message != null && message.contains("JSON")) {
                // Erro de processamento JSON
                log.error("Erro de processamento JSON em {}: {}", operationName, message, e);
                throw new RuntimeException(
                    String.format("Erro ao processar JSON em %s: %s", operationName, message), e);
            } else {
                // Outros erros não esperados
                log.error("Erro inesperado em {} ({}): {}", operationName, exceptionType, message, e);
                throw new RuntimeException(
                    String.format("Erro inesperado em %s: %s", operationName, message), e);
            }
        }
    }
    
    /**
     * Valida resposta HTTP com mensagens de erro detalhadas
     * 
     * @param response Resposta HTTP a ser validada
     * @param expectedStatus Status code esperado
     * @param operationName Nome da operação para logging
     */
    public static void validateResponse(
            Response response, 
            int expectedStatus, 
            String operationName) {
        
        if (response == null) {
            throw new AssertionError(
                String.format("Resposta não deve ser nula para operação: %s", operationName));
        }
        
        int actualStatus = response.getStatusCode();
        if (actualStatus != expectedStatus) {
            String errorBody = response.getBody().asString();
            log.error("Status code inesperado em {}. Esperado: {}, Recebido: {}, Body: {}", 
                operationName, expectedStatus, actualStatus, errorBody);
            
            throw new AssertionError(
                String.format("Status code inesperado em %s. Esperado: %d, Recebido: %d. Body: %s",
                    operationName, expectedStatus, actualStatus, errorBody));
        }
        
        log.debug("Validação bem-sucedida em {}: Status: {}", operationName, actualStatus);
    }
    
    /**
     * Extrai mensagem de erro da resposta HTTP
     * 
     * @param response Resposta HTTP
     * @return Mensagem de erro ou null se não houver
     */
    public static String extractErrorMessage(Response response) {
        if (response == null) {
            return "Resposta nula";
        }
        
        try {
            // Tentar extrair mensagem de erro do JSON
            String errorMessage = response.jsonPath().getString("error.message");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                return errorMessage;
            }
            
            // Tentar extrair mensagem genérica
            String message = response.jsonPath().getString("message");
            if (message != null && !message.isEmpty()) {
                return message;
            }
            
            // Retornar body completo se não conseguir extrair mensagem específica
            return response.getBody().asString();
            
        } catch (Exception e) {
            log.warn("Erro ao extrair mensagem de erro da resposta: {}", e.getMessage());
            return response.getBody().asString();
        }
    }
}

