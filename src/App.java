
import interfaces.Tema;
import interfaces.VentanaPrincipal;
import logica.DatosDemo;
import logica.SistemaMonedero;

import javax.swing.*;

/**
 * PUNTO DE ENTRADA. Ejecuta este archivo (boton "Run" en VS Code).
 * Crea el sistema, carga datos de ejemplo y abre la ventana.
 */
public class App {
    public static void main(String[] args) {
        SistemaMonedero sistema = new SistemaMonedero();
        DatosDemo.cargar(sistema); // comenta esta linea si quieres empezar sin datos

        SwingUtilities.invokeLater(() -> {
            Tema.aplicar(); // colores, fuentes y estilo moderno (ver gui/Tema.java)
            new VentanaPrincipal(sistema).setVisible(true);
        });
    }
}