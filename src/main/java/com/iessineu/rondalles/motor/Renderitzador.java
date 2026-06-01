/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iessineu.rondalles.motor;

import com.iessineu.rondalles.entitats.Enemic;
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
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JFrame;

/**
 *
 * @author jaume, dani, sergi, kanhai i pere
 */
public class Renderitzador { // classe per gestionar la pantalla

    //la pantalla de lanterna
    private Screen screen;

    //el radi de la llanterna en caselles
    //tot el que quedi fora d'aquest radi es veu negre
    private static final int RADI_LLANTERNA = 10;

    //amplada del panell dret d'estadístiques (columnes)
    private static final int AMPLE_HUD = 24;

    public Renderitzador() throws IOException {
        java.awt.Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        int midaFont = Math.min(pantalla.width / 120, pantalla.height / 36);
        midaFont = Math.max(14, midaFont);
        SwingTerminalFontConfiguration font = SwingTerminalFontConfiguration
                .newInstance(new Font("Monospaced", Font.PLAIN, midaFont));
        SwingTerminalFrame terminal = new DefaultTerminalFactory()
                .setInitialTerminalSize(new TerminalSize(120, 36))
                .setTerminalEmulatorFontConfiguration(font)
                .createSwingTerminal();
        terminal.setResizable(false);
        //maximitzam la finestra perquè ocupi tota la pantalla
        terminal.setTitle("RONDALLES");
        try (InputStream is = getClass().getResourceAsStream("/logo.png")) {
            if (is != null) {
                BufferedImage icon = ImageIO.read(is);
                terminal.setIconImage(icon);
            }
        } catch (IOException ignored) {}
        terminal.setExtendedState(JFrame.MAXIMIZED_BOTH);
        //la finestra ha d'estar visible abans de cridar startScreen
        //si no, les dimensions són 0x0 i peta
        terminal.setVisible(true);
        screen = new TerminalScreen(terminal);
        screen.startScreen();
        //amagam el cursor, no el necessitam
        screen.setCursorPosition(null);
    }

    public void dibuixa(Mapa mapa, int jx, int jy, List<Entitat> entitats, com.iessineu.rondalles.entitats.Jugador jugador, List<com.iessineu.rondalles.inventari.ItemMapa> itemsMapa, boolean[][] visible, boolean[][] explorat, char[][] mapaRecord) throws IOException {
        screen.clear();

        int cols   = screen.getTerminalSize().getColumns();
        int files  = screen.getTerminalSize().getRows();
        int colSep = cols - AMPLE_HUD; //columna on comença el panell dret

        TextColor blanc    = new TextColor.RGB(180, 180, 195);
        TextColor grisMarc = new TextColor.RGB(70, 70, 85);
        TextColor daurat   = new TextColor.RGB(220, 180, 50);

        //marc: fila superior
        pintaText(0, 0, "╔" + "═".repeat(colSep - 1) + "╦" + "═".repeat(AMPLE_HUD - 2) + "╗", blanc);
        //fila del títol
        pintaText(0, 1, "║", grisMarc);
        String titol = " TORRE DE RONDALLES  ~  " + mapa.getNom();
        if (titol.length() > colSep - 2) titol = titol.substring(0, colSep - 2);
        pintaText(1, 1, titol, daurat);
        pintaText(colSep, 1, "║", grisMarc);
        String titolHud = "PERSONATGE";
        pintaText(colSep + (AMPLE_HUD - 1 - titolHud.length()) / 2, 1, titolHud, new TextColor.RGB(80, 200, 120));
        pintaText(cols - 1, 1, "║", grisMarc);
        //separador davall el títol
        pintaText(0, 2, "╠" + "═".repeat(colSep - 1) + "╣" + " ".repeat(AMPLE_HUD - 2) + "║", blanc);
        //bordes laterals
        for (int i = 3; i < files - 1; i++) {
            pintaText(0, i,        "║", grisMarc);
            pintaText(colSep, i,   "║", grisMarc);
            pintaText(cols - 1, i, "║", grisMarc);
        }
        //fila inferior
        pintaText(0, files - 1, "╚" + "═".repeat(colSep - 1) + "╩" + "═".repeat(AMPLE_HUD - 2) + "╝", blanc);

        //zona visible del mapa: files 3..files-2, cols 1..colSep-1
        int vpW = colSep - 2;
        int vpH = files - 4;

        //càmera: si el mapa cap, el centram; si no, seguim el jugador
        int camX, camY;
        if (mapa.getAmplada() <= vpW) {
            camX = -((vpW - mapa.getAmplada()) / 2);
        } else {
            camX = Math.max(0, Math.min(mapa.getAmplada() - vpW, jx - vpW / 2));
        }
        if (mapa.getAlcada() <= vpH) {
            camY = -((vpH - mapa.getAlcada()) / 2);
        } else {
            camY = Math.max(0, Math.min(mapa.getAlcada() - vpH, jy - vpH / 2));
        }

        char[][] celles = mapa.getCelles();
        TextColor colorMemoria = new TextColor.RGB(45, 45, 55);

        for (int my = 0; my < celles.length; my++) {
            for (int mx = 0; mx < celles[my].length; mx++) {
                int sc = 1 + (mx - camX);
                int sf = 3 + (my - camY);
                if (sc < 1 || sc >= colSep || sf < 3 || sf >= files - 1) continue;
                if (visible[my][mx]) {
                    double dist   = Math.sqrt((mx - jx) * (mx - jx) + (my - jy) * (my - jy));
                    double factor = 1.0 - (dist / RADI_LLANTERNA) * 0.75;
                    screen.setCharacter(sc, sf, new TextCharacter(celles[my][mx], fosqueix(colorPerCasella(celles[my][mx]), factor), fosqueix(fonsCasella(celles[my][mx]), factor)));
                } else if (explorat[my][mx]) {
                    screen.setCharacter(sc, sf, new TextCharacter(mapaRecord[my][mx], colorMemoria, TextColor.ANSI.BLACK));
                }
            }
        }

        //entitats per damunt (només si visibles)
        for (Entitat e : entitats) {
            if (!e.isActiu()) continue;
            if (e.getY() >= visible.length || e.getX() >= visible[e.getY()].length) continue;
            if (!visible[e.getY()][e.getX()]) continue;
            int sc = 1 + (e.getX() - camX);
            int sf = 3 + (e.getY() - camY);
            if (sc < 1 || sc >= colSep || sf < 3 || sf >= files - 1) continue;
            double dist   = Math.sqrt((e.getX() - jx) * (e.getX() - jx) + (e.getY() - jy) * (e.getY() - jy));
            double factor = 1.0 - (dist / RADI_LLANTERNA) * 0.5;
            screen.setCharacter(sc, sf, new TextCharacter(e.getSimbol(), fosqueix(e.getColor(), factor), TextColor.ANSI.BLACK));
        }

        //ítems del terra (només si visibles)
        for (com.iessineu.rondalles.inventari.ItemMapa im : itemsMapa) {
            if (im.getY() >= visible.length || im.getX() >= visible[im.getY()].length) continue;
            if (!visible[im.getY()][im.getX()]) continue;
            int sc = 1 + (im.getX() - camX);
            int sf = 3 + (im.getY() - camY);
            if (sc < 1 || sc >= colSep || sf < 3 || sf >= files - 1) continue;
            double dist   = Math.sqrt((im.getX() - jx) * (im.getX() - jx) + (im.getY() - jy) * (im.getY() - jy));
            double factor = 1.0 - (dist / RADI_LLANTERNA) * 0.75;
            screen.setCharacter(sc, sf, new TextCharacter(im.getItem().getSimbol(), fosqueix(im.getItem().getColor(), factor), TextColor.ANSI.BLACK));
        }

        //jugador sempre verd per damunt de tot
        int psc = 1 + (jx - camX);
        int psf = 3 + (jy - camY);
        if (psc >= 1 && psc < colSep && psf >= 3 && psf < files - 1)
            screen.setCharacter(psc, psf, new TextCharacter('@', TextColor.ANSI.GREEN_BRIGHT, TextColor.ANSI.BLACK));

        dibuixaHUD(jugador, colSep + 1, 3, files - 1);
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

    private TextColor fonsCasella(char c) {
        return switch (c) {
            case '#' -> new TextColor.RGB(40, 40, 50);
            case '.' -> new TextColor.RGB(30, 20, 10);
            default  -> TextColor.ANSI.BLACK;
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

    private void dibuixaHUD(com.iessineu.rondalles.entitats.Jugador jugador, int col, int fila, int filaMax) { //dibuixaHUD pinta les estadistiques al panell dret
        int innerW = AMPLE_HUD - 2;

        TextColor vermell = new TextColor.RGB(220, 60, 60);
        TextColor groc    = new TextColor.RGB(180, 160, 80);
        TextColor taronja = new TextColor.RGB(200, 120, 50);
        TextColor blau    = new TextColor.RGB(100, 160, 220);
        TextColor gris    = new TextColor.RGB(100, 100, 115);

        //barra de vida
        int barW = 12;
        int plens = jugador.getVidaMaxima() > 0
            ? Math.max(0, Math.min(barW, (int)((double)jugador.getVida() / jugador.getVidaMaxima() * barW)))
            : 0;
        pintaText(col, fila, "HP [" + "█".repeat(plens) + "░".repeat(barW - plens) + "]", colorVida(jugador.getVida(), jugador.getVidaMaxima()));
        fila++;
        String vidaStr = jugador.getVida() + " / " + jugador.getVidaMaxima();
        pintaText(col + innerW - vidaStr.length(), fila, vidaStr, vermell);
        fila += 2;

        //estadistiques bàsiques
        pintaText(col, fila++, "ATK  " + jugador.getAtacTotal(),    taronja);
        pintaText(col, fila++, "DEF  " + jugador.getDefensaTotal(), blau);
        pintaText(col, fila++, "PES  " + jugador.getPes() + " / " + jugador.getpesMaxim(), groc);
        fila++;

        //inventari
        pintaText(col, fila++, "--- INVENTARI ---", gris);
        for (int i = 0; i < com.iessineu.rondalles.inventari.Inventari.MAX_SLOTS; i++) {
            if (fila >= filaMax - 2) break;
            var slot = jugador.getInventari().getSlot(i);
            if (slot != null) {
                String q = slot.quantitat() > 1 ? " x" + slot.quantitat() : "";
                String linia = "[" + (i + 1) + "] " + slot.item().getSimbol() + " " + slot.item().getNom() + q;
                if (linia.length() > innerW) linia = linia.substring(0, innerW);
                pintaText(col, fila, linia, slot.item().getColor());
            } else {
                pintaText(col, fila, "[" + (i + 1) + "] -", gris);
            }
            fila++;
        }

        //estats temporals (verí, foc, gel)
        boolean anyEstat = jugador.getTornsVeri() > 0 || jugador.getTornsFoc() > 0 || jugador.getTornsGel() > 0;
        if (anyEstat && fila < filaMax - 1) {
            fila++;
            pintaText(col, fila++, "--- ESTATS ---", gris);
            if (jugador.getTornsVeri() > 0 && fila < filaMax - 1)
                pintaText(col, fila++, "VERI  " + jugador.getTornsVeri() + " torns", new TextColor.RGB(100, 220, 80));
            if (jugador.getTornsFoc() > 0 && fila < filaMax - 1)
                pintaText(col, fila++, "FOC   " + jugador.getTornsFoc() + " torns", new TextColor.RGB(220, 120, 30));
            if (jugador.getTornsGel() > 0 && fila < filaMax - 1)
                pintaText(col, fila, "GEL   " + jugador.getTornsGel() + " torns", new TextColor.RGB(80, 180, 220));
        }
    }

    private void pintaText(int col, int fila, String text, TextColor color) { //pintaText pinta text a la pantalla sense sortir dels límits
        int maxC = screen.getTerminalSize().getColumns();
        int maxF = screen.getTerminalSize().getRows();
        if (fila < 0 || fila >= maxF) return;
        for (int i = 0; i < text.length(); i++) {
            int c = col + i;
            if (c >= 0 && c < maxC)
                screen.setCharacter(c, fila, new TextCharacter(text.charAt(i), color, TextColor.ANSI.BLACK));
        }
    }

    private void pintaTextFons(int col, int fila, String text, TextColor color, TextColor fons) {
        int maxC = screen.getTerminalSize().getColumns();
        int maxF = screen.getTerminalSize().getRows();
        if (fila < 0 || fila >= maxF) return;
        for (int i = 0; i < text.length(); i++) {
            int c = col + i;
            if (c >= 0 && c < maxC)
                screen.setCharacter(c, fila, new TextCharacter(text.charAt(i), color, fons));
        }
    }

        private static final String[] DIMONI_ART = {
    "   , ,, ,                              ",
    "   | || |    ,/  _____  \\.             ",
    "   \\_||_/    ||_/     \\_||             ",
    "     ||       \\_| . . |_/              ",
    "     ||         |  L  |                ",
    "    ,||         |`==='|                ",
    "    |>|      ___`>  -<'___             ",
    "    |>||\\    /             \\           ",
    "    \\>| \\  /  ,    .    .  |           ",
    "     ||  \\/  /| .  |  . |  |           ",
    "     ||\\  ` / | ___|___ |  |     (     ",
    "  (( || `--'  | _______ |  |     ))  ( ",
    "(  )\\|| (  )\\ | - --- - | -| (  ( \\ ))",
    "(\\/  || ))/ ( | -- - -- |  | )) )  \\((",
    " ( ()||((( ())|         |  |( (( () ) "
    };
    private static final String[] GEGANT_ART = {
    "                      __,='`````'=/__                    ",
    "                     '//  (o) \\(o) \\ `'         _,-,    ",
    "                     //|     ,_)   (`\\      ,-'`_,-\\     ",
    "                   ,-~~~\\  `'==='  /-,      \\==```` \\__  ",
    "                  /        `----'     `\\     \\       \\/  ",
    "               ,-`                  ,   \\  ,.-\\       \\  ",
    "              /      ,               \\,-`\\`_,-`\\_,..--'\\ ",
    "             ,`    ,/,              ,>,   )     \\--`````\\ ",
    "             (      `\\`---'`  `-,-'`_,<   \\      \\_,.--'`",
    "              `.      `--. _,-'`_,-`  |    \\              ",
    "               [`-.___   <`_,-'`------(    /              ",
    "               (`` _,-\\   \\ --`````````|--`               ",
    "                >-`_,-`\\,-` ,          |                  ",
    "              <`_,'     ,  /\\          /                  ",
    "               `  \\/\\,-/ `/  \\/`\\_/V\\_/                  ",
    "                  (  ._. )    ( .__. )                   ",
    "                  |      |    |      |                   ",
    "                   \\,---_|    |_---./                    ",
    "                   ooOO(_)    (_)OOoo                    "
};
    
        private static final String[] BUBOTA_ART = {
    "        .-----.",
    "      .' -   - '.",
    "     /  .-. .-.  \\",
    "     |  | | | |  |",
    "      \\ \\o/ \\o/ /",
    "     _/    ^    \\_",
    "    | \\  '---'  / |",
    "    / /`--. .--`\\ \\",
    "   / /'---` `---'\\ \\",
    "   '.__.       .__.'",
    "      `|     |`",
    "       |     \\",
    "       \\      '--.",
    "       '.        `\\",
    "         `'---.   |",
    "            ,__) /",
    "             `..'"
};
    private static final String[] MARIA_ART = {
        "        _____        ",
        "       /     \\       ",
        "      | () () |      ",
        "       \\  ^  /       ",
        "        |||||        ",
        "   _____|||||_____   ",
        "  /  Na Maria    \\   ",
        " /   Enganxa!!!   \\  ",
        "|   ,         ,   |  ",
        "|  /  \\     /  \\  |  ",
        " \\ \\___\\   /___/ /   ",
        "  \\.....   ...../    ",
        "    \\  | | | |  /    ",
        "     \\_|_|_|_|_/     ",
        "       |     |       ",
        "      /|     |\\      ",
        "     ( |     | )     ",
        "      `'     '`      "
    };

    private static final String[] DRAC_ART = {
        "      ^                       ^",
        "      |\\   \\        /        /|",
        "     /  \\  |\\__  __/|       /  \\",
        "    / /\\ \\ \\ _ \\/ _ /      /    \\",
        "   / / /\\ \\ {*}\\/{*}      /  / \\ \\",
        "   | | | \\ \\( (00) )     /  // |\\ \\",
        "   | | | |\\ \\(V\"\"V)\\    /  / | || \\|",
        "   | | | | \\ |^--^| \\  /  / || || ||",
        "  / / /  | |( WWWW__ \\/  /| || || ||",
        " | | | | | |  \\______\\  / / || || ||",
        " | | | / | | )|______\\ ) | / | || ||",
        " / / /  / /  /______/   /| \\ \\ || ||",
        "/ / /  / /  /\\_____/  |/ /__\\ \\ \\ \\ \\",
        "| | | / /  /\\______/    \\   \\__| \\ \\ \\",
        "| | | | | |\\______ __    \\_    \\__|_| \\",
        "| | ,___ /\\______ _  _     \\_       \\  |",
        "| |/    /\\_____  /    \\      \\__     \\ |    /\\",
        "|/ |   |\\______ |      |        \\___  \\ |__/  \\",
        "v  |   |\\______ |      |            \\___/     |",
        "   |   |\\______ |      |                    __/",
        "    \\   \\________\\_    _\\               ____/",
        "  __/   /\\_____ __/   /   )\\_,      _____/",
        " /  ___/  \\uuuu/  ___/___)    \\______/",
        " VVV  V        VVV  V"
    };

    public void dibuixaCombat(Enemic enemic, com.iessineu.rondalles.entitats.Jugador jugador, List<String> log) throws IOException {
        screen.clear();

        int cols = screen.getTerminalSize().getColumns();
        int rows = screen.getTerminalSize().getRows();

        TextColor blanc = new TextColor.RGB(220, 220, 220);
        TextColor gris = new TextColor.RGB(110, 110, 110);
        TextColor daurat = new TextColor.RGB(220, 180, 50);
        TextColor vermell = new TextColor.RGB(220, 70, 70);

        //marc exterior complet
        pintaText(0, 0,        "╔" + "═".repeat(cols - 2) + "╗", blanc);
        for (int i = 1; i < rows - 1; i++)
            pintaText(0, i,    "║" + " ".repeat(cols - 2) + "║", gris);
        pintaText(0, rows - 1, "╚" + "═".repeat(cols - 2) + "╝", blanc);

        //títol centrat
        String titol = "T O R R E   D E   R O N D A L L E S   —   C O M B A T";
        pintaText((cols - titol.length()) / 2, 1, titol, daurat);

        //línia separadora sota el títol
        pintaText(0, 2, "╠" + "═".repeat(cols - 2) + "╣", blanc);

        //separador vertical: 2/3 esquerra per al drac, 1/3 dreta per al HUD
        int colSep = cols * 2 / 3;
        pintaText(colSep, 2, "╦", blanc);
        for (int i = 3; i < rows - 6; i++)
            pintaText(colSep, i, "║", gris);
        pintaText(colSep, rows - 6, "╩", blanc);

        //--- ZONA ESQUERRA: caixa enemic + art ---
        String nomEnemic = enemic.getClass().getSimpleName().toUpperCase();
        int xBoxEn = 4;
        int wBoxEn = colSep - 9; //amplada exterior de la caixa
        String nomEn = nomEnemic.length() > wBoxEn - 6 ? nomEnemic.substring(0, wBoxEn - 6) : nomEnemic;
        pintaText(xBoxEn, 3, "╔═ " + nomEn + " " + "═".repeat(wBoxEn - nomEn.length() - 5) + "╗", vermell);
        pintaText(xBoxEn, 4, "║", vermell);
        pintaText(xBoxEn + 2, 4, barraVida(enemic.getVida(), enemic.getVidaMaxima(), 20), colorVida(enemic.getVida(), enemic.getVidaMaxima()));
        pintaText(xBoxEn + wBoxEn - 1, 4, "║", vermell);
        pintaText(xBoxEn, 5, "╚" + "═".repeat(wBoxEn - 2) + "╝", vermell);

        //art ascii de l'enemic amb el seu color real
        TextColor colorEnemic = enemic.getColor();
        int fila = 7;
        String[] art = switch (enemic.getLletra()) {
            case 'd', 'e' -> DIMONI_ART;
            case 'D'      -> DRAC_ART;
            case 'B'      -> BUBOTA_ART;
            case 'G'      -> GEGANT_ART;
            case 'M'      -> MARIA_ART;
            default       -> new String[]{};
        };
        for (String linia : art) {
            if (fila >= rows - 8) break;
            pintaText(4, fila, linia, colorEnemic);
            fila++;
        }

        //--- ZONA DRETA: caixa jugador + stats + accions ---
        TextColor verd = new TextColor.RGB(80, 200, 120);
        int cHud = colSep + 3;
        int wBoxJug = cols - colSep - 6;
        int fHud = 3;

        //caixa del jugador (verda per diferenciar-la de l'enemic)
        pintaText(cHud, fHud, "╔═ AVENTURER " + "═".repeat(Math.max(0, wBoxJug - 14)) + "╗", verd);
        fHud++;
        pintaText(cHud, fHud, "║", verd);
        pintaText(cHud + 2, fHud, barraVida(jugador.getVida(), jugador.getVidaMaxima(), 16), colorVida(jugador.getVida(), jugador.getVidaMaxima()));
        pintaText(cHud + wBoxJug - 1, fHud, "║", verd);
        fHud++;
        pintaText(cHud, fHud, "╚" + "═".repeat(wBoxJug - 2) + "╝", verd);
        fHud += 2;

        //stats del jugador
        pintaText(cHud, fHud, "ATK  " + jugador.getAtacTotal(), new TextColor.RGB(220, 130, 50));
        fHud++;
        pintaText(cHud, fHud, "DEF  " + jugador.getDefensaTotal(), new TextColor.RGB(100, 160, 220));
        fHud++;

        if (jugador.getTornsVeri() > 0) {
            pintaText(cHud, fHud, "VERI " + jugador.getTornsVeri() + " torns", new TextColor.RGB(100, 220, 80));
            fHud++;
        }
        if (jugador.getTornsFoc() > 0) {
            pintaText(cHud, fHud, "FOC  " + jugador.getTornsFoc() + " torns", new TextColor.RGB(220, 120, 30));
            fHud++;
        }
        if (jugador.getTornsGel() > 0) {
            pintaText(cHud, fHud, "GEL  " + jugador.getTornsGel() + " torns", new TextColor.RGB(80, 180, 220));
            fHud++;
        }
        fHud++;

        pintaText(cHud, fHud, "─".repeat(cols - colSep - 5), gris);
        fHud++;
        fHud++;

        //accions disponibles
        pintaText(cHud, fHud, "ACCIONS", new TextColor.RGB(160, 160, 160));
        fHud++;
        pintaText(cHud, fHud, "[ A ]  Atacar", blanc);
        fHud++;

        for (int i = 0; i < com.iessineu.rondalles.inventari.Inventari.MAX_SLOTS; i++) {
            var slot = jugador.getInventari().getSlot(i);
            if (slot == null) continue;
            String quant = slot.quantitat() > 1 ? " x" + slot.quantitat() : "";
            pintaText(cHud, fHud, "[ " + (i + 1) + " ]  " + slot.item().getSimbol() + " " + slot.item().getNom() + quant, daurat);
            fHud++;
        }

        pintaText(cHud, fHud, "[ F ]  Fugir", gris);

        //--- CAIXA DE LOG (amplada total, sota tot) ---
        int filaLog = rows - 6;
        int ampleLog = cols - 4;
        pintaText(1, filaLog,     "╔" + "═".repeat(ampleLog) + "╗", blanc);
        for (int i = 1; i <= 3; i++)
            pintaText(1, filaLog + i, "║" + " ".repeat(ampleLog) + "║", gris);
        pintaText(1, filaLog + 4, "╚" + "═".repeat(ampleLog) + "╝", blanc);

        for (int i = 0; i < log.size() && i < 3; i++) {
            String msg = "> " + log.get(i);
            if (msg.length() > ampleLog - 2) msg = msg.substring(0, ampleLog - 2);
            pintaText(3, filaLog + 1 + i, msg, colorMissatge(log.get(i)));
        }

        screen.refresh();
    }

    private String barraVida(int vida, int max, int ample) {
        int plens = max > 0 ? (int)((double)vida / max * ample) : 0;
        plens = Math.max(0, Math.min(ample, plens));
        return "HP  [" + "█".repeat(plens) + "░".repeat(ample - plens) + "]  " + vida + " / " + max;
    }

    private TextColor colorVida(int vida, int max) {
        double pct = max > 0 ? (double)vida / max : 0;
        if (pct > 0.50) return new TextColor.RGB(80,  200, 80);
        if (pct > 0.25) return new TextColor.RGB(220, 180, 50);
        return new TextColor.RGB(220, 60, 60);
    }

    private TextColor colorMissatge(String msg) {
        if (msg.startsWith("Has atacat"))                             return new TextColor.RGB(220, 180, 50);
        if (msg.contains("contraataca"))                              return new TextColor.RGB(220, 80,  80);
        if (msg.contains("caigut") || msg.startsWith("T'enfrentes")) return new TextColor.RGB(160, 180, 255);
        return new TextColor.RGB(180, 180, 180);
    }

    public void dibuixaInventari(Mapa mapa, int jx, int jy, List<Entitat> entitats, com.iessineu.rondalles.entitats.Jugador jugador, List<com.iessineu.rondalles.inventari.ItemMapa> itemsMapa, boolean[][] visible, boolean[][] explorat, char[][] mapaRecord) throws IOException {
        dibuixa(mapa, jx, jy, entitats, jugador, itemsMapa, visible, explorat, mapaRecord);

        int cols = screen.getTerminalSize().getColumns();
        int rows = screen.getTerminalSize().getRows();

        TextColor blanc = new TextColor.RGB(220, 220, 220);
        TextColor gris = new TextColor.RGB(110, 110, 110);
        TextColor daurat = new TextColor.RGB(220, 180, 50);
        TextColor fonsPanell = new TextColor.RGB(20, 20, 35);

        int amplePanel = 60;
        int alcadaPanel = 12;
        int colIni = (cols - amplePanel) / 2;
        int filaIni = (rows - alcadaPanel) / 2;

        //fons del panell
        for (int f = filaIni; f < filaIni + alcadaPanel; f++)
            for (int c = colIni; c < colIni + amplePanel; c++)
                screen.setCharacter(c, f, new TextCharacter(' ', blanc, fonsPanell));

        //marc
        pintaTextFons(colIni, filaIni, "╔" + "═".repeat(amplePanel - 2) + "╗", blanc, fonsPanell);
        pintaTextFons(colIni, filaIni + alcadaPanel - 1, "╚" + "═".repeat(amplePanel - 2) + "╝", blanc, fonsPanell);
        for (int f = filaIni + 1; f < filaIni + alcadaPanel - 1; f++) {
            screen.setCharacter(colIni, f, new TextCharacter('║', blanc, fonsPanell));
            screen.setCharacter(colIni + amplePanel - 1, f, new TextCharacter('║', blanc, fonsPanell));
        }

        //títol
        String titol = "[ INVENTARI ]";
        pintaTextFons(colIni + (amplePanel - titol.length()) / 2, filaIni + 1, titol, daurat, fonsPanell);

        //4 slots en una fila
        int filaSlots = filaIni + 4;
        int colSlots = colIni + 3;
        for (int i = 0; i < com.iessineu.rondalles.inventari.Inventari.MAX_SLOTS; i++) {
            int c = colSlots + i * 12;
            var slot = jugador.getInventari().getSlot(i);
            pintaTextFons(c, filaSlots - 1, " [" + (i + 1) + "] ", gris, fonsPanell);
            if (slot != null) {
                String quant = slot.quantitat() > 1 ? "x" + slot.quantitat() : " ";
                pintaTextFons(c + 1, filaSlots,     String.valueOf(slot.item().getSimbol()), slot.item().getColor(), fonsPanell);
                pintaTextFons(c + 3, filaSlots,     quant, blanc, fonsPanell);
                String nom = slot.item().getNom().length() > 9 ? slot.item().getNom().substring(0, 9) : slot.item().getNom();
                pintaTextFons(c, filaSlots + 1, nom, gris, fonsPanell);
            } else {
                pintaTextFons(c + 1, filaSlots, "·", gris, fonsPanell);
            }
        }

        //slots d'armadura
        int colArm = colIni + 3;
        int filaArm = filaIni + 8;
        String[] slots = {"CAP", "PIT", "CAM", "PEU"};
        var armadures = jugador.getInventari().getArmaduresEquipades();
        for (int i = 0; i < 4; i++) {
            com.iessineu.rondalles.inventari.Armadura.Slot slot = com.iessineu.rondalles.inventari.Armadura.Slot.values()[i];
            com.iessineu.rondalles.inventari.Armadura arm = armadures.get(slot);
            String txt = slots[i] + ": " + (arm != null ? arm.getSimbol() + " " + arm.getNom() : "---");
            pintaTextFons(colArm + i * 14, filaArm, txt, arm != null ? arm.getColor() : gris, fonsPanell);
        }

        //instruccions
        pintaTextFons(colIni + 2, filaIni + alcadaPanel - 2, "ESC / E per tancar", gris, fonsPanell);

        screen.refresh();
    }

    //espera que l'usuari premi una tecla (bloquejant)
    //és bloquejant perquè el joc és per torns, no fa falta bucle actiu
    public KeyStroke llegeixInput() throws IOException { // llegeixInput es perque llegeix la tecla que l'usuari ha premut
        return screen.readInput();
    }

    //dibuixa el menú inicial amb opcions: Iniciar partida / Sortir
    public void dibuixaMenuInicial() throws IOException {
        screen.clear();
        int cols = screen.getTerminalSize().getColumns();
        int files = screen.getTerminalSize().getRows();
        int cx = cols / 2;
        int cy = files / 2;

        //títol ASCII gran
        String[] titol = {
            "██████╗  ██████╗ ███╗   ██╗██████╗  █████╗ ██╗     ██╗     ███████╗███████╗",
            "██╔══██╗██╔═══██╗████╗  ██║██╔══██╗██╔══██╗██║     ██║     ██╔════╝██╔════╝",
            "██████╔╝██║   ██║██╔██╗ ██║██║  ██║███████║██║     ██║     █████╗  ███████╗",
            "██╔══██╗██║   ██║██║╚██╗██║██║  ██║██╔══██║██║     ██║     ██╔══╝  ╚════██║",
            "██║  ██║╚██████╔╝██║ ╚████║██████╔╝██║  ██║███████╗███████╗███████╗███████║",
            "╚═╝  ╚═╝ ╚═════╝ ╚═╝  ╚═══╝╚═════╝ ╚═╝  ╚═╝╚══════╝╚══════╝╚══════╝╚══════╝"
        };

        int titolY = cy - 8;
        for (int i = 0; i < titol.length; i++) {
            String linia = titol[i];
            int x = cx - linia.length() / 2;
            for (int j = 0; j < linia.length(); j++) {
                screen.setCharacter(x + j, titolY + i,
                    new TextCharacter(linia.charAt(j), TextColor.ANSI.YELLOW_BRIGHT, TextColor.ANSI.BLACK));
            }
        }

        //subtítol
        String subtitol = "~ Un joc de rondalles mallorquines ~";
        int sx = cx - subtitol.length() / 2;
        for (int j = 0; j < subtitol.length(); j++) {
            screen.setCharacter(sx + j, titolY + titol.length + 1,
                new TextCharacter(subtitol.charAt(j), TextColor.ANSI.WHITE, TextColor.ANSI.BLACK));
        }

        //opcions del menú
        String opcio1 = "[ ENTER ]  Iniciar partida";
        String opcio2 = "[  S   ]   Sortir";
        int oy = cy + 2;
        int ox1 = cx - opcio1.length() / 2;
        int ox2 = cx - opcio2.length() / 2;

        for (int j = 0; j < opcio1.length(); j++)
            screen.setCharacter(ox1 + j, oy,
                new TextCharacter(opcio1.charAt(j), TextColor.ANSI.GREEN_BRIGHT, TextColor.ANSI.BLACK));
        for (int j = 0; j < opcio2.length(); j++)
            screen.setCharacter(ox2 + j, oy + 2,
                new TextCharacter(opcio2.charAt(j), TextColor.ANSI.RED, TextColor.ANSI.BLACK));

        screen.refresh();
    }

    //dibuixa el menú de pausa amb les opcions: Reanudar / Guardar / Sortir
    public void dibuixaPausa(int opcioSeleccionada, String[] opcions) throws IOException {
        screen.clear();
        int cols = screen.getTerminalSize().getColumns();
        int files = screen.getTerminalSize().getRows();
        int cx = cols / 2;
        int cy = files / 2;

        //caixa de fons del menú de pausa
        int ampladaCaixa = 30;
        int altCaixa = opcions.length + 4;
        int boxX = cx - ampladaCaixa / 2;
        int boxY = cy - altCaixa / 2;

        //títol pausa
        String titolPausa = "  *** PAUSA ***  ";
        int tx = cx - titolPausa.length() / 2;
        for (int j = 0; j < titolPausa.length(); j++)
            screen.setCharacter(tx + j, boxY,
                new TextCharacter(titolPausa.charAt(j), TextColor.ANSI.CYAN_BRIGHT, TextColor.ANSI.BLACK));

        //instruccions
        String instruccions = "Fletxes + ENTER per seleccionar";
        int ix = cx - instruccions.length() / 2;
        for (int j = 0; j < instruccions.length(); j++)
            screen.setCharacter(ix + j, boxY + 1,
                new TextCharacter(instruccions.charAt(j), TextColor.ANSI.WHITE, TextColor.ANSI.BLACK));

        //opcions del menú
        for (int i = 0; i < opcions.length; i++) {
            boolean seleccionada = (i == opcioSeleccionada);
            String prefix = seleccionada ? " > " : "   ";
            String text = prefix + opcions[i];
            int ox = cx - text.length() / 2;
            TextColor color = seleccionada ? TextColor.ANSI.YELLOW_BRIGHT : TextColor.ANSI.WHITE;
            for (int j = 0; j < text.length(); j++)
                screen.setCharacter(ox + j, boxY + 3 + i,
                    new TextCharacter(text.charAt(j), color, TextColor.ANSI.BLACK));
        }

        //nota de tecla ràpida ESC
        String escNota = "[ ESC ] Reanudar";
        int en = cx - escNota.length() / 2;
        for (int j = 0; j < escNota.length(); j++)
            screen.setCharacter(en + j, boxY + 3 + opcions.length + 1,
                new TextCharacter(escNota.charAt(j), TextColor.ANSI.CYAN, TextColor.ANSI.BLACK));

        screen.refresh();
    }

    public void tanca() throws IOException { // tanca es perque tanca la pantalla
        screen.close();
    }
}