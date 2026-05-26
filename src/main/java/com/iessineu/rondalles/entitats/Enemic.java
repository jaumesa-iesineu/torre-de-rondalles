/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iessineu.rondalles.entitats;

/**
 *
 * @author jaume, dani, sergi, kanhai i pere
 */
public abstract class Enemic extends Entitat {

    //màquina d'estats de l'enemic
    //cada estat defineix un comportament diferent
    public enum EstatEnemic {
        //patrullant: es mou pel seu camí marcat al fitxer .game
        PATRULLANT,
        //alerta: ha sentit qualque cosa però no veu el jugador
        //es gira cap al so i mira
        ALERTA,
        //perseguint: ha vist el jugador i va directament cap a ell
        PERSEGUINT,
        //atacant: el jugador és al costat i li pega cada torn
        ATACANT,
        //atordit: ha rebut un cop fort, es salta un torn sencer
        ATORDIT,
        //fugint: li queda poca vida i intenta escapar del jugador
        FUGINT,
        //mort: vida és 0, l'entitat queda inactiva
        MORT
    }

    protected int vida;
    protected int vidaMaxima;
    protected int atac;

    //fins a quina distància detecta el jugador
    protected int radDeteccio;

    //estat actual de la màquina
    protected EstatEnemic estatEnemic;

    public Enemic(int x, int y, char simbol, int vida, int atac, int radDeteccio) {
        super(x, y, simbol);
        this.vidaMaxima = vida;
        this.vida = vida;
        this.atac = atac;
        this.radDeteccio = radDeteccio;
        this.estatEnemic = EstatEnemic.PATRULLANT;
    }

    //cada tipus d'enemic té la seva pròpia IA
    //es crida quan el jugador fa un moviment (és el seu torn)
    public abstract void actualitzaIA(Jugador jugador);

    @Override
    public void actualitza() {
        //haurem de fer que actualitza() tengui accés al jugador
    }

    @Override
    public void interactua(Jugador jugador) {
        //quan el jugador entra a la casella de l'enemic, pega
        //de moment simplement resta vida, el combat real el farem més endavant
        jugador.rebreDany(atac);
        estatEnemic = EstatEnemic.ATACANT;
    }

    //distància euclidiana fins al jugador
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
        } else if (vida < vidaMaxima / 4) {
            //si queda amb menys d'un quart de vida, fuig
            estatEnemic = EstatEnemic.FUGINT;
        }
    }

    public EstatEnemic getEstatEnemic() {
        return estatEnemic;
    }

    public int getVida() {
        return vida;
    }

    public boolean esMort() {
        return vida <= 0;
    }
}
