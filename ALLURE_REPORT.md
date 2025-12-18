# Allure Report - Guia de Uso

## 📋 Visão Geral

O projeto utiliza **Allure Report** para gerar relatórios detalhados e interativos dos testes E2E. O Allure oferece visualização rica com screenshots, logs, respostas HTTP e histórico de execuções.

## 🚀 Como Usar

### **1. Executar Testes e Gerar Relatório**

```bash
# Executar testes (gera resultados Allure automaticamente)
mvn clean test

# Gerar relatório Allure HTML
mvn allure:report

# Abrir relatório no navegador (servidor temporário)
mvn allure:serve
```

### **2. Localização dos Arquivos**

Após executar os testes, os arquivos do Allure estarão em:

```
target/
├── allure-results/          # Resultados brutos do Allure
│   ├── *.json
│   └── *.txt
└── site/
    └── allure-maven-plugin/ # Relatório HTML gerado
        └── index.html
```

### **3. Usando AllureHelper nos Step Definitions**

O `AllureHelper` facilita a adição de informações aos relatórios:

```java
import com.nulote.journey.utils.AllureHelper;

@Quando("eu envio os dados para criar identidade")
public void eu_envio_os_dados_para_criar_identidade() {
    // Marcar step no Allure
    AllureHelper.step("Enviando dados para criar identidade");
    
    // Adicionar texto ao relatório
    AllureHelper.attachText("Request: " + request.toString());
    
    // Fazer chamada HTTP
    lastResponse = identityClient.createUser(request);
    
    // Anexar resposta HTTP completa (status, headers, body)
    AllureHelper.attachHttpResponse(lastResponse, "criar identidade");
}

@Então("a identidade deve ser criada com sucesso")
public void a_identidade_deve_ser_criada_com_sucesso() {
    AllureHelper.step("Validando criação de identidade");
    
    // Validações...
    var userUuid = lastResponse.jsonPath().getString("uuid");
    AllureHelper.attachText("UUID criado: " + userUuid);
}
```

### **4. Métodos Disponíveis no AllureHelper**

| Método | Descrição | Exemplo |
|--------|-----------|---------|
| `step(String)` | Marca um step no Allure | `AllureHelper.step("Criando usuário")` |
| `attachText(String)` | Adiciona texto ao relatório | `AllureHelper.attachText("Log: ...")` |
| `attachJson(String)` | Adiciona JSON ao relatório | `AllureHelper.attachJson(responseBody)` |
| `attachScreenshot(byte[])` | Adiciona screenshot | `AllureHelper.attachScreenshot(imageBytes)` |
| `attachHttpResponse(Response, String)` | Adiciona resposta HTTP completa | `AllureHelper.attachHttpResponse(response, "step")` |
| `attachLog(String, String)` | Adiciona log estruturado | `AllureHelper.attachLog("Mensagem", "INFO")` |

## 📊 Visualizando Relatórios

### **Localmente**

```bash
# Após executar testes
mvn allure:serve

# O relatório abrirá automaticamente no navegador
# Geralmente em: http://localhost:port
```

### **No CI/CD**

Os relatórios são gerados automaticamente e podem ser publicados como artefatos:

- **GitHub Actions**: Use `actions/upload-artifact` para publicar `target/site/allure-maven-plugin/`
- **GitLab CI**: Configure `artifacts` para incluir `target/site/allure-maven-plugin/`

## 🔍 Recursos do Allure Report

- ✅ **Visualização Interativa**: Interface moderna e fácil de navegar
- ✅ **Screenshots**: Capturas automáticas em caso de falhas
- ✅ **Logs Detalhados**: Logs estruturados de cada step
- ✅ **Respostas HTTP**: Status codes, headers e bodies completos
- ✅ **Histórico**: Comparação entre execuções
- ✅ **Gráficos**: Estatísticas visuais de sucesso/falha
- ✅ **Filtros**: Por tags, features, scenarios

## 📝 Boas Práticas

1. **Sempre marque steps importantes**: Use `AllureHelper.step()` para marcar etapas críticas
2. **Anexe respostas HTTP em falhas**: Facilita debugging
3. **Use textos descritivos**: Facilita compreensão dos relatórios
4. **Anexe logs relevantes**: Apenas informações úteis para debugging
5. **Não anexe dados sensíveis**: Evite senhas, tokens, etc.

## 🐛 Troubleshooting

### **Relatório não é gerado**

```bash
# Verificar se testes foram executados
ls target/allure-results/

# Regenerar relatório
mvn allure:report
```

### **Allure serve não abre**

```bash
# Verificar porta disponível
mvn allure:serve -Dallure.results.directory=target/allure-results

# Ou especificar porta manualmente
mvn allure:serve -Dserver.port=8080
```

### **Dependências não encontradas**

```bash
# Atualizar dependências
mvn clean install

# Verificar versão do Allure
mvn dependency:tree | grep allure
```

## 📚 Referências

- [Documentação Allure](https://docs.qameta.io/allure/)
- [Allure Cucumber Integration](https://docs.qameta.io/allure/#_cucumber_jvm)
- [Allure Maven Plugin](https://github.com/allure-framework/allure-maven)

