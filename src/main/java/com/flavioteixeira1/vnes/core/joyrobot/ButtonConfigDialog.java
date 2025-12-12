package com.flavioteixeira1.vnes.core.joyrobot;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ButtonConfigDialog extends JDialog {
    public ButtonConfigDialog(Frame owner, int buttonNum) {
        super(owner, "Set Button " + buttonNum, true);
        setLayout(new GridLayout(3, 2));
        JTextField txtKey = new JTextField(""); txtKey.setEditable(false);
        JCheckBox sticky = new JCheckBox("Sticky");
        JCheckBox rapid = new JCheckBox("Rapid Fire");
        JButton cancel = new JButton("Cancel");
        JButton ok = new JButton("OK");
        add(txtKey); add(sticky); add(rapid);
        add(cancel); add(ok);

        cancel.addActionListener(e -> dispose());
        ok.addActionListener(e -> dispose());
        setSize(220, 120); setLocationRelativeTo(owner);
    }
}