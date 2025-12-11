@not_implemented @vs-identity @segment_2 @j2.7 @identity @upgrade @high @e2e
Feature: Upgrade para Segmento Profissional
  Como um comprador ocasional
  Eu quero fazer upgrade para segmento profissional
  Para ter acesso a funcionalidades profissionais

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando
    And que estou autenticado como Segmento 1

  Scenario: Upgrade bem-sucedido com CPF validado
    Given que tenho uma conta do Segmento 1
    And meu CPF já foi validado
    When eu solicito upgrade para segmento profissional
    Then o upgrade deve ser realizado com sucesso
    And o campo "relationship" ou segmento deve ser atualizado
    And o evento "identity.upgraded" deve ser publicado
    And as funcionalidades profissionais devem ser habilitadas
    And o MFA pode ser ativado imediatamente

  Scenario: Upgrade solicita validação de CPF se não validado
    Given que tenho uma conta do Segmento 1
    And meu CPF ainda não foi validado
    When eu solicito upgrade para segmento profissional
    Then o sistema deve solicitar validação de CPF primeiro
    When eu valido meu CPF via serviço externo
    Then o upgrade deve prosseguir automaticamente
    And o upgrade deve ser concluído com sucesso

  Scenario: Upgrade falha com CPF inválido
    Given que tenho uma conta do Segmento 1
    When eu solicito upgrade para segmento profissional
    And o sistema solicita validação de CPF
    When a validação de CPF falha (CPF inválido)
    Then o upgrade deve falhar
    And a conta deve permanecer no Segmento 1
    And o erro deve indicar que CPF precisa ser válido para upgrade

