package com.iessineu.rondalles.entitats;

import com.googlecode.lanterna.TextColor;

/**
 *
 * @author kanhai, jaume, dani, sergi, pere
 */
public class NaMariaEnganxa extends Enemic { // trampa estàtica — no es mou, mata instantàniament

    public NaMariaEnganxa(int x, int y) { // constructor de la classe NaMariaEnganxa
        super(x, y, 'M', 30, 9999, 0); // atac 9999 = kill instantani, radDeteccio 0 = no detecta
        this.lletra='M';
    }

  @Override
public void actualitzaIAambRadi(Jugador jugador, int radEfectiu) {

    if (distanciaAl(jugador) < radEfectiu) {
        canviaEstat(EstatEnemic.PERSEGUINT);
    } else {
        canviaEstat(EstatEnemic.PATRULLANT);
    }

}
    @Override
    public TextColor getColor() { // lila
        return new TextColor.RGB(180, 50, 220);
    }

}
