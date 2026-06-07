package com.iessineu.rondalles.motor;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

// Llegeix un fitxer JSON amb la pantalla de game over.
// Format esperat: { "titol": "...", "text": [...], "art": [...] }
public class CarregadorPantallaGameOver {

    // POJO intern que Gson omple amb el JSON.
    private static class Dades {
        String titol;
        String[] text;
        String[] art;
    }

    // Carrega un fitxer des de recursos (classpath). Retorna null si no el troba.
    public static PantallaGameOver carrega(String rutaRecurs) {
        if (rutaRecurs == null || rutaRecurs.isBlank()) return null;
        InputStream is = CarregadorPantallaGameOver.class.getClassLoader()
                .getResourceAsStream(rutaRecurs);
        if (is == null) return null;
        try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            Dades dades = new Gson().fromJson(reader, Dades.class);
            if (dades == null) return null;
            return new PantallaGameOver(dades.titol, dades.text, dades.art);
        } catch (Exception e) {
            return null;
        }
    }
}
