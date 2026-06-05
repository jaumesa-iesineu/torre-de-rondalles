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
    MENU, //pantalla inicial, menú principal
    MENU_INICIAL, //benvinguda abans de començar a jugar
    PAUSA, //menú de pausa, botons de sempre
    MON, //el jugador es mou pel mapa per torns
    COMBAT, //combat per torns, es mapa es queda congelat
    GAME_OVER, //el jugador ha mort, s'esborra sa partida
    INVENTARI, //inventari superposat damunt es mapa, perds un torn
    VICTORIA, //has guanyat! es drac ha caigut
    COMERCIANT, //pantalla de comerç amb es NPC
    ENIGMA //pantalla de l'endevinalla del NPC
}
