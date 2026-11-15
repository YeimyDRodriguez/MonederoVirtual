package estructuras;

/**
 * Tabla hash PROPIA con ENCADENAMIENTO SEPARADO.
 * Uso en el proyecto: buscar un cliente (y sus puntos) por su id en O(1) promedio.
 *
 *   cubetas[0] -> (k1,v1) -> (k7,v7)
 *   cubetas[1] -> null
 *   cubetas[2] -> (k3,v3)
 *   ...
 *
 * 1) hashCode() de la clave -> numero
 * 2) numero % cantidadDeCubetas -> indice de la cubeta
 * 3) en esa cubeta hay una cadenita (lista enlazada) de pares clave-valor
 *
 * Si la tabla se llena mucho (factor de carga > 0.75) se duplica y se
 * "rehashean" todos los elementos para que las cadenas sigan cortas.
 */
public class TablaHash<K, V> {

    /** Un par clave-valor que vive dentro de una cubeta. */
    private static class Entrada<K, V> {
        final K clave;
        V valor;
        Entrada<K, V> siguiente; //la flecha a la siguiente entrada de la misma cubeta
        Entrada(K clave, V valor, Entrada<K, V> siguiente) { //constructor
            this.clave = clave; this.valor = valor; this.siguiente = siguiente;
        }
    }

    private Entrada<K, V>[] cubetas;
    private int tamanio;
    private static final double FACTOR_CARGA_MAX = 0.75;

    @SuppressWarnings("unchecked")
    public TablaHash() {
        cubetas = (Entrada<K, V>[]) new Entrada[16];
    }

    private int indice(K clave, int largo) {
        return (clave.hashCode() & 0x7fffffff) % largo; // & quita el signo negativo
    }

    /** Inserta o reemplaza. O(1) promedio */
    public void poner(K clave, V valor) {
        if (clave == null) throw new IllegalArgumentException("Clave nula");
        int i = indice(clave, cubetas.length);
        for (Entrada<K, V> e = cubetas[i]; e != null; e = e.siguiente) {
            if (e.clave.equals(clave)) { e.valor = valor; return; } // ya existia
        }
        cubetas[i] = new Entrada<>(clave, valor, cubetas[i]); // se agrega al inicio de la cadena
        tamanio++;
        if ((double) tamanio / cubetas.length > FACTOR_CARGA_MAX) rehash();
    }

    /** O(1) promedio. Devuelve null si no existe. */
    public V obtener(K clave) {
        int i = indice(clave, cubetas.length);
        for (Entrada<K, V> e = cubetas[i]; e != null; e = e.siguiente)
            if (e.clave.equals(clave)) return e.valor;
        return null;
    }

    public V obtenerOPorDefecto(K clave, V porDefecto) {
        V v = obtener(clave);
        return v == null ? porDefecto : v;
    }

    public boolean contieneClave(K clave) { return obtener(clave) != null; }

    public V eliminar(K clave) {
        int i = indice(clave, cubetas.length);
        Entrada<K, V> anterior = null;
        for (Entrada<K, V> e = cubetas[i]; e != null; anterior = e, e = e.siguiente) {
            if (e.clave.equals(clave)) {
                if (anterior == null) cubetas[i] = e.siguiente;
                else anterior.siguiente = e.siguiente;
                tamanio--;
                return e.valor;
            }
        }
        return null;
    }

    public int tamanio() { return tamanio; }
    public boolean estaVacia() { return tamanio == 0; }

    /** Todas las claves (sin orden particular). */
    public ListaEnlazada<K> claves() {
        ListaEnlazada<K> res = new ListaEnlazada<>();
        for (Entrada<K, V> c : cubetas)
            for (Entrada<K, V> e = c; e != null; e = e.siguiente) res.agregarAlFinal(e.clave);
        return res;
    }

    /** Todos los valores (sin orden particular). */
    public ListaEnlazada<V> valores() {
        ListaEnlazada<V> res = new ListaEnlazada<>();
        for (Entrada<K, V> c : cubetas)
            for (Entrada<K, V> e = c; e != null; e = e.siguiente) res.agregarAlFinal(e.valor);
        return res;
    }

    @SuppressWarnings("unchecked")
    private void rehash() {
        Entrada<K, V>[] viejas = cubetas;
        cubetas = (Entrada<K, V>[]) new Entrada[viejas.length * 2];
        for (Entrada<K, V> c : viejas) {
            for (Entrada<K, V> e = c; e != null; e = e.siguiente) {
                int i = indice(e.clave, cubetas.length);
                cubetas[i] = new Entrada<>(e.clave, e.valor, cubetas[i]);
            }
        }
    }
}
