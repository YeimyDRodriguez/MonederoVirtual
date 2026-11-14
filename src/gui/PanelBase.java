package gui;

import logica.OperacionInvalidaException;
import logica.SistemaMonedero;
import modelo.Cliente;

import javax.swing.*;
import java.awt.*;

/** Comportamiento comun de todas las pestañas. */
public abstract class PanelBase extends JPanel {
    protected final SistemaMonedero sistema;
    protected final VentanaPrincipal ventana;

    protected PanelBase(SistemaMonedero sistema, VentanaPrincipal ventana) {
        super(new BorderLayout(10, 10));
        this.sistema = sistema;
        this.ventana = ventana;
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        setBackground(Tema.FONDO);
        putClientProperty("opaco", true); // el fondo gris del area de trabajo
    }

    /** Vuelve a leer los datos del sistema y los muestra. */
    public abstract void actualizar();

    protected Cliente clienteActual() { return ventana.getClienteActual(); }

    /**
     * Ejecuta una accion del usuario. Si el sistema lanza un error de negocio
     * (saldo insuficiente, etc.) lo mostramos en un cuadro de dialogo.
     * Al terminar, refrescamos TODAS las pestañas.
     */
    protected void ejecutar(Runnable accion, String mensajeExito) {
        try {
            if (clienteActual() == null && !(this instanceof PanelClientes)) {
                throw new OperacionInvalidaException("Primero selecciona un cliente arriba");
            }
            accion.run();
            ventana.refrescarTodo();
            if (mensajeExito != null) JOptionPane.showMessageDialog(this, mensajeExito);
        } catch (NumberFormatException e) {
            error("Escribe un numero valido (usa punto para los decimales)");
        } catch (OperacionInvalidaException | IllegalArgumentException e) {
            error(e.getMessage());
        }
    }

    protected void error(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "No se pudo completar", JOptionPane.WARNING_MESSAGE);
    }

    protected static double leerMonto(JTextField campo) {
        return Double.parseDouble(campo.getText().trim().replace(",", "."));
    }

    protected static String dinero(double v) { return String.format("$ %,.2f", v); }

    /** Agrega una fila "etiqueta: componente" a un formulario con GridBagLayout. */
    protected static void fila(JPanel form, GridBagConstraints g, int y, String etiqueta, JComponent comp) {
        g.gridy = y; g.gridx = 0; g.weightx = 0; g.fill = GridBagConstraints.NONE; g.anchor = GridBagConstraints.WEST;
        form.add(new JLabel(etiqueta), g);
        g.gridx = 1; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        form.add(comp, g);
    }

    protected static GridBagConstraints restricciones() {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 4, 6, 4);
        return g;
    }

    /** En la version moderna cada seccion es una "tarjeta" blanca (ver Tema.tarjeta). */
    protected static JPanel titulado(String titulo, JComponent contenido) {
        return Tema.tarjeta(titulo, contenido);
    }

    /** Llena un combo con los monederos del cliente, intentando conservar la seleccion. */
    protected static void llenarMonederos(JComboBox<String> combo, Cliente c) {
        Object sel = combo.getSelectedItem();
        combo.removeAllItems();
        if (c == null) return;
        for (String n : c.getNombresMonederos()) combo.addItem(n);
        if (sel != null) combo.setSelectedItem(sel);
    }
}