package com.nulote.journey.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Helper para interação com RabbitMQ em testes E2E.
 * Permite consumir mensagens de filas para verificar eventos assíncronos.
 */
@Component
public class RabbitMQHelper {
    
    @Value("${rabbitmq.host:localhost}")
    private String host;
    
    @Value("${rabbitmq.port:5672}")
    private int port;
    
    @Value("${rabbitmq.username:guest}")
    private String username;
    
    @Value("${rabbitmq.password:guest}")
    private String password;
    
    private Connection connection;
    private Channel channel;
    private ObjectMapper objectMapper;
    private Map<String, Event> lastConsumedMessages = new HashMap<>();
    
    @PostConstruct
    public void init() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        
        connection = factory.newConnection();
        channel = connection.createChannel();
        objectMapper = new ObjectMapper();
    }
    
    @PreDestroy
    public void close() throws IOException, TimeoutException {
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
        if (connection != null && connection.isOpen()) {
            connection.close();
        }
    }
    
    /**
     * Determina o nome da fila baseado no tipo de evento seguindo o padrão do projeto
     * Padrão: {service}.{event-type}.queue
     * 
     * @param eventType Tipo de evento (ex: "otp.sent", "otp.validated")
     * @return Nome da fila correspondente
     */
    private String determineQueueName(String eventType) {
        // Mapear eventType para nome de fila seguindo padrão do projeto
        switch (eventType) {
            case "otp.sent":
                return "auth.otp-sent.queue";
            case "otp.validated":
                return "auth.otp-validated.queue";
            case "credentials.provisioned.v1":
                return "identity.credentials-provisioned.queue";
            case "user.created.v1":
                return "auth.user-created.queue";
            default:
                // Fallback: tentar inferir do padrão
                String normalized = eventType.replace(".", "-");
                return "auth." + normalized + ".queue";
        }
    }
    
    /**
     * Consome uma mensagem de uma fila específica baseada no tipo de evento
     * Usa o padrão de nomenclatura do projeto: {service}.{event-type}.queue
     * 
     * @param eventType Tipo de evento esperado (ex: "otp.sent", "otp.validated")
     * @return Evento consumido ou null se não houver mensagem do tipo esperado
     */
    public Event consumeMessage(String eventType) {
        return consumeMessage(eventType, null);
    }
    
    /**
     * Consome uma mensagem de uma fila específica baseada no tipo de evento
     * 
     * @param eventType Tipo de evento esperado (ex: "otp.sent", "otp.validated")
     * @param queueName Nome da fila específica (opcional, se null será determinado automaticamente)
     * @return Evento consumido ou null se não houver mensagem do tipo esperado
     */
    public Event consumeMessage(String eventType, String queueName) {
        var logger = org.slf4j.LoggerFactory.getLogger(RabbitMQHelper.class);
        try {
            // Verificar se canal está aberto
            if (channel == null || !channel.isOpen()) {
                logger.warn("Canal RabbitMQ não está aberto. Tentando reconectar...");
                init();
            }
            
            // Determinar nome da fila seguindo padrão do projeto
            String finalQueueName = queueName != null ? queueName : determineQueueName(eventType);
            logger.debug("Consumindo evento {} da fila {}", eventType, finalQueueName);
            
            // Declarar fila caso não exista (modo passivo)
            // As filas devem ser criadas pelo RabbitConfig do microserviço
            try {
                channel.queueDeclarePassive(finalQueueName);
            } catch (IOException e) {
                logger.warn("Fila {} não existe. Tentando criar em modo não durável para testes.", finalQueueName);
                // Em ambiente de teste, criar fila não durável se não existir
                channel.queueDeclare(finalQueueName, false, false, false, null);
            }
            
            GetResponse response = channel.basicGet(finalQueueName, false);
            
            if (response == null) {
                logger.debug("Nenhuma mensagem encontrada na fila {}", finalQueueName);
                return null;
            }
            
            String messageBody = new String(response.getBody(), StandardCharsets.UTF_8);
            logger.debug("Mensagem recebida do RabbitMQ ({} bytes): {}", messageBody.length(), messageBody.substring(0, Math.min(200, messageBody.length())));
            
            // Capturar headers da mensagem
            Map<String, Object> headers = response.getProps().getHeaders();
            if (headers != null) {
                logger.debug("Headers da mensagem RabbitMQ: {}", headers.keySet());
            }
            
            // Tentar parsear como Event primeiro
            Event event;
            try {
                event = objectMapper.readValue(messageBody, Event.class);
            } catch (Exception e) {
                // Se não for Event, tentar parsear como Map direto
                @SuppressWarnings("unchecked")
                Map<String, Object> payload = objectMapper.readValue(messageBody, Map.class);
                event = new Event();
                event.setType(eventType); // Usar eventType como tipo
                event.setPayload(payload);
                logger.debug("Evento parseado como Map direto. Payload keys: {}", payload.keySet());
            }
            
            // Armazenar headers no evento
            if (headers != null) {
                event.setHeaders(headers);
            }
            
            // Aceitar mensagem apenas se for do tipo esperado ou se payload contém informações do evento
            boolean isExpectedEvent = event.getType() != null && event.getType().equals(eventType);
            if (!isExpectedEvent && event.getPayload() != null) {
                // Verificar se o payload contém informações que indicam o tipo de evento
                // Para eventos OTP, o payload pode conter campos específicos
                if (eventType.equals("otp.sent") && event.getPayload().containsKey("otpCode")) {
                    isExpectedEvent = true;
                    event.setType(eventType);
                    logger.debug("Evento identificado como otp.sent baseado no campo otpCode no payload");
                } else if (eventType.equals("otp.validated") && event.getPayload().containsKey("otpId")) {
                    isExpectedEvent = true;
                    event.setType(eventType);
                    logger.debug("Evento identificado como otp.validated baseado no campo otpId no payload");
                }
            }
            
            if (isExpectedEvent) {
                channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
                lastConsumedMessages.put(eventType, event);
                
                logger.info("✅ Evento {} consumido com sucesso da fila {}", eventType, finalQueueName);
                return event;
            } else {
                // Rejeitar e reenfileirar se não for o tipo esperado
                logger.warn("Evento recebido não é do tipo esperado. Tipo esperado: {}, Tipo recebido: {}, Payload keys: {}", 
                    eventType, event.getType(), event.getPayload() != null ? event.getPayload().keySet() : "null");
                channel.basicNack(response.getEnvelope().getDeliveryTag(), false, true);
                return null;
            }
        } catch (IOException | TimeoutException e) {
            logger.error("Erro ao consumir mensagem do RabbitMQ: {}", e.getMessage(), e);
            // Em ambiente de teste, não falhar o teste se RabbitMQ não estiver disponível
            // Apenas logar o erro
            return null;
        }
    }
    
    /**
     * Retorna a última mensagem consumida de um tipo específico
     * 
     * @param eventType Tipo de evento
     * @return Última mensagem consumida ou null
     */
    public Event getLastConsumedMessage(String eventType) {
        return lastConsumedMessages.get(eventType);
    }
    
    /**
     * Classe interna para representar eventos RabbitMQ
     */
    public static class Event {
        private String type;
        private Map<String, Object> payload;
        private String timestamp;
        private Map<String, Object> headers;
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public Map<String, Object> getPayload() {
            return payload;
        }
        
        public void setPayload(Map<String, Object> payload) {
            this.payload = payload;
        }
        
        public String getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
        
        public Map<String, Object> getHeaders() {
            return headers;
        }
        
        public void setHeaders(Map<String, Object> headers) {
            this.headers = headers;
        }
    }
}

