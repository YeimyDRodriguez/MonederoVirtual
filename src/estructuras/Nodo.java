package estructuras;
/*
Nodo génerico que usan la pila y la cola propias, 
para guardar un dato y una referencia al siguiente nodo.
*/
public class Nodo<T> {

    T dato; // Información que guarda el nodo
    Nodo<T> siguiente; // Flecha que apunta al siguiente nodo

    public Nodo(T dato) {
        this.dato = dato;
        this.siguiente = null;
    }

}
