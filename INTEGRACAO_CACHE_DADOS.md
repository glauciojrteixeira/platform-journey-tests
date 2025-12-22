# Integração de Cache de Dados de Teste

## 📊 Resumo

Este documento descreve a integração do `TestDataCache` nos step definitions para reutilização inteligente de dados de teste, reduzindo criação redundante sem comprometer o isolamento entre testes.

---

## ✅ Integrações Implementadas

### 1. **Cache de Usuários Criados** 👤

**Localização:** `AuthenticationSteps.java` e `ProfileSteps.java`

**Funcionalidade:**
- Quando um usuário é criado com sucesso, ele é adicionado ao cache (email → UUID)
- Antes de criar um novo usuário, verifica se já existe no cache
- Reutiliza usuário do cache quando possível (ex: `que estou autenticado na plataforma`)

**Código:**
```java
// Adicionar ao cache após criação
if (testDataCache != null && userEmail != null) {
    testDataCache.cacheUser(userEmail, userUuid);
}

// Verificar cache antes de criar
String cachedUuid = testDataCache.getCachedUserUuid(email);
if (cachedUuid != null) {
    // Reutilizar usuário existente
    userFixture.setCreatedUserUuid(cachedUuid);
    return;
}
```

**Benefícios:**
- Reduz criação redundante de usuários
- Acelera testes que precisam apenas de autenticação
- Mantém isolamento (cada teste pode criar seu próprio usuário se necessário)

---

## 🔒 Garantias de Isolamento

### **Princípios Implementados:**

1. **Cache é Opcional**: `@Autowired(required = false)` - não quebra testes se cache não estiver disponível
2. **Cache por Email**: Cada email único = usuário único no cache
3. **Não Força Reutilização**: Testes podem criar novos usuários mesmo se houver no cache
4. **Thread-Safe**: `ConcurrentHashMap` garante segurança em execução paralela

### **Quando o Cache é Usado:**

✅ **Usado quando:**
- Teste precisa apenas de autenticação (não precisa de usuário específico)
- Step `que estou autenticado na plataforma` (ProfileSteps)
- Email já existe no cache

❌ **NÃO usado quando:**
- Teste precisa de usuário com dados específicos
- Teste valida criação de usuário
- Teste valida duplicação de documento/email

---

## 📈 Impacto Esperado

### **Redução de Criações Redundantes:**

| Cenário | Antes | Depois | Redução |
|---------|-------|--------|---------|
| **Testes que precisam apenas autenticação** | Criar usuário | Reutilizar do cache | **~2-3s por teste** |
| **Múltiplos testes na mesma feature** | Criar para cada | Reutilizar quando possível | **~1-2s por teste** |

### **Estimativa de Ganho:**

- Se 20% dos testes reutilizam usuários do cache: **~30-60 segundos** de redução total
- Redução adicional estimada: **5-10%** do tempo total

---

## 🔧 Configuração

### **Habilitar/Desabilitar Cache:**

O cache está habilitado por padrão. Para desabilitar, basta não injetar o `TestDataCache`:

```java
// Cache desabilitado (não injetar)
// @Autowired(required = false)
// private TestDataCache testDataCache;
```

### **Monitorar Uso do Cache:**

Adicione logging para ver estatísticas do cache:

```java
if (testDataCache != null) {
    var stats = testDataCache.getStats();
    logger.info("Cache stats: {}", stats);
}
```

---

## 📋 Próximas Integrações (Opcional)

### **1. Cache de Documentos Gerados**

**Objetivo:** Reutilizar documentos (CPF, CNPJ) entre testes relacionados

**Implementação:**
```java
// Verificar cache antes de gerar
String cachedDoc = testDataCache.getCachedDocument("CPF");
if (cachedDoc != null) {
    // Reutilizar documento
} else {
    // Gerar novo e adicionar ao cache
    String newDoc = TestDataGenerator.generateUniqueCpf();
    testDataCache.cacheDocument("CPF", newDoc);
}
```

**Cuidado:** Usar apenas quando o teste não precisa de documento específico.

---

### **2. Cache de Tokens de Sessão**

**Objetivo:** Reutilizar tokens de sessão válidos

**Implementação:**
- Cache de tokens por usuário
- Validação de expiração
- Limpeza automática de tokens expirados

**Cuidado:** Tokens podem expirar, então validação é necessária.

---

## ⚠️ Limitações e Cuidados

### **1. Isolamento entre Features**

- Cache é compartilhado entre todas as features na mesma execução
- Isso é intencional para maximizar reutilização
- Se precisar de isolamento absoluto, não use o cache

### **2. Dados Específicos**

- Não use cache quando o teste precisa de dados específicos
- Exemplo: Teste que valida criação com CPF específico

### **3. Limpeza do Cache**

- Cache é limpo automaticamente ao final da execução
- Para limpar manualmente: `testDataCache.clearAll()`

---

## 📊 Métricas e Monitoramento

### **Estatísticas Disponíveis:**

```java
Map<String, Object> stats = testDataCache.getStats();
// Retorna:
// - hits: Número de cache hits
// - misses: Número de cache misses
// - total: Total de requisições
// - hitRate: Taxa de acerto (%)
// - userCacheSize: Tamanho do cache de usuários
// - documentCacheSize: Tamanho do cache de documentos
```

### **Logging Recomendado:**

Adicione logging no final da execução para ver estatísticas:

```java
@After("@e2e")
public void afterScenario() {
    if (testDataCache != null) {
        var stats = testDataCache.getStats();
        logger.info("Cache stats: {}", stats);
    }
}
```

---

## ✅ Checklist de Validação

- [x] Cache integrado em `AuthenticationSteps`
- [x] Cache integrado em `ProfileSteps`
- [x] Cache é opcional (não quebra testes)
- [x] Thread-safe para paralelização
- [x] Isolamento mantido (testes podem criar novos usuários)
- [ ] Métricas de cache implementadas (opcional)
- [ ] Cache de documentos implementado (opcional)

---

## 🚀 Como Usar

### **Execução Normal:**

O cache funciona automaticamente. Não é necessário configuração adicional:

```bash
mvn test -Dspring.profiles.active=local
```

### **Verificar Estatísticas:**

Adicione logging temporário para ver uso do cache:

```java
if (testDataCache != null) {
    logger.info("Cache stats: {}", testDataCache.getStats());
}
```

---

**Data de Implementação:** 2024  
**Versão:** 1.0  
**Status:** ✅ **Implementado** - Cache integrado e funcionando

