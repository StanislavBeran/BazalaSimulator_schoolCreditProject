package cz.bazala.simulator.ui;

import cz.bazala.simulator.model.*;
import cz.bazala.simulator.io.*;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class PolozkaNaPase extends JLabel {
    public enum Stav { NA_PASE, JEDE_NA_SCANNER, CEKA_NA_SCANNERU, JEDE_DO_ODSTAVNEHO_MISTA, V_ODSTAVNEM_MISTE, NASKENOVANO }

    Zbozi zboziData;
    int pocetKusu;
    Stav stav = Stav.NA_PASE;

    private Image img;
    private int sirkaZakladni;
    private int vyskaZakladni;

    public int vaha = 0;

    int cilX, cilY;
    public int idZakaznika;

    // Referenční (nenaškálované) posuny jednotlivých kusů v hromádce.
    private final int[] refSablonaX = {11, 0, 28, 15, 7, 21};
    private final int[] refSablonaY = {0,  9, 19, 29, 39, 48};
    private static final int REF_OFFSET = 6;

    // Aktuální škálovací faktory (odvozené od rozlišení pásu).
    private double meritkoX = 1.0;
    private double meritkoY = 1.0;

    public PolozkaNaPase(Zbozi z, int x, int y, int sirka, int vyska, int pocetKusu, int idZakaznika,
                         double meritkoX, double meritkoY) {
        this.zboziData = z;
        this.pocetKusu = pocetKusu;
        this.sirkaZakladni = sirka;
        this.vyskaZakladni = vyska;
        this.meritkoX = meritkoX;
        this.meritkoY = meritkoY;

        Random rand = new Random();
        if (z.minVaha > 0 && z.maxVaha > 0) {
            this.vaha = (rand.nextInt(z.maxVaha - z.minVaha + 1) + z.minVaha) * pocetKusu;
        }
        this.idZakaznika = idZakaznika;

        int maxShiftX = 0;
        int maxShiftY = 0;
        for (int i = 0; i < pocetKusu; i++) {
            int shiftX = posunX(i);
            int shiftY = posunY(i);
            if (shiftX > maxShiftX) maxShiftX = shiftX;
            if (shiftY > maxShiftY) maxShiftY = shiftY;
        }

        int sirkaCelkem = sirka + maxShiftX;
        int vyskaCelkem = vyska + maxShiftY;

        setBounds(x, y - (maxShiftY / 2), sirkaCelkem, vyskaCelkem);

        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setToolTipText(pocetKusu + "x " + z.nazev + " (" + z.cena + " Kč / ks)");

        nactiObrazek();
    }

    private void nactiObrazek() {
        try {
            java.net.URL imgUrl = getClass().getResource("/zboziObrazky/" + zboziData.nazev + ".png");
            if (imgUrl != null) {
                int w = Math.max(1, sirkaZakladni);
                int h = Math.max(1, vyskaZakladni);
                img = new ImageIcon(imgUrl).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            }
        } catch (Exception e) {
            System.err.println("Obrázek pro " + zboziData.nazev + " nenalezen.");
        }
    }

    // Naškálované posuny kusů v hromádce.
    private int posunX(int i) {
        int ref = (i < 6) ? refSablonaX[i] : (i * REF_OFFSET);
        return (int) Math.round(ref * meritkoX);
    }

    private int posunY(int i) {
        int ref = (i < 6) ? refSablonaY[i] : (i * REF_OFFSET);
        return (int) Math.round(ref * meritkoY);
    }

    /**
     * Přenastaví měřítko (po změně rozlišení) a znovu připraví obrázek
     * i základní rozměry podle aktuální velikosti komponenty.
     */
    public void prepocitejNaMeritko(double meritkoX, double meritkoY) {
        this.meritkoX = meritkoX;
        this.meritkoY = meritkoY;

        int maxShiftX = 0;
        int maxShiftY = 0;
        for (int i = 0; i < pocetKusu; i++) {
            int shiftX = posunX(i);
            int shiftY = posunY(i);
            if (shiftX > maxShiftX) maxShiftX = shiftX;
            if (shiftY > maxShiftY) maxShiftY = shiftY;
        }

        this.sirkaZakladni = Math.max(1, getWidth() - maxShiftX);
        this.vyskaZakladni = Math.max(1, getHeight() - maxShiftY);

        nactiObrazek();
        repaint();
    }
    public void nastavCil(int x, int y, Stav novyStav) {
        this.cilX = x;
        this.cilY = y;
        this.stav = novyStav;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        if (img != null) {
            for (int i = 0; i < pocetKusu; i++) {
                g2.drawImage(img, posunX(i), posunY(i), null);
            }
        } else {
            g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, Math.max(1, Math.min(sirkaZakladni, vyskaZakladni) / 2)));
            for (int i = 0; i < pocetKusu; i++) {
                g2.drawString("📦", posunX(i), (vyskaZakladni / 2) + posunY(i));
            }
        }
    }
}