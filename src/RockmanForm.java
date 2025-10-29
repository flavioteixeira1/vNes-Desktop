import java.applet.Applet;
import java.awt.*;
import javax.swing.plaf.PanelUI;
import java.awt.Container.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;

public class RockmanForm extends Applet implements Runnable, ActionListener {

    boolean scale;
    boolean scanlines;
    boolean sound;
    boolean fps;
    boolean stereo;
    boolean nicesound;
    boolean timeemulation;
    boolean showsoundbuffer;
    int samplerate;
    int romSize;
    int progress;
    //UIApp gui;
    AppletUIApp2 gui;
    NES jogo;
    ScreenView panelScreen;
    String rom = "";
    Font progressFont;
    Color bgColor = Color.black.darker().darker();
    boolean started = false;
    private boolean showWelcomeScreen = true;
    private boolean romLoaded = false;
    
    // Adicionar componentes para menu
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenuItem loadRomItem;
    private JFrame parentFrame;


    public void setParentFrame(JFrame frame) {
        this.parentFrame = frame;
        createMenu();
    }

    private void createMenu() {
        if (parentFrame != null) {
            menuBar = new JMenuBar();
            fileMenu = new JMenu("File");
            loadRomItem = new JMenuItem("Load ROM");
            loadRomItem.addActionListener(this);
            
            fileMenu.add(loadRomItem);
            menuBar.add(fileMenu);
            parentFrame.setJMenuBar(menuBar);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loadRomItem) {
            loadRomFromFile();
        }
    }

    private void loadRomFromFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || 
                       f.getName().toLowerCase().endsWith(".nes") ||
                       f.getName().toLowerCase().endsWith(".nez");
            }
            
            @Override
            public String getDescription() {
                return "NES ROM Files (*.nes, *.nez)";
            }
        });
        
        int result = fileChooser.showOpenDialog(parentFrame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            loadNewRom(selectedFile.getAbsolutePath());
        }
    }

    private void loadNewRom(String romPath) {
        System.out.println("=== LOAD NEW ROM ===");
        System.out.println("Caminho: " + romPath);
        
        // Parar completamente a emulação anterior
        if (jogo != null && jogo.isRunning()) {
            System.out.println("Parando emulação atual...");
            jogo.stopEmulation();
            
            // Esperar um pouco mais para garantir que parou
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        // Remover tela atual se existir
        if (panelScreen != null) {
            System.out.println("Removendo tela anterior...");
            this.remove(panelScreen);
            panelScreen.destroy();
            panelScreen = null;
            // Forçar garbage collection
            System.gc();
        }
        
        // Resetar estado
        started = false;
        showWelcomeScreen = false;
        romLoaded = false;

		// Notificar a UI que a ROM mudou
		if (gui != null) {
        System.out.println("Notificando UI sobre mudança de ROM...");
        gui.romChanged();
    	}
        
        // Carregar nova ROM
        this.rom = romPath;
        System.out.println("Nova ROM definida: " + rom);

        // Reiniciar thread de emulação
        System.out.println("Iniciando nova thread de emulação...");
        Thread t = new Thread(this);
        t.setName("NES-Emulation-Thread");
        t.start();
    }

    public void init() {
        System.gc();
        
        // CONFIGURAÇÕES OTIMIZADAS PARA 60 FPS COM ÁUDIO
        this.scale = true;
        this.timeemulation = true;
        this.fps = true;
        this.stereo = true;
        this.nicesound = true;
        this.sound = true;

     //   System.out.println("=== INICIALIZANDO SISTEMA ===");
     //   System.out.println("Áudio: " + (sound ? "HABILITADO" : "DESABILITADO"));
     //   System.out.println("FPS alvo: 60");
        
        // DIAGNÓSTICO DE ÁUDIO
      //  System.out.println("=== INICIALIZANDO SISTEMA DE ÁUDIO ===");
     //   AudioDiagnostic audioDiagnostic = new AudioDiagnostic();
      //  audioDiagnostic.checkAudioSystem();
        
        // Criar welcome.nes se não existir
        File welcomeROM = new File("welcome.nes");
        if (!welcomeROM.exists()) {
            WelcomeROMCreator.createWelcomeROM();
        }
        
        //gui = new UIApp(this);
        gui = new AppletUIApp2(this);
        gui.init(false);
        
        Globals.appletMode = true;
        Globals.memoryFlushValue = 0x00;
        Globals.preferredFrameRate = 60; // FORÇAR 60 FPS
        Globals.frameTime = 1000000 / 60;
        Globals.enableSound = true; // ← DESABILITAR GLOBALMENTE
        
        jogo = gui.getNES();
        jogo.enableSound(sound);
        jogo.setFramerate(60); // CONFIGURAR 60 FPS
        jogo.reset();

        showWelcomeScreen = true;
        romLoaded = false;
        
       // System.out.println("✅ Sistema inicializado - 60 FPS com áudio");
    }




    public void setRomPath(String romPath) {
        this.rom = romPath;
    }

    public void addScreenView() {
        showWelcomeScreen = false;
        romLoaded = true;
        
        panelScreen = (ScreenView) gui.getScreenView();
        panelScreen.setFPSEnabled(fps);
        
        this.setLayout(null);
        
        if (scale) {
            if (scanlines) {
                panelScreen.setScaleMode(BufferView.SCALE_SCANLINE);
            } else {
                panelScreen.setScaleMode(BufferView.SCALE_NORMAL);
            }
            
            this.setSize(512, 480);
            this.setBounds(0, 0, 512, 480);
            panelScreen.setBounds(0, 0, 512, 480);
        } else {
            panelScreen.setBounds(0, 0, 256, 240);
        }
        
        this.setIgnoreRepaint(true);
        this.add(panelScreen);
        
        // Forçar o componente a ser visível
        panelScreen.setVisible(true);
        this.validate();
        this.repaint();
    }

    public void start() {
        Thread t = new Thread(this);
        t.start();
    }

    public void run() {
        System.out.println("=== THREAD RUN INICIADA ===");
        System.out.println("Thread: " + Thread.currentThread().getName());
        
        // Se não há ROM definida, mostrar tela de boas-vindas
        if (rom == null || rom.trim().isEmpty()) {
           // System.out.println("Nenhuma ROM especificada, mostrando tela de boas-vindas");
            showWelcomeScreen = true;
            romLoaded = false;
            repaint();
            return;
        }
        
        try {
            // Load ROM file:
           // System.out.println("Carregando ROM: " + rom);
            
            // Verificar se o arquivo existe
            java.io.File romFile = new java.io.File(rom);
            if (!romFile.exists()) {
                System.out.println("ERRO: Arquivo não existe: " + rom);
                showErrorDialog("Arquivo não encontrado: " + rom);
                showWelcomeScreen = true;
                romLoaded = false;
                repaint();
                return;
            }
            
           // System.out.println("Arquivo existe, tamanho: " + romFile.length() + " bytes");

            // Carregar a ROM
            boolean loaded = jogo.loadRom(rom);
            
            if (loaded && jogo.rom.isValid()) {
                //System.out.println("ROM carregada com sucesso, adicionando tela...");
                showWelcomeScreen = false;
                romLoaded = true;
                        
                // Pequeno delay para estabilização
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        
                // Add the screen buffer:
                addScreenView();
                
                // Set some properties:
                Globals.timeEmulation = timeemulation;
                jogo.ppu.showSoundBuffer = showsoundbuffer;
                
                // Start emulation:
                //System.out.println("Iniciando execução da CPU...");
                jogo.startEmulation();
                System.out.println("Emulação iniciada!");
                
            } else {
                // ROM file was invalid.
                System.out.println("ERRO: ROM inválida - " + rom);
                showErrorDialog("ROM inválida ou corrompida: " + rom);
                showWelcomeScreen = true;
                romLoaded = false;
                repaint();
            }
        } catch (Exception e) {
            System.err.println("ERRO CRÍTICO na thread de emulação:");
            e.printStackTrace();
            showErrorDialog("Erro crítico: " + e.getMessage());
            showWelcomeScreen = true;
            romLoaded = false;
            repaint();
        }
    }

    public void paint(Graphics g) {
        if (showWelcomeScreen) {
            drawWelcomeScreen(g);
            return;
        }
        
        // Se uma ROM está carregada, deixar o emulador desenhar
        if (romLoaded && panelScreen != null) {
            // Delegar para o painel da tela
            return;
        }
        
        // Fallback: mostrar tela preta
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawWelcomeScreen(Graphics g) {
        int scrw = getWidth();
        int scrh = getHeight();
        
        if (scrw == 0) scrw = 512;
        if (scrh == 0) scrh = 480;
        
       // System.out.println("Desenhando tela de boas-vindas: " + scrw + "x" + scrh);
        
        // Fill background with dark blue
        g.setColor(new Color(10, 20, 40));
        g.fillRect(0, 0, scrw, scrh);
        
        // Configurar fontes
        Font titleFont = new Font("Arial", Font.BOLD, 28);
        Font subtitleFont = new Font("Arial", Font.BOLD, 18);
        Font infoFont = new Font("Arial", Font.PLAIN, 14);
        Font smallFont = new Font("Arial", Font.PLAIN, 12);
        
        // Título principal
        g.setFont(titleFont);
        g.setColor(Color.YELLOW);
        String title = "vNES Emulator";
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, scrw/2 - titleWidth/2, scrh/2 - 80);
        
        // Subtítulo
        g.setFont(subtitleFont);
        g.setColor(Color.WHITE);
        String subtitle = "Emulador NES para Desktop";
        int subtitleWidth = g.getFontMetrics().stringWidth(subtitle);
        g.drawString(subtitle, scrw/2 - subtitleWidth/2, scrh/2 - 40);
        
        // Instruções
        g.setFont(infoFont);
        g.setColor(Color.CYAN);
        String line1 = "Para começar, use: File → Load ROM";
        String line2 = "Controles: Setas (Direção) | Z (A) | X (B) | Ctrl (Select) | Enter (Start)";
        String line3 = "Versão Desktop por Flávio Augusto Teixeira - flavioteixeira1@gmail.com";
        int line1Width = g.getFontMetrics().stringWidth(line1);
        int line2Width = g.getFontMetrics().stringWidth(line2);
        int line3Width = g.getFontMetrics().stringWidth(line3);
        
        g.drawString(line1, scrw/2 - line1Width/2, scrh/2 + 10);
        g.drawString(line2, scrw/2 - line2Width/2, scrh/2 + 30);
        g.drawString(line3, scrw/2 - line3Width/2, scrh/2 + 50);
        
        
        // Dicas
        g.setColor(Color.ORANGE);
        String tip1 = "Agradecimentos a Jamie Sanders por elaborar o vNES original";
        String tip2 = "Agradecimentos a Ben Firshman por manter o repositório vNES no github";
        String tip3 = "Formatos suportados: .nes, .nez";
        
        int tip1Width = g.getFontMetrics().stringWidth(tip1);
        int tip2Width = g.getFontMetrics().stringWidth(tip2);
        int tip3Width = g.getFontMetrics().stringWidth(tip3);

        g.drawString(tip1, scrw/2 - tip1Width/2, scrh/2 + 74);
        g.drawString(tip2, scrw/2 - tip2Width/2, scrh/2 + 90);
        g.drawString(tip3, scrw/2 - tip3Width/2, scrh/2 + 110);
        
        // Rodapé
        g.setFont(smallFont);
        g.setColor(Color.GRAY);
        String footer = "Baseado no vNES original - Versão Desktop";
        int footerWidth = g.getFontMetrics().stringWidth(footer);
        g.drawString(footer, scrw/2 - footerWidth/2, scrh - 30);
        
        // Versão
        g.drawString("Java " + System.getProperty("java.version"), 10, scrh - 10);
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(parentFrame, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }
    
    public void stop() {
        if (jogo != null) {
            jogo.stopEmulation();
            jogo.getPapu().stop();
        }
    }
    
    public void destroy() {
        if (jogo != null && jogo.getCpu().isRunning()) {
            stop();
        }
        
        if (jogo != null) jogo.destroy();
        if (gui != null) gui.destroy();
        
        gui = null;
        jogo = null;
        panelScreen = null;
        rom = null;
        
        System.runFinalization();
        System.gc();
    }
    
    public void showLoadProgress(int percentComplete) {
        progress = percentComplete;
        repaint();
    }
    
    public void update(Graphics g) {
        paint(g);
    }
    
    // ... resto dos métodos readParams() permanecem iguais

	
	public void readParams(){
		
		String tmp;
		
		tmp = getParameter("rom");
		if(tmp==null || tmp.equals("")){
			rom = "vnes.nes";
		}else{
			rom = tmp;
		}
		
		tmp = getParameter("scale");
		if(tmp==null || tmp.equals("")){
			scale = false;
		}else{
			scale = tmp.equals("on");
		}
		
		tmp = getParameter("sound");
		if(tmp==null || tmp.equals("")){
			sound = true;
		}else{
			sound = tmp.equals("on");
		}
		
		tmp = getParameter("stereo");
		if(tmp==null || tmp.equals("")){
			stereo = true; // on by default
		}else{
			stereo = tmp.equals("on");
		}
		
		tmp = getParameter("scanlines");
		if(tmp==null || tmp.equals("")){
			scanlines = false;
		}else{
			scanlines = tmp.equals("on");
		}
		
		tmp = getParameter("fps");
		if(tmp==null || tmp.equals("")){
			fps = false;
		}else{
			fps = tmp.equals("on");
		}
		
		tmp = getParameter("nicesound");
		if(tmp==null || tmp.equals("")){
			nicesound = true;
		}else{
			nicesound = tmp.equals("on");
		}
		
		tmp = getParameter("timeemulation");
		if(tmp==null || tmp.equals("")){
			timeemulation = true;
		}else{
			timeemulation = tmp.equals("on");
		}
		
		tmp = getParameter("showsoundbuffer");
		if(tmp==null || tmp.equals("")){
			showsoundbuffer = false;
		}else{
			showsoundbuffer = tmp.equals("on");
		}
		
		tmp = getParameter("romsize");
		if(tmp==null || tmp.equals("")){
			romSize = -1;
		}else{
			try{
				romSize = Integer.parseInt(tmp);
			}catch(Exception e){
				romSize = -1;
			}
		}
		
	}

    private void printDiagnostics() {
        System.out.println("=== DIAGNÓSTICO DO SISTEMA ===");
        //System.out.println("FPS: " + (gui instanceof UIApp ? "UIApp" : "Unknown"));
        System.out.println("FPS: " + (gui instanceof AppletUIApp2 ? "AppletUIApp" : "Unknown"));
        System.out.println("Áudio habilitado: " + Globals.enableSound);
        System.out.println("ROM carregada: " + romLoaded);
        System.out.println("NES running: " + (jogo != null && jogo.isRunning()));
        System.out.println("PAPU running: " + (jogo != null && jogo.getPapu() != null && jogo.getPapu().isRunning()));
        System.out.println("==============================");
    }


}