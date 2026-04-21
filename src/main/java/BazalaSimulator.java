import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class BazalaSimulator extends JPanel {
    private Menu hlavniOkno;
    private JLayeredPane vrstvy;
    private Menu.BackgroundPanel bgPanel;
    private JLabel settingsLabel;

    private JPanel panelObrazovky;
    private JTextField numpadDisplay;
    private Boolean vypinacObrazovky = true;

    // Seznam interaktivních zón
    private List<InteraktivniZona> hotspoty = new ArrayList<>();

    // Souřadnice monitoru
    private final int SCREEN_X = 467;
    private final int SCREEN_Y = 96;
    private final int SCREEN_W = 417;
    private final int SCREEN_H = 215;

    public BazalaSimulator(Menu okno) {
        this.hlavniOkno = okno;
        setLayout(new BorderLayout());
        vrstvy = new JLayeredPane();

        String iconPath = "/nastaveni.png";
        String backgroundPath = "/pokladna.png";

        try {
            bgPanel = okno.new BackgroundPanel(backgroundPath);
            vrstvy.add(bgPanel, JLayeredPane.DEFAULT_LAYER);

            ImageIcon settingsIcon = new ImageIcon(getClass().getResource(iconPath));
            Image scaledImg = settingsIcon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            settingsLabel = new JLabel(new ImageIcon(scaledImg));
            settingsLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

            settingsLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    hlavniOkno.zobrazObrazovku("HLAVNI_MENU");
                }
            });
            vrstvy.add(settingsLabel, JLayeredPane.PALETTE_LAYER);

            // 1. Vytvoření designu obrazovky
            panelObrazovky = vytvorDesignObrazovky();
            vrstvy.add(panelObrazovky, Integer.valueOf(3));
            panelObrazovky.setVisible(vypinacObrazovky);
            // 2. Vytvoření interaktivních zón (tvoje přesné souřadnice)
            hotspoty.add(new InteraktivniZona(745, 476, 41, 72, "Platební terminál"));
            hotspoty.add(new InteraktivniZona(800, 330, 20, 20, "vypínač", true));
            // Bankovky
            hotspoty.add(new InteraktivniZona(532, 580, 38, 72, "100 Kč"));
            hotspoty.add(new InteraktivniZona(576, 580, 38, 72, "200 Kč"));
            hotspoty.add(new InteraktivniZona(617, 580, 38, 72, "500 Kč"));
            hotspoty.add(new InteraktivniZona(661, 580, 38, 72, "1000 Kč"));
            hotspoty.add(new InteraktivniZona(702, 580, 38, 72, "2000 Kč"));
            hotspoty.add(new InteraktivniZona(742, 580, 38, 72, "5000 Kč"));

            // Mince
            hotspoty.add(new InteraktivniZona(533, 661, 36, 36, "1 Kč", true));
            hotspoty.add(new InteraktivniZona(576, 661, 36, 36, "2 Kč", true));
            hotspoty.add(new InteraktivniZona(617, 661, 36, 36, "5 Kč", true));
            hotspoty.add(new InteraktivniZona(661, 661, 36, 36, "10 Kč", true));
            hotspoty.add(new InteraktivniZona(703, 661, 36, 36, "20 Kč", true));
            hotspoty.add(new InteraktivniZona(745, 661, 36, 36, "50 Kč", true));

            for (InteraktivniZona zona : hotspoty) {
                vrstvy.add(zona, Integer.valueOf(1));
            }

        } catch (Exception e) {
            System.err.println("Chyba při načítání prvků: " + e.getMessage());
            setBackground(Color.DARK_GRAY);
        }

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                aktualizujRozmery();
            }
        });

        add(vrstvy, BorderLayout.CENTER);
    }

    private void aktualizujRozmery() {
        int w = getWidth();
        int h = getHeight();
        vrstvy.setBounds(0, 0, w, h);

        if (bgPanel != null) bgPanel.setBounds(0, 0, w, h);
        if (settingsLabel != null) {
            settingsLabel.setBounds(w - 80, h - 80, 60, 60);
        }

        double scaleW = (double) w / 1280;
        double scaleH = (double) h / 720;

        if (panelObrazovky != null) {
            panelObrazovky.setBounds((int)(SCREEN_X * scaleW), (int)(SCREEN_Y * scaleH),
                    (int)(SCREEN_W * scaleW), (int)(SCREEN_H * scaleH));
        }

        for (InteraktivniZona zona : hotspoty) {
            zona.aktualizujPozici(scaleW, scaleH);
        }

        revalidate();
        repaint();
    }

    // ========================================================
    // KOMPLETNÍ DESIGN OBRAZOVKY POKLADNY
    // ========================================================
    private JPanel vytvorDesignObrazovky() {
        JPanel screen = new JPanel(new GridBagLayout());
        screen.setBackground(Color.BLACK);
        screen.setOpaque(true);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(1, 1, 1, 1);

        // 1. LEVÁ ČÁST
        JPanel levyPanel = new JPanel(new BorderLayout());
        levyPanel.setBackground(Color.WHITE);

        JPanel kategorie = new JPanel(new GridLayout(1, 4, 1, 1));
        kategorie.setBackground(Color.BLACK);
        kategorie.add(vytvorZalozku("PEČIVO"));
        kategorie.add(vytvorZalozku("ZELENINA"));
        kategorie.add(vytvorZalozku("OVOCE"));
        kategorie.add(vytvorZalozku("OSTATNÍ"));
        levyPanel.add(kategorie, BorderLayout.NORTH);

        JPanel seznamZbozi = new JPanel();
        seznamZbozi.setBackground(Color.WHITE);
        seznamZbozi.add(new JLabel("🍞 Rohlík    🥖 Chleba"));
        levyPanel.add(seznamZbozi, BorderLayout.CENTER);

        gbc.gridx = 0; gbc.weightx = 0.45;
        screen.add(levyPanel, gbc);

        // 2. STŘEDNÍ ČÁST (Klávesnice)
        JPanel numpadWrapper = new JPanel(new BorderLayout(0, 2));
        numpadWrapper.setBackground(Color.BLACK);

        JPanel numpadPanel = new JPanel(new GridLayout(5, 3, 1, 1));
        numpadPanel.setBackground(Color.BLACK);

        String[] klavesy = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "0", "#", ",", "<-", "↵"};

        for (String k : klavesy) {
            JButton btn = new JButton(k);

            btn.setForeground(Color.BLACK);

            if (k.equals("↵")) {
                btn.setFont(new Font("Segoe UI Symbol", Font.BOLD, 18));
                btn.setBackground(Color.green);
            } else {
                btn.setFont(new Font("Arial", Font.BOLD, 18));
                btn.setBackground(Color.WHITE);
            }

            btn.setFocusPainted(false);
            btn.setMargin(new Insets(0, 0, 0, 0));

            btn.addActionListener(e -> {
                String aktualniText = numpadDisplay.getText();

                // 2. OPRAVA: Logika pro jednotlivá speciální tlačítka
                if (k.equals("<-")) {
                    // Mazání posledního znaku
                    if (!aktualniText.isEmpty()) {
                        numpadDisplay.setText(aktualniText.substring(0, aktualniText.length() - 1));
                    }
                } else if (k.equals("↵")) {
                    // ENTER: Zde později přidáš kód pro vložení položky na účtenku
                    System.out.println("Potvrzeno (Enter): " + aktualniText);
                    numpadDisplay.setText(""); // Vymaže displej po potvrzení
                } else {
                    // Ostatní čísla a znaky se normálně připisují
                    numpadDisplay.setText(aktualniText + k);
                }
            });
            numpadPanel.add(btn);
        }
        numpadWrapper.add(numpadPanel, BorderLayout.CENTER);

        numpadDisplay = new JTextField();
        numpadDisplay.setEditable(true);
        numpadDisplay.setHorizontalAlignment(JTextField.RIGHT);
        numpadDisplay.setBackground(Color.WHITE);
        numpadDisplay.setFont(new Font("Monospaced", Font.BOLD, 20));
        numpadDisplay.setPreferredSize(new Dimension(0, 35));


        numpadDisplay.addKeyListener(new java.awt.event.KeyAdapter(){
            @Override
            public void keyTyped(java.awt.event.KeyEvent e){
                char c = e.getKeyChar();

                if(c == '.') {
                    e.setKeyChar(',');
                } else if (!Character.isDigit(c) && c != '*' && c != '#' && c != ','){
                    e.consume();
                }
            }
        });

        numpadDisplay.addActionListener(e -> {
            numpadDisplay.setText("");
        });

        numpadWrapper.add(numpadDisplay, BorderLayout.SOUTH);
        gbc.gridx = 1; gbc.weightx = 0.25;
        screen.add(numpadWrapper, gbc);

        // 3. PRAVÁ ČÁST (Účtenka)
        JPanel rightPanel = new JPanel(new BorderLayout(0, 2));
        rightPanel.setBackground(new Color(100, 120, 200));

        JTextArea uctenka = new JTextArea("NÁZEV OBCHODU\n----------------\n1x Rohlík   3 Kč\n\nCENA: 3 Kč");
        uctenka.setEditable(false);
        uctenka.setBackground(new Color(100, 120, 200));
        uctenka.setForeground(Color.BLACK);
        uctenka.setFont(new Font("Monospaced", Font.BOLD, 9));
        rightPanel.add(uctenka, BorderLayout.CENTER);

        JPanel spodniCast = new JPanel(new BorderLayout(0, 2));
        spodniCast.setBackground(new Color(100, 120, 200));

        JLabel vaha = new JLabel("Váha: -- kg", SwingConstants.CENTER);
        vaha.setFont(new Font("Monospaced", Font.BOLD, 11));
        spodniCast.add(vaha, BorderLayout.NORTH);

        JPanel tlacitka = new JPanel(new GridLayout(1, 2, 2, 0));
        tlacitka.setBackground(Color.BLACK);
        tlacitka.setPreferredSize(new Dimension(0, 35));

        JButton btnObchod = new JButton("Obchod");
        btnObchod.setBackground(new Color(60, 100, 220));
        btnObchod.setForeground(Color.BLACK);
        btnObchod.setFont(new Font("Arial", Font.BOLD, 8));
        btnObchod.setFocusPainted(false);
        btnObchod.setMargin(new Insets(0, 0, 0, 0));

        JButton btnEnter = new JButton("List");
        btnEnter.setBackground(new Color(60, 100, 220));
        btnEnter.setForeground(Color.BLACK);
        btnEnter.setFont(new Font("Arial", Font.BOLD, 10));
        btnEnter.setFocusPainted(false);
        btnEnter.setMargin(new Insets(0, 0, 0, 0));
        btnEnter.addActionListener(e -> numpadDisplay.setText(""));

        tlacitka.add(btnObchod);
        tlacitka.add(btnEnter);
        spodniCast.add(tlacitka, BorderLayout.SOUTH);

        rightPanel.add(spodniCast, BorderLayout.SOUTH);

        gbc.gridx = 2; gbc.weightx = 0.30;
        screen.add(rightPanel, gbc);

        return screen;
    }

    private JButton vytvorZalozku(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.BLACK);
        btn.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 8));
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(220, 220, 220)); }
            public void mouseExited(MouseEvent e) { btn.setBackground(Color.WHITE); }
        });
        return btn;
    }

    // ========================================================
    // TŘÍDA PRO INTERAKTIVNÍ ZÓNU (Hotspoty)
    // ========================================================
    class InteraktivniZona extends JPanel {
        private int origX, origY, origW, origH;
        private boolean jeKulaty;
        private boolean isHovered = false;
        private String nazev;

        public InteraktivniZona(int x, int y, int w, int h, String nazev) {
            this(x, y, w, h, nazev, false);
        }

        public InteraktivniZona(int x, int y, int w, int h, String nazev, boolean jeKulaty) {
            this.origX = x; this.origY = y; this.origW = w; this.origH = h;
            this.nazev = nazev;
            this.jeKulaty = jeKulaty;

            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setToolTipText(nazev);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
                @Override
                public void mouseClicked(MouseEvent e) {
                    System.out.println("Kliknuto na předmět: " + nazev);
                    if(nazev.equals("vypínač")){
                        if(vypinacObrazovky)
                            vypinacObrazovky = false;
                        else
                            vypinacObrazovky = true;
                        panelObrazovky.setVisible(vypinacObrazovky);
                    } else if (nazev.equals("Platební terminál")) {
                        // Zvuk pípnutí terminálu
                        prehrajZvuk("/pipnuti.wav");

                    } else {
                        // Zde řešíme peníze
                        if (jeKulaty) {
                            // Je to mince
                            prehrajZvuk("zvuky/mince.wav");
                        } else {
                            // Je to bankovka
                            prehrajZvuk("zvuky/bankovka.wav");
                        }
                    }


                }
            });
        }
        private void prehrajZvuk(String cestaKZvuku) {
            try {
                // Načtení zvuku ze složky resources
                java.net.URL url = getClass().getResource(cestaKZvuku);
                if (url != null) {
                    javax.sound.sampled.AudioInputStream audioIn = javax.sound.sampled.AudioSystem.getAudioInputStream(url);
                    javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
                    clip.open(audioIn);
                    clip.start(); // Spustí zvuk
                } else {
                    System.err.println("Zvuk nenalezen: " + cestaKZvuku);
                }
            } catch (Exception ex) {
                System.err.println("Chyba při přehrávání zvuku: " + ex.getMessage());
            }
        }
        public void aktualizujPozici(double scaleW, double scaleH) {
            setBounds((int)(origX * scaleW), (int)(origY * scaleH), (int)(origW * scaleW), (int)(origH * scaleH));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (isHovered) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 100));

                if (jeKulaty) {
                    g2.fillOval(0, 0, getWidth(), getHeight());
                } else {
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                g2.dispose();
            }
        }
    }
}

