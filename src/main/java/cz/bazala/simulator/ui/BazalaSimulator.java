package cz.bazala.simulator.ui;

import cz.bazala.simulator.model.*;
import cz.bazala.simulator.io.*;

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

    private PokladnaObrazovka panelObrazovky;
    private HerniPanel panelPenez;
    private HerniPanel panelXp;
    private InformacniOkno informacniOkno;
    private boolean vypinacObrazovky = true;
    private HerniPanel panelKonzole;
    private VracenePenize panelVracenychPenez;
    private HerniMenu herniMenu;
    private ObchodMenu obchodMenu;

    private JPanel fullscreenOverlay;
    private boolean isFullscreen = false;
    private final int FS_W = 1230;
    private final int FS_H = 667;

    private List<InteraktivniZona> hotspoty = new ArrayList<>();

    private final int SCREEN_X = 466;
    private final int SCREEN_Y = 95;
    private final int SCREEN_W = 420;
    private final int SCREEN_H = 218;

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

    // Herní stav (peníze, XP, vylepšení, odemčení, hudba, detaily hry).
    private final StavHry stav = new StavHry();

    // Konstanty pro výpočet XP za nákup
    private static final int XP_DELITEL_CENY = 5;      // část ceny zboží převedená na XP
    private static final int XP_ZA_KUSOVE_ZBOZI = 1;   // bonus za zboží prodávané na kusy (typ 0)
    private static final int XP_ZA_VAZENE_ZBOZI = 5;   // bonus za zboží z kategorií 1-4
    private static final int XP_BONUS_ZA_VAHU = 15;    // bonus za zboží, které se váží
    private static final int XP_ZA_UROVEN_ODEMKNUTI = 10;
    private static final int XP_FINALNI_DELITEL = 4;   // zmírnění celkového zisku XP
    private static final int XP_MINIMUM = 5;           // minimální XP za jakýkoli nákup

    // Prahy spokojenosti (podíl zbývající trpělivosti) pro kombo.
    private static final double PRAH_SPOKOJENOSTI_KOMBO = 0.6; // nad = kombo roste
    private static final double PRAH_NASTVANI = 0.3;           // pod = kombo se ruší

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
            vrstvy.add(spravcePasu, Integer.valueOf(3));

            panelPenez = new HerniPanel(HerniPanel.Typ.PENIZE);
            panelPenez.setPenize(1500);
            vrstvy.add(panelPenez, Integer.valueOf(5));

            panelXp = new HerniPanel(HerniPanel.Typ.XP);
            panelXp.setXpData(1, 45, 100);
            vrstvy.add(panelXp, Integer.valueOf(5));

            panelKonzole = new HerniPanel(HerniPanel.Typ.KONZOLE);
            vrstvy.add(panelKonzole, Integer.valueOf(5));

            panelVracenychPenez = new VracenePenize();
            vrstvy.add(panelVracenychPenez, Integer.valueOf(4));

            herniMenu = new HerniMenu(this, hlavniOkno);
            herniMenu.setVisible(false);
            vrstvy.add(herniMenu, Integer.valueOf(8));

            obchodMenu = new ObchodMenu(this);
            obchodMenu.setVisible(false);
            vrstvy.add(obchodMenu, Integer.valueOf(9));

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

            panelObrazovky = new PokladnaObrazovka(false, zboziList, this::toggleFullscreen, this, TypZbozi.KATEGORIE_VSE, "", "", new ArrayList<>());
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
            double scaleW = Rozmery.scaleX(getWidth());
            double scaleH = Rozmery.scaleY(getHeight());
            int scaledX = (int)(PAS_X * scaleW);
            int scaledY = (int)(PAS_Y * scaleH);
            int scaledW = (int)(PAS_W * scaleW);
            int scaledH = (int)(PAS_H * scaleH);
            pasGif.setBounds(scaledX, scaledY, scaledW, scaledH);
            if (spravcePasu != null) {
                spravcePasu.setBounds(scaledX, scaledY, (int)(Rozmery.REF_SIRKA * scaleW), scaledH);
            }

        }

        double scaleW = Rozmery.scaleX(w);
        double scaleH = Rozmery.scaleY(h);

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
            panelPenez.setBounds((int)(20 * scaleW), (int)(15 * scaleH), sirkaBaru, vyskaBaru);
        }
        if (panelXp != null) {
            int sirkaBaru = (int)(280 * scaleW);
            int vyskaBaru = (int)(60 * scaleH);
            panelXp.setBounds(w - sirkaBaru - (int)(20 * scaleW), (int)(15 * scaleH), sirkaBaru, vyskaBaru);
        }
        if (panelKonzole != null) {
            int sirkaBaru = (int)(380 * scaleW);
            int vyskaBaru = (int)(110 * scaleH);
            panelKonzole.setBounds((int)(20 * scaleW), h - vyskaBaru - (int)(20 * scaleH), sirkaBaru, vyskaBaru);
        }
        if (panelVracenychPenez != null) {
            panelVracenychPenez.setBounds((int)(1120 * scaleW), (int)(320 * scaleH), (int)(150 * scaleW), (int)(150 * scaleH));
        }
        if (herniMenu != null) {
            herniMenu.setBounds(0, 0, w, h);
        }
        if (obchodMenu != null) {
            obchodMenu.setBounds(0, 0, w, h);
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
        int ulozenaKategorie = panelObrazovky.getAktualniKategorie();
        String ulozeneHledani = panelObrazovky.getHledanyText();
        String ulozenyNumpad = panelObrazovky.getNumpadText();
        List<Zbozi> ulozenaUctenka = panelObrazovky.getPolozkyNaUctence();

        isFullscreen = !isFullscreen;

        if (isFullscreen) {
            vrstvy.remove(panelObrazovky);
            panelObrazovky = new PokladnaObrazovka(true, zboziList, this::toggleFullscreen, this, ulozenaKategorie, ulozeneHledani, ulozenyNumpad, ulozenaUctenka);
            if (fullscreenOverlay == null) {
                fullscreenOverlay = new JPanel(new BorderLayout());
                fullscreenOverlay.setOpaque(false);
            }
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
        stav.setPenize(penize);
        if (panelPenez != null) {
            panelPenez.setPenize(penize);
        }
    }
    public String getJmenoObchodu(){
        return stav.getJmenoObchodu();
    }
    public void nactiXp(int celkoveXp) {
        stav.setCelkoveXp(celkoveXp);

        if (panelXp != null) {
            panelXp.setXpData(stav.getLevel(), stav.getXpDoDalsihoLevelu(), stav.getMaxXpNaLevel());
        }
    }
    public void nactiVylepseni(String vylepseni) {
        stav.setVylepseni(vylepseni);
        aplikujVsechnaVylepseni();
    }
    public void nactiOdemceneZbozi(List<Integer> odemceneZbozi) {
        stav.setOdemceneZbozi(odemceneZbozi);
        if (panelObrazovky != null) {
            panelObrazovky.obnovSeznamZbozi();
        }
        if (obchodMenu != null) {
            obchodMenu.aktualizujNabidku();
        }
    }
    public void zobrazNavodANastartujHru() {
        if (informacniOkno != null) {
            informacniOkno.setVisible(true);
        }
    }
    public List<Integer> getOdemceneZbozi(){
        return stav.getOdemceneZbozi();
    }
    public void zobrazVahu(int vaha) {
        if (panelObrazovky != null) {
            panelObrazovky.nastavVahu(vaha);
        }
    }
    public void vypisDoKonzole(String text) {
        if(text.contains("Chyba")){
            SpravceZvuku.prehraj("chyba");
        }
        //System.out.println(text);
        if (panelKonzole != null) {
            panelKonzole.pridejZpravu(text);
        }
    }
    public boolean cekaNaPlatbu() {
        return cekaNaPlatbu;
    }
    public void zahajPlatbu() {
        cenaNakupu = panelObrazovky.getCelkovaCena();
        if (cenaNakupu == 0) {
            vypisDoKonzole("Zákazník nic nekoupil.");
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
            if (dalZakanik == 0) dalZakanik = cenaNakupu + 1000;

            castkaVratit = dalZakanik - cenaNakupu;
            castkaVraceno = 0;

            vypisDoKonzole("Zákazník platí HOTOVĚ. Dal ti " + dalZakanik + " Kč. Vrať mu: " + castkaVratit + " Kč.");
            panelObrazovky.nastavStavPlatby(true, castkaVratit, castkaVraceno);
        }
    }

    public void zpracujKliknutiNaPolozku(String nazev) {
        if (!cekaNaPlatbu) return;

        if (platbaKartou) {
            if (nazev.equals("Platební terminál")) {
                vypisDoKonzole("Platba kartou PŘIJATA!");
                dokonciPlatbu(true);
            } else {
                vypisDoKonzole("Zákazník platí kartou.");
            }
        } else {
            if (nazev.equals("Platební terminál")) {
                vypisDoKonzole("Zákazník platí hotově.");
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
                    vypisDoKonzole("✅ Nákup zaplacen.");
                    dokonciPlatbu(true);
                } else if (castkaVraceno > castkaVratit) {
                    vypisDoKonzole("Chyba: Vracíš moc! Částka se resetuje na 0.");
                    castkaVraceno = 0;
                    panelObrazovky.nastavStavPlatby(true, castkaVratit, castkaVraceno);
                    if (panelVracenychPenez != null) {
                        panelVracenychPenez.vycistiHromadku();
                    }
                }
            } catch (NumberFormatException e) {
                vypisDoKonzole("Chyba: Neznámá hodnota platidla (" + nazev + ").");
            }
        }
    }

    private void dokonciPlatbu(boolean uspesne) {
        cekaNaPlatbu = false;
        if (panelVracenychPenez != null) {
            panelVracenychPenez.vycistiHromadku();
        }
        if (uspesne) {
            SpravceZvuku.prehraj("mince"); // cinknutí kasy při zaplacení

            // Spokojenost zákazníka podle zbývající trpělivosti v okamžiku platby.
            double spokojenost = (spravcePasu != null) ? spravcePasu.getPomerTrpelivosti() : 1.0;

            // Kombo: bezchybný a spokojený zákazník ho zvedá, naštvaný ho nuluje.
            if (spokojenost >= PRAH_SPOKOJENOSTI_KOMBO) {
                stav.zvysKombo();
            } else if (spokojenost < PRAH_NASTVANI) {
                stav.vynulujKombo();
            }

            // Výsledné násobiče: vylepšení (bonus) × kombo × spokojenost (min 0.5).
            double faktorSpokojenosti = 0.5 + 0.5 * spokojenost; // 0.5..1.0
            double nasobic = stav.getNasobicOdmen() * stav.getNasobicKomba() * faktorSpokojenosti;

            int zakladXp = vypocitejXpZaNakup(panelObrazovky.getPolozkyNaUctence());
            int xpZaNakup = Math.max(1, (int) Math.round(zakladXp * nasobic));
            int penizeZaNakup = Math.max(0, (int) Math.round(cenaNakupu * nasobic));

            nactiXp(stav.getCelkoveXp() + xpZaNakup);
            nactiPenize(stav.getPenize() + penizeZaNakup);

            // Zaevidování do směny a statistik.
            boolean cilSplnen = stav.zaevidujNakup(penizeZaNakup);

            StringBuilder zprava = new StringBuilder();
            zprava.append("Získáno ").append(penizeZaNakup).append(" Kč a ").append(xpZaNakup).append(" XP");
            if (stav.getKomboSerie() >= 2) {
                zprava.append(" (kombo x").append(stav.getKomboSerie()).append(")");
            }
            zprava.append(".");
            vypisDoKonzole(zprava.toString());

            if (spokojenost < PRAH_NASTVANI) {
                vypisDoKonzole("😠 Zákazník odešel naštvaný - kombo ztraceno.");
            }

            aktualizujUkazatelSmeny();
            if (cilSplnen) {
                oznamSplneniCile();
            }
        }
        panelObrazovky.vycistiUctenku();

        if (spravcePasu != null) {
            spravcePasu.dokonciNakupAZacniNovy();
        }
    }

    /** Oznámí splnění denního cíle, posune směnu a nastaví nový cíl. */
    private void oznamSplneniCile() {
        int odmena = 300 + stav.getCisloSmeny() * 100;
        nactiPenize(stav.getPenize() + odmena);
        vypisDoKonzole("🎯 Cíl směny splněn! Bonus " + odmena + " Kč.");
        stav.setCisloSmeny(stav.getCisloSmeny() + 1);
        stav.zahajNovouSmenu();
        aktualizujUkazatelSmeny();
    }

    /** Aktualizuje zobrazení průběhu směny v konzoli/panelu (lehká zpětná vazba). */
    private void aktualizujUkazatelSmeny() {
        if (panelKonzole != null) {
            panelKonzole.nastavSmena(stav.getCisloSmeny(), stav.getTrzbaSmeny(), stav.getCilSmeny());
        }
    }
    private int vypocitejXpZaNakup(List<Zbozi> uctenka) {
        int ziskaneXp = 0;

        for (Zbozi z : uctenka) {
            int xpZaPolozku = z.cena / XP_DELITEL_CENY;
            if (z.jeKusove()) {
                xpZaPolozku += XP_ZA_KUSOVE_ZBOZI;
            } else if (z.jeSkenovane()) {
                xpZaPolozku += XP_ZA_VAZENE_ZBOZI;
            }
            if (z.minVaha > 0 && z.maxVaha > 0) {
                xpZaPolozku += XP_BONUS_ZA_VAHU;
            }
            xpZaPolozku += z.lvlOdemknuti * XP_ZA_UROVEN_ODEMKNUTI;
            if (z.minVaha == 0 && z.maxVaha == 0) {
                ziskaneXp += xpZaPolozku * Math.max(1, z.maxPocet);
            } else {
                ziskaneXp += xpZaPolozku;
            }
        }

        return Math.max(XP_MINIMUM, ziskaneXp / XP_FINALNI_DELITEL);
    }
    public String getVybranaHudba() {
        return stav.getVybranaHudba();
    }
    public List<String> getOdemcenaHudba() {
        return stav.getOdemcenaHudba();
    }
    public void nastavVybranouHudbu(String hudba) {
        stav.setVybranaHudba(hudba);
    }
    public void nastavDetailyHry(int slot, String jmeno, String vybrana, List<String> odemcena) {
        stav.setAktualniSlot(slot);
        stav.setJmenoObchodu(jmeno);
        stav.setHraNactena(true);
        if (panelObrazovky != null) {
            panelObrazovky.vycistiUctenku();
        }
        stav.setVybranaHudba(vybrana);
        stav.setOdemcenaHudba(odemcena);

        // Zahájení (pokračování) směny: nastaví cíl podle čísla směny a levelu.
        stav.zahajNovouSmenu();
        stav.vynulujKombo();
        aktualizujUkazatelSmeny();
    }

    /** Načte uložené statistiky/rekordy a číslo směny z uložené hry. */
    public void nactiStatistiky(int celkemObslouzeno, int nejlepsiKombo, int nejlepsiTrzbaSmeny,
                                int celkemVydelano, int cisloSmeny) {
        stav.setCelkemObslouzeno(celkemObslouzeno);
        stav.setNejlepsiKombo(nejlepsiKombo);
        stav.setNejlepsiTrzbaSmeny(nejlepsiTrzbaSmeny);
        stav.setCelkemVydelano(celkemVydelano);
        stav.setCisloSmeny(cisloSmeny);
    }
    public int getAktualniPenize() {
        return stav.getPenize();
    }

    public void odectiPenize(int castka) {
        nactiPenize(stav.getPenize() - castka);
    }

    public int getLevel() {
        return stav.getLevel();
    }

    public List<Zbozi> getZboziList() {
        return zboziList;
    }

    public void odemkniZbozi(int id) {
        if (stav.odemkniZbozi(id)) {
            if (panelObrazovky != null) {
                panelObrazovky.obnovSeznamZbozi();
            }
        }
    }
    public void ulozHru() {
        if (!stav.jeHraNactena()) return;
        SpravceSouboru.ulozHruDoSouboru(stav.getAktualniSlot(), stav.getJmenoObchodu(), String.valueOf(stav.getPenize()),
                stav.getCelkoveXp(), stav.getVylepseni(), stav.getOdemceneZbozi(), stav.getVybranaHudba(), stav.getOdemcenaHudba(),
                stav.getCelkemObslouzeno(), stav.getNejlepsiKombo(), stav.getNejlepsiTrzbaSmeny(),
                stav.getCelkemVydelano(), stav.getCisloSmeny());
        System.out.println("Hra byla úspěšně uložena!");
    }
    public void zobrazObchod() {
        if (obchodMenu != null) {
            obchodMenu.aktualizujNabidku();
            obchodMenu.setVisible(true);
        }
    }

    /** Pozastaví běh pásu (pohyb i spawn). Volá se při otevření menu/obchodu. */
    public void pozastavHru() {
        if (spravcePasu != null) spravcePasu.pozastav();
    }

    /** Obnoví běh pásu po zavření menu/obchodu, pokud hra běží. */
    public void obnovHru() {
        if (spravcePasu != null && stav.jeHraNactena()) spravcePasu.obnov();
    }
    public int getUrovenRychlostiPasu() {
        return stav.getUrovenRychlostiPasu();
    }

    public void zvysUrovenRychlostiPasu() {
        int novaUroven = stav.zvysUrovenRychlostiPasu();
        if (novaUroven >= 0 && spravcePasu != null) {
            spravcePasu.aplikujVylepseniRychlosti(novaUroven);
        }
    }

    /** Násobič trpělivosti z vylepšení - čte ho SpravcePasu při startu zákazníka. */
    public double getNasobicTrpelivosti() {
        return stav.getNasobicTrpelivosti();
    }

    /** True, pokud je aktivní druhý scanner (SpravcePasu povolí 2 položky na scanneru). */
    public boolean maDruhyScanner() {
        return stav.maDruhyScanner();
    }

    /** True, pokud se kusové zboží odbavuje automaticky. */
    public boolean maAutoSkenKusove() {
        return stav.maAutoSkenKusove();
    }

    public int getUrovenVylepseni(StavHry.Vylepseni v) {
        return stav.getUrovenVylepseni(v);
    }

    /**
     * Koupě vylepšení daného druhu: strhne cenu, zvýší úroveň a aplikuje
     * dopady do běžící hry. Vrací true při úspěchu.
     */
    public boolean kupVylepseni(StavHry.Vylepseni druh, int cena) {
        if (stav.getPenize() < cena) {
            vypisDoKonzole("Chyba: Nedostatek peněz na vylepšení!");
            return false;
        }
        int novaUroven = stav.zvysUrovenVylepseni(druh);
        if (novaUroven < 0) {
            vypisDoKonzole("Chyba: Toto vylepšení je už na maximu!");
            return false;
        }
        odectiPenize(cena);
        aplikujVsechnaVylepseni();
        return true;
    }

    /** Promítne aktuální úrovně všech vylepšení do běžící hry. */
    private void aplikujVsechnaVylepseni() {
        if (spravcePasu != null) {
            spravcePasu.aplikujVylepseniRychlosti(stav.getUrovenRychlostiPasu());
        }
    }
    public void resetujHerniPole() {
        stav.setHraNactena(false);
        cekaNaPlatbu = false;
        platbaKartou = false;
        if (spravcePasu != null) {
            spravcePasu.vycistiVsechno();
        }
        if (panelObrazovky != null) {
            panelObrazovky.vycistiUctenku();
            panelObrazovky.nastavVahu(0);
            panelObrazovky.nastavStavPlatby(false, 0, 0);
        }
        if (panelVracenychPenez != null) {
            panelVracenychPenez.vycistiHromadku();
        }

    }

    /**
     * Panic reset: zahodí rozdělaný nákup a zaseknutý stav (pás, scanner,
     * platba) a spustí nového zákazníka. Herní postup (peníze, XP, odemčení)
     * zůstává zachován - čistí se jen aktuální rozehraná situace.
     */
    public void resetujZakaznika() {
        cekaNaPlatbu = false;
        platbaKartou = false;
        if (spravcePasu != null) {
            spravcePasu.vycistiVsechno();
        }
        if (panelObrazovky != null) {
            panelObrazovky.vycistiUctenku();
            panelObrazovky.nastavVahu(0);
            panelObrazovky.nastavStavPlatby(false, 0, 0);
        }
        if (panelVracenychPenez != null) {
            panelVracenychPenez.vycistiHromadku();
        }
        stav.vynulujKombo();
        aktualizujUkazatelSmeny();
        vypisDoKonzole("Zákazník byl resetován. Přichází nový.");
        spustSimulaci();
    }
    public boolean kupHudbu(String souborHudby, int cena) {
        StavHry.VysledekKoupeHudby vysledek = stav.zkusKoupitHudbu(souborHudby, cena);
        switch (vysledek) {
            case KOUPENO:
                nactiPenize(stav.getPenize());
                vypisDoKonzole("Zakoupena nová hudba! Můžeš ji změnit v Nastavení.");
                return true;
            case JIZ_VLASTNENO:
                vypisDoKonzole("Chyba: Tuto hudbu už vlastníš!");
                return false;
            case NEDOSTATEK_PENEZ:
            default:
                vypisDoKonzole("Chyba: Nedostatek peněz!");
                return false;
        }
    }
}