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

    List<Zbozi> zboziList = new ArrayList<>();

    private JPanel panelObrazovky;
    private JTextField numpadDisplay;
    private Boolean vypinacObrazovky = true;

    // NOVÉ: Proměnné pro Fullscreen režim
    private JPanel fullscreenOverlay;
    private boolean isFullscreen = false;
    private final int FS_W = 1230;
    private final int FS_H = 667;

    private List<InteraktivniZona> hotspoty = new ArrayList<>();

    private final int SCREEN_X = 467;
    private final int SCREEN_Y = 96;
    private final int SCREEN_W = 417;
    private final int SCREEN_H = 215;

    private static final String iconPath = "/nastaveni.png";
    private static final String backgroundPath = "/pokladna.png";

    public BazalaSimulator(Menu okno) {
        this.hlavniOkno = okno;
        setLayout(new BorderLayout());
        vrstvy = new JLayeredPane();
        zboziList = SpravceSouboru.nactiZbozi();
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

            // 1. Vytvoření designu obrazovky s parametrem FALSE (malá verze)
            panelObrazovky = vytvorDesignObrazovky(false);
            vrstvy.add(panelObrazovky, Integer.valueOf(3));
            panelObrazovky.setVisible(vypinacObrazovky);

            // 2. Vytvoření interaktivních zón (přidáno slůvko 'this')
            hotspoty.add(new InteraktivniZona(745, 476, 41, 72, "Platební terminál", this));
            hotspoty.add(new InteraktivniZona(800, 330, 20, 20, "vypínač", true, this));

            // Bankovky
            hotspoty.add(new InteraktivniZona(532, 580, 38, 72, "100 Kč", this));
            hotspoty.add(new InteraktivniZona(576, 580, 38, 72, "200 Kč", this));
            hotspoty.add(new InteraktivniZona(617, 580, 38, 72, "500 Kč", this));
            hotspoty.add(new InteraktivniZona(661, 580, 38, 72, "1000 Kč", this));
            hotspoty.add(new InteraktivniZona(702, 580, 38, 72, "2000 Kč", this));
            hotspoty.add(new InteraktivniZona(742, 580, 38, 72, "5000 Kč", this));

            // Mince
            hotspoty.add(new InteraktivniZona(533, 661, 36, 36, "1 Kč", true, this));
            hotspoty.add(new InteraktivniZona(576, 661, 36, 36, "2 Kč", true, this));
            hotspoty.add(new InteraktivniZona(617, 661, 36, 36, "5 Kč", true, this));
            hotspoty.add(new InteraktivniZona(661, 661, 36, 36, "10 Kč", true, this));
            hotspoty.add(new InteraktivniZona(703, 661, 36, 36, "20 Kč", true, this));
            hotspoty.add(new InteraktivniZona(745, 661, 36, 36, "50 Kč", true, this));

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

        if (isFullscreen && fullscreenOverlay != null) {
            int fw = (int)(FS_W * scaleW);
            int fh = (int)(FS_H * scaleH);
            fullscreenOverlay.setBounds((w - fw) / 2, (h - fh) / 2, fw, fh);
            panelObrazovky.setBounds(0, 0, fw, fh);
        } else if (panelObrazovky != null) {
            panelObrazovky.setBounds((int)(SCREEN_X * scaleW), (int)(SCREEN_Y * scaleH),
                    (int)(SCREEN_W * scaleW), (int)(SCREEN_H * scaleH));
        }

        for (InteraktivniZona zona : hotspoty) {
            zona.aktualizujPozici(scaleW, scaleH);
        }

        revalidate();
        repaint();
    }

    private void toggleFullscreen() {
        isFullscreen = !isFullscreen;

        if (isFullscreen) {
            if (fullscreenOverlay == null) {
                // Tvůj obrázek přes celou obrazovku
                fullscreenOverlay = hlavniOkno.new BackgroundPanel("celaObrazovka.png");
                fullscreenOverlay.setLayout(new BorderLayout());
            }

            vrstvy.remove(panelObrazovky); // Smažeme starý malý design
            panelObrazovky = vytvorDesignObrazovky(true); // Vytvoříme NOVÝ velký
            fullscreenOverlay.removeAll();
            fullscreenOverlay.add(panelObrazovky, BorderLayout.CENTER);

            vrstvy.add(fullscreenOverlay, JLayeredPane.DRAG_LAYER);
            fullscreenOverlay.setVisible(true);
        } else {
            if (fullscreenOverlay != null) {
                fullscreenOverlay.remove(panelObrazovky);
                vrstvy.remove(fullscreenOverlay);
                fullscreenOverlay.setVisible(false);
            }
            // Vytvoříme zpět NOVÝ malý design
            panelObrazovky = vytvorDesignObrazovky(false);
            vrstvy.add(panelObrazovky, Integer.valueOf(3));
        }

        panelObrazovky.setVisible(vypinacObrazovky);
        aktualizujRozmery();
    }

    public void prepniVypinac() {
        vypinacObrazovky = !vypinacObrazovky;
        if (panelObrazovky != null) {
            panelObrazovky.setVisible(vypinacObrazovky);
        }
    }

    // ========================================================
    // KOMPLETNÍ DESIGN OBRAZOVKY POKLADNY S PARAMETREM isFs
    // ========================================================
    private JPanel vytvorDesignObrazovky(boolean isFs) {
        JPanel screen = new JPanel(new GridBagLayout());
        screen.setBackground(Color.BLACK);
        screen.setOpaque(true);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(1, 1, 1, 1);

        // 1. LEVÁ ČÁST
// 1. LEVÁ ČÁST (Produkty + Vyhledávač)
        JPanel levyPanel = new JPanel(new BorderLayout());
        levyPanel.setBackground(Color.WHITE);

        // --- HLAVIČKA LEVÉHO PANELU (Kategorie + Vyhledávání) ---
        JPanel hlavickaLevy = new JPanel(new BorderLayout());
        hlavickaLevy.setBackground(Color.WHITE);

        JPanel kategorie = new JPanel(new GridLayout(1, 4, 1, 1));
        kategorie.setBackground(Color.BLACK);
        kategorie.add(vytvorZalozku("PEČIVO", isFs));
        kategorie.add(vytvorZalozku("ZELENINA", isFs));
        kategorie.add(vytvorZalozku("OVOCE", isFs));
        kategorie.add(vytvorZalozku("OSTATNÍ", isFs));
        hlavickaLevy.add(kategorie, BorderLayout.NORTH);

        // NOVÉ: Vyhledávací pole
        JTextField vyhledavac = new JTextField();
        vyhledavac.setFont(new Font("Arial", Font.PLAIN, isFs ? 18 : 11));
        vyhledavac.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(Color.WHITE);
        JLabel searchIcon = new JLabel(" 🔍 ");
        searchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, isFs ? 18 : 12));
        searchPanel.add(searchIcon, BorderLayout.WEST);
        searchPanel.add(vyhledavac, BorderLayout.CENTER);

        hlavickaLevy.add(searchPanel, BorderLayout.SOUTH);
        levyPanel.add(hlavickaLevy, BorderLayout.NORTH);

        // --- SEZNAM ZBOŽÍ ---
        JPanel seznamZbozi = new JPanel(new GridLayout(0, 3, 2, 2));
        seznamZbozi.setBackground(Color.WHITE);

        // obalovací panel, který zarovná produkty nahoru a zakáže jejich roztahování
        JPanel obalovaciPanel = new JPanel(new BorderLayout());
        obalovaciPanel.setBackground(Color.WHITE);
        obalovaciPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        obalovaciPanel.add(seznamZbozi, BorderLayout.NORTH);

        String[][] vsechnyProdukty = {
                {"Rohlík", "101", "3 Kč", "Rohlík"},
                {"Chleba_Šumava", "102", "35 Kč", "Šumava"},
                {"jablko", "201", "112 Kč", "Jablko"},
                {"banan", "202", "800 Kč", "Banán"},
                {"mleko", "301", "225 Kč", "Mléko"},
                {"vejce", "302", "40 Kč", "Vejce"},
                {"maslo", "303", "55 Kč", "Máslo"}
        };

        // Funkce pro naplnění/aktualizaci seznamu podle hledaného textu
        Runnable aktualizujSeznam = () -> {
            String hledanyText = vyhledavac.getText().toLowerCase();
            seznamZbozi.removeAll();

            for (Zbozi z : zboziList) {
                if (z.nazev.toLowerCase().contains(hledanyText) || (z.id + "").contains(hledanyText)) {
                    seznamZbozi.add(vytvorProduktPanel(z.nazev, z.id, z.cena, z.zkracenyNazev, isFs));
                }
            }
            // ZMĚNA: Musíme říct i obalovacímu panelu, ať se přepočítá
            obalovaciPanel.revalidate();
            obalovaciPanel.repaint();
        };

        aktualizujSeznam.run();

        // Listener na vyhledávací políčko
        vyhledavac.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { aktualizujSeznam.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { aktualizujSeznam.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { aktualizujSeznam.run(); }
        });

        // ZMĚNA: Do posuvníku teď vkládáme náš 'obalovaciPanel' místo samotného 'seznamZbozi'
        JScrollPane scrollPane = new JScrollPane(obalovaciPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        levyPanel.add(scrollPane, BorderLayout.CENTER);

        gbc.gridx = 0; gbc.weightx = isFs ? 0.45 : 0.42;
        screen.add(levyPanel, gbc);

        // 2. STŘEDNÍ ČÁST (Klávesnice)
        JPanel numpadWrapper = new JPanel(new BorderLayout(0, 2));
        numpadWrapper.setBackground(Color.BLACK);

        JPanel numpadPanel = new JPanel(new GridLayout(5, 3, 2, 2));
        numpadPanel.setBackground(Color.BLACK);

        String[] klavesy = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "0", "#", ",", "<-", "↵"};

        for (String k : klavesy) {
            JButton btn = new JButton(k) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    if (k.equals("↵")) {
                        if (getModel().isPressed()) g2.setColor(new Color(0, 180, 0));
                        else if (getModel().isRollover()) g2.setColor(new Color(50, 255, 50));
                        else g2.setColor(new Color(0, 200, 0));
                    } else if (k.equals("<-")) {
                        if (getModel().isPressed()) g2.setColor(new Color(200, 50, 50));
                        else if (getModel().isRollover()) g2.setColor(new Color(255, 100, 100));
                        else g2.setColor(new Color(220, 80, 80));
                    } else {
                        if (getModel().isPressed()) g2.setColor(new Color(200, 200, 200));
                        else if (getModel().isRollover()) g2.setColor(new Color(240, 240, 240));
                        else g2.setColor(Color.WHITE);
                    }

                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.setColor(new Color(150, 150, 150));
                    g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };

            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createEmptyBorder());
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            if (k.equals("↵")) {
                btn.setFont(new Font("Segoe UI Symbol", Font.BOLD, isFs ? 40 : 22));
                btn.setForeground(Color.WHITE);
            } else if (k.equals("<-")) {
                btn.setFont(new Font("Arial", Font.BOLD, isFs ? 30 : 18));
                btn.setForeground(Color.WHITE);
            } else {
                btn.setFont(new Font("Arial", Font.BOLD, isFs ? 34 : 18));
                btn.setForeground(new Color(40, 40, 40));
            }

            btn.addActionListener(e -> {
                String aktualniText = numpadDisplay.getText();
                if (k.equals("<-")) {
                    if (!aktualniText.isEmpty()) {
                        numpadDisplay.setText(aktualniText.substring(0, aktualniText.length() - 1));
                    }
                } else if (k.equals("↵")) {
                    numpadDisplay.setText("");
                } else {
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
        numpadDisplay.setFont(new Font("Monospaced", Font.BOLD, isFs ? 40 : 20));
        numpadDisplay.setPreferredSize(new Dimension(0, isFs ? 70 : 45));

        numpadDisplay.addKeyListener(new java.awt.event.KeyAdapter(){
            @Override
            public void keyTyped(java.awt.event.KeyEvent e){
                char c = e.getKeyChar();
                if(c == '.') e.setKeyChar(',');
                else if (!Character.isDigit(c) && c != '*' && c != '#' && c != ',') e.consume();
            }
        });

        numpadDisplay.addActionListener(e -> numpadDisplay.setText(""));

        numpadWrapper.add(numpadDisplay, BorderLayout.SOUTH);

        // ZMĚNA: Klávesnice má 28 % (je to kompromis, aby se produkty vešly)
        gbc.gridx = 1; gbc.weightx = isFs ? 0.20 : 0.28;
        screen.add(numpadWrapper, gbc);

        // 3. PRAVÁ ČÁST (Účtenka)
        JPanel rightPanel = new JPanel(new BorderLayout(0, 2));
        rightPanel.setBackground(new Color(100, 120, 200));

        JTextArea uctenka = new JTextArea("NÁZEV OBCHODU\n----------------\n1x Rohlík   3 Kč\n\nCENA: 3 Kč");
        uctenka.setEditable(false);
        uctenka.setBackground(new Color(100, 120, 200));
        uctenka.setForeground(Color.BLACK);
        uctenka.setFont(new Font("Monospaced", Font.BOLD, isFs ? 18 : 9));
        rightPanel.add(uctenka, BorderLayout.CENTER);

        JPanel spodniCast = new JPanel(new BorderLayout(0, 2));
        spodniCast.setBackground(new Color(100, 120, 200));

        JLabel vaha = new JLabel("Váha: -- kg", SwingConstants.CENTER);
        vaha.setFont(new Font("Monospaced", Font.BOLD, isFs ? 22 : 11));
        spodniCast.add(vaha, BorderLayout.NORTH);

        JPanel tlacitka = new JPanel(new GridLayout(1, 2, 4, 0));
        tlacitka.setBackground(new Color(100, 120, 200));
        tlacitka.setPreferredSize(new Dimension(0, isFs ? 70 : 40));

        JButton btnObchod = vytvorModreTlacitko("Obchod", isFs);
        JButton btnFullscreen = vytvorModreTlacitko("Fullscreen", isFs);
        btnFullscreen.addActionListener(e -> toggleFullscreen());

        tlacitka.add(btnObchod);
        tlacitka.add(btnFullscreen);
        spodniCast.add(tlacitka, BorderLayout.SOUTH);

        rightPanel.add(spodniCast, BorderLayout.SOUTH);

        // ZMĚNA: Účtenka má 24 %
        gbc.gridx = 2; gbc.weightx = isFs ? 0.35 : 0.24;
        screen.add(rightPanel, gbc);

        return screen;
    }

    // ========================================================
    // POMOCNÉ METODY PRO VZHLED TLAČÍTEK
    // ========================================================
    private JButton vytvorZalozku(String text, boolean isFs) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) g2.setColor(new Color(200, 200, 200));
                else if (getModel().isRollover()) g2.setColor(new Color(235, 235, 235));
                else g2.setColor(Color.WHITE);

                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(180, 180, 180));
                g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setForeground(new Color(40, 40, 40));

        // Větší písmo záložek ve FS
        btn.setFont(new Font("Arial", Font.BOLD, isFs ? 18 : 9));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return btn;
    }

    private JButton vytvorModreTlacitko(String text, boolean isFs) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) g2.setColor(new Color(40, 80, 180));
                else if (getModel().isRollover()) g2.setColor(new Color(80, 120, 240));
                else g2.setColor(new Color(60, 100, 220));

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(30, 60, 150));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setForeground(Color.WHITE);

        // Větší tlačítka
        btn.setFont(new Font("Arial", Font.BOLD, isFs ? 22 : 11));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return btn;
    }

    // ========================================================
    // POMOCNÁ METODA PRO VYTVOŘENÍ KARTIČKY PRODUKTU
    // ========================================================
    private JPanel vytvorProduktPanel(String obrazekCesta, int id, int cena, String nazev, boolean isFs) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // ZMĚNA: V malém režimu zmenšíme obrázek na 32 px, ať se tam 3 sloupce vlezou
        int imgSize = isFs ? 80 : 32;

        JLabel lblObrazek = new JLabel("", SwingConstants.CENTER);
        try {
            java.net.URL imgUrl = getClass().getResource("zboziObrazky/" + obrazekCesta + ".png");
            if (imgUrl != null) {
                ImageIcon icon = new ImageIcon(imgUrl);
                Image img = icon.getImage().getScaledInstance(imgSize, imgSize, Image.SCALE_SMOOTH);
                lblObrazek.setIcon(new ImageIcon(img));
            } else {
                lblObrazek.setText("🖼️");
                lblObrazek.setFont(new Font("Segoe UI Emoji", Font.PLAIN, isFs ? 50 : 24));
            }
        } catch (Exception e) {
            lblObrazek.setText("🖼️");
            lblObrazek.setFont(new Font("Segoe UI Emoji", Font.PLAIN, isFs ? 50 : 24));
        }
        panel.add(lblObrazek, BorderLayout.CENTER);

        // Texty dolů
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel lblIdCena = new JLabel(id + " | " + cena + " Kč");
        lblIdCena.setAlignmentX(Component.CENTER_ALIGNMENT);
        // ZMĚNA: Extra malý font (8) pro malou obrazovku, aby neroztahoval buňku
        lblIdCena.setFont(new Font("Arial", Font.PLAIN, isFs ? 18 : 8));
        lblIdCena.setForeground(new Color(100, 100, 100));

        JLabel lblNazev = new JLabel(nazev);
        lblNazev.setAlignmentX(Component.CENTER_ALIGNMENT);
        // ZMĚNA: Zmenšení názvu pro malý monitor na 10
        lblNazev.setFont(new Font("Arial", Font.BOLD, isFs ? 22 : 10));
        lblNazev.setForeground(Color.BLACK);

        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(lblIdCena);
        textPanel.add(Box.createVerticalStrut(1));
        textPanel.add(lblNazev);
        textPanel.add(Box.createVerticalStrut(4));

        panel.add(textPanel, BorderLayout.SOUTH);

        // Interaktivita
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                numpadDisplay.setText(id + "");
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                panel.setBackground(new Color(230, 240, 255));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                panel.setBackground(Color.WHITE);
            }
        });

        return panel;
    }
}