package com.iessineu.rondalles.joc;

// controls del joc carregats des del game.json
public class Controls {

    private static char atacar = 'a';
    private static char fugir = 'f';
    private static char inventari = 'i';
    private static char interactuar = 'e';

    public static void inicialitza(ConfigGame.ControlsConfig cfg) {
        if (cfg == null) return;
        atacar = cfg.atacar;
        fugir = cfg.fugir;
        inventari = cfg.inventari;
        interactuar = cfg.interactuar;
    }

    public static boolean esAtacar(char c) { return Character.toLowerCase(c) == atacar; }
    public static boolean esFugir(char c) { return Character.toLowerCase(c) == fugir; }
    public static boolean esInventari(char c) { return Character.toLowerCase(c) == inventari; }
    public static boolean esInteractuar(char c) { return Character.toLowerCase(c) == interactuar; }
}
