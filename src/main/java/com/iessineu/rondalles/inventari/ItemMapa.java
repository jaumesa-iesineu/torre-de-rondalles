package com.iessineu.rondalles.inventari;

public class ItemMapa { //un item que hi ha al terra del mapa

    private int x;
    private int y;
    private Item item;

    public ItemMapa(int x, int y, Item item) {
        this.x = x;
        this.y = y;
        this.item = item;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Item getItem() {
        return item;
    }
    public void setX(int x) {
        this.x=x;
    }

    public void setY(int y) {
        this.y=y;
    }
}
