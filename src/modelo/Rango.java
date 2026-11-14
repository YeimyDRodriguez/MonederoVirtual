package modelo;

public enum Rango {
    BRONCE ("Bronce",     0,   500, 0.020, 0.010, 1.00),
    PLATA  ("Plata",    501,  1000, 0.015, 0.0075, 1.10),
    ORO    ("Oro",     1001,  5000, 0.010, 0.005, 1.25),
    PLATINO("Platino", 5001, Integer.MAX_VALUE, 0.005, 0.000, 1.50);

    private final String nombre;
    private final int minimo, maximo;
    private final double comisionTransferencia, comisionRetiro, multiplicadorPuntos;

    Rango(String nombre, int minimo, int maximo, double comTransf, double comRetiro, double mult) {
        this.nombre = nombre; this.minimo = minimo; this.maximo = maximo;
        this.comisionTransferencia = comTransf; this.comisionRetiro = comRetiro;
        this.multiplicadorPuntos = mult;
    }

    public static Rango desdePuntos(int puntos) {
        if (puntos > 5000) return PLATINO;
        if (puntos > 1000) return ORO;
        if (puntos > 500) return PLATA;
        return BRONCE;
    }

    public int getMinimo() { return minimo; }
    public int getMaximo() { return maximo; }
    public double getComisionTransferencia() { return comisionTransferencia; }
    public double getComisionRetiro() { return comisionRetiro; }
    public double getMultiplicadorPuntos() { return multiplicadorPuntos; }

    public String descripcionBeneficios() {
        return String.format("Comision transferencias %.2f%%, comision retiros %.2f%%, puntos x%.2f",
                comisionTransferencia * 100, comisionRetiro * 100, multiplicadorPuntos);
    }

    @Override public String toString() { return nombre; }
}
