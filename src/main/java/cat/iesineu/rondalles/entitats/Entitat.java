/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.iesineu.rondalles.entitats;

/**
 *
 * @author jaume, dani, sergi, kanhai i pere
 */
public abstract class Entitat {

    //posició de l'entitat al mapa
    protected int x;
    protected int y;

    //el caràcter que es pinta a la pantalla
    protected char simbol;

    //si actiu és false, l'entitat no es pinta ni interactua
    //ho feim servir per les entitats mortes o recollides
    protected boolean actiu;

    public Entitat(int x, int y, char simbol) {
        this.x = x;
        this.y = y;
        this.simbol = simbol;
        this.actiu = true;
    }

    //cada entitat sap actualitzar-se ella mateixa cada torn
    public abstract void actualitza();

    //quan el jugador xoca amb una entitat passa alguna cosa
    //pot ser combat, recollir un item, parlar amb un npc...
    public abstract void interactua(Jugador jugador);

    public int getX() { return x; }
    public int getY() { return y; }
    public char getSimbol() { return simbol; }
    public boolean isActiu() { return actiu; }

    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setActiu(boolean actiu) { this.actiu = actiu; }
}
