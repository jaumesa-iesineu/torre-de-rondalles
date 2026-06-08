package com.iessineu.rondalles.motor;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

// animacio typewriter reutilitzable, per dialegs, game over, victoria...
public class MotorDialog {

    private final String titol;
    private final String[] linies;
    private final int cps;

    private long inici;
    private int caractersVisibles;
    private boolean animant;
    private boolean acabada;

    public MotorDialog(String titol, String[] linies, int cps) {
        this.titol = titol != null ? titol : "";
        this.linies = linies != null ? linies : new String[0];
        this.cps = cps;
        this.animant = false;
        this.acabada = false;
    }

    // carrega un dialog des d'un json de resources
    public static MotorDialog carrega(String rutaRecurs, int cps) {
        if (rutaRecurs == null || rutaRecurs.isBlank()) return null;
        InputStream is = MotorDialog.class.getClassLoader().getResourceAsStream(rutaRecurs);
        if (is == null) return null;
        try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            Dades dades = new Gson().fromJson(reader, Dades.class);
            if (dades == null) return null;
            return new MotorDialog(dades.titol, dades.text, cps);
        } catch (Exception e) {
            return null;
        }
    }

    private static class Dades {
        String titol;
        String[] text;
    }

    // arrenca s'animacio desde zero
    public void inicia() {
        this.inici = System.currentTimeMillis();
        this.caractersVisibles = 0;
        this.animant = true;
        this.acabada = false;
    }

    // avança es caracters visibles segons es temps, s'ha de cridar cada frame
    public void actualitza() {
        if (!animant) return;
        long delta = System.currentTimeMillis() - inici;
        int total = totalCaracters();
        int nous = (int) ((long) delta * cps / 1000L);
        if (nous >= total) {
            caractersVisibles = total;
            animant = false;
            acabada = true;
        } else {
            caractersVisibles = nous;
        }
    }

    // total de caracters del text (salts de linia conten)
    public int totalCaracters() {
        int total = 0;
        for (int i = 0; i < linies.length; i++) {
            total += linies[i].length();
            if (i < linies.length - 1) total += 1;
        }
        return total;
    }

    public String getTitol() { return titol; }
    public String[] getLinies() { return linies; }
    public int getCaractersVisibles() { return caractersVisibles; }
    public boolean esAnimant() { return animant; }
    public boolean esAcabada() { return acabada; }
}
