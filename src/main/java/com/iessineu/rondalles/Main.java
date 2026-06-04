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
    //(res)  -> carrega la config des de la BD SQLite (joc empaquetado)
    //-game/-g <fitxer> -> carrega la config des d'un JSON extern (màxim 1)
    //-mod/-m <fitxer> -> aplica un mod JSON sobre la config base (es pot repetir, ordre important)
    //-mute -> arranca sense música
    //mapes -> obre l'editor de mapes buit
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
        boolean mut = false;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-game", "-g" -> {
                    if (i + 1 < args.length) fitxerGame = args[++i];
                }
                case "-mod", "-m" -> {
                    if (i + 1 < args.length) fitxersMod.add(args[++i]);
                }
                case "-mute" -> mut = true;
            }
        }

        //carregam la config base: JSON extern si han passat -game, BD SQLite si no
        ConfigGame config;
        if (fitxerGame != null) {
            config = CarregadorGame.carregaFitxerExtern(fitxerGame);
        } else {
            PartidaRepository.inicialitzaDefecte();
            config = PartidaRepository.carregaConfig();
        }

        //aplicam els mods en ordre (last wins per conflictes)
        for (String fitxerMod : fitxersMod) {
            try {
                ConfigGame mod = CarregadorGame.carregaFitxerExtern(fitxerMod);
                CarregadorGame.aplicaMod(config, mod);
            } catch (Exception e) {
                System.err.println("No s'ha pogut carregar el mod " + fitxerMod + ": " + e.getMessage());
            }
        }

        Joc joc = new Joc(config, mut);
        joc.start();
    }
}
