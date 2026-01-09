@implemented @vs-identity @segment_1 @j1.2 @authentication @denormalized_data @performance @e2e
Feature: Validação de Dados Denormalizados no Auth Service
  Como um desenvolvedor/testador
  Eu quero validar que os dados denormalizados no auth-service funcionam corretamente
  Para garantir performance, independência e otimização de consultas

  Background:
    Given a infraestrutura de testes está configurada
    And os microserviços estão rodando

  # Cenário 1: Performance - Consultas locais são mais rápidas que chamadas ao identity-service
  # Este cenário valida que consultas ao auth-service (dados denormalizados) são mais rápidas
  # que chamadas diretas ao identity-service, garantindo otimização de performance
  Scenario: Consultas locais no auth-service são mais rápidas que chamadas ao identity-service
    Given que tenho dados de teste únicos
    And que crio um usuário com esses dados
    And que estou autenticado na plataforma
    And o evento RabbitMQ "user.created.v1" é processado
    And o usuário existe no auth-service com os dados sincronizados
    When eu consulto os dados do usuário no identity-service
    And eu consulto os dados do usuário no auth-service
    Then o tempo de resposta do auth-service deve ser menor que o tempo de resposta do identity-service
    And a diferença de tempo deve ser significativa (pelo menos 50ms mais rápido)
    And os dados retornados devem ser idênticos em ambos os serviços

  # Cenário 2: Independência - Auth-service pode validar JWT sem chamar identity-service
  # Este cenário valida que o auth-service pode validar JWT usando apenas dados locais
  # (denormalizados), sem precisar chamar o identity-service, garantindo independência
  Scenario: Auth-service valida JWT usando apenas dados denormalizados (sem chamar identity-service)
    Given que tenho dados de teste únicos
    And que crio um usuário com esses dados
    And que estou autenticado na plataforma
    And o evento RabbitMQ "user.created.v1" é processado
    And o usuário existe no auth-service com os dados sincronizados
    And eu obtenho um JWT válido
    When eu valido o JWT no auth-service
    Then a validação do JWT deve ser bem-sucedida
    And o auth-service não deve fazer chamadas ao identity-service durante a validação
    And o JWT deve conter os dados corretos do usuário (baseados em dados denormalizados)

  # Cenário 3: Otimização - Login não precisa chamar identity-service (usa dados locais)
  # Este cenário valida que durante o login, o auth-service usa dados denormalizados
  # locais em vez de chamar o identity-service, garantindo otimização de performance
  Scenario: Login usa dados denormalizados locais sem chamar identity-service
    Given que tenho dados de teste únicos
    And que crio um usuário com esses dados
    And que estou autenticado na plataforma
    And o evento RabbitMQ "user.created.v1" é processado
    And o usuário existe no auth-service com os dados sincronizados
    When eu faço logout
    And eu faço login novamente com as mesmas credenciais
    Then o login deve ser bem-sucedido
    And o auth-service não deve fazer chamadas ao identity-service durante o login
    And o JWT gerado deve conter os dados corretos do usuário (baseados em dados denormalizados)

  # Cenário 4: Campos denormalizados - Apenas campos necessários para autenticação são denormalizados
  # Este cenário valida que apenas os campos necessários para autenticação são denormalizados
  # no auth-service, garantindo que não há dados desnecessários sendo copiados
  Scenario: Apenas campos necessários para autenticação são denormalizados
    Given que tenho dados de teste únicos
    And que crio um usuário com esses dados
    And que estou autenticado na plataforma
    And o evento RabbitMQ "user.created.v1" é processado
    When eu consulto os dados do usuário no auth-service
    Then o auth-service deve conter os seguintes campos denormalizados:
      | campo          | obrigatório | descrição                          |
      | uuid           | sim         | Identificador único do usuário     |
      | email          | sim         | Email para autenticação            |
      | name           | sim         | Nome do usuário                    |
      | isActive       | sim         | Status de ativação do usuário      |
      | role           | sim         | Role do usuário (para autorização) |
      | relationship   | sim         | Relacionamento (B2C/B2B)           |
      | mfaEnabled     | sim         | Status de MFA habilitado          |
    And o auth-service não deve conter campos desnecessários para autenticação
    And os campos denormalizados devem corresponder aos campos do identity-service

  # Cenário 5: Consistência eventual - Dados eventualmente ficam consistentes mesmo com atraso
  # Este cenário valida que mesmo com atraso no evento RabbitMQ, os dados eventualmente
  # ficam consistentes entre identity-service e auth-service (consistência eventual)
  Scenario: Dados eventualmente ficam consistentes mesmo com atraso no evento RabbitMQ
    Given que tenho dados de teste únicos
    And que crio um usuário com esses dados
    And que estou autenticado na plataforma
    When eu atualizo o nome do usuário no identity-service para "Nome Atualizado"
    And o evento "user.updated.v1" é publicado
    Then inicialmente os dados podem estar inconsistentes (atraso no evento)
    When eu aguardo a sincronização do evento RabbitMQ (até 5 segundos)
    Then os dados no auth-service devem eventualmente corresponder aos dados do identity-service
    And o nome no auth-service deve ser "Nome Atualizado"
    And a consistência eventual deve ser alcançada em tempo aceitável (menos de 5 segundos)

  # Cenário 6: Redução de carga - Múltiplas consultas não sobrecarregam identity-service
  # Este cenário valida que múltiplas consultas ao auth-service não resultam em múltiplas
  # chamadas ao identity-service, reduzindo a carga no identity-service
  Scenario: Múltiplas consultas ao auth-service não sobrecarregam identity-service
    Given que tenho dados de teste únicos
    And que crio um usuário com esses dados
    And que estou autenticado na plataforma
    And o evento RabbitMQ "user.created.v1" é processado
    And o usuário existe no auth-service com os dados sincronizados
    When eu consulto os dados do usuário no auth-service 10 vezes consecutivas
    Then todas as consultas devem ser bem-sucedidas
    And o tempo total de resposta deve ser menor que 500ms (10 consultas locais)
    And o auth-service não deve fazer chamadas ao identity-service durante as consultas
    And os dados retornados devem ser consistentes em todas as consultas

  # Cenário 7: Resiliência - Auth-service funciona mesmo se identity-service estiver temporariamente indisponível
  # Este cenário valida que o auth-service pode funcionar (consultas locais) mesmo se o
  # identity-service estiver temporariamente indisponível, garantindo resiliência
  Scenario: Auth-service funciona mesmo se identity-service estiver temporariamente indisponível
    Given que tenho dados de teste únicos
    And que crio um usuário com esses dados
    And que estou autenticado na plataforma
    And o evento RabbitMQ "user.created.v1" é processado
    And o usuário existe no auth-service com os dados sincronizados
    And eu obtenho um JWT válido
    When o identity-service fica temporariamente indisponível
    And eu valido o JWT no auth-service
    Then a validação do JWT deve ser bem-sucedida (usando dados denormalizados)
    And o auth-service deve funcionar normalmente para consultas locais
    And o auth-service não deve falhar por causa da indisponibilidade do identity-service

  # Cenário 8: Sincronização - Dados denormalizados são sincronizados corretamente via eventos
  # Este cenário valida que os dados denormalizados são sincronizados corretamente via
  # eventos RabbitMQ, garantindo que a cópia local está sempre atualizada
  Scenario: Dados denormalizados são sincronizados corretamente via eventos RabbitMQ
    Given que tenho dados de teste únicos
    And que crio um usuário com esses dados
    And que estou autenticado na plataforma
    When o evento RabbitMQ "user.created.v1" é processado
    Then o usuário deve existir no auth-service com os dados sincronizados
    And os dados no auth-service devem corresponder aos dados do identity-service
    When eu atualizo o nome do usuário no identity-service para "Nome Sincronizado"
    And o evento RabbitMQ "user.updated.v1" é processado
    Then o nome no auth-service deve ser atualizado para "Nome Sincronizado"
    And os dados no auth-service devem continuar correspondendo aos dados do identity-service
    When eu desativo o usuário no identity-service
    And o evento RabbitMQ "user.deactivated.v1" é processado
    Then o usuário deve estar desativado no auth-service
    And o status no auth-service deve corresponder ao status do identity-service

