import javax.sound.sampled.*;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class SpravceZvuku {

    private static Map<String, Clip> zvuky = new HashMap<>();
    private static float globalniHlasitost = 1.0f;

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
            clip.open(nactiStream("zvuky/" + cesta + ".wav"));
            aplikujHlasitost(clip);
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
                clip.open(nactiStream("zvuky/" + cesta + ".wav"));
                zvuky.put(id, clip);
            }
            if (smycka) clip.loop(Clip.LOOP_CONTINUOUSLY);
            else clip.loop(0);
            aplikujHlasitost(clip);
            clip.start();

            if (sekundy > 0) {
                new Timer().schedule(new TimerTask() {
                    @Override public void run() {
                        if (clip.isRunning()) clip.stop();
                    }
                }, (long) (sekundy * 1000));
            }

        } catch (Exception e) { System.err.println("Chyba zvuku: " + e.getMessage()); }
    }

    public static void zastav(String id) {
        if (zvuky.containsKey(id)) zvuky.get(id).stop();
    }

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
    public static void nastavHlasitost(int procenta) {
        globalniHlasitost = Math.max(0.0001f, procenta / 100f);
        for (Clip clip : zvuky.values()) {
            aplikujHlasitost(clip);
        }
    }

    private static void aplikujHlasitost(Clip clip) {
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                // Převod procentní hlasitosti na decibely (reálnější útlum zvuku)
                float db = (globalniHlasitost <= 0.01f) ? -80.0f : (float)(Math.log10(globalniHlasitost) * 20.0f);
                gainControl.setValue(db);
            }
        } catch (Exception e) {}
    }
}