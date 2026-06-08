package com.iessineu.rondalles.joc;

import com.iessineu.rondalles.motor.CeldaArt;
import java.util.List;

//POJO que Gson omple amb ses dades de cada tipus d'enemic del game.json
public class TipusEnemic {

    public List<String> simbols; //ex: ["d","e"] — un sol tipus pot tenir varis simbols
    public String nom;
    public int vida;
    public int atac;
    public int radi;
    public int colorR;
    public int colorG;
    public int colorB;
    public int velocitat = 1; //cada quants torns pot actuar (1=cada torn, 2=cada 2, etc)
    public boolean travessaParets; //pot passar a traves de parets (#)
    public boolean estatica; //per compatibilitat amb la BD (millor usar patroIA)

    //patrons d'IA: "perseguir", "guardia", "estatic", "pacman"
    public String patroIA = "perseguir";
    public int pacmanPrevisions = 4;  // passes de previsió per Pinky i Clyde
    public int pacmanFlancPasses = 4; // distància de flanqueig lateral per Clyde
    public boolean requereixDescobriment; //Bubota nomes persegueix si l'han vist
    public boolean esBoss; //quan el maten, apareix una clau a l'inventari

    public String artFitxer;     //ruta a resources/art/xxx.txt
    public String[] artAscii;   //fallback inline
    public String artJsonFitxer; //ruta a resources/art/xxx.json (prioritat sobre txt)
    public CeldaArt[][] artJson; //grid colorit carregat del JSON

    // Ruta al fitxer JSON amb la pantalla de game over personalitzada
    // d'aquest enemic (opcional). Ex: "gameover/drac.json".
    public String gameOver;
}
