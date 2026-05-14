import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;

public class ObchodMenu extends JPanel {
    private BazalaSimulator simulator;
    private int aktualniKategorie = 1;
    private JPanel seznamVylepseni;

    public ObchodMenu(BazalaSimulator simulator) {
        this.simulator = simulator;

        setLayout(new GridBagLayout());
        setOpaque(false); // Aby fungovalo ztmavení pozadí

        // Zabrání klikání "skrz" obchod na herní pult
        addMouseListener(new MouseAdapter() {});

        // HLAVNÍ BÍLÝ PANEL OBCHODU
        JPanel oknoPanel = new JPanel(new BorderLayout());
        oknoPanel.setBackground(Color.WHITE);
        oknoPanel.setPreferredSize(new Dimension(800, 500)); // Velikost okna obchodu
        oknoPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));

        // --- HLAVIČKA (Záložky jako v PokladnaObrazovka) ---
        JPanel hlavicka = new JPanel(new BorderLayout());
        JPanel kategorie = new JPanel(new GridLayout(1, 4, 1, 1));
        kategorie.setBackground(Color.BLACK);

        kategorie.add(vytvorZalozku("NOVÉ ZBOŽÍ", 1));
        kategorie.add(vytvorZalozku("VYLEPŠENÍ", 2));
        kategorie.add(vytvorZalozku("OSTATNÍ", 3));
        hlavicka.add(kategorie, BorderLayout.NORTH);

        // Nadpis sekce
        JLabel nadpis = new JLabel("OBCHOD", SwingConstants.LEFT);
        nadpis.setFont(new Font("Arial", Font.BOLD, 24));
        nadpis.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        hlavicka.add(nadpis, BorderLayout.SOUTH);

        oknoPanel.add(hlavicka, BorderLayout.NORTH);

        seznamVylepseni = new JPanel(new GridLayout(0, 3, 5, 5));
        seznamVylepseni.setBackground(Color.WHITE);

        JPanel obalovaciPanel = new JPanel(new BorderLayout());
        obalovaciPanel.setBackground(Color.WHITE);
        obalovaciPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        obalovaciPanel.add(seznamVylepseni, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(obalovaciPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        oknoPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel paticka = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        paticka.setBackground(Color.WHITE);
        paticka.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

        JButton btnZavrit = new JButton("Zpět");
        btnZavrit.setFont(new Font("Arial", Font.BOLD, 16));
        btnZavrit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnZavrit.addActionListener(e -> setVisible(false));
        paticka.add(btnZavrit);

        oknoPanel.add(paticka, BorderLayout.SOUTH);

        add(oknoPanel);

        aktualizujNabidku();
    }

    // Metoda pro ztmavení pozadí hry
    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }

    private void aktualizujNabidku() {
        seznamVylepseni.removeAll();
        // TADY SE BUDOU GENEROVAT VĚCI DO OBCHODU PODLE KATEGORIE
        for (int i = 1; i <= 6; i++) {
            JPanel polozka = new JPanel(new BorderLayout());
            polozka.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            polozka.setBackground(new Color(245, 245, 245));
            polozka.setPreferredSize(new Dimension(200, 100));

            JLabel lblNazev = new JLabel("Položka " + i, SwingConstants.CENTER);
            lblNazev.setFont(new Font("Arial", Font.BOLD, 16));
            polozka.add(lblNazev, BorderLayout.CENTER);

            JButton btnKoupit = new JButton("Koupit (500 Kč)");
            polozka.add(btnKoupit, BorderLayout.SOUTH);

            seznamVylepseni.add(polozka);
        }

        seznamVylepseni.revalidate();
        seznamVylepseni.repaint();
    }

    private JButton vytvorZalozku(String text, int idKategorie) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (aktualniKategorie == idKategorie) g2.setColor(new Color(200, 220, 255));
                else if (getModel().isPressed()) g2.setColor(new Color(200, 200, 200));
                else if (getModel().isRollover()) g2.setColor(new Color(235, 235, 235));
                else g2.setColor(Color.WHITE);

                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(180, 180, 180));
                g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        btn.setForeground(new Color(40, 40, 40));
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            aktualniKategorie = idKategorie;
            aktualizujNabidku();
            Container parent = btn.getParent();
            if (parent != null) parent.repaint();
        });
        return btn;
    }
}