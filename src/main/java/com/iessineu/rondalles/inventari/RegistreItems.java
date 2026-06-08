package com.iessineu.rondalles.inventari;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class RegistreItems {

    private static RegistreItems instancia;

    private final Map<String, Arma> armes = new LinkedHashMap<>();
    private final Map<String, Armadura> armadures = new LinkedHashMap<>();
    private final Map<String, Pocio> pocions = new LinkedHashMap<>();
    private final Map<String, Clau> claus = new LinkedHashMap<>();

    private RegistreItems() {
        carregaDeJson();
    }

    public static RegistreItems get() {
        if (instancia == null) {
            instancia = new RegistreItems();
        }
        return instancia;
    }

    private void carregaDeJson() {
        //primer miram si hi ha un items.json extern (basedir d'un -game), sino el de dins el jar
        InputStream in = obriItemsJson();
        if (in == null) throw new RuntimeException("No s'ha trobat items.json");

        JsonObject arrel = new Gson().fromJson(
            new InputStreamReader(in, StandardCharsets.UTF_8), JsonObject.class);

        JsonObject catalog = arrel.getAsJsonObject("catalogItems");
        if (catalog == null) return;

        carregaCatalog(catalog);
    }

    private InputStream obriItemsJson() {
        String basedir = com.iessineu.rondalles.joc.CarregadorGame.getBasedir();
        if (basedir != null) {
            java.io.File f = new java.io.File(basedir, "items.json");
            if (f.exists()) {
                try {
                    return new java.io.FileInputStream(f);
                } catch (Exception e) {
                    //si peta, seguim amb el de dins el jar
                }
            }
        }
        return getClass().getResourceAsStream("/items.json");
    }

    //afegeix (o sobreescriu per id) entrades del catalog d'un mod sobre les ja carregades
    public void aplicaMod(JsonObject catalogMod) {
        if (catalogMod == null) return;
        carregaCatalog(catalogMod);
    }

    //llegeix un node "catalogItems" i omple/sobreescriu els mapes (last wins per id)
    private void carregaCatalog(JsonObject catalog) {
        JsonArray jArmes = catalog.getAsJsonArray("armes");
        if (jArmes != null) {
            for (JsonElement e : jArmes) {
                JsonObject o = e.getAsJsonObject();
                String id = o.get("id").getAsString();
                Arma arma = new Arma(
                    o.get("nom").getAsString(),
                    o.get("pes").getAsInt(),
                    o.get("simbol").getAsString().charAt(0),
                    o.get("atac").getAsInt(),
                    o.get("rang").getAsInt()
                );
                if (o.has("tier")) arma.tier = o.get("tier").getAsInt();
                armes.put(id, arma);
            }
        }

        JsonArray jArmadures = catalog.getAsJsonArray("armadures");
        if (jArmadures != null) {
            for (JsonElement e : jArmadures) {
                JsonObject o = e.getAsJsonObject();
                String id = o.get("id").getAsString();
                Armadura arm = new Armadura(
                    o.get("nom").getAsString(),
                    o.get("pes").getAsInt(),
                    o.get("simbol").getAsString().charAt(0),
                    o.get("defensa").getAsInt(),
                    Armadura.Slot.valueOf(o.get("slot").getAsString())
                );
                if (o.has("tier")) arm.tier = o.get("tier").getAsInt();
                armadures.put(id, arm);
            }
        }

        JsonArray jPocions = catalog.getAsJsonArray("pocions");
        if (jPocions != null) {
            for (JsonElement e : jPocions) {
                JsonObject o = e.getAsJsonObject();
                String id = o.get("id").getAsString();
                Pocio pocio = new Pocio(
                    o.get("nom").getAsString(),
                    o.get("pes").getAsInt(),
                    o.get("simbol").getAsString().charAt(0),
                    Pocio.Tipus.valueOf(o.get("tipus").getAsString()),
                    o.get("valor").getAsInt()
                );
                if (o.has("tier")) pocio.tier = o.get("tier").getAsInt();
                pocions.put(id, pocio);
            }
        }

        JsonArray jClaus = catalog.getAsJsonArray("claus");
        if (jClaus != null) {
            for (JsonElement e : jClaus) {
                JsonObject o = e.getAsJsonObject();
                String id = o.get("id").getAsString();
                claus.put(id, new Clau(
                    id,
                    o.get("nom").getAsString(),
                    o.get("pes").getAsInt(),
                    o.get("simbol").getAsString().charAt(0),
                    o.get("planta").getAsInt()
                ));
            }
        }
    }

    public Arma arma(String id) {
        Arma src = armes.get(id);
        if (src == null) throw new IllegalArgumentException("Arma desconeguda: " + id);
        Arma copia = new Arma(src.getNom(), src.getPes(), src.getSimbol(), src.getAtac(), src.getRang());
        copia.tier = src.tier;
        return copia;
    }

    public Armadura armadura(String id) {
        Armadura src = armadures.get(id);
        if (src == null) throw new IllegalArgumentException("Armadura desconeguda: " + id);
        Armadura copia = new Armadura(src.getNom(), src.getPes(), src.getSimbol(), src.getDefensa(), src.getSlot());
        copia.tier = src.tier;
        return copia;
    }

    public Pocio pocio(String id) {
        Pocio src = pocions.get(id);
        if (src == null) throw new IllegalArgumentException("Poció desconeguda: " + id);
        Pocio copia = new Pocio(src.getNom(), src.getPes(), src.getSimbol(), src.getTipus(), src.getValor());
        copia.tier = src.tier;
        return copia;
    }

    public Clau clau(String id) {
        Clau src = claus.get(id);
        if (src == null) throw new IllegalArgumentException("Clau desconeguda: " + id);
        return new Clau(src.getId(), src.getNom(), src.getPes(), src.getSimbol(), src.getPlanta());
    }

    public Map<String, Arma> todesLesArmes() { return armes; }
    public Map<String, Armadura> todesLesArmadures() { return armadures; }
    public Map<String, Pocio> totesLesPocions() { return pocions; }

    public Item itemPerId(String id) {
        if (pocions.containsKey(id)) return pocio(id);
        if (armes.containsKey(id)) return arma(id);
        if (armadures.containsKey(id)) return armadura(id);
        if (claus.containsKey(id)) return clau(id);
        return null;
    }

    public Item itemPerNom(String nom) {
        for (Map.Entry<String, Pocio> e : pocions.entrySet())
            if (e.getValue().getNom().equals(nom)) return pocio(e.getKey());
        for (Map.Entry<String, Arma> e : armes.entrySet())
            if (e.getValue().getNom().equals(nom)) return arma(e.getKey());
        for (Map.Entry<String, Armadura> e : armadures.entrySet())
            if (e.getValue().getNom().equals(nom)) return armadura(e.getKey());
        for (Map.Entry<String, Clau> e : claus.entrySet())
            if (e.getValue().getNom().equals(nom)) return clau(e.getKey());
        return null;
    }
}
