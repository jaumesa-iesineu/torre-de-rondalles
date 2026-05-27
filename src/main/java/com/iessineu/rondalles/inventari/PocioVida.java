package com.iessineu.rondalles.inventari;

import com.iessineu.rondalles.entitats.Jugador;

public class PocioVida extends Item { //poció que cura el jugador

    private int quantitatCura;

    public PocioVida(int quantitatCura) {
        super("Poció de vida", 1, '!');
        this.quantitatCura = quantitatCura;
    }

    @Override
    public void aplicaEfecte(Jugador jugador) { //cura el jugador quan la beu
        jugador.curar(quantitatCura);
    }
}
