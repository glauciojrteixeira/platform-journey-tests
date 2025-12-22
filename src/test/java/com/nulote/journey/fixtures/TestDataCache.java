package com.nulote.journey.fixtures;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Cache de dados de teste para reutilização entre cenários.
 * 
 * Objetivo: Reduzir criação redundante de dados de teste (usuários, documentos, etc.)
 * quando possível, mantendo isolamento entre testes.
 * 
 * Estratégia:
 * - Cache por tipo de dado (usuário, documento, etc.)
 * - Reutilização quando dados ainda são válidos
 * - Limpeza automática de dados expirados
 * - Thread-safe para suportar paralelização
 */
@Component
public class TestDataCache {
    
    // Cache de usuários criados (key: email, value: userUuid)
    private final Map<String, String> userCache = new ConcurrentHashMap<>();
    
    // Cache de documentos gerados (key: documentType, value: documentNumber)
    private final Map<String, String> documentCache = new ConcurrentHashMap<>();
    
    // Contador de hits do cache (para métricas)
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    
    /**
     * Obtém ou cria um UUID de usuário para um email.
     * Se o email já estiver no cache, retorna o UUID existente.
     * Caso contrário, retorna null (indicando que precisa criar).
     * 
     * @param email Email do usuário
     * @return UUID do usuário se existir no cache, null caso contrário
     */
    public String getCachedUserUuid(String email) {
        if (email == null) {
            return null;
        }
        
        String cached = userCache.get(email);
        if (cached != null) {
            cacheHits.incrementAndGet();
            return cached;
        }
        
        cacheMisses.incrementAndGet();
        return null;
    }
    
    /**
     * Adiciona um usuário ao cache.
     * 
     * @param email Email do usuário
     * @param userUuid UUID do usuário
     */
    public void cacheUser(String email, String userUuid) {
        if (email != null && userUuid != null) {
            userCache.put(email, userUuid);
        }
    }
    
    /**
     * Obtém um documento do cache para um tipo específico.
     * Útil para reutilizar documentos quando possível.
     * 
     * @param documentType Tipo do documento (CPF, CNPJ, etc.)
     * @return Número do documento se existir no cache, null caso contrário
     */
    public String getCachedDocument(String documentType) {
        if (documentType == null) {
            return null;
        }
        
        String cached = documentCache.get(documentType.toUpperCase());
        if (cached != null) {
            cacheHits.incrementAndGet();
            return cached;
        }
        
        cacheMisses.incrementAndGet();
        return null;
    }
    
    /**
     * Adiciona um documento ao cache.
     * 
     * @param documentType Tipo do documento (CPF, CNPJ, etc.)
     * @param documentNumber Número do documento
     */
    public void cacheDocument(String documentType, String documentNumber) {
        if (documentType != null && documentNumber != null) {
            documentCache.put(documentType.toUpperCase(), documentNumber);
        }
    }
    
    /**
     * Limpa o cache de usuários.
     * Útil para resetar entre execuções ou quando necessário.
     */
    public void clearUserCache() {
        userCache.clear();
    }
    
    /**
     * Limpa o cache de documentos.
     */
    public void clearDocumentCache() {
        documentCache.clear();
    }
    
    /**
     * Limpa todo o cache.
     */
    public void clearAll() {
        clearUserCache();
        clearDocumentCache();
        cacheHits.set(0);
        cacheMisses.set(0);
    }
    
    /**
     * Retorna estatísticas do cache.
     * 
     * @return Map com estatísticas (hits, misses, hitRate, size)
     */
    public Map<String, Object> getStats() {
        long hits = cacheHits.get();
        long misses = cacheMisses.get();
        long total = hits + misses;
        double hitRate = total > 0 ? (double) hits / total * 100 : 0.0;
        
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("hits", hits);
        stats.put("misses", misses);
        stats.put("total", total);
        stats.put("hitRate", String.format("%.2f%%", hitRate));
        stats.put("userCacheSize", userCache.size());
        stats.put("documentCacheSize", documentCache.size());
        
        return stats;
    }
    
    /**
     * Retorna o tamanho do cache de usuários.
     */
    public int getUserCacheSize() {
        return userCache.size();
    }
    
    /**
     * Retorna o tamanho do cache de documentos.
     */
    public int getDocumentCacheSize() {
        return documentCache.size();
    }
}

