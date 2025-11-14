package com.flavioteixeira1.vnes.core;

import net.java.games.input.*;
import java.awt.Robot;
import java.awt.AWTException;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class JoystickManager {
    // Singleton instance
    private static JoystickManager instance;
    
    private Controller joystick;
    private Component[] components;
    private boolean[] lastButtonStates;
    private float[] lastAxisStates;
    private AtomicBoolean joystickEnabled = new AtomicBoolean(false);
    private AtomicBoolean pollingActive = new AtomicBoolean(false);
    private Robot robot;
    
    // Mapeamento de botões para teclas
    private Map<Integer, Integer> buttonToKeyMapping;
    
    // Estados atuais das teclas virtuais
    private boolean[] keyStates;
    
    // Threshold para eixos analógicos
    private final float AXIS_THRESHOLD = 0.5f;
    
    // Thread de polling
    private Thread pollingThread;
    
    // Contador de instâncias para debug
    private static int instanceCount = 0;

    private boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
    
    // Construtor privado para singleton
    private JoystickManager() {
        instanceCount++;
        System.out.println("🎮 JoystickManager instância " + instanceCount + " criada");
        
        try {
            this.robot = new Robot();
            this.keyStates = new boolean[256];
            setupDefaultMapping();
            initJoystick();
            
            if (joystickEnabled.get()) {
                startPollingThread();
            }
        } catch (AWTException e) {
            System.err.println("Erro ao criar Robot: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro ao inicializar joystick: " + e.getMessage());
        }
    }
    
    // Método singleton
    public static synchronized JoystickManager getInstance() {
        if (instance == null) {
            instance = new JoystickManager();
        }
        return instance;
    }
    
    // Método para verificar se o singleton foi inicializado
    public static boolean isInitialized() {
        return instance != null;
    }
    
    private void initJoystick() {
        try {
            System.out.println("🔄 Inicializando joystick...");
            
            // Verificar se JInput está disponível
            try {
                Class.forName("net.java.games.input.ControllerEnvironment");
            } catch (ClassNotFoundException e) {
                System.err.println("JInput não encontrado no classpath");
                return;
            }
            
            ControllerEnvironment env = ControllerEnvironment.getDefaultEnvironment();
            Controller[] controllers = env.getControllers();
            
            if (controllers.length == 0) {
                System.out.println(" Nenhum controller encontrado.");
                return;
            }
            
            boolean found = false;
            for (Controller controller : controllers) {
                System.out.println("Controller disponível: " + controller.getName() + " - Tipo: " + controller.getType());
                
                if (controller.getType() == Controller.Type.STICK || 
                    controller.getType() == Controller.Type.GAMEPAD) {
                    
                    this.joystick = controller;
                    this.components = controller.getComponents();
                    this.lastButtonStates = new boolean[components.length];
                    this.lastAxisStates = new float[components.length];
                    this.joystickEnabled.set(true);
                    
                    System.out.println("Joystick selecionado: " + controller.getName());
                    System.out.println("Componentes: " + components.length);

                     // Detectar automaticamente o offset dos botões
                    int buttonOffset = detectButtonOffset();
                    System.out.println("Offset de botões detectado: " + buttonOffset);
                
                    // Ajustar o mapeamento baseado no offset
                    adjustButtonMapping(buttonOffset);
                    
                    // Debug: listar componentes
                    for (int i = 0; i < components.length; i++) {
                        Component comp = components[i];
                        System.out.println("  Component " + i + ": " + comp.getName() + 
                                         " (" + comp.getIdentifier() + ") - Analog: " + comp.isAnalog());
                    }
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                System.out.println("Nenhum joystick/gamepad compatível encontrado.");
            }
            
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Biblioteca nativa do JInput não disponível: " + e.getMessage());
            System.err.println("O suporte a joystick será desabilitado");
        } catch (Exception e) {
            System.err.println("Erro ao inicializar joystick: " + e.getMessage());
            // Não imprimir stack trace para evitar poluição visual
        }
    }

    private int detectButtonOffset() {
            int buttonCount = 0;
            int firstButtonIndex = -1;
            
            for (int i = 0; i < components.length; i++) {
                Component comp = components[i];
                Component.Identifier id = comp.getIdentifier();
                
                // Verificar se é um botão (não analógico e identificador numérico)
                if (!comp.isAnalog() && 
                    id instanceof Component.Identifier.Button && 
                    id != Component.Identifier.Button.UNKNOWN) {
                    
                    if (firstButtonIndex == -1) {
                        firstButtonIndex = i;
                    }
                    buttonCount++;
                }
            }
            
            System.out.println("Detecção: " + buttonCount + " botões encontrados, começando no índice " + firstButtonIndex);
            
            // Se encontramos botões, usar o primeiro índice como offset
            if (firstButtonIndex != -1) {
                return firstButtonIndex;
            }
            
            // Fallback: procurar por componentes com nome contendo "Button"
            for (int i = 0; i < components.length; i++) {
                Component comp = components[i];
                if (comp.getName().toLowerCase().contains("button") && !comp.isAnalog()) {
                    System.out.println("Botão detectado por nome no índice: " + i);
                    return i;
                }
            }
            
            // Fallback final: assumir que botões começam após os eixos
            int axisCount = 0;
            for (int i = 0; i < components.length; i++) {
                if (components[i].isAnalog()) {
                    axisCount++;
                }
            }
            System.out.println("Fallback: " + axisCount + " eixos detectados, botões começam em: " + axisCount);
            return axisCount;
    }

    private void adjustButtonMapping(int buttonOffset) {
            // Criar novo mapeamento ajustado
            Map<Integer, Integer> adjustedMapping = new HashMap<>();
            // Mapear botões virtuais 0-5 para os índices físicos corretos
            for (int virtualButton = 0; virtualButton <= 5; virtualButton++) {
                int physicalIndex = buttonOffset + virtualButton;
                if (physicalIndex < components.length) {
                    adjustedMapping.put(physicalIndex, buttonToKeyMapping.get(virtualButton));
                }
            }
            // Substituir o mapeamento original
            buttonToKeyMapping = adjustedMapping;
            System.out.println("Mapeamento ajustado com offset " + buttonOffset + ":");
            for (Map.Entry<Integer, Integer> entry : buttonToKeyMapping.entrySet()) {
                System.out.println("  Botão físico " + entry.getKey() + " -> " + getKeyName(entry.getValue()));
            }
    }
    
    private void setupDefaultMapping() {
        buttonToKeyMapping = new HashMap<>();

        int buttonStartIndex = isWindows ? 6 : 0; // Windows: botões começam em 6, Linux em 0
        
        // Mapeamento padrão para NES
        buttonToKeyMapping.put(0, KeyEvent.VK_Z);      // A button
        buttonToKeyMapping.put(1, KeyEvent.VK_X);      // B button  
        buttonToKeyMapping.put(2, KeyEvent.VK_ENTER);  // Start
        buttonToKeyMapping.put(3, KeyEvent.VK_CONTROL); // Select
        buttonToKeyMapping.put(4, KeyEvent.VK_A);      // Extra 1
        buttonToKeyMapping.put(5, KeyEvent.VK_S);      // Extra 2
        
        //System.out.println("🎮 Mapeamento configurado:");
        System.out.println("🎮 Mapeamento configurado (OS: " + System.getProperty("os.name") + "):");
        for (Map.Entry<Integer, Integer> entry : buttonToKeyMapping.entrySet()) {
            System.out.println("  Botão " + entry.getKey() + " -> " + getKeyName(entry.getValue()));
        }
    }
    
    private void startPollingThread() {
        if (pollingThread != null && pollingThread.isAlive()) {
            System.out.println("⚠️ Thread de polling já está ativa");
            return;
        }
        
        pollingActive.set(true);
        pollingThread = new Thread(() -> {
            System.out.println("🔄 Iniciando polling do joystick...");
            while (pollingActive.get()) {
                if (joystickEnabled.get() && joystick != null) {
                    poll();
                }
                try {
                    Thread.sleep(16); // ~60Hz
                } catch (InterruptedException e) {
                    break;
                }
            }
            System.out.println("🔄 Polling do joystick finalizado.");
        });
        pollingThread.setDaemon(true);
        pollingThread.setName("Joystick-Polling");
        pollingThread.start();
    }
    
    private void poll() {
        try {
            if (!joystick.poll()) {
                System.out.println("⚠️ Falha no poll do joystick");
                // Não desabilitar completamente, apenas liberar teclas
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
        } catch (Exception e) {
            System.err.println("❌ Erro durante polling: " + e.getMessage());
            releaseAllKeys();
        }
    }
    
    private void processDigitalComponent(Component comp, float currentValue, int index) {
        boolean currentState = currentValue > 0.5f;
        
        if (currentState != lastButtonStates[index]) {
            lastButtonStates[index] = currentState;

             // Debug: mostrar todos os botões detectados
            if (currentState) {
                System.out.println("Botão físico " + index + " pressionado - Nome: " + comp.getName() + 
                                ", Identificador: " + comp.getIdentifier());
            }
            
            if (buttonToKeyMapping.containsKey(index)) {
                int keyCode = buttonToKeyMapping.get(index);
                dispatchKeyEvent(keyCode, currentState);
                
                // Debug apenas quando pressionado
                if (currentState) {
                    System.out.println("Botão " + index + " pressionado -> " + getKeyName(keyCode));
                }
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
    
    public void pausePolling() {
        pollingActive.set(false);
        releaseAllKeys();
        System.out.println("⏸️ Polling do joystick pausado");
    }
    
    public void resumePolling() {
        if (joystickEnabled.get() && joystick != null) {
            pollingActive.set(true);
            if (pollingThread == null || !pollingThread.isAlive()) {
                startPollingThread();
            }
            System.out.println("▶️ Polling do joystick retomado");
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
        return joystickEnabled.get() && joystick != null;
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
    
    public void cleanup() {
        pausePolling();
        releaseAllKeys();
        joystickEnabled.set(false);
        joystick = null;
        System.out.println("🧹 JoystickManager limpo");
    }
    
    // Método estático para limpeza global
    public static void globalCleanup() {
        if (instance != null) {
            instance.cleanup();
            instance = null;
        }
    }
}