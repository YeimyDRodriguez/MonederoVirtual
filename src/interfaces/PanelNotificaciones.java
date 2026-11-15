package interfaces;

import javax.swing.*;

import logica.SistemaMonedero;
import modelo.Cliente;
import modelo.Notificacion;

import java.awt.*;

public class PanelNotificaciones extends PanelBase {
    private final TablaSoloLectura tabla = new TablaSoloLectura("Fecha", "Tipo", "Mensaje", "Estado");
    private final JLabel lblInfo = new JLabel();
    private final JTextField txtUmbral = new JTextField(8);
    private final JLabel lblRonda = new JLabel(" ");

    public PanelNotificaciones(SistemaMonedero s, VentanaPrincipal v) {
        super(s, v);
        JPanel arriba = new JPanel(new FlowLayout(FlowLayout.LEFT));
        Boton bLeer = new Boton("Marcar todas como leidas");
        Boton bSiguiente = Boton.secundario("Ver siguiente (en ronda)");
        Boton bUmbral = new Boton("Cambiar alerta de saldo bajo");
        arriba.add(lblInfo); arriba.add(bLeer); arriba.add(bSiguiente);
        arriba.add(new JLabel("Umbral:")); arriba.add(txtUmbral); arriba.add(bUmbral);
        add(arriba, BorderLayout.NORTH);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(560); // mensaje mas ancho
        add(tabla.conScroll(), BorderLayout.CENTER);
        add(lblRonda, BorderLayout.SOUTH);

        bLeer.addActionListener(e -> ejecutar(() -> {
            for (Notificacion n : clienteActual().getNotificaciones()) n.marcarLeida();
        }, null));

        bSiguiente.addActionListener(e -> {
            if (clienteActual() == null) return;
            Notificacion n = clienteActual().getNotificaciones().siguiente();
            lblRonda.setText(n == null ? "No hay notificaciones" : "En ronda: " + n);
        });
        bUmbral.addActionListener(e -> ejecutar(() ->
                sistema.cambiarUmbralSaldoBajo(clienteActual().getId(), leerMonto(txtUmbral)), "Umbral actualizado"));
    }

    @Override
    public void actualizar() {
        tabla.limpiar();
        Cliente c = clienteActual();
        if (c == null) { lblInfo.setText("Selecciona un cliente"); return; }
        txtUmbral.setText(String.valueOf(c.getUmbralSaldoBajo()));
        estructuras.Pila<Notificacion> inversa = new estructuras.Pila<>();
        int noLeidas = 0;
        for (Notificacion n : c.getNotificaciones()) { inversa.apilar(n); if (!n.isLeida()) noLeidas++; }
        for (Notificacion n : inversa)
            tabla.agregarFila(n.getFecha(), n.getTipo(), n.getMensaje(), n.isLeida() ? "Leida" : "NUEVA");
        lblInfo.setText(noLeidas + " sin leer (" + c.getNotificaciones().tamanio() + "/"
                + c.getNotificaciones().capacidad() + ")");
        ventana.marcarNotificaciones(noLeidas);
    }
}
