package com.iessineu.rondalles.motor;

import com.googlecode.lanterna.TextColor;

public final class EinesColor {

    private EinesColor() {}

    //retorna entre 0 i 255
    public static int limita(int v) {
        return Math.max(0, Math.min(255, v));
    }

    //crea un textcolor a partir de rgb
    public static TextColor.RGB creaColor(int r, int g, int b) {
        return new TextColor.RGB(limita(r), limita(g), limita(b));
    }
}
