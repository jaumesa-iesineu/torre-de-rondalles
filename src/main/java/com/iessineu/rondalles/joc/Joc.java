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

    //el mapa actual carregat del fitxer .game
    private Mapa mapa;

    //el jugador
    private Jugador jugador;

    //tots els enemics del mapa
    //TODO: quan afegim npcs i items, haurem de tenir llistes separades o una llista de Entitat
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
        //TODO: treure la posició inicial del fitxer .game
        jugador = new Jugador(trobaInicialX(), trobaInicialY());

        //de moment la llista d'enemics és buida
        //TODO: crear els enemics llegint el fitxer .game
        enemics = new ArrayList<>();

        //TODO (punt 7): oïda dels enemics basada en distància (activació per proximitat)
        //TODO (punt 8): visió dels enemics en línia recta bloquejada per murs (line-of-sight)
        //TODO (punt 8): waypoints: quan no detecten el jugador, patrullen entre punts del .game

        estat = Estat.MON_SEMIOBERT;
    }

    @Override
    protected void actualitza(KeyStroke tecla) {
        //si l'usuari tanca la finestra o prem escape, aturem el joc
        if (tecla.getKeyType() == KeyType.Escape || tecla.getKeyType() == KeyType.EOF) {
            corrent = false;
            return;
        }

        //calculam la nova posició segons la tecla premuda
        int nx = jugador.getX();
        int ny = jugador.getY();

        switch (tecla.getKeyType()) {
            case ArrowUp    -> ny--;
            case ArrowDown  -> ny++;
            case ArrowLeft  -> nx--;
            case ArrowRight -> nx++;
            //si no és una fletxa no és el torn del jugador, no fem res
            default -> { return; }
        }

        //comprovam si la casella és passable
        if (mapa.esPasable(nx, ny)) {
            jugador.setX(nx);
            jugador.setY(ny);
            jugador.setEstatJugador(Jugador.EstatJugador.MOVIMENT);

            //el jugador s'ha mogut: ara és el torn dels enemics
            //cada enemic fa una acció basada en la seva IA
            for (Enemic e : enemics) {
                if (e.isActiu()) {
                    e.actualitzaIA(jugador);
                }
            }
        }

        //si el jugador ha mort acabam el joc
        if (jugador.esMort()) {
            corrent = false;
        }
    }

    @Override
    protected void renderitza() {
        try {
            //passam tots els enemics com a llista d'entitats al renderitzador
            List<Entitat> totes = new ArrayList<>(enemics);
            renderer.dibuixa(mapa, jugador.getX(), jugador.getY(), totes);
        } catch (IOException ex) {
            //si hi ha un error de pantalla aturem el joc
            corrent = false;
        }
    }

    //cerca la primera casella de terra on posar el jugador
    private int trobaInicialX() {
        char[][] celles = mapa.getCelles();
        for (int y = 0; y < celles.length; y++)
            for (int x = 0; x < celles[y].length; x++)
                if (celles[y][x] == '.') return x;
        return 1;
    }

    private int trobaInicialY() {
        char[][] celles = mapa.getCelles();
        for (int y = 0; y < celles.length; y++)
            for (int x = 0; x < celles[y].length; x++)
                if (celles[y][x] == '.') return y;
        return 1;
    }
}
