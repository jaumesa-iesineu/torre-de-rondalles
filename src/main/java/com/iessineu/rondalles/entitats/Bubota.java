package com.iessineu.rondalles.entitats;

import com.googlecode.lanterna.TextColor;

/**
 *
 * @author kanhai, jaume, dani, sergi, pere
 */
public class Bubota extends Enemic { // extends Enemic perquè és una subclasse d'enemic

    public Bubota(int x, int y) { // constructor de la classe Bubota
        super(x, y, 'B', 20, 5, 7);
        this.lletra = 'B';
    }

    @Override
    public void actualitzaIA(Jugador jugador, char[][] cells) {
        if (isDescobert() && distanciaAl(jugador) <= radDeteccio && potVeure(jugador, cells)) {
            canviaEstat(EstatEnemic.PERSEGUINT);
            mouCapA(jugador.getX(), jugador.getY(), cells, jugador);
        } else {
            canviaEstat(EstatEnemic.PATRULLANT);
        }
    }

    @Override
    public void actualitzaIAambRadi(Jugador jugador, int radEfectiu) {
        if (isDescobert() && distanciaAl(jugador) < radEfectiu) {
            canviaEstat(EstatEnemic.PERSEGUINT);
        } else {
            canviaEstat(EstatEnemic.PATRULLANT);
        }
    }

    @Override
    public TextColor getColor() { // blanc blavós
        if (colorDef != null) {
            return colorDef;
        }
        return new TextColor.RGB(180, 180, 255);
    }

}
