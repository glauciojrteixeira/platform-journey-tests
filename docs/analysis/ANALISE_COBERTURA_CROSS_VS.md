# Análise de Cobertura: Cenários Cross-VS (VS-Identity ↔ VS-Customer-Communications)

**Data de Criação**: 2025-12-11  
**Última Atualização**: 2025-12-11  
**Status**: ✅ Análise Completa  
**Versão**: 1.0

---

## 📊 Resumo Executivo

### Status Atual

| Categoria | Status | Quantidade |
|-----------|--------|------------|
| **Cenários VS-Identity (internos)** | ✅ Implementado | ~127 cenários |
| **Cenários Cross-VS** | ❌ **Não implementado** | 0 cenários |
| **Cenários VS-Customer-Communications** | ❌ **Não implementado** | 0 cenários |
| **Step Definitions Cross-VS** | ❌ **Não implementado** | 0 step definitions |

### Gap Identificado

**100% dos cenários cross-VS estão faltando** no projeto `platform-journey-tests`.

---

## 🔍 Análise Detalhada

### 1. Cobertura Atual (VS-Identity)

#### Cenários Implementados

**Arquivos .feature existentes:**
- ✅ `authentication/otp.feature` - OTP básico (sem validação cross-VS)
- ✅ `authentication/complete_registration_flow.feature` - Fluxo completo de registro
- ✅ `identity/create_identity.feature` - Criação de identidade
- ✅ `transversal/simulate_provider.feature` - Validação de simulação de providers

**Step Definitions Existentes:**
- ✅ `AuthenticationSteps.java` - Autenticação e OTP
- ✅ `IdentitySteps.java` - Operações de identidade
- ✅ `ProfileSteps.java` - Operações de perfil
- ✅ `SimulateProviderSteps.java` - Validação de simulação

**Clientes HTTP Existentes:**
- ✅ `AuthServiceClient.java` - Cliente para Auth Service
- ✅ `IdentityServiceClient.java` - Cliente para Identity Service
- ✅ `ProfileServiceClient.java` - Cliente para Profile Service

**Utils Existentes:**
- ✅ `RabbitMQHelper.java` - Helper para consumir eventos RabbitMQ
- ✅ `TestDataGenerator.java` - Geração de dados de teste
- ✅ `UserFixture.java` - Fixture para dados de usuário

#### Limitações Identificadas

1. **RabbitMQHelper não valida consumo cross-VS:**
   - ✅ Consome eventos do RabbitMQ
   - ❌ Não valida se Transactional Messaging Service consumiu
   - ❌ Não valida se Delivery Tracker Service processou
   - ❌ Não valida se Audit Compliance Service registrou

2. **Step Definitions não cobrem fluxos cross-VS:**
   - ❌ Não há steps para validar consumo de eventos por VS-Customer-Communications
   - ❌ Não há steps para validar envio de email/WhatsApp
   - ❌ Não há steps para validar tracking de entrega
   - ❌ Não há steps para validar logs de auditoria

3. **Clientes HTTP não incluem VS-Customer-Communications:**
   - ❌ Não há cliente para Transactional Messaging Service
   - ❌ Não há cliente para Delivery Tracker Service
   - ❌ Não há cliente para Audit Compliance Service

---

### 2. Cenários Planejados (Documentação)

#### VS-Identity - Cenários Cross-VS (10 cenários)

**Arquivo**: `VS-Identity/docs/tests/CENARIOS_TESTE_E2E_GHERKIN.feature`

1. ✅ Envio de OTP via Email - Fluxo Cross-VS Completo (REGISTRATION)
2. ✅ Envio de OTP via WhatsApp - Fluxo Cross-VS Completo (REGISTRATION)
3. ✅ Envio de OTP via Email - Fluxo Cross-VS Completo (LOGIN)
4. ✅ Envio de OTP via Email - Fluxo Cross-VS Completo (PASSWORD_RECOVERY)
5. ✅ Múltiplos OTPs simultâneos - Processamento assíncrono correto
6. ✅ Falha no Transactional Messaging Service - Evento deve ir para DLQ
7. ✅ Timeout no envio de email - Retry automático
8. ✅ Múltiplos eventos OTP - Ordem de processamento preservada
9. ✅ Consistência de dados entre VS-Identity e VS-Customer-Communications
10. ✅ Idempotência no processamento de eventos OTP
11. ✅ Rate limiting no envio de OTP - Múltiplas requisições

#### VS-Customer-Communications - Cenários de Integração (25 cenários)

**Arquivo**: `VS-CustomerCommunications/docs/tests/CENARIOS_TESTE_INTEGRACAO.feature`

**Integração Cross-VS (8 cenários):**
1. ✅ Consumir evento otp.sent e processar envio de OTP via Email
2. ✅ Consumir evento otp.sent e processar envio de OTP via WhatsApp
3. ✅ Rejeitar evento otp.sent com dados inválidos (email ausente)
4. ✅ Rejeitar evento otp.sent com canal inválido
5. ✅ Idempotência no processamento de evento otp.sent (evento duplicado)
6. ✅ Retry automático após falha temporária no envio de email
7. ✅ Evento otp.sent movido para DLQ após falhas repetidas

**Delivery Tracker Service (5 cenários):**
1. ✅ Delivery Tracker recebe evento de tracking criado pelo Transactional Messaging
2. ✅ Delivery Tracker recebe webhook do SendGrid e atualiza status
3. ✅ Rejeitar webhook do SendGrid com assinatura inválida
4. ✅ Rejeitar webhook do SendGrid com providerMessageId inexistente
5. ✅ Processar múltiplos webhooks do SendGrid para mesma mensagem

**Audit Compliance Service (2 cenários - @not_implemented):**
1. ✅ Audit Compliance recebe evento MESSAGE_SENT do Transactional Messaging
2. ✅ Audit Compliance recebe evento MESSAGE_DELIVERED do Delivery Tracker

**Edge Cases (10 cenários):**
1. ✅ Processar múltiplos eventos otp.sent simultaneamente
2. ✅ Falha parcial - Alguns eventos processados, outros não
3. ✅ Preservar ordem de eventos mesmo com retries
4. ✅ Consistência de dados entre Transactional Messaging e Delivery Tracker
5. ✅ Rate limiting no processamento de eventos
6. ✅ Timeout no processamento de evento - Retry e DLQ

---

### 3. Gap Analysis

#### Cenários Faltantes

| Categoria | Planejados | Implementados | Faltantes |
|-----------|------------|---------------|-----------|
| **Cross-VS (VS-Identity)** | 11 | 0 | **11 (100%)** |
| **Integração (VS-CC)** | 25 | 0 | **25 (100%)** |
| **Total** | **36** | **0** | **36 (100%)** |

#### Step Definitions Faltantes

| Step Definition | Status | Prioridade |
|-----------------|--------|------------|
| `o Transactional Messaging Service deve consumir o evento` | ❌ Faltando | 🔴 Crítica |
| `o SendOtpUseCase deve ser executado com sucesso` | ❌ Faltando | 🔴 Crítica |
| `o template de email OTP deve ser aplicado corretamente` | ❌ Faltando | 🟡 Alta |
| `o email deve ser enviado via SendGrid (simulado)` | ❌ Faltando | 🔴 Crítica |
| `a mensagem deve ser persistida no banco com status "SENT"` | ❌ Faltando | 🔴 Crítica |
| `o evento "delivery.tracking.created.v1" deve ser publicado` | ❌ Faltando | 🟡 Alta |
| `o Delivery Tracker Service deve consumir o evento` | ❌ Faltando | 🟡 Alta |
| `o Audit Compliance Service deve registrar log` | ❌ Faltando | 🟢 Média |

#### Clientes HTTP Faltantes

| Cliente | Status | Prioridade |
|---------|--------|------------|
| `TransactionalMessagingServiceClient.java` | ❌ Faltando | 🟡 Alta |
| `DeliveryTrackerServiceClient.java` | ❌ Faltando | 🟢 Média |
| `AuditComplianceServiceClient.java` | ❌ Faltando | 🟢 Baixa |

---

## 📋 Plano de Implementação

### Fase 1: Step Definitions Cross-VS (Prioridade Crítica)

**Arquivo**: `src/test/java/com/nulote/journey/stepdefinitions/CustomerCommunicationsSteps.java`

**Steps a implementar:**
1. ✅ `o Transactional Messaging Service (VS-Customer-Communications) deve consumir o evento da fila "{fila}"`
2. ✅ `o SendOtpUseCase deve ser executado com sucesso`
3. ✅ `o template de email OTP deve ser aplicado corretamente`
4. ✅ `o email deve ser enviado via SendGrid (simulado com header "simulate-provider: true")`
5. ✅ `a mensagem deve ser persistida no banco com status "{status}"`
6. ✅ `o evento "delivery.tracking.created.v1" deve ser publicado no RabbitMQ (exchange "{exchange}")`
7. ✅ `o Delivery Tracker Service deve consumir o evento e criar tracking inicial`

### Fase 2: Clientes HTTP (Prioridade Alta)

**Arquivos a criar:**
1. ✅ `src/test/java/com/nulote/journey/clients/TransactionalMessagingServiceClient.java`
2. ✅ `src/test/java/com/nulote/journey/clients/DeliveryTrackerServiceClient.java`
3. ✅ `src/test/java/com/nulote/journey/clients/AuditComplianceServiceClient.java`

### Fase 3: Arquivos .feature Cross-VS (Prioridade Crítica)

**Arquivos a criar:**
1. ✅ `src/test/resources/features/cross-vs/otp_email_registration.feature`
2. ✅ `src/test/resources/features/cross-vs/otp_whatsapp_registration.feature`
3. ✅ `src/test/resources/features/cross-vs/otp_email_login.feature`
4. ✅ `src/test/resources/features/cross-vs/otp_email_password_recovery.feature`
5. ✅ `src/test/resources/features/cross-vs/otp_edge_cases.feature`

### Fase 4: Arquivos .feature VS-Customer-Communications (Prioridade Alta)

**Arquivos a criar:**
1. ✅ `src/test/resources/features/vs-customer-communications/integration/otp_consumption.feature`
2. ✅ `src/test/resources/features/vs-customer-communications/delivery-tracker/webhook_processing.feature`
3. ✅ `src/test/resources/features/vs-customer-communications/audit-compliance/audit_logging.feature`
4. ✅ `src/test/resources/features/vs-customer-communications/edge_cases/integration_edge_cases.feature`

---

## 🎯 Próximos Passos

1. ✅ **Criar step definitions cross-VS** (`CustomerCommunicationsSteps.java`)
2. ✅ **Criar clientes HTTP** para VS-Customer-Communications
3. ✅ **Adicionar arquivos .feature cross-VS** na estrutura do projeto
4. ✅ **Adicionar arquivos .feature VS-Customer-Communications**
5. ✅ **Executar testes e validar cobertura**

---

**Última Atualização**: 2025-12-11
