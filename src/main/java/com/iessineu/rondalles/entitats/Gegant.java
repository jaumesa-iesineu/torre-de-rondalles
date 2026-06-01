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
    public void actualitzaIA(Jugador jugador) { // actualitzaIA actualitza la IA del Gegant cada torn
        if (distanciaAl(jugador) < radDeteccio) {
            canviaEstat(EstatEnemic.PERSEGUINT); // si el jugador entra al radi de detecció, persegueix
        } else {
            canviaEstat(EstatEnemic.PATRULLANT); // si no, patrulla
        }
    }
    @Override
public void actualitzaIAambRadi(Jugador jugador, int radEfectiu) {
    if (distanciaAl(jugador) < radEfectiu) {
        canviaEstat(EstatEnemic.PERSEGUINT);
    } else {
        canviaEstat(EstatEnemic.PATRULLANT);
    }
}
    @Override
    public TextColor getColor() { // ataronjat fosc
        return new TextColor.RGB(200, 120, 50);
    }

}
