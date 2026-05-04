import javax.swing.*;
import java.awt.*;

public class InformacniOkno extends JPanel {
    private Image bgImage;

    // Data, která budeme zobrazovat
    private String text = "";

    public InformacniOkno(String text) {
        this.text = text;
        setOpaque(false); // Aby bylo vidět pozadí simulátoru pod panelem

        try {
            java.net.URL imgUrl = getClass().getResource("/informacniOkno.png");
            if (imgUrl != null) {
                bgImage = new ImageIcon(imgUrl).getImage();
            } else {
                System.err.println("Chyba: Obrázek informacniOkno.jpg nebyl nalezen!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPenize(String text) {
        this.text = text;
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

        g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
        FontMetrics fm = g2.getFontMetrics();
        int textX = (getWidth() - fm.stringWidth(text)) / 2;
        int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

        g2.setColor(Color.WHITE);
        g2.drawString(text, textX, textY);
    }
}