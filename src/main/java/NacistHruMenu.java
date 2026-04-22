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
            String cestaKSouboru = "ulozeneHry/ulozenaHra" + i + ".txt";
            String textTlacitka = nactiInformaceZeSouboru(cestaKSouboru);

            JButton btnUlozenaHra = Menu.vytvorTlacitko(textTlacitka);

            if (textTlacitka.equals("Prázdná pozice") || textTlacitka.equals("Chyba při čtení")) {
                btnUlozenaHra.setEnabled(false);
            } else {
                final int pozice = i;
                btnUlozenaHra.addActionListener(e -> {
                    System.out.println("Načítám hru ze slotu " + pozice);
                    hlavniOkno.zobrazObrazovku("BAZALA_SIMULATOR");
                    SpravceZvuku.zastav("obchodak_theme_sound");
                });
            }

            btnUlozenaHra.setAlignmentX(Component.CENTER_ALIGNMENT);
            tlacitkaContainer.add(btnUlozenaHra);
            tlacitkaContainer.add(Box.createVerticalStrut(10));
        }

        tlacitkaContainer.revalidate();
        tlacitkaContainer.repaint();
    }

    private String nactiInformaceZeSouboru(String cesta) {
        File soubor = new File(cesta);

        if (!soubor.exists() || soubor.length() == 0) {
            return "Prázdná pozice";
        }

        String jmeno = "Neznámé";
        String obtiznost = "Neznámá";
        String penize = "0";

        try (BufferedReader br = new BufferedReader(new FileReader(soubor))) {
            String radek;
            while ((radek = br.readLine()) != null) {
                if (radek.trim().isEmpty()) continue;
                if (radek.contains(":")) {
                    String hodnota = radek.substring(radek.indexOf(":") + 1).replace(";", "").trim();

                    if (hodnota.startsWith("\"") && hodnota.endsWith("\"")) {
                        hodnota = hodnota.substring(1, hodnota.length() - 1);
                    }

                    if (radek.startsWith("Jmeno")) {
                        jmeno = hodnota;
                    } else if (radek.startsWith("Obtiznost")) {
                        switch (hodnota) {
                            case "0": obtiznost = "Lehká"; break;
                            case "1": obtiznost = "Střední"; break;
                            case "2": obtiznost = "Obtížná"; break;
                            case "3": obtiznost = "Adam (Hardcore)"; break;
                            default: obtiznost = hodnota; break;
                        }
                    } else if (radek.startsWith("Penize")) {
                        penize = hodnota;
                    }
                }
            }
            return jmeno + " | Obtížnost: " + obtiznost + " | Peníze: " + penize;

        } catch (IOException e) {
            e.printStackTrace();
            return "Chyba při čtení";
        }
    }
}