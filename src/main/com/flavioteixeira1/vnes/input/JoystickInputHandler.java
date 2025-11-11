package com.flavioteixeira1.vnes;


import java.awt.event.KeyEvent;
import java.util.*;
import net.java.games.input.*;


public class JoystickInputHandler implements InputHandler {
    private Map<Integer, Boolean> buttonStates;
    private Map<Integer, Integer> keyBindings;
    private Controller joystick;
    private boolean initialized;
    
    // Constantes para os botões do joystick
    public static final int BUTTON_0 = 0;
    public static final int BUTTON_1 = 1;
    public static final int BUTTON_2 = 2;
    public static final int BUTTON_3 = 3;
    public static final int BUTTON_4 = 4;
    public static final int BUTTON_5 = 5;
    public static final int BUTTON_6 = 6;
    public static final int BUTTON_7 = 7;
    public static final int BUTTON_8 = 8;
    public static final int BUTTON_9 = 9;
    
    public static final int AXIS_UP = 10;
    public static final int AXIS_DOWN = 11;
    public static final int AXIS_LEFT = 12;
    public static final int AXIS_RIGHT = 13;
    
    public static final int HAT_UP = 14;
    public static final int HAT_DOWN = 15;
    public static final int HAT_LEFT = 16;
    public static final int HAT_RIGHT = 17;
    
    public JoystickInputHandler() {
        buttonStates = new HashMap<>();
        keyBindings = new HashMap<>();
        initializeDefaultBindings();
        initializeJoystick();
    }
    
    private void initializeDefaultBindings() {
        // Bindings padrão
        keyBindings.put(BUTTON_0, KeyEvent.VK_Z);      // A do NES
        keyBindings.put(BUTTON_1, KeyEvent.VK_X);      // B do NES
        keyBindings.put(BUTTON_2, KeyEvent.VK_A);      // Turbo A
        keyBindings.put(BUTTON_3, KeyEvent.VK_S);      // Turbo B
        keyBindings.put(BUTTON_6, KeyEvent.VK_ENTER);  // Start
        keyBindings.put(BUTTON_7, KeyEvent.VK_SPACE);  // Select
        keyBindings.put(AXIS_UP, KeyEvent.VK_UP);      // Cima
        keyBindings.put(AXIS_DOWN, KeyEvent.VK_DOWN);  // Baixo
        keyBindings.put(AXIS_LEFT, KeyEvent.VK_LEFT);  // Esquerda
        keyBindings.put(AXIS_RIGHT, KeyEvent.VK_RIGHT); // Direita
        keyBindings.put(HAT_UP, KeyEvent.VK_UP);       // Hat Cima
        keyBindings.put(HAT_DOWN, KeyEvent.VK_DOWN);   // Hat Baixo
        keyBindings.put(HAT_LEFT, KeyEvent.VK_LEFT);   // Hat Esquerda
        keyBindings.put(HAT_RIGHT, KeyEvent.VK_RIGHT); // Hat Direita
    }
    
    private void initializeJoystick() {
        try {
            ControllerEnvironment env = ControllerEnvironment.getDefaultEnvironment();
            Controller[] controllers = env.getControllers();
            
            for (Controller controller : controllers) {
                if (controller.getType() == Controller.Type.GAMEPAD || 
                    controller.getType() == Controller.Type.STICK ||
                    controller.getName().toLowerCase().contains("joystick") ||
                    controller.getName().toLowerCase().contains("gamepad")) {
                    
                    joystick = controller;
                    System.out.println("Joystick encontrado: " + controller.getName());
                    initialized = true;
                    break;
                }
            }
            
            if (joystick == null) {
                System.out.println("Nenhum joystick encontrado");
            }
            
        } catch (Exception e) {
            System.err.println("Erro ao inicializar joystick: " + e.getMessage());
        }
    }
    
    @Override
    public void update() {
        if (!initialized || joystick == null) return;
        
        try {
            if (!joystick.poll()) {
                // Controller disconnected
                initialized = false;
                joystick = null;
                return;
            }
            
            // Limpa estados anteriores
            for (Integer key : buttonStates.keySet()) {
                buttonStates.put(key, false);
            }
            
            // Processa componentes
            Component[] components = joystick.getComponents();
            for (Component component : components) {
                processComponent(component);
            }
            
        } catch (Exception e) {
            System.err.println("Erro ao atualizar joystick: " + e.getMessage());
            initialized = false;
            joystick = null;
        }
    }
    
    private void processComponent(Component component) {
        float value = component.getPollData();
        Component.Identifier id = component.getIdentifier();
        
        if (component.isAnalog()) {
            processAnalogComponent(component, value);
        } else if (id instanceof Component.Identifier.Button) {
            processButtonComponent((Component.Identifier.Button) id, value);
        } else if (id instanceof Component.Identifier.Axis) {
            processAxisComponent((Component.Identifier.Axis) id, value);
        } 
    }
    
    private void processAnalogComponent(Component component, float value) {
        Component.Identifier id = component.getIdentifier();
        float deadZone = 0.3f;
        
        if (Math.abs(value) > deadZone) {
            if (id == Component.Identifier.Axis.X) {
                if (value > 0) {
                    buttonStates.put(AXIS_RIGHT, true);
                } else {
                    buttonStates.put(AXIS_LEFT, true);
                }
            } else if (id == Component.Identifier.Axis.Y) {
                if (value > 0) {
                    buttonStates.put(AXIS_DOWN, true);
                } else {
                    buttonStates.put(AXIS_UP, true);
                }
            }
        }
    }
    
    private void processButtonComponent(Component.Identifier.Button button, float value) {
        if (value == 1.0f) {
            int buttonIndex = getButtonIndex(button);
            if (buttonIndex >= 0) {
                buttonStates.put(buttonIndex, true);
            }
        }
    }
    
    private void processAxisComponent(Component.Identifier.Axis axis, float value) {
        // Para eixos digitais (como em alguns gamepads)
        if (Math.abs(value) == 1.0f) {
            if (axis == Component.Identifier.Axis.X) {
                if (value > 0) {
                    buttonStates.put(AXIS_RIGHT, true);
                } else {
                    buttonStates.put(AXIS_LEFT, true);
                }
            } else if (axis == Component.Identifier.Axis.Y) {
                if (value > 0) {
                    buttonStates.put(AXIS_DOWN, true);
                } else {
                    buttonStates.put(AXIS_UP, true);
                }
            }
        }
    }
    
    private void processDPadComponent(float value) {
        // Processa D-Pad (POV)
        if (value == 0.25f || value == 0.375f || value == 0.125f) {
            buttonStates.put(HAT_UP, true);    // Up
        }
        if (value == 0.75f || value == 0.625f || value == 0.875f) {
            buttonStates.put(HAT_DOWN, true);  // Down
        }
        if (value == 0.875f || value == 0.125f || value == 0.0f) {
            buttonStates.put(HAT_LEFT, true);  // Left
        }
        if (value == 0.375f || value == 0.625f || value == 0.5f) {
            buttonStates.put(HAT_RIGHT, true); // Right
        }
    }
    
    private int getButtonIndex(Component.Identifier.Button button) {
        String buttonName = button.getName();
        try {
            // Extrai número do botão (ex: "0", "1", etc)
            if (buttonName.startsWith("Button ")) {
                return Integer.parseInt(buttonName.substring(7));
            } else if (buttonName.matches("^[0-9]+$")) {
                return Integer.parseInt(buttonName);
            }
        } catch (NumberFormatException e) {
            // Mapeamento para botões nomeados
            if (buttonName.contains("A")) return BUTTON_0;
            if (buttonName.contains("B")) return BUTTON_1;
            if (buttonName.contains("X")) return BUTTON_2;
            if (buttonName.contains("Y")) return BUTTON_3;
            if (buttonName.contains("L")) return BUTTON_4;
            if (buttonName.contains("R")) return BUTTON_5;
            if (buttonName.contains("Start")) return BUTTON_6;
            if (buttonName.contains("Select") || buttonName.contains("Back")) return BUTTON_7;
        }
        return -1;
    }
    
    @Override
    public boolean isKeyPressed(int keyCode) {
        for (Map.Entry<Integer, Integer> entry : keyBindings.entrySet()) {
            if (entry.getValue() == keyCode) {
                Boolean state = buttonStates.get(entry.getKey());
                return state != null && state;
            }
        }
        return false;
    }
    
    @Override
    public void reset() {
        buttonStates.clear();
    }
    
    @Override
    public void setKeyBindings(Map<Integer, Integer> keyBindings) {
        this.keyBindings = new HashMap<>(keyBindings);
    }
    
    @Override
    public Map<Integer, Integer> getKeyBindings() {
        return new HashMap<>(keyBindings);
    }
    
    @Override
    public String getInputType() {
        return "JOYSTICK";
    }
    
    public boolean isConnected() {
        return initialized && joystick != null;
    }
    
    public String getJoystickName() {
        return joystick != null ? joystick.getName() : "Nenhum joystick conectado";
    }
    
    public void reconnect() {
        initializeJoystick();
    }

    @Override
    public void mapKey(int padKey, int deviceKey) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mapKey'");
    }

    @Override
    public short getKeyState(int padKey) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getKeyState'");
    }
}