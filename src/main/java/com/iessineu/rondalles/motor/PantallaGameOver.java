package com.iessineu.rondalles.motor;

// Dades d'una pantalla de game over: titol, text de mort i ascii art.
// Es carrega des d'un fitxer JSON a recursos i es renderitza amb
// una animacio tipus typewriter al text.
public class PantallaGameOver {

    private final String titol;
    private final String[] liniesText;
    private final String[] liniesArt;

    public PantallaGameOver(String titol, String[] liniesText, String[] liniesArt) {
        this.titol = titol != null ? titol : "GAME OVER";
        this.liniesText = liniesText != null ? liniesText : new String[0];
        this.liniesArt = liniesArt != null ? liniesArt : new String[0];
    }

    public String getTitol() { return titol; }
    public String[] getLiniesText() { return liniesText; }
    public String[] getLiniesArt() { return liniesArt; }

    // Total de caracters visibles del text (inclosos els salts de linia).
    public int totalCaracters() {
        int total = 0;
        for (int i = 0; i < liniesText.length; i++) {
            total += liniesText[i].length();
            if (i < liniesText.length - 1) total += 1;
        }
        return total;
    }
}
