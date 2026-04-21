import javax.swing.*;
import java.awt.*;

public class NacistHruMenu extends JPanel {
    private Menu hlavniOkno;

    public NacistHruMenu(Menu okno) {
        this.hlavniOkno = okno;

        setLayout(new BorderLayout());

        // OPRAVA CESTY: Změněno na relativní cestu
        Menu.BackgroundPanel bgPanel = okno.new BackgroundPanel("/pozadi.png");
        bgPanel.setLayout(new GridBagLayout());

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);


        JLabel label = new JLabel("Uložené hry:");
        label.setFont(new Font("Segoe UI", Font.BOLD, 20));
        label.setForeground(Color.WHITE);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(label);
        container.add(Box.createVerticalStrut(30));

        JButton btnZpet = Menu.vytvorTlacitko("Zpět do menu");
        btnZpet.addActionListener(e -> hlavniOkno.zobrazObrazovku("HLAVNI_MENU"));

        container.add(Box.createVerticalStrut(15));
        container.add(btnZpet);

        bgPanel.add(container);

        add(bgPanel, BorderLayout.CENTER);
    }
}