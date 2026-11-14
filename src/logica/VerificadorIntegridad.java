package logica;

import estructuras.ListaEnlazada;
import estructuras.Nodo;
import modelo.*;

/**
 * Verificacion de integridad con ALGORITMOS RECURSIVOS.
 */
public class VerificadorIntegridad {

    public static class Resultado {
        private final ListaEnlazada<String> errores = new ListaEnlazada<>();
        private int movimientosRevisados;
        private int monederosRevisados;

        public boolean esValido() {
            return errores.estaVacia();
        }

        public ListaEnlazada<String> getErrores() {
            return errores;
        }

        public int getMovimientosRevisados() {
            return movimientosRevisados;
        }

        public int getMonederosRevisados() {
            return monederosRevisados;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(esValido() ? "INTEGRIDAD CORRECTA" : "SE ENCONTRARON " + errores.tamanio() + " PROBLEMAS")
                    .append("\nMonederos revisados: ").append(monederosRevisados)
                    .append("\nMovimientos revisados: ").append(movimientosRevisados);
            for (String e : errores)
                sb.append("\n  - ").append(e);
            return sb.toString();
        }
    }

    private VerificadorIntegridad() {
    }

    // Verifica todos los clientes del sistema (recursion sobre la lista de
    // clientes).
    public static Resultado verificarSistema(SistemaMonedero s) {
        Resultado r = new Resultado();
        verificarClientes(s.listarClientes().getCabeza(), s, r);
        return r;
    }

    private static void verificarClientes(Nodo<Cliente> nodo, SistemaMonedero s, Resultado r) {
        if (nodo == null)
            return;
        verificarCliente(nodo.getDato(), s, r);
        verificarClientes(nodo.getSiguiente(), s, r);
    }

    public static Resultado verificarCliente(Cliente c, SistemaMonedero s) {
        Resultado r = new Resultado();
        verificarCliente(c, s, r);
        return r;
    }

    private static void verificarCliente(Cliente c, SistemaMonedero s, Resultado r) {
        for (String nombre : c.getNombresMonederos())
            verificarMonedero(c, c.getMonedero(nombre), r);

        int esperado = SistemaMonedero.sumarPuntosRecursivo(c.getHistorial().getCabeza(), c.getId());
        if (esperado != c.getPuntosAcumulados())
            r.errores.agregarAlFinal(c.getId() + ": puntos guardados " + c.getPuntosAcumulados()
                    + " pero el historial suma " + esperado);
        if (s.puntosSegunArbol(c.getId()) != c.getPuntosAcumulados())
            r.errores.agregarAlFinal(c.getId() + ": el arbol de puntos no coincide con el cliente");
        if (Rango.desdePuntos(c.getPuntosAcumulados()) != c.getRango())
            r.errores.agregarAlFinal(c.getId() + ": el rango no corresponde a sus puntos");
    }

    private static void verificarMonedero(Cliente c, Monedero m, Resultado r) {
        r.monederosRevisados++;
        String donde = c.getId() + "/" + m.getNombre();
        double saldoCalculado = verificarMovimientos(m.getMovimientos().getCabeza(), 0.0,
                Monedero.HUELLA_INICIAL, donde, r);
        if (Math.abs(saldoCalculado - m.getSaldo()) > 0.005)
            r.errores.agregarAlFinal(donde + ": el saldo guardado (" + SistemaMonedero.fmt(m.getSaldo())
                    + ") no coincide con los movimientos (" + SistemaMonedero.fmt(saldoCalculado) + ")");
    }

    /*
     * RECURSIVO. Revisa el nodo actual y continua con el siguiente.
     * Devuelve el saldo final calculado a partir de todos los movimientos.
     */
    private static double verificarMovimientos(Nodo<Movimiento> nodo, double saldoAnterior,
            String huellaAnterior, String donde, Resultado r) {
        if (nodo == null)
            return saldoAnterior; // caso base
        Movimiento mov = nodo.getDato();
        Transaccion t = mov.getTransaccion();
        r.movimientosRevisados++;

        double saldoEsperado = Monedero.redondear(saldoAnterior + mov.getEfecto());
        if (Math.abs(saldoEsperado - mov.getSaldoResultante()) > 0.005)
            r.errores.agregarAlFinal(donde + " #" + t.getId() + ": saldo inconsistente");
        if (saldoEsperado < -0.005)
            r.errores.agregarAlFinal(donde + " #" + t.getId() + ": saldo negativo");
        if (t.getMonto() <= 0 || t.getComision() < 0)
            r.errores.agregarAlFinal(donde + " #" + t.getId() + ": monto o comision invalidos");
        String huella = Movimiento.calcularHuella(huellaAnterior, t, mov.getEfecto(), mov.getSaldoResultante());
        if (!huella.equals(mov.getHuella()))
            r.errores.agregarAlFinal(donde + " #" + t.getId() + ": la huella no coincide (movimiento alterado)");

        return verificarMovimientos(nodo.getSiguiente(), saldoEsperado, mov.getHuella(), donde, r); // paso recursivo
    }
}
