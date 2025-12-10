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
        var logger = org.slf4j.LoggerFactory.getLogger(RabbitMQHelper.class);
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);
            factory.setPort(port);
            factory.setUsername(username);
            factory.setPassword(password);
            
            // Configurar timeout de conexão para evitar travamentos
            factory.setConnectionTimeout(5000); // 5 segundos
            factory.setNetworkRecoveryInterval(5000); // 5 segundos
            
            connection = factory.newConnection();
            channel = connection.createChannel();
            objectMapper = new ObjectMapper();
            logger.debug("Conexão RabbitMQ estabelecida com sucesso");
        } catch (Exception e) {
            logger.warn("Erro ao inicializar conexão RabbitMQ: {}", e.getMessage());
            throw e;
        }
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
        // O evento otp.sent é publicado no exchange auth.events e consumido por auth.otp-sent.queue
        switch (eventType) {
            case "otp.sent":
                // Tentar primeiro a fila do auth-service, depois a transactional
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
                logger.debug("Canal RabbitMQ não está aberto. Tentando reconectar...");
                try {
                    init();
                } catch (Exception e) {
                    logger.warn("Erro ao reconectar ao RabbitMQ: {}", e.getMessage());
                    return null;
                }
            }
            
            // Determinar nome da fila seguindo padrão do projeto
            String finalQueueName = queueName != null ? queueName : determineQueueName(eventType);
            logger.debug("Consumindo evento {} da fila {}", eventType, finalQueueName);
            
            // Para otp.sent, tentar ambas as filas possíveis
            // IMPORTANTE: Como há consumidores ativos nas filas principais, as mensagens são consumidas rapidamente
            // Vamos tentar consumir de ambas as filas, mas pode ser que a mensagem já tenha sido consumida
            if ("otp.sent".equals(eventType) && queueName == null) {
                // Tentar primeiro auth.otp-sent.queue
                Event event = tryConsumeFromQueue(eventType, "auth.otp-sent.queue", logger);
                if (event != null) {
                    return event;
                }
                // Se não encontrou, tentar transactional.auth-otp-sent.queue
                logger.debug("Nenhuma mensagem encontrada em auth.otp-sent.queue, tentando transactional.auth-otp-sent.queue");
                event = tryConsumeFromQueue(eventType, "transactional.auth-otp-sent.queue", logger);
                if (event != null) {
                    return event;
                }
                // Se ainda não encontrou, pode ser que a mensagem já foi consumida pelos consumidores ativos
                // Nesse caso, vamos verificar se podemos obter do banco de dados ou logs
                // Usar DEBUG ao invés de WARN pois isso é comportamento esperado durante polling
                logger.debug("Nenhuma mensagem encontrada nas filas. A mensagem pode ter sido consumida pelos consumidores ativos.");
                return null;
            }
            
            return tryConsumeFromQueue(eventType, finalQueueName, logger);
        } catch (Exception e) {
            logger.error("Erro ao consumir mensagem do RabbitMQ: {}", e.getMessage(), e);
            // Em ambiente de teste, não falhar o teste se RabbitMQ não estiver disponível
            // Apenas logar o erro
            return null;
        }
    }
    
    /**
     * Tenta consumir uma mensagem de uma fila específica
     */
    private Event tryConsumeFromQueue(String eventType, String queueName, org.slf4j.Logger logger) throws IOException {
        // Declarar fila caso não exista (modo passivo)
        // As filas devem ser criadas pelo RabbitConfig do microserviço
        try {
            channel.queueDeclarePassive(queueName);
        } catch (IOException e) {
            // Usar trace ao invés de debug para reduzir verbosidade
            logger.trace("Fila {} não existe ou não está acessível: {}", queueName, e.getMessage());
            return null;
        }
        
        GetResponse response = channel.basicGet(queueName, false);
            
            if (response == null) {
                // Usar trace ao invés de debug para reduzir verbosidade durante polling
                logger.trace("Nenhuma mensagem encontrada na fila {}", queueName);
                return null;
            }
            
            String messageBody = new String(response.getBody(), StandardCharsets.UTF_8);
            logger.debug("Mensagem recebida do RabbitMQ da fila {} ({} bytes): {}", queueName, messageBody.length(), messageBody.substring(0, Math.min(200, messageBody.length())));
            
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
                
                logger.info("✅ Evento {} consumido com sucesso da fila {}", eventType, queueName);
                return event;
            } else {
                // Rejeitar e reenfileirar se não for o tipo esperado
                logger.warn("Evento recebido não é do tipo esperado. Tipo esperado: {}, Tipo recebido: {}, Payload keys: {}", 
                    eventType, event.getType(), event.getPayload() != null ? event.getPayload().keySet() : "null");
                channel.basicNack(response.getEnvelope().getDeliveryTag(), false, true);
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

