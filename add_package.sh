#!/bin/bash

echo "=== ADICIONANDO PACKAGE AOS ARQUIVOS JAVA ==="

# Diretório base do projeto
BASE_DIR="src/main/java/com/flavioteixeira1/vnes/core"

# Verificar se o diretório existe
if [ ! -d "$BASE_DIR" ]; then
    echo "ERRO: Diretório $BASE_DIR não encontrado!"
    echo "Certifique-se de estar no diretório raiz do projeto"
    exit 1
fi

# Contador de arquivos processados
COUNT=0

# Processar todos os arquivos .java no diretório core
find "$BASE_DIR" -name "*.java" | while read -r file; do
    echo "Processando: $file"
    
    # Verificar se o arquivo já tem package declaration
    if grep -q "^package .*;" "$file"; then
        echo "  ⚠️  Já tem package, substituindo..."
        # Remover linha do package existente
        sed -i '/^package .*;/d' "$file"
    fi
    
    # Verificar se o arquivo está vazio ou só tem comments no início
    FIRST_NON_COMMENT_LINE=$(grep -v '^\s*//' "$file" | grep -v '^\s*$' | grep -v '^\s*/\*' | grep -v '^\s*\*' | head -1)
    
    if [ -z "$FIRST_NON_COMMENT_LINE" ]; then
        echo "  ⚠️  Arquivo vazio ou só com comentários"
        # Adicionar package no início
        echo "package com.flavioteixeira1.vnes.core;" > "$file"
    else
        # Adicionar package antes da primeira linha não-comentário
        TEMP_FILE=$(mktemp)
        echo "package com.flavioteixeira1.vnes.core;" > "$TEMP_FILE"
        cat "$file" >> "$TEMP_FILE"
        mv "$TEMP_FILE" "$file"
    fi
    
    ((COUNT++))
    echo "  ✅ Package adicionado"
done

echo "=== CONCLUSÃO ==="
echo "Total de arquivos processados: $COUNT"
echo "Package 'com.flavioteixeira1.vnes.core' adicionado a todos os arquivos Java"