/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iessineu.rondalles.motor;

import com.iessineu.rondalles.entitats.Entitat;
import com.iessineu.rondalles.mapa.Mapa;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFontConfiguration;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;
import java.awt.Font;
import java.io.IOException;
import java.util.List;
import javax.swing.JFrame;

/**
 *
 * @author jaume, dani, sergi, kanhai i pere
 */
public class Renderitzador {

    //la pantalla de lanterna
    private Screen screen;

    //el radi de la llanterna en caselles
    //tot el que quedi fora d'aquest radi es veu negre
    private static final int RADI_LLANTERNA = 10;

    public Renderitzador() throws IOException {
        //usam swing perquè funcioni des de netbeans i des de qualsevol màquina
        //obre una finestra pròpia del joc, com fan els jocs reals
        SwingTerminalFontConfiguration font = SwingTerminalFontConfiguration
                .newInstance(new Font("Monospaced", Font.PLAIN, 18));
        SwingTerminalFrame terminal = new DefaultTerminalFactory()
                .setInitialTerminalSize(new TerminalSize(120, 40))
                .setTerminalEmulatorFontConfiguration(font)
                .createSwingTerminal();
        //maximitzam la finestra perquè ocupi tota la pantalla
        terminal.setExtendedState(JFrame.MAXIMIZED_BOTH);
        //la finestra ha d'estar visible abans de cridar startScreen
        //si no, les dimensions són 0x0 i peta
        terminal.setVisible(true);
        screen = new TerminalScreen(terminal);
        screen.startScreen();
        //amagam el cursor, no el necessitam
        screen.setCursorPosition(null);
    }

    // CANVI 1: afegit el paràmetre "jugador" al mètode dibuixa
    public void dibuixa(Mapa mapa, int jx, int jy, List<Entitat> entitats, com.iessineu.rondalles.entitats.Jugador jugador) throws IOException {
        screen.clear();

        char[][] celles = mapa.getCelles();

        //calculam quant hem de desplaçar el mapa per centrar-lo a la pantalla
        int cols = screen.getTerminalSize().getColumns();
        int files = screen.getTerminalSize().getRows();
        int offsetX = Math.max(0, (cols - mapa.getAmplada()) / 2);
        int offsetY = Math.max(0, (files - mapa.getAlcada()) / 2);

        for (int y = 0; y < celles.length; y++) {
            for (int x = 0; x < celles[y].length; x++) {
                double dist = Math.sqrt(((x - jx) * (x - jx)) + ((y - jy) * (y - jy)*4));//Modificar camp te visió.
                if (dist > RADI_LLANTERNA) continue;

                double factor = 1.0 - (dist / RADI_LLANTERNA) * 0.75;//Modificar degradat de la visió.
                TextColor colorBase = colorPerCasella(celles[y][x]);
                TextColor colorFinal = fosqueix(colorBase, factor);

                screen.setCharacter(offsetX + x, offsetY + y, new TextCharacter(celles[y][x], colorFinal, TextColor.ANSI.BLACK));
            }
        }

        //pintem les entitats actives per damunt del mapa
        for (Entitat e : entitats) {
            if (!e.isActiu()) continue;
            double dist = Math.sqrt((e.getX() - jx) * (e.getX() - jx) + (e.getY() - jy) * (e.getY() - jy));
            if (dist > RADI_LLANTERNA) continue;

            double factor = 1.0 - (dist / RADI_LLANTERNA) * 0.5;
            TextColor color = fosqueix(colorPerEntitat(e.getSimbol()), factor);
            screen.setCharacter(offsetX + e.getX(), offsetY + e.getY(), new TextCharacter(e.getSimbol(), color, TextColor.ANSI.BLACK));
        }

        //el jugador sempre es pinta verd per damunt de tot
        screen.setCharacter(offsetX + jx, offsetY + jy, new TextCharacter('@', TextColor.ANSI.GREEN_BRIGHT, TextColor.ANSI.BLACK));

        // CANVI 2: cridam el HUD just abans de fer refresh
        dibuixaHUD(jugador);

        screen.refresh();
    }

    //cada tipus de casella té un color base diferent
    private TextColor colorPerCasella(char c) {
        return switch (c) {
            case '#' -> new TextColor.RGB(130, 130, 140); //parets
            case '.' -> new TextColor.RGB(70, 50, 35); //terra
            case 'e' -> new TextColor.RGB(200, 50, 50); //enemic
            case 'i' -> new TextColor.RGB(220, 180, 50); //item
            case 'N' -> new TextColor.RGB(80, 200, 220); //npc
            default -> new TextColor.RGB(90, 90, 90);
        };
    }

    //color base per als símbols d'entitats
    private TextColor colorPerEntitat(char simbol) {
        return switch (simbol) {
            case 'f', 'd' -> new TextColor.RGB(200, 50, 50); //follets i dimonis
            default -> new TextColor.RGB(180, 180, 50);
        };
    }

    //aplica un factor d'oscuritat als components rgb del color
    private TextColor fosqueix(TextColor color, double factor) {
        if (!(color instanceof TextColor.RGB rgb)) return color;
        int r = (int)(rgb.getRed() * factor);
        int g = (int)(rgb.getGreen() * factor);
        int b = (int)(rgb.getBlue() * factor);
        return new TextColor.RGB(r, g, b);
    }

    // CANVI 3: mètodes nous per pintar el HUD
    private void dibuixaHUD(com.iessineu.rondalles.entitats.Jugador jugador) {
        int col = 1;
        int fila = 1;

        String vida = "VIDA:  " + jugador.getVida() + " / " + jugador.getVidaMaxima();
        pintaText(col, fila, vida, new TextColor.RGB(220, 50, 50));

        String pes = "PES:   " + jugador.getPes() + " / " + jugador.getpesMaxim() + " kg";
        pintaText(col, fila + 1, pes, new TextColor.RGB(180, 160, 80));

        String armes = "ATAC:  " + jugador.getAtacTotal();
        pintaText(col, fila + 2, armes, new TextColor.RGB(200, 120, 50));

        String armadura = "DEF:   " + jugador.getDefensaTotal();
        pintaText(col, fila + 3, armadura, new TextColor.RGB(100, 160, 220));

        String inv = "INV:   (buit)";
        pintaText(col, fila + 4, inv, new TextColor.RGB(140, 200, 140));
    }

    private void pintaText(int col, int fila, String text, TextColor color) {
        for (int i = 0; i < text.length(); i++) {
            screen.setCharacter(col + i, fila, new TextCharacter(text.charAt(i), color, TextColor.ANSI.BLACK));
        }
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