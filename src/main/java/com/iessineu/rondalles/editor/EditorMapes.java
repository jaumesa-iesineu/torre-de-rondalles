package com.iessineu.rondalles.editor;

import com.iessineu.rondalles.mapa.CarregadorMapa;
import com.iessineu.rondalles.mapa.Habitacio;
import com.iessineu.rondalles.mapa.Mapa;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
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

    //mode habitació: el clic+drag defineix un rectangle en comptes de pintar tiles
    private boolean modeHabitacio = false;
    private List<Habitacio> habitacions = new ArrayList<>();

    //rectangle temporal mentre l'usuari arrossega per definir una habitació
    private Point ptInici = null;
    private Rectangle rectTemp = null;

    private PanellMapa canvas;
    private JTextField campNom;
    private JPanel panellLlistaHabs;
    private JButton btnModeHab;

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
                @Override public void mousePressed(MouseEvent e)  { gestiona(e, true);  }
                @Override public void mouseDragged(MouseEvent e)  { gestiona(e, false); }
                @Override public void mouseReleased(MouseEvent e) { allibera(e);        }

                private void gestiona(MouseEvent e, boolean primerClic) {
                    if (modeHabitacio) {
                        int tx = clamp(e.getX() / MIDA, 0, mapW - 1);
                        int ty = clamp(e.getY() / MIDA, 0, mapH - 1);
                        if (primerClic) ptInici = new Point(tx, ty);
                        if (ptInici != null) {
                            int rx = Math.min(ptInici.x, tx);
                            int ry = Math.min(ptInici.y, ty);
                            int rw = Math.abs(tx - ptInici.x) + 1;
                            int rh = Math.abs(ty - ptInici.y) + 1;
                            rectTemp = new Rectangle(rx, ry, rw, rh);
                            repaint();
                        }
                        return;
                    }
                    int tx = e.getX() / MIDA;
                    int ty = e.getY() / MIDA;
                    if (tx < 0 || tx >= mapW || ty < 0 || ty >= mapH) return;
                    celles[ty][tx] = SwingUtilities.isRightMouseButton(e) ? '#' : tileActual;
                    repaint();
                }

                private void allibera(MouseEvent e) {
                    if (!modeHabitacio || rectTemp == null) return;
                    if (rectTemp.width > 1 || rectTemp.height > 1) {
                        String id = JOptionPane.showInputDialog(PanellMapa.this,
                            "Nom de l'habitació:", "hab" + (habitacions.size() + 1));
                        if (id != null && !id.isBlank()) {
                            habitacions.add(new Habitacio(id.trim(), rectTemp.x, rectTemp.y, rectTemp.width, rectTemp.height));
                            actualitzaLlistaHabitacions();
                        }
                    }
                    rectTemp = null;
                    ptInici  = null;
                    repaint();
                }
            };
            addMouseListener(ma);
            addMouseMotionListener(ma);
        }

        private int clamp(int v, int min, int max) {
            return Math.max(min, Math.min(max, v));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            //tiles
            for (int ty = 0; ty < mapH; ty++)
                for (int tx = 0; tx < mapW; tx++) {
                    g2.setColor(colorTile(celles[ty][tx]));
                    g2.fillRect(tx * MIDA, ty * MIDA, MIDA, MIDA);
                }

            //graella
            g2.setColor(new Color(0, 0, 0, 60));
            g2.setStroke(new BasicStroke(1));
            for (int x = 0; x <= mapW; x++) g2.drawLine(x * MIDA, 0, x * MIDA, mapH * MIDA);
            for (int y = 0; y <= mapH; y++) g2.drawLine(0, y * MIDA, mapW * MIDA, y * MIDA);

            //habitacions definides
            g2.setFont(new Font("Monospaced", Font.BOLD, 11));
            for (Habitacio hab : habitacions) {
                g2.setColor(new Color(100, 200, 255, 50));
                g2.fillRect(hab.x * MIDA, hab.y * MIDA, hab.w * MIDA, hab.h * MIDA);
                g2.setStroke(new BasicStroke(2));
                g2.setColor(new Color(100, 200, 255));
                g2.drawRect(hab.x * MIDA, hab.y * MIDA, hab.w * MIDA - 1, hab.h * MIDA - 1);
                g2.setColor(Color.WHITE);
                g2.drawString(hab.id, hab.x * MIDA + 3, hab.y * MIDA + 13);
            }

            //rectangle temporal mentre arrossegam
            if (rectTemp != null) {
                g2.setColor(new Color(255, 200, 50, 50));
                g2.fillRect(rectTemp.x * MIDA, rectTemp.y * MIDA, rectTemp.width * MIDA, rectTemp.height * MIDA);
                float[] guions = {5};
                g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, guions, 0));
                g2.setColor(new Color(255, 200, 50));
                g2.drawRect(rectTemp.x * MIDA, rectTemp.y * MIDA, rectTemp.width * MIDA - 1, rectTemp.height * MIDA - 1);
            }
        }
    }

    private void inicialitzaMapa() {
        celles = new char[mapH][mapW];
        for (int y = 0; y < mapH; y++)
            for (int x = 0; x < mapW; x++)
                celles[y][x] = (x == 0 || x == mapW - 1 || y == 0 || y == mapH - 1) ? '#' : '.';
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

        //si ja hi havia habitacions carregades, les mostram al sidebar
        actualitzaLlistaHabitacions();
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
                habitacions.clear();
                actualitzaLlistaHabitacions();
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
        sidebar.add(Box.createVerticalStrut(12));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(60, 60, 60));
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(8));

        afegeixTitol(sidebar, "Habitacions");

        btnModeHab = new JButton("Definir habitació");
        btnModeHab.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        btnModeHab.addActionListener(e -> {
            modeHabitacio = !modeHabitacio;
            btnModeHab.setBackground(modeHabitacio ? new Color(80, 60, 20) : null);
            btnModeHab.setText(modeHabitacio ? "● Dibuixant..." : "Definir habitació");
        });
        sidebar.add(btnModeHab);
        sidebar.add(Box.createVerticalStrut(6));

        panellLlistaHabs = new JPanel();
        panellLlistaHabs.setLayout(new BoxLayout(panellLlistaHabs, BoxLayout.Y_AXIS));
        panellLlistaHabs.setBackground(new Color(30, 30, 30));
        panellLlistaHabs.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(panellLlistaHabs);

        sidebar.add(Box.createVerticalGlue());

        JLabel info1 = new JLabel("Clic esq: pintar");
        JLabel info2 = new JLabel("Clic dret: paret");
        info1.setForeground(new Color(90, 90, 90));
        info2.setForeground(new Color(90, 90, 90));
        sidebar.add(info1);
        sidebar.add(info2);

        return sidebar;
    }

    private void actualitzaLlistaHabitacions() {
        if (panellLlistaHabs == null) return;
        panellLlistaHabs.removeAll();
        for (int i = 0; i < habitacions.size(); i++) {
            Habitacio hab = habitacions.get(i);
            final int idx = i;
            JPanel fila = new JPanel(new BorderLayout(4, 0));
            fila.setBackground(new Color(40, 40, 40));
            fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
            JLabel lbl = new JLabel(hab.id + " (" + hab.w + "×" + hab.h + ")");
            lbl.setForeground(new Color(100, 200, 255));
            lbl.setFont(lbl.getFont().deriveFont(10f));
            JButton btnX = new JButton("✕");
            btnX.setPreferredSize(new Dimension(20, 20));
            btnX.setFont(btnX.getFont().deriveFont(9f));
            btnX.addActionListener(e -> {
                habitacions.remove(idx);
                actualitzaLlistaHabitacions();
                canvas.repaint();
            });
            fila.add(lbl,  BorderLayout.CENTER);
            fila.add(btnX, BorderLayout.EAST);
            panellLlistaHabs.add(fila);
            panellLlistaHabs.add(Box.createVerticalStrut(2));
        }
        panellLlistaHabs.revalidate();
        panellLlistaHabs.repaint();
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
                modeHabitacio = false;
                if (btnModeHab != null) {
                    btnModeHab.setBackground(null);
                    btnModeHab.setText("Definir habitació");
                }
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
            if (habitacions.isEmpty()) {
                CarregadorMapa.desaGameXml(new Mapa(celles, nomMapa), ruta);
            } else {
                CarregadorMapa.desaGameXmlAmbHabitacions(habitacions, celles, nomMapa, ruta);
            }
            JOptionPane.showMessageDialog(null, "Guardat a " + ruta);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error guardant: " + e.getMessage());
        }
    }

    private void carregar() {
        String nom = campNom.getText().trim();
        String ruta = "src/main/resources/mapes/" + nom + ".game";
        try {
            Mapa mapa = CarregadorMapa.carrega(ruta);
            celles = mapa.getCelles();
            mapW   = mapa.getAmplada();
            mapH   = mapa.getAlcada();
            habitacions.clear();
            habitacions.addAll(CarregadorMapa.carregaHabitacionsDefinides(ruta));
            actualitzaLlistaHabitacions();
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
            String ruta = "src/main/resources/mapes/" + nom + ".game";
            try {
                Mapa mapa = CarregadorMapa.carrega(ruta);
                EditorMapes editor = new EditorMapes();
                editor.celles  = mapa.getCelles();
                editor.mapW    = mapa.getAmplada();
                editor.mapH    = mapa.getAlcada();
                editor.nomMapa = nom;
                editor.habitacions.addAll(CarregadorMapa.carregaHabitacionsDefinides(ruta));
                editor.mostra();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "No s'ha pogut carregar: " + nom + ".game");
            }
        });
    }
}
