# Changelog - Platform Journey Tests

Todas as mudanças notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/),
e este projeto adere ao [Versionamento Semântico](https://semver.org/lang/pt-BR/).

## [Unreleased]

### Added
- N/A

### Changed
- N/A

### Deprecated
- N/A

### Removed
- N/A

### Fixed
- N/A

### Security
- N/A

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

