package cz.bazala.simulator.io;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Spravuje nastavení zobrazení (rozlišení / fullscreen) a jeho perzistenci.
 * Nastavení se ukládá do samostatného souboru nastaveni.cfg vedle aplikace,
 * aby se nemíchalo s uloženými hrami.
 */
public class SpravceNastaveni {

    /** Dostupné režimy zobrazení - tři 16:9 rozlišení a fullscreen. */
    public enum Zobrazeni {
        R_1280_720("1280 x 720", 1280, 720, false),
        R_1600_900("1600 x 900", 1600, 900, false),
        R_1920_1080("1920 x 1080", 1920, 1080, false),
        FULLSCREEN("Fullscreen", 0, 0, true);

        public final String popis;
        public final int sirka;
        public final int vyska;
        public final boolean fullscreen;

        Zobrazeni(String popis, int sirka, int vyska, boolean fullscreen) {
            this.popis = popis;
            this.sirka = sirka;
            this.vyska = vyska;
            this.fullscreen = fullscreen;
        }

        /** Najde režim podle uloženého názvu, s bezpečným výchozím fallbackem. */
        public static Zobrazeni zNazvu(String nazev) {
            if (nazev != null) {
                for (Zobrazeni z : values()) {
                    if (z.name().equals(nazev)) return z;
                }
            }
            return R_1280_720;
        }
    }

    private static final File SOUBOR = new File("nastaveni.cfg");
    private static final String KLIC_ZOBRAZENI = "Zobrazeni";

    private static Zobrazeni aktualniZobrazeni = null;

    /** Vrátí aktuální režim zobrazení, při prvním volání ho načte ze souboru. */
    public static Zobrazeni getZobrazeni() {
        if (aktualniZobrazeni == null) {
            aktualniZobrazeni = nactiZobrazeni();
        }
        return aktualniZobrazeni;
    }

    /** Nastaví a uloží nový režim zobrazení. */
    public static void setZobrazeni(Zobrazeni zobrazeni) {
        aktualniZobrazeni = zobrazeni;
        uloz();
    }

    private static Zobrazeni nactiZobrazeni() {
        if (!SOUBOR.exists() || SOUBOR.length() == 0) {
            return Zobrazeni.R_1280_720;
        }
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(SOUBOR), StandardCharsets.UTF_8))) {
            String radek;
            while ((radek = br.readLine()) != null) {
                if (radek.startsWith(KLIC_ZOBRAZENI) && radek.contains(":")) {
                    String hodnota = radek.substring(radek.indexOf(":") + 1).replace(";", "").trim();
                    return Zobrazeni.zNazvu(hodnota);
                }
            }
        } catch (IOException e) {
            System.err.println("Chyba při čtení nastavení: " + e.getMessage());
        }
        return Zobrazeni.R_1280_720;
    }

    private static void uloz() {
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(SOUBOR), StandardCharsets.UTF_8))) {
            writer.println(KLIC_ZOBRAZENI + ": " + aktualniZobrazeni.name() + ";");
        } catch (IOException e) {
            System.err.println("Chyba při ukládání nastavení: " + e.getMessage());
        }
    }
}
