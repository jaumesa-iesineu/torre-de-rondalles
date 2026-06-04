package com.iessineu.rondalles.joc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//POJO arrel que Gson omple amb tot el game.json
public class ConfigGame {

    public Configuracio configuracio;
    public Map<String, int[]> colorsPresets; //ex: {"vermellCremat": [180,40,10], ...}
    public MapesGroup mapes;
    public EnemicsGroup enemics;
    public ItemsGroup items;
    public PortesGroup portes;
    public EquipamentInicial equipamentInicial;
    public JugadorConfig jugador;

    //grups niats que reflecteixen l'estructura del JSON
    public static class Configuracio {

        public String mapaInicial;
    }

    public static class MapesGroup {

        public List<String> ordre;
        public List<MapaConfig> registres;
    }

    public static class EnemicsGroup {

        public List<TipusEnemic> tipus;
        public List<PosicioEnemic> posicions;
    }

    public static class ItemsGroup {

        public List<PosicioItem> posicions;
    }

    public static class PortesGroup {

        public List<PosicioPorta> posicions;
    }

    public static class EquipamentInicial {

        public List<String> armadures;
        public String arma;
    }

    public static class JugadorConfig {

        public String artFitxer;
        public String[] artAscii;
        public int vidaMaxima = 100;
        public int atac = 3;
        public int velocitat = 5;
        public int evasio = 10;
        public int pesMaxim = 50;
    }

    //cerca el tipus que conté el simbol donat dins la seva llista de simbols
    public TipusEnemic getTipusEnemic(String simbol) {
        if (enemics == null || enemics.tipus == null) {
            return null;
        }
        for (TipusEnemic t : enemics.tipus) {
            if (t.simbols != null && t.simbols.contains(simbol)) {
                return t;
            }
        }
        return null;
    }

    public MapaConfig getMapaConfig(String id) {
        if (mapes == null || mapes.registres == null) {
            return null;
        }
        for (MapaConfig m : mapes.registres) {
            if (m.id.equals(id)) {
                return m;
            }
        }
        return null;
    }

    public List<PosicioEnemic> getPosicionsPerMapa(String mapaId) {
        if (enemics == null || enemics.posicions == null) {
            return new ArrayList<>();
        }
        List<PosicioEnemic> resultat = new ArrayList<>();
        for (PosicioEnemic p : enemics.posicions) {
            if (p.mapa.equals(mapaId)) {
                resultat.add(p);
            }
        }
        return resultat;
    }

    public List<PosicioItem> getPosicionsItemPerMapa(String mapaId) {
        if (items == null || items.posicions == null) {
            return new ArrayList<>();
        }
        List<PosicioItem> resultat = new ArrayList<>();
        for (PosicioItem p : items.posicions) {
            if (p.mapa.equals(mapaId)) {
                resultat.add(p);
            }
        }
        return resultat;
    }

    public List<PosicioPorta> getPosicionsPortaPerMapa(String mapaId) {
        if (portes == null || portes.posicions == null) {
            return new ArrayList<>();
        }
        List<PosicioPorta> resultat = new ArrayList<>();
        for (PosicioPorta p : portes.posicions) {
            if (p.mapa.equals(mapaId)) {
                resultat.add(p);
            }
        }
        return resultat;
    }

    public String getMapaInicial() {
        return configuracio != null ? configuracio.mapaInicial : "planta1";
    }
}
