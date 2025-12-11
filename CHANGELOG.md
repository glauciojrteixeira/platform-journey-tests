# Changelog - Platform Journey Tests

Todas as mudanças notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/),
e este projeto adere ao [Versionamento Semântico](https://semver.org/lang/pt-BR/).

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

