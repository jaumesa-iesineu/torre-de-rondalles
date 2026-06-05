package com.iessineu.rondalles.inventari;

import com.googlecode.lanterna.TextColor;
import com.iessineu.rondalles.entitats.Jugador;

public class Clau extends Item {

    private int planta;
    private String id;

    public Clau(String id, String nom, int pes, char simbol, int planta) {
        super(nom, pes, simbol);
        this.id = id;
        this.planta = planta;
    }

    public String getId() {
        return id;
    }

    public int getPlanta() {
        return planta;
    }

    @Override
    public void aplicaEfecte(Jugador jugador) {
    }

    @Override
    public TextColor getColor() {
        return new TextColor.RGB(255, 215, 0); //daurat, com ha de ser
    }
}
