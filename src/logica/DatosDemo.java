package logica;

/** Carga clientes y movimientos de ejemplo para que la aplicacion no arranque vacia. */
public class DatosDemo {
    private DatosDemo() {}

    public static void cargar(SistemaMonedero s) {
        s.registrarCliente("C001", "Ana Gomez");
        s.registrarCliente("C002", "Luis Perez");
        s.registrarCliente("C003", "Sofia Martinez");
        s.registrarCliente("C004", "Carlos Ruiz");

        s.crearMonedero("C001", "Ahorros");
        s.crearMonedero("C001", "Gastos diarios");
        s.crearMonedero("C002", "Ahorros");

        s.depositar("C001", "Principal", 120000); // 1200 pts -> Oro
        s.depositar("C002", "Principal", 55000);  // 550 pts -> Plata
        s.depositar("C003", "Principal", 3000);
        s.depositar("C004", "Principal", 500);

        s.transferirEntreMonederos("C001", "Principal", "Ahorros", 5000);
        s.transferirEntreMonederos("C001", "Principal", "Gastos diarios", 2000);
        s.transferirEntreMonederos("C001", "Ahorros", "Gastos diarios", 500);

        s.registrarGasto("C001", "Gastos diarios", 300, "Comida");
        s.registrarGasto("C001", "Gastos diarios", 120, "Transporte");
        s.registrarGasto("C001", "Gastos diarios", 250, "Comida");
        s.registrarGasto("C001", "Gastos diarios", 400, "Entretenimiento");
        s.registrarGasto("C001", "Gastos diarios", 180, "Comida");
        s.registrarGasto("C002", "Principal", 600, "Servicios");
        s.registrarGasto("C002", "Principal", 200, "Comida");

        s.transferir("C001", "Principal", "C002", "Principal", 1500);
        s.transferir("C002", "Principal", "C003", "Principal", 700);
        s.transferir("C003", "Principal", "C001", "Principal", 200);
        s.transferir("C001", "Principal", "C004", "Principal", 300);

        s.retirar("C002", "Principal", 400);
        s.retirar("C001", "Principal", 1000);

        s.programarDeposito("C003", "Principal", 1200, s.getFechaActual().plusDays(2), 30, -1);
        s.programarTransferencia("C001", "Principal", "C001", "Ahorros", 1000, s.getFechaActual().plusDays(1), 7, 4);
        s.programarTransferencia("C002", "Principal", "C004", "Principal", 250, s.getFechaActual().plusDays(3), 15, -1);
    }
}