package estructuras;

import java.util.Iterator;
import java.util.NoSuchElementException;

/*
Pila (LIFO: El ultimo en entrar es el primero en salir) que usa nodos para guardar los datos.
El uso en el proyecto: transacciones reversibles. Para "deshacer" se saca la ultima transaccion que se hizo, es decir, la cima
*/
public class Pila<T> implements Iterable<T> {
    private Nodo<T> cima;
    private int tamanio;

    public void apilar(T dato) { // Mete un dato en la cima de la pila
        Nodo<T> nuevo = new Nodo<>(dato);
        nuevo.siguiente = cima;
        cima = nuevo;
        tamanio++;
    }

    public T desapilar() { // Saca el dato de la cima de la pila
        if (estaVacia())
            throw new NoSuchElementException("La pila está vacía");
        T dato = cima.dato;
        cima = cima.siguiente;
        tamanio--;
        return dato;
    }

    public T cima() { // Devuelve el dato de la cima de la pila sin sacarlo
        if (estaVacia())
            throw new NoSuchElementException("La pila está vacía");
        return cima.dato;
    }

    public boolean estaVacia() { // Devuelve true si la pila está vacía
        return cima == null;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Nodo<T> actual = cima; 
            public boolean hasNext() {
                return actual != null;
            }
            public T next() {
                T d = actual.dato; 
                actual = actual.siguiente;
                return d;
            }
        };
    }

}
