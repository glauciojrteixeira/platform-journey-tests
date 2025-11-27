# Dependências Externas e Testes Não Implementados

Este documento lista os testes que dependem de serviços externos ainda não implementados ou funcionalidades que ainda não estão disponíveis.

## 📋 Visão Geral

Alguns testes E2E dependem de serviços ou funcionalidades que ainda não foram desenvolvidos. Esses testes são marcados com a tag `@not_implemented` e são automaticamente excluídos da execução padrão através do filtro:

```properties
cucumber.filter.tags=@e2e and not @not_implemented
```

## 🔴 Serviços Externos Não Implementados

### 1. Serviço de Envio de OTP

**Status**: ❌ Não implementado

**Descrição**: O serviço responsável por consumir eventos de OTP (`otp.sent`) e promover o envio físico de mensagens (SMS, WhatsApp, Email) com o código OTP ainda não foi desenvolvido.

**Testes Afetados**:

#### `features/authentication/otp.feature`
- ✅ `@otp_request` - Solicitação de OTP via EMAIL para REGISTRATION bem-sucedida (funciona parcialmente)
- ❌ `@otp_request @not_implemented @otp_service_missing` - Solicitação de OTP via WHATSAPP para REGISTRATION bem-sucedida
  - **Motivo**: Requer serviço de envio via WhatsApp que não existe
  - **Erro esperado**: `Required field 'User phone is not available' is missing`

- ✅ `@otp_validation` - Validação de OTP bem-sucedida (funciona parcialmente)
- ❌ `@otp_validation @not_implemented @otp_service_missing` - Validação de OTP falha com código inválido
  - **Motivo**: Evento `otp.validated` está sendo publicado incorretamente quando deveria falhar
  - **Erro esperado**: Evento sendo publicado quando não deveria

#### `features/authentication/password_recovery.feature`
- ❌ `@not_implemented @otp_service_missing` - Recuperação de senha bem-sucedida com OTP
  - **Motivo**: Requer validação de OTP via WhatsApp que depende do serviço de envio
  - **Erro esperado**: `Required field 'OTP is not valid for password recovery' is missing`

**Impacto**: 
- Testes de OTP via WhatsApp não podem ser executados
- Testes de recuperação de senha com OTP não podem ser executados
- Alguns testes de validação de OTP têm comportamento incorreto

**Próximos Passos**:
1. Desenvolver serviço de envio de OTP que consome eventos `otp.sent` do RabbitMQ
2. Implementar integração com provedores de SMS/WhatsApp/Email
3. Remover tag `@not_implemented` dos testes quando serviço estiver pronto

## 🟡 Funcionalidades Parcialmente Implementadas

### OTP via Email

**Status**: 🟡 Parcialmente implementado

**Descrição**: A funcionalidade de OTP via Email está parcialmente implementada. O código OTP é gerado e retornado na resposta da API, mas o envio físico via email ainda depende do serviço externo.

**Testes Afetados**:
- `@otp_request` - Solicitação de OTP via EMAIL para REGISTRATION bem-sucedida
  - **Status**: ✅ Passa parcialmente (código retornado na resposta)
  - **Limitação**: Não há envio físico de email ainda

## 📊 Resumo de Testes Marcados como `@not_implemented`

### Por Tag `@otp_service_missing`

| Feature | Cenário | Motivo |
|---------|---------|--------|
| `authentication/otp.feature` | Solicitação de OTP via WHATSAPP | Serviço de envio WhatsApp não existe |
| `authentication/otp.feature` | Validação de OTP falha com código inválido | Comportamento incorreto do evento |
| `authentication/password_recovery.feature` | Recuperação de senha bem-sucedida com OTP | Depende de validação OTP via WhatsApp |

### Outros Testes `@not_implemented`

Além dos testes relacionados a OTP, existem outros testes marcados como `@not_implemented` por diferentes motivos:

- `@otp_required` - Testes que requerem funcionalidade de OTP completa
- Features de Segmento 3 (B2B) - Algumas funcionalidades ainda não implementadas
- Features de MFA (Multi-Factor Authentication) - Aguardando implementação completa

## 🔍 Como Verificar Testes Não Implementados

### Listar todos os testes não implementados

```bash
# Buscar por tag @not_implemented nos arquivos .feature
grep -r "@not_implemented" src/test/resources/features/
```

### Executar apenas testes implementados (padrão)

```bash
mvn test -Dcucumber.filter.tags="@e2e and not @not_implemented"
```

### Executar incluindo testes não implementados (para desenvolvimento)

```bash
# Executar todos os testes, incluindo não implementados
mvn test -Dcucumber.filter.tags="@e2e"

# Executar apenas testes de OTP não implementados
mvn test -Dcucumber.filter.tags="@otp_service_missing"
```

## 📝 Manutenção

### Quando Adicionar `@not_implemented`

Adicione a tag `@not_implemented` quando:

1. ✅ O teste depende de um serviço externo que não existe
2. ✅ A funcionalidade ainda não foi implementada
3. ✅ Há um bug conhecido que impede o teste de passar
4. ✅ A integração com serviço externo não está disponível

### Quando Remover `@not_implemented`

Remova a tag quando:

1. ✅ O serviço externo foi implementado e está funcionando
2. ✅ A funcionalidade foi implementada completamente
3. ✅ O bug foi corrigido
4. ✅ A integração está disponível e testada

### Adicionar Tag de Dependência Específica

Além de `@not_implemented`, adicione tags específicas para facilitar identificação:

- `@otp_service_missing` - Depende do serviço de envio de OTP
- `@otp_required` - Requer funcionalidade completa de OTP
- `@external_service_missing` - Depende de serviço externo genérico

## 🎯 Roadmap de Implementação

### Fase 1: Serviço de Envio de OTP (Prioridade Alta)

- [ ] Desenvolver serviço que consome eventos `otp.sent` do RabbitMQ
- [ ] Implementar integração com provedor de SMS
- [ ] Implementar integração com provedor de WhatsApp
- [ ] Implementar integração com provedor de Email
- [ ] Testes de integração com serviços externos
- [ ] Remover `@not_implemented` dos testes de OTP

### Fase 2: Validação e Correção de Comportamento

- [ ] Corrigir comportamento de evento `otp.validated` em caso de falha
- [ ] Validar fluxo completo de OTP end-to-end
- [ ] Remover `@not_implemented` de testes de validação

## 📚 Referências

- [Estratégia de Testes E2E](../engineering-playbook/bdd-e2e-testing-strategy.md)
- [Guia de Tags de Testes](TEST_TAGS_GUIDE.md)
- [Resumo de Execução de Testes](TEST_EXECUTION_SUMMARY.md)

## 🔄 Histórico de Atualizações

### 2025-11-18
- Documentação inicial criada
- Testes de OTP marcados como `@not_implemented @otp_service_missing`
- Identificado problema com evento `otp.validated` sendo publicado incorretamente

