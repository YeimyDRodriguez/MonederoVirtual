package app;
import javax.swing.SwingUtilities;

import gui.Tema;

public class App {
	public static void main (String[] args) {
		SistemaMonedero sistema = new SistemaMonedero();
		DatosDemo.cargar(sistema); // se puede comentar esta linea si queremos que la aplicación no tenga datos cargados
		SwingUtilities.invokeLater(() -> {
			Tema.aplicar();
			new VentanaPrincipal(sistema).setVisible(true);
		});
	}
}
