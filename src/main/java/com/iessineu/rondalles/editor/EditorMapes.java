package com.iessineu.rondalles.editor;

import com.iessineu.rondalles.mapa.CarregadorMapa;
import com.iessineu.rondalles.mapa.Mapa;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author jaume, dani, sergi, kanhai i pere
 */
public class EditorMapes {

    private static final int MIDA = 20;

    private char[][] celles;
    private int mapW;
    private int mapH;
    private char tileActual = '.';
    private String nomMapa = "nou_mapa";

    private PanellMapa canvas;
    private JTextField campNom;

    private static Color colorTile(char c) {
        return switch (c) {
            case '#' -> new Color(80,  80,  90);
            case '.' -> new Color(55,  40,  28);
            case 'B' -> new Color(180, 180, 255); //Bubota
            case 'd' -> new Color(180, 40,  10);  //DimoniBoiet
            case 'D' -> new Color(220, 30,  30);  //Drac
            case 'G' -> new Color(200, 120, 50);  //Gegant
            case 'M' -> new Color(180, 50,  220); //NaMariaEnganxa
            case 'i' -> new Color(200, 160, 40);  //item
            case 'N' -> new Color(60,  180, 200); //npc
            default  -> new Color(25,  25,  25);
        };
    }

    private class PanellMapa extends JPanel {

        public PanellMapa() {
            setPreferredSize(new Dimension(mapW * MIDA, mapH * MIDA));
            setBackground(new Color(20, 20, 20));

            MouseAdapter ma = new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e)  { pinta(e); }
                @Override public void mouseDragged(MouseEvent e)  { pinta(e); }

                private void pinta(MouseEvent e) {
                    int tx = e.getX() / MIDA;
                    int ty = e.getY() / MIDA;
                    if (tx < 0 || tx >= mapW || ty < 0 || ty >= mapH) return;
                    celles[ty][tx] = SwingUtilities.isRightMouseButton(e) ? '#' : tileActual;
                    repaint();
                }
            };
            addMouseListener(ma);
            addMouseMotionListener(ma);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            for (int ty = 0; ty < mapH; ty++) {
                for (int tx = 0; tx < mapW; tx++) {
                    g2.setColor(colorTile(celles[ty][tx]));
                    g2.fillRect(tx * MIDA, ty * MIDA, MIDA, MIDA);
                }
            }
            g2.setColor(new Color(0, 0, 0, 60));
            for (int x = 0; x <= mapW; x++) g2.drawLine(x * MIDA, 0, x * MIDA, mapH * MIDA);
            for (int y = 0; y <= mapH; y++) g2.drawLine(0, y * MIDA, mapW * MIDA, y * MIDA);
        }
    }

    private void inicialitzaMapa() {
        celles = new char[mapH][mapW];
        for (int y = 0; y < mapH; y++)
            for (int x = 0; x < mapW; x++)
                celles[y][x] = (x == 0 || x == mapW-1 || y == 0 || y == mapH-1) ? '#' : '.';
    }

    public void mostra() {
        if (celles == null) inicialitzaMapa();
        canvas = new PanellMapa();

        JFrame frame = new JFrame("Editor de mapes — Torre de Rondalles");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout(8, 0));
        frame.getContentPane().setBackground(new Color(25, 25, 25));
        frame.add(new JScrollPane(canvas), BorderLayout.CENTER);
        frame.add(construeixSidebar(frame), BorderLayout.EAST);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel construeixSidebar(JFrame frame) {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(30, 30, 30));
        sidebar.setBorder(new EmptyBorder(12, 12, 12, 12));
        sidebar.setPreferredSize(new Dimension(190, 0));

        afegeixTitol(sidebar, "Terreny");
        afegeixTile(sidebar, '.', "Terra");
        afegeixTile(sidebar, '#', "Paret");
        sidebar.add(Box.createVerticalStrut(8));

        afegeixTitol(sidebar, "Enemics");
        afegeixTile(sidebar, 'B', "Bubota");
        afegeixTile(sidebar, 'd', "DimoniBoiet");
        afegeixTile(sidebar, 'D', "Drac");
        afegeixTile(sidebar, 'G', "Gegant");
        afegeixTile(sidebar, 'M', "NaMariaEnganxa");
        sidebar.add(Box.createVerticalStrut(8));

        afegeixTitol(sidebar, "Altres");
        afegeixTile(sidebar, 'i', "Item");
        afegeixTile(sidebar, 'N', "NPC");
        sidebar.add(Box.createVerticalStrut(12));

        //botó nou mapa
        JButton btnNou = new JButton("Nou mapa...");
        btnNou.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        btnNou.addActionListener(e -> {
            JTextField campW = new JTextField(String.valueOf(mapW), 5);
            JTextField campH = new JTextField(String.valueOf(mapH), 5);
            JPanel p = new JPanel(new GridLayout(2, 2, 6, 6));
            p.add(new JLabel("Amplada:")); p.add(campW);
            p.add(new JLabel("Alçada:"));  p.add(campH);
            if (JOptionPane.showConfirmDialog(frame, p, "Dimensions", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
            try {
                mapW = Integer.parseInt(campW.getText().trim());
                mapH = Integer.parseInt(campH.getText().trim());
                inicialitzaMapa();
                canvas.setPreferredSize(new Dimension(mapW * MIDA, mapH * MIDA));
                canvas.revalidate();
                canvas.repaint();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Mida no vàlida.");
            }
        });
        sidebar.add(btnNou);
        sidebar.add(Box.createVerticalStrut(16));

        JLabel lblNom = new JLabel("Nom del mapa:");
        lblNom.setForeground(Color.LIGHT_GRAY);
        lblNom.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lblNom);
        sidebar.add(Box.createVerticalStrut(4));
        campNom = new JTextField(nomMapa);
        campNom.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        sidebar.add(campNom);
        sidebar.add(Box.createVerticalStrut(6));

        JButton btnGuardar = new JButton("Guardar (.game)");
        btnGuardar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        btnGuardar.addActionListener(e -> guardar());
        sidebar.add(btnGuardar);
        sidebar.add(Box.createVerticalStrut(4));

        JButton btnCarregar = new JButton("Carregar (.game)");
        btnCarregar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        btnCarregar.addActionListener(e -> carregar());
        sidebar.add(btnCarregar);
        sidebar.add(Box.createVerticalGlue());

        JLabel info1 = new JLabel("Clic esq: pintar");
        JLabel info2 = new JLabel("Clic dret: paret");
        info1.setForeground(new Color(90, 90, 90));
        info2.setForeground(new Color(90, 90, 90));
        sidebar.add(info1);
        sidebar.add(info2);

        return sidebar;
    }

    private void afegeixTitol(JPanel sidebar, String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(new Color(160, 160, 160));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lbl);
        sidebar.add(Box.createVerticalStrut(3));
    }

    private void afegeixTile(JPanel sidebar, char t, String nom) {
        JPanel boto = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 3));
        boto.setBackground(new Color(40, 40, 40));
        boto.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        boto.setBorder(BorderFactory.createLineBorder(new Color(55, 55, 55)));

        JPanel mostra = new JPanel();
        mostra.setBackground(colorTile(t));
        mostra.setPreferredSize(new Dimension(16, 16));

        JLabel etiqueta = new JLabel(nom + " (" + t + ")");
        etiqueta.setForeground(Color.LIGHT_GRAY);

        boto.add(mostra);
        boto.add(etiqueta);
        boto.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                tileActual = t;
                for (Component c : sidebar.getComponents())
                    if (c instanceof JPanel p && p.getLayout() instanceof FlowLayout)
                        p.setBackground(new Color(40, 40, 40));
                boto.setBackground(new Color(70, 60, 30));
            }
        });
        sidebar.add(boto);
        sidebar.add(Box.createVerticalStrut(2));
    }

    private void guardar() {
        nomMapa = campNom.getText().trim();
        String ruta = "src/main/resources/mapes/" + nomMapa + ".game";
        try {
            CarregadorMapa.desaGameXml(new Mapa(celles, nomMapa), ruta);
            JOptionPane.showMessageDialog(null, "Guardat a " + ruta);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error guardant: " + e.getMessage());
        }
    }

    private void carregar() {
        String nom = campNom.getText().trim();
        try {
            Mapa mapa = CarregadorMapa.carrega("src/main/resources/mapes/" + nom + ".game");
            celles = mapa.getCelles();
            mapW   = mapa.getAmplada();
            mapH   = mapa.getAlcada();
            canvas.setPreferredSize(new Dimension(mapW * MIDA, mapH * MIDA));
            canvas.revalidate();
            canvas.repaint();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "No s'ha trobat: " + nom + ".game");
        }
    }

    //nou mapa buit, demana les dimensions a l'usuari
    public static void lancar() {
        SwingUtilities.invokeLater(() -> {
            JTextField campW = new JTextField("60", 5);
            JTextField campH = new JTextField("40", 5);
            JPanel p = new JPanel(new GridLayout(2, 2, 6, 6));
            p.add(new JLabel("Amplada (caselles):")); p.add(campW);
            p.add(new JLabel("Alçada (caselles):"));  p.add(campH);
            if (JOptionPane.showConfirmDialog(null, p, "Nou mapa", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
            try {
                EditorMapes editor = new EditorMapes();
                editor.mapW = Integer.parseInt(campW.getText().trim());
                editor.mapH = Integer.parseInt(campH.getText().trim());
                editor.mostra();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Mida no vàlida.");
            }
        });
    }

    //obre directament un fitxer .game existent
    public static void lancarAmbMapa(String nom) {
        SwingUtilities.invokeLater(() -> {
            try {
                Mapa mapa = CarregadorMapa.carrega("src/main/resources/mapes/" + nom + ".game");
                EditorMapes editor = new EditorMapes();
                editor.celles  = mapa.getCelles();
                editor.mapW    = mapa.getAmplada();
                editor.mapH    = mapa.getAlcada();
                editor.nomMapa = nom;
                editor.mostra();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "No s'ha pogut carregar: " + nom + ".game");
            }
        });
    }
}
