package estructuras;
//Se usa para los puntos acumulados de cada cliente
//Clave es el id y valor son los puntos, al recorrerlo en inorden da los clientes ordenados por id
public class ArbolBinarioBusqueda<K extends Comparable<K>, V> {
	private static class NodoArbol<K, V>{
		K clave; V valor;
		NodoArbol<K, V> izq, der;
		NodoArbol(K clave, V valor) { this.clave = clave; this.valor = valor;}
	}
	private NodoArbol<K, V> raiz;
	private int tamanio;
	// insertar (o actualizar)
	public void insertar(K clave, V valor) { raiz = insertar(raiz, clave, valor);}
	private NodoArbol<K, V> insertar(NodoArbol<K, V> n, K clave, V valor){
		if(n == null) { tamanio++; return new NodoArbol<>(clave, valor);}
		int c = clave.compareTo(n.clave);
		if(c < 0) n.izq = insertar(n.izq, clave, valor);
		else if(c > 0) n.der = insertar(n.der, clave, valor);
		else n.valor = valor;
		return n;
	}
	//buscar
	public V buscar(K clave) { return buscar(raiz, clave);}
	private V buscar(NodoArbol<K, V> n, K clave) {
		if(n == null) return null;
		int c = clave.compareTo(n.clave);
		if (c == 0) return n.valor;
		return c < 0? buscar(n.izq, clave) : buscar(n.der, clave);
	}
	public boolean contiene(K clave) { return buscar(clave) != null;}
	//eliminar
	public void eliminar(K clave) { raiz = eliminar(raiz, clave);}
	private NodoArbol<K, V> eliminar(NodoArbol<K, V> n, K clave){
		if(n == null) return null;
		int c = clave.compareTo(n.clave);
		if(c < 0) n.izq = eliminar(n.izq, clave);
		else if( c > 0) n.der = eliminar(n.der, clave);
		else {
			//caso 1 y 2: cero o un hijo -> lo reemplaza su hijo
			if(n.izq == null) { tamanio--; return n.der;}
			if(n.der == null) { tamanio--; return n.izq;}
			//caso 3: dos hijos -> copiamos el menor del subarbol derecho (sucesor)
			NodoArbol<K, V> sucesor = minimo(n.der);
			n.clave = sucesor.clave;
			n.valor = sucesor.valor;
			n.der = eliminar(n.der, sucesor.clave);
		}
		return n;
	}
	private NodoArbol<K, V> minimo(NodoArbol<K, V> n){
		return n.izq == null ? n: minimo(n.izq);
	}
	//recorridos
	//** In-orden: izq- raiz- derecha
	public ListaEnlazada<Par<K, V>> inOrden(){
		ListaEnlazada<Par<K, V>> res = new ListaEnlazada<>();
		inOrden(raiz, res);
		return res;
	}
	private void inOrden (NodoArbol<K, V> n, ListaEnlazada<Par<K, V>> res) {
		if(n == null) return;
		inOrden(n.izq, res);
		res.agregarAlFinal(new Par<>(n.clave, n.valor));
		inOrden(n.der, res);
	}
	public int altura() { return altura(raiz);}
	private int altura(NodoArbol<K, V> n) {
		return n == null ? 0 : 1 + Math.max(altura(n.izq), altura(n.der));
	}
	public int tamanio() { return tamanio; }
	public boolean estaVacio() { return raiz == null;}
}
