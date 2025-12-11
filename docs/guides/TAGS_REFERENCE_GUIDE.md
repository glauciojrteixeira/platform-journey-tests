# Guia de Referência: Tags em Testes E2E

**Data de Criação**: 2025-12-11  
**Última Atualização**: 2025-12-11  
**Versão**: 1.0

---

## 📋 Visão Geral

Este guia fornece referência rápida sobre as tags utilizadas nos testes E2E do projeto `platform-journey-tests`, conforme definido no playbook `019.04 - BDD_E2E_TESTING_STRATEGY_EXECUTION.md`.

---

## ✅ Tags Obrigatórias

Cada arquivo `.feature` **DEVE** ter as seguintes tags:

### 1. Business Unit
- `@vs-identity` - Testes da Value Stream Identity
- `@vs-customer-communications` - Testes da Value Stream Customer Communications
- `@cross-bu` - Testes que cruzam múltiplas Business Units

**Exemplo**:
```gherkin
@implemented @vs-identity @segment_1 @j1.1 @identity @critical @e2e
Feature: Criação de Identidade
```

### 2. Segmento
- `@segment_1` - Compradores Ocasionais (B2C)
- `@segment_2` - Arrematadores Profissionais (B2C)
- `@segment_3` - Revendedores (B2B)
- `@segment_4` - Plataformas Parceiras (B2B Enterprise)

**Exemplo**:
```gherkin
@vs-identity @segment_1 @j1.1 @registration @critical @e2e
Feature: Registro e Onboarding
```

### 3. Jornada (quando aplicável)
- `@j1.1`, `@j1.2`, `@j1.3`, etc. - Jornadas do Segmento 1
- `@j2.1`, `@j2.2`, `@j2.3`, etc. - Jornadas do Segmento 2
- `@j3.1`, `@j3.2`, etc. - Jornadas do Segmento 3
- `@j4.1`, `@j4.2`, etc. - Jornadas do Segmento 4

**Exemplo**:
```gherkin
@vs-identity @segment_1 @j1.1 @registration @critical @e2e
Feature: Registro e Onboarding
```

### 4. Prioridade
- `@critical` - Testes críticos (devem passar sempre)
- `@high` - Alta prioridade
- `@medium` - Prioridade média
- `@low` - Baixa prioridade

**Exemplo**:
```gherkin
@vs-identity @segment_1 @j1.1 @registration @critical @e2e
Feature: Registro e Onboarding
```

---

## 📌 Tags Opcionais

### Status de Implementação
- `@implemented` - Feature implementada e testada
- `@wip` - Work in Progress (em desenvolvimento)
- `@not_implemented` - Ainda não implementado

**Exemplo**:
```gherkin
@implemented @vs-identity @segment_1 @j1.1 @registration @critical @e2e
Feature: Registro e Onboarding
```

### Tipo de Teste
- `@api` - Testes de API
- `@database` - Testes que envolvem banco de dados
- `@messaging` - Testes que envolvem mensageria (RabbitMQ)
- `@event-driven` - Testes baseados em eventos

**Exemplo**:
```gherkin
@vs-identity @segment_1 @j1.1 @registration @critical @api @database @e2e
Feature: Registro e Onboarding
```

### Funcionalidade
- `@authentication` - Autenticação
- `@registration` - Registro
- `@otp` - OTP (One-Time Password)
- `@mfa` - Multi-Factor Authentication
- `@password` - Gerenciamento de senha
- `@identity` - Gestão de identidade
- `@profile` - Perfil do usuário

**Exemplo**:
```gherkin
@vs-identity @segment_1 @j1.1 @registration @otp @critical @e2e
Feature: Registro com OTP
```

### Ambiente
- `@local` - Testes locais
- `@sit` - System Integration Testing
- `@uat` - User Acceptance Testing

**Exemplo**:
```gherkin
@implemented @vs-identity @segment_1 @j1.1 @registration @critical @local @e2e
Feature: Registro e Onboarding
```

### Outras Tags Especiais
- `@smoke` - Smoke tests (testes rápidos de fumaça)
- `@cross-vs` - Testes cross-Value Stream
- `@edge-case` - Casos extremos/edge cases
- `@integration` - Testes de integração
- `@transversal` - Testes transversais

---

## 🎯 Exemplos Completos

### Exemplo 1: Feature Completa
```gherkin
@implemented @vs-identity @segment_1 @j1.1 @registration @otp @critical @api @messaging @e2e
Feature: Registro e Onboarding com OTP
  Como um comprador ocasional
  Eu quero me registrar na plataforma
  Para poder fazer arremates

  Background:
    Given a infraestrutura de testes está configurada

  @smoke @api
  Scenario: Registro bem-sucedido com OTP via Email
    ...
```

### Exemplo 2: Feature Cross-VS
```gherkin
@implemented @vs-identity @cross-vs @vs-customer-communications @segment_1 @j1.1 @otp @critical @api @messaging @integration @event-driven @e2e
Feature: Envio de OTP via Email - Fluxo Cross-VS
  Como um usuário
  Eu quero receber OTP via email
  Para validar minha identidade

  @otp_request @cross-vs-email
  Scenario: OTP enviado via Email no fluxo de registro
    ...
```

### Exemplo 3: Feature Não Implementada
```gherkin
@not_implemented @vs-identity @segment_2 @j2.2 @mfa @high @e2e @otp_required
Feature: Ativação de MFA
  Como um arrematador profissional
  Eu quero ativar MFA na minha conta
  Para aumentar a segurança

  Scenario: Ativação de MFA bem-sucedida
    ...
```

---

## 🔍 Filtros Comuns

### Por Business Unit
```bash
mvn test -Dcucumber.filter.tags="@vs-identity"
```

### Por Segmento
```bash
mvn test -Dcucumber.filter.tags="@segment_1"
```

### Por Prioridade
```bash
mvn test -Dcucumber.filter.tags="@critical"
```

### Apenas Implementados
```bash
mvn test -Dcucumber.filter.tags="@implemented"
```

### Implementados e Críticos
```bash
mvn test -Dcucumber.filter.tags="@implemented and @critical"
```

### Excluir Não Implementados
```bash
mvn test -Dcucumber.filter.tags="@vs-identity and not @not_implemented"
```

### Smoke Tests
```bash
mvn test -Dcucumber.filter.tags="@smoke"
```

### Por Jornada
```bash
mvn test -Dcucumber.filter.tags="@j1.1"
```

### Combinação Complexa
```bash
# VS-Identity, Segmento 1, Implementados, Críticos, API
mvn test -Dcucumber.filter.tags="@vs-identity and @segment_1 and @implemented and @critical and @api"
```

---

## 🚀 Execução por Ambiente

### LOCAL
```bash
# Todos os implementados
mvn test -Dspring.profiles.active=local -Dcucumber.filter.tags="@implemented"
```

### SIT
```bash
# Apenas implementados (exclui não implementados)
mvn test -Dspring.profiles.active=sit -Dcucumber.filter.tags="@implemented and not @not_implemented"
```

### UAT
```bash
# Apenas críticos implementados
mvn test -Dspring.profiles.active=uat -Dcucumber.filter.tags="@implemented and @critical and not @not_implemented"
```

---

## ✅ Checklist para Novos Cenários

Ao criar um novo arquivo `.feature` ou cenário, verifique:

- [ ] Tag de Business Unit presente (`@vs-identity`, `@vs-customer-communications`, etc.)
- [ ] Tag de segmento presente (`@segment_1`, `@segment_2`, etc.)
- [ ] Tag de jornada presente (quando aplicável: `@j1.1`, `@j2.3`, etc.)
- [ ] Tag de prioridade presente (`@critical`, `@high`, `@medium`, `@low`)
- [ ] Tag de status presente (`@implemented`, `@wip`, `@not_implemented`)
- [ ] Tags de tipo quando aplicável (`@api`, `@database`, `@messaging`)
- [ ] Tags de funcionalidade quando aplicável (`@authentication`, `@registration`, etc.)

---

## 🔧 Validação Automática

Execute o script de validação para verificar conformidade:

```bash
./scripts/validate-tags.sh
```

O script verifica:
- ✅ Presença de tag de Business Unit
- ✅ Presença de tag de segmento
- ✅ Presença de tag de prioridade
- ⚠️ Presença de tag de status (recomendado)

---

## 📚 Referências

- **Playbook Principal**: `engineering-playbook/019.04 - BDD_E2E_TESTING_STRATEGY_EXECUTION.md`
- **Análise de Conformidade**: `docs/analysis/ANALISE_CONFORMIDADE_TAGS_CENARIOS_EXISTENTES.md`
- **Próximos Passos**: `docs/PRÓXIMOS_PASSOS_CONFORMIDADE_TAGS.md`

---

## 🆘 Dúvidas Frequentes

### Q: Posso usar múltiplas tags de prioridade?
**R**: Não. Use apenas uma tag de prioridade por feature/cenário.

### Q: Tags nos cenários são obrigatórias?
**R**: Não. Cenários herdam tags da Feature. Tags explícitas nos cenários são opcionais, mas recomendadas para cenários críticos.

### Q: Posso criar novas tags?
**R**: Sim, mas consulte a equipe primeiro e documente no playbook.

### Q: Como filtrar por múltiplas condições?
**R**: Use operadores lógicos: `and`, `or`, `not`. Exemplo: `@implemented and @critical and not @not_implemented`

---

**Última Atualização**: 2025-12-11
