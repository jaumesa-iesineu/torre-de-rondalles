package com.iessineu.rondalles.entitats;

import com.googlecode.lanterna.TextColor;
import com.iessineu.rondalles.mapa.Mapa;

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
    public void actualitzaIA(Jugador jugador, Mapa mapa) {
        if (distanciaAl(jugador) <= radDeteccio) {
            canviaEstat(EstatEnemic.PERSEGUINT);
            mouCap(jugador.getX(), jugador.getY(), mapa, jugador);
        } else {
            canviaEstat(EstatEnemic.PATRULLANT);
        }
    }

    @Override
    public TextColor getColor() { // blanc blavós
        if (colorDef != null) return colorDef;
        return new TextColor.RGB(180, 180, 255);
    }

}
