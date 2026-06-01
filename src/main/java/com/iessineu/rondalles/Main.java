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
    //(res)          -> joc normal amb planta1.map
    //-game <fitxer> -> joc normal amb un fitxer concret
    //-mute          -> arranca sense música
    //mapes          -> obre s'editor de mapes buit
    //mapes <fitxer> -> obre s'editor de mapes amb un fitxer existent
    public static void main(String[] args) throws Exception {

        if (args.length > 0) {
            switch (args[0]) {
                case "mapes" -> {
                    if (args.length > 1) EditorMapes.lancarAmbMapa(args[1]);
                    else                 EditorMapes.lancar();
                    return;
                }
            }
        }

        //mode normal
        String fitxer = "mapes/planta1.map";
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
