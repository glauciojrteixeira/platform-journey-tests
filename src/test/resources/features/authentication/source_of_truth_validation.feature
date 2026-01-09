@implemented @vs-identity @segment_1 @j1.2 @authentication @source_of_truth @critical @e2e
Feature: Validação de Fonte de Verdade (Identity Service como Single Source of Truth)
  Como um desenvolvedor/testador
  Eu quero validar que o Identity Service é a fonte de verdade para dados de usuário
  Para garantir que mudanças sempre vêm do Identity Service e são sincronizadas via eventos

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando

  # Cenário 1: Criação de usuário no Identity Service → Auth Service sincroniza
  # Este cenário valida que quando um usuário é criado no identity-service,
  # o auth-service sincroniza automaticamente via evento RabbitMQ
  Scenario: Criação de usuário no Identity Service é sincronizada no Auth Service
    Given que não existe usuário com email "teste.source.of.truth@example.com"
    When eu crio um usuário no identity-service com email "teste.source.of.truth@example.com"
    Then o usuário deve ser criado no identity-service com UUID válido
    And o evento "user.created.v1" deve ser publicado
    When o evento RabbitMQ "user.created.v1" é processado
    Then o usuário deve existir no auth-service com o mesmo UUID do identity-service
    And os dados do usuário no auth-service devem corresponder aos dados do identity-service
    And o email no auth-service deve ser "teste.source.of.truth@example.com"
    And o nome no auth-service deve corresponder ao nome do identity-service

  # Cenário 2: Atualização de dados no Identity Service → Auth Service sincroniza
  # Este cenário valida que quando dados são atualizados no identity-service,
  # o auth-service sincroniza automaticamente via evento RabbitMQ
  Scenario: Atualização de dados no Identity Service é sincronizada no Auth Service
    Given que tenho dados de teste únicos
    And que crio um usuário com esses dados
    And que estou autenticado na plataforma
    And o evento RabbitMQ "user.created.v1" é processado
    And o usuário existe no auth-service com os dados iniciais
    When eu atualizo o nome do usuário no identity-service para "Nome Atualizado"
    And o evento "user.updated.v1" é publicado
    When o evento RabbitMQ "user.updated.v1" é processado
    Then o nome do usuário no auth-service deve ser atualizado para "Nome Atualizado"
    And o nome do usuário no auth-service deve corresponder ao nome do identity-service
    And o email no auth-service deve permanecer inalterado (não foi modificado no identity-service)

  # Cenário 3: Tentativa de modificar dados diretamente no Auth Service deve falhar ou ser ignorada
  # Este cenário valida que o auth-service não permite modificação de dados que são
  # de responsabilidade do identity-service (fonte de verdade)
  Scenario: Tentativa de modificar dados diretamente no Auth Service é rejeitada
    Given que tenho dados de teste únicos
    And que crio um usuário com esses dados
    And que estou autenticado na plataforma
    And o evento RabbitMQ "user.created.v1" é processado
    When eu tento atualizar o nome do usuário diretamente no auth-service para "Nome Modificado"
    Then a atualização deve falhar com status 400 ou 403
    And o erro deve indicar que a atualização deve ser feita no identity-service
    And o nome do usuário no auth-service não deve ser alterado
    And o nome do usuário no identity-service não deve ser alterado

  # Cenário 4: Desativação de usuário no Identity Service → Auth Service sincroniza
  # Este cenário valida que quando um usuário é desativado no identity-service,
  # o auth-service sincroniza automaticamente via evento RabbitMQ
  Scenario: Desativação de usuário no Identity Service é sincronizada no Auth Service
    Given que tenho dados de teste únicos
    And que crio um usuário com esses dados
    And que estou autenticado na plataforma
    And o evento RabbitMQ "user.created.v1" é processado
    And o usuário está ativo no auth-service
    When eu desativo o usuário no identity-service
    And o evento "user.deactivated.v1" é publicado
    When o evento RabbitMQ "user.deactivated.v1" é processado
    Then o usuário deve estar desativado no auth-service
    And o status do usuário no auth-service deve corresponder ao status do identity-service
    And tentativas de login com este usuário devem falhar

  # Cenário 5: Dados no Auth Service sempre correspondem ao Identity Service após sincronização
  # Este cenário valida que após sincronização via eventos, os dados no auth-service
  # sempre correspondem aos dados do identity-service (fonte de verdade)
  Scenario: Dados no Auth Service correspondem ao Identity Service após sincronização
    Given que tenho dados de teste únicos
    And que crio um usuário com esses dados
    And que estou autenticado na plataforma
    And o evento RabbitMQ "user.created.v1" é processado
    When eu consulto os dados do usuário no identity-service
    And eu consulto os dados do usuário no auth-service
    Then o UUID do usuário deve ser idêntico em ambos os serviços
    And o email do usuário deve ser idêntico em ambos os serviços
    And o nome do usuário deve ser idêntico em ambos os serviços
    And o documentNumber do usuário deve ser idêntico em ambos os serviços (se presente)
    And o documentType do usuário deve ser idêntico em ambos os serviços (se presente)
    And o relationship do usuário deve ser idêntico em ambos os serviços

  # Cenário 6: Múltiplas atualizações no Identity Service → Auth Service sempre sincroniza
  # Este cenário valida que múltiplas atualizações no identity-service são sempre
  # refletidas no auth-service, mantendo a consistência
  Scenario: Múltiplas atualizações no Identity Service são sincronizadas no Auth Service
    Given que tenho dados de teste únicos
    And que crio um usuário com esses dados
    And que estou autenticado na plataforma
    And o evento RabbitMQ "user.created.v1" é processado
    When eu atualizo o nome do usuário no identity-service para "Nome 1"
    And o evento RabbitMQ "user.updated.v1" é processado
    Then o nome no auth-service deve ser "Nome 1"
    When eu atualizo o nome do usuário no identity-service para "Nome 2"
    And o evento RabbitMQ "user.updated.v1" é processado
    Then o nome no auth-service deve ser "Nome 2"
    When eu atualizo o nome do usuário no identity-service para "Nome 3"
    And o evento RabbitMQ "user.updated.v1" é processado
    Then o nome no auth-service deve ser "Nome 3"
    And o nome no auth-service deve corresponder ao nome do identity-service

  # Cenário 7: Criação de usuário via login social → Identity Service é fonte de verdade
  # Este cenário valida que mesmo quando um usuário é criado via login social,
  # o identity-service é sempre a fonte de verdade e o auth-service sincroniza
  Scenario: Login social cria usuário no Identity Service (fonte de verdade)
    Given que não existe usuário com email "teste.social.source.of.truth@example.com"
    When eu inicio login social com provider "GOOGLE" e redirect_uri "http://localhost:3000/auth/callback"
    And o provider retorna autorização bem-sucedida
    Then o usuário deve ser criado no identity-service com UUID válido
    And o evento "user.created.v1" deve ser publicado
    When o evento RabbitMQ "user.created.v1" é processado
    Then o usuário deve existir no auth-service com o mesmo UUID do identity-service
    And os dados do usuário no auth-service devem corresponder aos dados do identity-service
    And o email no auth-service deve ser "teste.social.source.of.truth@example.com"
    And o UUID do usuário no auth-service deve ser idêntico ao UUID do identity-service

  # Cenário 8: Inconsistência detectada → Auth Service deve sincronizar com Identity Service
  # Este cenário valida que se houver inconsistência entre auth-service e identity-service,
  # o auth-service deve sempre seguir o identity-service (fonte de verdade)
  Scenario: Inconsistência detectada - Auth Service sincroniza com Identity Service
    Given que tenho dados de teste únicos
    And que crio um usuário com esses dados
    And que estou autenticado na plataforma
    And o evento RabbitMQ "user.created.v1" é processado
    And o usuário existe no auth-service com os dados iniciais
    # Simular inconsistência (em produção, isso não deveria acontecer, mas validamos o comportamento)
    When eu atualizo o nome do usuário no identity-service para "Nome Corrigido"
    And o evento RabbitMQ "user.updated.v1" é processado
    Then o nome do usuário no auth-service deve ser atualizado para "Nome Corrigido"
    And o nome do usuário no auth-service deve corresponder ao nome do identity-service
    And o identity-service deve permanecer como fonte de verdade

