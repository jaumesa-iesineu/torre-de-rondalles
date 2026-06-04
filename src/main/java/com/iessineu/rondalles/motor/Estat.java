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
    MENU_INICIAL, //pantalla de benvinguda abans de començar a jugar
    PAUSA, //menú de pausa (reanudar, guardar, sortir)
    MON, //bucle principal: jugador explora el mapa per torns
    COMBAT, //pantalla de combat per torns, mapa congelat
    GAME_OVER, //permadeath: jugador mort, s'esborra la partida de SQLite
    INVENTARI, //overlay d'inventari, mapa de fons, costa 1 torn
    VICTORIA, //jugador ha derrotat Es Drac a la planta 5
    COMERCIANT, // overlay de comerç amb el NPC
    ENIGMA // pantalla d'enigma del NPC
}
