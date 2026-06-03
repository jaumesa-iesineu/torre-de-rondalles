package com.iessineu.rondalles.entitats;

import com.googlecode.lanterna.TextColor;

public class NpcComerciants extends Entitat {

    private final int pis;
    private boolean enigmaResult = false;

    private static final String[][] ENIGMES = { //enigma, resposta
        {"Tenc ales però no vol, tenc cua però no és animal. Què sóc?", "una fletxa"},
        {"Com més gran em fas, menys pots veure. Què sóc?",             "la foscor"},
        {"Estic sempre davant teu però no es pot veure. Què sóc?",      "el future"},
        {"Parleu de mi però mai m'heu vist. Qui sóc?",                  "el silenci"},
        {"Tenc boca però no puc parlar, tenc llit però no puc dormir. Què sóc?", "un riu"}
    };

    public NpcComerciants(int x, int y, int pis) { //pis entre 1 i 5
        super(x, y, 'N');
        this.pis = Math.max(1, Math.min(5, pis));
    }

    public String getEnigma()  { return ENIGMES[pis - 1][0]; } //pis-1 perquè el pis va de 1 a 5 però els índexs van de 0 a 4

    public boolean comprovaSolucio(String resposta) {
        enigmaResult = resposta.trim().equalsIgnoreCase(ENIGMES[pis - 1][1]);
        return enigmaResult;
    }

    public boolean isEnigmaResult() { return enigmaResult; }

    @Override public void actualitza() {}
    @Override public void interactua(Jugador jugador) {}

    @Override
    public TextColor getColor() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getColor'");
    }
}