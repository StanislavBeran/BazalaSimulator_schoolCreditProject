import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class NastaveniMenu extends JPanel {
    private Menu hlavniOkno;
    private JSlider hlasitostSlider;
    private JComboBox<String> hudbaComboBox;
    private BazalaSimulator simulator;
    private boolean nacitaniDat = false;

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
        });
        mainContainer.add(hlasitostSlider);
        mainContainer.add(Box.createVerticalStrut(30));

        JLabel hudbaLabel = new JLabel("Hudba:");
        hudbaLabel.setForeground(Color.WHITE);
        hudbaLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        hudbaLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainContainer.add(hudbaLabel);

        hudbaComboBox = new JComboBox<>();
        hudbaComboBox.setMaximumSize(new Dimension(300, 35));
        hudbaComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        hudbaComboBox.setBackground(new Color(200, 200, 200));
        hudbaComboBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        hudbaComboBox.addActionListener(e -> {
            if (hudbaComboBox.getSelectedItem() != null) {
                if (nacitaniDat) return;
                String vybranaZobrazena = (String) hudbaComboBox.getSelectedItem();
                BazalaSimulator sim = hlavniOkno.getSimulator();
                if (sim != null && sim.getOdemcenaHudba() != null) {
                    String vybranaPuvodni = null;
                    for (String h : sim.getOdemcenaHudba()) {
                        if (h.replace('_', ' ').equals(vybranaZobrazena)) {
                            vybranaPuvodni = h;
                            break;
                        }
                    }
                    if (vybranaPuvodni != null && !vybranaPuvodni.equals(sim.getVybranaHudba())) {
                        sim.nastavVybranouHudbu(vybranaPuvodni);
                        if (hlavniOkno.getPredchoziObrazovka().equals("BAZALA_SIMULATOR")) {
                            SpravceZvuku.zastavVsechnuHudbu();
                            SpravceZvuku.prehraj(vybranaPuvodni, vybranaPuvodni, 0, true);
                        }
                    }
                }
            }
        });
        mainContainer.add(hudbaComboBox);
        mainContainer.add(Box.createVerticalStrut(50));

        JButton btnZpet = Menu.vytvorTlacitko("Zpět");
        btnZpet.addActionListener(e -> {
            hlavniOkno.zobrazObrazovku(hlavniOkno.getPredchoziObrazovka());
        });
        mainContainer.add(btnZpet);

        bgPanel.add(mainContainer);
        add(bgPanel, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                obnovNabidkuHudby();
            }
        });
    }
    private void obnovNabidkuHudby() {
        nacitaniDat = true;
        hudbaComboBox.removeAllItems();
        simulator = hlavniOkno.getSimulator();
        if (simulator != null && simulator.getOdemcenaHudba() != null) {
            for (String h : simulator.getOdemcenaHudba()){
                hudbaComboBox.addItem(h.replace('_', ' '));
            }
            if (simulator.getVybranaHudba() != null) {
                hudbaComboBox.setSelectedItem(simulator.getVybranaHudba().replace('_', ' '));
            }
        } else {
            hudbaComboBox.addItem("obchod theme");
        }
        nacitaniDat = false;
    }
}