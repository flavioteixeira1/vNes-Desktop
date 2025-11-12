package com.flavioteixeira1.vnes.core;


import net.java.games.input.*;
import java.awt.Robot;
import java.awt.AWTException;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class JoystickManager {
    private Controller joystick;
    private Component[] components;
    private boolean[] lastButtonStates;
    private float[] lastAxisStates;
    private boolean joystickEnabled = false;
    private Robot robot;
    
    // Mapeamento de botões para teclas
    private Map<Integer, Integer> buttonToKeyMapping;
    
    // Estados atuais das teclas virtuais
    private boolean[] keyStates;
    
    // Threshold para eixos analógicos
    private final float AXIS_THRESHOLD = 0.5f;
    
    public JoystickManager() {
        try {
            this.robot = new Robot();
            this.keyStates = new boolean[256];
            setupDefaultMapping();
            initJoystick();
            
            if (joystickEnabled) {
                startPollingThread();
            }
        } catch (AWTException e) {
            System.err.println("Erro ao criar Robot: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro ao inicializar joystick: " + e.getMessage());
        }
    }
    
    private void initJoystick() {
        try {
            ControllerEnvironment env = ControllerEnvironment.getDefaultEnvironment();
            Controller[] controllers = env.getControllers();
            
            if (controllers.length == 0) {
                System.out.println("Nenhum controller encontrado.");
                return;
            }
            
            for (Controller controller : controllers) {
                System.out.println("Controller disponível: " + controller.getName() + " - Tipo: " + controller.getType());
                
                if (controller.getType() == Controller.Type.STICK || 
                    controller.getType() == Controller.Type.GAMEPAD) {
                    
                    this.joystick = controller;
                    this.components = controller.getComponents();
                    this.lastButtonStates = new boolean[components.length];
                    this.lastAxisStates = new float[components.length];
                    this.joystickEnabled = true;
                    
                    System.out.println("✅ Joystick selecionado: " + controller.getName());
                    System.out.println("📋 Componentes: " + components.length);
                    
                    // Debug: listar componentes
                    for (int i = 0; i < components.length; i++) {
                        Component comp = components[i];
                        System.out.println("  Component " + i + ": " + comp.getName() + 
                                         " (" + comp.getIdentifier() + ") - Analog: " + comp.isAnalog());
                    }
                    break;
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao inicializar joystick: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void setupDefaultMapping() {
        buttonToKeyMapping = new HashMap<>();
        
        // Mapeamento padrão para NES
        buttonToKeyMapping.put(0, KeyEvent.VK_Z);      // A button
        buttonToKeyMapping.put(1, KeyEvent.VK_X);      // B button  
        buttonToKeyMapping.put(2, KeyEvent.VK_ENTER);  // Start
        buttonToKeyMapping.put(3, KeyEvent.VK_CONTROL); // Select
        buttonToKeyMapping.put(4, KeyEvent.VK_A);      // Extra 1
        buttonToKeyMapping.put(5, KeyEvent.VK_S);      // Extra 2
        
        System.out.println("🎮 Mapeamento configurado:");
        for (Map.Entry<Integer, Integer> entry : buttonToKeyMapping.entrySet()) {
            System.out.println("  Botão " + entry.getKey() + " -> " + getKeyName(entry.getValue()));
        }
    }


    
    
    private void startPollingThread() {
        Thread pollingThread = new Thread(() -> {
            System.out.println("🔄 Iniciando polling do joystick...");
            while (true) {
                if (joystickEnabled && joystick != null) {
                    poll();
                }
                try {
                    Thread.sleep(16); // ~60Hz
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        pollingThread.setDaemon(true);
        pollingThread.setName("Joystick-Polling");
        pollingThread.start();
    }
    
    private void poll() {
        if (!joystick.poll()) {
            System.out.println("❌ Joystick desconectado");
            joystickEnabled = false;
            releaseAllKeys();
            return;
        }
        
        for (int i = 0; i < components.length; i++) {
            Component comp = components[i];
            float currentValue = comp.getPollData();
            
            if (comp.isAnalog()) {
                processAnalogComponent(comp, currentValue, i);
            } else {
                processDigitalComponent(comp, currentValue, i);
            }
        }
    }
    
    private void processDigitalComponent(Component comp, float currentValue, int index) {
        boolean currentState = currentValue > 0.5f;
        
        if (currentState != lastButtonStates[index]) {
            lastButtonStates[index] = currentState;
            
            if (buttonToKeyMapping.containsKey(index)) {
                int keyCode = buttonToKeyMapping.get(index);
                dispatchKeyEvent(keyCode, currentState);
                
                // Debug
               // if (currentState) {
               //     System.out.println("🎯 Botão " + index + " -> " + getKeyName(keyCode));
              //  }
            }
        }
    }
    
    private void processAnalogComponent(Component comp, float currentValue, int index) {
        Component.Identifier identifier = comp.getIdentifier();
        
        if (identifier == Component.Identifier.Axis.X) {
            processAxis(currentValue, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT);
        } 
        else if (identifier == Component.Identifier.Axis.Y) {
            processAxis(currentValue, KeyEvent.VK_UP, KeyEvent.VK_DOWN);
        }
        else if (identifier == Component.Identifier.Axis.POV) {
            processPOVAxis(currentValue);
        }
        
        lastAxisStates[index] = currentValue;
    }
    
    private void processAxis(float currentValue, int negativeKey, int positiveKey) {
        boolean negativePressed = currentValue < -AXIS_THRESHOLD;
        boolean positivePressed = currentValue > AXIS_THRESHOLD;
        
        if (negativePressed != keyStates[negativeKey]) {
            dispatchKeyEvent(negativeKey, negativePressed);
        }
        
        if (positivePressed != keyStates[positiveKey]) {
            dispatchKeyEvent(positiveKey, positivePressed);
        }
    }
    
    private void processPOVAxis(float povValue) {
        boolean upPressed = false, downPressed = false, leftPressed = false, rightPressed = false;
        
        if (povValue != Component.POV.OFF) {
            // POV retorna valor entre 0.0 e 1.0 para a direção
            if (povValue == Component.POV.UP) {
                upPressed = true;
            } else if (povValue == Component.POV.DOWN) {
                downPressed = true;
            } else if (povValue == Component.POV.LEFT) {
                leftPressed = true;
            } else if (povValue == Component.POV.RIGHT) {
                rightPressed = true;
            } else if (povValue == Component.POV.UP_LEFT) {
                upPressed = leftPressed = true;
            } else if (povValue == Component.POV.UP_RIGHT) {
                upPressed = rightPressed = true;
            } else if (povValue == Component.POV.DOWN_LEFT) {
                downPressed = leftPressed = true;
            } else if (povValue == Component.POV.DOWN_RIGHT) {
                downPressed = rightPressed = true;
            }
        }
        
        if (upPressed != keyStates[KeyEvent.VK_UP]) {
            dispatchKeyEvent(KeyEvent.VK_UP, upPressed);
        }
        if (downPressed != keyStates[KeyEvent.VK_DOWN]) {
            dispatchKeyEvent(KeyEvent.VK_DOWN, downPressed);
        }
        if (leftPressed != keyStates[KeyEvent.VK_LEFT]) {
            dispatchKeyEvent(KeyEvent.VK_LEFT, leftPressed);
        }
        if (rightPressed != keyStates[KeyEvent.VK_RIGHT]) {
            dispatchKeyEvent(KeyEvent.VK_RIGHT, rightPressed);
        }
    }
    
    private void dispatchKeyEvent(int keyCode, boolean pressed) {
        if (keyCode == -1) return;
        
        try {
            if (pressed) {
                robot.keyPress(keyCode);
                keyStates[keyCode] = true;
            } else {
                robot.keyRelease(keyCode);
                keyStates[keyCode] = false;
            }
        } catch (Exception e) {
            System.err.println("❌ Erro ao enviar tecla " + keyCode + ": " + e.getMessage());
        }
    }
    
    public void setButtonMapping(int buttonIndex, int keyCode) {
        buttonToKeyMapping.put(buttonIndex, keyCode);
        System.out.println("🔄 Remapeado botão " + buttonIndex + " para " + getKeyName(keyCode));
    }
    
    private String getKeyName(int keyCode) {
        try {
            return KeyEvent.getKeyText(keyCode);
        } catch (Exception e) {
            return "Unknown(" + keyCode + ")";
        }
    }
    
    public boolean isJoystickEnabled() {
        return joystickEnabled;
    }
    
    public String getJoystickName() {
        return joystick != null ? joystick.getName() : "Nenhum joystick";
    }
    
    public void releaseAllKeys() {
        for (int i = 0; i < keyStates.length; i++) {
            if (keyStates[i]) {
                try {
                    robot.keyRelease(i);
                    keyStates[i] = false;
                } catch (Exception e) {
                    // Ignorar
                }
            }
        }
    }
}