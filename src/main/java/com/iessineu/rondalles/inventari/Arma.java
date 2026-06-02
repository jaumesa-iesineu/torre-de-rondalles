package com.iessineu.rondalles.inventari;

import com.googlecode.lanterna.TextColor;
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

    @Override
    public TextColor getColor() { // daurat — intensitat creix amb l'atac (nivell 1-3)
        if (atac <= 3) {
            return new TextColor.RGB(180, 140, 40);  // daurat apagat
        }
        if (atac <= 9) {
            return new TextColor.RGB(220, 180, 50);  // daurat mitjà
        }
        return new TextColor.RGB(255, 215, 0);          // daurat brillant
    }
}
