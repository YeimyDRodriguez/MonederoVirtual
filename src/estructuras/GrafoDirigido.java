package estructuras;

public class GrafoDirigido<T> {

    public static class Arista<T> {
        private final T destino;
        private double peso;
        private int cantidad;
        Arista(T destino) {this.destino = destino; }
        public T getDestino() { return destino; }
        public double getPeso() { return peso; }
        public int getCantidad() { return cantidad; }
    }

    private final TablaHash<T, ListaEnlazada<Arista<T>>> adyacencia = new TablaHash<>();
    private final ListaEnlazada<T> vertices = new ListaEnlazada<>();

    public void agregarVertice(T v) {
        if (!adyacencia.contieneClave(v)) {
            adyacencia.poner(v, new ListaEnlazada<>());
            vertices.agregarAlFinal(v);
        }
    }

    public void agregarArista(T origen, T destino, double peso) {
        agregarVertice(origen);
        agregarVertice(destino);
        Arista<T> a = obtenerArista(origen, destino);
        if (a == null) {
            a = new Arista<>(destino);
            adyacencia.obtener(origen).agregarAlFinal(a);
        }
        a.peso += peso;
        a.cantidad++;
    }

    public void reducirArista(T origen, T destino, double peso) {
        Arista<T> a = obtenerArista(origen, destino);
        if (a == null) return;
        a.peso -= peso;
        a.cantidad--;
        if (a.cantidad <= 0) adyacencia.obtener(origen).eliminar(a);
    }

    public Arista<T> obtenerArista(T origen, T destino) {
        ListaEnlazada<Arista<T>> l = adyacencia.obtener(origen);
        if (l == null) return null;
        for (Arista<T> a : l) if (a.destino.equals(destino)) return a;
        return null;
    }

    public ListaEnlazada<Arista<T>> aristasDe(T v) {
        ListaEnlazada<Arista<T>> l = adyacencia.obtener(v);
        return l == null ? new ListaEnlazada<>() : l;
    }

    public ListaEnlazada<T> vertices() { return vertices; }
    public int cantidadVertices() { return vertices.tamanio(); }

    public int cantidadAristas() {
        int total = 0;
        for (T v : vertices) total += aristasDe(v).tamanio();
        return total;
    }

    public double pesoSalida(T v) {
        double s = 0;
        for (Arista<T> a : aristasDe(v)) s += a.peso;
        return s;
    }

    public double pesoEntrada(T v) {
        double s = 0;
        for (T u : vertices) {
            Arista<T> a = obtenerArista(u, v);
            if (a != null) s += a.peso;
        }
        return s;
    }

    public ListaEnlazada<T> recorridoAnchura(T origen) {
        ListaEnlazada<T> res = new ListaEnlazada<>();
        if (!adyacencia.contieneClave(origen)) return res;
        TablaHash<T, Boolean> visitado = new TablaHash<>();
        Cola<T> cola = new Cola<>();
        cola.encolar(origen);
        visitado.poner(origen, true);
        while (!cola.estaVacia()) {
            T v = cola.desencolar();
            res.agregarAlFinal(v);
            for (Arista<T> a : aristasDe(v)) {
                if (!visitado.contieneClave(a.destino)) {
                    visitado.poner(a.destino, true);
                    cola.encolar(a.destino);
                }
            }
        }
        return res;
    }

    public ListaEnlazada<T> recorridoProfundidad(T origen) {
        ListaEnlazada<T> res = new ListaEnlazada<>();
        if (adyacencia.contieneClave(origen)) dfs(origen, new TablaHash<>(), res);
        return res;
    }

    private void dfs(T v, TablaHash<T, Boolean> visitado, ListaEnlazada<T> res) {
        visitado.poner(v, true);
        res.agregarAlFinal(v);
        for (Arista<T> a : aristasDe(v))
            if (!visitado.contieneClave(a.destino)) dfs(a.destino, visitado, res);
    }

    public boolean existeCamino(T origen, T destino) {
        return recorridoProfundidad(origen).contiene(destino);
    }

    public boolean tieneCiclo() {
        TablaHash<T, Integer> estado = new TablaHash<>();
        for (T v : vertices)
            if (estado.obtenerOPorDefecto(v, 0) == 0 && cicloDesde(v, estado)) return true;
        return false;
    }

    private boolean cicloDesde(T v, TablaHash<T, Integer> estado) {
        estado.poner(v, 1);
        for (Arista<T> a : aristasDe(v)) {
            int e = estado.obtenerOPorDefecto(a.destino, 0);
            if (e == 1) return true;
            if (e == 0 && cicloDesde(a.destino, estado)) return true;
        }
        estado.poner(v, 2);
        return false;
    }

}
