package com.iessineu.rondalles.motor;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

// Llegeix un fitxer JSON amb la pantalla de victoria.
// Format esperat: { "titol": "...", "text": [...], "art": [...] }
public class CarregadorPantallaVictoria {

    private static class Dades {
        String titol;
        String[] text;
        String[] art;
    }

    public static PantallaVictoria carrega(String rutaRecurs) {
        if (rutaRecurs == null || rutaRecurs.isBlank()) return null;
        InputStream is = CarregadorPantallaVictoria.class.getClassLoader()
                .getResourceAsStream(rutaRecurs);
        if (is == null) return null;
        try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            Dades dades = new Gson().fromJson(reader, Dades.class);
            if (dades == null) return null;
            return new PantallaVictoria(dades.titol, dades.text, dades.art);
        } catch (Exception e) {
            return null;
        }
    }
}
