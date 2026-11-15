package modelo;

/**
 * Tipos de transaccion y cuantos puntos dan por cada 100 unidades.
 *   Deposito: 1, Retiro: 2, Transferencia: 3 (lo pide el enunciado).
 *   Gasto: 2 (es un retiro con categoria).
 *   Interna (entre monederos propios), Bono y Reversion: 0.
 */
public enum TipoTransaccion {
    DEPOSITO("Deposito", 1),
    RETIRO("Retiro", 2),
    TRANSFERENCIA("Transferencia", 3),
    GASTO("Gasto", 2),
    TRANSFERENCIA_INTERNA("Entre monederos", 0),
    BONO("Bono por canje", 0),
    REVERSION("Reversion", 0);

    private final String nombre;
    private final int puntosPorCada100;

    TipoTransaccion(String nombre, int puntosPorCada100) {
        this.nombre = nombre;
        this.puntosPorCada100 = puntosPorCada100;
    }

    public int getPuntosPorCada100() { return puntosPorCada100; }
    @Override public String toString() { return nombre; }
}
