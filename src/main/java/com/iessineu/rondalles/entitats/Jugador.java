/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iessineu.rondalles.entitats;

/**
 *
 * @author jaume, dani, sergi, kanhai i pere
 */
public class Jugador extends Entitat {

    //màquina d'estats del jugador, seguint el GDD
    public enum EstatJugador {
        //idle: esperam que premi una tecla
        IDLE,
        //moviment: s'acaba de moure
        MOVIMENT,
        //combatint: està en combat amb un enemic
        COMBATINT,
        //mort: hp ha arribat a 0
        MORT
    }

    //punts de vida actuals i màxims
    private int hp;
    private int hpMax;

    //dany base sense equipament
    private int atac;

    //reducció de dany que rep
    private int defensa;

    //el pes que du a sobre (afecta quant de ràpid es mou)
    private int pes;
    private int pesMax;

    //velocitat base, sense pes
    private int velocitat;

    //la visió afecta la boira de guerra (quantes caselles recorda)
    private int visio;

    //estat actual
    private EstatJugador estatJugador;

    public Jugador(int x, int y) {
        super(x, y, '@');
        this.hpMax = 100;
        this.hp = hpMax;
        this.atac = 3;
        this.defensa = 0;
        this.pesMax = 50;
        this.pes = 0;
        this.velocitat = 5;
        this.visio = 14;
        this.estatJugador = EstatJugador.IDLE;
    }

    @Override
    public void actualitza() {
        //aquí hi hauran els efectes per torn, verí, regeneració, fam...
    }

    @Override
    public void interactua(Jugador jugador) {
        //el jugador no interactua amb ell mateix
    }

    //tres nivells de càrrega: lleuger (0-50%), normal (51-80%), pesat (>80%)
    //com més pesat, menys moviments per torn
    public int velocitatEfectiva() {
        double percentatge = (double) pes / pesMax;
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
        return hp <= 0;
    }

    public void rebreDany(int dany) {
        //la defensa redueix el dany rebut
        int danyReal = Math.max(1, dany - defensa);
        hp -= danyReal;
        if (hp <= 0) {
            hp = 0;
            estatJugador = EstatJugador.MORT;
        }
    }

    public int getHp() { return hp; }
    public int getHpMax() { return hpMax; }
    public int getAtac() { return atac; }
    public int getDefensa() { return defensa; }
    public int getPes() { return pes; }
    public int getPesMax() { return pesMax; }
    public int getVisio() { return visio; }
    public EstatJugador getEstatJugador() { return estatJugador; }
    public void setEstatJugador(EstatJugador e) { this.estatJugador = e; }
}
