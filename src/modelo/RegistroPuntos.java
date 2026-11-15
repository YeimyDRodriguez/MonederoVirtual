package modelo;

import java.time.LocalDate;

/** Una linea del historial de puntos: +15 por "Deposito #3", -100 por "Canje", etc. */
public class RegistroPuntos {
    private final LocalDate fecha;
    private final int cambio;
    private final String motivo;
    private final int acumuladosDespues;

    public RegistroPuntos(LocalDate fecha, int cambio, String motivo, int acumuladosDespues) {
        this.fecha = fecha; this.cambio = cambio; this.motivo = motivo;
        this.acumuladosDespues = acumuladosDespues;
    }

    public LocalDate getFecha() { return fecha; }
    public int getCambio() { return cambio; }
    public String getMotivo() { return motivo; }
    public int getAcumuladosDespues() { return acumuladosDespues; }
}
