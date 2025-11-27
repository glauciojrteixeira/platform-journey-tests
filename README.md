# Platform Journey Tests

Projeto de testes E2E (End-to-End) utilizando BDD (Behavior-Driven Development) com Cucumber e Gherkin para validar jornadas de negócio em microserviços Java cloud-native.

## 📚 Documentação

Toda a documentação técnica, arquitetural e de implementação está organizada no diretório **[docs/](docs/)**.

> 💡 **Acesso rápido**: Consulte o **[Índice Completo de Documentação](docs/INDEX.md)** para navegar por todos os documentos disponíveis.

## 📋 Visão Geral

Este projeto implementa testes E2E que validam fluxos completos entre múltiplos microserviços, garantindo que as jornadas de negócio funcionem corretamente end-to-end.

### **Objetivos**

1. ✅ Validar **fluxos completos E2E** das jornadas de negócio
2. ✅ Garantir **comportamento correto** entre múltiplos microserviços
3. ✅ Documentar **especificações executáveis** em linguagem de negócio
4. ✅ Facilitar **colaboração** entre equipes técnicas e não técnicas
5. ✅ Integrar com **estratégia de testes existente** (Unit, Component, Integration)

## 🏗️ Estrutura do Projeto

```
platform-journey-tests/
├── src/
│   ├── test/
│   │   ├── java/com/nulote/journey/
│   │   │   ├── runners/          # Cucumber runners
│   │   │   ├── stepdefinitions/  # Step definitions Gherkin
│   │   │   ├── clients/          # Clientes HTTP para microserviços
│   │   │   ├── fixtures/         # Dados de teste e fixtures
│   │   │   ├── utils/            # Helpers e utilitários
│   │   │   └── config/            # Configurações de teste
│   │   └── resources/
│   │       └── features/         # Features Gherkin
│   └── main/java/com/nulote/journey/
│       └── config/                # Configurações da aplicação
├── pom.xml
└── README.md
```

## 🚀 Execução

### Ambiente Local

```bash
# Executar todos os testes implementados
mvn test -Dspring.profiles.active=local

# Executar testes específicos por tag
mvn test -Dspring.profiles.active=local -Dcucumber.filter.tags="@segment_1"

# Executar apenas testes críticos
mvn test -Dspring.profiles.active=local -Dcucumber.filter.tags="@critical"
```

### Ambiente SIT

```bash
mvn test -Dspring.profiles.active=sit -Dcucumber.filter.tags="@sit and @implemented"
```

### Ambiente UAT

Para executar testes contra o ambiente UAT a partir da sua máquina local:

**1. Configurar URLs via variáveis de ambiente:**
```bash
export UAT_IDENTITY_URL="https://identity-service.uat.exemplo.com.br"
export UAT_AUTH_URL="https://auth-service.uat.exemplo.com.br"
export UAT_PROFILE_URL="https://profile-service.uat.exemplo.com.br"

mvn test -Dspring.profiles.active=uat -Dcucumber.filter.tags="@e2e and not @not_implemented"
```

**2. Ou modificar `application-uat.yml` diretamente** (não commitar alterações)

**3. Ou criar `application-uat-local.yml`** (arquivo local não versionado)

> 📖 **Guia Completo**: Consulte **[UAT_EXECUTION_GUIDE.md](docs/guides/UAT_EXECUTION_GUIDE.md)** para instruções detalhadas sobre conectividade, VPN, proxy e troubleshooting.

## 📝 Documentação

Para mais detalhes sobre a estratégia de testes E2E, consulte:
- `engineering-playbook/bdd-e2e-testing-strategy.md` - Documentação completa da estratégia
- **[DEPENDENCIES_EXTERNAS.md](docs/guides/DEPENDENCIES_EXTERNAS.md)** - Testes que dependem de serviços externos não implementados

---

## 🔄 Integração com Estratégia de Simulação de Providers

### **Simulação de Providers**

Plano de normalização para suportar o header `simulate-provider` que permite simular o envio de mensagens aos providers em ambientes não-PROD:

- **[RESUMO_EXECUTIVO_NORMALIZACAO_SIMULACAO.md](docs/guides/RESUMO_EXECUTIVO_NORMALIZACAO_SIMULACAO.md)** - Resumo executivo e visão geral
- **[PLANO_NORMALIZACAO_SIMULACAO_PROVIDERS.md](docs/plans/PLANO_NORMALIZACAO_SIMULACAO_PROVIDERS.md)** - Plano detalhado de implementação

> 📋 **Status**: Planejamento completo. Aguardando aprovação para iniciar implementação.

## 🔧 Configuração

As configurações por ambiente estão em:
- `src/main/resources/application-local.yml` - Ambiente local
- `src/main/resources/application-sit.yml` - Ambiente SIT
- `src/main/resources/application-uat.yml` - Ambiente UAT

## 📊 Relatórios

Após a execução, os relatórios são gerados em:
- `target/cucumber-reports/cucumber.html` - Relatório HTML interativo
- `target/cucumber-reports/cucumber.json` - Relatório JSON estruturado

## 🏷️ Tags Estratégicas

O projeto utiliza tags para organização e execução seletiva:

### **Tags por Business Unit**
- `@vs-identity` - Testes da Business Unit Identity
- `@cross-bu` - Testes que cruzam múltiplas BUs

### **Tags por Segmento**
- `@segment_1` - Compradores Ocasionais
- `@segment_2` - Arrematadores Profissionais
- `@segment_3` - Revendedores e Lojistas
- `@segment_4` - Plataformas de Leilão

### **Tags por Status**
- `@implemented` - Features implementadas e testadas
- `@wip` - Features em desenvolvimento
- `@planned` - Features planejadas

### **Tags por Prioridade**
- `@critical` - Testes críticos
- `@high` - Alta prioridade
- `@medium` - Média prioridade
- `@low` - Baixa prioridade

### **Tags por Ambiente**
- `@local` - Ambiente local
- `@sit` - Ambiente SIT
- `@uat` - Ambiente UAT

### **Exemplos de Execução com Tags**

```bash
# Executar apenas testes críticos
mvn test -Dcucumber.filter.tags="@critical"

# Executar testes do Segmento 1 implementados
mvn test -Dcucumber.filter.tags="@segment_1 and @implemented"

# Executar smoke tests
mvn test -Dcucumber.filter.tags="@smoke"

# Excluir testes em desenvolvimento
mvn test -Dcucumber.filter.tags="@implemented and not @wip"
```

## 🔧 Pré-requisitos

### **Ambiente Local**
- Java 21
- Maven 3.8+
- Docker e Docker Compose (para infraestrutura)
- Microserviços rodando localmente:
  - Identity Service (porta 8084)
  - Auth Service (porta 8080)
  - Profile Service (porta 8088)
- RabbitMQ rodando (porta 5672)
- PostgreSQL rodando
- MongoDB rodando

### **Verificar Infraestrutura**

```bash
# Verificar serviços estão rodando
curl http://localhost:8084/actuator/health  # Identity Service
curl http://localhost:8080/actuator/health  # Auth Service
curl http://localhost:8088/actuator/health  # Profile Service
```

## 📝 Como Adicionar Novos Cenários

### **1. Criar Feature File**

Crie um arquivo `.feature` em `src/test/resources/features/`:

```gherkin
@implemented @segment_1 @j1.X @feature_name @critical @e2e
Feature: Nome da Feature
  Como um tipo de usuário
  Eu quero realizar uma ação
  Para alcançar um objetivo

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando

  Scenario: Cenário de teste
    Given uma pré-condição
    When uma ação é executada
    Then um resultado esperado deve ocorrer
```

### **2. Implementar Step Definitions**

Adicione os step definitions necessários em `src/test/java/com/nulote/journey/stepdefinitions/`:

```java
@Quando("uma ação é executada")
public void uma_acao_e_executada() {
    // Implementação do step
}
```

### **3. Executar e Validar**

```bash
# Executar apenas a nova feature
mvn test -Dcucumber.features="src/test/resources/features/caminho/nova_feature.feature"
```

## 🎓 Boas Práticas

### **1. Nomenclatura Clara**
- ✅ Features: Nomes descritivos em português (jornadas de negócio)
- ✅ Step Definitions: Nomes em português alinhados com Gherkin
- ✅ Classes Java: Nomes em inglês seguindo convenções Java

### **2. Reutilização**
- ✅ Step definitions genéricos quando possível
- ✅ Fixtures reutilizáveis para dados de teste
- ✅ Helpers compartilhados entre features

### **3. Isolamento**
- ✅ Cada cenário independente
- ✅ Dados únicos por execução (UUID + timestamp)
- ✅ Não depender de ordem de execução
- ✅ Sem necessidade de cleanup (idempotência + dados únicos)

### **4. Idempotência**
- ✅ Sempre usar dados únicos (email, CPF, telefone únicos por execução)
- ✅ Verificar antes de criar (idempotência)
- ✅ Operações idempotentes (criar apenas se não existir)
- ✅ Rastreamento com execution ID

## 🐛 Troubleshooting

### **Problemas Comuns**

#### **Serviços não estão acessíveis**
```bash
# Verificar serviços estão rodando
curl http://localhost:8084/actuator/health
```

#### **Timeout em chamadas HTTP**
- Verificar timeout configurado em `application.yml`
- Verificar saúde dos microserviços

#### **Eventos assíncronos não chegam**
- Verificar conectividade RabbitMQ
- Aumentar timeout em `await()` se necessário

Para mais detalhes, consulte a seção "Troubleshooting" na nota técnica completa.

