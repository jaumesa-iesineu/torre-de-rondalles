package com.iessineu.rondalles.inventari;

import com.googlecode.lanterna.TextColor;
import com.iessineu.rondalles.entitats.Jugador;

/**
 *
 * @author kanhai, jaume, dani, sergi, pere
 */
public abstract class Item { //classe base per a tots els items del joc

    protected String nom;
    protected int pes;
    protected char simbol;
    protected int tier; //0=comu 1=inusual 2=rar 3=epic

    public Item(String nom, int pes, char simbol) {
        this.nom = nom;
        this.pes = pes;
        this.simbol = simbol;
        this.tier = 0;
    }

    public Item(String nom, int pes, char simbol, int tier) {
        this(nom, pes, simbol);
        this.tier = tier;
    }

    //cada item sap com afectar es jugador quant s'equipa o s'usa
    public abstract void aplicaEfecte(Jugador jugador);

    //cada item defineix el seu color per al renderitzador
    public abstract TextColor getColor();

    public String getNom() {
        return nom;
    }

    public int getPes() {
        return pes;
    }

    public char getSimbol() {
        return simbol;
    }

    public int getTier() {
        return tier;
    }

    public String getTierSimbol() {
        return switch (tier) {
            case 1 -> "◆";
            case 2 -> "★";
            case 3 -> "✦";
            default -> "·";
        };
    }

    public TextColor getTierColor() {
        return switch (tier) {
            case 1 -> new TextColor.RGB(80, 200, 80);
            case 2 -> new TextColor.RGB(80, 140, 255);
            case 3 -> new TextColor.RGB(180, 80, 255);
            default -> new TextColor.RGB(160, 160, 160);
        };
    }
}
