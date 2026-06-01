package com.iessineu.rondalles.inventari;

import com.iessineu.rondalles.entitats.Jugador;
import java.util.EnumMap;
import java.util.Map;

/**
 *
 * @author kanhai, jaume, dani, sergi, pere
 */
public class Inventari {

    private static final int PES_MAXIM = 50;
    public static final int MAX_SLOTS = 4;

    public record Slot(Item item, int quantitat) {}

    // 4 slots fixos — null = buit
    private final Slot[] slots = new Slot[MAX_SLOTS];

    private Map<Armadura.Slot, Armadura> armaduresEquipades = new EnumMap<>(Armadura.Slot.class);
    private Arma armaEquipada;

    public boolean afegeix(Item item) {
        if (pesTotal() + item.getPes() > PES_MAXIM) return false;

        // si ja hi ha un slot amb el mateix tipus, apila
        for (int i = 0; i < MAX_SLOTS; i++) {
            if (slots[i] != null && slots[i].item().getNom().equals(item.getNom())) {
                slots[i] = new Slot(slots[i].item(), slots[i].quantitat() + 1);
                return true;
            }
        }
        // si no, primer slot buit
        for (int i = 0; i < MAX_SLOTS; i++) {
            if (slots[i] == null) {
                slots[i] = new Slot(item, 1);
                return true;
            }
        }
        return false; // inventari ple
    }

    // usa 1 unitat del slot (0-based). si arriba a 0 el buida
    public void elimina(int index) {
        if (index < 0 || index >= MAX_SLOTS || slots[index] == null) return;
        if (slots[index].quantitat() <= 1) {
            slots[index] = null;
        } else {
            slots[index] = new Slot(slots[index].item(), slots[index].quantitat() - 1);
        }
    }

    public Item get(int index) {
        if (index < 0 || index >= MAX_SLOTS || slots[index] == null) return null;
        return slots[index].item();
    }

    public Slot getSlot(int index) {
        return slots[index];
    }

    public int mida() {
        int count = 0;
        for (Slot s : slots) if (s != null) count++;
        return count;
    }

    public void equipaArmadura(Armadura armadura, Jugador jugador) {
        Armadura anterior = armaduresEquipades.put(armadura.getSlot(), armadura);
        if (anterior != null) afegeix(anterior);
        // treure armadura dels slots si hi és
        for (int i = 0; i < MAX_SLOTS; i++) {
            if (slots[i] != null && slots[i].item() == armadura) {
                elimina(i);
                break;
            }
        }
        recalculaDefensa(jugador);
    }

    public void equipaArma(Arma arma, Jugador jugador) {
        if (armaEquipada != null) afegeix(armaEquipada);
        armaEquipada = arma;
        for (int i = 0; i < MAX_SLOTS; i++) {
            if (slots[i] != null && slots[i].item() == arma) {
                elimina(i);
                break;
            }
        }
        arma.aplicaEfecte(jugador);
    }

    private void recalculaDefensa(Jugador jugador) {
        int total = armaduresEquipades.values().stream().mapToInt(Armadura::getDefensa).sum();
        jugador.setDefensaExtra(total);
    }

    public int pesTotal() {
        int pesMotxilla = 0;
        for (Slot s : slots) if (s != null) pesMotxilla += s.item().getPes() * s.quantitat();
        int pesEquipat = armaduresEquipades.values().stream().mapToInt(Item::getPes).sum();
        int pesArma = armaEquipada != null ? armaEquipada.getPes() : 0;
        return pesMotxilla + pesEquipat + pesArma;
    }

    public int penalitzacioVelocitat() {
        double percentatge = (double) pesTotal() / PES_MAXIM;
        if (percentatge <= 0.50) return 0;
        if (percentatge <= 0.80) return 1;
        return 2;
    }

    public Arma getArmaEquipada() { return armaEquipada; }
    public Map<Armadura.Slot, Armadura> getArmaduresEquipades() { return armaduresEquipades; }
}
