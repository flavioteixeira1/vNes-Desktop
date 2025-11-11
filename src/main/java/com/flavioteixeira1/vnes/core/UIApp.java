package com.flavioteixeira1.vnes.core;

import java.awt.event.*;

import javax.sound.sampled.SourceDataLine;

public class UIApp implements UI{
	
	RockmanForm jogo;
    NES nes;
	KbInputHandler kbJoy1;
	KbInputHandler kbJoy2;
	JoystickInputHandler joystickInputHandler;
	ScreenView vScreen;
	HiResTimer timer;
	private InputManager inputManager;
	
	long t1,t2;
	int sleepTime;
	
	public UIApp(RockmanForm jogo){
		timer = new HiResTimer();
		this.jogo = jogo;
	}
	
	public void initNES() {
		System.out.println("UIApp.initNES() - Criando nova instância do NES");
		nes = new NES(this);

		// Inicializar gerenciador de inputs
		inputManager = new InputManager(nes);

		// Configuração padrão: Player 1 = Teclado
        setupDefaultInputs();

		// Se os KbInputHandlers já existirem, atualize a referência do NES neles
		if (kbJoy1 != null) {
			kbJoy1.setNES(nes);
		}
		if (kbJoy2 != null) {
			kbJoy2.setNES(nes);
		}
		
		// Inicializar componentes visuais se ainda não existirem
		if (vScreen == null) {
			initVisualComponents();
		} else {
			// Atualizar referência do NES no ScreenView
			vScreen.updateNESReference(nes);
		}
	}

	private void setupDefaultInputs() {
        // Player 1: Teclado (configuração padrão)
        KbInputHandler kbHandler = (KbInputHandler) inputManager.getPlayerHandler(InputManager.PLAYER_1);
        
        // Map keyboard input keys for joypad 1:
        kbHandler.mapKey(InputHandler.KEY_A, KeyEvent.VK_X);
        kbHandler.mapKey(InputHandler.KEY_B, KeyEvent.VK_Z);
        kbHandler.mapKey(InputHandler.KEY_START, KeyEvent.VK_ENTER);
        kbHandler.mapKey(InputHandler.KEY_SELECT, KeyEvent.VK_CONTROL);
        kbHandler.mapKey(InputHandler.KEY_UP, KeyEvent.VK_UP);
        kbHandler.mapKey(InputHandler.KEY_DOWN, KeyEvent.VK_DOWN);
        kbHandler.mapKey(InputHandler.KEY_LEFT, KeyEvent.VK_LEFT);
        kbHandler.mapKey(InputHandler.KEY_RIGHT, KeyEvent.VK_RIGHT);
        
        // Player 2: Teclado numérico
        inputManager.setPlayerHandler(InputManager.PLAYER_2, InputConfig.HANDLER_KEYBOARD, nes);
        KbInputHandler kbHandler2 = (KbInputHandler) inputManager.getPlayerHandler(InputManager.PLAYER_2);
        
        kbHandler2.mapKey(InputHandler.KEY_A, KeyEvent.VK_NUMPAD7);
        kbHandler2.mapKey(InputHandler.KEY_B, KeyEvent.VK_NUMPAD9);
        kbHandler2.mapKey(InputHandler.KEY_START, KeyEvent.VK_NUMPAD1);
        kbHandler2.mapKey(InputHandler.KEY_SELECT, KeyEvent.VK_NUMPAD3);
        kbHandler2.mapKey(InputHandler.KEY_UP, KeyEvent.VK_NUMPAD8);
        kbHandler2.mapKey(InputHandler.KEY_DOWN, KeyEvent.VK_NUMPAD2);
        kbHandler2.mapKey(InputHandler.KEY_LEFT, KeyEvent.VK_NUMPAD4);
        kbHandler2.mapKey(InputHandler.KEY_RIGHT, KeyEvent.VK_NUMPAD6);
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
			InputHandler handler1 = inputManager.getPlayerHandler(InputManager.PLAYER_1);
            InputHandler handler2 = inputManager.getPlayerHandler(InputManager.PLAYER_2);
            
            if (handler1 instanceof KeyListener) {
                vScreen.addKeyListener((KeyListener) handler1);
            }
            if (handler2 instanceof KeyListener) {
                vScreen.addKeyListener((KeyListener) handler2);
            }
        }
			}
	}

	
		
		
	
	public void init(boolean showGui){
		// Criar primeira instância do NES
		initNES();
		initializeInputHandlers();
	}

	public void updateGameControls() {
        if (inputManager != null) {
            inputManager.update();
        }
    }
	

	public boolean isKeyPressed(int keyCode) {
        // Verifica ambos: teclado SEMPRE tem prioridade? Ou ambos funcionam?
        // Depende da sua preferência. Aqui ambos funcionam simultaneamente:
        boolean keyboardPressed = kbJoy1.isKeyPressed(keyCode);
        boolean joystickPressed = joystickInputHandler.isConnected() && 
                                 joystickInputHandler.isKeyPressed(keyCode);
        
        return keyboardPressed || joystickPressed;
    }

	


	public void showJoystickConfig() {
        if (jogo != null) {
            //jogo.JoystickConfigDialog(this.joystickInputHandler);
			
        }
    }

	public JoystickInputHandler getJoystickInputHandler() {
        return joystickInputHandler;
    }



	private void initializeInputHandlers() {
        // Teclado SEMPRE ativo
        kbJoy1 = new KbInputHandler(nes, sleepTime);
        
        // Joystick opcional
        joystickInputHandler = new JoystickInputHandler();
        
        // Configura o frame com listener de teclado
        if (jogo != null) {
            jogo.addKeyListener((KeyListener) kbJoy1);
			jogo.addKeyListener((KeyListener) kbJoy2);
        }
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

			// Atualizar referência do NES nos handlers existentes antes de re-adicionar
			if (kbJoy1 != null) {
				kbJoy1.setNES(nes);
				vScreen.addKeyListener(kbJoy1);
			}
			if (kbJoy2 != null) {
				kbJoy2.setNES(nes);
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
	
	public InputHandler getJoy1() {
        return inputManager != null ? inputManager.getPlayerHandler(InputManager.PLAYER_1) : null;
    }
    
    public InputHandler getJoy2() {
        return inputManager != null ? inputManager.getPlayerHandler(InputManager.PLAYER_2) : null;
    }

	// Novo método para obter o gerenciador de inputs
    public InputManager getInputManager() {
        return inputManager;
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