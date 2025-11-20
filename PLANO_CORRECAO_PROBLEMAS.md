# Plano de Correção de Problemas - Testes E2E

**Data:** 2025-11-17  
**Status:** Em Planejamento  
**Prioridade:** Alta

---

## 📊 Resumo Executivo

Este documento detalha o plano de ação para corrigir os 13 testes que estão falhando na execução dos testes E2E, organizados por prioridade e impacto.

### Problemas Identificados

1. **CPF Duplicado** (8 falhas) - 🔴 **CRÍTICO**
2. **Login falhando** (2 falhas) - 🔴 **CRÍTICO**
3. **Perfil não encontrado** (3 falhas) - 🟡 **ALTA**
4. **RabbitMQ** (avisos) - 🟢 **BAIXA**

---

## 🎯 Objetivos

- Reduzir falhas de 13 para 0
- Garantir unicidade de dados de teste
- Resolver problemas de integração entre serviços
- Melhorar robustez dos testes

---

## 📋 Plano de Ação Detalhado

### **FASE 1: Correção do Gerador de CPF** 🔴 **PRIORIDADE MÁXIMA**

**Problema:** CPF duplicado mesmo após tentativas de regeneração  
**Impacto:** 8 testes falhando  
**Tempo estimado:** 2-3 horas

#### Ações:

1. **Melhorar algoritmo de geração de CPF único**
   - [ ] Implementar contador sequencial por execução
   - [ ] Combinar UUID + timestamp + contador
   - [ ] Implementar algoritmo de validação de CPF real (dígitos verificadores)
   - [ ] Adicionar sincronização thread-safe para execuções paralelas

2. **Implementar cache de CPFs usados**
   - [ ] Criar `CpfRegistry` para rastrear CPFs gerados na execução
   - [ ] Verificar duplicatas antes de usar
   - [ ] Limpar cache entre execuções

3. **Melhorar estratégia de retry**
   - [ ] Aumentar delay entre tentativas (100ms → 500ms)
   - [ ] Implementar backoff exponencial
   - [ ] Adicionar validação de CPF antes de tentar criar usuário

#### Arquivos a modificar:
- `src/test/java/com/nulote/journey/fixtures/TestDataGenerator.java`
- `src/test/java/com/nulote/journey/stepdefinitions/AuthenticationSteps.java` (método `a_identidade_deve_ser_criada_com_sucesso`)

#### Critérios de sucesso:
- ✅ Zero CPFs duplicados em execução completa
- ✅ CPFs válidos (passam validação de dígitos verificadores)
- ✅ Retry funciona corretamente quando necessário

---

### **FASE 2: Resolver Problema de Login** 🔴 **PRIORIDADE ALTA**

**Problema:** Login falha com 401 após criação de identidade  
**Impacto:** 2 testes falhando + bloqueia outros testes  
**Tempo estimado:** 3-4 horas

#### Análise do Problema:

Após criar identidade via Identity Service, as credenciais não estão sendo criadas automaticamente no Auth Service. O teste tenta fazer login mas recebe 401 porque:
- Credenciais não foram provisionadas após registro
- Pode haver delay assíncrono não sendo aguardado
- Endpoint de login pode ter formato diferente

#### Ações:

1. **Verificar fluxo de provisionamento de credenciais**
   - [ ] Verificar se Identity Service publica evento após criação
   - [ ] Verificar se Auth Service consome evento e cria credenciais
   - [ ] Adicionar step para aguardar provisionamento antes de login

2. **Implementar aguardo explícito de credenciais**
   - [ ] Adicionar `await()` após criação de identidade
   - [ ] Verificar endpoint de credenciais antes de tentar login
   - [ ] Timeout configurável (padrão: 30s)

3. **Melhorar tratamento de erro de login**
   - [ ] Mensagens de erro mais claras
   - [ ] Verificar se credenciais existem antes de tentar login
   - [ ] Adicionar fallback: criar credenciais manualmente se necessário

4. **Verificar formato de requisição de login**
   - [ ] Validar formato esperado pela API (username vs email)
   - [ ] Verificar campos obrigatórios
   - [ ] Testar com diferentes formatos

#### Arquivos a modificar:
- `src/test/java/com/nulote/journey/stepdefinitions/AuthenticationSteps.java`
  - Método `que_crio_um_usuario_com_esses_dados()`
  - Método `eu_faco_login_com_minhas_credenciais()`
  - Método `que_ja_estou_autenticado_na_plataforma()`
- `src/test/java/com/nulote/journey/clients/AuthServiceClient.java` (verificar implementação)

#### Critérios de sucesso:
- ✅ Login bem-sucedido após criação de identidade
- ✅ Aguardo automático de provisionamento de credenciais
- ✅ Mensagens de erro claras quando login falha

---

### **FASE 3: Resolver Problema de Perfil** 🟡 **PRIORIDADE MÉDIA**

**Problema:** Perfil não encontrado (404) após registro  
**Impacto:** 3 testes falhando  
**Tempo estimado:** 2-3 horas

#### Análise do Problema:

Similar ao problema de login, o perfil não está sendo criado automaticamente após o registro. Os testes esperam que o perfil exista mas recebem 404.

#### Ações:

1. **Verificar fluxo de criação de perfil**
   - [ ] Verificar se evento `identity.created` é publicado
   - [ ] Verificar se Profile Service consome evento
   - [ ] Adicionar step para aguardar criação de perfil

2. **Implementar aguardo explícito de perfil**
   - [ ] Adicionar `await()` após criação de identidade
   - [ ] Verificar endpoint de perfil antes de tentar atualizar
   - [ ] Timeout configurável (padrão: 30s)

3. **Adicionar criação manual de perfil como fallback**
   - [ ] Se perfil não existe após timeout, criar manualmente
   - [ ] Usar dados da identidade criada
   - [ ] Logar warning quando fallback é usado

4. **Ajustar testes para serem mais resilientes**
   - [ ] Testes devem criar perfil se não existir
   - [ ] Não falhar imediatamente com 404
   - [ ] Tentar criar perfil antes de atualizar

#### Arquivos a modificar:
- `src/test/java/com/nulote/journey/stepdefinitions/ProfileSteps.java`
  - Método `que_estou_autenticado_na_plataforma()`
  - Método `que_consulto_meu_perfil_atual()`
- `src/test/java/com/nulote/journey/clients/ProfileServiceClient.java` (verificar implementação)

#### Critérios de sucesso:
- ✅ Perfil criado automaticamente após registro
- ✅ Aguardo automático de criação de perfil
- ✅ Testes não falham com 404 quando perfil não existe

---

### **FASE 4: Melhorias de Infraestrutura** 🟢 **PRIORIDADE BAIXA**

**Problema:** Avisos sobre RabbitMQ (filas não encontradas)  
**Impacto:** Apenas avisos, não causa falhas  
**Tempo estimado:** 1-2 horas

#### Ações:

1. **Configurar filas RabbitMQ para testes**
   - [ ] Criar script de setup de filas
   - [ ] Documentar configuração necessária
   - [ ] Adicionar verificação de conectividade

2. **Melhorar tratamento de eventos RabbitMQ**
   - [ ] Tornar verificação de eventos opcional
   - [ ] Logar avisos mas não falhar testes
   - [ ] Adicionar flag para desabilitar verificação de eventos

3. **Documentar requisitos de infraestrutura**
   - [ ] Atualizar README com requisitos de RabbitMQ
   - [ ] Adicionar instruções de setup
   - [ ] Documentar comportamento quando RabbitMQ não está disponível

#### Arquivos a modificar:
- `src/test/java/com/nulote/journey/utils/RabbitMQHelper.java`
- `src/test/java/com/nulote/journey/stepdefinitions/AuthenticationSteps.java` (método `o_evento_deve_ser_publicado`)
- `README.md`

#### Critérios de sucesso:
- ✅ Avisos sobre RabbitMQ reduzidos ou eliminados
- ✅ Testes funcionam mesmo sem RabbitMQ configurado
- ✅ Documentação atualizada

---

## 🔄 Ordem de Execução Recomendada

1. **FASE 1** → Correção do Gerador de CPF (resolve 8 falhas)
2. **FASE 2** → Resolver Problema de Login (resolve 2 falhas + desbloqueia outros)
3. **FASE 3** → Resolver Problema de Perfil (resolve 3 falhas)
4. **FASE 4** → Melhorias de Infraestrutura (melhora qualidade geral)

---

## 📈 Métricas de Sucesso

### Antes:
- ✅ Testes executados: 114
- ❌ Falhas: 13
- ⏭️ Pulados: 96
- ⏱️ Tempo: ~11s

### Meta:
- ✅ Testes executados: 114
- ✅ Falhas: 0
- ⏭️ Pulados: 96 (mantido)
- ⏱️ Tempo: <15s (aceitável)

---

## 🧪 Estratégia de Testes

### Testes Unitários:
- [ ] Testar `TestDataGenerator.generateUniqueCpf()` com múltiplas chamadas
- [ ] Verificar que não há duplicatas em execução paralela
- [ ] Validar algoritmo de dígitos verificadores de CPF

### Testes de Integração:
- [ ] Executar suite completa após cada fase
- [ ] Verificar que não há regressões
- [ ] Validar que tempo de execução não aumentou significativamente

### Testes Manuais:
- [ ] Executar testes localmente após cada correção
- [ ] Verificar logs para entender fluxo completo
- [ ] Validar comportamento em diferentes ambientes

---

## 📝 Notas Técnicas

### Geração de CPF Único

**Problema atual:**
```java
String base = String.format("%011d", TIMESTAMP % 100000000000L);
```
- Usa apenas timestamp, pode gerar duplicatas
- Não implementa dígitos verificadores reais

**Solução proposta:**
```java
private static final AtomicLong counter = new AtomicLong(0);
private static final Set<String> usedCpfs = ConcurrentHashMap.newKeySet();

public static String generateUniqueCpf() {
    String base;
    String cpf;
    do {
        long uniqueValue = (TIMESTAMP % 100000000L) * 100 + counter.incrementAndGet();
        base = String.format("%09d", uniqueValue);
        cpf = calculateCpfChecksum(base);
    } while (usedCpfs.contains(cpf));
    
    usedCpfs.add(cpf);
    return cpf;
}
```

### Aguardo de Provisionamento

**Problema atual:**
- Testes não aguardam criação de credenciais/perfil após registro
- Tentam usar recursos imediatamente

**Solução proposta:**
```java
@Então("a identidade deve ser criada com sucesso")
public void a_identidade_deve_ser_criada_com_sucesso() {
    // ... validação de criação ...
    
    // Aguardar provisionamento de credenciais
    await().atMost(30, SECONDS).pollInterval(500, MILLISECONDS)
        .until(() -> {
            var credentialsResponse = authClient.getCredentialsByUserUuid(userUuid);
            return credentialsResponse.getStatusCode() == 200;
        });
    
    // Aguardar criação de perfil
    await().atMost(30, SECONDS).pollInterval(500, MILLISECONDS)
        .until(() -> {
            var profileResponse = profileClient.getProfileByUserUuid(userUuid);
            return profileResponse.getStatusCode() == 200;
        });
}
```

---

## 🚀 Próximos Passos

1. **Revisar e aprovar plano** ✅
2. **Iniciar FASE 1** - Correção do Gerador de CPF
3. **Executar testes após FASE 1**
4. **Iniciar FASE 2** - Resolver Problema de Login
5. **Executar testes após FASE 2**
6. **Continuar com FASES 3 e 4**

---

## 📚 Referências

- [Algoritmo de Validação de CPF](https://www.macoratti.net/alg_cpf.htm)
- [Awaitility Documentation](https://github.com/awaitility/awaitility)
- [Cucumber Best Practices](https://cucumber.io/docs/cucumber/best-practices/)

---

**Última atualização:** 2025-11-17  
**Responsável:** Equipe de QA  
**Status:** Aguardando aprovação para início

