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

public class PartidaRepository {

    private static final String URL = "jdbc:sqlite:rondalles.db";

    //crea les taules i les omple amb les dades per defecte del game.json si estan buides
    public static void inicialitza(ConfigGame config) {
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
                    simbol    TEXT PRIMARY KEY,
                    nom       TEXT NOT NULL,
                    vida      INTEGER NOT NULL,
                    atac      INTEGER NOT NULL,
                    radi      INTEGER NOT NULL,
                    color_r   INTEGER NOT NULL,
                    color_g   INTEGER NOT NULL,
                    color_b   INTEGER NOT NULL,
                    estatica  INTEGER NOT NULL,
                    art_ascii TEXT
                )""");

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS posicions_enemics (
                    id     INTEGER PRIMARY KEY AUTOINCREMENT,
                    mapa   TEXT NOT NULL,
                    simbol TEXT NOT NULL,
                    x      INTEGER NOT NULL,
                    y      INTEGER NOT NULL
                )""");

            if (taulaBuida(conn, "configuracio"))    ompleConfiguracio(conn, config);
            if (taulaBuida(conn, "mapes"))           ompleMapes(conn, config);
            if (taulaBuida(conn, "tipus_enemics"))   ompleEnemics(conn, config);
            if (taulaBuida(conn, "posicions_enemics")) omplePosicions(conn, config);

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
                "INSERT OR IGNORE INTO tipus_enemics VALUES (?,?,?,?,?,?,?,?,?,?)")) {
            for (TipusEnemic t : config.enemics.tipus) {
                ps.setString(1, t.simbol);
                ps.setString(2, t.nom);
                ps.setInt(3, t.vida);
                ps.setInt(4, t.atac);
                ps.setInt(5, t.radi);
                ps.setInt(6, t.colorR);
                ps.setInt(7, t.colorG);
                ps.setInt(8, t.colorB);
                ps.setInt(9, t.estatica ? 1 : 0);
                //art ascii guardat com a text amb \n entre línies
                String art = t.artAscii != null ? String.join("\n", t.artAscii) : null;
                ps.setString(10, art);
                ps.executeUpdate();
            }
        }
    }

    private static void omplePosicions(Connection conn, ConfigGame config) throws Exception {
        if (config.enemics == null || config.enemics.posicions == null) return;
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO posicions_enemics (mapa, simbol, x, y) VALUES (?,?,?,?)")) {
            for (PosicioEnemic p : config.enemics.posicions) {
                ps.setString(1, p.mapa);
                ps.setString(2, p.simbol);
                ps.setInt(3, p.x);
                ps.setInt(4, p.y);
                ps.executeUpdate();
            }
        }
    }
}
