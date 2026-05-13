import javax.swing.*;
import java.awt.*;

public class NastaveniMenu extends JPanel {
    private Menu hlavniOkno;
    private JSlider hlasitostSlider;

    public NastaveniMenu(Menu okno) {
        this.hlavniOkno = okno;
        setLayout(new BorderLayout());

        Menu.BackgroundPanel bgPanel = okno.new BackgroundPanel("/pozadi.png");
        bgPanel.setLayout(new GridBagLayout());

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setOpaque(false);

        JLabel nadpis = new JLabel("NASTAVENÍ");
        nadpis.setFont(new Font("Segoe UI", Font.BOLD, 36));
        nadpis.setForeground(Color.WHITE);
        nadpis.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainContainer.add(nadpis);
        mainContainer.add(Box.createVerticalStrut(40));

        JLabel volLabel = new JLabel("Hlasitost hudby a zvuků:");
        volLabel.setForeground(Color.WHITE);
        volLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        volLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainContainer.add(volLabel);

        hlasitostSlider = new JSlider(0, 100, 100);
        hlasitostSlider.setOpaque(false);
        hlasitostSlider.setMajorTickSpacing(20);
        hlasitostSlider.setMinorTickSpacing(5);
        hlasitostSlider.setPaintTicks(true);
        hlasitostSlider.setPaintLabels(true);
        hlasitostSlider.setForeground(Color.WHITE);
        hlasitostSlider.setMaximumSize(new Dimension(300, 50));

        hlasitostSlider.addChangeListener(e -> {
            SpravceZvuku.nastavHlasitost(hlasitostSlider.getValue());
            if (!hlasitostSlider.getValueIsAdjusting()) {
                SpravceZvuku.prehraj("test_zvuk", "bankovka", 0, false);
            }
        });
        mainContainer.add(hlasitostSlider);
        mainContainer.add(Box.createVerticalStrut(50));

        JButton btnZpet = Menu.vytvorTlacitko("Zpět");
        btnZpet.addActionListener(e -> {
            hlavniOkno.zobrazObrazovku(hlavniOkno.getPredchoziObrazovka());
        });
        mainContainer.add(btnZpet);

        bgPanel.add(mainContainer);
        add(bgPanel, BorderLayout.CENTER);
    }

}