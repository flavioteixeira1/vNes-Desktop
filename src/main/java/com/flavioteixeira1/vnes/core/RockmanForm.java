package com.flavioteixeira1.vnes.core;

import java.applet.Applet;
import java.awt.*;
import javax.swing.plaf.PanelUI;

import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

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
            //Menu File
            fileMenu = new JMenu("File");
            loadRomItem = new JMenuItem("Load ROM");
            loadRomItem.addActionListener(this);
            
            fileMenu.add(loadRomItem);
            menuBar.add(fileMenu);

            //Menu Controles
             JMenu controlsMenu = new JMenu("Controles");
             JMenuItem configControlsItem = new JMenuItem("Configurar Controles...");
                  configControlsItem.addActionListener(e -> {
               if (gui != null && gui.getInputManager() != null) {
                     new JoystickConfigDialog(parentFrame, gui.getInputManager(), jogo).setVisible(true);
                }
              });
                 controlsMenu.add(configControlsItem);
                 menuBar.add(controlsMenu);
        
                parentFrame.setJMenuBar(menuBar);

        }
    }

public class JoystickConfigDialog extends JDialog {
    private InputManager inputManager;
    private NES nes;
    private JComboBox<String> player1Combo, player2Combo;
    private JTextArea statusArea;
    
            public JoystickConfigDialog(Frame parent, InputManager inputManager, NES nes) {
                super(parent, "Configuração de Controles", true);
                this.inputManager = inputManager;
                this.nes = nes;
                initializeUI();
                pack();
                setLocationRelativeTo(parent);
            }
            
            private void initializeUI() {
                setLayout(new BorderLayout(10, 10));
                
                // Painel de configuração dos players
                JPanel configPanel = new JPanel(new GridLayout(3, 2, 5, 5));
                configPanel.setBorder(BorderFactory.createTitledBorder("Configuração dos Players"));
                
                configPanel.add(new JLabel("Player 1:"));
                player1Combo = new JComboBox<>(new String[]{"Teclado", "Joystick", "Desabilitado"});
                 // Converter tipo atual para índice do combo
                int player1Type = inputManager.getPlayerHandlerType(InputManager.PLAYER_1);
                int player1Index = convertHandlerTypeToIndex(player1Type);
                player1Combo.setSelectedIndex(player1Index);
                // Configurar baseado no tipo atual, mas converter para índice correto
                
                //player1Combo.setSelectedIndex(player1Type - 1); // Ajuste porque teclado=1, joystick=2, desabilitado=0
                configPanel.add(player1Combo);
                
                configPanel.add(new JLabel("Player 2:"));
                player2Combo = new JComboBox<>(new String[]{"Teclado", "Joystick", "Desabilitado"});
                int player2Type = inputManager.getPlayerHandlerType(InputManager.PLAYER_2);
                int player2Index = convertHandlerTypeToIndex(player2Type);
                //player2Combo.setSelectedIndex(inputManager.getPlayerHandlerType(InputManager.PLAYER_2));
                player2Combo.setSelectedIndex(player2Index);
                configPanel.add(player2Combo);

                // Painel de status do joystick
                JPanel statusPanel = new JPanel(new BorderLayout());
                statusPanel.setBorder(BorderFactory.createTitledBorder("Status do Joystick"));
                
                statusArea = new JTextArea(4, 30);
                statusArea.setEditable(false);
                updateStatus();
                statusPanel.add(new JScrollPane(statusArea), BorderLayout.CENTER);
                
                JButton refreshButton = new JButton("Recarregar Joysticks");
                refreshButton.addActionListener(e -> refreshJoysticks());
                statusPanel.add(refreshButton, BorderLayout.SOUTH);
                
                // Painel de botões
                JPanel buttonPanel = new JPanel();
                JButton applyButton = new JButton("Aplicar");
                JButton cancelButton = new JButton("Cancelar");
                
                applyButton.addActionListener(e -> applyConfig());
                cancelButton.addActionListener(e -> dispose());
                
                buttonPanel.add(applyButton);
                buttonPanel.add(cancelButton);
                
                add(configPanel, BorderLayout.NORTH);
                add(statusPanel, BorderLayout.CENTER);
                add(buttonPanel, BorderLayout.SOUTH);
                
                setPreferredSize(new Dimension(450, 350));
            }


        private int convertHandlerTypeToIndex(int handlerType) {
            switch (handlerType) {
                case InputConfig.HANDLER_KEYBOARD: return 0; // Teclado
                case InputConfig.HANDLER_JOYSTICK: return 1; // Joystick
                case InputConfig.HANDLER_DISABLED: return 2; // Desabilitado
                default: return 2; // Default para desabilitado
            }
        }
    
        private int convertIndexToHandlerType(int index) {
            switch (index) {
                case 0: return InputConfig.HANDLER_KEYBOARD; // Teclado
                case 1: return InputConfig.HANDLER_JOYSTICK; // Joystick
                case 2: return InputConfig.HANDLER_DISABLED; // Desabilitado
                default: return InputConfig.HANDLER_DISABLED; // Default
            }
        }
            
            private void updateStatus() {
                StringBuilder status = new StringBuilder();
                
                // Verificar joysticks conectados
                ControllerEnvironment env = ControllerEnvironment.getDefaultEnvironment();
                Controller[] controllers = env.getControllers();
                
                int joystickCount = 0;
                for (Controller controller : controllers) {
                    if (controller.getType() == Controller.Type.GAMEPAD || 
                        controller.getType() == Controller.Type.STICK) {
                        joystickCount++;
                        status.append("Joystick ").append(joystickCount).append(": ")
                            .append(controller.getName()).append("\n");
                    }
                }
                
                if (joystickCount == 0) {
                    status.append("Nenhum joystick encontrado\n");
                }
                
                status.append("\nConfiguração atual:\n");
                status.append("Player 1: ").append(InputConfig.getHandlerName(
                    inputManager.getPlayerHandlerType(InputManager.PLAYER_1))).append("\n");
                status.append("Player 2: ").append(InputConfig.getHandlerName(
                    inputManager.getPlayerHandlerType(InputManager.PLAYER_2)));
                
                statusArea.setText(status.toString());
            }
            
            private void refreshJoysticks() {
                updateStatus();
            }
            
            private void applyConfig() {
                    try {
                          System.out.println("=== APLICANDO CONFIGURAÇÃO ===");
                        // Aplicar configuração do Player 1
                        int player1Index = player1Combo.getSelectedIndex();
                        int player1Type = convertIndexToHandlerType(player1Index);
                        System.out.println("Configurando Player 1 como Teclado...");
                        inputManager.setPlayerHandler(InputManager.PLAYER_1, player1Type, nes);
                        
                        // Aplicar configuração do Player 2
                        int player2Index = player2Combo.getSelectedIndex();
                        int player2Type = convertIndexToHandlerType(player2Index);
                        System.out.println("Configurando Player 2 como Desabilitado...");
                        inputManager.setPlayerHandler(InputManager.PLAYER_2, player2Type, nes);
                        System.out.println("=== CONFIGURAÇÃO APLICADA ===");
                        JOptionPane.showMessageDialog(this, "Configuração aplicada com sucesso!");
                        dispose();
                    } catch (Exception e) {
                        System.err.println("ERRO CRÍTICO: " + e.getMessage());
                        JOptionPane.showMessageDialog(this, 
                            "Erro ao aplicar configuração: " + e.getMessage(), 
                            "Erro", JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                    }
                JOptionPane.showMessageDialog(this, "Configuração aplicada com sucesso!");
                dispose();
            }
    }
     

  
    private String getSaveStateName() {
            if (jogo != null && jogo.getSaveStateManager() != null) {
                String name = jogo.getSaveStateManager().getStateName();
                boolean exists = jogo.getSaveStateManager().stateExists();
                return exists ? name : "[Sem save]";
            }
            return "[Sem save]"; 
        }

        
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loadRomItem) {
            loadRomFromFile();
        }
    }

    public void keyPressed(KeyEvent e) {
            int keyCode = e.getKeyCode();
            // F5 - Salvar estado
            if (e.getKeyCode() == KeyEvent.VK_F5) {
                // Salvar o save state na ROM ativa
                if (jogo != null) {
                    boolean sucesso = jogo.saveState("save"); 
                    if (sucesso) {
                        JOptionPane.showMessageDialog(parentFrame, "Save state salvo para " + jogo.getSaveStateManager().getStateName());
                    } else {
                        JOptionPane.showMessageDialog(parentFrame, "Erro ao salvar o save state!");
                    }
                }
            }
            //F6 - carregar estado
            if (e.getKeyCode() == KeyEvent.VK_F6) {
                // Carregar save state da ROM ativa
                if (jogo != null) {
                    boolean sucesso = jogo.loadState();
                    if (sucesso) {
                        JOptionPane.showMessageDialog(parentFrame, "Save state carregado para " + jogo.getSaveStateManager().getStateName());
                    } else {
                        JOptionPane.showMessageDialog(parentFrame, "Nenhum save state encontrado para esta ROM.");
                    }
                }
            }
            //Carregar rom
            if (e.getKeyCode() == KeyEvent.VK_F2) {
            
            this.loadRomFromFile();
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
                    
                    // 6.1 Testar controles
                    gui.testInputs();

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
                    gui.testInputs();

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
        
        this.setLayout(new BorderLayout()); // Usar BorderLayout
        
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
        this.add(panelScreen, BorderLayout.CENTER);
        
        // Forçar o componente a ser visível e focado
        panelScreen.setVisible(true);
        panelScreen.setFocusable(true);
        //panelScreen.requestFocus();
        panelScreen.requestFocusInWindow();

        // Timer para garantir foco após renderização
        Timer focusTimer = new Timer(500, e -> {
            System.out.println("Forçando foco no ScreenView...");
            panelScreen.requestFocusInWindow();
            if (!panelScreen.hasFocus()) {
                System.out.println("AVISO: ScreenView ainda não tem foco!");
            }
        });
        focusTimer.setRepeats(false);
        focusTimer.start();
        
        this.validate();
        this.repaint();
        
         System.out.println("ScreenView adicionado - Focusable: " + panelScreen.isFocusable() + 
                      ", HasFocus: " + panelScreen.hasFocus());
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