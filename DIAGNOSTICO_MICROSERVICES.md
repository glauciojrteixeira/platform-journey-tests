# Diagnóstico: Integração com Microserviços BU-Identity

**Data:** 2025-11-17  
**Status:** Investigação Completa

---

## 🔍 Problemas Identificados

### 1. Endpoint de Verificação de Credenciais ❌ **NÃO EXISTE**

**Problema:** Os testes estão tentando usar `/api/auth/credentials/user/{userUuid}` que **não existe** no Auth Service.

**Endpoint Correto:** `/api/v1/users/{uuid}` (retorna usuário com informações básicas)

**Fluxo Real:**
- Identity Service cria usuário → publica evento `user.created.v1`
- Auth Service consome evento → cria credenciais automaticamente via `UserCreatedConsumer`
- Credenciais são criadas **assincronamente** via RabbitMQ
- **Não há endpoint REST** para verificar credenciais diretamente

**Solução:** Usar `/api/v1/users/{uuid}` para verificar se usuário existe no Auth Service (indica que credenciais foram provisionadas).

---

### 2. Endpoint de Criação de Perfil ⚠️ **FORMATO INCORRETO**

**Problema:** Os testes estão tentando usar `POST /api/profile/users/{userUuid}` que **não existe**.

**Endpoint Correto:** `POST /api/v1/profile` (cria perfil com body JSON)

**Formato Esperado:**
```json
{
  "userUuid": "uuid",
  "language": "pt-BR",
  "notifications": true,
  "validationChannel": "EMAIL",
  "relationship": "B2C"
}
```

**Fluxo Real:**
- Identity Service cria usuário → publica evento `user.created.v1`
- Profile Service consome evento → cria perfil automaticamente via `UserCreatedConsumer`
- Perfil é criado **assincronamente** via RabbitMQ

**Solução:** Corrigir endpoint e formato da requisição.

---

### 3. Timeout Muito Alto ⏱️

**Problema:** Timeout de 30 segundos é muito alto para testes E2E.

**Solução:** Reduzir para 5 segundos (suficiente para processamento assíncrono via RabbitMQ).

---

## 📋 Endpoints Disponíveis

### Auth Service

| Endpoint | Método | Descrição |
|----------|--------|-----------|
| `/api/v1/users/{uuid}` | GET | Retorna usuário (indica que credenciais foram provisionadas) |
| `/api/v1/auth/login` | POST | Login com credenciais |
| `/api/v1/auth/token/validate` | POST | Valida token JWT |
| `/api/v1/auth/logout` | POST | Logout |
| `/api/v1/auth/password/change` | POST | Alterar senha |
| `/api/v1/auth/otp/request` | POST | Solicitar OTP |
| `/api/v1/auth/otp/validate` | POST | Validar OTP |

**Nota:** 
- Não há endpoint específico para verificar credenciais. O usuário existe no Auth Service apenas após provisionamento de credenciais.
- Todos os endpoints do Auth Service usam `/v1` para controle de versão.

### Profile Service

| Endpoint | Método | Descrição |
|----------|--------|-----------|
| `/api/v1/profile/user/{userUuid}` | GET | Busca perfil por userUuid |
| `/api/v1/profile` | POST | Cria perfil (formato JSON no body) |
| `/api/v1/profile/{uuid}` | PUT | Atualiza perfil |

---

## 🔄 Fluxo Assíncrono Real

### Criação de Identidade → Provisionamento de Credenciais

```
1. Identity Service: POST /api/v1/identity/users → cria usuário
   ↓
2. Identity Service: publica evento user.created.v1 → identity.events
   ↓
3. RabbitMQ: roteia para auth.user-created.q
   ↓
4. Auth Service: UserCreatedConsumer.onUserCreated() processa evento
   ↓
5. Auth Service: cria credenciais automaticamente
   ↓
6. Auth Service: publica evento credentials.provisioned.v1 → auth.events
```

**Tempo Estimado:** < 2 segundos (processamento assíncrono)

### Criação de Identidade → Criação de Perfil

```
1. Identity Service: POST /api/v1/identity/users → cria usuário
   ↓
2. Identity Service: publica evento user.created.v1 → identity.events
   ↓
3. RabbitMQ: roteia para profile.user-created.q
   ↓
4. Profile Service: UserCreatedConsumer.onUserCreated() processa evento
   ↓
5. Profile Service: cria perfil automaticamente
```

**Tempo Estimado:** < 2 segundos (processamento assíncrono)

---

## ✅ Correções Necessárias

1. **AuthServiceClient.getCredentialsByUserUuid()**
   - ❌ Atual: `/api/auth/credentials/user/{userUuid}` (não existe)
   - ✅ Correto: `/api/v1/users/{uuid}` (verifica se usuário existe)

2. **ProfileServiceClient.createProfile()**
   - ❌ Atual: `POST /api/profile/users/{userUuid}` (não existe)
   - ✅ Correto: `POST /api/v1/profile` (com body JSON)

3. **Timeouts**
   - ❌ Atual: 30 segundos
   - ✅ Correto: 5 segundos (suficiente para processamento assíncrono)

---

## 📝 Próximos Passos

1. ✅ Corrigir endpoint de verificação de credenciais
2. ✅ Corrigir endpoint de criação de perfil
3. ✅ Reduzir timeout de 30s para 5s
4. ✅ Testar correções

---

**Última atualização:** 2025-11-17

