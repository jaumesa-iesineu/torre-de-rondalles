package com.iessineu.rondalles.inventari;

import java.io.BufferedReader;
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
        carregaArmes();
        carregaArmadures();
        carregaPocions();
    }

    public static RegistreItems get() {
        if (instancia == null) {
            instancia = new RegistreItems();
        }
        return instancia;
    }

    private void carregaArmes() {
        for (String[] c : llegirFitxer("/armes.txt")) {
            // c[0]=id c[1]=nom c[2]=pes c[3]=simbol c[4]=atac c[5]=rang
            armes.put(c[0], new Arma(
                    c[1],
                    Integer.parseInt(c[2]),
                    c[3].charAt(0),
                    Integer.parseInt(c[4]),
                    Integer.parseInt(c[5])
            ));
        }
    }

    private void carregaArmadures() {
        for (String[] c : llegirFitxer("/armadures.txt")) {
            // c[0]=id c[1]=nom c[2]=pes c[3]=simbol c[4]=defensa c[5]=slot
            armadures.put(c[0], new Armadura(
                    c[1],
                    Integer.parseInt(c[2]),
                    c[3].charAt(0),
                    Integer.parseInt(c[4]),
                    Armadura.Slot.valueOf(c[5])
            ));
        }
    }

    private void carregaPocions() {
        for (String[] c : llegirFitxer("/pocions.txt")) {
            // c[0]=id c[1]=nom c[2]=pes c[3]=simbol c[4]=tipus c[5]=valor
            pocions.put(c[0], new Pocio(
                    c[1],
                    Integer.parseInt(c[2]),
                    c[3].charAt(0),
                    Pocio.Tipus.valueOf(c[4]),
                    Integer.parseInt(c[5])
            ));
        }
    }

    // Llegeix un fitxer de recursos, ignora comentaris i línies buides,
    // i retorna cada línia com un array de camps (separador: |, sense espais sobrers)
    private Iterable<String[]> llegirFitxer(String ruta) {
        Map<String, String[]> files = new LinkedHashMap<>();
        InputStream in = getClass().getResourceAsStream(ruta);
        if (in == null) {
            throw new RuntimeException("No s'ha trobat el fitxer: " + ruta);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String linia;
            while ((linia = br.readLine()) != null) {
                linia = linia.trim();
                if (linia.isEmpty() || linia.startsWith("#")) {
                    continue;
                }
                String[] camps = linia.split("\\|");
                for (int i = 0; i < camps.length; i++) {
                    camps[i] = camps[i].trim();
                }
                files.put(camps[0], camps);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error llegint " + ruta + ": " + e.getMessage());
        }

        return files.values();
    }

    // Retorna una NOVA instància cada cop — els items no es comparteixen entre jugadors
    public Arma arma(String id) {
        Arma src = armes.get(id);
        if (src == null) {
            throw new IllegalArgumentException("Arma desconeguda: " + id);
        }
        return new Arma(src.getNom(), src.getPes(), src.getSimbol(), src.getAtac(), src.getRang());
    }

    public Armadura armadura(String id) {
        Armadura src = armadures.get(id);
        if (src == null) {
            throw new IllegalArgumentException("Armadura desconeguda: " + id);
        }
        return new Armadura(src.getNom(), src.getPes(), src.getSimbol(), src.getDefensa(), src.getSlot());
    }

    public Pocio pocio(String id) {
        Pocio src = pocions.get(id);
        if (src == null) {
            throw new IllegalArgumentException("Poció desconeguda: " + id);
        }
        return new Pocio(src.getNom(), src.getPes(), src.getSimbol(), src.getTipus(), src.getValor());
    }

    public Map<String, Arma> todesLesArmes() { return armes; }
    public Map<String, Armadura> todesLesArmadures() { return armadures; }
    public Map<String, Pocio> totesLesPocions() { return pocions; }

    //cerca un item pel nom en totes les categories; retorna null si no el troba
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
