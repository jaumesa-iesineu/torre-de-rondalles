package com.iessineu.rondalles.entitats;

import com.googlecode.lanterna.TextColor;

public abstract class Enemic extends Entitat {

    // color i art carregats des del game.json
    protected TextColor colorDef = null;
    protected String[] artAscii = null;

    char lletra;

    public enum EstatEnemic {
        PATRULLANT,
        ALERTA,
        PERSEGUINT,
        EXECUTANT_ACCIO,
        MORT
    }

    protected int vida;
    protected int vidaMaxima;
    protected int atac;

    private int tornsVeri = 0;
    private int tornsFoc = 0;
    private int tornsGel = 0;

    protected int radDeteccio;

    public int getRadDeteccio() {
        return radDeteccio;
    }

    protected EstatEnemic estatEnemic;

    public Enemic(int x, int y, char simbol, int vida, int atac, int radDeteccio) {
        super(x, y, simbol);

        this.vidaMaxima = vida;
        this.vida = vida;
        this.atac = atac;
        this.radDeteccio = radDeteccio;
        this.estatEnemic = EstatEnemic.PATRULLANT;
    }

    public abstract void actualitzaIA(Jugador jugador, char[][] cells);

    public abstract void actualitzaIAambRadi(Jugador jugador, int radEfectiu);

    protected boolean potVeure(Jugador jugador, char[][] cells) {

        if (cells == null) {
            return true;
        }

        int x0 = this.x;
        int y0 = this.y;

        int x1 = jugador.getX();
        int y1 = jugador.getY();

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);

        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;

        int err = dx - dy;

        int cx = x0;
        int cy = y0;

        while (cx != x1 || cy != y1) {

            int e2 = 2 * err;

            if (e2 > -dy) {
                err -= dy;
                cx += sx;
            }

            if (e2 < dx) {
                err += dx;
                cy += sy;
            }

            if (cx == x1 && cy == y1) {
                break;
            }

            if (cy >= 0 && cy < cells.length
                    && cx >= 0 && cx < cells[cy].length) {

                if (cells[cy][cx] == '#') {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void actualitza() {
    }

    @Override
    public void interactua(Jugador jugador) {

        jugador.rebreDany(atac);
        estatEnemic = EstatEnemic.EXECUTANT_ACCIO;
    }

    protected double distanciaAl(Jugador jugador) {

        int dx = jugador.getX() - this.x;
        int dy = jugador.getY() - this.y;

        return Math.sqrt(dx * dx + dy * dy);
    }

    public void canviaEstat(EstatEnemic nouEstat) {
        this.estatEnemic = nouEstat;
    }

    public void rebreDany(int dany) {

        vida -= dany;

        if (vida <= 0) {

            vida = 0;
            estatEnemic = EstatEnemic.MORT;
            actiu = false;
        }
    }

    public void tickVeri() {

        if (tornsVeri <= 0) {
            return;
        }

        rebreDany(3);
        tornsVeri--;
    }

    public void tickFoc() {

        if (tornsFoc <= 0) {
            return;
        }

        tornsFoc--;
    }

    public void tickGel() {

        if (tornsGel <= 0) {
            return;
        }

        tornsGel--;
    }

    public char getLletra() {
        return lletra;
    }

    public int getAtacEfectiu() {

        int penalitzacio = tornsFoc > 0 ? 2 : 0;

        return Math.max(1, atac - penalitzacio);
    }

    public int getDefensaEfectiva() {
        return 0;
    }

    public void setTornsVeri(int t) {
        tornsVeri = t;
    }

    public void setTornsFoc(int t) {
        tornsFoc = t;
    }

    public void setTornsGel(int t) {
        tornsGel = t;
    }

    public int getTornsVeri() {
        return tornsVeri;
    }

    public int getTornsFoc() {
        return tornsFoc;
    }

    public int getTornsGel() {
        return tornsGel;
    }

    public EstatEnemic getEstatEnemic() {
        return estatEnemic;
    }

    public int getVida() {
        return vida;
    }

    public boolean esMort() {
        return vida <= 0;
    }

    public int getAtac() {
        return atac;
    }

    public int getVidaMaxima() {
        return vidaMaxima;
    }

    public void aplicaDefinicio(
            int vida,
            int atac,
            int radi,
            int r,
            int g,
            int b,
            String[] art) {

        this.vida = vida;
        this.vidaMaxima = vida;
        this.atac = atac;
        this.radDeteccio = radi;
        this.colorDef = new TextColor.RGB(r, g, b);
        this.artAscii = art;
    }

    public String[] getArtAscii() {
        return artAscii;
    }

    public TextColor getColor() {
        return colorDef;
    }
}