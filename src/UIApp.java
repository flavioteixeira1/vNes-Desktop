import java.awt.event.*;

import javax.sound.sampled.SourceDataLine;

public class UIApp implements UI{
	
	
	private static final int TARGET_FPS = 60;
    private static final long NANOS_PER_FRAME = 1000000000 / TARGET_FPS;
    private long lastFrameTime = System.nanoTime();
	private boolean running = false;
	private int frameCount = 0;
	private long lastFPSTime = System.currentTimeMillis();
	private int fps = 0;
	
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
		nes = new NES(this);
                
	
	}

private void renderScreen() {
        // CORREÇÃO: Implementar renderização básica
        if (vScreen != null) {
            vScreen.repaint();
        }
    }


 private boolean isRomLoaded() {
        return nes != null && nes.getRom() != null;
    }


 private void stopEmulation() {
        // CORREÇÃO: Usar a instância 'nes' em vez da classe 'NES'
        if (nes != null) {
            nes.stopEmulation();
        }
        running = false;
    }

	
	public void init(boolean showGui){
		
		vScreen = new ScreenView(nes,256,240);
		vScreen.setBgColor(jogo.bgColor.getRGB());
		vScreen.init();
		vScreen.setNotifyImageReady(true);
		
		kbJoy1 = new KbInputHandler(nes,0);
		kbJoy2 = new KbInputHandler(nes,1);
		
		// Map keyboard input keys for joypad 1:
		kbJoy1.mapKey(InputHandler.KEY_A,KeyEvent.VK_X);
		kbJoy1.mapKey(InputHandler.KEY_B,KeyEvent.VK_Z);
		kbJoy1.mapKey(InputHandler.KEY_START,KeyEvent.VK_ENTER);
		kbJoy1.mapKey(InputHandler.KEY_SELECT,KeyEvent.VK_CONTROL);
		kbJoy1.mapKey(InputHandler.KEY_UP,KeyEvent.VK_UP);
		kbJoy1.mapKey(InputHandler.KEY_DOWN,KeyEvent.VK_DOWN);
		kbJoy1.mapKey(InputHandler.KEY_LEFT,KeyEvent.VK_LEFT);
		kbJoy1.mapKey(InputHandler.KEY_RIGHT,KeyEvent.VK_RIGHT);
		vScreen.addKeyListener(kbJoy1);

		// Map keyboard input keys for joypad 2:
		kbJoy2.mapKey(InputHandler.KEY_A,KeyEvent.VK_NUMPAD7);
		kbJoy2.mapKey(InputHandler.KEY_B,KeyEvent.VK_NUMPAD9);
		kbJoy2.mapKey(InputHandler.KEY_START,KeyEvent.VK_NUMPAD1);
		kbJoy2.mapKey(InputHandler.KEY_SELECT,KeyEvent.VK_NUMPAD3);
		kbJoy2.mapKey(InputHandler.KEY_UP,KeyEvent.VK_NUMPAD8);
		kbJoy2.mapKey(InputHandler.KEY_DOWN,KeyEvent.VK_NUMPAD2);
		kbJoy2.mapKey(InputHandler.KEY_LEFT,KeyEvent.VK_NUMPAD4);
		kbJoy2.mapKey(InputHandler.KEY_RIGHT,KeyEvent.VK_NUMPAD6);
		vScreen.addKeyListener(kbJoy2);
		
	}
	 
	
	 public void imageReady() {
        // CORREÇÃO: Método completamente reescrito
        try {
            // Verifica se a ROM está carregada e se a emulação está rodando
            if (isRomLoaded() && running) {
                
                // CORREÇÃO: Usar a instância 'nes' em vez da classe 'NES'
                SourceDataLine audioLine = nes.getPapu().getLine();
                
                if (audioLine != null) {
                    try {
                        int bufSize = audioLine.getBufferSize();
                        int available = bufSize - audioLine.available();
                        
                        if (((float) available / (float) bufSize) > 0.99f) {
                            Thread.sleep(1);
                        }
                    } catch (Exception e) {
                        System.err.println("Audio buffer error: " + e.getMessage());
                    }
                }
                
                // Render the screen
                renderScreen();
                
                // Update FPS counter
                //updateFPS();
            }
        } catch (Exception e) {
            System.err.println("Error in imageReady: " + e.getMessage());
            e.printStackTrace();
        }
    }

private void updateFPS() {
        frameCount++;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFPSTime >= 1000) {
            fps = frameCount;
            frameCount = 0;
            lastFPSTime = currentTime;
            
             // Log apenas se estiver significativamente diferente de 60
            if (fps < 55 || fps > 65) {
                System.out.println("🎮 FPS: " + fps + " (alvo: 60) - " + 
                                 (fps < 60 ? "LENTO" : "RÁPIDO"));
            }
        }
    }

	
	public int getRomFileSize(){
		return jogo.romSize;
	}
	
	public void showLoadProgress(int percentComplete){
		
		// Show ROM load progress:
		//jogo.showLoadProgress(percentComplete);
		
		// Sleep a bit:
		timer.sleepMicros(20*1000);
		
	}
	
	public void destroy(){
		
		if(vScreen!=null)vScreen.destroy();
		if(kbJoy1!=null)kbJoy1.destroy();
		if(kbJoy2!=null)kbJoy2.destroy();
		
		nes = null;
		jogo = null;
		kbJoy1 = null;
		kbJoy2 = null;
		vScreen = null;
		timer = null;
		
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
	public void showErrorMsg(String msg){System.out.println(msg);}
	
	
    
    @Override
    public void romChanged() {
        System.out.println("UIApp.romChanged() - Recriando componentes de vídeo...");
        
        // Destruir tela anterior se existir
        if (vScreen != null) {
            vScreen.destroy();
            vScreen = null;
        }
        
        // Recriar a tela
        vScreen = new ScreenView(nes, 256, 240);
        vScreen.setBgColor(jogo.bgColor.getRGB());
        vScreen.init();
        vScreen.setNotifyImageReady(true);
        
        // Reconfigurar listeners de teclado
        if (kbJoy1 != null) {
            vScreen.addKeyListener(kbJoy1);
        }
        if (kbJoy2 != null) {
            vScreen.addKeyListener(kbJoy2);
        }
        
        System.out.println("UIApp.romChanged() - Componentes de vídeo recriados");
    }


	 // Modificar o método imageReady para controlar FPS
	
	@Override
	public void imageReady(boolean skipFrame) {
        if (!running) return;
        
        // CONTROLE DE FPS PRECISO
        long currentTime = System.nanoTime();
        long elapsed = currentTime - lastFrameTime;
        // Calcular tempo para o próximo frame
        long sleepTimeNanos = NANOS_PER_FRAME - elapsed;
        
        if (sleepTimeNanos > 0) {
            // Converter para milissegundos com precisão
            long sleepTimeMs = sleepTimeNanos / 1000000;
            int sleepTimeNanosRemainder = (int) (sleepTimeNanos % 1000000);
            
            try {
                if (sleepTimeMs > 0) {
                    Thread.sleep(sleepTimeMs, sleepTimeNanosRemainder);
                } else if (sleepTimeNanosRemainder > 0) {
                    Thread.sleep(0, sleepTimeNanosRemainder);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
       
        lastFrameTime = System.nanoTime();     
        // Renderização
        renderScreen();
        updateFPS();
    }

		/*
		@Override
   		 public void imageReady(boolean skipFrame) {
        if (!running || skipFrame) return;
        
        // RENDERIZAÇÃO DIRETA SEM CONTROLE DE FPS COMPLEXO
        renderScreen();
        updateFPS();
    }
		 */



	

	public void startEmulation() {
        running = true;
        lastFrameTime = System.nanoTime();
		System.out.println("✅ Emulação iniciada - FPS alvo: " + TARGET_FPS);
    }
    
    
    
  
}
	
