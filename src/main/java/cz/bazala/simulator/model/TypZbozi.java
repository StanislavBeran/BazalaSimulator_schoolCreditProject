package cz.bazala.simulator.model;

/**
 * Typ (kategorie) zboží. Číselný kód odpovídá hodnotě uložené v zbozi.txt
 * a zároveň ID kategorie používanému ve filtru pokladní obrazovky.
 *
 * Kódy: 0 = kusové (jde rovnou do odstavného místa, neskenuje se),
 * 1 = pečivo, 2 = zelenina, 3 = ovoce, 4 = ostatní (vše se skenuje přes scanner).
 */
public enum TypZbozi {
    KUSOVE(0, "Má kód"),
    PECIVO(1, "Pečivo"),
    ZELENINA(2, "Zelenina"),
    OVOCE(3, "Ovoce"),
    OSTATNI(4, "Ostatní");

    /** Hodnota kategorie ve filtru znamenající "všechny kategorie". */
    public static final int KATEGORIE_VSE = 5;

    private final int kod;
    private final String nazev;

    TypZbozi(int kod, String nazev) {
        this.kod = kod;
        this.nazev = nazev;
    }

    public int getKod() {
        return kod;
    }

    public String getNazev() {
        return nazev;
    }

    /** Najde typ podle číselného kódu, s bezpečným fallbackem na KUSOVE. */
    public static TypZbozi zKodu(int kod) {
        for (TypZbozi t : values()) {
            if (t.kod == kod) return t;
        }
        return KUSOVE;
    }

    /** True pro zboží, které se skenuje přes scanner (pečivo/zelenina/ovoce/ostatní). */
    public static boolean jeSkenovane(int kod) {
        return kod >= PECIVO.kod && kod <= OSTATNI.kod;
    }

    /** True pro kusové zboží, které se neskenuje. */
    public static boolean jeKusove(int kod) {
        return kod == KUSOVE.kod;
    }
}
