package estructuras;

/*
Tabla Hash prpia con Encadenamiento separado, es decir, cada casilla de la tabla es una lista de pares clave-valor.
Uso que se le da en el proyecto: buscar un cliente (y sus puntos) por su id en O(1) promedio, y en el peor caso O(n) (cuando todos los clientes tienen el mismo hash y se encadenan en la misma lista)

1) hashCode() de la clave -> numero
2) numero % cantidadDeCubetas -> Indice de la cubeta
3) en esa cubeta hay una cadenita (una lista enlazada) de pares clave-valor, se recorre la lista hasta encontrar la clave buscada y se devuelve el valor asociado a esa clave.

Si la tabla se llena mucho (factor de carga > 0.75) se duplica y se "rehashean" todos los elementos para que las cadenas sigan cortas


*/

public class TablaHash<K, V> {
    // Cantidad de cubetas (listas enlazadas) que tiene la tabla
    private static class Entrada<K, V> {
        final K clave;
        V valor;
        Entrada<K, V> siguiente;

        Entrada(K clave, V valor, Entrada<K, V> siguiente) {
            this.clave = clave;
            this.valor = valor;
            this.siguiente = siguiente;
        }
    }

    private Entrada<K, V>[] cubetas;
    private int tamanio;
    private static final double FACTOR_DE_CARGA = 0.75;

    @SuppressWarnings("unchecked")
    public TablaHash() {
        cubetas = (Entrada<K, V>[]) new Entrada[16];
    }

    private int indice(K clave, int largo) {
        return (clave.hashCode() & 0x7fffffff) % largo; // & quita el signo negativo)
    }

    public void poner(K clave, V valor) {
        if (clave == null)
            throw new IllegalArgumentException("Clave nula");
        int i = indice(clave, cubetas.length);
        for (Entrada<K, V> e = cubetas[i]; e != null; e = e.siguiente) {
            if (e.clave.equals(clave)) {
                e.valor = valor; // Si la clave ya existe, se actualiza el valor
                return;
            }
        }
        cubetas[i] = new Entrada<>(clave, valor, cubetas[i]); // Se agrega al inicio de la lista
        tamanio++;
        if ((double) tamanio / cubetas.length > FACTOR_DE_CARGA) {
            rehash();
        }
    }

    public V obtener(K clave) {
        int i = indice(clave, cubetas.length);
        for (Entrada<K, V> e = cubetas[i]; e != null; e = e.siguiente)
            if (e.clave.equals(clave))
                return e.valor;
        return null; // Si no se encuentra la clave, se devuelve null
    }

    public V obtenerPorDefecto(K clave, V porDefecto){
        V v = obtener(clave);
        return v != null ? v : porDefecto;
    }


    public boolean contieneClave(K clave) {
        return obtener(clave) != null;
    }   

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

    public int tamanio() {
        return tamanio;
    }

    public boolean estaVacia() {
        return tamanio == 0;
    }

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
