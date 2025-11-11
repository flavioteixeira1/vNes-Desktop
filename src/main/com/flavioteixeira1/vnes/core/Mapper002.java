package com.flavioteixeira1.vnes.core;

public class Mapper002 extends MapperDefault{

	 private int currentBank = 0;
	
	public void init(NES nes){
		
		super.init(nes);
		
	}
	
	public void write(int address, short value){
		
		if(address < 0x8000){
			
			// Let the base mapper take care of it.
			super.write(address,value);
			
		}else{
			
			// This is a ROM bank select command.
			// Swap in the given ROM bank at 0x8000:
			loadRomBank(value,0x8000);
			
		}
		
	}
	
	public void loadROM(ROM rom){
	
		if(!rom.isValid()){
			//System.out.println("UNROM: Invalid ROM! Unable to load.");
			return;
		}
		
		//System.out.println("UNROM: loading ROM..");
		
		// Load PRG-ROM:
		loadRomBank(0,0x8000);
		loadRomBank(rom.getRomBankCount()-1,0xC000);
		
		// Load CHR-ROM:
		loadCHRROM();
		
		// Do Reset-Interrupt:
		//nes.getCpu().doResetInterrupt();
		nes.getCpu().requestIrq(CPU.IRQ_RESET);
		
	}

	@Override
    public void stateLoad(ByteBuffer buf) {
				try {
				// Tente ler a versão primeiro
				byte mapperVersion = buf.readByte();
				System.out.println("Mapper002.stateLoad: versão do mapper = " + mapperVersion);
				
				if (mapperVersion == 1) {
					// Se versão correta, ler dados normais
					joy1StrobeState = buf.readInt();
					joy2StrobeState = buf.readInt();
					joypadLastWrite = buf.readInt();
				} else {
					// Se versão incorreta, pular os bytes e resetar
					System.out.println("Mapper002.stateLoad: versão incorreta, resetando estado");
					buf.move(11); // Pular 11 bytes (3 ints + 1 byte já lido)
					reset();
				}
				
				// Ler dados específicos do Mapper002
				currentBank = buf.readInt();
				System.out.println("Mapper002.stateLoad: currentBank = " + currentBank);
				
				// Reaplicar bancos
				loadRomBank(currentBank, 0x8000);
				loadRomBank(rom.getRomBankCount() - 1, 0xC000);
				
			} catch (Exception e) {
				System.err.println("Mapper002.stateLoad: erro crítico, resetando: " + e.getMessage());
				reset();
			}
        
    }
    
    @Override
    public void stateSave(ByteBuffer buf) {
        // Versão do mapper
		buf.putByte((byte)1);
		
		// Dados da classe base
		buf.putInt(joy1StrobeState);
		buf.putInt(joy2StrobeState);
		buf.putInt(joypadLastWrite);
		
		// Dados específicos do Mapper002
		buf.putInt(currentBank);
		System.out.println("Mapper002.stateSave: currentBank = " + currentBank);
    }

	
}