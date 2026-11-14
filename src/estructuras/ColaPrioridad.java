package estructuras;
import java.util.Comparator;
import java.util.NoSuchElementException;
public class ColaPrioridad<T> {
	private Object[] datos;
	private int tamanio;
	private final Comparator<T> comparador;
	public ColaPrioridad(Comparator<T> comparador) {
		this.comparador = comparador;
		this.datos = new Object[16];
	}
	public void insertar(T dato) {
		if(tamanio == datos.length) agrandar();
		datos[tamanio] = dato;
		subir(tamanio);//lo hacemos "flotar hasta su lugar
		tamanio++;
	}
	public T extraerMinimo() {
		if(estaVacia())throw new NoSuchElementException("Cola de prioridad vacia");
		T minimo = get(0);
		tamanio--;
		datos[0] = datos[tamanio]; // el ultimo pasa a la raiz
		datos[tamanio]= null;
		if( tamanio > 0) bajar(0);
		return minimo;
	}
	public T verMinimo() {
		if(estaVacia())throw new NoSuchElementException("Cola de prioridad vacia");
		return get(0);
	}
	//Elimna un elemento cualquiera (p. eje. cancelar una programada)
	public boolean eliminar(T dato) {
		for( int i = 0; i < tamanio; i++) {
			if(datos[i].equals(dato)) {
				tamanio--;
				datos[i]= datos[tamanio];
				datos[tamanio]= null;
				if( i < tamanio) { bajar(i); subir(i);}
				return true;
			}
		}
		return false;
	}
	public boolean estaVacia() { return tamanio == 0;}
	public int tamanio() {return tamanio;}
	//Copia ordenada (no modifica la cola). Util para mostrar en pantalla.
	public ListaEnlazada<T> enOrden(){
		ColaPrioridad<T> copia= new ColaPrioridad<>(comparador);
		for(int i=0; i < tamanio; i++) copia.insertar(get(i));
		ListaEnlazada<T> res = new ListaEnlazada<>();
		 while (!copia.estaVacia()) res.agregarAlFinal(copia.extraerMinimo());
	        return res;
	}
	 // ---------- auxiliares ----------
    @SuppressWarnings("unchecked")
    private T get(int i) { return (T) datos[i]; }
    private void subir(int i) {
        while (i > 0) {
            int padre = (i - 1) / 2;
            if (comparador.compare(get(i), get(padre)) >= 0) break;
            intercambiar(i, padre);
            i = padre;
        }
    }
    private void bajar(int i) {
        while (true) {
            int izq = 2 * i + 1, der = 2 * i + 2, menor = i;
            if (izq < tamanio && comparador.compare(get(izq), get(menor)) < 0) menor = izq;
            if (der < tamanio && comparador.compare(get(der), get(menor)) < 0) menor = der;
            if (menor == i) break;
            intercambiar(i, menor);
            i = menor;
        }
    }
    private void intercambiar(int a, int b) {
        Object t = datos[a]; datos[a] = datos[b]; datos[b] = t;
    }
    private void agrandar() {
        Object[] nuevo = new Object[datos.length * 2];
        for (int i = 0; i < datos.length; i++) nuevo[i] = datos[i];
        datos = nuevo;
    }
	

}
