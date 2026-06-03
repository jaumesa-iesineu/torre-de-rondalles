package com.iessineu.rondalles.combat;

import com.iessineu.rondalles.entitats.Enemic;
import com.iessineu.rondalles.entitats.Jugador;

public class SistemaCombat {

    public static int calculaDany(int atac, int defensa) { //dany mínim 1 sempre
        return Math.max(1, atac - defensa);
    }

    public static int atacaEnemic(Jugador jugador, Enemic enemic) { //jugador pega a l'enemic, retorna dany real
        int dany = calculaDany(jugador.getAtacTotal(), 0);
        enemic.rebreDany(dany);
        return dany;
    }

    public static int atacaJugador(Enemic enemic, Jugador jugador) {
        if (jugador.esquiva()) return -1; // -1 = esquivat
        int dany = calculaDany(enemic.getAtacEfectiu(), jugador.getDefensaTotal());
        jugador.rebreDany(dany);
        return dany;
    }

    public static void tickEnemics(Enemic enemic) {
        enemic.tickVeri();
        enemic.tickFoc();
        enemic.tickGel();
    }
}
