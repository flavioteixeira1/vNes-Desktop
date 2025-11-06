import java.io.*;

public class NES{
	
	public UI gui;
	public CPU cpu;
	public PPU ppu;
	public PAPU papu;
	public Memory cpuMem;
	public Memory ppuMem;
	public Memory sprMem;
	public MemoryMapper memMapper;
	public PaletteTable palTable;
	public ROM rom;
	public GameGenie gameGenie;
	int cc;
	private SaveStateManager saveStateManager;
	public String romFile;
	boolean isRunning = false;
	
	// Creates the NES system.
	public NES(UI gui){
		
		Globals.nes = this;
		this.gui = gui;
		this.saveStateManager = new SaveStateManager(this);
		
		// Create memory:
		cpuMem = new Memory(this,0x10000);	// Main memory (internal to CPU)
		ppuMem = new Memory(this,0x8000);	// VRAM memory (internal to PPU)
		sprMem = new Memory(this,0x100);	// Sprite RAM  (internal to PPU)
		
		
		// Create system units:
		cpu = new CPU(this);
		palTable = new PaletteTable();
		ppu = new PPU(this);
		papu = new PAPU(this);
		gameGenie = new GameGenie();
		
		// Init sound registers:
		for(int i=0;i<0x14;i++){
			if(i==0x10){
				papu.writeReg(0x4010,(short)0x10);
			}else{
				papu.writeReg(0x4000+i,(short)0);
			}
		}
		
		// Load NTSC palette:
		if(!palTable.loadNTSCPalette()){
			//System.out.println("Unable to load palette file. Using default.");
			palTable.loadDefaultPalette();
		}
		
		// Initialize units:
		cpu.init();
		ppu.init();
		
		// Enable sound:
		enableSound(true);
		
		// Clear CPU memory:
		clearCPUMemory();
		
	}

	//Return ROM name

	public String getRomName(){
		return this.romFile;
	}

	// Returns CPU object.
	public CPU getCpu(){
		return cpu;
	}
	
	
	// Returns PPU object.
	public PPU getPpu(){
		return ppu;
	}
	
	
	// Returns pAPU object.
	public PAPU getPapu(){
		return papu;
	}
	
	
	// Returns CPU Memory.
	public Memory getCpuMemory(){
		return cpuMem;
	}
	
	
	// Returns PPU Memory.
	public Memory getPpuMemory(){
		return ppuMem;
	}
	
	
	// Returns Sprite Memory.
	public Memory getSprMemory(){
		return sprMem;
	}
	
	
	// Returns the currently loaded ROM.
	public ROM getRom(){
		return this.rom;
	}


	public String getRomFilePath() {
        try {
            return this.romFile;
        } catch (Exception e) {
            return null;
        }
    }

	public String getRomFileNameSafe() {
        try {
            if (this.rom != null) {
                try {
                    String rn = this.rom.getFileName();
                    if (rn != null && !rn.trim().isEmpty()) {
                        return rn;
                    }
                } catch (Exception ignored) { }
            }
            if (this.romFile != null && !this.romFile.trim().isEmpty()) {
                java.io.File f = new java.io.File(this.romFile);
                String n = f.getName();
                if (n != null && !n.trim().isEmpty()) return n;
            }
        } catch (Exception ignored) {}
        return null;
    }
	
	
	// Returns the GUI.
	public UI getGui(){
		return gui;
	}
	
	
	// Returns the memory mapper.
	public MemoryMapper getMemoryMapper(){
		return memMapper;
	}
	
	// Returns the Game Genie:
	public GameGenie getGameGenie(){
		return gameGenie;
	}
	
	

	 public void setRom(ROM rom) {
        this.rom = rom;
    }
	

	 public SaveStateManager getSaveStateManager() {
        return saveStateManager;
    }
    
    public boolean saveState(String name) {
		
        return saveStateManager.saveState(name);
    }
    
    public boolean loadState() {
        return saveStateManager.loadState();
    }
	

public boolean stateLoad(ByteBuffer buf) {
				boolean continueEmulation = false;
				boolean success;

				// Validações iniciais para evitar NullPointer
				if (cpu == null || cpuMem == null || ppu == null || ppuMem == null || sprMem == null) {
					System.err.println("NES.stateLoad: componentes críticos nulos, abortando load.");
					return false;
				}

				// Pausar emulação
				if (cpu.isRunning()) {
					continueEmulation = true;
					stopEmulation();
				}

				try {
					byte version = (byte) buf.readByte();
					System.out.println("NES.stateLoad: versão do state = " + version);

					if (version != 1) {
						System.out.println("State file has wrong format.");
						return false;
					}

					// 1) Carregar mapper primeiro (se existir) — importante para bancos/PPU
					try {
						if (memMapper != null) {
							System.out.println("NES.stateLoad: carregando estado do mapper...");
							memMapper.stateLoad(buf);
						} else {
							System.out.println("NES.stateLoad: memMapper == null");
						}
					} catch (Exception e) {
						System.err.println("Erro ao carregar estado do mapper: " + e.getMessage());
						e.printStackTrace();
						// continuar para tentar diagnóstico, mas considerar failure
					}

					// 2) Carregar memórias e CPU
					try {
						System.out.println("NES.stateLoad: carregando cpuMem...");
						cpuMem.stateLoad(buf);

						System.out.println("NES.stateLoad: carregando ppuMem...");
						ppuMem.stateLoad(buf);

						System.out.println("NES.stateLoad: carregando sprMem...");
						sprMem.stateLoad(buf);

						System.out.println("NES.stateLoad: carregando cpu...");
						cpu.stateLoad(buf);
					} catch (Exception e) {
						System.err.println("Erro ao carregar memória/CPU: " + e.getMessage());
						e.printStackTrace();
						if (continueEmulation) startEmulation();
						return false;
					}

					// 3) Carregar PPU depois que mapper e memórias estiverem consistentes
					try {
						System.out.println("NES.stateLoad: carregando PPU...");
						ppu.stateLoad(buf);
					} catch (Exception e) {
						System.err.println("Erro ao carregar PPU: " + e.getMessage());
						e.printStackTrace();
						if (continueEmulation) startEmulation();
						return false;
					}

					// 4) Paleta e áudio
					try {
						if (palTable != null) {
							System.out.println("NES.stateLoad: carregando palTable...");
							palTable.stateLoad(buf);
						}
						if (papu != null) {
							System.out.println("NES.stateLoad: carregando PAPU...");
							papu.stateLoad(buf);
						}
					} catch (Exception e) {
						System.err.println("Erro ao carregar paleta/áudio: " + e.getMessage());
						e.printStackTrace();
					}

					success = true;

				} catch (Exception e) {
					System.err.println("Erro ao carregar estado (geral): " + e.getMessage());
					e.printStackTrace();
					success = false;
				}

				// Reiniciar emulação se estava rodando
				if (continueEmulation && success) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
					}
					startEmulation();
				} else if (continueEmulation && !success) {
					System.out.println("NES.stateLoad: falha no load; emulação não será reiniciada.");
				}

				return success;
			}
				


	public void stateSave(ByteBuffer buf) {
			boolean continueEmulation = isRunning();
			stopEmulation();
			
			try {
				// Versão
				buf.putByte((short)1);
				
				// Salvar estado de todas as unidades
				cpuMem.stateSave(buf);
				ppuMem.stateSave(buf);
				sprMem.stateSave(buf);
				cpu.stateSave(buf);
				
				if(memMapper != null) {
					memMapper.stateSave(buf);
				}
				
				ppu.stateSave(buf);
				
				// Salvar paleta
				if(palTable != null) {
					palTable.stateSave(buf);
				}
				
				// Salvar áudio
				if(papu != null) {
					papu.stateSave(buf);
				}
				
			} catch (Exception e) {
				System.err.println("Erro ao salvar estado: " + e.getMessage());
				e.printStackTrace();
			}
			
			// Continuar emulação se estava rodando
			if(continueEmulation) {
				startEmulation();
			}
		}


	
	
	public boolean isRunning() {
		// TODO Auto-generated method stub
		//throw new UnsupportedOperationException("Unimplemented method 'isRunning'");
		if (this.gui != null){
			return true;
		} else 
		return false;
	}

	public void startEmulation(){
		System.out.println("NES.startEmulation() - Iniciando emulação...");
		 // Verificar se temos uma ROM válida
			if (rom == null || !rom.isValid()) {
				System.err.println("Não é possível iniciar emulação: ROM não carregada ou inválida");
				return;
			}
		 // Verificar se o mapper foi criado
			if (memMapper == null) {
				System.err.println("Não é possível iniciar emulação: Mapper não criado");
				return;
			}
		// INICIALIZAR ÁUDIO APENAS UMA VEZ, no início da emulação
			if(Globals.enableSound && !papu.isRunning()){
				System.out.println("Iniciando sistema de áudio para emulação...");
				papu.start();
				// Pequena pausa para garantir inicialização
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			
			if(rom != null && rom.isValid() && !cpu.isRunning()){
				//System.out.println("Iniciando CPU...");
				cpu.beginExecution();
				isRunning = true;
				
			}
			//System.out.println("Emulação iniciada: " + isRunning);	
	}
	
	public void stopEmulation(){
		//System.out.println("NES.stopEmulation() - Parando emulação...");
		if(cpu != null && cpu.isRunning()) {
        //System.out.println("Parando CPU...");
        cpu.endExecution();
        isRunning = false;
        
        // Esperar a thread da CPU terminar
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            System.err.println("Interrupção ao parar CPU: " + e.getMessage());
			 Thread.currentThread().interrupt();
        }
    }
		
		if (Globals.enableSound && papu != null && papu.isRunning()) {
        System.out.println("Parando áudio...");
        papu.stop();
        
        // Pequeno delay para áudio parar
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        	}
    	}
		 System.out.println("Emulação parada.");
	}
	
	public void reloadRom(){
		
		if(romFile != null){
			loadRom(romFile);
		}
		
	}
	
	public void clearCPUMemory(){
		
		short flushval = Globals.memoryFlushValue;
		for(int i=0;i<0x2000;i++){
			cpuMem.mem[i] = flushval;
		}
		for(int p=0;p<4;p++){
			int i = p*0x800;
			cpuMem.mem[i+0x008] = 0xF7;
			cpuMem.mem[i+0x009] = 0xEF;
			cpuMem.mem[i+0x00A] = 0xDF;
			cpuMem.mem[i+0x00F] = 0xBF;
		}
		
	}
	
	public void setGameGenieState(boolean enable){
		if(memMapper!=null){
			memMapper.setGameGenieState(enable);
		}
	}
	
	
	
	// Loads a ROM file into the CPU and PPU.
	// The ROM file is validated first.
	public boolean loadRom(String file){

		 System.out.println("=== INICIANDO CARREGAMENTO DA ROM ===");
    	 //System.out.println("Arquivo: " + file);
		
		// Can't load ROM while still running.
		if(isRunning){
			System.out.println("Parando emulação atual...");
			stopEmulation();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		// Fechar ROM atual se existir
   		 if(rom != null){
        rom.closeRom();
		rom = null;
    	}

		  // Resetar mapper se existir
   		 if(memMapper != null){
       	  memMapper.reset();
		  memMapper = null;
  		  }

		 // GARANTIR que a PPU está inicializada
		if (ppu != null) {
			System.out.println("Garantindo inicialização da PPU...");
			ppu.init();
		}
		
		{
			// Load ROM file:
		//System.out.println("Criando objeto ROM...");
		rom = new ROM(this);
		//System.out.println("Carregando arquivo...");
		rom.load(file);
		if(rom.isValid()){

			System.out.println("ROM válida detectada!");
            System.out.println("Tipo do Mapper: " + rom.getMapperType());
            System.out.println("Mirroring: " + rom.getMirroringType());
            System.out.println("Tamanho PRG ROM: " + rom.getRomBankCount() + " bancos de 16KB");
            System.out.println("Tamanho CHR ROM: " + rom.getVromBankCount() + " bancos de 8KB");
				
				// The CPU will load
				// the ROM into the CPU
				// and PPU memory.
			  System.out.println("Resetando sistema...");
			reset();
				
			memMapper = rom.createMapper();
			System.out.println("Mapper criado: " + memMapper.getClass().getSimpleName());
			memMapper.init(this);
			cpu.setMapper(memMapper);
			memMapper.loadROM(rom);
			ppu.setMirroring(rom.getMirroringType());
			
			if(gameGenie.getCodeCount()>0){
				memMapper.setGameGenieState(true);
				}
				
				this.romFile = file;
				 System.out.println("=== ROM CARREGADA COM SUCESSO ===");
				
			}
			else{
				System.out.println("=== ERRO: ROM INVÁLIDA ===");
            	System.out.println("Status da ROM: " + rom.getFileName());
			}
			return rom.isValid();
		}



		
	}
	
	// Resets the system.
	public void reset() {
    System.out.println("NES.reset() - Resetando sistema...");

	 stopEmulation();

	 try {
        Thread.sleep(50);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }

	System.out.println("Resetando memórias...");
    if (cpuMem != null) cpuMem.reset();
    if (ppuMem != null) ppuMem.reset();
    if (sprMem != null) sprMem.reset();
    
    clearCPUMemory();
    
    if(rom != null){
        System.out.println("Fechando ROM atual...");
        rom.closeRom();
    }
    if(memMapper != null){
        System.out.println("Resetando mapper...");
        memMapper.reset();
    }
    
    System.out.println("Resetando memórias...");
    cpuMem.reset();
    ppuMem.reset();
    sprMem.reset();
    
    clearCPUMemory();
    
    System.out.println("Resetando componentes...");
    if (cpu != null) cpu.reset();
    if (cpu != null) cpu.init();
    if (ppu != null) ppu.reset();
	System.out.println("Reinicializando PPU...");
    ppu.reset();
    ppu.init(); 
    if (palTable != null) palTable.reset();    
    if (papu != null) papu.reset();
    
    InputHandler joy1 = gui.getJoy1();
    if(joy1 != null){
        joy1.reset();
    }
    
    //System.out.println("Reset completo.");
}
	
	
	// Enable or disable sound playback.
	public void enableSound(boolean enable){
		
		 // FORÇAR desabilitado se enable for false
        if (!enable) {
            if(papu.isRunning()){
                papu.stop();
            }
            Globals.enableSound = false;
            System.out.println("Áudio FORÇADO: DESABILITADO");
            return;
        }
        
        boolean wasRunning = isRunning();
        if(wasRunning){
            stopEmulation();
        }
        
        if(enable){
            papu.start();
        }else{
            papu.stop();
        }
        
        Globals.enableSound = enable;
        
        if(wasRunning){
            startEmulation();
        }
		
	}
	
	public void setFramerate(int rate){
		
		Globals.preferredFrameRate = rate;
		Globals.frameTime = 1000000/rate;
		papu.setSampleRate(papu.getSampleRate(),false);
		
	}
	
	public void destroy(){
		
		if(cpu!=null)cpu.destroy();
		if(ppu!=null)ppu.destroy();
		if(papu!=null)papu.destroy();
		if(cpuMem!=null)cpuMem.destroy();
		if(ppuMem!=null)ppuMem.destroy();
		if(sprMem!=null)sprMem.destroy();
		if(memMapper!=null)memMapper.destroy();
		if(rom!=null)rom.destroy();
		
		gui = null;
		cpu = null;
		ppu = null;
		papu = null;
		cpuMem = null;
		ppuMem = null;
		sprMem = null;
		memMapper = null;
		rom = null;
		gameGenie = null;
		palTable = null;
		
	}
	
}