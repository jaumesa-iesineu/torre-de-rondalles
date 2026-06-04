package com.iessineu.rondalles.joc;

import java.util.List;

//POJO que Gson omple amb les dades de cada tipus d'enemic del game.json
public class TipusEnemic {

    public List<String> simbols; //ex: ["d","e"] — un sol tipus pot tenir varis simbols
    public String nom;
    public int vida;
    public int atac;
    public int radi;
    public int colorR;
    public int colorG;
    public int colorB;
    public boolean estatica;
    public int velocitat = 1; //cada quants torns pot actuar (1=cada torn, 2=cada 2, etc.)
    public boolean travessaParets; //pot passar a través de parets (#)
    public String artFitxer;  //ruta a resources/art/xxx.txt (prioritat sobre artAscii)
    public String[] artAscii; //fallback inline si no hi ha artFitxer
}
