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
        InputStream in = getClass().getResourceAsStream("/game.json");
        if (in == null) throw new RuntimeException("No s'ha trobat game.json");

        JsonObject arrel = new Gson().fromJson(
            new InputStreamReader(in, StandardCharsets.UTF_8), JsonObject.class);

        JsonObject catalog = arrel.getAsJsonObject("catalogItems");
        if (catalog == null) return;

        JsonArray jArmes = catalog.getAsJsonArray("armes");
        if (jArmes != null) {
            for (JsonElement e : jArmes) {
                JsonObject o = e.getAsJsonObject();
                String id = o.get("id").getAsString();
                armes.put(id, new Arma(
                    o.get("nom").getAsString(),
                    o.get("pes").getAsInt(),
                    o.get("simbol").getAsString().charAt(0),
                    o.get("atac").getAsInt(),
                    o.get("rang").getAsInt()
                ));
            }
        }

        JsonArray jArmadures = catalog.getAsJsonArray("armadures");
        if (jArmadures != null) {
            for (JsonElement e : jArmadures) {
                JsonObject o = e.getAsJsonObject();
                String id = o.get("id").getAsString();
                armadures.put(id, new Armadura(
                    o.get("nom").getAsString(),
                    o.get("pes").getAsInt(),
                    o.get("simbol").getAsString().charAt(0),
                    o.get("defensa").getAsInt(),
                    Armadura.Slot.valueOf(o.get("slot").getAsString())
                ));
            }
        }

        JsonArray jPocions = catalog.getAsJsonArray("pocions");
        if (jPocions != null) {
            for (JsonElement e : jPocions) {
                JsonObject o = e.getAsJsonObject();
                String id = o.get("id").getAsString();
                pocions.put(id, new Pocio(
                    o.get("nom").getAsString(),
                    o.get("pes").getAsInt(),
                    o.get("simbol").getAsString().charAt(0),
                    Pocio.Tipus.valueOf(o.get("tipus").getAsString()),
                    o.get("valor").getAsInt()
                ));
            }
        }
    }

    public Arma arma(String id) {
        Arma src = armes.get(id);
        if (src == null) throw new IllegalArgumentException("Arma desconeguda: " + id);
        return new Arma(src.getNom(), src.getPes(), src.getSimbol(), src.getAtac(), src.getRang());
    }

    public Armadura armadura(String id) {
        Armadura src = armadures.get(id);
        if (src == null) throw new IllegalArgumentException("Armadura desconeguda: " + id);
        return new Armadura(src.getNom(), src.getPes(), src.getSimbol(), src.getDefensa(), src.getSlot());
    }

    public Pocio pocio(String id) {
        Pocio src = pocions.get(id);
        if (src == null) throw new IllegalArgumentException("Poció desconeguda: " + id);
        return new Pocio(src.getNom(), src.getPes(), src.getSimbol(), src.getTipus(), src.getValor());
    }

    public Map<String, Arma> todesLesArmes() { return armes; }
    public Map<String, Armadura> todesLesArmadures() { return armadures; }
    public Map<String, Pocio> totesLesPocions() { return pocions; }

    public Item itemPerId(String id) {
        if (pocions.containsKey(id)) return pocio(id);
        if (armes.containsKey(id)) return arma(id);
        if (armadures.containsKey(id)) return armadura(id);
        return null;
    }

    public Item itemPerNom(String nom) {
        for (Map.Entry<String, Pocio> e : pocions.entrySet())
            if (e.getValue().getNom().equals(nom)) return pocio(e.getKey());
        for (Map.Entry<String, Arma> e : armes.entrySet())
            if (e.getValue().getNom().equals(nom)) return arma(e.getKey());
        for (Map.Entry<String, Armadura> e : armadures.entrySet())
            if (e.getValue().getNom().equals(nom)) return armadura(e.getKey());
        return null;
    }
}
