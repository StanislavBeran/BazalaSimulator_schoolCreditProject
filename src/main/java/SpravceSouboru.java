import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SpravceSouboru {
    public static String nactiUlozeneHryZeSouboru(String cesta) {
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
            return jmeno;

        } catch (IOException e) {
            e.printStackTrace();
            return "Chyba při čtení";
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
                // Přeskočíme prázdné řádky
                if (radek.trim().isEmpty()) continue;

                // Odstranění případného "" tagu, pokud v texťáku uvízl
                radek = radek.replace("", "");

                // OPRAVA: Soubor zbozi.txt používá jako oddělovač středník
                if (radek.contains(";")) {
                    String[] slova = radek.split(";");

                    // Bezpečnostní kontrola, abychom měli všechny potřebné parametry (11)
                    if (slova.length >= 11) {
                        Zbozi zbozi = new Zbozi(
                                slova[0].trim(),
                                Integer.parseInt(slova[1].trim()),
                                Integer.parseInt(slova[2].trim()),
                                Integer.parseInt(slova[3].trim()),
                                Integer.parseInt(slova[4].trim()),
                                Integer.parseInt(slova[5].trim()),
                                Integer.parseInt(slova[6].trim()),
                                Integer.parseInt(slova[7].trim()),
                                Integer.parseInt(slova[8].trim()),
                                Integer.parseInt(slova[9].trim()),
                                slova[10].trim()
                        );
                        zboziVsechny.add(zbozi);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Ochrana proti pádu IndexOutOfBoundsException, pokud je list prázdný
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
