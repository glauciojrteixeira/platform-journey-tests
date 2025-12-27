# Mapeamento Completo de Jornadas - BU Identity

## 📊 Resumo Executivo

**Total de Jornadas Documentadas**: **55 jornadas**

Este documento mapeia todas as jornadas previstas nos microserviços da BU Identity e compara com os testes E2E implementados.

---

## 🧍 Segmento 1: Compradores Ocasionais (PF - B2C)

### **Total: 11 Jornadas**

| ID | Jornada | Status Teste | Feature File | Observações |
|----|---------|-------------|--------------|-------------|
| J1.1 | Registro e Onboarding | ✅ Parcial | `registration.feature` | Teste simplificado sem OTP |
| J1.2 | Primeiro Login | ⚠️ Parcial | `login.feature` | Pode precisar setup de credenciais |
| J1.3 | Login Recorrente | ❌ Não testado | - | Não implementado |
| J1.4 | Atualização de Perfil | ❌ Não testado | - | Não implementado |
| J1.5 | Alteração de Dados Pessoais | ❌ Não testado | - | Não implementado |
| J1.6 | Recuperação de Senha | ❌ Não testado | `password_recovery.feature` | Marcado como `@not_implemented` |
| J1.7 | Alteração de Senha | ❌ Não testado | - | Não implementado |
| J1.8 | Desativação de Conta | ❌ Não testado | - | Não implementado |
| J1.9 | Reativação de Conta | ❌ Não testado | - | Não implementado |
| J1.10 | Logout | ✅ Testado | `logout.feature` | ✅ Implementado |
| J1.11 | Verificação de Email/Telefone | ❌ Não testado | - | Não implementado |

**Cobertura**: 3/11 (27%) - Parcial

---

## 🧑‍💼 Segmento 2: Arrematadores Profissionais (PF - B2C)

### **Total: 17 Jornadas** (11 do Segmento 1 + 6 específicas)

| ID | Jornada | Status Teste | Feature File | Observações |
|----|---------|-------------|--------------|-------------|
| J2.1 | Registro com Validação CPF | ❌ Não testado | - | Não implementado |
| J2.2 | Ativação de MFA | ❌ Não testado | - | Não implementado |
| J2.3 | Login com MFA | ❌ Não testado | - | Não implementado |
| J2.4 | Histórico de Logins | ❌ Não testado | - | Não implementado |
| J2.5 | Gestão de Dispositivos | ❌ Não testado | - | Não implementado |
| J2.6 | Logout de Todos os Dispositivos | ❌ Não testado | - | Não implementado |
| J2.7 | Upgrade para Profissional | ❌ Não testado | - | Não implementado |
| + J1.2 a J1.11 | (herdadas do Segmento 1) | ⚠️ Parcial | - | Mesmo status do Segmento 1 |

**Cobertura**: 0/17 (0%) - Não testado

---

## 🏪 Segmento 3: Revendedores e Lojistas (PJ - B2B)

### **Total: 13 Jornadas** (7 específicas + 6 transversais)

| ID | Jornada | Status Teste | Feature File | Observações |
|----|---------|-------------|--------------|-------------|
| J3.1 | Registro de Entidade Jurídica | ⚠️ Parcial | `legal_entity.feature` | Marcado como `@partial` |
| J3.2 | Processo de Convite | ❌ Não testado | - | Não implementado |
| J3.3 | Alteração de Role | ❌ Não testado | - | Não implementado |
| J3.4 | Suspensão de Usuário | ❌ Não testado | - | Não implementado |
| J3.5 | Remoção de Usuário da PJ | ❌ Não testado | - | Não implementado |
| J3.6 | Transferência de Representação | ❌ Não testado | - | Não implementado |
| J3.7 | Cancelamento de Entidade Jurídica | ❌ Não testado | - | Não implementado |

**Cobertura**: 1/13 (8%) - Parcial

---

## 🧑‍💻 Segmento 4: Plataformas de Leilão (PJ - B2B)

### **Total: 14 Jornadas** (8 específicas + 6 do Segmento 3)

| ID | Jornada | Status Teste | Feature File | Observações |
|----|---------|-------------|--------------|-------------|
| J4.1 | Registro e Validação Completa | ❌ Não testado | - | Não implementado |
| J4.2 | Configuração Inicial de SSO | ❌ Não testado | - | Não implementado |
| J4.3 | Login via SSO B2B | ❌ Não testado | - | Não implementado |
| J4.4 | Geração e Gestão de API Keys | ❌ Não testado | - | Não implementado |
| J4.5 | Rotação de Certificados SSO | ❌ Não testado | - | Não implementado |
| J4.6 | Gestão de Sessões SSO | ❌ Não testado | - | Não implementado |
| J4.7 | Auditoria Completa | ❌ Não testado | - | Não implementado |
| J4.8 | Revogação de Tokens Ativos | ❌ Não testado | - | Não implementado |

**Cobertura**: 0/14 (0%) - Não testado

---

## 🔄 Jornadas Transversais

### **Total: 4 Jornadas**

| ID | Jornada | Status Teste | Feature File | Observações |
|----|---------|-------------|--------------|-------------|
| JT.1 | Refresh Token | ❌ Não testado | - | Não implementado |
| JT.2 | Verificação de Email Existente | ❌ Não testado | - | Não implementado |
| JT.3 | Verificação de Telefone Existente | ❌ Não testado | - | Não implementado |
| JT.4 | Logout de Todos os Dispositivos | ❌ Não testado | - | Não implementado |

**Cobertura**: 0/4 (0%) - Não testado

---

## 📊 Resumo de Cobertura

### **Por Segmento**

| Segmento | Total Jornadas | Testadas | Parcial | Não Testadas | Cobertura |
|----------|----------------|----------|--------|--------------|-----------|
| Segmento 1 | 11 | 1 | 2 | 8 | 27% ⚠️ |
| Segmento 2 | 17 | 0 | 0 | 17 | 0% ❌ |
| Segmento 3 | 13 | 0 | 1 | 12 | 8% ❌ |
| Segmento 4 | 14 | 0 | 0 | 14 | 0% ❌ |
| Transversais | 4 | 0 | 0 | 4 | 0% ❌ |
| **TOTAL** | **55** | **1** | **3** | **51** | **7%** ⚠️ |

### **Por Status de Implementação**

| Status | Quantidade | Jornadas |
|--------|------------|----------|
| ✅ Totalmente Testado | 1 | J1.10 |
| ⚠️ Parcialmente Testado | 3 | J1.1, J1.2, J3.1 |
| ❌ Não Testado | 51 | Todas as outras |

---

## 🎯 Análise Detalhada

### **Jornadas Críticas Não Testadas**

#### **Segmento 1 (Alta Prioridade)**
1. **J1.3: Login Recorrente** - Funcionalidade básica essencial
2. **J1.4: Atualização de Perfil** - Operação comum do usuário
3. **J1.5: Alteração de Dados Pessoais** - Requisito de LGPD
4. **J1.7: Alteração de Senha** - Segurança básica
5. **J1.8: Desativação de Conta** - Requisito de LGPD
6. **J1.9: Reativação de Conta** - Recuperação de conta
7. **J1.11: Verificação de Email/Telefone** - Validação de dados

#### **Segmento 2 (Média Prioridade)**
1. **J2.1: Registro com Validação CPF** - Diferencial do segmento
2. **J2.2: Ativação de MFA** - Segurança reforçada
3. **J2.3: Login com MFA** - Fluxo crítico de segurança
4. **J2.7: Upgrade para Profissional** - Conversão de segmento

#### **Segmento 3 (Alta Prioridade B2B)**
1. **J3.2: Processo de Convite** - Funcionalidade core B2B
2. **J3.3: Alteração de Role** - Gestão de permissões
3. **J3.4: Suspensão de Usuário** - Controle administrativo
4. **J3.5: Remoção de Usuário** - Gestão de equipe

#### **Segmento 4 (Média Prioridade - Enterprise)**
1. **J4.1: Registro e Validação Completa** - Onboarding enterprise
2. **J4.3: Login via SSO** - Autenticação corporativa
3. **J4.4: Geração de API Keys** - Integrações técnicas

---

## 📋 Endpoints por Jornada

### **Identity Service**

| Jornada | Endpoint | Método | Status Implementação | Status Teste |
|---------|----------|--------|---------------------|--------------|
| J1.1, J2.1 | `/api/identity/users` | POST | ✅ Implementado | ✅ Testado |
| J1.5 | `/api/identity/users/{uuid}` | PUT | ✅ Implementado | ❌ Não testado |
| J1.8 | `/api/identity/users/{uuid}/deactivate` | POST | ⚠️ Desconhecido | ❌ Não testado |
| J1.9 | `/api/identity/users/{uuid}/reactivate` | POST | ⚠️ Desconhecido | ❌ Não testado |
| J2.1, J2.7 | `/api/identity/users/{uuid}/validate-cpf` | POST | ⚠️ 501 (não implementado) | ❌ Não testado |
| J1.11, JT.2 | `/api/identity/users/{uuid}/verify-email` | POST | ⚠️ 501 (não implementado) | ❌ Não testado |
| J1.11, JT.3 | `/api/identity/users/{uuid}/verify-phone` | POST | ⚠️ 501 (não implementado) | ❌ Não testado |
| J2.7 | `/api/identity/users/{uuid}/upgrade-to-professional` | POST | ⚠️ 501 (não implementado) | ❌ Não testado |
| J3.1, J4.1 | `/api/identity/legal-entities` | POST | ✅ Implementado | ⚠️ Parcial |
| J3.1, J4.1 | `/api/identity/legal-entities/{uuid}` | GET | ✅ Implementado | ❌ Não testado |
| J4.1 | `/api/identity/legal-entities/{uuid}/validate-domain` | POST | ✅ Implementado | ❌ Não testado |
| J4.1 | `/api/identity/legal-entities/{uuid}/validate-partnership` | POST | ✅ Implementado | ❌ Não testado |
| J3.2 | `/api/identity/legal-entities/{uuid}/invite` | POST | ⚠️ Requer Auth | ❌ Não testado |
| J3.6 | `/api/identity/legal-entities/{uuid}/transfer-representation` | POST | ⚠️ Requer Auth | ❌ Não testado |
| J3.7 | `/api/identity/legal-entities/{uuid}/cancel` | POST | ✅ Implementado | ❌ Não testado |
| J3.2, J3.3 | `/api/identity/users/{uuid}/linked-users` | GET | ⚠️ Requer Auth | ❌ Não testado |
| J3.3 | `/api/identity/users/{uuid}/role` | PUT | ⚠️ Requer Auth | ❌ Não testado |
| J3.4 | `/api/identity/users/{uuid}/suspend` | POST | ⚠️ Requer Auth | ❌ Não testado |
| J3.5 | `/api/identity/users/{uuid}/remove-from-entity` | DELETE | ⚠️ Requer Auth | ❌ Não testado |

### **Auth Service**

| Jornada | Endpoint | Método | Status Implementação | Status Teste |
|---------|----------|--------|---------------------|--------------|
| J1.2, J1.3, J2.3 | `/api/auth/login` | POST | ✅ Implementado | ✅ Testado |
| J1.1 | `/api/auth/social-login` | POST | ❌ Não implementado | ❌ Não testado |
| J4.3 | `/api/auth/sso/login` | POST | ✅ Implementado | ❌ Não testado |
| J4.2 | `/api/auth/sso/test` | POST | ⚠️ Desconhecido | ❌ Não testado |
| J1.10, JT.4 | `/api/auth/logout` | POST | ✅ Implementado | ✅ Testado |
| J1.1, J1.5, J1.6, J1.8, J1.9, J1.11, JT.2, JT.3 | `/api/auth/otp/request` | POST | ❌ Não implementado | ❌ Não testado |
| J1.1, J1.5, J1.6, J1.8, J1.9, J1.11, JT.2, JT.3 | `/api/auth/otp/validate` | POST | ❌ Não implementado | ❌ Não testado |
| JT.1 | `/api/auth/token/validate` | POST | ✅ Implementado | ❌ Não testado |
| JT.1 | `/api/auth/token/refresh` | POST | ⚠️ Desconhecido | ❌ Não testado |
| J4.8 | `/api/auth/tokens/revoke` | POST | ⚠️ Desconhecido | ❌ Não testado |
| J1.8, J3.4, J4.8 | `/api/auth/tokens/revoke-all/{userUuid}` | POST | ⚠️ Desconhecido | ❌ Não testado |
| J1.6 | `/api/auth/password/reset` | POST | ⚠️ Desconhecido | ❌ Não testado |
| J1.7 | `/api/auth/password/change` | POST | ⚠️ Desconhecido | ❌ Não testado |
| J2.2 | `/api/auth/mfa/enable` | POST | ❌ Não implementado | ❌ Não testado |
| J2.3, J2.6, J3.6 | `/api/auth/mfa/verify` | POST | ❌ Não implementado | ❌ Não testado |
| J4.4 | `/api/auth/api-keys/generate` | POST | ✅ Implementado | ❌ Não testado |
| J4.4 | `/api/auth/api-keys/validate` | POST | ✅ Implementado | ❌ Não testado |
| J4.4 | `/api/auth/api-keys/revoke` | POST | ⚠️ Desconhecido | ❌ Não testado |
| J4.4 | `/api/auth/api-keys/{userUuid}` | GET | ⚠️ Desconhecido | ❌ Não testado |
| J2.5, J2.6, JT.4 | `/api/auth/sessions/{userUuid}` | GET | ✅ Implementado | ❌ Não testado |
| J2.5, J2.6 | `/api/auth/sessions/revoke/{sessionId}` | POST | ✅ Implementado | ❌ Não testado |
| J2.6, JT.4 | `/api/auth/sessions/revoke-all` | POST | ✅ Implementado | ❌ Não testado |
| J2.4 | `/api/auth/history/{userUuid}` | GET | ⚠️ Desconhecido | ❌ Não testado |
| J4.7, J4.8 | `/api/auth/audit/tokens/{userUuid}` | GET | ✅ Implementado | ❌ Não testado |
| J4.7 | `/api/auth/audit/accesses/{userUuid}` | GET | ⚠️ Desconhecido | ❌ Não testado |
| J4.7 | `/api/auth/audit/api-keys/{userUuid}` | GET | ⚠️ Desconhecido | ❌ Não testado |
| J4.6 | `/api/auth/sso/sessions/{legalEntityUuid}` | GET | ⚠️ Desconhecido | ❌ Não testado |
| J4.6 | `/api/auth/sso/sessions/{sessionId}` | GET | ⚠️ Desconhecido | ❌ Não testado |
| J4.6 | `/api/auth/sso/sessions/{sessionId}/revoke` | POST | ⚠️ Desconhecido | ❌ Não testado |
| J4.6 | `/api/auth/sso/sessions/revoke-all/{legalEntityUuid}` | POST | ⚠️ Desconhecido | ❌ Não testado |

### **User Profile Service**

| Jornada | Endpoint | Método | Status Implementação | Status Teste |
|---------|----------|--------|---------------------|--------------|
| J1.4 | `/api/profile/{uuid}` | GET | ✅ Implementado | ❌ Não testado |
| J1.4 | `/api/profile/{uuid}` | PUT | ✅ Implementado | ❌ Não testado |
| J2.5 | `/api/profile/{uuid}/security` | GET | ✅ Implementado | ❌ Não testado |
| J2.5 | `/api/profile/{uuid}/devices` | GET | ⚠️ Desconhecido | ❌ Não testado |

---

## 🎯 Recomendações de Priorização

### **Fase 1: Jornadas Críticas do Segmento 1** (Alta Prioridade)
1. ✅ J1.1: Registro e Onboarding (já parcial)
2. ✅ J1.2: Primeiro Login (já parcial)
3. 🔴 J1.3: Login Recorrente
4. 🔴 J1.4: Atualização de Perfil
5. 🔴 J1.7: Alteração de Senha
6. ✅ J1.10: Logout (já implementado)

### **Fase 2: Jornadas de Segurança e LGPD** (Alta Prioridade)
1. 🔴 J1.5: Alteração de Dados Pessoais
2. 🔴 J1.8: Desativação de Conta
3. 🔴 J1.9: Reativação de Conta
4. 🔴 J1.11: Verificação de Email/Telefone

### **Fase 3: Jornadas B2B** (Média Prioridade)
1. ⚠️ J3.1: Registro de Entidade Jurídica (já parcial)
2. 🔴 J3.2: Processo de Convite
3. 🔴 J3.3: Alteração de Role
4. 🔴 J3.4: Suspensão de Usuário

### **Fase 4: Jornadas de Segurança Avançada** (Média Prioridade)
1. 🔴 J2.1: Registro com Validação CPF
2. 🔴 J2.2: Ativação de MFA
3. 🔴 J2.3: Login com MFA
4. 🔴 J2.7: Upgrade para Profissional

### **Fase 5: Jornadas Enterprise** (Baixa Prioridade)
1. 🔴 J4.1: Registro e Validação Completa
2. 🔴 J4.3: Login via SSO
3. 🔴 J4.4: Geração de API Keys

---

## 📝 Legenda

- ✅ **Testado** - Teste E2E completo implementado
- ⚠️ **Parcial** - Teste parcial ou com limitações
- ❌ **Não Testado** - Nenhum teste E2E implementado
- 🔴 **Alta Prioridade** - Jornada crítica para o negócio
- 🟡 **Média Prioridade** - Jornada importante mas não crítica
- 🟢 **Baixa Prioridade** - Jornada menos frequente

---

**Última atualização**: 2025-11-14  
**Baseado em**: `BU-Identity/ETA/JORNADAS_CLIENTES.md` (55 jornadas documentadas)

