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

