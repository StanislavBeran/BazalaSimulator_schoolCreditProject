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
    private Boolean vypinacObrazovky = true;

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

            // ZMĚNA ZDE: Vytvoření naší nové třídy a předání "přepínače" pro Fullscreen
            panelObrazovky = new PokladnaObrazovka(false, zboziList, this::toggleFullscreen, 5, "", "", new ArrayList<>());
            vrstvy.add(panelObrazovky, Integer.valueOf(3));
            panelObrazovky.setVisible(vypinacObrazovky);

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
            pasGif.setBounds((int)(PAS_X * scaleW), (int)(PAS_Y * scaleH),
                    (int)(PAS_W * scaleW), (int)(PAS_H * scaleH));
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

            // 2. VYTVOŘÍME VELKOU OBRAZOVKU A PŘEDÁME JÍ ZACHRÁNĚNÝ STAV
            panelObrazovky = new PokladnaObrazovka(true, zboziList, this::toggleFullscreen,
                    ulozenaKategorie, ulozeneHledani, ulozenyNumpad, ulozenaUctenka);
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
            // 3. VYTVOŘÍME MALOU OBRAZOVKU A PŘEDÁME JÍ ZACHRÁNĚNÝ STAV
            panelObrazovky = new PokladnaObrazovka(false, zboziList, this::toggleFullscreen,
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
}