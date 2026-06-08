package com.iessineu.rondalles.motor;

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
    ENIGMA, //pantalla de l'endevinalla del NPC
    SELECCIO_PERSONATGE, //tria de tipus de personatge
    CREACIO_PERSONATGE, //distribució de punts per al personatge personalitzat
    DIALEG //pantalla de dialeg amb animacio typewriter
}
