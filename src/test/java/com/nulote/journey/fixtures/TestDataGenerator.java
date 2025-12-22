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
    
    // Cache de documentos gerados nesta execução para evitar duplicatas
    private static final Set<String> usedCpfs = ConcurrentHashMap.newKeySet();
    private static final Set<String> usedCnpjs = ConcurrentHashMap.newKeySet();
    private static final Set<String> usedCuits = ConcurrentHashMap.newKeySet();
    private static final Set<String> usedDnis = ConcurrentHashMap.newKeySet();
    private static final Set<String> usedRuts = ConcurrentHashMap.newKeySet();
    private static final Set<String> usedCis = ConcurrentHashMap.newKeySet();
    private static final Set<String> usedSsns = ConcurrentHashMap.newKeySet();
    
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
    
    /**
     * Gera um CNPJ único para testes com dígitos verificadores válidos.
     * 
     * @return CNPJ único com 14 dígitos e dígitos verificadores válidos
     */
    public static String generateUniqueCnpj() {
        String cnpj;
        int maxAttempts = 1000;
        int attempts = 0;
        
        do {
            long uniqueValue = (TIMESTAMP % 100000000L) * 100 + 
                              (Math.abs(EXECUTION_ID.hashCode()) % 100) + 
                              cpfCounter.incrementAndGet();
            
            String base = String.format("%012d", Math.abs(uniqueValue) % 1000000000000L);
            cnpj = calculateCnpjChecksum(base);
            
            attempts++;
            
            if (attempts >= maxAttempts) {
                long randomValue = System.nanoTime() % 1000000000000L;
                base = String.format("%012d", Math.abs(randomValue));
                cnpj = calculateCnpjChecksum(base);
                break;
            }
        } while (usedCnpjs.contains(cnpj));
        
        usedCnpjs.add(cnpj);
        return cnpj;
    }
    
    /**
     * Calcula dígitos verificadores de CNPJ usando algoritmo oficial brasileiro.
     */
    private static String calculateCnpjChecksum(String base) {
        if (base == null || base.length() != 12) {
            throw new IllegalArgumentException("Base do CNPJ deve ter exatamente 12 dígitos");
        }
        
        int[] digits = new int[14];
        for (int i = 0; i < 12; i++) {
            digits[i] = Character.getNumericValue(base.charAt(i));
        }
        
        // Primeiro dígito verificador
        int[] weights1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += digits[i] * weights1[i];
        }
        int firstDigit = sum % 11;
        firstDigit = firstDigit < 2 ? 0 : 11 - firstDigit;
        digits[12] = firstDigit;
        
        // Segundo dígito verificador
        int[] weights2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        sum = 0;
        for (int i = 0; i < 13; i++) {
            sum += digits[i] * weights2[i];
        }
        int secondDigit = sum % 11;
        secondDigit = secondDigit < 2 ? 0 : 11 - secondDigit;
        digits[13] = secondDigit;
        
        StringBuilder cnpj = new StringBuilder(14);
        for (int digit : digits) {
            cnpj.append(digit);
        }
        return cnpj.toString();
    }
    
    /**
     * Gera um CUIT único para testes (Argentina).
     * 
     * @return CUIT único com 11 dígitos e dígito verificador válido
     */
    public static String generateUniqueCuit() {
        String cuit;
        int maxAttempts = 1000;
        int attempts = 0;
        
        do {
            long uniqueValue = (TIMESTAMP % 100000000L) * 10 + 
                              (Math.abs(EXECUTION_ID.hashCode()) % 10) + 
                              cpfCounter.incrementAndGet();
            
            String base = String.format("%010d", Math.abs(uniqueValue) % 10000000000L);
            cuit = calculateCuitChecksum(base);
            
            attempts++;
            
            if (attempts >= maxAttempts) {
                long randomValue = System.nanoTime() % 10000000000L;
                base = String.format("%010d", Math.abs(randomValue));
                cuit = calculateCuitChecksum(base);
                break;
            }
        } while (usedCuits.contains(cuit));
        
        usedCuits.add(cuit);
        return cuit;
    }
    
    /**
     * Calcula dígito verificador de CUIT usando algoritmo Módulo 11.
     */
    private static String calculateCuitChecksum(String base) {
        if (base == null || base.length() != 10) {
            throw new IllegalArgumentException("Base do CUIT deve ter exatamente 10 dígitos");
        }
        
        int[] digits = new int[11];
        for (int i = 0; i < 10; i++) {
            digits[i] = Character.getNumericValue(base.charAt(i));
        }
        
        int[] coefficients = {2, 3, 4, 5, 6, 7, 2, 3, 4, 5};
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += digits[i] * coefficients[9 - i];
        }
        
        int remainder = sum % 11;
        int checkDigit = 11 - remainder;
        if (checkDigit == 11) {
            checkDigit = 0;
        } else if (checkDigit == 10) {
            checkDigit = 9;
        }
        digits[10] = checkDigit;
        
        StringBuilder cuit = new StringBuilder(11);
        for (int digit : digits) {
            cuit.append(digit);
        }
        return cuit.toString();
    }
    
    /**
     * Gera um DNI único para testes (Argentina).
     * 
     * @return DNI único com 8 dígitos
     */
    public static String generateUniqueDni() {
        long uniqueValue = (TIMESTAMP % 100000000L) + 
                          (Math.abs(EXECUTION_ID.hashCode()) % 100000000) + 
                          cpfCounter.incrementAndGet();
        
        String dni = String.format("%08d", Math.abs(uniqueValue) % 100000000L);
        
        // Garantir que não é duplicado
        if (usedDnis.contains(dni)) {
            dni = String.format("%08d", (System.nanoTime() % 100000000L));
        }
        usedDnis.add(dni);
        return dni;
    }
    
    /**
     * Gera um RUT único para testes (Chile).
     * 
     * @return RUT único com 8 dígitos base + dígito verificador válido
     */
    public static String generateUniqueRut() {
        String rut;
        int maxAttempts = 1000;
        int attempts = 0;
        
        do {
            long uniqueValue = (TIMESTAMP % 100000000L) + 
                              (Math.abs(EXECUTION_ID.hashCode()) % 100000000) + 
                              cpfCounter.incrementAndGet();
            
            String base = String.format("%08d", Math.abs(uniqueValue) % 100000000L);
            rut = calculateRutChecksum(base);
            
            attempts++;
            
            if (attempts >= maxAttempts) {
                long randomValue = System.nanoTime() % 100000000L;
                base = String.format("%08d", Math.abs(randomValue));
                rut = calculateRutChecksum(base);
                break;
            }
        } while (usedRuts.contains(rut));
        
        usedRuts.add(rut);
        return rut;
    }
    
    /**
     * Calcula dígito verificador de RUT usando algoritmo Módulo 11.
     */
    private static String calculateRutChecksum(String base) {
        if (base == null || base.length() != 8) {
            throw new IllegalArgumentException("Base do RUT deve ter exatamente 8 dígitos");
        }
        
        int[] coefficients = {2, 3, 4, 5, 6, 7};
        int sum = 0;
        int coefficientIndex = 0;
        
        for (int i = base.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(base.charAt(i));
            sum += digit * coefficients[coefficientIndex % coefficients.length];
            coefficientIndex++;
        }
        
        int remainder = sum % 11;
        String checkDigit;
        if (remainder == 0) {
            checkDigit = "0";
        } else if (remainder == 1) {
            checkDigit = "K";
        } else {
            checkDigit = String.valueOf(11 - remainder);
        }
        
        return base + "-" + checkDigit;
    }
    
    /**
     * Gera um CI único para testes (Bolívia).
     * 
     * @return CI único com 7-10 dígitos
     */
    public static String generateUniqueCi() {
        long uniqueValue = (TIMESTAMP % 100000000L) + 
                          (Math.abs(EXECUTION_ID.hashCode()) % 100000000) + 
                          cpfCounter.incrementAndGet();
        
        // CI boliviano tem 7-10 dígitos, vamos usar 8
        String ci = String.format("%08d", Math.abs(uniqueValue) % 100000000L);
        
        if (usedCis.contains(ci)) {
            ci = String.format("%08d", (System.nanoTime() % 100000000L));
        }
        usedCis.add(ci);
        return ci;
    }
    
    /**
     * Gera um SSN único para testes (EUA).
     * 
     * @return SSN único com 9 dígitos (formato XXX-XX-XXXX)
     */
    public static String generateUniqueSsn() {
        long uniqueValue = (TIMESTAMP % 1000000000L) + 
                          (Math.abs(EXECUTION_ID.hashCode()) % 1000000000) + 
                          cpfCounter.incrementAndGet();
        
        // SSN não pode começar com 000, 666, ou 9XX na primeira parte
        // Para testes, vamos gerar um formato válido
        long ssnNumber = Math.abs(uniqueValue) % 1000000000L;
        
        // Garantir que não começa com 000, 666, ou 9XX
        int firstPart = (int) (ssnNumber / 1000000);
        if (firstPart == 0 || firstPart == 666 || firstPart >= 900) {
            ssnNumber = (ssnNumber % 899000000L) + 1000000; // Entre 100-000-000 e 899-999-999
        }
        
        String ssn = String.format("%09d", ssnNumber);
        String formatted = ssn.substring(0, 3) + "-" + ssn.substring(3, 5) + "-" + ssn.substring(5, 9);
        
        if (usedSsns.contains(formatted)) {
            ssnNumber = (System.nanoTime() % 899000000L) + 1000000;
            ssn = String.format("%09d", ssnNumber);
            formatted = ssn.substring(0, 3) + "-" + ssn.substring(3, 5) + "-" + ssn.substring(5, 9);
        }
        usedSsns.add(formatted);
        return formatted;
    }
}

