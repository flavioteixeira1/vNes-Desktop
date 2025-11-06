import java.io.*;

public class SaveStateManager {
    private NES nes;
    private String saveStateName;
    private boolean saveStateExists;
    
    public SaveStateManager(NES nes) {
        this.nes = nes;
        this.saveStateName = "";
        this.saveStateExists = false;
        loadSaveStateInfo();
    }

    private String getRomBaseName() {
        if (nes.getRom() == null || nes.getRom().getFileName() == null) return "unknown";
        String romName = nes.getRom().getFileName();
        int endIndex = romName.lastIndexOf('.');
        if (endIndex > 0) {
            return romName.substring(0, endIndex);
        }
        return romName;
    }

    // Salva o estado em um arquivo usando parte do nome da ROM
    public boolean saveState(String customName) {
        System.out.println("chamando método saveState");
        try {
            String fileName = "savestate_" + getRomBaseName() + ".dat";
            FileOutputStream fos = new FileOutputStream(fileName);

            ByteBuffer buf = new ByteBuffer(1024 * 1024, ByteBuffer.BO_LITTLE_ENDIAN); // 1MB buffer
            
            nes.stateSave(buf);

            // Metadados
            buf.putString(customName != null ? customName : getRomBaseName());
            buf.putLong(System.currentTimeMillis());

            byte[] data = buf.getBytes();
            fos.write(data, 0, buf.getPos());
            fos.close();

            saveStateName = customName != null ? customName : getRomBaseName();
            saveStateExists = true;
            saveSaveStateInfo();
            System.out.println("Save state salvo: " + saveStateName);
            return true;

        } catch (IOException e) {
            System.err.println("Erro ao salvar estado: " + e.getMessage());
            return false;
        }
    }

    // Carrega o estado do arquivo atual, referente à ROM ativa
    public boolean loadState() {
        System.out.println("Chamando método loadState");
        try {
            String fileName = "savestate_" + getRomBaseName() + ".dat";
            File file = new File(fileName);
            if (!file.exists()) {
                System.err.println("Arquivo de save state não existe: " + fileName);
                return false;
            }
            FileInputStream fis = new FileInputStream(file);
            byte[] fileData = new byte[(int)file.length()];
            fis.read(fileData);
            fis.close();

            ByteBuffer buf = new ByteBuffer(fileData, ByteBuffer.BO_LITTLE_ENDIAN);

            boolean wasRunning = nes.isRunning();
            if (wasRunning) nes.stopEmulation();

            boolean success = nes.stateLoad(buf);

            if (success) {
                // Lê metadados (opcional)
                try {
                    saveStateName = buf.readString();
                    long timestamp = buf.readLong();
                    System.out.println("Save state carregado: " + saveStateName);
                } catch (Exception e) {
                    System.out.println("Save state carregado (metadados ausentes)");
                }
                if (wasRunning) {
                    try { Thread.sleep(100); } catch (InterruptedException ie) {}
                    nes.startEmulation();
                }
                saveStateExists = true;
            }
            return success;

        } catch (IOException e) {
            System.err.println("Erro ao carregar estado: " + e.getMessage());
            return false;
        }
    }

    // Exclui o save state da ROM atual
    public boolean deleteState() {
        try {
            String fileName = "savestate_" + getRomBaseName() + ".dat";
            File file = new File(fileName);
            boolean deleted = file.delete();
            if (deleted) {
                saveStateExists = false;
                saveStateName = "";
                saveSaveStateInfo();
                System.out.println("Save state deletado.");
            }
            return deleted;
        } catch (Exception e) {
            System.err.println("Erro ao deletar estado: " + e.getMessage());
            return false;
        }
    }

    public String getStateName() {
        return saveStateName != null && !saveStateName.isEmpty() ? saveStateName : "[Sem save]";
    }

    public boolean stateExists() {
        return saveStateExists;
    }

    private void saveSaveStateInfo() {
        try {
            FileOutputStream fos = new FileOutputStream("savestate_info.dat");
            DataOutputStream dos = new DataOutputStream(fos);
            dos.writeBoolean(saveStateExists);
            dos.writeUTF(saveStateName != null ? saveStateName : "");
            dos.close();
            fos.close();
        } catch (IOException e) {
            System.err.println("Erro ao salvar informações do save state: " + e.getMessage());
        }
    }

    private void loadSaveStateInfo() {
        try {
            FileInputStream fis = new FileInputStream("savestate_info.dat");
            DataInputStream dis = new DataInputStream(fis);
            saveStateExists = dis.readBoolean();
            saveStateName = dis.readUTF();
            dis.close();
            fis.close();
        } catch (IOException e) {
            System.out.println("Save info não encontrada, usando padrão vazio");
            saveStateExists = false;
            saveStateName = "";
        }
    }
}