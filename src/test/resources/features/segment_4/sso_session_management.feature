@not_implemented @vs-identity @segment_4 @j4.6 @b2b @sso @enterprise @medium @e2e @may_require_auth
Feature: Gestão de Sessões SSO
  Como admin técnico de uma plataforma B2B
  Eu quero gerenciar sessões SSO ativas
  Para controlar acessos corporativos

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando
    And que tenho uma plataforma com SSO configurado
    And que estou autenticado como admin técnico

  Scenario: Listagem de sessões SSO ativas
    Given que tenho múltiplas sessões SSO ativas
    When eu consulto lista de sessões SSO da minha PJ
    Then a lista deve retornar todas as sessões SSO ativas:
      | campo         | descrição                    |
      | session_id    | ID único da sessão           |
      | usuario       | Usuário autenticado          |
      | provider      | Provedor SSO                |
      | criada_em     | Quando foi criada            |
      | expira_em     | Quando expira                |
      | ultima_atividade | Última atividade registrada |

  Scenario: Detalhes de sessão SSO específica
    Given que tenho sessões SSO ativas
    When eu consulto detalhes de uma sessão específica
    Then as informações detalhadas devem ser retornadas:
      | campo         | descrição                    |
      | session_id    | ID único da sessão           |
      | usuario       | Informações do usuário       |
      | provider      | Provedor SSO usado            |
      | claims        | Claims mapeados              |
      | criada_em     | Timestamp de criação         |
      | expira_em     | Timestamp de expiração       |
      | ultima_atividade | Última atividade         |

  Scenario: Revogação de sessão SSO específica
    Given que tenho múltiplas sessões SSO ativas
    When eu revogo uma sessão SSO específica
    Then a sessão deve ser encerrada
    And o token JWT correspondente deve ser invalidado
    And o evento "sso.session.revoked" deve ser publicado
    And as outras sessões devem permanecer ativas

  Scenario: Revogação de todas as sessões SSO
    Given que tenho múltiplas sessões SSO ativas
    When eu revogo todas as sessões SSO da minha PJ
    Then todas as sessões SSO devem ser encerradas
    And todos os tokens JWT correspondentes devem ser invalidados
    And o evento "sso.sessions.revoked.all" deve ser publicado
    And será necessário fazer login SSO novamente

