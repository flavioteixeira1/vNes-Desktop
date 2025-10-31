import java.awt.event.*;

import javax.sound.sampled.SourceDataLine;

public class UIApp implements UI{
	
	RockmanForm jogo;
    NES nes;
	KbInputHandler kbJoy1;
	KbInputHandler kbJoy2;
	ScreenView vScreen;
	HiResTimer timer;
	
	long t1,t2;
	int sleepTime;
	
	public UIApp(RockmanForm jogo){
		timer = new HiResTimer();
		this.jogo = jogo;
	}
	
	public void initNES() {
		System.out.println("UIApp.initNES() - Criando nova instância do NES");
		nes = new NES(this);
		
		// Inicializar componentes visuais se ainda não existirem
		if (vScreen == null) {
			initVisualComponents();
		} else {
			// Atualizar referência do NES no ScreenView
			vScreen.updateNESReference(nes);
		}
	}
	
	public void initVisualComponents() {
		System.out.println("UIApp.initVisualComponents() - Inicializando componentes visuais");
		
		if (vScreen == null) {
			vScreen = new ScreenView(nes, 256, 240);
			vScreen.setBgColor(jogo.bgColor.getRGB());
			vScreen.init();
			vScreen.setNotifyImageReady(true);
		}
		
		if (kbJoy1 == null) {
			kbJoy1 = new KbInputHandler(nes, 0);
			// Map keyboard input keys for joypad 1:
			kbJoy1.mapKey(InputHandler.KEY_A, KeyEvent.VK_X);
			kbJoy1.mapKey(InputHandler.KEY_B, KeyEvent.VK_Z);
			kbJoy1.mapKey(InputHandler.KEY_START, KeyEvent.VK_ENTER);
			kbJoy1.mapKey(InputHandler.KEY_SELECT, KeyEvent.VK_CONTROL);
			kbJoy1.mapKey(InputHandler.KEY_UP, KeyEvent.VK_UP);
			kbJoy1.mapKey(InputHandler.KEY_DOWN, KeyEvent.VK_DOWN);
			kbJoy1.mapKey(InputHandler.KEY_LEFT, KeyEvent.VK_LEFT);
			kbJoy1.mapKey(InputHandler.KEY_RIGHT, KeyEvent.VK_RIGHT);
			
			if (vScreen != null) {
				vScreen.addKeyListener(kbJoy1);
			}
		}
		
		if (kbJoy2 == null) {
			kbJoy2 = new KbInputHandler(nes, 1);
			// Map keyboard input keys for joypad 2:
			kbJoy2.mapKey(InputHandler.KEY_A, KeyEvent.VK_NUMPAD7);
			kbJoy2.mapKey(InputHandler.KEY_B, KeyEvent.VK_NUMPAD9);
			kbJoy2.mapKey(InputHandler.KEY_START, KeyEvent.VK_NUMPAD1);
			kbJoy2.mapKey(InputHandler.KEY_SELECT, KeyEvent.VK_NUMPAD3);
			kbJoy2.mapKey(InputHandler.KEY_UP, KeyEvent.VK_NUMPAD8);
			kbJoy2.mapKey(InputHandler.KEY_DOWN, KeyEvent.VK_NUMPAD2);
			kbJoy2.mapKey(InputHandler.KEY_LEFT, KeyEvent.VK_NUMPAD4);
			kbJoy2.mapKey(InputHandler.KEY_RIGHT, KeyEvent.VK_NUMPAD6);
			
			if (vScreen != null) {
				vScreen.addKeyListener(kbJoy2);
			}
		}
	}
	
	public void init(boolean showGui){
		// Criar primeira instância do NES
		initNES();
	}
	
	public void imageReady(boolean skipFrame){
		if (vScreen != null) {
			vScreen.repaint();
		}
		
		// Sound stuff:
		if (nes != null && nes.getPapu() != null) {
			int tmp = nes.getPapu().getBufferIndex();
			if(Globals.enableSound && Globals.timeEmulation && tmp > 0){
				int min_avail = nes.getPapu().line.getBufferSize() - 4 * tmp;
				timer.sleepMicros(nes.papu.getMillisToAvailableAbove(min_avail));
				while(nes.getPapu().line.available() < min_avail){
					timer.yield();
				}
				nes.getPapu().writeBuffer();
			}
		}
		
		// Sleep a bit if sound is disabled:
		if(Globals.timeEmulation && !Globals.enableSound){
			sleepTime = Globals.frameTime;
			if((t2 = timer.currentMicros()) - t1 < sleepTime){
				timer.sleepMicros(sleepTime - (t2 - t1));
			}
		}

		// Update timer:
		t1 = t2;
	}

	public void imageReady(BufferView image) {
		if (image == null) {
			System.err.println("UI.imageReady() - Imagem null recebida");
			return; 
		}
	}
	
	public int getRomFileSize(){
		return jogo.romSize;
	}
	
	public void showLoadProgress(int percentComplete){
		// Show ROM load progress:
		jogo.showLoadProgress(percentComplete);
		
		// Sleep a bit:
		timer.sleepMicros(20 * 1000);
	}
	
	public void destroy(){
		// Parar emulação primeiro
		if (nes != null) {
			nes.stopEmulation();
		}
		
		if(vScreen != null) {
			vScreen.destroy();
			vScreen = null;
		}
		if(kbJoy1 != null) {
			kbJoy1.destroy();
			kbJoy1 = null;
		}
		if(kbJoy2 != null) {
			kbJoy2.destroy();
			kbJoy2 = null;
		}
		
		if (nes != null) {
			nes.destroy();
			nes = null;
		}
		
		jogo = null;
		timer = null;
	}
	
	public void loadNewRom(String filePath) {
		System.out.println("UIApp.loadNewRom() - Carregando nova ROM: " + filePath);
		// 1. Primeiro, remover listeners e referências
		if (vScreen != null) {
			System.out.println("Removendo listeners do ScreenView...");
			if (kbJoy1 != null) {
				vScreen.removeKeyListener(kbJoy1);
			}
			if (kbJoy2 != null) {
				vScreen.removeKeyListener(kbJoy2);
			}
        // Não destruir o vScreen, apenas limpar referências
        vScreen.updateNESReference(null); 
    	}
		// 2.Parar emulação atual se estiver rodando
		if (nes != null && nes.isRunning()) {
			System.out.println("Parando emulação atual...");
			nes.stopEmulation();
			
			// Dar tempo para parar completamente
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		
		// 3.Destruir instância anterior do NES
		if (nes != null) {
			System.out.println("Destruindo instância anterior do NES...");
			nes.destroy();
			nes = null;
			
			// Pequeno delay para cleanup
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		
		// 4.Criar nova instância do NES
		initNES();
		// 5. Reconfigurar o ScreenView com o novo NES
			if (vScreen != null) {
			System.out.println("Reconfigurando ScreenView com novo NES...");
			vScreen.updateNESReference(nes);
			
			// Re-adicionar listeners
			if (kbJoy1 != null) {
				vScreen.addKeyListener(kbJoy1);
			}
			if (kbJoy2 != null) {
				vScreen.addKeyListener(kbJoy2);
			}
   		 }
		// 6.Carregar ROM
		boolean success = nes.loadRom(filePath);
		if (success) {
			System.out.println("ROM carregada com sucesso, iniciando emulação...");
			//nes.startEmulation();
		    // NÃO iniciar emulação aqui - deixe o RockmanForm fazer isso depois de adicionar a tela
		} else {
			System.err.println("Falha ao carregar ROM: " + filePath);
		}
	}
	
	// Método para notificar que a ROM mudou
	public void romChanged() {
		System.out.println("UIApp.romChanged() - ROM foi alterada");
		// Atualizar referências se necessário
		if (vScreen != null && nes != null) {
			vScreen.updateNESReference(nes);
		}
	}

	public NES getNES(){
		return nes;
	}
	
	public InputHandler getJoy1(){
		return kbJoy1;
	}
	
	public InputHandler getJoy2(){
		return kbJoy2;
	}
	
	public BufferView getScreenView(){
		return vScreen;
	}
	
	public BufferView getPatternView(){
		return null;
	}
	
	public BufferView getSprPalView(){
		return null;
	}
	
	public BufferView getNameTableView(){
		return null;
	}
	
	public BufferView getImgPalView(){
		return null;
	}
	
	public HiResTimer getTimer(){
		return timer;
	}
	
	public String getWindowCaption(){
		return "";
	}
	
	public void setWindowCaption(String s){}
	
	public void setTitle(String s){}
	
	public java.awt.Point getLocation(){
		return new java.awt.Point(0,0);
	}
	
	public int getWidth(){
		return jogo.getWidth();
	}
	
	public int getHeight(){
		return jogo.getHeight();
	}
	
	public void println(String s){}
	
	public void showErrorMsg(String msg){
		System.out.println(msg);
	}
}