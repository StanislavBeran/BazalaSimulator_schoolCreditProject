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
        setBackground(new Color(0, 0, 0, 200)); // Tmavé poloprůhledné pozadí

        // Zabrání klikání "skrz" menu na herní pult
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

        // Využijeme tvoji stávající metodu z Menu.java pro stejný design tlačítek
        JButton btnPokracovat = Menu.vytvorTlacitko("Pokračovat");
        btnPokracovat.addActionListener(e -> setVisible(false));

        JButton btnNastaveni = Menu.vytvorTlacitko("Nastavení");
        btnNastaveni.addActionListener(e -> {
            // Zavře in-game menu a přepne na obrazovku nastavení v hlavním okně
            setVisible(false);
            hlavniOkno.zobrazObrazovku("NASTAVENI");
        });

        JButton btnUlozitOdejit = Menu.vytvorTlacitko("Uložit a odejít");
        btnUlozitOdejit.addActionListener(e -> {
            simulator.ulozHru();

            setVisible(false);

            SpravceZvuku.zastavVsechnuHudbu();
            SpravceZvuku.prehraj("obchodak_theme_sound", "/zvuk_v_pozadi.wav", 0, true);

            hlavniOkno.zobrazObrazovku("HLAVNI_MENU");
        });

        oknoPanel.add(btnPokracovat);
        oknoPanel.add(Box.createVerticalStrut(20));
        oknoPanel.add(btnNastaveni);
        oknoPanel.add(Box.createVerticalStrut(20));
        oknoPanel.add(btnUlozitOdejit);

        add(oknoPanel);
    }
}