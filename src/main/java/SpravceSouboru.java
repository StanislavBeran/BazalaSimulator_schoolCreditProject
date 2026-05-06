import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SpravceSouboru {
    public static UlozenaHra nactiUlozeneHryZeSouboru(String cesta) {
        File soubor = new File(cesta);

        if (!soubor.exists() || soubor.length() == 0) {
            return new UlozenaHra("Prázdné", 0, 0, 0);
        }

        String jmenoObchodu = "Neznámé";
        int obtiznost = 0;
        int penize = 0;
        int xp = 0;

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
                    } else if (radek.startsWith("Obtiznost")) {
                        obtiznost = Integer.parseInt(hodnota);
                    } else if (radek.startsWith("Penize")) {
                        penize = Integer.parseInt(hodnota);
                    } else if (radek.startsWith("XP")){
                        xp = Integer.parseInt(hodnota);
                    }
                }
            }
            UlozenaHra ulozenaHra = new UlozenaHra(jmenoObchodu, obtiznost, penize, xp);
            return ulozenaHra;

        } catch (IOException e) {
            e.printStackTrace();
            return new UlozenaHra("ERROR", 0, 0, 0);
        }
    }

    public static void ulozHruDoSouboru(int slot, String jmeno, int obtiznost, String penize, int xp) {
        try {
            File slozka = new File("ulozeneHry");
            if (!slozka.exists()) {
                slozka.mkdir();
            }
            File soubor = new File("src/main/resources/ulozeneHry/ulozenaHra" + slot + ".txt");
            PrintWriter writer = new PrintWriter(new FileWriter(soubor));
            writer.println("Jmeno: \"" + jmeno + "\";");
            writer.println("Obtiznost: " + obtiznost + ";");
            writer.println("Penize: " + penize + ";");
            writer.println("XP: " + xp + ";");
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
        if (!zboziVsechny.isEmpty()) {
            System.out.println(zboziVsechny.get(0).zkracenyNazev);
            if(zboziVsechny.size() > 1) {
                System.out.println(zboziVsechny.get(1).zkracenyNazev);
            }
        } else {
            System.out.println("Seznam zboží je prázdný, žádné platné řádky nebyly nalezeny.");
        }

        return zboziVsechny;
    }
    public static String nactiZboziObrazky(String cesta) {
        File soubor = new File(cesta);

        if (!soubor.exists() || soubor.length() == 0) {
            return "Prázdná pozice";
        }

        String jmeno = "Neznámé";
        String obtiznost = "Neznámá";
        String penize = "0";

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
                        jmeno = hodnota;
                    } else if (radek.startsWith("Obtiznost")) {
                        switch (hodnota) {
                            case "0": obtiznost = "Lehká"; break;
                            case "1": obtiznost = "Střední"; break;
                            case "2": obtiznost = "Obtížná"; break;
                            case "3": obtiznost = "Adam (Hardcore)"; break;
                            default: obtiznost = hodnota; break;
                        }
                    } else if (radek.startsWith("Penize")) {
                        penize = hodnota;
                    }
                }
            }
            return jmeno + " | Obtížnost: " + obtiznost + " | Peníze: " + penize;

        } catch (IOException e) {
            e.printStackTrace();
            return "Chyba při čtení";
        }
    }


}
