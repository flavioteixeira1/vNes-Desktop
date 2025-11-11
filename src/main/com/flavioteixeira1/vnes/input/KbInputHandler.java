package com.flavioteixeira1.vnes;


import java.awt.event.*;
import java.util.Map;

public class KbInputHandler implements KeyListener, InputHandler {

    boolean[] allKeysState;
    int[] keyMapping;
    int id;
    NES nes;

    public KbInputHandler(NES nes, int id) {
        this.nes = nes;
        this.id = id;
        this.allKeysState = new boolean[255];
        this.keyMapping = new int[InputHandler.NUM_KEYS];
    }

    //permite atualizar a instância do NES quando o UI recriar o NES
    public void setNES(NES nes) {
        this.nes = nes;
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isKeyPressed'");
    }

    @Override
    public void setKeyBindings(Map<Integer, Integer> keyBindings) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setKeyBindings'");
    }

    @Override
    public Map<Integer, Integer> getKeyBindings() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getKeyBindings'");
    }

    @Override
    public String getInputType() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getInputType'");
    }
}