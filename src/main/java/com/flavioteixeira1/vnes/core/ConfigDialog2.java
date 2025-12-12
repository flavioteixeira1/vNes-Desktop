package com.flavioteixeira1.vnes.core;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;

public class ConfigDialog2 extends JDialog {
    private static ConfigDialog2 currentInstance;

    public ConfigDialog2(Frame owner, JoystickManager jm) {
        super(owner, "Configurar mapeamento: " + jm.getJoystickName(), true);
        currentInstance = this;

        setLayout(new BorderLayout());
        JPanel center = new JPanel(new GridLayout(0, 3, 8, 8));
        JLabel[] labels = new JLabel[12];
        JButton[] configBtns = new JButton[12];

        for (int i = 0; i < labels.length; i++) {
            labels[i] = new JLabel("Botão " + (i + 1));
            int keyCode = jm.getKeyForButton(i);
            String txt = keyCode > 0 ? KeyEvent.getKeyText(keyCode) : "[NO KEY]";
            configBtns[i] = new JButton(txt);
            final int idx = i;
            configBtns[i].addActionListener(e -> {
                int novo = KeyCaptureDialog2.capture(this, "Botão " + (idx + 1));
                if (novo > 0) {
                    jm.setButtonMapping(idx, novo);
                    configBtns[idx].setText(KeyEvent.getKeyText(novo));
                }
            });
            center.add(labels[i]);
            center.add(configBtns[i]);
            center.add(new JLabel("")); // espaço
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