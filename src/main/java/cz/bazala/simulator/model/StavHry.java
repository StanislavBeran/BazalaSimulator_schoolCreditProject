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
    private static final int MAX_UROVEN_RYCHLOSTI = 4;
    private static final int VYCHOZI_ODEMCENE_ID = 16;

    private int penize = 0;
    private int celkoveXp = 0;
    private String vylepseni = "00000";
    private List<Integer> odemceneZbozi = new ArrayList<>(List.of(VYCHOZI_ODEMCENE_ID));
    private String jmenoObchodu = "Můj Obchod";
    private int aktualniSlot = 1;
    private String vybranaHudba = "obchod_theme";
    private List<String> odemcenaHudba = new ArrayList<>(List.of("obchod_theme"));
    private boolean hraJeNactena = false;

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

    // --- Vylepšení (rychlost pásu je první znak řetězce vylepšení) ---
    public String getVylepseni() {
        return vylepseni;
    }

    public void setVylepseni(String vylepseni) {
        this.vylepseni = vylepseni;
    }

    public int getUrovenRychlostiPasu() {
        if (vylepseni == null || vylepseni.isEmpty()) return 0;
        return Character.getNumericValue(vylepseni.charAt(0));
    }

    /**
     * Zvýší úroveň rychlosti pásu o 1, pokud ještě není na maximu.
     * @return novou úroveň, nebo -1 pokud už byla na maximu (beze změny).
     */
    public int zvysUrovenRychlostiPasu() {
        int current = getUrovenRychlostiPasu();
        if (current >= MAX_UROVEN_RYCHLOSTI) return -1;
        current++;
        StringBuilder sb = new StringBuilder(vylepseni);
        sb.setCharAt(0, Character.forDigit(current, 10));
        vylepseni = sb.toString();
        return current;
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
}
