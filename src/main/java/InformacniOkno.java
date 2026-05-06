import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;

public class InformacniOkno extends JPanel {
    private Runnable akcePoZavreni;

    public InformacniOkno(Runnable akcePoZavreni) {
        this.akcePoZavreni = akcePoZavreni;
        setLayout(new GridBagLayout()); // Slouží k vycentrování obsahu
        setBackground(new Color(0, 0, 0, 180)); // Poloprůhledné černé pozadí

        // Tento prázdný listener "požírá" kliknutí myši, aby neprošla dolů na hru
        addMouseListener(new MouseAdapter() {});

        // --- VNITŘNÍ BOX S NÁVODEM ---
        JPanel oknoPanel = new JPanel();
        oknoPanel.setLayout(new BoxLayout(oknoPanel, BoxLayout.Y_AXIS));
        oknoPanel.setBackground(new Color(40, 40, 40));
        oknoPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Nápis
        JLabel nadpis = new JLabel("Vítej v Bazala Simulatoru!");
        nadpis.setFont(new Font("Segoe UI", Font.BOLD, 28));
        nadpis.setForeground(Color.WHITE);
        nadpis.setAlignmentX(Component.CENTER_ALIGNMENT);
        oknoPanel.add(nadpis);
        oknoPanel.add(Box.createVerticalStrut(20));

        // Text návodu (používáme HTML tagy pro odřádkování a ztučnění)
        String navod = "<html><div style='text-align: center; width: 400px;'>" +
                "Tady je rychlý návod k tvé první směně na pokladně:<br><br>" +
                "1. Zboží na pásu <b>bez kódu</b> (např. typ 0) pouze rozklikni.<br>" +
                "2. Zboží <b>s kódem</b> dojede na scanner a zastaví se.<br>" +
                "3. Na číselníku zadej <b>[ID]*[POČET]</b> a stiskni <b>ENTER</b>.<br>" +
                "4. Nákup zákazníka končí odkliknutím <b>dělítka nákupu</b>.<br><br>" +
                "Hodně štěstí a co nejméně naštvaných zákazníků!</div></html>";

        JLabel textLabel = new JLabel(navod);
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        textLabel.setForeground(new Color(220, 220, 220));
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        oknoPanel.add(textLabel);
        oknoPanel.add(Box.createVerticalStrut(30));

        // Tlačítko pro start
        JButton btnZavrit = new JButton("Rozumím, jdeme na to!") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) g2.setColor(new Color(0, 120, 0));
                else if (getModel().isRollover()) g2.setColor(new Color(0, 180, 0));
                else g2.setColor(new Color(0, 150, 0));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnZavrit.setContentAreaFilled(false);
        btnZavrit.setFocusPainted(false);
        btnZavrit.setBorderPainted(false);
        btnZavrit.setForeground(Color.WHITE);
        btnZavrit.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnZavrit.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnZavrit.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Akce po kliknutí: skryje okno a zavolá příkaz ke startu pásu
        btnZavrit.addActionListener(e -> {
            setVisible(false);
            if (this.akcePoZavreni != null) {
                this.akcePoZavreni.run();
            }
        });

        oknoPanel.add(btnZavrit);
        add(oknoPanel);
    }
}