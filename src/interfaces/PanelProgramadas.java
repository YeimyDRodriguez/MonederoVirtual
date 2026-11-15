package interfaces;

import javax.swing.*;

import logica.SistemaMonedero;
import modelo.TransaccionProgramada;

import java.awt.*;
import java.time.LocalDate;

public class PanelProgramadas extends PanelBase {
    private final JComboBox<String> cmbTipo = new JComboBox<>(new String[]{"Deposito automatico", "Transferencia periodica"});
    private final JComboBox<String> cmbMonOrigen = new JComboBox<>();
    private final JComboBox<String> cmbDestino = new JComboBox<>();
    private final JComboBox<String> cmbMonDestino = new JComboBox<>();
    private final JTextField txtMonto = new JTextField("100", 8);
    private final JSpinner spnEnDias = new JSpinner(new SpinnerNumberModel(1, 0, 3650, 1));
    private final JSpinner spnCada = new JSpinner(new SpinnerNumberModel(30, 0, 3650, 1));
    private final JSpinner spnRepeticiones = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
    private final JCheckBox chkTodas = new JCheckBox("Ver las de todos los clientes");
    private final TablaSoloLectura tabla = new TablaSoloLectura("#", "Cliente", "Proxima fecha", "Descripcion", "Monto", "Restantes", "Ejecutadas");
    private boolean llenando;

    public PanelProgramadas(SistemaMonedero s, VentanaPrincipal v) {
        super(s, v);
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = restricciones();
        int y = 0;
        fila(form, g, y++, "Tipo:", cmbTipo);
        fila(form, g, y++, "Desde mi monedero:", cmbMonOrigen);
        fila(form, g, y++, "Cliente destino:", cmbDestino);
        fila(form, g, y++, "Monedero destino:", cmbMonDestino);
        fila(form, g, y++, "Monto:", txtMonto);
        fila(form, g, y++, "Primera ejecucion en (dias):", spnEnDias);
        fila(form, g, y++, "Repetir cada (dias, 0 = una vez):", spnCada);
        fila(form, g, y++, "Repeticiones (0 = sin limite):", spnRepeticiones);
        Boton bProgramar = new Boton("Programar");
        fila(form, g, y++, "", bProgramar);

        JPanel tiempo = new JPanel(new GridLayout(2, 1, 0, 6)); // dos filas para que quepa todo
        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tiempo.add(fila1); tiempo.add(fila2);
        Boton b1 = new Boton("Avanzar 1 dia"), b7 = Boton.secundario("Avanzar 7 dias"), b30 = Boton.secundario("Avanzar 30 dias");
        Boton bCancelar = Boton.peligro("Cancelar seleccionada");
        fila1.add(b1); fila1.add(b7); fila1.add(b30);
        fila2.add(bCancelar); fila2.add(chkTodas);
        chkTodas.setOpaque(false);

        JPanel envoltura = new JPanel(new BorderLayout());
        envoltura.add(form, BorderLayout.NORTH); // el formulario arriba, no centrado
        add(titulado("Nueva transaccion programada", envoltura), BorderLayout.WEST);
        JPanel centro = new JPanel(new BorderLayout());
        tiempo.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        centro.add(tiempo, BorderLayout.NORTH);
        centro.add(tabla.conScroll());
        add(titulado("Programadas (ordenadas por fecha: cola de prioridad)", centro), BorderLayout.CENTER);

        tabla.getColumnModel().getColumn(3).setPreferredWidth(320);
        cmbTipo.addActionListener(e -> actualizarHabilitados());
        cmbDestino.addActionListener(e -> {
            if (llenando) return;
            String id = (String) cmbDestino.getSelectedItem();
            llenarMonederos(cmbMonDestino, id == null ? null : sistema.getCliente(id));
        });
        chkTodas.addActionListener(e -> actualizar());
        bProgramar.addActionListener(e -> ejecutar(this::programar, "Transaccion programada"));
        b1.addActionListener(e -> avanzar(1));
        b7.addActionListener(e -> avanzar(7));
        b30.addActionListener(e -> avanzar(30));
        bCancelar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila < 0) { error("Selecciona una programada en la tabla"); return; }
            ejecutar(() -> sistema.cancelarProgramada((Integer) tabla.getValueAt(fila, 0)), "Programada cancelada");
        });
    }

    private void programar() {
        String id = clienteActual().getId();
        double monto = leerMonto(txtMonto);
        LocalDate fecha = sistema.getFechaActual().plusDays((Integer) spnEnDias.getValue());
        int cada = (Integer) spnCada.getValue();
        int rep = (Integer) spnRepeticiones.getValue();
        int repeticiones = rep == 0 ? -1 : rep;
        if (cmbTipo.getSelectedIndex() == 0)
            sistema.programarDeposito(id, (String) cmbMonDestino.getSelectedItem(), monto, fecha, cada, repeticiones);
        else
            sistema.programarTransferencia(id, (String) cmbMonOrigen.getSelectedItem(), (String) cmbDestino.getSelectedItem(),
                    (String) cmbMonDestino.getSelectedItem(), monto, fecha, cada, repeticiones);
    }

    private void avanzar(int dias) {
        int n = sistema.avanzarDias(dias);
        ventana.refrescarTodo();
        JOptionPane.showMessageDialog(this, "Nueva fecha: " + sistema.getFechaActual()
                + "\nTransacciones programadas ejecutadas: " + n + "\nRevisa la pestaña de notificaciones.");
    }

    private void actualizarHabilitados() {
        boolean deposito = cmbTipo.getSelectedIndex() == 0;
        cmbMonOrigen.setEnabled(!deposito);
        cmbDestino.setEnabled(!deposito);
        if (deposito && clienteActual() != null) {
            llenando = true;
            cmbDestino.setSelectedItem(clienteActual().getId());
            llenando = false;
            llenarMonederos(cmbMonDestino, clienteActual());
        }
    }

    @Override
    public void actualizar() {
        Cliente c = clienteActual();
        llenando = true;
        llenarMonederos(cmbMonOrigen, c);
        Object sel = cmbDestino.getSelectedItem();
        cmbDestino.removeAllItems();
        for (Cliente x : sistema.listarClientes()) cmbDestino.addItem(x.getId());
        if (sel != null) cmbDestino.setSelectedItem(sel);
        llenando = false;
        String id = (String) cmbDestino.getSelectedItem();
        llenarMonederos(cmbMonDestino, id == null ? null : sistema.getCliente(id));
        actualizarHabilitados();

        tabla.limpiar();
        Iterable<TransaccionProgramada> lista = (chkTodas.isSelected() || c == null)
                ? sistema.listarProgramadas() : sistema.listarProgramadas(c.getId());
        for (TransaccionProgramada p : lista)
            tabla.agregarFila(p.getId(), p.getIdCliente(), p.getProximaFecha(), p.descripcion(), dinero(p.getMonto()),
                    p.getRepeticionesRestantes() < 0 ? "Sin limite" : p.getRepeticionesRestantes(), p.getEjecutadas());
    }
}

