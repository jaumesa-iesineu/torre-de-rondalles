/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iessineu.rondalles.joc;

import com.iessineu.rondalles.entitats.Enemic;
import com.iessineu.rondalles.entitats.Entitat;
import com.iessineu.rondalles.entitats.Jugador;
import com.iessineu.rondalles.inventari.ItemMapa;
import com.iessineu.rondalles.inventari.PocioVida;
import com.iessineu.rondalles.inventari.PocioVeri;
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

    private List<Enemic> enemics; //tots els enemics carregats al mapa
    private List<ItemMapa> itemsMapa = new ArrayList<>(); //items que hi ha al terra

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

        enemics = new ArrayList<>();

        carregaItemsMapa(); //escanejam les 'i' del mapa i posam els items

        estat = Estat.MON;
    }

    @Override
    protected void actualitza(KeyStroke tecla) {
        if (tecla.getKeyType() == KeyType.Escape || tecla.getKeyType() == KeyType.EOF) {
            corrent = false;
            return;
        }

        //tecles 1-9 per usar l'item del slot corresponent, també gasta torn
        if (tecla.getKeyType() == KeyType.Character) {
            char c = tecla.getCharacter();
            if (c >= '1' && c <= '9') {
                jugador.usaItem(c - '1'); //convertim el caràcter a índex 0-based
                jugador.tickVeri();
                if (jugador.esMort()) corrent = false;
                return;
            }
        }

        int nx = jugador.getX();
        int ny = jugador.getY();

        switch (tecla.getKeyType()) {
            case ArrowUp    -> ny--;
            case ArrowDown  -> ny++;
            case ArrowLeft  -> nx--;
            case ArrowRight -> nx++;
            default -> { return; } //si no és moviment no passa el torn
        }

        if (mapa.esPasable(nx, ny)) {
            jugador.setX(nx);
            jugador.setY(ny);
            jugador.setEstatJugador(Jugador.EstatJugador.MOVIMENT);

            recullItemSiNHiHa(nx, ny); //comprova si ha trepitjat un item

            jugador.tickVeri(); //el verí avança cada torn

            for (Enemic e : enemics) {
                if (e.isActiu()) e.actualitzaIA(jugador);
            }
        }

        if (jugador.esMort()) corrent = false;
    }

    private void recullItemSiNHiHa(int x, int y) { //si hi ha un item a (x,y) el recull
        ItemMapa trobat = null;
        for (ItemMapa im : itemsMapa) {
            if (im.getX() == x && im.getY() == y) { trobat = im; break; }
        }
        if (trobat == null) return;
        jugador.afegeixItem(trobat.getItem());
        mapa.setCella(x, y, '.'); //treu la 'i' del mapa
        itemsMapa.remove(trobat);
    }

    private void carregaItemsMapa() { //escana totes les 'i' del mapa i crea items alternats
        char[][] celles = mapa.getCelles();
        int comptador = 0;
        for (int y = 0; y < celles.length; y++) {
            for (int x = 0; x < celles[y].length; x++) {
                if (celles[y][x] == 'i') {
                    //alternam vida i verí perquè hi hagi varietat
                    if (comptador % 2 == 0) itemsMapa.add(new ItemMapa(x, y, new PocioVida(30)));
                    else itemsMapa.add(new ItemMapa(x, y, new PocioVeri(5)));
                    comptador++;
                }
            }
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