package com.iessineu.rondalles.motor;

import com.googlecode.lanterna.input.KeyStroke;
import java.io.File;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequencer;

/**
 *
 * @author jaume, dani, sergi, kanhai i pere
 */
public abstract class Motor {

    // el renderitzador gestiona tot el que surt per pantalla
    protected Renderitzador renderer;

    // mentre corrent sigui true el bucle no s'atura
    protected boolean corrent;

    // en quin estat es troba el joc ara mateix
    protected Estat estat;

    // reproductor de música de fons
    private Sequencer sequencer;

    // si true no es reprodueix música
    protected boolean mut = false;

    // cada joc sap com inicialitzar-se
    protected abstract void init() throws Exception;

    // cada joc sap com reaccionar a les tecles
    protected abstract void actualitza(KeyStroke tecla);

    // cada joc sap com pintar la seva pantalla
    protected abstract void renderitza();

    private void iniciaMusica() {

        if (mut) {
            return;
        }

        try {

            File fitxer = new File("musica/musica_fons.mid");

            if (!fitxer.exists()) {
                return;
            }

            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequencer.setSequence(new java.io.FileInputStream(fitxer));
            sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();

        } catch (Exception e) {
            // si no hi ha àudio disponible el joc continua igual
        }
    }

    private void aturaMusica() {

        if (sequencer != null) {

            if (sequencer.isRunning()) {
                sequencer.stop();
            }

            sequencer.close();
        }
    }

    public void start() throws Exception {

        init();

        iniciaMusica();

        corrent = true;
        estat = Estat.MENU_INICIAL;

        while (corrent) {

            try {
                renderitza();
            } catch (Exception ex) {
                System.err.println("[RENDER ERROR] " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
                ex.printStackTrace(System.err);
            }

            KeyStroke tecla = renderer.llegeixInput();

            if (tecla != null) {
                try {
                    actualitza(tecla);
                } catch (Exception ex) {
                    System.err.println("[UPDATE ERROR] " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
                    ex.printStackTrace(System.err);
                }
            }
        }

        aturaMusica();

        renderer.tanca();
    }
}