import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VracenePenize extends JPanel {

    private class Penize {
        Image img;
        double relX, relY;
        boolean jeMince;
    }

    private List<Penize> hromadkaPenez = new ArrayList<>();
    private Random random = new Random();

    public VracenePenize() {
        setOpaque(false);
    }

    public void pridejPenize(int hodnota) {
        Penize p = new Penize();
        try {
            URL imgUrl = getClass().getResource("/minceBankovky/" + hodnota + ".png");
            if (imgUrl != null) {
                p.img = new ImageIcon(imgUrl).getImage();
            } else {
                System.out.println("Obrázek /minceBankovky/" + hodnota + ".png nebyl nalezen!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        p.jeMince = (hodnota <= 50);

        p.relX = random.nextDouble() * 0.7;
        p.relY = p.jeMince ? (random.nextDouble() * 0.7) : (random.nextDouble() * 0.23);

        hromadkaPenez.add(p);
        repaint();
    }

    public void vycistiHromadku() {
        hromadkaPenez.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (hromadkaPenez.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        for (Penize p : hromadkaPenez) {
            if (p.img != null) {
                // Bankovky půlku panelu, mince čtvrtina panelu
                int imgW = p.jeMince ? (int)(w * 0.12) : (int)(w * 0.16);

                int imgWOriginal = Math.max(1, p.img.getWidth(null));
                int imgHOriginal = p.img.getHeight(null);
                int imgH = (imgHOriginal * imgW) / imgWOriginal;

                int drawX = (int) (p.relX * w);
                int drawY = (int) (p.relY * h);

                g2.drawImage(p.img, drawX, drawY, imgW, imgH, this);
            } else {
                g2.setColor(p.jeMince ? new Color(200, 150, 50) : new Color(100, 200, 100));
                if (p.jeMince) g2.fillOval((int)(p.relX * w), (int)(p.relY * h), 30, 30);
                else g2.fillRect((int)(p.relX * w), (int)(p.relY * h), 60, 30);
            }
        }
    }
}