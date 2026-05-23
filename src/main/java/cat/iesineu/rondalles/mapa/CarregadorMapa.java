/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.iesineu.rondalles.mapa;

import java.io.File;
import java.io.InputStream;
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

    //carrega un mapa des d'un fitxer .game (format xml)
    //primer mira al sistema de fitxers, si no el troba el cerca dins el jar
    public static Mapa carrega(String rutaFitxer) throws Exception {
        Document doc;

        File fitxer = new File(rutaFitxer);
        if (fitxer.exists()) {
            //si el fitxer és al disc, el llegim directament
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = db.parse(fitxer);
        } else {
            //si no, el cercam dins els recursos del jar
            InputStream is = CarregadorMapa.class.getResourceAsStream("/" + rutaFitxer);
            if (is == null) throw new Exception("no trobat el fitxer: " + rutaFitxer);
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = db.parse(is);
        }

        return parsejaDocument(doc);
    }

    //converteix el document xml en un objecte Mapa
    private static Mapa parsejaDocument(Document doc) {
        Element arrel = doc.getDocumentElement();
        String nom = arrel.getAttribute("nom");

        //primer passada: calculam les dimensions màximes del mapa
        //cada habitació té una posició i una mida, necessitam saber quant gran ha de ser la graella
        NodeList habitacions = arrel.getElementsByTagName("habitacio");
        int maxX = 0;
        int maxY = 0;

        for (int i = 0; i < habitacions.getLength(); i++) {
            Element h = (Element) habitacions.item(i);
            int x = Integer.parseInt(h.getAttribute("x"));
            int y = Integer.parseInt(h.getAttribute("y"));
            int amplada = Integer.parseInt(h.getAttribute("amplada"));
            int alcada = Integer.parseInt(h.getAttribute("alcada"));

            if (x + amplada > maxX) maxX = x + amplada;
            if (y + alcada > maxY) maxY = y + alcada;
        }

        //creim la graella buida (tot parets per defecte)
        char[][] celles = new char[maxY][maxX];
        for (int y = 0; y < maxY; y++)
            for (int x = 0; x < maxX; x++)
                celles[y][x] = '#';

        //segona passada: dibuixam cada habitació
        for (int i = 0; i < habitacions.getLength(); i++) {
            Element h = (Element) habitacions.item(i);
            int ox = Integer.parseInt(h.getAttribute("x"));
            int oy = Integer.parseInt(h.getAttribute("y"));
            int amplada = Integer.parseInt(h.getAttribute("amplada"));
            int alcada = Integer.parseInt(h.getAttribute("alcada"));

            //dibuixam les parets i el terra de l'habitació
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
                if (ey < maxY && ex < maxX) celles[ey][ex] = 'e';
            }

            NodeList items = h.getElementsByTagName("item");
            for (int j = 0; j < items.getLength(); j++) {
                Element e = (Element) items.item(j);
                int ex = ox + Integer.parseInt(e.getAttribute("x"));
                int ey = oy + Integer.parseInt(e.getAttribute("y"));
                if (ey < maxY && ex < maxX) celles[ey][ex] = 'i';
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
            if (habDe == null || habA == null) continue;

            obrirPorta(celles, habDe, habA, dir);
        }

        return new Mapa(celles, nom);
    }

    //cerca una habitació per id dins la llista
    private static Element trobaHabitacio(NodeList llista, String id) {
        for (int i = 0; i < llista.getLength(); i++) {
            Element h = (Element) llista.item(i);
            if (h.getAttribute("id").equals(id)) return h;
        }
        return null;
    }

    //obre una porta entre dues habitacions posant '.' a la paret
    private static void obrirPorta(char[][] celles, Element de, Element a, String dir) {
        int x1 = Integer.parseInt(de.getAttribute("x"));
        int y1 = Integer.parseInt(de.getAttribute("y"));
        int a1 = Integer.parseInt(de.getAttribute("amplada"));
        int h1 = Integer.parseInt(de.getAttribute("alcada"));

        //calculam el punt mig de la paret on obrirem la porta
        switch (dir) {
            case "est" -> {
                int px = x1 + a1 - 1;
                int py = y1 + h1 / 2;
                celles[py][px] = '.';
                if (px + 1 < celles[0].length) celles[py][px + 1] = '.';
            }
            case "oest" -> {
                int px = x1;
                int py = y1 + h1 / 2;
                celles[py][px] = '.';
                if (px - 1 >= 0) celles[py][px - 1] = '.';
            }
            case "sud" -> {
                int px = x1 + a1 / 2;
                int py = y1 + h1 - 1;
                celles[py][px] = '.';
                if (py + 1 < celles.length) celles[py + 1][px] = '.';
            }
            case "nord" -> {
                int px = x1 + a1 / 2;
                int py = y1;
                celles[py][px] = '.';
                if (py - 1 >= 0) celles[py - 1][px] = '.';
            }
        }
    }
}
