# Lições Aprendidas - Testes E2E e Arquitetura Multi-Country

**Data**: 2025-12-22  
**Contexto**: Correção de falhas em testes E2E relacionados à arquitetura multi-country e validação de dados

---

## 📋 Resumo Executivo

Durante a correção de falhas nos testes E2E, identificamos e resolvemos problemas relacionados a:
1. Processamento de placeholders em feature files do Cucumber
2. Geração de documentos únicos em retries
3. Gerenciamento de `sessionToken` em fluxos de retry
4. Validação e normalização de `documentType`
5. Integração com RabbitMQ em arquitetura multi-country

**Resultado**: 202 testes executados, 0 falhas, 0 erros ✅

---

## 🎓 Conhecimentos Adquiridos

### 1. Processamento de Placeholders no Cucumber

#### Problema Identificado
- Feature files podem conter placeholders com aspas duplas: `"{unique_cpf}"` em vez de `{unique_cpf}`
- O Cucumber não remove aspas automaticamente
- Placeholders não processados causam falhas de validação no backend

#### Solução Implementada
```java
// Remover aspas duplas antes de processar placeholders
String trimmedValue = value.trim();
if (trimmedValue.startsWith("\"") && trimmedValue.endsWith("\"")) {
    trimmedValue = trimmedValue.substring(1, trimmedValue.length() - 1).trim();
}
if (trimmedValue.startsWith("{") && trimmedValue.endsWith("}")) {
    // Processar placeholder
}
```

#### Lição Aprendida
- **Sempre normalizar valores antes de processar placeholders**
- **Feature files podem ter formatação inconsistente** - o código deve ser resiliente
- **Processar placeholders múltiplas vezes** para garantir substituição completa

---

### 2. Geração de Documentos Únicos em Retries

#### Problema Identificado
- Código de retry sempre gerava CPF, independente do `documentType`
- Testes para RUT, CUIT, DNI, CI, SSN falhavam no retry
- Backend rejeitava documentos inválidos (ex: CPF quando esperava RUT)

#### Solução Implementada
```java
// Usar switch baseado no documentType para gerar documento correto
String documentNumber;
switch (documentType.toUpperCase()) {
    case "RUT":
        documentNumber = TestDataGenerator.generateUniqueRut();
        break;
    case "CUIT":
        documentNumber = TestDataGenerator.generateUniqueCuit();
        break;
    // ... outros tipos
    default:
        documentNumber = TestDataGenerator.generateUniqueCpf();
        break;
}
```

#### Lição Aprendida
- **Nunca assumir tipo de documento padrão em retries**
- **Sempre preservar o contexto original** (documentType, país, etc.)
- **Geradores de dados devem respeitar o contexto do teste**

---

### 3. Gerenciamento de SessionToken em Retries

#### Problema Identificado
- `sessionToken` é de uso único e é limpo após primeira tentativa
- Retry não executava porque verificava `useSessionToken` que era `false`
- Retry precisa criar novo OTP e `sessionToken`, não reutilizar o antigo

#### Solução Implementada
```java
// Retry sempre executa quando há 409, independente de sessionToken
if (lastResponse != null && lastResponse.getStatusCode() == 409) {
    // Limpar sessionToken antigo
    userFixture.setSessionToken(null);
    // Criar novo OTP e sessionToken
    // ... criar novo usuário com novos dados
}
```

#### Lição Aprendida
- **SessionToken é de uso único** - nunca reutilizar
- **Retries devem sempre criar novo OTP/sessionToken**
- **Não depender de estado anterior em retries** - sempre recriar o necessário
- **Limpar sessionToken apenas após sucesso** (201/200), não após erros

---

### 4. Validação e Normalização de documentType

#### Problema Identificado
- Feature files podem ter `documentType` com aspas: `"CPF"`
- Backend espera valores uppercase: `CPF`, `CNPJ`, `RUT`, etc.
- Testes de validação precisam que `documentType` seja `null` quando ausente

#### Solução Implementada
```java
// 1. Remover aspas duplas
if (documentType.startsWith("\"") && documentType.endsWith("\"")) {
    documentType = documentType.substring(1, documentType.length() - 1).trim();
}

// 2. Normalizar para uppercase
documentType = documentType.toUpperCase().trim();

// 3. Não incluir no request quando null (para testes de validação)
if (documentType != null && !documentType.trim().isEmpty()) {
    request.put("documentType", documentType);
} else {
    // Não adicionar - permite que backend valide
}
```

#### Lição Aprendida
- **Sempre normalizar dados antes de enviar ao backend**
- **Backend é a fonte de verdade para validações** - não validar no código de teste
- **Campos null devem ser omitidos** para testes de validação funcionarem
- **Aspas duplas podem aparecer em feature files** - sempre tratar

---

### 5. Arquitetura Multi-Country e RabbitMQ

#### Problema Identificado
- Eventos de VS-CustomerCommunications são publicados no vhost `/shared`
- Testes E2E estavam configurados apenas para vhost `/br`
- Timeouts ao consumir eventos do `/shared`

#### Solução Implementada
- `RabbitMQHelper` configurado para múltiplos vhosts (`/br` e `/shared`)
- `AuthService` faz dual-publishing de eventos específicos
- Helm charts e Docker Compose atualizados com vhosts corretos

#### Lição Aprendida
- **Ambiente de desenvolvimento deve espelhar produção**
- **Virtual hosts do RabbitMQ são críticos para isolamento**
- **Testes E2E devem suportar múltiplos vhosts**
- **Documentar estratégia de vhosts por vertical de serviço**

---

## 🔧 Padrões e Boas Práticas Identificadas

### 1. Processamento de Dados de Teste

#### ✅ Fazer
- Normalizar valores antes de processar (trim, uppercase, remover aspas)
- Processar placeholders múltiplas vezes até não haver mais placeholders
- Validar formato antes de usar (ex: email deve conter "@")
- Preservar contexto original em retries (documentType, país, etc.)

#### ❌ Evitar
- Assumir formato de dados sem normalização
- Processar placeholders apenas uma vez
- Validar dados no código de teste (deixar backend validar)
- Assumir tipo padrão em retries

---

### 2. Gerenciamento de Tokens e Sessões

#### ✅ Fazer
- Criar novo OTP/sessionToken em cada retry
- Limpar sessionToken apenas após sucesso (201/200)
- Verificar se sessionToken está presente antes de usar
- Logar sessionToken (parcialmente) para debug

#### ❌ Evitar
- Reutilizar sessionToken (é de uso único)
- Limpar sessionToken antes de verificar se retry é necessário
- Assumir que sessionToken está disponível
- Logar sessionToken completo (segurança)

---

### 3. Retries e Tratamento de Erros

#### ✅ Fazer
- Executar retry baseado em status HTTP, não em estado interno
- Gerar novos dados únicos respeitando o contexto original
- Criar novo OTP/sessionToken em cada retry
- Limitar número de retries (ex: 5 tentativas)

#### ❌ Evitar
- Depender de estado interno para decidir retry
- Gerar dados sem considerar contexto (documentType, país)
- Reutilizar tokens em retries
- Retries infinitos

---

### 4. Integração com Backend

#### ✅ Fazer
- Backend é a fonte de verdade para validações
- Enviar dados normalizados (uppercase, sem aspas)
- Omitir campos null para testes de validação
- Logar request body antes de enviar (para debug)

#### ❌ Evitar
- Validar dados no código de teste
- Enviar dados não normalizados
- Incluir campos null quando não necessário
- Assumir formato de resposta do backend

---

## 🚨 Anti-Padrões Identificados

### 1. Hardcoding de Tipos de Documento
```java
// ❌ ERRADO
userData.put("documentNumber", TestDataGenerator.generateUniqueCpf());

// ✅ CORRETO
String documentNumber = generateDocumentByType(documentType);
userData.put("documentNumber", documentNumber);
```

### 2. Validação no Código de Teste
```java
// ❌ ERRADO
if (email == null || !email.contains("@")) {
    throw new IllegalArgumentException("Email inválido");
}

// ✅ CORRETO
// Enviar ao backend e deixar ele validar
// Backend retornará erro apropriado
```

### 3. Reutilização de Tokens
```java
// ❌ ERRADO
if (sessionToken != null) {
    // Reutilizar sessionToken
}

// ✅ CORRETO
// SessionToken é de uso único - sempre criar novo em retries
userFixture.setSessionToken(null);
// Criar novo OTP e sessionToken
```

### 4. Processamento de Placeholders Incompleto
```java
// ❌ ERRADO
if (value.startsWith("{") && value.endsWith("}")) {
    // Processar
}

// ✅ CORRETO
// Remover aspas primeiro
String trimmed = value.trim();
if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
    trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
}
if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
    // Processar
}
```

---

## 📚 Conhecimentos Técnicos Adquiridos

### 1. Cucumber e Gherkin
- **Placeholders**: Podem vir com aspas do feature file
- **DataTable**: Valores podem ter formatação inconsistente
- **Scenario Outline**: Substitui placeholders antes de passar para step definitions
- **Processamento**: Deve ser feito múltiplas vezes para garantir substituição completa

### 2. Spring AMQP e RabbitMQ
- **Virtual Hosts**: Críticos para isolamento em arquitetura multi-country
- **Dual Publishing**: Serviços podem publicar em múltiplos vhosts
- **ConnectionFactory**: Deve ser explícito com `@Primary` e `@Qualifier`
- **Testes E2E**: Devem suportar múltiplos vhosts

### 3. Gerenciamento de Estado em Testes
- **SessionToken**: De uso único, não pode ser reutilizado
- **Retries**: Devem recriar todo o estado necessário
- **Contexto**: Deve ser preservado (documentType, país, etc.)
- **Limpeza**: Apenas após sucesso, não após erros

### 4. Validação de Dados
- **Backend é fonte de verdade**: Não validar no código de teste
- **Normalização**: Sempre normalizar antes de enviar
- **Campos null**: Omitir para testes de validação funcionarem
- **Formato**: Backend pode ser mais restritivo que esperado

---

## 🎯 Recomendações para o Futuro

### 1. Documentação
- ✅ Documentar estratégia de vhosts por vertical de serviço
- ✅ Documentar padrões de geração de dados únicos
- ✅ Documentar fluxo de OTP/sessionToken em testes
- ✅ Documentar tratamento de placeholders

### 2. Código
- ✅ Criar utilitários para normalização de dados
- ✅ Criar utilitários para geração de documentos por tipo
- ✅ Adicionar logging detalhado em pontos críticos
- ✅ Validar formato de dados antes de processar

### 3. Testes
- ✅ Testes unitários para processamento de placeholders
- ✅ Testes unitários para geração de documentos
- ✅ Testes de integração para fluxo de OTP/sessionToken
- ✅ Validação de formatação de feature files

### 4. Processo
- ✅ Code review focado em normalização de dados
- ✅ Validação de feature files antes de commit
- ✅ Documentar padrões em playbooks
- ✅ Compartilhar lições aprendidas com time

---

## 📊 Métricas de Sucesso

### Antes das Correções
- **Testes executados**: 202
- **Falhas**: 60
- **Erros**: 3
- **Taxa de sucesso**: ~70%

### Depois das Correções
- **Testes executados**: 202
- **Falhas**: 0
- **Erros**: 0
- **Taxa de sucesso**: 100% ✅

### Problemas Resolvidos
1. ✅ 7 falhas de placeholders não processados
2. ✅ 1 falha de `registration-token` header ausente
3. ✅ 1 falha de retry com documento incorreto
4. ✅ 1 erro de email inválido em teste de validação
5. ✅ 50+ falhas relacionadas a sessionToken inválido/expirado

---

## 🔍 Debugging e Troubleshooting

### Técnicas Utilizadas
1. **Logging detalhado**: Adicionado em pontos críticos do fluxo
2. **Validação incremental**: Testar cada etapa separadamente
3. **Isolamento de problemas**: Identificar causa raiz antes de corrigir
4. **Testes unitários**: Criar testes específicos para validar comportamento

### Ferramentas Utilizadas
- **Logs do Maven**: Análise de falhas e stack traces
- **Allure Reports**: Anexar dados de debug
- **Logging estruturado**: Usar emojis e prefixos para facilitar busca
- **Testes unitários**: Validar serialização e processamento

---

## 💡 Insights Importantes

### 1. Ambiente de Desenvolvimento vs Produção
- **Crítico**: Ambiente de desenvolvimento deve espelhar produção
- **Virtual hosts**: Devem ser configurados corretamente desde o início
- **Configuração**: Docker Compose e Helm charts devem estar alinhados

### 2. Testes E2E são Complexos
- **Múltiplas camadas**: Feature files → Step definitions → Clients → Backend
- **Estado compartilhado**: Precisa ser gerenciado cuidadosamente
- **Retries**: Devem recriar todo o estado necessário

### 3. Normalização é Fundamental
- **Dados inconsistentes**: Podem causar falhas silenciosas
- **Feature files**: Podem ter formatação inconsistente
- **Backend**: Pode ser mais restritivo que esperado

### 4. Tokens e Sessões
- **Uso único**: SessionToken não pode ser reutilizado
- **Retries**: Devem sempre criar novos tokens
- **Limpeza**: Apenas após sucesso, não após erros

---

## 📖 Referências e Documentação

### Arquivos Criados/Atualizados
1. `ANALISE_LOGS_MULTI_COUNTRY.md` - Análise inicial dos problemas
2. `RESULTADO_IMPLEMENTACAO_MULTI_VHOST.md` - Resolução de problemas RabbitMQ
3. `CORRECOES_VALIDACAO_DADOS.md` - Correções de validação
4. `CORRECOES_FINAIS_DOCUMENTTYPE.md` - Correções finais de documentType
5. `PROBLEMA_SESSIONTOKEN.md` - Análise de problemas de sessionToken
6. `DESCOBERTA_SERIALIZACAO.md` - Descobertas sobre serialização JSON
7. `RESUMO_INVESTIGACAO.md` - Resumo da investigação
8. `SOLUCAO_FINAL_DOCUMENTTYPE.md` - Solução final

### Playbooks Atualizados
1. `022.00 - MULTI-COUNTRY-ARCHITECTURE-STRATEGY.md`
2. `010.00 - RABBITMQ_RESILIENCE_STRATEGY.md`
3. `003.00 - APPLICATION-YML-CONFIGURATION-STRATEGY.md`
4. `015.00 - DOCKER-COMMUNICATION-AND-API-ROUTING-STRATEGY.md`

---

## 🎓 Conclusão

Esta sessão de debugging e correção demonstrou a importância de:
- **Normalização consistente de dados**
- **Gerenciamento cuidadoso de estado em testes**
- **Resiliência a formatação inconsistente**
- **Documentação clara de padrões e anti-padrões**
- **Ambiente de desenvolvimento alinhado com produção**

As lições aprendidas devem ser incorporadas em:
- **Code reviews**
- **Novos desenvolvimentos**
- **Documentação de padrões**
- **Treinamento de novos membros do time**

---

**Última atualização**: 2025-12-22  
**Status**: ✅ Todos os testes E2E passando (202/202)

