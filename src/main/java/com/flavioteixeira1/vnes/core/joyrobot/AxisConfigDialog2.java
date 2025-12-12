package com.flavioteixeira1.vnes.core.joyrobot;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AxisConfigDialog2 extends JDialog {
    public AxisConfigDialog2(Frame owner, int axisNum) {
        super(owner, "Set Axis " + axisNum, true);
        setLayout(new GridLayout(4, 2));
        JCheckBox grad = new JCheckBox("Gradient");
        JComboBox<String> cmb = new JComboBox<>(new String[]{"Keyboard/Mouse Button"});
        JTextField speed = new JTextField("100");
        speed.setEnabled(false);
        JTextField sens = new JTextField("1,00"); sens.setEnabled(false);

        add(grad); add(cmb); add(new JLabel("Mouse Speed")); add(speed);
        add(new JLabel("Sensitivity")); add(sens);

        JButton cancel = new JButton("Cancel"); JButton ok = new JButton("OK");
        JPanel bottom = new JPanel(new FlowLayout()); bottom.add(cancel); bottom.add(ok);
        add(bottom);

        cancel.addActionListener(e -> dispose());
        ok.addActionListener(e -> dispose());
        setSize(300,160); setLocationRelativeTo(owner);
    }
}