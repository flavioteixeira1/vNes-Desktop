package com.flavioteixeira1.vnes.core.joyrobot;

import javax.swing.*;

public class jkeyboard {
    public static void main(String[] args) {
        // Certifica que todo Swing roda na thread correta
        SwingUtilities.invokeLater(() -> {
            MainWindow tela = new MainWindow();
            tela.setVisible(true);
        });
    }
}