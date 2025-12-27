# Análise de Erros dos Testes

## ✅ Problemas Corrigidos

### 1. NullPointerException em `lastResponse`
**Erro**: `Cannot invoke "io.restassured.response.Response.getStatusCode()" because "this.lastResponse" is null`

**Causa**: O step "eu envio os dados para criar identidade" retornava early quando o usuário já existia, sem definir `lastResponse`.

**Correção**: Agora consulta o usuário existente e define `lastResponse` com a resposta, evitando NullPointerException.

**Arquivos afetados**:
- `AuthenticationSteps.java` - método `eu_envio_os_dados_para_criar_identidade()`

---

## ⚠️ Problemas de Infraestrutura/Backend

### 2. IP Bloqueado (403) - Múltiplos Testes
**Erro**: `"IP address is blocked" - IP address blocked due to X failed login attempts`

**Microserviço**: **Auth Service** (porta 8080)  
**Endpoint**: `POST /api/v1/auth/login`  
**Código de Erro**: `AU-A-BUS010`

**Causa**: O IP de teste (`138.68.11.125`) foi bloqueado por muitas tentativas de login falhadas acumuladas de múltiplos testes E2E.

**Mecanismo de Proteção**:
- ✅ **É comportamento esperado** em produção (proteção contra brute force)
- ⚠️ **Problema em testes E2E**: Múltiplos testes fazem login, alguns falham intencionalmente (testes de erro), e o IP acumula tentativas falhadas até ser bloqueado

**Impacto**: Afeta múltiplos testes de login:
- Login bem-sucedido após registro
- Login falha com credenciais inválidas
- Login falha com usuário não encontrado
- Login recorrente com token expirado/válido
- Primeiro acesso após registro
- Primeiro login após registro
- Logout (precisa estar autenticado primeiro)

**Soluções Recomendadas** (ver `BLOQUEIO_IP_ANALISE.md` para detalhes):
1. **Whitelist de IPs** para ambiente de teste no Auth Service (recomendado)
2. **Endpoint administrativo** para limpar bloqueios antes dos testes
3. **Aumentar limite** de tentativas apenas em ambiente de teste
4. Aguardar período de bloqueio expirar (não ideal)

**Nota**: Este é um mecanismo de segurança legítimo, mas precisa de configuração especial para ambiente de teste.

---

## 🔍 Problemas que Precisam Investigação

### 3. Alteração de Senha - Erro 401
**Erro**: `"Invalid username or password"` - Status 401 ao invés de 200/204

**Cenário**: "Alteração de senha bem-sucedida"

**Possíveis causas**:
- Token JWT (`currentJwtToken`) está inválido ou expirado
- Senha atual está incorreta (pode não estar sincronizada entre criação e alteração)
- Usuário não está realmente autenticado

**Arquivo**: `AuthenticationSteps.java` - método `eu_altero_minha_senha()`

**Próximos passos**:
1. Verificar se `currentJwtToken` está sendo definido corretamente no step "que já estou autenticado na plataforma"
2. Verificar se a senha usada na criação do usuário é a mesma usada na alteração
3. Adicionar logs para debug do token JWT

---

### 4. Atualização de Perfil - Erro 404 ao invés de 400
**Erro**: Esperava 400 (dados inválidos) mas recebeu 404 (não encontrado)

**Cenários afetados**:
- "Atualização de perfil falha com dados inválidos"
- "Tentativa de alterar dados de segurança via perfil"

**Possível causa**: O perfil não existe quando o teste tenta atualizá-lo.

**Próximos passos**:
1. Verificar se o perfil está sendo criado automaticamente após criação do usuário
2. Adicionar step para garantir que perfil existe antes de tentar atualizar
3. Verificar se o UUID do usuário está correto

---

### 5. Desativação de Conta - Erro 500
**Erro**: Status 500 ao invés de 200/204

**Cenário**: "Dados são mantidos após desativação (LGPD)"

**Causa**: Problema no backend ao desativar conta.

**Próximos passos**:
- Verificar logs do backend
- Verificar se o endpoint de desativação está funcionando corretamente

---

### 6. Rate Limiting de OTP - Não Funciona
**Erro**: Esperava 429 (Too Many Requests) mas recebeu 200

**Cenário**: "Rate limiting impede múltiplas solicitações de OTP"

**Possível causa**: 
- Rate limiting não está implementado no backend
- Limite de requisições está muito alto
- Teste não está fazendo requisições suficientes para atingir o limite

**Próximos passos**:
1. Verificar se rate limiting está implementado no backend
2. Verificar qual é o limite configurado
3. Ajustar teste para fazer mais requisições se necessário

---

### 7. Registro com Email Inválido - lastResponse null
**Erro**: `Expecting actual not to be null`

**Cenário**: "Registro falha com email inválido"

**Status**: ✅ Deve estar corrigido com a correção do NullPointerException

**Próximos passos**: Re-executar teste para verificar

---

### 8. Múltiplas Solicitações de OTP - Header simulate-provider
**Erro**: `Expecting actual: 0 to be greater than: 0`

**Cenário**: "Múltiplas solicitações de OTP devem incluir header simulate-provider"

**Possível causa**: 
- Evento RabbitMQ não está sendo publicado
- Header `simulate-provider` não está sendo incluído nas mensagens
- Consumo de mensagens do RabbitMQ não está funcionando

**Próximos passos**:
1. Verificar se RabbitMQ está configurado corretamente
2. Verificar se eventos estão sendo publicados
3. Verificar se o header `simulate-provider` está sendo incluído

---

## 📊 Resumo

- **Total de testes**: 128
- **Falhas**: 18
- **Erros**: 6
- **Pulados**: 94

### Por Categoria:

1. **IP Bloqueado (403)**: ~10 testes afetados (problema de infraestrutura)
2. **NullPointerException**: 6 testes (✅ CORRIGIDO)
3. **Problemas de Backend**: 3-4 testes (500, 404, 401)
4. **Rate Limiting**: 1 teste (pode não estar implementado)
5. **RabbitMQ/Eventos**: 1 teste (configuração)

---

## 🎯 Prioridades

1. **Alta**: Resolver bloqueio de IP (afeta muitos testes)
2. **Média**: Investigar alteração de senha (401)
3. **Média**: Investigar atualização de perfil (404)
4. **Baixa**: Rate limiting (pode não estar implementado)
5. **Baixa**: RabbitMQ (verificar configuração)
