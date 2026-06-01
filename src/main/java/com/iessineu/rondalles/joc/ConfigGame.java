package com.iessineu.rondalles.joc;

import java.util.ArrayList;
import java.util.List;

//POJO arrel que Gson omple amb tot el game.json
public class ConfigGame {

    public Configuracio configuracio;
    public MapesGroup mapes;
    public EnemicsGroup enemics;

    //grups niats que reflecteixen l'estructura del JSON
    public static class Configuracio {
        public String mapaInicial;
        public String mapaPerdre;
    }

    public static class MapesGroup {
        public List<String> ordre;
        public List<MapaConfig> registres;
    }

    public static class EnemicsGroup {
        public List<TipusEnemic> tipus;
        public List<PosicioEnemic> posicions;
    }

    //helpers per cercar per id o simbol
    public TipusEnemic getTipusEnemic(String simbol) {
        if (enemics == null || enemics.tipus == null) return null;
        for (TipusEnemic t : enemics.tipus)
            if (t.simbol.equals(simbol)) return t;
        return null;
    }

    public MapaConfig getMapaConfig(String id) {
        if (mapes == null || mapes.registres == null) return null;
        for (MapaConfig m : mapes.registres)
            if (m.id.equals(id)) return m;
        return null;
    }

    public List<PosicioEnemic> getPosicionsPerMapa(String mapaId) {
        if (enemics == null || enemics.posicions == null) return new ArrayList<>();
        List<PosicioEnemic> resultat = new ArrayList<>();
        for (PosicioEnemic p : enemics.posicions)
            if (p.mapa.equals(mapaId)) resultat.add(p);
        return resultat;
    }

    public String getMapaInicial() {
        return configuracio != null ? configuracio.mapaInicial : "planta1";
    }

    public String getMapaPerdre() {
        return configuracio != null ? configuracio.mapaPerdre : "perduda";
    }
}
