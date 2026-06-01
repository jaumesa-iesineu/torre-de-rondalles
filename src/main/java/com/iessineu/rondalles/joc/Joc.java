/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iessineu.rondalles.joc;

import com.iessineu.rondalles.combat.SistemaCombat;
import com.iessineu.rondalles.entitats.Bubota;
import com.iessineu.rondalles.entitats.DimoniBoiet;
import com.iessineu.rondalles.entitats.Drac;
import com.iessineu.rondalles.entitats.Gegant;
import com.iessineu.rondalles.entitats.NaMariaEnganxa;
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

/**
 *
 * @author jaume, dani, sergi, kanhai i pere
 */
public class Joc extends Motor {

    //el mapa actual carregat des fitxer .game
    private Mapa mapa;

    private Jugador jugador;

    private List<Enemic> enemics; //tots els enemics carregats al mapa
    private List<ItemMapa> itemsMapa = new ArrayList<>(); //items que hi ha al terra
    private Enemic enemicCombat = null; //l'enemic amb qui estam lluitant ara mateix
    private List<String> logCombat = new ArrayList<>();
    private static final int MAX_LOG = 3;

    //el fitxer .game que hem de carregar
    private String fitxerMapa;

    //fog of war: caselles que el jugador ja ha vist alguna vegada
    private boolean[][] explorat;
    //últim caràcter vist a cada casella (inclou items i enemics)
    private char[][] mapaRecord;

    private static final int RADI_VISIO = 10;

    //índex de l'opció seleccionada al menú de pausa (0=reanudar, 1=guardar, 2=sortir)
    private int opcioMenuPausa = 0;
    private static final String[] OPCIONS_PAUSA = {"Reanudar", "Guardar", "Sortir"};

    public Joc(String fitxerMapa) {
        this.fitxerMapa = fitxerMapa;
    }

    public Joc(String fitxerMapa, boolean mut) {
        this.fitxerMapa = fitxerMapa;
        this.mut = mut;
    }

    @Override
    protected void init() throws Exception {
        //creim el renderitzador que gestiona la pantalla
        renderer = new Renderitzador();

        //carregam el mapa des del fitxer
        mapa = CarregadorMapa.carrega(fitxerMapa);

        //posam el jugador a la primera posició lliure del mapa
        jugador = new Jugador(trobaInicialX(), trobaInicialY());

        enemics = new ArrayList<>();

        carregaEnemics(); //escanejam les 'e' del mapa i cream els enemics
        carregaItemsMapa(); //escanejam les 'i' del mapa i posam els items

        explorat = new boolean[mapa.getAlcada()][mapa.getAmplada()];
        mapaRecord = new char[mapa.getAlcada()][mapa.getAmplada()];
        for (char[] fila : mapaRecord) java.util.Arrays.fill(fila, ' ');

        //l'estat s'establirà a MENU_INICIAL des de Motor.start()
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

        //dins del joc, ESC obre el menú de pausa
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
        //ENTER o barra espai inicia la partida
        if (tecla.getKeyType() == KeyType.Enter) {
            estat = Estat.MON;
            return;
        }
        if (tecla.getKeyType() == KeyType.Character) {
            char c = tecla.getCharacter();
            if (c == ' ') { estat = Estat.MON; return; }
            if (c == 's' || c == 'S') { corrent = false; return; } //sortir
        }
        //tecles de fletxa per seleccionar: 1=iniciar, 2=sortir
        if (tecla.getKeyType() == KeyType.Escape) { corrent = false; }
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
            //ESC dins pausa torna al joc
            estat = Estat.MON;
            return;
        }
        if (tecla.getKeyType() == KeyType.Enter) {
            switch (opcioMenuPausa) {
                case 0 -> estat = Estat.MON;       //reanudar
                case 1 -> { /* guardar (per implementar) */ estat = Estat.MON; }
                case 2 -> corrent = false;          //sortir
            }
        }
        //tecles ràpides: r=reanudar, g=guardar, x=sortir
        if (tecla.getKeyType() == KeyType.Character) {
            char c = tecla.getCharacter();
            if (c == 'r' || c == 'R') { estat = Estat.MON; return; }
            if (c == 'x' || c == 'X') { corrent = false; }
        }
    }

    private void gestionaCombat(KeyStroke tecla) { //gestiona les tecles durant el combat
        if (tecla.getKeyType() != KeyType.Character) return;
        char c = tecla.getCharacter();

        if (c >= '1' && c <= '9') { //usar ítem no gasta torn de l'enemic
            jugador.usaItem(c - '1');
            return;
        }

        if (c == 'f' || c == 'F') { //fugir torna al mapa
            enemicCombat = null;
            estat = Estat.MON;
            return;
        }

        if (c == 'a' || c == 'A') { //atacar
            String nom = enemicCombat.getClass().getSimpleName().toUpperCase();
            int danyFet = SistemaCombat.atacaEnemic(jugador, enemicCombat);
            afegeixLog("Has atacat! " + nom + " ha rebut " + danyFet + " de dany.");
            if (enemicCombat.esMort()) {
                afegeixLog(nom + " ha caigut!");
                enemics.remove(enemicCombat);
                enemicCombat = null;
                estat = Estat.MON;
                return;
            }
            SistemaCombat.tickEnemics(enemicCombat);
            jugador.tickVeri();
            jugador.tickFoc();
            jugador.tickGel();
            int danyRebut = SistemaCombat.atacaJugador(enemicCombat, jugador);
            afegeixLog(nom + " contraataca! Has rebut " + danyRebut + " de dany.");
            if (jugador.esMort()) corrent = false;
        }
    }

    private void afegeixLog(String msg) {
        logCombat.add(msg);
        if (logCombat.size() > MAX_LOG) logCombat.remove(0);
    }

    private void gestionaMoviment(KeyStroke tecla) { //gestiona el moviment pel mapa
        if (tecla.getKeyType() == KeyType.Character) {
            char c = tecla.getCharacter();
            if (c == 'e' || c == 'E') {
                for (Enemic e : enemics) if (e.isActiu()) e.actualitzaIA(jugador, mapa.getCelles());
                estat = Estat.INVENTARI;
                return;
            }
            if (c >= '1' && c <= '9') { //usar ítem fora de combat també gasta torn
                jugador.usaItem(c - '1');
                jugador.tickVeri();
                jugador.tickFoc();
                jugador.tickGel();
                if (jugador.esMort()) corrent = false;
                return;
            }
        }

        int nx = jugador.getX();
        int ny = jugador.getY();

        switch (tecla.getKeyType()) {
            case ArrowUp    -> ny--;
            case ArrowDown  -> ny++;
            case ArrowLeft  -> nx--;
            case ArrowRight -> nx++;
            default -> { return; }
        }

        Enemic enemic = trobaEnemicA(nx, ny); //si hi ha un enemic, iniciam combat
        if (enemic != null) {
            enemicCombat = enemic;
            logCombat.clear();
            afegeixLog("T'enfrentes al " + enemic.getClass().getSimpleName().toUpperCase() + "!");
            estat = Estat.COMBAT;
            return;
        }

        if (mapa.esPasable(nx, ny)) {
            jugador.setX(nx);
            jugador.setY(ny);
            jugador.setEstatJugador(Jugador.EstatJugador.MOVIMENT);

            recullItemSiNHiHa(nx, ny);
            jugador.tickVeri();

            for (Enemic e : enemics) {
                if (e.isActiu()) e.actualitzaIA(jugador, mapa.getCelles());
            }
        }

        if (jugador.esMort()) corrent = false;
    }

    private Enemic trobaEnemicA(int x, int y) { //cerca si hi ha un enemic a la posició donada
        for (Enemic e : enemics)
            if (e.isActiu() && e.getX() == x && e.getY() == y) return e;
        return null;
    }

    private void recullItemSiNHiHa(int x, int y) { //si hi ha un item a (x,y) el recull
        ItemMapa trobat = null;
        for (ItemMapa im : itemsMapa) {
            if (im.getX() == x && im.getY() == y) { trobat = im; break; }
        }
        if (trobat == null) return;
        jugador.afegeixItem(trobat.getItem());
        mapa.setCella(x, y, '.'); //treu la 'i' del mapa
        itemsMapa.remove(trobat);
    }

    private void carregaItemsMapa() { //escana totes les 'i' del mapa i crea items alternats
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
        char[][] celles = mapa.getCelles();
        for (int y = 0; y < celles.length; y++) {
            for (int x = 0; x < celles[y].length; x++) {
                Enemic enemic = switch (celles[y][x]) {
                    case 'e', 'd' -> new DimoniBoiet(x, y);
                    case 'B'      -> new Bubota(x, y);
                    case 'D'      -> new Drac(x, y);
                    case 'G'      -> new Gegant(x, y);
                    case 'M'      -> new NaMariaEnganxa(x, y);
                    default       -> null;
                };
                if (enemic != null) {
                    enemics.add(enemic);
                    mapa.setCella(x, y, '.');
                }
            }
        }
    }

    @Override
    protected void renderitza() {
        try {
            if (estat == Estat.MENU_INICIAL) {
                renderer.dibuixaMenuInicial();
                return;
            }
            if (estat == Estat.PAUSA) {
                renderer.dibuixaPausa(opcioMenuPausa, OPCIONS_PAUSA);
                return;
            }
            List<Entitat> totes = new ArrayList<>(enemics);
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

    //Bresenham des del jugador fins a (x1,y1); retorna false si una paret talla la línia
    private boolean teLiniaDVista(int x0, int y0, int x1, int y1, char[][] celles) {
        int dx = Math.abs(x1 - x0), dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        int cx = x0, cy = y0;
        while (cx != x1 || cy != y1) {
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; cx += sx; }
            if (e2 < dx)  { err += dx; cy += sy; }
            if (cx == x1 && cy == y1) break;
            if (cy >= 0 && cy < celles.length && cx >= 0 && cx < celles[cy].length)
                if (celles[cy][cx] == '#') return false;
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
                double dist = Math.sqrt((x - jx) * (x - jx) + (y - jy) * (y - jy));
                if (dist > RADI_VISIO) continue;
                if (teLiniaDVista(jx, jy, x, y, celles)) {
                    visible[y][x] = true;
                    explorat[y][x] = true;
                    mapaRecord[y][x] = celles[y][x];
                }
            }
        }
        //items i enemics visibles també es recorden
        for (ItemMapa im : itemsMapa) {
            if (visible[im.getY()][im.getX()]) mapaRecord[im.getY()][im.getX()] = im.getItem().getSimbol();
        }
        for (Enemic e : enemics) {
            if (e.isActiu() && visible[e.getY()][e.getX()]) mapaRecord[e.getY()][e.getX()] = e.getSimbol();
        }
        return visible;
    }

    // Cerca '@' al mapa per la posició inicial del jugador; si no n'hi ha, usa la primera casella lliure
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