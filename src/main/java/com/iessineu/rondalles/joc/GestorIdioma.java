package com.iessineu.rondalles.joc;

import java.util.HashMap;
import java.util.Map;

public class GestorIdioma {

    private static String idioma = "ca";

    private static final Map<String, Map<String, String>> traduccions = new HashMap<>();

    static {
        // Català
        Map<String, String> ca = new HashMap<>();
        ca.put("menuIniciar", "Iniciar partida");
        ca.put("menuSortir", "Sortir");
        ca.put("menuMusica", "Música");
        ca.put("menuIdioma", "Idioma");
        ca.put("menuIdiomaNom", "Català");
        ca.put("menuReanudar", "Reanudar");
        ca.put("menuGuardar", "Guardar");
        ca.put("menuCarregar", "Carregar");
        ca.put("menuTornaAcomencar", "Torna a començar");
        ca.put("pauseTitle", "  *** PAUSA ***  ");
        ca.put("pauseInstructions", "Fletxes + ENTER per seleccionar");
        ca.put("pauseResumePista", "[ ESC ] Reanudar");
        ca.put("enigmaTitol", "DITES MALLORQUINES");
        ca.put("enigmaSeleccionaTitol", "COMERCIANT - TRIA UNA DITA");
        ca.put("enigmaSeleccioText", "Tria una dita per desbloquejar la botiga:");
        ca.put("enigmaFacil", "Fàcil   (2 ítems)");
        ca.put("enigmaNormal", "Normal  (4 ítems)");
        ca.put("enigmaDificil", "Difícil (6 ítems)");
        ca.put("enigmaResposta", "Resposta: ");
        ca.put("enigmaConfirmar", "ENTER confirmar  |  ESC tornar");
        ca.put("enigmaNavegar", "[ ↑↓ ] Navegar  |  [ ENTER ] Triar  |  [ ESC ] Sortir");
        ca.put("enigmaError", "Resposta incorrecta! Torna-ho a intentar.");
        ca.put("enigmaCorrecta", "Correcte!");
        ca.put("botiga", "BOTIGA DEL COMERCIANT");
        ca.put("botigaAgafa", "ENTER agafar  |  ESC sortir");
        ca.put("botigaItemsAgafats", "Ítems agafats: ");
        ca.put("botigaMaxItems", " / ");
        ca.put("botigaDisponibles", "Ítems disponibles:");
        ca.put("botigaPlena", "Inventari ple! No pots agafar més.");
        ca.put("botigaEpuisada", "Has agafat tots els ítems permesos.");
        traduccions.put("ca", ca);

        // Castellano
        Map<String, String> es = new HashMap<>();
        es.put("menuIniciar", "Iniciar partida");
        es.put("menuSortir", "Salir");
        es.put("menuMusica", "Música");
        es.put("menuIdioma", "Idioma");
        es.put("menuIdiomaNom", "Castellano");
        es.put("menuReanudar", "Reanudar");
        es.put("menuGuardar", "Guardar");
        es.put("menuCarregar", "Cargar");
        es.put("menuTornaAcomencar", "Volver a empezar");
        es.put("pauseTitle", "  *** PAUSA ***  ");
        es.put("pauseInstructions", "Flechas + ENTER para seleccionar");
        es.put("pauseResumePista", "[ ESC ] Reanudar");
        es.put("enigmaTitol", "DICHOS MALLORQUINES");
        es.put("enigmaSeleccionaTitol", "COMERCIANTE - ELIGE UN DICHO");
        es.put("enigmaSeleccioText", "Elige un dicho para desbloquear la tienda:");
        es.put("enigmaFacil", "Fácil   (2 ítems)");
        es.put("enigmaNormal", "Normal  (4 ítems)");
        es.put("enigmaDificil", "Difícil (6 ítems)");
        es.put("enigmaResposta", "Respuesta: ");
        es.put("enigmaConfirmar", "ENTER confirmar  |  ESC volver");
        es.put("enigmaNavegar", "[ ↑↓ ] Navegar  |  [ ENTER ] Elegir  |  [ ESC ] Salir");
        es.put("enigmaError", "¡Respuesta incorrecta! Inténtalo de nuevo.");
        es.put("enigmaCorrecta", "¡Correcto!");
        es.put("botiga", "TIENDA DEL COMERCIANTE");
        es.put("botigaAgafa", "ENTER coger  |  ESC salir");
        es.put("botigaItemsAgafats", "Ítems cogidos: ");
        es.put("botigaMaxItems", " / ");
        es.put("botigaDisponibles", "Ítems disponibles:");
        es.put("botigaPlena", "¡Inventario lleno! No puedes coger más.");
        es.put("botigaEpuisada", "Has cogido todos los ítems permitidos.");
        traduccions.put("es", es);

        // English
        Map<String, String> en = new HashMap<>();
        en.put("menuIniciar", "Start game");
        en.put("menuSortir", "Exit");
        en.put("menuMusica", "Music");
        en.put("menuIdioma", "Language");
        en.put("menuIdiomaNom", "English");
        en.put("menuReanudar", "Resume");
        en.put("menuGuardar", "Save");
        en.put("menuCarregar", "Load");
        en.put("menuTornaAcomencar", "Play again");
        en.put("pauseTitle", "  *** PAUSE ***  ");
        en.put("pauseInstructions", "Arrows + ENTER to select");
        en.put("pauseResumePista", "[ ESC ] Resume");
        en.put("enigmaTitol", "MALLORCAN SAYINGS");
        en.put("enigmaSeleccionaTitol", "MERCHANT - CHOOSE A SAYING");
        en.put("enigmaSeleccioText", "Choose a saying to unlock the shop:");
        en.put("enigmaFacil", "Easy    (2 items)");
        en.put("enigmaNormal", "Normal  (4 items)");
        en.put("enigmaDificil", "Hard    (6 items)");
        en.put("enigmaResposta", "Answer: ");
        en.put("enigmaConfirmar", "ENTER confirm  |  ESC back");
        en.put("enigmaNavegar", "[ ↑↓ ] Navigate  |  [ ENTER ] Choose  |  [ ESC ] Exit");
        en.put("enigmaError", "Wrong answer! Try again.");
        en.put("enigmaCorrecta", "Correct!");
        en.put("botiga", "MERCHANT SHOP");
        en.put("botigaAgafa", "ENTER take  |  ESC exit");
        en.put("botigaItemsAgafats", "Items taken: ");
        en.put("botigaMaxItems", " / ");
        en.put("botigaDisponibles", "Available items:");
        en.put("botigaPlena", "Inventory full! Cannot take more.");
        en.put("botigaEpuisada", "You have taken all allowed items.");
        traduccions.put("en", en);
    }

    public static void setIdioma(String id) {
        if (traduccions.containsKey(id)) idioma = id;
    }

    public static String getIdioma() { return idioma; }

    public static String getNomIdioma() {
        return t("menuIdiomaNom");
    }

    public static String[] getIdiomasCodi() { return new String[]{"ca", "es", "en"}; }

    public static String t(String clau) {
        Map<String, String> lang = traduccions.getOrDefault(idioma, traduccions.get("ca"));
        return lang.getOrDefault(clau, traduccions.get("ca").getOrDefault(clau, clau));
    }
}
