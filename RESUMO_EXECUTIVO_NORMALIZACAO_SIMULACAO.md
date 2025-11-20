# Resumo Executivo: Normalização do Header `simulate-provider` no Platform Journey Tests

**Versão:** 1.0  
**Data:** 2025-01-27  
**Status:** 📋 Planejamento

---

## 🎯 Objetivo

Normalizar o `platform-journey-tests` para adicionar automaticamente o header `simulate-provider: true` em todas as requisições que geram mensagens transacionais, permitindo simular envio de mensagens aos providers em ambientes não-PROD (Local, SIT, UAT).

---

## 📊 Escopo

### Componentes Afetados

| Componente | Mudanças Necessárias | Prioridade |
|------------|---------------------|------------|
| **E2EConfiguration** | Adicionar lógica de simulação | 🔴 **ALTA** |
| **AuthServiceClient** | Adicionar header em `requestOtp()` | 🔴 **ALTA** |
| **IdentityServiceClient** | Adicionar header em `createUser()` | 🟡 **MÉDIA** |
| **application.yml** (todos) | Adicionar configuração | 🔴 **ALTA** |

### Endpoints Impactados

- ✅ `POST /api/v1/auth/otp/request` → Gera evento `otp.sent` (envio de OTP)
- ✅ `POST /api/v1/identity/users` → Gera evento `user.created.v1` (notificações futuras)

---

## 🔄 Estratégia

### Abordagem: Automática por Ambiente

**Decisão:** Header adicionado automaticamente baseado no ambiente:

- ✅ **Local/SIT/UAT:** Sempre adiciona `simulate-provider: true`
- ❌ **PROD:** Nunca adiciona (não deve executar testes em PROD)

**Vantagens:**
- ✅ Zero mudanças nos testes existentes
- ✅ Redução automática de custos
- ✅ Seguro (nunca simula em PROD)

---

## 📅 Timeline

| Fase | Atividades | Duração |
|------|------------|---------|
| **Fase 1** | Configuração (E2EConfiguration + YAML) | 1 dia |
| **Fase 2** | Atualizar Clients (Auth + Identity) | 1 dia |
| **Fase 3** | Documentação e Testes | 1 dia |
| **TOTAL** | | **3 dias úteis** |

---

## ✅ Principais Entregas

1. ✅ Configuração automática por ambiente
2. ✅ Header adicionado automaticamente nos clients
3. ✅ Zero mudanças necessárias nos testes existentes
4. ✅ Redução de custos em testes E2E
5. ✅ Documentação completa

---

## 🔐 Segurança

- ⚠️ **Nunca simular em PROD:** Validação no `E2EConfiguration`
- ✅ **Configurável:** Pode ser desabilitado via propriedade
- ✅ **Logs:** Registrar quando simulação está habilitada

---

## 📋 Checklist Rápido

### Configuração
- [ ] Atualizar `E2EConfiguration.java`
- [ ] Atualizar `application.yml` (todos os ambientes)

### Clients
- [ ] Atualizar `AuthServiceClient.requestOtp()`
- [ ] Atualizar `IdentityServiceClient.createUser()`

### Validação
- [ ] Testes unitários
- [ ] Testes E2E em local
- [ ] Testes E2E em SIT (se disponível)
- [ ] Documentação atualizada

---

## 📚 Documentação Completa

- 📄 **[PLANO_NORMALIZACAO_SIMULACAO_PROVIDERS.md](./PLANO_NORMALIZACAO_SIMULACAO_PROVIDERS.md)** - Plano detalhado de implementação
- 📋 **[RESUMO_EXECUTIVO_NORMALIZACAO_SIMULACAO.md](./RESUMO_EXECUTIVO_NORMALIZACAO_SIMULACAO.md)** - Este documento

---

## 🚀 Próximos Passos

1. ✅ Revisar e aprovar plano de normalização
2. ✅ Criar branch de feature: `feature/simulate-provider-support`
3. ✅ Iniciar Fase 1 (Configuração)
4. ✅ Implementar Fase 2 (Clients)
5. ✅ Validar e documentar (Fase 3)

---

**Status Atual:** 📋 Aguardando aprovação para iniciar implementação

