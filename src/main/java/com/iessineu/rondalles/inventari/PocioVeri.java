package com.iessineu.rondalles.inventari;

import com.iessineu.rondalles.entitats.Jugador;

public class PocioVeri extends Item { //poció de verí, fa mal per torns

    private int torns;

    public PocioVeri(int torns) {
        super("Poció de verí", 1, '~');
        this.torns = torns;
    }

    @Override
    public void aplicaEfecte(Jugador jugador) { //enverina el jugador durant uns torns
        jugador.setTornsVeri(torns);
    }
}
