package com.iessineu.rondalles.inventari;

import com.googlecode.lanterna.TextColor;
import com.iessineu.rondalles.entitats.Jugador;

public class Pocio extends Item {

    public enum Tipus { VIDA, VERI }

    private Tipus tipus;
    private int valor;

    public Pocio(String nom, int pes, char simbol, Tipus tipus, int valor) {
        super(nom, pes, simbol);
        this.tipus = tipus;
        this.valor = valor;
    }

    @Override
    public void aplicaEfecte(Jugador jugador) {
        switch (tipus) {
            case VIDA -> jugador.curar(valor);
            case VERI -> jugador.setTornsVeri(valor);
        }
    }

    @Override
    public TextColor getColor() {
        return switch (tipus) {
            case VIDA -> new TextColor.RGB(220, 60, 60);
            case VERI -> new TextColor.RGB(80, 200, 80);
        };
    }

    public Tipus getTipus() { return tipus; }
    public int getValor()   { return valor; }
}
