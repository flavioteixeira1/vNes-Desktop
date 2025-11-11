#!/bin/bash

echo "Corrigindo packages e estrutura..."

# Criar estrutura correta
mkdir -p src/main/java/vnes/ui
mkdir -p src/main/java/vnes/input

# Mover e corrigir RockmanForm.java
if [ -f "src/main/java/com/flavioteixeira1/vnes/RockmanForm.java" ]; then
    echo "Corrigindo RockmanForm.java..."
    sed -i 's/package vnes;/package vnes.ui;/' src/main/java/com/flavioteixeira1/vnes/RockmanForm.java
    mv src/main/java/com/flavioteixeira1/vnes/RockmanForm.java src/main/java/vnes/ui/
fi

# Mover UIApp.java (já está correto)
if [ -f "src/main/java/com/flavioteixeira1/vnes/ui/UIApp.java" ]; then
    echo "Movendo UIApp.java..."
    sed -i 's/package com.flavioteixeira1.vnes.ui;/package vnes.ui;/' src/main/java/com/flavioteixeira1/vnes/ui/UIApp.java
    mv src/main/java/com/flavioteixeira1/vnes/ui/UIApp.java src/main/java/vnes/ui/
fi

# Mover e corrigir InputHandler.java
if [ -f "src/main/java/com/flavioteixeira1/vnes/input/InputHandler.java" ]; then
    echo "Corrigindo InputHandler.java..."
    sed -i 's/package vnes.input;/package vnes.input;/' src/main/java/com/flavioteixeira1/vnes/input/InputHandler.java
    mv src/main/java/com/flavioteixeira1/vnes/input/InputHandler.java src/main/java/vnes/input/
fi

# Mover e corrigir KbInputHandler.java
if [ -f "src/main/java/com/flavioteixeira1/vnes/input/KbInputHandler.java" ]; then
    echo "Corrigindo KbInputHandler.java..."
    sed -i 's/package vnes.input;/package vnes.input;/' src/main/java/com/flavioteixeira1/vnes/input/KbInputHandler.java
    mv src/main/java/com/flavioteixeira1/vnes/input/KbInputHandler.java src/main/java/vnes/input/
fi

# Mover outros arquivos .java
find src/main/java/com/flavioteixeira1/vnes -name "*.java" -exec mv {} src/main/java/vnes/ \; 2>/dev/null || true

# Remover estrutura antiga
rm -rf src/main/java/com

echo "Estrutura corrigida:"
find src/main/java -name "*.java"