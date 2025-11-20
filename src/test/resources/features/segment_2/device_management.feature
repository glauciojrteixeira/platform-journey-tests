@not_implemented @segment_2 @j2.5 @security @devices @medium @e2e
Feature: Gestão de Dispositivos Conectados
  Como um arrematador profissional
  Eu quero gerenciar dispositivos onde estou logado
  Para controlar o acesso à minha conta

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando
    And que estou autenticado na plataforma

  Scenario: Listagem de dispositivos conectados
    Given que tenho sessões ativas em múltiplos dispositivos
    When eu consulto meus dispositivos conectados
    Then a lista deve retornar informações de cada dispositivo:
      | campo         | descrição                    |
      | dispositivo   | Navegador/app                |
      | ip_origem     | IP de origem                 |
      | ultimo_acesso | Timestamp do último acesso   |
      | localizacao   | Localização (se disponível)   |
    And cada dispositivo deve ter um session_id único

  Scenario: Revogação de acesso de dispositivo específico
    Given que tenho sessões ativas em múltiplos dispositivos
    When eu revogo acesso de um dispositivo específico
    Then a sessão desse dispositivo deve ser encerrada
    And o token JWT desse dispositivo deve ser invalidado
    And o evento "auth.session.revoked" deve ser publicado
    And os outros dispositivos devem continuar ativos

  Scenario: Detalhes de dispositivo específico
    Given que tenho sessões ativas em múltiplos dispositivos
    When eu consulto detalhes de um dispositivo específico
    Then as informações detalhadas devem ser retornadas:
      | campo         | descrição                    |
      | session_id    | ID único da sessão           |
      | criado_em     | Quando a sessão foi criada   |
      | expira_em     | Quando a sessão expira       |
      | ultima_atividade | Última atividade registrada |

