@not_implemented @vs-identity @segment_2 @j2.6 @security @logout @high @e2e @otp_required
Feature: Logout de Todos os Dispositivos
  Como um arrematador profissional
  Eu quero desconectar todos os dispositivos
  Para garantir segurança máxima

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando
    And que estou autenticado na plataforma
    And que tenho sessões ativas em múltiplos dispositivos

  Scenario: Logout de todos os dispositivos bem-sucedido
    Given que tenho sessões ativas em múltiplos dispositivos
    When eu solicito logout de todos os dispositivos
    Then um OTP ou MFA deve ser solicitado para confirmação
    When eu valido o código de confirmação
    Then todas as sessões devem ser encerradas
    And todos os tokens JWT devem ser invalidados
    And o evento "auth.sessions.revoked" deve ser publicado
    When eu tento usar qualquer token anterior
    Then o acesso deve ser negado com status 401
    And será necessário fazer login novamente em todos os dispositivos

  Scenario: Logout de todos os dispositivos falha sem confirmação
    Given que tenho sessões ativas em múltiplos dispositivos
    When eu tento fazer logout sem confirmar OTP/MFA
    Then a operação deve falhar com status 401
    And as sessões devem permanecer ativas

