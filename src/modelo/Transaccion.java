package modelo;

import java.time.LocalDate;

/**
 * Una transaccion del sistema.
 *  - origen  = de donde SALE el dinero (null en un deposito)
 *  - destino = a donde ENTRA el dinero (null en un retiro o gasto)
 * Al origen se le descuenta (monto + comision); al destino se le suma monto.
 * 'titular' es el cliente que hizo la operacion y por tanto el que gana los puntos.
 */
public class Transaccion {
    private final int id;
    private final TipoTransaccion tipo;
    private final double monto;
    private final double comision;
    private final LocalDate fecha;
    private final String titular;
    private final String clienteOrigen, monederoOrigen;
    private final String clienteDestino, monederoDestino;
    private final String categoria;   // solo para gastos
    private final String descripcion;
    private int puntos;
    private boolean revertida;

    public Transaccion(int id, TipoTransaccion tipo, double monto, double comision, LocalDate fecha,
                       String titular, String clienteOrigen, String monederoOrigen,
                       String clienteDestino, String monederoDestino,
                       String categoria, String descripcion) {
        this.id = id; this.tipo = tipo; this.monto = monto; this.comision = comision;
        this.fecha = fecha; this.titular = titular;
        this.clienteOrigen = clienteOrigen; this.monederoOrigen = monederoOrigen;
        this.clienteDestino = clienteDestino; this.monederoDestino = monederoDestino;
        this.categoria = categoria; this.descripcion = descripcion;
    }

    public int getId() { return id; }
    public TipoTransaccion getTipo() { return tipo; }
    public double getMonto() { return monto; }
    public double getComision() { return comision; }
    public double getTotalDebitado() { return monto + comision; }
    public LocalDate getFecha() { return fecha; }
    public String getTitular() { return titular; }
    public String getClienteOrigen() { return clienteOrigen; }
    public String getMonederoOrigen() { return monederoOrigen; }
    public String getClienteDestino() { return clienteDestino; }
    public String getMonederoDestino() { return monederoDestino; }
    public String getCategoria() { return categoria; }
    public String getDescripcion() { return descripcion; }
    public int getPuntos() { return puntos; }
    public void setPuntos(int puntos) { this.puntos = puntos; }
    public boolean isRevertida() { return revertida; }
    public void marcarRevertida() { this.revertida = true; }

    /** Texto con los datos que no cambian; se usa para la huella de integridad. */
    public String datosFijos() {
        return id + "|" + tipo.name() + "|" + monto + "|" + comision + "|" + fecha + "|" + titular
                + "|" + clienteOrigen + "|" + monederoOrigen + "|" + clienteDestino + "|" + monederoDestino;
    }

    @Override
    public String toString() {
        return String.format("#%d %s %.2f (%s)%s", id, tipo, monto, fecha, revertida ? " [REVERTIDA]" : "");
    }
}
