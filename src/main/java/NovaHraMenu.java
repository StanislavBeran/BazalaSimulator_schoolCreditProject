import javax.swing.*;
import java.awt.*;

public class NovaHraMenu extends JPanel {
    private Menu hlavniOkno;

    public NovaHraMenu(Menu okno) {
        this.hlavniOkno = okno;

        setLayout(new BorderLayout());

        // OPRAVA CESTY: Změněno na relativní cestu
        Menu.BackgroundPanel bgPanel = okno.new BackgroundPanel("/pozadi.png");
        bgPanel.setLayout(new GridBagLayout());

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);


        JLabel label = new JLabel("Zadej název nové hry:");
        label.setFont(new Font("Segoe UI", Font.BOLD, 20));
        label.setForeground(Color.WHITE);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(label);
        container.add(Box.createVerticalStrut(20));


        JTextField txtNazev = new JTextField();
        txtNazev.setMaximumSize(new Dimension(300, 40));
        txtNazev.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        txtNazev.setBackground(new Color(200, 200, 200));
        txtNazev.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(txtNazev);
        container.add(Box.createVerticalStrut(30));

        JButton btnVytvorit = Menu.vytvorTlacitko("Vytvořit hru");
        btnVytvorit.addActionListener(e -> {
            String jmenoHry = txtNazev.getText();
            if (!jmenoHry.isEmpty()) {
                System.out.println("Vytvářím hru: " + jmenoHry);
                hlavniOkno.zobrazObrazovku("BAZALA_SIMULATOR");
            } else {
                JOptionPane.showMessageDialog(this, "Musíš zadat název!");
            }
        });

        JButton btnZpet = Menu.vytvorTlacitko("Zpět do menu");
        btnZpet.addActionListener(e -> hlavniOkno.zobrazObrazovku("HLAVNI_MENU"));

        container.add(btnVytvorit);
        container.add(Box.createVerticalStrut(15));
        container.add(btnZpet);

        bgPanel.add(container);

        add(bgPanel, BorderLayout.CENTER);
    }
}