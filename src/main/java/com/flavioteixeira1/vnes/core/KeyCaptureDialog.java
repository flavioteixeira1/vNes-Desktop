package com.flavioteixeira1.vnes.core;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;



public class KeyCaptureDialog extends JDialog {
    private int capturedKey = -1;
    private InputConfigDialog parent;
    
    public KeyCaptureDialog(InputConfigDialog parent, String buttonName) {
        super(parent, "Configurar " + buttonName, true);
        initComponents(buttonName);
    }
    
    private void initComponents(String buttonName) {
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        JLabel label = new JLabel(
            "<html><center>Pressione a tecla para:<br><b>" + buttonName + "</b><br><br>" +
            "Ou ESC para cancelar</center></html>", 
            JLabel.CENTER
        );
        label.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        add(label, BorderLayout.CENTER);
        
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    capturedKey = -1;
                    dispose();
                } else {
                    capturedKey = e.getKeyCode();
                    dispose();
                }
            }
        });
        
        setFocusable(true);
        requestFocusInWindow();
        pack();
        setLocationRelativeTo(getParent());
        setResizable(false);
    }
    
    public int getCapturedKey() {
        return capturedKey;
    }
}