# Guia: Testes de Fluxo Completo de Registro com OTP

**Data de Criação**: 2025-12-09  
**Última Atualização**: 2025-12-09  
**Status**: ✅ Ativo  
**Versão**: 1.0

---

## 📋 Introdução

Este guia descreve os testes E2E criados para validar o fluxo completo de registro com OTP, baseados nos testes manuais executados. Os testes cobrem os principais fluxos de autenticação e registro, incluindo simulação de providers e um teste específico com envio real de email.

## 🎯 Objetivo

Validar todos os fluxos principais de autenticação e registro:
1. **Registro completo com OTP** (com simulação)
2. **Recuperação de senha** (com simulação)
3. **Primeiro acesso** (com simulação)
4. **Registro com envio real** (apenas para validação específica)

## 📖 Detalhes

### 📁 Arquivos Criados/Modificados

### Features
- `src/test/resources/features/authentication/complete_registration_flow.feature`
  - Feature completa com 4 cenários principais

### Step Definitions
- `src/test/java/com/nulote/journey/stepdefinitions/AuthenticationSteps.java`
  - Adicionados novos steps para suportar fluxo completo:
    - `eu devo receber um sessionToken válido`
    - `eu envio os dados para criar identidade com o sessionToken`
    - `eu solicito OTP via ... sem simulação`
    - `eu valido o OTP informando o código do email real`
    - `eu redefino minha senha com o OTP validado`
    - `que as credenciais foram provisionadas`
    - `o sistema deve solicitar alteração de senha obrigatória`

### Clients
- `src/test/java/com/nulote/journey/clients/AuthServiceClient.java`
  - Adicionado método `requestOtpWithoutSimulation()` para envio real
- `src/test/java/com/nulote/journey/clients/IdentityServiceClient.java`
  - Adicionado método `createUserWithSessionToken()` para criar usuário com token de registro

### Fixtures
- `src/test/java/com/nulote/journey/fixtures/UserFixture.java`
  - Adicionado suporte para `sessionToken`
  - Ajustado `buildOtpRequest()` para suportar REGISTRATION sem userUuid
  - Adicionado email no request de OTP para REGISTRATION

### 🧪 Cenários de Teste

### 1. Registro Completo com OTP via EMAIL (com simulação)
**Tags**: `@simulate-provider @otp_request @otp_validation`

Fluxo completo de registro usando OTP com simulação de provider:
- Solicita OTP via EMAIL
- Recebe código do evento RabbitMQ
- Valida OTP
- Obtém sessionToken
- Cria usuário com sessionToken
- Valida provisionamento de credenciais
- Valida eventos publicados

### 2. Recuperação de Senha Completa (com simulação)
**Tags**: `@simulate-provider @password_recovery`

Fluxo completo de recuperação de senha:
- Solicita recuperação de senha
- Recebe código OTP
- Valida OTP
- Redefine senha

### 3. Primeiro Acesso Após Registro (com simulação)
**Tags**: `@simulate-provider @first_access`

Validação do primeiro acesso:
- Cria usuário
- Aguarda provisionamento de credenciais
- Faz login
- Valida que alteração de senha é obrigatória

### 4. Registro Completo com Envio Real de Email
**Tags**: `@real-provider @production-test @glaucio-teixeira @manual`

**⚠️ ATENÇÃO**: Este teste faz envio REAL de email!

- Usa email: `glaucio.teixeira@outlook.com`
- CPF: `87853310668`
- **Requer intervenção manual** para obter código OTP do email
- Use apenas quando necessário validar integração real com provider

## 🚀 Início Rápido

### Executar todos os testes com simulação
```bash
mvn test -Dspring.profiles.active=local -Dcucumber.filter.tags="@complete_registration_flow and @simulate-provider"
```

### Executar apenas registro completo (simulação)
```bash
mvn test -Dspring.profiles.active=local -Dcucumber.filter.tags="@otp_request and @otp_validation and @simulate-provider"
```

### Executar teste com envio real (CUIDADO!)
```bash
# ⚠️ Este teste envia email REAL!
mvn test -Dspring.profiles.active=local -Dcucumber.filter.tags="@real-provider and @glaucio-teixeira"
```

## 🔧 Exemplos

### Exemplo de Execução Completa

```bash
# 1. Executar teste de registro com simulação
mvn test -Dspring.profiles.active=local \
  -Dcucumber.filter.tags="@simulate-provider and @otp_request and @otp_validation"

# 2. Verificar resultados
cat target/cucumber-reports/cucumber.json | jq '.[] | select(.elements[].tags[].name == "@simulate-provider")'
```

### Exemplo de Teste com Envio Real

```bash
# ⚠️ ATENÇÃO: Este comando envia email REAL para glaucio.teixeira@outlook.com
mvn test -Dspring.profiles.active=local \
  -Dcucumber.filter.tags="@real-provider and @glaucio-teixeira"

# Após execução, verificar email para obter código OTP
# Em seguida, executar manualmente o step de validação com o código obtido
```

---

## 📝 Notas Importantes

### Simulação de Provider
- Por padrão, todos os testes usam `simulate-provider: true`
- Isso evita envio real de emails/SMS durante testes
- O código OTP é obtido do evento RabbitMQ

### Teste com Envio Real
- Apenas o cenário marcado com `@real-provider` faz envio real
- Requer intervenção manual para obter código OTP do email
- Use apenas quando necessário validar integração com provider real

### Headers de Correlação
- Os testes usam `request-trace-id` automaticamente
- Headers `request-caller` e `request-origin` são adicionados pelos serviços automaticamente em ambiente de teste

## 🔍 Validações Realizadas

### Persistência
- ✅ Usuário criado no Identity Service
- ✅ Usuário sincronizado no Auth Service
- ✅ Credenciais provisionadas automaticamente
- ✅ Version = 0 (correto após correção)

### Eventos RabbitMQ
- ✅ `otp.sent` publicado
- ✅ `otp.validated` publicado
- ✅ `user.created.v1` publicado
- ✅ `credentials.provisioned.v1` publicado

### Fluxos
- ✅ Solicitar OTP
- ✅ Validar OTP
- ✅ Criar usuário
- ✅ Password Recovery
- ✅ Primeiro acesso

## 🐛 Troubleshooting

### OTP não é recebido do evento
- Verifique se RabbitMQ está configurado
- Verifique se o evento está sendo publicado
- Aumente timeout em `application.yml` se necessário

### SessionToken não é obtido
- Verifique se OTP foi validado com sucesso
- Verifique logs do auth-service
- Confirme que a resposta contém `sessionToken`

### Credenciais não são provisionadas
- Aguarde alguns segundos (processamento assíncrono)
- Verifique logs do `UserCreatedConsumer`
- Verifique se evento `user.created.v1` foi publicado

## 📚 Referências

- **[Estratégia de Testes E2E](../../../engineering-playbook/bdd-e2e-testing-strategy.md)** - Estratégia de testes E2E com BDD
- **[Simulação de Providers](RESUMO_EXECUTIVO_NORMALIZACAO_SIMULACAO.md)** - Resumo executivo de normalização de simulação
- **[Configuração de Ambientes](CONFIGURATION_SUMMARY.md)** - Resumo de configuração por ambiente
- **[Guia de Tags de Teste](TEST_TAGS_GUIDE.md)** - Guia de tags de teste
- **[Troubleshooting](TROUBLESHOOTING.md)** - Guia de troubleshooting

---

## 📝 Versão e Histórico

**Versão**: 1.0  
**Data de Criação**: 2025-12-09  
**Última Atualização**: 2025-12-09  
**Autor**: Baseado em testes E2E manuais executados

### **Changelog**

- **v1.0 (2025-12-09)**: 
  - ✅ Criação inicial do guia
  - ✅ Documentação de 4 cenários de teste E2E
  - ✅ Implementação de step definitions necessários
  - ✅ Suporte para simulação e envio real de providers
  - ✅ Documentação de arquivos criados/modificados
