package estructuras;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Uso en el proyecto: transacciones reversibles. Para "deshacer" sacamos
 * la ultima transaccion que se hizo (la de la cima).
 */
public class Pila<T> implements Iterable<T> {
    private Nodo<T> cima; //el nodo de arriba el último que entró
    private int tamanio;

    public void apilar(T dato) {          // push
        Nodo<T> nuevo = new Nodo<>(dato);
        nuevo.siguiente = cima; //el nuevo apunta a la cima actual
        cima = nuevo; //el nuevo pasa a ser la cima
        tamanio++;
    }

    public T desapilar() {                // pop
        if (estaVacia()) throw new NoSuchElementException("Pila vacia");
        T dato = cima.dato;
        cima = cima.siguiente; //la cima baja al siguiente nodo
        tamanio--;
        return dato;
    }

    public T verCima() {                  // peek
        if (estaVacia()) throw new NoSuchElementException("Pila vacia");
        return cima.dato;
    }

    public boolean estaVacia() { return cima == null; }
    public int tamanio() { return tamanio; }

    /** Recorre desde la cima hacia el fondo. */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Nodo<T> actual = cima;
            public boolean hasNext() { return actual != null; }
            public T next() { T d = actual.dato; actual = actual.siguiente; return d; }
        };
    }
}
