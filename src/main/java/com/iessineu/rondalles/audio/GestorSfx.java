package com.iessineu.rondalles.audio;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GestorSfx {

    private static final Map<String, String> fitxers = new HashMap<>();
    private static final Map<String, Clip> clips = new HashMap<>();
    private static final ExecutorService pool = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });
    private static boolean silenci = false;
    private static String personatgeId = "";

    public static void inicialitza(Map<String, String> cfg) {
        fitxers.clear();
        if (cfg != null) fitxers.putAll(cfg);
        precarregaClips();
    }

    //precarregam tots els WAV a l'inici per evitar talls
    private static void precarregaClips() {
        clips.clear();
        for (Map.Entry<String, String> entry : fitxers.entrySet()) {
            String path = entry.getValue();
            if (path == null || !path.toLowerCase().endsWith(".wav")) continue;
            try {
                InputStream is = GestorSfx.class.getClassLoader().getResourceAsStream(path);
                if (is == null) continue;
                AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                clips.put(entry.getKey(), clip);
            } catch (Exception e) {
                //si no carrega, simplement no tendra so
            }
        }
    }

    public static void setSilenci(boolean m) { silenci = m; }
    public static void setPersonatgeId(String id) { personatgeId = id != null ? id : ""; }

    public static void reprodueix(String clau) {
        if (silenci) return;
        String fitxer = fitxers.getOrDefault(clau + "_" + personatgeId, fitxers.get(clau));
        if (fitxer == null) return;

        //si tenim el clip precarregat, el reproduim directament (mes rapid)
        String clauReal = fitxers.containsKey(clau + "_" + personatgeId) ? clau + "_" + personatgeId : clau;
        Clip precarregat = clips.get(clauReal);
        if (precarregat != null) {
            precarregat.setFramePosition(0);
            precarregat.start();
            return;
        }

        //MP3 o fallback per si no s'ha pogut precarregar
        pool.submit(() -> {
            try {
                InputStream is = GestorSfx.class.getClassLoader().getResourceAsStream(fitxer);
                if (is == null) return;
                if (fitxer.toLowerCase().endsWith(".mp3")) {
                    javazoom.jl.player.Player player = new javazoom.jl.player.Player(new BufferedInputStream(is));
                    player.play();
                    player.close();
                }
            } catch (Exception ignored) {}
        });
    }
}
