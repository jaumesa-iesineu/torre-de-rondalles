package com.iessineu.rondalles.motor;

// Dades de la pantalla de victoria: titol, text i ascii art.
public class PantallaVictoria {

    private final String titol;
    private final String[] liniesText;
    private final String[] liniesArt;

    public PantallaVictoria(String titol, String[] liniesText, String[] liniesArt) {
        this.titol = titol != null ? titol : "VICTORIA!";
        this.liniesText = liniesText != null ? liniesText : new String[0];
        this.liniesArt = liniesArt != null ? liniesArt : new String[0];
    }

    public String getTitol() { return titol; }
    public String[] getLiniesText() { return liniesText; }
    public String[] getLiniesArt() { return liniesArt; }

    public int totalCaracters() {
        int total = 0;
        for (int i = 0; i < liniesText.length; i++) {
            total += liniesText[i].length();
            if (i < liniesText.length - 1) total += 1;
        }
        return total;
    }
}
