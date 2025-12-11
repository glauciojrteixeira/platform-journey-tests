@implemented @vs-identity @segment_1 @j1.10 @authentication @critical @e2e
Feature: Logout
  Como um comprador ocasional
  Eu quero fazer logout da plataforma
  Para encerrar minha sessão com segurança

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando
    And que estou autenticado na plataforma

  @not_implemented
  Scenario: Logout bem-sucedido
    Given que tenho um token JWT válido
    When eu faço logout
    Then o token deve ser invalidado no servidor
    # Nota: Evento auth.logout não está implementado ainda no Auth Service
    # O evento deveria ser publicado no exchange auth.events quando logout ocorre
    # Consumidor deveria criar a fila (ex: profile.auth-logout.queue para analytics)
    And o evento "auth.logout" deve ser publicado
    When eu tento usar o token invalidado
    Then o acesso deve ser negado com status 401
    And o erro deve indicar token inválido

  Scenario: Logout apenas local (sem invalidar no servidor)
    Given que tenho um token JWT válido
    When eu removo o token apenas do frontend
    Then o token ainda é válido no servidor
    # Nota: Logout completo requer chamada ao servidor

