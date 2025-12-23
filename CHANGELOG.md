# Changelog - Platform Journey Tests

Todas as mudanças notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/),
e este projeto adere ao [Versionamento Semântico](https://semver.org/lang/pt-BR/).

## [0.0.9-SNAPSHOT] - 2025-12-22

### Added
- **Testes E2E para Refresh Token**: Implementação completa de testes E2E para funcionalidade de refresh token
  - `token_refresh.feature`: Feature file com 7 cenários de teste para refresh token
  - Step definitions para refresh token (20+ steps) em `AuthenticationSteps.java`
  - Método `refreshToken()` adicionado ao `AuthServiceClient` para chamar endpoint de refresh
  - Cenários implementados:
    - Renovação bem-sucedida com refresh token válido
    - Falha com refresh token nulo
    - Falha com refresh token vazio
    - Falha com refresh token inválido (formato incorreto)
    - Falha com refresh token expirado
    - Falha com refresh token revogado
    - Falha se usuário está inativo (marcado como @not_implemented - requer sincronização via eventos)
- **Correção de Geração de SSN**: Correção do método `generateUniqueSsn()` para validar todas as regras do SSN
  - Validação de Area Number (não pode ser 000, 666 ou 900-999)
  - Validação de Group Number (não pode ser 00)
  - Validação de Serial Number (não pode ser 0000)
  - Geração de SSNs válidos para testes E2E

### Changed
- **AuthenticationSteps**: Adicionadas step definitions para refresh token
  - 20+ novas step definitions para cenários de refresh token
  - Validações ajustadas para mensagens genéricas da API
  - Integração com AuthServiceClient.refreshToken()
- **TestDataGenerator**: Correção do método generateUniqueSsn()
  - Validação completa de todas as regras do SSN
  - Geração de SSNs válidos para testes E2E

## [0.0.8-SNAPSHOT] - 2025-12-22

### Added
- **Documentação de Análise e Otimização**: Documentação completa de análises de performance e otimizações
  - `ANALISE_OTIMIZACAO_CENARIOS.md`: Análise detalhada de otimização de cenários de teste
  - `OTIMIZACAO_PERFORMANCE.md`: Estratégias e melhorias de performance implementadas
  - `OTIMIZACOES_CENARIOS_IMPLEMENTADAS.md`: Otimizações de cenários implementadas
  - `RESULTADOS_FINAIS_OTIMIZACAO.md`: Resultados finais das otimizações realizadas
  - `OTIMIZACOES_ADICIONAIS.md`: Otimizações adicionais implementadas
- **Documentação de Análise de Código e Recursos**: Análises detalhadas de implementação e gerenciamento de recursos
  - `ANALISE_GERENCIAMENTO_RECURSOS.md`: Análise de gerenciamento de recursos do sistema
  - `ANALISE_IMPLEMENTACAO_CODIGO_FONTE.md`: Análise detalhada da implementação do código fonte
- **Documentação de Features**: Análise e resumo de features pendentes
  - `FEATURES_PENDENTES_ANALISE.md`: Análise detalhada de features pendentes
  - `RESUMO_FEATURES_PENDENTES.md`: Resumo consolidado de features pendentes
- **Documentação de Cache**: Documentação de integração de cache de dados
  - `INTEGRACAO_CACHE_DADOS.md`: Documentação completa da integração de cache de dados
- **TestDataCache**: Nova fixture para cache de dados de teste
  - `TestDataCache.java`: Implementação de cache para dados de teste, reduzindo geração redundante
- **Script de Execução Seletiva**: Script para execução seletiva de testes
  - `scripts/selective-test-execution.sh`: Script para execução seletiva de testes baseado em tags e critérios

### Changed
- **AuthenticationSteps**: Adicionadas step definitions para refresh token
  - 20+ novas step definitions para cenários de refresh token
  - Validações ajustadas para mensagens genéricas da API
  - Integração com AuthServiceClient.refreshToken()
- **TestDataGenerator**: Correção do método generateUniqueSsn()
  - Validação completa de todas as regras do SSN
  - Geração de SSNs válidos para testes E2E
- **E2EConfiguration**: Adicionadas configurações de cache e otimizações
  - Configuração de cache de dados de teste
  - Configurações de otimização de performance
- **E2ETestConfiguration**: Melhorias com suporte a cache
  - Integração com TestDataCache para cache de dados de teste
  - Otimizações de inicialização e configuração
- **AuthenticationSteps**: Melhorias de performance e otimizações
  - Uso de cache para dados de teste quando apropriado
  - Otimizações no processamento de placeholders
  - Melhorias no gerenciamento de estado
- **ProfileSteps**: Otimizações e melhorias
  - Melhorias no tratamento de dados
  - Otimizações de performance
  - Uso de cache quando apropriado
- **MultiCountrySteps**: Melhorias e otimizações
  - Otimizações no processamento de dados multi-country
  - Melhorias no gerenciamento de estado
- **SimulateProviderSteps**: Melhorias e otimizações
  - Otimizações no processamento de eventos
  - Melhorias no consumo de mensagens RabbitMQ
- **Hooks**: Adicionada inicialização de cache
  - Inicialização do TestDataCache antes dos testes
  - Limpeza de cache após execução de testes
- **application.yml**: Configurações de cache adicionadas
  - Configurações de cache de dados de teste
  - Configurações de otimização de performance
- **Feature Files**: Otimizações e melhorias
  - `complete_registration_flow.feature`: Otimizações nos cenários
  - `registration.feature`: Melhorias e simplificações nos cenários

### Removed
- **create_identity.feature**: Feature removida e consolidada em outras features
  - Cenários movidos para features mais apropriadas
  - Consolidação de funcionalidades relacionadas

### Technical Details
- **Cache de Dados de Teste**: Implementação de cache para reduzir geração redundante de dados de teste
- **Otimizações de Performance**: Melhorias significativas na performance dos testes E2E
- **Gerenciamento de Recursos**: Melhorias no gerenciamento de recursos durante execução de testes
- **Execução Seletiva**: Suporte para execução seletiva de testes baseado em critérios específicos

## [0.0.7-SNAPSHOT] - 2025-12-22

### Added
- **Lições Aprendidas E2E**: Documentação completa das lições aprendidas durante correção de falhas em testes E2E
  - `LICOES_APRENDIDAS_E2E.md`: Documento abrangente com conhecimentos adquiridos, padrões, anti-padrões e recomendações
  - Documentação de problemas identificados e soluções implementadas
  - Métricas de sucesso: 202 testes executados, 0 falhas, 0 erros (100% de sucesso)
- **Documentação de Troubleshooting**: Documentos detalhados sobre problemas e soluções
  - `ANALISE_LOGS_MULTI_COUNTRY.md`: Análise inicial dos problemas de RabbitMQ multi-country
  - `RESULTADO_IMPLEMENTACAO_MULTI_VHOST.md`: Resolução de problemas RabbitMQ
  - `CORRECOES_VALIDACAO_DADOS.md`: Correções de validação de dados
  - `CORRECOES_FINAIS_DOCUMENTTYPE.md`: Correções finais de documentType
  - `PROBLEMA_SESSIONTOKEN.md`: Análise de problemas de sessionToken
  - `DESCOBERTA_SERIALIZACAO.md`: Descobertas sobre serialização JSON
  - `RESUMO_INVESTIGACAO.md`: Resumo da investigação
  - `SOLUCAO_FINAL_DOCUMENTTYPE.md`: Solução final
  - `CORRECAO_CRITICA_DOCUMENTTYPE.md`: Correção crítica de documentType
  - `ANALISE_PROBLEMA_DOCUMENTTYPE.md`: Análise do problema de documentType
  - `CORRECOES_DEBUG_DOCUMENTTYPE.md`: Correções de debug de documentType
- **Teste Unitário de Serialização**: `UserFixtureSerializationTest.java` para validar serialização JSON de requests
- **Feature Multi-Country Documents**: `multi_country_documents.feature` com cenários de validação de documentos por país

### Changed
- **Processamento de Placeholders**: Melhorias significativas no processamento de placeholders em feature files
  - Remoção automática de aspas duplas de placeholders (ex: `"{unique_cpf}"` → `{unique_cpf}`)
  - Processamento múltiplo de placeholders até substituição completa
  - Normalização de valores antes de processar (trim, remover aspas)
- **Geração de Documentos Únicos em Retries**: Correção crítica para preservar contexto original
  - Retry agora gera documento correto baseado no `documentType` (RUT, CUIT, DNI, CI, SSN, etc.)
  - Switch statement baseado em `documentType` em vez de sempre gerar CPF
  - Preservação de contexto original (documentType, país) em retries
- **Gerenciamento de SessionToken em Retries**: Implementação completa de gerenciamento correto
  - Retry sempre cria novo OTP/sessionToken em vez de reutilizar
  - Limpeza de sessionToken apenas após sucesso (201/200), não após erros
  - Retry baseado em status HTTP (409), não em estado interno
- **Validação e Normalização de documentType**: Implementação completa de normalização
  - Remoção de aspas duplas de documentType
  - Normalização para uppercase (CPF, CNPJ, RUT, etc.)
  - Omissão de campo quando null para testes de validação funcionarem
  - Validação contra lista de tipos aceitos pelo backend
- **RabbitMQ Multi-Country Support**: Suporte completo para múltiplos virtual hosts
  - `RabbitMQHelper` configurado para múltiplos vhosts (`/br` e `/shared`)
  - Determinação automática de vhost baseado no tipo de evento
  - Suporte para eventos de VS-Identity (`/br`) e VS-CustomerCommunications (`/shared`)
- **Step Definitions**: Melhorias significativas em todos os step definitions
  - `AuthenticationSteps`: Processamento robusto de placeholders, normalização de documentType, gerenciamento correto de sessionToken
  - `IdentitySteps`: Melhorias no tratamento de documentType e validação
  - `ProfileSteps`: Melhorias no tratamento de dados
- **UserFixture**: Melhorias na construção de requests
  - Normalização completa de documentType (uppercase, sem aspas)
  - Omissão de campos null para testes de validação
  - Logging detalhado para debugging
- **IdentityServiceClient**: Melhorias no tratamento de requests
  - Logging de request body antes de serializar
  - Tratamento correto de `registration-token` header
- **TestDataGenerator**: Melhorias na geração de dados únicos
  - Suporte para todos os tipos de documento (CPF, CNPJ, CUIT, DNI, RUT, CI, SSN)
  - Validação automática de dados gerados

### Fixed
- **Placeholders com Aspas Duplas**: Corrigido problema onde placeholders vinham com aspas (`"{unique_cpf}"`) causando falhas de validação
- **Geração de Documentos em Retries**: Corrigido problema onde retry sempre gerava CPF independente do `documentType`
- **SessionToken em Retries**: Corrigido problema onde retry não executava ou reutilizava sessionToken inválido
- **documentType Normalização**: Corrigido problema onde documentType não era normalizado corretamente (aspas, case, null)
- **RabbitMQ Multi-Country Timeouts**: Corrigidos 9 timeouts relacionados a eventos não encontrados em virtual hosts incorretos
- **Registration Token Header**: Corrigido problema onde `registration-token` header não era passado corretamente
- **Validação de Email Inválido**: Corrigido problema onde teste de validação falhava prematuramente em vez de permitir backend validar
- **Retry com Documento Incorreto**: Corrigido problema onde retry gerava documento incorreto (ex: CPF quando esperava RUT)

### Technical Details
- **Processamento de Placeholders**: Sistema robusto de processamento que remove aspas, normaliza valores e processa múltiplas vezes
- **Gerenciamento de Estado em Retries**: Preservação de contexto original e recriação completa de estado necessário
- **Normalização de Dados**: Sistema completo de normalização antes de enviar ao backend
- **RabbitMQ Multi-VHost**: Suporte completo para múltiplos virtual hosts com determinação automática
- **Validação de Dados**: Backend como fonte de verdade, código de teste apenas normaliza e envia

## [0.0.6-SNAPSHOT] - 2025-12-19

### Added
- **Multi-Country E2E Test Scenarios**: Novos cenários de teste para validação de isolamento e propagação multi-country
  - Cenário "Dados devem ser isolados por país - Idempotência por país": Valida que CPF não pode ser duplicado no mesmo país
  - Cenário "countryCode deve ser propagado entre microserviços": Valida propagação de `country-code` através de transactional-messaging → delivery-tracker → audit-compliance
  - Cenário "Sistema deve suportar múltiplos países simultaneamente": Valida criação de usuários em diferentes países (BR, AR, CL)
- **Enhanced Step Definitions**: Melhorias significativas em step definitions para suporte multi-country
  - `AuthenticationSteps`: Adicionado suporte para criação de usuário com dados dinâmicos via DataTable com placeholders (`{unique_cpf}`, `{unique_email}`, etc.)
  - `MultiCountrySteps`: Implementação completa de steps para criação de usuário em diferentes países e validação de duplicação
  - `MultiCountrySteps`: Adicionado step `eu configuro o país padrão como {string}` para configuração dinâmica de país durante execução
  - `MultiCountrySteps`: Adicionado step `eu tento criar um usuário com os mesmos dados no país {string}` para validação de idempotência por país
- **Placeholder Processing**: Sistema de processamento de placeholders em dados de teste
  - Suporte para `{unique_cpf}`, `{unique_email}`, `{unique_phone}` com variações por país (`{unique_cpf_br}`, `{unique_email_ar}`, etc.)
  - Geração automática de dados únicos usando `TestDataGenerator`
- **Multi-Service Integration**: Integração completa com múltiplos serviços para validação de propagação de `country-code`
  - Integração com `AuthServiceClient` para solicitação e validação de OTP
  - Integração com `IdentityServiceClient` para criação de usuário
  - Integração com `DeliveryTrackerServiceClient` para validação de tracking
  - Integração com `AuditComplianceServiceClient` para validação de logs de auditoria
  - Integração com `TransactionalMessagingServiceClient` para validação de mensageria

### Changed
- **AuthenticationSteps**: Refatoração para suportar criação de usuário com dados dinâmicos
  - Método `que_crio_um_usuario_com_esses_dados(DataTable)` agora processa placeholders automaticamente
  - Método auxiliar `criarUsuarioComDadosDoFixture()` extraído para reutilização
  - Método `processarPlaceholders()` adicionado para processar placeholders dinâmicos
- **MultiCountrySteps**: Implementação completa de lógica de criação de usuário com OTP
  - Step `eu_tento_criar_um_usuario_com_os_mesmos_dados_no_pais()` agora realiza criação completa de usuário (OTP request → OTP validation → user creation)
  - Logging detalhado adicionado para troubleshooting de fluxo de criação de usuário
  - Validação explícita de `sessionToken` antes de criar usuário
  - Compartilhamento de `lastResponse` com `AuthenticationSteps` via reflection para validação de erros
- **multi_country.feature**: Adicionados 3 novos cenários de teste
  - Cenário de idempotência ajustado para refletir comportamento atual do backend (CPF é único globalmente, não por país)
  - Comentários explicativos adicionados sobre limitações atuais do backend (CPF é específico do Brasil)
  - Nota sobre necessidade de atualização quando backend suportar documentos de outros países (CUIT, RUT, etc.)

### Fixed
- **UndefinedStepException**: Corrigido erro de step não definido para `que crio um usuário com esses dados:` com DataTable
- **DuplicateStepDefinition**: Corrigido erro de step duplicado removendo anotação `@Quando` redundante de `que_crio_um_usuario_com_esses_dados(DataTable)`
- **Missing Registration Token**: Corrigido erro de `registration-token header is required` implementando fluxo completo de OTP antes de criar usuário
- **Response Sharing**: Implementado compartilhamento de `lastResponse` entre `MultiCountrySteps` e `AuthenticationSteps` para validação de erros

### Technical Details
- **DataTable Processing**: Processamento de DataTables com suporte a placeholders dinâmicos
- **OTP Flow**: Implementação completa de fluxo OTP (request → validation → sessionToken) para criação de usuário
- **Multi-Country Validation**: Validação de isolamento de dados por país e propagação de `country-code` entre serviços
- **Error Handling**: Melhor tratamento de erros com logging detalhado e validações explícitas

## [0.0.5-SNAPSHOT] - 2025-12-18

### Added
- **Multi-Country Support**: Implementação completa de testes para suporte multi-país
  - Nova feature `multi_country.feature` com 4 cenários de teste
  - `MultiCountrySteps` - Step definitions para validação de suporte multi-país
  - Validação de header `country-code` em eventos RabbitMQ
  - Validação de virtual hosts RabbitMQ baseados em país
  - Validação de formato lowercase do header `country-code` (RFC 6648 compliant)
- **RabbitMQ Helper Improvements**: Melhorias significativas no RabbitMQHelper
  - Implementação de lazy connection (conexão sob demanda)
  - Tratamento resiliente de erros durante inicialização
  - Conexão automática quando necessário (consumeMessage, getQueueInfo)
  - Logs detalhados para troubleshooting
  - Suporte para virtual hosts baseados em país
- **Enhanced Logging**: Melhorias significativas no logging
  - Logs detalhados para extração e propagação de `country-code` header
  - Logs de troubleshooting com prefixo `🔧 [TROUBLESHOOTING]`
  - Melhor rastreabilidade de eventos através do sistema
- **Configuration Updates**: Atualizações de configuração
  - `E2EConfiguration`: Adicionado suporte para país padrão
  - `application.yml`: Configurações de país padrão
  - `application-local.yml`, `application-sit.yml`, `application-uat.yml`: Configurações por ambiente
- **Service Clients Updates**: Atualizações em todos os service clients
  - `AuthServiceClient`: Melhorias no tratamento de headers
  - `IdentityServiceClient`: Melhorias no tratamento de headers
  - `AuditComplianceServiceClient`: Melhorias no tratamento de headers
  - `DeliveryTrackerServiceClient`: Melhorias no tratamento de headers
  - `ProfileServiceClient`: Melhorias no tratamento de headers
  - `TransactionalMessagingServiceClient`: Melhorias no tratamento de headers
- **Feature Updates**: Atualizações em features existentes
  - `simulate_provider.feature`: Validação de header `country-code` adicionada
- **Documentation**: Documentação abrangente adicionada (40+ arquivos)
  - Documentação de conformidade, implementação, execução, troubleshooting
  - Guias de referência, análises, planos de ação
  - Status de execução, resultados, cobertura de testes

### Changed
- **RabbitMQHelper**: Refatoração completa para melhor resiliência
  - `init()` agora não falha se RabbitMQ não estiver disponível durante inicialização
  - Conexão lazy implementada (conecta quando necessário)
  - Melhor tratamento de erros e logging
  - Suporte para virtual hosts baseados em país
- **E2EConfiguration**: Adicionado suporte para país padrão
  - Nova propriedade `defaultCountryCode` para configuração de país padrão
  - Suporte para diferentes países por ambiente

### Fixed
- **RabbitMQ Connection Resilience**: Correção de problema crítico
  - `RabbitMQHelper.init()` não falha mais se RabbitMQ não estiver disponível
  - ApplicationContext agora carrega mesmo se RabbitMQ estiver indisponível
  - Conexão estabelecida de forma lazy quando necessário
  - Logs de warning informativos quando conexão inicial falha

### Technical Details
- **Multi-Country Testing**: Suporte completo para testes multi-país
  - Validação de headers `country-code` em eventos RabbitMQ
  - Validação de virtual hosts baseados em país
  - Validação de formato lowercase (RFC 6648 compliant)
- **RabbitMQ Resilience**: Conexão resiliente que não bloqueia inicialização
- **Observability**: Logs detalhados em todos os pontos críticos para facilitar debugging
- **Documentation**: Documentação completa de todas as funcionalidades e melhorias

## [0.0.4-SNAPSHOT] - 2025-12-11

### Added
- **Rate Limit Retry Configuration**: Configuração de retry para requisições que recebem rate limiting
  - Nova classe `RateLimitRetry` em `E2EConfiguration` com `maxAttempts`, `initialDelayMs` e `enabled`
  - Suporte para retry automático em requisições OTP quando rate limit é atingido
  - Configuração via `application.yml` e `application-local.yml`

- **Cross-VS Service Clients**: Novos clientes para serviços cross-VS
  - `AuditComplianceServiceClient` - Cliente para serviço de auditoria e conformidade
  - `DeliveryTrackerServiceClient` - Cliente para serviço de rastreamento de entregas
  - `TransactionalMessagingServiceClient` - Cliente para serviço de mensageria transacional
  - `CustomerCommunicationsSteps` - Step definitions para testes de comunicação com clientes

- **Cross-VS Test Features**: Novos cenários de teste para serviços cross-VS
  - Features em `src/test/resources/features/cross-vs/` para testes entre vertical services
  - Features em `src/test/resources/features/vs-customer-communications/` para testes de comunicação

- **Documentation**: Documentação abrangente de conformidade e implementação
  - `docs/EXECUCAO_PASSOS_CONFORMIDADE_TAGS.md` - Execução de passos de conformidade de tags
  - `docs/LICOES_APRENDIDAS_E2E_TESTING.md` - Lições aprendidas em testes E2E
  - `docs/STATUS_FINAL_IMPLEMENTACAO.md` - Status final da implementação
  - `docs/analysis/ANALISE_COBERTURA_CROSS_VS.md` - Análise de cobertura cross-VS
  - `docs/analysis/ANALISE_CONFORMIDADE_TAGS_CENARIOS_EXISTENTES.md` - Análise de conformidade de tags
  - `docs/analysis/COMPARACAO_CENARIOS_DOCUMENTACAO_VS_IMPLEMENTACAO.md` - Comparação de cenários
  - `docs/guides/TAGS_REFERENCE_GUIDE.md` - Guia de referência de tags
  - `docs/verification/IMPLEMENTACAO_COMPLETA_CROSS_VS.md` - Verificação de implementação cross-VS
  - `docs/verification/IMPLEMENTACAO_CROSS_VS_SUMMARY.md` - Resumo de implementação cross-VS

- **Scripts**: Scripts utilitários para execução e validação
  - Scripts em `scripts/` para facilitar execução de testes

### Changed
- **E2EConfiguration**: Adicionados novos serviços na configuração
  - `transactionalMessagingUrl` - URL do serviço de mensageria transacional
  - `deliveryTrackerUrl` - URL do serviço de rastreamento de entregas
  - `auditComplianceUrl` - URL do serviço de auditoria e conformidade

- **AuthServiceClient**: Melhorias no tratamento de rate limiting
  - Implementação de retry automático para requisições OTP quando rate limit é atingido
  - Detecção de ambiente local/teste para usar configurações mais permissivas
  - Logging detalhado com prefixo `🔧 [TROUBLESHOOTING]` para facilitar debug
  - Validação de email antes de enviar requisições OTP

- **AuthenticationSteps**: Melhorias significativas em troubleshooting e geração de dados
  - Geração automática de email e telefone quando não presentes no `userData`
  - Correção de problemas com maps imutáveis retornados por DataTables
  - Logging extensivo para diagnóstico de problemas
  - Validação e correção automática de dados antes de construir requests

- **UserFixture**: Melhorias no gerenciamento de estado
  - Melhor tratamento de dados de usuário
  - Suporte para geração automática de dados quando necessário

- **RabbitMQHelper**: Melhorias no consumo de mensagens
  - Suporte para cache e padrões de consumo mais robustos
  - Melhor tratamento de timeouts

- **Feature Tags**: Atualização de tags em todos os arquivos .feature
  - Tags atualizadas de `@implemented` para `@vs-identity` em todos os cenários
  - Melhor organização e filtragem de testes por vertical service

- **GitHub Actions Workflow**: Atualizações no workflow de CI/CD
  - Melhorias no workflow `e2e-tests.yml`

### Fixed
- **Rate Limiting Issues**: Correção de problemas com rate limiting em testes E2E
  - Implementação de retry automático para requisições que recebem 429 (Too Many Requests)
  - Detecção de ambiente para usar configurações apropriadas (local: 100 req/hora, prod: 5 req/hora)

- **Data Management**: Correção de problemas com gerenciamento de dados de teste
  - Correção de problemas com maps imutáveis retornados por DataTables
  - Geração automática de dados quando necessário (email, telefone)
  - Melhor validação de dados antes de construir requests

- **Troubleshooting**: Melhorias significativas em logging e diagnóstico
  - Logging detalhado em pontos críticos do fluxo de testes
  - Prefixos padronizados para facilitar filtragem de logs
  - Validações adicionais para identificar problemas rapidamente

### Documentation
- Documentação completa de conformidade de tags e implementação cross-VS
- Guias de referência e análise de cobertura
- Lições aprendidas e próximos passos documentados

## [0.0.3-SNAPSHOT] - 2025-12-10

### Added
- Comprehensive lessons learned documentation (`docs/lessons-learned/LICOES_APRENDIDAS_E2E.md`)
- Guide for complete registration flow tests (`docs/guides/COMPLETE_REGISTRATION_FLOW_TESTS.md`)
- CPF validation utility in `TestDataGenerator.isValidCpf()`
- Multi-layer RabbitMQ message consumption strategy for active consumer environments
- Manual test handling with `@manual` tag and default exclusion in `cucumber.properties`
- Strategic logging patterns with `🔍 [TROUBLESHOOTING]` prefix for easier filtering
- Enhanced OTP handling for non-simulated flows with clear manual instructions

### Changed
- Enhanced `AuthenticationSteps` with robust OTP retrieval and validation logic
- Improved `SimulateProviderSteps` with multi-layer message consumption strategy
- Updated `IdentitySteps` to validate and auto-generate valid CPFs when invalid ones are provided
- Enhanced `ProfileSteps` with increased timeouts and detailed logging for troubleshooting
- Improved `RabbitMQHelper` with cache support and more robust consumption patterns
- Updated all service clients (`AuthServiceClient`, `IdentityServiceClient`, `ProfileServiceClient`) with better error handling
- Enhanced `UserFixture` with better state management
- Updated `cucumber.properties` to exclude `@manual` tests by default
- Removed manual test scenario from `complete_registration_flow.feature`

### Fixed
- Fixed `UnsupportedOperationException` in `AuthenticationSteps` when modifying immutable maps
- Fixed compilation errors related to lambda variable scoping in `SimulateProviderSteps`
- Fixed RabbitMQ message consumption timeouts by implementing multi-layer strategy
- Fixed CPF validation issues by adding automatic validation and generation
- Fixed test data state management issues in step definitions
- Improved handling of tests requiring manual intervention (OTP from real emails)

### Documentation
- Added comprehensive lessons learned document covering all debugging and fixing efforts
- Updated `docs/INDEX.md` with new documentation structure
- Documented troubleshooting patterns and best practices

## [0.0.2-SNAPSHOT] - 2025-11-27

### Changed
- Reorganização completa da documentação conforme Nota Técnica 011
- Documentação técnica movida para estrutura `docs/` organizada por tipo
- Atualização de README.md com link para `docs/INDEX.md`
- Conformidade 100% com estratégia de gestão de documentos

### Documentation
- Documentação reorganizada em `docs/analysis/`, `docs/plans/`, `docs/verification/`, `docs/guides/`
- Criação de `docs/INDEX.md` como índice centralizado
- Remoção de documentos técnicos da raiz (mantidos apenas README.md e CHANGELOG.md)

## [0.0.1-SNAPSHOT] - 2024-12-19

### Added
- Projeto inicial criado
- Configuração básica Spring Boot 3.5.7
- Java 21 como versão de desenvolvimento
- Dependências principais:
  - Spring Boot Starter Web
  - Spring Boot Starter Test
  - Cucumber JUnit Platform Engine
  - Cucumber Spring
  - RestAssured para testes de API
  - Allure para relatórios
- Estrutura de testes E2E com BDD (Cucumber + Gherkin)
- Configuração Maven independente
- Documentação inicial

### Technical Details
- **Spring Boot Version**: 3.5.7
- **Java Version**: 21
- **Maven**: Independent module structure
- **Testing Framework**: Cucumber + JUnit 5
- **API Testing**: RestAssured
- **Reporting**: Allure

### Known Issues
- Test scenarios not fully implemented
- Integration with microservices not configured
- No CI/CD integration
- Basic configuration only

### Next Steps
- Implement complete test scenarios for all journeys
- Configure integration with microservices
- Add CI/CD integration
- Add comprehensive test coverage
- Configure test environments (local, SIT, UAT)
- Add test data management
- Implement test reporting and metrics

---

## Versioning

Este projeto usa [Versionamento Semântico](https://semver.org/lang/pt-BR/). Para as versões disponíveis, veja as [tags neste repositório](https://github.com/projeto2026/platform-journey-tests/tags).

## Release Types

- **MAJOR**: Mudanças incompatíveis na API de testes
- **MINOR**: Novos cenários de teste ou funcionalidades adicionadas
- **PATCH**: Correções de bugs nos testes

## Categories

- **Added**: Para novos cenários de teste ou funcionalidades
- **Changed**: Para mudanças em cenários existentes
- **Deprecated**: Para cenários que serão removidos
- **Removed**: Para cenários removidos
- **Fixed**: Para correções de bugs nos testes
- **Security**: Para vulnerabilidades de segurança

