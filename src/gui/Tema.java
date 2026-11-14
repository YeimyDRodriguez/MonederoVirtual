package gui;

import modelo.Rango;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/*
TEMA VISUAL. Aqui viven TODOS los colores y fuentes de la interfaz.
*/
public final class Tema {
    private Tema() {
    }

    //Paleta 
    public static final Color FONDO = new Color(0xF1F5F9); // gris muy claro del area de trabajo
    public static final Color TARJETA = Color.WHITE;
    public static final Color BORDE = new Color(0xE2E8F0);
    public static final Color TEXTO = new Color(0x0F172A);
    public static final Color TEXTO_SUAVE = new Color(0x64748B);
    public static final Color PRIMARIO = new Color(0x4F46E5); // indigo
    public static final Color PRIMARIO_OSC = new Color(0x4338CA);
    public static final Color EXITO = new Color(0x16A34A);
    public static final Color PELIGRO = new Color(0xDC2626);
    public static final Color LATERAL = new Color(0x0F172A); // menu lateral oscuro
    public static final Color LATERAL_HOVER = new Color(0x1E293B);
    public static final Color LATERAL_TEXTO = new Color(0xCBD5E1);
    public static final Color FILA_ALTERNA = new Color(0xF8FAFC);
    public static final Color SELECCION = new Color(0xE0E7FF);

    // Fuentes
    public static final String FAMILIA = elegirFamilia();
    public static final Font NORMAL = new Font(FAMILIA, Font.PLAIN, 13);
    public static final Font NEGRITA = new Font(FAMILIA, Font.BOLD, 13);
    public static final Font TITULO = new Font(FAMILIA, Font.BOLD, 20);
    public static final Font SUBTITULO = new Font(FAMILIA, Font.BOLD, 15);
    public static final Font CIFRA = new Font(FAMILIA, Font.BOLD, 18);
    public static final Font PEQUENA = new Font(FAMILIA, Font.PLAIN, 11);

    // Usa "Segoe UI" (Windows) o una parecida si existe; si no, la de Java.
    private static String elegirFamilia() {
        String[] preferidas = { "Segoe UI", "Inter", "Roboto", "Helvetica Neue", "Ubuntu", "DejaVu Sans" };
        String[] disponibles = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String p : preferidas)
            for (String d : disponibles)
                if (d.equalsIgnoreCase(p))
                    return d;
        return Font.SANS_SERIF;
    }

    // Se llama UNA vez al iniciar (en App.main), antes de crear la ventana.
    public static void aplicar() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
        } catch (Exception ignorado) {
        }

        // Colores base de Nimbus (de ellos se derivan los demas)
        UIManager.put("control", new ColorUIResource(TARJETA));
        UIManager.put("nimbusBase", new ColorUIResource(new Color(0x334155)));
        UIManager.put("nimbusBlueGrey", new ColorUIResource(new Color(0xCBD5E1)));
        UIManager.put("nimbusFocus", new ColorUIResource(PRIMARIO));
        UIManager.put("nimbusSelectionBackground", new ColorUIResource(PRIMARIO));
        UIManager.put("nimbusSelectedText", new ColorUIResource(Color.WHITE));
        UIManager.put("text", new ColorUIResource(TEXTO));
        UIManager.put("Table.alternateRowColor", new ColorUIResource(FILA_ALTERNA));
        UIManager.put("Table.background", new ColorUIResource(TARJETA));
        UIManager.put("Table.selectionBackground", new ColorUIResource(SELECCION));
        UIManager.put("Table.selectionForeground", new ColorUIResource(TEXTO));
        UIManager.put("ScrollPane.background", new ColorUIResource(TARJETA));
        UIManager.put("OptionPane.background", new ColorUIResource(TARJETA));
        UIManager.put("Panel.background", new ColorUIResource(TARJETA));
        UIManager.put("defaultFont", new FontUIResource(NORMAL));
        UIManager.put("ProgressBar.foreground", new ColorUIResource(PRIMARIO));
        UIManager.put("nimbusOrange", new ColorUIResource(PRIMARIO)); // color de la barra de progreso
    }

    // Color que identifica a cada rango.
    public static Color colorRango(Rango r) {
        switch (r) {
            case PLATA:
                return new Color(0x64748B);
            case ORO:
                return new Color(0xCA8A04);
            case PLATINO:
                return new Color(0x7C3AED);
            default:
                return new Color(0xB45309); // bronce
        }
    }

    /**
     * Una TARJETA: rectangulo blanco con borde suave, titulo y contenido.
     * Reemplaza los bordes con titulo de la version clasica.
     */
    public static JPanel tarjeta(String titulo, JComponent contenido) {
        JPanel t = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(TARJETA);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.setColor(BORDE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        t.setOpaque(false);
        t.putClientProperty("tarjeta", true);
        t.setBorder(new EmptyBorder(14, 16, 16, 16));
        if (titulo != null) {
            JLabel lbl = new JLabel(titulo);
            lbl.setFont(SUBTITULO);
            lbl.setForeground(TEXTO);
            t.add(lbl, BorderLayout.NORTH);
        }
        t.add(contenido, BorderLayout.CENTER);
        return t;
    }

    // Estilo moderno para tablas: filas altas, sin rejilla, encabezado limpio.
    public static void estilizarTabla(JTable tabla) {
        tabla.setRowHeight(30);
        tabla.setShowGrid(false);
        tabla.setIntercellSpacing(new Dimension(0, 0));
        tabla.setFont(NORMAL);
        tabla.setFillsViewportHeight(true);
        tabla.setBackground(TARJETA);
        DefaultTableCellRenderer celdas = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco, int f,
                    int c) {
                super.getTableCellRendererComponent(t, v, sel, false, f, c);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                setBackground(sel ? SELECCION : (f % 2 == 0 ? TARJETA : FILA_ALTERNA));
                setForeground(colorTexto(v));
                return this;
            }
        };
        tabla.setDefaultRenderer(Object.class, celdas);
        tabla.setDefaultRenderer(Integer.class, celdas);

        JTableHeader h = tabla.getTableHeader();
        h.setPreferredSize(new Dimension(h.getPreferredSize().width, 34));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco, int f,
                    int c) {
                super.getTableCellRendererComponent(t, v, false, false, f, c);
                setFont(NEGRITA.deriveFont(12f));
                setForeground(TEXTO_SUAVE);
                setBackground(FILA_ALTERNA);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE), new EmptyBorder(0, 10, 0, 10)));
                return this;
            }
        });
    }

    // Pequeños detalles de color: estados y montos con signo.
    private static Color colorTexto(Object v) {
        if (v == null)
            return TEXTO;
        String s = v.toString();
        if (s.equals("REVERTIDA") || s.startsWith("-"))
            return PELIGRO;
        if (s.equals("NUEVA") || s.startsWith("+"))
            return EXITO;
        return TEXTO;
    }

    // ComboBox plano: fondo blanco, borde suave y una flechita dibujada a mano.
    public static void estilizarCombo(JComboBox<?> combo) {
        combo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton b = new JButton() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(TEXTO_SUAVE);
                        int cx = getWidth() / 2, cy = getHeight() / 2;
                        g2.fillPolygon(new int[] { cx - 4, cx + 4, cx }, new int[] { cy - 2, cy - 2, cy + 3 }, 3);
                        g2.dispose();
                    }
                };
                b.setBorder(BorderFactory.createEmptyBorder());
                b.setPreferredSize(new Dimension(26, 26));
                b.setContentAreaFilled(false);
                b.setFocusPainted(false);
                return b;
            }
        });
        combo.setBackground(TARJETA);
        combo.setForeground(TEXTO);
        combo.setFont(NORMAL);
        combo.setBorder(BorderFactory.createLineBorder(BORDE));
        Dimension d = combo.getPreferredSize();
        combo.setPreferredSize(new Dimension(Math.max(d.width + 10, 105), 32));
    }

    // Icono redondo de color (logo y puntos del menu). Se dibuja, no depende de
    // fuentes.
    public static Icon punto(Color color, int tam) {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(x, y, tam, tam);
                g2.dispose();
            }

            public int getIconWidth() {
                return tam;
            }

            public int getIconHeight() {
                return tam;
            }
        };
    }

    // Logo: cuadrito redondeado con el simbolo $.
    public static Icon logo() {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(PRIMARIO);
                g2.fillRoundRect(x, y, 30, 30, 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font(FAMILIA, Font.BOLD, 18));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("$", x + (30 - fm.stringWidth("$")) / 2, y + (30 + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }

            public int getIconWidth() {
                return 30;
            }

            public int getIconHeight() {
                return 30;
            }
        };
    }

    // Scroll sin el borde gris por defecto.
    public static JScrollPane scroll(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createLineBorder(BORDE));
        sp.getViewport().setBackground(TARJETA);
        return sp;
    }

    /*
     * Recorre los componentes y vuelve transparentes los paneles "contenedores"
     * para que se vea el fondo gris y, dentro de las tarjetas, el blanco.
     */
    public static void transparentar(Component c) {
        if (c instanceof JPanel && ((JPanel) c).getClientProperty("tarjeta") == null
                && ((JPanel) c).getClientProperty("opaco") == null) {
            ((JPanel) c).setOpaque(false);
        }
        if (c instanceof JSplitPane) {
            ((JSplitPane) c).setOpaque(false);
            ((JSplitPane) c).setBorder(null);
        }
        if (c instanceof JComboBox)
            estilizarCombo((JComboBox<?>) c);
        if (c instanceof JTextField && !(c.getParent() instanceof JSpinner.DefaultEditor)) {
            ((JTextField) c).setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDE), new EmptyBorder(6, 8, 6, 8)));
        }
        if (c instanceof JScrollPane)
            return; // el contenido de los scrolls conserva su fondo blanco
        if (c instanceof Container)
            for (Component hijo : ((Container) c).getComponents())
                transparentar(hijo);
    }
}
