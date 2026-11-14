package modelo;

/** Beneficios canjeables con puntos (enunciado). */
public enum Beneficio {
    DESCUENTO_TRANSFERENCIAS(100, "10% menos de comision en tus proximas 10 transferencias"),
    RETIROS_SIN_CARGO(500, "Un mes sin cargos por retiros"),
    BONO_SALDO(1000, "Bono de 50 unidades en tu monedero principal");

    private final int costo;
    private final String descripcion;

    Beneficio(int costo, String descripcion) { this.costo = costo; this.descripcion = descripcion; }

    public int getCosto() { return costo; }
    public String getDescripcion() { return descripcion; }
    @Override public String toString() { return costo + " pts: " + descripcion; }
}