/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iessineu.rondalles.entitats;

/**
 *
 * @author jaume, dani, sergi, kanhai i pere
 */
public abstract class Enemic extends Entitat { // extends Entitat es perque extends la classe Entitat
    
    char lletra;
    //màquina d'estats
    public enum EstatEnemic {
        PATRULLANT,
        ALERTA,
        PERSEGUINT,
        EXECUTANT_ACCIO,
        MORT
    }

    protected int vida;
    protected int vidaMaxima;
    protected int atac;

    private int tornsVeri = 0;
    private int tornsFoc  = 0;
    private int tornsGel  = 0;

    //distancia de detecció jugador
    protected int radDeteccio;

    //estat actual de la màquina
    protected EstatEnemic estatEnemic;

    public Enemic(int x, int y, char simbol, int vida, int atac, int radDeteccio) { // constructor de la classe Enemic
        super(x, y, simbol);
        this.vidaMaxima = vida;
        this.vida = vida;
        this.atac = atac;
        this.radDeteccio = radDeteccio;
        this.estatEnemic = EstatEnemic.PATRULLANT;
    }

    //cada tipus d'enemic té sa seva pròpia IA
    //es crida quan el jugador fa un moviment (és el seu torn)
    public abstract void actualitzaIA(Jugador jugador, char[][] celles);

    //Bresenham: comprova si hi ha línia de visió directa fins al jugador sense parets
    protected boolean potVeure(Jugador jugador, char[][] celles) {
        int x0 = this.x, y0 = this.y;
        int x1 = jugador.getX(), y1 = jugador.getY();
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        int cx = x0, cy = y0;
        while (cx != x1 || cy != y1) {
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; cx += sx; }
            if (e2 < dx)  { err += dx; cy += sy; }
            if (cx == x1 && cy == y1) break; //el jugador no és una paret
            if (cy >= 0 && cy < celles.length && cx >= 0 && cx < celles[cy].length)
                if (celles[cy][cx] == '#') return false;
        }
        return true;
    }

    @Override
    public void actualitza() {
        //per actualitzacions
    }

    @Override
    public void interactua(Jugador jugador) {
        //quan el jugador entra a la casella de l'enemic, pega
        //de moment simplement resta vida, el combat real el farem més endavant
        jugador.rebreDany(atac);
        estatEnemic = EstatEnemic.EXECUTANT_ACCIO;
    }

    //distància efins jugador
    protected double distanciaAl(Jugador jugador) {
        int dx = jugador.getX() - this.x;
        int dy = jugador.getY() - this.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public void canviaEstat(EstatEnemic nouEstat) { 
        this.estatEnemic = nouEstat;
    }

    public void rebreDany(int dany) {
        vida -= dany;
        if (vida <= 0) {
            vida = 0;
            estatEnemic = EstatEnemic.MORT;
            actiu = false;
        }
    }

    public void tickVeri() {
        if (tornsVeri <= 0) return;
        rebreDany(3);
        tornsVeri--;
    }

    public void tickFoc() {
        if (tornsFoc <= 0) return;
        tornsFoc--;
    }

    public void tickGel() {
        if (tornsGel <= 0) return;
        tornsGel--;
    }
    
    public char getLletra(){
    return lletra;
    }
    public int getAtacEfectiu() {
        int penalitzacio = tornsFoc > 0 ? 2 : 0;
        return Math.max(1, atac - penalitzacio);
    }

    public int getDefensaEfectiva() {
        return tornsGel > 0 ? 0 : 0; //enemics de moment no tenen defensa base
    }

    public void setTornsVeri(int t) { tornsVeri = t; }
    public void setTornsFoc(int t)  { tornsFoc = t; }
    public void setTornsGel(int t)  { tornsGel = t; }

    public int getTornsVeri() { return tornsVeri; }
    public int getTornsFoc()  { return tornsFoc; }
    public int getTornsGel()  { return tornsGel; }

    // getters i setters

    public EstatEnemic getEstatEnemic() { // getEstatEnemic es perque retorna l'estat actual de la màquina d'estats
        return estatEnemic;
    }

    public int getVida() { // getVida es perque retorna la vida actual de l'enemic
        return vida;
    }

    public boolean esMort() {
        return vida <= 0;
    }

    public int getAtac() { return atac; } //necessari pel sistema de combat

    public int getVidaMaxima() { return vidaMaxima; }
}
