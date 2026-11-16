package logica;

public class ClavePuntos implements Comparable<ClavePuntos> {
    private final int puntos;
    private final String id;

    public ClavePuntos(int puntos, String id) { this.puntos = puntos; this.id = id; }

    @Override
    public int compareTo(ClavePuntos o) {
        if (puntos != o.puntos) return Integer.compare(puntos, o.puntos);
        return id.compareTo(o.id);
    }

    public int getPuntos() { return puntos; }
    public String getId() { return id; }
}