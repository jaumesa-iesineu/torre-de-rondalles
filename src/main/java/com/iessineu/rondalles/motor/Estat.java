/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iessineu.rondalles.motor;

/**
 *
 * @author jaume, dani, sergi, kanhai i pere
 */
public enum Estat {
    //pantalla inicial, menú principal
    MENU,
    //explorant el mapa, el món semiobert
    MON_SEMIOBERT,
    //quan s'inicia un combat (futur)
    COMBAT,
    //quan s'obre l'inventari (futur)
    INVENTARI,
    //quan parles amb un npc (futur)
    DIALEG,
    //el jugador ha mort, pantalla de mort
    MORT,
    //permadeath: fi de la partida
    GAME_OVER
}
