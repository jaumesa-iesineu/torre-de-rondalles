/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iessineu.rondalles.entitats;

/**
 *
 * @author jaume, dani, sergi, kanhai i pere
 */
public abstract class Entitat {

    //posició de s'entitat a nes mapa
    protected int x;
    protected int y;

    //es simbol que es pinta
    protected char simbol;

    //per gestionar el sistema per torns rogue
    protected boolean actiu;

    public Entitat(int x, int y, char simbol) {
        this.x = x;
        this.y = y;
        this.simbol = simbol;
        this.actiu = true;
    }

    //funció per si l'entitat per exemple té un efecte de verí
    public abstract void actualitza();

    //interacció
    public abstract void interactua(Jugador jugador);

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
