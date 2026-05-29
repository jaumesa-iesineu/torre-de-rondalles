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

    public Renderitzador() throws IOException {
        //usam swing perquè funcioni des de netbeans i des de qualsevol màquina
        //obre una finestra pròpia del joc, com fan els jocs reals
        SwingTerminalFontConfiguration font = SwingTerminalFontConfiguration
                .newInstance(new Font("Monospaced", Font.PLAIN, 18));
        SwingTerminalFrame terminal = new DefaultTerminalFactory()
                .setInitialTerminalSize(new TerminalSize(180, 40))
                .setTerminalEmulatorFontConfiguration(font)
                .createSwingTerminal();
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
                double dist = Math.sqrt((x - jx) * (x - jx) + (y - jy) * (y - jy));
                if (dist > RADI_LLANTERNA) continue;

                double factor = 1.0 - (dist / RADI_LLANTERNA) * 0.75;//Modificar degradat de la visió.
                TextColor colorBase = colorPerCasella(celles[y][x]);
                TextColor colorFinal = fosqueix(colorBase, factor);

                TextColor fonsFinal = fosqueix(fonsCasella(celles[y][x]), factor);
                screen.setCharacter(offsetX + x, offsetY + y, new TextCharacter(celles[y][x], colorFinal, fonsFinal));
            }
        }

        //pintem les entitats actives per damunt del mapa
        for (Entitat e : entitats) {
            if (!e.isActiu()) continue;
            double dist = Math.sqrt((e.getX() - jx) * (e.getX() - jx) + (e.getY() - jy) * (e.getY() - jy));
            if (dist > RADI_LLANTERNA) continue;

            double factor = 1.0 - (dist / RADI_LLANTERNA) * 0.5;
            TextColor color = fosqueix(e.getColor(), factor);
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

    // CANVI 3: mètodes nous per pintar el HUD
    private void dibuixaHUD(com.iessineu.rondalles.entitats.Jugador jugador) { 
        int col = 1;
        int fila = 1;
        
        //Bloc de vida:
        String vida = "VIDA:  " + jugador.getVida() + " / " + jugador.getVidaMaxima();
        pintaText(col, fila, vida, new TextColor.RGB(220, 50, 50)); 
        
        //Bloc de pes:
        String pes = "PES:   " + jugador.getPes() + " / " + jugador.getpesMaxim();
        pintaText(col, fila + 1, pes, new TextColor.RGB(180, 160, 80));
        
         //Bloc d'atac:
        String armes = "ATAC:  " + jugador.getAtacTotal();
        pintaText(col, fila + 2, armes, new TextColor.RGB(200, 120, 50));
        
         //Bloc de defensa:
        String armadura = "DEF:   " + jugador.getDefensaTotal();
        pintaText(col, fila + 3, armadura, new TextColor.RGB(100, 160, 220));
        
        //cada item de l'inventari en una línia pròpia amb el seu número
        if (jugador.getInventari().mida() == 0) {
            pintaText(col, fila + 4, "INV:   (buit)", new TextColor.RGB(140, 200, 140));
        } else {
            for (int i = 0; i < jugador.getInventari().mida(); i++) {
                String linia = "[" + (i + 1) + "] " + jugador.getInventari().get(i).getNom();
                pintaText(col, fila + 4 + i, linia, new TextColor.RGB(140, 200, 140));
            }
        }

        if (jugador.getTornsVeri() > 0) { //si hi ha verí actiu, avisam
            int filaVeri = fila + 4 + Math.max(1, jugador.getInventari().mida());
            pintaText(col, filaVeri, "VERI:  " + jugador.getTornsVeri() + " torns", new TextColor.RGB(100, 220, 80));
        }
    }

    private void pintaText(int col, int fila, String text, TextColor color) { // pintaText es perque pinta el text a la pantalla
        for (int i = 0; i < text.length(); i++) {
            screen.setCharacter(col + i, fila, new TextCharacter(text.charAt(i), color, TextColor.ANSI.BLACK));
        }
    }

    private void pintaTextFons(int col, int fila, String text, TextColor color, TextColor fons) {
        for (int i = 0; i < text.length(); i++) {
            screen.setCharacter(col + i, fila, new TextCharacter(text.charAt(i), color, fons));
        }
    }

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

        TextColor blanc   = new TextColor.RGB(220, 220, 220);
        TextColor gris    = new TextColor.RGB(110, 110, 110);
        TextColor daurat  = new TextColor.RGB(220, 180, 50);
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

        //--- ZONA ESQUERRA: enemic + art ---
        String nomEnemic = enemic.getClass().getSimpleName().toUpperCase();
        pintaText(4, 4, nomEnemic, vermell);

        int filaDrac = 5;
        for (String linia : DRAC_ART) {
            if (filaDrac >= rows - 8) break;
            pintaText(4, filaDrac, linia, new TextColor.RGB(200, 70, 70));
            filaDrac++;
        }

        int filaHpEnemic = filaDrac + 1;
        if (filaHpEnemic < rows - 7) {
            pintaText(4, filaHpEnemic, barraVida(enemic.getVida(), enemic.getVidaMaxima(), 22),
                colorVida(enemic.getVida(), enemic.getVidaMaxima()));
        }

        //--- ZONA DRETA: HUD del jugador ---
        int cHud = colSep + 3;
        int fHud = 4;

        //títol del panell
        pintaText(cHud, fHud, "[ AVENTURER ]", new TextColor.RGB(80, 200, 120));
        fHud += 2;

        //barra de vida
        pintaText(cHud, fHud, barraVida(jugador.getVida(), jugador.getVidaMaxima(), 18),
            colorVida(jugador.getVida(), jugador.getVidaMaxima()));
        fHud += 2;

        //stats
        pintaText(cHud, fHud,     "ATAC  " + jugador.getAtacTotal(), new TextColor.RGB(220, 130, 50));
        pintaText(cHud, fHud + 1, "DEF   " + jugador.getDefensaTotal(), new TextColor.RGB(100, 160, 220));
        if (jugador.getTornsVeri() > 0)
            pintaText(cHud, fHud + 2, "VERI  " + jugador.getTornsVeri() + " torns", new TextColor.RGB(100, 220, 80));
        fHud += 4;

        //separador
        pintaText(cHud, fHud, "─".repeat(cols - colSep - 5), gris);
        fHud += 2;

        //accions
        pintaText(cHud, fHud, "ACCIONS", new TextColor.RGB(160, 160, 160));
        fHud++;
        pintaText(cHud, fHud, "[ A ]  Atacar", blanc);
        fHud++;
        for (int i = 0; i < jugador.getInventari().mida(); i++) {
            pintaText(cHud, fHud, "[ " + (i + 1) + " ]  " + jugador.getInventari().get(i).getNom(), daurat);
            fHud++;
        }
        pintaText(cHud, fHud, "[ F ]  Fugir", gris);

        //--- CAIXA DE LOG (amplada total, sota tot) ---
        int filaLog = rows - 6;
        int ampleLog = cols - 4;
        TextColor fonsLog = new TextColor.RGB(20, 30, 60);
        pintaText(1, filaLog,     "╔" + "═".repeat(ampleLog) + "╗", blanc);
        for (int i = 1; i <= 3; i++)
            pintaTextFons(1, filaLog + i, "║" + " ".repeat(ampleLog) + "║", gris, fonsLog);
        pintaText(1, filaLog + 4, "╚" + "═".repeat(ampleLog) + "╝", blanc);

        for (int i = 0; i < log.size() && i < 3; i++) {
            String msg = "> " + log.get(i);
            if (msg.length() > ampleLog - 2) msg = msg.substring(0, ampleLog - 2);
            pintaTextFons(3, filaLog + 1 + i, msg, colorMissatge(log.get(i)), fonsLog);
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