# Guia Rápido de Referência

## 🚀 Executar Testes

### **Executar todos os testes implementados**
```bash
mvn test -Dspring.profiles.active=local
```

### **Executar apenas testes críticos**
```bash
mvn test -Dspring.profiles.active=local -Dcucumber.filter.tags="@critical"
```

### **Executar testes de um segmento específico**
```bash
mvn test -Dspring.profiles.active=local -Dcucumber.filter.tags="@segment_1"
```

### **Executar testes excluindo não implementados**
```bash
mvn test -Dspring.profiles.active=local -Dcucumber.filter.tags="@e2e and not @not_implemented"
```

### **Executar testes de uma jornada específica**
```bash
mvn test -Dspring.profiles.active=local -Dcucumber.filter.tags="@j1.1"
```

---

## 📋 Tags Principais

| Tag | Descrição |
|-----|-----------|
| `@e2e` | Todos os testes E2E |
| `@not_implemented` | Funcionalidade não implementada |
| `@otp_required` | Requer OTP (não implementado) |
| `@partial` | Implementação parcial |
| `@critical` | Testes críticos |
| `@segment_1` | Segmento 1 - Compradores Ocasionais |
| `@segment_2` | Segmento 2 - Arrematadores Profissionais |
| `@segment_3` | Segmento 3 - Empresas |
| `@segment_4` | Segmento 4 - Plataformas |
| `@j1.1`, `@j1.2`, etc. | Jornadas específicas |

---

## 🔧 Comandos Úteis

### **Compilar apenas**
```bash
mvn clean test-compile
```

### **Executar com logs detalhados**
```bash
mvn test -Dspring.profiles.active=local -X
```

### **Executar em ambiente SIT**
```bash
# Configurar URLs via variáveis de ambiente
export SIT_IDENTITY_URL="https://identity-service.sit.exemplo.com.br"
export SIT_AUTH_URL="https://auth-service.sit.exemplo.com.br"
export SIT_PROFILE_URL="https://profile-service.sit.exemplo.com.br"

mvn test -Dspring.profiles.active=sit -Dcucumber.filter.tags="@e2e and not @not_implemented"
```

### **Executar em ambiente UAT**
```bash
# 1. Configurar URLs via variáveis de ambiente
export UAT_IDENTITY_URL="https://identity-service.uat.exemplo.com.br"
export UAT_AUTH_URL="https://auth-service.uat.exemplo.com.br"
export UAT_PROFILE_URL="https://profile-service.uat.exemplo.com.br"

# 2. Executar testes
mvn test -Dspring.profiles.active=uat -Dcucumber.filter.tags="@e2e and not @not_implemented"
```

> 📖 **Guia Completo**: Consulte `UAT_EXECUTION_GUIDE.md` para detalhes sobre VPN, proxy, conectividade e troubleshooting.

---

## 📁 Estrutura de Features

```
features/
├── authentication/     # Autenticação e registro
├── identity/         # Gestão de identidade
├── profile/          # Perfil do usuário
├── journeys/         # Jornadas completas
├── segment_2/       # Arrematadores profissionais
├── segment_3/        # Empresas
├── segment_4/        # Plataformas
└── transversal/       # Funcionalidades transversais
```

---

## 🐛 Troubleshooting

### **Problema: CPF duplicado (409)**
- **Solução**: Retry automático implementado
- **Se persistir**: Limpar dados de teste ou aguardar entre execuções

### **Problema: Login retorna 401**
- **Causa**: Credenciais podem não ser criadas automaticamente após registro
- **Solução**: Aguardar implementação de provisionamento automático

### **Problema: Testes não executam**
- **Verificar**: Serviços estão rodando?
- **Verificar**: Configuração de ambiente está correta?
- **Verificar**: Dependências Maven instaladas?

### **Problema: RabbitMQ não disponível**
- **Solução**: Testes continuam executando (RabbitMQ é opcional)
- **Logs**: Warnings são logados mas não falham testes

---

## 📊 Interpretando Resultados

### **Tests run: 114**
- Total de testes executados

### **Failures: 15**
- Alguns esperados (serviços não implementados)
- Verificar logs para detalhes específicos

### **Errors: 1**
- Erros de configuração ou step definitions faltantes
- Verificar stack trace

### **Skipped: 96**
- Testes com `@not_implemented` sendo corretamente pulados
- ✅ Tags funcionando!

---

## 🔍 Encontrar Step Definitions

### **Por arquivo**
- `AuthenticationSteps.java` - Autenticação e registro
- `IdentitySteps.java` - Gestão de identidade
- `ProfileSteps.java` - Perfil do usuário
- `Hooks.java` - Setup e teardown

### **Por padrão**
```bash
grep -r "@Dado\|@Quando\|@Então" src/test/java/com/nulote/journey/stepdefinitions/
```

---

## 📝 Adicionar Novo Cenário

1. **Criar feature file** em `src/test/resources/features/`
2. **Aplicar tags** apropriadas
3. **Implementar step definitions** se necessário
4. **Executar testes** para validar

---

## 🔗 Links Úteis

- **README.md** - Documentação completa
- **UAT_EXECUTION_GUIDE.md** - Guia completo para executar testes contra UAT
- **FINAL_STATUS_REPORT.md** - Status atual do projeto
- **TEST_TAGS_GUIDE.md** - Guia detalhado de tags
- **JOURNEYS_MAPPING.md** - Mapeamento de jornadas

---

**Última atualização**: 2025-11-14

