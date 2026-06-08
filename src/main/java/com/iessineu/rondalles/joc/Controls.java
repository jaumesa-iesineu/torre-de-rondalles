package com.iessineu.rondalles.joc;

// controls del joc carregats des del game.json
public class Controls {

    private static char atacar = 'a';
    private static char fugir = 'f';
    private static char esquivar = 'd';
    private static char inventari = 'i';
    private static char interactuar = 'e';
    private static char mourePes = 'm';

    public static void inicialitza(ConfigGame.ControlsConfig cfg) {
        if (cfg == null) return;
        atacar = cfg.atacar;
        fugir = cfg.fugir;
        esquivar = cfg.esquivar;
        inventari = cfg.inventari;
        interactuar = cfg.interactuar;
        mourePes = cfg.mourePes;
    }

    public static boolean esAtacar(char c) { return Character.toLowerCase(c) == atacar; }
    public static boolean esFugir(char c) { return Character.toLowerCase(c) == fugir; }
    public static boolean esEsquivar(char c) { return Character.toLowerCase(c) == esquivar; }
    public static boolean esInventari(char c) { return Character.toLowerCase(c) == inventari; }
    public static boolean esInteractuar(char c) { return Character.toLowerCase(c) == interactuar; }
    public static boolean esMourePes(char c)    { return Character.toLowerCase(c) == mourePes; }
}
