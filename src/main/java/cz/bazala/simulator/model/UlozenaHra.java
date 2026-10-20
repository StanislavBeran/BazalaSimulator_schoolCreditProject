package cz.bazala.simulator.model;

import java.util.ArrayList;
import java.util.List;

public class UlozenaHra {
    public String nazevObchodu;
    public int pocetPenez;
    public int xp;
    public String vylepseni;
    public List<Integer> odemceneZbozi;
    public String vybranaHudba;
    public List<String> odemcenaHudba;

    public UlozenaHra(String nazevObchodu, int pocetPenez, int xp,  String vylepseni,  List<Integer> odemceneZbozi, String vybranaHudba, List<String> odemcenaHudba) {
        this.nazevObchodu = nazevObchodu;
        this.pocetPenez = pocetPenez;
        this.xp = xp;
        this.vylepseni = vylepseni;
        if(odemceneZbozi != null){
            this.odemceneZbozi = odemceneZbozi;
        } else {
            this.odemceneZbozi = new ArrayList<>();
            this.odemceneZbozi.add(16);
        }
        this.vybranaHudba = (vybranaHudba != null && !vybranaHudba.isEmpty()) ? vybranaHudba : "obchod_theme";

        if (odemcenaHudba != null && !odemcenaHudba.isEmpty()) {
            this.odemcenaHudba = odemcenaHudba;
        } else {
            this.odemcenaHudba = new ArrayList<>();
            this.odemcenaHudba.add("obchod_theme");
        }
    }
}
