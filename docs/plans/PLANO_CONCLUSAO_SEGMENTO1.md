# Plano de Conclusão: Segmento 1 com OTP Implementado

## 📊 Status Atual

### Segmento 1: Compradores Ocasionais (B2C)
- **Cenários Implementados**: 18/27 (67%)
- **Cenários Bloqueados por OTP**: 11/27 (41%) ✅ **AGORA PODEM SER IMPLEMENTADOS**
- **Cenários Não Implementados (sem OTP)**: 0/27 (0%)

---

## ✅ Cenários que Podem Ser Concluídos Agora (com OTP)

### 1. Recuperação de Senha (`password_recovery.feature`) - **1 cenário**
**Status**: ✅ OTP implementado - Pronto para implementar
**Prioridade**: 🔴 **ALTA**

**Cenário**:
- Recuperação de senha bem-sucedida com OTP

**Ações Necessárias**:
- [ ] Implementar endpoint `POST /api/v1/auth/password/recover` (solicita OTP)
- [ ] Implementar endpoint `POST /api/v1/auth/password/reset` (redefine senha com OTP)
- [ ] Criar step definitions para recuperação de senha
- [ ] Remover tag `@not_implemented @otp_required` do cenário

---

### 2. Alteração de Dados Pessoais (`personal_data_update.feature`) - **3 cenários**
**Status**: ✅ OTP implementado - Pronto para implementar
**Prioridade**: 🔴 **ALTA**

**Cenários**:
1. Alteração de email bem-sucedida com validação OTP
2. Alteração de telefone bem-sucedida com validação OTP
3. Alteração de email falha com OTP inválido

**Ações Necessárias**:
- [ ] Modificar endpoint `PUT /api/v1/identity/users/{uuid}` para:
  - Quando email/telefone é alterado, gerar OTP e retornar `otpId`
  - Criar endpoint `PUT /api/v1/identity/users/{uuid}/confirm-update` que valida OTP e aplica alteração
- [ ] Criar step definitions para fluxo de alteração com OTP
- [ ] Remover tags `@not_implemented @otp_required` dos cenários

---

### 3. Desativação de Conta (`account_deactivation.feature`) - **2 cenários**
**Status**: ✅ OTP implementado - Pronto para implementar
**Prioridade**: 🟡 **MÉDIA**

**Cenários**:
1. Desativação de conta bem-sucedida com confirmação OTP
2. Desativação de conta falha sem confirmação OTP

**Ações Necessárias**:
- [ ] Modificar endpoint `DELETE /api/v1/identity/users/{uuid}` para:
  - Requerer `otpUuid` e `otpCode` no body
  - Validar OTP antes de desativar
- [ ] Criar step definitions para desativação com OTP
- [ ] Remover tags `@not_implemented @otp_required` dos cenários

---

### 4. Reativação de Conta (`account_reactivation.feature`) - **2 cenários**
**Status**: ✅ OTP implementado - Pronto para implementar
**Prioridade**: 🟡 **MÉDIA**

**Cenários**:
1. Reativação de conta bem-sucedida
2. Reativação de conta falha com OTP inválido

**Ações Necessárias**:
- [ ] Implementar endpoint `POST /api/v1/identity/users/{uuid}/reactivate`:
  - Gerar OTP e enviar para email/telefone cadastrado
  - Criar endpoint `POST /api/v1/identity/users/{uuid}/confirm-reactivate` que valida OTP e reativa
- [ ] Implementar lógica de reativação no Identity Service
- [ ] Sincronizar com Auth Service via evento `user.reactivated`
- [ ] Criar step definitions para reativação com OTP
- [ ] Remover tags `@not_implemented @otp_required` dos cenários

---

### 5. Verificação de Email/Telefone (`email_phone_verification.feature`) - **3 cenários**
**Status**: ✅ OTP implementado - Pronto para implementar
**Prioridade**: 🟡 **MÉDIA**

**Cenários**:
1. Verificação de email bem-sucedida
2. Verificação de telefone bem-sucedida
3. Verificação falha com OTP inválido

**Ações Necessárias**:
- [ ] Implementar endpoint `POST /api/v1/identity/users/{uuid}/verify-email`:
  - Gera OTP e envia para email cadastrado
  - Retorna `otpId`
- [ ] Implementar endpoint `POST /api/v1/identity/users/{uuid}/verify-phone`:
  - Gera OTP e envia para telefone cadastrado via WhatsApp
  - Retorna `otpId`
- [ ] Implementar endpoint `POST /api/v1/identity/users/{uuid}/confirm-verification`:
  - Valida OTP e atualiza `email_verified` ou `phone_verified`
- [ ] Adicionar campos `email_verified` e `phone_verified` ao modelo User (se não existirem)
- [ ] Criar step definitions para verificação
- [ ] Remover tag `@not_implemented @otp_required` da feature

---

## 📈 Progresso Esperado

### Após Implementação Completa
- **Cenários Implementados**: 18 + 11 = **29/27 (107%)** ✅
  - Nota: Alguns cenários podem ser consolidados ou ajustados

---

## 🚀 Plano de Execução Priorizado

### Fase 1: Alta Prioridade (Imediato)
1. **Recuperação de Senha** (1 cenário)
   - Impacto: Alto (funcionalidade crítica)
   - Complexidade: Média
   - Tempo estimado: 2-3 dias

2. **Alteração de Dados Pessoais** (3 cenários)
   - Impacto: Alto (funcionalidade crítica)
   - Complexidade: Média-Alta
   - Tempo estimado: 3-4 dias

### Fase 2: Média Prioridade
3. **Desativação de Conta** (2 cenários)
   - Impacto: Médio
   - Complexidade: Média
   - Tempo estimado: 2-3 dias

4. **Reativação de Conta** (2 cenários)
   - Impacto: Médio
   - Complexidade: Média-Alta
   - Tempo estimado: 3-4 dias

5. **Verificação de Email/Telefone** (3 cenários)
   - Impacto: Médio
   - Complexidade: Baixa-Média
   - Tempo estimado: 2-3 dias

---

## 📝 Notas Importantes

1. **OTP já está implementado**: ✅ Todos os endpoints OTP estão funcionando
2. **Eventos necessários**: Alguns eventos podem precisar ser criados (`identity.updated`, `user.reactivated`, `identity.email.verified`, `identity.phone.verified`)
3. **Sincronização**: Alterações no Identity Service precisam sincronizar com Auth Service via eventos
4. **Testes E2E**: Todos os step definitions já estão preparados para usar OTP

---

## ✅ Checklist de Conclusão

### Recuperação de Senha
- [ ] Endpoint `POST /api/v1/auth/password/recover` implementado
- [ ] Endpoint `POST /api/v1/auth/password/reset` implementado
- [ ] Step definitions criados
- [ ] Testes E2E passando
- [ ] Tag `@not_implemented @otp_required` removida

### Alteração de Dados Pessoais
- [ ] Fluxo de alteração com OTP implementado
- [ ] Endpoint de confirmação criado
- [ ] Step definitions criados
- [ ] Testes E2E passando
- [ ] Tags `@not_implemented @otp_required` removidas

### Desativação de Conta
- [ ] Validação OTP adicionada ao endpoint de desativação
- [ ] Step definitions criados
- [ ] Testes E2E passando
- [ ] Tags `@not_implemented @otp_required` removidas

### Reativação de Conta
- [ ] Endpoint de reativação implementado
- [ ] Fluxo com OTP implementado
- [ ] Sincronização com Auth Service via eventos
- [ ] Step definitions criados
- [ ] Testes E2E passando
- [ ] Tags `@not_implemented @otp_required` removidas

### Verificação de Email/Telefone
- [ ] Endpoints de verificação implementados
- [ ] Campos `email_verified` e `phone_verified` adicionados
- [ ] Step definitions criados
- [ ] Testes E2E passando
- [ ] Tag `@not_implemented @otp_required` removida

---

**Data de Criação**: 2025-11-17  
**Status**: ✅ OTP Implementado - Pronto para Conclusão  
**Próxima Ação**: Implementar recuperação de senha (Fase 1)

