package com.iessineu.rondalles.entitats;

import com.googlecode.lanterna.TextColor;
import com.iessineu.rondalles.inventari.Clau;
import com.iessineu.rondalles.inventari.Inventari;

public class Porta extends Entitat {
    //portes normals (+), obertes (/), bloquejades (&)

    private boolean oberta;
    private boolean bloquejada;
    private boolean esPortaCanviPlanta;
    private String clauId;

    public Porta(int x, int y) {
        super(x, y, '+');
        this.oberta = false;
        this.bloquejada = false;
        this.esPortaCanviPlanta = false;
        this.clauId = null;
    }

    public Porta(int x, int y, boolean bloquejada, boolean esPortaCanviPlanta, String clauId) {
        super(x, y, bloquejada ? '&' : '+');
        this.oberta = false;
        this.bloquejada = bloquejada;
        this.esPortaCanviPlanta = esPortaCanviPlanta;
        this.clauId = clauId;
    }

    public boolean isOberta() {
        return oberta;
    }

    public boolean isTancada() {
        return !oberta;
    }

    public boolean isBloquejada() {
        return bloquejada;
    }

    public boolean isPortaCanviPlanta() {
        return esPortaCanviPlanta;
    }

    public String getClauId() {
        return clauId;
    }

    public boolean desbloqueja() {
        if (!bloquejada) return false;
        bloquejada = false;
        simbol = '+';
        return true;
    }

    public void alterna() {
        oberta = !oberta;
        simbol = oberta ? '/' : '+';
    }

    @Override
    public void actualitza() {
    }

    @Override
    public void interactua(Jugador jugador) {
        alterna();
    }

    public boolean teClau(Inventari inventari, int pisActual) {
        String clauNecessaria = clauId != null ? clauId : "clau-planta" + pisActual;
        for (int i = 0; i < inventari.getMaxSlots(); i++) {
            var slot = inventari.getSlot(i);
            if (slot != null && slot.item() instanceof Clau clau && clau.getId().equals(clauNecessaria)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public TextColor getColor() {
        if (bloquejada) {
            return new TextColor.RGB(200, 40, 40);
        }
        if (oberta) {
            return new TextColor.RGB(120, 80, 40);
        }
        return new TextColor.RGB(180, 100, 40);
    }
}
