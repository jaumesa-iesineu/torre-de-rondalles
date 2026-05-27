/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iessineu.rondalles.entitats;

import com.googlecode.lanterna.TextColor;
import com.iessineu.rondalles.inventari.Inventari;
import com.iessineu.rondalles.inventari.Item;

/**
 *
 * @author jaume, dani, sergi, kanhai i pere
 */
public class Jugador extends Entitat { // extends Entitat es perque extends la classe Entitat

    //màquina d'estats del jugador
    public enum EstatJugador {
        IDLE,
        MOVIMENT,
        COMBATINT,
        MORT
    }

    private int vida; // vida actual del jugador
    private int vidaMaxima; // vida maxima del jugador

    private int atac; // atac base del jugador
    private int atacExtra; // atac afegit per l'arma equipada

    private int defensa; // defensa actual del jugador
    private int defensaExtra; // defensa afegida per l'armadura equipada

    private int pes; // pes actual del jugador
    private int pesMaxim; // pes maxim del jugador

    private int velocitat; // velocitat actual del jugador

    private int visio; //visio actual del jugador

    private EstatJugador estatJugador; //estat actual del jugador

    private Inventari inventari = new Inventari(); //el que du a sobre
    private int tornsVeri = 0; //quants torns li queden de verí

    public Jugador(int x, int y) { // constructor de la classe Jugador
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
        return atac + atacExtra;
    }

    public int getDefensaTotal() {
        return defensa + defensaExtra;
    }

    public void setAtacExtra(int atacExtra) {
        this.atacExtra = atacExtra;
    }

    public void setDefensaExtra(int defensaExtra) {
        this.defensaExtra = defensaExtra;
    }

    public boolean esMort() {
        return vida <= 0;
    }

    public void rebreDany(int dany) {
        int danyReal = Math.max(1, dany - defensa); //la defensa absorb part del cop
        vida -= danyReal;
        if (vida <= 0) {
            vida = 0;
            estatJugador = EstatJugador.MORT;
        }
    }

    public void curar(int quantitat) { //recupera vida sense passar del màxim
        vida = Math.min(vidaMaxima, vida + quantitat);
    }

    public void afegeixItem(Item item) { //recull un item del terra
        inventari.afegir(item);
        pes += item.getPes();
    }

    public void usaItem(int index) { //usa l'item del slot indicat (0-based)
        if (index < 0 || index >= inventari.mida()) return;
        Item item = inventari.get(index);
        item.aplicaEfecte(this);
        pes -= item.getPes();
        inventari.elimina(index);
    }

    public void tickVeri() { //cada torn que passa amb verí, fa mal
        if (tornsVeri <= 0) return;
        rebreDany(3);
        tornsVeri--;
    }

    // getters i setters

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

    public void setTornsVeri(int t) { tornsVeri = t; }

    public int getTornsVeri() { return tornsVeri; }

    public Inventari getInventari() { return inventari; }

    @Override
    public TextColor getColor() {
        return TextColor.ANSI.GREEN_BRIGHT;
    }

}
