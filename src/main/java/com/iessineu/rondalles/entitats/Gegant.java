package com.iessineu.rondalles.entitats;

import com.googlecode.lanterna.TextColor;

/**
 *
 * @author kanhai, jaume, dani, sergi, pere
 */
public class Gegant extends Enemic { // extends Enemic perquè és una subclasse d'enemic

    public Gegant(int x, int y) { // constructor de la classe Gegant
        super(x, y, 'G', 40, 10, 4);
        this.lletra = 'G';
    }

    @Override
    public void actualitzaIA(Jugador jugador, char[][] cells) {
        if (dinsArea(jugador.getX(), jugador.getY()) && distanciaAl(jugador) <= radDeteccio && potVeure(jugador, cells)) {
            canviaEstat(EstatEnemic.PERSEGUINT);
            int[] pas = primerPasBFS(jugador.getX(), jugador.getY(), cells);
            if (pas[0] != -1) mouCapA(pas[0], pas[1], cells, jugador);
        } else if (!dinsArea(jugador.getX(), jugador.getY()) && distanciaAlSpawn() > 1.0) {
            //el jugador ha sortit de l'area, tornam al spawn
            canviaEstat(EstatEnemic.PATRULLANT);
            tornaAlSpawn(cells);
        } else {
            canviaEstat(EstatEnemic.PATRULLANT);
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
        if (colorDef != null) return colorDef;
        return new TextColor.RGB(200, 120, 50);
    }

}
