package estructuras;
public class ArbolAVL <K extends Comparable<K>, V> { // clase generica con dos tipos K es la clave V es el valor 
	private static class NodoAVL <K, V>{ //representa cada caja del arbol
		K clave; V valor; //cada nodo guarda su clave y su valor 
		NodoAVL <K, V> izq, der; //referencias a los hijos
		int altura = 1;
		NodoAVL(K clave, V valor) { this.clave = clave; this.valor = valor;} //constructor
	}
	
	private NodoAVL <K, V> raiz;
	private int tamanio;
	
	//utilidades de altura y rotaciones
	
	private int altura(NodoAVL <K, V> n) { return n == null ? 0: n.altura;}
	private int balance(NodoAVL <K, V> n) { return n == null ? 0: altura(n.izq) - altura(n.der);}
	private void actualizarAltura(NodoAVL<K, V> n) {
		n.altura = 1 + Math.max(altura(n.izq), altura(n.der));
	}
	
	private NodoAVL <K, V> rotarDerecha(NodoAVL<K, V> z){
		NodoAVL<K, V> y = z.izq;
		z.izq = y.der;
		y.der = z;
		actualizarAltura(z);
		actualizarAltura(y);
		return y;
	}
	private NodoAVL<K, V> rotarIzquierda(NodoAVL<K, V> z) {
        NodoAVL<K, V> y = z.der;
        z.der = y.izq;
        y.izq = z;
        actualizarAltura(z);
        actualizarAltura(y);
        return y;
    }
	
	//Revisa el balance del nodo y aplica la rotación que corresponda
	
	private NodoAVL<K, V> balancear(NodoAVL<K, V> n) {
		actualizarAltura(n);
		int b = balance(n);
		if (b > 1) {
			if (balance(n.izq)< 0) n.izq = rotarIzquierda(n.izq);
			return rotarDerecha(n);
		}
		if (b < -1) {
			if (balance(n.der) > 0) n.der = rotarDerecha(n.der);
			return rotarIzquierda(n);
		}
		return n;
	}
	// insertar
	   public void insertar(K clave, V valor) { raiz = insertar(raiz, clave, valor); }

	    private NodoAVL<K, V> insertar(NodoAVL<K, V> n, K clave, V valor) {
	        if (n == null) { tamanio++; return new NodoAVL<>(clave, valor); }
	        int c = clave.compareTo(n.clave);
	        if (c < 0) n.izq = insertar(n.izq, clave, valor);
	        else if (c > 0) n.der = insertar(n.der, clave, valor);
	        else { n.valor = valor; return n; }
	        return balancear(n);
	    }
	    
	//eliminar
	    
	    public void eliminar(K clave) { raiz = eliminar(raiz, clave); }

	    private NodoAVL<K, V> eliminar(NodoAVL<K, V> n, K clave) {
	        if (n == null) return null;
	        int c = clave.compareTo(n.clave);
	        if (c < 0) n.izq = eliminar(n.izq, clave);
	        else if (c > 0) n.der = eliminar(n.der, clave);
	        else {
	            if (n.izq == null || n.der == null) {
	                tamanio--;
	                return (n.izq != null) ? n.izq : n.der;
	            }
	            NodoAVL<K, V> s = n.der;
	            while (s.izq != null) s = s.izq;
	            n.clave = s.clave;
	            n.valor = s.valor;
	            n.der = eliminar(n.der, s.clave);
	        }
	        return balancear(n);
	    }
	    
	    //busqueda
	    public V buscar(K clave) {
	        NodoAVL<K, V> n = raiz;
	        while (n != null) {
	            int c = clave.compareTo(n.clave);
	            if (c == 0) return n.valor;
	            n = c < 0 ? n.izq : n.der;
	        }
	        return null;
	    }
	    
	    public ListaEnlazada<V> buscarEnRango(K min, K max) {
	        ListaEnlazada<V> res = new ListaEnlazada<>();
	        rango(raiz, min, max, res);
	        return res;
	    }
	    private void rango(NodoAVL<K, V> n, K min, K max, ListaEnlazada<V> res) {
	        if (n == null) return;
	        if (min.compareTo(n.clave) < 0) rango(n.izq, min, max, res);
	        if (min.compareTo(n.clave) <= 0 && max.compareTo(n.clave) >= 0) res.agregarAlFinal(n.valor);
	        if (max.compareTo(n.clave) > 0) rango(n.der, min, max, res);
	    }
	    
	    public ListaEnlazada<V> valoresDescendente() {
	        ListaEnlazada<V> res = new ListaEnlazada<>();
	        desc(raiz, res);
	        return res;
	    }
	    private void desc(NodoAVL<K, V> n, ListaEnlazada<V> res) {
	        if (n == null) return;
	        desc(n.der, res);
	        res.agregarAlFinal(n.valor);
	        desc(n.izq, res);
	    }

	    public int altura() { return altura(raiz); }
	    public int tamanio() { return tamanio; }
	    public boolean estaVacio() { return raiz == null; }

	    /** Verifica (recursivamente) que todo el arbol cumple la propiedad AVL. */
	    public boolean estaBalanceado() { return verificar(raiz); }
	    private boolean verificar(NodoAVL<K, V> n) {
	        if (n == null) return true;
	        return Math.abs(balance(n)) <= 1 && verificar(n.izq) && verificar(n.der);
	    }


  
	
}
