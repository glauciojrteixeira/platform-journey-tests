#!/bin/bash

# Script de Referência: Comandos para Testar Filtros de Tags
# Lista comandos úteis para validar filtros de tags do Cucumber
#
# Uso: ./scripts/test-tag-filters.sh
# Este script não executa testes, apenas documenta comandos úteis

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
cd "$PROJECT_ROOT"

# Cores para output
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
GREEN='\033[0;32m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== Comandos para Testar Filtros de Tags ===${NC}"
echo ""
echo -e "${YELLOW}Para testar filtros de tags, execute os seguintes comandos:${NC}"
echo ""

echo -e "${GREEN}1. Filtro por Business Unit (VS-Identity)${NC}"
echo "   mvn test -Dspring.profiles.active=local -Dcucumber.filter.tags=\"@vs-identity\""
echo ""

echo -e "${GREEN}2. Filtro por Segmento${NC}"
echo "   mvn test -Dspring.profiles.active=local -Dcucumber.filter.tags=\"@segment_1\""
echo ""

echo -e "${GREEN}3. Filtro por Prioridade (Críticos)${NC}"
echo "   mvn test -Dspring.profiles.active=local -Dcucumber.filter.tags=\"@critical\""
echo ""

echo -e "${GREEN}4. Filtro Combinado (Implementados e Críticos)${NC}"
echo "   mvn test -Dspring.profiles.active=local -Dcucumber.filter.tags=\"@implemented and @critical\""
echo ""

echo -e "${GREEN}5. Filtro com Exclusão${NC}"
echo "   mvn test -Dspring.profiles.active=local -Dcucumber.filter.tags=\"@vs-identity and not @not_implemented\""
echo ""

echo -e "${GREEN}6. Filtro por Jornada${NC}"
echo "   mvn test -Dspring.profiles.active=local -Dcucumber.filter.tags=\"@j1.1\""
echo ""

echo -e "${GREEN}7. Filtro Complexo${NC}"
echo "   mvn test -Dspring.profiles.active=local -Dcucumber.filter.tags=\"@vs-identity and @segment_1 and @implemented and @critical\""
echo ""

echo -e "${YELLOW}Nota:${NC} Para validar apenas a sintaxe sem executar testes, use:"
echo "   mvn test-compile"
echo ""
echo -e "${YELLOW}Para validar conformidade de tags, use:${NC}"
echo "   ./scripts/validate-tags.sh"
echo ""
echo -e "${BLUE}=== Fim ===${NC}"
