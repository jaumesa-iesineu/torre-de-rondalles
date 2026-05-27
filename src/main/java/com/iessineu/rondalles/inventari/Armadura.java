package com.iessineu.rondalles.inventari;

import com.iessineu.rondalles.entitats.Jugador;

/**
 *
 * @author kanhai, jaume, dani, sergi, pere
 */
public class Armadura extends Item { // armadura equipable per slot

    public enum Slot {
        CASC,
        COS,
        CAMES,
        PEUS
    }

    private int defensa;
    private Slot slot; // on s'equipa aquesta peça

    public Armadura(String nom, int pes, char simbol, int defensa, Slot slot) {
        super(nom, pes, simbol);
        this.defensa = defensa;
        this.slot = slot;
    }

    @Override
    public void aplicaEfecte(Jugador jugador) { // Inventari crida aquest mètode després de recalcular tota la defensa equipada
        jugador.setDefensaExtra(defensa);
    }

    public int getDefensa() {
        return defensa;
    }

    public Slot getSlot() {
        return slot;
    }
}
