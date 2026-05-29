/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iessineu.rondalles.entitats;

import com.googlecode.lanterna.TextColor;

/**
 *
 * @author kanhai, jaume, dani, sergi, pere
 */
public class DimoniBoiet extends Enemic { // extends Enemic es perque extends la classe Enemic

    public DimoniBoiet(int x, int y) { // constructor de la classe DimoniBoiet
        super(x, y, 'd', 10, 5, 5);
        this.lletra='d';
    }

    @Override
    public void actualitzaIA(Jugador jugador) { // actualitzaIA es perque actualitza la IA del Dimoni Boiet
        if (distanciaAl(jugador) < radDeteccio) {
            canviaEstat(EstatEnemic.PERSEGUINT); // si entres al radi de deteccio, canvia a PERSEGUINT
        } else {
            canviaEstat(EstatEnemic.PATRULLANT); // si no entres al radi de deteccio, canvia a PATRULLANT
        }
    }

    @Override
    public TextColor getColor() { // getColor es perque retorna el color del Dimoni Boiet
        return new TextColor.RGB(180, 40, 10); // intent de vermell cremat :)
    }

}