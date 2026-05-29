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

    // items al motxilla (no equipats)
    private List<Item> items = new ArrayList<>();

    // peces equipades per slot — un slot = una peça
    private Map<Armadura.Slot, Armadura> armaduresEquipades = new EnumMap<>(Armadura.Slot.class);

    // arma equipada
    private Arma armaEquipada;

    // afegeix un item a la motxilla si hi ha pes disponible
    public boolean afegeix(Item item) {
        if (pesTotal() + item.getPes() > PES_MAXIM) {
            return false;
        }
        items.add(item);
        return true;
    }

    public void treu(Item item) {
        items.remove(item);
    }

    public Item get(int index) {
        return items.get(index);
    }

    public void elimina(int index) {
        items.remove(index);
    }

    public int mida() {
        return items.size();
    }

    public void equipaArmadura(Armadura armadura, Jugador jugador) {
        Armadura anterior = armaduresEquipades.put(armadura.getSlot(), armadura);
        if (anterior != null) {
            items.add(anterior);
        }
        items.remove(armadura);
        recalculaDefensa(jugador);
    }

    // equipa una arma i la treu de la motxilla
    public void equipaArma(Arma arma, Jugador jugador) {
        if (armaEquipada != null) {
            items.add(armaEquipada);
        }
        armaEquipada = arma;
        items.remove(arma);
        arma.aplicaEfecte(jugador);
    }

    private void recalculaDefensa(Jugador jugador) {
        int total = armaduresEquipades.values().stream().mapToInt(Armadura::getDefensa).sum();
        jugador.setDefensaExtra(total);
    }

    public int pesTotal() {
        int pesMotxilla = items.stream().mapToInt(Item::getPes).sum();
        int pesEquipat = armaduresEquipades.values().stream().mapToInt(Item::getPes).sum();
        int pesArma = armaEquipada != null ? armaEquipada.getPes() : 0;
        return pesMotxilla + pesEquipat + pesArma;
    }

    public int penalitzacioVelocitat() {
        double percentatge = (double) pesTotal() / PES_MAXIM;
        if (percentatge <= 0.50) {
            return 0;
        }
        if (percentatge <= 0.80) {
            return 1;
        }
        return 2;
    }

    public record GrupItem(Item item, int quantitat, int indexPrimer) {}

    public List<GrupItem> getAgrupats() {
        List<GrupItem> grups = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            boolean trobat = false;
            for (GrupItem g : grups) {
                if (g.item().getNom().equals(item.getNom())) {
                    grups.set(grups.indexOf(g), new GrupItem(g.item(), g.quantitat() + 1, g.indexPrimer()));
                    trobat = true;
                    break;
                }
            }
            if (!trobat) grups.add(new GrupItem(item, 1, i));
        }
        return grups;
    }

    public List<Item> getItems() { return items; }

    public Arma getArmaEquipada() { return armaEquipada; }

    public Map<Armadura.Slot, Armadura> getArmaduresEquipades() { return armaduresEquipades; }
}
