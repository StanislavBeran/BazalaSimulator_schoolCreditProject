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
    private final int SCANNER_X = 610;
    private final int SCANNER_Y = 34;
    private final int ODSTAV_X = 900;

    private final int VELIKOST_POLOZKY = 80;

    private AnimovanyGif pasGif;
    private BazalaSimulator bazalaSimulator;

    // UPRAVENÝ KONSTRUKTOR: Nyní přijímá i BazalaSimulator
    public SpravcePasu(List<Zbozi> zbozi, AnimovanyGif pasGif, BazalaSimulator bazalaSimulator) {
        this.zboziList = zbozi;
        this.aktivniPolozky = new ArrayList<>();
        this.pasGif = pasGif;
        this.bazalaSimulator = bazalaSimulator;
        this.random = new Random();

        setOpaque(false);
        setLayout(null);

        pohybTimer = new Timer(16, e -> pohniSPolozkami());
        pohybTimer.start();

        spawnTimer = new Timer(2000, e -> zkusPridatZbozi()); // Změněno z 20ms zpět na 2000ms (2 vteřiny)
        spawnTimer.start();
    }

    private Zbozi vyberNahodneZbozi() {
        int nahodnaHodnota = random.nextInt(zboziList.size());
        return zboziList.get(nahodnaHodnota);
    }

    private void zkusPridatZbozi() {
        boolean jeMisto = true;
        for (PolozkaNaPase p : aktivniPolozky) {
            // Kontrolujeme pouze ty položky, které jsou ještě fyzicky na páse
            if (p.stav == PolozkaNaPase.Stav.NA_PASE && p.getX() < p.getWidth() + 20) {
                jeMisto = false;
                break;
            }
        }

        if (jeMisto) {
            Zbozi vybrane = vyberNahodneZbozi();
            int pocetKusu = random.nextInt(vybrane.maxPocet) + 1;
            int startY = (getHeight() / 2) - (VELIKOST_POLOZKY / 2);
            if(getHeight() == 0) startY = 40;

            PolozkaNaPase novaPolozka = new PolozkaNaPase(vybrane, -VELIKOST_POLOZKY, startY, VELIKOST_POLOZKY, pocetKusu);

            // NOVÉ: Kliknutí na zboží určí, kam poletí
            novaPolozka.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (novaPolozka.stav == PolozkaNaPase.Stav.NA_PASE) {

                        if (novaPolozka.zboziData.typ == 0) {
                            // TYP 0: Letí rovnou do odstavu a automaticky se propíše na účtenku
                            if (bazalaSimulator != null) {
                                bazalaSimulator.pridejZboziNaUctenku(novaPolozka.zboziData, novaPolozka.pocetKusu);
                            }
                            novaPolozka.nastavCil(ODSTAV_X, novaPolozka.getY(), PolozkaNaPase.Stav.JEDE_DO_ODSTAVU);

                        } else if (novaPolozka.zboziData.typ >= 1 && novaPolozka.zboziData.typ <= 4) {
                            // TYP 1-4: Zkontrolujeme, zda je scanner volný (max 1 položka)
                            boolean scannerVolny = true;
                            for (PolozkaNaPase p : aktivniPolozky) {
                                if (p.stav == PolozkaNaPase.Stav.JEDE_NA_SCANNER || p.stav == PolozkaNaPase.Stav.CEKA_NA_SCANNERU) {
                                    scannerVolny = false;
                                    break;
                                }
                            }
                            if (scannerVolny) {
                                novaPolozka.nastavCil(SCANNER_X, SCANNER_Y, PolozkaNaPase.Stav.JEDE_NA_SCANNER);
                            } else {
                                System.out.println("Scanner je momentálně obsazený!");
                            }
                        }
                    }
                }
            });
            aktivniPolozky.add(novaPolozka);
            add(novaPolozka);
            repaint();
        }
    }

    private void pohniSPolozkami() {
        boolean pasBezi = true;

        for (PolozkaNaPase p : aktivniPolozky) {
            if (p.stav == PolozkaNaPase.Stav.NA_PASE && p.getX() >= BOD_ZASTAVENI) {
                pasBezi = false;
                break;
            }
        }

        if (pasGif != null) {
            pasGif.beziPas(pasBezi);
        }

        for (int i = 0; i < aktivniPolozky.size(); i++) {
            PolozkaNaPase p = aktivniPolozky.get(i);

            if (p.stav == PolozkaNaPase.Stav.NA_PASE) {
                if (pasBezi) {
                    int limit = BOD_ZASTAVENI;
                    if (i > 0) {
                        PolozkaNaPase predchozi = aktivniPolozky.get(i - 1);
                        if (predchozi.stav == PolozkaNaPase.Stav.NA_PASE) {
                            limit = predchozi.getX() - p.getWidth() - 10;
                        }
                    }

                    if (p.getX() < limit) {
                        p.setLocation(p.getX() + RYCHLOST_POSUVU, p.getY());
                    }
                }
            } else {
                animujKTargetu(p);
            }
        }
    }

    // NOVÉ: Zajišťuje plynulý přelet zboží
    private void animujKTargetu(PolozkaNaPase p) {
        int dx = p.cilX - p.getX();
        int dy = p.cilY - p.getY();
        int rychlost = 8;

        if (Math.abs(dx) > rychlost) p.setLocation(p.getX() + (dx > 0 ? rychlost : -rychlost), p.getY());
        if (Math.abs(dy) > rychlost) p.setLocation(p.getX(), p.getY() + (dy > 0 ? rychlost : -rychlost));

        // Když zboží dorazí do svého cíle
        if (Math.abs(dx) <= rychlost && Math.abs(dy) <= rychlost) {
            if (p.stav == PolozkaNaPase.Stav.JEDE_NA_SCANNER) {
                // Dorazil na scanner -> ZASTAVÍ SE A ČEKÁ NA ZADÁNÍ Z KLÁVESNICE
                p.stav = PolozkaNaPase.Stav.CEKA_NA_SCANNERU;

            } else if (p.stav == PolozkaNaPase.Stav.JEDE_DO_ODSTAVU) {
                // Zboží je v odstavném prostoru, můžeme ho smazat
                remove(p);
                aktivniPolozky.remove(p);
            }
        }
    }
    public boolean overAOdjedZeScanneru(int idZbozi, int mnozstvi) {
        for (PolozkaNaPase p : aktivniPolozky) {
            if (p.stav == PolozkaNaPase.Stav.CEKA_NA_SCANNERU && p.zboziData.id == idZbozi && p.pocetKusu == mnozstvi) {
                // Údaje sedí! Pošleme zboží do odstavu
                p.nastavCil(ODSTAV_X, p.getY(), PolozkaNaPase.Stav.JEDE_DO_ODSTAVU);
                return true;
            }
        }
        return false;
    }
}