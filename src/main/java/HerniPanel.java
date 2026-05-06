import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class HerniPanel extends JPanel {
    public enum Typ { PENIZE, XP, KONZOLE }

    private Image bgImage;
    private Typ typPanelu;

    // Data pro Peníze a XP
    private int penize = 0;
    private int xp = 0;
    private int maxXp = 100;
    private int level = 1;

    // Seznam zpráv pro Konzoli
    private List<String> zpravy = new ArrayList<>();

    public HerniPanel(Typ typPanelu) {
        this.typPanelu = typPanelu;
        setOpaque(false);

        try {
            java.net.URL imgUrl = getClass().getResource("/UI.png");
            if (imgUrl != null) {
                bgImage = new ImageIcon(imgUrl).getImage();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPenize(int penize) {
        this.penize = penize;
        repaint();
    }

    public void setXpData(int level, int xp, int maxXp) {
        this.level = level;
        this.xp = xp;
        this.maxXp = maxXp;
        repaint();
    }

    // Přidá text a umaže starý
    public void pridejZpravu(String zprava) {
        zpravy.add(zprava);
        if (zpravy.size() > 4) {
            zpravy.remove(0);
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (bgImage != null) {
            g2.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
        }

        FontMetrics fm;

        if (typPanelu == Typ.PENIZE) {
            g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
            fm = g2.getFontMetrics();
            String text = penize + " Kč";
            int textX = (getWidth() - fm.stringWidth(text)) / 2;
            int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.setColor(Color.WHITE);
            g2.drawString(text, textX, textY);

        } else if (typPanelu == Typ.XP) {
            g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
            fm = g2.getFontMetrics();
            int okrajX = 12;
            int okrajY = 15;
            int vnitrniSirka = getWidth() - (2 * okrajX);
            int vnitrniVyska = getHeight() - (2 * okrajY);
            int fillWidth = (int) (((double) xp / maxXp) * vnitrniSirka);

            g2.setColor(new Color(50, 150, 255, 180));
            g2.fillRoundRect(okrajX, okrajY, fillWidth, vnitrniVyska, 6, 6);

            String text = "Level " + level;
            int textX = (getWidth() - fm.stringWidth(text)) / 2;
            int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.setColor(Color.BLACK);
            g2.drawString(text, textX + 1, textY + 1);
            g2.setColor(Color.WHITE);
            g2.drawString(text, textX, textY);

        } else if (typPanelu == Typ.KONZOLE) {
            // Vykreslení konzole (jako v opravdovém terminálu)
            g2.setFont(new Font("Monospaced", Font.BOLD, 10));
            g2.setColor(new Color(180, 255, 180));
            int y = 25; // Počáteční Y pozice shora (uvnitř černého pole)
            for (String zprava : zpravy) {
                g2.drawString("> " + zprava, 20, y); // Každý řádek začne šipkou
                y += 20; // Posuneme se o 20 pixelů níž pro další řádek
            }
        }
    }
}