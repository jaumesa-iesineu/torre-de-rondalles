package com.iessineu.rondalles.entitats;

import com.googlecode.lanterna.TextColor;

/**
 *
 * @author kanhai, jaume, dani, sergi, pere
 */
public class Bubota extends Enemic { // extends Enemic perquè és una subclasse d'enemic

    public Bubota(int x, int y) { // constructor de la classe Bubota
        super(x, y, 'B', 20, 5, 7);
        this.lletra='B';
    }

    @Override
    public void actualitzaIA(Jugador jugador, char[][] celles) { // actualitzaIA actualitza la IA de la Bubota cada torn
        if (distanciaAl(jugador) < radDeteccio && potVeure(jugador, celles)) {
            canviaEstat(EstatEnemic.PERSEGUINT); // si el jugador entra al radi de detecció i el veu, persegueix
        } else {
            canviaEstat(EstatEnemic.PATRULLANT); // si no, patrulla
        }
    }

    @Override
    public TextColor getColor() { // blanc blavós
        if (colorDef != null) return colorDef;
        return new TextColor.RGB(180, 180, 255);
    }

}
