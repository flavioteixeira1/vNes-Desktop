import java.awt.event.*;

public class ScreenView extends BufferView{

	private MyMouseAdapter mouse;
	private boolean notifyImageReady;

	public ScreenView(NES nes, int width, int height){
		super(nes,width,height);
	}


	public void init(){

		System.out.println("ScreenView.init() - Inicializando tela");
		
		if(mouse==null){
			mouse = new MyMouseAdapter();
			this.addMouseListener(mouse);
		}
		super.init();
		
	}

	// Método para atualizar a referência do NES se necessário
    public void updateNESReference(NES newNes) {
        if (newNes != null) {
            this.nes = newNes;
            System.out.println("ScreenView - Referência do NES atualizada");
        }
    }



	private class MyMouseAdapter extends MouseAdapter{
		
		long lastClickTime=0;
		
		public void mouseClicked(MouseEvent me){
			setFocusable(true);
			requestFocus();
		}
		
		public void mousePressed(MouseEvent me){
			setFocusable(true);
			requestFocus();
			 // Verificar se o NES e mapper estão disponíveis
            if (nes != null && nes.memMapper != null) {
			
				if(me.getX()>=0 && me.getY()>=0 && me.getX()<256 && me.getY()<240){
					if(nes!=null && nes.memMapper!=null){
						nes.memMapper.setMouseState(true,me.getX(),me.getY());
					}
				}
			}
			
		}
		
		public void mouseReleased(MouseEvent me){
			
			if(nes!=null && nes.memMapper!=null){
				nes.memMapper.setMouseState(false,0,0);
			}
			
		}
		
	}
	
	public void setNotifyImageReady(boolean value){
		this.notifyImageReady = value;
	}

	
	public void clear() {
    System.out.println("ScreenView.clear() - Limpando tela");
		try {
			// Usar o método existente do BufferView ou criar uma imagem preta
			if (img != null) {
				java.awt.Graphics g = img.getGraphics();
				g.setColor(java.awt.Color.BLACK);
				g.fillRect(0, 0, img.getWidth(), img.getHeight());
				g.dispose();
				repaint();
			}
		} catch (Exception e) {
			System.err.println("ScreenView.clear() - Erro: " + e.getMessage());
		}
	}

	public void imageReady(boolean skipFrame){
		
		  // Verificação mais robusta dos componentes
        if (this.nes == null) {
            System.err.println("ScreenView.imageReady() - NES é null, impossível continuar");
            return;
        }

		if (this.nes.getGui() == null) {
            System.err.println("ScreenView.imageReady() - GUI do NES é null");
            return;
        }
			
			try {
				if(!Globals.focused){
				setFocusable(true);
				requestFocus();
        		Globals.focused = true;
       			}
				// Draw image first:
				super.imageReady(skipFrame);


				// Notificar a GUI para atualizar a tela
				this.nes.getGui().imageReady(skipFrame);
			} catch (Exception e) {
				System.err.println("Erro em imageReady: " + e.getMessage());
			}

		/*
		if(!Globals.focused){
       		setFocusable(true);
		requestFocus();
        	Globals.focused = true;
       		}

		// Draw image first:
		super.imageReady(skipFrame);
		
		// Notify GUI, so it can write the sound buffer:
		if(notifyImageReady){
			nes.getGui().imageReady(skipFrame);
		}
		 */

	}
		
}