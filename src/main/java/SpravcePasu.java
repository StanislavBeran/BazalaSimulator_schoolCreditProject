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

    // Proměnné pro dělítko
    private int velikostNakupu;
    private boolean cekaNaDelitko = false;
    private boolean delitkoJeNaPase = false;

    public SpravcePasu(List<Zbozi> zbozi, AnimovanyGif pasGif, BazalaSimulator bazalaSimulator) {
        this.zboziList = zbozi;
        this.aktivniPolozky = new ArrayList<>();
        this.pasGif = pasGif;
        this.bazalaSimulator = bazalaSimulator;
        this.random = new Random();

        setOpaque(false);
        setLayout(null);

        pohybTimer = new Timer(16, e -> pohniSPolozkami());
        spawnTimer = new Timer(400, e -> zkusPridatZbozi());
    }
    public void odstartujPas() {
        this.velikostNakupu = random.nextInt(25) + 1; // Změň si podle libosti (např. random.nextInt(3) + 3; pro testování)
        System.out.println("🛒 Nový zákazník! Bude kupovat " + this.velikostNakupu + " položek.");
        if (!pohybTimer.isRunning()) {
            pohybTimer.start();
        }
        if (!spawnTimer.isRunning()) {
            spawnTimer.start();
        }
        System.out.println("Pás byl spuštěn!");
    }
    private Zbozi vyberNahodneZbozi() {
        int nahodnaHodnota = random.nextInt(zboziList.size());
        return zboziList.get(nahodnaHodnota);
    }

    private void zkusPridatZbozi() {
        boolean jeMisto = true;
        for (PolozkaNaPase p : aktivniPolozky) {
            if (p.stav == PolozkaNaPase.Stav.NA_PASE && p.getX() < 30) {
                jeMisto = false;
                break;
            }
        }

        if (jeMisto) {
            // LOGIKA PRO DĚLÍTKO
            if (cekaNaDelitko) {
                if (!delitkoJeNaPase) {
                    vygenerujDelitko();
                }
                return;
            }

            // BĚŽNÉ GENEROVÁNÍ ZBOŽÍ
            Zbozi vybrane = vyberNahodneZbozi();
            int pocetKusu = random.nextInt(vybrane.maxPocet) + 1;
            int startY = (getHeight() / 2) - (VELIKOST_POLOZKY / 2);
            if(getHeight() == 0) startY = 40;

            PolozkaNaPase novaPolozka = new PolozkaNaPase(vybrane, -VELIKOST_POLOZKY, startY, VELIKOST_POLOZKY, VELIKOST_POLOZKY, pocetKusu);

            novaPolozka.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (novaPolozka.stav == PolozkaNaPase.Stav.NA_PASE) {
                        if (novaPolozka.zboziData.typ == 0) {
                            if (bazalaSimulator != null) {
                                bazalaSimulator.pridejZboziNaUctenku(novaPolozka.zboziData, novaPolozka.pocetKusu);
                            }
                            novaPolozka.nastavCil(ODSTAV_X, novaPolozka.getY(), PolozkaNaPase.Stav.JEDE_DO_ODSTAVU);
                        } else if (novaPolozka.zboziData.typ >= 1 && novaPolozka.zboziData.typ <= 4) {
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
            velikostNakupu--;

            if (velikostNakupu <= 0) {
                cekaNaDelitko = true;
            }
        }
    }

    private void vygenerujDelitko() {
        System.out.println("⚠️ Generuji dělítko nákupu!");
        Zbozi delitkoData = new Zbozi("delitko_nakupu", -1, -1, 0, 0, 0, 1, 0, "delitko_nakupu");

        // ROZMĚRY DĚLÍTKA (Můžeš si s čísly libovolně hrát!)
        int vyskaDelitka = VELIKOST_POLOZKY * 2; // 2x větší na výšku
        int sirkaDelitka = VELIKOST_POLOZKY / 2; // Užší šířka, ať to vypadá jako tyčka

        // Výpočet startY musíme udělat podle nové výšky dělítka, ať nevyjede z pásu!
        int startY = (getHeight() / 2) - (vyskaDelitka / 2);
        if(getHeight() == 0) startY = 40;

        // Vytvoříme dělítko s novými proporcemi
        PolozkaNaPase delitko = new PolozkaNaPase(delitkoData, -sirkaDelitka, startY, sirkaDelitka, vyskaDelitka, 1);

        delitko.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (delitko.stav == PolozkaNaPase.Stav.NA_PASE) {

                    // NOVÁ PODMÍNKA: Kliknout jde, jen když je dělítko na konci pásu
                    if (delitko.getX() >= BOD_ZASTAVENI) {
                        delitko.nastavCil(ODSTAV_X, delitko.getY(), PolozkaNaPase.Stav.JEDE_DO_ODSTAVU);

                        System.out.println("✅ Nákup ukončen (dělítko odstraněno). Začíná další zákazník.");
                        cekaNaDelitko = false;
                        delitkoJeNaPase = false;
                        velikostNakupu = random.nextInt(25) + 1;
                    } else {
                        // Pokud je dělítko moc daleko, nic se nestane (nebo to napíše zprávu)
                        System.out.println("❌ Na dělítko nelze kliknout, ještě nedojelo ke scanneru!");
                        // Pokud máš nahraný chybový zvuk, můžeš ho tu přehrát:
                        // SpravceZvuku.prehraj("/chyba.wav");
                    }

                }
            }
        });

        aktivniPolozky.add(delitko);
        add(delitko);
        repaint();
        delitkoJeNaPase = true;
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
                            limit = predchozi.getX() - p.getWidth();
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

    private void animujKTargetu(PolozkaNaPase p) {
        int dx = p.cilX - p.getX();
        int dy = p.cilY - p.getY();
        int rychlost = 8;

        if (Math.abs(dx) > rychlost) p.setLocation(p.getX() + (dx > 0 ? rychlost : -rychlost), p.getY());
        if (Math.abs(dy) > rychlost) p.setLocation(p.getX(), p.getY() + (dy > 0 ? rychlost : -rychlost));

        if (Math.abs(dx) <= rychlost && Math.abs(dy) <= rychlost) {
            if (p.stav == PolozkaNaPase.Stav.JEDE_NA_SCANNER) {
                p.stav = PolozkaNaPase.Stav.CEKA_NA_SCANNERU;
            } else if (p.stav == PolozkaNaPase.Stav.JEDE_DO_ODSTAVU) {
                remove(p);
                aktivniPolozky.remove(p);
            }
        }
    }

    public boolean overAOdjedZeScanneru(int idZbozi, int mnozstvi) {
        for (PolozkaNaPase p : aktivniPolozky) {
            if (p.stav == PolozkaNaPase.Stav.CEKA_NA_SCANNERU && p.zboziData.id == idZbozi && p.pocetKusu == mnozstvi) {
                p.nastavCil(ODSTAV_X, p.getY(), PolozkaNaPase.Stav.JEDE_DO_ODSTAVU);
                return true;
            }
        }
        return false;
    }
}