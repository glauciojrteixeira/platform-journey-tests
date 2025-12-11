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
            logger.info("🔧 [TROUBLESHOOTING] Iniciando conexão com RabbitMQ em {}:{}", host, port);
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
            logger.info("✅ [TROUBLESHOOTING] Conexão RabbitMQ estabelecida com sucesso em {}:{}", host, port);
        } catch (Exception e) {
            logger.error("❌ [TROUBLESHOOTING] Erro ao inicializar conexão RabbitMQ em {}:{} - {}", 
                host, port, e.getMessage());
            logger.error("❌ [TROUBLESHOOTING] Verifique se:");
            logger.error("   1. RabbitMQ está rodando (docker ps | grep rabbitmq)");
            logger.error("   2. Host e porta estão corretos ({}:{})", host, port);
            logger.error("   3. Credenciais estão corretas (usuário: {})", username);
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
        // Padrão: {service}.{event-type}.queue
        switch (eventType) {
            case "otp.sent":
                // Evento publicado no exchange auth.events, consumido por Transactional Messaging Service
                return "transactional.auth-otp-sent.queue";
            case "otp.validated":
                return "auth.otp-validated.queue";
            case "credentials.provisioned.v1":
                return "identity.credentials-provisioned.queue";
            case "user.created.v1":
                return "auth.user-created.queue";
            case "auth.logout":
                // Evento de logout - pode não estar implementado ainda
                return "auth.logout.queue";
            case "delivery.tracking.created.v1":
                // Evento publicado no exchange delivery-tracker.events
                // Consumido pelo próprio Delivery Tracker Service ou outros serviços
                return "delivery-tracker.delivery-tracking-created.queue";
            case "callback.received":
                // Evento de webhook recebido, publicado no exchange delivery-tracker.delivery-callbacks
                return "delivery-tracker.callback-received.queue";
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
                logger.debug("🔧 [TROUBLESHOOTING] Tentando consumir evento otp.sent. Verificando múltiplas filas...");
                
                // Tentar primeiro auth.otp-sent.queue
                Event event = tryConsumeFromQueue(eventType, "auth.otp-sent.queue", logger);
                if (event != null) {
                    logger.info("✅ [TROUBLESHOOTING] Evento otp.sent encontrado em auth.otp-sent.queue");
                    return event;
                }
                
                // Se não encontrou, tentar transactional.auth-otp-sent.queue
                logger.debug("🔧 [TROUBLESHOOTING] Nenhuma mensagem encontrada em auth.otp-sent.queue, tentando transactional.auth-otp-sent.queue");
                event = tryConsumeFromQueue(eventType, "transactional.auth-otp-sent.queue", logger);
                if (event != null) {
                    logger.info("✅ [TROUBLESHOOTING] Evento otp.sent encontrado em transactional.auth-otp-sent.queue");
                    return event;
                }
                
                // Se ainda não encontrou, pode ser que a mensagem já foi consumida pelos consumidores ativos
                // Nesse caso, vamos verificar se podemos obter do banco de dados ou logs
                logger.debug("🔧 [TROUBLESHOOTING] Nenhuma mensagem encontrada nas filas. A mensagem pode ter sido consumida pelos consumidores ativos.");
                logger.debug("🔧 [TROUBLESHOOTING] Isso é ESPERADO quando há consumidores ativos (ex: Transactional Messaging Service)");
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
        // Verificar conexão
        if (connection == null || !connection.isOpen()) {
            logger.warn("⚠️ [TROUBLESHOOTING] Conexão RabbitMQ não está aberta. Tentando reconectar...");
            try {
                init();
            } catch (Exception e) {
                logger.error("❌ [TROUBLESHOOTING] Falha ao reconectar ao RabbitMQ: {}", e.getMessage());
                return null;
            }
        }
        
        // Declarar fila caso não exista (modo passivo)
        // As filas devem ser criadas pelo RabbitConfig do microserviço
        try {
            channel.queueDeclarePassive(queueName);
            logger.debug("🔧 [TROUBLESHOOTING] Fila {} existe e está acessível", queueName);
            
            // Tentar obter informações da fila (quantidade de mensagens)
            try {
                com.rabbitmq.client.AMQP.Queue.DeclareOk queueInfo = channel.queueDeclarePassive(queueName);
                int messageCount = queueInfo.getMessageCount();
                int consumerCount = queueInfo.getConsumerCount();
                logger.info("🔧 [TROUBLESHOOTING] Fila {} - Mensagens: {}, Consumidores ativos: {}", 
                    queueName, messageCount, consumerCount);
                
                if (messageCount == 0 && consumerCount > 0) {
                    logger.warn("⚠️ [TROUBLESHOOTING] Fila {} está vazia mas tem {} consumidor(es) ativo(s). " +
                        "As mensagens podem ter sido consumidas antes desta validação.", queueName, consumerCount);
                }
            } catch (Exception e) {
                logger.debug("Não foi possível obter informações da fila {}: {}", queueName, e.getMessage());
            }
        } catch (IOException e) {
            logger.error("❌ [TROUBLESHOOTING] Fila {} não existe ou não está acessível: {}", queueName, e.getMessage());
            logger.error("❌ [TROUBLESHOOTING] Possíveis causas:");
            logger.error("   - RabbitMQ não está rodando");
            logger.error("   - Fila não foi criada pelo microserviço");
            logger.error("   - Permissões insuficientes");
            
            // Mensagem específica para auth.logout
            if (queueName.contains("logout")) {
                logger.warn("⚠️ [TROUBLESHOOTING] Evento auth.logout pode não estar implementado ainda.");
                logger.warn("⚠️ [TROUBLESHOOTING] Verifique se o Auth Service publica eventos de logout.");
                logger.warn("⚠️ [TROUBLESHOOTING] Se não estiver implementado, marque o cenário como @not_implemented.");
            }
            
            return null;
        }
        
        GetResponse response = channel.basicGet(queueName, false);
            
            if (response == null) {
                // Usar trace ao invés de debug para reduzir verbosidade durante polling
                logger.trace("🔧 [TROUBLESHOOTING] Nenhuma mensagem encontrada na fila {} (pode ter sido consumida)", queueName);
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
     * Obtém informações sobre uma fila (quantidade de mensagens, consumidores ativos)
     * 
     * @param queueName Nome da fila
     * @return Informações da fila ou null se a fila não existir ou houver erro
     */
    public QueueInfo getQueueInfo(String queueName) {
        var logger = org.slf4j.LoggerFactory.getLogger(RabbitMQHelper.class);
        try {
            // Verificar conexão
            if (connection == null || !connection.isOpen() || channel == null || !channel.isOpen()) {
                logger.debug("Conexão RabbitMQ não está aberta. Tentando reconectar...");
                try {
                    init();
                } catch (Exception e) {
                    logger.warn("Erro ao reconectar ao RabbitMQ: {}", e.getMessage());
                    return null;
                }
            }
            
            // Declarar fila em modo passivo (apenas verifica se existe)
            com.rabbitmq.client.AMQP.Queue.DeclareOk queueInfo = channel.queueDeclarePassive(queueName);
            
            return new QueueInfo(
                queueInfo.getMessageCount(),
                queueInfo.getConsumerCount()
            );
        } catch (IOException e) {
            logger.debug("Fila {} não existe ou não está acessível: {}", queueName, e.getMessage());
            return null;
        } catch (Exception e) {
            logger.debug("Erro ao obter informações da fila {}: {}", queueName, e.getMessage());
            return null;
        }
    }
    
    /**
     * Classe para representar informações de uma fila RabbitMQ
     */
    public static class QueueInfo {
        private final int messageCount;
        private final int consumerCount;
        
        public QueueInfo(int messageCount, int consumerCount) {
            this.messageCount = messageCount;
            this.consumerCount = consumerCount;
        }
        
        public int getMessageCount() {
            return messageCount;
        }
        
        public int getConsumerCount() {
            return consumerCount;
        }
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

