package cz.bazala.simulator.io;

import cz.bazala.simulator.model.UlozenaHra;
import cz.bazala.simulator.model.Zbozi;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SpravceSouboru {

    // Stavové texty, které očekává NacistHruMenu pro zakázání tlačítek.
    public static final String STAV_PRAZDNY = "Prázdná pozice";
    public static final String STAV_CHYBA = "Chyba při čtení";

    // Složka pro uložené hry - vedle spuštěné aplikace, aby ukládání fungovalo
    // i po zabalení do JARu (do src/main/resources se zapisovat nedá).
    private static final File SLOZKA_ULOZENYCH_HER = new File("ulozeneHry");

    // Původní umístění uložených her - používá se jen pro čtení kvůli
    // zpětné kompatibilitě se staršími uloženými hrami.
    private static final String STARA_SLOZKA = "src/main/resources/ulozeneHry/";

    /** Maximální délka názvu obchodu. */
    public static final int MAX_DELKA_JMENA = 30;

    /**
     * Očistí název obchodu, aby neporušil formát uloženého souboru.
     * Odstraní znaky, které kolidují se syntaxí (uvozovky, středníky,
     * dvojtečky, konce řádků), ořízne mezery a omezí délku.
     */
    public static String ocistiJmeno(String jmeno) {
        if (jmeno == null) return "Obchod";
        String vysledek = jmeno.replaceAll("[\";:\\r\\n]", "").trim();
        if (vysledek.length() > MAX_DELKA_JMENA) {
            vysledek = vysledek.substring(0, MAX_DELKA_JMENA).trim();
        }
        return vysledek.isEmpty() ? "Obchod" : vysledek;
    }

    private static File souborProSlot(int slot) {
        return new File(SLOZKA_ULOZENYCH_HER, "ulozenaHra" + slot + ".txt");
    }

    /**
     * Načte uloženou hru pro daný slot. Nejdřív zkusí nové umístění,
     * pak starší umístění ve zdrojových resources.
     */
    public static UlozenaHra nactiUlozenouHru(int slot) {
        File novy = souborProSlot(slot);
        if (novy.exists() && novy.length() > 0) {
            return nactiUlozeneHryZeSouboru(novy.getPath());
        }
        return nactiUlozeneHryZeSouboru(STARA_SLOZKA + "ulozenaHra" + slot + ".txt");
    }

    public static UlozenaHra nactiUlozeneHryZeSouboru(String cesta) {
        File soubor = new File(cesta);
        if (!soubor.exists() || soubor.length() == 0) {
            return new UlozenaHra(STAV_PRAZDNY, 0, 0, "00000", null, "obchod_theme", new ArrayList<>(List.of("obchod_theme")));
        }

        String jmenoObchodu = "Neznámé";
        int penize = 0;
        int xp = 0;
        String vylepseni = "00000";
        ArrayList<Integer> odemceneZbozi = new ArrayList<>();
        String vybranaHudba = "obchod_theme";
        List<String> odemcenaHudba = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(soubor), StandardCharsets.UTF_8))) {
            String radek;
            while ((radek = br.readLine()) != null) {
                if (radek.trim().isEmpty()) continue;
                if (radek.contains(":")) {
                    String hodnota = radek.substring(radek.indexOf(":") + 1).replace(";", "").trim();

                    if (hodnota.startsWith("\"") && hodnota.endsWith("\"")) {
                        hodnota = hodnota.substring(1, hodnota.length() - 1);
                    }

                    // Jeden vadný řádek nesmí zahodit celou uloženou hru - proto se
                    // číselné položky parsují tolerantně a vadný řádek se jen přeskočí.
                    try {
                        if (radek.startsWith("Jmeno")) {
                            jmenoObchodu = hodnota;
                        } else if (radek.startsWith("Penize")) {
                            penize = Integer.parseInt(hodnota);
                        } else if (radek.startsWith("XP")) {
                            xp = Integer.parseInt(hodnota);
                        } else if (radek.startsWith("Vylepseni")) {
                            vylepseni = hodnota;
                        } else if (radek.startsWith("OdemceneZbozi")) {
                            String[] ids = hodnota.split(",");
                            for (String id : ids) {
                                if (id.trim().isEmpty()) continue;
                                try {
                                    odemceneZbozi.add(Integer.parseInt(id.trim()));
                                } catch (NumberFormatException e) {
                                    System.err.println("Přeskakuji neplatné ID zboží v uložené hře (" + cesta + "): " + id.trim());
                                }
                            }
                        } else if (radek.startsWith("VybranaHudba")) {
                            vybranaHudba = hodnota;
                        } else if (radek.startsWith("OdemcenaHudba")) {
                            String[] hudby = hodnota.split(",");
                            for (String h : hudby) {
                                if (!h.trim().isEmpty()) odemcenaHudba.add(h.trim());
                            }
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Přeskakuji poškozený řádek v uložené hře (" + cesta + "): " + radek);
                    }
                }
            }
            return new UlozenaHra(jmenoObchodu, penize, xp, vylepseni, odemceneZbozi, vybranaHudba, odemcenaHudba);
        } catch (IOException e) {
            System.err.println("Chyba při čtení uložené hry (" + cesta + "): " + e.getMessage());
            return new UlozenaHra(STAV_CHYBA, 0, 0, "00000", null, "obchod_theme", null);
        }
    }

    public static void ulozHruDoSouboru(int slot, String jmeno, String penize, int xp, String vylepseni, List<Integer> odemceneZbozi, String vybranaHudba, List<String> odemcenaHudba) {
        if (odemceneZbozi == null) {
            odemceneZbozi = new ArrayList<>();
            odemceneZbozi.add(16);
        }
        if (!SLOZKA_ULOZENYCH_HER.exists() && !SLOZKA_ULOZENYCH_HER.mkdirs()) {
            System.err.println("Nepodařilo se vytvořit složku pro uložené hry: " + SLOZKA_ULOZENYCH_HER.getAbsolutePath());
            return;
        }
        File soubor = souborProSlot(slot);
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(soubor), StandardCharsets.UTF_8))) {
            writer.println("Jmeno: \"" + ocistiJmeno(jmeno) + "\";");
            writer.println("Penize: " + penize + ";");
            writer.println("XP: " + xp + ";");
            writer.println("Vylepseni: " + vylepseni + ";");

            StringBuilder odemceneZboziText = new StringBuilder();
            for (int i = 0; i < odemceneZbozi.size(); i++) {
                odemceneZboziText.append(odemceneZbozi.get(i));
                if (i != odemceneZbozi.size() - 1) odemceneZboziText.append(",");
            }
            writer.println("OdemceneZbozi: " + odemceneZboziText + ";");
            writer.println("VybranaHudba: " + vybranaHudba + ";");

            String odemcenaHudbaText = (odemcenaHudba == null) ? "" : String.join(",", odemcenaHudba);
            writer.println("OdemcenaHudba: " + odemcenaHudbaText + ";");

            System.out.println("Hra uložena do slotu: " + slot);
        } catch (IOException ex) {
            System.err.println("Chyba při ukládání hry do slotu " + slot + ": " + ex.getMessage());
        }
    }

    public static List<Zbozi> nactiZbozi() {
        // Standardní zdroj je classpath (/zbozi.txt) - funguje i ze zabaleného JARu.
        // Soubor v src/main/resources má přednost jen jako override pro vývoj,
        // aby šlo upravit zboží bez rebuildu.
        File diskovyOverride = new File("src/main/resources/zbozi.txt");
        if (diskovyOverride.exists() && diskovyOverride.length() > 0) {
            return nactiZboziZeStreamu(() -> new FileInputStream(diskovyOverride), diskovyOverride.getPath());
        }
        return nactiZboziZClasspath();
    }

    private static List<Zbozi> nactiZboziZClasspath() {
        InputStream is = SpravceSouboru.class.getResourceAsStream("/zbozi.txt");
        if (is == null) {
            throw new IllegalStateException("Nenačetlo se zboží: soubor zbozi.txt nebyl nalezen na disku ani v classpath.");
        }
        return nactiZboziZeStreamu(() -> is, "classpath:/zbozi.txt");
    }

    private interface DodavatelStreamu {
        InputStream otevri() throws IOException;
    }

    private static List<Zbozi> nactiZboziZeStreamu(DodavatelStreamu dodavatel, String popisZdroje) {
        List<Zbozi> zboziVsechny = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(dodavatel.otevri(), StandardCharsets.UTF_8))) {
            String radek;
            while ((radek = br.readLine()) != null) {
                if (radek.trim().isEmpty()) continue;
                if (radek.contains(";")) {
                    String[] slova = radek.split(";");
                    if (slova.length >= 9) {
                        Zbozi zbozi = new Zbozi(
                                slova[0].trim(),
                                Integer.parseInt(slova[1].trim()),
                                Integer.parseInt(slova[2].trim()),
                                Integer.parseInt(slova[3].trim()),
                                Integer.parseInt(slova[4].trim()),
                                Integer.parseInt(slova[5].trim()),
                                Integer.parseInt(slova[6].trim()),
                                Integer.parseInt(slova[7].trim()),
                                slova[8].trim()
                        );
                        zboziVsechny.add(zbozi);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Chyba při čtení zboží (" + popisZdroje + "): " + e.getMessage());
        }

        if (zboziVsechny.isEmpty()) {
            System.out.println("Seznam zboží je prázdný, žádné platné řádky nebyly nalezeny.");
        } else {
            System.out.println("Načteno " + zboziVsechny.size() + " položek zboží ze zdroje: " + popisZdroje);
        }

        return zboziVsechny;
    }
}
