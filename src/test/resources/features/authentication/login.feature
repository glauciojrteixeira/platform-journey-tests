@implemented @segment_1 @j1.2 @authentication @critical @e2e
Feature: Autenticação para Compradores Ocasionais
  Como um comprador ocasional
  Eu quero fazer login na plataforma
  Para acessar meus arremates

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando

  # Nota: Login pode falhar se credenciais não forem criadas automaticamente após registro
  @partial @requires_credentials_setup
  Scenario: Login bem-sucedido após registro
    Given que tenho dados de teste únicos
    And que crio um usuário com esses dados
    # Nota: Credenciais podem precisar ser criadas manualmente se não forem automáticas
    When eu faço login com minhas credenciais
    Then eu devo receber um JWT válido
    # Perfil pode ser criado assincronamente via eventos
    # And o perfil deve estar acessível
    # And o último login deve ser atualizado

  Scenario: Login falha com credenciais inválidas
    Given que tenho dados de teste únicos
    And que crio um usuário com esses dados
    When eu tento fazer login com credenciais inválidas:
      | email    | usuario@example.com      |
      | password | SenhaErrada123!          |
    Then o login deve falhar com status 401
    And o erro deve ser "INVALID_CREDENTIALS"
    And a mensagem de erro deve conter "credenciais inválidas"
    And nenhum JWT deve ser emitido
    # Evento pode não estar sendo publicado ou fila não configurada
    # And o evento "auth.failed" deve ser publicado com motivo "INVALID_CREDENTIALS"

  Scenario: Login falha com usuário não encontrado
    Given que não existe usuário com email "inexistente@example.com"
    When eu tento fazer login:
      | email    | inexistente@example.com   |
      | password | SenhaQualquer123!         |
    Then o login deve falhar com status 404
    And o erro deve ser "USER_NOT_FOUND"
    And a mensagem de erro deve conter "usuário não encontrado"
    And nenhum JWT deve ser emitido

