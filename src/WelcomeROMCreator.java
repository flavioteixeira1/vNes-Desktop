// Classe utilitária para criar ROM de boas-vindas
import java.io.*;

public class WelcomeROMCreator {
    public static void createWelcomeROM() {
        try {
            FileOutputStream fos = new FileOutputStream("welcome.nes");
            
            // Header NES básico
            byte[] header = new byte[16];
            header[0] = 'N'; header[1] = 'E'; header[2] = 'S'; header[3] = 0x1A;
            header[4] = 1;  // 1 bank de PRG ROM (16KB)
            header[5] = 1;  // 1 bank de CHR ROM (8KB)
            header[6] = 0;  // Mapper 0, mirroring horizontal
            header[7] = 0;  // Mapper continuação
            // Resto do header com zeros
            for (int i = 8; i < 16; i++) header[i] = 0;
            
            fos.write(header);
            
            // PRG ROM - 16KB preenchido com um padrão simples
            byte[] prgRom = new byte[16384];
            // Padrão simples que não crasha o emulador
            for (int i = 0; i < prgRom.length; i++) {
                prgRom[i] = (byte)0xEA; // NOP instruction
            }
            fos.write(prgRom);
            
            // CHR ROM - 8KB com padrão simples
            byte[] chrRom = new byte[8192];
            fos.write(chrRom);
            
            fos.close();
            System.out.println("ROM de boas-vindas criada: welcome.nes");
        } catch (IOException e) {
            System.err.println("Erro ao criar ROM de boas-vindas: " + e.getMessage());
        }
    }
}