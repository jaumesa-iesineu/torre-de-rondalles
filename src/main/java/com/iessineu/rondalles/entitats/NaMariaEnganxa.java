package com.iessineu.rondalles.entitats;

import com.googlecode.lanterna.TextColor;

public class NaMariaEnganxa extends Enemic {

    public NaMariaEnganxa(int x, int y) {

        super(x, y, 'M', 30, 9999, 0);
        this.lletra = 'M';
    }

    @Override
    public void actualitzaIA(Jugador jugador, char[][] cells) {
        // Trampa estàtica: no fa res
    }

    @Override
    public void actualitzaIAambRadi(Jugador jugador, int radEfectiu) {
        // Trampa estàtica: no fa res
    }

    @Override
    public TextColor getColor() {

        if (colorDef != null) {
            return colorDef;
        }

        return new TextColor.RGB(180, 50, 220);
    }
}