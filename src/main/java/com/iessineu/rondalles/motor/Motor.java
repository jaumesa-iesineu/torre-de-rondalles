package com.iessineu.rondalles.motor;

import com.googlecode.lanterna.input.KeyStroke;

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

    // cada joc sap com inicialitzar-se (carregar mapa, crear jugador...)
    protected abstract void init() throws Exception;

    // cada joc sap com reaccionar a les tecles
    protected abstract void actualitza(KeyStroke tecla);

    // cada joc sap com pintar la seva pantalla
    protected abstract void renderitza();

    // aquí és on arranca tot
    public void start() throws Exception {
        init();
        corrent = true;
        estat = Estat.MENU_INICIAL;

        // el joc és per torns: esperam que el jugador premi una tecla
        while (corrent) {
            renderitza();

            KeyStroke tecla = renderer.llegeixInput();

            if (tecla != null) {
                actualitza(tecla);
            }
        }

        renderer.tanca();
    }
}