/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iessineu.rondalles.entitats;

import com.googlecode.lanterna.TextColor;

/**
 *
 * @author kanhai
 */
public class DimoniBoiet extends Enemic {

    public DimoniBoiet(int x, int y) {
        super(x, y, 'd', 10, 5, 5);
    }

    @Override
    public void actualitzaIA(Jugador jugador) {
        if (distanciaAl(jugador) < radDeteccio) {
            canviaEstat(EstatEnemic.PERSEGUINT);
        } else {
            canviaEstat(EstatEnemic.PATRULLANT);
        }
    }

    @Override
    public TextColor getColor() {
        return new TextColor.RGB(180, 40, 10);
    }

}