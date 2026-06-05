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

    //el renderitzador pinta tot el que surt per pantalla
    protected Renderitzador renderer;

    //mentre sigui vertader es joc continua
    protected boolean corrent;

    //l'estat actual del joc (menu, combat, etc)
    protected Estat estat;

    //reproductor de musica de fons
    private Sequencer sequencer;

    //si esta a true no sona musica
    protected boolean mut = false;

    //cada joc te la seva propia manera d'iniciar
    protected abstract void init() throws Exception;

    //cada joc reacciona a les tecles de forma diferent
    protected abstract void actualitza(KeyStroke tecla);

    //per si una animacio esta en marxa, no esperam entrada
    protected boolean estaAnimant() { return false; }

    //cada joc es pinta a la seva manera
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
            //si no hi ha audio no passa res, es joc segueix
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
        //bucle principal del joc: pintar, input, actualitzar

        init();
        iniciaMusica();

        corrent = true;
        estat = Estat.MENU_INICIAL;

        while (corrent) {
            //pintam primer, que es mes rapid
            try {
                renderitza();
            } catch (Exception ex) {
                //no volem que peti per un error de render
                System.err.println("[RENDER ERROR] " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
                ex.printStackTrace(System.err);
            }

            //despres esperam que premi una tecla
            KeyStroke tecla;
            if (estaAnimant()) {
                //si esta animant no esperam, llegim sense bloquejar
                tecla = renderer.pollInput();
                if (tecla == null) {
                    try { Thread.sleep(16); } catch (InterruptedException ignored) {} //60fps aprox
                }
            } else {
                tecla = renderer.llegeixInput();
            }

            //actualitzam es estat del joc
            if (tecla != null || estaAnimant()) {
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
