/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iessineu.rondalles.motor;

import com.iessineu.rondalles.entitats.Enemic;
import com.iessineu.rondalles.entitats.Entitat;
import com.iessineu.rondalles.joc.Simbols;
import com.iessineu.rondalles.mapa.Mapa;
import com.iessineu.rondalles.mapa.TipusTerra;
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
    private int radiLlanterna = 10;

    //amplada del panell dret d'estadístiques (columnes)
    private int ampleHud = 30;

    private String[] artJugador;

    public void setRadiLlanterna(int r) { this.radiLlanterna = r; }
    public void setAmpleHud(int a) { this.ampleHud = a; }

    private String windowTitle = "RONDALLES";
    private String headerTitle = " TORRE DE RONDALLES  ~  ";
    private String subtitle = "~ Un joc de rondalles mallorquines ~";
    private String pauseTitle = "  *** PAUSA ***  ";
    private String pauseInstructions = "Fletxes + ENTER per seleccionar";
    private String pauseResumeHint = "[ ESC ] Reanudar";
    private String nomPersonatge = "PERSONATGE";

    public void setWindowTitle(String t) { this.windowTitle = t; }
    public void setHeaderTitle(String t) { this.headerTitle = t; }
    public void setSubtitle(String t) { this.subtitle = t; }
    public void setPauseTitle(String t) { this.pauseTitle = t; }
    public void setPauseInstructions(String t) { this.pauseInstructions = t; }
    public void setPauseResumeHint(String t) { this.pauseResumeHint = t; }
    public void setNomPersonatge(String t) { this.nomPersonatge = t != null ? t.toUpperCase() : "PERSONATGE"; }

    public void setArtJugador(String[] art) {
        this.artJugador = art;
    }

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
        terminal.setTitle(windowTitle);
        try (InputStream is = getClass().getResourceAsStream("/logo.png")) {
            if (is != null) {
                BufferedImage icon = ImageIO.read(is);
                terminal.setIconImage(icon);
            }
        } catch (IOException ignored) {
        }
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

        int cols = screen.getTerminalSize().getColumns();
        int files = screen.getTerminalSize().getRows();
        int colSep = cols - ampleHud; //columna on comença el panell dret

        TextColor blanc = new TextColor.RGB(180, 180, 195);
        TextColor grisMarc = new TextColor.RGB(70, 70, 85);
        TextColor daurat = new TextColor.RGB(220, 180, 50);

        //marc: fila superior
        pintaText(0, 0, "╔" + "═".repeat(colSep - 1) + "╦" + "═".repeat(ampleHud - 2) + "╗", blanc);
        //fila del títol
        pintaText(0, 1, "║", grisMarc);
        String titol = headerTitle + mapa.getNom();
        if (titol.length() > colSep - 2) {
            titol = titol.substring(0, colSep - 2);
        }
        pintaText(1, 1, titol, daurat);
        pintaText(colSep, 1, "║", grisMarc);
        String titolHud = nomPersonatge;
        pintaText(colSep + (ampleHud - 1 - titolHud.length()) / 2, 1, titolHud, new TextColor.RGB(80, 200, 120));
        pintaText(cols - 1, 1, "║", grisMarc);
        //separador davall el títol
        pintaText(0, 2, "╠" + "═".repeat(colSep - 1) + "╣" + " ".repeat(ampleHud - 2) + "║", blanc);
        //bordes laterals
        for (int i = 3; i < files - 1; i++) {
            pintaText(0, i, "║", grisMarc);
            pintaText(colSep, i, "║", grisMarc);
            pintaText(cols - 1, i, "║", grisMarc);
        }
        //fila inferior
        pintaText(0, files - 1, "╚" + "═".repeat(colSep - 1) + "╩" + "═".repeat(ampleHud - 2) + "╝", blanc);

        //zona visible del mapa: files 3..files-2, cols 1..colSep-1
        int vpW = colSep - 2;
        int vpH = files - 4;

        //camara: si el mapa cap, el centram; si no, seguim el jugador
        //es com una llanterna que segueix es jugador
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
                if (sc < 1 || sc >= colSep || sf < 3 || sf >= files - 1) {
                    continue;
                }
                boolean esVisible = visible == null || visible[my][mx];
                boolean esExplorada = explorat != null && explorat[my][mx];
                if (esVisible) {
                    double dist = Math.sqrt((mx - jx) * (mx - jx) + (my - jy) * (my - jy));
                    double factor = 1.0 - (dist / radiLlanterna) * 0.75;
                    screen.setCharacter(sc, sf, new TextCharacter(celles[my][mx], fosqueix(colorPerCasella(celles[my][mx]), factor), fosqueix(fonsCasella(celles[my][mx]), factor)));
                } else if (esExplorada) {
                    screen.setCharacter(sc, sf, new TextCharacter(mapaRecord[my][mx], colorMemoria, TextColor.ANSI.BLACK));
                }
            }
        }

        //entitats per damunt (només si visibles)
        for (Entitat e : entitats) {
            if (!e.isActiu()) {
                continue;
            }
            if (visible != null && (e.getY() >= visible.length || e.getX() >= visible[e.getY()].length)) {
                continue;
            }
            if (visible != null && !visible[e.getY()][e.getX()]) {
                continue;
            }
            int sc = 1 + (e.getX() - camX);
            int sf = 3 + (e.getY() - camY);
            if (sc < 1 || sc >= colSep || sf < 3 || sf >= files - 1) {
                continue;
            }
            double dist = Math.sqrt((e.getX() - jx) * (e.getX() - jx) + (e.getY() - jy) * (e.getY() - jy));
            double factor = 1.0 - (dist / radiLlanterna) * 0.5;
            screen.setCharacter(sc, sf, new TextCharacter(e.getSimbol(), fosqueix(e.getColor(), factor), TextColor.ANSI.BLACK));
        }

        //ítems del terra (només si visibles)
        for (com.iessineu.rondalles.inventari.ItemMapa im : itemsMapa) {
            if (visible != null && (im.getY() >= visible.length || im.getX() >= visible[im.getY()].length)) {
                continue;
            }
            if (visible != null && !visible[im.getY()][im.getX()]) {
                continue;
            }
            int sc = 1 + (im.getX() - camX);
            int sf = 3 + (im.getY() - camY);
            if (sc < 1 || sc >= colSep || sf < 3 || sf >= files - 1) {
                continue;
            }
            double dist = Math.sqrt((im.getX() - jx) * (im.getX() - jx) + (im.getY() - jy) * (im.getY() - jy));
            double factor = 1.0 - (dist / radiLlanterna) * 0.75;
            screen.setCharacter(sc, sf, new TextCharacter(im.getItem().getSimbol(), fosqueix(im.getItem().getColor(), factor), TextColor.ANSI.BLACK));
        }

        //jugador sempre verd per damunt de tot
        int psc = 1 + (jx - camX);
        int psf = 3 + (jy - camY);
        if (psc >= 1 && psc < colSep && psf >= 3 && psf < files - 1) {
            screen.setCharacter(psc, psf, new TextCharacter('@', TextColor.ANSI.GREEN_BRIGHT, TextColor.ANSI.BLACK));
        }

        dibuixaHUD(jugador, colSep + 1, 3, files - 1);
        screen.refresh();
    }

    //cada tipus de casella té un color base diferent
    private TextColor colorPerCasella(char c) {
        //si es un tipus de terra definit al JSON, agafam el seu color
        TipusTerra t = TipusTerra.de(c);
        if (t != null) {
            return new TextColor.RGB(t.getColorR(), t.getColorG(), t.getColorB());
        }
        //simbols especials que no son terrenys
        if (Simbols.esMur(c)) return new TextColor.RGB(130, 130, 140);
        if (Simbols.esPortaTancada(c)) return new TextColor.RGB(180, 100, 40);
        if (Simbols.esPortaOberta(c)) return new TextColor.RGB(120, 80, 40);
        if (Simbols.esPortaBloquejada(c)) return new TextColor.RGB(200, 40, 40);
        if (Simbols.esEscalaBaix(c)) return new TextColor.RGB(200, 200, 50);
        if (Simbols.esMarcadorItem(c)) return new TextColor.RGB(220, 180, 50);
        if (Simbols.esMarcadorNpc(c)) return new TextColor.RGB(80, 200, 220);
        return new TextColor.RGB(90, 90, 90);
    }

    private TextColor fonsCasella(char c) {
        //si es un tipus de terra, agafam el fons des del JSON
        TipusTerra t = TipusTerra.de(c);
        if (t != null) {
            return new TextColor.RGB(t.getFonsR(), t.getFonsG(), t.getFonsB());
        }
        if (Simbols.esMur(c)) return new TextColor.RGB(40, 40, 50);
        return TextColor.ANSI.BLACK;
    }

    //aplica un factor d'oscuritat als components rgb del color
    private TextColor fosqueix(TextColor color, double factor) {
        if (!(color instanceof TextColor.RGB rgb)) {
            return color;
        }
        int r = Math.max(0, Math.min(255, (int) (rgb.getRed() * factor)));
        int g = Math.max(0, Math.min(255, (int) (rgb.getGreen() * factor)));
        int b = Math.max(0, Math.min(255, (int) (rgb.getBlue() * factor)));
        return new TextColor.RGB(r, g, b);
    }

    private void dibuixaHUD(com.iessineu.rondalles.entitats.Jugador jugador, int col, int fila, int filaMax) { //dibuixaHUD pinta les estadistiques al panell dret
        int innerW = ampleHud - 2;

        TextColor vermell = new TextColor.RGB(220, 60, 60);
        TextColor groc = new TextColor.RGB(180, 160, 80);
        TextColor taronja = new TextColor.RGB(200, 120, 50);
        TextColor blau = new TextColor.RGB(100, 160, 220);
        TextColor gris = new TextColor.RGB(100, 100, 115);

        //barra de vida + número a la mateixa línia
        String vidaStr = jugador.getVida() + "/" + jugador.getVidaMaxima();
        int barW = innerW - 6 - vidaStr.length(); // "HP [" + bar + "] " + vidaStr = innerW
        barW = Math.max(4, barW);
        int plens = jugador.getVidaMaxima() > 0
                ? Math.max(0, Math.min(barW, (int) ((double) jugador.getVida() / jugador.getVidaMaxima() * barW)))
                : 0;
        pintaText(col, fila++, "HP [" + "█".repeat(plens) + "░".repeat(barW - plens) + "] " + vidaStr, colorVida(jugador.getVida(), jugador.getVidaMaxima()));
        fila++;

        //estadistiques bàsiques
        pintaText(col, fila++, "ATK  " + jugador.getAtacTotal(), taronja);
        pintaText(col, fila++, "DEF  " + jugador.getDefensaTotal(), blau);
        pintaText(col, fila++, "VEL  " + jugador.velocitatEfectiva(), new TextColor.RGB(100, 200, 255));
        pintaText(col, fila++, "EVA  " + jugador.evasioEfectiva() + "%", new TextColor.RGB(180, 255, 180));
        pintaText(col, fila++, "PES  " + jugador.getPes() + " / " + jugador.getpesMaxim(), groc);
        TextColor colorCarrega = switch (jugador.categoriaCarrega()) {
            case LLEUGER ->
                new TextColor.RGB(80, 220, 80);
            case NORMAL ->
                new TextColor.RGB(230, 200, 40);
            case PESAT ->
                new TextColor.RGB(220, 60, 60);
        };
        pintaText(col, fila++, "SAC  " + jugador.categoriaCarrega().name(), colorCarrega);
        fila++;

        //inventari
        pintaText(col, fila++, "--- INVENTARI ---", gris);
        for (int i = 0; i < jugador.getInventari().getMaxSlots(); i++) {
            if (fila >= filaMax - 2) {
                break;
            }
            var slot = jugador.getInventari().getSlot(i);
            if (slot != null) {
                String q = slot.quantitat() > 1 ? " x" + slot.quantitat() : "";
                String linia = "[" + (i + 1) + "] " + slot.item().getSimbol() + " " + slot.item().getNom() + q;
                if (linia.length() > innerW) {
                    linia = linia.substring(0, innerW);
                }
                pintaText(col, fila, linia, slot.item().getColor());
            } else {
                pintaText(col, fila, "[" + (i + 1) + "] -", gris);
            }
            fila++;
        }

        //equip
        if (fila < filaMax - 1) {
            fila++;
            pintaText(col, fila++, "--- EQUIP ---", gris);
            var armaEq = jugador.getInventari().getArmaEquipada();
            if (fila < filaMax - 1) {
                String txtA = "Arma : " + (armaEq != null ? armaEq.getNom() : "---");
                if (txtA.length() > innerW) txtA = txtA.substring(0, innerW);
                pintaText(col, fila++, txtA, armaEq != null ? armaEq.getColor() : gris);
            }
            String[] slotNoms = {"Casc ", "Cos  ", "Cames", "Peus "};
            var armadures = jugador.getInventari().getArmaduresEquipades();
            for (int i = 0; i < 4 && fila < filaMax - 1; i++) {
                com.iessineu.rondalles.inventari.Armadura.Slot s = com.iessineu.rondalles.inventari.Armadura.Slot.values()[i];
                com.iessineu.rondalles.inventari.Armadura arm = armadures.get(s);
                String txt = slotNoms[i] + ": " + (arm != null ? arm.getNom() : "---");
                if (txt.length() > innerW) txt = txt.substring(0, innerW);
                pintaText(col, fila++, txt, arm != null ? arm.getColor() : gris);
            }
        }

        //estats temporals (verí, foc, gel)
        boolean anyEstat = jugador.getTornsVeri() > 0 || jugador.getTornsFoc() > 0 || jugador.getTornsGel() > 0;
        if (anyEstat && fila < filaMax - 1) {
            fila++;
            pintaText(col, fila++, "--- ESTATS ---", gris);
            if (jugador.getTornsVeri() > 0 && fila < filaMax - 1) {
                pintaText(col, fila++, "VERI  " + jugador.getTornsVeri() + " torns", new TextColor.RGB(100, 220, 80));
            }
            if (jugador.getTornsFoc() > 0 && fila < filaMax - 1) {
                pintaText(col, fila++, "FOC   " + jugador.getTornsFoc() + " torns", new TextColor.RGB(220, 120, 30));
            }
            if (jugador.getTornsGel() > 0 && fila < filaMax - 1) {
                pintaText(col, fila++, "GEL   " + jugador.getTornsGel() + " torns", new TextColor.RGB(80, 180, 220));
            }
        }

        //ascii art del jugador al racó inferior
        if (artJugador != null && artJugador.length > 0) {
            int artLines = artJugador.length;
            int artStartFila = filaMax - artLines - 1;
            if (artStartFila > fila) {
                TextColor colorArt = new TextColor.RGB(120, 200, 120);
                for (int i = 0; i < artLines && artStartFila + i < filaMax - 1; i++) {
                    String linia = artJugador[i];
                    if (linia.length() > innerW) {
                        linia = linia.substring(0, innerW);
                    }
                    pintaText(col, artStartFila + i, linia, colorArt);
                }
            }
        }
    }

    private void pintaText(int col, int fila, String text, TextColor color) { //pintaText pinta text a la pantalla sense sortir dels límits
        int maxC = screen.getTerminalSize().getColumns();
        int maxF = screen.getTerminalSize().getRows();
        if (fila < 0 || fila >= maxF) {
            return;
        }
        for (int i = 0; i < text.length(); i++) {
            int c = col + i;
            if (c >= 0 && c < maxC) {
                screen.setCharacter(c, fila, new TextCharacter(text.charAt(i), color, TextColor.ANSI.BLACK));
            }
        }
    }

    private void pintaTextFons(int col, int fila, String text, TextColor color, TextColor fons) {
        int maxC = screen.getTerminalSize().getColumns();
        int maxF = screen.getTerminalSize().getRows();
        if (fila < 0 || fila >= maxF) {
            return;
        }
        for (int i = 0; i < text.length(); i++) {
            int c = col + i;
            if (c >= 0 && c < maxC) {
                screen.setCharacter(c, fila, new TextCharacter(text.charAt(i), color, fons));
            }
        }
    }



    public void dibuixaSeleccioPersonatge(List<com.iessineu.rondalles.joc.ConfigGame.TipusPersonatgeConfig> llista, int opcio) throws IOException {
        screen.clear();
        int cols = screen.getTerminalSize().getColumns();
        int rows = screen.getTerminalSize().getRows();
        TextColor blanc = new TextColor.RGB(220, 220, 220);
        TextColor daurat = new TextColor.RGB(220, 180, 50);
        TextColor gris = new TextColor.RGB(110, 110, 110);
        TextColor verd = new TextColor.RGB(80, 200, 120);
        TextColor groc = new TextColor.RGB(180, 160, 80);

        pintaText(0, 0, "╔" + "═".repeat(cols - 2) + "╗", blanc);
        for (int i = 1; i < rows - 1; i++) pintaText(0, i, "║" + " ".repeat(cols - 2) + "║", gris);
        pintaText(0, rows - 1, "╚" + "═".repeat(cols - 2) + "╝", blanc);

        String titol = "T R I A   E L   T E U   P E R S O N A T G E";
        pintaText((cols - titol.length()) / 2, 1, titol, daurat);
        pintaText(0, 2, "╠" + "═".repeat(cols - 2) + "╣", blanc);

        int panellW = cols - 6;
        int fila = 4;
        for (int i = 0; i < llista.size(); i++) {
            var p = llista.get(i);
            boolean sel = (i == opcio);
            TextColor color = sel ? verd : blanc;
            String prefix = sel ? "► " : "  ";
            pintaText(3, fila, prefix + p.nom, color);
            pintaText(5, fila + 1, p.descripcio != null ? p.descripcio : "", gris);
            if (p.descripcioPassiu != null && !p.descripcioPassiu.isBlank()) {
                pintaText(5, fila + 2, "✦ " + p.descripcioPassiu, groc);
            }
            pintaText(5, fila + 3, "HP:" + p.vidaMaxima + "  ATK:" + p.atac + "  VEL:" + p.velocitat + "  EVA:" + p.evasio + "%", gris);
            fila += 5;
        }
        // opció custom
        boolean selCustom = (opcio == llista.size());
        TextColor colorC = selCustom ? verd : blanc;
        pintaText(3, fila, (selCustom ? "► " : "  ") + "Personatge propi", colorC);
        pintaText(5, fila + 1, "Distribueix els teus propis punts de stat", gris);
        pintaText(5, fila + 2, "Sense passiu especial", gris);

        pintaText(3, rows - 2, "[ ↑↓ ] Navegar    [ ENTER ] Seleccionar    [ ESC ] Tornar", gris);
        screen.refresh();
    }

    public void dibuixaCreacioPersonatge(int[] pts, int statSel, com.iessineu.rondalles.joc.ConfigGame.PersonatgeCustomConfig cc) throws IOException {
        screen.clear();
        int cols = screen.getTerminalSize().getColumns();
        int rows = screen.getTerminalSize().getRows();
        TextColor blanc = new TextColor.RGB(220, 220, 220);
        TextColor daurat = new TextColor.RGB(220, 180, 50);
        TextColor gris = new TextColor.RGB(110, 110, 110);
        TextColor verd = new TextColor.RGB(80, 200, 120);
        TextColor groc = new TextColor.RGB(180, 160, 80);
        TextColor vermell = new TextColor.RGB(220, 70, 70);

        pintaText(0, 0, "╔" + "═".repeat(cols - 2) + "╗", blanc);
        for (int i = 1; i < rows - 1; i++) pintaText(0, i, "║" + " ".repeat(cols - 2) + "║", gris);
        pintaText(0, rows - 1, "╚" + "═".repeat(cols - 2) + "╝", blanc);

        String titol = "C R E A   E L   T E U   P E R S O N A T G E";
        pintaText((cols - titol.length()) / 2, 1, titol, daurat);
        pintaText(0, 2, "╠" + "═".repeat(cols - 2) + "╣", blanc);

        if (cc == null) cc = new com.iessineu.rondalles.joc.ConfigGame.PersonatgeCustomConfig();
        int gastats = pts[0] + pts[1] + pts[2] + pts[3];
        int restants = cc.pressupost - gastats;

        String ptsStr = "Punts restants: " + restants + " / " + cc.pressupost;
        pintaText((cols - ptsStr.length()) / 2, 4, ptsStr, restants > 0 ? groc : vermell);

        String[] noms = {"Vida      ", "Atac      ", "Velocitat ", "Evasió    "};
        int[] valors = {
            cc.vidaBase + pts[0] * cc.vidaPerPunt,
            cc.atacBase + pts[1],
            cc.velocitatBase + pts[2],
            cc.evasioBase + pts[3] * cc.evasioPerPunt
        };
        int[] maxVals = {cc.vidaMax, cc.atacMax, cc.velocitatMax, cc.evasioMax};
        String[] unitats = {"", "", "", "%"};

        int fila = 6;
        for (int i = 0; i < 4; i++) {
            boolean sel = (i == statSel);
            TextColor color = sel ? verd : blanc;
            String prefix = sel ? "► " : "  ";
            int barMax = 20;
            int barPlens = maxVals[i] > 0 ? (int)((double) valors[i] / maxVals[i] * barMax) : 0;
            String barra = "[" + "█".repeat(barPlens) + "░".repeat(barMax - barPlens) + "]";
            pintaText(3, fila, prefix + noms[i] + " " + barra + " " + valors[i] + unitats[i], color);
            fila += 2;
        }

        pintaText(3, rows - 2, "[ ↑↓ ] Seleccionar stat    [ ←→ ] Ajustar    [ ENTER ] Confirmar    [ ESC ] Tornar", gris);
        screen.refresh();
    }

    public void dibuixaCombat(Enemic enemic, com.iessineu.rondalles.entitats.Jugador jugador, List<String> log) throws IOException {
        screen.clear();

        int cols = screen.getTerminalSize().getColumns();
        int rows = screen.getTerminalSize().getRows();

        TextColor blanc = new TextColor.RGB(220, 220, 220);
        TextColor gris = new TextColor.RGB(110, 110, 110);
        TextColor daurat = new TextColor.RGB(220, 180, 50);
        TextColor vermell = new TextColor.RGB(220, 70, 70);

        //marc exterior complet
        pintaText(0, 0, "╔" + "═".repeat(cols - 2) + "╗", blanc);
        for (int i = 1; i < rows - 1; i++) {
            pintaText(0, i, "║" + " ".repeat(cols - 2) + "║", gris);
        }
        pintaText(0, rows - 1, "╚" + "═".repeat(cols - 2) + "╝", blanc);

        //títol centrat
        String titol = "T O R R E   D E   R O N D A L L E S   —   C O M B A T";
        pintaText((cols - titol.length()) / 2, 1, titol, daurat);

        //línia separadora sota el títol
        pintaText(0, 2, "╠" + "═".repeat(cols - 2) + "╣", blanc);

        //separador vertical: mateixa amplada que el HUD del mapa
        int colSep = cols - ampleHud;
        pintaText(colSep, 2, "╦", blanc);
        for (int i = 3; i < rows - 6; i++) {
            pintaText(colSep, i, "║", gris);
        }
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

        //art ascii de l'enemic (carregat des del game.json)
        TextColor colorEnemic = enemic.getColor();
        int fila = 7;
        String[] art = enemic.getArtAscii();
        for (String linia : art) {
            if (fila >= rows - 8) {
                break;
            }
            pintaText(4, fila, linia, colorEnemic);
            fila++;
        }

        //--- ZONA DRETA: stats + equip + accions (mateix estil que HUD del mapa) ---
        int cHud = colSep + 1;
        int innerW = ampleHud - 2;
        int fHud = 3;
        TextColor groc = new TextColor.RGB(180, 160, 80);
        TextColor taronja2 = new TextColor.RGB(200, 120, 50);
        TextColor blau2 = new TextColor.RGB(100, 160, 220);
        TextColor gris2 = new TextColor.RGB(100, 100, 115);

        //barra de vida
        String vidaStr = jugador.getVida() + "/" + jugador.getVidaMaxima();
        int barW = innerW - 6 - vidaStr.length();
        barW = Math.max(4, barW);
        int plens = jugador.getVidaMaxima() > 0
                ? Math.max(0, Math.min(barW, (int) ((double) jugador.getVida() / jugador.getVidaMaxima() * barW)))
                : 0;
        pintaText(cHud, fHud++, "HP [" + "█".repeat(plens) + "░".repeat(barW - plens) + "] " + vidaStr, colorVida(jugador.getVida(), jugador.getVidaMaxima()));
        fHud++;

        //estadistiques
        pintaText(cHud, fHud++, "ATK  " + jugador.getAtacTotal(), taronja2);
        pintaText(cHud, fHud++, "DEF  " + jugador.getDefensaTotal(), blau2);
        pintaText(cHud, fHud++, "VEL  " + jugador.velocitatEfectiva(), new TextColor.RGB(100, 200, 255));
        pintaText(cHud, fHud++, "EVA  " + jugador.evasioEfectiva() + "%", new TextColor.RGB(180, 255, 180));
        pintaText(cHud, fHud++, "PES  " + jugador.getPes() + " / " + jugador.getpesMaxim(), groc);
        TextColor colorSac = switch (jugador.categoriaCarrega()) {
            case LLEUGER -> new TextColor.RGB(80, 220, 80);
            case NORMAL  -> new TextColor.RGB(230, 200, 40);
            case PESAT   -> new TextColor.RGB(220, 60, 60);
        };
        pintaText(cHud, fHud++, "SAC  " + jugador.categoriaCarrega().name(), colorSac);
        fHud++;

        //estats temporals
        if (jugador.getTornsVeri() > 0) { pintaText(cHud, fHud++, "VERI " + jugador.getTornsVeri() + " torns", new TextColor.RGB(100, 220, 80)); }
        if (jugador.getTornsFoc()  > 0) { pintaText(cHud, fHud++, "FOC  " + jugador.getTornsFoc()  + " torns", new TextColor.RGB(220, 120, 30)); }
        if (jugador.getTornsGel()  > 0) { pintaText(cHud, fHud++, "GEL  " + jugador.getTornsGel()  + " torns", new TextColor.RGB(80, 180, 220)); }
        fHud++;

        //equip
        pintaText(cHud, fHud++, "--- EQUIP ---", gris2);
        var armaEqC = jugador.getInventari().getArmaEquipada();
        String txtArma = "Arma : " + (armaEqC != null ? armaEqC.getNom() : "---");
        if (txtArma.length() > innerW) txtArma = txtArma.substring(0, innerW);
        pintaText(cHud, fHud++, txtArma, armaEqC != null ? armaEqC.getColor() : gris2);
        String[] slotNomsC = {"Casc ", "Cos  ", "Cames", "Peus "};
        var armEqC = jugador.getInventari().getArmaduresEquipades();
        for (int i = 0; i < 4; i++) {
            com.iessineu.rondalles.inventari.Armadura.Slot s = com.iessineu.rondalles.inventari.Armadura.Slot.values()[i];
            com.iessineu.rondalles.inventari.Armadura arm = armEqC.get(s);
            String tS = slotNomsC[i] + ": " + (arm != null ? arm.getNom() : "---");
            if (tS.length() > innerW) tS = tS.substring(0, innerW);
            pintaText(cHud, fHud++, tS, arm != null ? arm.getColor() : gris2);
        }
        fHud++;

        //accions
        pintaText(cHud, fHud++, "--- ACCIONS ---", gris2);
        pintaText(cHud, fHud++, "[ A ]  Atacar", blanc);
        for (int i = 0; i < jugador.getInventari().getMaxSlots(); i++) {
            var slot = jugador.getInventari().getSlot(i);
            if (slot == null) continue;
            String quant = slot.quantitat() > 1 ? " x" + slot.quantitat() : "";
            String prefix = "[ " + (i + 1) + " ]  " + slot.item().getSimbol() + " ";
            int maxNom = innerW - prefix.length() - quant.length();
            String nomTallat = slot.item().getNom();
            if (nomTallat.length() > maxNom) nomTallat = nomTallat.substring(0, Math.max(0, maxNom));
            pintaText(cHud, fHud++, prefix + nomTallat + quant, slot.item().getColor());
        }
        pintaText(cHud, fHud, "[ F ]  Fugir", gris2);

        //--- CAIXA DE LOG (amplada total, sota tot) ---
        int filaLog = rows - 6;
        int ampleLog = cols - 4;
        pintaText(1, filaLog, "╔" + "═".repeat(ampleLog) + "╗", blanc);
        for (int i = 1; i <= 3; i++) {
            pintaText(1, filaLog + i, "║" + " ".repeat(ampleLog) + "║", gris);
        }
        pintaText(1, filaLog + 4, "╚" + "═".repeat(ampleLog) + "╝", blanc);

        for (int i = 0; i < log.size() && i < 3; i++) {
            String msg = "> " + log.get(i);
            if (msg.length() > ampleLog - 2) {
                msg = msg.substring(0, ampleLog - 2);
            }
            pintaText(3, filaLog + 1 + i, msg, colorMissatge(log.get(i)));
        }

        screen.refresh();
    }

    private String barraVida(int vida, int max, int ample) {
        int plens = max > 0 ? (int) ((double) vida / max * ample) : 0;
        plens = Math.max(0, Math.min(ample, plens));
        return "[" + "█".repeat(plens) + "░".repeat(ample - plens) + "] " + vida + "/" + max;
    }

    private TextColor colorVida(int vida, int max) {
        double pct = max > 0 ? (double) vida / max : 0;
        if (pct > 0.50) {
            return new TextColor.RGB(80, 200, 80);
        }
        if (pct > 0.25) {
            return new TextColor.RGB(220, 180, 50);
        }
        return new TextColor.RGB(220, 60, 60);
    }

    private TextColor colorMissatge(String msg) {
        if (msg.startsWith("Has atacat")) {
            return new TextColor.RGB(220, 180, 50);
        }
        if (msg.contains("contraataca")) {
            return new TextColor.RGB(220, 80, 80);
        }
        if (msg.contains("caigut") || msg.startsWith("T'enfrentes")) {
            return new TextColor.RGB(160, 180, 255);
        }
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

        int amplePanel = 64;
        int alcadaPanel = 16;
        int colIni = (cols - amplePanel) / 2;
        int filaIni = (rows - alcadaPanel) / 2;

        //fons del panell
        for (int f = filaIni; f < filaIni + alcadaPanel; f++) {
            for (int c = colIni; c < colIni + amplePanel; c++) {
                screen.setCharacter(c, f, new TextCharacter(' ', blanc, fonsPanell));
            }
        }

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
        int filaSlots = filaIni + 3;
        int colSlots = colIni + 3;
        for (int i = 0; i < jugador.getInventari().getMaxSlots(); i++) {
            int c = colSlots + i * 14;
            var slot = jugador.getInventari().getSlot(i);
            pintaTextFons(c, filaSlots, " [" + (i + 1) + "] ", gris, fonsPanell);
            if (slot != null) {
                var item = slot.item();
                //tier a dalt a sa dreta del slot
                pintaTextFons(c + 5, filaSlots, item.getTierSimbol(), item.getTierColor(), fonsPanell);
                //sprite o simbol
                if (item instanceof com.iessineu.rondalles.inventari.Pocio pocioItem) {
                    String[] sprite = spritePocio(pocioItem);
                    for (int l = 0; l < sprite.length; l++) {
                        pintaTextFons(c + 1, filaSlots + 1 + l, sprite[l], item.getColor(), fonsPanell);
                    }
                } else {
                    pintaTextFons(c + 2, filaSlots + 1, String.valueOf(item.getSimbol()), item.getColor(), fonsPanell);
                }
                //quantitat + nom
                String quant = slot.quantitat() > 1 ? "x" + slot.quantitat() : "";
                String nom = item.getNom().length() > 11 ? item.getNom().substring(0, 11) : item.getNom();
                pintaTextFons(c, filaSlots + 3, quant.isEmpty() ? nom : quant + " " + nom, gris, fonsPanell);
            } else {
                pintaTextFons(c + 2, filaSlots + 1, "·", gris, fonsPanell);
            }
        }

        //separador
        pintaTextFons(colIni, filaIni + 7, "╠" + "═".repeat(amplePanel - 2) + "╣", blanc, fonsPanell);
        pintaTextFons(colIni + 3, filaIni + 7, " EQUIPAMENT ", gris, fonsPanell);

        //equip: arma + 4 armadures en llista vertical
        int colArm = colIni + 3;
        int filaArm = filaIni + 8;
        int maxNomEq = amplePanel - 14;
        String[] slotsNoms = {"Casc ", "Cos  ", "Cames", "Peus "};
        var armadures = jugador.getInventari().getArmaduresEquipades();
        var armaEq = jugador.getInventari().getArmaEquipada();

        String nomArma = armaEq != null ? armaEq.getNom() : "---";
        if (nomArma.length() > maxNomEq) nomArma = nomArma.substring(0, maxNomEq);
        pintaTextFons(colArm, filaArm, "Arma : " + nomArma, armaEq != null ? armaEq.getColor() : gris, fonsPanell);
        if (armaEq != null) pintaTextFons(colArm + maxNomEq + 9, filaArm, armaEq.getTierSimbol(), armaEq.getTierColor(), fonsPanell);

        for (int i = 0; i < 4; i++) {
            com.iessineu.rondalles.inventari.Armadura.Slot slot = com.iessineu.rondalles.inventari.Armadura.Slot.values()[i];
            com.iessineu.rondalles.inventari.Armadura arm = armadures.get(slot);
            String nom = arm != null ? arm.getNom() : "---";
            if (nom.length() > maxNomEq) nom = nom.substring(0, maxNomEq);
            pintaTextFons(colArm, filaArm + 1 + i, slotsNoms[i] + ": " + nom, arm != null ? arm.getColor() : gris, fonsPanell);
            if (arm != null) pintaTextFons(colArm + maxNomEq + 8, filaArm + 1 + i, arm.getTierSimbol(), arm.getTierColor(), fonsPanell);
        }

        //instruccions
        pintaTextFons(colIni + 2, filaIni + alcadaPanel - 2, "1-4 equipar  |  ESC/I tancar", gris, fonsPanell);

        screen.refresh();
    }

    private String[] spritePocio(com.iessineu.rondalles.inventari.Pocio p) {
        return switch (p.getTipus()) {
            case VIDA  -> new String[]{" ╭╮ ", "▓██▓", " ╰╯ "};
            case VERI  -> new String[]{" ╭╮ ", "░██░", " ╰╯ "};
            case FOC   -> new String[]{" ╭╮ ", "▒██▒", " ╰╯ "};
            case GEL   -> new String[]{" ╭╮ ", "·██·", " ╰╯ "};
        };
    }

    //espera que l'usuari premi una tecla (bloquejant)
    //és bloquejant perquè el joc és per torns, no fa falta bucle actiu
    public KeyStroke llegeixInput() throws IOException {
        return screen.readInput();
    }

    public KeyStroke pollInput() throws IOException {
        return screen.pollInput();
    }

    //dibuixa el menú inicial amb opcions navegables amb fletxes
    public void dibuixaMenuInicial(int opcioSeleccionada, String[] opcions) throws IOException {
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
        String subtitol = subtitle;
        int sx = cx - subtitol.length() / 2;
        for (int j = 0; j < subtitol.length(); j++) {
            screen.setCharacter(sx + j, titolY + titol.length + 1,
                    new TextCharacter(subtitol.charAt(j), TextColor.ANSI.WHITE, TextColor.ANSI.BLACK));
        }

        //opcions del menú amb fletxa de selecció
        int oy = cy + 2;
        for (int i = 0; i < opcions.length; i++) {
            String prefix = (i == opcioSeleccionada) ? " > " : "   ";
            String text = prefix + opcions[i];
            int ox = cx - text.length() / 2;
            TextColor color = (i == opcioSeleccionada) ? TextColor.ANSI.YELLOW_BRIGHT : TextColor.ANSI.WHITE;
            for (int j = 0; j < text.length(); j++) {
                screen.setCharacter(ox + j, oy + i * 2,
                        new TextCharacter(text.charAt(j), color, TextColor.ANSI.BLACK));
            }
        }

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
        String titolPausa = pauseTitle;
        int tx = cx - titolPausa.length() / 2;
        for (int j = 0; j < titolPausa.length(); j++) {
            screen.setCharacter(tx + j, boxY,
                    new TextCharacter(titolPausa.charAt(j), TextColor.ANSI.CYAN_BRIGHT, TextColor.ANSI.BLACK));
        }

        //instruccions
        String instruccions = pauseInstructions;
        int ix = cx - instruccions.length() / 2;
        for (int j = 0; j < instruccions.length(); j++) {
            screen.setCharacter(ix + j, boxY + 1,
                    new TextCharacter(instruccions.charAt(j), TextColor.ANSI.WHITE, TextColor.ANSI.BLACK));
        }

        //opcions del menú
        for (int i = 0; i < opcions.length; i++) {
            boolean seleccionada = (i == opcioSeleccionada);
            String prefix = seleccionada ? " > " : "   ";
            String text = prefix + opcions[i];
            int ox = cx - text.length() / 2;
            TextColor color = seleccionada ? TextColor.ANSI.YELLOW_BRIGHT : TextColor.ANSI.WHITE;
            for (int j = 0; j < text.length(); j++) {
                screen.setCharacter(ox + j, boxY + 3 + i,
                        new TextCharacter(text.charAt(j), color, TextColor.ANSI.BLACK));
            }
        }

        //nota de tecla ràpida ESC
        String escNota = pauseResumeHint;
        int en = cx - escNota.length() / 2;
        for (int j = 0; j < escNota.length(); j++) {
            screen.setCharacter(en + j, boxY + 3 + opcions.length + 1,
                    new TextCharacter(escNota.charAt(j), TextColor.ANSI.CYAN, TextColor.ANSI.BLACK));
        }

        screen.refresh();
    }

    public void tanca() throws IOException { // tanca es perque tanca la pantalla
        screen.close();
    }

    public void dibuixaEnigma(String pregunta, String inputActual) throws IOException {
        screen.clear();
        int cols = screen.getTerminalSize().getColumns();
        int files = screen.getTerminalSize().getRows();
        int cx = cols / 2, cy = files / 2;
        TextColor blanc = new TextColor.RGB(220, 220, 220);
        TextColor groc = new TextColor.RGB(220, 180, 50);
        TextColor verd = new TextColor.RGB(80, 200, 120);

        pintaText(cx - 10, cy - 4, "[ ENIGMA DEL NPC ]", groc);
        pintaText(cx - pregunta.length() / 2, cy - 2, pregunta, blanc);
        pintaText(cx - 15, cy + 1, "Resposta: " + inputActual + "_", verd);
        pintaText(cx - 12, cy + 3, "ENTER per confirmar  |  ESC per sortir", new TextColor.RGB(110, 110, 110));
        screen.refresh();
    }

    public void dibuixaComerciants(int pis) throws IOException {
        screen.clear();
        int cols = screen.getTerminalSize().getColumns();
        int files = screen.getTerminalSize().getRows();
        int cx = cols / 2, cy = files / 2;
        TextColor blanc = new TextColor.RGB(220, 220, 220);
        TextColor groc = new TextColor.RGB(220, 180, 50);

        pintaText(cx - 12, cy - 3, "[ COMERCIANT - PIS " + pis + " ]", groc);
        pintaText(cx - 15, cy, "Benvingut! (comerç per implementar)", blanc);
        pintaText(cx - 10, cy + 3, "ESC / ENTER per sortir", new TextColor.RGB(110, 110, 110));
        screen.refresh();
    }

}
