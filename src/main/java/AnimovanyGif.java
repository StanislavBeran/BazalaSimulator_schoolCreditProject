import javax.swing.*;
import java.awt.*;

public class AnimovanyGif extends JPanel {
    private Image gifImage;

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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (gifImage != null) {
            g.drawImage(gifImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}