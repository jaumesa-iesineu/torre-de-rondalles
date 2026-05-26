/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.iesineu.rondalles.motor;

import cat.iesineu.rondalles.entitats.Entitat;
import cat.iesineu.rondalles.mapa.Mapa;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author jaume, dani, sergi, kanhai i pere
 */
public class Renderitzador {

    //la pantalla de lanterna
    private Screen screen;

    //el radi de la llanterna en caselles
    //tot el que quedi fora d'aquest radi es veu negre
    private static final int RADI_LLANTERNA = 14;

    public Renderitzador() throws IOException {
        Terminal terminal = new DefaultTerminalFactory().createTerminal();
        screen = new TerminalScreen(terminal);
        screen.startScreen();
        //amagam el cursor, no el necessitam
        screen.setCursorPosition(null);
    }

    //pinta tot el mapa aplicant l'efecte de llanterna des de la posició del jugador
    public void dibuixa(Mapa mapa, int jx, int jy, List<Entitat> entitats) throws IOException {
        screen.clear();

        char[][] celles = mapa.getCelles();

        for (int y = 0; y < celles.length; y++) {
            for (int x = 0; x < celles[y].length; x++) {
                //calculam la distància des d'aquesta casella fins al jugador
                double dist = Math.sqrt((x - jx) * (x - jx) + (y - jy) * (y - jy));

                //si queda fora del radi de la llanterna no pintam res (negre total)
                if (dist > RADI_LLANTERNA) continue;

                //com més lluny del jugador, més fosc
                //al límit del radi queda al 50% de brillantor
                double factor = 1.0 - (dist / RADI_LLANTERNA) * 0.5;

                TextColor colorBase = colorPerCasella(celles[y][x]);
                TextColor colorFinal = fosqueix(colorBase, factor);

                screen.setCharacter(x, y, new TextCharacter(celles[y][x], colorFinal, TextColor.ANSI.BLACK));
            }
        }

        //pintem les entitats actives per damunt del mapa
        for (Entitat e : entitats) {
            if (!e.isActiu()) continue;
            double dist = Math.sqrt((e.getX() - jx) * (e.getX() - jx) + (e.getY() - jy) * (e.getY() - jy));
            if (dist > RADI_LLANTERNA) continue;

            double factor = 1.0 - (dist / RADI_LLANTERNA) * 0.5;
            TextColor color = fosqueix(colorPerEntitat(e.getSimbol()), factor);
            screen.setCharacter(e.getX(), e.getY(), new TextCharacter(e.getSimbol(), color, TextColor.ANSI.BLACK));
        }

        //el jugador sempre es pinta verd per damunt de tot
        screen.setCharacter(jx, jy, new TextCharacter('@', TextColor.ANSI.GREEN_BRIGHT, TextColor.ANSI.BLACK));

        screen.refresh();
    }

    //cada tipus de casella té un color base diferent
    private TextColor colorPerCasella(char c) {
        return switch (c) {
            case '#' -> new TextColor.RGB(130, 130, 140); //parets gris blavós
            case '.' -> new TextColor.RGB(70, 50, 35);   //terra marró fosc
            case 'e' -> new TextColor.RGB(200, 50, 50);  //enemic vermell
            case 'i' -> new TextColor.RGB(220, 180, 50); //item groc
            case 'N' -> new TextColor.RGB(80, 200, 220); //npc cian
            default  -> new TextColor.RGB(90, 90, 90);
        };
    }

    //color base per als símbols d'entitats
    private TextColor colorPerEntitat(char simbol) {
        return switch (simbol) {
            case 'f', 'd' -> new TextColor.RGB(200, 50, 50);  //follets i dimonis, vermell
            default       -> new TextColor.RGB(180, 180, 50);  //la resta, groc
        };
    }

    //aplica un factor d'oscuritat als components rgb del color
    private TextColor fosqueix(TextColor color, double factor) {
        if (!(color instanceof TextColor.RGB rgb)) return color;
        int r = (int)(rgb.getRed()   * factor);
        int g = (int)(rgb.getGreen() * factor);
        int b = (int)(rgb.getBlue()  * factor);
        return new TextColor.RGB(r, g, b);
    }

    //espera que l'usuari premi una tecla (bloquejant)
    //és bloquejant perquè el joc és per torns, no fa falta bucle actiu
    public KeyStroke llegeixInput() throws IOException {
        return screen.readInput();
    }

    public void tanca() throws IOException {
        screen.close();
    }
}
