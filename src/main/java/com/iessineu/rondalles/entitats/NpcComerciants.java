package com.iessineu.rondalles.entitats;

public class NpcComerciants extends Entitat {

    private final int pis;
    private boolean enigmaResolt = false;

    private static final String[][] ENIGMES = {
        {"Tenc ales però no vol, tenc cua però no és animal. Què sóc?", "una fletxa"},
        {"Com més gran em fas, menys pots veure. Què sóc?",             "la foscor"},
        {"Estic sempre davant teu però no es pot veure. Què sóc?",      "el futur"},
        {"Parleu de mi però mai m'heu vist. Qui sóc?",                  "el silenci"},
        {"Tenc boca però no puc parlar, tenc llit però no puc dormir. Què sóc?", "un riu"}
    };

    public NpcComerciants(int x, int y, int pis) {
        super(x, y, 'N');
        this.pis = Math.max(1, Math.min(5, pis));
    }

    public String getEnigma()  { return ENIGMES[pis - 1][0]; }

    public boolean comprovaSolucio(String resposta) {
        enigmaResolt = resposta.trim().equalsIgnoreCase(ENIGMES[pis - 1][1]);
        return enigmaResolt;
    }

    public boolean isEnigmaResolt() { return enigmaResolt; }

    @Override public void actualitza() {}
    @Override public void interactua(Jugador jugador) {}
}