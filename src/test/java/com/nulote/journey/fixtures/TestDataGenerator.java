package com.nulote.journey.fixtures;

import java.util.UUID;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Gerador de dados únicos para testes E2E.
 * Garante que cada execução use dados únicos para evitar conflitos.
 */
public class TestDataGenerator {
    
    private static final String EXECUTION_ID = UUID.randomUUID().toString().substring(0, 8);
    private static final long TIMESTAMP = System.currentTimeMillis();
    
    // Contador sequencial thread-safe para garantir unicidade
    private static final AtomicLong cpfCounter = new AtomicLong(0);
    
    // Cache de CPFs gerados nesta execução para evitar duplicatas
    private static final Set<String> usedCpfs = ConcurrentHashMap.newKeySet();
    
    /**
     * Gera um email único para testes
     * 
     * @return Email único no formato test-{executionId}-{timestamp}-{counter}@example.com
     */
    public static String generateUniqueEmail() {
        long counter = cpfCounter.incrementAndGet();
        return String.format("test-%s-%d-%d@example.com", EXECUTION_ID, TIMESTAMP, counter);
    }
    
    /**
     * Gera um CPF único para testes com dígitos verificadores válidos.
     * Usa combinação de timestamp, execution ID e contador sequencial para garantir unicidade.
     * 
     * @return CPF único com 11 dígitos e dígitos verificadores válidos
     */
    public static String generateUniqueCpf() {
        String cpf;
        int maxAttempts = 1000; // Limite de tentativas para evitar loop infinito
        int attempts = 0;
        
        do {
            // Combinar timestamp, execution ID hash e contador para garantir unicidade
            long uniqueValue = (TIMESTAMP % 1000000L) * 1000 + 
                              (Math.abs(EXECUTION_ID.hashCode()) % 1000) + 
                              cpfCounter.incrementAndGet();
            
            // Garantir que temos 9 dígitos para a base
            String base = String.format("%09d", Math.abs(uniqueValue) % 1000000000L);
            
            // Calcular dígitos verificadores usando algoritmo real de CPF
            cpf = calculateCpfChecksum(base);
            
            attempts++;
            
            // Se exceder tentativas, adicionar mais aleatoriedade
            if (attempts >= maxAttempts) {
                long randomValue = System.nanoTime() % 1000000000L;
                base = String.format("%09d", Math.abs(randomValue));
                cpf = calculateCpfChecksum(base);
                break;
            }
        } while (usedCpfs.contains(cpf));
        
        // Adicionar ao cache de CPFs usados
        usedCpfs.add(cpf);
        
        return cpf;
    }
    
    /**
     * Gera um telefone único para testes
     * 
     * @return Telefone único no formato +5511999{5 dígitos}
     */
    public static String generateUniquePhone() {
        long counter = cpfCounter.incrementAndGet();
        return String.format("+5511999%s", String.format("%05d", (TIMESTAMP + counter) % 100000));
    }
    
    /**
     * Gera um nome único para testes
     * 
     * @return Nome único no formato "Test User {executionId}"
     */
    public static String generateUniqueName() {
        return String.format("Test User %s", EXECUTION_ID);
    }
    
    /**
     * Calcula dígitos verificadores de CPF usando algoritmo oficial brasileiro.
     * 
     * @param base Base do CPF (9 dígitos)
     * @return CPF completo com 11 dígitos incluindo dígitos verificadores
     */
    private static String calculateCpfChecksum(String base) {
        if (base == null || base.length() != 9) {
            throw new IllegalArgumentException("Base do CPF deve ter exatamente 9 dígitos");
        }
        
        // Converter string para array de inteiros
        int[] digits = new int[11];
        for (int i = 0; i < 9; i++) {
            digits[i] = Character.getNumericValue(base.charAt(i));
        }
        
        // Calcular primeiro dígito verificador
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += digits[i] * (10 - i);
        }
        int firstDigit = 11 - (sum % 11);
        if (firstDigit >= 10) {
            firstDigit = 0;
        }
        digits[9] = firstDigit;
        
        // Calcular segundo dígito verificador
        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += digits[i] * (11 - i);
        }
        int secondDigit = 11 - (sum % 11);
        if (secondDigit >= 10) {
            secondDigit = 0;
        }
        digits[10] = secondDigit;
        
        // Construir CPF completo como string
        StringBuilder cpf = new StringBuilder(11);
        for (int digit : digits) {
            cpf.append(digit);
        }
        
        return cpf.toString();
    }
    
    /**
     * Valida se um CPF tem dígitos verificadores válidos.
     * 
     * @param cpf CPF a ser validado (11 dígitos)
     * @return true se o CPF tem dígitos verificadores válidos, false caso contrário
     */
    public static boolean isValidCpf(String cpf) {
        if (cpf == null || cpf.length() != 11 || !cpf.matches("\\d+")) {
            return false;
        }
        
        // Extrair base (9 primeiros dígitos) e dígitos verificadores (2 últimos)
        String base = cpf.substring(0, 9);
        String providedChecksum = cpf.substring(9, 11);
        
        // Calcular dígitos verificadores corretos
        String calculatedCpf = calculateCpfChecksum(base);
        String calculatedChecksum = calculatedCpf.substring(9, 11);
        
        // Comparar dígitos verificadores
        return providedChecksum.equals(calculatedChecksum);
    }
    
    /**
     * Limpa o cache de CPFs usados. Útil para testes ou reset entre execuções.
     */
    public static void clearUsedCpfs() {
        usedCpfs.clear();
        cpfCounter.set(0);
    }
    
    /**
     * Retorna o número de CPFs gerados nesta execução.
     * 
     * @return Número de CPFs únicos gerados
     */
    public static int getGeneratedCpfCount() {
        return usedCpfs.size();
    }
}

