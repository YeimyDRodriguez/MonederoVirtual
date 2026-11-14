package modelo;

public class Movimiento {
    private final Transaccion transaccion;
    private final double efecto;
    private final double saldoResultante;
    private final String huella;

    public Movimiento(Transaccion transaccion, double efecto, double saldoResultante, String huellaAnterior) {
        this.transaccion = transaccion;
        this.efecto = efecto;
        this.saldoResultante = saldoResultante;
        this.huella = calcularHuella(huellaAnterior, transaccion, efecto, saldoResultante);
    }

    public static String calcularHuella(String anterior, Transaccion t, double efecto, double saldo) {
        String texto = anterior + "#" + t.datosFijos() + "#" + efecto + "#" + saldo;
        return Integer.toHexString(texto.hashCode());
    }

    public Transaccion getTransaccion() { return transaccion; }
    public double getEfecto() { return efecto; }
    public double getSaldoResultante() { return saldoResultante; }
    public String getHuella() { return huella; }
}

