public class Zbozi {
    String nazev;
    int id;
    int typ ;
    int cena;
    int minVaha;
    int maxVaha;
    int maxPocet;
    int lvlOdemknuti;
    String zkracenyNazev;
    public Zbozi(String nazev, int id, int typ, int cena, int minVaha, int maxVaha, int maxPocet, int lvlOdemknuti, String zkracenyNazev) {
        //typ pecivo=1 zelenina=2 ovoce=3 ostatni=4
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
}
