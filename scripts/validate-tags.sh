#!/bin/bash

# Script de Validação de Tags em Arquivos .feature
# Valida conformidade de tags conforme playbook 019.04
#
# Uso: ./scripts/validate-tags.sh
# Exit code: 0 se todas as tags estão corretas, 1 caso contrário

set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
FEATURES_DIR="${PROJECT_ROOT}/src/test/resources/features"
ERRORS=0

echo "=== Validação de Tags em Arquivos .feature ==="
echo "Diretório: ${FEATURES_DIR}"
echo ""

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Função para contar arquivos
count_files() {
    find "$FEATURES_DIR" -name "*.feature" -not -path "*/cross-vs/*" -not -path "*/vs-customer-communications/*" | wc -l | tr -d ' '
}

# Função para listar arquivos sem tag
list_files_without_tag() {
    local tag_pattern="$1"
    find "$FEATURES_DIR" -name "*.feature" -not -path "*/cross-vs/*" -not -path "*/vs-customer-communications/*" | \
        while read -r file; do
            if ! grep -q "$tag_pattern" "$file"; then
                echo "  - $file"
            fi
        done
}

# 1. Verificar @vs-identity ou @vs-customer-communications ou @cross-bu
echo "1. Verificando tag de Business Unit (@vs-identity, @vs-customer-communications, @cross-bu)..."
TOTAL_FILES=$(count_files)
MISSING_VS=$(find "$FEATURES_DIR" -name "*.feature" -not -path "*/cross-vs/*" -not -path "*/vs-customer-communications/*" | \
    xargs grep -L "@vs-identity\|@vs-customer-communications\|@cross-bu" 2>/dev/null | wc -l | tr -d ' ')

if [ "$MISSING_VS" -gt 0 ]; then
    echo -e "${RED}❌ $MISSING_VS de $TOTAL_FILES arquivos sem tag de Business Unit${NC}"
    list_files_without_tag "@vs-identity\|@vs-customer-communications\|@cross-bu"
    ERRORS=$((ERRORS + 1))
else
    echo -e "${GREEN}✅ Todos os $TOTAL_FILES arquivos têm tag de Business Unit${NC}"
fi
echo ""

# 2. Verificar @segment_
echo "2. Verificando tag de segmento (@segment_1, @segment_2, etc.)..."
MISSING_SEGMENT=$(find "$FEATURES_DIR" -name "*.feature" -not -path "*/cross-vs/*" -not -path "*/vs-customer-communications/*" | \
    xargs grep -L "@segment_" 2>/dev/null | wc -l | tr -d ' ')

if [ "$MISSING_SEGMENT" -gt 0 ]; then
    echo -e "${RED}❌ $MISSING_SEGMENT de $TOTAL_FILES arquivos sem tag de segmento${NC}"
    list_files_without_tag "@segment_"
    ERRORS=$((ERRORS + 1))
else
    echo -e "${GREEN}✅ Todos os $TOTAL_FILES arquivos têm tag de segmento${NC}"
fi
echo ""

# 3. Verificar prioridade (@critical, @high, @medium, @low)
echo "3. Verificando tag de prioridade (@critical, @high, @medium, @low)..."
MISSING_PRIORITY=$(find "$FEATURES_DIR" -name "*.feature" -not -path "*/cross-vs/*" -not -path "*/vs-customer-communications/*" | \
    xargs grep -L "@critical\|@high\|@medium\|@low" 2>/dev/null | wc -l | tr -d ' ')

if [ "$MISSING_PRIORITY" -gt 0 ]; then
    echo -e "${RED}❌ $MISSING_PRIORITY de $TOTAL_FILES arquivos sem tag de prioridade${NC}"
    list_files_without_tag "@critical\|@high\|@medium\|@low"
    ERRORS=$((ERRORS + 1))
else
    echo -e "${GREEN}✅ Todos os $TOTAL_FILES arquivos têm tag de prioridade${NC}"
fi
echo ""

# 4. Verificar tag de status (@implemented, @wip, @not_implemented)
echo "4. Verificando tag de status (@implemented, @wip, @not_implemented)..."
MISSING_STATUS=$(find "$FEATURES_DIR" -name "*.feature" -not -path "*/cross-vs/*" -not -path "*/vs-customer-communications/*" | \
    xargs grep -L "@implemented\|@wip\|@not_implemented" 2>/dev/null | wc -l | tr -d ' ')

if [ "$MISSING_STATUS" -gt 0 ]; then
    echo -e "${YELLOW}⚠️  $MISSING_STATUS de $TOTAL_FILES arquivos sem tag de status (recomendado, mas não obrigatório)${NC}"
    list_files_without_tag "@implemented\|@wip\|@not_implemented"
    # Não incrementa ERRORS pois é apenas recomendado
else
    echo -e "${GREEN}✅ Todos os $TOTAL_FILES arquivos têm tag de status${NC}"
fi
echo ""

# Resumo final
echo "=== Resumo ==="
if [ $ERRORS -eq 0 ]; then
    echo -e "${GREEN}✅ Todas as tags obrigatórias estão presentes!${NC}"
    echo ""
    exit 0
else
    echo -e "${RED}❌ Encontrados $ERRORS problema(s) de conformidade${NC}"
    echo ""
    echo "Para corrigir, adicione as tags faltantes nos arquivos listados acima."
    echo "Consulte: engineering-playbook/019.04 - BDD_E2E_TESTING_STRATEGY_EXECUTION.md"
    echo ""
    exit 1
fi
