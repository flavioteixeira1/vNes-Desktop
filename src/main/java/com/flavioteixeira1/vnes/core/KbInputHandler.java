package com.flavioteixeira1.vnes.core;

import java.awt.event.*;
import java.util.Map;

public class KbInputHandler implements KeyListener, InputHandler {

    boolean[] allKeysState;
    int[] keyMapping;
    int id;
    NES nes;
    private JoystickManager joystickManager;
    private boolean keystate[];
    private int keymap[];
    

    public KbInputHandler(NES nes, int id) {
        this.nes = nes;
        this.id = id; // 0 = Player 1, 1 = Player 2
        allKeysState = new boolean[255];
        keyMapping = new int[InputHandler.NUM_KEYS];
        keystate = new boolean[0x100];
        keymap = new int[0x100];
         // Inicializar joystick manager específico para este jogador
            try {
                this.joystickManager = JoystickManager.getInstanceForPlayer(id);
                if (joystickManager.isJoystickEnabled()) {
                    System.out.println(" Joystick integrado com sucesso para Player " + (id + 1));
                } else {
                    System.out.println("  Nenhum joystick detectado para Player " + (id + 1));
                }
            } catch (Exception e) {
                System.err.println(" Falha ao acessar joystick para Player " + (id + 1) + ": " + e.getMessage());
                this.joystickManager = null;
            }

        }


   
    public void remapKey(int nesButton, int newKeyCode) {
        if (nesButton >= 0 && nesButton < NUM_KEYS) {
            keyMapping[nesButton] = newKeyCode;
            System.out.println("Player " + (id + 1) + " - " + getButtonName(nesButton) + 
                              " remapeado para " + KeyEvent.getKeyText(newKeyCode));
            
            // SINCRONIZAR COM O JOYSTICK
            syncWithJoystick(nesButton, newKeyCode);
        }
    }

    private void syncWithJoystick(int nesButton, int newKeyCode) {
        if (joystickManager != null && joystickManager.isJoystickEnabled()) {
            // Mapear botão NES para índice físico do joystick
            int joyButton = getJoystickButtonForNESButton(nesButton);
            if (joyButton != -1) {
                joystickManager.setButtonMapping(joyButton, newKeyCode);
            }
        }
    }


    private int getJoystickButtonForNESButton(int nesButton) {
        // Mapear botões NES para índices físicos do joystick
        // Esta lógica precisa corresponder à detecção automática do JoystickManager
        switch(nesButton) {
            case KEY_A: return 0;      // Primeiro botão físico
            case KEY_B: return 1;      // Segundo botão físico
            case KEY_START: return 2;  // Terceiro botão físico  
            case KEY_SELECT: return 3; // Quarto botão físico
            // Os direcionais são tratados nos eixos, não em botões
            default: return -1;
        }
    }

     // Método para restaurar padrões também sincronizado
    public void restoreDefaultMappings() {
        int[] defaultKeys;
        
        if (id == 0) {
            // Player 1 defaults
            defaultKeys = new int[]{
                KeyEvent.VK_Z,      // A
                KeyEvent.VK_X,      // B
                KeyEvent.VK_ENTER,  // Start
                KeyEvent.VK_CONTROL,// Select
                KeyEvent.VK_UP,     // Up
                KeyEvent.VK_DOWN,   // Down
                KeyEvent.VK_LEFT,   // Left
                KeyEvent.VK_RIGHT   // Right
            };
        } else {
            // Player 2 defaults  
            defaultKeys = new int[]{
                KeyEvent.VK_NUMPAD7, // A
                KeyEvent.VK_NUMPAD9, // B
                KeyEvent.VK_NUMPAD1, // Start
                KeyEvent.VK_NUMPAD3, // Select
                KeyEvent.VK_NUMPAD8, // Up
                KeyEvent.VK_NUMPAD2, // Down
                KeyEvent.VK_NUMPAD4, // Left
                KeyEvent.VK_NUMPAD6  // Right
            };
        }
        
        int[] nesButtons = {KEY_A, KEY_B, KEY_START, KEY_SELECT, KEY_UP, KEY_DOWN, KEY_LEFT, KEY_RIGHT};
        
        for (int i = 0; i < nesButtons.length; i++) {
            remapKey(nesButtons[i], defaultKeys[i]);
        }
    }



     // Método para obter nome do botão NES
    private String getButtonName(int nesButton) {
        switch(nesButton) {
            case KEY_A: return "A";
            case KEY_B: return "B"; 
            case KEY_START: return "Start";
            case KEY_SELECT: return "Select";
            case KEY_UP: return "Up";
            case KEY_DOWN: return "Down";
            case KEY_LEFT: return "Left";
            case KEY_RIGHT: return "Right";
            default: return "Unknown";
        }
    }

     // Método para obter mapeamento atual
    public int getCurrentMapping(int nesButton) {
        return keyMapping[nesButton];
    }


    public short getKeyState(int padKey) {
        return (short) (allKeysState[keyMapping[padKey]] ? 0x41 : 0x40);
    }

    

    public void mapKey(int padKey, int kbKeycode) {
        keyMapping[padKey] = kbKeycode;
    }

    public void keyPressed(KeyEvent ke) {

        int kc = ke.getKeyCode();
        if (kc >= allKeysState.length) {
            return;
        }

        allKeysState[kc] = true;

        // Can't hold both left & right or up & down at same time:
        if (kc == keyMapping[InputHandler.KEY_LEFT]) {
            allKeysState[keyMapping[InputHandler.KEY_RIGHT]] = false;
        } else if (kc == keyMapping[InputHandler.KEY_RIGHT]) {
            allKeysState[keyMapping[InputHandler.KEY_LEFT]] = false;
        } else if (kc == keyMapping[InputHandler.KEY_UP]) {
            allKeysState[keyMapping[InputHandler.KEY_DOWN]] = false;
        } else if (kc == keyMapping[InputHandler.KEY_DOWN]) {
            allKeysState[keyMapping[InputHandler.KEY_UP]] = false;
        }
    }

    public void keyReleased(KeyEvent ke) {

        int kc = ke.getKeyCode();
        if (kc >= allKeysState.length) {
            return;
        }

        allKeysState[kc] = false;

        if (id == 0) {
            switch (kc) {
                case KeyEvent.VK_F5: {
                    // Reset game:
                    if (nes.isRunning()) {
                        nes.stopEmulation();
                        nes.reset();
                        nes.reloadRom();
                        nes.startEmulation();
                    }
                    break;
                }
                case KeyEvent.VK_F10: {
                    // Just using this to display the battery RAM contents to user.
                    if (nes.rom != null) {
                        nes.rom.closeRom();
                    }
                    break;
                }
            }
        }

    }

    public JoystickManager getJoystickManager(){
        return this.joystickManager;
    }

    
    public void keyTyped(KeyEvent ke) {
        // Ignore.
    }

    public void reset() {
        allKeysState = new boolean[255];
    }

    public void update() {
        // doesn't do anything.
    }

    public void destroy() {
        nes = null;
    }

    public void cleanup() {
            if (joystickManager != null) {
                joystickManager.releaseAllKeys();
            }
        }


  

   

    

   

   

   
}