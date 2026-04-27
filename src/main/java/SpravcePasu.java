import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpravcePasu extends JPanel {
    private List<Zbozi> zboziList;
    private List<PolozkaNaPase> aktivniPolozky;
    private Random random;

    private Timer pohybTimer;
    private Timer spawnTimer;

    private final int RYCHLOST_POSUVU = 2;
    private final int BOD_ZASTAVENI = 480;

    private final int VELIKOST_POLOZKY = 80;

    public SpravcePasu(List<Zbozi> zbozi) {
        this.zboziList = zbozi;
        this.aktivniPolozky = new ArrayList<>();
        this.random = new Random();

        setOpaque(false);
        setLayout(null);

        // Timer pro plynulý pohyb položek (cca 60 FPS)
        pohybTimer = new Timer(16, e -> pohniSPolozkami());
        pohybTimer.start();

        // Timer pro přidávání nového zboží (zkusí přidat každé 2 sekundy)
        spawnTimer = new Timer(2000, e -> zkusPridatZbozi());
        spawnTimer.start();
    }

    private Zbozi vyberNahodneZbozi() {
        int nahodnaHodnota = random.nextInt(zboziList.size());
        return zboziList.get(nahodnaHodnota);
    }

    // --- LOGIKA: PŘIDÁNÍ NA PÁS ---
    private void zkusPridatZbozi() {
        // Kontrola, jestli je na začátku pásu (vlevo) místo
        boolean jeMisto = true;
        for (PolozkaNaPase p : aktivniPolozky) {
            if (p.getX() < VELIKOST_POLOZKY + 20) {
                jeMisto = false;
                break;
            }
        }

        if (jeMisto) {
            Zbozi vybrane = vyberNahodneZbozi();

            int startY = (getHeight() / 2) - (VELIKOST_POLOZKY / 2);

            if(getHeight() == 0) startY = 40;

            PolozkaNaPase novaPolozka = new PolozkaNaPase(vybrane, -VELIKOST_POLOZKY, startY, VELIKOST_POLOZKY);

            aktivniPolozky.add(novaPolozka);
            add(novaPolozka);
            repaint();
        }
    }

    private void pohniSPolozkami() {
        for (int i = 0; i < aktivniPolozky.size(); i++) {
            PolozkaNaPase aktualni = aktivniPolozky.get(i);
            int zbyvajiciMistoVPravo = BOD_ZASTAVENI;

            // Zkontrolujeme, jestli před tímto předmětem neleží jiný předmět
            if (i > 0) {
                PolozkaNaPase predchozi = aktivniPolozky.get(i - 1);
                // Necháme 10 pixelů mezeru mezi produkty
                zbyvajiciMistoVPravo = predchozi.getX() - VELIKOST_POLOZKY - 10;
            }

            // Pokud ještě nenarazil do zastavovacího bodu ani do předmětu před ním, posuneme ho
            if (aktualni.getX() < zbyvajiciMistoVPravo) {
                aktualni.setLocation(aktualni.getX() + RYCHLOST_POSUVU, aktualni.getY());
            }
        }
    }

    // --- VNITŘNÍ TŘÍDA: VIZUÁLNÍ POLOŽKA NA PÁSE ---
    private class PolozkaNaPase extends JLabel {
        Zbozi zboziData;

        public PolozkaNaPase(Zbozi z, int x, int y, int velikost) {
            this.zboziData = z;
            setBounds(x, y, velikost, velikost);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setToolTipText(z.nazev + " (" + z.cena + " Kč)");

            // Načtení obrázku úplně stejně jako to děláš v PokladnaObrazovka
            try {
                java.net.URL imgUrl = getClass().getResource("/zboziObrazky/" + z.zkracenyNazev + ".png");
                if (imgUrl != null) {
                    Image img = new ImageIcon(imgUrl).getImage().getScaledInstance(velikost, velikost, Image.SCALE_SMOOTH);
                    setIcon(new ImageIcon(img));
                } else {
                    setText("📦");
                    setFont(new Font("Segoe UI Emoji", Font.PLAIN, velikost/2));
                    setHorizontalAlignment(SwingConstants.CENTER);
                }
            } catch (Exception e) {
                setText("📦");
            }
        }
    }
}