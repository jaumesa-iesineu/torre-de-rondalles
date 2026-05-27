package com.iessineu.rondalles.inventari;

import com.googlecode.lanterna.TextColor;
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

    @Override
    public TextColor getColor() {
        // cada slot té un color base; la intensitat puja amb la defensa (nivells 1/2/3)
        return switch (slot) {
            case CASC  -> defensa <= 1 ? new TextColor.RGB(100, 160, 100)  // verd apagat
                        : defensa <= 3 ? new TextColor.RGB(60,  200, 60)   // verd mitjà
                        :               new TextColor.RGB(0,   255, 80);   // verd brillant
            case COS   -> defensa <= 2 ? new TextColor.RGB(100, 100, 180)  // blau apagat
                        : defensa <= 5 ? new TextColor.RGB(60,  60,  220)  // blau mitjà
                        :               new TextColor.RGB(80,  120, 255);  // blau brillant
            case CAMES -> defensa <= 1 ? new TextColor.RGB(160, 100, 60)   // marró apagat
                        : defensa <= 3 ? new TextColor.RGB(200, 130, 60)   // marró mitjà
                        :               new TextColor.RGB(240, 160, 60);   // marró brillant
            case PEUS  -> defensa <= 1 ? new TextColor.RGB(120, 80,  120)  // porpra apagat
                        : defensa <= 2 ? new TextColor.RGB(170, 80,  170)  // porpra mitjà
                        :               new TextColor.RGB(220, 80,  220);  // porpra brillant
        };
    }
}
