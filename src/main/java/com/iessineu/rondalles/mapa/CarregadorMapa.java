/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iessineu.rondalles.mapa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author jaume, dani, sergi, kanhai i pere
 */
public class CarregadorMapa {

    // Carrega un mapa detectant automàticament el format:
    // - si el fitxer acaba en .map → text pla
    // - si no → XML (format antic .game)
    public static Mapa carrega(String rutaFitxer) throws Exception {
        if (rutaFitxer.endsWith(".map")) {
            return carregaPlà(rutaFitxer);
        }
        return parsejaDocument(parsejaFitxer(rutaFitxer));
    }

    // Carrega un mapa des d'un fitxer .map de text pla.
    public static Mapa carregaPlà(String rutaFitxer) throws Exception {
        List<String> files = new ArrayList<>();
        String nom = "sense nom";

        InputStream in = obriStream(rutaFitxer);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String linia;
            while ((linia = br.readLine()) != null) {
                if (linia.startsWith("nom:")) {
                    nom = linia.substring(4).trim();
                    continue;
                }
                if (linia.startsWith("//")) {
                    continue;
                }
                if (!linia.isBlank()) {
                    files.add(linia);
                }
            }
        }

        int alcada = files.size();
        int amplada = files.stream().mapToInt(String::length).max().orElse(0);
        char[][] celles = new char[alcada][amplada];
        for (int y = 0; y < alcada; y++) {
            String row = files.get(y);
            for (int x = 0; x < amplada; x++) {
                celles[y][x] = x < row.length() ? row.charAt(x) : '#';
            }
        }
        return new Mapa(celles, nom);
    }

    private static InputStream obriStream(String rutaFitxer) throws Exception {
        File fitxer = new File(rutaFitxer);
        if (fitxer.exists()) {
            return new java.io.FileInputStream(fitxer);
        }
        InputStream is = CarregadorMapa.class.getResourceAsStream("/" + rutaFitxer);
        if (is == null) {
            throw new Exception("no trobat el fitxer: " + rutaFitxer);
        }
        return is;
    }

    //retorna les habitacions definides al fitxer (per a l'editor)
    //funciona tant amb l'antic format <habitacio amplada=...> com amb el nou <habitacio><tiles>
    //si el fitxer és tilemap pur retorna llista buida
    public static List<Habitacio> carregaHabitacionsDefinides(String rutaFitxer) throws Exception {
        Document doc = parsejaFitxer(rutaFitxer);
        Element arrel = doc.getDocumentElement();
        List<Habitacio> resultat = new ArrayList<>();

        NodeList nodes = arrel.getElementsByTagName("habitacio");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element h = (Element) nodes.item(i);
            int x = h.hasAttribute("x") ? Integer.parseInt(h.getAttribute("x")) : 0;
            int y = h.hasAttribute("y") ? Integer.parseInt(h.getAttribute("y")) : 0;
            String id = h.hasAttribute("id") ? h.getAttribute("id") : "hab" + (i + 1);

            //format nou: <tiles> embedded, calculam w/h de les línies
            NodeList tilesNodes = h.getElementsByTagName("tiles");
            if (tilesNodes.getLength() > 0) {
                List<String> files = extrauFiles(tilesNodes.item(0).getTextContent());
                int w = files.stream().mapToInt(String::length).max().orElse(0);
                int hh = files.size();
                if (w > 0 && hh > 0) {
                    resultat.add(new Habitacio(id, x, y, w, hh));
                }
                continue;
            }

            //format antic: amplada i alcada com a atributs
            if (h.hasAttribute("amplada") && h.hasAttribute("alcada")) {
                int w = Integer.parseInt(h.getAttribute("amplada"));
                int hh = Integer.parseInt(h.getAttribute("alcada"));
                resultat.add(new Habitacio(id, x, y, w, hh));
            }
        }
        return resultat;
    }

    private static Document parsejaFitxer(String rutaFitxer) throws Exception {
        File fitxer = new File(rutaFitxer);
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        if (fitxer.exists()) {
            return db.parse(fitxer);
        }
        InputStream is = CarregadorMapa.class.getResourceAsStream("/" + rutaFitxer);
        if (is == null) {
            throw new Exception("no trobat el fitxer: " + rutaFitxer);
        }
        return db.parse(is);
    }

    //funció per parsejar el xml i convertir-lo a nes mapa
    //suporta tres formats: habitacions clàssiques, tilemap pla, habitacions amb tiles embedded
    private static Mapa parsejaDocument(Document doc) {
        Element arrel = doc.getDocumentElement();
        String nom = arrel.getAttribute("nom");

        //si té <tilemap> és un mapa pintat tile a tile
        NodeList tilemapNodes = arrel.getElementsByTagName("tilemap");
        if (tilemapNodes.getLength() > 0) {
            return parsejaTilemap(tilemapNodes.item(0).getTextContent(), nom);
        }

        //si la primera <habitacio> té un fill <tiles> és el format nou
        NodeList habNodes = arrel.getElementsByTagName("habitacio");
        if (habNodes.getLength() > 0) {
            Element primera = (Element) habNodes.item(0);
            if (primera.getElementsByTagName("tiles").getLength() > 0) {
                return parsejaHabitacionsAmbTiles(arrel, nom);
            }
        }

        //primer miram les dimensions del mapa
        //cada habitació té una posició i una mida, necessitam saber quant gran ha de ser
        NodeList habitacions = arrel.getElementsByTagName("habitacio");
        int maxX = 0;
        int maxY = 0;

        for (int i = 0; i < habitacions.getLength(); i++) { // per cada habitacio
            Element h = (Element) habitacions.item(i);
            int x = Integer.parseInt(h.getAttribute("x"));
            int y = Integer.parseInt(h.getAttribute("y"));
            int amplada = Integer.parseInt(h.getAttribute("amplada"));
            int alcada = Integer.parseInt(h.getAttribute("alcada"));

            if (x + amplada > maxX) {
                maxX = x + amplada;
            }
            if (y + alcada > maxY) {
                maxY = y + alcada;
            }
        }

        //cream els "bordes" de sa habitació
        char[][] celles = new char[maxY][maxX];
        for (int y = 0; y < maxY; y++) {
            for (int x = 0; x < maxX; x++) {
                celles[y][x] = '#';
            }
        }

        //la dibuixam
        for (int i = 0; i < habitacions.getLength(); i++) {
            Element h = (Element) habitacions.item(i);
            int ox = Integer.parseInt(h.getAttribute("x"));
            int oy = Integer.parseInt(h.getAttribute("y"));
            int amplada = Integer.parseInt(h.getAttribute("amplada"));
            int alcada = Integer.parseInt(h.getAttribute("alcada"));

            //dibuixam # a ses parets i · si es en terra
            for (int y = oy; y < oy + alcada; y++) {
                for (int x = ox; x < ox + amplada; x++) {
                    boolean esParet = (x == ox || x == ox + amplada - 1 || y == oy || y == oy + alcada - 1);
                    celles[y][x] = esParet ? '#' : '.';
                }
            }

            //posam les entitats que hi ha dins l'habitació
            NodeList entitats = h.getElementsByTagName("enemic");
            for (int j = 0; j < entitats.getLength(); j++) {
                Element e = (Element) entitats.item(j);
                int ex = ox + Integer.parseInt(e.getAttribute("x"));
                int ey = oy + Integer.parseInt(e.getAttribute("y"));
                if (ey < maxY && ex < maxX) {
                    celles[ey][ex] = 'e';
                }
            }

            NodeList items = h.getElementsByTagName("item");
            for (int j = 0; j < items.getLength(); j++) {
                Element e = (Element) items.item(j);
                int ex = ox + Integer.parseInt(e.getAttribute("x"));
                int ey = oy + Integer.parseInt(e.getAttribute("y"));
                if (ey < maxY && ex < maxX) {
                    celles[ey][ex] = 'i';
                }
            }
        }

        //connexions: obrim les portes entre habitacions
        //simplement substituïm la paret del punt de connexió per terra
        NodeList connexions = arrel.getElementsByTagName("connexio");
        for (int i = 0; i < connexions.getLength(); i++) {
            Element c = (Element) connexions.item(i);
            String de = c.getAttribute("de");
            String a = c.getAttribute("a");
            String dir = c.getAttribute("direccio");

            //trobam les dues habitacions per obrir la porta
            Element habDe = trobaHabitacio(habitacions, de);
            Element habA = trobaHabitacio(habitacions, a);
            if (habDe == null || habA == null) {
                continue;
            }

            obrirPorta(celles, habDe, habA, dir);
        }

        return new Mapa(celles, nom);
    }

    //cerca una habitació per id dins la llista
    private static Element trobaHabitacio(NodeList llista, String id) {
        for (int i = 0; i < llista.getLength(); i++) {
            Element h = (Element) llista.item(i);
            if (h.getAttribute("id").equals(id)) {
                return h;
            }
        }
        return null;
    }

    //parseja un bloc <tilemap> amb la quadrícula de caràcters directament
    private static Mapa parsejaTilemap(String contingut, String nom) {
        List<String> files = new ArrayList<>();
        for (String linia : contingut.split("\n")) {
            String t = linia.stripTrailing();
            if (!t.isBlank()) {
                files.add(t);
            }
        }
        int alcada = files.size();
        int amplada = files.isEmpty() ? 0 : files.stream().mapToInt(String::length).max().orElse(0);
        char[][] celles = new char[alcada][amplada];
        for (int y = 0; y < alcada; y++) {
            String row = files.get(y);
            for (int x = 0; x < amplada; x++) {
                celles[y][x] = x < row.length() ? row.charAt(x) : '#';
            }
        }
        return new Mapa(celles, nom);
    }

    //parseja el nou format: <habitacio> amb <tiles> embedded
    private static Mapa parsejaHabitacionsAmbTiles(Element arrel, String nom) {
        NodeList nodes = arrel.getElementsByTagName("habitacio");

        //primera passada: calculam les dimensions màximes del mapa global
        int maxX = 0, maxY = 0;
        for (int i = 0; i < nodes.getLength(); i++) {
            Element h = (Element) nodes.item(i);
            int ox = Integer.parseInt(h.getAttribute("x"));
            int oy = Integer.parseInt(h.getAttribute("y"));
            NodeList tilesNodes = h.getElementsByTagName("tiles");
            if (tilesNodes.getLength() == 0) {
                continue;
            }
            List<String> files = extrauFiles(tilesNodes.item(0).getTextContent());
            int w = files.stream().mapToInt(String::length).max().orElse(0);
            int hh = files.size();
            if (ox + w > maxX) {
                maxX = ox + w;
            }
            if (oy + hh > maxY) {
                maxY = oy + hh;
            }
        }

        //cream la graella buida plena de parets
        char[][] celles = new char[maxY][maxX];
        for (char[] row : celles) {
            java.util.Arrays.fill(row, '#');
        }

        //segona passada: enganxam els tiles de cada habitació a la posició corresponent
        for (int i = 0; i < nodes.getLength(); i++) {
            Element h = (Element) nodes.item(i);
            int ox = Integer.parseInt(h.getAttribute("x"));
            int oy = Integer.parseInt(h.getAttribute("y"));
            NodeList tilesNodes = h.getElementsByTagName("tiles");
            if (tilesNodes.getLength() == 0) {
                continue;
            }
            List<String> files = extrauFiles(tilesNodes.item(0).getTextContent());
            for (int dy = 0; dy < files.size(); dy++) {
                String row = files.get(dy);
                for (int dx = 0; dx < row.length(); dx++) {
                    int gy = oy + dy, gx = ox + dx;
                    if (gy < maxY && gx < maxX) {
                        celles[gy][gx] = row.charAt(dx);
                    }
                }
            }
        }

        return new Mapa(celles, nom);
    }

    //extreu les línies no buides d'un bloc de text (ús intern)
    private static List<String> extrauFiles(String text) {
        List<String> files = new ArrayList<>();
        for (String linia : text.split("\n")) {
            String t = linia.stripTrailing();
            if (!t.isBlank()) {
                files.add(t);
            }
        }
        return files;
    }

    //desa un mapa pintat com a fitxer .game amb format <tilemap>
    public static void desaGameXml(Mapa mapa, String rutaFitxer) throws Exception {
        try (PrintWriter pw = new PrintWriter(new FileWriter(rutaFitxer))) {
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            pw.println("<mapa id=\"" + mapa.getNom() + "\" nom=\"" + mapa.getNom() + "\">");
            pw.println("    <tilemap>");
            for (char[] fila : mapa.getCelles()) {
                pw.println("        " + new String(fila));
            }
            pw.println("    </tilemap>");
            pw.println("</mapa>");
        }
    }

    //desa un mapa amb habitacions definides en el nou format <habitacio><tiles>
    public static void desaGameXmlAmbHabitacions(List<Habitacio> habitacions, char[][] celles, String nom, String rutaFitxer) throws Exception {
        try (PrintWriter pw = new PrintWriter(new FileWriter(rutaFitxer))) {
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            pw.println("<mapa id=\"" + nom + "\" nom=\"" + nom + "\">");
            pw.println("    <habitacions>");
            for (Habitacio hab : habitacions) {
                pw.println("        <habitacio id=\"" + hab.getId() + "\" x=\"" + hab.getX() + "\" y=\"" + hab.getY() + "\">");
                pw.println("            <tiles>");
                for (int dy = 0; dy < hab.getH(); dy++) {
                    StringBuilder sb = new StringBuilder("                ");
                    for (int dx = 0; dx < hab.getW(); dx++) {
                        int gy = hab.getY() + dy, gx = hab.getX() + dx;
                        sb.append((gy < celles.length && gx < celles[0].length) ? celles[gy][gx] : '#');
                    }
                    pw.println(sb);
                }
                pw.println("            </tiles>");
                pw.println("        </habitacio>");
            }
            pw.println("    </habitacions>");
            pw.println("</mapa>");
        }
    }

    //obre una porta entre dues habitacions posant '.' a la paret
    private static void obrirPorta(char[][] celles, Element de, Element a, String dir) {
        int x1 = Integer.parseInt(de.getAttribute("x"));
        int y1 = Integer.parseInt(de.getAttribute("y"));
        int a1 = Integer.parseInt(de.getAttribute("amplada"));
        int h1 = Integer.parseInt(de.getAttribute("alcada"));
        int po = Integer.parseInt(de.getAttribute("porta"));
        int poEsp = Integer.parseInt(de.getAttribute("portaEsp"));

        //calculam el punt mig de la paret on obrirem la porta
        switch (dir) {
            case "est" -> {
                int px = x1 + a1 - 1;
                for (int i = 0; i < poEsp; i++) {
                    int py = y1 + po + i;
                    if (py < celles.length) {
                        celles[py][px] = '.';
                        if (px + 1 < celles[0].length) {
                            celles[py][px + 1] = '.';
                        }
                    }
                }
            }
            case "oest" -> {
                int px = x1;
                for (int i = 0; i < poEsp; i++) {
                    int py = y1 + po + i;
                    if (py < celles.length) {
                        celles[py][px] = '.';
                        if (px - 1 >= 0) {
                            celles[py][px - 1] = '.';
                        }
                    }
                }
            }
            case "sud" -> {
                int py = y1 + h1 - 1;
                for (int i = 0; i < poEsp; i++) {
                    int px = x1 + po + i;
                    if (px < celles[0].length) {
                        celles[py][px] = '.';
                        if (py + 1 < celles.length) {
                            celles[py + 1][px] = '.';
                        }
                    }
                }
            }
            case "nord" -> {
                int py = y1;
                for (int i = 0; i < poEsp; i++) {
                    int px = x1 + po + i;
                    if (px < celles[0].length) {
                        celles[py][px] = '.';
                        if (py - 1 >= 0) {
                            celles[py - 1][px] = '.';
                        }
                    }
                }
            }
        }
    }
}
