package com.flavioteixeira1.vnes.core;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import net.java.games.input.Component;

public class JoystickMainWindow extends JFrame {
    private JComboBox<String> perfilCombo;
    private JButton addPerfil, removePerfil, renomearPerfil;
    private JButton importBtn, exportBtn, saveBtn, revertBtn, helpBtn;
    private JTabbedPane joystickTabs;
    private List<DevicePanel> devicePanels = new ArrayList<>();
    private JoystickManager joystickManager1, joystickManager2, joystickManager3, joystickManager4;
    private javax.swing.Timer uiUpdateTimer;
    HelpDialog helpDialog;

    public JoystickMainWindow() {
        super("vNes Desktop - Joystick Config ");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- Top Bar ---
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        perfilCombo = new JComboBox<>(new String[]{"VNES"});
        addPerfil = new JButton("+");
        removePerfil = new JButton("-");
        renomearPerfil = new JButton("Renomear");
        
        topBar.add(new JLabel("Perfil:"));
        topBar.add(perfilCombo);
        topBar.add(addPerfil); 
        topBar.add(removePerfil); 
        topBar.add(renomearPerfil);

        importBtn = new JButton("Importar");
        exportBtn = new JButton("Exportar");
        saveBtn = new JButton("Salvar");
        revertBtn = new JButton("Reverter");
	    helpBtn = new JButton("Sobre");
        
        topBar.add(Box.createHorizontalStrut(20));
        topBar.add(importBtn); 
        topBar.add(exportBtn); 
        topBar.add(saveBtn); 
        topBar.add(revertBtn);
	    topBar.add(helpBtn);
	
        add(topBar, BorderLayout.NORTH);

        // --- Joystick Tabs ---
        joystickTabs = new JTabbedPane();
        
        try {
            // Inicializar joysticks
            joystickManager1 = JoystickManager.getInstanceForPlayer(0);
            joystickManager2 = JoystickManager.getInstanceForPlayer(1);
            joystickManager3 = JoystickManager.getInstanceForPlayer(2);
            joystickManager4 = JoystickManager.getInstanceForPlayer(3);
            
            // Painel para Player 1
            if (joystickManager1.isJoystickEnabled()) {
                DevicePanel panel1 = new DevicePanel(joystickManager1, 0);
                devicePanels.add(panel1);
                joystickTabs.addTab("Player 1", createPlayerPanel(panel1, 0));
            } else {
                JPanel noDevicePanel = new JPanel(new BorderLayout());
                noDevicePanel.add(new JLabel("Nenhum joystick detectado para Player 1", JLabel.CENTER), BorderLayout.CENTER);
                joystickTabs.addTab("Player 1", noDevicePanel);
            }
            
            // Painel para Player 2  
            if (joystickManager2.isJoystickEnabled()) {
                DevicePanel panel2 = new DevicePanel(joystickManager2, 1);
                devicePanels.add(panel2);
                joystickTabs.addTab("Player 2", createPlayerPanel(panel2, 1));
            } else {
                JPanel noDevicePanel = new JPanel(new BorderLayout());
                noDevicePanel.add(new JLabel("Nenhum joystick detectado para Player 2", JLabel.CENTER), BorderLayout.CENTER);
                joystickTabs.addTab("Player 2", noDevicePanel);
            }
            // Painel para Player 3  
            if (joystickManager3.isJoystickEnabled()) {
                DevicePanel panel3 = new DevicePanel(joystickManager3, 2);
                devicePanels.add(panel3);
                joystickTabs.addTab("Player 3", createPlayerPanel(panel3, 2));
            } else {
                JPanel noDevicePanel = new JPanel(new BorderLayout());
                noDevicePanel.add(new JLabel("Nenhum joystick detectado para Player 3", JLabel.CENTER), BorderLayout.CENTER);
                joystickTabs.addTab("Player 3", noDevicePanel);
            }
            // Painel para Player 4  
            if (joystickManager4.isJoystickEnabled()) {
                DevicePanel panel4 = new DevicePanel(joystickManager4, 3);
                devicePanels.add(panel4);
                joystickTabs.addTab("Player 4", createPlayerPanel(panel4, 3));
            } else {
                JPanel noDevicePanel = new JPanel(new BorderLayout());
                noDevicePanel.add(new JLabel("Nenhum joystick detectado para Player 3", JLabel.CENTER), BorderLayout.CENTER);
                joystickTabs.addTab("Player 4", noDevicePanel);
            }
        } catch (Exception e) {
            System.err.println("Erro ao inicializar painéis de joystick: " + e.getMessage());
            e.printStackTrace();
            
            JPanel errorPanel = new JPanel(new BorderLayout());
            errorPanel.add(new JLabel("Erro ao inicializar joysticks. Verifique se os dispositivos estão conectados.", 
                                    JLabel.CENTER), BorderLayout.CENTER);
            joystickTabs.addTab("Erro", errorPanel);
        }
        
        // Painel de status
        JPanel statusPanel = new JPanel(new BorderLayout());
        JTextArea statusArea = new JTextArea(5, 50);
        statusArea.setEditable(false);
        statusArea.setText(JoystickManager.getGlobalStatus());
        statusPanel.add(new JScrollPane(statusArea), BorderLayout.CENTER);
        
        JButton refreshBtn = new JButton("Atualizar Status");
        refreshBtn.addActionListener(e -> {
            statusArea.setText(JoystickManager.getGlobalStatus());
        });
        statusPanel.add(refreshBtn, BorderLayout.SOUTH);
        
        joystickTabs.addTab("Status", statusPanel);
        
        add(joystickTabs, BorderLayout.CENTER);

        // --- Bottom Bar ---
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton clearBtn = new JButton("Limpar");
        JButton quickSetBtn = new JButton("Configuração Rápida");
        JButton toggleMappingBtn = new JButton("Usar Mapeamento Customizado");
        JButton closeBtn = new JButton("Fechar Diálogo");
        JButton quitBtn = new JButton("Sair");
        
        toggleMappingBtn.addActionListener(e -> {
            boolean useCustom = !joystickManager1.getUseCustomMapping();
            joystickManager1.setUseCustomMapping(useCustom);
            joystickManager2.setUseCustomMapping(useCustom);
            joystickManager3.setUseCustomMapping(false);
            joystickManager4.setUseCustomMapping(false);
            toggleMappingBtn.setText(useCustom ? "Usar Mapeamento Padrão" : "Usar Mapeamento Customizado");
            
            // Atualizar labels dos botões
            for (DevicePanel panel : devicePanels) {
                panel.updateButtonLabels();
            }
        });
        
        bottomBar.add(clearBtn); 
        bottomBar.add(quickSetBtn);
        bottomBar.add(toggleMappingBtn);
        bottomBar.add(closeBtn); 
        bottomBar.add(quitBtn);
        add(bottomBar, BorderLayout.SOUTH);

        // Configurar ações
        clearBtn.addActionListener(e -> clearAllMappings());
        helpBtn.addActionListener((e -> showHelpDialog()));
        quickSetBtn.addActionListener(e -> showQuickSetupDialog());
        closeBtn.addActionListener(e -> this.setVisible(false));
        quitBtn.addActionListener(e -> {
            JoystickManager.globalCleanup();
            System.exit(0);
        });

        // Iniciar timer para atualizar UI apenas se houver painéis
        if (!devicePanels.isEmpty()) {
            startUIUpdateTimer();
        }
    }
    
    private JPanel createPlayerPanel(DevicePanel devicePanel, int playerId) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(devicePanel, BorderLayout.CENTER);
        
        // Adicionar informações do joystick
        String joystickName = null;
        switch(playerId) {
            case 0:
            joystickName = joystickManager1.getJoystickName();
                break;
            case 1:
            joystickName = joystickManager2.getJoystickName();
                break;
            case 2:  
            joystickName = joystickManager3.getJoystickName();
                 break;
            case 3:
            joystickName = joystickManager4.getJoystickName();  
                break;
            default:
            joystickName = joystickManager1.getJoystickName();
                break;    
        }
        JLabel infoLabel = new JLabel("Joystick: " + joystickName);
        infoLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panel.add(infoLabel, BorderLayout.NORTH);
        
        return panel;
    }
    
     private void startUIUpdateTimer() {
        uiUpdateTimer = new javax.swing.Timer(50, e -> { // 20 FPS
            for (DevicePanel panel : devicePanels) {
                try {
                    panel.updateUIState();
                } catch (Exception ex) {
                    System.err.println("Erro ao atualizar UI: " + ex.getMessage());
                }
            }
        });
        uiUpdateTimer.start();
    }
    
    private void clearAllMappings() {
        int option = JOptionPane.showConfirmDialog(this,
            "Deseja limpar todos os mapeamentos?",
            "Confirmar limpeza",
            JOptionPane.YES_NO_OPTION);
            
        if (option == JOptionPane.YES_OPTION) {
            joystickManager1.setUseCustomMapping(false);
            joystickManager2.setUseCustomMapping(false);
            joystickManager3.setUseCustomMapping(false);
            joystickManager4.setUseCustomMapping(false);
            // Recarregar mapeamento padrão
            JoystickManager.globalCleanup();
            joystickManager1 = JoystickManager.getInstanceForPlayer(0);
            joystickManager2 = JoystickManager.getInstanceForPlayer(1);
            joystickManager3 = JoystickManager.getInstanceForPlayer(2);
            joystickManager4 = JoystickManager.getInstanceForPlayer(3);
            
            // Recriar painéis
            devicePanels.clear();
            joystickTabs.removeAll();
            
            try {
                if (joystickManager1.isJoystickEnabled()) {
                    DevicePanel panel1 = new DevicePanel(joystickManager1, 0);
                    devicePanels.add(panel1);
                    joystickTabs.addTab("Player 1", createPlayerPanel(panel1, 0));
                }
                
                if (joystickManager2.isJoystickEnabled()) {
                    DevicePanel panel2 = new DevicePanel(joystickManager2, 1);
                    devicePanels.add(panel2);
                    joystickTabs.addTab("Player 2", createPlayerPanel(panel2, 1));
                }

                if (joystickManager3.isJoystickEnabled()) {
                    DevicePanel panel3 = new DevicePanel(joystickManager3, 2);
                    devicePanels.add(panel3);
                    joystickTabs.addTab("Player 3", createPlayerPanel(panel3, 1));
                }
                if (joystickManager4.isJoystickEnabled()) {
                    DevicePanel panel4 = new DevicePanel(joystickManager4, 3);
                    devicePanels.add(panel4);
                    joystickTabs.addTab("Player 4", createPlayerPanel(panel4, 1));
                }
                
                // Reiniciar timer se houver painéis
                if (!devicePanels.isEmpty()) {
                    startUIUpdateTimer();
                }
            } catch (Exception e) {
                System.err.println("Erro ao recriar painéis: " + e.getMessage());
            }
        }
    }

    private void showHelpDialog(){
       HelpDialog helpDialog = new HelpDialog(this);
       helpDialog.setVisible(true);
    }
    
    private void showQuickSetupDialog() {
        JDialog dialog = new JDialog(this, "Configuração Rápida", true);
        dialog.setLayout(new BorderLayout());
        
        JTextArea instructions = new JTextArea(
            "Configuração Rápida:\n\n" +
            "1. Pressione qualquer botão ou eixo no Joystick e clique \n" + 
            " no correspondente na tela principal - \n" +
            "2. Pressione a tecla desejada no teclado\n" +
            "3. Para eixos: configure direções separadamente\n" +
            "4. Use 'Usar Mapeamento Customizado' para ativar - IMPORTANTE \n\n" +
            "Dica: Pressione ESC para cancelar durante a captura.\n" +
            "Pressione Ctrl + L para apagar um mapeamento"
        );
        instructions.setEditable(false);
        instructions.setMargin(new Insets(10, 10, 10, 10));
        
        dialog.add(new JScrollPane(instructions), BorderLayout.CENTER);
        
        JButton closeBtn = new JButton("Fechar");
        closeBtn.addActionListener(e -> dialog.dispose());
        dialog.add(closeBtn, BorderLayout.SOUTH);
        
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // Painel para cada dispositivo de entrada
    public class DevicePanel extends JPanel {
        private JButton[] axisBtns = new JButton[4];    // Eixos (X, Y, Z, RZ)
        private JButton[] buttonBtns = new JButton[12]; // Botões comuns
        private JButton[] povBtns = new JButton[4];     // Direcionais (POV)
        private JoystickManager joystickManager;
        private int playerId;
        private Map<Integer, Boolean> lastButtonStates = new HashMap<>();
        private Map<String, Float> lastAxisStates = new HashMap<>();
        private float lastPOVState = Component.POV.OFF;
        
        public DevicePanel(JoystickManager manager, int playerId) {
            this.joystickManager = manager;
            this.playerId = playerId;
            
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            
            // Título
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 3;
            JLabel title = new JLabel("Controles - Player " + (playerId + 1), JLabel.CENTER);
            title.setFont(new Font("Arial", Font.BOLD, 14));
            add(title, gbc);
            
            // Status
            gbc.gridy++;
            JLabel statusLabel = new JLabel("Status: " + 
                (manager.isJoystickEnabled() ? "Conectado" : "Desconectado"), 
                JLabel.CENTER);
            statusLabel.setForeground(manager.isJoystickEnabled() ? Color.GREEN.darker() : Color.RED);
            add(statusLabel, gbc);
            
            // Separador
            gbc.gridy++;
            add(new JSeparator(), gbc);
            
            // Eixos Analógicos
            gbc.gridy++;
            gbc.gridwidth = 1;
            JLabel axisLabel = new JLabel("Eixos Analógicos:", JLabel.LEFT);
            axisLabel.setFont(new Font("Arial", Font.BOLD, 12));
            add(axisLabel, gbc);
            
            gbc.gridy++;
            String[] axisNames = {"Eixo X", "Eixo Y", "Eixo Z", "Eixo RZ"};
            for (int i = 0; i < axisBtns.length; i++) {
                axisBtns[i] = new JButton(axisNames[i] + ": [Não configurado]");
                axisBtns[i].setPreferredSize(new Dimension(200, 35));
                axisBtns[i].setToolTipText("Clique para configurar este eixo");
                final int axisIdx = i;
                axisBtns[i].addActionListener(e -> configureAxis(axisIdx));
                
                gbc.gridx = i % 2;
                gbc.gridy = 4 + (i / 2);
                add(axisBtns[i], gbc);
            }
            
            // Direcionais (POV/Hat)
            gbc.gridx = 0;
            gbc.gridy = 8;
            gbc.gridwidth = 3;
            JLabel povLabel = new JLabel("Direcionais (POV/Hat):", JLabel.LEFT);
            povLabel.setFont(new Font("Arial", Font.BOLD, 12));
            add(povLabel, gbc);
            
            gbc.gridy++;
            gbc.gridwidth = 1;
            String[] povNames = {"Cima", "Baixo", "Esquerda", "Direita"};
            for (int i = 0; i < povBtns.length; i++) {
                povBtns[i] = new JButton(povNames[i] + ": [Não configurado]");
                povBtns[i].setPreferredSize(new Dimension(160, 35));
                povBtns[i].setToolTipText("Clique para configurar esta direção");
                final int povIdx = i;
                povBtns[i].addActionListener(e -> configurePOV(povIdx));
                
                gbc.gridx = i;
                gbc.gridy = 10;
                add(povBtns[i], gbc);
            }
            
            // Botões
            gbc.gridx = 0;
            gbc.gridy = 11;
            gbc.gridwidth = 3;
            JLabel buttonLabel = new JLabel("Botões:", JLabel.LEFT);
            buttonLabel.setFont(new Font("Arial", Font.BOLD, 12));
            add(buttonLabel, gbc);
            
            gbc.gridy++;
            gbc.gridwidth = 1;
            try {
                int buttonCount = Math.min(joystickManager.getButtonCount(), 12);
                for (int i = 0; i < buttonCount; i++) {
                    buttonBtns[i] = new JButton("Botão " + (i+1) + ": [Não configurado]");
                    buttonBtns[i].setPreferredSize(new Dimension(150, 35));
                    buttonBtns[i].setToolTipText("Clique para configurar este botão");
                    final int btnIdx = i;
                    buttonBtns[i].addActionListener(e -> configureButton(btnIdx));
                    
                    gbc.gridx = i % 3;
                    gbc.gridy = 13 + (i / 3);
                    add(buttonBtns[i], gbc);
                }
                
                // Inicializar estados
                for (int i = 0; i < buttonCount; i++) {
                    lastButtonStates.put(i, false);
                }
            } catch (Exception e) {
                System.err.println("Erro ao criar botões: " + e.getMessage());
                JLabel errorLabel = new JLabel("Erro ao inicializar botões", JLabel.CENTER);
                gbc.gridx = 0;
                gbc.gridy = 13;
                gbc.gridwidth = 3;
                add(errorLabel, gbc);
            }
            
            updateButtonLabels();
        }
        
        private void configureButton(int buttonIndex) {
                try {
                    String label = "Botão " + (buttonIndex + 1) + " - Player " + (playerId + 1);
                    KeyCaptureDialog2.CaptureResult result = KeyCaptureDialog2.captureWithClear(
                        ConfigDialog2.getCurrentInstance(), label);
                    
                    if (result.isClear()) {
                        // Limpar o mapeamento deste botão
                        joystickManager.setCustomButtonMapping(buttonIndex, -1); // -1 indica sem mapeamento
                        updateButtonLabels();
                        
                        JOptionPane.showMessageDialog(this,
                            "Mapeamento do Botão " + (buttonIndex + 1) + " foi limpo.",
                            "Mapeamento Limpo",
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                    else if (result.keyCode > 0) {
                        joystickManager.setCustomButtonMapping(buttonIndex, result.keyCode);
                        updateButtonLabels();
                        joystickManager.setUseCustomMapping(true);
                        
                        JOptionPane.showMessageDialog(this,
                            "Botão " + (buttonIndex + 1) + " mapeado para: " + 
                            KeyEvent.getKeyText(result.keyCode),
                            "Mapeamento Configurado",
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao configurar botão: " + e.getMessage());
                    JOptionPane.showMessageDialog(this,
                        "Erro ao configurar botão: " + e.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
                }
        }
        
        private void configureAxis(int axisIndex) {
                try {
                    String[] directions = {"Negativo", "Positivo"};
                    String[] axisNames = {"Eixo X", "Eixo Y", "Eixo Z", "Eixo RZ"};
                    
                    // Obter identificador do eixo
                    List<Component.Identifier> availableAxes = joystickManager.getAvailableAxes();
                    if (axisIndex < availableAxes.size()) {
                        Component.Identifier axisId = availableAxes.get(axisIndex);
                        
                        // Criar um diálogo de seleção para escolher qual direção limpar/configurar
                        Object[] options = {"Negativo", "Positivo", "Ambas as direções"};
                        int choice = JOptionPane.showOptionDialog(this,
                            "Qual direção do eixo deseja configurar/limpar?",
                            "Configurar Eixo",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[0]);
                        
                        if (choice == -1) return; // Usuário cancelou
                        
                        if (choice == 0 || choice == 1) { // Negativo ou Positivo
                            String label = axisNames[axisIndex] + " (" + directions[choice] + ") - Player " + (playerId + 1);
                            KeyCaptureDialog2.CaptureResult result = KeyCaptureDialog2.captureWithClear(
                                ConfigDialog2.getCurrentInstance(), label);
                            
                            if (result.isClear()) {
                                // Limpar mapeamento da direção específica
                                Integer[] currentMapping = joystickManager.getMappedKeysForAxis(axisId);
                                Integer[] newMapping = new Integer[]{currentMapping[0], currentMapping[1]};
                                
                                if (choice == 0) { // Negativo
                                    newMapping[0] = -1;
                                } else { // Positivo
                                    newMapping[1] = -1;
                                }
                                
                                joystickManager.setCustomAxisMapping(axisId, newMapping[0], newMapping[1]);
                                updateButtonLabels();
                                
                                JOptionPane.showMessageDialog(this,
                                    "Mapeamento " + directions[choice] + " do " + axisNames[axisIndex] + " foi limpo.",
                                    "Mapeamento Limpo",
                                    JOptionPane.INFORMATION_MESSAGE);
                            }
                            else if (result.keyCode > 0) {
                                Integer[] currentMapping = joystickManager.getMappedKeysForAxis(axisId);
                                Integer[] newMapping = new Integer[]{currentMapping[0], currentMapping[1]};
                                
                                if (choice == 0) { // Negativo
                                    newMapping[0] = result.keyCode;
                                } else { // Positivo
                                    newMapping[1] = result.keyCode;
                                }
                                
                                joystickManager.setCustomAxisMapping(axisId, newMapping[0], newMapping[1]);
                                updateButtonLabels();
                                joystickManager.setUseCustomMapping(true);
                                
                                JOptionPane.showMessageDialog(this,
                                    axisNames[axisIndex] + " " + directions[choice] + " mapeado para: " + 
                                    KeyEvent.getKeyText(result.keyCode),
                                    "Mapeamento Configurado",
                                    JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                        else if (choice == 2) { // Ambas as direções
                            int option = JOptionPane.showConfirmDialog(this,
                                "Deseja limpar o mapeamento de ambas as direções do " + axisNames[axisIndex] + "?",
                                "Confirmar Limpeza",
                                JOptionPane.YES_NO_OPTION);
                            
                            if (option == JOptionPane.YES_OPTION) {
                                joystickManager.setCustomAxisMapping(axisId, -1, -1);
                                updateButtonLabels();
                                
                                JOptionPane.showMessageDialog(this,
                                    "Mapeamento de ambas as direções do " + axisNames[axisIndex] + " foi limpo.",
                                    "Mapeamento Limpo",
                                    JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Eixo não disponível neste joystick",
                            "Erro",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao configurar eixo: " + e.getMessage());
                    JOptionPane.showMessageDialog(this,
                        "Erro ao configurar eixo: " + e.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
                }
        }

        
        private void configurePOV(int povIndex) {
            try {
                String[] directions = {"Cima", "Baixo", "Esquerda", "Direita"};
                String label = "POV " + directions[povIndex] + " - Player " + (playerId + 1);
                
                int keyCode = KeyCaptureDialog2.capture(
                    ConfigDialog2.getCurrentInstance(), label);
                
                if (keyCode > 0) {
                    // Configurar mapeamento POV (simplificado)
                    povBtns[povIndex].setText(directions[povIndex] + ": " + 
                        KeyEvent.getKeyText(keyCode));
                    
                    JOptionPane.showMessageDialog(this,
                        "POV " + directions[povIndex] + " mapeado para: " + 
                        KeyEvent.getKeyText(keyCode),
                        "Mapeamento Configurado",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception e) {
                System.err.println("Erro ao configurar POV: " + e.getMessage());
            }
        }
        
        public void updateUIState() {
            if (!joystickManager.isJoystickEnabled()) return;
            
            try {
                // Atualizar estados dos botões
                int buttonCount = Math.min(joystickManager.getButtonCount(), 12);
                for (int i = 0; i < buttonCount; i++) {
                    boolean currentState = joystickManager.getButtonState(i);
                    Boolean lastState = lastButtonStates.get(i);
                    
                    if (lastState == null || currentState != lastState) {
                        highlightButton(i, currentState);
                        lastButtonStates.put(i, currentState);
                    }
                }
                
                // Atualizar estados dos eixos
                List<Component.Identifier> availableAxes = joystickManager.getAvailableAxes();
                for (int i = 0; i < Math.min(availableAxes.size(), 4); i++) {
                    Component.Identifier axisId = availableAxes.get(i);
                    float currentValue = joystickManager.getAxisState(axisId);
                    String axisKey = axisId.toString();
                    Float lastValue = lastAxisStates.get(axisKey);
                    
                    if (lastValue == null || Math.abs(currentValue - lastValue) > 0.1f) {
                        highlightAxis(i, currentValue);
                        lastAxisStates.put(axisKey, currentValue);
                    }
                }
                
                // Atualizar POV
                float currentPOV = joystickManager.getPOVState();
                if (currentPOV != lastPOVState) {
                    highlightPOV(currentPOV);
                    lastPOVState = currentPOV;
                }
            } catch (Exception e) {
                System.err.println("Erro ao atualizar UI state: " + e.getMessage());
            }
        }
        
        public void updateButtonLabels() {
            try {
                // Atualizar labels dos botões baseados no mapeamento atual
                int buttonCount = Math.min(joystickManager.getButtonCount(), 12);
                for (int i = 0; i < buttonCount; i++) {
                    int keyCode = joystickManager.getMappedKeyForButton(i);
                    String keyName = (keyCode > 0) ? KeyEvent.getKeyText(keyCode) : "[Não configurado]";
                    buttonBtns[i].setText("Botão " + (i+1) + ": " + keyName);
                }
                
                // Atualizar labels dos eixos
                List<Component.Identifier> availableAxes = joystickManager.getAvailableAxes();
                String[] axisNames = {"Eixo X", "Eixo Y", "Eixo Z", "Eixo RZ"};
                for (int i = 0; i < Math.min(availableAxes.size(), 4); i++) {
                    Component.Identifier axisId = availableAxes.get(i);
                    Integer[] keys = joystickManager.getMappedKeysForAxis(axisId);
                    
                    String negKey = (keys[0] > 0) ? KeyEvent.getKeyText(keys[0]) : "Nenhuma";
                    String posKey = (keys[1] > 0) ? KeyEvent.getKeyText(keys[1]) : "Nenhuma";
                    
                    axisBtns[i].setText(axisNames[i] + ": " + negKey + " / " + posKey);
                    
                    // Mostrar se o eixo está disponível
                    axisBtns[i].setEnabled(i < availableAxes.size());
                }
                
                // Atualizar cor baseada no tipo de mapeamento
                Color mappingColor = joystickManager.getUseCustomMapping() ? 
                    new Color(220, 240, 255) : UIManager.getColor("Panel.background");
                setBackground(mappingColor);
                
                for (JButton btn : buttonBtns) {
                    if (btn != null) {
                        btn.setBackground(mappingColor);
                    }
                }
                for (JButton btn : axisBtns) {
                    if (btn != null) {
                        btn.setBackground(mappingColor);
                    }
                }
                for (JButton btn : povBtns) {
                    if (btn != null) {
                        btn.setBackground(mappingColor);
                    }
                }
            } catch (Exception e) {
                System.err.println("Erro ao atualizar labels dos botões: " + e.getMessage());
            }
        }
        
        // Métodos para feedback visual
        public void highlightButton(int buttonIdx, boolean pressed) {
            try {
                if (buttonIdx >= 0 && buttonIdx < buttonBtns.length && buttonBtns[buttonIdx] != null) {
                    if (pressed) {
                        buttonBtns[buttonIdx].setBackground(new Color(144, 238, 144)); // Verde claro
                        buttonBtns[buttonIdx].setOpaque(true);
                        buttonBtns[buttonIdx].setBorder(BorderFactory.createLineBorder(Color.GREEN.darker(), 2));
                    } else {
                        Color mappingColor = joystickManager.getUseCustomMapping() ? 
                            new Color(220, 240, 255) : UIManager.getColor("Button.background");
                        buttonBtns[buttonIdx].setBackground(mappingColor);
                        buttonBtns[buttonIdx].setBorder(UIManager.getBorder("Button.border"));
                    }
                    buttonBtns[buttonIdx].repaint();
                }
            } catch (Exception e) {
                System.err.println("Erro ao destacar botão: " + e.getMessage());
            }
        }
        
        public void highlightAxis(int axisIdx, float value) {
            try {
                if (axisIdx >= 0 && axisIdx < axisBtns.length && axisBtns[axisIdx] != null) {
                    if (Math.abs(value) > 0.3f) {
                        Color color = value > 0 ? new Color(173, 216, 230) : new Color(255, 218, 185); // Azul claro ou laranja claro
                        axisBtns[axisIdx].setBackground(color);
                        axisBtns[axisIdx].setOpaque(true);
                        
                        // Adicionar valor no tooltip
                        axisBtns[axisIdx].setToolTipText(String.format("Valor: %.2f", value));
                    } else {
                        Color mappingColor = joystickManager.getUseCustomMapping() ? 
                            new Color(220, 240, 255) : UIManager.getColor("Button.background");
                        axisBtns[axisIdx].setBackground(mappingColor);
                        axisBtns[axisIdx].setToolTipText("Clique para configurar este eixo");
                    }
                    axisBtns[axisIdx].repaint();
                }
            } catch (Exception e) {
                System.err.println("Erro ao destacar eixo: " + e.getMessage());
            }
        }
        
        public void highlightPOV(float povValue) {
            try {
                // Resetar todas as direções POV
                for (JButton btn : povBtns) {
                    if (btn != null) {
                        Color mappingColor = joystickManager.getUseCustomMapping() ? 
                            new Color(220, 240, 255) : UIManager.getColor("Button.background");
                        btn.setBackground(mappingColor);
                    }
                }
                
                // Destacar direções ativas
                if (povValue != Component.POV.OFF) {
                    if (povValue == Component.POV.UP || povValue == Component.POV.UP_LEFT || povValue == Component.POV.UP_RIGHT) {
                        if (povBtns[0] != null) {
                            povBtns[0].setBackground(new Color(200, 255, 200));
                            povBtns[0].setOpaque(true);
                        }
                    }
                    if (povValue == Component.POV.DOWN || povValue == Component.POV.DOWN_LEFT || povValue == Component.POV.DOWN_RIGHT) {
                        if (povBtns[1] != null) {
                            povBtns[1].setBackground(new Color(200, 255, 200));
                            povBtns[1].setOpaque(true);
                        }
                    }
                    if (povValue == Component.POV.LEFT || povValue == Component.POV.UP_LEFT || povValue == Component.POV.DOWN_LEFT) {
                        if (povBtns[2] != null) {
                            povBtns[2].setBackground(new Color(200, 255, 200));
                            povBtns[2].setOpaque(true);
                        }
                    }
                    if (povValue == Component.POV.RIGHT || povValue == Component.POV.UP_RIGHT || povValue == Component.POV.DOWN_RIGHT) {
                        if (povBtns[3] != null) {
                            povBtns[3].setBackground(new Color(200, 255, 200));
                            povBtns[3].setOpaque(true);
                        }
                    }
                }
                
                for (JButton btn : povBtns) {
                    if (btn != null) btn.repaint();
                }
            } catch (Exception e) {
                System.err.println("Erro ao destacar POV: " + e.getMessage());
            }
        }
    }
    
    @Override
    public void dispose() {
        if (uiUpdateTimer != null) {
            uiUpdateTimer.stop();
        }
        JoystickManager.globalCleanup();
        super.dispose();
    }
}


