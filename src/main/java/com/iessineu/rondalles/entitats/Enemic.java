/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iessineu.rondalles.entitats;

import com.googlecode.lanterna.TextColor;
import com.iessineu.rondalles.mapa.Mapa;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jaume, dani, sergi, kanhai i pere
 */
public abstract class Enemic extends Entitat { // extends Entitat es perque extends la classe Entitat

    //color i art carregats des del game.json; null = usa els valors hardcoded de la subclasse
    protected TextColor colorDef = null;
    protected String[] artAscii = null;

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
    private int tornsFoc = 0;
    private int tornsGel = 0;

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
    public abstract void actualitzaIA(Jugador jugador, Mapa mapa);

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

    protected double calculaDistanciaAlJugador(Jugador jugador) {
        int dx = jugador.getX() - this.x;
        int dy = jugador.getY() - this.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    // compatibilitat amb el nom antic usat a les subclasses
    protected double distanciaAl(Jugador jugador) {
        return calculaDistanciaAlJugador(jugador);
    }

    // mou l'enemic un pas cap a la casella (casellaDesti_x, casellaDesti_y) evitant parets.
    // prova primer en diagonal, després sols horitzontal, després sols vertical.
    // mai entra a la casella del jugador — el xoc el gestiona Joc.java.
    protected void mouUnPasCapA(int casellaDesti_x, int casellaDesti_y, Mapa mapa, Jugador jugador) {
        int direccioHoritzontal = Integer.signum(casellaDesti_x - this.x);
        int direccioVertical = Integer.signum(casellaDesti_y - this.y);

        int novaX_solsHoritzontal = this.x + direccioHoritzontal;
        int novaY_solsHoritzontal = this.y;
        boolean casellaHoritzontalOcupadaPerJugador = (novaX_solsHoritzontal == jugador.getX() && novaY_solsHoritzontal == jugador.getY());
        if (direccioHoritzontal != 0 && !casellaHoritzontalOcupadaPerJugador && mapa.esPasable(novaX_solsHoritzontal, novaY_solsHoritzontal)) { // si la direcció horitzontal no es 0 i la casella horitzontal no es ocupada per el jugador i es pasable, movem l'enemic a la casella horitzontal
            this.x = novaX_solsHoritzontal;
            return;
        }

        int novaX_solsVertical = this.x;
        int novaY_solsVertical = this.y + direccioVertical;
        boolean casellaVerticalOcupadaPerJugador = (novaX_solsVertical == jugador.getX() && novaY_solsVertical == jugador.getY());
        if (direccioVertical != 0 && !casellaVerticalOcupadaPerJugador && mapa.esPasable(novaX_solsVertical, novaY_solsVertical)) { // si la direcció vertical no es 0 i la casella vertical no es ocupada per el jugador i es pasable, movem l'enemic a la casella vertical
            this.y = novaY_solsVertical;
        }
    }

    // compatibilitat amb el nom antic usat a les subclasses
    protected void mouCap(int tx, int ty, Mapa mapa, Jugador jugador) {
        mouUnPasCapA(tx, ty, mapa, jugador);
    }

    // A* — cerca el camí més curt fins a (casellaDesti_x, casellaDesti_y) evitant parets.
    // retorna les coordenades del PRIMER PAS del camí, o {-1,-1} si no hi ha camí.
    protected int[] cercaCamiAmbAEstrella(int casellaDesti_x, int casellaDesti_y, Mapa mapa) {
        record CasellaDelCami(int x, int y) {

        }

        int casellaOrigen_x = this.x; // casella d'origen
        int casellaOrigen_y = this.y; // casella d'origen
        if (casellaOrigen_x == casellaDesti_x && casellaOrigen_y == casellaDesti_y) {
            return new int[]{-1, -1};
        }

        Map<CasellaDelCami, CasellaDelCami> casellaPare = new HashMap<>(); // casella pare
        Map<CasellaDelCami, Integer> costRealDesDeOrigen = new HashMap<>(); // cost real des de l'origen
        List<CasellaDelCami> casellesObertes = new ArrayList<>(); // caselles obertes

        CasellaDelCami casellaInici = new CasellaDelCami(casellaOrigen_x, casellaOrigen_y); // casella inici
        CasellaDelCami casellaMeta = new CasellaDelCami(casellaDesti_x, casellaDesti_y); // casella meta
        casellaPare.put(casellaInici, null); // casella pare
        costRealDesDeOrigen.put(casellaInici, 0); // cost real des de l'origen
        casellesObertes.add(casellaInici); // caselles obertes

        int[][] direccionsPossibles = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}}; // direccions possibles

        while (!casellesObertes.isEmpty()) { // mentre hi hagi caselles obertes
            // tria la casella amb menor cost total estimat (f = costReal + heuristica)
            casellesObertes.sort((casellaA, casellaB) -> {
                int costTotalEstimatA = costRealDesDeOrigen.get(casellaA) + Math.abs(casellaA.x() - casellaDesti_x) + Math.abs(casellaA.y() - casellaDesti_y); // cost total estimat de la casella A
                int costTotalEstimatB = costRealDesDeOrigen.get(casellaB) + Math.abs(casellaB.x() - casellaDesti_x) + Math.abs(casellaB.y() - casellaDesti_y); // cost total estimat de la casella B
                return costTotalEstimatA - costTotalEstimatB;
            });
            CasellaDelCami casellaActual = casellesObertes.remove(0);

            if (casellaActual.equals(casellaMeta)) {
                // reconstruïm el camí remuntant pels pares fins a l'origen
                CasellaDelCami casellaReconstruint = casellaActual;
                while (casellaPare.get(casellaReconstruint) != null && !casellaPare.get(casellaReconstruint).equals(casellaInici)) // mentre la casella reconstruint no sigui la casella inici
                {
                    casellaReconstruint = casellaPare.get(casellaReconstruint); // reconstruïm el camí remuntant pels pares fins a l'origen
                }
                return new int[]{casellaReconstruint.x(), casellaReconstruint.y()};
            }

            int costRealCasellaActual = costRealDesDeOrigen.get(casellaActual);
            for (int[] direccio : direccionsPossibles) { // per cada direcció possible
                int casellaveina_x = casellaActual.x() + direccio[0]; // casella veïna x
                int casellaVeina_y = casellaActual.y() + direccio[1]; // casella veïna y
                CasellaDelCami casellaVeina = new CasellaDelCami(casellaveina_x, casellaVeina_y);
                if (!mapa.esPasable(casellaveina_x, casellaVeina_y) && !casellaVeina.equals(casellaMeta)) {
                    continue; // si la casella veïna no es pasable i no es la casella meta, continue
                }
                int nouCostReal = costRealCasellaActual + 1;
                if (nouCostReal < costRealDesDeOrigen.getOrDefault(casellaVeina, Integer.MAX_VALUE)) { // si el nou cost real es menor que el cost real des de l'origen, actualitzem el cost real des de l'origen
                    costRealDesDeOrigen.put(casellaVeina, nouCostReal);
                    casellaPare.put(casellaVeina, casellaActual);
                    if (!casellesObertes.contains(casellaVeina)) {
                        casellesObertes.add(casellaVeina); // si la casella veïna no es troba en les caselles obertes, l'afegim
                    }
                }
            }
        }
        return new int[]{-1, -1}; // si no hi ha camí possible, retornem {-1, -1}
    }

    // compatibilitat amb el nom antic usat a les subclasses
    protected int[] aEstrella(int gx, int gy, Mapa mapa) {
        return cercaCamiAmbAEstrella(gx, gy, mapa);
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

    public char getLletra() {
        return lletra;
    }

    public int getAtacEfectiu() {
        int penalitzacio = tornsFoc > 0 ? 2 : 0;
        return Math.max(1, atac - penalitzacio);
    }

    public int getDefensaEfectiva() {
        return tornsGel > 0 ? 0 : 0; //enemics de moment no tenen defensa base
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

    public int getAtac() {
        return atac;
    } //necessari pel sistema de combat

    public int getVidaMaxima() {
        return vidaMaxima;
    }

    //aplica stats, color i art del game.json sobreescrivint els valors hardcoded
    public void aplicaDefinicio(int vida, int atac, int radi, int r, int g, int b, String[] art) {
        this.vida = vida;
        this.vidaMaxima = vida;
        this.atac = atac;
        this.radDeteccio = radi;
        this.colorDef = new TextColor.RGB(r, g, b);
        this.artAscii = art;
    }

    public String[] getArtAscii() { return artAscii; }
}
