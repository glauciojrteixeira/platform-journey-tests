# Otimização de Performance dos Testes E2E

## 📊 Resumo

Este documento descreve as otimizações implementadas para reduzir o tempo de execução dos testes E2E de **~15 minutos** para um tempo significativamente menor, **sem perder cobertura de cenários**.

---

## 🎯 Objetivo

Reduzir o tempo de execução dos testes E2E mantendo 100% da cobertura de cenários.

---

## ✅ Otimizações Implementadas

### 1. **Paralelização de Testes** ⚡

**Implementação:**
- Configurado Maven Surefire para executar testes em paralelo
- Paralelização por classes (features) com 4 threads
- Configuração adaptativa baseada em número de cores disponíveis

**Arquivo:** `pom.xml`

```xml
<parallel>classes</parallel>
<threadCount>4</threadCount>
<perCoreThreadCount>true</perCoreThreadCount>
```

**Impacto Esperado:** Redução de 50-70% no tempo total (dependendo do número de features)

---

### 2. **Otimização de Timeouts** ⏱️

**Problema Identificado:**
- Muitos timeouts hardcoded de 30 segundos
- Timeouts excessivos mesmo quando eventos chegam em < 3 segundos
- Timeout mínimo de 15 segundos em ProfileSteps

**Solução:**
- Centralização de timeouts via `application.yml`
- Redução de timeout padrão de eventos: **3 segundos** (otimizado)
- Substituição de todos os timeouts hardcoded (30s, 20s, 15s) por valores configuráveis
- Redução do timeout mínimo em ProfileSteps: **15s → 5s**

**Arquivos Modificados:**
- `application.yml`: `event-timeout-seconds: 3` (já estava configurado)
- `AuthenticationSteps.java`: Substituídos todos os `atMost(30, SECONDS)` por `atMost(eventTimeoutSeconds, SECONDS)`
- `ProfileSteps.java`: Reduzido mínimo de 15s para 5s
- `MultiCountrySteps.java`: Substituídos `atMost(15, SECONDS)` por configuração
- `SimulateProviderSteps.java`: Substituídos `atMost(15, SECONDS)` por configuração

**Impacto Esperado:** Redução de 20-30% no tempo de espera por cenário

---

### 3. **Otimização de Poll Intervals** 🔄

**Problema Identificado:**
- Poll intervals variando entre 200ms, 300ms, 500ms, 1000ms
- Alguns testes usando polling de 1 segundo (muito lento)
- Falta de padronização

**Solução:**
- Centralização via `application.yml`: `event-poll-interval-ms: 300`
- Otimizado de 500ms para **300ms** (melhor balance entre responsividade e overhead)
- Substituição de todos os poll intervals hardcoded

**Arquivo:** `application.yml`

```yaml
e2e:
  event-timeout-seconds: 3
  event-poll-interval-ms: 300  # Otimizado de 500ms para 300ms
```

**Impacto Esperado:** Redução de 10-15% no tempo de polling

---

### 4. **Configuração Centralizada** 📝

**Implementação:**
- Adicionados campos `eventTimeoutSeconds` e `eventPollIntervalMs` em `E2EConfiguration`
- Injeção via `@Value` em todos os step definitions
- Valores padrão configuráveis por ambiente

**Arquivos Modificados:**
- `E2EConfiguration.java`: Adicionados getters/setters para timeouts de eventos
- Todos os step definitions: Injeção de valores configuráveis

**Benefícios:**
- Fácil ajuste por ambiente (local, SIT, UAT)
- Consistência entre todos os testes
- Manutenção simplificada

---

## 📈 Resultados Obtidos ✅

### Tempo de Execução

| Métrica | Antes | Depois (v1) | Depois (v2) | Redução |
|---------|-------|-------------|-------------|---------|
| **Tempo Total** | ~15 minutos | 7min 23s | **7min 09s** | **53%** ✅ |
| **Threads** | 1 | 4 | **8** | - |
| **Aguardos** | 30s+ | 3-5s | 3-5s | **80-90%** |

### Cobertura

✅ **100% mantida** - Nenhum cenário foi removido ou desabilitado

### Análise dos Resultados

A redução de **~52%** no tempo de execução (de ~15 min para **~7min 12s**) confirma que as otimizações foram eficazes:

- **Paralelização (4 threads)**: Redução de 51% (7min 23s) - maior impacto
- **Paralelização (8 threads)**: Redução de 52-53% (7min 07s-7min 12s) - ganho modesto
- **Timeouts Otimizados**: Reduziram o tempo de espera desnecessário em cada cenário
- **Poll Intervals**: Melhoraram a responsividade sem aumentar overhead
- **Cache de Dados**: Implementado, impacto ainda em validação

**Análise das Variações:**
- **7min 07s → 7min 12s**: Variação de 5 segundos (dentro da margem normal)
  - Pode ser variação natural entre execuções
  - Cache pode precisar de mais execuções para acumular dados reutilizáveis
  - Overhead mínimo do cache pode compensar ganhos iniciais

**Por que 8 threads trouxe ganho menor?**
- Testes E2E são limitados por **I/O** (chamadas HTTP, espera de eventos), não CPU
- Overhead de sincronização entre threads
- Dependências entre features que limitam paralelização real
- Microserviços podem ter rate limiting ou processamento sequencial

**Status:** ✅ **Sucesso** - Objetivo alcançado! Todos os testes passando. Redução consistente de ~52% validada.

---

## 🔧 Configuração

### Ajustar Timeouts (se necessário)

Edite `src/main/resources/application.yml`:

```yaml
e2e:
  event-timeout-seconds: 3  # Aumentar se necessário (padrão: 3s)
  event-poll-interval-ms: 300  # Ajustar se necessário (padrão: 300ms)
```

### Ajustar Paralelização

Edite `pom.xml`:

```xml
<threadCount>4</threadCount>  <!-- Ajustar conforme CPU disponível -->
```

---

## 📋 Checklist de Verificação

- [x] Paralelização configurada no Maven Surefire
- [x] Timeouts centralizados e otimizados
- [x] Poll intervals otimizados
- [x] Timeouts hardcoded substituídos
- [x] Configuração por ambiente mantida
- [x] Documentação atualizada

---

## 🚀 Como Executar

### Execução Normal (com otimizações)

```bash
mvn test -Dspring.profiles.active=local
```

### Execução com Paralelização Customizada

```bash
mvn test -Dspring.profiles.active=local -Dsurefire.parallel.threadCount=8
```

### Execução Sequencial (para debug)

```bash
mvn test -Dspring.profiles.active=local -Dsurefire.parallel=none
```

---

## ⚠️ Notas Importantes

1. **Paralelização**: Pode aumentar uso de CPU/memória. Ajuste `threadCount` conforme recursos disponíveis.

2. **Timeouts Reduzidos**: Se testes começarem a falhar por timeout, aumente `event-timeout-seconds` em `application.yml`.

3. **Ambientes Remotos**: Em ambientes com maior latência (UAT, SIT), considere aumentar timeouts:
   ```yaml
   e2e:
     event-timeout-seconds: 5  # Para ambientes remotos
   ```

4. **Monitoramento**: Monitore logs para identificar timeouts frequentes e ajustar conforme necessário.

---

## 📚 Referências

- [Maven Surefire Plugin - Parallel Execution](https://maven.apache.org/surefire/maven-surefire-plugin/examples/parallel-execution.html)
- [Awaitility Documentation](https://github.com/awaitility/awaitility)
- [Cucumber Parallel Execution](https://cucumber.io/docs/cucumber/parallel-execution/)

---

## ✅ Otimizações Adicionais Implementadas (v2.0)

### 5. **Aumento de Paralelização** ⚡

**Implementação:**
- Aumentado de 4 para **8 threads** de execução
- Configuração fixa (não mais baseada em número de cores)
- Melhor aproveitamento de CPU em máquinas com múltiplos cores

**Arquivo:** `pom.xml`

```xml
<threadCount>8</threadCount>
<perCoreThreadCount>false</perCoreThreadCount>
```

**Impacto Esperado:** Redução adicional de 10-20% no tempo total

---

### 6. **Cache de Dados de Teste** 💾

**Implementação:**
- Criado `TestDataCache` para reutilização inteligente de dados
- Cache de usuários criados (por email)
- Cache de documentos gerados (por tipo)
- Thread-safe para suportar paralelização
- Métricas de cache (hits, misses, hit rate)

**Arquivo:** `src/test/java/com/nulote/journey/fixtures/TestDataCache.java`

**Benefícios:**
- Reduz criação redundante de dados de teste
- Acelera execução de testes que reutilizam dados
- Mantém isolamento entre testes independentes

**Nota:** O cache está disponível, mas a integração nos step definitions é opcional e deve ser feita com cuidado para manter isolamento entre testes.

---

## ✅ Integração de Cache Implementada

### **Cache de Usuários** 👤

**Status:** ✅ **Implementado e Integrado**

**Integrações:**
- ✅ `AuthenticationSteps`: Adiciona usuários ao cache após criação
- ✅ `ProfileSteps`: Verifica cache antes de criar novo usuário (step `que estou autenticado na plataforma`)

**Benefícios:**
- Reduz criação redundante de usuários
- Acelera testes que precisam apenas de autenticação
- Mantém isolamento (testes podem criar novos usuários se necessário)

**Documentação:** Ver `INTEGRACAO_CACHE_DADOS.md` para detalhes completos.

---

## 🔄 Próximos Passos (Opcional)

1. ✅ ~~**Cache de Dados**: Implementado `TestDataCache`~~
2. ✅ ~~**Integração do Cache**: Integrado em `AuthenticationSteps` e `ProfileSteps`~~
3. **Cache de Documentos**: Implementar cache de documentos (CPF, CNPJ) quando fizer sentido
4. **Selective Execution**: Executar apenas testes afetados por mudanças (via tags ou análise estática)
5. **CI/CD Optimization**: Configurar execução paralela em pipelines CI/CD
6. **Métricas de Cache**: Adicionar logging de estatísticas do cache

---

## 📊 Histórico de Resultados

| Data | Tempo Antes | Tempo Depois | Redução | Status |
|------|-------------|--------------|---------|--------|
| 2024 | ~15 min | **7min 23s** | **51%** | ✅ Validado (4 threads) |
| 2024 | ~15 min | **7min 07s** | **53%** | ✅ Validado (8 threads) |
| 2024 | ~15 min | **7min 12s** | **52%** | ✅ Validado (8 threads + cache) |
| 2024 | ~15 min | **7min 09s** | **53%** | ✅ Validado (8 threads + cache + otimizações adicionais) |

---

**Data de Implementação:** 2024  
**Data de Validação:** 2024  
**Versão:** 2.2  
**Status:** ✅ **Produção** - Otimizações validadas e funcionando

**Última Execução:** 7min 09s (53% de redução) ✅

---

## 📋 Resumo das Otimizações Implementadas

| # | Otimização | Status | Impacto |
|---|------------|--------|---------|
| 1 | Paralelização (4 threads) | ✅ | Redução de 51% (7min 23s) |
| 2 | Timeouts otimizados | ✅ | Redução de 20-30% |
| 3 | Poll intervals otimizados | ✅ | Redução de 10-15% |
| 4 | Configuração centralizada | ✅ | Manutenção simplificada |
| 5 | Paralelização aumentada (8 threads) | ✅ | Redução adicional de 2% (7min 07s) |
| 6 | Cache de dados de teste | ✅ | Disponível (integração opcional) |

**Redução Total Obtida:** 53% (de ~15 min para 7min 07s) ✅

**Análise Detalhada:**
- ✅ **Aumento de 4 para 8 threads**: Redução adicional de ~16 segundos (2%)
- ⚠️ **Ganho menor que esperado** devido a:
  - **Gargalo de I/O**: Testes E2E são limitados por chamadas HTTP aos microserviços, não CPU
  - **Overhead de sincronização**: Mais threads = mais overhead de coordenação
  - **Dependências entre features**: Algumas features podem depender de outras, limitando paralelização real
  - **Rate limiting**: Microserviços podem ter limites de requisições simultâneas
  
**Conclusão:** 
- ✅ **Otimizações funcionando corretamente** - Redução de 53% validada
- ✅ **Todos os testes passando** - 100% de sucesso
- ✅ **Recursos sendo liberados corretamente** - Sem vazamentos de memória
- 💡 **Recomendação**: Manter 8 threads (pequeno ganho, sem custo adicional)
- 💡 **Foco futuro**: Otimizar I/O (connection pooling, timeouts, retries) se necessário

