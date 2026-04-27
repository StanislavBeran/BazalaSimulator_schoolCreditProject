import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class AnimovanyGif extends JPanel {
    private Image gifImage;
    private boolean bezi = true;
    private BufferedImage zastavenySnimek;

    public AnimovanyGif(String cestaKObrazku) {
        setOpaque(false);
        try {
            java.net.URL imgUrl = getClass().getResource(cestaKObrazku);
            if (imgUrl != null) {
                gifImage = new ImageIcon(imgUrl).getImage();
            } else {
                System.err.println("Chyba: Soubor " + cestaKObrazku + " nebyl nalezen v resources.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void beziPas(boolean maBezet) {
        if (this.bezi == maBezet) return; // Nemusíme nic dělat, pokud se stav nemění
        this.bezi = maBezet;

        if (!bezi && getWidth() > 0 && getHeight() > 0) {
            // Trik pro zastavení: Vyfotíme aktuální snímek GIFu a uložíme si ho
            zastavenySnimek = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = zastavenySnimek.createGraphics();
            g2.drawImage(gifImage, 0, 0, getWidth(), getHeight(), null);
            g2.dispose();
        } else {
            zastavenySnimek = null; // Smažeme fotku, GIF se začne zase hybat
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Pokud běží, vykreslujeme normálně GIF. Pokud stojí, vykreslíme naši statickou "fotku".
        if (bezi && gifImage != null) {
            g.drawImage(gifImage, 0, 0, getWidth(), getHeight(), this);
        } else if (!bezi && zastavenySnimek != null) {
            g.drawImage(zastavenySnimek, 0, 0, null);
        }
    }
}