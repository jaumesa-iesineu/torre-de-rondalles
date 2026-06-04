package com.iessineu.rondalles.joc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//POJO arrel que Gson omple amb tot el game.json
public class ConfigGame {

    public Configuracio configuracio;
    public Map<String, int[]> colorsPresets; //ex: {"vermellCremat": [180,40,10], ...}
    public List<TerrenyConfig> terrenys;
    public MapesGroup mapes;
    public EnemicsGroup enemics;
    public ItemsGroup items;
    public PortesGroup portes;
    public EquipamentInicial equipamentInicial;
    public JugadorConfig jugador;
    public List<EnigmeConfig> enigmes;
    public TextsConfig texts;

    //grups niats que reflecteixen l'estructura del JSON
    public static class Configuracio {

        public String mapaInicial;
        public int radiLlanterna = 10;
        public int ampleHud = 30;
        public int radiVisio = 10;
        public int msPasGel = 140;
        public int maxLog = 3;
        public int maxSlotsInventari = 4;
    }

    //definició d'un tipus de terra des del JSON
    public static class TerrenyConfig {

        public List<Character> simbols;
        public String nom;
        public int colorR, colorG, colorB;
        public int fonsR, fonsG, fonsB;
        public boolean doblePas;
        public boolean llisca;
        public double modRadi = 1.0;
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

    //un enigme per planta
    public static class EnigmeConfig {

        public int planta;
        public String pregunta;
        public String resposta;
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

    //retorna l'enigme per un numero de planta (o el primer si no troba)
    public EnigmeConfig getEnigmaPerPlanta(int planta) {
        if (enigmes == null) return null;
        for (EnigmeConfig e : enigmes) {
            if (e.planta == planta) return e;
        }
        return enigmes.isEmpty() ? null : enigmes.get(0);
    }

    //textos d'interfície d'usuari (títol, menús, etc.)
    public static class TextsConfig {

        public String windowTitle = "RONDALLES";
        public String headerTitle = " TORRE DE RONDALLES  ~  ";
        public String subtitle = "~ Un joc de rondalles mallorquines ~";
        public String menuIniciar = "Iniciar partida";
        public String menuSortir = "Sortir";
        public String menuReanudar = "Reanudar";
        public String menuGuardar = "Guardar";
        public String menuCarregar = "Carregar";
        public String pauseTitle = "  *** PAUSA ***  ";
        public String pauseInstructions = "Fletxes + ENTER per seleccionar";
        public String pauseResumeHint = "[ ESC ] Reanudar";
    }
}
