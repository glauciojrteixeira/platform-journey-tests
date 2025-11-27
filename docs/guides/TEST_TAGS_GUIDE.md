# Guia de Tags dos Testes E2E

## 📋 Tags Disponíveis

### **Status de Implementação**

- `@implemented` - Funcionalidade está implementada e funcionando
- `@not_implemented` - Funcionalidade não está implementada (teste será pulado)
- `@partial` - Funcionalidade parcialmente implementada ou com limitações

### **Dependências**

- `@otp_required` - Teste depende de OTP que não está implementado
- `@requires_credentials_setup` - Teste requer criação manual de credenciais
- `@may_require_auth` - Teste pode requerer autenticação adicional

### **Segmentos e Jornadas**

- `@segment_1` - Compradores Ocasionais (PF - B2C)
- `@segment_2` - Arrematadores Profissionais (PF - B2C)
- `@segment_3` - Revendedores e Lojistas (PJ - B2B)
- `@segment_4` - Plataformas de Leilão (PJ - B2B Enterprise)
- `@j1.1`, `@j1.2`, etc. - Identificadores de jornadas específicas

### **Funcionalidades**

- `@registration` - Registro de usuário
- `@authentication` - Autenticação/login
- `@password` - Recuperação de senha
- `@identity` - Operações de identidade
- `@legal_entity` - Entidades jurídicas
- `@profile` - Perfis de usuário

### **Prioridade**

- `@critical` - Crítico para o negócio
- `@high` - Alta prioridade
- `@medium` - Média prioridade
- `@low` - Baixa prioridade

### **Tipo de Teste**

- `@e2e` - Teste end-to-end completo
- `@integration` - Teste de integração
- `@unit` - Teste unitário

---

## 🎯 Como Executar Testes por Tag

### Executar apenas testes implementados:
```bash
mvn test -Dcucumber.filter.tags="@implemented and @e2e"
```

### Executar testes que não dependem de OTP:
```bash
mvn test -Dcucumber.filter.tags="@e2e and not @otp_required"
```

### Executar testes de um segmento específico:
```bash
mvn test -Dcucumber.filter.tags="@segment_1 and @e2e"
```

### Executar apenas testes críticos:
```bash
mvn test -Dcucumber.filter.tags="@critical and @e2e"
```

### Pular testes não implementados:
```bash
mvn test -Dcucumber.filter.tags="@e2e and not @not_implemented"
```

---

## 📊 Status Atual por Tag

### ✅ Implementado e Funcionando
- `@implemented` + `@registration` - Criação de identidade básica
- `@implemented` + `@authentication` - Login básico
- `@implemented` + validações de dados

### ❌ Não Implementado
- `@not_implemented` + `@otp_required` - Todos os testes de OTP
- `@not_implemented` + `@password` - Recuperação de senha (depende de OTP)

### ⚠️ Parcialmente Implementado
- `@partial` + `@registration` - Registro completo (sem OTP)
- `@partial` + `@authentication` - Login após registro (pode precisar setup)
- `@partial` + `@legal_entity` - Entidade jurídica (pode requerer auth)

---

## 🔄 Atualização de Tags

Conforme funcionalidades são implementadas, atualize as tags:

1. Quando OTP for implementado:
   - Remover `@not_implemented` e `@otp_required`
   - Adicionar `@implemented`

2. Quando provisionamento automático de credenciais for implementado:
   - Remover `@requires_credentials_setup`
   - Atualizar cenários para validar criação automática

3. Quando eventos RabbitMQ estiverem configurados:
   - Ativar validações de eventos nos cenários
   - Remover comentários sobre eventos

---

**Última atualização**: 2025-11-14

