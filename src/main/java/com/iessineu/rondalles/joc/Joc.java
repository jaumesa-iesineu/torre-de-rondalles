/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iessineu.rondalles.joc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.iessineu.rondalles.audio.GestorMusica;
import com.iessineu.rondalles.combat.SistemaCombat;
import com.iessineu.rondalles.entitats.Enemic;
import com.iessineu.rondalles.entitats.Entitat;
import com.iessineu.rondalles.entitats.Jugador;
import com.iessineu.rondalles.entitats.NpcComerciants;
import com.iessineu.rondalles.entitats.Porta;
import com.iessineu.rondalles.inventari.Clau;
import com.iessineu.rondalles.inventari.Inventari;
import com.iessineu.rondalles.inventari.Item;
import com.iessineu.rondalles.inventari.ItemMapa;
import com.iessineu.rondalles.inventari.Pocio;
import com.iessineu.rondalles.inventari.RegistreItems;
import com.iessineu.rondalles.mapa.CarregadorMapa;
import com.iessineu.rondalles.mapa.Mapa;
import com.iessineu.rondalles.mapa.TipusTerra;
import com.iessineu.rondalles.motor.Estat;
import com.iessineu.rondalles.motor.Motor;
import com.iessineu.rondalles.motor.Renderitzador;

public class Joc extends Motor {

    private Mapa mapa;

    Jugador jugador;
    List<Enemic> enemics;

    private List<ItemMapa> itemsMapa = new ArrayList<>();
    private Enemic enemicCombat = null;
    private List<String> logCombat = new ArrayList<>();

    // --- Pisos ---
    private int pisActual = 1;

    // --- Portes ---
    private List<Porta> portes = new ArrayList<>();

    // --- NPCs ---
    private List<NpcComerciants> npcs = new ArrayList<>();
    private NpcComerciants npcActual = null;
    private String enigmaInput = "";

    // --- Menú inicial amb fletxa ---
    private int opcioMenuInicial = 0;
    private String[] opcionsInicials = {"Iniciar partida", "Sortir"};

    // --- Terra especial ---
    private boolean esperantSegonaAigua = false;
    private int aiguaNx = 0, aiguaNy = 0;
    private int tornsEsperantEntrada = 0;
    private boolean lliscantGel = false;
    private int tornsDesorientat = 0;
    private boolean godMode = false;
    private final java.util.Deque<String> bufferCheto = new java.util.ArrayDeque<>();
    private static final int MAX_BUFFER_CHETO = 10;
    private int tornsDesorientacioGel = 1;
    private int gelDx = 0, gelDy = 0;
    private long ultimPasGel = 0;
    private long msPasGel = 140;

    private int maxLog = 3;
    private boolean jugadorIniciaCombat = false;

    private String fitxerMapa;
    private char[][] mapaRecord;
    private char[][] terraAmagat; // guarda els simbols reals dels terrenys ocults

    //configuració carregada del game.json
    private ConfigGame config;
    private String[] artJugador;

    private int radiVisio = 10;

    private int opcioMenuPausa = 0;

    public String idMapaActual;

    public List<int[]> enemicsMorts = new ArrayList<>();
    public boolean[][] explorat;
    private String[] opcionsPausa = {"Reanudar", "Guardar", "Carregar", "Sortir"};

    public Joc(String fitxerMapa) {
        this.fitxerMapa = fitxerMapa;
    }

    public Joc(String fitxerMapa, boolean mut) {
        this.fitxerMapa = fitxerMapa;
        this.mut = mut;
    }

    //constructor principal: rep sa config ja construida i fusionada (amb -game i tots els -mod aplicats)
    public Joc(ConfigGame configExterna, boolean mut) {
        this.config = configExterna;
        this.mut = mut;
        //es mapa inicial ve de la config; s'acabara de resoldre a init()
        this.fitxerMapa = configExterna.getMapaInicial();
    }

    @Override
    protected void init() throws Exception {
        renderer = new Renderitzador();

        //si sa config no ve de fora (constructor amb -game/-mod), la carregam des del game.json de dins
        try {
            if (config == null) {
                config = CarregadorGame.carrega("game.json");
            }
            MapaConfig mc = config.getMapaConfig(fitxerMapa);
            if (mc != null) {
                idMapaActual = fitxerMapa;
                fitxerMapa = mc.fitxer;
            } else {
                idMapaActual = config.getMapaInicial();
                MapaConfig inicial = config.getMapaConfig(idMapaActual);
                if (inicial != null) {
                    fitxerMapa = inicial.fitxer;
                }
            }
        } catch (Exception e) {
            idMapaActual = "planta1";
            fitxerMapa = "mapes/planta1.map";
        }

        mapa = CarregadorMapa.carrega(fitxerMapa);

        //carregam els tipus de terra des del JSON
        if (config != null && config.terrenys != null) {
            TipusTerra.inicialitza(config.terrenys);
            //amagaTerrenyEspecial();
        } else {
            //fallback per si no hi ha terrenys al JSON (no hauria de passar)
            try {
                ConfigGame cfgJson = CarregadorGame.carrega("game.json");
                if (cfgJson != null) TipusTerra.inicialitza(cfgJson.terrenys);
            } catch (Exception e) {
                System.err.println("[TERRA] No s'han pogut carregar els terrenys: " + e.getMessage());
            }
        }

        //carregam els símbols especials del mapa des del JSON
        Simbols.inicialitza(config != null ? config.simbols : null);

        //carregam els controls des del JSON
        Controls.inicialitza(config != null ? config.controls : null);

        //carregam les pistes de música des del JSON
        GestorMusica.inicialitza(config != null ? config.musica : null);

        //carregam les constants des del JSON
        if (config != null && config.configuracio != null) {
            ConfigGame.Configuracio cfg = config.configuracio;
            radiVisio = cfg.radiVisio;
            msPasGel = cfg.msPasGel;
            tornsDesorientacioGel = cfg.tornsDesorientacioGel;
            maxLog = cfg.maxLog;
            renderer.setRadiLlanterna(cfg.radiLlanterna);
            renderer.setAmpleHud(cfg.ampleHud);
        }

        //carregam els textos d'UI des del JSON
        if (config != null && config.texts != null) {
            ConfigGame.TextsConfig t = config.texts;
            renderer.setWindowTitle(t.windowTitle);
            renderer.setHeaderTitle(t.headerTitle);
            renderer.setSubtitle(t.subtitle);
            renderer.setPauseTitle(t.pauseTitle);
            renderer.setPauseInstructions(t.pauseInstructions);
            renderer.setPauseResumeHint(t.pauseResumeHint);
            opcionsInicials = new String[]{t.menuIniciar, t.menuSortir};
            opcionsPausa = new String[]{t.menuReanudar, t.menuGuardar, t.menuCarregar, t.menuSortir};
        }

        jugador = creaJugador(trobaInicialX(), trobaInicialY());
        enemics = new ArrayList<>();
        carregaEnemics();
        for (Enemic e : enemics) e.setTotsEnemics(enemics);
        carregaItemsMapa();
        carregaPortes();
        carregaNpcs();
        carregaEquipamentInicial();
        carregaArtJugador();
        renderer.setArtJugador(artJugador);

        explorat = new boolean[mapa.getAlcada()][mapa.getAmplada()];
        mapaRecord = new char[mapa.getAlcada()][mapa.getAmplada()];
        for (char[] fila : mapaRecord) {
            java.util.Arrays.fill(fila, ' ');
        }
        amagaTerrenyEspecial();
        
        GestorMusica.reprodueix("MENU");
    }

    @Override
    protected boolean estaAnimant() {
        return lliscantGel;
    }
    
    private void amagaTerrenyEspecial() {
        terraAmagat = new char[mapa.getAlcada()][mapa.getAmplada()];
        char[][] celles = mapa.getCelles();
        for (int y = 0; y < celles.length; y++) {
            for (int x = 0; x < celles[y].length; x++) {
                TipusTerra t = TipusTerra.de(celles[y][x]);
                if (t != null && t.getAmagat() != '\0') {
                    terraAmagat[y][x] = celles[y][x]; // guardam el real
                    mapa.setCella(x, y, t.getAmagat()); // mostram l'amagat
                }
            }
        }
    }
    
    @Override
    protected void actualitza(KeyStroke tecla) {
        if (lliscantGel) {//Lògica de patinar.
            long ara = System.currentTimeMillis();
            if (ara - ultimPasGel >= msPasGel) {
                ultimPasGel = ara;
                int gx = jugador.getX() + gelDx;
                int gy = jugador.getY() + gelDy;
                char terraDesti = mapa.getCelles()[Math.max(0, Math.min(mapa.getAlcada() - 1, gy))][Math.max(0, Math.min(mapa.getAmplada() - 1, gx))];
                Enemic enmig = trobaEnemicA(gx, gy);
                if (enmig != null) {
                    lliscantGel = false;
                    tornsDesorientat = 0;
                    enemicCombat = enmig;
                    jugadorIniciaCombat = true;
                    logCombat.clear();
                    afegeixLog("Combat amb " + enmig.getNom().toUpperCase() + "!");
                    estat = Estat.COMBAT;
                    GestorMusica.reprodueix(esBoss(enmig) ? "BOSS" : "COMBAT");
                    aplicaAtacSorpresa();
                } else {
                    TipusTerra tt = TipusTerra.de(terraDesti);
                    // si el terreny destí té un real amagat, usam el tipus real per decidir si llisca
                    if (terraAmagat != null && gy >= 0 && gy < terraAmagat.length 
                        && gx >= 0 && gx < terraAmagat[gy].length 
                        && terraAmagat[gy][gx] != '\0') {
                        tt = TipusTerra.de(terraAmagat[gy][gx]);
                    }

                    if (mapa.esPasable(gx, gy) && tt != null && tt.isLlisca()) {
                        jugador.setX(gx);
                        jugador.setY(gy);
                    
                        // revelam i aplicam mal
                        if (terraAmagat[gy][gx] != '\0') {
                            char simbolReal = terraAmagat[gy][gx];
                            terraAmagat[gy][gx] = '\0';
                            mapa.setCella(gx, gy, simbolReal);
                        }

                        // aplicam mal del terreny actual (ja sigui revelat o no)
                        TipusTerra terraActual = TipusTerra.de(mapa.getCelles()[gy][gx]);
                        if (terraActual != null && terraActual.getMal() > 0) {
                            jugador.rebreDany(terraActual.getMal());
                        }
                    
                        tickTorn();
                    } else {
                        lliscantGel = false;
                        tornsDesorientat = tornsDesorientacioGel;
                        if (mapa.esPasable(gx, gy)) {
                            jugador.setX(gx);
                            jugador.setY(gy);
                        
                             // revelam i aplicam mal
                            if (terraAmagat != null && terraAmagat[gy][gx] != '\0') {
                                char simbolReal = terraAmagat[gy][gx];
                                terraAmagat[gy][gx] = '\0';
                                mapa.setCella(gx, gy, simbolReal);
                                TipusTerra tReal = TipusTerra.de(simbolReal);
                                if (tReal != null && tReal.getMal() > 0) {
                                    jugador.rebreDany(tReal.getMal());
                                }
                            }
                        
                            tickTorn();
                        }
                    }
                }
                if (jugador.esMort()) {
                    corrent = false;
                }
            }
            return;
        }

        if (tecla == null) return;

        if (tornsDesorientat > 0) {
            tornsDesorientat--;
            tickTorn();
            return;
        }


        comprovanChetos(tecla);

        if (estat == Estat.MENU_INICIAL) {
            gestionaMenuInicial(tecla);
            return;
        }
        if (estat == Estat.PAUSA) {
            gestionaPausa(tecla);
            return;
        }

        if (estat == Estat.ENIGMA) {
            gestionaEnigma(tecla);
            return;
        }
        if (estat == Estat.COMERCIANT) {
            gestionaComerciants(tecla);
            return;
        }

        if (tecla.getKeyType() == KeyType.Escape) {
            opcioMenuPausa = 0;
            estat = Estat.PAUSA;
            return;
        }
        if (tecla.getKeyType() == KeyType.EOF) {
            corrent = false;
            return;
        }
        if (estat == Estat.INVENTARI) {
            gestionaInventari(tecla);
            return;
        }
        if (estat == Estat.COMBAT) {
            gestionaCombat(tecla);
        } else {
            gestionaMoviment(tecla);
        }
    }

    private void gestionaInventari(KeyStroke tecla) {
        if (tecla.getKeyType() == KeyType.Escape) {
            estat = Estat.MON;
            return;
        }
        if (tecla.getKeyType() == KeyType.Character) {
            char c = tecla.getCharacter();
            if (Controls.esInventari(c)) {
                estat = Estat.MON;
                return;
            }
            if (c >= '1' && c <= '4') {
                int idx = c - '1';
                Item item = jugador.getInventari().get(idx);
                if (item instanceof com.iessineu.rondalles.inventari.Armadura arm) {
                    jugador.getInventari().equipaArmadura(arm, jugador);
                } else if (item instanceof com.iessineu.rondalles.inventari.Arma arma) {
                    jugador.getInventari().equipaArma(arma, jugador);
                }
            }
        }
    }

    private void gestionaMenuInicial(KeyStroke tecla) {

        if (tecla.getKeyType() == KeyType.ArrowUp || tecla.getKeyType() == KeyType.ArrowDown) {
            opcioMenuInicial = 1 - opcioMenuInicial;
            return;
        }
        if (tecla.getKeyType() == KeyType.Enter) {
            if (opcioMenuInicial == 0) {
                estat = Estat.MON;
                GestorMusica.reprodueix("PIS_1");
            } else {
                corrent = false;
            }
            return;
        }
        if (tecla.getKeyType() == KeyType.Escape) {
            corrent = false;
        }

    }

    private void gestionaPausa(KeyStroke tecla) {
        if (tecla.getKeyType() == KeyType.ArrowUp) {
            opcioMenuPausa = (opcioMenuPausa + opcionsPausa.length - 1) % opcionsPausa.length;
            return;
        }
        if (tecla.getKeyType() == KeyType.ArrowDown) {
            opcioMenuPausa = (opcioMenuPausa + 1) % opcionsPausa.length;
            return;
        }
        if (tecla.getKeyType() == KeyType.Escape) {
            estat = Estat.MON;
            return;
        }
        if (tecla.getKeyType() == KeyType.Enter) {
            switch (opcioMenuPausa) {
                case 0 ->
                    estat = Estat.MON;

                case 1 -> {
                    estat = Estat.MON;
                }
                case 2 ->
                    corrent = false;

            }
        }
        if (tecla.getKeyType() == KeyType.Character) {
            char c = tecla.getCharacter();
            if (c == 'r' || c == 'R') {
                estat = Estat.MON;
                return;
            }
            if (c == 'x' || c == 'X') {
                corrent = false;
            }
        }
    }

    private void gestionaCombat(KeyStroke tecla) {
        if (tecla.getKeyType() != KeyType.Character) {
            return;
        }
        char c = tecla.getCharacter();

        if (c >= '1' && c <= '9') {
            int idx = c - '1';
            Item item = jugador.getInventari().get(idx);
            if (item != null) {
                String nom = enemicCombat.getNom().toUpperCase();
                if (item instanceof Pocio pocio && pocio.getTipus() != Pocio.Tipus.VIDA) {
                    pocio.aplicaEfecteEnemic(enemicCombat);
                    jugador.getInventari().elimina(idx);
                    afegeixLog("Has llançat " + item.getNom() + " a " + nom + "!");
                } else {
                    jugador.usaItem(idx);
                    afegeixLog("Has usat: " + item.getNom() + ".");
                }
                String tickLog = SistemaCombat.tickEnemics(enemicCombat);
                if (!tickLog.isEmpty()) {
                    afegeixLog(tickLog);
                }
                jugador.tickVeri();
                jugador.tickFoc();
                jugador.tickGel();
                int danyRebut = atacaJugadorAmbGod(enemicCombat);
                if (danyRebut == -1) {
                    afegeixLog("Has esquivat l'atac de " + nom + "!");
                } else {
                    afegeixLog(nom + " aprofita! Has rebut " + danyRebut + " de dany.");
                }
                if (jugador.esMort()) {
                    GestorMusica.reprodueix("GAME_OVER");
                    corrent = false;
                }
            }
            return;
        }
        if (Controls.esFugir(c)) {
            enemicCombat = null;

            GestorMusica.reprodueix("PIS_" + Math.min(pisActual, 5));

            estat = Estat.MON;
            return;
        }

        if (Controls.esAtacar(c)) {
            String nom = enemicCombat.getNom().toUpperCase();
            int danyFet = SistemaCombat.atacaEnemic(jugador, enemicCombat);
            afegeixLog("Has atacat! " + nom + " ha rebut " + danyFet + " de dany.");
            if (enemicCombat.esMort()) {
                afegeixLog(nom + " ha caigut!");

                //guardam ses dades del boss abans de treure'l de la llista
                boolean eraBoss = esBoss(enemicCombat);
                int bossX = enemicCombat.getX();
                int bossY = enemicCombat.getY();
                String clauDropejada = enemicCombat.getClauDropejada();

                enemics.remove(enemicCombat);
                enemicCombat = null;

                if (eraBoss && clauDropejada != null && !clauDropejada.isBlank()) {
                    try {
                        Clau clau = RegistreItems.get().clau(clauDropejada);
                        jugador.afegeixItem(clau);
                        afegeixLog("Has derrotat es boss! Has obtingut la " + clau.getNom() + ".");
                    } catch (Exception ex) {
                        afegeixLog("Has derrotat es boss!");
                    }
                } else if (eraBoss) {
                    afegeixLog("Has derrotat es boss!");
                }

                GestorMusica.reprodueix(
                        "PIS_" + Math.min(pisActual, 5)
                );

                estat = Estat.MON;
                return;
            }
            SistemaCombat.tickEnemics(enemicCombat);
            jugador.tickVeri();
            jugador.tickFoc();
            jugador.tickGel();
            int danyRebut = atacaJugadorAmbGod(enemicCombat);
            if (danyRebut == -1) {
                afegeixLog("Has esquivat l'atac de " + nom + "!");
            } else {
                afegeixLog(nom + " contraataca! Has rebut " + danyRebut + " de dany.");
            }

            if (jugador.esMort()) {
                GestorMusica.reprodueix("GAME_OVER");
                corrent = false;
            }

        }
    }

    private void afegeixLog(String msg) {
        logCombat.add(msg);
        if (logCombat.size() > maxLog) {
            logCombat.remove(0);
        }
    }

    private void gestionaMoviment(KeyStroke tecla) {
        if (tecla.getKeyType() == KeyType.Character) {
            char c = tecla.getCharacter();
            if (Controls.esInventari(c)) {
                for (Enemic e : enemics) {
                    if (e.isActiu()) {
                        e.actualitzaIA(jugador, mapa.getCelles());
                    }
                }
                estat = Estat.INVENTARI;
                return;
            }
            if (Controls.esInteractuar(c)) {
                interactuaPorta();
                return;
            }
            if (c >= '1' && c <= '9') {
                jugador.usaItem(c - '1');

                tickTorn();

                return;
            }
        }

        int nx = jugador.getX();
        int ny = jugador.getY();
        int dx = 0, dy = 0;

        switch (tecla.getKeyType()) {
            case ArrowUp -> {
                ny--;
                dy = -1;
            }
            case ArrowDown -> {
                ny++;
                dy = 1;
            }
            case ArrowLeft -> {
                nx--;
                dx = -1;
            }
            case ArrowRight -> {
                nx++;
                dx = 1;
            }
            default -> {
                return;
            }
        }

        NpcComerciants npc = trobaNpcA(nx, ny);
        if (npc != null) {
            npcActual = npc;
            enigmaInput = "";
            if (npc.getEnigma() == null || npc.isEnigmaResult()) {
                estat = Estat.COMERCIANT;
            } else {
                estat = Estat.ENIGMA;
            }
            return;
        }

        Enemic enemic = trobaEnemicA(nx, ny);
        if (enemic != null) {
            enemicCombat = enemic;
            jugadorIniciaCombat = true;
            logCombat.clear();
            afegeixLog("Combat amb " + enemic.getNom().toUpperCase() + "!");
            estat = Estat.COMBAT;
            GestorMusica.reprodueix(esBoss(enemic) ? "BOSS" : "COMBAT");
            aplicaAtacSorpresa();
            return;
        }

        Porta porta = trobaPortaA(nx, ny);
        if (porta != null && porta.isTancada()) {
            return;
        }

        if (!mapa.esPasable(nx, ny)) {
            return;
        }

        char simbolDesti = mapa.getCelles()[ny][nx];
        TipusTerra terraDestiT = TipusTerra.de(simbolDesti);
        // també miram el tipus real si està amagat
        TipusTerra terraDestiReal = terraDestiT;
        if (terraAmagat != null && terraAmagat[ny][nx] != '\0') {
            terraDestiReal = TipusTerra.de(terraAmagat[ny][nx]);
        }
        
        int velTerra = (terraDestiReal != null) ? terraDestiReal.getVelocitat() : 1;
        
        if (velTerra > 1) {
            if (aiguaNx != nx || aiguaNy != ny) {
                // nova cel·la, reiniciam comptador
                tornsEsperantEntrada = 1;
                aiguaNx = nx;
                aiguaNy = ny;
                tickTorn(); // passa el torn sense moure
                return;
            } else {
                tornsEsperantEntrada++;
                if (tornsEsperantEntrada < velTerra) {
                    tickTorn(); // passa el torn sense moure
                    return;
                }
                // ja hem esperat prou, entram
                tornsEsperantEntrada = 0;
            }
        } else {
            tornsEsperantEntrada = 0;
            aiguaNx = -1;
            aiguaNy = -1;
        }

        jugador.setX(nx);
        jugador.setY(ny);
        jugador.setEstatJugador(Jugador.EstatJugador.MOVIMENT);
        
        // revelam terreny amagat si n'hi ha
        if (terraAmagat != null && terraAmagat[ny][nx] != '\0') {//Aqui es gestionen els terrenys amagats.
            char simbolReal = terraAmagat[ny][nx];
            terraAmagat[ny][nx] = '\0'; // ja no està amagat
            mapa.setCella(nx, ny, simbolReal); // restauram el simbol real

            // ara aplicam l'efecte real
            TipusTerra tReal = TipusTerra.de(simbolReal);
            if (tReal != null && tReal.getMal() > 0) {
                jugador.rebreDany(tReal.getMal());
            }
            if (tReal != null && tReal.isLlisca()) {
                lliscantGel = true;
                gelDx = dx;
                gelDy = dy;
            } else {
                lliscantGel = false;
            }

            recullItemSiNHiHa(nx, ny);
            tickTorn();
             if (jugador.esMort()) corrent = false;
                return; // <-- IMPORTANT: no continuar al bloc normal
        }
       
        // terreny normal (no amagat)
        TipusTerra terraPeu = TipusTerra.de(mapa.getCelles()[ny][nx]);
        
        if(terraPeu !=null && terraPeu.getMal()>0){
               jugador.rebreDany(terraPeu.getMal());
        }
        
        if (terraPeu != null && terraPeu.isLlisca()) {
            lliscantGel = true;
            gelDx = dx;
            gelDy = dy;
        } else {
            lliscantGel = false;
        }

        recullItemSiNHiHa(nx, ny);
        tickTorn();

        if (jugador.esMort()) {
            corrent = false;
        }
    }

    private void tickTorn() {
        jugador.tickVeri();
        jugador.tickFoc();
        jugador.tickGel();
        TipusTerra terra = TipusTerra.de(
                mapa.getCelles()[jugador.getY()][jugador.getX()]);
        double modRadi = (terra != null) ? terra.getModRadi() : 1.0;
        //torns extra per penalitzacio de pes (velocitat 5=normal, 4=-1, 3=-2)
        int tornsEnemics = 1 + (5 - jugador.velocitatEfectiva());
        for (int torn = 0; torn < tornsEnemics; torn++) {
            for (Enemic e : enemics) {
                if (!e.isActiu()) continue;
                if (!e.haDActuar()) continue;
                int radEfectiu = (int) (e.getRadDeteccio() * modRadi);
                e.actualitzaIAambRadi(jugador, radEfectiu);
                e.actualitzaIA(jugador, mapa.getCelles());
                if (torn == 0) {
                    e.tickVeri();
                    e.tickFoc();
                    e.tickGel();
                }
            }
        }
        // enemic adjacent i perseguint → inicia combat amb atac sorpresa
        if (estat == Estat.MON) {
            for (Enemic e : enemics) {
                if (!e.isActiu()) continue;
                if (e.getEstatEnemic() != Enemic.EstatEnemic.PERSEGUINT) continue;
                int dist = Math.abs(e.getX() - jugador.getX()) + Math.abs(e.getY() - jugador.getY());
                if (dist == 1) {
                    enemicCombat = e;
                    jugadorIniciaCombat = false;
                    logCombat.clear();
                    afegeixLog(e.getNom().toUpperCase() + " t'ataca!");
                    estat = Estat.COMBAT;
                    GestorMusica.reprodueix(esBoss(e) ? "BOSS" : "COMBAT");
                    aplicaAtacSorpresa();
                    break;
                }
            }
        }
    }

    private boolean esBoss(Enemic e) {
        return e != null && e.isBoss();
    }

    private int atacaJugadorAmbGod(Enemic enemic) {
        if (godMode) return -1;
        return SistemaCombat.atacaJugador(enemic, jugador);
    }

    private void aplicaAtacSorpresa() {
        String nom = enemicCombat.getNom().toUpperCase();
        if (jugadorIniciaCombat) {
            int dany = SistemaCombat.atacaEnemic(jugador, enemicCombat);
            afegeixLog("Atac sorpresa! " + nom + " ha rebut " + dany + " de dany.");
            if (enemicCombat.esMort()) {
                afegeixLog(nom + " ha caigut!");
                boolean eraBoss = esBoss(enemicCombat);
                String clauDropejada = enemicCombat.getClauDropejada();
                enemics.remove(enemicCombat);
                if (eraBoss && clauDropejada != null && !clauDropejada.isBlank()) {
                    try {
                        Clau clau = RegistreItems.get().clau(clauDropejada);
                        jugador.afegeixItem(clau);
                        afegeixLog("Has obtingut la " + clau.getNom() + ".");
                    } catch (Exception ex) {}
                }
                enemicCombat = null;
                GestorMusica.reprodueix("PIS_" + Math.min(pisActual, 5));
                estat = Estat.MON;
            }
        } else {
            int dany = atacaJugadorAmbGod(enemicCombat);
            if (dany == -1) {
                afegeixLog(nom + " t'ataca per sorpresa! Has esquivat!");
            } else {
                afegeixLog(nom + " t'ataca per sorpresa! Has rebut " + dany + " de dany.");
            }
            if (jugador.esMort()) {
                GestorMusica.reprodueix("GAME_OVER");
                corrent = false;
            }
        }
    }

    private String teclaANom(KeyStroke tecla) {
        return switch (tecla.getKeyType()) {
            case ArrowUp -> "UP";
            case ArrowDown -> "DOWN";
            case ArrowLeft -> "LEFT";
            case ArrowRight -> "RIGHT";
            case Character -> String.valueOf(tecla.getCharacter()).toUpperCase();
            default -> "";
        };
    }

    private void comprovanChetos(KeyStroke tecla) {
        if (config.chetos == null) return;
        String nom = teclaANom(tecla);
        if (nom.isEmpty()) return;
        bufferCheto.addLast(nom);
        if (bufferCheto.size() > MAX_BUFFER_CHETO) bufferCheto.pollFirst();
        String[] buffer = bufferCheto.toArray(new String[0]);
        for (ConfigGame.ChetoConfig cheto : config.chetos) {
            if (cheto.sequencia == null || cheto.sequencia.isEmpty()) continue;
            int mida = cheto.sequencia.size();
            if (buffer.length < mida) continue;
            boolean coincideix = true;
            for (int i = 0; i < mida; i++) {
                if (!cheto.sequencia.get(i).equalsIgnoreCase(buffer[buffer.length - mida + i])) {
                    coincideix = false;
                    break;
                }
            }
            if (coincideix) {
                executaCheto(cheto.accio);
                bufferCheto.clear();
            }
        }
    }

    private void executaCheto(String accio) {
        switch (accio) {
            case "hp" -> {
                jugador.curar(jugador.getVidaMaxima());
                afegeixLog(">>> CHETO: HP al màxim!");
            }
            case "kills" -> {
                for (Enemic e : enemics) e.rebreDany(99999);
                afegeixLog(">>> CHETO: Tots els enemics morts!");
            }
            case "inventari" -> {
                var registre = com.iessineu.rondalles.inventari.RegistreItems.get();
                registre.totesLesPocions().keySet().forEach(id -> jugador.getInventari().afegeix(registre.pocio(id)));
                afegeixLog(">>> CHETO: Inventari ple!");
            }
            case "nextpis" -> passaSeguantPis();
            case "god" -> {
                godMode = !godMode;
                afegeixLog(">>> CHETO: God mode " + (godMode ? "ON" : "OFF") + "!");
            }
        }
    }

    private void passaSeguantPis() {
        pisActual++;
        if (pisActual > config.mapes.ordre.size()) {
            GestorMusica.reprodueix("VICTORIA");
            estat = Estat.VICTORIA;
            return;
        }
        try {
            String idMapa = config.mapes.ordre.get(pisActual - 1);
            idMapaActual = idMapa;
            fitxerMapa = idMapa;
            MapaConfig mc = config.getMapaConfig(idMapa);
            if (mc != null) {
                fitxerMapa = mc.fitxer;
            }
            mapa = CarregadorMapa.carrega(fitxerMapa);
            jugador.setX(trobaInicialX());
            jugador.setY(trobaInicialY());
            enemics.clear();
            itemsMapa.clear();
            npcs.clear();
            portes.clear();
            carregaEnemics();
            for (Enemic e : enemics) e.setTotsEnemics(enemics);
            carregaItemsMapa();
            carregaPortes();
            carregaNpcs();

            exploraClausPisAnterior();

            explorat = new boolean[mapa.getAlcada()][mapa.getAmplada()];
            mapaRecord = new char[mapa.getAlcada()][mapa.getAmplada()];
            for (char[] fila : mapaRecord) {
                java.util.Arrays.fill(fila, ' ');
            }

            GestorMusica.reprodueix("PIS_" + pisActual);
        } catch (Exception ex) {
            corrent = false;
        }
    }

    private void exploraClausPisAnterior() {
        Inventari inv = jugador.getInventari();
        for (int i = inv.getMaxSlots() - 1; i >= 0; i--) {
            var slot = inv.getSlot(i);
            if (slot != null && slot.item() instanceof Clau) {
                inv.elimina(i);
            }
        }
    }

    //Bresenham des del jugador fins a (x1,y1); retorna false si una paret talla sa linia
    //ho feim per sa boira de guerra, aixi no veus a traves de ses parets
    private boolean teLiniaDVista(int x0, int y0, int x1, int y1, char[][] celles) {
        int dx = Math.abs(x1 - x0), dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        int cx = x0, cy = y0;
        while (cx != x1 || cy != y1) {
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                cx += sx;
            }
            if (e2 < dx) {
                err += dx;
                cy += sy;
            }
            if (cx == x1 && cy == y1) {
                break;
            }
            if (cy >= 0 && cy < celles.length && cx >= 0 && cx < celles[cy].length) {
                if (Simbols.bloquejaVisio(celles[cy][cx])) {
                    return false;
                }
            }
        }
        return true;
    }

    //calcula quines caselles veu el jugador i actualitza explorat i mapaRecord
    private boolean[][] actualitzaVisio() {
        boolean[][] visible = new boolean[mapa.getAlcada()][mapa.getAmplada()];
        char[][] celles = mapa.getCelles();
        int jx = jugador.getX(), jy = jugador.getY();
        for (int y = 0; y < mapa.getAlcada(); y++) {
            for (int x = 0; x < mapa.getAmplada(); x++) {
                double dist = Math.sqrt(((x - jx) * (x - jx) / 3) + ((y - jy) * (y - jy) * 2));//Posat per a que se vegi la visió redona
                if (dist > radiVisio) {
                    continue;
                }
                if (teLiniaDVista(jx, jy, x, y, celles)) {
                    visible[y][x] = true;
                    explorat[y][x] = true;
                    mapaRecord[y][x] = celles[y][x];
                }
            }
        }
        //items i enemics visibles també es recorden
        for (ItemMapa im : itemsMapa) {
            if (visible[im.getY()][im.getX()]) {
                mapaRecord[im.getY()][im.getX()] = im.getItem().getSimbol();
            }
        }
        for (Enemic e : enemics) {
            if (e.isActiu() && visible[e.getY()][e.getX()]) {
                e.setDescobert(true);
                mapaRecord[e.getY()][e.getX()] = e.getSimbol();
            }
        }
        for (Porta p : portes) {
            if (visible[p.getY()][p.getX()]) {
                mapaRecord[p.getY()][p.getX()] = p.getSimbol();
            }
        }
        return visible;
    }

    private void carregaPortes() {
        portes.clear();
        if (config != null) {
            List<PosicioPorta> posicions = config.getPosicionsPortaPerMapa(idMapaActual);
            if (!posicions.isEmpty()) {
                for (PosicioPorta p : posicions) {
                    boolean bloquejada = p.esPortaCanviPlanta;
                    Porta porta = new Porta(p.x, p.y, bloquejada, p.esPortaCanviPlanta, p.clauId);
                    portes.add(porta);
                    mapa.setCella(p.x, p.y, porta.getSimbol());
                }
                return;
            }
        }
        char[][] celles = mapa.getCelles();
        for (int y = 0; y < celles.length; y++) {
            for (int x = 0; x < celles[y].length; x++) {
                if (Simbols.esMarcadorPorta(celles[y][x])) {
                    portes.add(new Porta(x, y));
                    celles[y][x] = '+';
                }
            }
        }
    }

    private void carregaNpcs() {
        char[][] celles = mapa.getCelles();
        for (int y = 0; y < celles.length; y++) {
            for (int x = 0; x < celles[y].length; x++) {
                if (Simbols.esMarcadorNpc(celles[y][x])) {
                    npcs.add(new NpcComerciants(x, y, pisActual, config));
                }
            }
        }
    }

    private NpcComerciants trobaNpcA(int x, int y) {
        for (NpcComerciants n : npcs) {
            if (n.getX() == x && n.getY() == y) {
                return n;
            }
        }
        return null;
    }

    private void gestionaEnigma(KeyStroke tecla) {
        if (tecla.getKeyType() == KeyType.Escape) {
            estat = Estat.MON;
            return;
        }
        if (tecla.getKeyType() == KeyType.Enter) {
            if (npcActual.comprovaSolucio(enigmaInput)) {
                estat = Estat.COMERCIANT;
            } else {
                enigmaInput = "";
            }
            return;
        }
        if (tecla.getKeyType() == KeyType.Backspace && enigmaInput.length() > 0) {
            enigmaInput = enigmaInput.substring(0, enigmaInput.length() - 1);
        }
        if (tecla.getKeyType() == KeyType.Character) {
            enigmaInput += tecla.getCharacter();
        }
    }

    private void gestionaComerciants(KeyStroke tecla) {
        if (tecla.getKeyType() == KeyType.Escape || tecla.getKeyType() == KeyType.Enter) {
            estat = Estat.MON;
        }
    }

    private Enemic trobaEnemicA(int x, int y) {
        for (Enemic e : enemics) {
            if (e.isActiu() && e.getX() == x && e.getY() == y) {
                return e;
            }
        }
        return null;
    }

    private void interactuaPorta() {
        int jx = jugador.getX();
        int jy = jugador.getY();
        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
        for (int[] d : dirs) {
            int dx = jx + d[0], dy = jy + d[1];
            Porta porta = trobaPortaA(dx, dy);
            if (porta != null) {
                if (porta.isBloquejada()) {
                    if (porta.teClau(jugador.getInventari(), pisActual)) {
                        porta.desbloqueja();
                        mapa.setCella(dx, dy, porta.getSimbol());
                        afegeixLog("Has obert la porta amb la clau!");
                        tickTorn();
                    } else {
                        afegeixLog("La porta està tancada amb un cadenat! Necessites una clau.");
                    }
                    return;
                }
                porta.interactua(jugador);
                mapa.setCella(dx, dy, porta.getSimbol());
                tickTorn();
                return;
            }
        }
    }

    private Porta trobaPortaA(int x, int y) {
        for (Porta p : portes) {
            if (p.getX() == x && p.getY() == y) {
                return p;
            }
        }
        return null;
    }

    private void recullItemSiNHiHa(int x, int y) {
        ItemMapa trobat = null;
        for (ItemMapa im : itemsMapa) {
            if (im.getX() == x && im.getY() == y) {
                trobat = im;
                break;
            }
        }
        if (trobat == null) {
            return;
        }
        jugador.afegeixItem(trobat.getItem());
        mapa.setCella(x, y, '.');
        itemsMapa.remove(trobat);
    }

    private Jugador creaJugador(int x, int y) {
        ConfigGame.JugadorConfig jc = null;
        if (config != null && config.jugador != null) {
            jc = config.jugador;
        } else {
            try {
                ConfigGame cfgJson = CarregadorGame.carrega("game.json");
                if (cfgJson != null) jc = cfgJson.jugador;
            } catch (Exception e) {
                System.err.println("[JUGADOR] No s'han pogut llegir els stats: " + e.getMessage());
            }
        }
        int ms = (config != null && config.configuracio != null) ? config.configuracio.maxSlotsInventari : 4;
        if (jc != null) {
            return new Jugador(x, y, jc.vidaMaxima, jc.atac, jc.velocitat, jc.evasio, jc.pesMaxim, ms);
        }
        return new Jugador(x, y, 100, 3, 5, 10, 50, ms);
    }

    private void carregaEquipamentInicial() {
        //config.equipamentInicial pot ser null si sa config ve de la BD (PartidaRepository)
        //en aquest cas llegim l'equipamentInicial directament del game.json de dins
        ConfigGame.EquipamentInicial eq = null;
        if (config != null && config.equipamentInicial != null) {
            eq = config.equipamentInicial;
        } else {
            try {
                ConfigGame cfgJson = CarregadorGame.carrega("game.json");
                if (cfgJson != null) {
                    eq = cfgJson.equipamentInicial;
                }
            } catch (Exception e) {
                System.err.println("[EQUIP] No s'ha pogut llegir equipamentInicial del game.json: " + e.getMessage());
            }
        }
        if (eq == null) {
            return;
        }
        if (eq.armadures != null) {
            for (String id : eq.armadures) {
                try {
                    com.iessineu.rondalles.inventari.Armadura arm = RegistreItems.get().armadura(id);
                    jugador.getInventari().equipaArmadura(arm, jugador);
                } catch (Exception e) {
                    System.err.println("[EQUIP] Error carregant armadura '" + id + "': " + e.getMessage());
                }
            }
        }
        if (eq.arma != null) {
            try {
                com.iessineu.rondalles.inventari.Arma arma = RegistreItems.get().arma(eq.arma);
                jugador.getInventari().equipaArma(arma, jugador);
            } catch (Exception e) {
                System.err.println("[EQUIP] Error carregant arma '" + eq.arma + "': " + e.getMessage());
            }
        }
    }

    private void carregaArtJugador() {
        if (config != null && config.jugador != null && config.jugador.artAscii != null) {
            artJugador = config.jugador.artAscii;
            return;
        }
        try {
            ConfigGame cfgJson = CarregadorGame.carrega("game.json");
            if (cfgJson != null && cfgJson.jugador != null) {
                artJugador = cfgJson.jugador.artAscii;
            }
        } catch (Exception e) {
            System.err.println("[ART] No s'ha pogut carregar art del jugador: " + e.getMessage());
        }
    }

    public String[] getArtJugador() {
        return artJugador;
    }

    private void carregaItemsMapa() {
        //si el game.json té posicions per aquest mapa, les usam (no escanejam el mapa)
        if (config != null) {
            List<PosicioItem> posicions = config.getPosicionsItemPerMapa(idMapaActual);
            if (!posicions.isEmpty()) {
                for (PosicioItem p : posicions) {
                    Item item = RegistreItems.get().itemPerId(p.id);
                    if (item != null) {
                        itemsMapa.add(new ItemMapa(p.x, p.y, item));
                        mapa.setCella(p.x, p.y, 'i'); //marca d'item per a la IA dels enemics
                    }
                }
                return;
            }
        }
        //fallback: escanejam el mapa per marcador d'item (p. ex. mapes sense posicions al json)
        char[][] celles = mapa.getCelles();
        int comptador = 0;
        for (int y = 0; y < celles.length; y++) {
            for (int x = 0; x < celles[y].length; x++) {
                if (Simbols.esMarcadorItem(celles[y][x])) {
                    String id = switch (comptador % 4) {
                        case 0 ->
                            "pocio-vida";
                        case 1 ->
                            "pocio-veri";
                        case 2 ->
                            "pocio-foc";
                        default ->
                            "pocio-gel";
                    };
                    itemsMapa.add(new ItemMapa(x, y, RegistreItems.get().pocio(id)));
                    comptador++;
                }
            }
        }
    }

    private void carregaEnemics() {
        //si el game.json té posicions per aquest mapa, les usam (no escanejam el mapa)
        if (config != null) {
            //String idMapaActual = null;
            List<PosicioEnemic> posicions = config.getPosicionsPerMapa(idMapaActual);
            if (!posicions.isEmpty()) {
                for (PosicioEnemic p : posicions) {
                    Enemic enemic = creaEnemic(p.simbol, p.x, p.y);
                    if (enemic != null) {
                        enemic.setSpawn(p.x, p.y);
                        enemic.setArea(p.area);
                        enemics.add(enemic);
                    }
                }
                return;
            }
        }
        //fallback: llegim els simbols directament del mapa (mapes sense posicions al json)
        char[][] celles = mapa.getCelles();
        for (int y = 0; y < celles.length; y++) {
            for (int x = 0; x < celles[y].length; x++) {
                Enemic enemic = creaEnemic(String.valueOf(celles[y][x]), x, y);
                if (enemic != null) {
                    enemics.add(enemic);
                    mapa.setCella(x, y, '.');
                }
            }
        }

    }

    private Enemic creaEnemic(String simbol, int x, int y) {
        if (config == null) return null;
        TipusEnemic def = config.getTipusEnemic(simbol);
        if (def == null) return null;
        Enemic enemic = new Enemic(x, y, simbol.charAt(0));
        enemic.aplicaDefinicio(def);
        return enemic;
    }

    @Override
    protected void renderitza() {
        try {
            if (estat == Estat.MENU_INICIAL) {
                renderer.dibuixaMenuInicial(opcioMenuInicial, opcionsInicials);
                return;
            }
            if (estat == Estat.PAUSA) {
                renderer.dibuixaPausa(opcioMenuPausa, opcionsPausa);
                return;
            }
            if (estat == Estat.ENIGMA) {
                renderer.dibuixaEnigma(npcActual.getEnigma(), enigmaInput);
                return;
            }
            if (estat == Estat.COMERCIANT) {
                renderer.dibuixaComerciants(pisActual);
                return;
            }

            List<Entitat> totes = new ArrayList<>(enemics);
            totes.addAll(portes);

            boolean[][] visible = actualitzaVisio();
            if (estat == Estat.COMBAT) {
                renderer.dibuixaCombat(enemicCombat, jugador, logCombat);
            } else if (estat == Estat.INVENTARI) {
                renderer.dibuixaInventari(mapa, jugador.getX(), jugador.getY(), totes, jugador, itemsMapa, visible, explorat, mapaRecord);
            } else {
                renderer.dibuixa(mapa, jugador.getX(), jugador.getY(), totes, jugador, itemsMapa, visible, explorat, mapaRecord);
            }
        } catch (IOException ex) {
            corrent = false;
        }
    }

    private int trobaInicialX() {
        char[][] celles = mapa.getCelles();
        for (int y = 0; y < celles.length; y++) {
            for (int x = 0; x < celles[y].length; x++) {
                if (Simbols.esSpawnJugador(celles[y][x])) {
                    mapa.setCella(x, y, '.');
                    return x;
                }
            }
        }
        for (int y = 0; y < celles.length; y++) {
            for (int x = 0; x < celles[y].length; x++) {
                if (celles[y][x] == '.') {
                    return x;
                }
            }
        }
        return 1;
    }

    private int trobaInicialY() {
        char[][] celles = mapa.getCelles();
        for (int y = 0; y < celles.length; y++) {
            for (int x = 0; x < celles[y].length; x++) {
                if (Simbols.esSpawnJugador(celles[y][x])) {
                    return y;
                }
            }
        }
        for (int y = 0; y < celles.length; y++) {
            for (int x = 0; x < celles[y].length; x++) {
                if (celles[y][x] == '.') {
                    return y;
                }
            }
        }
        return 1;
    }
}
