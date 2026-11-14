package logica;

import java.util.Comparator;

public class Ordenamiento {
    private Ordenamiento() {}

    public static <T> void mergeSort(T[] a, Comparator<T> cmp) {
        if (a.length < 2) return;
        @SuppressWarnings("unchecked")
        T[] aux = (T[]) new Object[a.length];
        ordenar(a, aux, 0, a.length - 1, cmp);
    }

    private static <T> void ordenar(T[] a, T[] aux, int ini, int fin, Comparator<T> cmp) {
        if (ini >= fin) return;
        int medio = (ini + fin) / 2;
        ordenar(a, aux, ini, medio, cmp);
        ordenar(a, aux, medio + 1, fin, cmp);
        mezclar(a, aux, ini, medio, fin, cmp);
    }

    private static <T> void mezclar(T[] a, T[] aux, int ini, int medio, int fin, Comparator<T> cmp) {
        for (int k = ini; k <= fin; k++) aux[k] = a[k];
        int i = ini, j = medio + 1;
        for (int k = ini; k <= fin; k++) {
            if (i > medio) a[k] = aux[j++];
            else if (j > fin) a[k] = aux[i++];
            else if (cmp.compare(aux[j], aux[i]) < 0) a[k] = aux[j++];
            else a[k] = aux[i++];
        }
    }
}

