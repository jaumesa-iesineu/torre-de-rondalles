package com.iessineu.rondalles.joc;

public class Mecaniques {

    public static int danyVeri = 3;
    public static int penalitzacioFoc = 2;
    public static int penalitzacioGel = 2;
    public static double llindCarregaNormal = 0.50;
    public static double llindCarregaPesat = 0.80;
    public static int penalitzacioEvasioCarrega = 5;
    public static int penalitzacioVelNormal = 1;
    public static int penalitzacioVelPesat = 2;
    public static int pctSortRondalla = 15;
    public static int bonusEvasioLleugera = 20;

    public static void inicialitza(ConfigGame.Configuracio cfg) {
        if (cfg == null) return;
        danyVeri = cfg.danyVeri;
        penalitzacioFoc = cfg.penalitzacioFoc;
        penalitzacioGel = cfg.penalitzacioGel;
        llindCarregaNormal = cfg.llindCarregaNormal;
        llindCarregaPesat = cfg.llindCarregaPesat;
        penalitzacioEvasioCarrega = cfg.penalitzacioEvasioCarrega;
        penalitzacioVelNormal = cfg.penalitzacioVelNormal;
        penalitzacioVelPesat = cfg.penalitzacioVelPesat;
        pctSortRondalla = cfg.pctSortRondalla;
        bonusEvasioLleugera = cfg.bonusEvasioLleugera;
    }
}
