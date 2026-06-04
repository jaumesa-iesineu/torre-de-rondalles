package com.iessineu.rondalles.db;

import com.iessineu.rondalles.joc.ConfigGame;
import com.iessineu.rondalles.joc.MapaConfig;
import com.iessineu.rondalles.joc.PosicioEnemic;
import com.iessineu.rondalles.joc.TipusEnemic;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;

public class PartidaRepository {

    private static final String URL = "jdbc:sqlite:rondalles.db";

    // inicialitza la BD des del game.json empaquetado (sense -game)
    // si les taules ja existeixen i no estan buides, no fa res
    public static void inicialitzaDefecte() {
        try {
            com.iessineu.rondalles.joc.ConfigGame config =
                com.iessineu.rondalles.joc.CarregadorGame.carrega("game.json");
            inicialitza(config);
        } catch (Exception e) {
            System.err.println("No s'ha pogut inicialitzar la BD per defecte: " + e.getMessage());
        }
    }

    //crea les taules i les omple amb les dades per defecte del game.json si estan buides
    public static void inicialitza(ConfigGame config) {
        inicialitza(config, false);
    }

    //amb forcar=true, esborra les dades existents i les torna a posar des del config
    public static void inicialitza(ConfigGame config, boolean forcar) {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement st = conn.createStatement()) {

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS configuracio (
                    clau  TEXT PRIMARY KEY,
                    valor TEXT NOT NULL
                )""");

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS mapes (
                    id     TEXT PRIMARY KEY,
                    fitxer TEXT NOT NULL,
                    ordre  INTEGER NOT NULL
                )""");

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS tipus_enemics (
                    simbols    TEXT PRIMARY KEY,
                    nom        TEXT NOT NULL,
                    vida       INTEGER NOT NULL,
                    atac       INTEGER NOT NULL,
                    radi       INTEGER NOT NULL,
                    color_r    INTEGER NOT NULL,
                    color_g    INTEGER NOT NULL,
                    color_b    INTEGER NOT NULL,
                    estatica       INTEGER NOT NULL,
                    velocitat      INTEGER NOT NULL DEFAULT 1,
                    travessa_parets INTEGER NOT NULL DEFAULT 0,
                    art_fitxer     TEXT,
                    art_ascii      TEXT
                )""");

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS posicions_enemics (
                    id     INTEGER PRIMARY KEY AUTOINCREMENT,
                    mapa   TEXT NOT NULL,
                    simbol TEXT NOT NULL,
                    x      INTEGER NOT NULL,
                    y      INTEGER NOT NULL,
                    area   INTEGER NOT NULL DEFAULT 0
                )""");

            //migració: afegir columnes noves si no existeixen (BD creades abans dels canvis)
            try { st.executeUpdate("ALTER TABLE posicions_enemics ADD COLUMN area INTEGER NOT NULL DEFAULT 0"); } catch (Exception ignored) {}
            try { st.executeUpdate("ALTER TABLE tipus_enemics ADD COLUMN velocitat INTEGER NOT NULL DEFAULT 1"); } catch (Exception ignored) {}
            try { st.executeUpdate("ALTER TABLE tipus_enemics ADD COLUMN travessa_parets INTEGER NOT NULL DEFAULT 0"); } catch (Exception ignored) {}

            if (forcar) {
                st.executeUpdate("DELETE FROM configuracio");
                st.executeUpdate("DELETE FROM mapes");
                st.executeUpdate("DELETE FROM tipus_enemics");
                st.executeUpdate("DELETE FROM posicions_enemics");
            }

            if (forcar || taulaBuida(conn, "configuracio"))    ompleConfiguracio(conn, config);
            if (forcar || taulaBuida(conn, "mapes"))           ompleMapes(conn, config);
            if (forcar || taulaBuida(conn, "tipus_enemics"))   ompleEnemics(conn, config);
            if (forcar || taulaBuida(conn, "posicions_enemics")) omplePosicions(conn, config);

        } catch (Exception e) {
            System.err.println("Error inicialitzant BD: " + e.getMessage());
        }
    }

    private static boolean taulaBuida(Connection conn, String taula) throws Exception {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + taula)) {
            return rs.getInt(1) == 0;
        }
    }

    private static void ompleConfiguracio(Connection conn, ConfigGame config) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO configuracio VALUES (?,?)")) {
            ps.setString(1, "mapaInicial"); ps.setString(2, config.getMapaInicial()); ps.executeUpdate();
        }
    }

    private static void ompleMapes(Connection conn, ConfigGame config) throws Exception {
        if (config.mapes == null) return;
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

    private static void ompleEnemics(Connection conn, ConfigGame config) throws Exception {
        if (config.enemics == null || config.enemics.tipus == null) return;
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT OR IGNORE INTO tipus_enemics VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)")) {
            for (TipusEnemic t : config.enemics.tipus) {
                //simbols guardats com a text coma-separat ex: "d,e"
                String simbolsStr = t.simbols != null ? String.join(",", t.simbols) : "";
                ps.setString(1, simbolsStr);
                ps.setString(2, t.nom);
                ps.setInt(3, t.vida);
                ps.setInt(4, t.atac);
                ps.setInt(5, t.radi);
                ps.setInt(6, t.colorR);
                ps.setInt(7, t.colorG);
                ps.setInt(8, t.colorB);
                ps.setInt(9, t.estatica ? 1 : 0);
                ps.setInt(10, t.velocitat);
                ps.setInt(11, t.travessaParets ? 1 : 0);
                ps.setString(12, t.artFitxer);
                String art = t.artAscii != null ? String.join("\n", t.artAscii) : null;
                ps.setString(13, art);
                ps.executeUpdate();
            }
        }
    }

    // reconstrueix un ConfigGame llegint les taules de la BD (s'usa quan no es passa -game)
    public static ConfigGame carregaConfig() {
        ConfigGame config = new ConfigGame();
        try (Connection conn = DriverManager.getConnection(URL);
             Statement st = conn.createStatement()) {

            config.configuracio = new ConfigGame.Configuracio();
            try (ResultSet rs = st.executeQuery("SELECT valor FROM configuracio WHERE clau='mapaInicial'")) {
                if (rs.next()) config.configuracio.mapaInicial = rs.getString(1);
            }

            config.mapes = new ConfigGame.MapesGroup();
            config.mapes.ordre = new ArrayList<>();
            config.mapes.registres = new ArrayList<>();
            try (ResultSet rs = st.executeQuery("SELECT id, fitxer FROM mapes ORDER BY ordre")) {
                while (rs.next()) {
                    MapaConfig mc = new MapaConfig();
                    mc.id = rs.getString(1);
                    mc.fitxer = rs.getString(2);
                    config.mapes.ordre.add(mc.id);
                    config.mapes.registres.add(mc);
                }
            }

            config.enemics = new ConfigGame.EnemicsGroup();
            config.enemics.tipus = new ArrayList<>();
            config.enemics.posicions = new ArrayList<>();
            try (ResultSet rs = st.executeQuery("SELECT simbols,nom,vida,atac,radi,color_r,color_g,color_b,estatica,velocitat,travessa_parets,art_ascii FROM tipus_enemics")) {
                while (rs.next()) {
                    TipusEnemic te = new TipusEnemic();
                    String simbolsStr = rs.getString(1);
                    te.simbols = simbolsStr != null ? Arrays.asList(simbolsStr.split(",")) : new ArrayList<>();
                    te.nom = rs.getString(2);
                    te.vida = rs.getInt(3);
                    te.atac = rs.getInt(4);
                    te.radi = rs.getInt(5);
                    te.colorR = rs.getInt(6);
                    te.colorG = rs.getInt(7);
                    te.colorB = rs.getInt(8);
                    te.estatica = rs.getInt(9) == 1;
                    te.velocitat = rs.getInt(10);
                    te.travessaParets = rs.getInt(11) == 1;
                    String art = rs.getString(12);
                    te.artAscii = art != null ? art.split("\n") : null;
                    config.enemics.tipus.add(te);
                }
            }
            try (ResultSet rs = st.executeQuery("SELECT mapa,simbol,x,y,area FROM posicions_enemics")) {
                while (rs.next()) {
                    PosicioEnemic pe = new PosicioEnemic();
                    pe.mapa = rs.getString(1);
                    pe.simbol = rs.getString(2);
                    pe.x = rs.getInt(3);
                    pe.y = rs.getInt(4);
                    pe.area = rs.getInt(5);
                    config.enemics.posicions.add(pe);
                }
            }

        } catch (Exception e) {
            System.err.println("Error carregant config de BD: " + e.getMessage());
        }
        return config;
    }

    private static void omplePosicions(Connection conn, ConfigGame config) throws Exception {
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
}
