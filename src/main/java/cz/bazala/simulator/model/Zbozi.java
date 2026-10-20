package cz.bazala.simulator.model;

public class Zbozi {
    public String nazev;
    public int id;
    public int typ;
    public int cena;
    public int minVaha;
    public int maxVaha;
    public int maxPocet;
    public int lvlOdemknuti;
    public String zkracenyNazev;

    public Zbozi(String nazev, int id, int typ, int cena, int minVaha, int maxVaha, int maxPocet, int lvlOdemknuti, String zkracenyNazev) {
        // typ: viz enum TypZbozi (0 = kusové, 1 = pečivo, 2 = zelenina, 3 = ovoce, 4 = ostatní)
        this.nazev = nazev;
        this.id = id;
        this.typ = typ;
        this.cena = cena;
        this.minVaha = minVaha;
        this.maxVaha = maxVaha;
        this.maxPocet = maxPocet;
        this.lvlOdemknuti = lvlOdemknuti;
        this.zkracenyNazev = zkracenyNazev;
    }

    /** True, pokud je to kusové zboží (neskenuje se, jde rovnou do odstavného místa). */
    public boolean jeKusove() {
        return TypZbozi.jeKusove(typ);
    }

    /** True, pokud se zboží skenuje přes scanner. */
    public boolean jeSkenovane() {
        return TypZbozi.jeSkenovane(typ);
    }
}
