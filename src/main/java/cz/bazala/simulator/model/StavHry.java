package cz.bazala.simulator.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Drží herní stav odděleně od UI: peníze, XP, vylepšení, odemčené zboží,
 * detaily uložené hry a hudbu. Obsahuje jen čistou stavovou logiku bez
 * vazby na Swing nebo vedlejší efekty (zvuk, konzole).
 */
public class StavHry {

    /** Výsledek pokusu o koupi hudby - UI podle něj zvolí zpětnou vazbu. */
    public enum VysledekKoupeHudby {
        KOUPENO,
        JIZ_VLASTNENO,
        NEDOSTATEK_PENEZ
    }

    private static final int XP_NA_LEVEL = 100;
    private static final int VYCHOZI_ODEMCENE_ID = 16;

    /**
     * Druhy vylepšení. Každé vylepšení odpovídá jedné pozici v řetězci
     * {@code vylepseni} (index = pozice). Řetězec má délku {@link #POCET_VYLEPSENI}.
     */
    public enum Vylepseni {
        RYCHLOST_PASU(0, 4),      // zrychlí pás a úklid (stávající chování)
        BONUS_ODMEN(1, 4),        // vyšší peníze i XP za nákup
        TRPELIVOST(2, 4),         // zákazníci vydrží déle
        DRUHY_SCANNER(3, 1),      // povolí druhou položku na scanneru (zap/vyp)
        AUTO_SKEN_KUSOVE(4, 1);   // kusové zboží se odstaví samo (zap/vyp)

        public final int pozice;
        public final int maxUroven;

        Vylepseni(int pozice, int maxUroven) {
            this.pozice = pozice;
            this.maxUroven = maxUroven;
        }
    }

    /** Délka řetězce vylepšení = počet různých vylepšení. */
    public static final int POCET_VYLEPSENI = 5;
    private static final String VYCHOZI_VYLEPSENI = "00000";

    private int penize = 0;
    private int celkoveXp = 0;
    private String vylepseni = VYCHOZI_VYLEPSENI;
    private List<Integer> odemceneZbozi = new ArrayList<>(List.of(VYCHOZI_ODEMCENE_ID));
    private String jmenoObchodu = "Můj Obchod";
    private int aktualniSlot = 1;
    private String vybranaHudba = "obchod_theme";
    private List<String> odemcenaHudba = new ArrayList<>(List.of("obchod_theme"));
    private boolean hraJeNactena = false;

    // --- Kombo (série bezchybně obsloužených zákazníků) ---
    private int komboSerie = 0;

    // --- Statistiky / rekordy (perzistentní napříč směnami) ---
    private int celkemObslouzeno = 0;       // kolik zákazníků celkem odbaveno
    private int nejlepsiKombo = 0;          // nejdelší série bez chyby
    private int nejlepsiTrzbaSmeny = 0;     // největší tržba za jednu směnu
    private int celkemVydelano = 0;         // kumulativní tržba za celou hru

    // --- Denní cíl / směna ---
    private int cilSmeny = 0;               // cílová tržba aktuální směny
    private int trzbaSmeny = 0;             // dosažená tržba aktuální směny
    private int cisloSmeny = 1;             // pořadí aktuální směny

    // --- Peníze ---
    public int getPenize() {
        return penize;
    }

    public void setPenize(int penize) {
        this.penize = penize;
    }

    public void pridejPenize(int castka) {
        this.penize += castka;
    }

    public void odectiPenize(int castka) {
        this.penize -= castka;
    }

    // --- XP / level ---
    public int getCelkoveXp() {
        return celkoveXp;
    }

    public void setCelkoveXp(int celkoveXp) {
        this.celkoveXp = celkoveXp;
    }

    public int getLevel() {
        return (celkoveXp / XP_NA_LEVEL) + 1;
    }

    public int getXpDoDalsihoLevelu() {
        return celkoveXp % XP_NA_LEVEL;
    }

    public int getMaxXpNaLevel() {
        return XP_NA_LEVEL;
    }

    // --- Vylepšení (každá pozice řetězce = jedno vylepšení dle enumu Vylepseni) ---
    public String getVylepseni() {
        return vylepseni;
    }

    public void setVylepseni(String vylepseni) {
        this.vylepseni = normalizujVylepseni(vylepseni);
    }

    /** Zajistí, že řetězec vylepšení má správnou délku a jen číslice. */
    private static String normalizujVylepseni(String vstup) {
        StringBuilder sb = new StringBuilder(VYCHOZI_VYLEPSENI);
        if (vstup != null) {
            for (int i = 0; i < POCET_VYLEPSENI && i < vstup.length(); i++) {
                char c = vstup.charAt(i);
                if (Character.isDigit(c)) sb.setCharAt(i, c);
            }
        }
        return sb.toString();
    }

    /** Vrátí aktuální úroveň daného vylepšení (0 = nekoupeno). */
    public int getUrovenVylepseni(Vylepseni v) {
        if (vylepseni == null || v.pozice >= vylepseni.length()) return 0;
        int uroven = Character.getNumericValue(vylepseni.charAt(v.pozice));
        return (uroven < 0) ? 0 : Math.min(uroven, v.maxUroven);
    }

    /**
     * Zvýší úroveň daného vylepšení o 1, pokud ještě není na maximu.
     * @return novou úroveň, nebo -1 pokud už byla na maximu (beze změny).
     */
    public int zvysUrovenVylepseni(Vylepseni v) {
        int current = getUrovenVylepseni(v);
        if (current >= v.maxUroven) return -1;
        current++;
        StringBuilder sb = new StringBuilder(normalizujVylepseni(vylepseni));
        sb.setCharAt(v.pozice, Character.forDigit(current, 10));
        vylepseni = sb.toString();
        return current;
    }

    // --- Pohodlné zkratky nad vylepšeními ---

    public int getUrovenRychlostiPasu() {
        return getUrovenVylepseni(Vylepseni.RYCHLOST_PASU);
    }

    /** @return novou úroveň rychlosti, nebo -1 když už byla na maximu. */
    public int zvysUrovenRychlostiPasu() {
        return zvysUrovenVylepseni(Vylepseni.RYCHLOST_PASU);
    }

    /** Násobič odměn (peníze i XP) daný úrovní vylepšení BONUS_ODMEN: 1.0, 1.15, ... */
    public double getNasobicOdmen() {
        return 1.0 + 0.15 * getUrovenVylepseni(Vylepseni.BONUS_ODMEN);
    }

    /** Násobič trpělivosti zákazníka daný vylepšením TRPELIVOST: 1.0, 1.3, ... */
    public double getNasobicTrpelivosti() {
        return 1.0 + 0.3 * getUrovenVylepseni(Vylepseni.TRPELIVOST);
    }

    /** True, pokud je aktivní druhý scanner. */
    public boolean maDruhyScanner() {
        return getUrovenVylepseni(Vylepseni.DRUHY_SCANNER) >= 1;
    }

    /** True, pokud se kusové zboží odstavuje automaticky. */
    public boolean maAutoSkenKusove() {
        return getUrovenVylepseni(Vylepseni.AUTO_SKEN_KUSOVE) >= 1;
    }

    // --- Odemčené zboží ---
    public List<Integer> getOdemceneZbozi() {
        return odemceneZbozi;
    }

    public void setOdemceneZbozi(List<Integer> odemceneZbozi) {
        if (odemceneZbozi == null) {
            this.odemceneZbozi = new ArrayList<>(List.of(VYCHOZI_ODEMCENE_ID));
        } else {
            this.odemceneZbozi = odemceneZbozi;
        }
    }

    /**
     * Odemkne zboží daného ID.
     * @return true pokud bylo nově přidáno, false pokud už bylo odemčené.
     */
    public boolean odemkniZbozi(int id) {
        if (odemceneZbozi.contains(id)) return false;
        odemceneZbozi.add(id);
        return true;
    }

    // --- Detaily hry ---
    public String getJmenoObchodu() {
        return jmenoObchodu;
    }

    public void setJmenoObchodu(String jmenoObchodu) {
        this.jmenoObchodu = jmenoObchodu;
    }

    public int getAktualniSlot() {
        return aktualniSlot;
    }

    public void setAktualniSlot(int aktualniSlot) {
        this.aktualniSlot = aktualniSlot;
    }

    public boolean jeHraNactena() {
        return hraJeNactena;
    }

    public void setHraNactena(boolean hraJeNactena) {
        this.hraJeNactena = hraJeNactena;
    }

    // --- Hudba ---
    public String getVybranaHudba() {
        return vybranaHudba;
    }

    public void setVybranaHudba(String vybranaHudba) {
        this.vybranaHudba = (vybranaHudba != null && !vybranaHudba.isEmpty()) ? vybranaHudba : "obchod_theme";
    }

    public List<String> getOdemcenaHudba() {
        return odemcenaHudba;
    }

    public void setOdemcenaHudba(List<String> odemcenaHudba) {
        if (odemcenaHudba != null && !odemcenaHudba.isEmpty()) {
            this.odemcenaHudba = odemcenaHudba;
        } else {
            this.odemcenaHudba = new ArrayList<>(List.of("obchod_theme"));
        }
    }

    /**
     * Pokusí se koupit hudbu - čistě stavová operace (žádný zvuk/konzole).
     * Při úspěchu strhne cenu a přidá hudbu mezi odemčené.
     */
    public VysledekKoupeHudby zkusKoupitHudbu(String souborHudby, int cena) {
        if (odemcenaHudba.contains(souborHudby)) {
            return VysledekKoupeHudby.JIZ_VLASTNENO;
        }
        if (penize < cena) {
            return VysledekKoupeHudby.NEDOSTATEK_PENEZ;
        }
        penize -= cena;
        odemcenaHudba.add(souborHudby);
        return VysledekKoupeHudby.KOUPENO;
    }

    // ==========================================
    // Kombo (série bezchybně obsloužených zákazníků)
    // ==========================================
    public int getKomboSerie() {
        return komboSerie;
    }

    public void setKomboSerie(int komboSerie) {
        this.komboSerie = Math.max(0, komboSerie);
        if (this.komboSerie > nejlepsiKombo) nejlepsiKombo = this.komboSerie;
    }

    /** Zvýší kombo o 1 (bezchybný zákazník) a aktualizuje rekord. */
    public void zvysKombo() {
        komboSerie++;
        if (komboSerie > nejlepsiKombo) nejlepsiKombo = komboSerie;
    }

    /** Vynuluje kombo (zákazník obsloužen s chybou / resetován). */
    public void vynulujKombo() {
        komboSerie = 0;
    }

    /**
     * Násobič odměny z komba. Roste po 10 % za každého bezchybného zákazníka
     * v sérii, zastropovaný na 2.0 (tj. +100 %). Při komboSerie 0 nebo 1 vrací 1.0.
     */
    public double getNasobicKomba() {
        if (komboSerie <= 1) return 1.0;
        double nasobic = 1.0 + 0.10 * (komboSerie - 1);
        return Math.min(nasobic, 2.0);
    }

    // ==========================================
    // Statistiky / rekordy
    // ==========================================
    public int getCelkemObslouzeno() { return celkemObslouzeno; }
    public void setCelkemObslouzeno(int v) { this.celkemObslouzeno = Math.max(0, v); }

    public int getNejlepsiKombo() { return nejlepsiKombo; }
    public void setNejlepsiKombo(int v) { this.nejlepsiKombo = Math.max(0, v); }

    public int getNejlepsiTrzbaSmeny() { return nejlepsiTrzbaSmeny; }
    public void setNejlepsiTrzbaSmeny(int v) { this.nejlepsiTrzbaSmeny = Math.max(0, v); }

    public int getCelkemVydelano() { return celkemVydelano; }
    public void setCelkemVydelano(int v) { this.celkemVydelano = Math.max(0, v); }

    // ==========================================
    // Denní cíl / směna
    // ==========================================
    public int getCilSmeny() { return cilSmeny; }
    public void setCilSmeny(int v) { this.cilSmeny = Math.max(0, v); }

    public int getTrzbaSmeny() { return trzbaSmeny; }
    public void setTrzbaSmeny(int v) { this.trzbaSmeny = Math.max(0, v); }

    public int getCisloSmeny() { return cisloSmeny; }
    public void setCisloSmeny(int v) { this.cisloSmeny = Math.max(1, v); }

    /**
     * Spustí novou směnu. Cíl poroste s číslem směny a úrovní hráče, aby
     * výzva rostla, jak hráč sílí.
     */
    public void zahajNovouSmenu() {
        cilSmeny = vypoctiCilSmeny(cisloSmeny, getLevel());
        trzbaSmeny = 0;
    }

    /** Výpočet cílové tržby pro směnu - základ roste s číslem směny i levelem. */
    public static int vypoctiCilSmeny(int cisloSmeny, int level) {
        int zaklad = 800 + (cisloSmeny - 1) * 400;
        zaklad += (level - 1) * 150;
        return Math.max(500, zaklad);
    }

    /** True, pokud už byl cíl aktuální směny splněn. */
    public boolean jeCilSmenySplnen() {
        return cilSmeny > 0 && trzbaSmeny >= cilSmeny;
    }

    /**
     * Zaeviduje dokončený (zaplacený) nákup: zvedne tržbu směny, počet
     * obsloužených a kumulativní výdělek, aktualizuje rekord tržby za směnu.
     * @return true, pokud tímto nákupem došlo ke splnění cíle směny.
     */
    public boolean zaevidujNakup(int castka) {
        boolean cilBylSplnen = jeCilSmenySplnen();
        trzbaSmeny += Math.max(0, castka);
        celkemVydelano += Math.max(0, castka);
        celkemObslouzeno++;
        if (trzbaSmeny > nejlepsiTrzbaSmeny) nejlepsiTrzbaSmeny = trzbaSmeny;
        return !cilBylSplnen && jeCilSmenySplnen();
    }
}
