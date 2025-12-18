# 🚀 Plano de Implementação - Features dos Microserviços BU Identity

**Data de Criação**: 2025-11-18  
**Objetivo**: Implementar as 48 jornadas pendentes (87% do total) nos microserviços da BU Identity  
**Prazo Estimado**: 6-8 meses (considerando equipe dedicada)

---

## 📊 Situação Atual

### **Estatísticas**
- ✅ **Features Gherkin**: 55/55 criadas (100%)
- ✅ **Step Definitions**: 7/55 implementados (13%)
- ❌ **Pendentes**: 48 jornadas (87%)
- ⚠️ **Cenários Executáveis**: 27/120 (23%)

### **Distribuição por Segmento**

| Segmento | Features | Implementadas | Pendentes | Prioridade |
|----------|----------|---------------|-----------|------------|
| **Segmento 1** | 11 | 6 (55%) | 5 (45%) | 🔴 Crítica |
| **Segmento 2** | 7 | 0 (0%) | 7 (100%) | 🟡 Alta |
| **Segmento 3** | 7 | 1 (14%) | 6 (86%) | 🟡 Alta |
| **Segmento 4** | 8 | 0 (0%) | 8 (100%) | 🟢 Média |
| **Transversais** | 1 | 0 (0%) | 1 (100%) | 🟢 Baixa |

---

## 🎯 Objetivos Estratégicos

### **Curto Prazo (1-2 meses)**
1. ✅ Completar Segmento 1 (100% de cobertura)
2. ✅ Resolver dependências críticas (OTP, Admin Auth)
3. ✅ Implementar Segmento 2 básico (validação CPF, MFA)

### **Médio Prazo (3-4 meses)**
1. ✅ Implementar Segmento 3 completo (B2B)
2. ✅ Implementar Segmento 2 completo
3. ✅ Implementar funcionalidades transversais

### **Longo Prazo (5-6 meses)**
1. ✅ Implementar Segmento 4 (Enterprise)
2. ✅ Completar todas as jornadas
3. ✅ Alcançar 100% de cobertura de testes E2E

---

## 🔴 Dependências Críticas

### **1. Serviço OTP (Prioridade CRÍTICA)**
**Impacto**: Bloqueia 20+ cenários em múltiplos segmentos

**Jornadas Afetadas**:
- Segmento 1: J1.1, J1.5, J1.6, J1.8, J1.9, J1.11 (14 cenários)
- Segmento 2: J2.2, J2.3, J2.6 (8 cenários)
- Segmento 3: J3.5, J3.6, J3.7 (3 cenários)

**Ação Requerida**:
- [ ] Implementar serviço OTP ou integrar serviço externo
- [ ] Criar endpoints `/api/auth/otp/request` e `/api/auth/otp/validate`
- [ ] Implementar integração com WhatsApp e Email
- [ ] Criar testes unitários e de integração

**Responsável**: Equipe Auth Service  
**Prazo**: 2-3 semanas  
**Bloqueia**: Fase 1 e Fase 2

---

### **2. Autenticação Admin/B2B (Prioridade ALTA)**
**Impacto**: Bloqueia todas as jornadas B2B

**Jornadas Afetadas**:
- Segmento 3: J3.2, J3.3, J3.4, J3.5, J3.6, J3.7 (21 cenários)
- Segmento 4: Todas as jornadas (30 cenários)

**Ação Requerida**:
- [ ] Implementar sistema de roles (ADMIN, OPERATOR, TECHNICAL)
- [ ] Criar middleware de autorização baseado em roles
- [ ] Implementar validação de escopo B2B no JWT
- [ ] Criar testes de autorização

**Responsável**: Equipe Auth Service + Identity Service  
**Prazo**: 3-4 semanas  
**Bloqueia**: Fase 3 e Fase 4

---

### **3. Validação CPF Externa (Prioridade MÉDIA)**
**Impacto**: Bloqueia 2 jornadas do Segmento 2

**Jornadas Afetadas**:
- J2.1: Registro com Validação CPF
- J2.7: Upgrade para Profissional

**Ação Requerida**:
- [ ] Integrar serviço externo de validação CPF (ex: ReceitaWS, Serpro)
- [ ] Criar endpoint `/api/identity/users/{uuid}/validate-cpf`
- [ ] Implementar cache de validações
- [ ] Criar testes de integração

**Responsável**: Equipe Identity Service  
**Prazo**: 1-2 semanas  
**Bloqueia**: Fase 2 (parcial)

---

### **4. Infraestrutura SSO (Prioridade MÉDIA)**
**Impacto**: Bloqueia 4 jornadas do Segmento 4

**Jornadas Afetadas**:
- J4.2: Configuração SSO
- J4.3: Login SSO
- J4.5: Rotação Certificados SSO
- J4.6: Gestão Sessões SSO

**Ação Requerida**:
- [ ] Implementar suporte a SAML 2.0
- [ ] Implementar suporte a OAuth2/OIDC
- [ ] Criar endpoints de configuração SSO
- [ ] Implementar gestão de certificados
- [ ] Criar testes de integração SSO

**Responsável**: Equipe Auth Service  
**Prazo**: 4-6 semanas  
**Bloqueia**: Fase 4

---

## 📅 Fases de Implementação

---

## 🟢 FASE 1: Completar Segmento 1 (Semanas 1-4)

**Objetivo**: Alcançar 100% de cobertura no Segmento 1  
**Prioridade**: 🔴 CRÍTICA  
**Dependências**: Serviço OTP

### **Sprint 1.1: Resolver Dependências Críticas (Semana 1-2)**

#### **Tarefas Auth Service**
- [ ] **OTP Service - Implementação Base**
  - Criar entidade `OtpRequest` e `OtpValidation`
  - Implementar geração de código OTP (6 dígitos, expiração 5 min)
  - Criar endpoints:
    - `POST /api/auth/otp/request` (tipo: EMAIL, WHATSAPP, SMS)
    - `POST /api/auth/otp/validate`
  - Implementar integração com serviço de mensageria (WhatsApp/Email)
  - Criar testes unitários
  - **Estimativa**: 1 semana

- [ ] **Integração Mensageria**
  - Integrar com serviço WhatsApp (ex: Twilio, Z-API)
  - Integrar com serviço Email (ex: SendGrid, AWS SES)
  - Implementar retry e tratamento de erros
  - Criar testes de integração
  - **Estimativa**: 1 semana

#### **Tarefas Identity Service**
- [ ] **Verificação Email/Telefone**
  - Criar endpoint `POST /api/identity/users/{uuid}/verify-email`
  - Criar endpoint `POST /api/identity/users/{uuid}/verify-phone`
  - Implementar atualização de flags `email_verified` e `phone_verified`
  - Publicar eventos `identity.email.verified` e `identity.phone.verified`
  - Criar testes unitários e de integração
  - **Estimativa**: 3 dias

#### **Tarefas User Profile Service**
- [ ] **Nenhuma tarefa específica nesta sprint**

**Entregáveis**:
- ✅ Serviço OTP funcional
- ✅ Endpoints de verificação implementados
- ✅ Testes unitários e de integração passando

---

### **Sprint 1.2: Completar Jornadas Pendentes (Semana 3-4)**

#### **Tarefas Identity Service**
- [ ] **J1.5 - Alteração de Dados Pessoais**
  - Implementar endpoint `PUT /api/identity/users/{uuid}` completo
  - Adicionar validação de email único
  - Implementar proteção contra alteração de CPF
  - Integrar com OTP para alteração de email/telefone
  - Publicar evento `identity.updated`
  - Criar testes E2E
  - **Estimativa**: 3 dias

- [ ] **J1.8 - Desativação de Conta**
  - Criar endpoint `POST /api/identity/users/{uuid}/deactivate`
  - Implementar soft delete (flag `isActive = false`)
  - Integrar com Auth Service para revogar tokens
  - Publicar evento `user.deactivated`
  - Criar testes E2E
  - **Estimativa**: 2 dias

- [ ] **J1.9 - Reativação de Conta**
  - Criar endpoint `POST /api/identity/users/{uuid}/reactivate`
  - Implementar validação de OTP
  - Reativar credenciais no Auth Service
  - Publicar evento `user.reactivated`
  - Criar testes E2E
  - **Estimativa**: 2 dias

#### **Tarefas Auth Service**
- [ ] **J1.6 - Recuperação de Senha**
  - Criar endpoint `POST /api/auth/password/reset`
  - Implementar fluxo: solicitação → OTP → reset
  - Integrar com Identity Service para validação
  - Criar testes E2E
  - **Estimativa**: 2 dias

- [ ] **J1.11 - Verificação Email/Telefone (Integração)**
  - Integrar endpoints de verificação com OTP
  - Criar testes E2E
  - **Estimativa**: 1 dia

#### **Tarefas User Profile Service**
- [ ] **Nenhuma tarefa específica nesta sprint**

**Entregáveis**:
- ✅ J1.5, J1.6, J1.8, J1.9, J1.11 implementados
- ✅ Testes E2E passando
- ✅ Segmento 1 com 100% de cobertura

**Critérios de Sucesso**:
- ✅ Todos os testes E2E do Segmento 1 passando
- ✅ Cobertura de 100% nas jornadas do Segmento 1
- ✅ Documentação atualizada

---

## 🟡 FASE 2: Implementar Segmento 2 (Semanas 5-8)

**Objetivo**: Implementar todas as jornadas do Segmento 2  
**Prioridade**: 🟡 ALTA  
**Dependências**: Validação CPF Externa, MFA

### **Sprint 2.1: Validação CPF e Upgrade (Semana 5-6)**

#### **Tarefas Identity Service**
- [ ] **J2.1 - Validação CPF**
  - Integrar serviço externo de validação CPF
  - Criar endpoint `POST /api/identity/users/{uuid}/validate-cpf`
  - Implementar cache de validações (evitar múltiplas chamadas)
  - Atualizar flag `cpf_validated` no perfil
  - Publicar evento `identity.cpf.validated`
  - Criar testes unitários e de integração
  - **Estimativa**: 1 semana

- [ ] **J2.7 - Upgrade para Profissional**
  - Criar endpoint `POST /api/identity/users/{uuid}/upgrade-to-professional`
  - Validar CPF antes de permitir upgrade
  - Atualizar segmento do usuário (B2C → B2C Professional)
  - Publicar evento `user.upgraded`
  - Criar testes E2E
  - **Estimativa**: 3 dias

**Entregáveis**:
- ✅ Validação CPF funcional
- ✅ Upgrade para profissional implementado
- ✅ Testes E2E passando

---

### **Sprint 2.2: MFA e Segurança (Semana 7-8)**

#### **Tarefas Auth Service**
- [ ] **J2.2 - Ativação de MFA**
  - Criar endpoint `POST /api/auth/mfa/enable`
  - Implementar geração de secret (TOTP)
  - Integrar com OTP para confirmação
  - Atualizar flag `mfa_enabled` no Identity Service
  - Publicar evento `mfa.enabled`
  - Criar testes unitários e E2E
  - **Estimativa**: 1 semana

- [ ] **J2.3 - Login com MFA**
  - Modificar endpoint `POST /api/auth/login` para suportar MFA
  - Implementar fluxo: credenciais → MFA code → JWT
  - Validar código MFA (TOTP)
  - Criar testes E2E
  - **Estimativa**: 3 dias

- [ ] **J2.6 - Logout Todos Dispositivos**
  - Criar endpoint `POST /api/auth/sessions/revoke-all`
  - Implementar validação OTP/MFA obrigatória
  - Revogar todas as sessões do usuário
  - Publicar evento `sessions.revoked-all`
  - Criar testes E2E
  - **Estimativa**: 2 dias

#### **Tarefas Auth Service + User Profile Service**
- [ ] **J2.4 - Histórico de Logins**
  - Criar endpoint `GET /api/auth/history/{userUuid}`
  - Implementar registro de tentativas de login
  - Adicionar filtros (data, status, IP)
  - Criar testes E2E
  - **Estimativa**: 3 dias

- [ ] **J2.5 - Gestão de Dispositivos**
  - Criar endpoint `GET /api/profile/{uuid}/devices`
  - Criar endpoint `POST /api/auth/sessions/revoke/{sessionId}`
  - Implementar rastreamento de dispositivos
  - Criar testes E2E
  - **Estimativa**: 3 dias

**Entregáveis**:
- ✅ MFA implementado e funcional
- ✅ Histórico de logins implementado
- ✅ Gestão de dispositivos implementada
- ✅ Testes E2E passando

**Critérios de Sucesso**:
- ✅ Todas as jornadas do Segmento 2 implementadas
- ✅ Testes E2E passando
- ✅ Documentação atualizada

---

## 🟡 FASE 3: Implementar Segmento 3 (Semanas 9-12)

**Objetivo**: Implementar todas as jornadas B2B do Segmento 3  
**Prioridade**: 🟡 ALTA  
**Dependências**: Autenticação Admin/B2B

### **Sprint 3.1: Autenticação e Autorização B2B (Semana 9-10)**

#### **Tarefas Auth Service**
- [ ] **Sistema de Roles**
  - Implementar enum de roles: `ADMIN`, `OPERATOR`, `TECHNICAL`
  - Adicionar claims de role no JWT
  - Criar middleware de autorização baseado em roles
  - Implementar validação de escopo B2B
  - Criar testes unitários
  - **Estimativa**: 1 semana

- [ ] **Validação de Escopo B2B**
  - Validar se usuário pertence a entidade jurídica
  - Validar se usuário tem permissão para ação
  - Implementar cache de permissões
  - Criar testes de integração
  - **Estimativa**: 3 dias

#### **Tarefas Identity Service**
- [ ] **Vinculação Usuário-Entidade**
  - Implementar relacionamento N:N entre User e LegalEntity
  - Criar tabela `user_legal_entity` (user_uuid, legal_entity_uuid, role)
  - Implementar queries de vinculação
  - Criar testes unitários
  - **Estimativa**: 3 dias

**Entregáveis**:
- ✅ Sistema de roles implementado
- ✅ Autorização B2B funcional
- ✅ Vinculação usuário-entidade implementada

---

### **Sprint 3.2: Gestão de Usuários B2B (Semana 11-12)**

#### **Tarefas Identity Service**
- [ ] **J3.2 - Processo de Convite**
  - Criar endpoint `POST /api/identity/legal-entities/{uuid}/invite`
  - Implementar criação de convite com token temporário
  - Validar domínio corporativo do email
  - Implementar expiração de convite (7 dias)
  - Publicar evento `entity.invite.created`
  - Criar testes E2E
  - **Estimativa**: 1 semana

- [ ] **J3.3 - Alteração de Role**
  - Criar endpoint `PUT /api/identity/users/{uuid}/role`
  - Implementar validação: não pode remover último admin
  - Atualizar role do usuário na entidade
  - Publicar evento `entity.user.role.changed`
  - Criar testes E2E
  - **Estimativa**: 2 dias

- [ ] **J3.4 - Suspensão de Usuário**
  - Criar endpoint `POST /api/identity/users/{uuid}/suspend`
  - Implementar validação de justificativa obrigatória
  - Integrar com Auth Service para revogar tokens
  - Publicar evento `entity.user.suspended`
  - Criar testes E2E
  - **Estimativa**: 2 dias

- [ ] **J3.5 - Remoção de Usuário**
  - Criar endpoint `DELETE /api/identity/users/{uuid}/remove-from-entity`
  - Implementar validação: não pode remover último admin
  - Implementar validação: não pode remover representante legal sem transferir
  - Requerer confirmação OTP/MFA
  - Publicar evento `entity.user.removed`
  - Criar testes E2E
  - **Estimativa**: 3 dias

- [ ] **J3.6 - Transferência de Representação**
  - Criar endpoint `POST /api/identity/legal-entities/{uuid}/transfer-representation`
  - Implementar validação: novo usuário deve ser ADMIN
  - Requerer confirmação de ambos os usuários (OTP/MFA)
  - Transferir representação legal
  - Publicar evento `entity.representation.transferred`
  - Criar testes E2E
  - **Estimativa**: 3 dias

- [ ] **J3.7 - Cancelamento de Entidade**
  - Criar endpoint `POST /api/identity/legal-entities/{uuid}/cancel`
  - Implementar validação de pendências
  - Suspender todos os usuários vinculados
  - Requerer confirmação explícita (OTP/MFA)
  - Publicar evento `entity.cancelled`
  - Criar testes E2E
  - **Estimativa**: 3 dias

**Entregáveis**:
- ✅ Todas as jornadas do Segmento 3 implementadas
- ✅ Testes E2E passando
- ✅ Documentação atualizada

**Critérios de Sucesso**:
- ✅ Todas as jornadas do Segmento 3 implementadas
- ✅ Testes E2E passando
- ✅ Autorização B2B funcionando corretamente

---

## 🟢 FASE 4: Implementar Segmento 4 (Semanas 13-18)

**Objetivo**: Implementar todas as jornadas Enterprise do Segmento 4  
**Prioridade**: 🟢 MÉDIA  
**Dependências**: Infraestrutura SSO, Autenticação Admin

### **Sprint 4.1: SSO e Infraestrutura (Semana 13-16)**

#### **Tarefas Auth Service**
- [ ] **J4.2 - Configuração SSO**
  - Implementar suporte a SAML 2.0
  - Implementar suporte a OAuth2/OIDC
  - Criar endpoint `POST /api/auth/sso/setup`
  - Criar endpoint `POST /api/auth/sso/test`
  - Validar metadados e certificados
  - Criar testes de integração
  - **Estimativa**: 2 semanas

- [ ] **J4.3 - Login SSO**
  - Criar endpoint `POST /api/auth/sso/login`
  - Implementar fluxo SAML (SP-initiated e IdP-initiated)
  - Implementar fluxo OAuth2/OIDC
  - Criar usuário automaticamente se não existir
  - Emitir JWT após autenticação SSO
  - Criar testes E2E
  - **Estimativa**: 1 semana

- [ ] **J4.5 - Rotação de Certificados**
  - Criar endpoint `POST /api/auth/sso/certificates/rotate`
  - Implementar validação de certificados
  - Implementar período de transição
  - Criar testes E2E
  - **Estimativa**: 3 dias

- [ ] **J4.6 - Gestão de Sessões SSO**
  - Criar endpoint `GET /api/auth/sso/sessions/{legalEntityUuid}`
  - Criar endpoint `GET /api/auth/sso/sessions/{sessionId}`
  - Criar endpoint `POST /api/auth/sso/sessions/{sessionId}/revoke`
  - Criar endpoint `POST /api/auth/sso/sessions/revoke-all/{legalEntityUuid}`
  - Implementar rastreamento de sessões SSO
  - Criar testes E2E
  - **Estimativa**: 1 semana

**Entregáveis**:
- ✅ SSO SAML e OAuth2 implementados
- ✅ Gestão de sessões SSO funcional
- ✅ Testes E2E passando

---

### **Sprint 4.2: API Keys e Auditoria (Semana 17-18)**

#### **Tarefas Auth Service**
- [ ] **J4.4 - API Keys**
  - Criar endpoint `POST /api/auth/api-keys/generate`
  - Criar endpoint `GET /api/auth/api-keys/{userUuid}`
  - Criar endpoint `POST /api/auth/api-keys/validate`
  - Criar endpoint `POST /api/auth/api-keys/revoke`
  - Implementar formato de key (ex: `pk_live_xxxxxxxxxxxxx`)
  - Implementar hash SHA-256 e prefixo
  - Implementar renovação de keys
  - Validar role TECHNICAL obrigatória
  - Criar testes E2E
  - **Estimativa**: 1 semana

- [ ] **J4.7 - Auditoria**
  - Criar endpoint `GET /api/auth/audit/tokens/{userUuid}`
  - Criar endpoint `GET /api/auth/audit/accesses/{userUuid}`
  - Criar endpoint `GET /api/auth/audit/api-keys/{userUuid}`
  - Implementar filtros e exportação
  - Criar testes E2E
  - **Estimativa**: 3 dias

- [ ] **J4.8 - Revogação de Tokens**
  - Criar endpoint `POST /api/auth/tokens/revoke`
  - Criar endpoint `POST /api/auth/tokens/revoke-all/{userUuid}`
  - Implementar revogação massiva
  - Criar testes E2E
  - **Estimativa**: 2 dias

#### **Tarefas Identity Service**
- [ ] **J4.1 - Registro Plataforma**
  - Criar endpoint `POST /api/identity/legal-entities` (com validação de parceria)
  - Criar endpoint `POST /api/identity/legal-entities/{uuid}/validate-domain`
  - Criar endpoint `POST /api/identity/legal-entities/{uuid}/validate-partnership`
  - Implementar validação de contrato de parceria
  - Criar testes E2E
  - **Estimativa**: 3 dias

**Entregáveis**:
- ✅ API Keys implementadas
- ✅ Auditoria implementada
- ✅ Revogação de tokens implementada
- ✅ Testes E2E passando

**Critérios de Sucesso**:
- ✅ Todas as jornadas do Segmento 4 implementadas
- ✅ Testes E2E passando
- ✅ SSO funcionando em produção

---

## 🟢 FASE 5: Funcionalidades Transversais (Semana 19-20)

**Objetivo**: Completar jornadas transversais  
**Prioridade**: 🟢 BAIXA

### **Sprint 5.1: Refresh Token e Outros (Semana 19-20)**

#### **Tarefas Auth Service**
- [ ] **JT.1 - Refresh Token**
  - Criar endpoint `POST /api/auth/token/refresh`
  - Implementar validação de token atual
  - Validar se usuário está ativo
  - Emitir novo JWT com claims atualizados
  - Implementar período de grace (5 minutos)
  - Criar testes E2E
  - **Estimativa**: 3 dias

- [ ] **JT.2/JT.3 - Verificação Email/Telefone Existente**
  - Verificar se já está coberto por J1.11
  - Se não, criar endpoints específicos
  - Criar testes E2E
  - **Estimativa**: 2 dias

- [ ] **JT.4 - Logout Todos Dispositivos**
  - Verificar se já está coberto por J2.6
  - Se não, criar endpoint específico
  - Criar testes E2E
  - **Estimativa**: 1 dia

**Entregáveis**:
- ✅ Refresh token implementado
- ✅ Todas as jornadas transversais completas
- ✅ Testes E2E passando

**Critérios de Sucesso**:
- ✅ Todas as jornadas transversais implementadas
- ✅ 100% de cobertura de testes E2E

---

## 📋 Responsabilidades por Microserviço

### **Identity Service**
**Responsável**: Equipe Identity Service  
**Jornadas**:
- Segmento 1: J1.5, J1.8, J1.9, J1.11
- Segmento 2: J2.1, J2.7
- Segmento 3: J3.1, J3.2, J3.3, J3.4, J3.5, J3.6, J3.7
- Segmento 4: J4.1

**Total**: 15 jornadas

---

### **Auth Service**
**Responsável**: Equipe Auth Service  
**Jornadas**:
- Segmento 1: J1.6, J1.7, J1.10
- Segmento 2: J2.2, J2.3, J2.4, J2.6
- Segmento 3: (suporte via roles e autorização)
- Segmento 4: J4.2, J4.3, J4.4, J4.5, J4.6, J4.7, J4.8
- Transversais: JT.1

**Total**: 18 jornadas

**Dependências Críticas**:
- Serviço OTP (Fase 1)
- Sistema de Roles (Fase 3)
- Infraestrutura SSO (Fase 4)

---

### **User Profile Service**
**Responsável**: Equipe User Profile Service  
**Jornadas**:
- Segmento 1: J1.4 (já implementado)
- Segmento 2: J2.5 (suporte)

**Total**: 2 jornadas (1 já implementada)

---

## ✅ Critérios de Sucesso por Fase

### **Fase 1 - Segmento 1**
- [ ] 100% das jornadas do Segmento 1 implementadas
- [ ] Todos os testes E2E passando
- [ ] Serviço OTP funcional em produção
- [ ] Documentação atualizada

### **Fase 2 - Segmento 2**
- [ ] 100% das jornadas do Segmento 2 implementadas
- [ ] Validação CPF funcional
- [ ] MFA implementado e testado
- [ ] Todos os testes E2E passando

### **Fase 3 - Segmento 3**
- [ ] 100% das jornadas do Segmento 3 implementadas
- [ ] Sistema de roles funcionando
- [ ] Autorização B2B implementada
- [ ] Todos os testes E2E passando

### **Fase 4 - Segmento 4**
- [ ] 100% das jornadas do Segmento 4 implementadas
- [ ] SSO funcionando em produção
- [ ] API Keys implementadas
- [ ] Auditoria completa
- [ ] Todos os testes E2E passando

### **Fase 5 - Transversais**
- [ ] 100% das jornadas transversais implementadas
- [ ] Refresh token funcionando
- [ ] Todos os testes E2E passando

---

## 📊 Métricas de Acompanhamento

### **KPIs Principais**
- **Cobertura de Features**: Meta 100% (atual: 13%)
- **Cobertura de Testes E2E**: Meta 100% (atual: 23%)
- **Taxa de Sucesso de Testes**: Meta >95%
- **Tempo de Resolução de Bugs**: Meta <2 dias

### **Métricas por Fase**
- **Velocidade de Implementação**: Features por sprint
- **Taxa de Defeitos**: Bugs encontrados vs. corrigidos
- **Cobertura de Código**: Meta >80% por microserviço

---

## 🚨 Riscos e Mitigações

### **Risco 1: Atraso no Serviço OTP**
**Probabilidade**: Média  
**Impacto**: Alto  
**Mitigação**: 
- Criar mock/stub para desenvolvimento
- Priorizar implementação na Fase 1
- Ter equipe dedicada

### **Risco 2: Complexidade da Infraestrutura SSO**
**Probabilidade**: Alta  
**Impacto**: Médio  
**Mitigação**:
- Usar bibliotecas prontas (ex: Spring Security SAML)
- Contratar consultoria especializada se necessário
- Fazer POC antes da implementação completa

### **Risco 3: Dependências Externas (Validação CPF)**
**Probabilidade**: Baixa  
**Impacto**: Médio  
**Mitigação**:
- Ter múltiplos provedores de backup
- Implementar cache agressivo
- Criar fallback para validação básica

### **Risco 4: Mudanças de Escopo**
**Probabilidade**: Média  
**Impacto**: Médio  
**Mitigação**:
- Revisar escopo semanalmente
- Manter backlog priorizado
- Comunicar mudanças imediatamente

---

## 📝 Próximos Passos Imediatos

### **Semana 1**
1. [ ] Revisar e aprovar este plano com stakeholders
2. [ ] Alocar equipes por microserviço
3. [ ] Criar issues/tickets no sistema de gestão
4. [ ] Iniciar Sprint 1.1 (Serviço OTP)

### **Semana 2**
1. [ ] Continuar implementação do Serviço OTP
2. [ ] Iniciar testes de integração
3. [ ] Preparar ambiente de desenvolvimento

### **Semana 3**
1. [ ] Finalizar Serviço OTP
2. [ ] Iniciar Sprint 1.2 (Completar Segmento 1)
3. [ ] Primeira revisão de progresso

---

## 📚 Documentação Relacionada

- [JOURNEYS_MAPPING.md](./JOURNEYS_MAPPING.md) - Mapeamento completo de jornadas
- [COBERTURA_COMPLETA_SEGMENTOS.md](./COBERTURA_COMPLETA_SEGMENTOS.md) - Análise de cobertura
- [SEGMENTO1_COBERTURA.md](./SEGMENTO1_COBERTURA.md) - Cobertura detalhada do Segmento 1
- [DEPENDENCIAS_EXTERNAS.md](./DEPENDENCIAS_EXTERNAS.md) - Dependências externas

---

**Última atualização**: 2025-11-18  
**Próxima revisão**: Semanal (toda segunda-feira)  
**Responsável**: Equipe BU Identity + BU QA

