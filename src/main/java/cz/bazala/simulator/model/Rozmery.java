package cz.bazala.simulator.model;

/**
 * Sdílené referenční rozměry hry. Veškeré pevné souřadnice v UI jsou vymyšlené
 * pro toto referenční rozlišení a za běhu se škálují poměrem vůči aktuální
 * velikosti okna. Dřív byla čísla 1280 a 720 roztroušená po více třídách, což
 * vedlo k nekonzistencím při změně rozlišení - proto jsou teď na jednom místě.
 */
public final class Rozmery {

    private Rozmery() {
        // Konstantová třída, nevytvářet instance.
    }

    /** Referenční šířka okna, pro kterou jsou souřadnice navržené. */
    public static final int REF_SIRKA = 1280;

    /** Referenční výška okna, pro kterou jsou souřadnice navržené. */
    public static final int REF_VYSKA = 720;

    /** Referenční výška pásu (prostor, ve kterém se pohybují položky). */
    public static final int REF_VYSKA_PASU = 190;

    /** Horizontální škálovací faktor pro danou aktuální šířku. */
    public static double scaleX(int aktualniSirka) {
        return (aktualniSirka <= 0) ? 1.0 : (double) aktualniSirka / REF_SIRKA;
    }

    /** Vertikální škálovací faktor pro danou aktuální výšku. */
    public static double scaleY(int aktualniVyska) {
        return (aktualniVyska <= 0) ? 1.0 : (double) aktualniVyska / REF_VYSKA;
    }
}
