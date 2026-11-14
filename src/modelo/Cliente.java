package modelo;

import estructuras.*;
import java.time.LocalDate;

public class Cliente {
    public static final String MONEDERO_PRINCIPAL = "Principal";
    public static final int CAPACIDAD_NOTIFICACIONES = 25;

    private final String id;
    private final String nombre;

    private final TablaHash<String, Monedero> monederos = new TablaHash<>();
    private final ListaEnlazada<String> nombresMonederos = new ListaEnlazada<>(); // para mostrarlos en orden
    private final ListaEnlazada<Transaccion> historial = new ListaEnlazada<>();
    private final Pila<Transaccion> reversibles = new Pila<>();
    private final ListaEnlazada<RegistroPuntos> historialPuntos = new ListaEnlazada<>();
    private final ListaCircular<Notificacion> notificaciones = new ListaCircular<>(CAPACIDAD_NOTIFICACIONES);
    private final GrafoDirigido<String> grafoMonederos = new GrafoDirigido<>();
    private final GrafoDirigido<String> grafoCategorias = new GrafoDirigido<>();
    private String ultimaCategoria;

    private int puntosAcumulados;   // definen el rango
    private int puntosCanjeados;    // gastados en beneficios
    private Rango rango = Rango.BRONCE;

    private int transferenciasConDescuento;
    private LocalDate retirosGratisHasta;
    private double umbralSaldoBajo = 100;

    public Cliente(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
        agregarMonedero(MONEDERO_PRINCIPAL);
    }

    public Monedero agregarMonedero(String nombre) {
        if (monederos.contieneClave(nombre)) throw new IllegalArgumentException("Ya existe el monedero " + nombre);
        Monedero m = new Monedero(nombre);
        monederos.poner(nombre, m);
        nombresMonederos.agregarAlFinal(nombre);
        grafoMonederos.agregarVertice(nombre);
        return m;
    }

    public Monedero getMonedero(String nombre) { return monederos.obtener(nombre); }
    public ListaEnlazada<String> getNombresMonederos() { return nombresMonederos; }

    public double saldoTotal() {
        double total = 0;
        for (String n : nombresMonederos) total += monederos.obtener(n).getSaldo();
        return Monedero.redondear(total);
    }

    public int getPuntosDisponibles() { return Math.max(0, puntosAcumulados - puntosCanjeados); }

    public boolean tieneRetirosGratis(LocalDate hoy) {
        return retirosGratisHasta != null && !hoy.isAfter(retirosGratisHasta);
    }

    // ---- getters / setters ----
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public ListaEnlazada<Transaccion> getHistorial() { return historial; }
    public Pila<Transaccion> getReversibles() { return reversibles; }
    public ListaEnlazada<RegistroPuntos> getHistorialPuntos() { return historialPuntos; }
    public ListaCircular<Notificacion> getNotificaciones() { return notificaciones; }
    public GrafoDirigido<String> getGrafoMonederos() { return grafoMonederos; }
    public GrafoDirigido<String> getGrafoCategorias() { return grafoCategorias; }
    public String getUltimaCategoria() { return ultimaCategoria; }
    public void setUltimaCategoria(String c) { ultimaCategoria = c; }
    public int getPuntosAcumulados() { return puntosAcumulados; }
    public void setPuntosAcumulados(int p) { puntosAcumulados = Math.max(0, p); }
    public int getPuntosCanjeados() { return puntosCanjeados; }
    public void sumarPuntosCanjeados(int p) { puntosCanjeados += p; }
    public Rango getRango() { return rango; }
    public void setRango(Rango r) { rango = r; }
    public int getTransferenciasConDescuento() { return transferenciasConDescuento; }
    public void setTransferenciasConDescuento(int n) { transferenciasConDescuento = n; }
    public LocalDate getRetirosGratisHasta() { return retirosGratisHasta; }
    public void setRetirosGratisHasta(LocalDate f) { retirosGratisHasta = f; }
    public double getUmbralSaldoBajo() { return umbralSaldoBajo; }
    public void setUmbralSaldoBajo(double u) { umbralSaldoBajo = u; }

    @Override public String toString() { return id + " - " + nombre; }
}