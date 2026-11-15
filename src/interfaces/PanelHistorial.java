package interfaces;

import logica.SistemaMonedero;
import logica.VerificadorIntegridad;
import modelo.*;

import javax.swing.*;

import java.awt.*;

/** Historial de transacciones (lista enlazada) y verificacion de integridad (recursiva). */
public class PanelHistorial extends PanelBase {
    private final TablaSoloLectura tabla = new TablaSoloLectura(
            "#", "Fecha", "Tipo", "Detalle", "Monto", "Comision", "Puntos", "Estado");
    private final TablaSoloLectura tablaMov = new TablaSoloLectura("#", "Fecha", "Tipo", "Efecto", "Saldo", "Huella");
    private final JComboBox<String> cmbMonedero = new JComboBox<>();

    public PanelHistorial(SistemaMonedero s, VentanaPrincipal v) {
        super(s, v);
        JPanel arriba = new JPanel(new FlowLayout(FlowLayout.LEFT));
        Boton bVerificar = new Boton("Verificar integridad de este cliente");
        Boton bVerificarTodo = Boton.secundario("Verificar todo el sistema");
        arriba.add(bVerificar); arriba.add(bVerificarTodo);
        add(arriba, BorderLayout.NORTH);

        JPanel abajo = new JPanel(new BorderLayout());
        JPanel sel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sel.add(new JLabel("Monedero:")); sel.add(cmbMonedero);
        abajo.add(sel, BorderLayout.NORTH);
        abajo.add(tablaMov.conScroll());

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                titulado("Historial de transacciones", tabla.conScroll()),
                titulado("Movimientos del monedero (cadena de huellas)", abajo));
        split.setResizeWeight(0.6);
        add(split, BorderLayout.CENTER);

        cmbMonedero.addActionListener(e -> llenarMovimientos());
        bVerificar.addActionListener(e -> {
            if (clienteActual() == null) { error("Selecciona un cliente"); return; }
            mostrarResultado(VerificadorIntegridad.verificarCliente(clienteActual(), sistema));
        });
        bVerificarTodo.addActionListener(e -> mostrarResultado(VerificadorIntegridad.verificarSistema(sistema)));
    }

    private void mostrarResultado(VerificadorIntegridad.Resultado r) {
        JTextArea area = new JTextArea(r.toString(), 12, 50);
        area.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Verificacion de integridad",
                r.esValido() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void actualizar() {
        tabla.limpiar();
        Cliente c = clienteActual();
        llenarMonederos(cmbMonedero, c);
        if (c == null) return;
        for (Transaccion t : c.getHistorial()) {
            boolean recibida = !c.getId().equals(t.getTitular());
            tabla.agregarFila(t.getId(), t.getFecha(), t.getTipo(), detalle(t, c), dinero(t.getMonto()),
                    dinero(t.getComision()), recibida ? "-" : t.getPuntos(),
                    t.isRevertida() ? "REVERTIDA" : (recibida ? "Recibida" : "OK"));
        }
        llenarMovimientos();
    }

    private String detalle(Transaccion t, Cliente c) {
        switch (t.getTipo()) {
            case TRANSFERENCIA:
                return c.getId().equals(t.getClienteOrigen())
                        ? t.getMonederoOrigen() + " -> " + t.getClienteDestino() + "/" + t.getMonederoDestino()
                        : "De " + t.getClienteOrigen() + " a " + t.getMonederoDestino();
            case GASTO: return t.getCategoria() + " (" + t.getMonederoOrigen() + ")";
            default: return t.getDescripcion();
        }
    }

    private void llenarMovimientos() {
        tablaMov.limpiar();
        Cliente c = clienteActual();
        String nombre = (String) cmbMonedero.getSelectedItem();
        if (c == null || nombre == null || c.getMonedero(nombre) == null) return;
        for (Movimiento m : c.getMonedero(nombre).getMovimientos())
            tablaMov.agregarFila(m.getTransaccion().getId(), m.getTransaccion().getFecha(), m.getTransaccion().getTipo(),
                    String.format("%+.2f", m.getEfecto()), dinero(m.getSaldoResultante()), m.getHuella());
    }
}