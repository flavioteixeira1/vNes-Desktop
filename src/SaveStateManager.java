// SaveStateManager.java
import java.io.*;

public class SaveStateManager {
    private static final int MAX_SAVE_STATES = 10;
    private NES nes;
    private String[] saveStateNames;
    private boolean[] saveStateExists;
    
    public SaveStateManager(NES nes) {
        this.nes = nes;
        this.saveStateNames = new String[MAX_SAVE_STATES];
        this.saveStateExists = new boolean[MAX_SAVE_STATES];
        
        // Inicializar nomes padrão
        for (int i = 0; i < MAX_SAVE_STATES; i++) {
            saveStateNames[i] = "Save State " + (i + 1);
            saveStateExists[i] = false;
        }
        
        loadSaveStateInfo();
    }
    
    public boolean saveState(int slot, String customName) {
        if (slot < 0 || slot >= MAX_SAVE_STATES) return false;
                
        try {
            String fileName = "savestate_" + slot + ".dat";
            FileOutputStream fos = new FileOutputStream(fileName);
            
            // Usar sua ByteBuffer customizada
            ByteBuffer buf = new ByteBuffer(1024 * 1024, ByteBuffer.BO_LITTLE_ENDIAN); // 1MB buffer
                        
            // Salvar estado do jogo primeiro
            nes.stateSave(buf);
            
            // Depois salvar metadados
            buf.putString(customName != null ? customName : saveStateNames[slot]);
            buf.putLong(System.currentTimeMillis());
            
            // Escrever apenas os bytes usados
            byte[] data = buf.getBytes();
            fos.write(data, 0, buf.getPos());
            fos.close();
            
            // Atualizar informações locais
            saveStateNames[slot] = customName != null ? customName : saveStateNames[slot];
            saveStateExists[slot] = true;
            
            saveSaveStateInfo();
            
            System.out.println("Save state salvo no slot " + slot + ": " + saveStateNames[slot]);
            return true;
            
        } catch (IOException e) {
            System.err.println("Erro ao salvar estado: " + e.getMessage());
            return false;
        }
    }
    
    public boolean loadState(int slot) {
        if (slot < 0 || slot >= MAX_SAVE_STATES || !saveStateExists[slot]) return false;
        
        try {
            String fileName = "savestate_" + slot + ".dat";
            File file = new File(fileName);
            if (!file.exists()) {
                System.err.println("Arquivo de save state não existe: " + fileName);
                return false;
            }
            
            FileInputStream fis = new FileInputStream(file);
            byte[] fileData = new byte[(int)file.length()];
            fis.read(fileData);
            fis.close();
            
            // Criar ByteBuffer customizado com os dados do arquivo
            ByteBuffer buf = new ByteBuffer(fileData, ByteBuffer.BO_LITTLE_ENDIAN);
            
            // Parar emulação antes de carregar
            boolean wasRunning = nes.isRunning();
            if (wasRunning) {
                nes.stopEmulation();
            }
            
            // Carregar estado do jogo
            boolean success = nes.stateLoad(buf);
            
            if (success) {
                // Tentar ler metadados (pode falhar se o arquivo for antigo)
                try {
                    String name = buf.readString();
                    long timestamp = buf.readLong();
                    System.out.println("Save state carregado do slot " + slot + ": " + name);
                } catch (Exception e) {
                    System.out.println("Save state carregado do slot " + slot + " (metadados ausentes)");
                }
                
                // Reiniciar emulação se estava rodando
                if (wasRunning) {
                    // Pequeno delay para estabilização
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ie) {}
                    
                    nes.startEmulation();
                }
            }
            
            return success;
            
        } catch (IOException e) {
            System.err.println("Erro ao carregar estado: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deleteState(int slot) {
        if (slot < 0 || slot >= MAX_SAVE_STATES || !saveStateExists[slot]) return false;
        
        try {
            String fileName = "savestate_" + slot + ".dat";
            File file = new File(fileName);
            boolean deleted = file.delete();
            
            if (deleted) {
                saveStateExists[slot] = false;
                saveStateNames[slot] = "Save State " + (slot + 1);
                saveSaveStateInfo();
                System.out.println("Save state deletado do slot " + slot);
            }
            
            return deleted;
            
        } catch (Exception e) {
            System.err.println("Erro ao deletar estado: " + e.getMessage());
            return false;
        }
    }
    
    public void renameState(int slot, String newName) {
        if (slot < 0 || slot >= MAX_SAVE_STATES) return;
        
        if (newName != null && !newName.trim().isEmpty()) {
            saveStateNames[slot] = newName;
            saveSaveStateInfo();
        }
    }
    
    public String getStateName(int slot) {
        if (slot < 0 || slot >= MAX_SAVE_STATES) return "";
        return saveStateNames[slot];
    }
    
    public boolean stateExists(int slot) {
        if (slot < 0 || slot >= MAX_SAVE_STATES) return false;
        return saveStateExists[slot];
    }
    
    public int getMaxSaveStates() {
        return MAX_SAVE_STATES;
    }
    
    private void saveSaveStateInfo() {
        try {
            FileOutputStream fos = new FileOutputStream("savestate_info.dat");
            DataOutputStream dos = new DataOutputStream(fos);
            
            for (int i = 0; i < MAX_SAVE_STATES; i++) {
                dos.writeBoolean(saveStateExists[i]);
                dos.writeUTF(saveStateNames[i]);
            }
            
            dos.close();
            fos.close();
        } catch (IOException e) {
            System.err.println("Erro ao salvar informações dos save states: " + e.getMessage());
        }
    }
    
    private void loadSaveStateInfo() {
        try {
            FileInputStream fis = new FileInputStream("savestate_info.dat");
            DataInputStream dis = new DataInputStream(fis);
            
            for (int i = 0; i < MAX_SAVE_STATES; i++) {
                saveStateExists[i] = dis.readBoolean();
                saveStateNames[i] = dis.readUTF();
            }
            
            dis.close();
            fis.close();
        } catch (IOException e) {
            // Arquivo não existe, usar valores padrão
            System.out.println("Informações dos save states não encontradas, usando padrões");
        }
    }

    private boolean validateState(int slot) {
        try {
            String fileName = "savestate_" + slot + ".dat";
            File file = new File(fileName);
            if (!file.exists()) return false;
            
            // Verificar tamanho mínimo razoável
            if (file.length() < 1024) {
                System.err.println("Save state muito pequeno: " + file.length() + " bytes");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}