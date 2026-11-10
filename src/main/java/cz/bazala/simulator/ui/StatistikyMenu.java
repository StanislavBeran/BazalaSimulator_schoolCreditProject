package cz.bazala.simulator.ui;

import cz.bazala.simulator.model.*;
import cz.bazala.simulator.io.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Obrazovka s přehledem statistik a rekordů ze všech uložených her (slotů).
 * Data se čtou ze stejných souborů jako uložené hry přes SpravceSouboru.
 */
public class StatistikyMenu extends JPanel {
    private final Menu hlavniOkno;
    private JPanel kartyContainer;

    public StatistikyMenu(Menu okno) {
        this.hlavniOkno = okno;

        setLayout(new BorderLayout());

        Menu.BackgroundPanel bgPanel = okno.new BackgroundPanel("/pozadi.png");
        bgPanel.setLayout(new GridBagLayout());

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setOpaque(false);

        JLabel nadpis = new JLabel("Statistiky a rekordy");
        nadpis.setFont(new Font("Segoe UI", Font.BOLD, 28));
        nadpis.setForeground(Color.WHITE);
        nadpis.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainContainer.add(nadpis);
        mainContainer.add(Box.createVerticalStrut(25));

        kartyContainer = new JPanel();
        kartyContainer.setLayout(new BoxLayout(kartyContainer, BoxLayout.Y_AXIS));
        kartyContainer.setOpaque(false);
        kartyContainer.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainContainer.add(kartyContainer);

        mainContainer.add(Box.createVerticalStrut(25));

        JButton btnZpet = Menu.vytvorTlacitko("Zpět do menu");
        btnZpet.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnZpet.addActionListener(e -> hlavniOkno.zobrazObrazovku("HLAVNI_MENU"));
        mainContainer.add(btnZpet);

        bgPanel.add(mainContainer);
        add(bgPanel, BorderLayout.CENTER);

        // Při každém zobrazení přečti aktuální stav slotů.
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                obnovKarty();
            }
        });

        obnovKarty();
    }

    private void obnovKarty() {
        kartyContainer.removeAll();

        for (int slot = 1; slot <= 3; slot++) {
            UlozenaHra hra = SpravceSouboru.nactiUlozenouHru(slot);
            boolean jePrazdny = SpravceSouboru.STAV_PRAZDNY.equals(hra.nazevObchodu)
                    || SpravceSouboru.STAV_CHYBA.equals(hra.nazevObchodu);

            kartyContainer.add(vytvorKartu(slot, hra, jePrazdny));
            kartyContainer.add(Box.createVerticalStrut(12));
        }

        kartyContainer.revalidate();
        kartyContainer.repaint();
    }

    private JPanel vytvorKartu(int slot, UlozenaHra hra, boolean jePrazdny) {
        JPanel karta = new JPanel(new BorderLayout());
        karta.setMaximumSize(new Dimension(460, 160));
        karta.setPreferredSize(new Dimension(460, 160));
        karta.setBackground(new Color(45, 45, 45, 230));
        karta.setBorder(BorderFactory.createLineBorder(new Color(90, 90, 90), 1));

        String obsah;
        if (jePrazdny) {
            obsah = "<html><div style='text-align:center; color:#bbbbbb; padding:20px;'>"
                    + "<b>Pozice " + slot + "</b><br><br>(prázdná pozice)</div></html>";
        } else {
            int level = (hra.xp / 100) + 1;
            obsah = "<html><div style='color:white; padding:10px 16px;'>"
                    + "<b style='font-size:16px;'>Pozice " + slot + " &ndash; " + hra.nazevObchodu + "</b>"
                    + "<span style='color:#ffd54a;'> (level " + level + ")</span><br>"
                    + "<hr>"
                    + "<table style='color:white; font-size:12px;'>"
                    + radek("Směna", "#" + hra.cisloSmeny)
                    + radek("Obslouženo zákazníků", hra.celkemObslouzeno + "")
                    + radek("Nejdelší kombo", hra.nejlepsiKombo + " x")
                    + radek("Rekordní tržba za směnu", hra.nejlepsiTrzbaSmeny + " Kč")
                    + radek("Celkem vyděláno", hra.celkemVydelano + " Kč")
                    + "</table></div></html>";
        }

        JLabel lbl = new JLabel(obsah);
        karta.add(lbl, BorderLayout.CENTER);
        return karta;
    }

    private String radek(String popis, String hodnota) {
        return "<tr><td style='padding-right:18px;'>" + popis + ":</td>"
                + "<td><b>" + hodnota + "</b></td></tr>";
    }
}
