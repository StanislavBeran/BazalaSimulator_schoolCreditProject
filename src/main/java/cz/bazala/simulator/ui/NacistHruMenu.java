package cz.bazala.simulator.ui;

import cz.bazala.simulator.model.*;
import cz.bazala.simulator.io.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

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
            UlozenaHra ulozenaHra = SpravceSouboru.nactiUlozenouHru(i);

            boolean jePrazdny = SpravceSouboru.STAV_PRAZDNY.equals(ulozenaHra.nazevObchodu)
                    || SpravceSouboru.STAV_CHYBA.equals(ulozenaHra.nazevObchodu);

            String textTlacitka = jePrazdny
                    ? ulozenaHra.nazevObchodu
                    : ulozenaHra.nazevObchodu + " lvl: " + ((ulozenaHra.xp / 100) + 1) + " " + ulozenaHra.pocetPenez + " Kč";

            JButton btnUlozenaHra = Menu.vytvorTlacitko(textTlacitka);

            if (jePrazdny) {
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
                    simulator.nastavDetailyHry(pozice, ulozenaHra.nazevObchodu, ulozenaHra.vybranaHudba, ulozenaHra.odemcenaHudba);
                    SpravceZvuku.zastav("obchodak_theme_sound");
                    if (!"Bez hudby".equals(ulozenaHra.vybranaHudba)) {
                        SpravceZvuku.prehraj("hra_hudba_v_pozadi", ulozenaHra.vybranaHudba, 0, true);
                    }
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