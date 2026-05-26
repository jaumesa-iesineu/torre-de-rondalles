/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iessineu.rondalles;

import com.iessineu.rondalles.joc.Joc;

/**
 *
 * @author jaume, dani, sergi, kanhai i pere
 */
public class Main {

    public static void main(String[] args) throws Exception {
        //arguments acceptats:
        //-game <fitxer> carrega un fitxer .game concret
        //-mod <fitxer> carrega un mod (futur, de moment igual que -game)
        //res (carrega el fitxer per defecte)
        String fitxer = "mapes/planta1.game";

        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equals("-game") || args[i].equals("-mod")) {
                fitxer = args[i + 1];
            }
        }

        Joc joc = new Joc(fitxer);
        joc.start();
    }
}
