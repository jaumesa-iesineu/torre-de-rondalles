package com.iessineu.rondalles.joc;

import java.util.List;

//POJO arrel que Gson omple amb tot el game.json
public class ConfigGame {
    public String mapaInicial;
    public String mapaPerdre;
    public List<String> ordre;
    public List<MapaConfig> mapes;
    public List<TipusEnemic> tipusEnemics;

    public TipusEnemic getTipusEnemic(String simbol) {
        if (tipusEnemics == null) return null;
        for (TipusEnemic t : tipusEnemics)
            if (t.simbol.equals(simbol)) return t;
        return null;
    }

    public MapaConfig getMapaConfig(String id) {
        if (mapes == null) return null;
        for (MapaConfig m : mapes)
            if (m.id.equals(id)) return m;
        return null;
    }
}
