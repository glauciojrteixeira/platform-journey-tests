@not_implemented @vs-identity @segment_3 @j3.2 @b2b @invite @high @e2e @may_require_auth
Feature: Processo de Convite para Novo Usuário
  Como um admin de uma entidade jurídica
  Eu quero convidar novos usuários para minha empresa
  Para expandir o acesso corporativo

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando
    And que sou admin de uma entidade jurídica
    And que estou autenticado na plataforma

  Scenario: Convite bem-sucedido para novo usuário
    Given que tenho uma entidade jurídica ativa
    When eu crio um convite para novo usuário:
      | campo      | valor                    |
      | email      | novo.usuario@empresa.com |
      | role       | OPERATOR                 |
    Then o convite deve ser criado com sucesso
    And um email deve ser enviado com link de aceite
    And o link deve conter token temporário válido por 7 dias
    And o evento "entity.invite.created" deve ser publicado

  Scenario: Usuário aceita convite e se vincula à PJ
    Given que recebi um convite por email
    When eu clico no link de aceite do convite
    Then o sistema deve validar o token do link
    And deve verificar se email corresponde ao domínio corporativo
    When eu informo meus dados pessoais:
      | campo           | valor                    |
      | nome            | Novo Usuário             |
      | documentNumber  | {unique_cpf}             |
      | documentType    | CPF                      |
      | phone           | +5511999998888           |
    And eu valido o OTP recebido
    Then o usuário deve ser criado e vinculado à PJ
    And o role deve ser atribuído conforme convite
    And o evento "entity.user.linked" deve ser publicado
    And o admin deve ser notificado sobre aceite

  Scenario: Convite falha com email de domínio diferente
    Given que tenho uma entidade jurídica ativa
    When eu tento criar convite com email de domínio diferente:
      | campo      | valor                    |
      | email      | usuario@outro.com        |
      | role       | OPERATOR                 |
    Then o convite deve falhar com status 400
    And o erro deve indicar que email deve ser do domínio corporativo

  Scenario: Convite expira após 7 dias
    Given que recebi um convite por email há 8 dias
    When eu tento aceitar o convite
    Then a aceitação deve falhar com status 410
    And o erro deve indicar que convite expirou
    And um novo convite deve ser necessário

