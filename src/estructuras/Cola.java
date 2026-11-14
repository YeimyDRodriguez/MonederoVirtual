package estructuras;
import java.util.Iterator;
import java.util.NoSuchElementException;
public class Cola<T> implements Iterable<T>{
	private Nodo<T> frente;
	private Nodo<T> fin;
	private int tamanio;
	public void encolar(T dato) {
		Nodo<T> nuevo = new Nodo<>(dato);
		if(estaVacia()) frente = nuevo;
		else fin.siguiente = nuevo;
		fin = nuevo;
		tamanio++;
	}
	public T desencolar() {
		if(estaVacia()) throw new NoSuchElementException("Cola Vacia");
		T dato = frente.dato;
		frente = frente.siguiente;
		if(frente == null) fin = null;
		tamanio--;
		return dato;
	}
	public T verFrente() {
		if(estaVacia()) throw new NoSuchElementException("Cola Vacia");
		return frente.dato;
	}
	public boolean estaVacia() { return frente == null;}
	public int tamanio() { return tamanio;}
	@Override
	public Iterator<T> iterator(){
		return new Iterator<T>() {
			private Nodo<T> actual = frente;
			public boolean hasNext() { return actual != null;}
			public T next() { T d = actual.dato; actual = actual.siguiente; return d;}
		};
	}
}
