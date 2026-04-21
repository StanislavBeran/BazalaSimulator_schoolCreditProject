import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * Výuková aplikace pro steganografii ve Swingu.
 *
 * Tato verze:
 * - NEPOUŽÍVÁ délku zprávy uloženou ve 32 bitech
 * - místo toho používá pevnou HLAVIČKU + KONEC ZPRÁVY
 *
 * Formát uložených dat:
 *
 * [MAGIC][ESCAPOVANÁ_DATA][END]
 *
 * kde:
 * - MAGIC = pevná identifikační značka, podle které poznáme,
 *           že obrázek obsahuje zprávu vytvořenou tímto programem
 * - ESCAPOVANÁ_DATA = samotný text, upravený tak, aby se speciální bajty
 *                     nepletly s řídicími znaky
 * - END = skutečný konec zprávy
 *
 * Použité speciální bajty:
 * - MAGIC = "SIFRA"
 * - ESC   = 27 (escape byte)
 * - END   = 0  (konec zprávy)
 *
 * Escape pravidla:
 * - pokud se v textu vyskytne ESC, uložíme ESC ESC
 * - pokud se v textu vyskytne END, uložíme ESC END
 *
 * Díky tomu při dekódování bezpečně rozpoznáme:
 * - začátek zprávy
 * - konec zprávy
 * - a přitom dovolíme, aby se speciální hodnoty objevily i uvnitř textu
 */
public class SteganografieSwing extends JFrame {

    // =========================================================
    // KONSTANTY FORMÁTU
    // =========================================================

    /**
     * Pevná hlavička.
     *
     * Tohle je identifikační značka formátu.
     * Při dekódování se nejprve kontroluje, zda první bajty
     * skutečně odpovídají této hodnotě.
     *
     * Pokud ne, budeme předpokládat, že obrázek neobsahuje
     * zprávu vytvořenou tímto programem.
     */
    private static final byte[] MAGIC = "SIFRA".getBytes(StandardCharsets.US_ASCII);

    /**
     * Escape byte.
     *
     * Když se v textu objeví speciální bajt,
     * uloží se s prefixem ESC.
     */
    private static final int ESC = 27; // 0x1B

    /**
     * Konec zprávy.
     *
     * Tento bajt přidáváme na úplný konec datového bloku.
     * Dekodér podle něj pozná, kde zpráva skutečně končí.
     */
    private static final int END = 0; // 0x00

    // =========================================================
    // ATRIBUTY GUI
    // =========================================================

    /**
     * Aktuální obrázek v paměti.
     */
    private BufferedImage obrazek;

    /**
     * Komponenta pro zobrazení obrázku.
     */
    private JLabel labelObrazek;

    /**
     * Vstupní text - text ke skrytí do obrázku.
     */
    private JTextArea vstupText;

    /**
     * Výstupní text - text vyčtený z obrázku.
     */
    private JTextArea vystupText;

    /**
     * Stavový řádek dole v okně.
     */
    private JLabel stavovyRadek;

    // =========================================================
    // KONSTRUKTOR
    // =========================================================

    public SteganografieSwing() {
        setTitle("Steganografie bez délky ve 32 bitech - Swing");
        setSize(1150, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        vytvorGui();
    }

    // =========================================================
    // VYTVOŘENÍ GUI
    // =========================================================

    private void vytvorGui() {
        JPanel hlavniPanel = new JPanel(new BorderLayout());

        // -----------------------------------------------------
        // HORNÍ PANEL S TLAČÍTKY
        // -----------------------------------------------------
        JPanel horniPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton btnNacist = new JButton("Načíst obrázek");
        JButton btnZakodovat = new JButton("Zakódovat text");
        JButton btnDekodovat = new JButton("Dekódovat text");
        JButton btnUlozit = new JButton("Uložit obrázek");

        horniPanel.add(btnNacist);
        horniPanel.add(btnZakodovat);
        horniPanel.add(btnDekodovat);
        horniPanel.add(btnUlozit);

        hlavniPanel.add(horniPanel, BorderLayout.NORTH);

        // -----------------------------------------------------
        // STŘEDNÍ ČÁST OKNA
        // -----------------------------------------------------
        JPanel stredniPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        // Levá část - obrázek
        JPanel levyPanel = new JPanel(new BorderLayout());
        levyPanel.setBorder(BorderFactory.createTitledBorder("Obrázek"));

        labelObrazek = new JLabel("Zatím není načten žádný obrázek", SwingConstants.CENTER);
        levyPanel.add(new JScrollPane(labelObrazek), BorderLayout.CENTER);

        // Pravá část - textová pole
        JPanel pravyPanel = new JPanel(new GridLayout(2, 1, 5, 5));

        JPanel panelVstup = new JPanel(new BorderLayout());
        panelVstup.setBorder(BorderFactory.createTitledBorder("Text ke skrytí"));
        vstupText = new JTextArea();
        vstupText.setLineWrap(true);
        vstupText.setWrapStyleWord(true);
        panelVstup.add(new JScrollPane(vstupText), BorderLayout.CENTER);

        JPanel panelVystup = new JPanel(new BorderLayout());
        panelVystup.setBorder(BorderFactory.createTitledBorder("Vyčtený text"));
        vystupText = new JTextArea();
        vystupText.setLineWrap(true);
        vystupText.setWrapStyleWord(true);
        panelVystup.add(new JScrollPane(vystupText), BorderLayout.CENTER);

        pravyPanel.add(panelVstup);
        pravyPanel.add(panelVystup);

        stredniPanel.add(levyPanel);
        stredniPanel.add(pravyPanel);

        hlavniPanel.add(stredniPanel, BorderLayout.CENTER);

        // -----------------------------------------------------
        // STAVOVÝ ŘÁDEK
        // -----------------------------------------------------
        stavovyRadek = new JLabel("Připraveno.");
        stavovyRadek.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        hlavniPanel.add(stavovyRadek, BorderLayout.SOUTH);

        setContentPane(hlavniPanel);

        // -----------------------------------------------------
        // OBSLUHA TLAČÍTEK
        // -----------------------------------------------------
        btnNacist.addActionListener(e -> nactiObrazek());
        btnUlozit.addActionListener(e -> ulozObrazek());
        btnZakodovat.addActionListener(e -> zakodujTextDoObrazku());
        btnDekodovat.addActionListener(e -> dekodujTextZObrazku());
    }

    // =========================================================
    // NAČTENÍ A ULOŽENÍ OBRÁZKU
    // =========================================================

    private void nactiObrazek() {
        JFileChooser vyber = new JFileChooser();
        vyber.setDialogTitle("Vyber obrázek");

        int vysledek = vyber.showOpenDialog(this);

        if (vysledek == JFileChooser.APPROVE_OPTION) {
            File soubor = vyber.getSelectedFile();

            try {
                obrazek = ImageIO.read(soubor);

                if (obrazek == null) {
                    throw new Exception("Vybraný soubor není podporovaný obrázek.");
                }

                zobrazObrazek(obrazek);
                stavovyRadek.setText("Načten obrázek: " + soubor.getAbsolutePath());

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        this,
                        ex.getMessage(),
                        "Chyba",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void ulozObrazek() {
        if (obrazek == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Nejdříve načti nebo vytvoř obrázek.",
                    "Upozornění",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        JFileChooser vyber = new JFileChooser();
        vyber.setDialogTitle("Uložit obrázek");

        int vysledek = vyber.showSaveDialog(this);

        if (vysledek == JFileChooser.APPROVE_OPTION) {
            File soubor = vyber.getSelectedFile();

            try {
                if (!soubor.getName().toLowerCase().endsWith(".png")) {
                    soubor = new File(soubor.getAbsolutePath() + ".png");
                }

                ImageIO.write(obrazek, "png", soubor);
                stavovyRadek.setText("Obrázek uložen: " + soubor.getAbsolutePath());

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        this,
                        ex.getMessage(),
                        "Chyba",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void zobrazObrazek(BufferedImage img) {
        labelObrazek.setText("");
        labelObrazek.setIcon(new ImageIcon(img));
    }

    // =========================================================
    // KÓDOVÁNÍ
    // =========================================================

    private void zakodujTextDoObrazku() {
        if (obrazek == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Nejdříve načti obrázek.",
                    "Upozornění",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String text = vstupText.getText();

        if (text == null || text.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Zadej text ke skrytí.",
                    "Upozornění",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            obrazek = zakoduj(obrazek, text);
            zobrazObrazek(obrazek);
            stavovyRadek.setText("Text byl zakódován do obrázku.");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Chyba",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Zakóduje text do obrázku metodou LSB.
     *
     * Hlavní myšlenka:
     * 1) Nejprve vytvoříme celý datový blok:
     *    [MAGIC][escapovaná data][END]
     *
     * 2) Tento blok převedeme na bity.
     *
     * 3) Jednotlivé bity ukládáme do nejnižších bitů složek:
     *    - R
     *    - G
     *    - B
     *
     * Každý pixel tedy unese 3 bity.
     */
    private BufferedImage zakoduj(BufferedImage puvodni, String text) throws Exception {
        // Připravíme celý blok dat, který se uloží do obrázku.
        // Ten už obsahuje:
        // - hlavičku MAGIC
        // - escapovanou zprávu
        // - ukončovací znak END
        byte[] payload = pripravDataProUlozeni(text);

        int sirka = puvodni.getWidth();
        int vyska = puvodni.getHeight();

        // Kapacita obrázku v bitech.
        // Každý pixel nese 3 bity (R, G, B).
        int kapacitaVBitech = sirka * vyska * 3;

        // Počet bitů, které potřebujeme uložit.
        int potrebneBity = payload.length * 8;

        if (potrebneBity > kapacitaVBitech) {
            throw new Exception(
                    "Zpráva je příliš dlouhá.\n"
                            + "Potřebuje " + potrebneBity + " bitů,\n"
                            + "ale obrázek má kapacitu jen " + kapacitaVBitech + " bitů."
            );
        }

        // Vytvoříme kopii obrázku, do které budeme zapisovat.
        BufferedImage novy = new BufferedImage(sirka, vyska, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = novy.createGraphics();
        g.drawImage(puvodni, 0, 0, null);
        g.dispose();

        int indexBitu = 0;

        // Postupně procházíme pixely a ukládáme do nich bity.
        for (int y = 0; y < vyska; y++) {
            for (int x = 0; x < sirka; x++) {
                int argb = novy.getRGB(x, y);

                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int gCol = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                if (indexBitu < potrebneBity) {
                    r = nastavPosledniBit(r, ziskejBit(payload, indexBitu));
                    indexBitu++;
                }

                if (indexBitu < potrebneBity) {
                    gCol = nastavPosledniBit(gCol, ziskejBit(payload, indexBitu));
                    indexBitu++;
                }

                if (indexBitu < potrebneBity) {
                    b = nastavPosledniBit(b, ziskejBit(payload, indexBitu));
                    indexBitu++;
                }

                int novyArgb = (a << 24) | (r << 16) | (gCol << 8) | b;
                novy.setRGB(x, y, novyArgb);
            }
        }

        return novy;
    }

    // =========================================================
    // DEKÓDOVÁNÍ
    // =========================================================

    private void dekodujTextZObrazku() {
        if (obrazek == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Nejdříve načti obrázek.",
                    "Upozornění",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            String text = dekoduj(obrazek);
            vystupText.setText(text);
            stavovyRadek.setText("Text byl z obrázku vyčten.");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Chyba",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Dekóduje text z obrázku.
     *
     * Postup:
     * 1) Z nejnižších bitů RGB složek složíme bajty.
     * 2) Ověříme, že začátek dat je MAGIC.
     * 3) Pak čteme payload:
     *    - pokud narazíme na ESC, další bajt bereme doslova
     *    - pokud narazíme na END, zpráva končí
     *    - jinak jde o běžný datový bajt
     */
    private String dekoduj(BufferedImage img) throws Exception {
        ByteArrayOutputStream vsechnaData = new ByteArrayOutputStream();

        int sirka = img.getWidth();
        int vyska = img.getHeight();

        int bitCounter = 0;
        int currentByte = 0;

        // -----------------------------------------------------
        // 1) SLOŽENÍ BAJTŮ Z LSB BITŮ RGB
        // -----------------------------------------------------
        for (int y = 0; y < vyska; y++) {
            for (int x = 0; x < sirka; x++) {
                int argb = img.getRGB(x, y);

                int r = (argb >> 16) & 0xFF;
                int gCol = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                int[] slozky = {r, gCol, b};

                for (int slozka : slozky) {
                    int bit = ziskejPosledniBit(slozka);

                    currentByte = (currentByte << 1) | bit;
                    bitCounter++;

                    if (bitCounter == 8) {
                        vsechnaData.write(currentByte);
                        bitCounter = 0;
                        currentByte = 0;
                    }
                }
            }
        }

        byte[] data = vsechnaData.toByteArray();

        // -----------------------------------------------------
        // 2) OVĚŘENÍ HLAVIČKY MAGIC
        // -----------------------------------------------------
        if (data.length < MAGIC.length) {
            throw new Exception("Obrázek neobsahuje zprávu vytvořenou tímto programem.");
        }

        for (int i = 0; i < MAGIC.length; i++) {
            if (data[i] != MAGIC[i]) {
                throw new Exception("Obrázek neobsahuje zprávu vytvořenou tímto programem.");
            }
        }

        // -----------------------------------------------------
        // 3) ČTENÍ PAYLOADU AŽ DO END
        // -----------------------------------------------------
        ByteArrayOutputStream payload = new ByteArrayOutputStream();

        // Začneme hned za hlavičkou
        int i = MAGIC.length;

        while (i < data.length) {
            int value = data[i] & 0xFF;

            if (value == ESC) {
                // ESC znamená:
                // další bajt je doslovná hodnota, nikoli řídicí znak
                i++;

                if (i >= data.length) {
                    throw new Exception("Data jsou poškozená: neúplná escape sekvence.");
                }

                payload.write(data[i]);

            } else if (value == END) {
                // END = skutečný konec zprávy
                return new String(payload.toByteArray(), StandardCharsets.UTF_8);

            } else {
                // běžný datový bajt
                payload.write(value);
            }

            i++;
        }

        throw new Exception("Byla nalezena hlavička, ale ne konec zprávy.");
    }

    // =========================================================
    // PŘÍPRAVA DAT PRO ULOŽENÍ
    // =========================================================

    /**
     * Sestaví celý datový blok, který budeme zapisovat do obrázku.
     *
     * Přesně zde se:
     * - přidá HLAVIČKA
     * - přidá ESCAPOVANÁ zpráva
     * - přidá UKONČOVACÍ značka
     *
     * Výsledný tvar:
     * [MAGIC][ESCAPOVANÁ_DATA][END]
     */
    private byte[] pripravDataProUlozeni(String text) throws Exception {
        // 1) Text převedeme na UTF-8 bajty
        byte[] data = text.getBytes(StandardCharsets.UTF_8);

        // 2) Data escapujeme, aby se speciální bajty
        //    nepletly s ESC a END
        byte[] escaped = escapujData(data);

        // 3) Sestavíme výsledný blok dat
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Přidání pevné hlavičky
        out.write(MAGIC);

        // Přidání samotné zprávy
        out.write(escaped);

        // Přidání ukončovací značky
        out.write(END);

        return out.toByteArray();
    }

    /**
     * Escapuje data.
     *
     * Pokud se uvnitř textu vyskytne:
     * - ESC, uložíme ESC ESC
     * - END, uložíme ESC END
     *
     * Díky tomu pak dekodér pozná, že:
     * - nejde o řídicí znak,
     * - ale o skutečnou hodnotu uvnitř dat.
     */
    private byte[] escapujData(byte[] data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        for (byte b : data) {
            int value = b & 0xFF;

            if (value == ESC) {
                out.write(ESC);
                out.write(ESC);
            } else if (value == END) {
                out.write(ESC);
                out.write(END);
            } else {
                out.write(value);
            }
        }

        return out.toByteArray();
    }

    // =========================================================
    // BITOVÉ POMOCNÉ METODY
    // =========================================================

    /**
     * Nastaví poslední bit čísla na 0 nebo 1.
     *
     * Například:
     * 200 = 11001000
     * po uložení bitu 1:
     * 201 = 11001001
     */
    private int nastavPosledniBit(int value, int bit) {
        return (value & 0b11111110) | bit;
    }

    /**
     * Vrátí nejnižší bit čísla.
     */
    private int ziskejPosledniBit(int value) {
        return value & 1;
    }

    /**
     * Vrátí konkrétní bit z pole bajtů.
     *
     * Například:
     * bitIndex 0 = první bit prvního bajtu
     * bitIndex 8 = první bit druhého bajtu
     */
    private int ziskejBit(byte[] data, int bitIndex) {
        int byteIndex = bitIndex / 8;
        int poziceVBajtu = 7 - (bitIndex % 8);
        return (data[byteIndex] >> poziceVBajtu) & 1;
    }

    // =========================================================
    // MAIN
    // =========================================================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SteganografieSwing okno = new SteganografieSwing();
            okno.setVisible(true);
        });
    }
}