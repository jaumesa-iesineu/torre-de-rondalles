/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iessineu.rondalles;

import com.iessineu.rondalles.db.PartidaRepository;
import com.iessineu.rondalles.editor.EditorMapes;
import com.iessineu.rondalles.joc.CarregadorGame;
import com.iessineu.rondalles.joc.ConfigGame;
import com.iessineu.rondalles.joc.Joc;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jaume, dani, sergi, kanhai i pere
 */
public class Main {

    //arguments acceptats:
    //(res)     -> carrega sa config des del game.json de dins
    //-game/-g <fitxer> -> carrega un JSON extern i el sincronitza (maxim 1)
    //-mod/-m <fitxer> -> aplica un mod JSON sobre la config base (es pot repetir, ordre important)
    //-mute     -> arranca sense musica
    //mapes     -> obre l'editor de mapes buit
    //mapes <fitxer> -> obre l'editor de mapes amb un fitxer existent
    public static void main(String[] args) throws Exception {

        if (args.length > 0 && args[0].equals("mapes")) {
            if (args.length > 1) {
                EditorMapes.lancarAmbMapa(args[1]);
            } else {
                EditorMapes.lancar();
            }
            return;
        }

        String fitxerGame = null;
        List<String> fitxersMod = new ArrayList<>();
        boolean silenci = false;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-game", "-g", "--game" -> {
                    if (i + 1 < args.length) fitxerGame = args[++i];
                }
                case "-mod", "-m" -> {
                    //agafam tots es fitxers seguits fins a sa proxima opcio (per poder encadenar -mod a.json b.json)
                    while (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                        fitxersMod.add(args[++i]);
                    }
                }
                case "-mute" -> silenci = true;
            }
        }

        //carregam la configuració directament des del JSON
        ConfigGame config;
        if (fitxerGame != null) {
            // establim la base dir perquè subfitxers i mapes es trobin relatius al game.json
            java.io.File gameFile = new java.io.File(fitxerGame).getAbsoluteFile();
            CarregadorGame.setBasedir(gameFile.getParent());
            config = CarregadorGame.carregaFitxerExtern(fitxerGame, true);
        } else {
            //per defecte agafam es game.json; si no es troba, recorrem a sa copia de sa BD SQLite
            try {
                config = CarregadorGame.carrega("game.json");
                PartidaRepository.inicialitza(config);
            } catch (Exception e) {
                System.err.println("No s'ha trobat game.json, carregant des de sa BD SQLite: " + e.getMessage());
                config = PartidaRepository.carregaConfig();
            }
        }

        //aplicam els mods en ordre (last wins per conflictes)
        for (String fitxerMod : fitxersMod) {
            try {
                System.out.println("[mod] carregant " + fitxerMod + "...");
                ConfigGame mod = CarregadorGame.carregaFitxerExtern(fitxerMod);
                CarregadorGame.aplicaMod(config, mod);

                if (mod.enemics != null && mod.enemics.tipus != null) {
                    System.out.println("   -> ha tocat " + mod.enemics.tipus.size() + " tipus d'enemic (vida/atac/etc)");
                }
                if (mod.enemics != null && mod.enemics.posicions != null) {
                    System.out.println("   -> ha afegit " + mod.enemics.posicions.size() + " enemics nous an es mapa");
                }
                if (mod.items != null && mod.items.posicions != null) {
                    System.out.println("   -> ha posat " + mod.items.posicions.size() + " objectes nous pen es mapa");
                }
                if (mod.portes != null && mod.portes.posicions != null) {
                    System.out.println("   -> ha afegit " + mod.portes.posicions.size() + " portes noves");
                }
                if (mod.equipamentInicial != null) {
                    System.out.println("   -> ha canviat s'equipament inicial des jugador");
                }

                //si es mod du objectes nous de cap (armes, armadures, pocions, claus...)
                var catalogMod = CarregadorGame.llegeixCatalogItemsDelMod(fitxerMod);
                if (catalogMod != null) {
                    com.iessineu.rondalles.inventari.RegistreItems.get().aplicaMod(catalogMod);
                    System.out.println("   -> ha donat d'alta objectes nous an es catàleg (armes/armadures/pocions/claus)");
                }

                System.out.println("[mod] " + fitxerMod + " aplicat be");
            } catch (Exception e) {
                System.err.println("[mod] no s'ha pogut carregar " + fitxerMod + ": " + e.getMessage());
            }
        }

        Joc joc = new Joc(config, silenci);
        joc.start();
    }
}
