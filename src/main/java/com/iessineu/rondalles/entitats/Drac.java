package com.iessineu.rondalles.entitats;

import com.googlecode.lanterna.TextColor;
import com.iessineu.rondalles.mapa.Mapa;

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
    public TextColor getColor() { // vermell intens
        if (colorDef != null) return colorDef;
        return new TextColor.RGB(220, 30, 30);
    }

}
