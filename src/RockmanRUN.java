import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;

public class RockmanRUN extends JFrame {
   
    public RockmanRUN() {
        initComponents();
    }
    
    private void initComponents() { 
        setTitle("Rockman NES Emulator - Use: Arrow Keys, Z, X, Ctrl, Enter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String args[]) {
         System.out.println("=== INICIANDO ROCKMANRUN ===");
        // Tentar usar o look and feel do sistema
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            System.out.println("Look and Feel configurado");
        } catch (Exception ex) {
             System.err.println("Erro no Look and Feel: " + ex.getMessage());
            try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex2) {
                System.err.println("Não foi possível carregar o System Look and Feel: " + ex.getMessage());
            }
        }
         // Garantir que a UI é criada na EDT
        try {
            SwingUtilities.invokeAndWait(new Runnable(){
                public void run() {
                    System.out.println("Criando frame...");
                    RockmanRUN frame = new RockmanRUN();
                    System.out.println("Criando RockmanForm...");
                    final RockmanForm game = new RockmanForm();

                    if (args.length > 0 && args[0] != null && !args[0].trim().isEmpty()) {
                        game.setRomPath(args[0]);
                    }
                    frame.setSize(650, 620);
                    frame.getContentPane().add(game);
                    // Passar referência do frame para o RockmanForm
                    game.setParentFrame(frame);
                    frame.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent we) {
                            game.stop();
                            game.destroy();
                            System.exit(0);
                        }
                    });

                    frame.setVisible(true);
                    game.init();
                    game.start();
                    // Forçar redesenho inicial para mostrar tela de boas-vindas
                    game.repaint(); 
                }
            });
        } catch (InterruptedException | java.lang.reflect.InvocationTargetException e) {
            e.printStackTrace();
            //System.exit(1);
             createAndShowGUI(args);
        }
    
  }//Fim do método main

private static void createAndShowGUI(String[] args) {
            RockmanRUN frame = new RockmanRUN();
            final RockmanForm game = new RockmanForm();

            if (args.length > 0 && args[0] != null && !args[0].trim().isEmpty()) {
                game.setRomPath(args[0]);
            }

            frame.setSize(650, 620);
            frame.getContentPane().add(game);
            
            // Passar referência do frame para o RockmanForm
            game.setParentFrame(frame);
            
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent we) {
                    game.stop();
                    game.destroy();
                    System.exit(0);
                }
            });

            frame.setVisible(true);
            
            // Inicializar o jogo na EDT
            game.init();
            game.start();
            
            // Forçar redesenho inicial para mostrar tela de boas-vindas
            game.repaint();
            
            System.out.println("RockmanRUN - Interface criada com sucesso!");
        }//Fim do método estático alternativo createA
    




}