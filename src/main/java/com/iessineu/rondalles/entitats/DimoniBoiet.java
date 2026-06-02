/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iessineu.rondalles.entitats;

import com.googlecode.lanterna.TextColor;
import com.iessineu.rondalles.mapa.Mapa;

/**
 *
 * @author kanhai, jaume, dani, sergi, pere
 */
public class DimoniBoiet extends Enemic { // extends Enemic es perque extends la classe Enemic

    public DimoniBoiet(int x, int y) { // constructor de la classe DimoniBoiet
        super(x, y, 'd', 10, 5, 5);
        this.lletra = 'd';
    }

    @Override
    public void actualitzaIA(Jugador jugador, Mapa mapa) {
        if (distanciaAl(jugador) <= radDeteccio) {
            canviaEstat(EstatEnemic.PERSEGUINT);
            mouCap(jugador.getX(), jugador.getY(), mapa, jugador);
        } else {
            canviaEstat(EstatEnemic.PATRULLANT);
        }
    }

    @Override
    public TextColor getColor() { // getColor es perque retorna el color del Dimoni Boiet
        if (colorDef != null) return colorDef;
        return new TextColor.RGB(180, 40, 10); // intent de vermell cremat :)
    }

}
