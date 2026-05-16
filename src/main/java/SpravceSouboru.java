import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SpravceSouboru {
    public static UlozenaHra nactiUlozeneHryZeSouboru(String cesta) {
        File soubor = new File(cesta);
        if (!soubor.exists() || soubor.length() == 0) {
            return new UlozenaHra("Prázdné", 0, 0, "00000", null, "obchod_theme", new ArrayList<>(List.of("obchod_theme")));
        }

        String jmenoObchodu = "Neznámé";
        int obtiznost = 0;
        int penize = 0;
        int xp = 0;
        String vylepseni = "00000";
        ArrayList<Integer> odemceneZbozi = new ArrayList<>();
        String vybranaHudba = "obchod_theme";
        List<String> odemcenaHudba = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(soubor))) {
            String radek;
            while ((radek = br.readLine()) != null) {
                if (radek.trim().isEmpty()) continue;
                if (radek.contains(":")) {
                    String hodnota = radek.substring(radek.indexOf(":") + 1).replace(";", "").trim();

                    if (hodnota.startsWith("\"") && hodnota.endsWith("\"")) {
                        hodnota = hodnota.substring(1, hodnota.length() - 1);
                    }

                    if (radek.startsWith("Jmeno")) {
                        jmenoObchodu = hodnota;
                    } else if (radek.startsWith("Penize")) {
                        penize = Integer.parseInt(hodnota);
                    } else if (radek.startsWith("XP")){
                        xp = Integer.parseInt(hodnota);
                    } else if (radek.startsWith("Vylepseni")) {
                        vylepseni = hodnota;
                    } else if (radek.startsWith("OdemceneZbozi")) {
                        String[] ids = hodnota.split(",");
                        for (String id : ids) {
                            if (!id.trim().isEmpty())odemceneZbozi.add(Integer.parseInt(id.trim()));
                        }
                    } else if (radek.startsWith("VybranaHudba")) {
                        vybranaHudba = hodnota;
                    } else if (radek.startsWith("OdemcenaHudba")) {
                        String[] hudby = hodnota.split(",");
                        for (String h : hudby) {
                            if (!h.trim().isEmpty()) odemcenaHudba.add(h.trim());
                        }
                    }
                }
            }
            return new UlozenaHra(jmenoObchodu, penize, xp, vylepseni, odemceneZbozi, vybranaHudba, odemcenaHudba);
        } catch (IOException e) {
            e.printStackTrace();
            return new UlozenaHra("ERROR", 0, 0, "00000", null, "obchod_theme", null);
        }
    }

    public static void ulozHruDoSouboru(int slot, String jmeno, String penize, int xp, String vylepseni, List<Integer> odemceneZbozi, String vybranaHudba, List<String> odemcenaHudba) {
        if (odemceneZbozi == null) {
            odemceneZbozi = new ArrayList<>();
            odemceneZbozi.add(16);
        }
        try {
            File slozka = new File("ulozeneHry");
            if (!slozka.exists()) {
                slozka.mkdir();
            }
            File soubor = new File("src/main/resources/ulozeneHry/ulozenaHra" + slot + ".txt");
            PrintWriter writer = new PrintWriter(new FileWriter(soubor));
            writer.println("Jmeno: \"" + jmeno + "\";");
            writer.println("Penize: " + penize + ";");
            writer.println("XP: " + xp + ";");
            writer.println("Vylepseni: " + vylepseni + ";");
            String odemceneZboziText = "";
            for(int i=0; i < odemceneZbozi.size(); i++) {
                odemceneZboziText += odemceneZbozi.get(i) + (i == odemceneZbozi.size()-1 ? "" : ",");
            }
            writer.println("OdemceneZbozi: " + odemceneZboziText + ";");
            writer.println("VybranaHudba: " + vybranaHudba + ";");
            String odemcenaHudbaText = String.join(",", odemcenaHudba);
            writer.println("OdemcenaHudba: " + odemcenaHudbaText + ";");
            writer.close();
            System.out.println("Hra uložena do slotu: " + slot);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static List<Zbozi> nactiZbozi() {
        File soubor = new File("src/main/resources/zbozi.txt");
        if (!soubor.exists() || soubor.length() == 0) {
            System.out.println("Nenačetlo se zboží.");
            System.exit(0);
        }
        List<Zbozi> zboziVsechny = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(soubor))) {
            String radek;
            while ((radek = br.readLine()) != null) {
                if (radek.trim().isEmpty()) continue;
                radek = radek.replace("", "");
                if (radek.contains(";")) {
                    String[] slova = radek.split(";");
                    if (slova.length >= 9) {
                        Zbozi zbozi = new Zbozi(
                                slova[0].trim(),
                                Integer.parseInt(slova[1].trim()),
                                Integer.parseInt(slova[2].trim()),
                                Integer.parseInt(slova[3].trim()),
                                Integer.parseInt(slova[4].trim()),
                                Integer.parseInt(slova[5].trim()),
                                Integer.parseInt(slova[6].trim()),
                                Integer.parseInt(slova[7].trim()),
                                slova[8].trim()
                        );
                        zboziVsechny.add(zbozi);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        if (zboziVsechny.isEmpty()) {
            System.out.println("Seznam zboží je prázdný, žádné platné řádky nebyly nalezeny.");
        }

        return zboziVsechny;
    }
}
