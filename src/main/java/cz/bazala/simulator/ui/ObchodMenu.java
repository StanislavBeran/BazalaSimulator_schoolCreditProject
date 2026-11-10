package cz.bazala.simulator.ui;

import cz.bazala.simulator.model.*;
import cz.bazala.simulator.io.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.List;

public class ObchodMenu extends JPanel {
    private BazalaSimulator simulator;
    private int aktualniKategorie = 1;
    private JPanel seznamVylepseni;

    public ObchodMenu(BazalaSimulator simulator) {
        this.simulator = simulator;

        setLayout(new GridBagLayout());
        setOpaque(false);

        addMouseListener(new MouseAdapter() {});

        JPanel oknoPanel = new JPanel(new BorderLayout());
        oknoPanel.setBackground(Color.WHITE);
        oknoPanel.setPreferredSize(new Dimension(850, 550));
        oknoPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));

        // --- HLAVIČKA ---
        JPanel hlavicka = new JPanel(new BorderLayout());
        JPanel kategorie = new JPanel(new GridLayout(1, 3, 1, 1));
        kategorie.setBackground(Color.BLACK);

        kategorie.add(vytvorZalozku("ZBOŽÍ", 1));
        kategorie.add(vytvorZalozku("VYLEPŠENÍ", 2));
        kategorie.add(vytvorZalozku("OSTATNÍ", 3));
        hlavicka.add(kategorie, BorderLayout.NORTH);

        JLabel nadpis = new JLabel("OBCHOD", SwingConstants.LEFT);
        nadpis.setFont(new Font("Arial", Font.BOLD, 24));
        nadpis.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        hlavicka.add(nadpis, BorderLayout.SOUTH);
        oknoPanel.add(hlavicka, BorderLayout.NORTH);

        // --- TĚLO OBCHODU ---
        seznamVylepseni = new JPanel(new GridLayout(0, 4, 10, 10)); // 4 sloupce
        seznamVylepseni.setBackground(Color.WHITE);

        JPanel obalovaciPanel = new JPanel(new BorderLayout());
        obalovaciPanel.setBackground(Color.WHITE);
        obalovaciPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        obalovaciPanel.add(seznamVylepseni, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(obalovaciPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        oknoPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel paticka = new JPanel(new BorderLayout());
        paticka.setBackground(Color.WHITE);
        paticka.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setOpaque(false);
        JLabel lblPenizeInfo = new JLabel();
        lblPenizeInfo.setFont(new Font("Arial", Font.BOLD, 16));
        infoPanel.add(lblPenizeInfo);
        paticka.add(infoPanel, BorderLayout.WEST);
        if (simulator != null && isVisible()) {
            lblPenizeInfo.setText("Peníze: " + simulator.getAktualniPenize() + " Kč | Level: " + simulator.getLevel());
        }
        JButton btnZavrit = new JButton("Zpět");
        btnZavrit.setFont(new Font("Arial", Font.BOLD, 16));
        btnZavrit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnZavrit.addActionListener(e -> setVisible(false));
        paticka.add(btnZavrit, BorderLayout.EAST);

        oknoPanel.add(paticka, BorderLayout.SOUTH);
        add(oknoPanel);

        aktualizujNabidku();
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        // Otevřený obchod zastaví pás, zavření ho obnoví.
        if (simulator != null) {
            if (visible) simulator.pozastavHru();
            else simulator.obnovHru();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }

    public void aktualizujNabidku() {
        seznamVylepseni.removeAll();

        if (aktualniKategorie == 1) {
            List<Zbozi> zbozi = simulator.getZboziList();
            if (zbozi != null) {
                List<Zbozi> setrideneZbozi = new java.util.ArrayList<>(zbozi);
                setrideneZbozi.sort((z1, z2) -> Integer.compare(z1.lvlOdemknuti, z2.lvlOdemknuti));
                for (Zbozi z : setrideneZbozi) {

                    boolean vlastni = simulator.getOdemceneZbozi().contains(z.id);
                    int cenaOdemknuti = vypocitejCenuOdemknuti(z);
                    boolean maPenizeALevel = simulator.getAktualniPenize() >= cenaOdemknuti && simulator.getLevel() >= z.lvlOdemknuti;

                    JPanel polozka = new JPanel(new BorderLayout());
                    polozka.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                    polozka.setBackground(vlastni ? Color.WHITE : new Color(220, 220, 220));

                    JLabel lblObrazek = new JLabel("", SwingConstants.CENTER);
                    lblObrazek.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
                    try {
                        java.net.URL imgUrl = getClass().getResource("/zboziObrazky/" + z.nazev + ".png");
                        if (imgUrl != null) {
                            ImageIcon icon = new ImageIcon(imgUrl);
                            Image img = icon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
                            if (!vlastni) {
                                lblObrazek.setIcon(new ImageIcon(GrayFilter.createDisabledImage(img)));
                            } else {
                                lblObrazek.setIcon(new ImageIcon(img));
                            }
                        } else {
                            lblObrazek.setText("🖼️");
                            lblObrazek.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
                        }
                    } catch (Exception e) {
                        lblObrazek.setText("🖼️");
                        lblObrazek.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
                    }

                    String mnozstviText = (z.minVaha > 0) ? "Váha: " + z.minVaha + "-" + z.maxVaha + " g" : "Max kusů: " + z.maxPocet;
                    String upozorneniLevel = (!vlastni && simulator.getLevel() < z.lvlOdemknuti) ? "<font color='red'>" : "";

                    String cistyNazev = z.nazev.replace('_', ' ');
                    int velikostPisma = cistyNazev.length() > 15 ? 11 : 14;

                    String infoHtml = "<html><div style='width: 140px; text-align: center; padding: 5px;'>"
                            + "<b style='font-size:" + velikostPisma + "px;'>" + cistyNazev + "</b><br>"
                            + "<i style='font-size:10px;'>" + getTypText(z.typ) + "</i><br><br>"
                            + "<span style='font-size:11px;'>" + mnozstviText + "</span><br>"
                            + upozorneniLevel + "<span style='font-size:11px;'>Od levelu: " + z.lvlOdemknuti + "</span>" + (upozorneniLevel.isEmpty() ? "" : "</font>") + "<br>"
                            + "<span style='font-size:11px;'>Prodej za: " + z.cena + " Kč</span><br><br>"
                            + "<b style='font-size:16px; color:#1a5276;'>" + (vlastni ? "ZAKOUPENO" : (simulator.getAktualniPenize()>=cenaOdemknuti ? cenaOdemknuti : "<font color='red'>" + cenaOdemknuti) + " Kč") + "</b>"
                            + "</div></html>";

                    JLabel lblInfo = new JLabel(infoHtml, SwingConstants.CENTER);
                    JPanel stredPanel = new JPanel(new BorderLayout());
                    stredPanel.setOpaque(false);
                    stredPanel.add(lblObrazek, BorderLayout.NORTH);
                    stredPanel.add(lblInfo, BorderLayout.CENTER);

                    polozka.add(stredPanel, BorderLayout.CENTER);

                    JButton btnKoupit = new JButton(vlastni ? "Vlastníš" : "Koupit");
                    btnKoupit.setCursor(new Cursor(Cursor.HAND_CURSOR));

                    if (vlastni) {
                        btnKoupit.setEnabled(false);
                        btnKoupit.setBackground(new Color(150, 220, 150));
                    } else {
                        btnKoupit.setEnabled(maPenizeALevel);
                        btnKoupit.addActionListener(e -> {
                            simulator.odectiPenize(cenaOdemknuti);
                            simulator.odemkniZbozi(z.id);
                            aktualizujNabidku();
                        });
                    }
                    polozka.add(btnKoupit, BorderLayout.SOUTH);
                    seznamVylepseni.add(polozka);
                }
            }
        } else if (aktualniKategorie == 2){
            for (StavHry.Vylepseni v : StavHry.Vylepseni.values()) {
                seznamVylepseni.add(vytvorPolozkuVylepseni(v));
            }
        } else {
            String[][] dostupnaHudba = {
                    {"Tereza Kerndlová - Schody z nebe", "Tereza_Kerndlová_-_Schody_z_nebe", "500"},
                    {"Athena Chlebová - Večerka", "Athena_Chlebová_-_Večerka", "1000"},
                    {"Filip Dang - Pojď nakoupit bejbe", "Filip_Dang_-_Pojď_nakoupit_bejbe", "1500"},
                    {"Filip Dang - Štědrá večerka", "Filip_Dang_-_Štědrá_večerka", "2500"},
                    {"Gala - Freed from desire", "Gala_-_Freed_from_desire", "2000"},
                    {"Michael Jackson - Billie Jean", "Michael_Jackson_-_Billie_Jean", "1000"},
                    {"Filip Dang & Petr Pinkas - Čeká mě sleva", "Filip_Dang_&_Petr_Pinkas_-_Čeká_mě_sleva", "3000"}
            };

            for (String[] h : dostupnaHudba) {
                String nazev = h[0];
                String soubor = h[1];
                int cena = Integer.parseInt(h[2]);
                boolean vlastni = simulator.getOdemcenaHudba().contains(soubor);
                boolean maPenize = simulator.getAktualniPenize() >= cena;

                JPanel polozka = new JPanel(new BorderLayout());
                polozka.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                polozka.setBackground(vlastni ? Color.WHITE : new Color(220, 220, 220));

                JLabel lblObrazek = new JLabel("", SwingConstants.CENTER);
                lblObrazek.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
                try {
                    java.net.URL imgUrl = getClass().getResource("/hudbaObrazky/" + soubor + ".png");
                    if (imgUrl != null) {
                        ImageIcon icon = new ImageIcon(imgUrl);
                        Image img = icon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
                        if (!vlastni) {
                            lblObrazek.setIcon(new ImageIcon(GrayFilter.createDisabledImage(img)));
                        } else {
                            lblObrazek.setIcon(new ImageIcon(img));
                        }
                    } else {
                        lblObrazek.setText("🖼️");
                        lblObrazek.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
                    }
                } catch (Exception e) {
                    lblObrazek.setText("🖼️");
                    lblObrazek.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
                }
                String[] casti = nazev.split(" - ");
                String nazevHudby = casti[1];
                int velikostPismaHudba = nazevHudby.length() > 15 ? 11 : 14;
                String infoHtml = "<html><div style='width: 140px; text-align: center; padding: 5px;'>"
                        + "<b style='font-size:" + velikostPismaHudba + "px;'>" + nazevHudby + "</b><br>"
                        + "<i style='font-size:10px;'>Hudba</i><br><br>"
                        + "<span style='font-size:11px;'>" + casti[0] + "</span><br>"
                        + "<span style='font-size:11px;'>|</span><br>"
                        + "<span style='font-size:11px;'>Změna v Nastavení</span><br><br>"
                        + "<b style='font-size:16px; color:#1a5276;'>" + (vlastni ? "ZAKOUPENO" : (maPenize ? cena : "<font color='red'>" + cena) + " Kč") + "</b>"
                        + "</div></html>";

                JLabel lblInfo = new JLabel(infoHtml, SwingConstants.CENTER);
                JPanel stredPanel = new JPanel(new BorderLayout());
                stredPanel.setOpaque(false);
                stredPanel.add(lblObrazek, BorderLayout.NORTH);
                stredPanel.add(lblInfo, BorderLayout.CENTER);

                polozka.add(stredPanel, BorderLayout.CENTER);

                JButton btnKoupit = new JButton(vlastni ? "Vlastníš" : "Koupit");
                btnKoupit.setCursor(new Cursor(Cursor.HAND_CURSOR));

                if (vlastni) {
                    btnKoupit.setEnabled(false);
                    btnKoupit.setBackground(new Color(150, 220, 150));
                } else {
                    btnKoupit.setEnabled(maPenize);
                    btnKoupit.addActionListener(e -> {
                        if (simulator.kupHudbu(soubor, cena)) {
                            aktualizujNabidku();
                        }
                    });
                }

                polozka.add(btnKoupit, BorderLayout.SOUTH);
                seznamVylepseni.add(polozka);
            }
        }

        seznamVylepseni.revalidate();
        seznamVylepseni.repaint();
    }

    private String getTypText(int typ) {
        return TypZbozi.zKodu(typ).getNazev();
    }

    /** Lidský název vylepšení pro obchod. */
    private String nazevVylepseni(StavHry.Vylepseni v) {
        switch (v) {
            case RYCHLOST_PASU: return "Nový motor pásu";
            case BONUS_ODMEN: return "Věrnostní program";
            case TRPELIVOST: return "Příjemné prostředí";
            case DRUHY_SCANNER: return "Druhý scanner";
            case AUTO_SKEN_KUSOVE: return "Chytré kódy";
            default: return v.name();
        }
    }

    /** Popis efektu vylepšení. */
    private String popisVylepseni(StavHry.Vylepseni v) {
        switch (v) {
            case RYCHLOST_PASU: return "Zrychlí pohyb zboží po páse, zkrátí prodlevu generování a urychlí úklid.";
            case BONUS_ODMEN: return "Zvýší peníze i XP za každý nákup (+15 % za úroveň).";
            case TRPELIVOST: return "Zákazníci vydrží déle v klidu (+30 % trpělivosti za úroveň).";
            case DRUHY_SCANNER: return "Umožní mít na scanneru dvě položky zároveň.";
            case AUTO_SKEN_KUSOVE: return "Kusové zboží (s kódem) se odbaví samo bez klikání.";
            default: return "";
        }
    }

    /** Cena dalšího stupně daného vylepšení podle aktuální úrovně. */
    private int cenaVylepseni(StavHry.Vylepseni v, int uroven) {
        switch (v) {
            case RYCHLOST_PASU: return 5000 * (uroven + 1);
            case BONUS_ODMEN: return 4000 * (uroven + 1);
            case TRPELIVOST: return 3500 * (uroven + 1);
            case DRUHY_SCANNER: return 8000;
            case AUTO_SKEN_KUSOVE: return 10000;
            default: return 5000 * (uroven + 1);
        }
    }

    private JPanel vytvorPolozkuVylepseni(StavHry.Vylepseni v) {
        int uroven = simulator.getUrovenVylepseni(v);
        boolean jeMax = (uroven >= v.maxUroven);
        int cena = cenaVylepseni(v, uroven);
        boolean maPenize = simulator.getAktualniPenize() >= cena;

        JPanel polozka = new JPanel(new BorderLayout());
        polozka.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        polozka.setBackground(jeMax ? new Color(255, 235, 150) : Color.WHITE);

        // Přepínatelná vylepšení (max úroveň 1) ukazují Zap/Vyp místo úrovně.
        String urovenText = (v.maxUroven == 1)
                ? (jeMax ? "Stav: ZAPNUTO" : "Stav: vypnuto")
                : ("Úroveň: " + uroven + " / " + v.maxUroven);

        String infoHtml = "<html><div style='width: 150px; text-align: center; padding: 5px;'>"
                + "<b style='font-size:15px;'>" + nazevVylepseni(v) + "</b><br><br>"
                + "<span style='font-size:11px;'>" + popisVylepseni(v) + "</span><br><br>"
                + "<b style='font-size:13px;'>" + urovenText + "</b><br><br>"
                + "<b style='font-size:16px; color:#1a5276;'>"
                + (jeMax ? "MAXIMÁLNÍ ÚROVEŇ" : (maPenize ? cena : "<font color='red'>" + cena) + " Kč")
                + "</b></div></html>";

        JLabel lblInfo = new JLabel(infoHtml, SwingConstants.CENTER);
        polozka.add(lblInfo, BorderLayout.CENTER);

        JButton btnKoupit = new JButton(jeMax ? "Vylepšeno naplno" : "Vylepšit");
        btnKoupit.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (jeMax) {
            btnKoupit.setEnabled(false);
            btnKoupit.setBackground(new Color(255, 215, 0));
        } else {
            btnKoupit.setEnabled(maPenize);
            btnKoupit.addActionListener(e -> {
                if (simulator.kupVylepseni(v, cena)) {
                    aktualizujNabidku();
                }
            });
        }
        polozka.add(btnKoupit, BorderLayout.SOUTH);
        return polozka;
    }

    private int vypocitejCenuOdemknuti(Zbozi z) {
        int zaklad = z.cena * z.maxPocet * 3;
        if (z.minVaha > 0 && z.maxVaha > 0) {
            zaklad += 1000;
        }
        zaklad += z.typ * 200;
        zaklad += z.lvlOdemknuti * 1200;

        return Math.max(100, (zaklad / 50) * 25);
    }

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
        btn.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        btn.setForeground(new Color(40, 40, 40));
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            aktualniKategorie = idKategorie;
            aktualizujNabidku();
            Container parent = btn.getParent();
            if (parent != null) parent.repaint();
        });
        return btn;
    }
}