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
    private BazalaSimulator simulator;
    private int aktualniSpawnovanyZakaznik = 1;
    private int obsluhovanyZakaznik = 1;

    // Proměnné pro dělítko
    private int velikostNakupu;
    private boolean cekaNaDelitko = false;
    private boolean delitkoJeNaPase = false;

    public SpravcePasu(List<Zbozi> zbozi, AnimovanyGif pasGif, BazalaSimulator simulator) {
        this.zboziList = zbozi;
        this.aktivniPolozky = new ArrayList<>();
        this.pasGif = pasGif;
        this.simulator = simulator;
        this.random = new Random();


        setOpaque(false);
        setLayout(null);

        pohybTimer = new Timer(16, e -> pohniSPolozkami());
        spawnTimer = new Timer(400, e -> zkusPridatZbozi());
    }
    public void odstartujPas() {
        this.velikostNakupu = random.nextInt(5) + 1;
        simulator.vypisDoKonzole("🛒 Nový zákazník! Bude kupovat " + this.velikostNakupu + " položek.");
        if (!pohybTimer.isRunning()) {
            pohybTimer.start();
        }
        if (!spawnTimer.isRunning()) {
            spawnTimer.start();
        }
    }
    private Zbozi vyberNahodneZbozi() {
        Zbozi vybrane;
        do {
            int nahodnyIndex = random.nextInt(zboziList.size());
            vybrane = zboziList.get(nahodnyIndex);
        } while (!simulator.getOdemceneZbozi().contains(vybrane.id));
        return vybrane;
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
            if (cekaNaDelitko) {
                if (!delitkoJeNaPase) {
                    vygenerujDelitko();
                }
                return;
            }
            Zbozi vybrane = vyberNahodneZbozi();
            int pocetKusu = random.nextInt(vybrane.maxPocet) + 1;
            int startY = (getHeight() / 2) - (VELIKOST_POLOZKY / 2);
            if(getHeight() == 0) startY = 40;

            // Vytvorenie položky s priradením ID aktuálne generovaného zákazníka
            PolozkaNaPase novaPolozka = new PolozkaNaPase(vybrane, -VELIKOST_POLOZKY, startY, VELIKOST_POLOZKY, VELIKOST_POLOZKY, pocetKusu, aktualniSpawnovanyZakaznik);

            novaPolozka.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (novaPolozka.idZakaznika != obsluhovanyZakaznik) {
                        if (simulator != null) simulator.vypisDoKonzole("Chyba: Nesahej na nákup dalšího zákazníka!");
                        return;
                    }
                    if (simulator != null && simulator.cekaNaPlatbu()) {
                        simulator.vypisDoKonzole("Chyba: Nejdřív musíš dokončit platbu!");
                        return;
                    }

                    if (novaPolozka.stav == PolozkaNaPase.Stav.NA_PASE) {
                        if (novaPolozka.zboziData.typ == 0) {
                            // Logika pre položky typu "Ostatné"
                            if (simulator != null) {
                                simulator.pridejZboziNaUctenku(novaPolozka.zboziData, novaPolozka.pocetKusu);
                            }
                            int nahodneY = novaPolozka.getY() + (random.nextInt(101) - 50);
                            novaPolozka.nastavCil(ODSTAV_X, nahodneY, PolozkaNaPase.Stav.JEDE_DO_ODSTAVNEHO_MISTA);
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
                                if (simulator != null) simulator.vypisDoKonzole("Chyba: Scanner je momentálně obsazený!");
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
        Zbozi delitkoData = new Zbozi("delitko_nakupu", -1, -1, 0, 0, 0, 1, 0, "delitko_nakupu");

        // ROZMĚRY DĚLÍTKA
        int vyskaDelitka = VELIKOST_POLOZKY * 2;
        int sirkaDelitka = VELIKOST_POLOZKY / 2;

        int startY = (getHeight() / 2) - (vyskaDelitka / 2);
        if(getHeight() == 0) startY = 40;

        PolozkaNaPase delitko = new PolozkaNaPase(delitkoData, -sirkaDelitka, startY, sirkaDelitka, vyskaDelitka, 1, aktualniSpawnovanyZakaznik);

        delitko.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (delitko.idZakaznika != obsluhovanyZakaznik) return;
                if (simulator != null && simulator.cekaNaPlatbu()) return;

                if (delitko.stav == PolozkaNaPase.Stav.NA_PASE) {
                    if (delitko.getX() >= BOD_ZASTAVENI) {
                        aktivniPolozky.remove(delitko);
                        remove(delitko);
                        repaint();
                        if (simulator != null) {
                            simulator.zahajPlatbu();
                        }
                    } else {
                        if (simulator != null) {
                            simulator.vypisDoKonzole("Chyba: Dělítko ještě nedojelo ke scanneru!");
                        }
                    }
                }
            }
        });

        aktivniPolozky.add(delitko);
        add(delitko);
        repaint();

        cekaNaDelitko = false;
        delitkoJeNaPase = false;
        aktualniSpawnovanyZakaznik++;

        velikostNakupu = random.nextInt(5) + 1;
        if (simulator != null) {
            simulator.vypisDoKonzole("🛒 Další zákazník skládá věci na pás (" + velikostNakupu + " položek).");
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
                if (simulator != null) simulator.zobrazVahu(p.vaha);
            } else if (p.stav == PolozkaNaPase.Stav.JEDE_DO_ODSTAVNEHO_MISTA) {
                p.stav = PolozkaNaPase.Stav.V_ODSTAVNEM_MISTE;
            }
        }
    }

    public boolean overAOdjedZeScanneru(int idZbozi, int zadanaHodnota) {
        for (PolozkaNaPase p : aktivniPolozky) {
            if (p.stav == PolozkaNaPase.Stav.CEKA_NA_SCANNERU && p.zboziData.id == idZbozi) {

                boolean jeSpravne = false;
                if (p.vaha > 0) {
                    jeSpravne = (p.vaha == zadanaHodnota);
                } else {
                    jeSpravne = (p.pocetKusu == zadanaHodnota);
                }

                if (jeSpravne) {
                    int nahodneY = p.getY() + (random.nextInt(101) - 50);
                    p.nastavCil(ODSTAV_X, nahodneY, PolozkaNaPase.Stav.JEDE_DO_ODSTAVNEHO_MISTA);
                    if (simulator != null) simulator.zobrazVahu(0); // Vynuluje váhu na displeji
                    return true;
                }
            }
        }
        return false;
    }
    public void dokonciNakupAZacniNovy() {
        if (simulator != null) simulator.vypisDoKonzole("✅ Nákup zaplacen. Čistím odstavný prostor.");
        obsluhovanyZakaznik++;
        List<PolozkaNaPase> keSmazani = new ArrayList<>();
        for (PolozkaNaPase p : aktivniPolozky) {
            if (p.stav == PolozkaNaPase.Stav.V_ODSTAVNEM_MISTE || p.stav == PolozkaNaPase.Stav.JEDE_DO_ODSTAVNEHO_MISTA) {
                keSmazani.add(p);
                remove(p);
            }
        }
        aktivniPolozky.removeAll(keSmazani);
        repaint();
    }
}