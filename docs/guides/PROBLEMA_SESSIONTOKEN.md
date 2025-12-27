# Problema: SessionToken Inválido ou Expirado

**Data**: 2025-12-22  
**Status**: 🔍 **Investigando**

---

## 🔍 Problema Identificado

Todos os testes estão falhando com o mesmo erro:

```
"Error validating registration session: 401 Unauthorized: \"{\"valid\":false,\"message\":\"Invalid or expired registration session\"}\""
```

Isso indica que o `registration-token` (sessionToken) está sendo enviado, mas o backend está rejeitando como inválido ou expirado.

---

## 🔍 Possíveis Causas

### 1. SessionToken sendo usado múltiplas vezes
- O `sessionToken` é de uso único e não pode ser reutilizado
- Se o mesmo `sessionToken` for usado em múltiplas requisições, o backend rejeitará

### 2. SessionToken expirando muito rápido
- O `sessionToken` pode ter um tempo de expiração muito curto
- Se houver delay entre a criação e o uso, pode expirar

### 3. Problema com a validação do SessionToken no backend
- O backend pode estar validando o `sessionToken` de forma incorreta
- Pode haver um problema com a forma como o `sessionToken` é armazenado ou validado

### 4. SessionToken sendo limpo antes de ser usado
- O `sessionToken` pode estar sendo limpo antes de ser usado
- Pode haver um problema de timing ou ordem de operações

---

## ✅ Correções Implementadas

### 1. Melhorar Logging do SessionToken

Adicionado logging detalhado para rastrear o `sessionToken`:

```java
logger.info("✅ OTP criado e validado. SessionToken obtido: {}... (length: {})", 
    sessionToken.substring(0, Math.min(8, sessionToken.length())), sessionToken.length());
logger.debug("🔍 [DEBUG] SessionToken completo (primeiros 32 chars): {}...", 
    sessionToken.length() > 32 ? sessionToken.substring(0, 32) : sessionToken);
```

### 2. Limpar SessionToken apenas após sucesso

Modificado para limpar o `sessionToken` apenas se a criação foi bem-sucedida:

```java
// IMPORTANTE: Limpar sessionToken apenas se a criação foi bem-sucedida (201 ou 200)
// Se falhar, manter o sessionToken para debug (mas não reutilizar - é de uso único)
if (lastResponse != null && (lastResponse.getStatusCode() == 201 || lastResponse.getStatusCode() == 200)) {
    // Limpar sessionToken após uso bem-sucedido (é de uso único e não pode ser reutilizado)
    userFixture.setSessionToken(null);
} else {
    // Se falhou, manter sessionToken para debug mas logar que não deve ser reutilizado
    logger.warn("⚠️ Criação falhou. SessionToken mantido para debug, mas NÃO deve ser reutilizado (é de uso único). Status: {}", 
        lastResponse != null ? lastResponse.getStatusCode() : "null");
}
```

### 3. Melhorar Tratamento de Erros

Adicionado tratamento de erros mais detalhado:

```java
if (sessionToken == null || sessionToken.trim().isEmpty()) {
    String responseBody = validationResponse.getBody() != null ? validationResponse.getBody().asString() : "null";
    logger.error("❌ SessionToken não foi retornado na validação de OTP. Resposta completa: {}", responseBody);
    throw new IllegalStateException("SessionToken não foi retornado na validação de OTP. Resposta: " + responseBody);
}
```

---

## 🧪 Próximos Passos

1. **Executar testes novamente** e verificar os logs detalhados do `sessionToken`
2. **Verificar se o `sessionToken` está sendo gerado corretamente** pelo backend
3. **Verificar se há algum problema de timing** entre a criação e o uso do `sessionToken`
4. **Verificar se o `sessionToken` está sendo usado múltiplas vezes** acidentalmente

---

## 📊 Logs Esperados

Com as correções, você deve ver logs como:

```
✅ OTP criado e validado. SessionToken obtido: 3a2b9686... (length: 36)
🔍 [DEBUG] SessionToken completo (primeiros 32 chars): 3a2b9686-1234-5678-9abc-def012345678...
✅ [IdentityClient] registration-token header garantido: 3a2b9686...
```

Se o problema persistir, os logs mostrarão:
- Se o `sessionToken` está sendo gerado corretamente
- Se o `sessionToken` está sendo enviado corretamente
- Se há algum problema com a validação do `sessionToken` no backend

---

## ⚠️ Nota Importante

Se o problema persistir após essas correções, pode ser necessário:

1. **Verificar o backend** para entender por que o `sessionToken` está sendo rejeitado
2. **Verificar se há algum problema de configuração** no backend relacionado à validação de `sessionToken`
3. **Verificar se há algum problema de timing** ou expiração do `sessionToken`

