# Configuração de Variáveis de Ambiente - Brute Force Detection

## 📋 Visão Geral

As variáveis de ambiente são injetadas nos containers Docker através do arquivo `.env`. Cada serviço possui um arquivo `env.example` que serve como template.

---

## 🔧 Configuração no Auth Service

### 1. Arquivo `env.example`

**Localização**: `auth-service/env.example`

**Adicionar seção**:
```bash
# ============================================
# Brute Force Detection Configuration
# ============================================
# Configuração para detecção e prevenção de ataques de força bruta

# Habilitar/desabilitar detecção de força bruta
SECURITY_BRUTE_FORCE_ENABLED=true

# Whitelist de IPs (separados por vírgula)
# IPs na whitelist nunca são bloqueados
# Formato: "127.0.0.1,localhost,::1"
# Em produção: deixar vazio
SECURITY_BRUTE_FORCE_IP_WHITELIST=127.0.0.1,localhost,::1

# Máximo de tentativas falhadas por email dentro da janela de tempo
SECURITY_BRUTE_FORCE_MAX_ATTEMPTS_PER_EMAIL=3

# Máximo de tentativas falhadas por IP dentro da janela de tempo
# Local/SIT: 200 (alto para testes)
# UAT: 100 (conservador)
# PROD: 10 (seguro)
SECURITY_BRUTE_FORCE_MAX_ATTEMPTS_PER_IP=200

# Janela de tempo deslizante (em minutos)
# Local/SIT: 10 (curto)
# UAT: 15 (médio)
# PROD: 30 (padrão)
SECURITY_BRUTE_FORCE_WINDOW_MINUTES=10

# Duração do bloqueio após exceder limite (em minutos)
# Local/SIT: 2 (curto)
# UAT: 5 (médio)
# PROD: 15 (padrão)
SECURITY_BRUTE_FORCE_LOCKOUT_DURATION_MINUTES=2

# Tipo de janela: SLIDING (deslizante) ou FIXED (fixa)
SECURITY_BRUTE_FORCE_WINDOW_TYPE=SLIDING
```

---

### 2. Arquivo `.env` por Ambiente

**⚠️ IMPORTANTE**: Copiar `env.example` para `.env` e configurar conforme o ambiente.

#### 2.1. Local (Desenvolvimento)

**Arquivo**: `auth-service/.env`

```bash
# Brute Force Detection - Local
SECURITY_BRUTE_FORCE_ENABLED=true
SECURITY_BRUTE_FORCE_IP_WHITELIST=127.0.0.1,localhost,::1
SECURITY_BRUTE_FORCE_MAX_ATTEMPTS_PER_EMAIL=3
SECURITY_BRUTE_FORCE_MAX_ATTEMPTS_PER_IP=200
SECURITY_BRUTE_FORCE_WINDOW_MINUTES=10
SECURITY_BRUTE_FORCE_LOCKOUT_DURATION_MINUTES=2
SECURITY_BRUTE_FORCE_WINDOW_TYPE=SLIDING
```

**Características**:
- ✅ Whitelist inclui localhost (127.0.0.1, localhost, ::1)
- ✅ Limite alto (200) para permitir muitos testes
- ✅ Janela curta (10 minutos)
- ✅ Bloqueio curto (2 minutos)

---

#### 2.2. SIT (Ambiente de Teste)

**Arquivo**: `auth-service/.env`

```bash
# Brute Force Detection - SIT
SECURITY_BRUTE_FORCE_ENABLED=true
SECURITY_BRUTE_FORCE_IP_WHITELIST=127.0.0.1,localhost,::1,138.68.11.125
SECURITY_BRUTE_FORCE_MAX_ATTEMPTS_PER_EMAIL=3
SECURITY_BRUTE_FORCE_MAX_ATTEMPTS_PER_IP=200
SECURITY_BRUTE_FORCE_WINDOW_MINUTES=10
SECURITY_BRUTE_FORCE_LOCKOUT_DURATION_MINUTES=2
SECURITY_BRUTE_FORCE_WINDOW_TYPE=SLIDING
```

**Características**:
- ✅ Whitelist inclui localhost + IP de teste (138.68.11.125)
- ✅ Limite alto (200) para permitir muitos testes
- ✅ Janela curta (10 minutos)
- ✅ Bloqueio curto (2 minutos)

**Nota**: Substituir `138.68.11.125` pelo IP real do ambiente SIT.

---

#### 2.3. UAT (Ambiente de Teste - Mais Conservador)

**Arquivo**: `auth-service/.env`

```bash
# Brute Force Detection - UAT
SECURITY_BRUTE_FORCE_ENABLED=true
SECURITY_BRUTE_FORCE_IP_WHITELIST=138.68.11.125
SECURITY_BRUTE_FORCE_MAX_ATTEMPTS_PER_EMAIL=3
SECURITY_BRUTE_FORCE_MAX_ATTEMPTS_PER_IP=100
SECURITY_BRUTE_FORCE_WINDOW_MINUTES=15
SECURITY_BRUTE_FORCE_LOCKOUT_DURATION_MINUTES=5
SECURITY_BRUTE_FORCE_WINDOW_TYPE=SLIDING
```

**Características**:
- ✅ Whitelist apenas IP de teste (sem localhost)
- ✅ Limite médio (100) - mais conservador que SIT
- ✅ Janela média (15 minutos)
- ✅ Bloqueio médio (5 minutos)

**Nota**: Substituir `138.68.11.125` pelo IP real do ambiente UAT.

---

#### 2.4. PROD (Produção - Seguro)

**Arquivo**: `auth-service/.env`

```bash
# Brute Force Detection - PROD
SECURITY_BRUTE_FORCE_ENABLED=true
SECURITY_BRUTE_FORCE_IP_WHITELIST=
SECURITY_BRUTE_FORCE_MAX_ATTEMPTS_PER_EMAIL=3
SECURITY_BRUTE_FORCE_MAX_ATTEMPTS_PER_IP=10
SECURITY_BRUTE_FORCE_WINDOW_MINUTES=30
SECURITY_BRUTE_FORCE_LOCKOUT_DURATION_MINUTES=15
SECURITY_BRUTE_FORCE_WINDOW_TYPE=SLIDING
```

**Características**:
- ❌ Sem whitelist (proteção máxima)
- ✅ Limite baixo (10) - padrão seguro
- ✅ Janela padrão (30 minutos)
- ✅ Bloqueio padrão (15 minutos)

---

## 📊 Tabela Comparativa

| Ambiente | Whitelist | Max Attempts/IP | Window (min) | Lockout (min) |
|----------|-----------|-----------------|--------------|---------------|
| **Local** | localhost | 200 | 10 | 2 |
| **SIT** | localhost + IP teste | 200 | 10 | 2 |
| **UAT** | IP teste apenas | 100 | 15 | 5 |
| **PROD** | Nenhuma | 10 | 30 | 15 |

---

## 🚀 Como Aplicar

### Passo 1: Atualizar `env.example`

```bash
cd auth-service
# Editar env.example e adicionar seção de brute-force
```

### Passo 2: Configurar `.env` para cada ambiente

```bash
# Local
cp env.example .env
# Editar .env com valores para local

# SIT
cp env.example .env
# Editar .env com valores para SIT

# UAT
cp env.example .env
# Editar .env com valores para UAT

# PROD
cp env.example .env
# Editar .env com valores para PROD
```

### Passo 3: Reiniciar containers

```bash
docker-compose down
docker-compose up -d
```

---

## ✅ Validação

Após configurar, verificar logs:

```bash
docker-compose logs auth-service | grep "IP Whitelist"
```

**Saída esperada**:
```
✅ IP Whitelist configured with 3 IPs: [127.0.0.1, localhost, ::1]
🔒 Brute Force Detection configured: maxAttemptsPerEmail=3, maxAttemptsPerIp=200, windowMinutes=10, lockoutDurationMinutes=2
```

---

## ⚠️ Notas Importantes

1. **Arquivo `.env` não deve ser commitado** (já está no `.gitignore`)
2. **`env.example` deve ser commitado** como template
3. **IPs de teste** devem ser atualizados conforme o ambiente real
4. **Produção** nunca deve ter whitelist (segurança máxima)
5. **Reiniciar containers** após alterar `.env`

---

## 📚 Referências

- **Guia de Implementação**: `IMPLEMENTACAO_ABORDAGEM_HIBRIDA.md`
- **Exemplo de Código**: `EXEMPLO_CODIGO_COMPLETO.md`
- **Resumo**: `RESUMO_IMPLEMENTACAO.md`
