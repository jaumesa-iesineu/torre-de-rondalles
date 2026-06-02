package com.iessineu.rondalles.mapa;

public enum TipusTerra {

    NORMAL('.'),
    AIGUA('~'),
    GESPA(','),
    METAL('='),
    GEL('*');

    public final char simbol;

    TipusTerra(char simbol) {
        this.simbol = simbol;
    }

    public static TipusTerra de(char c) {

        for (TipusTerra t : values()) {
            if (t.simbol == c) {
                return t;
            }
        }

        return NORMAL;
    }
}