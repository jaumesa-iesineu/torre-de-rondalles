package com.iessineu.rondalles.joc;

import com.iessineu.rondalles.entitats.Jugador;
import com.iessineu.rondalles.inventari.Inventari;
import com.iessineu.rondalles.inventari.Item;
import com.iessineu.rondalles.inventari.RegistreItems;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;

//gestiona el guardat i la càrrega de partides en format binari (.save)
public class GestorPartida {

    private static final String FITXER = "partida.save";
    private static final byte[] MAGIC = {'S', 'A', 'V', 'E'};
    private static final byte VERSION = 1;

    public static void desa(Joc joc) {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(FITXER))) {
            dos.write(MAGIC);
            dos.writeByte(VERSION);

            //mapa actual
            byte[] idBytes = ((String) joc.idMapaActual).getBytes(StandardCharsets.UTF_8);
            dos.writeShort(idBytes.length);
            dos.write(idBytes);

            //posició i stats del jugador
            Jugador j = joc.jugado;
            dos.writeShort(j.getX());
            dos.writeShort(j.getY());
            dos.writeShort(j.getVida());
            dos.writeShort(j.getVidaMaxima());
            dos.writeShort(j.getAtac());
            dos.writeShort(j.getDefensa());
            dos.writeByte(j.getTornsVeri());
            dos.writeByte(j.getTornsFoc());
            dos.writeByte(j.getTornsGel());

            //inventari (sempre 4 slots)
            dos.writeByte(Inventari.MAX_SLOTS);
            for (int i = 0; i < Inventari.MAX_SLOTS; i++) {
                Inventari.Slot slot = j.getInventari().getSlot(i);
                if (slot == null) {
                    dos.writeByte(0); //built
                } else {
                    dos.writeByte(1); //ple
                    byte[] nomBytes = slot.item().getNom().getBytes(StandardCharsets.UTF_8);
                    dos.writeShort(nomBytes.length);
                    dos.write(nomBytes);
                    dos.writeShort(slot.quantitat());
                }
            }

            //enemics morts (llista de posicions originals)
            dos.writeShort(((DataOutputStream) joc.enemicsMorts).size());
            for (int[] pos : joc.enemicsMorts) {
                dos.writeShort(pos[0]);
                dos.writeShort(pos[1]);
            }

            //fog of war: dimensions + flags explorat
            int alcada = joc.explorat.length;
            int amplada = alcada > 0 ? joc.explorat[0].length : 0;
            dos.writeShort(amplada);
            dos.writeShort(alcada);
            for (boolean[] fila : joc.explorat)
                for (boolean cel : fila)
                    dos.writeByte(cel ? 1 : 0);

        } catch (Exception e) {
            System.err.println("Error desant partida: " + e.getMessage());
        }
    }

    public static boolean existeix() {
        return new File(FITXER).exists();
    }

    //quan el jugador perd, esborra la partida guardada
    public static void esborra() {
        new File(FITXER).delete();
    }

    public static void carrega(Joc joc) {
        if (!existeix()) return;
        try (DataInputStream dis = new DataInputStream(new FileInputStream(FITXER))) {
            //validar magic bytes
            for (byte b : MAGIC)
                if (dis.readByte() != b) throw new RuntimeException("Fitxer de partida invàlid");
            if (dis.readByte() != VERSION) throw new RuntimeException("Versió de partida incompatible");

            //mapa
            int idLen = dis.readShort();
            byte[] idBytes = new byte[idLen];
            dis.readFully(idBytes);
            joc.idMapaActual = new String(idBytes, StandardCharsets.UTF_8);

            //posició i stats
            Jugador j = joc.jugador;
            j.setX(dis.readShort());
            j.setY(dis.readShort());
            j.setVida(dis.readShort());
            j.setVidaMaxima(dis.readShort());
            j.setAtac(dis.readShort());
            j.setDefensa(dis.readShort());
            j.setTornsVeri(dis.readByte());
            j.setTornsFoc(dis.readByte());
            j.setTornsGel(dis.readByte());

            //inventari
            int numSlots = dis.readByte();
            j.getInventari().buidaSlots();
            for (int i = 0; i < numSlots; i++) {
                int ple = dis.readByte();
                if (ple == 1) {
                    int nomLen = dis.readShort();
                    byte[] nomBytes = new byte[nomLen];
                    dis.readFully(nomBytes);
                    String nom = new String(nomBytes, StandardCharsets.UTF_8);
                    int quantitat = dis.readShort();
                    Item item = RegistreItems.get().itemPerNom(nom);
                    if (item != null)
                        for (int q = 0; q < quantitat; q++) j.afegeixItem(item);
                }
            }

            //enemics morts
            joc.enemicsMorts.clear();
            int numMorts = dis.readShort();
            for (int i = 0; i < numMorts; i++) {
                int ex = dis.readShort();
                int ey = dis.readShort();
                joc.enemicsMorts.add(new int[]{ex, ey});
                //elimina l'enemic viu que estigui a aquella posició (si n'hi ha)
                joc.enemics.removeIf(e -> e.getX() == ex && e.getY() == ey);
            }

            //fog of war
            int amplada = dis.readShort();
            int alcada  = dis.readShort();
            if (joc.explorat != null && joc.explorat.length == alcada
                    && alcada > 0 && joc.explorat[0].length == amplada) {
                for (int y = 0; y < alcada; y++)
                    for (int x = 0; x < amplada; x++)
                        joc.explorat[y][x] = dis.readByte() == 1;
            }

        } catch (Exception e) {
            System.err.println("Error carregant partida: " + e.getMessage());
        }
    }
}
