package estructuras;

import java.util.Iterator;

public class ListaCircular<T> implements Iterable<T> {
    private Nodo<T> ultimo;
    private Nodo<T> cursor;
    private int tamanio;
    private final int capacidad;

    public ListaCircular(int capacidad) {
        if (capacidad <= 0) throw new IllegalArgumentException("Capacidad invalida");
        this.capacidad = capacidad;
    }

    public void agregar(T dato) {
        if (tamanio == capacidad) {
            ultimo = ultimo.siguiente;
            ultimo.dato = dato;
            return;
        }
        Nodo<T> nuevo = new Nodo<>(dato);
        if (ultimo == null) {
            nuevo.siguiente = nuevo;
        } else {
            nuevo.siguiente = ultimo.siguiente;
            ultimo.siguiente = nuevo;
        }
        ultimo = nuevo;
        tamanio++;
    }

    public T siguiente() {
        if (estaVacia()) return null;
        if (cursor == null) cursor = ultimo.siguiente;
        T d = cursor.dato;
        cursor = cursor.siguiente;
        return d;
    }

    public boolean eliminar(T dato) {
        if (estaVacia()) return false;
        Nodo<T> anterior = ultimo;
        for (int i = 0; i < tamanio; i++) {
            Nodo<T> actual = anterior.siguiente;
            if (actual.dato.equals(dato)) {
                if (tamanio == 1) { ultimo = null; cursor = null; }
                else {
                    anterior.siguiente = actual.siguiente;
                    if (actual == ultimo) ultimo = anterior;
                    if (cursor == actual) cursor = actual.siguiente;
                }
                tamanio--;
                return true;
            }
            anterior = actual;
        }
        return false;
    }

    public boolean estaVacia() { return tamanio == 0; }
    public int tamanio() { return tamanio; }
    public int capacidad() { return capacidad; }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Nodo<T> actual = (ultimo == null) ? null : ultimo.siguiente;
            private int vistos = 0;
            public boolean hasNext() { return vistos < tamanio; }
            public T next() { T d = actual.dato; actual = actual.siguiente; vistos++; return d; }
        };
    }
}