package com.iessineu.rondalles.audio;

import com.iessineu.rondalles.joc.ConfigGame;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.*;
import java.net.URL;

public class GestorMusica {

    private static Clip clipActual = null;
    private static boolean silenciat = false;
    private static String pistaActual = null;
    private static final Map<String, String> pistes = new HashMap<>();

    // carrega les pistes des del JSON
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

        try {
            URL url = GestorMusica.class
                    .getClassLoader()
                    .getResource("audio/" + fitxer);

            if (url == null) {
                System.out.println("No s'ha trobat l'arxiu de so: " + fitxer);
                return;
            }

            try (AudioInputStream ais = AudioSystem.getAudioInputStream(url)) {
                clipActual = AudioSystem.getClip();
                clipActual.open(ais);
                clipActual.loop(Clip.LOOP_CONTINUOUSLY);
                clipActual.start();
            }

        } catch (Exception e) {
            System.out.println("Error reproduint música: " + e.getMessage());
        }
    }

    public static void atura() {
        if (clipActual != null) {
            if (clipActual.isRunning()) clipActual.stop();
            clipActual.close();
            clipActual = null;
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

    public static boolean estaSilenciat() { return silenciat; }
    public static String getPistaActual() { return pistaActual; }
}
