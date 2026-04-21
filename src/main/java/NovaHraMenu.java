import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

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

        JLabel label = new JLabel("Zadej název nové hry:");
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

        JLabel lblObtiznost = new JLabel("Vyber obtížnost:");
        stylizujLabel(lblObtiznost);
        container.add(lblObtiznost);
        container.add(Box.createVerticalStrut(5));

        String[] moznostiObtiznosti = {"Lehká", "Střední", "Obtížná", "Adam (Hardcore)"};
        JComboBox<String> cbObtiznost = new JComboBox<>(moznostiObtiznosti);
        stylizujKomponentu(cbObtiznost);
        container.add(cbObtiznost);
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
            int indexObtiznosti = cbObtiznost.getSelectedIndex();
            // Odstraníme " Kč" a převedeme na číslo
            String penizeText = ((String) cbPenize.getSelectedItem()).replace(" Kč", "");

            if (!jmenoHry.isEmpty()) {
                ulozNovouHruDoSouboru(indexSlotu, jmenoHry, indexObtiznosti, penizeText);
                hlavniOkno.zobrazObrazovku("BAZALA_SIMULATOR");
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

    private void ulozNovouHruDoSouboru(int slot, String jmeno, int obtiznost, String penize) {
        try {
            // Vytvoření složky, pokud neexistuje
            File slozka = new File("ulozeneHry");
            if (!slozka.exists()) {
                slozka.mkdir();
            }

            File soubor = new File("ulozeneHry/ulozenaHra" + slot + ".txt");
            PrintWriter writer = new PrintWriter(new FileWriter(soubor));

            writer.println("Jmeno: \"" + jmeno + "\";");
            writer.println("Obtiznost: " + obtiznost + ";");
            writer.println("Penize: " + penize + ";");

            writer.close();
            System.out.println("Hra uložena do slotu: " + slot);

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Chyba při ukládání hry: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Pomocné metody pro vzhled, aby se kód neopakoval
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