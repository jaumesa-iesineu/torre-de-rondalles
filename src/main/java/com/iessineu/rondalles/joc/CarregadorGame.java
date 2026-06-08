package com.iessineu.rondalles.joc;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.iessineu.rondalles.motor.CeldaArt;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CarregadorGame {

    private static String basedir = null;

    public static void setBasedir(String dir) { basedir = dir; }
    public static String getBasedir() { return basedir; }

    //carrega es game.json des de dins del jar (us intern per defecte)
    public static ConfigGame carrega(String rutaRecurs) throws Exception {
        InputStream is = CarregadorGame.class.getClassLoader().getResourceAsStream(rutaRecurs);
        if (is == null) {
            throw new RuntimeException("No s'ha trobat: " + rutaRecurs);
        }
        String json;
        try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            json = llegeixReader(reader);
        }
        json = resolveIncludes(json, false);
        ConfigGame config = new Gson().fromJson(json, ConfigGame.class);
        carregaSubfitxers(config);
        resolgArt(config);
        return config;
    }

    private static String llegeixReader(Reader reader) throws Exception {
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[4096];
        int n;
        while ((n = reader.read(buf)) != -1) sb.append(buf, 0, n);
        return sb.toString();
    }

    private static String resolveIncludes(String json, boolean extern) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\"\\$include:([^\"]+)\"");
        java.util.regex.Matcher m = p.matcher(json);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String fitxer = m.group(1);
            String contingut = null;
            try {
                if (extern && basedir != null) {
                    java.io.File f = new java.io.File(basedir, fitxer);
                    if (f.exists()) contingut = java.nio.file.Files.readString(f.toPath(), StandardCharsets.UTF_8);
                }
                if (contingut == null) {
                    InputStream inc = CarregadorGame.class.getClassLoader().getResourceAsStream(fitxer);
                    if (inc != null) {
                        try (Reader r = new InputStreamReader(inc, StandardCharsets.UTF_8)) {
                            contingut = llegeixReader(r);
                        }
                    }
                }
            } catch (Exception ignored) {}
            m.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(
                contingut != null ? contingut.trim() : "null"
            ));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static void carregaSubfitxers(ConfigGame config) throws Exception {
        String[] subfitxers = {"terrenys.json", "mapes.json", "enemics.json", "items.json", "portes.json", "personatges.json"};
        Gson gson = new Gson();
        for (String nom : subfitxers) {
            InputStream is = obriRecurs(nom);
            if (is == null) continue;
            ConfigGame fragment;
            try (Reader r = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                fragment = gson.fromJson(r, ConfigGame.class);
            }
            fusiona(config, fragment);
        }
    }

    private static InputStream obriRecurs(String nom) throws Exception {
        if (basedir != null) {
            java.io.File f = new java.io.File(basedir, nom);
            if (f.exists()) return new java.io.FileInputStream(f);
        }
        return CarregadorGame.class.getClassLoader().getResourceAsStream(nom);
    }

    private static void fusiona(ConfigGame base, ConfigGame fragment) {
        if (fragment == null) return;
        if (fragment.terrenys != null) base.terrenys = fragment.terrenys;
        if (fragment.mapes != null) base.mapes = fragment.mapes;
        if (fragment.enemics != null) base.enemics = fragment.enemics;
        if (fragment.items != null) base.items = fragment.items;
        if (fragment.portes != null) base.portes = fragment.portes;
        if (fragment.tipusPersonatge != null) base.tipusPersonatge = fragment.tipusPersonatge;
        if (fragment.personatgeCustom != null) base.personatgeCustom = fragment.personatgeCustom;
    }

    //carrega un JSON extern del sistema de fitxers (argument -mod)
    //no fusiona subfitxers, perque un mod nomes ha d'aportar es seus propis canvis
    public static ConfigGame carregaFitxerExtern(String ruta) throws Exception {
        return carregaFitxerExtern(ruta, false);
    }

    //carrega un JSON extern, fusionant-hi tambe es seus subfitxers si en te (argument -game)
    //un -game pot estar partit en mapes.json, enemics.json... igual que es game.json de dins
    public static ConfigGame carregaFitxerExtern(String ruta, boolean ambSubfitxers) throws Exception {
        String json;
        try (Reader reader = new InputStreamReader(new FileInputStream(ruta), StandardCharsets.UTF_8)) {
            json = llegeixReader(reader);
        }
        json = resolveIncludes(json, true);
        ConfigGame config = new Gson().fromJson(json, ConfigGame.class);
        if (ambSubfitxers) {
            carregaSubfitxers(config);
        }
        resolgArt(config);
        return config;
    }

    //aplica un mod sobre una config base: els camps del mod sobreescriuen els de la base (last wins)
    //s'ha de cridar en ordre, del primer mod al darrer
    public static void aplicaMod(ConfigGame base, ConfigGame mod) {
        if (mod == null) {
            return;
        }

        if (mod.configuracio != null && mod.configuracio.mapaInicial != null) {
            if (base.configuracio == null) {
                base.configuracio = new ConfigGame.Configuracio();
            }
            base.configuracio.mapaInicial = mod.configuracio.mapaInicial;
        }

        if (mod.equipamentInicial != null) {
            if (base.equipamentInicial == null) {
                base.equipamentInicial = new ConfigGame.EquipamentInicial();
            }
            if (mod.equipamentInicial.arma != null) {
                base.equipamentInicial.arma = mod.equipamentInicial.arma;
            }
            if (mod.equipamentInicial.armadures != null) {
                base.equipamentInicial.armadures = mod.equipamentInicial.armadures;
            }
        }

        if (mod.mapes != null) {
            if (base.mapes == null) {
                base.mapes = new ConfigGame.MapesGroup();
            }
            if (mod.mapes.ordre != null) {
                base.mapes.ordre = mod.mapes.ordre;
            }
            if (mod.mapes.registres != null) {
                if (base.mapes.registres == null) {
                    base.mapes.registres = new ArrayList<>();
                }
                for (MapaConfig mc : mod.mapes.registres) {
                    base.mapes.registres.removeIf(b -> b.id.equals(mc.id));
                    base.mapes.registres.add(mc);
                }
            }
        }

        if (mod.enemics != null) {
            if (base.enemics == null) {
                base.enemics = new ConfigGame.EnemicsGroup();
            }
            if (mod.enemics.tipus != null) {
                if (base.enemics.tipus == null) {
                    base.enemics.tipus = new ArrayList<>();
                }
                for (TipusEnemic te : mod.enemics.tipus) {
                    //un tipus s'identifica pel primer simbol; si coincideix, el mod el sobreescriu
                    String primerSimbol = (te.simbols != null && !te.simbols.isEmpty()) ? te.simbols.get(0) : null;
                    if (primerSimbol != null) {
                        final String ps = primerSimbol;
                        base.enemics.tipus.removeIf(b -> b.simbols != null && b.simbols.contains(ps));
                    }
                    base.enemics.tipus.add(te);
                }
            }
            if (mod.enemics.posicions != null) {
                if (base.enemics.posicions == null) {
                    base.enemics.posicions = new ArrayList<>();
                }
                base.enemics.posicions.addAll(mod.enemics.posicions);
            }
        }

        if (mod.items != null) {
            if (base.items == null) {
                base.items = new ConfigGame.ItemsGroup();
            }
            if (mod.items.posicions != null) {
                if (base.items.posicions == null) {
                    base.items.posicions = new ArrayList<>();
                }
                base.items.posicions.addAll(mod.items.posicions);
            }
        }

        if (mod.portes != null) {
            if (base.portes == null) {
                base.portes = new ConfigGame.PortesGroup();
            }
            if (mod.portes.posicions != null) {
                if (base.portes.posicions == null) {
                    base.portes.posicions = new ArrayList<>();
                }
                base.portes.posicions.addAll(mod.portes.posicions);
            }
        }
    }

    //llegeix es node items.catalogItems d'un mod (si en te), per poder afegir armes/armadures/pocions/claus noves
    public static JsonObject llegeixCatalogItemsDelMod(String ruta) throws Exception {
        try (Reader reader = new InputStreamReader(new FileInputStream(ruta), StandardCharsets.UTF_8)) {
            JsonObject arrel = new Gson().fromJson(reader, JsonObject.class);
            if (arrel == null || !arrel.has("items")) return null;
            JsonObject items = arrel.getAsJsonObject("items");
            return items.has("catalogItems") ? items.getAsJsonObject("catalogItems") : null;
        }
    }

    private static void resolgArt(ConfigGame config) {
        if (config.enemics != null && config.enemics.tipus != null) {
            for (TipusEnemic t : config.enemics.tipus) {
                if (t.artJsonFitxer != null && !t.artJsonFitxer.isBlank()) {
                    t.artJson = carregaArtJson(t.artJsonFitxer);
                } else if (t.artFitxer != null && !t.artFitxer.isBlank()) {
                    t.artAscii = carregaArt(t.artFitxer);
                }
            }
        }
        if (config.jugador != null && config.jugador.artFitxer != null && !config.jugador.artFitxer.isBlank()) {
            config.jugador.artAscii = carregaArt(config.jugador.artFitxer);
        }
        if (config.jugador != null && config.jugador.artFitxerEsquena != null && !config.jugador.artFitxerEsquena.isBlank()) {
            config.jugador.artAsciiEsquena = carregaArt(config.jugador.artFitxerEsquena);
        }
    }

    private static CeldaArt[][] carregaArtJson(String rutaRecurs) {
        InputStream is = CarregadorGame.class.getClassLoader().getResourceAsStream(rutaRecurs);
        if (is == null) return null;
        try (Reader r = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            JsonObject arrel = new Gson().fromJson(r, JsonObject.class);
            JsonArray grid = arrel.getAsJsonArray("grid");
            int rows = grid.size();
            CeldaArt[][] resultat = new CeldaArt[rows][];
            for (int y = 0; y < rows; y++) {
                JsonArray fila = grid.get(y).getAsJsonArray();
                resultat[y] = new CeldaArt[fila.size()];
                for (int x = 0; x < fila.size(); x++) {
                    JsonObject cel = fila.get(x).getAsJsonObject();
                    char c = cel.get("char").getAsString().charAt(0);
                    String fg = cel.has("fg") ? cel.get("fg").getAsString() : "#FFFFFF";
                    String bg = cel.has("bg") ? cel.get("bg").getAsString() : "#000000";
                    resultat[y][x] = new CeldaArt(c, CeldaArt.hexAColor(fg), CeldaArt.hexAColor(bg));
                }
            }
            return resultat;
        } catch (Exception e) {
            return null;
        }
    }

    private static String[] carregaArt(String rutaRecurs) {
        InputStream is = CarregadorGame.class.getClassLoader().getResourceAsStream(rutaRecurs);
        if (is == null) {
            return null;
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return br.lines().toArray(String[]::new);
        } catch (Exception e) {
            return null;
        }
    }
}
