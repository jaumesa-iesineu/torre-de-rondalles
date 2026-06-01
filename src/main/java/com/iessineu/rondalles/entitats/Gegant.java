package com.iessineu.rondalles.entitats;

import com.googlecode.lanterna.TextColor;

/**
 *
 * @author kanhai, jaume, dani, sergi, pere
 */
public class Gegant extends Enemic { // extends Enemic perquè és una subclasse d'enemic

    public Gegant(int x, int y) { // constructor de la classe Gegant
        super(x, y, 'G', 40, 10, 4);
        this.lletra='G';
    }

    @Override
    public void actualitzaIA(Jugador jugador, char[][] celles) { // actualitzaIA actualitza la IA del Gegant cada torn
        if (distanciaAl(jugador) < radDeteccio && potVeure(jugador, celles)) {
            canviaEstat(EstatEnemic.PERSEGUINT); // si el jugador entra al radi de detecció i el veu, persegueix
        } else {
            canviaEstat(EstatEnemic.PATRULLANT); // si no, patrulla
        }
    }

    @Override
    public TextColor getColor() { // ataronjat fosc
        if (colorDef != null) return colorDef;
        return new TextColor.RGB(200, 120, 50);
    }

}
