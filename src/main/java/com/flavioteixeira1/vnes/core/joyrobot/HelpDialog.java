package com.flavioteixeira1.vnes.core.joyrobot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Help / About dialog moved to its own class (modal)
 */
public class HelpDialog extends JDialog {
    private JTextArea text;
    private JButton bt_ok;
    MainWindow mainWindow;
   

    public HelpDialog(Frame owner) {
        super(owner, "Jkeyboard - Sobre o programa...", true);
        initialize();
    }

    private void initialize() {
        bt_ok = new JButton("Ok");
        bt_ok.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { setVisible(false); } });

        JScrollPane scrollpanel = new JScrollPane();
        text = new JTextArea();
        text.setEditable(false);
        scrollpanel.setViewportView(text);

        text.append(" - Jkeyboard Mapear eventos de Joystick para teclado similar ao QJoypad  feito em Java.");
        text.append("\n--------------------------------------------------------------------------------------------");
        text.append("\n Desenvolvido por Flávio Augusto Teixeira - flavioteixeira1@gmail.com                       ");
        text.append("\n repositório github:  https://github.com/flavioteixeira1/Jkeyboard                          ");
        text.append("\n--------------------------------------------------------------------------------------------");
        text.append("\n--------------------------------------------------------------------------------------------");
        text.append("\nColoque as bibliotecas nas pastas devidas conforme o seu sistema operacional                ");
        text.append("\nWindows (64-bit) :copie os arquivos  jinput-dx8_64.dll e  jinput-raw_64.dll para a pasta    ");
        text.append("\npara a pasta System32 (SysWOW64)  ou para a pasta q possui o executável do java (java.exe)  ");
        text.append("\n--------------------------------------------------------------------------------------------");
        text.append("\nLinux: Copie   libjinput-linux64.so    para  /usr/lib                                       ");
        text.append("\n--------------------------------------------------------------------------------------------");
        text.append("\nTodos estes arquivos podem ser encontrados no repositório do github na pasta external_lib   ");
        text.append("\nhttps://github.com/flavioteixeira1/Jkeyboard/tree/main/src/main/resources/external_lib      ");
        text.append("\n                                                                                            ");
        text.append("\nIMPORTANTE: Ao mapear uma tecla clique no botão  <Usar Mapeamento Customizado>              ");
        text.append("\nAo limpar uma tecla clique no botão <Usar Mapeamento Customizado>                           ");
        text.append("\n");

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.add(bt_ok);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(scrollpanel, BorderLayout.CENTER);
        getContentPane().add(bottom, BorderLayout.SOUTH);

        setSize(490, 380);
        setResizable(false);
        setLocationRelativeTo(getOwner());

        addWindowListener(new WindowAdapter() { public void windowClosing(WindowEvent e) { setVisible(false); } } );
    }
}