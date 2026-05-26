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
    MON, //bucle principal: jugador explora el mapa per torns
    COMBAT, //pantalla de combat per torns, mapa congelat
    GAME_OVER, //permadeath: jugador mort, s'esborra la partida de SQLite
    VICTORIA //jugador ha derrotat Es Drac a la planta 5
}
