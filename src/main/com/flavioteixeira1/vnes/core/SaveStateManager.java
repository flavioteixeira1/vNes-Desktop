package com.flavioteixeira1.vnes;

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

    

    private String getRomBaseNameSafe() {
        try {
            if (nes == null) {
                System.out.println("SaveStateManager: nes == null");
                return "unknown";
            }

            // 1) Tentar pelo objeto ROM exposto (método existente ROM.getFileName())
            ROM rom = nes.getRom();
            if (rom != null) {
                try {
                    String romFile = rom.getFileName();
                    System.out.println("SaveStateManager: rom.getFileName() -> " + romFile);
                    if (romFile != null && !romFile.trim().isEmpty()) {
                        int idx = romFile.lastIndexOf('.');
                        String base = idx > 0 ? romFile.substring(0, idx) : romFile;
                        base = base.replaceAll("[^a-zA-Z0-9._-]", "_");
                        return base;
                    }
                } catch (Exception e) {
                    System.out.println("SaveStateManager: rom.getFileName() lançou exceção: " + e.getMessage());
                }
            } else {
                System.out.println("SaveStateManager: nes.getRom() == null");
            }

            // 2) Tentar pelo caminho armazenado no NES (romFile), usando getter seguro
            try {
                String romFilePath = nes.getRomFilePath();
                System.out.println("SaveStateManager: nes.getRomFilePath() -> " + romFilePath);
                if (romFilePath != null && !romFilePath.trim().isEmpty()) {
                    java.io.File f = new java.io.File(romFilePath);
                    String name = f.getName();
                    if (name != null && !name.trim().isEmpty()) {
                        int idx = name.lastIndexOf('.');
                        String base = idx > 0 ? name.substring(0, idx) : name;
                        base = base.replaceAll("[^a-zA-Z0-9._-]", "_");
                        return base;
                    }
                }
            } catch (Exception e) {
                System.out.println("SaveStateManager: nes.getRomFilePath() lançou exceção: " + e.getMessage());
            }

            // 3) Tentar método auxiliar do NES (getRomFileNameSafe) se implementado
            try {
                String nameSafe = nes.getRomFileNameSafe();
                System.out.println("SaveStateManager: nes.getRomFileNameSafe() -> " + nameSafe);
                if (nameSafe != null && !nameSafe.trim().isEmpty()) {
                    int idx = nameSafe.lastIndexOf('.');
                    String base = idx > 0 ? nameSafe.substring(0, idx) : nameSafe;
                    base = base.replaceAll("[^a-zA-Z0-9._-]", "_");
                    return base;
                }
            } catch (Exception e) {
                // ignore
            }

            // fallback
            System.out.println("SaveStateManager: não conseguiu obter nome da ROM, retornando 'unknown'");
            return "unknown";
        } catch (Exception e) {
            System.out.println("SaveStateManager: exceção em getRomBaseNameSafe: " + e.getMessage());
            return "unknown";
        }
    }



    private String getRomBaseName() {
        if (nes.getRom().getFileName() == null) return "unknown";
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
        if (nes == null) {
            System.err.println("SaveStateManager: NES é null, não é possível salvar.");
            return false;
        }
        String romBase = getRomBaseNameSafe();
        String fileName = "savestate_" + romBase + ".dat";
        System.out.println(romBase);
        try {
            
            FileOutputStream fos = new FileOutputStream(fileName);

            ByteBuffer buf = new ByteBuffer(1024 * 1024, ByteBuffer.BO_LITTLE_ENDIAN); // 1MB buffer
            
            nes.stateSave(buf);

            // Metadados
            buf.putString(customName != null ? customName : getRomBaseNameSafe());
            buf.putLong(System.currentTimeMillis());

            byte[] data = buf.getBytes();
            fos.write(data, 0, buf.getPos());
            fos.close();

            saveStateName = customName != null ? customName : getRomBaseNameSafe();
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
        if (nes == null) {
            System.err.println("SaveStateManager: NES é null, não é possível carregar.");
            return false;
        }

        String romBase = getRomBaseNameSafe();
        String fileName = "savestate_" + romBase + ".dat";
        
        try {
            
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
                    System.out.println("Save state carregado: " + saveStateName + " (gravado em " + timestamp + ")");
                } catch (Exception e) {
                    System.out.println("Save state carregado (metadados ausentes)");
                }
                // ----- Diagnóstico extra: inspecionar PC e memória ao redor -----
    try {
        CPU cpu = nes.getCpu();
        int pc = -1;
        if (cpu != null) {
            try {
                pc = cpu.getPC();
            } catch (Throwable t) {
                pc = -1;
            }
        }
        System.out.println("DEBUG: CPU.getPC() -> " + pc);

        if (pc >= 0) {
            // Mostrar 16 bytes a partir do PC
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("DEBUG: mem[@%04X..+15]:", pc));
            for (int i = 0; i < 16; i++) {
                int addr = (pc + i) & 0xFFFF;
                int b = nes.getMemoryByte(addr);
                sb.append(String.format(" %02X", b >= 0 ? b : 0));
            }
            System.out.println(sb.toString());

            // Verificar se o opcode em PC é conhecido (usa CpuInfo)
                        try {
                            int opcode = nes.getMemoryByte(pc);
                            int[] opdata = CpuInfo.getOpData(); // retorna tabela de opcodes
                            boolean valid = false;
                            if (opcode >= 0 && opcode < 256 && opdata != null) {
                                valid = (opdata[opcode] != 0xFF);
                            }
                            System.out.println(String.format("DEBUG: opcode at PC = 0x%02X (%s)", opcode,
                                    valid ? "valid" : "INVALID"));
                        } catch (Throwable t) {
                            System.out.println("DEBUG: não foi possível verificar opcode (ex: classe CpuInfo inacessível)");
                        }
                    } else {
                        System.out.println("DEBUG: PC inválido, não é possível inspecionar memória.");
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao rodar diagnósticos pós-load: " + e.getMessage());
                    e.printStackTrace();
                }
                // ----- fim do diagnóstico -----
                // Forçar atualização da UI / PPU
                try {
                        // Se a UI estiver disponível, forçar repaint imediato
                        UI ui = nes.getGui();
                        if (ui != null) {
                            System.out.println("SaveStateManager: notificando GUI para redraw.");
                            // método genérico: se existir um método para notificar imagem pronta, usá-lo.
                            // Aqui usamos imageReady se estiver disponível; caso contrário, tente repaint na view.
                            try {
                                ui.getScreenView().repaint();
                            } catch (Exception e) {
                                // fallback: tentar notificar via método genérico
                                try {
                                    ui.getNES().getGui().imageReady(true);
                                } catch (Exception ignore) {}
                            }
                        } else {
                            System.out.println("SaveStateManager: UI nula, não é possível forçar repaint.");
                        }
                    } catch (Exception e) {
                        System.err.println("Erro ao forçar redraw após loadState: " + e.getMessage());
                        e.printStackTrace();
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
            String fileName = "savestate_" + getRomBaseNameSafe() + ".dat";
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