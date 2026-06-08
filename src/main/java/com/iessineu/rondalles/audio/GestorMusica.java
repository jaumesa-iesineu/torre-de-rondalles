package com.iessineu.rondalles.audio;

import com.iessineu.rondalles.joc.ConfigGame;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class GestorMusica {

    private static volatile SourceDataLine lineActual = null;
    private static Thread threadReprodueix = null;
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

        reprodueixAudio("audio/" + fitxer);
    }

    private static void reprodueixAudio(String path) {
        reproduint = true;
        threadReprodueix = new Thread(() -> {
            while (reproduint && !Thread.currentThread().isInterrupted()) {
                try {
                    URL url = GestorMusica.class.getClassLoader().getResource(path);
                    if (url == null) {
                        System.out.println("No s'ha trobat: " + path);
                        return;
                    }

                    AudioInputStream aisOriginal = AudioSystem.getAudioInputStream(url);
                    AudioFormat formatOriginal = aisOriginal.getFormat();

                    // Convertir a PCM si cal (MP3, etc.)
                    AudioInputStream ais = aisOriginal;
                    AudioFormat format = formatOriginal;
                    if (formatOriginal.getEncoding() != AudioFormat.Encoding.PCM_SIGNED
                            && formatOriginal.getEncoding() != AudioFormat.Encoding.PCM_UNSIGNED) {
                        format = new AudioFormat(
                                AudioFormat.Encoding.PCM_SIGNED,
                                formatOriginal.getSampleRate(),
                                16,
                                formatOriginal.getChannels(),
                                formatOriginal.getChannels() * 2,
                                formatOriginal.getSampleRate(),
                                false
                        );
                        ais = AudioSystem.getAudioInputStream(format, aisOriginal);
                    }

                    DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
                    SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
                    line.open(format);
                    lineActual = line;
                    aplicaVolum();
                    line.start();

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while (reproduint && !Thread.currentThread().isInterrupted()
                            && (bytesRead = ais.read(buffer)) != -1) {
                        line.write(buffer, 0, bytesRead);
                    }

                    line.drain();
                    line.stop();
                    line.close();
                    lineActual = null;
                    ais.close();

                } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
                    if (reproduint) System.out.println("Error audio: " + e.getMessage());
                    break;
                } catch (Exception e) {
                    break;
                }
            }
        });
        threadReprodueix.setDaemon(true);
        threadReprodueix.start();
    }

    public static void atura() {
        reproduint = false;
        SourceDataLine line = lineActual;
        if (line != null) {
            line.stop();
            line.close();
            lineActual = null;
        }
        if (threadReprodueix != null) {
            threadReprodueix.interrupt();
            threadReprodueix = null;
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
        SourceDataLine line = lineActual;
        if (line == null) return;
        try {
            if (line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
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
