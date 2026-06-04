package com.iessineu.rondalles.entitats;

import com.googlecode.lanterna.TextColor;

public class DimoniBoiet extends Enemic {

    public DimoniBoiet(int x, int y) {
        super(x, y, 'd', 10, 5, 5);
        this.lletra = 'd';
    }

    @Override
    public void actualitzaIA(Jugador jugador, char[][] cells) {

        if (distanciaAl(jugador) < radDeteccio && potVeure(jugador, cells)) {
            canviaEstat(EstatEnemic.PERSEGUINT);
            mouCapA(jugador.getX(), jugador.getY(), cells, jugador);
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
    public TextColor getColor() {

        if (colorDef != null) {
            return colorDef;
        }

        return new TextColor.RGB(180, 40, 10);
    }
}
