package com.iessineu.rondalles.inventari;

import com.iessineu.rondalles.entitats.Jugador;

/**
 *
 * @author kanhai, jaume, dani, sergi, pere
 */
public abstract class Item { // classe base per a tots els items del joc

    protected String nom;
    protected int pes;
    protected char simbol;

    public Item(String nom, int pes, char simbol) {
        this.nom = nom;
        this.pes = pes;
        this.simbol = simbol;
    }

    // cada item sap com afectar el jugador quan s'equipa
    public abstract void aplicaEfecte(Jugador jugador);

    public String getNom() {
        return nom;
    }

    public int getPes() {
        return pes;
    }

    public char getSimbol() {
        return simbol;
    }
}
