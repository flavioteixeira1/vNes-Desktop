import javax.sound.sampled.*;

public class AudioTest {
    public static void main(String[] args) {
        try {
            AudioFormat format = new AudioFormat(44100, 16, 2, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
            
            System.out.println("Áudio funcionando! Linha: " + line.getLineInfo());
            line.close();
            
        } catch (Exception e) {
            System.err.println("Falha no teste de áudio: " + e.getMessage());
            e.printStackTrace();
        }
    }
}