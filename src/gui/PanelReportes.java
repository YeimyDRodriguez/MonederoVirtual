package gui;

import logica.Reportes;
import logica.SistemaMonedero;

import javax.swing.*;
import java.awt.*;

/*
Informes de patrones de gasto, transferencias (grafos) y ranking. 
*/
public class PanelReportes extends PanelBase {
    private final JTextArea area = new JTextArea();

    public PanelReportes(SistemaMonedero s, VentanaPrincipal v) {
        super(s, v);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        area.setForeground(Tema.TEXTO);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        Boton bCliente = new Boton("Reporte del cliente");
        Boton bGeneral = Boton.secundario("Reporte general del sistema");
        botones.add(bCliente); botones.add(bGeneral);
        add(botones, BorderLayout.NORTH);
        add(Tema.tarjeta(null, Tema.scroll(area)), BorderLayout.CENTER);

        bCliente.addActionListener(e -> {
            if (clienteActual() == null) { error("Selecciona un cliente"); return; }
            mostrar(Reportes.reporteCliente(sistema, clienteActual().getId()));
        });
        bGeneral.addActionListener(e -> mostrar(Reportes.reporteGeneral(sistema)));
    }

    private void mostrar(String texto) { area.setText(texto); area.setCaretPosition(0); }

    @Override
    public void actualizar() {
        if (clienteActual() != null) mostrar(Reportes.reporteCliente(sistema, clienteActual().getId()));
    }
}
