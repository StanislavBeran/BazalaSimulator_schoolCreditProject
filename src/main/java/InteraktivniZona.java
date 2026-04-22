import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class InteraktivniZona extends JPanel {
    private int origX, origY, origW, origH;
    private boolean jeKulaty;
    private boolean isHovered = false;
    private String nazev;

    // Nová proměnná pro odkaz na hlavní simulátor
    private BazalaSimulator simulator;

    // Kratší konstruktor pro hranaté zóny
    public InteraktivniZona(int x, int y, int w, int h, String nazev, BazalaSimulator simulator) {
        this(x, y, w, h, nazev, false, simulator);
    }

    // Hlavní konstruktor pro všechny zóny
    public InteraktivniZona(int x, int y, int w, int h, String nazev, boolean jeKulaty, BazalaSimulator simulator) {
        this.origX = x;
        this.origY = y;
        this.origW = w;
        this.origH = h;
        this.nazev = nazev;
        this.jeKulaty = jeKulaty;
        this.simulator = simulator; // Uložíme si odkaz

        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setToolTipText(nazev);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("Kliknuto na předmět: " + nazev);

                if (nazev.equals("vypínač")) {
                    // Zavoláme veřejnou metodu ze simulátoru
                    simulator.prepniVypinac();
                } else if (nazev.equals("Platební terminál")) {
                    SpravceZvuku.prehraj("/pipnuti.wav");
                } else {
                    if (jeKulaty) {
                        SpravceZvuku.prehraj("/mince.wav");
                    } else {
                        SpravceZvuku.prehraj("/bankovka.wav");
                    }
                }
            }
        });
    }

    public void aktualizujPozici(double scaleW, double scaleH) {
        setBounds((int)(origX * scaleW), (int)(origY * scaleH), (int)(origW * scaleW), (int)(origH * scaleH));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (isHovered) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255, 255, 255, 100));

            if (jeKulaty) {
                g2.fillOval(0, 0, getWidth(), getHeight());
            } else {
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
            g2.dispose();
        }
    }
}