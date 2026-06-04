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

    private int velocitat;
    private int evasio; // % de probabilitat d'esquivar un atac (0-100)

    private int visio;

    private EstatJugador estatJugador; //estat actual del jugador
    private int dirX = 0; // darrera direcció horitzontal (per Pinky)
    private int dirY = 1; // darrera direcció vertical (per Pinky)

    private Inventari inventari = new Inventari(); //el que du a sobre
    private int tornsVeri = 0;
    private int tornsFoc = 0; //baixa atac
    private int tornsGel = 0; //baixa defensa

    public Jugador(int x, int y) { // constructor de la classe Jugador
        super(x, y, '@');
        this.vidaMaxima = 100;
        this.vida = vidaMaxima;
        this.atac = 3;
        this.defensa = 0;
        this.pesMaxim = 50;
        this.pes = 0;
        this.velocitat = 5;
        this.evasio = 10;
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

    public enum Carrega {
        LLEUGER, NORMAL, PESAT
    }

    public Carrega categoriaCarrega() {
        double pct = (double) pes / pesMaxim;
        if (pct <= 0.50) {
            return Carrega.LLEUGER;
        }
        if (pct <= 0.80) {
            return Carrega.NORMAL;
        }
        return Carrega.PESAT;
    }

    //funció que calcula sa velocitat depenent de es pes / pesMaxim
    public int velocitatEfectiva() {
        return switch (categoriaCarrega()) {
            case LLEUGER ->
                velocitat;
            case NORMAL ->
                Math.max(1, velocitat - 1);
            case PESAT ->
                Math.max(1, velocitat - 2);
        };
    }

    public int evasioEfectiva() {
        return categoriaCarrega() == Carrega.PESAT ? Math.max(0, evasio - 5) : evasio;
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

    public boolean esquiva() {
        int eva = evasioEfectiva();
        return eva > 0 && java.util.concurrent.ThreadLocalRandom.current().nextInt(100) < eva;
    }

    public void rebreDany(int dany) {
        int danyReal = Math.max(1, dany - defensa);
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
        inventari.afegeix(item);
        pes += item.getPes();
    }

    public String usaItem(int index) { //usa l'item del slot indicat (0-based); retorna nom o null
        if (index < 0 || index >= com.iessineu.rondalles.inventari.Inventari.MAX_SLOTS) {
            return null;
        }
        if (inventari.get(index) == null) {
            return null;
        }
        Item item = inventari.get(index);
        item.aplicaEfecte(this);
        pes -= item.getPes();
        inventari.elimina(index);
        return item.getNom();
    }

    public void tickVeri() {
        if (tornsVeri <= 0) {
            return;
        }
        rebreDany(3);
        tornsVeri--;
    }

    public void tickFoc() {
        if (tornsFoc <= 0) {
            return;
        }
        tornsFoc--;
    }

    public void tickGel() {
        if (tornsGel <= 0) {
            return;
        }
        tornsGel--;
    }

    public int getAtacTotal() {
        int penalitzacio = tornsFoc > 0 ? 2 : 0;
        return Math.max(1, atac + atacExtra - penalitzacio);
    }

    public int getDefensaTotal() {
        int penalitzacio = tornsGel > 0 ? 2 : 0;
        return Math.max(0, defensa + defensaExtra - penalitzacio);
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

    public int getEvasio() {
        return evasio;
    }

    public void setEvasio(int e) {
        evasio = e;
    }

    public int getVelocitat() {
        return velocitat;
    }

    public void setVelocitat(int v) {
        velocitat = v;
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

    public void setDir(int dx, int dy) {
        if (dx != 0 || dy != 0) {
            dirX = dx;
            dirY = dy;
        }
    }

    public int getDirX() {
        return dirX;
    }

    public int getDirY() {
        return dirY;
    }

    public void setVida(int v) {
        vida = Math.max(0, v);
    }

    public void setVidaMaxima(int v) {
        vidaMaxima = v;
    }

    public void setAtac(int a) {
        atac = a;
    }

    public void setDefensa(int d) {
        defensa = d;
    }

    public void setTornsVeri(int t) {
        tornsVeri = t;
    }

    public void setTornsFoc(int t) {
        tornsFoc = t;
    }

    public void setTornsGel(int t) {
        tornsGel = t;
    }

    public int getTornsVeri() {
        return tornsVeri;
    }

    public int getTornsFoc() {
        return tornsFoc;
    }

    public int getTornsGel() {
        return tornsGel;
    }

    public Inventari getInventari() {
        return inventari;
    }

    @Override
    public TextColor getColor() {
        return TextColor.ANSI.GREEN_BRIGHT;
    }

}
