package com.iessineu.rondalles.entitats;

import com.googlecode.lanterna.TextColor;

public class DimoniBoiet extends Enemic {

    public DimoniBoiet(int x, int y) {
        super(x, y, 'd', 10, 5, 5);
        this.lletra = 'd';
    }

    @Override
    public void actualitzaIA(Jugador jugador) {
        actualitzaIAambRadi(jugador, radDeteccio);
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
        return new TextColor.RGB(180, 40, 10);
    }
}