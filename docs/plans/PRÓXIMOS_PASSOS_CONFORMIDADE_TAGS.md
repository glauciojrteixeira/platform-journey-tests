# Próximos Passos: Conformidade de Tags

**Data de Criação**: 2025-12-11  
**Status**: ✅ Tags Corrigidas - Próximos Passos  
**Versão**: 1.0

---

## ✅ Status Atual

Todas as **tags obrigatórias** foram adicionadas nas Features:
- ✅ `@vs-identity` em 39 arquivos
- ✅ Tags de segmento em todos os arquivos
- ✅ Tags de prioridade em todos os arquivos
- ✅ Arquivos transversais corrigidos

---

## 🎯 Próximos Passos Recomendados

### 1. ⏳ **Validação Funcional das Tags**

**Objetivo**: Garantir que os filtros por tags funcionam corretamente.

**Ações**:
```bash
# Testar filtro por Business Unit
mvn test -Dspring.profiles.active=local -Dcucumber.filter.tags="@vs-identity" -Dtest=CucumberTestRunner

# Testar filtro por segmento
mvn test -Dspring.profiles.active=local -Dcucumber.filter.tags="@segment_1" -Dtest=CucumberTestRunner

# Testar filtro por prioridade
mvn test -Dspring.profiles.active=local -Dcucumber.filter.tags="@critical" -Dtest=CucumberTestRunner

# Testar filtro combinado (implementados e críticos)
mvn test -Dspring.profiles.active=local -Dcucumber.filter.tags="@implemented and @critical" -Dtest=CucumberTestRunner

# Testar exclusão de não implementados
mvn test -Dspring.profiles.active=local -Dcucumber.filter.tags="@vs-identity and not @not_implemented" -Dtest=CucumberTestRunner
```

**Validação Esperada**:
- ✅ Apenas cenários com a tag especificada são executados
- ✅ Contagem de cenários corresponde ao esperado
- ✅ Relatórios gerados corretamente

---

### 2. ⏳ **Adicionar Tags Explícitas nos Cenários Críticos (Opcional)**

**Objetivo**: Tornar os cenários mais explícitos e facilitar filtros granulares.

**Estratégia**:
- Adicionar tags nos cenários críticos (`@critical`, `@smoke`)
- Adicionar tags de tipo (`@api`, `@database`, `@messaging`) quando aplicável
- Manter tags de funcionalidade específicas

**Exemplo**:
```gherkin
@implemented @vs-identity @segment_1 @j1.1 @identity @critical @e2e
Feature: Criação de Identidade
  ...
  @vs-identity @segment_1 @j1.1 @identity @critical @api @database @smoke
  Scenario: Criação de identidade bem-sucedida
    ...
```

**Prioridade**: ⚠️ **Baixa** - Cenários já herdam tags da Feature, mas tags explícitas facilitam filtros mais granulares.

---

### 3. ⏳ **Atualizar CI/CD Pipelines**

**Objetivo**: Garantir que pipelines usam filtros corretos por tags.

**Verificações**:
- [ ] Pipeline de **LOCAL** executa todos os `@implemented`
- [ ] Pipeline de **SIT** executa apenas `@implemented` (exclui `@not_implemented`)
- [ ] Pipeline de **UAT** executa apenas `@implemented and @critical`
- [ ] Pipeline de **PROD** não executa testes E2E (ou apenas smoke tests)

**Exemplo de configuração**:
```yaml
# .github/workflows/e2e-tests.yml ou similar
jobs:
  e2e-sit:
    steps:
      - name: Run E2E Tests (SIT)
        run: mvn test -Dspring.profiles.active=sit -Dcucumber.filter.tags="@implemented and not @not_implemented"
  
  e2e-uat:
    steps:
      - name: Run E2E Tests (UAT)
        run: mvn test -Dspring.profiles.active=uat -Dcucumber.filter.tags="@implemented and @critical"
```

---

### 4. ⏳ **Documentar Convenções de Tags**

**Objetivo**: Criar guia rápido de referência para a equipe.

**Conteúdo Sugerido**:
- Lista de tags obrigatórias
- Lista de tags opcionais e quando usar
- Exemplos de filtros comuns
- Convenções de nomenclatura

**Localização**: 
- `VS-QA/platform-journey-tests/docs/TAGS_GUIDE.md` ou
- Atualizar `engineering-playbook/019.04` se necessário

---

### 5. ⏳ **Criar Script de Validação Automática**

**Objetivo**: Automatizar verificação de conformidade de tags.

**Script Sugerido** (`scripts/validate-tags.sh`):
```bash
#!/bin/bash
# Valida conformidade de tags em arquivos .feature

echo "=== Validação de Tags ==="

# Verificar @vs-identity
echo "Verificando @vs-identity..."
missing_vs=$(find src/test/resources/features -name "*.feature" -not -path "*/cross-vs/*" -not -path "*/vs-customer-communications/*" | xargs grep -L "@vs-identity\|@vs-customer-communications\|@cross-bu" | wc -l)
if [ "$missing_vs" -gt 0 ]; then
  echo "❌ $missing_vs arquivos sem tag de Business Unit"
  exit 1
fi

# Verificar @segment_
echo "Verificando @segment_..."
missing_segment=$(find src/test/resources/features -name "*.feature" -not -path "*/cross-vs/*" -not -path "*/vs-customer-communications/*" | xargs grep -L "@segment_" | wc -l)
if [ "$missing_segment" -gt 0 ]; then
  echo "❌ $missing_segment arquivos sem tag de segmento"
  exit 1
fi

# Verificar prioridade
echo "Verificando prioridade..."
missing_priority=$(find src/test/resources/features -name "*.feature" -not -path "*/cross-vs/*" -not -path "*/vs-customer-communications/*" | xargs grep -L "@critical\|@high\|@medium\|@low" | wc -l)
if [ "$missing_priority" -gt 0 ]; then
  echo "❌ $missing_priority arquivos sem tag de prioridade"
  exit 1
fi

echo "✅ Todas as tags obrigatórias estão presentes!"
exit 0
```

**Integração**: Adicionar ao pipeline de CI/CD como etapa de validação.

---

### 6. ⏳ **Revisar Relatórios de Testes**

**Objetivo**: Garantir que relatórios refletem tags corretamente.

**Verificações**:
- [ ] Relatórios HTML mostram tags corretamente
- [ ] Relatórios JSON incluem tags para análise
- [ ] Agrupamento por tags funciona nos relatórios
- [ ] Métricas por tag estão disponíveis

**Ferramentas**:
- Cucumber HTML Reports
- Allure Reports (se configurado)
- Relatórios customizados

---

### 7. ⏳ **Treinamento da Equipe**

**Objetivo**: Garantir que todos conhecem as convenções de tags.

**Ações**:
- [ ] Compartilhar documento de análise de conformidade
- [ ] Apresentar guia de tags em reunião de equipe
- [ ] Criar checklist para novos cenários
- [ ] Documentar exemplos práticos

---

## 📋 Checklist de Implementação

### Imediato (Esta Sprint)
- [ ] Executar validação funcional das tags (Passo 1)
- [ ] Verificar pipelines CI/CD (Passo 3)
- [ ] Criar script de validação (Passo 5)

### Curto Prazo (Próximas 2 Sprints)
- [ ] Adicionar tags explícitas em cenários críticos (Passo 2)
- [ ] Documentar convenções (Passo 4)
- [ ] Revisar relatórios (Passo 6)

### Médio Prazo (Próximo Mês)
- [ ] Treinamento da equipe (Passo 7)
- [ ] Automatizar validação no CI/CD
- [ ] Criar dashboard de métricas por tags

---

## 🎯 Priorização

| Passo | Prioridade | Esforço | Impacto | Quando |
|-------|------------|---------|---------|--------|
| 1. Validação Funcional | 🔴 **Alta** | Baixo | Alto | **Agora** |
| 3. Atualizar CI/CD | 🔴 **Alta** | Médio | Alto | **Esta Sprint** |
| 5. Script Validação | 🟡 **Média** | Baixo | Médio | **Esta Sprint** |
| 2. Tags Explícitas | 🟢 **Baixa** | Alto | Baixo | **Futuro** |
| 4. Documentação | 🟡 **Média** | Médio | Médio | **Próxima Sprint** |
| 6. Revisar Relatórios | 🟡 **Média** | Baixo | Médio | **Próxima Sprint** |
| 7. Treinamento | 🟢 **Baixa** | Médio | Baixo | **Quando necessário** |

---

## 📝 Notas

- **Tags explícitas nos cenários**: Opcional, mas recomendado para cenários críticos
- **Validação automática**: Deve ser integrada ao CI/CD para prevenir regressões
- **Documentação**: Manter atualizada conforme novas tags são adicionadas

---

**Última Atualização**: 2025-12-11
