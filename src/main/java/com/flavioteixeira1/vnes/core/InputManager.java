package com.flavioteixeira1.vnes.core;


import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class InputManager {
    public static final int PLAYER_1 = 0;
    public static final int PLAYER_2 = 1;
    
    private InputHandler[] playerHandlers;
    private Map<Integer, Integer> playerConfig; // player -> handler type
    
    public InputManager(NES nes) {
        playerHandlers = new InputHandler[2];
        playerConfig = new HashMap<>();
        
        // Configuração padrão: Player 1 = Teclado, Player 2 = Desabilitado
        initializeDefaultConfig(nes);
    }
    
    private void initializeDefaultConfig(NES nes) {
        // Player 1: Teclado
        //playerHandlers[PLAYER_1] = new KbInputHandler(nes, PLAYER_1);
        //playerConfig.put(PLAYER_1, InputConfig.HANDLER_KEYBOARD);
         setPlayerHandler(PLAYER_1, InputConfig.HANDLER_KEYBOARD, nes);

        // Configurar mapeamentos padrão do teclado para Player 1
        setupDefaultKeyboardMappings((KbInputHandler) playerHandlers[PLAYER_1], PLAYER_1);
        
        // Player 2: Inicialmente desabilitado
       // playerHandlers[PLAYER_2] = new DummyInputHandler();
       // playerConfig.put(PLAYER_2, InputConfig.HANDLER_DISABLED);
         setPlayerHandler(PLAYER_2, InputConfig.HANDLER_DISABLED, nes);
    }

    private void setupDefaultKeyboardMappings(KbInputHandler handler, int player) {
        if (player == PLAYER_1) {
            // Player 1: Z, X, Enter, Control e setas
            handler.mapKey(InputHandler.KEY_A, KeyEvent.VK_Z);
            handler.mapKey(InputHandler.KEY_B, KeyEvent.VK_X);
            handler.mapKey(InputHandler.KEY_START, KeyEvent.VK_ENTER);
            handler.mapKey(InputHandler.KEY_SELECT, KeyEvent.VK_CONTROL);
            handler.mapKey(InputHandler.KEY_UP, KeyEvent.VK_UP);
            handler.mapKey(InputHandler.KEY_DOWN, KeyEvent.VK_DOWN);
            handler.mapKey(InputHandler.KEY_LEFT, KeyEvent.VK_LEFT);
            handler.mapKey(InputHandler.KEY_RIGHT, KeyEvent.VK_RIGHT);
            System.out.println("Mapeamentos do teclado configurados para Player 1");
        } else {
            // Player 2: Teclado numérico (se for teclado)
            handler.mapKey(InputHandler.KEY_A, KeyEvent.VK_NUMPAD7);
            handler.mapKey(InputHandler.KEY_B, KeyEvent.VK_NUMPAD9);
            handler.mapKey(InputHandler.KEY_START, KeyEvent.VK_NUMPAD1);
            handler.mapKey(InputHandler.KEY_SELECT, KeyEvent.VK_NUMPAD3);
            handler.mapKey(InputHandler.KEY_UP, KeyEvent.VK_NUMPAD8);
            handler.mapKey(InputHandler.KEY_DOWN, KeyEvent.VK_NUMPAD2);
            handler.mapKey(InputHandler.KEY_LEFT, KeyEvent.VK_NUMPAD4);
            handler.mapKey(InputHandler.KEY_RIGHT, KeyEvent.VK_NUMPAD6);
             System.out.println("Mapeamentos do teclado configurados para Player 2");
        }
    }
    
    
    public void setPlayerHandler(int player, int handlerType, NES nes) {
        if (player < 0 || player > 1) 
        {
             System.err.println("Player inválido: " + player);
            return;}
        System.out.println("Configurando Player " + (player + 1) + " para: " + 
        
        playerConfig.put(player, handlerType));

        System.out.println("Tipo solicitado: " + handlerType + " (" + InputConfig.getHandlerName(handlerType) + ")");
        System.out.println("NES: " + (nes != null ? "OK" : "NULL"));
        playerConfig.put(player, handlerType);
        try{
        switch (handlerType) {
            case InputConfig.HANDLER_KEYBOARD:
                playerHandlers[player] = new KbInputHandler(nes, player);
                setupDefaultKeyboardMappings((KbInputHandler) playerHandlers[player], player);
                break;
            case InputConfig.HANDLER_JOYSTICK:
                playerHandlers[player] = new JoystickInputHandler();
                 // O JoystickInputHandler já tem seus mapeamentos padrão internos
                break;
            case InputConfig.HANDLER_DISABLED:
                playerHandlers[player] = new DummyInputHandler();
                break;
            default:
                System.err.println("Tipo de handler desconhecido: " + handlerType);
                playerHandlers[player] = new DummyInputHandler();
                break;
        }

            } catch (Exception e) {
            System.err.println("ERRO ao configurar handler: " + e.getMessage());
            e.printStackTrace();
            // Fallback para handler dummy
            playerHandlers[player] = new DummyInputHandler();
        }

    }

    public void updateNESReference(NES nes) {
    System.out.println("InputManager - Atualizando referência do NES");
    
        for (int i = 0; i < playerHandlers.length; i++) {
            if (playerHandlers[i] instanceof KbInputHandler) {
                ((KbInputHandler) playerHandlers[i]).setNES(nes);
            }
        }
    }
    
    
    
    public InputHandler getPlayerHandler(int player) {
        if (player < 0 || player > 1) return null;
        return playerHandlers[player];
    }
    
    public int getPlayerHandlerType(int player) {
        return playerConfig.getOrDefault(player, InputConfig.HANDLER_DISABLED);
    }
    
    public void update() {
        for (InputHandler handler : playerHandlers) {
            if (handler != null) {
                handler.update();
            }
        }
    }
    
    public void reset() {
        for (InputHandler handler : playerHandlers) {
            if (handler != null) {
                handler.reset();
            }
        }
    }
}