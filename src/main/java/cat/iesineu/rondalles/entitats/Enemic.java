/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.iesineu.rondalles.entitats;

/**
 *
 * @author jaume, dani, sergi, kanhai i pere
 */
public abstract class Enemic extends Entitat {

    //màquina d'estats de l'enemic
    //cada estat defineix un comportament diferent
    public enum EstatEnemic {
        //patrullant: es mou pel seu camí marcat al fitxer .game
        PATRULLANT,
        //alerta: ha sentit qualque cosa però no veu el jugador
        //es gira cap al so i mira
        ALERTA,
        //perseguint: ha vist el jugador i va directament cap a ell
        PERSEGUINT,
        //atacant: el jugador és al costat i li pega cada torn
        ATACANT,
        //atordit: ha rebut un cop fort, es salta un torn sencer
        ATORDIT,
        //fugint: li queden pocs hp i intenta escapar del jugador
        FUGINT,
        //mort: hp és 0, l'entitat queda inactiva
        MORT
    }

    //vida i dany de l'enemic
    protected int hp;
    protected int hpMax;
    protected int atac;

    //fins a quina distància detecta el jugador
    protected int radDeteccio;

    //estat actual de la màquina
    protected EstatEnemic estatEnemic;

    public Enemic(int x, int y, char simbol, int hp, int atac, int radDeteccio) {
        super(x, y, simbol);
        this.hpMax = hp;
        this.hp = hp;
        this.atac = atac;
        this.radDeteccio = radDeteccio;
        this.estatEnemic = EstatEnemic.PATRULLANT;
    }

    //cada tipus d'enemic té la seva pròpia IA
    //es crida quan el jugador fa un moviment (és el seu torn)
    public abstract void actualitzaIA(Jugador jugador);

    @Override
    public void actualitza() {
        //TODO: quan tinguem el jugador accessible des d'aquí cridam actualitzaIA
    }

    @Override
    public void interactua(Jugador jugador) {
        //quan el jugador intenta entrar a la casella de l'enemic, ataca
        //TODO: implementar el sistema de combat complet
        jugador.rebreDany(atac);
        estatEnemic = EstatEnemic.ATACANT;
    }

    //distància euclidiana fins al jugador
    protected double distanciaAl(Jugador jugador) {
        int dx = jugador.getX() - this.x;
        int dy = jugador.getY() - this.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public void canviaEstat(EstatEnemic nouEstat) {
        this.estatEnemic = nouEstat;
    }

    public void rebreDany(int dany) {
        hp -= dany;
        if (hp <= 0) {
            hp = 0;
            estatEnemic = EstatEnemic.MORT;
            actiu = false;
        } else if (hp < hpMax / 4) {
            //si queda amb menys d'un quart de vida, fuig
            estatEnemic = EstatEnemic.FUGINT;
        }
    }

    public EstatEnemic getEstatEnemic() { return estatEnemic; }
    public int getHp() { return hp; }
    public boolean esMort() { return hp <= 0; }
}
