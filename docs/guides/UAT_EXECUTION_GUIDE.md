# Guia de Execução de Testes contra Ambiente UAT

## 🎯 Objetivo

Este guia explica como executar os testes E2E a partir da sua máquina local contra o ambiente de **UAT (User Acceptance Testing)**.

---

## 📋 Pré-requisitos

### **1. Conectividade com Ambiente UAT**

Antes de executar os testes, você precisa garantir:

- ✅ **Acesso à rede do ambiente UAT** (VPN, proxy, ou acesso direto)
- ✅ **URLs dos serviços UAT** disponíveis
- ✅ **Credenciais de acesso** (se necessário)
- ✅ **Firewall configurado** para permitir conexões

### **2. Informações Necessárias**

Você precisará das seguintes informações do ambiente UAT:

- URL do Identity Service
- URL do Auth Service  
- URL do Profile Service
- Host do RabbitMQ (se necessário)
- Credenciais de teste (se necessário)

---

## 🔧 Configuração

### **Opção 1: Usando Variáveis de Ambiente (Recomendado)**

A configuração atual já suporta variáveis de ambiente. Basta definir as URLs antes de executar:

```bash
# Definir URLs do ambiente UAT
export UAT_IDENTITY_URL="https://identity-service.uat.exemplo.com.br"
export UAT_AUTH_URL="https://auth-service.uat.exemplo.com.br"
export UAT_PROFILE_URL="https://profile-service.uat.exemplo.com.br"

# Opcional: Configurar RabbitMQ se necessário
export UAT_RABBITMQ_HOST="rabbitmq.uat.exemplo.com.br"
export UAT_RABBITMQ_PORT="5672"
export UAT_RABBITMQ_USERNAME="usuario"
export UAT_RABBITMQ_PASSWORD="senha"

# Executar testes
mvn test -Dspring.profiles.active=uat -Dcucumber.filter.tags="@e2e and not @not_implemented"
```

### **Opção 2: Modificando application-uat.yml**

Se preferir, você pode editar diretamente o arquivo `src/main/resources/application-uat.yml`:

```yaml
e2e:
  environment: uat
  services:
    identity-url: https://identity-service.uat.exemplo.com.br
    auth-url: https://auth-service.uat.exemplo.com.br
    profile-url: https://profile-service.uat.exemplo.com.br
  timeout: 90000

rabbitmq:
  host: rabbitmq.uat.exemplo.com.br
  port: 5672
  username: usuario
  password: senha
```

> **⚠️ Atenção**: Se modificar o arquivo diretamente, **não faça commit** dessas alterações no repositório, pois URLs podem variar entre desenvolvedores ou conter informações sensíveis.

### **Opção 3: Criando application-uat-local.yml (Recomendado para Desenvolvimento)**

Para evitar modificar o arquivo compartilhado, você pode criar um arquivo local que não será versionado:

```bash
# Criar arquivo local (não versionado)
cat > src/main/resources/application-uat-local.yml << EOF
e2e:
  environment: uat
  services:
    identity-url: https://identity-service.uat.exemplo.com.br
    auth-url: https://auth-service.uat.exemplo.com.br
    profile-url: https://profile-service.uat.exemplo.com.br
  timeout: 90000

rabbitmq:
  host: rabbitmq.uat.exemplo.com.br
  port: 5672
  username: usuario
  password: senha
EOF

# Adicionar ao .gitignore para não versionar
echo "application-uat-local.yml" >> .gitignore
```

Depois, execute com o profile `uat-local`:

```bash
mvn test -Dspring.profiles.active=uat-local -Dcucumber.filter.tags="@e2e and not @not_implemented"
```

---

## 🚀 Execução

### **Comando Básico**

```bash
mvn test -Dspring.profiles.active=uat -Dcucumber.filter.tags="@e2e and not @not_implemented"
```

### **Executar Apenas Testes Críticos**

```bash
mvn test -Dspring.profiles.active=uat -Dcucumber.filter.tags="@critical and @implemented"
```

### **Executar Testes de um Segmento Específico**

```bash
mvn test -Dspring.profiles.active=uat -Dcucumber.filter.tags="@segment_1 and @implemented"
```

### **Executar com Logs Detalhados**

```bash
mvn test -Dspring.profiles.active=uat \
  -Dcucumber.filter.tags="@e2e and not @not_implemented" \
  -X
```

### **Executar com Timeout Aumentado**

Se os testes estiverem demorando mais devido à latência de rede:

```bash
# Definir timeout maior via variável de ambiente
export UAT_TIMEOUT=120000

mvn test -Dspring.profiles.active=uat \
  -Dcucumber.filter.tags="@e2e and not @not_implemented"
```

---

## 🔍 Verificação de Conectividade

Antes de executar os testes completos, você pode verificar a conectividade:

### **Teste Manual de Conectividade**

```bash
# Verificar se os serviços estão acessíveis
curl -v https://identity-service.uat.exemplo.com.br/health
curl -v https://auth-service.uat.exemplo.com.br/health
curl -v https://profile-service.uat.exemplo.com.br/health
```

### **Teste via Maven (Dry Run)**

```bash
# Executar apenas um teste simples para validar conectividade
mvn test -Dspring.profiles.active=uat \
  -Dcucumber.filter.tags="@smoke and @implemented" \
  -Dcucumber.execution.dry-run=true
```

---

## 🌐 Considerações de Rede

### **VPN**

Se o ambiente UAT requer VPN:

1. **Conecte-se à VPN** antes de executar os testes
2. **Verifique conectividade** com `ping` ou `curl`
3. **Execute os testes** normalmente

```bash
# Exemplo: Conectar VPN (ajuste conforme sua ferramenta)
# openconnect vpn.empresa.com.br
# ou
# sudo vpnc config.conf

# Verificar conectividade
ping identity-service.uat.exemplo.com.br

# Executar testes
mvn test -Dspring.profiles.active=uat ...
```

### **Proxy**

Se você precisa usar proxy:

```bash
# Configurar proxy para Maven
export MAVEN_OPTS="-Dhttp.proxyHost=proxy.exemplo.com.br -Dhttp.proxyPort=8080 -Dhttps.proxyHost=proxy.exemplo.com.br -Dhttps.proxyPort=8080"

# Executar testes
mvn test -Dspring.profiles.active=uat ...
```

Ou configure no `~/.m2/settings.xml`:

```xml
<proxies>
  <proxy>
    <id>proxy</id>
    <active>true</active>
    <protocol>http</protocol>
    <host>proxy.exemplo.com.br</host>
    <port>8080</port>
  </proxy>
</proxies>
```

### **Firewall**

Se houver problemas de firewall:

1. **Verifique regras** de firewall local
2. **Solicite acesso** à equipe de infraestrutura
3. **Teste conectividade** antes de executar testes completos

---

## 📊 Exemplos Práticos

### **Exemplo 1: Execução Completa**

```bash
#!/bin/bash
# Script para executar testes UAT

# Configurar URLs
export UAT_IDENTITY_URL="https://identity-service.uat.exemplo.com.br"
export UAT_AUTH_URL="https://auth-service.uat.exemplo.com.br"
export UAT_PROFILE_URL="https://profile-service.uat.exemplo.com.br"

# Executar testes
mvn clean test \
  -Dspring.profiles.active=uat \
  -Dcucumber.filter.tags="@e2e and not @not_implemented" \
  -Dcucumber.plugin="pretty,html:target/cucumber-reports/uat.html,json:target/cucumber-reports/uat.json"

# Abrir relatório
open target/cucumber-reports/uat.html
```

### **Exemplo 2: Execução com Validação Prévia**

```bash
#!/bin/bash
# Script com validação de conectividade

UAT_IDENTITY_URL="https://identity-service.uat.exemplo.com.br"
UAT_AUTH_URL="https://auth-service.uat.exemplo.com.br"
UAT_PROFILE_URL="https://profile-service.uat.exemplo.com.br"

echo "🔍 Verificando conectividade..."

# Verificar cada serviço
for url in "$UAT_IDENTITY_URL" "$UAT_AUTH_URL" "$UAT_PROFILE_URL"; do
  if curl -f -s "$url/health" > /dev/null 2>&1; then
    echo "✅ $url está acessível"
  else
    echo "❌ $url não está acessível"
    exit 1
  fi
done

echo "✅ Todos os serviços estão acessíveis"
echo "🚀 Executando testes..."

export UAT_IDENTITY_URL
export UAT_AUTH_URL
export UAT_PROFILE_URL

mvn test -Dspring.profiles.active=uat \
  -Dcucumber.filter.tags="@e2e and not @not_implemented"
```

### **Exemplo 3: Execução com Retry**

```bash
#!/bin/bash
# Script com retry em caso de falha de rede

MAX_RETRIES=3
RETRY_COUNT=0

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
  echo "Tentativa $((RETRY_COUNT + 1))/$MAX_RETRIES"
  
  mvn test -Dspring.profiles.active=uat \
    -Dcucumber.filter.tags="@e2e and not @not_implemented"
  
  if [ $? -eq 0 ]; then
    echo "✅ Testes executados com sucesso"
    exit 0
  fi
  
  RETRY_COUNT=$((RETRY_COUNT + 1))
  echo "⏳ Aguardando antes de tentar novamente..."
  sleep 5
done

echo "❌ Testes falharam após $MAX_RETRIES tentativas"
exit 1
```

---

## 🔐 Segurança

### **Credenciais**

⚠️ **NUNCA** commite credenciais ou URLs sensíveis no repositório!

- Use **variáveis de ambiente** para credenciais
- Use **arquivos locais** (`.gitignore`) para configurações pessoais
- Use **secrets** no CI/CD para ambientes remotos

### **Boas Práticas**

1. ✅ Use variáveis de ambiente para URLs e credenciais
2. ✅ Crie arquivos locais não versionados para configurações pessoais
3. ✅ Documente URLs padrão sem credenciais
4. ✅ Use `.gitignore` para arquivos locais sensíveis

---

## 🐛 Troubleshooting

### **Problema: Connection Refused**

```
java.net.ConnectException: Connection refused
```

**Solução**:
- Verifique se está conectado à VPN
- Verifique se as URLs estão corretas
- Teste conectividade manual com `curl`

### **Problema: Timeout**

```
java.net.SocketTimeoutException: Read timed out
```

**Solução**:
- Aumente o timeout no `application-uat.yml`
- Verifique latência de rede
- Execute testes menores primeiro

### **Problema: SSL Certificate**

```
javax.net.ssl.SSLHandshakeException
```

**Solução**:
- Verifique certificados SSL
- Configure truststore se necessário
- Use `-Djavax.net.ssl.trustStore` se necessário

### **Problema: 401 Unauthorized**

```
401 Unauthorized
```

**Solução**:
- Verifique se precisa de autenticação prévia
- Configure credenciais se necessário
- Verifique tokens de acesso

---

## 📝 Checklist de Execução

Antes de executar testes contra UAT:

- [ ] Conectividade verificada (VPN, proxy, firewall)
- [ ] URLs dos serviços configuradas
- [ ] Variáveis de ambiente definidas (ou arquivo local criado)
- [ ] Conectividade testada manualmente (`curl`)
- [ ] Timeout configurado adequadamente
- [ ] Tags de teste selecionadas apropriadamente
- [ ] Relatórios configurados para análise

---

## 🔗 Referências

- **README.md** - Documentação geral do projeto
- **QUICK_REFERENCE.md** - Referência rápida de comandos
- **application-uat.yml** - Configuração padrão do ambiente UAT
- **engineering-playbook/bdd-e2e-testing-strategy.md** - Estratégia completa de testes

---

**Última atualização**: 2025-11-14

