package com.iessineu.rondalles.audio;

import javax.sound.sampled.*;
import java.net.URL;

public class GestorMusica {

    private static Clip clipActual = null;
    private static boolean silenciat = false;
    private static Pista pistaActual = null;

    public enum Pista {
        MENU("menu.wav"),
        PIS_1("pis1.wav"),
        PIS_2("pis2.wav"),
        PIS_3("pis3.wav"),
        PIS_4("pis4.wav"),
        PIS_5("pis5.wav"),
        COMBAT("combat.wav"),
        BOSS("boss.wav"),
        VICTORIA("victoria.wav"),
        GAME_OVER("gameover.wav");

        public final String fitxer;

        Pista(String f) {
            this.fitxer = f;
        }
    }

    public static void reprodueix(Pista pista) {
        pistaActual = pista;

        if (silenciat) {
            return;
        }

        atura();

        try {
            URL url = GestorMusica.class
                    .getClassLoader()
                    .getResource("audio/" + pista.fitxer);

            if (url == null) {
                System.out.println("No s'ha trobat l'arxiu de so: " + pista.fitxer);
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
            if (clipActual.isRunning()) {
                clipActual.stop();
            }

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

    public static boolean estaSilenciat() {
        return silenciat;
    }

    public static Pista getPistaActual() {
        return pistaActual;
    }
}