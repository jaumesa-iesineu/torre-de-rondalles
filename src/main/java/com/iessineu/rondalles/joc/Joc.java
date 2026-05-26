/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iessineu.rondalles.joc;

import com.iessineu.rondalles.entitats.Enemic;
import com.iessineu.rondalles.entitats.Entitat;
import com.iessineu.rondalles.entitats.Jugador;
import com.iessineu.rondalles.mapa.CarregadorMapa;
import com.iessineu.rondalles.mapa.Mapa;
import com.iessineu.rondalles.motor.Estat;
import com.iessineu.rondalles.motor.Motor;
import com.iessineu.rondalles.motor.Renderitzador;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jaume, dani, sergi, kanhai i pere
 */
public class Joc extends Motor {

    //el mapa actual carregat des fitxer .game
    private Mapa mapa;

    private Jugador jugador;

    //tots els enemics del mapa
    //potser quan afegim npcs i items haurem de separar-ho en llistes o usar una sola de Entitat
    private List<Enemic> enemics;

    //el fitxer .game que hem de carregar
    private String fitxerMapa;

    public Joc(String fitxerMapa) {
        this.fitxerMapa = fitxerMapa;
    }

    @Override
    protected void init() throws Exception {
        //creim el renderitzador que gestiona la pantalla
        renderer = new Renderitzador();

        //carregam el mapa des del fitxer
        mapa = CarregadorMapa.carrega(fitxerMapa);

        //posam el jugador a la primera posició lliure del mapa
        //la posició inicial hauria de venir del .game, de moment cercam la primera casella lliure
        jugador = new Jugador(trobaInicialX(), trobaInicialY());

        //de moment la llista d'enemics és buida, els crearem quan llegim el .game
        enemics = new ArrayList<>();

        //els enemics haurien de tenir oïda (activar-se per distància) i visió (línia recta bloquejada per parets)
        //quan no detectin el jugador, patrullen entre els waypoints definits al .game

        estat = Estat.MON;
    }

    @Override
    protected void actualitza(KeyStroke tecla) { // reacció des joc a les tecles
        //escape tanca es joc
        if (tecla.getKeyType() == KeyType.Escape || tecla.getKeyType() == KeyType.EOF) {
            corrent = false;
            return;
        }

        //calcul posició segons tecla
        int nx = jugador.getX();
        int ny = jugador.getY();

        switch (tecla.getKeyType()) {
            case ArrowUp    -> ny--;
            case ArrowDown  -> ny++;
            case ArrowLeft  -> nx--;
            case ArrowRight -> nx++;
            //si no és una fletxa no és el torn del jugador, no fem res
            default -> {
                return;
            }
        }

        //comprovam si la casella és una paret o si s'hi pot passar
        if (mapa.esPasable(nx, ny)) {
            jugador.setX(nx);
            jugador.setY(ny);
            jugador.setEstatJugador(Jugador.EstatJugador.MOVIMENT);

            //el jugador s'ha mogut per tant ara és el torn dels enemics
            //cada enemic fa una acció basada en la seva IA
            for (Enemic e : enemics) {
                if (e.isActiu()) {
                    e.actualitzaIA(jugador);
                }
            }
        }

        //si el jugador ha mort acabam es joc
        if (jugador.esMort()) {
            corrent = false;
        }
    }

    @Override
    protected void renderitza() { // renderitza la pantalla
        try {
            //passam tots els enemics com a llista d'entitats al renderitzador
            List<Entitat> totes = new ArrayList<>(enemics);
            // CANVI: afegit "jugador" com a paràmetre per poder pintar el HUD
            renderer.dibuixa(mapa, jugador.getX(), jugador.getY(), totes, jugador);
        } catch (IOException ex) {
            //errorr
            corrent = false;
        }
    }

    //cerca la primera casella de terra on posar es jugador
    private int trobaInicialX() { // cerca la primera casella de terra on posar es jugador (x)
        char[][] celles = mapa.getCelles();
        for (int y = 0; y < celles.length; y++)
            for (int x = 0; x < celles[y].length; x++)
                if (celles[y][x] == '.') return x;
        return 1;
    }

    private int trobaInicialY() { // cerca la primera casella de terra on posar es jugador (y)
        char[][] celles = mapa.getCelles();
        for (int y = 0; y < celles.length; y++)
            for (int x = 0; x < celles[y].length; x++)
                if (celles[y][x] == '.') return y;
        return 1;
    }
}