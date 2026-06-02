/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iessineu.rondalles;

import com.iessineu.rondalles.editor.EditorMapes;
import com.iessineu.rondalles.joc.Joc;

/**
 *
 * @author jaume, dani, sergi, kanhai i pere
 */
public class Main {

    //arguments acceptats:
    //(res)          -> joc normal llegint game.json (mapa inicial definit al json)
    //-game <id|fitxer> -> joc amb un id de mapa del game.json o fitxer directe
    //-mute          -> arranca sense música
    //mapes          -> obre s'editor de mapes buit
    //mapes <fitxer> -> obre s'editor de mapes amb un fitxer existent
    public static void main(String[] args) throws Exception {

        if (args.length > 0) {
            switch (args[0]) {
                case "mapes" -> {
                    if (args.length > 1) {
                        EditorMapes.lancarAmbMapa(args[1]);
                    } else {
                        EditorMapes.lancar();
                    }
                    return;
                }
            }
        }

        //mode normal: per defecte usa el mapaInicial del game.json
        String fitxer = "planta1"; //id del game.json; Joc el resol a un fitxer real
        boolean mut = false;
        for (int i = 0; i < args.length; i++) {
            if ((args[i].equals("-game") || args[i].equals("-mod")) && i + 1 < args.length) {
                fitxer = args[i + 1];
            }
            if (args[i].equals("-mute")) {
                mut = true;
            }
        }

        Joc joc = new Joc(fitxer, mut);
        joc.start();
    }
}
