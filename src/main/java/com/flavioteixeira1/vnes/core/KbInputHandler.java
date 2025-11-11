package com.flavioteixeira1.vnes.core;


import java.awt.event.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class KbInputHandler implements KeyListener, InputHandler {

    boolean[] allKeysState;
    int[] keyMapping;
    int id;
    NES nes;
   private Map<Integer, Integer> currentKeyBindings;


    public KbInputHandler(NES nes, int id) {
        this.nes = nes;
        this.id = id;
        this.allKeysState = new boolean[512]; // Aumentado para cobrir todas teclas
        this.keyMapping = new int[InputHandler.NUM_KEYS];
        this.currentKeyBindings = new HashMap<>();
        // Inicializar arrays com valores padrão
        Arrays.fill(allKeysState, false);
        Arrays.fill(keyMapping, 0); // 0 = tecla não mapeada
        System.out.println("KbInputHandler criado para Player " + id);
    }

    //permite atualizar a instância do NES quando o UI recriar o NES
    public void setNES(NES nes) {
        this.nes = nes;
    }

    @Override
    public short getKeyState(int padKey) {
            if (padKey < 0 || padKey >= keyMapping.length) {
                System.out.println("getKeyState - PadKey inválido: " + padKey);
                return 0x40; // Não pressionado
            }
            
            int keyCode = keyMapping[padKey];
            if (keyCode < 0 || keyCode >= allKeysState.length) {
                System.out.println("getKeyState - KeyCode inválido: " + keyCode + " para padKey: " + padKey);
                return 0x40; // Não pressionado
            }
            
            boolean isPressed = allKeysState[keyCode];
            short result = (short) (isPressed ? 0x41 : 0x40);
            
            // Debug: mostrar apenas quando pressionado para não poluir o console
            if (isPressed) {
                System.out.println("KEY PRESSED - Player: " + id + ", PadKey: " + padKey + 
                                ", KeyCode: " + keyCode + ", Result: " + result);
            }
            
            return result;
    }


    public void printKeyMappings() {
        System.out.println("=== MAPEAMENTOS DO PLAYER " + id + " ===");
        String[] keyNames = {"A", "B", "START", "SELECT", "UP", "DOWN", "LEFT", "RIGHT"};
        for (int i = 0; i < keyMapping.length; i++) {
            if (i < keyNames.length) {
                System.out.println(keyNames[i] + " -> KeyCode: " + keyMapping[i] + 
                                " (" + KeyEvent.getKeyText(keyMapping[i]) + ")");
            }
        }
        System.out.println("=== FIM MAPEAMENTOS ===");
    }

    
    public void mapKey(int padKey, int kbKeycode) {
        keyMapping[padKey] = kbKeycode;
    }

    public void keyPressed(KeyEvent ke) {

        int kc = ke.getKeyCode();

        // Debug
        System.out.println("KEY PRESSED - Player: " + id + ", KeyCode: " + kc +" (" + KeyEvent.getKeyText(kc) + ")");


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
        // Debug
        System.out.println("KEY RELEASED - Player: " + id + ", KeyCode: " + kc +" (" + KeyEvent.getKeyText(kc) + ")");

        if (kc >= allKeysState.length) {
            return;
        }

        allKeysState[kc] = false;

        if (id == 0) {
            switch (kc) {
                case KeyEvent.VK_F5: {
                     // Salvar estado (slot 0). Se sua API NES for diferente, ajuste a chamada.
                    try {
                        boolean sucesso;
                        sucesso = nes.saveState(null);
                        if (sucesso) {
                            System.out.println("Save state salvo (F5) para ROM: " + (nes.getRom() != null ? nes.getRom().getFileName() : "unknown"));
                        } else {
                            System.err.println("Falha ao salvar save state (F5).");
                        }
                    } catch (Exception ex) {
                        System.err.println("Erro ao executar save (F5): " + ex.getMessage());
                        ex.printStackTrace();
                    }
                    break;
                }
                case KeyEvent.VK_F6: {
                    // Carregar estado (slot 0).
                    try {
                        boolean sucesso;
                        sucesso = nes.loadState();
                        if (sucesso) {
                            System.out.println("Save state carregado (F6) para ROM: " + (nes.getRom() != null ? nes.getRom().getFileName() : "unknown"));
                        } else {
                            System.err.println("Nenhum save state encontrado ou falha ao carregar (F6).");
                        }
                    } catch (Exception ex) {
                        System.err.println("Erro ao executar load (F6): " + ex.getMessage());
                        ex.printStackTrace();
                    }
                    break;
                }
                case KeyEvent.VK_F2: {
                    //System.out.println("f2 pressionada");
                    
                    }
                    break;
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

    @Override
    public boolean isKeyPressed(int keyCode) {
        return keyCode >= 0 && keyCode < allKeysState.length && allKeysState[keyCode];
    }

    @Override
    public void setKeyBindings(Map<Integer, Integer> keyBindings) {
        this.currentKeyBindings = new HashMap<>(keyBindings);
        // Aplicar os mapeamentos
        for (Map.Entry<Integer, Integer> entry : keyBindings.entrySet()) {
            mapKey(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Map<Integer, Integer> getKeyBindings() {
       return new HashMap<>(currentKeyBindings);
    }

    @Override
    public String getInputType() {
         return "KEYBOARD";
    }
}