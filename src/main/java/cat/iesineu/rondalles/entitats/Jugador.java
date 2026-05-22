/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.iesineu.rondalles.entitats;

/**
 *
 * @author jaume, dani, sergi, kanhai i pere
 */
public class Jugador extends Entitat {

    //tots els estats en que pot estar el jugador
    public enum EstatJugador {
        //quiet vol dir que esperem que premi una tecla
        QUIET,
        //movent és just quan acaba de fer un pas
        MOVENT,
        //atacant és quan pega a un enemic
        ATACANT,
        //mort quan hp arriba a 0, fin del joc
        MORT
    }

    //punts de vida actuals i màxims
    private int hp;
    private int hpMax;

    //el pes que du a sobre (afecta quant de ràpid es mou)
    private int pes;
    private int pesMax;

    //velocitat base, sense pes
    private int velocitat;

    //en quin estat es troba el jugador ara mateix
    private EstatJugador estatJugador;

    public Jugador(int x, int y) {
        super(x, y, '@');
        this.hpMax = 100;
        this.hp = hpMax;
        this.pesMax = 50;
        this.pes = 0;
        this.velocitat = 5;
        this.estatJugador = EstatJugador.QUIET;
    }

    @Override
    public void actualitza() {
        //TODO: aquí hi hauran els efectes per torn (verí, regeneració de hp, fam...)
    }

    @Override
    public void interactua(Jugador jugador) {
        //el jugador no interactua amb ell mateix
    }

    //la velocitat efectiva baixa proporcionalment al pes que du
    public int velocitatEfectiva() {
        if (pes >= pesMax) return 1;
        return Math.max(1, (int)(velocitat * (1.0 - (double) pes / pesMax)));
    }

    public boolean esMort() {
        return hp <= 0;
    }

    public void rebreDany(int dany) {
        hp -= dany;
        if (hp <= 0) {
            hp = 0;
            estatJugador = EstatJugador.MORT;
        }
    }

    public int getHp() { return hp; }
    public int getHpMax() { return hpMax; }
    public int getPes() { return pes; }
    public int getPesMax() { return pesMax; }
    public EstatJugador getEstatJugador() { return estatJugador; }
    public void setEstatJugador(EstatJugador e) { this.estatJugador = e; }
}
