package com.iessineu.rondalles.entitats;

import com.googlecode.lanterna.TextColor;
import com.iessineu.rondalles.joc.ConfigGame;
import java.util.ArrayList;
import java.util.List;

public class NpcComerciants extends Entitat {

    private final int pis;
    private int nivellResolt = 0; // 0=no resolt, 1/2/3=nivell resolt
    private boolean comerciatJa = false;
    private List<ConfigGame.DitaConfig> dites = new ArrayList<>();
    private List<String> itemsVenda = new ArrayList<>();

    public NpcComerciants(int x, int y, int pis, ConfigGame config) {
        super(x, y, 'N');
        this.pis = pis;
        if (config != null) {
            ConfigGame.EnigmeConfig enigme = config.getEnigmaPerPlanta(pis);
            if (enigme != null) {
                if (enigme.dites != null) this.dites = enigme.dites;
                if (enigme.itemsVenda != null) this.itemsVenda = enigme.itemsVenda;
            }
        }
    }

    public List<ConfigGame.DitaConfig> getDites() {
        return dites;
    }

    public ConfigGame.DitaConfig getDita(int index) {
        if (index >= 0 && index < dites.size()) return dites.get(index);
        return null;
    }

    public boolean teDites() {
        return !dites.isEmpty();
    }

    public boolean comprovaSolucio(int indexDita, String resposta) {
        ConfigGame.DitaConfig dita = getDita(indexDita);
        if (dita == null) return false;
        boolean correcte = resposta.trim().equalsIgnoreCase(dita.resposta);
        if (correcte) {
            nivellResolt = dita.nivell;
        }
        return correcte;
    }

    public boolean isResolt() {
        return nivellResolt > 0;
    }

    public int getNivellResolt() {
        return nivellResolt;
    }

    public int getMaxItems() {
        return nivellResolt * 2; // 1→2, 2→4, 3→6
    }

    public List<String> getItemsVenda() {
        return itemsVenda;
    }

    public boolean isComerciatJa() {
        return comerciatJa;
    }

    public void setComerciatJa(boolean comerciatJa) {
        this.comerciatJa = comerciatJa;
    }

    public int getPis() {
        return pis;
    }

    @Override
    public void actualitza() {
    }

    @Override
    public void interactua(Jugador jugador) {
    }

    @Override
    public TextColor getColor() {
        return new TextColor.RGB(80, 200, 220);
    }
}
