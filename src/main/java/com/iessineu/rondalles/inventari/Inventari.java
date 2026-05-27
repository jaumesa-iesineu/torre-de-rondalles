package com.iessineu.rondalles.inventari;

import com.iessineu.rondalles.entitats.Jugador;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author kanhai, jaume, dani, sergi, pere
 */
public class Inventari {

    private static final int PES_MAXIM = 50;

    private List<Item> items = new ArrayList<>(); //items a la motxilla (no equipats)
    private Map<Armadura.Slot, Armadura> armaduresEquipades = new EnumMap<>(Armadura.Slot.class); //un slot = una peça
    private Arma armaEquipada;

    public boolean afegeix(Item item) { //afegeix un item si hi ha pes disponible
        if (pesTotal() + item.getPes() > PES_MAXIM) return false;
        items.add(item);
        return true;
    }

    public void treu(Item item) { items.remove(item); } //treu un item de la motxilla

    public Item get(int index) { return items.get(index); } //accés per índex, útil per usar pocions

    public void elimina(int index) { items.remove(index); } //elimina per índex

    public int mida() { return items.size(); } //quants items hi ha a la motxilla

    public void equipaArmadura(Armadura armadura, Jugador jugador) { //equipa armadura i la treu de la motxilla
        Armadura anterior = armaduresEquipades.put(armadura.getSlot(), armadura);
        if (anterior != null) items.add(anterior);
        items.remove(armadura);
        recalculaDefensa(jugador);
    }

    public void equipaArma(Arma arma, Jugador jugador) { //equipa arma i la treu de la motxilla
        if (armaEquipada != null) items.add(armaEquipada);
        armaEquipada = arma;
        items.remove(arma);
        arma.aplicaEfecte(jugador);
    }

    private void recalculaDefensa(Jugador jugador) { //suma la defensa de totes les peces equipades
        int total = armaduresEquipades.values().stream().mapToInt(Armadura::getDefensa).sum();
        jugador.setDefensaExtra(total);
    }

    public int pesTotal() { //pes total: motxilla + equipament
        int pesMotxilla = items.stream().mapToInt(Item::getPes).sum();
        int pesEquipat = armaduresEquipades.values().stream().mapToInt(Item::getPes).sum();
        int pesArma = armaEquipada != null ? armaEquipada.getPes() : 0;
        return pesMotxilla + pesEquipat + pesArma;
    }

    public int penalitzacioVelocitat() { //penalització de velocitat segons el pes
        double percentatge = (double) pesTotal() / PES_MAXIM;
        if (percentatge <= 0.50) return 0;
        if (percentatge <= 0.80) return 1;
        return 2;
    }

    public List<Item> getItems() { return items; }

    public Arma getArmaEquipada() { return armaEquipada; }

    public Map<Armadura.Slot, Armadura> getArmaduresEquipades() { return armaduresEquipades; }
}
