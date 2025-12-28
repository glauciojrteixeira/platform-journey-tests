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
     * Regras de validação do SSN:
     * 1. Area Number (primeiros 3 dígitos): não pode ser "000", "666", ou "900-999"
     * 2. Group Number (dígitos 4-5): não pode ser "00"
     * 3. Serial Number (últimos 4 dígitos): não pode ser "0000"
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
        
        // Garantir que Group Number (dígitos 4-5) não seja "00"
        int groupNumber = (int) ((ssnNumber / 10000) % 100);
        if (groupNumber == 0) {
            // Se Group Number for 00, ajustar para um valor válido (01-99)
            ssnNumber = (ssnNumber / 10000) * 10000 + (Math.abs(uniqueValue) % 99 + 1) * 10000 + (ssnNumber % 10000);
        }
        
        // Garantir que Serial Number (últimos 4 dígitos) não seja "0000"
        int serialNumber = (int) (ssnNumber % 10000);
        if (serialNumber == 0) {
            // Se Serial Number for 0000, ajustar para um valor válido (0001-9999)
            ssnNumber = (ssnNumber / 10000) * 10000 + (Math.abs(uniqueValue) % 9999 + 1);
        }
        
        String ssn = String.format("%09d", ssnNumber);
        String formatted = ssn.substring(0, 3) + "-" + ssn.substring(3, 5) + "-" + ssn.substring(5, 9);
        
        // Validar novamente após formatação
        int area = Integer.parseInt(ssn.substring(0, 3));
        int group = Integer.parseInt(ssn.substring(3, 5));
        int serial = Integer.parseInt(ssn.substring(5, 9));
        
        // Se ainda não for válido, gerar um novo
        if (area == 0 || area == 666 || area >= 900 || group == 0 || serial == 0) {
            // Gerar um SSN válido garantindo todas as regras
            area = (int) ((Math.abs(uniqueValue) % 899) + 1); // 1-899 (evita 000, 666, 900-999)
            if (area == 666) area = 667; // Evitar 666
            group = (int) ((Math.abs(uniqueValue) % 99) + 1); // 1-99 (evita 00)
            serial = (int) ((Math.abs(uniqueValue) % 9999) + 1); // 1-9999 (evita 0000)
            
            formatted = String.format("%03d-%02d-%04d", area, group, serial);
        }
        
        if (usedSsns.contains(formatted)) {
            // Se já foi usado, gerar um novo com valores diferentes
            long newUnique = System.nanoTime();
            area = (int) ((Math.abs(newUnique) % 899) + 1);
            if (area == 666) area = 667;
            group = (int) ((Math.abs(newUnique) % 99) + 1);
            serial = (int) ((Math.abs(newUnique) % 9999) + 1);
            formatted = String.format("%03d-%02d-%04d", area, group, serial);
        }
        usedSsns.add(formatted);
        return formatted;
    }
    
    private static final Set<String> usedNits = ConcurrentHashMap.newKeySet();
    private static final Set<String> usedEins = ConcurrentHashMap.newKeySet();
    
    /**
     * Gera um NIT único para testes (Bolívia).
     * NIT (Número de Identificación Tributaria) tem 11 dígitos com dígito verificador.
     * 
     * @return NIT único com 11 dígitos e dígito verificador válido
     */
    public static String generateUniqueNit() {
        String nit;
        int maxAttempts = 1000;
        int attempts = 0;
        
        do {
            long uniqueValue = (TIMESTAMP % 10000000000L) + 
                              (Math.abs(EXECUTION_ID.hashCode()) % 10000000000L) + 
                              cpfCounter.incrementAndGet();
            
            String base = String.format("%010d", Math.abs(uniqueValue) % 10000000000L);
            nit = calculateNitChecksum(base);
            
            attempts++;
            
            if (attempts >= maxAttempts) {
                long randomValue = System.nanoTime() % 10000000000L;
                base = String.format("%010d", Math.abs(randomValue));
                nit = calculateNitChecksum(base);
                break;
            }
        } while (usedNits.contains(nit));
        
        usedNits.add(nit);
        return nit;
    }
    
    /**
     * Calcula dígito verificador de NIT usando algoritmo Módulo 11.
     */
    private static String calculateNitChecksum(String base) {
        if (base == null || base.length() != 10) {
            throw new IllegalArgumentException("Base do NIT deve ter exatamente 10 dígitos");
        }
        
        int[] digits = new int[11];
        for (int i = 0; i < 10; i++) {
            digits[i] = Character.getNumericValue(base.charAt(i));
        }
        
        // Algoritmo de validação NIT (Bolívia): Módulo 11 com pesos decrescentes
        int[] weights = {11, 10, 9, 8, 7, 6, 5, 4, 3, 2};
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += digits[i] * weights[i];
        }
        
        int remainder = sum % 11;
        int checkDigit = remainder < 2 ? 0 : 11 - remainder;
        digits[10] = checkDigit;
        
        StringBuilder nit = new StringBuilder(11);
        for (int digit : digits) {
            nit.append(digit);
        }
        return nit.toString();
    }
    
    /**
     * Gera um EIN único para testes (Estados Unidos).
     * EIN (Employer Identification Number) tem 9 dígitos no formato XX-XXXXXXX.
     * 
     * Regras de validação do EIN:
     * 1. Primeiros 2 dígitos (prefixo): não pode ser "00", "07", "08", "09", "17", "18", "19", "28", "29", "49", "69", "70", "78", "79", "80", "90", "96", "97"
     * 2. Para testes, vamos gerar um formato válido
     * 
     * @return EIN único com 9 dígitos (formato XX-XXXXXXX)
     */
    public static String generateUniqueEin() {
        long uniqueValue = (TIMESTAMP % 1000000000L) + 
                          (Math.abs(EXECUTION_ID.hashCode()) % 1000000000) + 
                          cpfCounter.incrementAndGet();
        
        // EIN não pode começar com prefixos inválidos
        // Para testes, vamos gerar um formato válido (prefixos válidos: 01-06, 10-16, 20-27, 30-48, 50-68, 71-77, 81-89, 91-95, 98-99)
        long einNumber = Math.abs(uniqueValue) % 1000000000L;
        
        // Garantir que o prefixo (primeiros 2 dígitos) seja válido
        int prefix = (int) (einNumber / 10000000);
        int[] invalidPrefixes = {0, 7, 8, 9, 17, 18, 19, 28, 29, 49, 69, 70, 78, 79, 80, 90, 96, 97};
        boolean isValidPrefix = true;
        for (int invalid : invalidPrefixes) {
            if (prefix == invalid) {
                isValidPrefix = false;
                break;
            }
        }
        
        if (!isValidPrefix || prefix < 1 || prefix > 99) {
            // Gerar um prefixo válido
            prefix = (int) ((Math.abs(uniqueValue) % 98) + 1); // 1-98
            // Ajustar se for inválido
            for (int invalid : invalidPrefixes) {
                if (prefix == invalid) {
                    prefix = (prefix + 1) % 100;
                    if (prefix == 0) prefix = 1;
                    break;
                }
            }
            einNumber = prefix * 10000000L + (einNumber % 10000000L);
        }
        
        String ein = String.format("%09d", einNumber);
        String formatted = ein.substring(0, 2) + "-" + ein.substring(2, 9);
        
        if (usedEins.contains(formatted)) {
            // Se já foi usado, gerar um novo com valores diferentes
            long newUnique = System.nanoTime();
            prefix = (int) ((Math.abs(newUnique) % 98) + 1);
            for (int invalid : invalidPrefixes) {
                if (prefix == invalid) {
                    prefix = (prefix + 1) % 100;
                    if (prefix == 0) prefix = 1;
                    break;
                }
            }
            long newEinNumber = prefix * 10000000L + (Math.abs(newUnique) % 10000000L);
            ein = String.format("%09d", newEinNumber);
            formatted = ein.substring(0, 2) + "-" + ein.substring(2, 9);
        }
        usedEins.add(formatted);
        return formatted;
    }
}

