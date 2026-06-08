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
    }

    public static void setSilenci(boolean m) { silenci = m; }
    public static void setPersonatgeId(String id) { personatgeId = id != null ? id : ""; }

    public static void reprodueix(String clau) {
        if (silenci) return;
        String fitxer = fitxers.getOrDefault(clau + "_" + personatgeId, fitxers.get(clau));
        if (fitxer == null) return;
        pool.submit(() -> {
            try {
                InputStream is = GestorSfx.class.getClassLoader().getResourceAsStream(fitxer);
                if (is == null) return;
                if (fitxer.toLowerCase().endsWith(".mp3")) {
                    javazoom.jl.player.Player player = new javazoom.jl.player.Player(new BufferedInputStream(is));
                    player.play();
                    player.close();
                } else {
                    AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
                    Clip clip = AudioSystem.getClip();
                    clip.open(ais);
                    clip.start();
                    while (clip.isRunning()) Thread.sleep(20);
                    clip.close();
                }
            } catch (Exception ignored) {}
        });
    }
}
