package com.nulote.journey.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nulote.journey.config.E2EConfiguration;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import org.springframework.beans.factory.annotation.Autowired;
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
    
    @Value("${rabbitmq.virtual-host:/}")
    private String virtualHost;
    
    @Autowired
    private E2EConfiguration config;
    
    // Multi-Country: Múltiplas conexões por virtual host
    private Map<String, Connection> connections = new HashMap<>();
    private Map<String, Channel> channels = new HashMap<>();
    private ObjectMapper objectMapper;
    private Map<String, Event> lastConsumedMessages = new HashMap<>();
    
    @PostConstruct
    public void init() {
        var logger = org.slf4j.LoggerFactory.getLogger(RabbitMQHelper.class);
        // Inicializar ObjectMapper imediatamente (não depende de RabbitMQ)
        objectMapper = new ObjectMapper();
        
        // Multi-Country: Não conectar durante init() - conexões serão estabelecidas de forma lazy
        // quando necessário, usando o virtual host correto para cada tipo de evento
        logger.info("🌍 [MULTI-COUNTRY] RabbitMQHelper inicializado. Conexões serão estabelecidas de forma lazy por virtual host.");
    }
    
    /**
     * Estabelece conexão com RabbitMQ para um virtual host específico.
     * Multi-Country: Mantém conexões separadas para cada virtual host.
     * 
     * @param vhost Virtual host a ser usado (ex: "/br", "/shared")
     * @throws IOException Se houver erro de I/O
     * @throws TimeoutException Se houver timeout
     */
    private void connect(String vhost) throws IOException, TimeoutException {
        var logger = org.slf4j.LoggerFactory.getLogger(RabbitMQHelper.class);
        
        // Se já está conectado para este virtual host, não reconectar
        Connection existingConnection = connections.get(vhost);
        Channel existingChannel = channels.get(vhost);
        if (existingConnection != null && existingConnection.isOpen() && 
            existingChannel != null && existingChannel.isOpen()) {
            logger.debug("🌍 [MULTI-COUNTRY] Conexão RabbitMQ já está estabelecida para vhost: {}", vhost);
            return;
        }
        
        logger.info("🌍 [MULTI-COUNTRY] Iniciando conexão com RabbitMQ em {}:{} (virtual host: {})", 
            host, port, vhost);
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setVirtualHost(vhost);
        
        // Configurar timeout de conexão para evitar travamentos
        factory.setConnectionTimeout(5000); // 5 segundos
        factory.setNetworkRecoveryInterval(5000); // 5 segundos
        
        Connection newConnection = factory.newConnection();
        Channel newChannel = newConnection.createChannel();
        
        // Armazenar conexão e canal para este virtual host
        connections.put(vhost, newConnection);
        channels.put(vhost, newChannel);
        
        logger.info("✅ [MULTI-COUNTRY] Conexão RabbitMQ estabelecida com sucesso em {}:{} (virtual host: {})", 
            host, port, vhost);
    }
    
    /**
     * Estabelece conexão com RabbitMQ usando o virtual host padrão.
     * Mantido para compatibilidade com código existente.
     * 
     * @throws IOException Se houver erro de I/O
     * @throws TimeoutException Se houver timeout
     */
    private void connect() throws IOException, TimeoutException {
        String defaultVhost = determineVirtualHost();
        connect(defaultVhost);
    }
    
    /**
     * Determina o virtual host a ser usado baseado na configuração.
     * Prioridade:
     * 1. Configuração explícita (rabbitmq.virtual-host)
     * 2. Baseado no país (config.getCountryCodeHeader() -> "/br")
     * 3. Fallback para "/" (padrão)
     */
    private String determineVirtualHost() {
        // Se virtual host foi configurado explicitamente, usar
        if (virtualHost != null && !virtualHost.isEmpty() && !virtualHost.equals("/")) {
            return virtualHost;
        }
        
        // Se não, tentar inferir do país
        if (config != null && config.getDefaultCountryCode() != null) {
            String countryCode = config.getCountryCodeHeader(); // Retorna lowercase (ex: "br")
            String vhost = "/" + countryCode; // Ex: "/br"
            var logger = org.slf4j.LoggerFactory.getLogger(RabbitMQHelper.class);
            logger.debug("🌍 [MULTI-COUNTRY] Virtual host inferido do país: {} -> {}", 
                config.getDefaultCountryCode(), vhost);
            return vhost;
        }
        
        // Fallback para padrão
        return "/";
    }
    
    /**
     * Determina o virtual host correto baseado no tipo de evento.
     * Multi-Country: Eventos VS-Identity usam /br, eventos VS-CustomerCommunications usam /shared.
     * 
     * @param eventType Tipo de evento (ex: "otp.sent", "user.created.v1")
     * @return Virtual host correto para o tipo de evento
     */
    private String determineVirtualHostForEvent(String eventType) {
        var logger = org.slf4j.LoggerFactory.getLogger(RabbitMQHelper.class);
        
        // Eventos VS-Identity -> /br
        if (isVSIdentityEvent(eventType)) {
            String vhost = "/br";
            logger.debug("🌍 [MULTI-COUNTRY] Evento {} identificado como VS-Identity -> vhost: {}", eventType, vhost);
            return vhost;
        }
        
        // Eventos VS-CustomerCommunications -> /shared
        if (isVSCustomerCommunicationsEvent(eventType)) {
            String vhost = "/shared";
            logger.debug("🌍 [MULTI-COUNTRY] Evento {} identificado como VS-CustomerCommunications -> vhost: {}", eventType, vhost);
            return vhost;
        }
        
        // Fallback: usar virtual host padrão da configuração
        String defaultVhost = determineVirtualHost();
        logger.debug("🌍 [MULTI-COUNTRY] Evento {} não mapeado, usando vhost padrão: {}", eventType, defaultVhost);
        return defaultVhost;
    }
    
    /**
     * Verifica se um evento pertence à VS-Identity (virtual host /br).
     */
    private boolean isVSIdentityEvent(String eventType) {
        return eventType.equals("user.created.v1") ||
               eventType.equals("credentials.provisioned.v1") ||
               eventType.equals("otp.validated") ||
               eventType.equals("auth.logout");
    }
    
    /**
     * Verifica se um evento pertence à VS-CustomerCommunications (virtual host /shared).
     */
    private boolean isVSCustomerCommunicationsEvent(String eventType) {
        return eventType.equals("otp.sent") ||
               eventType.equals("welcome.message.sent") ||
               eventType.equals("delivery.tracking.created.v1") ||
               eventType.equals("callback.received");
    }
    
    @PreDestroy
    public void close() throws IOException, TimeoutException {
        var logger = org.slf4j.LoggerFactory.getLogger(RabbitMQHelper.class);
        
        // Multi-Country: Fechar todas as conexões e canais
        for (Map.Entry<String, Channel> entry : channels.entrySet()) {
            String vhost = entry.getKey();
            Channel ch = entry.getValue();
            if (ch != null && ch.isOpen()) {
                try {
                    ch.close();
                    logger.debug("🌍 [MULTI-COUNTRY] Canal fechado para vhost: {}", vhost);
                } catch (Exception e) {
                    logger.warn("Erro ao fechar canal para vhost {}: {}", vhost, e.getMessage());
                }
            }
        }
        channels.clear();
        
        for (Map.Entry<String, Connection> entry : connections.entrySet()) {
            String vhost = entry.getKey();
            Connection conn = entry.getValue();
            if (conn != null && conn.isOpen()) {
                try {
                    conn.close();
                    logger.debug("🌍 [MULTI-COUNTRY] Conexão fechada para vhost: {}", vhost);
                } catch (Exception e) {
                    logger.warn("Erro ao fechar conexão para vhost {}: {}", vhost, e.getMessage());
                }
            }
        }
        connections.clear();
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
                // Evento de logout publicado no exchange auth.events quando logout ocorre
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
            // Multi-Country: Determinar virtual host correto para este evento
            String vhost = determineVirtualHostForEvent(eventType);
            logger.debug("🌍 [MULTI-COUNTRY] Consumindo evento {} do vhost: {}", eventType, vhost);
            
            // Obter conexão e canal para este virtual host
            Channel channel = channels.get(vhost);
            if (channel == null || !channel.isOpen()) {
                logger.debug("🌍 [MULTI-COUNTRY] Canal não está aberto para vhost {}. Tentando conectar...", vhost);
                try {
                    connect(vhost);
                    channel = channels.get(vhost);
                } catch (Exception e) {
                    logger.warn("Erro ao conectar ao RabbitMQ no vhost {}: {}", vhost, e.getMessage());
                    return null;
                }
            }
            
            // Determinar nome da fila seguindo padrão do projeto
            String finalQueueName = queueName != null ? queueName : determineQueueName(eventType);
            logger.debug("🌍 [MULTI-COUNTRY] Consumindo evento {} da fila {} no vhost {}", eventType, finalQueueName, vhost);
            
            // Para otp.sent, tentar ambas as filas possíveis (pode estar em /br ou /shared)
            // IMPORTANTE: Como há consumidores ativos nas filas principais, as mensagens são consumidas rapidamente
            if ("otp.sent".equals(eventType) && queueName == null) {
                logger.debug("🔧 [TROUBLESHOOTING] Tentando consumir evento otp.sent. Verificando múltiplas filas e vhosts...");
                
                // Primeiro tentar no vhost /shared (onde está a fila transactional.auth-otp-sent.queue)
                Event event = tryConsumeFromQueue(eventType, "transactional.auth-otp-sent.queue", "/shared", logger);
                if (event != null) {
                    logger.info("✅ [TROUBLESHOOTING] Evento otp.sent encontrado em transactional.auth-otp-sent.queue (vhost /shared)");
                    return event;
                }
                
                // Se não encontrou, tentar no vhost /br (onde pode estar auth.otp-sent.queue)
                logger.debug("🔧 [TROUBLESHOOTING] Nenhuma mensagem encontrada em /shared, tentando /br");
                event = tryConsumeFromQueue(eventType, "auth.otp-sent.queue", "/br", logger);
                if (event != null) {
                    logger.info("✅ [TROUBLESHOOTING] Evento otp.sent encontrado em auth.otp-sent.queue (vhost /br)");
                    return event;
                }
                
                // Se ainda não encontrou, pode ser que a mensagem já foi consumida pelos consumidores ativos
                logger.debug("🔧 [TROUBLESHOOTING] Nenhuma mensagem encontrada nas filas. A mensagem pode ter sido consumida pelos consumidores ativos.");
                logger.debug("🔧 [TROUBLESHOOTING] Isso é ESPERADO quando há consumidores ativos (ex: Transactional Messaging Service)");
                return null;
            }
            
            return tryConsumeFromQueue(eventType, finalQueueName, vhost, logger);
        } catch (Exception e) {
            logger.error("Erro ao consumir mensagem do RabbitMQ: {}", e.getMessage(), e);
            // Em ambiente de teste, não falhar o teste se RabbitMQ não estiver disponível
            // Apenas logar o erro
            return null;
        }
    }
    
    /**
     * Tenta consumir uma mensagem de uma fila específica no virtual host especificado.
     * 
     * @param eventType Tipo de evento
     * @param queueName Nome da fila
     * @param vhost Virtual host a ser usado
     * @param logger Logger
     * @return Evento consumido ou null
     */
    private Event tryConsumeFromQueue(String eventType, String queueName, String vhost, org.slf4j.Logger logger) throws IOException {
        // Multi-Country: Verificar conexão para este virtual host específico
        Connection connection = connections.get(vhost);
        Channel channel = channels.get(vhost);
        
        if (connection == null || !connection.isOpen() || channel == null || !channel.isOpen()) {
            logger.warn("⚠️ [MULTI-COUNTRY] Conexão RabbitMQ não está aberta para vhost {}. Tentando reconectar...", vhost);
            try {
                connect(vhost);
                connection = connections.get(vhost);
                channel = channels.get(vhost);
            } catch (Exception e) {
                logger.error("❌ [MULTI-COUNTRY] Falha ao reconectar ao RabbitMQ no vhost {}: {}", vhost, e.getMessage());
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
                logger.warn("⚠️ [TROUBLESHOOTING] Evento auth.logout deve ser publicado pelo Auth Service após logout.");
                logger.warn("⚠️ [TROUBLESHOOTING] Verifique se o evento está sendo publicado no exchange auth.events.");
                logger.warn("⚠️ [TROUBLESHOOTING] Verifique se a fila auth.logout.queue foi criada e está configurada corretamente.");
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
    /**
     * Obtém informações sobre uma fila, tentando em ambos os virtual hosts se necessário.
     * 
     * @param queueName Nome da fila
     * @return Informações da fila ou null se a fila não existir ou houver erro
     */
    public QueueInfo getQueueInfo(String queueName) {
        var logger = org.slf4j.LoggerFactory.getLogger(RabbitMQHelper.class);
        
        // Tentar primeiro no vhost /shared (VS-CustomerCommunications)
        QueueInfo info = getQueueInfo(queueName, "/shared", logger);
        if (info != null) {
            return info;
        }
        
        // Se não encontrou, tentar no vhost /br (VS-Identity)
        info = getQueueInfo(queueName, "/br", logger);
        if (info != null) {
            return info;
        }
        
        // Se ainda não encontrou, tentar no vhost padrão
        String defaultVhost = determineVirtualHost();
        if (!defaultVhost.equals("/shared") && !defaultVhost.equals("/br")) {
            return getQueueInfo(queueName, defaultVhost, logger);
        }
        
        return null;
    }
    
    /**
     * Obtém informações sobre uma fila em um virtual host específico.
     * 
     * @param queueName Nome da fila
     * @param vhost Virtual host a ser usado
     * @param logger Logger
     * @return Informações da fila ou null se a fila não existir ou houver erro
     */
    private QueueInfo getQueueInfo(String queueName, String vhost, org.slf4j.Logger logger) {
        try {
            // Multi-Country: Verificar conexão para este virtual host específico
            Connection connection = connections.get(vhost);
            Channel channel = channels.get(vhost);
            
            if (connection == null || !connection.isOpen() || channel == null || !channel.isOpen()) {
                logger.debug("🌍 [MULTI-COUNTRY] Conexão RabbitMQ não está aberta para vhost {}. Tentando conectar...", vhost);
                try {
                    connect(vhost);
                    connection = connections.get(vhost);
                    channel = channels.get(vhost);
                } catch (Exception e) {
                    logger.debug("Erro ao conectar ao RabbitMQ no vhost {}: {}", vhost, e.getMessage());
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
            logger.debug("🌍 [MULTI-COUNTRY] Fila {} não existe ou não está acessível no vhost {}: {}", queueName, vhost, e.getMessage());
            return null;
        } catch (Exception e) {
            logger.debug("🌍 [MULTI-COUNTRY] Erro ao obter informações da fila {} no vhost {}: {}", queueName, vhost, e.getMessage());
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

