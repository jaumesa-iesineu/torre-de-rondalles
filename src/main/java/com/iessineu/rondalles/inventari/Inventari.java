package com.iessineu.rondalles.inventari;

import java.util.ArrayList;
import java.util.List;

public class Inventari { //guarda tots els items que du el jugador
    private List<Item> items = new ArrayList<>();

    public void afegir(Item item) { items.add(item); }

    public Item get(int index) { return items.get(index); }

    public void elimina(int index) { items.remove(index); }

    public int mida() { return items.size(); }

    public List<Item> getItems() { return items; }
}
