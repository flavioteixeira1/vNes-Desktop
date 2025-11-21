package com.flavioteixeira1.vnes.core;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class InputConfigDialog extends JDialog {
    private KbInputHandler kbHandler;
    private JoystickManager joyManager;
    private static final int MODE_KEYBOARD_JOYSTICK = 0;
    private static final int MODE_JOYSTICK_ONLY = 1;
    private int playerId;
    private JButton[] keyButtons;
    private JComboBox<String> modeComboBox;
    private int currentMode;

    
    public InputConfigDialog(Frame parent, KbInputHandler kbHandler, JoystickManager joyManager, int playerId) {
        super(parent, "Configurar Controles - Player " + (playerId + 1), true);
        this.kbHandler = kbHandler;
        this.joyManager = joyManager;
        this.playerId = playerId;
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
         // Painel de seleção de modo
        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        modePanel.setBorder(BorderFactory.createTitledBorder("Modo de Controle"));
        
        modeComboBox = new JComboBox<>(new String[]{
            "Teclado + Joystick (Robot)", 
            "Somente Joystick"
        });
        // Determinar modo atual baseado na configuração do joystick
        currentMode = (joyManager != null && joyManager.isJoystickEnabled()) ? 
            MODE_JOYSTICK_ONLY : MODE_KEYBOARD_JOYSTICK;
        modeComboBox.setSelectedIndex(currentMode);
        
        modeComboBox.addActionListener(e -> {
            currentMode = modeComboBox.getSelectedIndex();
            updateUIForMode();
        });
        
        modePanel.add(new JLabel("Modo:"));
        modePanel.add(modeComboBox);
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Teclado", createKeyboardConfigPanel());
        
        if (joyManager != null && joyManager.isJoystickEnabled()) {
            tabbedPane.addTab("Joystick", createJoystickConfigPanel());
        }
        
        add(modePanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(getParent());
        setResizable(false);
    }

    //Método para atualizar a UI baseado no modo
    private void updateUIForMode() {
            boolean joystickOnly = (currentMode == MODE_JOYSTICK_ONLY);
            
            // Atualizar a aba de teclado baseado no modo
            Component keyboardTab = ((JTabbedPane)getContentPane().getComponent(1)).getComponentAt(0);
            if (keyboardTab instanceof JPanel) {
                enableComponents((JPanel)keyboardTab, !joystickOnly);
            }
    }

    // Método auxiliar para habilitar/desabilitar componentes
    private void enableComponents(JPanel panel, boolean enabled) {
        panel.setEnabled(enabled);
        for (Component comp : panel.getComponents()) {
            comp.setEnabled(enabled);
            if (comp instanceof JPanel) {
                enableComponents((JPanel)comp, enabled);
            }
         }
    }

    
    private JPanel createKeyboardConfigPanel() {
        JPanel panel = new JPanel(new GridLayout(8, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        String[] buttonNames = {"Botão A", "Botão B", "Start", "Select", "Cima", "Baixo", "Esquerda", "Direita"};
        int[] nesButtons = {InputHandler.KEY_A, InputHandler.KEY_B, InputHandler.KEY_START, 
                           InputHandler.KEY_SELECT, InputHandler.KEY_UP, InputHandler.KEY_DOWN, 
                           InputHandler.KEY_LEFT, InputHandler.KEY_RIGHT};
        
        keyButtons = new JButton[buttonNames.length];
        
        for (int i = 0; i < buttonNames.length; i++) {
            JLabel label = new JLabel(buttonNames[i]);
            keyButtons[i] = new JButton(getKeyText(kbHandler.getCurrentMapping(nesButtons[i])));
            keyButtons[i].setFocusable(false);
            int a = i;
            
            final int buttonIndex = nesButtons[a];
            keyButtons[a].addActionListener(e -> captureKey(buttonIndex, keyButtons[a], buttonNames[a]));
            
            panel.add(label);
            panel.add(keyButtons[a]);
        }
        
        return panel;
    }
    
    private JPanel createJoystickConfigPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
       if (joyManager == null || !joyManager.isJoystickEnabled()) {
        JLabel noJoystickLabel = new JLabel(
            "<html><center><b>Nenhum joystick detectado</b><br><br>" +
            "Conecte um joystick e reinicie o emulador.</center></html>", 
            JLabel.CENTER
        );
        panel.add(noJoystickLabel, BorderLayout.CENTER);
        return panel;
    }
       // Painel de configuração do joystick
    JPanel configPanel = new JPanel(new GridLayout(0, 2, 5, 5));
    
    String[] buttonNames = {"Botão A", "Botão B", "Start", "Select"};
    int[] joyButtons = {0, 1, 2, 3}; // Índices físicos dos botões
    
    for (int a = 0; a < buttonNames.length; a++) {
        JLabel label = new JLabel(buttonNames[a]);
        JButton configButton = new JButton("Configurar");
        configButton.setFocusable(false);
        int i = a;
        final int joyButtonIndex = joyButtons[i];
        configButton.addActionListener(e -> {
            // Quando configurar um botão do joystick, automaticamente
            // usa o mesmo mapeamento do teclado
            int currentKey = kbHandler.getCurrentMapping(getNESButtonForJoyButton(joyButtonIndex));
            KeyCaptureDialog captureDialog = new KeyCaptureDialog(this, buttonNames[i]);
            captureDialog.setVisible(true);
            
            if (captureDialog.getCapturedKey() != -1) {
                // Atualiza tanto teclado quanto joystick
                kbHandler.remapKey(getNESButtonForJoyButton(joyButtonIndex), captureDialog.getCapturedKey());
            }
        });
        
        configPanel.add(label);
        configPanel.add(configButton);
    }
    
    JTextArea infoArea = new JTextArea(
        "Joystick: " + joyManager.getJoystickName() + "\n\n" +
        "Os botões do joystick seguirão automaticamente\n" +
        "o mapeamento definido na aba Teclado.\n\n" +
        "Direcionais: Usam os mesmos mapeamentos\n" +
        "definidos para as setas/teclado numérico."
    );
    infoArea.setEditable(false);
    infoArea.setBackground(panel.getBackground());
    
    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.add(configPanel, BorderLayout.NORTH);
    mainPanel.add(infoArea, BorderLayout.CENTER);
    
    panel.add(mainPanel, BorderLayout.CENTER);
    return panel;

    }


    private int getNESButtonForJoyButton(int joyButton) {
            switch(joyButton) {
                case 0: return InputHandler.KEY_A;
                case 1: return InputHandler.KEY_B;
                case 2: return InputHandler.KEY_START;
                case 3: return InputHandler.KEY_SELECT;
                default: return -1;
            }
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancelar");
        JButton defaultsButton = new JButton("Restaurar Padrões");
        
        okButton.addActionListener(e -> dispose());
        cancelButton.addActionListener(e -> dispose());
        defaultsButton.addActionListener(e -> restoreDefaults());
        
        panel.add(defaultsButton);
        panel.add(cancelButton);
        panel.add(okButton);
        
        return panel;
    }
    
    private void captureKey(int nesButton, JButton button, String buttonName) {
        KeyCaptureDialog captureDialog = new KeyCaptureDialog(this, buttonName);
        captureDialog.setVisible(true);
        
        if (captureDialog.getCapturedKey() != -1) {
            kbHandler.remapKey(nesButton, captureDialog.getCapturedKey());
            button.setText(getKeyText(captureDialog.getCapturedKey()));
        }
    }
    
    private void restoreDefaults() {
         if (kbHandler != null) {
        kbHandler.restoreDefaultMappings();
        
        // Atualizar os botões da UI
        updateKeyButtons();
        
        JOptionPane.showMessageDialog(this, 
            "Controles restaurados para configuração padrão do Player " + (playerId + 1),
            "Padrões Restaurados", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void updateKeyButtons() {
        if (keyButtons != null) {
            int[] nesButtons = {InputHandler.KEY_A, InputHandler.KEY_B, InputHandler.KEY_START, 
                            InputHandler.KEY_SELECT, InputHandler.KEY_UP, InputHandler.KEY_DOWN, 
                            InputHandler.KEY_LEFT, InputHandler.KEY_RIGHT};
            
            for (int i = 0; i < nesButtons.length; i++) {
                keyButtons[i].setText(getKeyText(kbHandler.getCurrentMapping(nesButtons[i])));
            }
        }
    }
    
    private String getKeyText(int keyCode) {
        return KeyEvent.getKeyText(keyCode);
    }
}