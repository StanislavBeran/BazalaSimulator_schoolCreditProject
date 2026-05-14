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

    // UPRAVENÝ KONSTRUKTOR: Přijímá parametr navíc (pocetKusu)
    public PolozkaNaPase(Zbozi z, int x, int y, int sirka, int vyska, int pocetKusu, int idZakaznika) {
        this.zboziData = z;
        this.pocetKusu = pocetKusu;
        this.sirkaZakladni = sirka;
        this.vyskaZakladni = vyska;

        Random rand = new Random();
        if (z.minVaha > 0 && z.maxVaha > 0) {
            this.vaha = (rand.nextInt(z.maxVaha - z.minVaha + 1) + z.minVaha) * pocetKusu;
        }
        this.idZakaznika = idZakaznika;
        int offset = 6;
        int sirkaCelkem = sirka + ((pocetKusu - 1) * offset);
        int vyskaCelkem = vyska + ((pocetKusu - 1) * offset);


        // Zvětšíme velikost komponenty tak, aby se do ní "hromádka" vešla a neosekla se
        setBounds(x, y - ((pocetKusu - 1) * offset), sirkaCelkem, vyskaCelkem);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setToolTipText(pocetKusu + "x " + z.nazev + " (" + z.cena + " Kč / ks)");

        try {
            java.net.URL imgUrl = getClass().getResource("/zboziObrazky/" + z.nazev + ".png");
            if (imgUrl != null) {
                img = new ImageIcon(imgUrl).getImage().getScaledInstance(sirka, vyska, Image.SCALE_SMOOTH);

            }
        } catch (Exception e) {
            System.err.println("Obrázek pro " + z.nazev + " nenalezen.");
        }
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

        int offset = 6;

        if (img != null) {
            for (int i = 0; i < pocetKusu; i++) {
                int drawX = i * offset;
                int drawY = i * offset;
                g2.drawImage(img, drawX, drawY, null);
            }
        } else {
            // Pokud chybí obrázek, krabice se přizpůsobí menšímu z rozměrů
            g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, Math.min(sirkaZakladni, vyskaZakladni) / 2));
            for (int i = 0; i < pocetKusu; i++) {
                g2.drawString("📦", i * offset, (vyskaZakladni / 2) + (i * offset));
            }
        }
    }
}