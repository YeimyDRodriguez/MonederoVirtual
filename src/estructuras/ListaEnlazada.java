package estructuras;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ListaEnlazada<T> implements Iterable<T> {
    private Nodo<T> cabeza;
    private Nodo<T> ultimo;
    private int tamanio;

    public boolean estaVacia() { return cabeza == null; }
    public int tamanio() { return tamanio; }
    public Nodo<T> getCabeza() { return cabeza; }

    public void agregarAlInicio(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        nuevo.siguiente = cabeza;
        cabeza = nuevo;
        if (ultimo == null) ultimo = nuevo;
        tamanio++;
    }

    public void agregarAlFinal(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (estaVacia()) {
            cabeza = nuevo;
        } else {
            ultimo.siguiente = nuevo;
        }
        ultimo = nuevo;
        tamanio++;
    }

    public T obtener(int posicion) {
        if (posicion < 0 || posicion >= tamanio)
            throw new IndexOutOfBoundsException("Posicion invalida: " + posicion);
        Nodo<T> actual = cabeza;
        for (int i = 0; i < posicion; i++) actual = actual.siguiente;
        return actual.dato;
    }

    public T obtenerPrimero() {
        if (estaVacia()) throw new NoSuchElementException("Lista vacia");
        return cabeza.dato;
    }

    public T obtenerUltimo() {
        if (estaVacia()) throw new NoSuchElementException("Lista vacia");
        return ultimo.dato;
    }

    public boolean contiene(T dato) {
        for (Nodo<T> a = cabeza; a != null; a = a.siguiente)
            if (a.dato.equals(dato)) return true;
        return false;
    }

    public T eliminarPrimero() {
        if (estaVacia()) throw new NoSuchElementException("Lista vacia");
        T dato = cabeza.dato;
        cabeza = cabeza.siguiente;
        if (cabeza == null) ultimo = null;
        tamanio--;
        return dato;
    }

    public boolean eliminar(T dato) {
        if (estaVacia()) return false;
        if (cabeza.dato.equals(dato)) { eliminarPrimero(); return true; }
        Nodo<T> anterior = cabeza;
        while (anterior.siguiente != null && !anterior.siguiente.dato.equals(dato)) {
            anterior = anterior.siguiente;
        }
        if (anterior.siguiente == null) return false;
        if (anterior.siguiente == ultimo) ultimo = anterior;
        anterior.siguiente = anterior.siguiente.siguiente;
        tamanio--;
        return true;
    }

    public void limpiar() { cabeza = ultimo = null; tamanio = 0; }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Nodo<T> actual = cabeza;
            public boolean hasNext() { return actual != null; }
            public T next() {
                if (actual == null) throw new NoSuchElementException();
                T d = actual.dato;
                actual = actual.siguiente;
                return d;
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (Nodo<T> a = cabeza; a != null; a = a.siguiente) {
            sb.append(a.dato);
            if (a.siguiente != null) sb.append(", ");
        }
        return sb.append("]").toString();
    }
}

