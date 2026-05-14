import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class NacistHruMenu extends JPanel {
    private Menu hlavniOkno;
    private JPanel tlacitkaContainer;

    public NacistHruMenu(Menu okno) {
        this.hlavniOkno = okno;

        setLayout(new BorderLayout());

        Menu.BackgroundPanel bgPanel = okno.new BackgroundPanel("/pozadi.png");
        bgPanel.setLayout(new GridBagLayout());

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setOpaque(false);

        JLabel label = new JLabel("Uložené hry:");
        label.setFont(new Font("Segoe UI", Font.BOLD, 20));
        label.setForeground(Color.WHITE);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainContainer.add(label);
        mainContainer.add(Box.createVerticalStrut(20));

        tlacitkaContainer = new JPanel();
        tlacitkaContainer.setLayout(new BoxLayout(tlacitkaContainer, BoxLayout.Y_AXIS));
        tlacitkaContainer.setOpaque(false);
        mainContainer.add(tlacitkaContainer);

        mainContainer.add(Box.createVerticalStrut(20));

        JButton btnZpet = Menu.vytvorTlacitko("Zpět do menu");
        btnZpet.addActionListener(e -> hlavniOkno.zobrazObrazovku("HLAVNI_MENU"));
        mainContainer.add(btnZpet);

        bgPanel.add(mainContainer);
        add(bgPanel, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                vykresliAktualniTlacitka();
            }
        });

        vykresliAktualniTlacitka();
    }


    private void vykresliAktualniTlacitka() {

        tlacitkaContainer.removeAll();

        for (int i = 1; i <= 3; i++) {
            String cestaKSouboru = "src/main/resources/ulozeneHry/ulozenaHra" + i + ".txt";
            UlozenaHra ulozenaHra = SpravceSouboru.nactiUlozeneHryZeSouboru(cestaKSouboru);
            String textTlacitka = ulozenaHra.nazevObchodu + " lvl: " + (ulozenaHra.xp/100) + " " + ulozenaHra.pocetPenez + " Kč";

            JButton btnUlozenaHra = Menu.vytvorTlacitko(textTlacitka);

            if (textTlacitka.equals("Prázdná pozice") || textTlacitka.equals("Chyba při čtení")) {
                btnUlozenaHra.setEnabled(false);
            } else {
                final int pozice = i;
                btnUlozenaHra.addActionListener(e -> {
                    System.out.println("Načítám hru ze slotu " + pozice);
                    BazalaSimulator simulator = hlavniOkno.getSimulator();
                    simulator.nactiPenize(ulozenaHra.pocetPenez);
                    simulator.nactiXp(ulozenaHra.xp);
                    simulator.nactiVylepseni(ulozenaHra.vylepseni);
                    simulator.nactiOdemceneZbozi(ulozenaHra.odemceneZbozi);
                    simulator.nastavDetailyHry(pozice, ulozenaHra.nazevObchodu, ulozenaHra.obtiznost);
                    SpravceZvuku.zastav("obchodak_theme_sound");
                    SpravceZvuku.prehraj("hra_hudba_v_pozadi", "obchod_theme", 0, true);
                    hlavniOkno.zobrazObrazovku("BAZALA_SIMULATOR");


                    simulator.spustSimulaci();
                });
            }

            btnUlozenaHra.setAlignmentX(Component.CENTER_ALIGNMENT);
            tlacitkaContainer.add(btnUlozenaHra);
            tlacitkaContainer.add(Box.createVerticalStrut(10));
        }

        tlacitkaContainer.revalidate();
        tlacitkaContainer.repaint();
    }


}