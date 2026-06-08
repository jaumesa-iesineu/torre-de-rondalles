package com.iessineu.rondalles.joc;

import java.util.HashSet;
import java.util.Set;

// símbols especials del mapa (parets, portes, escales...)
// es carreguen des del game.json i es consulten de forma estàtica
public class Simbols {

    private static Set<Character> mur = new HashSet<>();
    private static Set<Character> portaTancada = new HashSet<>();
    private static Set<Character> portaOberta = new HashSet<>();
    private static Set<Character> portaBloquejada = new HashSet<>();
    private static Set<Character> spawnJugador = new HashSet<>();
    private static Set<Character> escalaBaix = new HashSet<>();
    private static Set<Character> escalaPuj = new HashSet<>();
    private static Set<Character> marcadorItem = new HashSet<>();
    private static Set<Character> marcadorPorta = new HashSet<>();
    private static Set<Character> marcadorNpc = new HashSet<>();

    public static void inicialitza(ConfigGame.SimbolsConfig cfg) {
        if (cfg == null) {
            cfg = new ConfigGame.SimbolsConfig();
        }
        mur = new HashSet<>(cfg.mur);
        portaTancada = new HashSet<>(cfg.portaTancada);
        portaOberta = new HashSet<>(cfg.portaOberta);
        portaBloquejada = new HashSet<>(cfg.portaBloquejada);
        spawnJugador = new HashSet<>(cfg.spawnJugador);
        escalaBaix = new HashSet<>(cfg.escalaBaix);
        escalaPuj = new HashSet<>(cfg.escalaPuj);
        marcadorItem = new HashSet<>(cfg.marcadorItem);
        marcadorPorta = new HashSet<>(cfg.marcadorPorta);
        marcadorNpc = new HashSet<>(cfg.marcadorNpc);
    }

    private static boolean te(Set<Character> conjunt, char c) {
        return conjunt.contains(c);
    }

    public static boolean esMur(char c) { return te(mur, c); }
    public static boolean esPortaTancada(char c) { return te(portaTancada, c); }
    public static boolean esPortaOberta(char c) { return te(portaOberta, c); }
    public static boolean esPortaBloquejada(char c) { return te(portaBloquejada, c); }
    public static boolean esSpawnJugador(char c) { return te(spawnJugador, c); }
    public static boolean esEscalaBaix(char c) { return te(escalaBaix, c); }
    public static boolean esEscalaPuj(char c) { return te(escalaPuj, c); }
    public static boolean esMarcadorItem(char c) { return te(marcadorItem, c); }
    public static boolean esMarcadorPorta(char c) { return te(marcadorPorta, c); }
    public static boolean esMarcadorNpc(char c) { return te(marcadorNpc, c); }

    //qualsevol tipus de porta
    public static boolean esPorta(char c) {
        return esPortaTancada(c) || esPortaOberta(c) || esPortaBloquejada(c);
    }

    //simbols que bloquegen es moviment (parets, portes tancades i bloquejades)
    public static boolean bloquejaMoviment(char c) {
        return esMur(c) || esPortaTancada(c) || esPortaBloquejada(c);
    }

    //simbols que bloquegen sa linia de visio
    public static boolean bloquejaVisio(char c) {
        return esMur(c) || esPortaTancada(c) || esPortaBloquejada(c);
    }
}
