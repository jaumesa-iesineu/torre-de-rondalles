package com.iessineu.rondalles.joc;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class CarregadorGame {

    public static ConfigGame carrega(String rutaRecurs) throws Exception {
        InputStream is = CarregadorGame.class.getClassLoader().getResourceAsStream(rutaRecurs);
        if (is == null) throw new RuntimeException("No s'ha trobat: " + rutaRecurs);
        try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            return new Gson().fromJson(reader, ConfigGame.class);
        }
    }
}
