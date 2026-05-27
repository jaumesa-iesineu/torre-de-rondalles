package com.iessineu.rondalles.combat;

import com.iessineu.rondalles.entitats.Enemic;
import com.iessineu.rondalles.entitats.Jugador;

public class SistemaCombat {

    public static int calculaDany(int atac, int defensa) { //dany mínim 1 sempre
        return Math.max(1, atac - defensa);
    }

    public static void atacaEnemic(Jugador jugador, Enemic enemic) { //jugador pega a l'enemic
        enemic.rebreDany(calculaDany(jugador.getAtacTotal(), 0));
    }

    public static void atacaJugador(Enemic enemic, Jugador jugador) { //enemic pega al jugador
        jugador.rebreDany(calculaDany(enemic.getAtac(), jugador.getDefensaTotal()));
    }
}
