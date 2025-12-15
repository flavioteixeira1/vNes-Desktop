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
import java.util.Arrays;

public class JoystickManager {
    // Singleton instance
    private static JoystickManager instancePlayer1;
    private static JoystickManager instancePlayer2;
    private static JoystickManager instancePlayer3;
    private static JoystickManager instancePlayer4;
    private Controller joystick;
    private Component[] components;
    private boolean[] lastButtonStates;
    private float[] lastAxisStates;
    private AtomicBoolean joystickEnabled = new AtomicBoolean(false);
    private AtomicBoolean pollingActive = new AtomicBoolean(false);
    private Robot robot;

    // Mapeamento para eixos
    private Map<Component.Identifier, Integer[]> axisToKeyMapping;
    
    // Mapeamento de botões para teclas
    private Map<Integer, Integer> buttonToKeyMapping;

    private Map<Integer, Integer> buttonMap; // idx botão -> KeyEvent.VK_?
    
    // Estados atuais das teclas virtuais
    private boolean[] keyStates;
    
    // Threshold para eixos analógicos
    private final float AXIS_THRESHOLD = 0.5f;
    
    // Thread de polling
    private Thread pollingThread;

    // Identificador do jogador (0 = Player 1, 1 = Player 2, 2 = Player 3, 4 = Player 4)
    private int playerId;

    // Nome do joystick para debug
    private String joystickName;

    private Map<Integer, Integer> customButtonMapping;
    private Map<Component.Identifier, Integer[]> customAxisMapping;
    private boolean useCustomMapping = false;

    // Estados atuais para UI
    private Map<Integer, Boolean> currentButtonStates = new HashMap<>();
    private Map<Component.Identifier, Float> currentAxisStates = new HashMap<>();
    private float currentPOVState = Component.POV.OFF;
    
    // Contador de instâncias para debug
    private static int instanceCount = 0;

    private boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
    
    // Construtor 
    JoystickManager(int playerId) {
        this.playerId = playerId;
        System.out.println(" JoystickManager instância criada para Player " + (playerId + 1));
        
        try {
            this.robot = new Robot();
            this.keyStates = new boolean[256];
            
            // Inicializar os mapeamentos ANTES de initJoystick
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
            e.printStackTrace(); // Adicionar stack trace para debug
        }
    }

    private void setupCustomMapping() {
        customButtonMapping = new HashMap<>();
        customAxisMapping = new HashMap<>();
        // Inicializar com os mesmos valores do mapeamento padrão
        if (buttonToKeyMapping != null) {
            customButtonMapping.putAll(buttonToKeyMapping);
        }
        if (axisToKeyMapping != null) {
            customAxisMapping.putAll(axisToKeyMapping);
        }
    }
    
    // Métodos estáticos para obter instâncias específicas
    public static synchronized JoystickManager  getInstanceForPlayer(int playerId) {
        if (playerId == 0) {
            if (instancePlayer1 == null) {
                instancePlayer1 = new JoystickManager(0);
            }
            return instancePlayer1;
        } else if(playerId == 1 ){
            if (instancePlayer2 == null) {
                instancePlayer2 = new JoystickManager(1);
            }
            return instancePlayer2;
        }
        else if(playerId == 2 ){
            if (instancePlayer3 == null) {
                instancePlayer3 = new JoystickManager(2);
            }
            return instancePlayer3;
        }
        else{
             if (instancePlayer4 == null) {
                instancePlayer4 = new JoystickManager(3);
            }
            return instancePlayer4;
        }


        
    }
    
     // Método para verificar se uma instância foi inicializada
    public static boolean isInitializedForPlayer(int playerId) {
       if (playerId == 0) {
            return instancePlayer1 != null;
        } else if(playerId == 1) {
            return instancePlayer2 != null;
        }
        else if(playerId == 2) {
            return instancePlayer3 != null;
        }
        else {
            return instancePlayer4 != null;
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
                
                // Inicializar estados UI
                for (int i = 0; i < components.length; i++) {
                    if (!components[i].isAnalog()) {
                        currentButtonStates.put(i, false);
                    }
                }
                
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
            e.printStackTrace(); // Adicionar stack trace para debug
        }
    }

    private int detectButtonOffset() {
        try {
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
        } catch (Exception e) {
            System.err.println("Erro ao detectar offset dos botões: " + e.getMessage());
            return 0; // Fallback seguro
        }
    }

    private void adjustButtonMapping(int buttonOffset) {
        try {
            // Criar novo mapeamento ajustado
            Map<Integer, Integer> adjustedMapping = new HashMap<>();
            
            // Copiar mapeamento original com ajuste de offset
            for (Map.Entry<Integer, Integer> entry : buttonToKeyMapping.entrySet()) {
                int virtualButton = entry.getKey();
                int physicalIndex = buttonOffset + virtualButton;
                if (physicalIndex < components.length) {
                    adjustedMapping.put(physicalIndex, entry.getValue());
                } else {
                    // Se não houver componente físico, manter mapeamento original
                    adjustedMapping.put(virtualButton, entry.getValue());
                }
            }
            
            // Adicionar também os mapeamentos originais (para índices mais baixos)
            for (Map.Entry<Integer, Integer> entry : buttonToKeyMapping.entrySet()) {
                if (!adjustedMapping.containsKey(entry.getKey())) {
                    adjustedMapping.put(entry.getKey(), entry.getValue());
                }
            }
            
            // Substituir o mapeamento original
            buttonToKeyMapping = adjustedMapping;
            System.out.println("Mapeamento ajustado com offset " + buttonOffset + ":");
            for (Map.Entry<Integer, Integer> entry : buttonToKeyMapping.entrySet()) {
                System.out.println("  Botão " + entry.getKey() + " -> " + getKeyName(entry.getValue()));
            }
        } catch (Exception e) {
            System.err.println("Erro ao ajustar mapeamento: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void setupDefaultMapping() {
        buttonToKeyMapping = new HashMap<>();
        axisToKeyMapping = new HashMap<>();

        // Para Linux, botões começam em 0
        int buttonStartIndex = isWindows ? 6 : 0;
        
        if (playerId == 0) {
            // Player 1 - Botões (usando índices base 0 para Linux)
            buttonToKeyMapping.put(buttonStartIndex + 0, KeyEvent.VK_Z);      // A button
            buttonToKeyMapping.put(buttonStartIndex + 1, KeyEvent.VK_X);      // B button  
            buttonToKeyMapping.put(buttonStartIndex + 2, KeyEvent.VK_ENTER);  // Start
            buttonToKeyMapping.put(buttonStartIndex + 3, KeyEvent.VK_CONTROL); // Select
            
            // Player 1 - Eixos
            axisToKeyMapping.put(Component.Identifier.Axis.X, new Integer[]{KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT});
            axisToKeyMapping.put(Component.Identifier.Axis.Y, new Integer[]{KeyEvent.VK_UP, KeyEvent.VK_DOWN});
        } else {
            // Player 2 - Botões (usando índices base 0 para Linux)
            buttonToKeyMapping.put(buttonStartIndex + 0, KeyEvent.VK_NUMPAD7); // A button
            buttonToKeyMapping.put(buttonStartIndex + 1, KeyEvent.VK_NUMPAD9); // B button  
            buttonToKeyMapping.put(buttonStartIndex + 2, KeyEvent.VK_NUMPAD1); // Start
            buttonToKeyMapping.put(buttonStartIndex + 3, KeyEvent.VK_NUMPAD3); // Select
            
            // Player 2 - Eixos
            axisToKeyMapping.put(Component.Identifier.Axis.X, new Integer[]{KeyEvent.VK_NUMPAD4, KeyEvent.VK_NUMPAD6});
            axisToKeyMapping.put(Component.Identifier.Axis.Y, new Integer[]{KeyEvent.VK_NUMPAD8, KeyEvent.VK_NUMPAD2});
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

        
    
    public void stopPolling() {
        if (pollingThread != null) {
            pollingThread.interrupt();
        }
     }

    private void startPollingThread() {
        if (pollingThread != null && pollingThread.isAlive()) {
            System.out.println(" Thread de polling já está ativa");
            return;
        }
        
        pollingActive.set(true);
        pollingThread = new Thread(() -> {
            System.out.println(" Iniciando polling do joystick...");
            while (pollingActive.get() && !Thread.interrupted()) {
                if (joystickEnabled.get() && joystick != null) {
                    poll();
                }
                try {
                    Thread.sleep(16); // ~60Hz
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            System.out.println(" Polling do joystick finalizado.");
        });
        pollingThread.setDaemon(true);
        pollingThread.setName("Joystick-Polling-" + playerId);
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
                
                // Atualizar estados para UI
                if (comp.isAnalog()) {
                    if (comp.getIdentifier() == Component.Identifier.Axis.POV) {
                        currentPOVState = currentValue;
                    } else {
                        currentAxisStates.put(comp.getIdentifier(), currentValue);
                    }
                    processAnalogComponent(comp, currentValue, i);
                } else {
                    currentButtonStates.put(i, currentValue > 0.5f);
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
            if (activeMapping.containsKey(index)) {
                int keyCode = activeMapping.get(index);
                if (keyCode != -1) {  // Só dispara se não for -1 (limpo)
                dispatchKeyEvent(keyCode, currentState);
            } else {
                // Se for -1, apenas atualiza o estado sem disparar tecla
                System.out.println("Botão " + index + " ignorado (mapeamento limpo)");
            }
                dispatchKeyEvent(keyCode, currentState);
            }
        }
    }
    
    private void processAnalogComponent(Component comp, float currentValue, int index) {
        Component.Identifier identifier = comp.getIdentifier();
        Map<Component.Identifier, Integer[]> activeAxisMapping = useCustomMapping ? customAxisMapping : axisToKeyMapping;
        if (activeAxisMapping.containsKey(identifier)) {
            Integer[] keys = activeAxisMapping.get(identifier);
            processAxis(currentValue, keys[0], keys[1]);
        } else if (identifier == Component.Identifier.Axis.POV) {
            processPOVAxis(currentValue);
        }
            
            lastAxisStates[index] = currentValue;
    }

    public void setCustomButtonMapping(int buttonIndex, int keyCode) {
        customButtonMapping.put(buttonIndex, keyCode);
        if (keyCode == -1) {
            customButtonMapping.put(buttonIndex, -1);
            System.out.println("Mapeamento do botão " + buttonIndex + " foi limpo");
             // Se estiver usando mapeamento customizado, também limpe do padrão
        if (useCustomMapping && buttonToKeyMapping.containsKey(buttonIndex)) {
            buttonToKeyMapping.remove(buttonIndex);
        }
        } else {
            customButtonMapping.put(buttonIndex, keyCode);
            System.out.println("Botão customizado " + buttonIndex + " mapeado para " + getKeyName(keyCode));
        }
    }

    public void setCustomAxisMapping(Component.Identifier axis, int negativeKey, int positiveKey) {
        customAxisMapping.put(axis, new Integer[]{negativeKey, positiveKey});
        
        if (negativeKey == -1 && positiveKey == -1) {
            System.out.println("Eixo " + axis + " teve seu mapeamento limpo");
        } else {
            String negKeyName = negativeKey > 0 ? getKeyName(negativeKey) : "Nenhuma";
            String posKeyName = positiveKey > 0 ? getKeyName(positiveKey) : "Nenhuma";
            System.out.println("Eixo " + axis + " mapeado para " + 
                        negKeyName + "/" + posKeyName);
        }
    }

    //Setter
    public void setUseCustomMapping(boolean useCustom) {
        this.useCustomMapping = useCustom;
        System.out.println("Usando mapeamento " + (useCustom ? "customizado" : "padrão"));
    }

    // Método GETTER para usarCustomMapping
    public boolean getUseCustomMapping() {
        return useCustomMapping;
    }

    // Métodos para obter estados para UI
    public boolean getButtonState(int buttonIndex) {
        return currentButtonStates.getOrDefault(buttonIndex, false);
    }
    
    public float getAxisState(Component.Identifier axis) {
        return currentAxisStates.getOrDefault(axis, 0.0f);
    }
    
    public float getPOVState() {
        return currentPOVState;
    }
    
    public int getMappedKeyForButton(int buttonIndex) {
        try {
            Map<Integer, Integer> activeMapping = useCustomMapping ? customButtonMapping : buttonToKeyMapping;
            if (activeMapping != null) {
                return activeMapping.getOrDefault(buttonIndex, -1);
            }
        } catch (Exception e) {
            System.err.println("Erro em getMappedKeyForButton(" + buttonIndex + "): " + e.getMessage());
        }
        return -1;
    }
    
    public Integer[] getMappedKeysForAxis(Component.Identifier axis) {
        try {
            Map<Component.Identifier, Integer[]> activeMapping = useCustomMapping ? customAxisMapping : axisToKeyMapping;
            if (activeMapping != null) {
                return activeMapping.getOrDefault(axis, new Integer[]{-1, -1});
            }
        } catch (Exception e) {
            System.err.println("Erro em getMappedKeysForAxis(" + axis + "): " + e.getMessage());
        }
        return new Integer[]{-1, -1};
    }
    
    public List<Component.Identifier> getAvailableAxes() {
    List<Component.Identifier> axes = new ArrayList<>();
        if (joystick != null && joystick.getComponents() != null) {
            for (Component comp : joystick.getComponents()) {
                if (comp.isAnalog()) {
                    axes.add(comp.getIdentifier());
                }
            }
        }
        return axes;
    }
    
    public int getButtonCount() {
        if (components == null) return 0;
        int count = 0;
        for (Component comp : components) {
            if (!comp.isAnalog() && comp.getIdentifier() instanceof Component.Identifier.Button) {
                count++;
            }
        }
        return Math.min(count, 12); // Máximo 12 botões para UI
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
        if (keyCode == -1 || keyCode < 0) { return; }
        
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

    public String getMappingStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("Status do mapeamento - Player ").append(playerId + 1).append("\n");
        sb.append("Usando custom: ").append(useCustomMapping).append("\n");
        
        sb.append("\nMapeamento padrão:\n");
        for (Map.Entry<Integer, Integer> entry : buttonToKeyMapping.entrySet()) {
            sb.append("  Botão ").append(entry.getKey()).append(" -> ")
            .append(getKeyName(entry.getValue())).append("\n");
        }
        
        sb.append("\nMapeamento customizado:\n");
        for (Map.Entry<Integer, Integer> entry : customButtonMapping.entrySet()) {
            sb.append("  Botão ").append(entry.getKey()).append(" -> ")
            .append(getKeyName(entry.getValue())).append("\n");
        }
        
        return sb.toString();
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

        if (instancePlayer3 != null && instancePlayer3.isJoystickEnabled()) {
            status.append("Player 3:  ").append(instancePlayer3.getJoystickName()).append("\n");
        } else {
            status.append("Player 3:  Não conectado\n");
        }

        if (instancePlayer4 != null && instancePlayer4.isJoystickEnabled()) {
            status.append("Player 4:  ").append(instancePlayer4.getJoystickName()).append("\n");
        } else {
            status.append("Player 4:  Não conectado\n");
        }

        return status.toString();
        
    }

     public int getKeyForButton(int buttonIdx) {
         return getMappedKeyForButton(buttonIdx);
    }


    public void clearButtonMapping(int buttonIndex) {
        // Limpa tanto no custom quanto no padrão
        customButtonMapping.put(buttonIndex, -1);
        
        // IMPORTANTE: Quando limpa um botão, força o uso do mapeamento customizado
        // para que a limpeza tenha efeito
        if (!useCustomMapping) {
            useCustomMapping = true;
            System.out.println("Auto-ativando mapeamento customizado após limpeza");
        }
        
        // Também remove do mapeamento padrão para garantir
        if (buttonToKeyMapping.containsKey(buttonIndex)) {
            buttonToKeyMapping.remove(buttonIndex);
        }
        
        System.out.println("Botão " + buttonIndex + " completamente limpo");
    }


    public void cleanup() {
        pausePolling();
        releaseAllKeys();
        joystickEnabled.set(false);
        joystick = null;
        System.out.println(" JoystickManager limpo");
    }

    public void clearAllCustomMappings() {
        customButtonMapping.clear();
        customAxisMapping.clear();
        System.out.println("Todos os mapeamentos customizados foram limpos");
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
        if (instancePlayer3 != null) {
            instancePlayer3.cleanup();
            instancePlayer3 = null;
        }
        if (instancePlayer4 != null) {
            instancePlayer4.cleanup();
            instancePlayer4 = null;
        }
    }

    
}

