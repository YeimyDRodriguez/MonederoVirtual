package estructuras;

/**
 * Lo usan la ListaEnlazada, la Pila, la Cola y la ListaCircular.
 */
public class Nodo<T> {
    T dato;
    Nodo<T> siguiente;

    public Nodo(T dato) {
        this.dato = dato;
        this.siguiente = null;
    }

    public T getDato() { return dato; }
    public Nodo<T> getSiguiente() { return siguiente; }
}
