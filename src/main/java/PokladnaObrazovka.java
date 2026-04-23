import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class PokladnaObrazovka extends JPanel {
    private int aktualniKategorie;
    private JTextField numpadDisplay;
    private JTextField vyhledavac;
    private List<Zbozi> zboziList;
    private boolean isFs;
    private Runnable onToggleFullscreen;
    private Runnable aktualizujSeznam;

    // NOVÉ: Seznam pro ukládání nákupu
    private List<Zbozi> polozkyNaUctence;

    // UPRAVENÝ KONSTRUKTOR: Přijímá stav z minulé obrazovky (včetně účtenky)
    public PokladnaObrazovka(boolean isFs, List<Zbozi> zboziList, Runnable onToggleFullscreen,
                             int startKategorie, String startHledani, String startNumpad,
                             List<Zbozi> startUctenka) {
        this.isFs = isFs;
        this.zboziList = zboziList;
        this.onToggleFullscreen = onToggleFullscreen;

        this.aktualniKategorie = startKategorie;
        this.polozkyNaUctence = startUctenka != null ? new ArrayList<>(startUctenka) : new ArrayList<>();

        setLayout(new GridBagLayout());
        setBackground(Color.BLACK);
        setOpaque(true);

        vytvorDesign(startHledani, startNumpad);
    }

    private void vytvorDesign(String startHledani, String startNumpad) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(1, 1, 1, 1);

        // ==========================================
        // 1. LEVÁ ČÁST (Produkty + Vyhledávač)
        // ==========================================
        JPanel levyPanel = new JPanel(new BorderLayout());
        levyPanel.setBackground(Color.WHITE);

        vyhledavac = new JTextField(startHledani);
        vyhledavac.setFont(new Font("Arial", Font.PLAIN, isFs ? 18 : 11));
        vyhledavac.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));

        JPanel seznamZbozi = new JPanel(new GridLayout(0, 3, 2, 2));
        seznamZbozi.setBackground(Color.WHITE);

        JPanel obalovaciPanel = new JPanel(new BorderLayout());
        obalovaciPanel.setBackground(Color.WHITE);
        obalovaciPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        obalovaciPanel.add(seznamZbozi, BorderLayout.NORTH);

        aktualizujSeznam = () -> {
            String hledanyText = vyhledavac.getText().toLowerCase();
            seznamZbozi.removeAll();

            for (Zbozi z : zboziList) {
                boolean odpovidaTextu = z.nazev.toLowerCase().contains(hledanyText) || (z.id + "").contains(hledanyText);
                boolean odpovidaKategorii = (aktualniKategorie == 5) || (z.typ == aktualniKategorie);

                if (odpovidaTextu && odpovidaKategorii) {
                    seznamZbozi.add(vytvorProduktPanel(z.nazev, z.id, z.cena, z.zkracenyNazev));
                }
            }
            obalovaciPanel.revalidate();
            obalovaciPanel.repaint();
        };

        aktualizujSeznam.run();

        vyhledavac.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { aktualizujSeznam.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { aktualizujSeznam.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { aktualizujSeznam.run(); }
        });

        JPanel hlavickaLevy = new JPanel(new BorderLayout());
        hlavickaLevy.setBackground(Color.WHITE);

        JPanel kategorie = new JPanel(new GridLayout(1, 4, 1, 1));
        kategorie.setBackground(Color.BLACK);
        kategorie.add(vytvorZalozku("PEČIVO", 1));
        kategorie.add(vytvorZalozku("ZELENINA", 3));
        kategorie.add(vytvorZalozku("OVOCE", 2));
        kategorie.add(vytvorZalozku("OSTATNÍ", 0));
        hlavickaLevy.add(kategorie, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(Color.WHITE);
        JLabel searchIcon = new JLabel(" 🔍 ");
        searchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, isFs ? 18 : 12));
        searchPanel.add(searchIcon, BorderLayout.WEST);
        searchPanel.add(vyhledavac, BorderLayout.CENTER);

        hlavickaLevy.add(searchPanel, BorderLayout.SOUTH);
        levyPanel.add(hlavickaLevy, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(obalovaciPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        levyPanel.add(scrollPane, BorderLayout.CENTER);

        gbc.gridx = 0; gbc.weightx = isFs ? 0.45 : 0.42;
        add(levyPanel, gbc);

        // ==========================================
        // 3. PRAVÁ ČÁST (Účtenka) - Přesunuto výše kvůli klávesnici
        // ==========================================
        JPanel rightPanel = new JPanel(new BorderLayout(0, 2));
        rightPanel.setBackground(new Color(100, 120, 200));

        JTextArea uctenkaText = new JTextArea();
        uctenkaText.setEditable(false);
        uctenkaText.setBackground(new Color(100, 120, 200));
        uctenkaText.setForeground(Color.BLACK);
        uctenkaText.setFont(new Font("Monospaced", Font.BOLD, isFs ? 18 : 9));
        rightPanel.add(new JScrollPane(uctenkaText), BorderLayout.CENTER);


        Runnable prekresliUctenku = () -> {
            StringBuilder sb = new StringBuilder("NÁZEV OBCHODU\n----------------\n");
            int celkem = 0;
            for(Zbozi z : polozkyNaUctence) {
                // Cena za položku * počet kusů
                int cenaZaVsechny = z.cena * z.maxPocet;
                sb.append(z.maxPocet).append("x ").append(z.nazev).append("\n")
                        .append(cenaZaVsechny).append(" Kč\n");
                celkem += cenaZaVsechny;
            }
            sb.append("\n----------------\nCENA: ").append(celkem).append(" Kč");
            uctenkaText.setText(sb.toString());
        };
        prekresliUctenku.run();

        JPanel spodniCast = new JPanel(new BorderLayout(0, 2));
        spodniCast.setBackground(new Color(100, 120, 200));

        JLabel vaha = new JLabel("Váha: -- kg", SwingConstants.CENTER);
        vaha.setFont(new Font("Monospaced", Font.BOLD, isFs ? 22 : 11));
        spodniCast.add(vaha, BorderLayout.NORTH);

        JPanel tlacitka = new JPanel(new GridLayout(1, 2, 4, 0));
        tlacitka.setBackground(new Color(100, 120, 200));
        tlacitka.setPreferredSize(new Dimension(0, isFs ? 70 : 40));

        JButton btnObchod = vytvorModreTlacitko("Obchod");
        JButton btnFullscreen = vytvorModreTlacitko("Fullscreen");
        btnFullscreen.addActionListener(e -> onToggleFullscreen.run());

        tlacitka.add(btnObchod);
        tlacitka.add(btnFullscreen);
        spodniCast.add(tlacitka, BorderLayout.SOUTH);

        rightPanel.add(spodniCast, BorderLayout.SOUTH);


        // ==========================================
        // 2. STŘEDNÍ ČÁST (Klávesnice)
        // ==========================================
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
                    if (!aktualniText.isEmpty()) numpadDisplay.setText(aktualniText.substring(0, aktualniText.length() - 1));
                } else if (k.equals("↵")) {
                    if (!aktualniText.isEmpty()) {
                        try {
                            // Rozdělení textu podle hvězdičky
                            String[] casti = aktualniText.split("\\*");
                            int zadaneMnozstvi;
                            if (casti.length == 2 || casti.length == 1) {
                                int hledaneId = Integer.parseInt(casti[0]);

                                if(casti.length == 1){
                                    zadaneMnozstvi = 1;
                                } else{
                                    zadaneMnozstvi = Integer.parseInt(casti[1]);
                                }
                                for (Zbozi z : zboziList) {
                                    if (z.id == hledaneId) {
                                        // VYTVOŘENÍ KOPIE: Abychom nezměnili původní maxPocet v databázi
                                        Zbozi polozka = new Zbozi(z.nazev, z.id, z.typ, z.cena, z.minVaha,
                                                z.maxVaha, z.sance, zadaneMnozstvi,
                                                z.xp, z.lvlOdemknuti, z.zkracenyNazev);

                                        polozkyNaUctence.add(polozka);
                                        prekresliUctenku.run();
                                        break;
                                    }
                                }
                            }
                        } catch (NumberFormatException ex) {

                        }
                    }
                    numpadDisplay.setText("");
                } else {
                    numpadDisplay.setText(aktualniText + k);
                }
            });
            numpadPanel.add(btn);
        }
        numpadWrapper.add(numpadPanel, BorderLayout.CENTER);

        numpadDisplay = new JTextField(startNumpad);
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
        numpadWrapper.add(numpadDisplay, BorderLayout.SOUTH);

        gbc.gridx = 1; gbc.weightx = isFs ? 0.20 : 0.28;
        add(numpadWrapper, gbc);

        // Vložení pravého panelu na konec
        gbc.gridx = 2; gbc.weightx = isFs ? 0.35 : 0.24;
        add(rightPanel, gbc);
    }

    // ==========================================
    // POMOCNÉ METODY PRO VZHLED
    // ==========================================
    private JButton vytvorZalozku(String text, int idKategorie) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (aktualniKategorie == idKategorie) g2.setColor(new Color(200, 220, 255));
                else if (getModel().isPressed()) g2.setColor(new Color(200, 200, 200));
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
        btn.setFont(new Font("Arial", Font.BOLD, isFs ? 18 : 9));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            if (aktualniKategorie == idKategorie) aktualniKategorie = 5;
            else aktualniKategorie = idKategorie;

            aktualizujSeznam.run();

            Container parent = btn.getParent();
            if (parent != null) parent.repaint();
        });

        return btn;
    }

    private JButton vytvorModreTlacitko(String text) {
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
        btn.setFont(new Font("Arial", Font.BOLD, isFs ? 22 : 11));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return btn;
    }

    private JPanel vytvorProduktPanel(String obrazekCesta, int id, int cena, String nazev) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

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

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel lblIdCena = new JLabel(id + " | " + cena + " Kč");
        lblIdCena.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblIdCena.setFont(new Font("Arial", Font.PLAIN, isFs ? 18 : 8));
        lblIdCena.setForeground(new Color(100, 100, 100));

        JLabel lblNazev = new JLabel(nazev);
        lblNazev.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblNazev.setFont(new Font("Arial", Font.BOLD, isFs ? 22 : 10));
        lblNazev.setForeground(Color.BLACK);

        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(lblIdCena);
        textPanel.add(Box.createVerticalStrut(1));
        textPanel.add(lblNazev);
        textPanel.add(Box.createVerticalStrut(4));

        panel.add(textPanel, BorderLayout.SOUTH);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (numpadDisplay != null) numpadDisplay.setText(id + "");
            }
            @Override
            public void mouseEntered(MouseEvent e) { panel.setBackground(new Color(230, 240, 255)); }
            @Override
            public void mouseExited(MouseEvent e) { panel.setBackground(Color.WHITE); }
        });

        return panel;
    }

    // ==========================================
    // GETTERY PRO ULOŽENÍ STAVU PŘED PŘEPNUTÍM
    // ==========================================
    public int getAktualniKategorie() {
        return aktualniKategorie;
    }

    public String getHledanyText() {
        return vyhledavac != null ? vyhledavac.getText() : "";
    }

    public String getNumpadText() {
        return numpadDisplay != null ? numpadDisplay.getText() : "";
    }

    public List<Zbozi> getPolozkyNaUctence() {
        return polozkyNaUctence;
    }
}