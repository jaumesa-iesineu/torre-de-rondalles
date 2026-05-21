/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.iesineu.rondalles;

import cat.iesineu.rondalles.joc.Joc;

/**
 *
 * @author jaume, dani, sergi, kanhai i pere
 */
public class Main {

    public static void main(String[] args) throws Exception {
        //el primer argument és el fitxer del mapa
        //si no en posam cap, agafam el de per defecte
        String fitxer = args.length > 0 ? args[0] : "mapes/planta1.game";

        //creim el joc i l'engegam
        Joc joc = new Joc(fitxer);
        joc.start();
    }
}
