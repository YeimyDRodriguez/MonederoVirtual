package modelo;

import java.time.LocalDate;

/**
 * Transaccion que se ejecutara automaticamente en una fecha.
 * Puede repetirse cada 'cadaDias' dias (0 = una sola vez).
 * repeticionesRestantes = -1 significa "sin limite".
 */
public class TransaccionProgramada {
    private final int id;
    private final TipoTransaccion tipo; // DEPOSITO o TRANSFERENCIA
    private final String idCliente, monederoOrigen;
    private final String idDestino, monederoDestino;
    private final double monto;
    private LocalDate proximaFecha;
    private final int cadaDias;
    private int repeticionesRestantes;
    private int ejecutadas;

    public TransaccionProgramada(int id, TipoTransaccion tipo, String idCliente, String monederoOrigen,
                                 String idDestino, String monederoDestino, double monto,
                                 LocalDate proximaFecha, int cadaDias, int repeticiones) {
        this.id = id; this.tipo = tipo; this.idCliente = idCliente; this.monederoOrigen = monederoOrigen;
        this.idDestino = idDestino; this.monederoDestino = monederoDestino; this.monto = monto;
        this.proximaFecha = proximaFecha; this.cadaDias = cadaDias;
        this.repeticionesRestantes = repeticiones;
    }

    /** Despues de ejecutarse: ¿hay que volver a programarla? */
    public boolean reprogramar() {
        ejecutadas++;
        if (cadaDias <= 0) return false;
        if (repeticionesRestantes > 0) repeticionesRestantes--;
        if (repeticionesRestantes == 0) return false;
        proximaFecha = proximaFecha.plusDays(cadaDias);
        return true;
    }

    public int getId() { return id; }
    public TipoTransaccion getTipo() { return tipo; }
    public String getIdCliente() { return idCliente; }
    public String getMonederoOrigen() { return monederoOrigen; }
    public String getIdDestino() { return idDestino; }
    public String getMonederoDestino() { return monederoDestino; }
    public double getMonto() { return monto; }
    public LocalDate getProximaFecha() { return proximaFecha; }
    public int getCadaDias() { return cadaDias; }
    public int getRepeticionesRestantes() { return repeticionesRestantes; }
    public int getEjecutadas() { return ejecutadas; }

    public String descripcion() {
        String que = tipo == TipoTransaccion.DEPOSITO
                ? "Deposito automatico a " + monederoDestino
                : "Transferencia " + monederoOrigen + " -> " + idDestino + "/" + monederoDestino;
        String cada = cadaDias > 0 ? " cada " + cadaDias + " dias" : " (una vez)";
        return que + cada;
    }
}
