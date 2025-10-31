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
		return rom;
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
    
    public boolean saveState(int slot, String name) {
        return saveStateManager.saveState(slot, name);
    }
    
    public boolean loadState(int slot) {
        return saveStateManager.loadState(slot);
    }
	


	public boolean stateLoad(ByteBuffer buf) {
			boolean continueEmulation = false;
			boolean success;
			
			// Pausar emulação
			if(cpu.isRunning()) {
				continueEmulation = true;
				stopEmulation();
			}
			
			// Verificar versão
			if(buf.readByte() == 1) {
				try {
					// Carregar estado de todas as unidades
					cpuMem.stateLoad(buf);
					ppuMem.stateLoad(buf);
					sprMem.stateLoad(buf);
					cpu.stateLoad(buf);
					
					// IMPORTANTE: Carregar mapper antes da PPU
					if(memMapper != null) {
						memMapper.stateLoad(buf);
					}
					
					ppu.stateLoad(buf);
					
					// Carregar paleta
					if(palTable != null) {
						palTable.stateLoad(buf);
					}
					
					// Carregar áudio
					if(papu != null) {
						papu.stateLoad(buf);
					}
					
					success = true;
					
				} catch (Exception e) {
					System.err.println("Erro ao carregar estado: " + e.getMessage());
					e.printStackTrace();
					success = false;
				}
			} else {
				System.out.println("State file has wrong format.");
				success = false;
			}
			
			// Reiniciar emulação se estava rodando
			if(continueEmulation) {
				startEmulation();
			}
			
			return success;
		}


		public boolean isRunning() {
			return isRunning;
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