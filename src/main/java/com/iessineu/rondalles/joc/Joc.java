/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iessineu.rondalles.joc;


import com.iessineu.rondalles.audio.GestorMusica;
import com.iessineu.rondalles.mapa.TipusTerra;
import com.iessineu.rondalles.entitats.NpcComerciants;

import com.iessineu.rondalles.entitats.Drac;
import com.iessineu.rondalles.entitats.Gegant;
import com.iessineu.rondalles.entitats.NaMariaEnganxa;
import com.iessineu.rondalles.combat.SistemaCombat;
import com.iessineu.rondalles.entitats.Bubota;
import com.iessineu.rondalles.entitats.DimoniBoiet;
import com.iessineu.rondalles.entitats.Enemic;
import com.iessineu.rondalles.entitats.Entitat;
import com.iessineu.rondalles.entitats.Jugador;
import com.iessineu.rondalles.inventari.ItemMapa;
import com.iessineu.rondalles.inventari.RegistreItems;
import com.iessineu.rondalles.mapa.CarregadorMapa;
import com.iessineu.rondalles.mapa.Mapa;
import com.iessineu.rondalles.motor.Estat;
import com.iessineu.rondalles.motor.Motor;
import com.iessineu.rondalles.motor.Renderitzador;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Joc extends Motor {

    private Mapa mapa;

    Jugador jugador;
    private List<Enemic> enemics;

    private List<ItemMapa> itemsMapa = new ArrayList<>();
    private Enemic enemicCombat = null;
    private List<String> logCombat = new ArrayList<>();


    // --- Pisos ---
    private int pisActual = 1;
    private static final String[] FITXERS_PISOS = {
        "pis1.game","pis2.game","pis3.game","pis4.game","pis5.game"
    };

    // --- NPCs ---
    private List<NpcComerciants> npcs = new ArrayList<>();
    private NpcComerciants npcActual = null;
    private String enigmaInput = "";

    // --- Menú inicial amb fletxa ---
    private int opcioMenuInicial = 0;
    private static final String[] OPCIONS_INICIALS = {"Iniciar partida", "Sortir"};

    // --- Terra especial ---
    private boolean esperantSegonaAigua = false;
    private int aiguaNx = 0, aiguaNy = 0;
    private boolean lliscantGel = false;
    private int gelDx = 0, gelDy = 0;

    private static final int MAX_LOG = 3;

    private String fitxerMapa;
    private char[][] mapaRecord;

    //configuració carregada del game.json
    private ConfigGame config;

    private static final int RADI_VISIO = 10;

    private int opcioMenuPausa = 0;

    public Object idMapaActual;

    public Jugador jugado;

    public Object enemicsMorts;
    private static final String[] OPCIONS_PAUSA = {"Reanudar", "Guardar", "Carregar", "Sortir"};

    public Joc(String fitxerMapa) {
        this.fitxerMapa = fitxerMapa;
    }

    public Joc(String fitxerMapa, boolean mut) {
        this.fitxerMapa = fitxerMapa;
        this.mut = mut;
    }

    @Override
    protected void init() throws Exception {
        renderer = new Renderitzador();

        mapa = CarregadorMapa.carrega(fitxerMapa);
        jugador = new Jugador(trobaInicialX(), trobaInicialY());
        enemics = new ArrayList<>();
        carregaEnemics();
        carregaItemsMapa();
        carregaNpcs();
        GestorMusica.reprodueix(GestorMusica.Pista.MENU);

    }

    @Override
    protected void actualitza(KeyStroke tecla) {
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
        if (tecla.getKeyType() == KeyType.Character && (tecla.getCharacter() == 'e' || tecla.getCharacter() == 'E')) {
            estat = Estat.MON;
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
                GestorMusica.reprodueix(GestorMusica.Pista.PIS_1);
            } else {
                corrent = false;
            }
            return;
        }
        if (tecla.getKeyType() == KeyType.Escape) corrent = false;

    }

    private void gestionaPausa(KeyStroke tecla) {
        if (tecla.getKeyType() == KeyType.ArrowUp) {
            opcioMenuPausa = (opcioMenuPausa + OPCIONS_PAUSA.length - 1) % OPCIONS_PAUSA.length;
            return;
        }
        if (tecla.getKeyType() == KeyType.ArrowDown) {
            opcioMenuPausa = (opcioMenuPausa + 1) % OPCIONS_PAUSA.length;
            return;
        }
        if (tecla.getKeyType() == KeyType.Escape) {
            estat = Estat.MON;
            return;
        }
        if (tecla.getKeyType() == KeyType.Enter) {
            switch (opcioMenuPausa) {
                case 0 -> estat = Estat.MON;

                case 1 -> { estat = Estat.MON; }
                case 2 -> corrent = false;

            }
        }
        if (tecla.getKeyType() == KeyType.Character) {
            char c = tecla.getCharacter();
            if (c == 'r' || c == 'R') { estat = Estat.MON; return; }
            if (c == 'x' || c == 'X') { corrent = false; }
        }
    }

    private void gestionaCombat(KeyStroke tecla) {
        if (tecla.getKeyType() != KeyType.Character) return;
        char c = tecla.getCharacter();

        if (c >= '1' && c <= '9') {
            jugador.usaItem(c - '1');
            return;
        }
        if (c == 'f' || c == 'F') {
    enemicCombat = null;


    GestorMusica.reprodueix(
        GestorMusica.Pista.valueOf("PIS_" + Math.min(pisActual, 5))
    );

    estat = Estat.MON;
    return;
}

        if (c == 'a' || c == 'A') {
            String nom = enemicCombat.getClass().getSimpleName().toUpperCase();
            int danyFet = SistemaCombat.atacaEnemic(jugador, enemicCombat);
            afegeixLog("Has atacat! " + nom + " ha rebut " + danyFet + " de dany.");
            if (enemicCombat.esMort()) {
                afegeixLog(nom + " ha caigut!");

                // Comprova boss ABANS de treure de la llista
                boolean eraBoss = esBoss(enemicCombat);
                int bossX = enemicCombat.getX();
                int bossY = enemicCombat.getY();

                enemics.remove(enemicCombat);
enemicCombat = null;

if (eraBoss) {
    mapa.setCella(bossX, bossY, '<');
    afegeixLog("Has derrotat el boss! Han aparegut unes escales (<).");
}

GestorMusica.reprodueix(
    GestorMusica.Pista.valueOf("PIS_" + Math.min(pisActual, 5))
);

estat = Estat.MON;
return;
            }
            SistemaCombat.tickEnemics(enemicCombat);
            jugador.tickVeri();
            jugador.tickFoc();
            jugador.tickGel();
            int danyRebut = SistemaCombat.atacaJugador(enemicCombat, jugador);
            afegeixLog(nom + " contraataca! Has rebut " + danyRebut + " de dany.");

            if (jugador.esMort()) {
    GestorMusica.reprodueix(GestorMusica.Pista.GAME_OVER);
    corrent = false;
}

        }
    }

    private void afegeixLog(String msg) {
        logCombat.add(msg);
        if (logCombat.size() > MAX_LOG) logCombat.remove(0);
    }

    private void gestionaMoviment(KeyStroke tecla) {
        if (tecla.getKeyType() == KeyType.Character) {
            char c = tecla.getCharacter();
            if (c == 'e' || c == 'E') {
                for (Enemic e : enemics) if (e.isActiu()) e.actualitzaIA(jugador, mapa.getCelles());
                estat = Estat.INVENTARI;
                return;
            }
            if (c >= '1' && c <= '9') {
                jugador.usaItem(c - '1');

                tickTorn();

                return;
            }
        }

        if (lliscantGel) {
            int gx = jugador.getX() + gelDx;
            int gy = jugador.getY() + gelDy;
            char terraDesti = mapa.getCelles()[Math.max(0,Math.min(mapa.getAlcada()-1,gy))]
                                             [Math.max(0,Math.min(mapa.getAmplada()-1,gx))];
            if (mapa.esPasable(gx, gy) && TipusTerra.de(terraDesti) == TipusTerra.GEL) {
                jugador.setX(gx); jugador.setY(gy); tickTorn();
            } else {
                lliscantGel = false;
                if (mapa.esPasable(gx, gy)) { jugador.setX(gx); jugador.setY(gy); tickTorn(); }
            }
            if (jugador.esMort()) corrent = false;
            return;
        }

        int nx = jugador.getX();
        int ny = jugador.getY();
        int dx = 0, dy = 0;

        switch (tecla.getKeyType()) {
            case ArrowUp    -> { ny--; dy = -1; }
            case ArrowDown  -> { ny++; dy =  1; }
            case ArrowLeft  -> { nx--; dx = -1; }
            case ArrowRight -> { nx++; dx =  1; }
            default -> { return; }
        }


        NpcComerciants npc = trobaNpcA(nx, ny);
        if (npc != null) {
            npcActual = npc;
            enigmaInput = "";
            estat = npc.isEnigmaResolt() ? Estat.COMERCIANT : Estat.ENIGMA;
            return;
        }


        Enemic enemic = trobaEnemicA(nx, ny);
        if (enemic != null) {
            enemicCombat = enemic;
            logCombat.clear();
            afegeixLog("T'enfrentes al " + enemic.getClass().getSimpleName().toUpperCase() + "!");
            estat = Estat.COMBAT;
            GestorMusica.reprodueix(esBoss(enemic) ? GestorMusica.Pista.BOSS : GestorMusica.Pista.COMBAT);
            return;
        }

        if (!mapa.esPasable(nx, ny)) return;


        char simbolDesti = mapa.getCelles()[ny][nx];
        if (TipusTerra.de(simbolDesti) == TipusTerra.AIGUA) {
            if (!esperantSegonaAigua || aiguaNx != nx || aiguaNy != ny) {
                esperantSegonaAigua = true; aiguaNx = nx; aiguaNy = ny;
                return;

            }
            esperantSegonaAigua = false;
        } else {
            esperantSegonaAigua = false;
        }


        if (simbolDesti == '<') {
            passaSeguantPis(); return;
        }

        jugador.setX(nx); jugador.setY(ny);
        jugador.setEstatJugador(Jugador.EstatJugador.MOVIMENT);

        if (TipusTerra.de(mapa.getCelles()[ny][nx]) == TipusTerra.GEL) {

    while (true) {

        int segX = jugador.getX() + dx;
        int segY = jugador.getY() + dy;

        if (!mapa.esPasable(segX, segY)) {
            break;
        }

        char seguent = mapa.getCelles()[segY][segX];

        jugador.setX(segX);
        jugador.setY(segY);

        if (TipusTerra.de(seguent) != TipusTerra.GEL) {
            break;
        }
    }
}

        recullItemSiNHiHa(nx, ny);
        tickTorn();

        if (jugador.esMort()) corrent = false;
    }

    private void tickTorn() {
        jugador.tickVeri(); jugador.tickFoc(); jugador.tickGel();
        TipusTerra terra = TipusTerra.de(
            mapa.getCelles()[jugador.getY()][jugador.getX()]);
        for (Enemic e : enemics) {
            if (!e.isActiu()) continue;
            int radEfectiu = switch (terra) {
                case GESPA -> (int)(e.getRadDeteccio() * 0.5);
                case METAL -> (int)(e.getRadDeteccio() * 2.0);
                default    -> e.getRadDeteccio();
            };
            e.actualitzaIAambRadi(jugador, radEfectiu);
        }
    }

    private boolean esBoss(Enemic e) {
        return e instanceof Drac || e instanceof Gegant || e instanceof NaMariaEnganxa;
    }

    private void passaSeguantPis() {
        pisActual++;
        if (pisActual > FITXERS_PISOS.length) {
    GestorMusica.reprodueix(GestorMusica.Pista.VICTORIA);
    estat = Estat.VICTORIA;
    return;
}
        try {
            fitxerMapa = FITXERS_PISOS[pisActual - 1];
            mapa = CarregadorMapa.carrega(fitxerMapa);
            jugador.setX(trobaInicialX()); jugador.setY(trobaInicialY());
            enemics.clear(); itemsMapa.clear(); npcs.clear();
            carregaEnemics(); carregaItemsMapa(); carregaNpcs();
            GestorMusica.reprodueix(GestorMusica.Pista.valueOf("PIS_" + pisActual));
        } catch (Exception ex) { corrent = false; }
    }

    private void carregaNpcs() {
        char[][] celles = mapa.getCelles();
        for (int y = 0; y < celles.length; y++)
            for (int x = 0; x < celles[y].length; x++)
                if (celles[y][x] == 'N') {
                    npcs.add(new NpcComerciants(x, y, pisActual));
                }
    }

    private NpcComerciants trobaNpcA(int x, int y) {
        for (NpcComerciants n : npcs)
            if (n.getX() == x && n.getY() == y) return n;
        return null;
    }

    private void gestionaEnigma(KeyStroke tecla) {
        if (tecla.getKeyType() == KeyType.Escape) { estat = Estat.MON; return; }
        if (tecla.getKeyType() == KeyType.Enter) {
            if (npcActual.comprovaSolucio(enigmaInput)) {
                estat = Estat.COMERCIANT;
            } else {
                enigmaInput = "";
            }
            return;
        }
        if (tecla.getKeyType() == KeyType.Backspace && enigmaInput.length() > 0)
            enigmaInput = enigmaInput.substring(0, enigmaInput.length() - 1);
        if (tecla.getKeyType() == KeyType.Character)
            enigmaInput += tecla.getCharacter();
    }

    private void gestionaComerciants(KeyStroke tecla) {
        if (tecla.getKeyType() == KeyType.Escape || tecla.getKeyType() == KeyType.Enter)
            estat = Estat.MON;
    }


    private Enemic trobaEnemicA(int x, int y) {
        for (Enemic e : enemics)
            if (e.isActiu() && e.getX() == x && e.getY() == y) return e;
        return null;
    }

    private void recullItemSiNHiHa(int x, int y) {
        ItemMapa trobat = null;
        for (ItemMapa im : itemsMapa) {
            if (im.getX() == x && im.getY() == y) { trobat = im; break; }
        }
        if (trobat == null) return;
        jugador.afegeixItem(trobat.getItem());
        mapa.setCella(x, y, '.');
        itemsMapa.remove(trobat);
    }

    private void carregaItemsMapa() {
        char[][] celles = mapa.getCelles();
        int comptador = 0;
        for (int y = 0; y < celles.length; y++) {
            for (int x = 0; x < celles[y].length; x++) {
                if (celles[y][x] == 'i') {
                    String id = switch (comptador % 4) {
                        case 0 -> "pocio-vida";
                        case 1 -> "pocio-veri";
                        case 2 -> "pocio-foc";
                        default -> "pocio-gel";
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
            String idMapaActual = null;
            List<PosicioEnemic> posicions = config.getPosicionsPerMapa(idMapaActual);
            if (!posicions.isEmpty()) {
                for (PosicioEnemic p : posicions) {
                    Enemic enemic = creaEnemic(p.simbol, p.x, p.y);
                    if (enemic != null) enemics.add(enemic);
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
        Enemic enemic = switch (simbol) {
            case "e", "d" -> new DimoniBoiet(x, y);
            case "B"      -> new Bubota(x, y);
            case "D"      -> new Drac(x, y);
            case "G"      -> new Gegant(x, y);
            case "M"      -> new NaMariaEnganxa(x, y);
            default       -> null;
        };
        if (enemic != null && config != null) {
            TipusEnemic def = config.getTipusEnemic(simbol);
            if (def != null) enemic.aplicaDefinicio(def.vida, def.atac, def.radi, def.colorR, def.colorG, def.colorB, def.artAscii);
        }
        return enemic;
    }

    @Override
    protected void renderitza() {
        try {
            if (estat == Estat.MENU_INICIAL) {
                renderer.dibuixaMenuInicial(opcioMenuInicial, OPCIONS_INICIALS);
                return;
            }
            if (estat == Estat.PAUSA) {
                renderer.dibuixaPausa(opcioMenuPausa, OPCIONS_PAUSA);
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



            boolean[][] visible = null;
            boolean[][] explorat = null;
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
        for (int y = 0; y < celles.length; y++)
            for (int x = 0; x < celles[y].length; x++)
                if (celles[y][x] == '@') { mapa.setCella(x, y, '.'); return x; }
        for (int y = 0; y < celles.length; y++)
            for (int x = 0; x < celles[y].length; x++)
                if (celles[y][x] == '.') return x;
        return 1;
    }

    private int trobaInicialY() {
        char[][] celles = mapa.getCelles();
        for (int y = 0; y < celles.length; y++)
            for (int x = 0; x < celles[y].length; x++)
                if (celles[y][x] == '@') return y;
        for (int y = 0; y < celles.length; y++)
            for (int x = 0; x < celles[y].length; x++)
                if (celles[y][x] == '.') return y;
        return 1;
    }
}