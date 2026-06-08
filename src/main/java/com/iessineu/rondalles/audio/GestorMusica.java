package com.iessineu.rondalles.audio;

import com.iessineu.rondalles.joc.ConfigGame;
import javazoom.jl.player.Player;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class GestorMusica {

    private static Clip clipActual = null;
    private static Thread threadMp3 = null;
    private static volatile boolean reproduint = false;
    private static volatile boolean silenciat = false;
    private static String pistaActual = null;
    private static final Map<String, String> pistes = new HashMap<>();
    private static float volum = 0.75f;

    public static void inicialitza(ConfigGame.MusicaConfig cfg) {
        pistes.clear();
        if (cfg == null) return;
        pistes.put("MENU", cfg.menu);
        pistes.put("PIS_1", cfg.pis_1);
        pistes.put("PIS_2", cfg.pis_2);
        pistes.put("PIS_3", cfg.pis_3);
        pistes.put("PIS_4", cfg.pis_4);
        pistes.put("PIS_5", cfg.pis_5);
        pistes.put("COMBAT", cfg.combat);
        pistes.put("BOSS", cfg.boss);
        pistes.put("VICTORIA", cfg.victoria);
        pistes.put("GAME_OVER", cfg.gameOver);
    }

    public static void reprodueix(String nomPista) {
        pistaActual = nomPista;
        if (silenciat) return;
        atura();

        String fitxer = pistes.get(nomPista);
        if (fitxer == null) {
            System.out.println("Pista no trobada: " + nomPista);
            return;
        }

        if (fitxer.toLowerCase().endsWith(".mp3")) {
            reprodueixMp3("audio/" + fitxer);
        } else {
            reprodueixWav("audio/" + fitxer);
        }
    }

    private static void reprodueixWav(String path) {
        try {
            URL url = GestorMusica.class.getClassLoader().getResource(path);
            if (url == null) { System.out.println("No s'ha trobat: " + path); return; }
            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            clipActual = AudioSystem.getClip();
            clipActual.open(ais);
            ais.close();
            aplicaVolum();
            clipActual.loop(Clip.LOOP_CONTINUOUSLY);
            clipActual.start();
        } catch (Exception e) {
            System.out.println("Error WAV: " + e.getMessage());
        }
    }

    private static void reprodueixMp3(String path) {
        reproduint = true;
        threadMp3 = new Thread(() -> {
            while (reproduint && !Thread.currentThread().isInterrupted()) {
                try {
                    InputStream is = GestorMusica.class.getClassLoader().getResourceAsStream(path);
                    if (is == null) { System.out.println("No s'ha trobat: " + path); return; }
                    Player player = new Player(new BufferedInputStream(is));
                    player.play();
                    player.close();
                    is.close();
                } catch (javazoom.jl.decoder.JavaLayerException e) {
                    if (reproduint) System.out.println("Error MP3: " + e.getMessage());
                    break;
                } catch (Exception e) {
                    break;
                }
            }
        });
        threadMp3.setDaemon(true);
        threadMp3.start();
    }

    public static void atura() {
        reproduint = false;
        if (clipActual != null) {
            if (clipActual.isRunning()) clipActual.stop();
            clipActual.close();
            clipActual = null;
        }
        if (threadMp3 != null) {
            threadMp3.interrupt();
            threadMp3 = null;
        }
    }

    public static void toggleSilenci() {
        silenciat = !silenciat;
        if (silenciat) {
            atura();
        } else if (pistaActual != null) {
            reprodueix(pistaActual);
        }
    }

    public static void setVolum(float v) {
        volum = Math.max(0f, Math.min(1f, v));
        aplicaVolum();
    }

    public static float getVolum() { return volum; }

    private static void aplicaVolum() {
        if (clipActual == null) return;
        try {
            if (clipActual.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) clipActual.getControl(FloatControl.Type.MASTER_GAIN);
                float dB;
                if (volum <= 0.001f) {
                    dB = gain.getMinimum();
                } else {
                    dB = 20f * (float) Math.log10(volum);
                    dB = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB));
                }
                gain.setValue(dB);
            }
        } catch (Exception ignored) {}
    }

    public static boolean estaSilenciat() { return silenciat; }
    public static String getPistaActual() { return pistaActual; }
}
