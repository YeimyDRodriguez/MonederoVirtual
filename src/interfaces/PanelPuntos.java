package interfaces;

import modelo.Beneficio;
import modelo.Cliente;
import modelo.Rango;
import modelo.RegistroPuntos;

import javax.swing.*;

import logica.SistemaMonedero;

import java.awt.*;

public class PanelPuntos extends PanelBase {
    private final JLabel lblInfo = new JLabel();
    private final JProgressBar barra = new JProgressBar();
    private final TablaSoloLectura tablaHistorial = new TablaSoloLectura("Fecha", "Cambio", "Motivo", "Acumulados");
    private final TablaSoloLectura tablaRanking = new TablaSoloLectura("Pos", "Cliente", "Puntos", "Rango");
    private final TablaSoloLectura tablaRangos = new TablaSoloLectura("Rango", "Puntos", "Beneficios", "Clientes");

    public PanelPuntos(SistemaMonedero s, VentanaPrincipal v) {
        super(s, v);
        JPanel arriba = new JPanel(new BorderLayout(8, 8));
        lblInfo.setFont(Tema.NORMAL.deriveFont(14f));
        arriba.add(lblInfo, BorderLayout.NORTH);
        barra.setStringPainted(true);
        barra.setPreferredSize(new Dimension(0, 26));
        arriba.add(barra, BorderLayout.CENTER);

        JPanel canjes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (Beneficio b : Beneficio.values()) {
            Boton btn = new Boton("Canjear " + b.getCosto() + " pts");
            btn.setToolTipText(b.getDescripcion());
            btn.addActionListener(e -> ejecutar(() -> sistema.canjear(clienteActual().getId(), b),
                    "Canjeaste: " + b.getDescripcion()));
            canjes.add(btn);
        }
        JPanel listaBenef = new JPanel(new GridLayout(0, 1));
        for (Beneficio b : Beneficio.values()) listaBenef.add(new JLabel("• " + b));
        JPanel panelCanje = new JPanel(new BorderLayout());
        panelCanje.add(listaBenef, BorderLayout.NORTH);
        panelCanje.add(canjes, BorderLayout.CENTER);
        arriba.add(titulado("Canjear beneficios", panelCanje), BorderLayout.SOUTH);
        add(arriba, BorderLayout.NORTH);

        // Selector moderno: tres botones tipo "chip" + CardLayout (muestra una tabla a la vez)
        CardLayout cartas = new CardLayout();
        JPanel vistas = new JPanel(cartas);
        String[] titulos = {"Mi historial de puntos", "Ranking (arbol AVL)", "Rangos y beneficios"};
        JComponent[] tablas = {tablaHistorial.conScroll(), tablaRanking.conScroll(), tablaRangos.conScroll()};
        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        Boton[] botones = new Boton[titulos.length];
        for (int i = 0; i < titulos.length; i++) {
            final int k = i;
            vistas.add(tablas[i], titulos[i]);
            botones[i] = i == 0 ? new Boton(titulos[i]) : Boton.secundario(titulos[i]);
            botones[i].addActionListener(e -> {
                cartas.show(vistas, titulos[k]);
                for (int j = 0; j < botones.length; j++)
                    botones[j].setEstilo(j == k ? Boton.Estilo.PRIMARIO : Boton.Estilo.SECUNDARIO);
            });
            chips.add(botones[i]);
            chips.add(Box.createHorizontalStrut(8));
        }
        JPanel abajo = new JPanel(new BorderLayout(0, 10));
        abajo.add(chips, BorderLayout.NORTH);
        abajo.add(vistas, BorderLayout.CENTER);
        add(Tema.tarjeta(null, abajo), BorderLayout.CENTER);
    }

    @Override
    public void actualizar() {
        tablaRanking.limpiar();
        int pos = 1;
        for (Cliente c : sistema.ranking())
            tablaRanking.agregarFila(pos++, c.getNombre(), c.getPuntosAcumulados(), c.getRango());

        tablaRangos.limpiar();
        for (Rango r : Rango.values()) {
            String puntos = r == Rango.PLATINO ? "Mas de 5000" : r.getMinimo() + " - " + r.getMaximo();
            tablaRangos.agregarFila(r, puntos, r.descripcionBeneficios(), sistema.clientesDeRango(r).tamanio());
        }

        tablaHistorial.limpiar();
        Cliente c = clienteActual();
        if (c == null) { lblInfo.setText("Selecciona un cliente"); return; }
        for (RegistroPuntos r : c.getHistorialPuntos())
            tablaHistorial.agregarFila(r.getFecha(), String.format("%+d", r.getCambio()), r.getMotivo(), r.getAcumuladosDespues());

        String extras = "";
        if (c.getTransferenciasConDescuento() > 0)
            extras += "<br>Transferencias con 10% de descuento restantes: " + c.getTransferenciasConDescuento();
        if (c.tieneRetirosGratis(sistema.getFechaActual()))
            extras += "<br>Retiros sin cargo hasta: " + c.getRetirosGratisHasta();
        lblInfo.setText("<html>Puntos acumulados: <b>" + c.getPuntosAcumulados() + "</b> &nbsp; | &nbsp; Disponibles para canjear: <b>"
                + c.getPuntosDisponibles() + "</b> &nbsp; | &nbsp; Rango: <b>" + c.getRango() + "</b>" + extras + "</html>");

        Rango r = c.getRango();
        if (r == Rango.PLATINO) {
            barra.setMinimum(0); barra.setMaximum(1); barra.setValue(1);
            barra.setString("Rango maximo alcanzado");
        } else {
            Rango sig = Rango.values()[r.ordinal() + 1];
            barra.setMinimum(r.getMinimo()); barra.setMaximum(sig.getMinimo());
            barra.setValue(c.getPuntosAcumulados());
            barra.setString((sig.getMinimo() - c.getPuntosAcumulados()) + " puntos para llegar a " + sig);
        }
    }
}

