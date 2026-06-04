package com.iessineu.rondalles.entitats;

import com.googlecode.lanterna.TextColor;

/**
 *
 * @author kanhai, jaume, dani, sergi, pere
 */
public class Drac extends Enemic { // boss final de la planta 5

    public Drac(int x, int y) { // constructor de la classe Drac
        super(x, y, 'D', 50, 20, 8);
        this.lletra = 'D';
    }

    @Override
    public void actualitzaIA(Jugador jugador, char[][] cells) {
        boolean a = dinsArea(jugador.getX(), jugador.getY());
        boolean b = distanciaAl(jugador) <= radDeteccio;
        boolean c = potVeure(jugador, cells);
        double ds = distanciaAlSpawn();
        if (a && b && c) {
            canviaEstat(EstatEnemic.PERSEGUINT);
            int[] pas = primerPasBFS(jugador.getX(), jugador.getY(), cells);
            if (pas[0] != -1) mouCapA(pas[0], pas[1], cells, jugador);
        } else if (!a && ds > 1.0) {

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
    public TextColor getColor() { // vermell intens
        if (colorDef != null) return colorDef;
        return new TextColor.RGB(220, 30, 30);
    }

}
