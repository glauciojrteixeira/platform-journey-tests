#!/bin/bash

# Script para execução seletiva de testes E2E baseado em mudanças no código
# Uso: ./scripts/selective-test-execution.sh [base-branch]

set -e

BASE_BRANCH="${1:-main}"
FEATURES_DIR="src/test/resources/features"
STEP_DEFINITIONS_DIR="src/test/java/com/nulote/journey/stepdefinitions"

echo "🔍 Analisando mudanças desde ${BASE_BRANCH}..."
echo ""

# Obter arquivos modificados
CHANGED_FILES=$(git diff --name-only ${BASE_BRANCH}...HEAD)

if [ -z "$CHANGED_FILES" ]; then
    echo "⚠️  Nenhuma mudança detectada. Executando todos os testes."
    mvn test
    exit 0
fi

echo "📝 Arquivos modificados:"
echo "$CHANGED_FILES" | sed 's/^/   - /'
echo ""

# Identificar features afetadas
AFFECTED_FEATURES=()

# Verificar se step definitions foram modificados
if echo "$CHANGED_FILES" | grep -q "$STEP_DEFINITIONS_DIR"; then
    echo "🔧 Step definitions modificados - executando todos os testes"
    mvn test
    exit 0
fi

# Verificar se features foram modificadas diretamente
while IFS= read -r file; do
    if [[ "$file" == *".feature" ]]; then
        feature_name=$(basename "$file" .feature)
        AFFECTED_FEATURES+=("$feature_name")
    fi
done <<< "$CHANGED_FILES"

# Mapear mudanças em serviços para features relacionadas
# Este mapeamento pode ser expandido conforme necessário
if echo "$CHANGED_FILES" | grep -q "identity-service\|IdentityServiceClient"; then
    AFFECTED_FEATURES+=("authentication" "registration" "profile")
fi

if echo "$CHANGED_FILES" | grep -q "auth-service\|AuthServiceClient"; then
    AFFECTED_FEATURES+=("authentication" "login" "otp")
fi

if echo "$CHANGED_FILES" | grep -q "profile-service\|ProfileServiceClient"; then
    AFFECTED_FEATURES+=("profile" "preferences")
fi

# Remover duplicatas
UNIQUE_FEATURES=($(printf '%s\n' "${AFFECTED_FEATURES[@]}" | sort -u))

if [ ${#UNIQUE_FEATURES[@]} -eq 0 ]; then
    echo "✅ Nenhuma feature afetada identificada. Executando todos os testes."
    mvn test
    exit 0
fi

echo "🎯 Features afetadas identificadas:"
for feature in "${UNIQUE_FEATURES[@]}"; do
    echo "   - $feature"
done
echo ""

# Construir comando Maven com tags Cucumber
TAGS=""
for feature in "${UNIQUE_FEATURES[@]}"; do
    if [ -z "$TAGS" ]; then
        TAGS="@${feature}"
    else
        TAGS="${TAGS} or @${feature}"
    fi
done

echo "🚀 Executando testes seletivos..."
echo "   Tags: ${TAGS}"
echo ""

# Executar testes com tags
mvn test -Dcucumber.filter.tags="${TAGS}"

