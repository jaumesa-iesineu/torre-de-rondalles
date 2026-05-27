package com.iessineu.rondalles.mapa;

/**
 *
 * @author jaume, dani, sergi, kanhai i pere
 */
public class Habitacio {

    private String id;
    private int x, y, w, h;

    public Habitacio(String id, int x, int y, int w, int h) {
        this.id = id;
        this.x  = x;
        this.y  = y;
        this.w  = w;
        this.h  = h;
    }

    public String getId() { return id; }
    public int getX()     { return x; }
    public int getY()     { return y; }
    public int getW()     { return w; }
    public int getH()     { return h; }
}
