import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;

public class HerniMenu extends JPanel {
    private BazalaSimulator simulator;
    private Menu hlavniOkno;

    public HerniMenu(BazalaSimulator simulator, Menu hlavniOkno) {
        this.simulator = simulator;
        this.hlavniOkno = hlavniOkno;

        setLayout(new GridBagLayout());
        setOpaque(false);

        addMouseListener(new MouseAdapter() {});

        JPanel oknoPanel = new JPanel();
        oknoPanel.setLayout(new BoxLayout(oknoPanel, BoxLayout.Y_AXIS));
        oknoPanel.setBackground(new Color(40, 40, 40));
        oknoPanel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        JLabel nadpis = new JLabel("PAUZA");
        nadpis.setFont(new Font("Segoe UI", Font.BOLD, 36));
        nadpis.setForeground(Color.WHITE);
        nadpis.setAlignmentX(Component.CENTER_ALIGNMENT);
        oknoPanel.add(nadpis);
        oknoPanel.add(Box.createVerticalStrut(40));

        JButton btnPokracovat = Menu.vytvorTlacitko("Pokračovat");
        btnPokracovat.addActionListener(e -> setVisible(false));

        JButton btnNastaveni = Menu.vytvorTlacitko("Nastavení");
        btnNastaveni.addActionListener(e -> {
            hlavniOkno.setPredchoziObrazovka("BAZALA_SIMULATOR");
            hlavniOkno.zobrazObrazovku("NASTAVENI");
        });

        JButton btnUlozitOdejit = Menu.vytvorTlacitko("Uložit a odejít");
        btnUlozitOdejit.addActionListener(e -> {
            simulator.ulozHru();
            simulator.resetujHerniPole();
            setVisible(false);
            SpravceZvuku.zastavVsechnuHudbu();
            SpravceZvuku.prehraj("obchodak_theme_sound", "zvuk_v_pozadi", 0, true);
            hlavniOkno.zobrazObrazovku("HLAVNI_MENU");
        });

        oknoPanel.add(btnPokracovat);
        oknoPanel.add(Box.createVerticalStrut(20));
        oknoPanel.add(btnNastaveni);
        oknoPanel.add(Box.createVerticalStrut(20));
        oknoPanel.add(btnUlozitOdejit);

        add(oknoPanel);
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }
}