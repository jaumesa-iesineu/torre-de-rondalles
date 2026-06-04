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

    protected void mouCapA(int tx, int ty, char[][] cells, Jugador jugador) {
        int dx = Integer.signum(tx - this.x);
        int dy = Integer.signum(ty - this.y);
        int nx = this.x + dx, ny = this.y + dy;
        if (pucMourem(nx, ny, cells, jugador)) { this.x = nx; this.y = ny; return; }
        nx = this.x + dx; ny = this.y;
        if (dx != 0 && pucMourem(nx, ny, cells, jugador)) { this.x = nx; this.y = ny; return; }
        nx = this.x; ny = this.y + dy;
        if (dy != 0 && pucMourem(nx, ny, cells, jugador)) { this.x = nx; this.y = ny; }
    }

    private boolean pucMourem(int nx, int ny, char[][] cells, Jugador jugador) {
        if (cells == null) return true;
        if (ny < 0 || ny >= cells.length || nx < 0 || nx >= cells[ny].length) return false;
        if (cells[ny][nx] == '#') return false;
        if (cells[ny][nx] == 'i') return false; // no volem trepitjar els items
        if (jugador != null && nx == jugador.getX() && ny == jugador.getY()) return false;
        return true;
    }

    protected int[] primerPasBFS(int tx, int ty, char[][] cells) {
        if (cells == null) return new int[]{tx, ty};
        int rows = cells.length, cols = rows > 0 ? cells[0].length : 0;
        boolean[][] visitat = new boolean[rows][cols];
        int[] firstX = new int[rows * cols];
        int[] firstY = new int[rows * cols];
        java.util.Arrays.fill(firstX, -1);
        java.util.Arrays.fill(firstY, -1);
        java.util.Queue<int[]> cua = new java.util.LinkedList<>();
        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
        visitat[this.y][this.x] = true;
        for (int[] d : dirs) {
            int nx = this.x + d[0], ny = this.y + d[1];
            if (ny >= 0 && ny < rows && nx >= 0 && nx < cols && cells[ny][nx] != '#' && cells[ny][nx] != 'i' && !visitat[ny][nx]) {
                visitat[ny][nx] = true;
                firstX[ny * cols + nx] = nx;
                firstY[ny * cols + nx] = ny;
                cua.add(new int[]{nx, ny});
            }
        }
        while (!cua.isEmpty()) {
            int[] cur = cua.poll();
            int cx = cur[0], cy = cur[1];
            if (cx == tx && cy == ty) return new int[]{firstX[cy * cols + cx], firstY[cy * cols + cx]};
            for (int[] d : dirs) {
                int nx = cx + d[0], ny = cy + d[1];
                if (ny >= 0 && ny < rows && nx >= 0 && nx < cols && cells[ny][nx] != '#' && cells[ny][nx] != 'i' && !visitat[ny][nx]) {
                    visitat[ny][nx] = true;
                    firstX[ny * cols + nx] = firstX[cy * cols + cx];
                    firstY[ny * cols + nx] = firstY[cy * cols + cx];
                    cua.add(new int[]{nx, ny});
                }
            }
        }
        return new int[]{-1, -1};
    }

    public String[] getArtAscii() {
        return artAscii;
    }

    public TextColor getColor() {
        return colorDef;
    }
}