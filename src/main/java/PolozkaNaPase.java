import javax.swing.*;
import java.awt.*;

public class PolozkaNaPase extends JLabel {
    public enum Stav { NA_PASE, JEDE_NA_SCANNER, CEKA_NA_SCANNERU, JEDE_DO_ODSTAVU, NASKENOVANO }

    Zbozi zboziData;
    int pocetKusu;
    Stav stav = Stav.NA_PASE;

    private Image img;
    private int velikostZakladni;

    int cilX, cilY;

    // UPRAVENÝ KONSTRUKTOR: Přijímá parametr navíc (pocetKusu)
    public PolozkaNaPase(Zbozi z, int x, int y, int velikost, int pocetKusu) {
        this.zboziData = z;
        this.pocetKusu = pocetKusu;
        this.velikostZakladni = velikost;

        // Nastavení offsetu (o kolik pixelů se každý další kus posune)
        int offset = 6;
        int sirkaCelkem = velikost + ((pocetKusu - 1) * offset);
        int vyskaCelkem = velikost + ((pocetKusu - 1) * offset);

        // Zvětšíme velikost komponenty tak, aby se do ní "hromádka" vešla a neosekla se
        setBounds(x, y - ((pocetKusu - 1) * offset), sirkaCelkem, vyskaCelkem);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setToolTipText(pocetKusu + "x " + z.nazev + " (" + z.cena + " Kč / ks)");

        // Místo setIcon() si obrázek jen uložíme do proměnné img
        try {
            java.net.URL imgUrl = getClass().getResource("/zboziObrazky/" + z.nazev + ".png");
            if (imgUrl != null) {
                img = new ImageIcon(imgUrl).getImage().getScaledInstance(velikost, velikost, Image.SCALE_SMOOTH);
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
            // Vykreslujeme od prvního kusu po poslední s posunem
            for (int i = 0; i < pocetKusu; i++) {
                int drawX = i * offset;
                int drawY = i * offset;
                g2.drawImage(img, drawX, drawY, null);
            }
        } else {
            // Pokud obrázek chybí, vykreslí se textově tolik krabic, kolik je kusů
            g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, velikostZakladni / 2));
            for (int i = 0; i < pocetKusu; i++) {
                g2.drawString("📦", i * offset, (velikostZakladni / 2) + (i * offset));
            }
        }
    }
}