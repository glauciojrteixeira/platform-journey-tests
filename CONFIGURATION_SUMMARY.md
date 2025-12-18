# Resumo de Configuração por Ambiente

## ✅ Sim, basta informar as URLs dos microserviços!

A configuração é **muito simples**. Você só precisa informar as URLs dos 3 microserviços principais.

---

## 🎯 Configuração Mínima Necessária

### **Microserviços Obrigatórios**

```bash
export UAT_IDENTITY_URL="https://identity-service.uat.exemplo.com.br"
export UAT_AUTH_URL="https://auth-service.uat.exemplo.com.br"
export UAT_PROFILE_URL="https://profile-service.uat.exemplo.com.br"
```

**Isso é suficiente!** Os testes já funcionam com apenas essas 3 URLs.

---

## 🔧 Configurações Opcionais

### **RabbitMQ (Opcional)**

Se você quiser validar eventos assíncronos, pode configurar RabbitMQ:

```bash
export UAT_RABBITMQ_HOST="rabbitmq.uat.exemplo.com.br"
export UAT_RABBITMQ_PORT="5672"
export UAT_RABBITMQ_USERNAME="usuario"
export UAT_RABBITMQ_PASSWORD="senha"
```

> **Nota**: Se RabbitMQ não estiver disponível, os testes **continuam executando normalmente**. Apenas a validação de eventos será pulada (com warning).

### **Timeout (Opcional)**

Se os testes estiverem demorando muito devido à latência de rede:

```bash
export UAT_TIMEOUT=120000  # 120 segundos (padrão: 90 segundos)
```

---

## 📋 Comparação de Ambientes

| Ambiente | URLs Necessárias | RabbitMQ | Timeout Padrão |
|----------|------------------|----------|----------------|
| **LOCAL** | `localhost:8084`, `localhost:8080`, `localhost:8088` | `localhost:5672` | 30s |
| **SIT** | Via variáveis `SIT_*_URL` | Opcional | 60s |
| **UAT** | Via variáveis `UAT_*_URL` | Opcional | 90s |

---

## 🚀 Exemplo Completo

### **Configuração Mínima (Suficiente)**

```bash
# Apenas URLs dos microserviços
export UAT_IDENTITY_URL="https://identity-service.uat.exemplo.com.br"
export UAT_AUTH_URL="https://auth-service.uat.exemplo.com.br"
export UAT_PROFILE_URL="https://profile-service.uat.exemplo.com.br"

# Executar
mvn test -Dspring.profiles.active=uat
```

### **Configuração Completa (Com Opcionais)**

```bash
# URLs dos microserviços (obrigatório)
export UAT_IDENTITY_URL="https://identity-service.uat.exemplo.com.br"
export UAT_AUTH_URL="https://auth-service.uat.exemplo.com.br"
export UAT_PROFILE_URL="https://profile-service.uat.exemplo.com.br"

# RabbitMQ (opcional - para validação de eventos)
export UAT_RABBITMQ_HOST="rabbitmq.uat.exemplo.com.br"
export UAT_RABBITMQ_PORT="5672"
export UAT_RABBITMQ_USERNAME="usuario"
export UAT_RABBITMQ_PASSWORD="senha"

# Timeout (opcional - se necessário)
export UAT_TIMEOUT=120000

# Executar
mvn test -Dspring.profiles.active=uat
```

---

## 📝 Como Funciona

1. **Spring Boot** carrega `application-uat.yml` quando você usa `-Dspring.profiles.active=uat`
2. **Variáveis de ambiente** sobrescrevem valores padrão usando sintaxe `${VAR:default}`
3. **E2EConfiguration** injeta as URLs nos clientes HTTP automaticamente
4. **RabbitMQHelper** é opcional - se não configurado, apenas loga warnings

---

## ✅ Checklist Rápido

Para executar testes contra UAT, você precisa apenas de:

- [x] **URL do Identity Service** ✅
- [x] **URL do Auth Service** ✅
- [x] **URL do Profile Service** ✅
- [ ] RabbitMQ (opcional)
- [ ] Timeout customizado (opcional)

---

## 🔍 Validação Rápida

Antes de executar todos os testes, valide conectividade:

```bash
# Verificar se os serviços estão acessíveis
curl -v $UAT_IDENTITY_URL/health
curl -v $UAT_AUTH_URL/health
curl -v $UAT_PROFILE_URL/health
```

Se todos responderem, você está pronto para executar os testes!

---

**Última atualização**: 2025-11-14

