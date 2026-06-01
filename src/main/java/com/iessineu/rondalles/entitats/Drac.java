package com.iessineu.rondalles.entitats;

import com.googlecode.lanterna.TextColor;

/**
 *
 * @author kanhai, jaume, dani, sergi, pere
 */
public class Drac extends Enemic { // boss final de la planta 5
    

    public Drac(int x, int y) { // constructor de la classe Drac
        super(x, y, 'D', 50, 20, 8);
        this.lletra='D';
    }

    @Override
    public void actualitzaIA(Jugador jugador, char[][] celles) { // actualitzaIA actualitza la IA del Drac cada torn
        if (distanciaAl(jugador) < radDeteccio && potVeure(jugador, celles)) {
            canviaEstat(EstatEnemic.PERSEGUINT); // si el jugador entra al radi de detecció i el veu, persegueix
        } else {
            canviaEstat(EstatEnemic.PATRULLANT); // si no, patrulla
        }
    }

    @Override
    public TextColor getColor() { // vermell intens
        if (colorDef != null) return colorDef;
        return new TextColor.RGB(220, 30, 30);
    }

}
