package com.iessineu.rondalles.joc;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class CarregadorGame {

    public static ConfigGame carrega(String rutaRecurs) throws Exception {
        InputStream is = CarregadorGame.class.getClassLoader().getResourceAsStream(rutaRecurs);
        if (is == null) throw new RuntimeException("No s'ha trobat: " + rutaRecurs);
        ConfigGame config;
        try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            config = new Gson().fromJson(reader, ConfigGame.class);
        }
        //per cada tipus que té artFitxer, carregam l'art del fitxer .txt
        if (config.enemics != null && config.enemics.tipus != null) {
            for (TipusEnemic t : config.enemics.tipus) {
                if (t.artFitxer != null && !t.artFitxer.isBlank()) {
                    t.artAscii = carregaArt(t.artFitxer);
                }
            }
        }
        return config;
    }

    private static String[] carregaArt(String rutaRecurs) {
        InputStream is = CarregadorGame.class.getClassLoader().getResourceAsStream(rutaRecurs);
        if (is == null) return null;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return br.lines().toArray(String[]::new);
        } catch (Exception e) {
            return null;
        }
    }
}
