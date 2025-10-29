
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;



/**
 *
 * @author flavio
 * 
 */
public class RockmanVnesFrame extends javax.swing.JFrame {
   
    /**
     * Creates new form RockmanVnesFrame
     */
    public RockmanVnesFrame() throws AWTException {
      
        initComponents();
    }

    
    @SuppressWarnings("Aplicativo extraido do Applet Rockman - Baseado no Vnes")

    private void initComponents() { 
        setTitle("Rockman - use:setas teclas: Z, X, Ctrl, Enter ");
        
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws AWTException {
        
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(RockmanVnesFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(RockmanVnesFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(RockmanVnesFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(RockmanVnesFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
      
         RockmanVnesFrame RockmanVnesFrame1 = new RockmanVnesFrame();
            

    final RockmanApp game = new RockmanApp();
    
    RockmanVnesFrame1.setSize(650,620);
    RockmanVnesFrame1.getContentPane().add(game);
    RockmanVnesFrame1.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent we) {
            game.stop();
            game.destroy();
            System.exit(0);
        }
    });

    RockmanVnesFrame1.setVisible(true);
    game.init();
    game.start();
    }

}
