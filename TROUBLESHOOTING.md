# Guia de Troubleshooting - Platform Journey Tests

Este guia ajuda a resolver problemas comuns ao executar testes E2E.

## 🔍 Problemas Comuns e Soluções

### **1. Erro de Compilação**

**Sintomas**:
- `mvn compile` falha
- Erros de classe não encontrada
- Erros de import

**Soluções**:
```bash
# Limpar e recompilar
mvn clean compile test-compile

# Verificar dependências
mvn dependency:tree

# Verificar versão do Java
java -version  # Deve ser Java 21
```

### **2. Serviços não estão acessíveis**

**Sintomas**:
- `Connection refused` ao chamar microserviços
- Timeouts em chamadas HTTP
- Erro 503 Service Unavailable

**Soluções**:
```bash
# Verificar serviços estão rodando
curl http://localhost:8084/actuator/health  # Identity Service
curl http://localhost:8080/actuator/health  # Auth Service
curl http://localhost:8088/actuator/health  # Profile Service

# Verificar conectividade de rede
ping localhost
telnet localhost 8084

# Verificar configuração de URLs
cat src/main/resources/application-local.yml
```

### **3. Timeout em chamadas HTTP**

**Sintomas**:
- Testes falham com `SocketTimeoutException`
- Respostas lentas dos microserviços

**Soluções**:
```bash
# Verificar timeout configurado
grep timeout src/main/resources/application-local.yml

# Aumentar timeout se necessário (editar application-local.yml)
e2e:
  timeout: 60000  # Aumentar de 30000 para 60000
```

### **4. Eventos assíncronos não chegam**

**Sintomas**:
- Testes falham aguardando eventos RabbitMQ
- Timeout ao verificar eventos
- Eventos chegam após timeout

**Soluções**:
```bash
# Verificar RabbitMQ está rodando
docker ps | grep rabbitmq
curl http://localhost:15672  # Management UI

# Verificar conectividade RabbitMQ
telnet localhost 5672

# Verificar configuração
grep rabbitmq src/main/resources/application-local.yml
```

**No código**:
```java
// Aumentar timeout em step definitions se necessário
await().atMost(60, SECONDS)  // Aumentar de 30 para 60
    .pollInterval(500, MILLISECONDS)
    .until(() -> eventReceived(eventType));
```

### **5. Testes Flaky (Inconsistentes)**

**Sintomas**:
- Testes passam às vezes e falham outras vezes
- Falhas intermitentes sem mudança de código

**Soluções**:
- ✅ Sempre usar dados únicos (UUID + timestamp)
- ✅ Evitar `Thread.sleep()` - usar `Awaitility`
- ✅ Garantir idempotência dos testes
- ✅ Evitar dependências de ordem de execução

### **6. Step Definitions não encontrados**

**Sintomas**:
- `StepDefinitionNotFoundException`
- Steps ambíguos (múltiplas definições)

**Soluções**:
```bash
# Verificar step definitions compilam
mvn test-compile

# Verificar steps duplicados
grep -r "@Quando\|@Dado\|@Então" src/test/java/com/nulote/journey/stepdefinitions/

# Verificar package do glue
grep GLUE_PROPERTY_NAME src/test/java/com/nulote/journey/runners/CucumberTestRunner.java
```

### **7. Relatórios não gerados**

**Sintomas**:
- Relatórios não aparecem em `target/cucumber-reports/`
- Relatórios vazios

**Soluções**:
```bash
# Criar diretório se não existir
mkdir -p target/cucumber-reports

# Verificar permissões
chmod -R 755 target/cucumber-reports

# Verificar configuração de plugins no Runner
grep PLUGIN_PROPERTY_NAME src/test/java/com/nulote/journey/runners/CucumberTestRunner.java
```

### **8. Erro ao conectar RabbitMQ**

**Sintomas**:
- `java.net.ConnectException` ao conectar RabbitMQ
- Erro de autenticação

**Soluções**:
```bash
# Verificar RabbitMQ está rodando
docker ps | grep rabbitmq

# Verificar credenciais
grep -A 4 rabbitmq src/main/resources/application-local.yml

# Testar conexão manual
rabbitmqadmin -H localhost -u guest -p guest list queues
```

### **9. Dados de teste conflitantes**

**Sintomas**:
- Constraint violations (CPF duplicado, email duplicado)
- Testes falham por dados já existentes

**Soluções**:
- ✅ Sempre usar `TestDataGenerator.generateUniqueEmail()`
- ✅ Usar `ExecutionContext.tagWithExecutionId()` para rastreabilidade
- ✅ Verificar antes de criar (idempotência)

### **10. Ambiente incorreto**

**Sintomas**:
- Testes conectam em ambiente errado
- URLs incorretas

**Soluções**:
```bash
# Verificar profile ativo
echo $SPRING_PROFILES_ACTIVE

# Executar com profile explícito
mvn test -Dspring.profiles.active=local

# Verificar configuração carregada
# Adicionar log no E2EConfiguration para debug
```

## 🛠️ Comandos Úteis de Debug

```bash
# Compilar e verificar erros
mvn clean compile test-compile

# Executar apenas validação de estrutura
mvn test-compile -Dcucumber.filter.tags="@nonexistent"

# Executar teste específico
mvn test -Dcucumber.features="src/test/resources/features/authentication/login.feature"

# Executar com debug
mvn test -Dspring.profiles.active=local -X

# Verificar dependências
mvn dependency:tree | grep cucumber

# Limpar e recompilar tudo
mvn clean install -DskipTests
```

## 📞 Suporte

Para mais detalhes, consulte:
- Nota técnica completa: `technical-note/bdd-e2e-testing-strategy.md`
- Seção Troubleshooting na nota técnica (linha 3902+)

