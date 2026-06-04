package com.iessineu.rondalles.mapa;

import com.iessineu.rondalles.joc.ConfigGame;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// cada tipus de terra es carrega des del game.json
public class TipusTerra {

    private final List<Character> simbols;
    private final String nom;
    private final int colorR, colorG, colorB;
    private final int fonsR, fonsG, fonsB;
    private final boolean doblePas;
    private final boolean llisca;
    private final double modRadi;

    private TipusTerra(List<Character> simbols, String nom,
                       int colorR, int colorG, int colorB,
                       int fonsR, int fonsG, int fonsB,
                       boolean doblePas, boolean llisca, double modRadi) {
        this.simbols = simbols;
        this.nom = nom;
        this.colorR = colorR;
        this.colorG = colorG;
        this.colorB = colorB;
        this.fonsR = fonsR;
        this.fonsG = fonsG;
        this.fonsB = fonsB;
        this.doblePas = doblePas;
        this.llisca = llisca;
        this.modRadi = modRadi;
    }

    // cache de simbol -> TipusTerra
    private static final Map<Character, TipusTerra> cache = new HashMap<>();

    // borra i omple la cache amb els terrenys del JSON (es crida desde Joc.init)
    public static void inicialitza(List<ConfigGame.TerrenyConfig> configs) {
        cache.clear();
        if (configs == null) return;
        for (ConfigGame.TerrenyConfig tc : configs) {
            TipusTerra t = new TipusTerra(tc.simbols, tc.nom,
                    tc.colorR, tc.colorG, tc.colorB,
                    tc.fonsR, tc.fonsG, tc.fonsB,
                    tc.doblePas, tc.llisca, tc.modRadi);
            for (Character s : tc.simbols) {
                cache.put(s, t);
            }
        }
    }

    // retorna el TipusTerra per un simbol concret, o null si no es terra
    public static TipusTerra de(char c) {
        return cache.get(c);
    }

    // mètodes per accedir a les propietats
    public String getNom() { return nom; }
    public int getColorR() { return colorR; }
    public int getColorG() { return colorG; }
    public int getColorB() { return colorB; }
    public int getFonsR() { return fonsR; }
    public int getFonsG() { return fonsG; }
    public int getFonsB() { return fonsB; }
    public boolean isDoblePas() { return doblePas; }
    public boolean isLlisca() { return llisca; }
    public double getModRadi() { return modRadi; }
}
