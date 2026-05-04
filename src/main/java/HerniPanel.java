import javax.swing.*;
import java.awt.*;

public class HerniPanel extends JPanel {
    private Image bgImage;
    private boolean isXpBar;

    // Data, která budeme zobrazovat
    private int penize = 0;
    private int xp = 0;
    private int maxXp = 100;
    private int level = 1;

    public HerniPanel(boolean isXpBar) {
        this.isXpBar = isXpBar;
        setOpaque(false); // Aby bylo vidět pozadí simulátoru pod panelem

        try {
            java.net.URL imgUrl = getClass().getResource("/UI.png");
            if (imgUrl != null) {
                bgImage = new ImageIcon(imgUrl).getImage();
            } else {
                System.err.println("Chyba: Obrázek UI.jpg nebyl nalezen!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Metoda pro aktualizaci peněz
    public void setPenize(int penize) {
        this.penize = penize;
        repaint();
    }

    // Metoda pro aktualizaci levelu a XP
    public void setXpData(int level, int xp, int maxXp) {
        this.level = level;
        this.xp = xp;
        this.maxXp = maxXp;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Vykreslení kovového pozadí (tvůj obrázek)
        if (bgImage != null) {
            g2.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
        }

        g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
        FontMetrics fm = g2.getFontMetrics();

        if (!isXpBar) {
            // --- VYKRESLENÍ PANELU PRO PENÍZE ---
            String text = penize + " Kč";
            int textX = (getWidth() - fm.stringWidth(text)) / 2;
            int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

            // Text nakreslíme bíle
            g2.setColor(Color.WHITE);
            g2.drawString(text, textX, textY);

        } else {
            // --- OPRAVENÉ VYKRESLENÍ XP BARU ---

            // Pevné okraje (v pixelech). Zabráníme přetečení přes kovový rám.
            int okrajX = 12; // Zleva a zprava vynecháme 12 pixelů
            int okrajY = 15;  // Shora a zdola vynecháme 8 pixelů

            int vnitrniSirka = getWidth() - (2 * okrajX);
            int vnitrniVyska = getHeight() - (2 * okrajY);

            // Kolik pixelů se má vybarvit podle XP
            int fillWidth = (int) (((double) xp / maxXp) * vnitrniSirka);

            // Vykreslení modrého pruhu přesně do černého pole
            // Přidal jsem i zaoblení rohů (poslední dvě čísla 6, 6) pro hezčí vzhled
            g2.setColor(new Color(50, 150, 255, 180));
            g2.fillRoundRect(okrajX, okrajY, fillWidth, vnitrniVyska, 6, 6);

            // Vykreslení textu s levelem uprostřed
            String text = "Level " + level;
            int textX = (getWidth() - fm.stringWidth(text)) / 2;
            int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

            // Stín textu (černý obrys pro lepší čitelnost)
            g2.setColor(Color.BLACK);
            g2.drawString(text, textX + 1, textY + 1);
            g2.setColor(Color.WHITE);
            g2.drawString(text, textX, textY);
        }
    }
}