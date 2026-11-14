package interfaces;

import logica.SistemaMonedero;
import modelo.Cliente;

import javax.swing.*;

import java.awt.*;

/** Registrar clientes y ver la lista (ordenada por id gracias al ABB). */
public class PanelClientes extends PanelBase {
    private final TablaSoloLectura tabla = new TablaSoloLectura("Id", "Nombre", "Saldo total", "Puntos", "Rango", "Monederos");
    private final JTextField txtId = new JTextField(10);
    private final JTextField txtNombre = new JTextField(18);

    public PanelClientes(SistemaMonedero s, VentanaPrincipal v) {
        super(s, v);
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(new JLabel("Id:")); form.add(txtId);
        form.add(new JLabel("Nombre:")); form.add(txtNombre);
        Boton btn = new Boton("Registrar cliente");
        form.add(btn);
        btn.addActionListener(e -> ejecutar(() -> {
            Cliente c = sistema.registrarCliente(txtId.getText(), txtNombre.getText());
            txtId.setText(""); txtNombre.setText("");
            ventana.seleccionarCliente(c.getId());
        }, "Cliente registrado"));

        add(titulado("Nuevo cliente", form), BorderLayout.NORTH);
        add(titulado("Clientes (doble clic para seleccionarlo)", tabla.conScroll()), BorderLayout.CENTER);
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && tabla.getSelectedRow() >= 0)
                    ventana.seleccionarCliente((String) tabla.getValueAt(tabla.getSelectedRow(), 0));
            }
        });
    }

    @Override
    public void actualizar() {
        tabla.limpiar();
        for (Cliente c : sistema.listarClientes())
            tabla.agregarFila(c.getId(), c.getNombre(), dinero(c.saldoTotal()), c.getPuntosAcumulados(),
                    c.getRango(), c.getNombresMonederos().tamanio());
    }
}