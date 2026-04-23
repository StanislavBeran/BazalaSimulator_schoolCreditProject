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
            return jmeno + " | Obtížnost: " + obtiznost + " | Peníze: " + penize;

        } catch (IOException e) {
            e.printStackTrace();
            return "Chyba při čtení";
        }
    }
    public static List<Zbozi> nactiZbozi(String cesta) {
        File soubor = new File(cesta);

        if (!soubor.exists() || soubor.length() == 0) {
            System.out.println("Nenačetlo se zboží.");
            System.exit(0);
        }
        List<Zbozi> zboziVsechny = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(soubor))) {
            String radek;
            while ((radek = br.readLine()) != null) {
                if (radek.trim().isEmpty()) continue;
                if (radek.contains(":")) {
                    String[] slova = radek.split(";");
                    Zbozi zbozi = new Zbozi(
                            slova[0],
                            Integer.valueOf(slova[1]),
                            Integer.valueOf(slova[2]),
                            Integer.valueOf(slova[3]),
                            Integer.valueOf(slova[4]),
                            Integer.valueOf(slova[5]),
                            Integer.valueOf(slova[6]),
                            Integer.valueOf(slova[7]),
                            Integer.valueOf(slova[8]),
                            Integer.valueOf(slova[9]),
                            slova[10]
                    );
                    zboziVsechny.add(zbozi);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
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
