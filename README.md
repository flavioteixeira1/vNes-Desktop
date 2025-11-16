vNes Desktop

Emulador NES para desktop escrito em Java
Versões
v1.1 (Atual)

    ✅ Suporte a joystick com Jinput

    ✅ Mapeamento de botões do joystick para teclas do emulador

    ✅ Compatibilidade com Windows, Linux e macOS

    ⚠️ Utiliza Java Robot para simulação de teclas

v1.0 (Estável)

    Versão original estável

    Suporte apenas a teclado

    Implementação base do emulador

📋 Pré-requisitos
Para v1.1

    ✅ JDK 21 RECOMENDADO - Versão ideal para melhor performance e estabilidade

    JDK 8+ - Compatível mas com ressalvas

    Biblioteca Jinput (incluída no projeto)

    Drivers de joystick/gamepad instalados no sistema

    Maven 3.6+ (opcional - para compilação com Maven)

Para v1.0

    JDK 8+ - Versão originalmente testada

    Apenas teclado

    Maven 3.6+ (opcional - para compilação com Maven)

⚠️ Informações Importantes sobre Versões do Java
JDK 21 (RECOMENDADO para v1.1)

    Performance otimizada para o emulador

    Estabilidade superior no menu e durante a execução

    Sem travamentos no menu durante gameplay

    Melhor suporte a bibliotecas nativas do Jinput

JRE 1.8 (Compatível com ressalvas)

    ⚠️ Problema conhecido: Menu pode travar quando um jogo está em execução

    ⚠️ Workaround: Fechar o jogo antes de acessar menus

    ✅ Funciona para testes básicos

    ✅ Compatível com a v1.0 original

🎮 Suporte a Joystick (v1.1)
⚠️ Importante sobre Jinput

A biblioteca Jinput requer arquivos nativos específicos do sistema operacional que devem estar localizados na pasta do executável Java ou em caminhos do sistema:
Windows 64-bit:

    jinput-dx8_64.dll

    jinput-raw_64.dll

    jinput-wintab.dll

Windows 32-bit:

    jinput-dx8.dll

    jinput-raw.dll

    jinput-wintab.dll

Linux 64-bit:

    libjinput-linux64.so (deve estar em /usr/lib/ ou pasta do Java)

Linux 32-bit:

    libjinput-linux.so (deve estar em /usr/lib/ ou pasta do Java)

macOS:

    libjinput-osx.jnilib (deve estar na pasta do Java ou /usr/lib/)

🔧 Configuração do Joystick

O emulador detecta automaticamente os joysticks conectados. Os botões são mapeados para:

    D-Pad: Controles direcionais

    Botão A: Tecla Z

    Botão B: Tecla X

    Start: Enter

    Select: Shift

⚠️ Aviso sobre Java Robot

O suporte a joystick utiliza a classe java.awt.Robot para simular pressionamento de teclas. Isso significa:

    O emulador deve estar em foco para funcionar corretamente

    Eventos de teclado são disparados continuamente durante o uso do joystick

    Pode interferir com outras aplicações se trocar de janela durante o uso

    Antivírus podem alertar sobre automação de teclado

🛠️ Procedimentos de Compilação
📦 Compilação com Maven (RECOMENDADO)
Para v1.1 (branch main):
bash

# Navegar para a pasta do projeto
cd vNes-Desktop

# Limpar e compilar o projeto
mvn clean compile

# Executar a aplicação
mvn exec:java -Dexec.mainClass="RockmanRUN"    ou

mvn exec:java -Dexec.mainClass="RockmanVnesFrame"   para versão sem menu (legado)

# Criar JAR executável
mvn clean package

# O JAR será gerado em: target/vNes-Desktop-1.1.jar

Para v1.0 (tag v1.0):
bash

# Mudar para a versão 1.0
git checkout v1.0

# Compilar versão original
mvn clean compile

# Executar versão original
mvn exec:java -Dexec.mainClass="RockmanRUN"    ou

mvn exec:java -Dexec.mainClass="RockmanVnesFrame"   para versão sem menu (legado)


# Criar JAR da versão 1.0
mvn clean package

🔨 Compilação Manual com javac
Versão 1.1 (Com Joystick)
Compilação com javac (JDK 21 RECOMENDADO):
bash

# Navegue até a pasta src do projeto
cd src

# Compilar todas as classes (incluindo suporte a joystick)
javac -cp ".;libs/*" -d ../bin **/*.java

# Executar com JDK 21 (RECOMENDADO)
java -cp "bin;libs/*" Main

# Executar com Java 8 (pode ter travamentos no menu)
"C:\Program Files (x86)\Java\jre1.8.0_361\bin\java" -cp "bin;libs/*" Main

Estrutura de pastas necessária:
text

vNes-Desktop/
├── src/
├── bin/
├── libs/
│   ├── jinput.jar
│   ├── [arquivos nativos do seu SO]/
├── target/ (gerado pelo Maven)
└── roms/

Versão 1.0 (Apenas Teclado)
Para compilar versões anteriores (tag v1.0):
bash

# Mudar para a versão 1.0
git checkout v1.0

# Compilar sem dependências do Jinput (Java 8 compatível)
javac -d bin src/**/*.java

# Executar versão original
java -cp bin Main

Estrutura da v1.0:
text

vNes-Desktop/
├── src/
├── bin/
├── target/ (gerado pelo Maven)
└── roms/

🚀 Comandos Mavan Úteis
bash

# Limpar e recompilar tudo
mvn clean compile

# Executar testes (se houver)
mvn test

# Criar JAR executável
mvn clean package

# Instalar no repositório local
mvn clean install

# Executar aplicação diretamente
mvn exec:java -Dexec.mainClass="RockmanRUN"

# Ver dependências do projeto
mvn dependency:tree

# Gerar documentação
mvn javadoc:javadoc

📥 Download de Versões
Releases Disponíveis:

    v1.1 - Com suporte a joystick (JDK 21 RECOMENDADO)

    v1.0 - Versão original estável (Java 8 compatível)

🐛 Solução de Problemas
Problemas comuns no v1.1:

Joystick não detectado:

    Verifique se os arquivos nativos do Jinput estão na pasta correta

    Execute com privilégios de administrador se necessário

Erro de biblioteca nativa:

    Confirme que está usando a versão correta (32/64 bits) para seu sistema

    No Linux, copie os .so para /usr/lib/ ou use LD_LIBRARY_PATH

Teclas travadas:

    O Java Robot pode manter teclas "pressionadas", minimize e restaure a janela

Menu travando durante jogo (Java 8):

    ⚠️ PROBLEMA CONHECIDO: Use JDK 21 para resolver

    Workaround: Feche o jogo antes de acessar menus no emulador

Problemas com Maven:

    Verifique se o Maven está instalado: mvn --version

    Limpe o cache do Maven: mvn dependency:purge-local-repository

    Forçar atualização de dependências: mvn clean compile -U

🔄 Mudanças entre Versões
Feature	v1.0	v1.1
Teclado	✅	✅
Joystick	❌	✅
Dependências	Nenhuma	Jinput
Java Recomendado	JDK 8	JDK 21
Compilação Maven	✅	✅
Complexidade	Baixa	Média
📞 Suporte

    Para melhor experiência: use v1.1 com JDK 21 e Maven

    Para problemas com joystick: use a v1.1 com JDK 21

    Para versão estável sem dependências: use a v1.0

    Compilação recomendada: Use Maven para maior facilidade

    Reportar issues: GitHub Issues

Nota: A v1.1 com JDK 21 é altamente recomendada para melhor performance e estabilidade, especialmente para evitar travamentos no menu durante a execução de jogos. A compilação com Maven é a forma mais fácil e confiável de construir o projeto. A v1.0 permanece como opção estável para usuários que preferem usar apenas teclado com Java 8.


README v1.0

# vNes Desktop - NES Emulator

A Java-based NES emulator with focus on Rockman (Mega Man) series compatibility.

## About

This project is based on the original **vNES** emulator created by **Jamie Sanders**. 
The original vNES was available at **virtualnes.com** and served as the foundation for this enhanced desktop version.

## Features

- NES emulation with cycle-accurate CPU timing
- GUI interface for ROM loading and management
- Direct ROM execution capability
- Sound support (requires proper Java audio setup)
- Save state functionality

## Requirements

- **Java Runtime Environment (JRE) 8** or later
  - Oracle JRE 8+ or OpenJDK 8+
  - Download from: [Oracle Java](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://openjdk.org/)

## Quick Start

### Option 1: Using Pre-built JAR (Recommended)
Download the latest `vNes-Desktop.jar` from the [Releases](../../releases) section and run:
```bash
java -jar vNes-Desktop.jar

Option 2: Compile from Source
Compilation
bash

# Clone the repository
git clone https://github.com/flavioteixeira1/vNes-Desktop.git
cd vNes-Desktop

# Compile all Java files
javac -d bin src/*.java src/**/*.java

# Create JAR file
jar cfe vNes-Desktop.jar RockmanRUN -C bin .

Execution Methods

Method A: GUI File Browser
Run the application with file browser interface:
bash

java -cp bin RockmanRUN

or using the JAR:
bash

java -jar vNes-Desktop.jar

This opens a GUI where you can load NES ROM files through the menu.

Method B: Direct ROM Execution
Run with the bundled "rockman.nes" ROM:
bash

java -cp bin RockmanVnesFrame

Project Structure
Main Classes

    RockmanRUN.java - Main entry point with GUI file browser

        Provides menu-driven ROM loading

        File selection dialog for choosing NES ROMs

    RockmanVnesFrame.java - Direct ROM executor

        Automatically loads the bundled "rockman.nes" file

        Starts emulation immediately

Key Packages

    nes - NES hardware emulation (CPU, PPU, APU, Mapper)

    ui - User interface components

    utils - Utility classes and helpers

Audio Notes

For proper audio functionality on Linux systems, ensure:

    PulseAudio or ALSA is properly configured

    Java sound dependencies are installed:
    bash

# On Debian/Ubuntu
sudo apt install libasound2 libasound2-plugins alsa-utils

If experiencing audio issues, try running with specific audio backend:
bash

java -Djavax.sound.sampled.Clip=com.sun.media.sound.DirectAudioDeviceProvider -jar vNes-Desktop.jar

Controls

    Arrow Keys - Directional input

    Z - A button

    X - B button

    Enter - Start button

    Shift - Select button

    Esc - Open menu/Pause

Supported ROMs

The emulator is optimized for Rockman/Mega Man series but supports most NES ROMs. The bundled "rockman.nes" file is included for demonstration.
Building from Source
Prerequisites

    JDK 8 or later

    Git (for cloning repository)

Steps

    Clone the repository

    Navigate to project directory

    Compile with javac

    Create JAR file (optional)

Troubleshooting
Common Issues

No sound on Linux:
bash

# Install required audio libraries
sudo apt install pulseaudio pulseaudio-utils alsa-utils

# Test Java audio
java -version

ROM not loading:

    Ensure ROM file is a valid NES format

    Check file permissions

Performance issues:

    Close other applications to free system resources

    Ensure you're using the latest Java version

Credits

    Original vNES emulator: Jamie Sanders

    Original vNES website: virtualnes.com

    Desktop adaptation: Flavio Teixeira

License

This project is for educational and demonstration purposes. Based on the original vNES emulator.
Contributing

Feel free to submit issues and pull requests for improvements and bug fixes.