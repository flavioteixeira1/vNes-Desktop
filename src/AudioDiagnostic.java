import javax.sound.sampled.*;

public class AudioDiagnostic {
    
    public void checkAudioSystem() {
        System.out.println("=== DIAGNÓSTICO DE ÁUDIO DETALHADO ===");
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("OS: " + System.getProperty("os.name"));
        
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        System.out.println("Mixers disponíveis: " + mixerInfos.length);
        
        for (Mixer.Info info : mixerInfos) {
            System.out.println("Mixer: " + info.getName());
            System.out.println("  Descrição: " + info.getDescription());
            System.out.println("  Vendor: " + info.getVendor());
            
            // Verificar se é o mixer Intel
            if (info.getName().contains("Intel")) {
                System.out.println("  ✅ MIXER INTEL ENCONTRADO - SERÁ USADO");
            }
        }
        
        // Testar formato específico
        AudioFormat format = new AudioFormat(44100, 16, 2, true, false);
        DataLine.Info lineInfo = new DataLine.Info(SourceDataLine.class, format);
        
        System.out.println("Formato 44100Hz 16-bit stereo suportado: " + 
                          AudioSystem.isLineSupported(lineInfo));
        
        System.out.println("=== FIM DO DIAGNÓSTICO ===");
    }
}