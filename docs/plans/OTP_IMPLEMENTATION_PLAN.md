# 📋 Plano de Implementação de OTP - Baseado em ARCHITECTURE.md

## 🎯 Visão Geral

O **OTP (One-Time Password)** é uma funcionalidade crítica do **Auth Service** que permite validação de identidade via WhatsApp ou e-mail. É usado em múltiplos fluxos de negócio e é essencial para segurança e conformidade.

---

## 🔐 Responsabilidades por BU

### 🔑 Auth Service (BU Identity) - Responsabilidades

O **Auth Service** é responsável por:
- ✅ Autenticação via login social ou credenciais
- ✅ Emissão e validação de JWT
- ❌ **Geração e validação de código OTP** ← **NÃO IMPLEMENTADO**
- ❌ **Armazenamento seguro de OTP** ← **NÃO IMPLEMENTADO**
- ❌ **Gerenciamento de tentativas e expiração** ← **NÃO IMPLEMENTADO**
- ❌ Proteção com reCAPTCHA ← **NÃO IMPLEMENTADO**
- ✅ MFA (autenticação multifator) - parcialmente implementado

**Persistência**: PostgreSQL (dados críticos de segurança)

**O que o Auth Service NÃO faz:**
- ❌ **Envio físico de mensagens** (e-mail, WhatsApp, SMS) ← **Responsabilidade da BU Messaging**
- ❌ **Integração com provedores externos** (Twilio, SendGrid, etc.) ← **Responsabilidade da BU Messaging**
- ❌ **Orquestração de canais** ← **Responsabilidade da BU Messaging**

---

### 📨 BU Messaging - Responsabilidades

A **BU Messaging** é responsável por:
- ✅ **Envio físico de mensagens** via múltiplos canais
- ✅ **Integração com provedores externos** (Twilio, Zenvia, SendGrid, AWS SES)
- ✅ **Orquestração de canais** (escolher canal apropriado, fallback)
- ✅ **Gerenciamento de templates** de mensagens (escolha do template baseado em `purpose` + `channel`)
- ✅ **Mapeamento de propósito para template** (REGISTRATION + EMAIL → template específico)
- ✅ **Confirmação de entrega** e tracking

**Componentes (conforme diagrama):**
- **Transactional Messaging Service**: Envia mensagens críticas como confirmações, alertas e **códigos de segurança (OTP)**
- **Notification Orchestrator**: Coordena envio de notificações por múltiplos canais (e-mail, push, SMS)

**O que a BU Messaging NÃO faz:**
- ❌ **Geração de código OTP** ← **Responsabilidade do Auth Service**
- ❌ **Validação de código OTP** ← **Responsabilidade do Auth Service**
- ❌ **Armazenamento de OTP** ← **Responsabilidade do Auth Service**

---

## 🔗 Endpoints Previstos (Não Implementados)

### ❌ Endpoints REST do Auth Service

#### 1. Solicitação de OTP
```http
POST /api/v1/auth/otp/request
Content-Type: application/json
```

**Request Body:**
```json
{
  "userUuid": "uuid-do-usuario",
  "channel": "EMAIL" | "WHATSAPP",
  "purpose": "REGISTRATION" | "PASSWORD_RECOVERY" | "EMAIL_VERIFICATION" | "PHONE_VERIFICATION" | "ACCOUNT_DEACTIVATION" | "ACCOUNT_REACTIVATION"
}
```

**Response (200):**
```json
{
  "otpId": "uuid-do-otp",
  "channel": "EMAIL",
  "expiresAt": "2024-01-15T10:35:00Z",
  "message": "OTP enviado com sucesso"
}
```

#### 2. Validação de OTP
```http
POST /api/v1/auth/otp/validate
Content-Type: application/json
```

**Request Body:**
```json
{
  "otpId": "uuid-do-otp",
  "code": "123456",
  "userUuid": "uuid-do-usuario"
}
```

**Response (200):**
```json
{
  "valid": true,
  "purpose": "REGISTRATION",
  "token": "jwt-token-opcional" // Se for para registro/login
}
```

**Response (401) - OTP Inválido:**
```json
{
  "errorCode": "OTP_INVALID",
  "message": "Código OTP inválido ou expirado",
  "remainingAttempts": 2
}
```

---

## 📨 Eventos Assíncronos Previstos (Não Implementados)

### ❌ Eventos do Auth Service (Exchange: `auth.events`)

#### 1. `otp.sent` - OTP Gerado e Pronto para Envio
```json
{
  "eventId": "uuid-do-evento",
  "eventType": "otp.sent",
  "occurredAt": "2024-01-15T10:30:00Z",
  "userUuid": "uuid-do-usuario",
  "channel": "EMAIL" | "WHATSAPP",
  "purpose": "REGISTRATION" | "PASSWORD_RECOVERY" | "EMAIL_VERIFICATION" | "PHONE_VERIFICATION" | "ACCOUNT_DEACTIVATION" | "ACCOUNT_REACTIVATION",
  "otpId": "uuid-do-otp",
  "otpCode": "123456", // ⚠️ Código em texto plano (necessário para envio pela BU Messaging)
  "expiresAt": "2024-01-15T10:35:00Z",
  "userEmail": "usuario@example.com", // Para canal EMAIL
  "userPhone": "+5511999998888", // Para canal WHATSAPP
  "userName": "Nome do Usuário" // Opcional, para personalização da mensagem
}
```

**Publicado por:**
- `auth-service` → após gerar código OTP e armazenar (com hash)

**Consumido por:**
- **`BU Messaging`** → **envia mensagem física via provedor externo** ← **PRINCIPAL CONSUMIDOR**
- `user-profile-service` → registra tentativa de validação no `ValidationLog` (opcional)

**Notas Importantes:**

1. **Segurança**: O código OTP em texto plano é necessário no evento para que a BU Messaging possa incluí-lo na mensagem. O código também é armazenado com hash no Auth Service para validação posterior.

2. **Purpose no Evento**: O campo `purpose` é mantido no evento porque:
   - Fornece contexto necessário para a BU Messaging escolher o template apropriado
   - É útil para auditoria e rastreabilidade
   - Permite que a BU Messaging personalize a mensagem conforme o contexto

3. **Template é Responsabilidade da BU Messaging**: A BU Messaging decide qual template usar baseado em:
   - `purpose` (REGISTRATION, PASSWORD_RECOVERY, etc.)
   - `channel` (EMAIL, WHATSAPP)
   - Configurações internas da BU Messaging (templates disponíveis, preferências de negócio)
   
   **Exemplo de mapeamento interno na BU Messaging:**
   ```
   purpose: REGISTRATION + channel: EMAIL → template: "otp-registration-email"
   purpose: REGISTRATION + channel: WHATSAPP → template: "otp-registration-whatsapp"
   purpose: PASSWORD_RECOVERY + channel: EMAIL → template: "otp-password-recovery-email"
   ```
   
   Isso mantém a separação de responsabilidades: o Auth Service não precisa conhecer os templates disponíveis na BU Messaging.

#### 2. `otp.validated` - OTP Validado com Sucesso
```json
{
  "eventId": "uuid-do-evento",
  "eventType": "otp.validated",
  "occurredAt": "2024-01-15T10:32:00Z",
  "userUuid": "uuid-do-usuario",
  "channel": "EMAIL" | "WHATSAPP",
  "purpose": "REGISTRATION" | "PASSWORD_RECOVERY" | "EMAIL_VERIFICATION" | "PHONE_VERIFICATION" | "ACCOUNT_DEACTIVATION" | "ACCOUNT_REACTIVATION",
  "otpId": "uuid-do-otp",
  "validatedAt": "2024-01-15T10:32:00Z"
}
```

**Consumido por:**
- `user-profile-service` → atualiza `ValidationLog` com status `SUCCESS`
- `identity-service` → pode atualizar flags de verificação (email_verified, phone_verified)

---

## 🔄 Fluxos de Negócio que Usam OTP

### 1. 📝 Registro de Usuário (PF – B2C)

**Etapas:**
1. Usuário inicia registro via login social ou formulário
2. **reCAPTCHA validado** ← Não implementado
3. **OTP enviado via WhatsApp ou e-mail** ← Não implementado
4. **OTP validado** ← Não implementado
5. Identidade criada (`identity.created`) ✅ Implementado
6. Credencial registrada (`credential.created`) ❌ Não implementado
7. Perfil gerado automaticamente (`profile.created`) ✅ Implementado
8. JWT emitido ✅ Implementado

**Serviços envolvidos:**
- `Auth Service` → **OTP, reCAPTCHA, JWT** ← OTP não implementado
- `Identity Service` → criação de identidade ✅
- `User Profile Service` → perfil inicial ✅

---

### 2. 🔁 Recuperação de Senha

**Etapas:**
1. Usuário solicita recuperação
2. **reCAPTCHA validado** ← Não implementado
3. **OTP enviado para canal preferido** ← Não implementado
4. **OTP validado** ← Não implementado
5. Nova senha registrada ✅ Implementado (sem OTP)
6. Evento `credential.updated` publicado ❌ Não implementado

**Serviços envolvidos:**
- `Auth Service` → **OTP, redefinição de senha** ← OTP não implementado
- `User Profile Service` → atualização de segurança ✅

---

### 3. ✉️ Verificação de Email/Telefone

**Etapas:**
1. Usuário solicita verificação de email/telefone
2. **OTP enviado para o canal** ← Não implementado
3. **OTP validado** ← Não implementado
4. Campo `email_verified` ou `phone_verified` atualizado para `true`
5. Evento `identity.email.verified` ou `identity.phone.verified` publicado ❌ Não implementado

---

### 4. 🚫 Desativação de Conta

**Etapas:**
1. Usuário solicita desativação
2. **OTP enviado para confirmação** ← Não implementado
3. **OTP validado** ← Não implementado
4. Conta desativada (soft delete) ✅ Implementado (sem OTP)
5. Tokens revogados ✅ Implementado
6. Evento `user.deactivated` publicado ❌ Não implementado

---

### 5. ✅ Reativação de Conta

**Etapas:**
1. Usuário tenta fazer login com conta desativada
2. Sistema detecta conta desativada
3. **OTP enviado para email/telefone cadastrado** ← Não implementado
4. **OTP validado** ← Não implementado
5. Conta reativada ❌ Não implementado (retorna 501)
6. Evento `user.reactivated` publicado ❌ Não implementado

---

### 6. 🏢 Vinculação de Usuário à PJ

**Etapas:**
1. Admin da PJ convida novo usuário
2. **Email do funcionário deve ser do mesmo domínio cadastrado no email corporativo**
3. **Plataforma envia invite validado com OTP** ← Não implementado
4. **Validação via email corporativo** ← Não implementado
5. Usuário aceita convite e valida identidade
6. Credencial criada ✅ Implementado
7. Role atribuída ✅ Implementado
8. Perfil gerado ✅ Implementado
9. Evento `entity.linked` publicado ❌ Não implementado

---

## 🎯 Estratégia de Validação por Segmento

### 🔹 Segmento 1: Compradores Ocasionais (PF – B2C)
- **Objetivo**: Entrada rápida, mínima fricção
- **Validação**:
  - reCAPTCHA para prevenir bots ← Não implementado
  - **OTP via WhatsApp ou e-mail** ← Não implementado
  - Validação leve de e-mail/telefone ✅ Implementado
- **Risco**: Baixo
- **Foco**: Usabilidade

---

### 🔹 Segmento 2: Arrematadores Profissionais (PF – B2C)
- **Objetivo**: Segurança e recorrência
- **Validação**:
  - **OTP via canal preferido** ← Não implementado
  - MFA opcional ✅ Parcialmente implementado
  - Validação de CPF (via serviço externo) ← Não implementado
- **Risco**: Médio
- **Foco**: Confiança e rastreabilidade

---

### 🔹 Segmento 3: Revendedores e Lojistas (PJ – B2B)
- **Objetivo**: Gestão corporativa
- **Validação**:
  - Validação de CNPJ (via Receita ou serviço externo) ← Não implementado
  - Validação do representante legal (CPF + vínculo) ← Não implementado
  - **OTP + MFA para usuários vinculados** ← Não implementado
- **Risco**: Alto
- **Foco**: Conformidade e controle de acesso

---

## 🌐 Integrações entre BUs

### 📨 Comunicação Auth Service ↔ BU Messaging

#### Opção 1: Eventos Assíncronos (Recomendado)
```
Auth Service gera OTP
  ↓
Publica evento otp.sent (RabbitMQ)
  ↓
BU Messaging consome evento
  ↓
BU Messaging envia mensagem via provedor externo
  ↓
BU Messaging publica evento message.sent (opcional)
```

#### Opção 2: REST Síncrono (Alternativa)
```
Auth Service gera OTP
  ↓
Chama POST /api/v1/messaging/send-otp (BU Messaging)
  ↓
BU Messaging envia mensagem via provedor externo
  ↓
Retorna confirmação de entrega
```

**Recomendação**: Usar **eventos assíncronos** para desacoplamento e resiliência.

---

### 🌐 Integrações Externas da BU Messaging

#### Provedores de Mensageria (Responsabilidade da BU Messaging)

**Protocolo**: REST  
**Exemplos de Provedores:**
- **Twilio** - WhatsApp e SMS
- **Zenvia** - WhatsApp e SMS (Brasil)
- **SendGrid** - E-mail
- **AWS SES** - E-mail
- **Outros provedores** conforme necessidade

#### Fluxo Completo:
1. **Auth Service gera código OTP** e armazena (com hash)
2. **Auth Service publica evento `otp.sent`** (RabbitMQ)
3. **BU Messaging consome evento `otp.sent`**
4. **BU Messaging escolhe canal** (baseado em `validationChannel` do Profile)
5. **BU Messaging integra com provedor externo** (Twilio, SendGrid, etc.)
6. **BU Messaging envia mensagem física** com código OTP
7. **BU Messaging recebe confirmação de entrega** do provedor
8. **BU Messaging publica evento `message.sent`** (opcional)
9. **Usuário recebe código e informa ao Auth Service**
10. **Auth Service valida código** recebido
11. **Auth Service publica evento `otp.validated`**
12. **User Profile Service registra tentativa** no `ValidationLog`

---

## 🗄️ Estrutura de Dados Prevista

### ValidationLog (User Profile Service)

**Coleção MongoDB:**
```javascript
validation_log: {
  _id: ObjectId,
  uuid: UUID,
  userUuid: UUID,
  validationType: String (enum: OTP, MFA, RECAPTCHA),
  validationChannel: String (enum: EMAIL, WHATSAPP),
  validationStatus: String (enum: PENDING, SUCCESS, FAILED, EXPIRED),
  validatedAt: Date,
  details: Object, // { otpId, purpose, attempts, etc. }
  ipAddress: String,
  userAgent: String,
  createdAt: Date,
  updatedAt: Date,
  version: Integer,
  isActive: Boolean
}
```

**Status**: ✅ Estrutura implementada no User Profile Service

---

## ⏱️ Retenção de Dados

Conforme ARCHITECTURE.md:

| Tipo de dado | Retenção |
|---------------|----------|
| **OTPs e MFA temporários** | **15 minutos** |

**Estratégia**: TTL em banco, rotinas de limpeza automatizadas

---

## 🔒 Regras de Negócio

### 1. Geração de OTP
- **Formato**: 6 dígitos numéricos (padrão)
- **Validade**: 15 minutos (conforme retenção)
- **Tentativas**: Máximo 3 tentativas por OTP
- **Rate Limiting**: Máximo 5 solicitações por hora por usuário/canal

### 2. Validação de OTP
- **Case-insensitive**: Código pode ser digitado em maiúsculas ou minúsculas
- **Expiração**: OTP expira após 15 minutos
- **Tentativas**: Após 3 tentativas inválidas, OTP é invalidado
- **Idempotência**: Validar o mesmo OTP múltiplas vezes não deve causar efeitos colaterais

### 3. Canal de Envio
- **Preferência**: Usar canal preferido do usuário (`validationChannel` do Profile)
- **Fallback**: Se WhatsApp falhar, tentar e-mail (e vice-versa)
- **Registro**: Todas as tentativas devem ser registradas no `ValidationLog`

---

## 📊 Monitoramento e Alertas

### Métricas Previstas:
- Taxa de sucesso de envio de OTP (por canal)
- Taxa de validação bem-sucedida
- Tempo médio de validação (tempo entre envio e validação)
- Taxa de expiração (OTPs não validados)
- Taxa de falha de entrega (por provedor)

### Alertas Recomendados:
- ❌ **Falha na entrega de OTP** (conforme ARCHITECTURE.md)
- Taxa de falha acima de 5% por 5 minutos
- Timeout em integração com provedor externo
- Taxa de validação abaixo de 50% (pode indicar problema)

---

## 🏗️ Arquitetura Modular

Conforme ARCHITECTURE.md:

### ✅ Modularização por tipo de validação
- Cada tipo de validação é um **componente desacoplado** (ex: `otp-service`, `cpf-validator`, `cnpj-verifier`)
- O **Auth Service** orquestra qual validação aplicar com base no segmento

### ✅ Arquitetura Hexagonal
- Design Pattern Hexagonal para desacoplamento
- Chain of Responsibility para pipeline de validações
- Mudanças nas regras exigem novo deploy

---

## 📋 Checklist de Implementação

### Fase 1: Estrutura Base (Auth Service)
- [ ] Criar entidade `OtpEntity` no Auth Service (PostgreSQL)
- [ ] Criar repositório `OtpRepository`
- [ ] Criar serviço `OtpService` (domain layer) - geração, armazenamento, validação
- [ ] Criar adapter `OtpAdapter` (infrastructure layer)
- [ ] Criar controller `OtpController` com endpoints `/otp/request` e `/otp/validate`
- [ ] Criar evento `OtpSentEvent` (domain event)
- [ ] Criar evento `OtpValidatedEvent` (domain event)

### Fase 2: Geração e Armazenamento
- [ ] Implementar geração de código OTP (6 dígitos)
- [ ] Implementar armazenamento com TTL (15 minutos)
- [ ] Implementar controle de tentativas (máximo 3)
- [ ] Implementar rate limiting (máximo 5 solicitações/hora)

### Fase 3: Integração com BU Messaging
- [ ] Definir contrato de evento `otp.sent` (schema)
- [ ] Implementar publicação de evento `otp.sent` no Auth Service (Outbox Pattern)
- [ ] Criar consumer no BU Messaging para consumir `otp.sent`
- [ ] Implementar lógica de escolha de canal no BU Messaging
- [ ] Implementar integração com provedores externos no BU Messaging (SendGrid, Twilio, etc.)
- [ ] Implementar fallback entre canais no BU Messaging
- [ ] Implementar retry logic para falhas de entrega no BU Messaging
- [ ] Definir contrato de evento `message.sent` (opcional, para confirmação)

### Fase 4: Eventos Assíncronos
- [ ] Implementar publicação de evento `otp.sent` (Outbox Pattern)
- [ ] Implementar publicação de evento `otp.validated` (Outbox Pattern)
- [ ] Criar consumer no User Profile Service para atualizar `ValidationLog`
- [ ] Criar consumer no Identity Service para atualizar flags de verificação

### Fase 5: Integração com Fluxos Existentes
- [ ] Integrar OTP no fluxo de registro (após reCAPTCHA)
- [ ] Integrar OTP no fluxo de recuperação de senha
- [ ] Integrar OTP no fluxo de verificação de email/telefone
- [ ] Integrar OTP no fluxo de desativação de conta
- [ ] Integrar OTP no fluxo de reativação de conta
- [ ] Integrar OTP no fluxo de vinculação à PJ

### Fase 6: Testes
- [ ] Testes unitários para `OtpService` (Auth Service)
- [ ] Testes unitários para publicação de eventos `otp.sent` (Auth Service)
- [ ] Testes unitários para consumer de `otp.sent` (BU Messaging)
- [ ] Testes unitários para integração com provedores (BU Messaging - mocks)
- [ ] Testes de integração para endpoints REST (Auth Service)
- [ ] Testes de integração para eventos assíncronos (Auth Service ↔ BU Messaging)
- [ ] Testes E2E para fluxos completos (já existem nos feature files)

### Fase 7: Monitoramento
- [ ] Implementar métricas de OTP (Prometheus)
- [ ] Configurar alertas (Alertmanager)
- [ ] Dashboard no Grafana
- [ ] Logs estruturados com correlation IDs

---

## 🎯 Priorização de Implementação

### Prioridade ALTA (Crítico para Segmento 1)

**Auth Service:**
1. ✅ Endpoints básicos (`/otp/request`, `/otp/validate`)
2. ✅ Geração e armazenamento de OTP
3. ✅ Validação de código OTP
4. ✅ Publicação de evento `otp.sent`

**BU Messaging:**
5. ✅ Consumer de evento `otp.sent`
6. ✅ Integração com provedor de e-mail (SendGrid/AWS SES)
7. ✅ Integração no fluxo de registro (via eventos)

**Integração:**
8. ✅ Integração no fluxo de recuperação de senha

### Prioridade MÉDIA (Melhora UX)

**BU Messaging:**
9. ✅ Integração com WhatsApp (Twilio/Zenvia)
10. ✅ Fallback entre canais (WhatsApp → E-mail)

**Integração:**
11. ✅ Integração no fluxo de verificação de email/telefone
12. ✅ Integração no fluxo de desativação/reativação

### Prioridade BAIXA (Funcionalidades Avançadas)
13. ✅ Integração no fluxo de vinculação à PJ
14. ✅ Dashboard de métricas (BU Messaging)
15. ✅ Análise de padrões de uso
16. ✅ Evento `message.sent` para confirmação de entrega

---

## 📝 Notas Técnicas

### Segurança (Auth Service)
- **Código OTP**: Gerar usando `SecureRandom` (Java)
- **Armazenamento**: Hash do código (não armazenar código em texto plano)
- **Rate Limiting**: Implementar no nível de controller usando Redis
- **Expiração**: Usar TTL no banco de dados (PostgreSQL)
- **Tentativas**: Máximo 3 tentativas por OTP

### Performance (Auth Service)
- **Cache**: Cachear configurações de OTP (Redis)
- **Assíncrono**: Publicação de evento `otp.sent` é assíncrona (não bloquear resposta)
- **Validação**: Validação de código deve ser rápida (< 50ms)

### Segurança (BU Messaging)
- **Templates**: Usar templates seguros (não incluir código em logs)
- **Rate Limiting**: Implementar rate limiting por usuário/canal
- **Retry**: Implementar retry com exponential backoff para falhas de provedor
- **Fallback**: Implementar fallback automático entre canais

### Performance (BU Messaging)
- **Assíncrono**: Processamento de eventos deve ser assíncrono
- **Cache**: Cachear templates de mensagens (Redis)
- **Batching**: Agrupar envios quando possível (não crítico para OTP)

### Observabilidade
- **Correlation ID**: Incluir em todos os logs e eventos (Auth Service e BU Messaging)
- **Tracing**: Rastrear fluxo completo de OTP (OpenTelemetry) - Auth Service → BU Messaging → Provedor
- **Métricas Auth Service**: Taxa de geração, taxa de validação, tentativas
- **Métricas BU Messaging**: Taxa de entrega, latência de envio, falhas por provedor

---

## 🔗 Referências no ARCHITECTURE.md

- **Linha 47**: Auth Service responsável por validação via OTP
- **Linha 54**: Eventos `otp.sent` mencionados
- **Linha 156**: ValidationType inclui `OTP`
- **Linha 361**: Enum `OTP` definido
- **Linha 371**: Validação via OTP (WhatsApp/e-mail) e reCAPTCHA
- **Linha 385**: Proteção adicional com reCAPTCHA e OTP
- **Linha 444-445**: Endpoints `/otp/request` e `/otp/validate` não implementados
- **Linha 480**: OTP via WhatsApp ou e-mail para Segmento 1
- **Linha 490**: OTP via canal preferido para Segmento 2
- **Linha 503**: OTP + MFA para Segmento 3
- **Linha 533**: Validação com reCAPTCHA + OTP
- **Linha 617-618**: OTP enviado e validado no fluxo de registro
- **Linha 651-652**: OTP enviado e validado no fluxo de recuperação
- **Linha 701**: MFA pode usar OTP
- **Linha 930-932**: Fluxo de integração com provedores externos
- **Linha 969**: Alerta para falha na entrega de OTP
- **Linha 1051**: Retenção de 15 minutos para OTPs
- **Linha 1076**: `otp-service` como componente desacoplado
- **Linha 1150**: Convite validado com OTP
- **Linha 1192-1193**: Eventos `otp.sent` e `otp.validated`
- **Linha 1663-1664**: Endpoints não implementados
- **Linha 1673-1674**: Eventos não implementados
- **Linha 1685-1686**: Integrações com provedores não implementadas
- **Linha 1733**: Recuperação de senha com OTP não implementada
- **Linha 1774**: Provedores de OTP não integrados

---

**Última Atualização**: 2025-11-17  
**Baseado em**: ARCHITECTURE.md (versão 2.0)

