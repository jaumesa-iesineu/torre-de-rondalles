/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iessineu.rondalles.mapa;

/**
 *
 * @author jaume, dani, sergi, kanhai i pere
 */
public class Mapa {

    //la graella de caràcters que representa el mapa
    //'#' = paret, '.' = terra, 'e' = enemic, 'i' = item, 'N' = npc
    private char[][] celles;

    //el nom del mapa, ve del fitxer .game
    private String nom;

    //dimensions en nombre de caselles
    private int amplada;
    private int alcada;

    public Mapa(char[][] celles, String nom) {
        this.celles = celles;
        this.nom = nom;
        this.alcada = celles.length;
        this.amplada = celles.length > 0 ? celles[0].length : 0;
    }

    //comprova si el jugador pot trepitjar aquella posició
    public boolean esPasable(int x, int y) {
        //si surt dels límits del mapa no pot passar
        if (x < 0 || y < 0 || x >= amplada || y >= alcada) return false;

        char c = celles[y][x];
        //les parets no es travessen, tot lo demés sí
        //quan hi ha una entitat (enemic, npc...) s'hi pot entrar per interactuar
        return c != '#';
    }

    public char[][] getCelles() { return celles; }
    public String getNom() { return nom; }
    public int getAmplada() { return amplada; }
    public int getAlcada() { return alcada; }
}
