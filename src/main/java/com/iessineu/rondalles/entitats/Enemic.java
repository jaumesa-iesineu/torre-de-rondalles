package com.iessineu.rondalles.entitats;

import com.googlecode.lanterna.TextColor;
import com.iessineu.rondalles.motor.CeldaArt;
import com.iessineu.rondalles.motor.EinesColor;
import com.iessineu.rondalles.joc.TipusEnemic;
import com.iessineu.rondalles.joc.Simbols;
import java.util.List;

public class Enemic extends Entitat {

    protected transient TextColor colorDef = null;
    protected transient String[] artAscii = null;
    protected transient CeldaArt[][] artJson = null;

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

    //camps per area-boss (no surten de la seva zona)
    private int spawnX;
    private int spawnY;
    private int area;

    //velocitat de l'enemic (cada quants torns actua)
    private int velocitat = 1;
    private int contadorMoviment = 0;

    //pot passar a traves de parets (configurable des del JSON)
    protected boolean travessaParets;

    //primer contacte: fins que no veu el jugador sense parets, no activa visio fantasma
    private boolean haVistJugador;

    //l'enemic nomes comenca a perseguir quant el jugador l'ha vist
    private boolean descobert;

    //per evitar que es posin uns damunt dels altres (serveix molt)
    private List<Enemic> totsEnemics;

    //nom per als logs de combat (ex: "Bubota", "Drac")
    private String nom;

    //patrons d'IA definits al JSON
    private String patroIA = "perseguir";
    private int pacmanPrevisions = 4;
    private int pacmanFlancPasses = 4;
    private boolean requereixDescobriment;
    private boolean esBoss;
    private String gameOver; //ruta al fitxer JSON de game over personalitzat (opcional)

    public void setTotsEnemics(List<Enemic> l) {
        this.totsEnemics = l;
    }

    public void setSpawn(int x, int y) {
        this.spawnX = x;
        this.spawnY = y;
    }

    public int getSpawnX() { return spawnX; }
    public int getSpawnY() { return spawnY; }

    public void setArea(int area) {
        this.area = area;
    }

    public int getArea() {
        return area;
    }

    public void setVelocitat(int v) {
        this.velocitat = Math.max(1, v);
    }

    public int getVelocitat() {
        return velocitat;
    }

    public void setTravessaParets(boolean b) {
        this.travessaParets = b;
    }

    public boolean getTravessaParets() {
        return travessaParets;
    }

    public boolean isDescobert() {
        return descobert;
    }

    public void setDescobert(boolean d) {
        this.descobert = d;
    }

    public boolean isBoss() {
        return esBoss;
    }

    //retorna la ruta al fitxer JSON de game over personalitzat, o null
    public String getGameOver() {
        return gameOver;
    }

    public String getNom() {
        return nom;
    }

    //retorna true si l'enemic pot actuar aquest torn (segons sa velocitat)
    public boolean haDActuar() {
        contadorMoviment++;
        boolean actua = contadorMoviment >= velocitat;
        if (actua) contadorMoviment = 0;
        return actua;
    }

    protected boolean dinsArea(int px, int py) {
        if (area <= 0) return true; //si no hi ha area, sempre es dins
        //quadrat simple al voltant del spawn (+-area en X i Y)
        return Math.abs(spawnX - px) <= area && Math.abs(spawnY - py) <= area;
    }

    protected double distanciaAlSpawn() {
        int dx = spawnX - this.x;
        int dy = spawnY - this.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    protected void tornaAlSpawn(char[][] cells) {
        int[] pas = primerPasBFS(spawnX, spawnY, cells);
        if (pas[0] != -1) {
            mouCapA(pas[0], pas[1], cells, null);
        }
    }

    //constructor simple: ses stats es carreguen despres amb aplicaDefinicio
    public Enemic(int x, int y, char simbol) {
        super(x, y, simbol);
        this.estatEnemic = EstatEnemic.PATRULLANT;
        this.spawnX = x;
        this.spawnY = y;
    }

    // --- IA generica segons el patro definit al JSON ---
    //cada enemic te el seu comportament: perseguir, guardia, estatic...

    public void actualitzaIA(Jugador jugador, char[][] cells) {
        switch (patroIA) {
            case "guardia" -> actualitzaIAGuardia(jugador, cells);
            case "estatic" -> {} //trampa estatica, no fa res de res
            case "pacman" -> actualitzaIAPacman(jugador, cells);
            default -> actualitzaIAPerseguir(jugador, cells);
        }
    }

    public void actualitzaIAambRadi(Jugador jugador, int radEfectiu) {
        if ("estatic".equals(patroIA)) return; //es trampa, no reacciona a res
        boolean pot = true;
        if (requereixDescobriment && !isDescobert()) pot = false;
        if (pot && distanciaAl(jugador) < radEfectiu) {
            canviaEstat(EstatEnemic.PERSEGUINT);
        } else {
            canviaEstat(EstatEnemic.PATRULLANT);
        }
    }

    private void actualitzaIAPerseguir(Jugador jugador, char[][] cells) {
        if (potPerseguir(jugador, cells)) {
            canviaEstat(EstatEnemic.PERSEGUINT);
            mouCapA(jugador.getX(), jugador.getY(), cells, jugador);
        } else {
            canviaEstat(EstatEnemic.PATRULLANT);
        }
    }

    private boolean potPerseguir(Jugador jugador, char[][] cells) {
        if (requereixDescobriment && !isDescobert()) return false;
        return distanciaAl(jugador) <= radDeteccio && potVeure(jugador, cells);
    }

    private void actualitzaIAGuardia(Jugador jugador, char[][] cells) {
        if (dinsArea(jugador.getX(), jugador.getY()) && distanciaAl(jugador) <= radDeteccio && potVeure(jugador, cells)) {
            canviaEstat(EstatEnemic.PERSEGUINT);
            int[] pas = primerPasBFS(jugador.getX(), jugador.getY(), cells);
            if (pas[0] != -1) mouCapA(pas[0], pas[1], cells, jugador);
        } else if (!dinsArea(jugador.getX(), jugador.getY()) && distanciaAlSpawn() > 1.0) {
            //el jugador ha sortit de l'area, tornam al spawn
            //aixi no s'allunyen massa de la zona de guardia
            canviaEstat(EstatEnemic.PATRULLANT);
            tornaAlSpawn(cells);
        } else {
            canviaEstat(EstatEnemic.PATRULLANT);
        }
    }

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

        //si ja ha vist es jugador, es fantasma pot atravesar parets
        boolean paretsBloquejades = !(travessaParets && haVistJugador);

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

                if (paretsBloquejades && Simbols.bloquejaVisio(cells[cy][cx])) {
                    return false;
                }
            }
        }

        if (travessaParets && !haVistJugador) {
            haVistJugador = true;
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

    //carrega totes ses propietats des de la definicio del JSON
    public void aplicaDefinicio(TipusEnemic def) {
        this.nom = def.nom;
        this.vida = def.vida;
        this.vidaMaxima = def.vida;
        this.atac = def.atac;
        this.radDeteccio = def.radi;
        this.colorDef = EinesColor.creaColor(def.colorR, def.colorG, def.colorB);
        this.artAscii = def.artAscii;
        this.artJson = def.artJson;
        setVelocitat(def.velocitat);
        setTravessaParets(def.travessaParets);
        this.patroIA = def.patroIA != null ? def.patroIA : "perseguir";
        this.pacmanPrevisions = def.pacmanPrevisions;
        this.pacmanFlancPasses = def.pacmanFlancPasses;
        this.requereixDescobriment = def.requereixDescobriment;
        this.esBoss = def.esBoss;
        this.gameOver = def.gameOver;
    }

    private void actualitzaIAPacman(Jugador jugador, char[][] cells) {
        if (!potPerseguir(jugador, cells)) {
            canviaEstat(EstatEnemic.PATRULLANT);
            return;
        }
        canviaEstat(EstatEnemic.PERSEGUINT);

        // recull tots els enemics del mateix tipus que estan perseguint, ordenats per distancia al jugador
        List<Enemic> mateixTipus = new java.util.ArrayList<>();
        if (totsEnemics != null) {
            for (Enemic e : totsEnemics) {
                if (e.isActiu() && e.lletra == this.lletra && e.estatEnemic == EstatEnemic.PERSEGUINT) {
                    mateixTipus.add(e);
                }
            }
        }
        mateixTipus.sort((a, b) -> Double.compare(a.distanciaAl(jugador), b.distanciaAl(jugador)));

        int rol = mateixTipus.indexOf(this);
        int tx, ty;

        if (rol <= 0) {
            // Blinky: va directe al jugador
            tx = jugador.getX();
            ty = jugador.getY();
        } else if (rol == 1) {
            // Pinky: preveu on estarà el jugador en N passes
            tx = jugador.getX() + jugador.getDirX() * pacmanPrevisions;
            ty = jugador.getY() + jugador.getDirY() * pacmanPrevisions;
        } else {
            // Clyde: flanqueja per la perpendicular al moviment del jugador
            int perpX = jugador.getDirY();
            int perpY = -jugador.getDirX();
            int signe = (rol % 2 == 0) ? 1 : -1;
            tx = jugador.getX() + perpX * signe * pacmanFlancPasses;
            ty = jugador.getY() + perpY * signe * pacmanFlancPasses;
        }

        // limita als bounds del mapa
        if (cells != null) {
            ty = Math.max(0, Math.min(cells.length - 1, ty));
            tx = Math.max(0, Math.min(cells[0].length - 1, tx));
        }

        int[] pas = primerPasBFS(tx, ty, cells);
        if (pas[0] != -1) {
            mouCapA(pas[0], pas[1], cells, jugador);
        } else {
            // si no arriba al punt objectiu, persegueix directe
            mouCapA(jugador.getX(), jugador.getY(), cells, jugador);
        }
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
        char c = cells[ny][nx];
        if (travessaParets) {
            if (c == '*') return false; // fantasmes no poden passar pel gel
        } else {
            if (Simbols.bloquejaMoviment(c)) return false;
        }
        if (jugador != null && nx == jugador.getX() && ny == jugador.getY()) return false;
        //que no es posin uns damunt dels altres, seria molt lleig
        if (totsEnemics != null) {
            for (Enemic altre : totsEnemics) {
                if (altre != this && altre.isActiu() && altre.getX() == nx && altre.getY() == ny) return false;
            }
        }
        return true;
    }

    protected int[] primerPasBFS(int tx, int ty, char[][] cells) {
        if (cells == null) return new int[]{tx, ty};
        int rows = cells.length, cols = rows > 0 ? cells[0].length : 0;
        int[] gCost = new int[rows * cols];
        int[] firstX = new int[rows * cols];
        int[] firstY = new int[rows * cols];
        java.util.Arrays.fill(gCost, Integer.MAX_VALUE);
        java.util.Arrays.fill(firstX, -1);
        java.util.Arrays.fill(firstY, -1);
        // PriorityQueue ordenada per f = g + h (heurística Manhattan)
        java.util.PriorityQueue<int[]> obert = new java.util.PriorityQueue<>(
            java.util.Comparator.comparingInt(a -> a[2])
        );
        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
        gCost[this.y * cols + this.x] = 0;
        for (int[] d : dirs) {
            int nx = this.x + d[0], ny = this.y + d[1];
            if (ny >= 0 && ny < rows && nx >= 0 && nx < cols && esTravessable(cells, nx, ny)) {
                int g = 1;
                int h = Math.abs(nx - tx) + Math.abs(ny - ty);
                gCost[ny * cols + nx] = g;
                firstX[ny * cols + nx] = nx;
                firstY[ny * cols + nx] = ny;
                obert.add(new int[]{nx, ny, g + h, g});
            }
        }
        while (!obert.isEmpty()) {
            int[] cur = obert.poll();
            int cx = cur[0], cy = cur[1], cg = cur[3];
            if (cg > gCost[cy * cols + cx]) continue; // nodo obsolet
            if (cx == tx && cy == ty) return new int[]{firstX[cy * cols + cx], firstY[cy * cols + cx]};
            for (int[] d : dirs) {
                int nx = cx + d[0], ny = cy + d[1];
                if (ny < 0 || ny >= rows || nx < 0 || nx >= cols) continue;
                if (!esTravessable(cells, nx, ny)) continue;
                int ng = cg + 1;
                if (ng < gCost[ny * cols + nx]) {
                    gCost[ny * cols + nx] = ng;
                    firstX[ny * cols + nx] = firstX[cy * cols + cx];
                    firstY[ny * cols + nx] = firstY[cy * cols + cx];
                    int h = Math.abs(nx - tx) + Math.abs(ny - ty);
                    obert.add(new int[]{nx, ny, ng + h, ng});
                }
            }
        }
        return new int[]{-1, -1};
    }

    private boolean esTravessable(char[][] cells, int nx, int ny) {
        if (travessaParets) {
            return cells[ny][nx] != '*';
        }
        return !Simbols.bloquejaMoviment(cells[ny][nx]);
    }

    public CeldaArt[][] getArtJson() { return artJson; }

    public String[] getArtAscii() {
        return artAscii;
    }

    public TextColor getColor() {
        return colorDef;
    }
}
