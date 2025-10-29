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