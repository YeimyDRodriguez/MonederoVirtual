package interfaces;

import logica.SistemaMonedero;
import modelo.Cliente;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Ventana principal (version moderna).
*/
public class VentanaPrincipal extends JFrame {
    private final SistemaMonedero sistema;
    private final JComboBox<String> cmbCliente = new JComboBox<>();
    private final JLabel lblSeccion = new JLabel();
    private final JLabel valSaldo = new JLabel(), valPuntos = new JLabel(), valRango = new JLabel(), valFecha = new JLabel();
    private final CardLayout tarjetas = new CardLayout();
    private final JPanel contenido = new JPanel(tarjetas);
    private final PanelBase[] paneles;
    private final String[] nombres = {"Operaciones", "Historial e integridad", "Puntos y rangos", "Programadas",
            "Notificaciones", "Reportes", "Clientes"};
    private final ItemMenu[] items;
    private static final int INDICE_NOTIFICACIONES = 4;
    private int seleccionado = 0;
    private boolean llenando;

    public VentanaPrincipal(SistemaMonedero sistema) {
        super("Monedero Virtual con Sistema de Puntos");
        this.sistema = sistema;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);

        paneles = new PanelBase[]{
                new PanelOperaciones(sistema, this),
                new PanelHistorial(sistema, this),
                new PanelPuntos(sistema, this),
                new PanelProgramadas(sistema, this),
                new PanelNotificaciones(sistema, this),
                new PanelReportes(sistema, this),
                new PanelClientes(sistema, this)
        };
        for (int i = 0; i < paneles.length; i++) {
            Tema.transparentar(paneles[i]);
            contenido.add(paneles[i], nombres[i]);
        }

        // menu lateral
        JPanel lateral = new JPanel();
        lateral.setLayout(new BoxLayout(lateral, BoxLayout.Y_AXIS));
        lateral.setBackground(Tema.LATERAL);
        lateral.setBorder(new EmptyBorder(22, 14, 22, 14));
        lateral.setPreferredSize(new Dimension(230, 0));
        JLabel logo = new JLabel("Monedero", Tema.logo(), SwingConstants.LEFT);
        logo.setIconTextGap(10);
        logo.setFont(Tema.TITULO);
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = new JLabel("Virtual con puntos");
        sub.setFont(Tema.PEQUENA);
        sub.setForeground(Tema.LATERAL_TEXTO);
        sub.setBorder(new EmptyBorder(2, 40, 26, 0));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        lateral.add(logo);
        lateral.add(sub);
        items = new ItemMenu[nombres.length];
        for (int i = 0; i < nombres.length; i++) {
            final int indice = i;
            items[i] = new ItemMenu(nombres[i]);
            items[i].addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { mostrar(indice); }
            });
            lateral.add(items[i]);
            lateral.add(Box.createVerticalStrut(4));
        }
        lateral.add(Box.createVerticalGlue());
        JLabel pie = new JLabel("Estructuras de Datos");
        pie.setFont(Tema.PEQUENA);
        pie.setForeground(new Color(0x475569));
        pie.setAlignmentX(Component.LEFT_ALIGNMENT);
        lateral.add(pie);

        //encabezado 
        JPanel encabezado = new JPanel(new BorderLayout(0, 16));
        encabezado.setBackground(Tema.FONDO);
        encabezado.setBorder(new EmptyBorder(22, 24, 0, 24));
        JPanel filaTitulo = new JPanel(new BorderLayout());
        filaTitulo.setOpaque(false);
        lblSeccion.setFont(new Font(Tema.FAMILIA, Font.BOLD, 24));
        lblSeccion.setForeground(Tema.TEXTO);
        filaTitulo.add(lblSeccion, BorderLayout.WEST);
        JPanel selector = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        selector.setOpaque(false);
        JLabel lblCli = new JLabel("Cliente");
        lblCli.setForeground(Tema.TEXTO_SUAVE);
        Tema.estilizarCombo(cmbCliente);
        cmbCliente.setPreferredSize(new Dimension(240, 32));
        selector.add(lblCli);
        selector.add(cmbCliente);
        filaTitulo.add(selector, BorderLayout.EAST);
        encabezado.add(filaTitulo, BorderLayout.NORTH);

        JPanel indicadores = new JPanel(new GridLayout(1, 4, 16, 0));
        indicadores.setOpaque(false);
        indicadores.add(indicador("Saldo total", valSaldo));
        indicadores.add(indicador("Puntos acumulados", valPuntos));
        indicadores.add(indicador("Rango", valRango));
        indicadores.add(indicador("Fecha simulada", valFecha));
        encabezado.add(indicadores, BorderLayout.CENTER);

        JPanel derecha = new JPanel(new BorderLayout());
        derecha.setBackground(Tema.FONDO);
        derecha.add(encabezado, BorderLayout.NORTH);
        contenido.setOpaque(false);
        derecha.add(contenido, BorderLayout.CENTER);

        getContentPane().setBackground(Tema.FONDO);
        add(lateral, BorderLayout.WEST);
        add(derecha, BorderLayout.CENTER);

        cmbCliente.addActionListener(e -> { if (!llenando) refrescarTodo(); });
        mostrar(0);
        refrescarTodo();
    }

    //Tarjeta pequeña con un titulo gris y un valor grande. 
    private JPanel indicador(String titulo, JLabel valor) {
        JLabel t = new JLabel(titulo.toUpperCase());
        t.setFont(Tema.PEQUENA.deriveFont(Font.BOLD));
        t.setForeground(Tema.TEXTO_SUAVE);
        valor.setFont(Tema.CIFRA);
        valor.setForeground(Tema.TEXTO);
        JPanel cuerpo = new JPanel(new BorderLayout(0, 4));
        cuerpo.setOpaque(false);
        cuerpo.add(t, BorderLayout.NORTH);
        cuerpo.add(valor, BorderLayout.CENTER);
        JPanel tarjeta = Tema.tarjeta(null, cuerpo);
        tarjeta.setBorder(new EmptyBorder(12, 16, 12, 16));
        return tarjeta;
    }

    private void mostrar(int indice) {
        seleccionado = indice;
        tarjetas.show(contenido, nombres[indice]);
        lblSeccion.setText(nombres[indice]);
        for (int i = 0; i < items.length; i++) items[i].setSeleccionado(i == indice);
    }

    public Cliente getClienteActual() {
        String item = (String) cmbCliente.getSelectedItem();
        if (item == null) return null;
        String id = item.split(" - ")[0];
        return sistema.existeCliente(id) ? sistema.getCliente(id) : null;
    }

    public void seleccionarCliente(String id) {
        for (int i = 0; i < cmbCliente.getItemCount(); i++)
            if (cmbCliente.getItemAt(i).startsWith(id + " - ")) { cmbCliente.setSelectedIndex(i); return; }
    }

    // Recarga el combo de clientes, las tarjetas de arriba y todas las secciones. 
    public void refrescarTodo() {
        llenando = true;
        Object sel = cmbCliente.getSelectedItem();
        cmbCliente.removeAllItems();
        for (Cliente c : sistema.listarClientes()) cmbCliente.addItem(c.getId() + " - " + c.getNombre());
        if (sel != null) cmbCliente.setSelectedItem(sel);
        llenando = false;

        Cliente c = getClienteActual();
        valSaldo.setText(c == null ? "-" : String.format("$ %,.2f", c.saldoTotal()));
        valPuntos.setText(c == null ? "-" : String.format("%,d pts", c.getPuntosAcumulados()));
        valRango.setText(c == null ? "-" : "● " + c.getRango());
        valRango.setForeground(c == null ? Tema.TEXTO : Tema.colorRango(c.getRango()));
        valFecha.setText(sistema.getFechaActual().toString());
        for (PanelBase p : paneles) p.actualizar();
    }

    public void marcarNotificaciones(int noLeidas) {
        items[INDICE_NOTIFICACIONES].setInsignia(noLeidas);
    }

    // Una opcion del menu lateral: icono + texto + (opcional) contador redondo.
    private static class ItemMenu extends JPanel {
        private final JLabel texto;
        private final JLabel ico;
        private boolean seleccionado, encima;
        private int insignia;

        ItemMenu(String nombre) {
            super(new BorderLayout(10, 0));
            setOpaque(false);
            setBorder(new EmptyBorder(10, 12, 10, 12));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            ico = new JLabel(Tema.punto(new Color(0x475569), 8));
            ico.setPreferredSize(new Dimension(14, 14));
            texto = new JLabel(nombre);
            texto.setFont(Tema.NORMAL.deriveFont(14f));
            texto.setForeground(Tema.LATERAL_TEXTO);
            add(ico, BorderLayout.WEST);
            add(texto, BorderLayout.CENTER);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { encima = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { encima = false; repaint(); }
            });
        }

        void setSeleccionado(boolean s) {
            seleccionado = s;
            texto.setForeground(s ? Color.WHITE : Tema.LATERAL_TEXTO);
            texto.setFont(s ? Tema.NEGRITA.deriveFont(14f) : Tema.NORMAL.deriveFont(14f));
            ico.setIcon(Tema.punto(s ? Color.WHITE : new Color(0x475569), 8));
            repaint();
        }

        void setInsignia(int n) { insignia = n; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (seleccionado || encima) {
                g2.setColor(seleccionado ? Tema.PRIMARIO : Tema.LATERAL_HOVER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
            if (insignia > 0) {
                String s = insignia > 99 ? "99+" : String.valueOf(insignia);
                g2.setFont(Tema.PEQUENA.deriveFont(Font.BOLD));
                FontMetrics fm = g2.getFontMetrics();
                int w = Math.max(22, fm.stringWidth(s) + 12), h = 20;
                int x = getWidth() - w - 10, y = (getHeight() - h) / 2;
                g2.setColor(seleccionado ? Color.WHITE : Tema.PELIGRO);
                g2.fillRoundRect(x, y, w, h, h, h);
                g2.setColor(seleccionado ? Tema.PRIMARIO : Color.WHITE);
                g2.drawString(s, x + (w - fm.stringWidth(s)) / 2, y + (h + fm.getAscent() - fm.getDescent()) / 2);
            }
            g2.dispose();
        }
    }
}
