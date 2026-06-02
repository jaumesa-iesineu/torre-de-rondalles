package com.iessineu.rondalles.entitats;

import com.googlecode.lanterna.TextColor;
import com.iessineu.rondalles.mapa.Mapa;

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
    public void actualitzaIA(Jugador jugador, Mapa mapa) {
        if (calculaDistanciaAlJugador(jugador) <= radDeteccio) {
            canviaEstat(EstatEnemic.PERSEGUINT);
            int[] primerPasDelCami = cercaCamiAmbAEstrella(jugador.getX(), jugador.getY(), mapa);
            if (primerPasDelCami[0] != -1) {
                mouUnPasCapA(primerPasDelCami[0], primerPasDelCami[1], mapa, jugador);
            }
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
