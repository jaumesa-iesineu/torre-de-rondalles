package com.iessineu.rondalles.inventari;

import com.iessineu.rondalles.entitats.Jugador;

/**
 *
 * @author kanhai, jaume, dani, sergi, pere
 */
public class Arma extends Item { // arma equipable pel jugador

    private int atac;
    private int rang; // 1 = cuerp a cuerp, 2 = distància

    public Arma(String nom, int pes, char simbol, int atac, int rang) {
        super(nom, pes, simbol);
        this.atac = atac;
        this.rang = rang;
    }

    @Override
    public void aplicaEfecte(Jugador jugador) { // suma l'atac de l'arma a les stats del jugador
        jugador.setAtacExtra(atac);
    }

    public int getAtac() {
        return atac;
    }

    public int getRang() {
        return rang;
    }
}
