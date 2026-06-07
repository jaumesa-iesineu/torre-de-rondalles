package com.iessineu.rondalles.joc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//POJO arrel que Gson omple amb tot es game.json
public class ConfigGame {

    public Configuracio configuracio;
    public Map<String, int[]> colorsPresets;
    public List<TerrenyConfig> terrenys;
    public SimbolsConfig simbols;
    public ControlsConfig controls;
    public MusicaConfig musica;
    public Map<String, String> sfx;
    public MapesGroup mapes;
    public EnemicsGroup enemics;
    public ItemsGroup items;
    public PortesGroup portes;
    public NpcsGroup npcs;
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
        public int tornsDesorientacioGel = 1;
        public int maxLog = 3;
        public int maxSlotsInventari = 4;

        public int danyVeri = 3;
        public int penalitzacioFoc = 2;
        public int penalitzacioGel = 2;
        public double llindCarregaNormal = 0.50;
        public double llindCarregaPesat = 0.80;
        public int penalitzacioEvasioCarrega = 5;
        public int penalitzacioVelNormal = 1;
        public int penalitzacioVelPesat = 2;
        public int pctSortRondalla = 15;
        public int bonusEvasioLleugera = 20;
    }

    //definició d'un tipus de terra des del JSON
    public static class TerrenyConfig {

        public List<Character> simbols;
        public String nom;
        public char amagat;
        public int colorR, colorG, colorB;
        public int fonsR, fonsG, fonsB;
        public int velocitat;
        //public boolean doblePas;
        public boolean llisca;
        public double modRadi = 1.0;
        public int mal;
    }

    //definició de les tecles d'acció (controls)
    public static class ControlsConfig {
        public char atacar = 'a';
        public char fugir = 'f';
        public char inventari = 'i';
        public char interactuar = 'e';
        public char mourePes = 'm';
    }

    //definició dels fitxers de música per a cada situació
    public static class MusicaConfig {
        public String menu = "menu.wav";
        public String pis_1 = "pis1.wav";
        public String pis_2 = "pis2.wav";
        public String pis_3 = "pis3.wav";
        public String pis_4 = "pis4.wav";
        public String pis_5 = "pis5.wav";
        public String combat = "combat.wav";
        public String boss = "boss.wav";
        public String victoria = "victoria.wav";
        public String gameOver = "gameover.wav";
    }

    //definició dels símbols especials del mapa (parets, portes, escales...)
    public static class SimbolsConfig {

        public List<Character> mur = Arrays.asList('#');
        public List<Character> portaTancada = Arrays.asList('+');
        public List<Character> portaOberta = Arrays.asList('/');
        public List<Character> portaBloquejada = Arrays.asList('&');
        public List<Character> spawnJugador = Arrays.asList('@');
        public List<Character> escalaBaix = Arrays.asList('<');
        public List<Character> marcadorItem = Arrays.asList('i');
        public List<Character> marcadorPorta = Arrays.asList('P');
        public List<Character> marcadorNpc = Arrays.asList('N');
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

    public static class NpcsGroup {

        public List<Object> tipus;
        public List<PosicioNpc> posicions;
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

    public static class TipusPersonatgeConfig {
        public String id;
        public String nom;
        public String descripcio;
        public String passiu;       // "sort_rondalla" | "pocions_infinites" | "atac_sorpresa_doble" | "lleugera_vent" | ""
        public String descripcioPassiu;
        public String[] artAscii;
        public int vidaMaxima = 100;
        public int atac = 3;
        public int velocitat = 5;
        public int evasio = 10;
        public int pesMaxim = 50;
    }

    public static class PersonatgeCustomConfig {
        public int pressupost = 10;
        public int vidaBase = 70;
        public int vidaPerPunt = 8;
        public int vidaMax = 150;
        public int atacBase = 1;
        public int atacMax = 9;
        public int velocitatBase = 2;
        public int velocitatMax = 7;
        public int evasioBase = 0;
        public int evasioPerPunt = 5;
        public int evasioMax = 30;
        public int pesMaxim = 50;
    }

    public List<TipusPersonatgeConfig> tipusPersonatge;
    public PersonatgeCustomConfig personatgeCustom;

    //Chetos
    public static class ChetoConfig {
        public String accio;       // "hp", "kills", "inventari", "nextpis", "god"
        public List<String> sequencia; // ["UP","UP","DOWN","DOWN","LEFT","RIGHT"]
    }

    public List<ChetoConfig> chetos;


    //una dita mallorquina amb el seu nivell de dificultat
    public static class DitaConfig {
        public int nivell; // 1=fàcil, 2=normal, 3=difícil
        public String pregunta;
        public String resposta;
    }

    //configuració de les dites i botiga per planta
    public static class EnigmeConfig {
        public int planta;
        public List<DitaConfig> dites; // 3 dites de diferent dificultat
        public List<String> itemsVenda; // IDs dels ítems disponibles a la botiga
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

    public List<PosicioNpc> getPosicionsNpcPerMapa(String mapaId) {
        if (npcs == null || npcs.posicions == null) {
            return new ArrayList<>();
        }
        List<PosicioNpc> resultat = new ArrayList<>();
        for (PosicioNpc p : npcs.posicions) {
            if (mapaId.equals(p.mapa)) {
                resultat.add(p);
            }
        }
        return resultat;
    }

    public String getMapaInicial() {
        return configuracio != null ? configuracio.mapaInicial : "planta1";
    }

    //retorna l'endevinalla per un numero de planta (o la primera si no troba)
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
        public String pauseResumePista = "[ ESC ] Reanudar";
        public String menuTornaAcomencar = "Torna a començar";
    }
}
