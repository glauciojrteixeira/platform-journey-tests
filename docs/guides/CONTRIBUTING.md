# Guia de Contribuição - Platform Journey Tests

Este guia explica como adicionar novos cenários de teste E2E ao projeto.

## 📝 Como Adicionar Novos Cenários

### **Passo 1: Criar ou Editar Feature File**

Crie um arquivo `.feature` em `src/test/resources/features/` ou edite um existente:

```gherkin
@implemented @segment_1 @j1.X @feature_name @critical @e2e
Feature: Nome Descritivo da Feature
  Como um tipo de usuário
  Eu quero realizar uma ação específica
  Para alcançar um objetivo de negócio

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando

  Scenario: Descrição do cenário
    Given uma pré-condição
    When uma ação é executada
    Then um resultado esperado deve ocorrer
    And o evento "event.type" deve ser publicado
```

### **Passo 2: Adicionar Tags Estratégicas**

Cada feature deve ter tags mínimas:
- **Business Unit**: `@vs-identity`, `@cross-bu`
- **Segmento**: `@segment_1`, `@segment_2`, etc.
- **Jornada**: `@j1.1`, `@j2.3`, etc.
- **Status**: `@implemented`, `@wip`, `@planned`
- **Prioridade**: `@critical`, `@high`, `@medium`, `@low`
- **Ambiente**: `@local`, `@sit`, `@uat` (opcional)

### **Passo 3: Implementar Step Definitions**

Se os steps não existirem, adicione em `src/test/java/com/nulote/journey/stepdefinitions/`:

```java
@Quando("uma ação é executada")
public void uma_acao_e_executada() {
    // Implementação usando clientes HTTP, fixtures, etc.
    var request = userFixture.buildRequest();
    lastResponse = identityClient.createUser(request);
}
```

### **Passo 4: Reutilizar Steps Existentes**

Sempre verifique se os steps já existem antes de criar novos:

```bash
# Buscar steps existentes
grep -r "@Quando\|@Dado\|@Então" src/test/java/com/nulote/journey/stepdefinitions/
```

### **Passo 5: Usar Dados Únicos**

Sempre use dados únicos para garantir idempotência:

```java
@Dado("que tenho dados de teste únicos")
public void que_tenho_dados_de_teste_unicos() {
    String email = TestDataGenerator.generateUniqueEmail();
    String cpf = TestDataGenerator.generateUniqueCpf();
    // ...
}
```

### **Passo 6: Testar Localmente**

```bash
# Executar apenas a nova feature
mvn test -Dspring.profiles.active=local \
  -Dcucumber.features="src/test/resources/features/caminho/nova_feature.feature"

# Executar por tags
mvn test -Dspring.profiles.active=local \
  -Dcucumber.filter.tags="@feature_name"
```

## 🎯 Boas Práticas

### **1. Nomenclatura**
- ✅ Features: Português, descritivo, focado em negócio
- ✅ Steps: Português, alinhado com Gherkin
- ✅ Classes Java: Inglês, convenções Java

### **2. Organização**
- ✅ Features por domínio funcional (authentication, identity, profile)
- ✅ Step definitions agrupados por domínio
- ✅ Um cliente HTTP por microserviço

### **3. Idempotência**
- ✅ Sempre usar dados únicos (UUID + timestamp)
- ✅ Verificar antes de criar
- ✅ Operações idempotentes

### **4. Isolamento**
- ✅ Cada cenário independente
- ✅ Sem dependências entre cenários
- ✅ Sem necessidade de cleanup

## 📚 Exemplos

### **Exemplo: Adicionar Cenário de Login com MFA**

1. **Editar `login.feature`**:
```gherkin
@j2.3 @mfa @high
Scenario: Login bem-sucedido com MFA
  Given que tenho dados de teste únicos
  And que crio um usuário com esses dados
  And o MFA está ativado para o usuário
  When eu faço login com minhas credenciais
  And o sistema solicita código MFA
  And eu informo o código MFA "654321"
  Then o login deve ser bem-sucedido
  And eu devo receber um JWT válido
```

2. **Adicionar Step Definitions** (se necessário):
```java
@Dado("o MFA está ativado para o usuário")
public void o_mfa_esta_ativado_para_o_usuario() {
    // Ativar MFA para o usuário de teste
}

@Quando("eu informo o código MFA {string}")
public void eu_informo_o_codigo_mfa(String codigo) {
    var request = Map.of("mfaCode", codigo);
    lastResponse = authClient.validateMfa(request);
}
```

3. **Testar**:
```bash
mvn test -Dcucumber.filter.tags="@mfa"
```

## 🔍 Validação

Antes de fazer commit:

- [ ] Feature compila sem erros
- [ ] Steps estão implementados
- [ ] Tags estratégicas adicionadas
- [ ] Dados únicos sendo usados
- [ ] Teste executado localmente (se possível)
- [ ] Documentação atualizada (se necessário)

## 📖 Referências

- Nota técnica completa: `engineering-playbook/bdd-e2e-testing-strategy.md`
- Exemplos de features: `src/test/resources/features/`
- Step definitions existentes: `src/test/java/com/nulote/journey/stepdefinitions/`

