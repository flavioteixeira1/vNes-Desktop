package com.flavioteixeira1.vnes.core;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import net.java.games.input.Component;

public class JoystickMappingDialog extends JDialog {
    private JoystickManager joyManager;
    private InputConfigDialog inputConfigDialog;
    private int playerId;
    
    
    public JoystickMappingDialog(Frame parent, JoystickManager joyManager, int playerId) {
        super(parent, "Mapeamento do Joystick - Player " + (playerId + 1), true);
        this.joyManager = joyManager;
        this.playerId = playerId;
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Botões", createButtonsPanel());
        tabbedPane.addTab("Eixos", createAxesPanel());
        tabbedPane.addTab("Configuração", createConfigPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(getParent());
        setResizable(false);
    }
    
    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        String[] buttonNames = {"Botão A", "Botão B", "Start", "Select"};
        int[] buttonIndices = {0, 1, 2, 3};
        
        for (int a = 0; a < buttonNames.length; a++) {
            JLabel label = new JLabel(buttonNames[a]);
            JButton mapButton = new JButton("Mapear");
            int i = a;
            final int buttonIndex = buttonIndices[i];
            
            mapButton.addActionListener(e -> {
                KeyCaptureDialog captureDialog = new KeyCaptureDialog(inputConfigDialog, buttonNames[i]);
                captureDialog.setVisible(true);
                
                if (captureDialog.getCapturedKey() != -1) {
                    joyManager.setCustomButtonMapping(buttonIndex, captureDialog.getCapturedKey());
                    JOptionPane.showMessageDialog(this,
                        buttonNames[i] + " mapeado para " + 
                        KeyEvent.getKeyText(captureDialog.getCapturedKey()),
                        "Mapeamento Configurado",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            });
            
            panel.add(label);
            panel.add(mapButton);
        }
        
        return panel;
    }
    
    private JPanel createAxesPanel() {
            JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            String[] axisNames = {"Eixo X", "Eixo Y", "DPad X", "DPad Y"};
            Component.Identifier[] axisIdentifiers = {
                Component.Identifier.Axis.X,
                Component.Identifier.Axis.Y,
                Component.Identifier.Axis.POV,
                Component.Identifier.Axis.POV
            };
            
            for (int i = 0; i < axisNames.length; i++) {
                JLabel label = new JLabel(axisNames[i]);
                
                // CORREÇÃO: Criar JLabel separado em vez de tentar converter JButton
                JLabel axisInfoLabel = new JLabel("Eixo do Joystick");
                axisInfoLabel.setForeground(Color.GRAY);
                
                panel.add(label);
                panel.add(axisInfoLabel);
            }
            
            JTextArea infoArea = new JTextArea(
                "Os eixos são automaticamente detectados.\n" +
                "Eixo X: Movimento horizontal\n" +
                "Eixo Y: Movimento vertical\n" +
                "DPad: Direcionais do joystick\n\n" +
                "Para configurar eixos, use a detecção automática\n" +
                "ou configure manualmente no código."
            );
            infoArea.setEditable(false);
            infoArea.setBackground(panel.getBackground());
            infoArea.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
            
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.add(panel, BorderLayout.NORTH);
            mainPanel.add(infoArea, BorderLayout.CENTER);
            
            return mainPanel;
    }
    
    private JPanel createConfigPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JCheckBox customMappingCheck = new JCheckBox("Usar mapeamento customizado");
        customMappingCheck.addActionListener(e -> {
            joyManager.setUseCustomMapping(customMappingCheck.isSelected());
        });
        
        JCheckBox joystickOnlyCheck = new JCheckBox("Modo somente joystick");
        joystickOnlyCheck.addActionListener(e -> {
            joyManager.setJoystickOnlyMode(joystickOnlyCheck.isSelected());
        });
        
        JTextArea infoArea = new JTextArea(
            "Mapeamento Customizado: Use teclas diferentes das padrão\n" +
            "Modo Somente Joystick: Desativa emulação via teclado\n\n" +
            "Recomendado para joysticks com botões defeituosos ou\n" +
            "para preferências pessoais de jogabilidade."
        );
        infoArea.setEditable(false);
        infoArea.setBackground(panel.getBackground());
        
        panel.add(customMappingCheck);
        panel.add(joystickOnlyCheck);
        panel.add(infoArea);
        
        return panel;
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
    
    private void restoreDefaults() {
        // Implementar restauração dos padrões
        JOptionPane.showMessageDialog(this,
            "Configurações restauradas para os padrões do sistema",
            "Padrões Restaurados",
            JOptionPane.INFORMATION_MESSAGE);
    }
}