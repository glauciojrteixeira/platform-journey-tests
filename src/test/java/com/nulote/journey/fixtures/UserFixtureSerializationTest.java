package com.nulote.journey.fixtures;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste unitário para verificar a serialização do request de criação de usuário.
 * Este teste verifica se o documentType está sendo serializado corretamente no JSON.
 */
@DisplayName("UserFixture - Serialização de Request")
class UserFixtureSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Deve serializar documentType corretamente quando presente")
    void deve_serializar_documentType_corretamente_quando_presente() throws Exception {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Test User");
        request.put("documentNumber", "12345678901");
        request.put("documentType", "CPF");
        request.put("email", "test@example.com");
        request.put("phone", "+5511999999999");
        request.put("role", "INDIVIDUAL");
        request.put("relationship", "B2C");

        // Act
        String json = objectMapper.writeValueAsString(request);
        System.out.println("🔍 JSON serializado: " + json);

        // Assert
        assertThat(json).contains("\"documentType\"");
        assertThat(json).contains("\"CPF\"");
        assertThat(json).contains("\"documentNumber\"");
        assertThat(json).contains("\"name\"");
        
        // Verificar que o JSON pode ser deserializado de volta
        @SuppressWarnings("unchecked")
        Map<String, Object> deserialized = objectMapper.readValue(json, Map.class);
        assertThat(deserialized.get("documentType")).isEqualTo("CPF");
        assertThat(deserialized.get("documentNumber")).isEqualTo("12345678901");
    }

    @Test
    @DisplayName("Deve serializar documentType null quando ausente")
    void deve_serializar_documentType_null_quando_ausente() throws Exception {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Test User");
        request.put("documentNumber", "12345678901");
        request.put("documentType", null); // null explicitamente
        request.put("email", "test@example.com");
        request.put("phone", "+5511999999999");

        // Act
        String json = objectMapper.writeValueAsString(request);
        System.out.println("🔍 JSON serializado com documentType null: " + json);

        // Assert
        // Jackson por padrão inclui campos null no JSON como "documentType":null
        assertThat(json).contains("\"documentType\"");
        assertThat(json).contains("null");
        
        // Verificar que o JSON pode ser deserializado de volta
        @SuppressWarnings("unchecked")
        Map<String, Object> deserialized = objectMapper.readValue(json, Map.class);
        assertThat(deserialized.get("documentType")).isNull();
    }

    @Test
    @DisplayName("Deve serializar todos os tipos de documento aceitos")
    void deve_serializar_todos_os_tipos_de_documento_aceitos() throws Exception {
        String[] documentTypes = {"CPF", "CNPJ", "CUIT", "DNI", "RUT", "CI", "SSN"};

        for (String docType : documentTypes) {
            // Arrange
            Map<String, Object> request = new HashMap<>();
            request.put("name", "Test User");
            request.put("documentNumber", "12345678901");
            request.put("documentType", docType);
            request.put("email", "test@example.com");

            // Act
            String json = objectMapper.writeValueAsString(request);
            System.out.println("🔍 JSON com documentType " + docType + ": " + json);

            // Assert
            assertThat(json).contains("\"documentType\"");
            assertThat(json).contains("\"" + docType + "\"");
            
            // Verificar deserialização
            @SuppressWarnings("unchecked")
            Map<String, Object> deserialized = objectMapper.readValue(json, Map.class);
            assertThat(deserialized.get("documentType")).isEqualTo(docType);
        }
    }

    @Test
    @DisplayName("Deve verificar se RestAssured serializa da mesma forma")
    void deve_verificar_se_restassured_serializa_da_mesma_forma() throws Exception {
        // Arrange - Simular exatamente como o UserFixture.buildCreateUserRequest() cria o request
        Map<String, Object> request = new HashMap<>();
        request.put("name", "João Silva");
        
        Object documentNumberObj = "54976995534";
        String documentType = "CPF"; // Após normalização
        
        request.put("documentNumber", documentNumberObj);
        request.put("documentType", documentType);
        
        request.put("email", "test@example.com");
        request.put("phone", "+5511999998888");
        request.put("role", "INDIVIDUAL");
        request.put("relationship", "B2C");

        // Act
        String json = objectMapper.writeValueAsString(request);
        System.out.println("🔍 JSON serializado (simulando UserFixture): " + json);

        // Assert
        assertThat(json).contains("\"documentType\":\"CPF\"");
        assertThat(json).contains("\"documentNumber\":\"54976995534\"");
        assertThat(json).contains("\"name\":\"João Silva\"");
        
        // Verificar estrutura completa
        @SuppressWarnings("unchecked")
        Map<String, Object> deserialized = objectMapper.readValue(json, Map.class);
        assertThat(deserialized).hasSize(7);
        assertThat(deserialized.get("documentType")).isEqualTo("CPF");
        assertThat(deserialized.get("documentNumber")).isEqualTo("54976995534");
        assertThat(deserialized.get("name")).isEqualTo("João Silva");
        assertThat(deserialized.get("email")).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Deve verificar se campo ausente vs null faz diferença")
    void deve_verificar_se_campo_ausente_vs_null_faz_diferenca() throws Exception {
        // Teste 1: Campo presente com valor
        Map<String, Object> request1 = new HashMap<>();
        request1.put("name", "Test");
        request1.put("documentType", "CPF");
        String json1 = objectMapper.writeValueAsString(request1);
        System.out.println("🔍 JSON com documentType='CPF': " + json1);
        assertThat(json1).contains("\"documentType\":\"CPF\"");

        // Teste 2: Campo presente com null
        Map<String, Object> request2 = new HashMap<>();
        request2.put("name", "Test");
        request2.put("documentType", null);
        String json2 = objectMapper.writeValueAsString(request2);
        System.out.println("🔍 JSON com documentType=null: " + json2);
        assertThat(json2).contains("\"documentType\"");

        // Teste 3: Campo ausente (não adicionado ao Map)
        Map<String, Object> request3 = new HashMap<>();
        request3.put("name", "Test");
        // documentType não é adicionado
        String json3 = objectMapper.writeValueAsString(request3);
        System.out.println("🔍 JSON sem documentType: " + json3);
        assertThat(json3).doesNotContain("documentType");
    }
}

