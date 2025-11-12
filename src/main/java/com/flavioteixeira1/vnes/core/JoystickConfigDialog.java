package com.flavioteixeira1.vnes.core;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

public class JoystickConfigDialog extends JDialog {
    private JoystickManager joystickManager;
    private Map<Integer, JButton> buttonMap;
    private Map<Integer, Integer> currentMapping;
    private JButton saveButton;
    private JButton cancelButton;
    private int waitingForButton = -1;
    private JLabel instructionLabel;

    public JoystickConfigDialog(Frame parent, JoystickManager joystickManager) {
        super(parent, "Configuração do Joystick", true);
        this.joystickManager = joystickManager;
        this.buttonMap = new HashMap<>();
        this.currentMapping = new HashMap<>();
        
        initializeDialog();
        loadCurrentMapping();
    }
    
    private void initializeDialog() {
        setLayout(new BorderLayout());
        setSize(500, 400);
        setLocationRelativeTo(getOwner());
        
        // Painel de instruções
        instructionLabel = new JLabel("Clique em um botão para remapeá-lo", JLabel.CENTER);
        instructionLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(instructionLabel, BorderLayout.NORTH);
        
        // Painel principal com botões
        JPanel mainPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Botões para mapeamento (apenas os 4 principais para simplificar)
        String[] buttonLabels = {
            "Botão A (Ataque)", "Botão B (Pulo)", 
            "Start (Iniciar)", "Select (Selecionar)",
            "Direcional Cima", "Direcional Baixo",
            "Direcional Esquerda", "Direcional Direita"
        };
        
        int[] buttonIndices = {0, 1, 2, 3, -1, -2, -3, -4};
        
        for (int i = 0; i < buttonLabels.length; i++) {
            JLabel label = new JLabel(buttonLabels[i]);
            JButton mapButton = new JButton("Clique para mapear");
            mapButton.setActionCommand(String.valueOf(buttonIndices[i]));
            mapButton.addActionListener(new MapButtonListener());
            
            mainPanel.add(label);
            mainPanel.add(mapButton);
            buttonMap.put(buttonIndices[i], mapButton);
        }
        
        add(new JScrollPane(mainPanel), BorderLayout.CENTER);
        
        // Painel de botões
        JPanel buttonPanel = new JPanel(new FlowLayout());
        saveButton = new JButton("Salvar");
        cancelButton = new JButton("Cancelar");
        
        saveButton.addActionListener(e -> saveConfiguration());
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadCurrentMapping() {
        // Usar mapeamento padrão
        currentMapping.put(0, KeyEvent.VK_Z);      // A
        currentMapping.put(1, KeyEvent.VK_X);      // B  
        currentMapping.put(2, KeyEvent.VK_ENTER);  // Start
        currentMapping.put(3, KeyEvent.VK_CONTROL); // Select
        
        updateButtonLabels();
    }
    
    private void updateButtonLabels() {
        for (Map.Entry<Integer, JButton> entry : buttonMap.entrySet()) {
            int buttonIndex = entry.getKey();
            JButton button = entry.getValue();
            
            if (buttonIndex >= 0) {
                int keyCode = currentMapping.getOrDefault(buttonIndex, -1);
                if (keyCode != -1) {
                    button.setText(KeyEvent.getKeyText(keyCode));
                }
            } else {
                // Direcionais
                switch(buttonIndex) {
                    case -1: button.setText(KeyEvent.getKeyText(KeyEvent.VK_UP)); break;
                    case -2: button.setText(KeyEvent.getKeyText(KeyEvent.VK_DOWN)); break;
                    case -3: button.setText(KeyEvent.getKeyText(KeyEvent.VK_LEFT)); break;
                    case -4: button.setText(KeyEvent.getKeyText(KeyEvent.VK_RIGHT)); break;
                }
            }
        }
    }
    
    private class MapButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int buttonIndex = Integer.parseInt(e.getActionCommand());
            startMapping(buttonIndex);
        }
    }
    
    private void startMapping(int buttonIndex) {
        waitingForButton = buttonIndex;
        instructionLabel.setText("Pressione uma tecla para: " + getButtonName(buttonIndex));
        instructionLabel.setForeground(Color.RED);
        
        // Configurar key listener
        setFocusable(true);
        requestFocus();
        
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                finishMapping(e.getKeyCode());
            }
        });
    }
    
    private String getButtonName(int buttonIndex) {
        switch(buttonIndex) {
            case 0: return "Botão A (Ataque)";
            case 1: return "Botão B (Pulo)";
            case 2: return "Start";
            case 3: return "Select";
            case -1: return "Cima ↑";
            case -2: return "Baixo ↓";
            case -3: return "Esquerda ←";
            case -4: return "Direita →";
            default: return "Botão " + buttonIndex;
        }
    }
    
    private void finishMapping(int keyCode) {
        if (waitingForButton >= 0) {
            currentMapping.put(waitingForButton, keyCode);
        }
        
        waitingForButton = -1;
        instructionLabel.setText("Clique em um botão para remapeá-lo");
        instructionLabel.setForeground(Color.BLACK);
        
        updateButtonLabels();
        
        // Remover todos os key listeners
        for (KeyListener listener : getKeyListeners()) {
            removeKeyListener(listener);
        }
    }
    
    private void saveConfiguration() {
        // Aplicar o mapeamento ao JoystickManager
        for (Map.Entry<Integer, Integer> entry : currentMapping.entrySet()) {
            if (entry.getKey() >= 0) {
                joystickManager.setButtonMapping(entry.getKey(), entry.getValue());
            }
        }
        
        JOptionPane.showMessageDialog(this, 
            "Configuração salva com sucesso!\n\n" +
            "Os novos mapeamentos estarão ativos imediatamente.",
            "Configuração Salva", 
            JOptionPane.INFORMATION_MESSAGE);
        
        dispose();
    }
}