package com.nulote.journey.fixtures;

import java.util.UUID;

/**
 * Contexto de execução para rastreamento de dados criados durante testes E2E.
 * Garante que cada execução tenha um ID único para rastreabilidade.
 */
public class ExecutionContext {
    
    private static final String EXECUTION_ID = UUID.randomUUID().toString();
    private static final long START_TIME = System.currentTimeMillis();
    
    public static String getExecutionId() {
        return EXECUTION_ID;
    }
    
    public static String getExecutionPrefix() {
        return "e2e-" + EXECUTION_ID.substring(0, 8) + "-";
    }
    
    public static boolean isDataFromThisExecution(String identifier) {
        return identifier != null && identifier.startsWith(getExecutionPrefix());
    }
    
    /**
     * Usar em todos os dados de teste para garantir rastreabilidade
     * 
     * @param base Valor base a ser prefixado
     * @return Valor prefixado com execution ID
     */
    public static String tagWithExecutionId(String base) {
        return getExecutionPrefix() + base;
    }
}

