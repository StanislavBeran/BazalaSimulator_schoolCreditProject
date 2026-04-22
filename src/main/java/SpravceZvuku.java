import javax.sound.sampled.*;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class SpravceZvuku {

    private static Map<String, Clip> zvuky = new HashMap<>();

    private static AudioInputStream nactiStream(String cesta) throws Exception {
        URL url = SpravceZvuku.class.getResource(cesta);
        if (url != null) return AudioSystem.getAudioInputStream(url);
        File soubor = new File(cesta);
        if (soubor.exists()) return AudioSystem.getAudioInputStream(soubor);
        throw new Exception("Soubor nenalezen: " + cesta);
    }

    public static void prehraj(String cesta) {
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(nactiStream("zvuky" + cesta));
            clip.start();
        } catch (Exception e) { System.err.println("Chyba zvuku: " + e.getMessage()); }
    }

    public static void prehraj(String id, String cesta, double sekundy, boolean smycka) {
        try {
            Clip clip;

            if (zvuky.containsKey(id)) {
                clip = zvuky.get(id);
            } else {
                clip = AudioSystem.getClip();
                clip.open(nactiStream(cesta));
                zvuky.put(id, clip);
            }
            if (smycka) clip.loop(Clip.LOOP_CONTINUOUSLY);
            else clip.loop(0);
            clip.start();

            if (sekundy > 0) {
                new Timer().schedule(new TimerTask() {
                    @Override public void run() {
                        if (clip.isRunning()) clip.stop(); // stop() zvuk jen pauzne!
                    }
                }, (long) (sekundy * 1000));
            }

        } catch (Exception e) { System.err.println("Chyba zvuku: " + e.getMessage()); }
    }

    // --- 3. RUČNÍ ZASTAVENÍ (Pauza) ---
    public static void zastav(String id) {
        if (zvuky.containsKey(id)) zvuky.get(id).stop();
    }

    // --- 4. ÚPLNÉ VYMAZÁNÍ (Když zvuk už nechceš a chceš mu resetovat pozici) ---
    public static void vymaz(String id) {
        if (zvuky.containsKey(id)) {
            Clip clip = zvuky.remove(id);
            clip.stop();
            clip.close();
        }
    }
    public static void zastavVsechnuHudbu() {
        for (Clip clip : zvuky.values()) {
            if (clip.isRunning()) {
                clip.stop();
            }
        }
        zvuky.clear();
    }
}