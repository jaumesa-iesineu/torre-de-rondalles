package com.iessineu.rondalles.db;

import com.iessineu.rondalles.joc.CarregadorGame;
import com.iessineu.rondalles.joc.ConfigGame;
import com.iessineu.rondalles.joc.MapaConfig;
import com.iessineu.rondalles.joc.PosicioEnemic;
import com.iessineu.rondalles.joc.PosicioItem;
import com.iessineu.rondalles.joc.PosicioNpc;
import com.iessineu.rondalles.joc.PosicioPorta;
import com.iessineu.rondalles.joc.TipusEnemic;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//bolca tota sa configuracio del joc (game.json i es seus subfitxers) a una BD SQLite
//nomes serveix de copia: si hi ha game.json es fa servir aquest, i sa BD es refresca sencera
//si no es troba game.json (per exemple en una distribucio sense recursos), es carrega d'aqui
public class PartidaRepository {

    private static final String FITXER_BD = "rondalles.db";
    private static final String URL = "jdbc:sqlite:" + FITXER_BD;

    //carrega es game.json per defecte i en bolca tota sa info a la BD
    public static void inicialitzaDefecte() {
        try {
            inicialitza(CarregadorGame.carrega("game.json"));
        } catch (Exception e) {
            System.err.println("No s'ha pogut inicialitzar la BD per defecte: " + e.getMessage());
        }
    }

    //esborra sa BD vella (si n'hi havia una amb estructura antiga) i la torna a crear
    //sencera amb ses dades actuals de sa config rebuda. Sa BD es nomes una copia del JSON,
    //per tant cada vegada que tenim es JSON disponible la refrescam del tot.
    public static void inicialitza(ConfigGame config) {
        esborraBD();
        try (Connection conn = DriverManager.getConnection(URL);
             Statement st = conn.createStatement()) {

            creaTaules(st);

            ompleConfiguracio(conn, config);
            ompleControls(conn, config);
            ompleMusica(conn, config);
            ompleSfx(conn, config);
            ompleSimbols(conn, config);
            ompleColorsPresets(conn, config);
            ompleTerrenys(conn, config);
            ompleMapes(conn, config);
            ompleTipusEnemics(conn, config);
            omplePosicionsEnemics(conn, config);
            ompleCatalogItems(conn);
            omplePosicionsItems(conn, config);
            omplePosicionsPortes(conn, config);
            ompleTipusNpcs(conn, config);
            omplePosicionsNpcs(conn, config);
            ompleEquipamentInicial(conn, config);
            ompleJugador(conn, config);
            ompleTipusPersonatge(conn, config);
            omplePersonatgeCustom(conn, config);
            ompleEnigmes(conn, config);
            ompleChetos(conn, config);
            ompleTexts(conn, config);

        } catch (Exception e) {
            System.err.println("Error inicialitzant BD: " + e.getMessage());
        }
    }

    private static void esborraBD() {
        File f = new File(FITXER_BD);
        if (f.exists()) f.delete();
    }

    // ---------------------------------------------------------------
    //  CREACIO DE TAULES
    // ---------------------------------------------------------------

    private static void creaTaules(Statement st) throws Exception {
        st.executeUpdate("""
            CREATE TABLE configuracio (
                clau  TEXT PRIMARY KEY,
                valor TEXT
            )""");

        st.executeUpdate("""
            CREATE TABLE controls (
                accio TEXT PRIMARY KEY,
                tecla TEXT NOT NULL
            )""");

        st.executeUpdate("""
            CREATE TABLE musica (
                situacio TEXT PRIMARY KEY,
                fitxer   TEXT NOT NULL
            )""");

        st.executeUpdate("""
            CREATE TABLE sfx (
                esdeveniment TEXT PRIMARY KEY,
                fitxer       TEXT NOT NULL
            )""");

        st.executeUpdate("""
            CREATE TABLE simbols (
                tipus  TEXT NOT NULL,
                simbol TEXT NOT NULL,
                PRIMARY KEY (tipus, simbol)
            )""");

        st.executeUpdate("""
            CREATE TABLE colors_presets (
                nom     TEXT PRIMARY KEY,
                color_r INTEGER NOT NULL,
                color_g INTEGER NOT NULL,
                color_b INTEGER NOT NULL
            )""");

        st.executeUpdate("""
            CREATE TABLE terrenys (
                simbols   TEXT PRIMARY KEY,
                nom       TEXT NOT NULL,
                amagat    TEXT,
                color_r   INTEGER NOT NULL,
                color_g   INTEGER NOT NULL,
                color_b   INTEGER NOT NULL,
                fons_r    INTEGER NOT NULL,
                fons_g    INTEGER NOT NULL,
                fons_b    INTEGER NOT NULL,
                velocitat INTEGER NOT NULL DEFAULT 1,
                llisca    INTEGER NOT NULL DEFAULT 0,
                mod_radi  REAL    NOT NULL DEFAULT 1.0,
                mal       INTEGER NOT NULL DEFAULT 0
            )""");

        st.executeUpdate("""
            CREATE TABLE mapes (
                id     TEXT PRIMARY KEY,
                fitxer TEXT NOT NULL,
                ordre  INTEGER NOT NULL
            )""");

        st.executeUpdate("""
            CREATE TABLE tipus_enemics (
                simbols                TEXT PRIMARY KEY,
                nom                    TEXT NOT NULL,
                vida                   INTEGER NOT NULL,
                atac                   INTEGER NOT NULL,
                radi                   INTEGER NOT NULL,
                color_r                INTEGER NOT NULL,
                color_g                INTEGER NOT NULL,
                color_b                INTEGER NOT NULL,
                velocitat              INTEGER NOT NULL DEFAULT 1,
                travessa_parets        INTEGER NOT NULL DEFAULT 0,
                estatica               INTEGER NOT NULL DEFAULT 0,
                patro_ia               TEXT    NOT NULL DEFAULT 'perseguir',
                pacman_previsions      INTEGER NOT NULL DEFAULT 4,
                pacman_flanc_passes    INTEGER NOT NULL DEFAULT 4,
                requereix_descobriment INTEGER NOT NULL DEFAULT 0,
                es_boss                INTEGER NOT NULL DEFAULT 0,
                clau_dropejada         TEXT,
                art_fitxer             TEXT,
                art_ascii              TEXT,
                game_over              TEXT
            )""");

        st.executeUpdate("""
            CREATE TABLE posicions_enemics (
                id     INTEGER PRIMARY KEY AUTOINCREMENT,
                mapa   TEXT NOT NULL,
                simbol TEXT NOT NULL,
                x      INTEGER NOT NULL,
                y      INTEGER NOT NULL,
                area   INTEGER NOT NULL DEFAULT 0
            )""");

        //catàleg d'items (armes, armadures, claus, pocions): info de referencia, ve de items.json
        st.executeUpdate("""
            CREATE TABLE catalog_items (
                id          TEXT PRIMARY KEY,
                categoria   TEXT NOT NULL,
                nom         TEXT NOT NULL,
                pes         INTEGER NOT NULL DEFAULT 0,
                simbol      TEXT,
                atac        INTEGER,
                rang        INTEGER,
                defensa     INTEGER,
                slot        TEXT,
                tipus_pocio TEXT,
                valor       INTEGER,
                planta      INTEGER,
                tier        INTEGER NOT NULL DEFAULT 0
            )""");

        st.executeUpdate("""
            CREATE TABLE posicions_items (
                id      INTEGER PRIMARY KEY AUTOINCREMENT,
                mapa    TEXT NOT NULL,
                item_id TEXT NOT NULL,
                x       INTEGER NOT NULL,
                y       INTEGER NOT NULL
            )""");

        st.executeUpdate("""
            CREATE TABLE posicions_portes (
                id              INTEGER PRIMARY KEY AUTOINCREMENT,
                mapa            TEXT NOT NULL,
                x               INTEGER NOT NULL,
                y               INTEGER NOT NULL,
                es_canvi_planta INTEGER NOT NULL DEFAULT 0,
                clau_id         TEXT
            )""");

        st.executeUpdate("""
            CREATE TABLE tipus_npcs (
                simbol   TEXT PRIMARY KEY,
                nom      TEXT NOT NULL,
                dialeg   TEXT,
                color_r  INTEGER,
                color_g  INTEGER,
                color_b  INTEGER,
                estatica INTEGER NOT NULL DEFAULT 1,
                art_fitxer TEXT
            )""");

        st.executeUpdate("""
            CREATE TABLE posicions_npcs (
                id     INTEGER PRIMARY KEY AUTOINCREMENT,
                mapa   TEXT NOT NULL,
                simbol TEXT NOT NULL,
                x      INTEGER NOT NULL,
                y      INTEGER NOT NULL
            )""");

        st.executeUpdate("""
            CREATE TABLE equipament_inicial (
                categoria TEXT NOT NULL,
                item_id   TEXT NOT NULL,
                PRIMARY KEY (categoria, item_id)
            )""");

        st.executeUpdate("""
            CREATE TABLE jugador (
                clau  TEXT PRIMARY KEY,
                valor TEXT
            )""");

        st.executeUpdate("""
            CREATE TABLE tipus_personatge (
                id                TEXT PRIMARY KEY,
                nom               TEXT NOT NULL,
                descripcio        TEXT,
                passiu            TEXT,
                descripcio_passiu TEXT,
                vida_maxima       INTEGER NOT NULL,
                atac              INTEGER NOT NULL,
                velocitat         INTEGER NOT NULL,
                evasio            INTEGER NOT NULL,
                pes_maxim         INTEGER NOT NULL
            )""");

        st.executeUpdate("""
            CREATE TABLE personatge_custom (
                clau  TEXT PRIMARY KEY,
                valor TEXT
            )""");

        st.executeUpdate("""
            CREATE TABLE enigmes (
                planta INTEGER PRIMARY KEY
            )""");

        st.executeUpdate("""
            CREATE TABLE dites (
                id        INTEGER PRIMARY KEY AUTOINCREMENT,
                planta    INTEGER NOT NULL,
                nivell    INTEGER NOT NULL,
                pregunta  TEXT NOT NULL,
                resposta  TEXT NOT NULL
            )""");

        st.executeUpdate("""
            CREATE TABLE items_venda (
                id      INTEGER PRIMARY KEY AUTOINCREMENT,
                planta  INTEGER NOT NULL,
                item_id TEXT NOT NULL,
                ordre   INTEGER NOT NULL
            )""");

        st.executeUpdate("""
            CREATE TABLE chetos (
                accio     TEXT PRIMARY KEY,
                sequencia TEXT NOT NULL
            )""");

        st.executeUpdate("""
            CREATE TABLE texts (
                clau  TEXT PRIMARY KEY,
                valor TEXT
            )""");
    }

    // ---------------------------------------------------------------
    //  OMPLIMENT DE TAULES (des de sa ConfigGame ja carregada del JSON)
    // ---------------------------------------------------------------

    private static void ompleConfiguracio(Connection conn, ConfigGame config) throws Exception {
        ConfigGame.Configuracio c = config.configuracio;
        if (c == null) return;
        try (PreparedStatement ps = clauValor(conn, "configuracio")) {
            desa(ps, "mapaInicial", c.mapaInicial);
            desa(ps, "radiLlanterna", c.radiLlanterna);
            desa(ps, "ampleHud", c.ampleHud);
            desa(ps, "radiVisio", c.radiVisio);
            desa(ps, "msPasGel", c.msPasGel);
            desa(ps, "tornsDesorientacioGel", c.tornsDesorientacioGel);
            desa(ps, "maxLog", c.maxLog);
            desa(ps, "maxSlotsInventari", c.maxSlotsInventari);
            desa(ps, "danyVeri", c.danyVeri);
            desa(ps, "penalitzacioFoc", c.penalitzacioFoc);
            desa(ps, "penalitzacioGel", c.penalitzacioGel);
            desa(ps, "llindCarregaNormal", c.llindCarregaNormal);
            desa(ps, "llindCarregaPesat", c.llindCarregaPesat);
            desa(ps, "penalitzacioEvasioCarrega", c.penalitzacioEvasioCarrega);
            desa(ps, "penalitzacioVelNormal", c.penalitzacioVelNormal);
            desa(ps, "penalitzacioVelPesat", c.penalitzacioVelPesat);
            desa(ps, "pctSortRondalla", c.pctSortRondalla);
            desa(ps, "bonusEvasioLleugera", c.bonusEvasioLleugera);
        }
    }

    private static void ompleControls(Connection conn, ConfigGame config) throws Exception {
        ConfigGame.ControlsConfig ctrl = config.controls;
        if (ctrl == null) return;
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO controls VALUES (?,?)")) {
            desaAccio(ps, "atacar", ctrl.atacar);
            desaAccio(ps, "fugir", ctrl.fugir);
            desaAccio(ps, "esquivar", ctrl.esquivar);
            desaAccio(ps, "inventari", ctrl.inventari);
            desaAccio(ps, "interactuar", ctrl.interactuar);
            desaAccio(ps, "mourePes", ctrl.mourePes);
        }
    }

    private static void desaAccio(PreparedStatement ps, String accio, char tecla) throws Exception {
        ps.setString(1, accio);
        ps.setString(2, String.valueOf(tecla));
        ps.executeUpdate();
    }

    private static void ompleMusica(Connection conn, ConfigGame config) throws Exception {
        ConfigGame.MusicaConfig m = config.musica;
        if (m == null) return;
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO musica VALUES (?,?)")) {
            desaSituacio(ps, "menu", m.menu);
            desaSituacio(ps, "pis_1", m.pis_1);
            desaSituacio(ps, "pis_2", m.pis_2);
            desaSituacio(ps, "pis_3", m.pis_3);
            desaSituacio(ps, "pis_4", m.pis_4);
            desaSituacio(ps, "pis_5", m.pis_5);
            desaSituacio(ps, "combat", m.combat);
            desaSituacio(ps, "boss", m.boss);
            desaSituacio(ps, "victoria", m.victoria);
            desaSituacio(ps, "gameOver", m.gameOver);
        }
    }

    private static void desaSituacio(PreparedStatement ps, String situacio, String fitxer) throws Exception {
        ps.setString(1, situacio);
        ps.setString(2, fitxer);
        ps.executeUpdate();
    }

    private static void ompleSfx(Connection conn, ConfigGame config) throws Exception {
        if (config.sfx == null) return;
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO sfx VALUES (?,?)")) {
            for (Map.Entry<String, String> e : config.sfx.entrySet()) {
                ps.setString(1, e.getKey());
                ps.setString(2, e.getValue());
                ps.executeUpdate();
            }
        }
    }

    private static void ompleSimbols(Connection conn, ConfigGame config) throws Exception {
        ConfigGame.SimbolsConfig s = config.simbols;
        if (s == null) return;
        try (PreparedStatement ps = conn.prepareStatement("INSERT OR IGNORE INTO simbols VALUES (?,?)")) {
            desaSimbols(ps, "mur", s.mur);
            desaSimbols(ps, "portaTancada", s.portaTancada);
            desaSimbols(ps, "portaOberta", s.portaOberta);
            desaSimbols(ps, "portaBloquejada", s.portaBloquejada);
            desaSimbols(ps, "spawnJugador", s.spawnJugador);
            desaSimbols(ps, "escalaBaix", s.escalaBaix);
            desaSimbols(ps, "marcadorItem", s.marcadorItem);
            desaSimbols(ps, "marcadorPorta", s.marcadorPorta);
            desaSimbols(ps, "marcadorNpc", s.marcadorNpc);
        }
    }

    private static void desaSimbols(PreparedStatement ps, String tipus, List<Character> simbols) throws Exception {
        if (simbols == null) return;
        for (char simbol : simbols) {
            ps.setString(1, tipus);
            ps.setString(2, String.valueOf(simbol));
            ps.executeUpdate();
        }
    }

    private static void ompleColorsPresets(Connection conn, ConfigGame config) throws Exception {
        if (config.colorsPresets == null) return;
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO colors_presets VALUES (?,?,?,?)")) {
            for (Map.Entry<String, int[]> e : config.colorsPresets.entrySet()) {
                int[] rgb = e.getValue();
                ps.setString(1, e.getKey());
                ps.setInt(2, rgb[0]);
                ps.setInt(3, rgb[1]);
                ps.setInt(4, rgb[2]);
                ps.executeUpdate();
            }
        }
    }

    private static void ompleTerrenys(Connection conn, ConfigGame config) throws Exception {
        if (config.terrenys == null) return;
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT OR IGNORE INTO terrenys VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)")) {
            for (ConfigGame.TerrenyConfig t : config.terrenys) {
                ps.setString(1, unirCaracters(t.simbols));
                ps.setString(2, t.nom);
                ps.setString(3, t.amagat == '\0' ? null : String.valueOf(t.amagat));
                ps.setInt(4, t.colorR);
                ps.setInt(5, t.colorG);
                ps.setInt(6, t.colorB);
                ps.setInt(7, t.fonsR);
                ps.setInt(8, t.fonsG);
                ps.setInt(9, t.fonsB);
                ps.setInt(10, t.velocitat);
                ps.setInt(11, t.llisca ? 1 : 0);
                ps.setDouble(12, t.modRadi);
                ps.setInt(13, t.mal);
                ps.executeUpdate();
            }
        }
    }

    private static void ompleMapes(Connection conn, ConfigGame config) throws Exception {
        if (config.mapes == null || config.mapes.ordre == null) return;
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO mapes VALUES (?,?,?)")) {
            for (int i = 0; i < config.mapes.ordre.size(); i++) {
                String id = config.mapes.ordre.get(i);
                MapaConfig mc = config.getMapaConfig(id);
                if (mc == null) continue;
                ps.setString(1, id);
                ps.setString(2, mc.fitxer);
                ps.setInt(3, i + 1);
                ps.executeUpdate();
            }
        }
    }

    private static void ompleTipusEnemics(Connection conn, ConfigGame config) throws Exception {
        if (config.enemics == null || config.enemics.tipus == null) return;
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT OR IGNORE INTO tipus_enemics VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")) {
            for (TipusEnemic t : config.enemics.tipus) {
                ps.setString(1, unirTextos(t.simbols));
                ps.setString(2, t.nom);
                ps.setInt(3, t.vida);
                ps.setInt(4, t.atac);
                ps.setInt(5, t.radi);
                ps.setInt(6, t.colorR);
                ps.setInt(7, t.colorG);
                ps.setInt(8, t.colorB);
                ps.setInt(9, t.velocitat);
                ps.setInt(10, t.travessaParets ? 1 : 0);
                ps.setInt(11, t.estatica ? 1 : 0);
                ps.setString(12, t.patroIA);
                ps.setInt(13, t.pacmanPrevisions);
                ps.setInt(14, t.pacmanFlancPasses);
                ps.setInt(15, t.requereixDescobriment ? 1 : 0);
                ps.setInt(16, t.esBoss ? 1 : 0);
                ps.setString(17, t.clauDropejada);
                ps.setString(18, t.artFitxer);
                ps.setString(19, t.artAscii != null ? String.join("\n", t.artAscii) : null);
                ps.setString(20, t.gameOver);
                ps.executeUpdate();
            }
        }
    }

    private static void omplePosicionsEnemics(Connection conn, ConfigGame config) throws Exception {
        if (config.enemics == null || config.enemics.posicions == null) return;
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO posicions_enemics (mapa, simbol, x, y, area) VALUES (?,?,?,?,?)")) {
            for (PosicioEnemic p : config.enemics.posicions) {
                ps.setString(1, p.mapa);
                ps.setString(2, p.simbol);
                ps.setInt(3, p.x);
                ps.setInt(4, p.y);
                ps.setInt(5, p.area);
                ps.executeUpdate();
            }
        }
    }

    //es catàleg d'items no forma part de sa ConfigGame (es carrega a part des d'items.json),
    //per tant el llegim aqui directament com a JSON cru, igual que fa RegistreItems
    private static void ompleCatalogItems(Connection conn) throws Exception {
        com.google.gson.JsonObject arrel = llegeixJsonRecurs("items.json");
        if (arrel == null) return;
        com.google.gson.JsonObject catalog = arrel.getAsJsonObject("catalogItems");
        if (catalog == null) return;

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT OR IGNORE INTO catalog_items VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)")) {
            desaCategoriaItems(ps, catalog, "armes", "ARMA");
            desaCategoriaItems(ps, catalog, "armadures", "ARMADURA");
            desaCategoriaItems(ps, catalog, "claus", "CLAU");
            desaCategoriaItems(ps, catalog, "pocions", "POCIO");
        }
    }

    private static void desaCategoriaItems(PreparedStatement ps, com.google.gson.JsonObject catalog,
            String clauJson, String categoria) throws Exception {
        com.google.gson.JsonArray items = catalog.getAsJsonArray(clauJson);
        if (items == null) return;
        for (com.google.gson.JsonElement el : items) {
            com.google.gson.JsonObject o = el.getAsJsonObject();
            ps.setString(1, text(o, "id"));
            ps.setString(2, categoria);
            ps.setString(3, text(o, "nom"));
            ps.setInt(4, enter(o, "pes", 0));
            ps.setString(5, text(o, "simbol"));
            ps.setObject(6, enterONull(o, "atac"));
            ps.setObject(7, enterONull(o, "rang"));
            ps.setObject(8, enterONull(o, "defensa"));
            ps.setString(9, text(o, "slot"));
            ps.setString(10, text(o, "tipus"));
            ps.setObject(11, enterONull(o, "valor"));
            ps.setObject(12, enterONull(o, "planta"));
            ps.setInt(13, enter(o, "tier", 0));
            ps.executeUpdate();
        }
    }

    private static void omplePosicionsItems(Connection conn, ConfigGame config) throws Exception {
        if (config.items == null || config.items.posicions == null) return;
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO posicions_items (mapa, item_id, x, y) VALUES (?,?,?,?)")) {
            for (PosicioItem p : config.items.posicions) {
                ps.setString(1, p.mapa);
                ps.setString(2, p.id);
                ps.setInt(3, p.x);
                ps.setInt(4, p.y);
                ps.executeUpdate();
            }
        }
    }

    private static void omplePosicionsPortes(Connection conn, ConfigGame config) throws Exception {
        if (config.portes == null || config.portes.posicions == null) return;
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO posicions_portes (mapa, x, y, es_canvi_planta, clau_id) VALUES (?,?,?,?,?)")) {
            for (PosicioPorta p : config.portes.posicions) {
                ps.setString(1, p.mapa);
                ps.setInt(2, p.x);
                ps.setInt(3, p.y);
                ps.setInt(4, p.esPortaCanviPlanta ? 1 : 0);
                ps.setString(5, p.clauId);
                ps.executeUpdate();
            }
        }
    }

    //es tipus de npc venen com a Map generic (List<Object>) perque encara no hi ha un POJO propi
    @SuppressWarnings("unchecked")
    private static void ompleTipusNpcs(Connection conn, ConfigGame config) throws Exception {
        if (config.npcs == null || config.npcs.tipus == null) return;
        try (PreparedStatement ps = conn.prepareStatement("INSERT OR IGNORE INTO tipus_npcs VALUES (?,?,?,?,?,?,?,?)")) {
            for (Object obj : config.npcs.tipus) {
                if (!(obj instanceof Map)) continue;
                Map<String, Object> m = (Map<String, Object>) obj;
                List<Object> simbols = (List<Object>) m.get("simbols");
                String simbol = (simbols != null && !simbols.isEmpty()) ? simbols.get(0).toString() : null;
                if (simbol == null) continue;
                ps.setString(1, simbol);
                ps.setString(2, valorText(m.get("nom")));
                ps.setString(3, valorText(m.get("dialeg")));
                ps.setObject(4, valorEnter(m.get("colorR")));
                ps.setObject(5, valorEnter(m.get("colorG")));
                ps.setObject(6, valorEnter(m.get("colorB")));
                ps.setInt(7, valorBoolea(m.get("estatica")) ? 1 : 0);
                ps.setString(8, valorText(m.get("artFitxer")));
                ps.executeUpdate();
            }
        }
    }

    private static void omplePosicionsNpcs(Connection conn, ConfigGame config) throws Exception {
        if (config.npcs == null || config.npcs.posicions == null) return;
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO posicions_npcs (mapa, simbol, x, y) VALUES (?,?,?,?)")) {
            for (PosicioNpc p : config.npcs.posicions) {
                ps.setString(1, p.mapa);
                ps.setString(2, p.simbol);
                ps.setInt(3, p.x);
                ps.setInt(4, p.y);
                ps.executeUpdate();
            }
        }
    }

    private static void ompleEquipamentInicial(Connection conn, ConfigGame config) throws Exception {
        ConfigGame.EquipamentInicial eq = config.equipamentInicial;
        if (eq == null) return;
        try (PreparedStatement ps = conn.prepareStatement("INSERT OR IGNORE INTO equipament_inicial VALUES (?,?)")) {
            if (eq.arma != null) {
                ps.setString(1, "arma");
                ps.setString(2, eq.arma);
                ps.executeUpdate();
            }
            if (eq.armadures != null) {
                for (String id : eq.armadures) {
                    ps.setString(1, "armadura");
                    ps.setString(2, id);
                    ps.executeUpdate();
                }
            }
        }
    }

    private static void ompleJugador(Connection conn, ConfigGame config) throws Exception {
        ConfigGame.JugadorConfig j = config.jugador;
        if (j == null) return;
        try (PreparedStatement ps = clauValor(conn, "jugador")) {
            desa(ps, "artFitxer", j.artFitxer);
            desa(ps, "vidaMaxima", j.vidaMaxima);
            desa(ps, "atac", j.atac);
            desa(ps, "velocitat", j.velocitat);
            desa(ps, "evasio", j.evasio);
            desa(ps, "pesMaxim", j.pesMaxim);
        }
    }

    private static void ompleTipusPersonatge(Connection conn, ConfigGame config) throws Exception {
        if (config.tipusPersonatge == null) return;
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT OR IGNORE INTO tipus_personatge VALUES (?,?,?,?,?,?,?,?,?,?)")) {
            for (ConfigGame.TipusPersonatgeConfig tp : config.tipusPersonatge) {
                ps.setString(1, tp.id);
                ps.setString(2, tp.nom);
                ps.setString(3, tp.descripcio);
                ps.setString(4, tp.passiu);
                ps.setString(5, tp.descripcioPassiu);
                ps.setInt(6, tp.vidaMaxima);
                ps.setInt(7, tp.atac);
                ps.setInt(8, tp.velocitat);
                ps.setInt(9, tp.evasio);
                ps.setInt(10, tp.pesMaxim);
                ps.executeUpdate();
            }
        }
    }

    private static void omplePersonatgeCustom(Connection conn, ConfigGame config) throws Exception {
        ConfigGame.PersonatgeCustomConfig pc = config.personatgeCustom;
        if (pc == null) return;
        try (PreparedStatement ps = clauValor(conn, "personatge_custom")) {
            desa(ps, "pressupost", pc.pressupost);
            desa(ps, "vidaBase", pc.vidaBase);
            desa(ps, "vidaPerPunt", pc.vidaPerPunt);
            desa(ps, "vidaMax", pc.vidaMax);
            desa(ps, "atacBase", pc.atacBase);
            desa(ps, "atacMax", pc.atacMax);
            desa(ps, "velocitatBase", pc.velocitatBase);
            desa(ps, "velocitatMax", pc.velocitatMax);
            desa(ps, "evasioBase", pc.evasioBase);
            desa(ps, "evasioPerPunt", pc.evasioPerPunt);
            desa(ps, "evasioMax", pc.evasioMax);
            desa(ps, "pesMaxim", pc.pesMaxim);
        }
    }

    private static void ompleEnigmes(Connection conn, ConfigGame config) throws Exception {
        if (config.enigmes == null) return;
        try (PreparedStatement psEnigma = conn.prepareStatement("INSERT OR IGNORE INTO enigmes VALUES (?)");
             PreparedStatement psDita = conn.prepareStatement("INSERT INTO dites (planta, nivell, pregunta, resposta) VALUES (?,?,?,?)");
             PreparedStatement psVenda = conn.prepareStatement("INSERT INTO items_venda (planta, item_id, ordre) VALUES (?,?,?)")) {

            for (ConfigGame.EnigmeConfig e : config.enigmes) {
                psEnigma.setInt(1, e.planta);
                psEnigma.executeUpdate();

                if (e.dites != null) {
                    for (ConfigGame.DitaConfig d : e.dites) {
                        psDita.setInt(1, e.planta);
                        psDita.setInt(2, d.nivell);
                        psDita.setString(3, d.pregunta);
                        psDita.setString(4, d.resposta);
                        psDita.executeUpdate();
                    }
                }
                if (e.itemsVenda != null) {
                    for (int i = 0; i < e.itemsVenda.size(); i++) {
                        psVenda.setInt(1, e.planta);
                        psVenda.setString(2, e.itemsVenda.get(i));
                        psVenda.setInt(3, i + 1);
                        psVenda.executeUpdate();
                    }
                }
            }
        }
    }

    private static void ompleChetos(Connection conn, ConfigGame config) throws Exception {
        if (config.chetos == null) return;
        try (PreparedStatement ps = conn.prepareStatement("INSERT OR IGNORE INTO chetos VALUES (?,?)")) {
            for (ConfigGame.ChetoConfig c : config.chetos) {
                ps.setString(1, c.accio);
                ps.setString(2, unirTextos(c.sequencia));
                ps.executeUpdate();
            }
        }
    }

    private static void ompleTexts(Connection conn, ConfigGame config) throws Exception {
        ConfigGame.TextsConfig t = config.texts;
        if (t == null) return;
        try (PreparedStatement ps = clauValor(conn, "texts")) {
            desa(ps, "windowTitle", t.windowTitle);
            desa(ps, "headerTitle", t.headerTitle);
            desa(ps, "subtitle", t.subtitle);
            desa(ps, "menuIniciar", t.menuIniciar);
            desa(ps, "menuSortir", t.menuSortir);
            desa(ps, "menuReanudar", t.menuReanudar);
            desa(ps, "menuGuardar", t.menuGuardar);
            desa(ps, "menuCarregar", t.menuCarregar);
            desa(ps, "pauseTitle", t.pauseTitle);
            desa(ps, "pauseInstructions", t.pauseInstructions);
            desa(ps, "pauseResumePista", t.pauseResumePista);
            desa(ps, "menuTornaAcomencar", t.menuTornaAcomencar);
        }
    }

    // ---------------------------------------------------------------
    //  RECONSTRUCCIO DE LA CONFIG DES DE LA BD (quan no hi ha game.json)
    // ---------------------------------------------------------------

    public static ConfigGame carregaConfig() {
        ConfigGame config = new ConfigGame();
        try (Connection conn = DriverManager.getConnection(URL);
             Statement st = conn.createStatement()) {

            carregaConfiguracio(conn, config);
            carregaControls(conn, config);
            carregaMusica(conn, config);
            carregaSfx(conn, config);
            carregaSimbols(conn, config);
            carregaColorsPresets(conn, config);
            carregaTerrenys(conn, config);
            carregaMapes(conn, config);
            carregaEnemics(conn, config);
            carregaItems(conn, config);
            carregaPortes(conn, config);
            carregaNpcs(conn, config);
            carregaEquipamentInicial(conn, config);
            carregaJugador(conn, config);
            carregaTipusPersonatge(conn, config);
            carregaPersonatgeCustom(conn, config);
            carregaEnigmes(conn, config);
            carregaChetos(conn, config);
            carregaTexts(conn, config);

        } catch (Exception e) {
            System.err.println("Error carregant config de BD: " + e.getMessage());
        }
        return config;
    }

    private static void carregaConfiguracio(Connection conn, ConfigGame config) throws Exception {
        Map<String, String> kv = clauValorMap(conn, "configuracio");
        if (kv.isEmpty()) return;
        ConfigGame.Configuracio c = new ConfigGame.Configuracio();
        c.mapaInicial = kv.get("mapaInicial");
        c.radiLlanterna = enter(kv, "radiLlanterna", c.radiLlanterna);
        c.ampleHud = enter(kv, "ampleHud", c.ampleHud);
        c.radiVisio = enter(kv, "radiVisio", c.radiVisio);
        c.msPasGel = enter(kv, "msPasGel", c.msPasGel);
        c.tornsDesorientacioGel = enter(kv, "tornsDesorientacioGel", c.tornsDesorientacioGel);
        c.maxLog = enter(kv, "maxLog", c.maxLog);
        c.maxSlotsInventari = enter(kv, "maxSlotsInventari", c.maxSlotsInventari);
        c.danyVeri = enter(kv, "danyVeri", c.danyVeri);
        c.penalitzacioFoc = enter(kv, "penalitzacioFoc", c.penalitzacioFoc);
        c.penalitzacioGel = enter(kv, "penalitzacioGel", c.penalitzacioGel);
        c.llindCarregaNormal = decimal(kv, "llindCarregaNormal", c.llindCarregaNormal);
        c.llindCarregaPesat = decimal(kv, "llindCarregaPesat", c.llindCarregaPesat);
        c.penalitzacioEvasioCarrega = enter(kv, "penalitzacioEvasioCarrega", c.penalitzacioEvasioCarrega);
        c.penalitzacioVelNormal = enter(kv, "penalitzacioVelNormal", c.penalitzacioVelNormal);
        c.penalitzacioVelPesat = enter(kv, "penalitzacioVelPesat", c.penalitzacioVelPesat);
        c.pctSortRondalla = enter(kv, "pctSortRondalla", c.pctSortRondalla);
        c.bonusEvasioLleugera = enter(kv, "bonusEvasioLleugera", c.bonusEvasioLleugera);
        config.configuracio = c;
    }

    private static void carregaControls(Connection conn, ConfigGame config) throws Exception {
        Map<String, String> accions = llegeixDosCamps(conn, "SELECT accio, tecla FROM controls");
        if (accions.isEmpty()) return;
        ConfigGame.ControlsConfig ctrl = new ConfigGame.ControlsConfig();
        ctrl.atacar = caracter(accions, "atacar", ctrl.atacar);
        ctrl.fugir = caracter(accions, "fugir", ctrl.fugir);
        ctrl.esquivar = caracter(accions, "esquivar", ctrl.esquivar);
        ctrl.inventari = caracter(accions, "inventari", ctrl.inventari);
        ctrl.interactuar = caracter(accions, "interactuar", ctrl.interactuar);
        ctrl.mourePes = caracter(accions, "mourePes", ctrl.mourePes);
        config.controls = ctrl;
    }

    private static void carregaMusica(Connection conn, ConfigGame config) throws Exception {
        Map<String, String> m = llegeixDosCamps(conn, "SELECT situacio, fitxer FROM musica");
        if (m.isEmpty()) return;
        ConfigGame.MusicaConfig musica = new ConfigGame.MusicaConfig();
        musica.menu = m.getOrDefault("menu", musica.menu);
        musica.pis_1 = m.getOrDefault("pis_1", musica.pis_1);
        musica.pis_2 = m.getOrDefault("pis_2", musica.pis_2);
        musica.pis_3 = m.getOrDefault("pis_3", musica.pis_3);
        musica.pis_4 = m.getOrDefault("pis_4", musica.pis_4);
        musica.pis_5 = m.getOrDefault("pis_5", musica.pis_5);
        musica.combat = m.getOrDefault("combat", musica.combat);
        musica.boss = m.getOrDefault("boss", musica.boss);
        musica.victoria = m.getOrDefault("victoria", musica.victoria);
        musica.gameOver = m.getOrDefault("gameOver", musica.gameOver);
        config.musica = musica;
    }

    private static void carregaSfx(Connection conn, ConfigGame config) throws Exception {
        Map<String, String> sfx = llegeixDosCamps(conn, "SELECT esdeveniment, fitxer FROM sfx");
        if (!sfx.isEmpty()) config.sfx = sfx;
    }

    private static void carregaSimbols(Connection conn, ConfigGame config) throws Exception {
        Map<String, List<Character>> agrupats = new LinkedHashMap<>();
        try (ResultSet rs = conn.createStatement().executeQuery("SELECT tipus, simbol FROM simbols")) {
            while (rs.next()) {
                agrupats.computeIfAbsent(rs.getString(1), k -> new ArrayList<>()).add(rs.getString(2).charAt(0));
            }
        }
        if (agrupats.isEmpty()) return;
        ConfigGame.SimbolsConfig s = new ConfigGame.SimbolsConfig();
        s.mur = agrupats.getOrDefault("mur", s.mur);
        s.portaTancada = agrupats.getOrDefault("portaTancada", s.portaTancada);
        s.portaOberta = agrupats.getOrDefault("portaOberta", s.portaOberta);
        s.portaBloquejada = agrupats.getOrDefault("portaBloquejada", s.portaBloquejada);
        s.spawnJugador = agrupats.getOrDefault("spawnJugador", s.spawnJugador);
        s.escalaBaix = agrupats.getOrDefault("escalaBaix", s.escalaBaix);
        s.marcadorItem = agrupats.getOrDefault("marcadorItem", s.marcadorItem);
        s.marcadorPorta = agrupats.getOrDefault("marcadorPorta", s.marcadorPorta);
        s.marcadorNpc = agrupats.getOrDefault("marcadorNpc", s.marcadorNpc);
        config.simbols = s;
    }

    private static void carregaColorsPresets(Connection conn, ConfigGame config) throws Exception {
        Map<String, int[]> colors = new LinkedHashMap<>();
        try (ResultSet rs = conn.createStatement().executeQuery("SELECT nom, color_r, color_g, color_b FROM colors_presets")) {
            while (rs.next()) {
                colors.put(rs.getString(1), new int[]{rs.getInt(2), rs.getInt(3), rs.getInt(4)});
            }
        }
        if (!colors.isEmpty()) config.colorsPresets = colors;
    }

    private static void carregaTerrenys(Connection conn, ConfigGame config) throws Exception {
        List<ConfigGame.TerrenyConfig> terrenys = new ArrayList<>();
        try (ResultSet rs = conn.createStatement().executeQuery(
                "SELECT simbols, nom, amagat, color_r, color_g, color_b, fons_r, fons_g, fons_b, velocitat, llisca, mod_radi, mal FROM terrenys")) {
            while (rs.next()) {
                ConfigGame.TerrenyConfig t = new ConfigGame.TerrenyConfig();
                t.simbols = separarCaracters(rs.getString(1));
                t.nom = rs.getString(2);
                String amagat = rs.getString(3);
                t.amagat = amagat != null && !amagat.isEmpty() ? amagat.charAt(0) : '\0';
                t.colorR = rs.getInt(4);
                t.colorG = rs.getInt(5);
                t.colorB = rs.getInt(6);
                t.fonsR = rs.getInt(7);
                t.fonsG = rs.getInt(8);
                t.fonsB = rs.getInt(9);
                t.velocitat = rs.getInt(10);
                t.llisca = rs.getInt(11) == 1;
                t.modRadi = rs.getDouble(12);
                t.mal = rs.getInt(13);
                terrenys.add(t);
            }
        }
        if (!terrenys.isEmpty()) config.terrenys = terrenys;
    }

    private static void carregaMapes(Connection conn, ConfigGame config) throws Exception {
        ConfigGame.MapesGroup mapes = new ConfigGame.MapesGroup();
        mapes.ordre = new ArrayList<>();
        mapes.registres = new ArrayList<>();
        try (ResultSet rs = conn.createStatement().executeQuery("SELECT id, fitxer FROM mapes ORDER BY ordre")) {
            while (rs.next()) {
                MapaConfig mc = new MapaConfig();
                mc.id = rs.getString(1);
                mc.fitxer = rs.getString(2);
                mapes.ordre.add(mc.id);
                mapes.registres.add(mc);
            }
        }
        if (!mapes.ordre.isEmpty()) config.mapes = mapes;
    }

    private static void carregaEnemics(Connection conn, ConfigGame config) throws Exception {
        ConfigGame.EnemicsGroup enemics = new ConfigGame.EnemicsGroup();
        enemics.tipus = new ArrayList<>();
        enemics.posicions = new ArrayList<>();

        try (ResultSet rs = conn.createStatement().executeQuery("""
                SELECT simbols, nom, vida, atac, radi, color_r, color_g, color_b, velocitat,
                       travessa_parets, estatica, patro_ia, pacman_previsions, pacman_flanc_passes,
                       requereix_descobriment, es_boss, clau_dropejada, art_fitxer, art_ascii, game_over
                FROM tipus_enemics""")) {
            while (rs.next()) {
                TipusEnemic te = new TipusEnemic();
                te.simbols = separarTextos(rs.getString(1));
                te.nom = rs.getString(2);
                te.vida = rs.getInt(3);
                te.atac = rs.getInt(4);
                te.radi = rs.getInt(5);
                te.colorR = rs.getInt(6);
                te.colorG = rs.getInt(7);
                te.colorB = rs.getInt(8);
                te.velocitat = rs.getInt(9);
                te.travessaParets = rs.getInt(10) == 1;
                te.estatica = rs.getInt(11) == 1;
                te.patroIA = rs.getString(12);
                te.pacmanPrevisions = rs.getInt(13);
                te.pacmanFlancPasses = rs.getInt(14);
                te.requereixDescobriment = rs.getInt(15) == 1;
                te.esBoss = rs.getInt(16) == 1;
                te.clauDropejada = rs.getString(17);
                te.artFitxer = rs.getString(18);
                String art = rs.getString(19);
                te.artAscii = art != null ? art.split("\n") : null;
                te.gameOver = rs.getString(20);
                enemics.tipus.add(te);
            }
        }
        try (ResultSet rs = conn.createStatement().executeQuery("SELECT mapa, simbol, x, y, area FROM posicions_enemics")) {
            while (rs.next()) {
                PosicioEnemic pe = new PosicioEnemic();
                pe.mapa = rs.getString(1);
                pe.simbol = rs.getString(2);
                pe.x = rs.getInt(3);
                pe.y = rs.getInt(4);
                pe.area = rs.getInt(5);
                enemics.posicions.add(pe);
            }
        }
        config.enemics = enemics;
    }

    private static void carregaItems(Connection conn, ConfigGame config) throws Exception {
        ConfigGame.ItemsGroup items = new ConfigGame.ItemsGroup();
        items.posicions = new ArrayList<>();
        try (ResultSet rs = conn.createStatement().executeQuery("SELECT mapa, item_id, x, y FROM posicions_items")) {
            while (rs.next()) {
                PosicioItem p = new PosicioItem();
                p.mapa = rs.getString(1);
                p.id = rs.getString(2);
                p.x = rs.getInt(3);
                p.y = rs.getInt(4);
                items.posicions.add(p);
            }
        }
        config.items = items;
    }

    private static void carregaPortes(Connection conn, ConfigGame config) throws Exception {
        ConfigGame.PortesGroup portes = new ConfigGame.PortesGroup();
        portes.posicions = new ArrayList<>();
        try (ResultSet rs = conn.createStatement().executeQuery(
                "SELECT mapa, x, y, es_canvi_planta, clau_id FROM posicions_portes")) {
            while (rs.next()) {
                PosicioPorta p = new PosicioPorta();
                p.mapa = rs.getString(1);
                p.x = rs.getInt(2);
                p.y = rs.getInt(3);
                p.esPortaCanviPlanta = rs.getInt(4) == 1;
                p.clauId = rs.getString(5);
                portes.posicions.add(p);
            }
        }
        config.portes = portes;
    }

    private static void carregaNpcs(Connection conn, ConfigGame config) throws Exception {
        ConfigGame.NpcsGroup npcs = new ConfigGame.NpcsGroup();
        npcs.posicions = new ArrayList<>();
        try (ResultSet rs = conn.createStatement().executeQuery("SELECT mapa, simbol, x, y FROM posicions_npcs")) {
            while (rs.next()) {
                PosicioNpc p = new PosicioNpc();
                p.mapa = rs.getString(1);
                p.simbol = rs.getString(2);
                p.x = rs.getInt(3);
                p.y = rs.getInt(4);
                npcs.posicions.add(p);
            }
        }
        config.npcs = npcs;
    }

    private static void carregaEquipamentInicial(Connection conn, ConfigGame config) throws Exception {
        ConfigGame.EquipamentInicial eq = new ConfigGame.EquipamentInicial();
        eq.armadures = new ArrayList<>();
        boolean trobat = false;
        try (ResultSet rs = conn.createStatement().executeQuery("SELECT categoria, item_id FROM equipament_inicial")) {
            while (rs.next()) {
                trobat = true;
                String categoria = rs.getString(1);
                String itemId = rs.getString(2);
                if ("arma".equals(categoria)) {
                    eq.arma = itemId;
                } else if ("armadura".equals(categoria)) {
                    eq.armadures.add(itemId);
                }
            }
        }
        if (trobat) config.equipamentInicial = eq;
    }

    private static void carregaJugador(Connection conn, ConfigGame config) throws Exception {
        Map<String, String> kv = clauValorMap(conn, "jugador");
        if (kv.isEmpty()) return;
        ConfigGame.JugadorConfig j = new ConfigGame.JugadorConfig();
        j.artFitxer = kv.get("artFitxer");
        j.vidaMaxima = enter(kv, "vidaMaxima", j.vidaMaxima);
        j.atac = enter(kv, "atac", j.atac);
        j.velocitat = enter(kv, "velocitat", j.velocitat);
        j.evasio = enter(kv, "evasio", j.evasio);
        j.pesMaxim = enter(kv, "pesMaxim", j.pesMaxim);
        config.jugador = j;
    }

    private static void carregaTipusPersonatge(Connection conn, ConfigGame config) throws Exception {
        List<ConfigGame.TipusPersonatgeConfig> llista = new ArrayList<>();
        try (ResultSet rs = conn.createStatement().executeQuery(
                "SELECT id, nom, descripcio, passiu, descripcio_passiu, vida_maxima, atac, velocitat, evasio, pes_maxim FROM tipus_personatge")) {
            while (rs.next()) {
                ConfigGame.TipusPersonatgeConfig tp = new ConfigGame.TipusPersonatgeConfig();
                tp.id = rs.getString(1);
                tp.nom = rs.getString(2);
                tp.descripcio = rs.getString(3);
                tp.passiu = rs.getString(4);
                tp.descripcioPassiu = rs.getString(5);
                tp.vidaMaxima = rs.getInt(6);
                tp.atac = rs.getInt(7);
                tp.velocitat = rs.getInt(8);
                tp.evasio = rs.getInt(9);
                tp.pesMaxim = rs.getInt(10);
                llista.add(tp);
            }
        }
        if (!llista.isEmpty()) config.tipusPersonatge = llista;
    }

    private static void carregaPersonatgeCustom(Connection conn, ConfigGame config) throws Exception {
        Map<String, String> kv = clauValorMap(conn, "personatge_custom");
        if (kv.isEmpty()) return;
        ConfigGame.PersonatgeCustomConfig pc = new ConfigGame.PersonatgeCustomConfig();
        pc.pressupost = enter(kv, "pressupost", pc.pressupost);
        pc.vidaBase = enter(kv, "vidaBase", pc.vidaBase);
        pc.vidaPerPunt = enter(kv, "vidaPerPunt", pc.vidaPerPunt);
        pc.vidaMax = enter(kv, "vidaMax", pc.vidaMax);
        pc.atacBase = enter(kv, "atacBase", pc.atacBase);
        pc.atacMax = enter(kv, "atacMax", pc.atacMax);
        pc.velocitatBase = enter(kv, "velocitatBase", pc.velocitatBase);
        pc.velocitatMax = enter(kv, "velocitatMax", pc.velocitatMax);
        pc.evasioBase = enter(kv, "evasioBase", pc.evasioBase);
        pc.evasioPerPunt = enter(kv, "evasioPerPunt", pc.evasioPerPunt);
        pc.evasioMax = enter(kv, "evasioMax", pc.evasioMax);
        pc.pesMaxim = enter(kv, "pesMaxim", pc.pesMaxim);
        config.personatgeCustom = pc;
    }

    private static void carregaEnigmes(Connection conn, ConfigGame config) throws Exception {
        List<ConfigGame.EnigmeConfig> enigmes = new ArrayList<>();
        try (ResultSet rsPlantes = conn.createStatement().executeQuery("SELECT planta FROM enigmes ORDER BY planta")) {
            while (rsPlantes.next()) {
                int planta = rsPlantes.getInt(1);
                ConfigGame.EnigmeConfig e = new ConfigGame.EnigmeConfig();
                e.planta = planta;
                e.dites = new ArrayList<>();
                e.itemsVenda = new ArrayList<>();

                try (PreparedStatement psDites = conn.prepareStatement(
                        "SELECT nivell, pregunta, resposta FROM dites WHERE planta=? ORDER BY nivell")) {
                    psDites.setInt(1, planta);
                    try (ResultSet rsDites = psDites.executeQuery()) {
                        while (rsDites.next()) {
                            ConfigGame.DitaConfig d = new ConfigGame.DitaConfig();
                            d.nivell = rsDites.getInt(1);
                            d.pregunta = rsDites.getString(2);
                            d.resposta = rsDites.getString(3);
                            e.dites.add(d);
                        }
                    }
                }
                try (PreparedStatement psVenda = conn.prepareStatement(
                        "SELECT item_id FROM items_venda WHERE planta=? ORDER BY ordre")) {
                    psVenda.setInt(1, planta);
                    try (ResultSet rsVenda = psVenda.executeQuery()) {
                        while (rsVenda.next()) e.itemsVenda.add(rsVenda.getString(1));
                    }
                }
                enigmes.add(e);
            }
        }
        if (!enigmes.isEmpty()) config.enigmes = enigmes;
    }

    private static void carregaChetos(Connection conn, ConfigGame config) throws Exception {
        List<ConfigGame.ChetoConfig> chetos = new ArrayList<>();
        try (ResultSet rs = conn.createStatement().executeQuery("SELECT accio, sequencia FROM chetos")) {
            while (rs.next()) {
                ConfigGame.ChetoConfig c = new ConfigGame.ChetoConfig();
                c.accio = rs.getString(1);
                c.sequencia = separarTextos(rs.getString(2));
                chetos.add(c);
            }
        }
        if (!chetos.isEmpty()) config.chetos = chetos;
    }

    private static void carregaTexts(Connection conn, ConfigGame config) throws Exception {
        Map<String, String> kv = clauValorMap(conn, "texts");
        if (kv.isEmpty()) return;
        ConfigGame.TextsConfig t = new ConfigGame.TextsConfig();
        t.windowTitle = kv.getOrDefault("windowTitle", t.windowTitle);
        t.headerTitle = kv.getOrDefault("headerTitle", t.headerTitle);
        t.subtitle = kv.getOrDefault("subtitle", t.subtitle);
        t.menuIniciar = kv.getOrDefault("menuIniciar", t.menuIniciar);
        t.menuSortir = kv.getOrDefault("menuSortir", t.menuSortir);
        t.menuReanudar = kv.getOrDefault("menuReanudar", t.menuReanudar);
        t.menuGuardar = kv.getOrDefault("menuGuardar", t.menuGuardar);
        t.menuCarregar = kv.getOrDefault("menuCarregar", t.menuCarregar);
        t.pauseTitle = kv.getOrDefault("pauseTitle", t.pauseTitle);
        t.pauseInstructions = kv.getOrDefault("pauseInstructions", t.pauseInstructions);
        t.pauseResumePista = kv.getOrDefault("pauseResumePista", t.pauseResumePista);
        t.menuTornaAcomencar = kv.getOrDefault("menuTornaAcomencar", t.menuTornaAcomencar);
        config.texts = t;
    }

    // ---------------------------------------------------------------
    //  PETITES UTILITATS COMPARTIDES
    // ---------------------------------------------------------------

    //prepara un INSERT cap a una taula clau/valor (configuracio, jugador, texts...)
    private static PreparedStatement clauValor(Connection conn, String taula) throws Exception {
        return conn.prepareStatement("INSERT INTO " + taula + " VALUES (?,?)");
    }

    private static void desa(PreparedStatement ps, String clau, Object valor) throws Exception {
        ps.setString(1, clau);
        ps.setString(2, valor == null ? null : String.valueOf(valor));
        ps.executeUpdate();
    }

    private static Map<String, String> clauValorMap(Connection conn, String taula) throws Exception {
        return llegeixDosCamps(conn, "SELECT clau, valor FROM " + taula);
    }

    private static Map<String, String> llegeixDosCamps(Connection conn, String consulta) throws Exception {
        Map<String, String> mapa = new LinkedHashMap<>();
        try (ResultSet rs = conn.createStatement().executeQuery(consulta)) {
            while (rs.next()) mapa.put(rs.getString(1), rs.getString(2));
        }
        return mapa;
    }

    private static int enter(Map<String, String> kv, String clau, int perDefecte) {
        try {
            return Integer.parseInt(kv.get(clau));
        } catch (Exception e) {
            return perDefecte;
        }
    }

    private static double decimal(Map<String, String> kv, String clau, double perDefecte) {
        try {
            return Double.parseDouble(kv.get(clau));
        } catch (Exception e) {
            return perDefecte;
        }
    }

    private static char caracter(Map<String, String> kv, String clau, char perDefecte) {
        String v = kv.get(clau);
        return (v != null && !v.isEmpty()) ? v.charAt(0) : perDefecte;
    }

    //uneix una llista de caràcters en un text separat per comes (ex: ['#'] -> "#")
    private static String unirCaracters(List<Character> simbols) {
        if (simbols == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < simbols.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(simbols.get(i));
        }
        return sb.toString();
    }

    private static List<Character> separarCaracters(String text) {
        List<Character> resultat = new ArrayList<>();
        if (text == null || text.isBlank()) return resultat;
        for (String tros : text.split(",")) {
            if (!tros.isEmpty()) resultat.add(tros.charAt(0));
        }
        return resultat;
    }

    private static String unirTextos(List<String> textos) {
        return textos != null ? String.join(",", textos) : "";
    }

    private static List<String> separarTextos(String text) {
        return (text != null && !text.isBlank()) ? Arrays.asList(text.split(",")) : new ArrayList<>();
    }

    //llegeix un fitxer JSON dels recursos del jar (igual que fa RegistreItems amb items.json)
    private static com.google.gson.JsonObject llegeixJsonRecurs(String nom) {
        try (java.io.InputStream is = PartidaRepository.class.getClassLoader().getResourceAsStream(nom)) {
            if (is == null) return null;
            try (java.io.Reader r = new java.io.InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8)) {
                return new com.google.gson.Gson().fromJson(r, com.google.gson.JsonObject.class);
            }
        } catch (Exception e) {
            return null;
        }
    }

    private static String text(com.google.gson.JsonObject o, String camp) {
        return o.has(camp) && !o.get(camp).isJsonNull() ? o.get(camp).getAsString() : null;
    }

    private static int enter(com.google.gson.JsonObject o, String camp, int perDefecte) {
        return o.has(camp) && !o.get(camp).isJsonNull() ? o.get(camp).getAsInt() : perDefecte;
    }

    private static Integer enterONull(com.google.gson.JsonObject o, String camp) {
        return o.has(camp) && !o.get(camp).isJsonNull() ? o.get(camp).getAsInt() : null;
    }

    private static String valorText(Object o) {
        return o != null ? o.toString() : null;
    }

    private static Integer valorEnter(Object o) {
        if (o instanceof Number) return ((Number) o).intValue();
        return null;
    }

    private static boolean valorBoolea(Object o) {
        return Boolean.TRUE.equals(o);
    }
}
