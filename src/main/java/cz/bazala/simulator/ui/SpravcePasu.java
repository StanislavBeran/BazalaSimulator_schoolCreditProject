package cz.bazala.simulator.ui;

import cz.bazala.simulator.model.*;
import cz.bazala.simulator.io.*;

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

    private int RYCHLOST_POSUVU = 1;
    private int RYCHLOST_ANIMACE = 4;

    // Referenční rozlišení, pro které byly souřadnice vymyšleny.
    // Panel SpravcePasu je v BazalaSimulatoru roztažen na šířku odpovídající
    // 1280 referenčním pixelům a na výšku odpovídající 190 referenčním pixelům
    // pásu, takže z aktuálních rozměrů panelu umíme odvodit škálovací faktor.
    private static final int REF_SIRKA = 1280;
    private static final int REF_VYSKA_PASU = 190;

    // Referenční (nenaškálované) souřadnice v prostoru 1280 x 190.
    private final int REF_BOD_ZASTAVENI = 480;
    private final int REF_SCANNER_X = 610;
    private final int REF_SCANNER_Y = 34;
    private final int REF_ODSTAV_X = 900;
    private final int REF_VELIKOST_POLOZKY = 80;

    private AnimovanyGif pasGif;
    private BazalaSimulator simulator;
    private int aktualniSpawnovanyZakaznik = 1;
    private int obsluhovanyZakaznik = 1;

    // Proměnné pro dělítko
    private int velikostNakupu;
    private boolean cekaNaDelitko = false;
    private boolean delitkoJeNaPase = false;

    // Poslední známé rozměry panelu, kvůli přepočtu při změně rozlišení.
    private int posledniSirka = 0;
    private int posledniVyska = 0;

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

    @Override
    public void setBounds(int x, int y, int sirka, int vyska) {
        super.setBounds(x, y, sirka, vyska);
        prepocitejPolozky(sirka, vyska);
    }

    /**
     * Přepočítá pozice, cíle a velikosti všech položek na pásu po změně rozměrů
     * panelu (např. při změně rozlišení), aby zůstaly na správných relativních
     * místech vůči naškálovanému pásu.
     */
    private void prepocitejPolozky(int novaSirka, int novaVyska) {
        if (posledniSirka <= 0 || posledniVyska <= 0) {
            posledniSirka = novaSirka;
            posledniVyska = novaVyska;
            return;
        }
        if (novaSirka <= 0 || novaVyska <= 0) return;
        if (novaSirka == posledniSirka && novaVyska == posledniVyska) return;

        double pomerX = (double) novaSirka / posledniSirka;
        double pomerY = (double) novaVyska / posledniVyska;

        for (PolozkaNaPase p : aktivniPolozky) {
            int novyX = (int) Math.round(p.getX() * pomerX);
            int novyY = (int) Math.round(p.getY() * pomerY);
            int novaSirkaP = (int) Math.round(p.getWidth() * pomerX);
            int novaVyskaP = (int) Math.round(p.getHeight() * pomerY);
            p.setBounds(novyX, novyY, novaSirkaP, novaVyskaP);

            p.cilX = (int) Math.round(p.cilX * pomerX);
            p.cilY = (int) Math.round(p.cilY * pomerY);

            p.prepocitejNaMeritko(scaleX(), scaleY());
        }

        posledniSirka = novaSirka;
        posledniVyska = novaVyska;
    }

    // ==========================================
    // Škálování podle aktuální velikosti panelu
    // ==========================================
    private double scaleX() {
        int w = getWidth();
        return (w <= 0) ? 1.0 : (double) w / REF_SIRKA;
    }

    private double scaleY() {
        int h = getHeight();
        return (h <= 0) ? 1.0 : (double) h / REF_VYSKA_PASU;
    }

    private int sX(int refX) { return (int) Math.round(refX * scaleX()); }
    private int sY(int refY) { return (int) Math.round(refY * scaleY()); }

    // Naškálované verze referenčních souřadnic
    private int bodZastaveni()   { return sX(REF_BOD_ZASTAVENI); }
    private int scannerX()       { return sX(REF_SCANNER_X); }
    private int scannerY()       { return sY(REF_SCANNER_Y); }
    private int odstavX()        { return sX(REF_ODSTAV_X); }
    private int velikostPolozkyX() { return sX(REF_VELIKOST_POLOZKY); }
    private int velikostPolozkyY() { return sY(REF_VELIKOST_POLOZKY); }

    // Naškálované rychlosti. RYCHLOST_POSUVU je čistě vodorovný pohyb (podle scaleX),
    // RYCHLOST_ANIMACE je šikmý pohyb po obou osách (průměr měřítek).
    // Vždy aspoň 1 px, aby se pohyb při malém měřítku nezastavil.
    private int rychlostPosuvu() {
        return Math.max(1, (int) Math.round(RYCHLOST_POSUVU * scaleX()));
    }

    private int rychlostAnimace() {
        return Math.max(1, (int) Math.round(RYCHLOST_ANIMACE * (scaleX() + scaleY()) / 2.0));
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
        List<Zbozi> dostupneZbozi = new ArrayList<>();
        for (Zbozi z : zboziList) {
            if (simulator.getOdemceneZbozi().contains(z.id)) {
                dostupneZbozi.add(z);
            }
        }
        if (dostupneZbozi.isEmpty()) {
            return zboziList.get(0);
        }
        int nahodnyIndex = random.nextInt(dostupneZbozi.size());
        return dostupneZbozi.get(nahodnyIndex);
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
            int sirkaPolozky = velikostPolozkyX();
            int vyskaPolozky = velikostPolozkyY();
            int startY = (getHeight() / 2) - (vyskaPolozky / 2);
            if(getHeight() == 0) startY = 40;

            PolozkaNaPase novaPolozka = new PolozkaNaPase(vybrane, -sirkaPolozky, startY, sirkaPolozky, vyskaPolozky, pocetKusu, aktualniSpawnovanyZakaznik, scaleX(), scaleY());

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
                        if (novaPolozka.zboziData.jeKusove()) {
                            // Logika pro kusové zboží ("Má kód")
                            if (simulator != null) {
                                simulator.pridejZboziNaUctenku(novaPolozka.zboziData, novaPolozka.pocetKusu);
                            }
                            int nahodneY = novaPolozka.getY() + sY(random.nextInt(101) - 50);
                            novaPolozka.nastavCil(odstavX(), nahodneY, PolozkaNaPase.Stav.JEDE_DO_ODSTAVNEHO_MISTA);
                        } else if (novaPolozka.zboziData.jeSkenovane()) {
                            boolean scannerVolny = true;
                            for (PolozkaNaPase p : aktivniPolozky) {
                                if (p.stav == PolozkaNaPase.Stav.JEDE_NA_SCANNER || p.stav == PolozkaNaPase.Stav.CEKA_NA_SCANNERU) {
                                    scannerVolny = false;
                                    break;
                                }
                            }
                            if (scannerVolny) {
                                novaPolozka.nastavCil(scannerX(), scannerY(), PolozkaNaPase.Stav.JEDE_NA_SCANNER);
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
        int vyskaDelitka = sY(REF_VELIKOST_POLOZKY * 2);
        int sirkaDelitka = sX(REF_VELIKOST_POLOZKY / 2);

        int startY = (getHeight() / 2) - (vyskaDelitka / 2);
        if(getHeight() == 0) startY = 40;

        PolozkaNaPase delitko = new PolozkaNaPase(delitkoData, -sirkaDelitka, startY, sirkaDelitka, vyskaDelitka, 1, aktualniSpawnovanyZakaznik, scaleX(), scaleY());

        delitko.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (delitko.idZakaznika != obsluhovanyZakaznik) return;
                if (simulator != null && simulator.cekaNaPlatbu()) return;

                if (delitko.stav == PolozkaNaPase.Stav.NA_PASE) {
                    if (delitko.getX() >= bodZastaveni()) {
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

        int bodZastaveni = bodZastaveni();
        for (PolozkaNaPase p : aktivniPolozky) {
            if (p.stav == PolozkaNaPase.Stav.NA_PASE && p.getX() >= bodZastaveni) {
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
                    int limit = bodZastaveni;
                    if (i > 0) {
                        PolozkaNaPase predchozi = aktivniPolozky.get(i - 1);
                        if (predchozi.stav == PolozkaNaPase.Stav.NA_PASE) {
                            limit = predchozi.getX() - p.getWidth();
                        }
                    }

                    if (p.getX() < limit) {
                        p.setLocation(p.getX() + rychlostPosuvu(), p.getY());
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

        int rychlost = rychlostAnimace();

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
                    int nahodneY = p.getY() + sY(random.nextInt(101) - 50);
                    p.nastavCil(odstavX(), nahodneY, PolozkaNaPase.Stav.JEDE_DO_ODSTAVNEHO_MISTA);
                    if (simulator != null) simulator.zobrazVahu(0);
                    return true;
                }
            }
        }
        return false;
    }
    public void dokonciNakupAZacniNovy() {
        if (simulator != null) simulator.vypisDoKonzole("✅ Nákup zaplacen.");
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
    public void aplikujVylepseniRychlosti(int uroven) {
        RYCHLOST_POSUVU = 2 + (uroven/2);
        RYCHLOST_ANIMACE = 8 + (uroven * 2);

        int novyDelay = 400 - (uroven * 50);
        if (spawnTimer != null) {
            spawnTimer.setDelay(novyDelay);
        }
    }
    public void vycistiVsechno() {
        if (pohybTimer != null) pohybTimer.stop();
        if (spawnTimer != null) spawnTimer.stop();
        for (PolozkaNaPase p : aktivniPolozky) {
            remove(p);
        }
        aktivniPolozky.clear();
        cekaNaDelitko = false;
        delitkoJeNaPase = false;
        aktualniSpawnovanyZakaznik = 1;
        obsluhovanyZakaznik = 1;
        repaint();
    }
}