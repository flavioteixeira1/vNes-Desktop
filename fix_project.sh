#!/bin/bash
echo "Corrigindo TODOS os packages para core..."

# Corrigir TODOS os arquivos para usar package core
for file in src/main/java/com/flavioteixeira1/vnes/core/*.java; do
    if [ -f "$file" ]; then
        # Mudar QUALQUER package para core
        sed -i 's/^package .*;/package com.flavioteixeira1.vnes.core;/' "$file"
        echo "Corrigido: $(basename "$file") -> package com.flavioteixeira1.vnes.core"
    fi
done

echo "Packages corrigidos."