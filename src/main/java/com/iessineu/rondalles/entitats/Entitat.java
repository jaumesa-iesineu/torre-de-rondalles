/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iessineu.rondalles.entitats;

import com.googlecode.lanterna.TextColor;

/**
 *
 * @author jaume, dani, sergi, kanhai i pere
 */
public abstract class Entitat { //classe base per ses entitats (enemics, jugadors, npcs...)

    //posicio de l'entitat al mapa
    protected int x;
    protected int y;

    //el simbol que es pinta per pantalla
    protected char simbol;

    //per gestionar el sistema per torns (roguelike)
    protected boolean actiu;

    public Entitat(int x, int y, char simbol) {
        this.x = x;
        this.y = y;
        this.simbol = simbol;
        this.actiu = true;
    }

    //per si te un efecte de veri o algo cada torn
    public abstract void actualitza();

    //interactuar amb portes, cofres...
    public abstract void interactua(Jugador jugador);

    public abstract TextColor getColor();

    //getters i setters, aixi funciona java
    //getters
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public char getSimbol() {
        return simbol;
    }

    public boolean isActiu() {
        return actiu;
    }

    //setters
    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setActiu(boolean actiu) {
        this.actiu = actiu;
    }
}
