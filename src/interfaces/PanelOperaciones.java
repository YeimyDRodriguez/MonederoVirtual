package interfaces;

import logica.SistemaMonedero;
import modelo.Cliente;
import modelo.Transaccion;

import javax.swing.*;
import java.awt.*;

/** Depositos, retiros, gastos, transferencias, multimonedero y reversion. */
public class PanelOperaciones extends PanelBase {
    private final JComboBox<String> cmbMonedero = new JComboBox<>();
    private final JTextField txtMonto = new JTextField(10);
    private final JTextField txtCategoria = new JTextField("Comida", 10);
    private final JComboBox<String> cmbDestinoCliente = new JComboBox<>();
    private final JComboBox<String> cmbDestinoMonedero = new JComboBox<>();
    private final JComboBox<String> cmbMonederoHacia = new JComboBox<>();
    private final JTextField txtNuevoMonedero = new JTextField(10);
    private final TablaSoloLectura tablaMonederos = new TablaSoloLectura("Monedero", "Saldo", "Movimientos");
    private final JLabel lblResumen = new JLabel();
    private boolean llenando;

    public PanelOperaciones(SistemaMonedero s, VentanaPrincipal v) {
        super(s, v);
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = restricciones();
        int y = 0;
        fila(form, g, y++, "Monedero:", cmbMonedero);
        fila(form, g, y++, "Monto:", txtMonto);

        JPanel basicas = new JPanel(new FlowLayout(FlowLayout.LEFT));
        Boton bDep = new Boton("Depositar"), bRet = new Boton("Retirar"), bSaldo = Boton.secundario("Consultar saldo");
        basicas.add(bDep); basicas.add(bRet); basicas.add(bSaldo);
        fila(form, g, y++, "", basicas);

        JPanel gasto = new JPanel(new FlowLayout(FlowLayout.LEFT));
        Boton bGasto = new Boton("Registrar gasto");
        gasto.add(new JLabel("Categoria:")); gasto.add(txtCategoria); gasto.add(bGasto);
        fila(form, g, y++, "Gasto:", gasto);

        JPanel transf = new JPanel(new FlowLayout(FlowLayout.LEFT));
        Boton bTransf = new Boton("Transferir");
        transf.add(new JLabel("A cliente:")); transf.add(cmbDestinoCliente);
        transf.add(new JLabel("monedero:")); transf.add(cmbDestinoMonedero); transf.add(bTransf);
        fila(form, g, y++, "Transferencia:", transf);

        JPanel interna = new JPanel(new FlowLayout(FlowLayout.LEFT));
        Boton bInterna = new Boton("Mover");
        interna.add(new JLabel("Hacia mi monedero:")); interna.add(cmbMonederoHacia); interna.add(bInterna);
        fila(form, g, y++, "Entre mis monederos:", interna);

        JPanel nuevo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        Boton bNuevo = new Boton("Crear monedero");
        nuevo.add(txtNuevoMonedero); nuevo.add(bNuevo);
        fila(form, g, y++, "Nuevo monedero:", nuevo);

        Boton bRevertir = Boton.peligro("Revertir ultima transaccion");
        fila(form, g, y++, "Deshacer:", bRevertir);

        JPanel izquierda = new JPanel(new BorderLayout());
        izquierda.add(titulado("Operaciones", form), BorderLayout.NORTH);
        add(izquierda, BorderLayout.CENTER);
        JPanel derecha = new JPanel(new BorderLayout());
        derecha.add(lblResumen, BorderLayout.NORTH);
        derecha.add(tablaMonederos.conScroll());
        derecha.setPreferredSize(new Dimension(300, 200));
        add(titulado("Mis monederos", derecha), BorderLayout.EAST);

        // ---- acciones ----
        bDep.addActionListener(e -> ejecutar(() -> {
            Transaccion t = sistema.depositar(id(), monedero(), leerMonto(txtMonto));
            mostrar("Deposito exitoso. Ganaste " + t.getPuntos() + " puntos.");
        }, null));
        bRet.addActionListener(e -> ejecutar(() -> {
            Transaccion t = sistema.retirar(id(), monedero(), leerMonto(txtMonto));
            mostrar("Retiro exitoso. Comision: " + dinero(t.getComision()) + ". Ganaste " + t.getPuntos() + " puntos.");
        }, null));
        bSaldo.addActionListener(e -> ejecutar(() ->
                mostrar("Saldo de '" + monedero() + "': " + dinero(sistema.consultarSaldo(id(), monedero()))
                        + "\nSaldo total: " + dinero(sistema.consultarSaldoTotal(id()))), null));
        bGasto.addActionListener(e -> ejecutar(() -> {
            Transaccion t = sistema.registrarGasto(id(), monedero(), leerMonto(txtMonto), txtCategoria.getText());
            mostrar("Gasto registrado en " + t.getCategoria() + ". Ganaste " + t.getPuntos() + " puntos.");
        }, null));
        bTransf.addActionListener(e -> ejecutar(() -> {
            Transaccion t = sistema.transferir(id(), monedero(), (String) cmbDestinoCliente.getSelectedItem(),
                    (String) cmbDestinoMonedero.getSelectedItem(), leerMonto(txtMonto));
            mostrar("Transferencia exitosa. Comision: " + dinero(t.getComision()) + ". Ganaste " + t.getPuntos() + " puntos.");
        }, null));
        bInterna.addActionListener(e -> ejecutar(() ->
                sistema.transferirEntreMonederos(id(), monedero(), (String) cmbMonederoHacia.getSelectedItem(),
                        leerMonto(txtMonto)), "Dinero movido entre tus monederos"));
        bNuevo.addActionListener(e -> ejecutar(() -> {
            sistema.crearMonedero(id(), txtNuevoMonedero.getText());
            txtNuevoMonedero.setText("");
        }, "Monedero creado"));
        bRevertir.addActionListener(e -> {
            if (clienteActual() == null || clienteActual().getReversibles().estaVacia()) {
                error("No hay transacciones para revertir"); return;
            }
            Transaccion cima = clienteActual().getReversibles().verCima();
            int r = JOptionPane.showConfirmDialog(this, "¿Revertir " + cima + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION)
                ejecutar(() -> sistema.revertirUltima(id()), "Transaccion revertida. Los puntos se recalcularon.");
        });
        cmbDestinoCliente.addActionListener(e -> {
            if (llenando) return;
            String idDest = (String) cmbDestinoCliente.getSelectedItem();
            llenarMonederos(cmbDestinoMonedero, idDest == null ? null : sistema.getCliente(idDest));
        });
    }

    private String id() { return clienteActual().getId(); }
    private String monedero() { return (String) cmbMonedero.getSelectedItem(); }
    private void mostrar(String m) { JOptionPane.showMessageDialog(this, m); }

    @Override
    public void actualizar() {
        Cliente c = clienteActual();
        llenando = true;
        llenarMonederos(cmbMonedero, c);
        llenarMonederos(cmbMonederoHacia, c);
        Object sel = cmbDestinoCliente.getSelectedItem();
        cmbDestinoCliente.removeAllItems();
        for (Cliente otro : sistema.listarClientes())
            if (c == null || otro != c) cmbDestinoCliente.addItem(otro.getId());
        if (sel != null) cmbDestinoCliente.setSelectedItem(sel);
        llenando = false;
        String idDest = (String) cmbDestinoCliente.getSelectedItem();
        llenarMonederos(cmbDestinoMonedero, idDest == null ? null : sistema.getCliente(idDest));

        tablaMonederos.limpiar();
        if (c == null) { lblResumen.setText("Selecciona un cliente"); return; }
        for (String n : c.getNombresMonederos())
            tablaMonederos.agregarFila(n, dinero(c.getMonedero(n).getSaldo()), c.getMonedero(n).getMovimientos().tamanio());
        lblResumen.setText("<html>Saldo total: <b>" + dinero(c.saldoTotal()) + "</b><br>Rango: <b>" + c.getRango()
                + "</b> — " + c.getRango().descripcionBeneficios() + "<br>Transacciones reversibles en la pila: "
                + c.getReversibles().tamanio() + "</html>");
    }
}
