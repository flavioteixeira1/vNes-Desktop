package com.flavioteixeira1.vnes.core;
import java.awt.Frame;
import java.awt.event.*;

import javax.swing.JOptionPane;

public class AppletUIApp implements UI{
	
	//RockmanForm app;
    RockmanApp applet;
    NES nes;
	KbInputHandler kbJoy1;
	KbInputHandler kbJoy2;
	ScreenView vScreen;
	HiResTimer timer;
	private InputHandler inputHandler;
	private InputHandler inputHandlerPlayer2;
	long t1,t2;
	int sleepTime;
	
	public AppletUIApp(RockmanApp applet){
	
		timer = new HiResTimer();
		this.applet = applet;
		nes = new NES(this);           
	
	}
	
	public void init(boolean showGui){
		
		vScreen = new ScreenView(nes,256,240);
		vScreen.setBgColor(applet.bgColor.getRGB());
		vScreen.init();
		vScreen.setNotifyImageReady(true);
		
		kbJoy1 = new KbInputHandler(nes,0); //teclado player1
		kbJoy2 = new KbInputHandler(nes,1); //teclado player2

		inputHandler = new KbInputHandler(nes,0); //joystick player1
		inputHandlerPlayer2 = new KbInputHandler(nes,1); //joystick player2
		
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


		if (inputHandler instanceof KbInputHandler) {
   		 JoystickManager jm = ((KbInputHandler) inputHandler).getJoystickManager();
   			 if (jm != null) {
        			System.out.println(" Joystick Player1: " + jm.getJoystickName());
        			System.out.println(" Mapeamento ativo:");
        			System.out.println("   Botão 0 -> Z (A)");
        			System.out.println("   Botão 1 -> X (B)");
        			System.out.println("   Botão 2 -> Enter (Start)");
        			System.out.println("   Botão 3 -> Ctrl (Select)");
        			System.out.println("   Eixos -> Setas direcionais");
    			}
		}

		 // Player 2
		if (inputHandlerPlayer2 instanceof KbInputHandler) {
            JoystickManager jm2 = ((KbInputHandler) inputHandlerPlayer2).getJoystickManager();
            if (jm2 != null && jm2.isJoystickEnabled()) {
                System.out.println(" Player 2 - Joystick: " + jm2.getJoystickName());
                System.out.println(" Player 2 - Mapeamento ativo:");
                System.out.println("   Botão 0 -> VK_NUMPAD7 (A)");
                System.out.println("   Botão 1 -> VK_NUMPAD9 (B)");
                System.out.println("   Botão 2 -> VK_NUMPAD1 (Start)");
                System.out.println("   Botão 3 -> VK_NUMPAD3 (Select)");
                System.out.println("   UP -> VK_NUMPAD8");
                System.out.println("   DOWN -> VK_NUMPAD2");
                System.out.println("   LEFT -> VK_NUMPAD4");
                System.out.println("   RIGHT -> VK_NUMPAD6");
            }
        }

		
		
	}

	public JoystickManager getJoystickManager(int playerId) {
				if (playerId == 0) {
				if (inputHandler instanceof KbInputHandler) {
					return ((KbInputHandler) inputHandler).getJoystickManager();
				}
			} else {
				if (inputHandlerPlayer2 instanceof KbInputHandler) {
					return ((KbInputHandler) inputHandlerPlayer2).getJoystickManager();
				}
			}
			return null;
	}
	
	public boolean isJoystickEnabled(int playerId) {
		JoystickManager jm = getJoystickManager(playerId);
        return jm != null && jm.isJoystickEnabled();	
	}



	public void showJoystickConfig(Frame parentFrame , int playerId) {
		JoystickManager jm = getJoystickManager(playerId);
			if (jm != null && jm.isJoystickEnabled()) {
				JoystickConfigDialog dialog = new JoystickConfigDialog(parentFrame, jm, playerId);
				dialog.setVisible(true);
			} else {
				JOptionPane.showMessageDialog(parentFrame,
					"Nenhum joystick detectado para Player " + (playerId + 1) + "!\n\n" +
					"Conecte um joystick e reinicie o emulador.\n" +
					"Verifique se o java possui jinput-dx8_64, jinput-raw_64 ou libjinput-linux64 conforme o sistema operacional",
					"Joystick Não Encontrado - Player " + (playerId + 1),
					JOptionPane.WARNING_MESSAGE);
			}
	}



	public void imageReady(boolean skipFrame){
		
		// Sound stuff:
		int tmp = nes.getPapu().getBufferIndex();
		if(Globals.enableSound && Globals.timeEmulation && tmp>0){
			
			int min_avail = nes.getPapu().line.getBufferSize()-4*tmp;
			timer.sleepMicros(nes.papu.getMillisToAvailableAbove(min_avail));
			while(nes.getPapu().line.available() < min_avail){
				timer.yield();
			}
			nes.getPapu().writeBuffer();

		}
		
		// Sleep a bit if sound is disabled:
		if(Globals.timeEmulation && !Globals.enableSound){
			
			sleepTime = Globals.frameTime;
			if((t2=timer.currentMicros())-t1 < sleepTime){
				timer.sleepMicros(sleepTime-(t2-t1));
			}
			
		}

		// Update timer:
		t1 = t2;
		
	}
	
	public int getRomFileSize(){
		return applet.romSize;
	}
	
	public void showLoadProgress(int percentComplete){
		
		// Show ROM load progress:
		applet.showLoadProgress(percentComplete);
		
		// Sleep a bit:
		timer.sleepMicros(20*1000);
		
	}
	
	public void destroy(){
		
		if(vScreen!=null)vScreen.destroy();
		if(kbJoy1!=null)kbJoy1.destroy();
		if(kbJoy2!=null)kbJoy2.destroy();
		
		nes = null;
		applet = null;
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
		return applet.getWidth();
	}
	public int getHeight(){
		return applet.getHeight();
	}
	public void println(String s){}
	public void showErrorMsg(String msg){System.out.println(msg);}
	
}