package com.flavioteixeira1.vnes.core;

import net.java.games.input.*;
import java.awt.Robot;
import java.awt.AWTException;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.ArrayList;
import java.util.List;


public class JoystickManager {
    // Singleton instance
    private static JoystickManager instancePlayer1;
    private static JoystickManager instancePlayer2;
    private Controller joystick;
    private Component[] components;
    private boolean[] lastButtonStates;
    private float[] lastAxisStates;
    private AtomicBoolean joystickEnabled = new AtomicBoolean(false);
    private AtomicBoolean pollingActive = new AtomicBoolean(false);
    private Robot robot;

    //Mapeamento para eixos
    private Map<Component.Identifier, Integer[]> axisToKeyMapping;
    
    // Mapeamento de botões para teclas
    private Map<Integer, Integer> buttonToKeyMapping;
    
    // Estados atuais das teclas virtuais
    private boolean[] keyStates;
    
    // Threshold para eixos analógicos
    private final float AXIS_THRESHOLD = 0.5f;
    
    // Thread de polling
    private Thread pollingThread;

    // Identificador do jogador (0 = Player 1, 1 = Player 2)
    private int playerId;

    // Nome do joystick para debug
    private String joystickName;

    private Map<Integer, Integer> customButtonMapping;
    private Map<Component.Identifier, Integer[]> customAxisMapping;
    private boolean useCustomMapping = false;

    
    // Contador de instâncias para debug
    private static int instanceCount = 0;

    private boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
    
    // Construtor privado 
    private JoystickManager(int playerId) {
        this.playerId = playerId;
        System.out.println(" JoystickManager instância criada para Player " + (playerId + 1));
        
        try {
            this.robot = new Robot();
            this.keyStates = new boolean[256];
            setupDefaultMapping();
            setupCustomMapping(); 
            initJoystick(playerId);
            
            if (joystickEnabled.get()) {
                startPollingThread();
            }
        } catch (AWTException e) {
            System.err.println("Erro ao criar Robot para Player " + (playerId + 1) + ": " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro ao inicializar joystick para Player " + (playerId + 1) + ": " + e.getMessage());
        }
    }

    private void setupCustomMapping() {
    customButtonMapping = new HashMap<>();
    customAxisMapping = new HashMap<>();
    // Inicializar com os mesmos valores do mapeamento padrão
    customButtonMapping.putAll(buttonToKeyMapping);
    customAxisMapping.putAll(axisToKeyMapping);
    }
    
    // Métodos estáticos para obter instâncias específicas
    public static synchronized JoystickManager  getInstanceForPlayer(int playerId) {
        if (playerId == 0) {
            if (instancePlayer1 == null) {
                instancePlayer1 = new JoystickManager(0);
            }
            return instancePlayer1;
        } else {
            if (instancePlayer2 == null) {
                instancePlayer2 = new JoystickManager(1);
            }
            return instancePlayer2;
        }
    }
    
     // Método para verificar se uma instância foi inicializada
    public static boolean isInitializedForPlayer(int playerId) {
       if (playerId == 0) {
            return instancePlayer1 != null;
        } else {
            return instancePlayer2 != null;
        }
    }
    
    private void initJoystick(int playerId) {
        try {
            System.out.println(" Inicializando joystick para Player " + (playerId + 1)+ "...");
            
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
             // Lista de controladores disponíveis
            List<Controller> availableControllers = new ArrayList<>();
            for (Controller controller : controllers) {
                if (controller.getType() == Controller.Type.STICK || 
                    controller.getType() == Controller.Type.GAMEPAD) {
                    availableControllers.add(controller);
                }
            }
            
            if (availableControllers.isEmpty()) {
                System.out.println(" Nenhum joystick/gamepad compatível encontrado para Player " + (playerId + 1));
                return;
            }
            
            // Selecionar controlador baseado no playerId
            if (playerId < availableControllers.size()) {
                this.joystick = availableControllers.get(playerId);
                this.components = joystick.getComponents();
                this.lastButtonStates = new boolean[components.length];
                this.lastAxisStates = new float[components.length];
                this.joystickEnabled.set(true);
                this.joystickName = joystick.getName();
                
                System.out.println("Joystick selecionado para Player " + (playerId + 1) + ": " + joystickName);
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
            } else {
                System.out.println("Não há controlador suficiente para Player " + (playerId + 1));
                System.out.println("Controladores disponíveis: " + availableControllers.size());
                for (int i = 0; i < availableControllers.size(); i++) {
                    System.out.println("  " + i + ": " + availableControllers.get(i).getName());
                }
            }
            
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Biblioteca nativa do JInput não disponível: " + e.getMessage());
            System.err.println("O suporte a joystick será desabilitado");
        } catch (Exception e) {
            System.err.println("Erro ao inicializar joystick para Player " + (playerId + 1) + ": " + e.getMessage());
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
        axisToKeyMapping = new HashMap<>();

        int buttonStartIndex = isWindows ? 6 : 0; // Windows: botões começam em 6, Linux em 0
        
        if (playerId == 0) {
            // Player 1 - Botões
            buttonToKeyMapping.put(0, KeyEvent.VK_Z);      // A button
            buttonToKeyMapping.put(1, KeyEvent.VK_X);      // B button  
            buttonToKeyMapping.put(2, KeyEvent.VK_ENTER);  // Start
            buttonToKeyMapping.put(3, KeyEvent.VK_CONTROL); // Select
            // Player 1 - Eixos
            axisToKeyMapping.put(Component.Identifier.Axis.X,new Integer[]{KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT});
            axisToKeyMapping.put(Component.Identifier.Axis.Y,new Integer[]{KeyEvent.VK_UP, KeyEvent.VK_DOWN});
        } else {
            // Player 2 - Botões
            buttonToKeyMapping.put(0, KeyEvent.VK_NUMPAD7); // A button
            buttonToKeyMapping.put(1, KeyEvent.VK_NUMPAD9); // B button  
            buttonToKeyMapping.put(2, KeyEvent.VK_NUMPAD1); // Start
            buttonToKeyMapping.put(3, KeyEvent.VK_NUMPAD3); // Select
             // Player 2 - Eixos
            axisToKeyMapping.put(Component.Identifier.Axis.X,new Integer[]{KeyEvent.VK_NUMPAD4, KeyEvent.VK_NUMPAD6});
            axisToKeyMapping.put(Component.Identifier.Axis.Y,new Integer[]{KeyEvent.VK_NUMPAD8, KeyEvent.VK_NUMPAD2});
        }
        
        System.out.println(" Mapeamento configurado para Player " + (playerId + 1) + " (OS: " + System.getProperty("os.name") + "):");
        for (Map.Entry<Integer, Integer> entry : buttonToKeyMapping.entrySet()) {
            System.out.println("  Botão " + entry.getKey() + " -> " + getKeyName(entry.getValue()));
        }
    }

    public void setAxisMapping(Component.Identifier axis, int negativeKey, int positiveKey) {
        axisToKeyMapping.put(axis, new Integer[]{negativeKey, positiveKey});
        System.out.println("Eixo " + axis + " mapeado para " + 
                         getKeyName(negativeKey) + "/" + getKeyName(positiveKey));
    }

    
    private void startPollingThread() {
        if (pollingThread != null && pollingThread.isAlive()) {
            System.out.println(" Thread de polling já está ativa");
            return;
        }
        
        pollingActive.set(true);
        pollingThread = new Thread(() -> {
            System.out.println(" Iniciando polling do joystick...");
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
            System.out.println(" Polling do joystick finalizado.");
        });
        pollingThread.setDaemon(true);
        pollingThread.setName("Joystick-Polling");
        pollingThread.start();
    }
    
    private void poll() {
        try {
            if (!joystick.poll()) {
                System.out.println(" Falha no poll do joystick");
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
            System.err.println(" Erro durante polling: " + e.getMessage());
            releaseAllKeys();
        }
    }
    
    private void processDigitalComponent(Component comp, float currentValue, int index) {
        boolean currentState = currentValue > 0.5f;
        
        if (currentState != lastButtonStates[index]) {
            lastButtonStates[index] = currentState;
            Map<Integer, Integer> activeMapping = useCustomMapping ? customButtonMapping : buttonToKeyMapping;   
            if (buttonToKeyMapping.containsKey(index)) {
                int keyCode = buttonToKeyMapping.get(index);
                dispatchKeyEvent(keyCode, currentState);
            }
        }
    }
    
    private void processAnalogComponent(Component comp, float currentValue, int index) {
        Component.Identifier identifier = comp.getIdentifier();
        Map<Component.Identifier, Integer[]> activeAxisMapping = useCustomMapping ? customAxisMapping : axisToKeyMapping;
        if (axisToKeyMapping.containsKey(identifier)) {
            Integer[] keys = axisToKeyMapping.get(identifier);
            processAxis(currentValue, keys[0], keys[1]);
        } else if (identifier == Component.Identifier.Axis.POV) {
            processPOVAxis(currentValue);
        }
            
            lastAxisStates[index] = currentValue;
    }

    public void setCustomButtonMapping(int buttonIndex, int keyCode) {
    customButtonMapping.put(buttonIndex, keyCode);
    System.out.println("Botão customizado " + buttonIndex + " mapeado para " + getKeyName(keyCode));
    }

    public void setCustomAxisMapping(Component.Identifier axis, int negativeKey, int positiveKey) {
    customAxisMapping.put(axis, new Integer[]{negativeKey, positiveKey});
    System.out.println("Eixo customizado " + axis + " mapeado para " + 
                     getKeyName(negativeKey) + "/" + getKeyName(positiveKey));
    }

    public void setUseCustomMapping(boolean useCustom) {
    this.useCustomMapping = useCustom;
    System.out.println("Usando mapeamento " + (useCustom ? "customizado" : "padrão"));
    }

    public void setJoystickOnlyMode(boolean joystickOnly) {
    // No modo somente joystick, não usamos o Robot
        if (joystickOnly) {
            pausePolling();
        } else {
            resumePolling();
        }
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
        
         if (playerId == 0) {
            // Player 1 - setas direcionais
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
        } else {
            // Player 2 - teclado numérico
            if (upPressed != keyStates[KeyEvent.VK_NUMPAD8]) {
                dispatchKeyEvent(KeyEvent.VK_NUMPAD8, upPressed);
            }
            if (downPressed != keyStates[KeyEvent.VK_NUMPAD2]) {
                dispatchKeyEvent(KeyEvent.VK_NUMPAD2, downPressed);
            }
            if (leftPressed != keyStates[KeyEvent.VK_NUMPAD4]) {
                dispatchKeyEvent(KeyEvent.VK_NUMPAD4, leftPressed);
            }
            if (rightPressed != keyStates[KeyEvent.VK_NUMPAD6]) {
                dispatchKeyEvent(KeyEvent.VK_NUMPAD6, rightPressed);
            }
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
            System.err.println("Erro ao enviar tecla " + keyCode + ": " + e.getMessage());
        }
    }
    
    public void pausePolling() {
        pollingActive.set(false);
        releaseAllKeys();
        System.out.println("Polling do joystick pausado");
    }
    
    public void resumePolling() {
        if (joystickEnabled.get() && joystick != null) {
            pollingActive.set(true);
            if (pollingThread == null || !pollingThread.isAlive()) {
                startPollingThread();
            }
            System.out.println("Polling do joystick retomado");
        }
    }
    
    public void setButtonMapping(int buttonIndex, int keyCode) {
        buttonToKeyMapping.put(buttonIndex, keyCode);
        System.out.println("Remapeado botão " + buttonIndex + " para " + getKeyName(keyCode));
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
         return joystick != null ? joystickName + " (Player " + (playerId + 1) + ")" : "Nenhum joystick";
    }

    public int getPlayerId() {
        return playerId;
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

    // Método estático para verificar status de ambos os jogadores
    public static String getGlobalStatus() {
        StringBuilder status = new StringBuilder();
        status.append(" Status dos Joysticks\n\n");
        
        if (instancePlayer1 != null && instancePlayer1.isJoystickEnabled()) {
            status.append("Player 1:  ").append(instancePlayer1.getJoystickName()).append("\n");
        } else {
            status.append("Player 1:  Não conectado\n");
        }
        
        if (instancePlayer2 != null && instancePlayer2.isJoystickEnabled()) {
            status.append("Player 2:  ").append(instancePlayer2.getJoystickName()).append("\n");
        } else {
            status.append("Player 2:  Não conectado\n");
        }
        
        return status.toString();
    }

    public void cleanup() {
        pausePolling();
        releaseAllKeys();
        joystickEnabled.set(false);
        joystick = null;
        System.out.println(" JoystickManager limpo");
    }

    
    // Método estático para limpeza global de ambos os jogadores
    public static void globalCleanup() {
        if (instancePlayer1 != null) {
            instancePlayer1.cleanup();
            instancePlayer1 = null;
        }
        if (instancePlayer2 != null) {
            instancePlayer2.cleanup();
            instancePlayer2 = null;
        }
    }

    
}