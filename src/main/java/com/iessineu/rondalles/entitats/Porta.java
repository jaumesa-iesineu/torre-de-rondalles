package com.iessineu.rondalles.entitats;

import com.googlecode.lanterna.TextColor;

public class Porta extends Entitat {

    private boolean oberta;

    public Porta(int x, int y) {
        super(x, y, '+');
        this.oberta = false;
    }

    public boolean isOberta() {
        return oberta;
    }

    public boolean isTancada() {
        return !oberta;
    }

    public void alterna() {
        oberta = !oberta;
        simbol = oberta ? '/' : '+';
    }

    @Override
    public void actualitza() {
    }

    @Override
    public void interactua(Jugador jugador) {
        alterna();
    }

    @Override
    public TextColor getColor() {
        if (oberta) {
            return new TextColor.RGB(120, 80, 40);
        }
        return new TextColor.RGB(180, 100, 40);
    }
}
