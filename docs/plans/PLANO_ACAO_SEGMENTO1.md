# Plano de Ação: Completar Testes do Segmento 1

## 📊 Status Atual

### Segmento 1: Compradores Ocasionais (B2C)
- **Cenários Implementados**: 18/27 (67%)
- **Cenários Não Implementados**: 9/27 (33%)
- **Cenários Parciais**: 5/27 (19%)

---

## 🎯 Objetivo

Completar todos os cenários previstos para o Segmento 1 antes de replicar para os demais segmentos.

---

## 📋 Cenários Faltantes do Segmento 1

### 1. ✅ Alteração de Senha (`password_change.feature`) - **4 cenários**
**Status**: Endpoint pode estar implementado, precisa verificar
**Prioridade**: 🔴 **ALTA** (não depende de OTP)
**Dependências**: 
- Verificar se endpoint `POST /api/v1/auth/password/change` existe
- Se não existir, implementar endpoint no Auth Service

**Cenários**:
1. Alteração de senha bem-sucedida
2. Alteração de senha falha com senha atual incorreta
3. Alteração de senha falha com senha nova não atende complexidade
4. Alteração de senha com confirmação OTP (recomendado) - ⚠️ Requer OTP

**Ações**:
- [ ] Verificar se endpoint existe no Auth Service
- [ ] Se existir, implementar step definitions
- [ ] Se não existir, criar endpoint `POST /api/v1/auth/password/change`
- [ ] Implementar validação de senha atual
- [ ] Implementar validação de complexidade de senha nova
- [ ] Implementar testes E2E

---

### 2. ⚠️ Recuperação de Senha (`password_recovery.feature`) - **1 cenário**
**Status**: Requer OTP (não implementado)
**Prioridade**: 🟡 **MÉDIA** (bloqueado por OTP)
**Dependências**: 
- Implementar OTP no Auth Service
- Endpoint `POST /api/v1/auth/password/recover`
- Endpoint `POST /api/v1/auth/password/reset`

**Cenários**:
1. Recuperação de senha bem-sucedida com OTP - ⚠️ Requer OTP

**Ações**:
- [ ] Aguardar implementação de OTP
- [ ] Implementar endpoints de recuperação de senha
- [ ] Implementar testes E2E

---

### 3. ⚠️ Alteração de Dados Pessoais (`personal_data_update.feature`) - **5 cenários**
**Status**: Requer OTP para validação
**Prioridade**: 🟡 **MÉDIA** (bloqueado por OTP)
**Dependências**: 
- Endpoint `PUT /api/v1/identity/users/{uuid}` já existe ✅
- Implementar validação OTP para mudança de email/telefone
- Implementar evento `identity.updated`

**Cenários**:
1. Alteração de email bem-sucedida com validação OTP - ⚠️ Requer OTP
2. Alteração de telefone bem-sucedida com validação OTP - ⚠️ Requer OTP
3. Alteração de email falha com email já existente - ✅ Pode implementar sem OTP
4. Tentativa de alterar CPF - ✅ Pode implementar sem OTP
5. Alteração de email falha com OTP inválido - ⚠️ Requer OTP

**Ações**:
- [ ] Implementar validação de CPF imutável (cenário 4)
- [ ] Implementar validação de email duplicado (cenário 3)
- [ ] Aguardar implementação de OTP para cenários 1, 2, 5
- [ ] Implementar evento `identity.updated` quando dados forem atualizados
- [ ] Implementar sincronização no Auth Service via evento

---

### 4. ✅ Desativação de Conta (`account_deactivation.feature`) - **3 cenários**
**Status**: Endpoint existe (`DELETE /api/v1/identity/users/{uuid}` ou `POST /api/v1/identity/users/{uuid}/deactivate`)
**Prioridade**: 🔴 **ALTA** (não depende totalmente de OTP)
**Dependências**: 
- Endpoint `deactivateUser` já existe ✅
- Implementar revogação de tokens e suspensão de credenciais
- Implementar evento `user.deactivated`

**Cenários**:
1. Desativação de conta bem-sucedida com confirmação OTP - ⚠️ Requer OTP
2. Desativação de conta falha sem confirmação OTP - ⚠️ Requer OTP
3. Dados são mantidos após desativação (LGPD) - ✅ Pode implementar sem OTP

**Ações**:
- [ ] Verificar endpoint exato de desativação
- [ ] Implementar versão simplificada sem OTP (cenário 3)
- [ ] Implementar revogação de tokens no Auth Service
- [ ] Implementar suspensão de credenciais
- [ ] Implementar evento `user.deactivated`
- [ ] Aguardar OTP para cenários 1 e 2

---

### 5. ⚠️ Reativação de Conta (`account_reactivation.feature`) - **3 cenários**
**Status**: Endpoint existe mas retorna 501 (NOT_IMPLEMENTED)
**Prioridade**: 🟡 **MÉDIA** (bloqueado por implementação)
**Dependências**: 
- Endpoint `POST /api/v1/identity/users/{uuid}/reactivate` existe mas não implementado
- Implementar lógica de reativação
- Implementar evento `user.reactivated`
- Requer OTP para confirmação

**Cenários**:
1. Reativação de conta bem-sucedida - ⚠️ Requer OTP + implementação
2. Reativação de conta falha com OTP inválido - ⚠️ Requer OTP
3. Histórico é preservado após reativação - ✅ Pode implementar sem OTP

**Ações**:
- [ ] Implementar endpoint `reactivate` no Identity Service
- [ ] Implementar reativação de credenciais no Auth Service
- [ ] Implementar evento `user.reactivated`
- [ ] Implementar versão simplificada sem OTP (cenário 3)
- [ ] Aguardar OTP para cenários 1 e 2

---

### 6. ⚠️ Verificação de Email e Telefone (`email_phone_verification.feature`) - **3 cenários**
**Status**: Endpoints existem mas retornam 501 (NOT_IMPLEMENTED)
**Prioridade**: 🟡 **MÉDIA** (bloqueado por implementação + OTP)
**Dependências**: 
- Endpoints `POST /api/v1/identity/users/{uuid}/verify-email` e `verify-phone` existem mas não implementados
- Implementar lógica de verificação
- Requer OTP

**Cenários**:
1. Verificação de email bem-sucedida - ⚠️ Requer OTP + implementação
2. Verificação de telefone bem-sucedida - ⚠️ Requer OTP + implementação
3. Verificação falha com OTP inválido - ⚠️ Requer OTP

**Ações**:
- [ ] Implementar endpoints `verifyEmail` e `verifyPhone` no Identity Service
- [ ] Implementar campos `email_verified` e `phone_verified` no modelo User
- [ ] Implementar eventos `identity.email.verified` e `identity.phone.verified`
- [ ] Aguardar OTP para todos os cenários

---

## 🚀 Plano de Execução Priorizado

### Fase 1: Implementações Sem Dependência de OTP (Imediato)

#### 1.1 Alteração de Senha (Sem OTP)
- **Prazo**: 1-2 dias
- **Ações**:
  1. Verificar se endpoint `POST /api/v1/auth/password/change` existe
  2. Se não existir, implementar no Auth Service
  3. Implementar validação de senha atual
  4. Implementar validação de complexidade
  5. Implementar step definitions
  6. Implementar testes E2E (3 cenários sem OTP)

#### 1.2 Alteração de Dados Pessoais (Parcial)
- **Prazo**: 1 dia
- **Ações**:
  1. Implementar validação de CPF imutável
  2. Implementar validação de email duplicado
  3. Implementar step definitions para cenários sem OTP
  4. Implementar testes E2E (2 cenários)

#### 1.3 Desativação de Conta (Parcial)
- **Prazo**: 1-2 dias
- **Ações**:
  1. Verificar endpoint exato de desativação
  2. Implementar revogação de tokens
  3. Implementar suspensão de credenciais
  4. Implementar evento `user.deactivated`
  5. Implementar step definitions
  6. Implementar teste E2E (1 cenário sem OTP)

**Total Fase 1**: ~6 cenários implementados

---

### Fase 2: Implementações Parcialmente Bloqueadas (Após Fase 1)

#### 2.1 Reativação de Conta (Parcial)
- **Prazo**: 2-3 dias
- **Ações**:
  1. Implementar endpoint `reactivate` no Identity Service
  2. Implementar reativação de credenciais no Auth Service
  3. Implementar evento `user.reactivated`
  4. Implementar step definitions
  5. Implementar teste E2E (1 cenário sem OTP)

#### 2.2 Verificação de Email/Telefone (Implementação Base)
- **Prazo**: 2-3 dias
- **Ações**:
  1. Implementar endpoints `verifyEmail` e `verifyPhone`
  2. Adicionar campos `email_verified` e `phone_verified` ao modelo User
  3. Implementar eventos `identity.email.verified` e `identity.phone.verified`
  4. Preparar estrutura para OTP (quando implementado)

**Total Fase 2**: ~1 cenário implementado + estrutura para OTP

---

### Fase 3: Aguardar Implementação de OTP

#### 3.1 Funcionalidades Bloqueadas por OTP
- Recuperação de Senha (1 cenário)
- Alteração de Dados Pessoais com OTP (3 cenários)
- Desativação de Conta com OTP (2 cenários)
- Reativação de Conta com OTP (2 cenários)
- Verificação de Email/Telefone (3 cenários)

**Total Fase 3**: ~11 cenários aguardando OTP

---

## 📈 Progresso Esperado

### Após Fase 1
- **Cenários Implementados**: 18 + 6 = **24/27 (89%)**
- **Cenários Não Implementados**: 9 - 6 = **3/27 (11%)**

### Após Fase 2
- **Cenários Implementados**: 24 + 1 = **25/27 (93%)**
- **Cenários Não Implementados**: 3 - 1 = **2/27 (7%)**

### Após Fase 3 (com OTP)
- **Cenários Implementados**: 25 + 11 = **36/27 (133%)** ⚠️
  - Nota: Alguns cenários podem ser duplicados ou consolidados

---

## 🎯 Próximos Passos Imediatos

1. **Verificar endpoints existentes**:
   - [ ] `POST /api/v1/auth/password/change` existe?
   - [ ] `DELETE /api/v1/identity/users/{uuid}` ou `POST /api/v1/identity/users/{uuid}/deactivate`?
   - [ ] `POST /api/v1/identity/users/{uuid}/reactivate` implementado?

2. **Implementar testes sem OTP**:
   - [ ] Alteração de senha (3 cenários)
   - [ ] Alteração de dados pessoais - validações (2 cenários)
   - [ ] Desativação de conta - LGPD (1 cenário)

3. **Implementar funcionalidades faltantes**:
   - [ ] Endpoint de alteração de senha (se não existir)
   - [ ] Endpoint de reativação (implementar lógica)
   - [ ] Endpoints de verificação (implementar lógica)

---

## 📝 Notas Importantes

1. **OTP é Bloqueador**: 11 cenários dependem de OTP que não está implementado
2. **Versões Simplificadas**: Alguns cenários podem ser implementados sem OTP inicialmente
3. **Eventos Faltantes**: Alguns eventos precisam ser implementados (`identity.updated`, `user.deactivated`, `user.reactivated`)
4. **Sincronização**: Alterações no Identity Service precisam sincronizar com Auth Service via eventos

---

**Data de Criação**: 2025-11-17  
**Próxima Revisão**: Após conclusão da Fase 1

