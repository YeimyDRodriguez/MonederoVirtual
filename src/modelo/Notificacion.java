package modelo;

import java.time.LocalDate;

public class Notificacion {
    private final String idCliente;
    private final TipoNotificacion tipo;
    private final String mensaje;
    private final LocalDate fecha;
    private boolean leida;

    public Notificacion(String idCliente, TipoNotificacion tipo, String mensaje, LocalDate fecha) {
        this.idCliente = idCliente; this.tipo = tipo; this.mensaje = mensaje; this.fecha = fecha;
    }

    public String getIdCliente() { return idCliente; }
    public TipoNotificacion getTipo() { return tipo; }
    public String getMensaje() { return mensaje; }
    public LocalDate getFecha() { return fecha; }
    public boolean isLeida() { return leida; }
    public void marcarLeida() { leida = true; }

    @Override public String toString() { return "[" + fecha + "] " + tipo + ": " + mensaje; }
}
