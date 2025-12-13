package com.flavioteixeira1.vnes.core;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ConfigDialog2 extends JDialog {
    private static ConfigDialog2 currentInstance;
<<<<<<< HEAD:src/main/java/com/flavioteixeira1/vnes/core/ConfigDialog2.java
=======
    private JoystickManager joystickManager;
>>>>>>> joystick:src/main/java/com/flavioteixeira1/vnes/core/joyrobot/ConfigDialog.java

    public ConfigDialog2(Frame owner, JoystickManager jm) {
        super(owner, "Configurar mapeamento: " + jm.getJoystickName(), true);
        currentInstance = this;
        this.joystickManager = jm;

        setLayout(new BorderLayout());
        JPanel center = new JPanel(new GridLayout(0, 3, 8, 8));
        JLabel[] labels = new JLabel[12];
        JButton[] configBtns = new JButton[12];

        for (int i = 0; i < labels.length; i++) {
            labels[i] = new JLabel("Botão " + (i + 1));
            
            // Obter o mapeamento
            int keyCode = jm.getMappedKeyForButton(i);
            String txt = keyCode > 0 ? KeyEvent.getKeyText(keyCode) : "[NO KEY]";
            
            configBtns[i] = new JButton(txt);
            final int idx = i;
            
            configBtns[i].addActionListener(e -> {
<<<<<<< HEAD:src/main/java/com/flavioteixeira1/vnes/core/ConfigDialog2.java
                int novo = KeyCaptureDialog2.capture(this, "Botão " + (idx + 1));
                if (novo > 0) {
                    jm.setButtonMapping(idx, novo);
                    configBtns[idx].setText(KeyEvent.getKeyText(novo));
=======
                String label = "Botão " + (idx + 1);
                KeyCaptureDialog2.CaptureResult result = 
                    KeyCaptureDialog2.captureWithClear(this, label);
                
                if (result.isClear()) {
                    // Limpar mapeamento
                    jm.setCustomButtonMapping(idx, -1);
                    configBtns[idx].setText("[NO KEY]");
                }
                else if (result.keyCode > 0) {
                    // Configurar nova tecla
                    jm.setCustomButtonMapping(idx, result.keyCode);
                    jm.setUseCustomMapping(true);
                    configBtns[idx].setText(KeyEvent.getKeyText(result.keyCode));
>>>>>>> joystick:src/main/java/com/flavioteixeira1/vnes/core/joyrobot/ConfigDialog.java
                }
            });
            
            center.add(labels[i]);
            center.add(configBtns[i]);
            center.add(new JLabel(""));
        }
        
        add(center, BorderLayout.CENTER);

        JButton btnFechar = new JButton("Fechar");
        btnFechar.addActionListener(e -> dispose());
        add(btnFechar, BorderLayout.SOUTH);

        setSize(480, 400);
        setLocationRelativeTo(owner);
    }
    
    public static ConfigDialog2 getCurrentInstance() {
        return currentInstance;
    }
    
    @Override
    public void dispose() {
        currentInstance = null;
        super.dispose();
    }
}