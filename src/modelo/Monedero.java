package modelo;

import estructuras.ListaEnlazada;

/** Un monedero (ej. "Principal", "Ahorros", "Gastos diarios") dentro de la cuenta de un cliente. */
public class Monedero {
    public static final String HUELLA_INICIAL = "INICIO";

    private final String nombre;
    private double saldo;
    private final ListaEnlazada<Movimiento> movimientos = new ListaEnlazada<>();

    public Monedero(String nombre) { this.nombre = nombre; }

    /** Unica forma de cambiar el saldo: siempre deja registro del movimiento. */
    public void registrar(Transaccion t, double efecto) {
        saldo = redondear(saldo + efecto);
        String anterior = movimientos.estaVacia() ? HUELLA_INICIAL : movimientos.obtenerUltimo().getHuella();
        movimientos.agregarAlFinal(new Movimiento(t, efecto, saldo, anterior));
    }

    /** SOLO para demostrar que el verificador detecta manipulaciones. */
    public void manipularSaldoParaPrueba(double nuevoSaldo) { this.saldo = nuevoSaldo; }

    public String getNombre() { return nombre; }
    public double getSaldo() { return saldo; }
    public ListaEnlazada<Movimiento> getMovimientos() { return movimientos; }

    public static double redondear(double v) { return Math.round(v * 100.0) / 100.0; }

    @Override public String toString() { return nombre; }
}