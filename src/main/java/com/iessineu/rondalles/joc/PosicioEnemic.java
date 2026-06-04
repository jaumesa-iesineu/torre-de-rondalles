package com.iessineu.rondalles.joc;

//POJO que Gson omple amb cada posicio d'enemic del game.json
public class PosicioEnemic {
    public String mapa;
    public String simbol;
    public int x;
    public int y;
    public int area; //radi de l'àrea de vigilancia (0 = sense àrea, perseguix sempre)
}
