import java.util.ArrayList;
import java.util.List;

public class UlozenaHra {
    String nazevObchodu;
    int obtiznost;
    int pocetPenez;
    int xp;
    String vylepseni;
    List<Integer> odemceneZbozi;
    public UlozenaHra(String nazevObchodu, int obtiznost, int pocetPenez, int xp,  String vylepseni,  List<Integer> odemceneZbozi) {
        this.nazevObchodu = nazevObchodu;
        this.obtiznost = obtiznost;
        this.pocetPenez = pocetPenez;
        this.xp = xp;
        this.vylepseni = vylepseni;
        if(odemceneZbozi != null){
            this.odemceneZbozi = odemceneZbozi;
        } else {
            this.odemceneZbozi = new ArrayList<>();
            this.odemceneZbozi.add(16);
        }
    }
}
