package com.flavioteixeira1.vnes.core;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import net.java.games.input.Component;

public class AxisConfigDialog extends JDialog {
    private int negativeKey = -1;
    private int positiveKey = -1;
    private boolean configured = false;
    private InputConfigDialog inputConfigDialog;
    
    public AxisConfigDialog(Frame parent, String axisName, Component.Identifier axisId) {
        super(parent, "Configurar " + axisName, true);
        initComponents(axisName);
    }
    
    private void initComponents(String axisName) {
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel negativeLabel = new JLabel("Direção Negativa:");
        JButton negativeButton = new JButton("Pressione uma tecla");
        
        JLabel positiveLabel = new JLabel("Direção Positiva:");
        JButton positiveButton = new JButton("Pressione uma tecla");
        
        JLabel infoLabel = new JLabel("<html><center>Configure as teclas para " + axisName + "<br>Clique em cada botão e pressione a tecla desejada</center></html>");
        
        negativeButton.addActionListener(e -> captureKey("Negativa", negativeButton, false));
        positiveButton.addActionListener(e -> captureKey("Positiva", positiveButton, true));
        
        mainPanel.add(negativeLabel);
        mainPanel.add(negativeButton);
        mainPanel.add(positiveLabel);
        mainPanel.add(positiveButton);
        mainPanel.add(new JLabel()); // espaço vazio
        mainPanel.add(new JLabel()); // espaço vazio
        
        add(mainPanel, BorderLayout.CENTER);
        add(infoLabel, BorderLayout.NORTH);
        add(createButtonPanel(), BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(getParent());
        setResizable(false);
    }
    
    private void captureKey(String direction, JButton button, boolean isPositive) {
        KeyCaptureDialog captureDialog = new KeyCaptureDialog(inputConfigDialog, direction);
        captureDialog.setVisible(true);
        
        int keyCode = captureDialog.getCapturedKey();
        if (keyCode != -1) {
            if (isPositive) {
                positiveKey = keyCode;
            } else {
                negativeKey = keyCode;
            }
            button.setText(KeyEvent.getKeyText(keyCode));
        }
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancelar");
        
        okButton.addActionListener(e -> {
            if (negativeKey != -1 && positiveKey != -1) {
                configured = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Configure ambas as direções antes de confirmar.",
                    "Configuração Incompleta",
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dispose());
        
        panel.add(cancelButton);
        panel.add(okButton);
        
        return panel;
    }
    
    public int getNegativeKey() { return negativeKey; }
    public int getPositiveKey() { return positiveKey; }
    public boolean isConfigured() { return configured; }
}