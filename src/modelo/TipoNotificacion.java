package modelo;

public enum TipoNotificacion {
    SALDO_BAJO("Saldo bajo"),
    RECORDATORIO("Recordatorio"),
    PROGRAMADA("Programada"),
    RANGO("Rango"),
    PUNTOS("Puntos"),
    TRANSFERENCIA("Transferencia"),
    SISTEMA("Sistema");

    private final String nombre;
    TipoNotificacion(String nombre) { this.nombre = nombre; }
    @Override public String toString() { return nombre; }
}
