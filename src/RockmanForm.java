import java.applet.Applet;
import java.awt.*;
import javax.swing.plaf.PanelUI;
import java.awt.Container.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;

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
    UIApp gui;
    //AppletUIApp2 gui;
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

    private JMenu saveStateMenu;
    private JMenuItem[] saveStateItems;
    private JMenuItem loadStateItem;
    private JMenuItem saveStateWithNameItem;


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

            saveStateMenu = new JMenu("Save States");

             saveStateItems = new JMenuItem[10];
            for (int i = 0; i < 10; i++) {
                final int slot = i;
                saveStateItems[i] = new JMenuItem((i + 1) + ": " + getSaveStateName(i));
                saveStateItems[i].addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        loadSaveState(slot);
                    }
                });
                saveStateMenu.add(saveStateItems[i]);
            }
            
            saveStateMenu.addSeparator();
            
            // Salvar estado com nome personalizado
            saveStateWithNameItem = new JMenuItem("Salvar Estado com Nome...");
            saveStateWithNameItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showSaveStateWithNameDialog();
                }
            });
            saveStateMenu.add(saveStateWithNameItem);
            
            // Carregar estado
            loadStateItem = new JMenuItem("Carregar Estado...");
            loadStateItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showLoadStateDialog();
                }
            });
            saveStateMenu.add(loadStateItem);
            
            menuBar.add(fileMenu);
            menuBar.add(saveStateMenu);
            parentFrame.setJMenuBar(menuBar);
            
            updateSaveStateMenu();
        

        }
    }


    private String getSaveStateName(int slot) {
            if (jogo != null && jogo.getSaveStateManager() != null) {
                String name = jogo.getSaveStateManager().getStateName(slot);
                boolean exists = jogo.getSaveStateManager().stateExists(slot);
                return exists ? name : "[Vazio] " + name;
            }
            return "[Vazio] Save State " + (slot + 1);
        }

     private void updateSaveStateMenu() {
            if (saveStateItems != null && jogo != null && jogo.getSaveStateManager() != null) {
                for (int i = 0; i < 10; i++) {
                    saveStateItems[i].setText((i + 1) + ": " + getSaveStateName(i));
                }
            }
        }

    private void loadSaveState(int slot) {
            if (jogo != null && jogo.loadState(slot)) {
                JOptionPane.showMessageDialog(parentFrame, 
                    "Estado carregado do slot " + (slot + 1) + ": " + 
                    jogo.getSaveStateManager().getStateName(slot),
                    "Estado Carregado", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(parentFrame,
                    "Erro ao carregar estado do slot " + (slot + 1),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }

    
    private void showSaveStateWithNameDialog() {
                if (jogo == null) return;
                
                // Diálogo para selecionar slot
                String[] slotOptions = new String[10];
                for (int i = 0; i < 10; i++) {
                    slotOptions[i] = "Slot " + (i + 1) + " - " + getSaveStateName(i);
                }
                
                String slotChoice = (String) JOptionPane.showInputDialog(parentFrame,
                    "Selecione o slot para salvar:",
                    "Salvar Estado",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    slotOptions,
                    slotOptions[0]);
                
                if (slotChoice != null) {
                    int slot = -1;
                    for (int i = 0; i < 10; i++) {
                        if (slotOptions[i].equals(slotChoice)) {
                            slot = i;
                            break;
                        }
                    }
                    
                    if (slot != -1) {
                        // Diálogo para nome personalizado
                        String currentName = jogo.getSaveStateManager().getStateName(slot);
                        String customName = JOptionPane.showInputDialog(parentFrame,
                            "Digite um nome para o save state:",
                            currentName);
                        
                        if (customName != null && !customName.trim().isEmpty()) {
                            if (jogo.saveState(slot, customName.trim())) {
                                JOptionPane.showMessageDialog(parentFrame,
                                    "Estado salvo no slot " + (slot + 1) + ": " + customName,
                                    "Estado Salvo", JOptionPane.INFORMATION_MESSAGE);
                                updateSaveStateMenu();
                            } else {
                                JOptionPane.showMessageDialog(parentFrame,
                                    "Erro ao salvar estado",
                                    "Erro", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                }
            }

    
    private void showLoadStateDialog() {
                if (jogo == null) return;
                
                // Listar apenas slots que existem
                java.util.List<String> availableSlots = new ArrayList<>();
                java.util.List<Integer> slotNumbers = new ArrayList<>();
                
                for (int i = 0; i < 10; i++) {
                    if (jogo.getSaveStateManager().stateExists(i)) {
                        availableSlots.add("Slot " + (i + 1) + ": " + jogo.getSaveStateManager().getStateName(i));
                        slotNumbers.add(i);
                    }
                }
                
                if (availableSlots.isEmpty()) {
                    JOptionPane.showMessageDialog(parentFrame,
                        "Nenhum save state encontrado",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                String[] slotArray = availableSlots.toArray(new String[0]);
                String slotChoice = (String) JOptionPane.showInputDialog(parentFrame,
                    "Selecione o estado para carregar:",
                    "Carregar Estado",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    slotArray,
                    slotArray[0]);
                
                if (slotChoice != null) {
                    for (int i = 0; i < availableSlots.size(); i++) {
                        if (slotArray[i].equals(slotChoice)) {
                            loadSaveState(slotNumbers.get(i));
                            break;
                        }
                    }
                }
            }

        
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loadRomItem) {
            loadRomFromFile();
        }
    }

    public void keyPressed(KeyEvent e) {
            int keyCode = e.getKeyCode();
            // F1-F10: Salvar estados
            if (keyCode >= KeyEvent.VK_F1 && keyCode <= KeyEvent.VK_F10 && e.isControlDown()) {
                int slot = keyCode - KeyEvent.VK_F1;
                if (jogo != null) {
                    String currentName = jogo.getSaveStateManager().getStateName(slot);
                    jogo.saveState(slot, currentName);
                    System.out.println("Estado salvo rapidamente no slot " + (slot + 1));
                }
            }
            // F1-F10: Carregar estados
            else if (keyCode >= KeyEvent.VK_F1 && keyCode <= KeyEvent.VK_F10) {
                int slot = keyCode - KeyEvent.VK_F1;
                if (jogo != null && jogo.getSaveStateManager().stateExists(slot)) {
                    jogo.loadState(slot);
                    System.out.println("Estado carregado rapidamente do slot " + (slot + 1));
                }
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
            System.out.println("RockmanForm.loadNewRom() - Iniciando carregamento de nova ROM");
            System.out.println("Caminho: " + romPath);
            
            // 1. Parar thread de emulação atual se estiver rodando
            if (jogo != null && jogo.isRunning()) {
                System.out.println("Parando emulação atual...");
                jogo.stopEmulation();
                
                try {
                    Thread.sleep(400); // Delay maior
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            
            // 2. Não remover o panelScreen - vamos reutilizá-lo
            // apenas garantir que está limpo
            if (panelScreen != null) {
                System.out.println("Limpando ScreenView atual...");
                // Não remover do container, apenas limpar
                panelScreen.clear();
            }
            
            // 3. Resetar estado
            started = false;
            showWelcomeScreen = false;
            romLoaded = false;
            this.rom = romPath;
            
            System.out.println("Nova ROM definida: " + rom);
            
            if (gui != null) {
                System.out.println("Solicitando carregamento da ROM via UIApp...");
                gui.loadNewRom(romPath);
                
                // Obter nova referência do NES
                jogo = gui.getNES();
                
                if (jogo != null && jogo.rom != null && jogo.rom.isValid()) {
                    System.out.println("ROM carregada com sucesso!");
                    
                    // 4. Se o panelScreen já existe, apenas atualizar a referência
                    if (panelScreen != null) {
                        System.out.println("Atualizando referência do NES no ScreenView existente...");
                        panelScreen.updateNESReference(jogo);
                    } else {
                        // 5. Se não existe, criar novo
                        System.out.println("Criando novo ScreenView...");
                        addScreenView();
                    }
                    
                    // 6. Pequeno delay para estabilização
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    
                    // 7. Configurar propriedades
                    Globals.timeEmulation = timeemulation;
                    if (jogo.ppu != null) {
                        jogo.ppu.showSoundBuffer = showsoundbuffer;
                    }
                    
                    // 8. Diagnóstico final antes de iniciar
                    System.out.println("=== DIAGNÓSTICO FINAL ===");
                    System.out.println("NES: " + (jogo != null ? "OK" : "NULL"));
                    System.out.println("PPU: " + (jogo != null && jogo.getPpu() != null ? "OK" : "NULL"));
                    System.out.println("ScreenView: " + (panelScreen != null ? "OK" : "NULL"));
                    System.out.println("Buffer: " + (jogo != null && jogo.getPpu() != null && jogo.getPpu().buffer != null ? "OK" : "NULL"));
                    System.out.println("=========================");
                    
                    // 9. AGORA iniciar emulação
                    System.out.println("Iniciando emulação...");
                    jogo.startEmulation();
                    
                    romLoaded = true;
                    showWelcomeScreen = false;
                    System.out.println("Emulação configurada com sucesso!");
                    
                } else {
                    System.err.println("Falha ao carregar ROM, mostrando tela de boas-vindas");
                    showWelcomeScreen = true;
                    romLoaded = false;
                    repaint();
                }
            }
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
    // Criar welcome.nes se não existir
    File welcomeROM = new File("welcome.nes");
    if (!welcomeROM.exists()) {
        WelcomeROMCreator.createWelcomeROM();
    }
    // Inicializar GUI primeiro
    gui = new UIApp(this);
    gui.init(false);
    // Obter referência do NES
    jogo = gui.getNES();
    // Configurar globais
    Globals.appletMode = true;
    Globals.memoryFlushValue = 0x00;
    Globals.preferredFrameRate = 60;
    Globals.frameTime = 1000000 / 60;
    Globals.enableSound = true;
    if (jogo != null) {
        jogo.enableSound(sound);
        jogo.setFramerate(60);
        jogo.reset();
    }
    showWelcomeScreen = true;
    romLoaded = false;
    System.out.println("RockmanForm.init() - Inicialização completa");
    }


    public void setRomPath(String romPath) {
        this.rom = romPath;
    }

    public void addScreenView() {
        System.out.println("RockmanForm.addScreenView() - Adicionando ScreenView");
        
        if (gui == null || gui.getScreenView() == null) {
            System.err.println("GUI ou ScreenView não inicializados");
            return;
        }
        
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
        
        // Forçar o componente a ser visível e focado
        panelScreen.setVisible(true);
        panelScreen.setFocusable(true);
        panelScreen.requestFocus();
        
        this.validate();
        this.repaint();
        
        System.out.println("ScreenView adicionado com sucesso");
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
        System.out.println("FPS: " + (gui instanceof UIApp ? "AppletUIApp" : "Unknown"));
        System.out.println("Áudio habilitado: " + Globals.enableSound);
        System.out.println("ROM carregada: " + romLoaded);
        System.out.println("NES running: " + (jogo != null && jogo.isRunning()));
        System.out.println("PAPU running: " + (jogo != null && jogo.getPapu() != null && jogo.getPapu().isRunning()));
        System.out.println("==============================");
    }


}