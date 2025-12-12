package com.flavioteixeira1.vnes.core.joyrobot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class KeyCaptureDialog extends JDialog {
    private int capturedKey = -1;
    private boolean clearMapping = false;
    ConfigDialog configDialog;
    private String label;

    private KeyCaptureDialog(ConfigDialog configDialog, String label) {
        super(configDialog, "Pressione uma tecla...", true);
        this.label = label;
        setLayout(new BorderLayout());
        
        // Atualizando instruções para incluir Ctrl+L para limpeza
        JLabel instrucao = new JLabel(
            "<html><div style='text-align: center;'>" +
            "Pressione a tecla para " + label + "<br>" +
            "(ESC para cancelar | Ctrl+L para limpar mapeamento)" +
            "</div></html>", 
            JLabel.CENTER
        );
        instrucao.setFont(new Font("Arial", Font.PLAIN, 12));
        add(instrucao, BorderLayout.CENTER);
        
        setSize(350, 150); 
        setLocationRelativeTo(configDialog);

        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                // Verificar se Ctrl+L foi pressionado
                if (e.getKeyCode() == KeyEvent.VK_L && 
                    (e.isControlDown() || e.isMetaDown())) { // Meta para Mac
                    clearMapping = true;
                    capturedKey = -2; // Código especial para indicar limpeza
                    dispose();
                }
                // Verificar se foi apenas Ctrl pressionado (sem L)
                else if (e.isControlDown() || e.isMetaDown()) {
                    // Aguardar a próxima tecla
                    return;
                }
                else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    capturedKey = -1;
                    dispose();
                } else {
                    capturedKey = e.getKeyCode();
                    dispose();
                }
            }
            
            // Também processar combinação via keyReleased para garantir captura
            public void keyReleased(KeyEvent e) {
                // Não é necessário processar aqui, o keyPressed já cuida
            }
        });
        
        // Adicionar também um KeyListener ao painel principal para garantir captura
        setFocusable(true);
        requestFocusInWindow();
    }

    public static int capture(ConfigDialog configDialog, String label) {
        KeyCaptureDialog dlg = new KeyCaptureDialog(configDialog, label);
        dlg.setVisible(true);
        return dlg.capturedKey;
    }
    
    // Novo método para capturar com indicação de limpeza
    public static CaptureResult captureWithClear(ConfigDialog configDialog, String label) {
        KeyCaptureDialog dlg = new KeyCaptureDialog(configDialog, label);
        dlg.setVisible(true);
        return new CaptureResult(dlg.capturedKey, dlg.clearMapping);
    }
    
    // Classe interna para retornar resultado da captura - 
    // tem necessidade de ficar em arquivo próprio ? Ficaria mais organizado ?
    public static class CaptureResult {
        public final int keyCode;
        public final boolean clearMapping;
        
        public CaptureResult(int keyCode, boolean clearMapping) {
            this.keyCode = keyCode;
            this.clearMapping = clearMapping;
        }
        
        public boolean isCanceled() {
            return keyCode == -1;
        }
        
        public boolean isClear() {
            return clearMapping;
        }
    }
}