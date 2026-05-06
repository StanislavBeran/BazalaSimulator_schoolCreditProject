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

    // ZMĚNA ZDE: Nyní používáme naši novou třídu!
    private PokladnaObrazovka panelObrazovky;
    private HerniPanel panelPenez;
    private HerniPanel panelXp;
    private InformacniOkno informacniOkno;
    private Boolean vypinacObrazovky = true;
    private HerniPanel panelKonzole;
    private VracenePenize panelVracenychPenez;
    private HerniMenu herniMenu;

    private JPanel fullscreenOverlay;
    private boolean isFullscreen = false;
    private final int FS_W = 1230;
    private final int FS_H = 667;

    private List<InteraktivniZona> hotspoty = new ArrayList<>();

    private final int SCREEN_X = 467;
    private final int SCREEN_Y = 96;
    private final int SCREEN_W = 417;
    private final int SCREEN_H = 215;

    private AnimovanyGif pasGif;
    private final int PAS_X = 0;
    private final int PAS_Y = 362;
    private final int PAS_W = 596;
    private final int PAS_H = 190;

    private static final String iconPath = "/nastaveni.png";
    private static final String backgroundPath = "/pokladna.png";

    private SpravcePasu spravcePasu;

    private boolean cekaNaPlatbu = false;
    private boolean platbaKartou = false;
    private int cenaNakupu = 0;
    private int castkaVratit = 0;
    private int castkaVraceno = 0;
    private int aktualniPenize = 0;

    private int aktualniSlot = 1;
    private String jmenoObchodu = "Můj Obchod";
    private int obtiznost = 0;
    private int celkoveXp = 0;

    public BazalaSimulator(Menu okno) {
        this.hlavniOkno = okno;
        setLayout(new BorderLayout());
        vrstvy = new JLayeredPane();
        zboziList = SpravceSouboru.nactiZbozi();

        try {
            bgPanel = okno.new BackgroundPanel(backgroundPath);
            vrstvy.add(bgPanel, JLayeredPane.DEFAULT_LAYER);
            pasGif = new AnimovanyGif("/pas.gif");
            vrstvy.add(pasGif, Integer.valueOf(2));

            spravcePasu = new SpravcePasu(zboziList, pasGif, this);
            vrstvy.add(spravcePasu, Integer.valueOf(3)); // Pás je vrstva 3

            panelPenez = new HerniPanel(HerniPanel.Typ.PENIZE);
            panelPenez.setPenize(1500);
            vrstvy.add(panelPenez, Integer.valueOf(5));

            // MÍSTO PŮVODNÍHO: panelXp = new HerniPanel(true);
            panelXp = new HerniPanel(HerniPanel.Typ.XP);
            panelXp.setXpData(1, 45, 100);
            vrstvy.add(panelXp, Integer.valueOf(5));

            // NOVÝ PANEL KONZOLE
            panelKonzole = new HerniPanel(HerniPanel.Typ.KONZOLE);
            vrstvy.add(panelKonzole, Integer.valueOf(5));

            panelVracenychPenez = new VracenePenize();
            vrstvy.add(panelVracenychPenez, Integer.valueOf(4));

            herniMenu = new HerniMenu(this, hlavniOkno);
            herniMenu.setVisible(false);
            vrstvy.add(herniMenu, Integer.valueOf(8));

            informacniOkno = new InformacniOkno(() -> {
                if (spravcePasu != null) {
                    spravcePasu.odstartujPas();
                }
            });
            informacniOkno.setVisible(false);
            vrstvy.add(informacniOkno, Integer.valueOf(6));

            ImageIcon settingsIcon = new ImageIcon(getClass().getResource(iconPath));
            Image scaledImg = settingsIcon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            settingsLabel = new JLabel(new ImageIcon(scaledImg));
            settingsLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            settingsLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (herniMenu != null) {
                        herniMenu.setVisible(true);
                    }
                }
            });
            vrstvy.add(settingsLabel, JLayeredPane.PALETTE_LAYER);

            panelObrazovky = new PokladnaObrazovka(false, zboziList, this::toggleFullscreen, this, 5, "", "", new ArrayList<>());
            vrstvy.add(panelObrazovky, Integer.valueOf(4));

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
        if (pasGif != null) {
            double scaleW = (double) getWidth() / 1280;
            double scaleH = (double) getHeight() / 720;
            int scaledX = (int)(PAS_X * scaleW);
            int scaledY = (int)(PAS_Y * scaleH);
            int scaledW = (int)(PAS_W * scaleW);
            int scaledH = (int)(PAS_H * scaleH);
            pasGif.setBounds(scaledX, scaledY, scaledW, scaledH);
            if (spravcePasu != null) {
                spravcePasu.setBounds(scaledX, scaledY, (int)(1280 * scaleW), scaledH);
            }

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
        if (panelPenez != null) {
            int sirkaBaru = (int)(280 * scaleW);
            int vyskaBaru = (int)(60 * scaleH);
            // Umístění vlevo nahoře s malým odsazením
            panelPenez.setBounds((int)(20 * scaleW), (int)(15 * scaleH), sirkaBaru, vyskaBaru);
        }
        if (panelXp != null) {
            int sirkaBaru = (int)(280 * scaleW);
            int vyskaBaru = (int)(60 * scaleH);
            // Umístění vpravo nahoře s malým odsazením
            panelXp.setBounds(w - sirkaBaru - (int)(20 * scaleW), (int)(15 * scaleH), sirkaBaru, vyskaBaru);
        }
        if (panelKonzole != null) {
            int sirkaBaru = (int)(380 * scaleW);
            int vyskaBaru = (int)(110 * scaleH);
            panelKonzole.setBounds((int)(20 * scaleW), h - vyskaBaru - (int)(20 * scaleH), sirkaBaru, vyskaBaru);
        }
        if (panelVracenychPenez != null) {
            panelVracenychPenez.setBounds((int)(1090 * scaleW), (int)(320 * scaleH), (int)(210 * scaleW), (int)(150 * scaleH));
        }
        if (herniMenu != null) {
            herniMenu.setBounds(0, 0, w, h);
        }
        for (InteraktivniZona zona : hotspoty) {
            zona.aktualizujPozici(scaleW, scaleH);
        }
        if (informacniOkno != null) {
            informacniOkno.setBounds(0, 0, w, h);
        }
        revalidate();
        repaint();
    }
    public void pridejZboziNaUctenku(Zbozi z, int mnozstvi) {
        if (panelObrazovky != null) {
            panelObrazovky.pridejPolozkuNaUctenku(z, mnozstvi);
        }
    }
    public boolean overZboziZeScanneru(int id, int mnozstvi) {
        if (spravcePasu != null) {
            return spravcePasu.overAOdjedZeScanneru(id, mnozstvi);
        }
        return false;
    }
    private void toggleFullscreen() {
        // 1. ZACHRÁNÍME AKTUÁLNÍ STAV ZE STARÉ OBRAZOVKY (Včetně účtenky!)
        int ulozenaKategorie = panelObrazovky.getAktualniKategorie();
        String ulozeneHledani = panelObrazovky.getHledanyText();
        String ulozenyNumpad = panelObrazovky.getNumpadText();
        List<Zbozi> ulozenaUctenka = panelObrazovky.getPolozkyNaUctence();

        isFullscreen = !isFullscreen;

        if (isFullscreen) {
            if (fullscreenOverlay == null) {
                fullscreenOverlay = hlavniOkno.new BackgroundPanel("celaObrazovka.png");
                fullscreenOverlay.setLayout(new BorderLayout());
            }

            vrstvy.remove(panelObrazovky);

            panelObrazovky = new PokladnaObrazovka(true, zboziList, this::toggleFullscreen, this, ulozenaKategorie, ulozeneHledani, ulozenyNumpad, ulozenaUctenka);
            fullscreenOverlay.add(panelObrazovky, BorderLayout.CENTER);

            vrstvy.add(fullscreenOverlay, JLayeredPane.DRAG_LAYER);
            fullscreenOverlay.setVisible(true);
        } else {
            if (fullscreenOverlay != null) {
                fullscreenOverlay.remove(panelObrazovky);
                vrstvy.remove(fullscreenOverlay);
                fullscreenOverlay.setVisible(false);
            }
            panelObrazovky = new PokladnaObrazovka(false, zboziList, this::toggleFullscreen, this,
                    ulozenaKategorie, ulozeneHledani, ulozenyNumpad, ulozenaUctenka);
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
    public void spustSimulaci() {
        if (spravcePasu != null) {
            spravcePasu.odstartujPas();
        }
    }
    public void nactiPenize(int penize) {
        this.aktualniPenize = penize;
        if (panelPenez != null) {
            panelPenez.setPenize(penize);
        }
    }
    public String getJmenoObchodu(){
        return jmenoObchodu;
    }
    public void nactiXp(int celkoveXp) {
        this.celkoveXp = celkoveXp; // NOVÉ: Zapamatujeme si to pro ukládání

        if (panelXp != null) {
            int level = (celkoveXp / 100) + 1;
            int zbyvajiciXpDoDalsihoLevelu = celkoveXp % 100;
            int maxXp = 100;
            panelXp.setXpData(level, zbyvajiciXpDoDalsihoLevelu, maxXp);
        }
    }
    public void zobrazNavodANastartujHru() {
        if (informacniOkno != null) {
            informacniOkno.setVisible(true);
        }
    }
    public void vypisDoKonzole(String text) {
        System.out.println(text);
        if (panelKonzole != null) {
            panelKonzole.pridejZpravu(text);
        }
    }
    public void zahajPlatbu() {
        cenaNakupu = panelObrazovky.getCelkovaCena();
        if (cenaNakupu == 0) {
            vypisDoKonzole("Zákazník nic nekoupil. Odchází...");
            dokonciPlatbu(false);
            return;
        }

        cekaNaPlatbu = true;
        platbaKartou = Math.random() < 0.5;
        if (platbaKartou) {
            vypisDoKonzole("Zákazník chce platit KARTOU (" + cenaNakupu + " Kč).");
            vypisDoKonzole("Klikni na Platební terminál.");
            panelObrazovky.nastavStavPlatby(false, 0, 0);
        } else {
            int[] bankovky = {100, 200, 500, 1000, 2000, 5000};
            int dalZakanik = 0;
            for (int b : bankovky) {
                if (b > cenaNakupu) {
                    dalZakanik = b;
                    break;
                }
            }
            if (dalZakanik == 0) dalZakanik = cenaNakupu + 1000; // Pojistka pro obří nákupy

            castkaVratit = dalZakanik - cenaNakupu;
            castkaVraceno = 0;

            vypisDoKonzole("Zákazník platí HOTOVĚ. Dal ti " + dalZakanik + " Kč.");
            vypisDoKonzole("Vrať mu " + castkaVratit + " Kč naklikáním mincí a bankovek.");
            panelObrazovky.nastavStavPlatby(true, castkaVratit, castkaVraceno);
        }
    }

    public void zpracujKliknutiNaPolozku(String nazev) {
        if (!cekaNaPlatbu) return;

        if (platbaKartou) {
            if (nazev.equals("Platební terminál")) {
                vypisDoKonzole("Pip... Platba kartou PŘIJATA!");
                dokonciPlatbu(true);
            } else {
                vypisDoKonzole("Zákazník platí kartou! Nemusíš sahat na peníze.");
            }
        } else {
            if (nazev.equals("Platební terminál")) {
                vypisDoKonzole("Zákazník platí hotově, terminál nepotřebuješ.");
                return;
            }
            try {
                int hodnota = Integer.parseInt(nazev.replace(" Kč", "").trim());
                castkaVraceno += hodnota;
                panelObrazovky.nastavStavPlatby(true, castkaVratit, castkaVraceno);
                if (panelVracenychPenez != null) {
                    panelVracenychPenez.pridejPenize(hodnota);
                }
                if (castkaVraceno == castkaVratit) {
                    vypisDoKonzole("Nákup zaplacen.");
                    dokonciPlatbu(true);
                } else if (castkaVraceno > castkaVratit) {
                    vypisDoKonzole("Chyba: Vracíš moc! Částka se resetuje na 0.");
                    castkaVraceno = 0;
                    panelObrazovky.nastavStavPlatby(true, castkaVratit, castkaVraceno);
                    if (panelVracenychPenez != null) {
                        panelVracenychPenez.vycistiHromadku();
                    }
                }
            } catch (Exception e) {}
        }
    }

    private void dokonciPlatbu(boolean uspesne) {
        cekaNaPlatbu = false;
        if (panelVracenychPenez != null) {
            panelVracenychPenez.vycistiHromadku();
        }
        if (uspesne) {
            nactiPenize(aktualniPenize + cenaNakupu);
            vypisDoKonzole("Získáno " + cenaNakupu + " Kč. Další zákazník na řadě!");
        }
        panelObrazovky.vycistiUctenku();

        if (spravcePasu != null) {
            spravcePasu.dokonciNakupAZacniNovy(); // Řekneme pásu, že může uklidit a jet dál
        }
    }
    public void nastavDetailyHry(int slot, String jmeno, int obtiznost) {
        this.aktualniSlot = slot;
        this.jmenoObchodu = jmeno;
        this.obtiznost = obtiznost;
        if (panelObrazovky != null) {
            panelObrazovky.vycistiUctenku();
        }
    }

    public void ulozHru() {
        SpravceSouboru.ulozHruDoSouboru(aktualniSlot, jmenoObchodu, obtiznost, String.valueOf(aktualniPenize), celkoveXp);
        System.out.println("Hra byla úspěšně uložena!");
    }
}