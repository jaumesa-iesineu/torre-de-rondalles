package com.iessineu.rondalles.entitats;

import com.googlecode.lanterna.TextColor;
import com.iessineu.rondalles.joc.ConfigGame;

public class NpcComerciants extends Entitat {

    private final int pis;
    private boolean enigmaResult = false;
    private String pregunta;
    private String resposta;

    public NpcComerciants(int x, int y, int pis, ConfigGame config) {
        super(x, y, 'N');
        this.pis = pis;
        //carregam l'enigme des del JSON si hi és
        if (config != null) {
            ConfigGame.EnigmeConfig enigme = config.getEnigmaPerPlanta(pis);
            if (enigme != null) {
                this.pregunta = enigme.pregunta;
                this.resposta = enigme.resposta;
            }
        }
    }

    public String getEnigma() {
        return pregunta;
    }

    public boolean comprovaSolucio(String resposta) {
        enigmaResult = resposta.trim().equalsIgnoreCase(this.resposta);
        return enigmaResult;
    }

    public boolean isEnigmaResult() {
        return enigmaResult;
    }

    @Override
    public void actualitza() {
    }

    @Override
    public void interactua(Jugador jugador) {
    }

    @Override
    public TextColor getColor() {
        return new TextColor.RGB(80, 200, 220); //blau cel
    }
}
