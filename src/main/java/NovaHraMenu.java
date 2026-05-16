import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class NovaHraMenu extends JPanel {
    private Menu hlavniOkno;

    public NovaHraMenu(Menu okno) {
        this.hlavniOkno = okno;

        setLayout(new BorderLayout());

        Menu.BackgroundPanel bgPanel = okno.new BackgroundPanel("/pozadi.png");
        bgPanel.setLayout(new GridBagLayout());

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);

        JLabel label = new JLabel("Zadejte název tvého obchodu:");
        stylizujLabel(label);
        container.add(label);
        container.add(Box.createVerticalStrut(5));

        JTextField txtNazev = new JTextField();
        stylizujKomponentu(txtNazev);
        container.add(txtNazev);
        container.add(Box.createVerticalStrut(15));

        JLabel lblSlot = new JLabel("Vyber pozici pro uložení:");
        stylizujLabel(lblSlot);
        container.add(lblSlot);
        container.add(Box.createVerticalStrut(5));

        String[] moznostiSlotu = {"Pozice 1", "Pozice 2", "Pozice 3"};
        JComboBox<String> cbSlot = new JComboBox<>(moznostiSlotu);
        stylizujKomponentu(cbSlot);
        container.add(cbSlot);
        container.add(Box.createVerticalStrut(15));

        JLabel lblPenize = new JLabel("Začáteční počet peněz:");
        stylizujLabel(lblPenize);
        container.add(lblPenize);
        container.add(Box.createVerticalStrut(5));

        String[] moznostiPenez = {"1000 Kč", "2000 Kč", "5000 Kč", "10000 Kč"};
        JComboBox<String> cbPenize = new JComboBox<>(moznostiPenez);
        stylizujKomponentu(cbPenize);
        container.add(cbPenize);
        container.add(Box.createVerticalStrut(25));


        // --- TLAČÍTKO VYTVOŘIT ---
        JButton btnVytvorit = Menu.vytvorTlacitko("Vytvořit hru");
        btnVytvorit.addActionListener(e -> {
            String jmenoHry = txtNazev.getText();
            int indexSlotu = cbSlot.getSelectedIndex() + 1;
            String penizeText = ((String) cbPenize.getSelectedItem()).replace(" Kč", "");

            if (!jmenoHry.isEmpty()) {
                List<Integer> startovniZbozi = new ArrayList<>();
                startovniZbozi.add(16);
                SpravceSouboru.ulozHruDoSouboru(indexSlotu, jmenoHry, penizeText, 0, "00000", startovniZbozi, "obchod_theme", new ArrayList<>(List.of("obchod_theme")));
                BazalaSimulator simulator = hlavniOkno.getSimulator();
                simulator.nactiPenize(Integer.parseInt(penizeText));
                simulator.nactiXp(0);
                simulator.nactiVylepseni("00000");
                simulator.nactiOdemceneZbozi(startovniZbozi);

                simulator.nastavDetailyHry(indexSlotu, jmenoHry, "obchod_theme", null);
                SpravceZvuku.zastav("obchodak_theme_sound");
                SpravceZvuku.prehraj("hra_hudba_v_pozadi","obchod_theme", 0, true);
                hlavniOkno.zobrazObrazovku("BAZALA_SIMULATOR");

                simulator.zobrazNavodANastartujHru();
            } else {
                JOptionPane.showMessageDialog(this, "Musíš zadat název!");
            }
        });

        // --- TLAČÍTKO ZPĚT ---
        JButton btnZpet = Menu.vytvorTlacitko("Zpět do menu");
        btnZpet.addActionListener(e -> hlavniOkno.zobrazObrazovku("HLAVNI_MENU"));

        container.add(btnVytvorit);
        container.add(Box.createVerticalStrut(10));
        container.add(btnZpet);

        bgPanel.add(container);
        add(bgPanel, BorderLayout.CENTER);
    }

    private void stylizujLabel(JLabel l) {
        l.setFont(new Font("Segoe UI", Font.BOLD, 18));
        l.setForeground(Color.WHITE);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private void stylizujKomponentu(JComponent c) {
        c.setMaximumSize(new Dimension(300, 35));
        c.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        c.setBackground(new Color(200, 200, 200));
        c.setAlignmentX(Component.CENTER_ALIGNMENT);
    }
}