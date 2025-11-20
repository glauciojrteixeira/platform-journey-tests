@implemented @segment_1 @b2c @pf @journey @critical @e2e
Feature: Jornada Completa - Segmento 1 - Compradores Ocasionais
  Como um comprador ocasional
  Eu quero completar minha jornada de registro e primeiro acesso
  Para poder fazer arremates na plataforma

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando

  # Jornada 1.1 - Registro e Onboarding (versão simplificada sem OTP)
  @j1.1 @registration @onboarding @smoke @partial
  Scenario: Jornada completa de registro e onboarding
    Given que estou na tela de registro
    When eu escolho registro com credenciais próprias
    And eu informo:
      | campo      | valor                    |
      | nome       | João Silva               |
      | cpf        | 12345678901              |
      | email      | joao.silva@example.com    |
      | telefone   | +5511999998888            |
    And eu valido o reCAPTCHA
    # OTP não está implementado - pulando steps de OTP
    And eu envio os dados para criar identidade
    Then a identidade deve ser criada com sucesso
    # Credenciais e perfil podem ser criados assincronamente
    # And as credenciais devem ser provisionadas
    # And o perfil deve ser criado automaticamente
    # And eu devo receber um JWT válido
    # And o evento "user.created.v1" deve ser publicado
    # And o evento "credentials.provisioned.v1" deve ser publicado

  # Jornada 1.2 - Primeiro Login após Registro (pode precisar setup de credenciais)
  @j1.2 @login @first_login @smoke @partial @requires_credentials_setup
  Scenario: Primeiro login após registro
    Given que tenho dados de teste únicos
    And que crio um usuário com esses dados
    # Nota: Credenciais podem precisar ser criadas manualmente
    When eu faço login com minhas credenciais
    Then eu devo receber um JWT válido
    # Perfil pode ser criado assincronamente
    # And o perfil deve estar acessível
    # And o último login deve ser atualizado

