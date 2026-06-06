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
public class Jugador extends Entitat { //el jugador tambe es una entitat

    //maquina d'estats del jugador
    public enum EstatJugador {
        IDLE,
        MOVIMENT,
        COMBATINT,
        MORT
    }

    private int vida; //vida actual del jugador
    private int vidaMaxima; //vida maxima del jugador

    private int atac; //atac base del jugador
    private int atacExtra; //atac afegit per l'arma equipada

    private int defensa; //defensa actual del jugador
    private int defensaExtra; //defensa afegida per l'armadura equipada

    private int pes; //pes actual del jugador
    private int pesMaxim; //pes maxim del jugador

    private int velocitat;
    private int evasio; //% de probabilitat d'esquivar un atac (0-100)

    private int visio;

    private EstatJugador estatJugador; //estat actual del jugador
    private int dirX = 0; //darrera direccio horitzontal (per Pinky)
    private int dirY = 1; //darrera direccio vertical (per Pinky)

    private Inventari inventari;
    private int tornsVeri = 0;
    private int tornsFoc = 0; //baixa atac
    private int tornsGel = 0; //baixa defensa
    private String passiu = "";

    public Jugador(int x, int y) {
        this(x, y, 100, 3, 5, 10, 50, 4);
    }

    public Jugador(int x, int y, int vidaMaxima, int atac, int velocitat, int evasio, int pesMaxim) {
        this(x, y, vidaMaxima, atac, velocitat, evasio, pesMaxim, 4);
    }

    public Jugador(int x, int y, int vidaMaxima, int atac, int velocitat, int evasio, int pesMaxim, int maxSlots) {
        super(x, y, '@');
        this.vidaMaxima = vidaMaxima;
        this.vida = vidaMaxima;
        this.atac = atac;
        this.defensa = 0;
        this.pesMaxim = pesMaxim;
        this.pes = 0;
        this.velocitat = velocitat;
        this.evasio = evasio;
        this.visio = 5;
        this.estatJugador = EstatJugador.IDLE;
        this.inventari = new Inventari(maxSlots);
    }

    @Override
    public void actualitza() {
        //per si algun dia afegim regeneracio o algo
    }

    @Override
    public void interactua(Jugador jugador) {
        //per interactuar amb cofres i truges
    }

    public enum Carrega {
        LLEUGER, NORMAL, PESAT
    }

    public Carrega categoriaCarrega() {
        if ("lleugera_vent".equals(passiu)) return Carrega.LLEUGER;
        double pct = (double) pes / pesMaxim;
        if (pct <= 0.50) return Carrega.LLEUGER;
        if (pct <= 0.80) return Carrega.NORMAL;
        return Carrega.PESAT;
    }

    public int velocitatEfectiva() {
        return switch (categoriaCarrega()) {
            case LLEUGER -> velocitat;
            case NORMAL  -> Math.max(1, velocitat - 1);
            case PESAT   -> Math.max(1, velocitat - 2);
        };
    }

    public int evasioEfectiva() {
        int base = categoriaCarrega() == Carrega.PESAT ? Math.max(0, evasio - 5) : evasio;
        if ("lleugera_vent".equals(passiu)) base = Math.min(100, base + 20);
        return base;
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
        if ("sort_rondalla".equals(passiu) && java.util.concurrent.ThreadLocalRandom.current().nextInt(100) < 15) return true;
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

    public void curar(int quantitat) { //recupera vida sense passar del maxim
        vida = Math.min(vidaMaxima, vida + quantitat);
    }

    public void afegeixItem(Item item) { //recull un item de sa terra
        inventari.afegeix(item);
        pes += item.getPes();
    }

    public String usaItem(int index) { //usa l'item del slot indicat (0-based); retorna nom o null
        if (index < 0 || index >= inventari.getMaxSlots()) {
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
        if (tornsVeri <= 0) return;
        rebreDany(3);
        if (!"pocions_infinites".equals(passiu)) tornsVeri--;
    }

    public void tickFoc() {
        if (tornsFoc <= 0) return;
        if (!"pocions_infinites".equals(passiu)) tornsFoc--;
    }

    public void tickGel() {
        if (tornsGel <= 0) return;
        if (!"pocions_infinites".equals(passiu)) tornsGel--;
    }

    public int getAtacTotal() {
        int penalitzacio = tornsFoc > 0 ? 2 : 0;
        return Math.max(1, atac + atacExtra - penalitzacio);
    }

    public int getDefensaTotal() {
        int penalitzacio = tornsGel > 0 ? 2 : 0;
        return Math.max(0, defensa + defensaExtra - penalitzacio);
    }

    //getters i setters
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

    public void setPassiu(String passiu) { this.passiu = passiu != null ? passiu : ""; }
    public String getPassiu() { return passiu; }

    public Inventari getInventari() {
        return inventari;
    }

    @Override
    public TextColor getColor() {
        return TextColor.ANSI.GREEN_BRIGHT;
    }

}
