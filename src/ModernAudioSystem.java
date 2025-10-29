import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

public class ModernAudioSystem {
    private SourceDataLine audioLine;
    private boolean initialized = false;
    private final int BUFFER_SIZE = 4096;
    private long lastWriteTime = 0;
    private static final long MAX_SILENCE_TIME = 2000; // 2 segundos
    


    
    /*
    public boolean initialize() {
         try {
            AudioFormat format = new AudioFormat(44100, 16, 2, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            
             // BUSCAR ESPECIFICAMENTE O MIXER QUE FUNCIONA: "Intel [plughw:0,0]"
            Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
            Mixer targetMixer = null;
             System.out.println("=== BUSCANDO MIXER COMPATÍVEL ===");
            for (Mixer.Info mixerInfo : mixerInfos) {
                System.out.println("Mixer disponível: " + mixerInfo.getName());
                
                // Procurar pelo mixer que sabemos que funciona
                if (mixerInfo.getName().equals("Intel [plughw:0,0]")) {
                    targetMixer = AudioSystem.getMixer(mixerInfo);
                    System.out.println("✅ MIXER CORRETO ENCONTRADO: " + mixerInfo.getName());
                    break;
                }
            }
             // Se não encontrou o específico, tentar qualquer Intel com "plughw"
            
            if (targetMixer == null) {
                for (Mixer.Info mixerInfo : mixerInfos) {
                    if (mixerInfo.getName().contains("plughw") && mixerInfo.getName().contains("Intel")) {
                        targetMixer = AudioSystem.getMixer(mixerInfo);
                        System.out.println("✅ MIXER ALTERNATIVO ENCONTRADO: " + mixerInfo.getName());
                        break;
                    }
                }
            }

            // Se ainda não encontrou, usar mixer default

             if (targetMixer == null) {
                System.out.println("⚠ Nenhum mixer Intel plughw encontrado, usando default");
                for (Mixer.Info mixerInfo : mixerInfos) {
                    if (!mixerInfo.getName().contains("Port") && mixerInfo.getName().contains("Intel")) {
                        targetMixer = AudioSystem.getMixer(mixerInfo);
                        System.out.println("✅ Tentando mixer: " + mixerInfo.getName());
                        break;
                    }
                }
            }

             // Fallback final

             if (targetMixer == null) {
                System.out.println("⚠ Usando mixer default do sistema");
                audioLine = (SourceDataLine) AudioSystem.getLine(info);
            } else {
                audioLine = (SourceDataLine) targetMixer.getLine(info);
            }

             // Tentar diferentes tamanhos de buffer se necessário

             boolean opened = false;
            int[] bufferSizes = {4096, 8192, 2048, 1024};
            
            for (int bufferSize : bufferSizes) {
                try {
                    audioLine.open(format, bufferSize);
                    opened = true;
                    System.out.println("✅ Buffer aberto com tamanho: " + bufferSize);
                    break;
                } catch (LineUnavailableException e) {
                    System.out.println("⚠ Buffer size " + bufferSize + " falhou, tentando próximo...");
                }
            }
            
            if (!opened) {
                // Última tentativa com buffer automático
                try {
                    audioLine.open(format);
                    System.out.println("✅ Buffer aberto com tamanho automático");
                } catch (LineUnavailableException e) {
                    System.err.println("✗ Não foi possível abrir áudio com nenhum tamanho de buffer");
                    return false;
                }
            }
            

            
            //audioLine.open(format, BUFFER_SIZE);
            audioLine.start();
            initialized = true;
            
            System.out.println("✓ ModernAudioSystem inicializado com sucesso");
            System.out.println("  Mixer: " + (targetMixer != null ? targetMixer.getMixerInfo().getName() : "Default"));
            System.out.println("  Buffer: " + audioLine.getBufferSize() + " bytes");
            System.out.println("  Format: " + format);
            
            return true;
            
        } catch (Exception e) {
            System.err.println("✗ Falha na inicialização do áudio: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
     */

     public boolean initialize() {

        
        System.out.println("=== INICIALIZAÇÃO DE ÁUDIO AVANÇADA ===");
        
        // Lista de formatos para tentar (em ordem de preferência)
        AudioFormat[] formats = {
            // Formato preferido
            new AudioFormat(44100, 16, 2, true, false),
            // Alternativas
            new AudioFormat(44100, 16, 2, true, true),   // Big-endian
            new AudioFormat(44100, 16, 1, true, false),  // Mono
            new AudioFormat(48000, 16, 2, true, false),  // 48kHz
            new AudioFormat(22050, 16, 2, true, false),  // 22.05kHz
            new AudioFormat(44100, 8, 2, true, false),   // 8-bit
            new AudioFormat(44100, 16, 2, false, false), // Unsigned
        };
        
        // Lista de mixers para tentar (em ordem de preferência)
        List<Mixer.Info> targetMixers = new ArrayList<>();
        Mixer.Info[] allMixers = AudioSystem.getMixerInfo();
        
        for (Mixer.Info mixer : allMixers) {
                    String name = mixer.getName();
                    if (name.equals("Intel [plughw:0,0]")) {
                        targetMixers.add(0, mixer); // Prioridade ABSOLUTA
                        System.out.println("🎯 MIXER PREFERIDO ENCONTRADO: " + name);
                    }
                }

                // Depois adicionar os outros normalmente...
                for (Mixer.Info mixer : allMixers) {
                    String name = mixer.getName();
                    if (!name.equals("Intel [plughw:0,0]")) {
                        if (name.contains("plughw") && name.contains("Intel")) {
                            if (!targetMixers.contains(mixer)) targetMixers.add(mixer);
                        } else if (name.contains("Intel") && !name.contains("Port")) {
                            if (!targetMixers.contains(mixer)) targetMixers.add(mixer);
                        } else if (name.contains("default")) {
                            if (!targetMixers.contains(mixer)) targetMixers.add(mixer);
                        }
                    }
                }

                // Se não encontrou mixers específicos, usar todos
                if (targetMixers.isEmpty()) {
                    targetMixers.addAll(List.of(allMixers));
                }
                    
        // Tentar cada combinação de mixer e formato
        for (Mixer.Info mixerInfo : targetMixers) {
            for (AudioFormat format : formats) {
                System.out.println("\n🔊 Testando: " + mixerInfo.getName() + 
                                 " com " + format.getSampleRate() + "Hz, " + 
                                 format.getSampleSizeInBits() + "bit, " + 
                                 (format.getChannels() == 2 ? "stereo" : "mono"));
                
                try {
                    DataLine.Info lineInfo = new DataLine.Info(SourceDataLine.class, format);
                    Mixer mixer = AudioSystem.getMixer(mixerInfo);
                    
                    if (!mixer.isLineSupported(lineInfo)) {
                        System.out.println("  ⚠ Formato não suportado");
                        continue;
                    }
                    
                    audioLine = (SourceDataLine) mixer.getLine(lineInfo);
                    
                    // Tentar diferentes tamanhos de buffer
                    int[] bufferSizes = {4096, 8192, 2048, 1024, 512};
                    boolean opened = false;
                    
                    for (int bufferSize : bufferSizes) {
                        try {
                            audioLine.open(format, bufferSize);
                            opened = true;
                            System.out.println("  ✅ Sucesso! Buffer: " + bufferSize);
                            break;
                        } catch (LineUnavailableException e) {
                            System.out.println("  ⚠ Buffer " + bufferSize + " falhou");
                        }
                    }
                    
                    // Se não abriu com tamanhos específicos, tentar com tamanho automático
                    if (!opened) {
                        try {
                            audioLine.open(format);
                            opened = true;
                            System.out.println("  ✅ Sucesso! Buffer automático");
                        } catch (LineUnavailableException e) {
                            System.out.println("  ❌ Falha mesmo com buffer automático");
                            continue;
                        }
                    }
                    
                    //Aqui

                    audioLine.start();
                    initialized = true;
                    
                    System.out.println("🎉 SISTEMA DE ÁUDIO INICIALIZADO COM SUCESSO!");
                    System.out.println("   Mixer: " + mixerInfo.getName());
                    System.out.println("   Formato: " + format.getSampleRate() + "Hz, " + 
                                     format.getSampleSizeInBits() + "bit, " + 
                                     format.getChannels() + " canal(ais)");
                    System.out.println("   Buffer: " + audioLine.getBufferSize() + " bytes");
                    
                    return true;
                    
                } catch (Exception e) {
                    System.out.println("  ❌ Erro: " + e.getClass().getSimpleName());
                    if (audioLine != null) {
                        audioLine.close();
                        audioLine = null;
                    }
                }
            }
        }
        
        System.err.println("💥 NENHUMA COMBINAÇÃO DE MIXER/FORMATO FUNCIONOU!");
         
        return false;
    }



    // MÉTODO 1: write(byte[]) - para array completo
    public void write(byte[] audioData) {
        if (!initialized || audioLine == null) {
            return;
        }
        
        try {
            audioLine.write(audioData, 0, audioData.length);
        } catch (Exception e) {
            System.err.println("✗ Erro ao escrever áudio: " + e.getMessage());
            initialized = false;
        }
    }
    
    // MÉTODO 2: write(byte[], offset, length) - para controle preciso
    public void write(byte[] audioData, int offset, int length) {
        if (!initialized || audioLine == null) {
            return;
        }
        
        try {
            // Manter registro da última escrita
            lastWriteTime = System.currentTimeMillis();
            
            // Verificar se a linha ainda está ativa
            if (!audioLine.isActive()) {
                System.out.println("🔄 Reativando linha de áudio...");
                audioLine.start();
            }
            
            // Escrever dados
            audioLine.write(audioData, offset, length);
            
        } catch (Exception e) {
            System.err.println("Erro ao escrever áudio: " + e.getMessage());
            initialized = false;
        }
    }
   

    // MÉTODO 3: write(short[]) - para arrays short
    public void write(short[] audioData) {
        if (!initialized || audioLine == null) return;
        
        try {
            byte[] byteBuffer = new byte[audioData.length * 2];
            
            // Converter short[] para byte[]
            for (int i = 0; i < audioData.length; i++) {
                byteBuffer[i * 2] = (byte)(audioData[i] & 0xFF);
                byteBuffer[i * 2 + 1] = (byte)((audioData[i] >> 8) & 0xFF);
            }
            
            audioLine.write(byteBuffer, 0, byteBuffer.length);
            
        } catch (Exception e) {
            System.err.println("✗ Erro ao escrever áudio: " + e.getMessage());
            initialized = false;
        }
    }
    
    // MÉTODO 4: drain() - para aguardar a reprodução terminar
    public void drain() {
        if (initialized && audioLine != null) {
            try {
                audioLine.drain();
            } catch (Exception e) {
                System.err.println("✗ Erro no drain: " + e.getMessage());
            }
        }
    }
    
    // MÉTODO 5: flush() - para limpar o buffer
    public void flush() {
        if (initialized && audioLine != null) {
            try {
                audioLine.flush();
            } catch (Exception e) {
                System.err.println("✗ Erro no flush: " + e.getMessage());
            }
        }
    }
    
    public void close() {
        if (audioLine != null) {
            audioLine.close();
            initialized = false;
        }
    }
    
    // MÉTODO 6: isInitialized() - para verificar o estado
    public boolean isInitialized() {
        return initialized && audioLine != null && audioLine.isOpen();
    }


    // Método para testar se o áudio está realmente funcionando
    public boolean testAudio() {
        if (!initialized || audioLine == null) return false;
        try {
            // Gerar um beep simples de teste
            byte[] testSound = generateTestBeep(440, 100); // 440Hz por 100ms
            write(testSound);
            drain(); // Esperar tocar
            return true;
        } catch (Exception e) {
            System.err.println("Teste de áudio falhou: " + e.getMessage());
            return false;
        }
    }

     private byte[] generateTestBeep(double frequency, int durationMs) {
        int sampleRate = 44100;
        int samples = (int) (sampleRate * durationMs / 1000.0);
        byte[] data = new byte[samples * 4]; // stereo 16-bit
        
        double volume = 0.3;
        
        for (int i = 0; i < samples; i++) {
            double time = i / (double) sampleRate;
            double value = Math.sin(2 * Math.PI * frequency * time) * volume;
            short sample = (short) (value * Short.MAX_VALUE);
            
            int index = i * 4;
            // Little endian
            data[index] = (byte) (sample & 0xFF);
            data[index + 1] = (byte) ((sample >> 8) & 0xFF);
            data[index + 2] = (byte) (sample & 0xFF);     // Left channel
            data[index + 3] = (byte) ((sample >> 8) & 0xFF); // Right channel
        }
        
        return data;
    }

     public boolean isAudioAlive() {
        return initialized && 
               audioLine != null && 
               audioLine.isOpen() && 
               (System.currentTimeMillis() - lastWriteTime) < MAX_SILENCE_TIME;
    }

}