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
public class Jugador extends Entitat {

    //màquina d'estats del jugador
    public enum EstatJugador {
        IDLE,
        MOVIMENT,
        COMBATINT,
        MORT
    }

    private int vida;
    private int vidaMaxima;

    private int atac;

    private int defensa;

    private int pes;
    private int pesMaxim;

    private int velocitat;

    private int visio;

    private EstatJugador estatJugador;

    public Jugador(int x, int y) {
        super(x, y, '@');
        this.vidaMaxima = 100;
        this.vida = vidaMaxima;
        this.atac = 3;
        this.defensa = 0;
        this.pesMaxim = 50;
        this.pes = 0;
        this.velocitat = 5;
        this.visio = 5;
        this.estatJugador = EstatJugador.IDLE;
    }

    @Override
    public void actualitza() {
        //per actualitzar l'estat del jugador per exemple si té regeneració
    }

    @Override
    public void interactua(Jugador jugador) {
        //per interactuar amb cofres etc
    }

    //funció que calcula sa velocitat depenent de es pes / pesMaxim
    public int velocitatEfectiva() {
        double percentatge = (double) pes / pesMaxim;
        if (percentatge <= 0.50) return velocitat;
        if (percentatge <= 0.80) return Math.max(1, velocitat - 1);
        return Math.max(1, velocitat - 2);
    }

    //les stats reals sumen base + equipament (quan tinguem equipament)
    public int getAtacTotal() {
        //quan tinguem arma equipada aquí sumarem el seu atac
        return atac;
    }

    public int getDefensaTotal() {
        //quan tinguem armadura aquí sumarem la seva defensa
        return defensa;
    }

    public boolean esMort() {
        return vida <= 0;
    }

    public void rebreDany(int dany) {
        //la defensa redueix el dany rebut
        int danyReal = Math.max(1, dany - defensa);
        vida -= danyReal;
        if (vida <= 0) {
            vida = 0;
            estatJugador = EstatJugador.MORT;
        }
    }

    public int getVida() {
        return vida;
    }
    
    public int getVidaMaxima() {
        return vidaMaxima;
    }
    
    public int getAtac() {
        return atac;
    }
    
    public int getDefensa() {
        return defensa;
    }
    
    public int getPes() {
        return pes;
    }
    
    public int getpesMaxim() {
        return pesMaxim;
    }
    
    public int getVisio() {
        return visio;
    }
    
    public EstatJugador getEstatJugador() {
        return estatJugador;
    }
    
    public void setEstatJugador(EstatJugador e) {
        this.estatJugador = e;
    }

    @Override
    public TextColor getColor() {
        return TextColor.ANSI.GREEN_BRIGHT;
    }

}
