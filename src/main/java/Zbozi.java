public class Zbozi {
    String nazev;
    int id;
    int typ ;
    int cena;
    int minVaha;
    int maxVaha;
    int sance;
    int maxPocet;
    int xp;
    int lvlOdemknuti;
    public Zbozi(String nazev, int id, int typ, int cena, int minVaha, int maxVaha, int sance, int maxPocet, int xp, int lvlOdemknuti){
        //typ ostatni=0 pecivo=1 ovoce=2 zelenina=3
        this.nazev = nazev;
        this.id = id;
        this.typ = typ;
        this.cena = cena;
        this.minVaha = minVaha;
        this.maxVaha = maxVaha;
        this.sance = sance;
        this.maxPocet = maxPocet;
        this.xp = xp;
        this.lvlOdemknuti = lvlOdemknuti;
    }
}
