package com.iessineu.rondalles.joc;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CarregadorGame {

    //carrega es game.json des de dins del jar (us intern per defecte)
    public static ConfigGame carrega(String rutaRecurs) throws Exception {
        InputStream is = CarregadorGame.class.getClassLoader().getResourceAsStream(rutaRecurs);
        if (is == null) {
            throw new RuntimeException("No s'ha trobat: " + rutaRecurs);
        }
        ConfigGame config;
        try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            config = new Gson().fromJson(reader, ConfigGame.class);
        }
        resolgArt(config);
        return config;
    }

    //carrega un JSON extern del sistema de fitxers (argument -game o -mod)
    public static ConfigGame carregaFitxerExtern(String ruta) throws Exception {
        try (Reader reader = new InputStreamReader(new FileInputStream(ruta), StandardCharsets.UTF_8)) {
            ConfigGame config = new Gson().fromJson(reader, ConfigGame.class);
            resolgArt(config);
            return config;
        }
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

    private static void resolgArt(ConfigGame config) {
        if (config.enemics != null && config.enemics.tipus != null) {
            for (TipusEnemic t : config.enemics.tipus) {
                if (t.artFitxer != null && !t.artFitxer.isBlank()) {
                    t.artAscii = carregaArt(t.artFitxer);
                }
            }
        }
        if (config.jugador != null && config.jugador.artFitxer != null && !config.jugador.artFitxer.isBlank()) {
            config.jugador.artAscii = carregaArt(config.jugador.artFitxer);
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
