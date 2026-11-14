package logica;

import estructuras.*;
import modelo.*;
import java.time.LocalDate;

/**
 * El "cerebro" del monedero virtual. La interfaz grafica solo llama a estos metodos.
 *
 * Estructuras globales:
 *  - clientes:            TablaHash<id, Cliente>      acceso O(1) al cliente y a sus puntos
 *  - arbolPuntos:         ABB<id, puntos>             puntos de cada cliente, ordenados por id
 *  - arbolRangos:         AVL<(puntos,id), Cliente>   clientes ordenados por puntos -> rangos/ranking
 *  - programadas:         ColaPrioridad (min-heap)    la proxima transaccion programada sale primero
 *  - pendientes:          Cola                        notificaciones esperando ser entregadas
 *  - grafoTransferencias: GrafoDirigido               quien le transfiere a quien y cuanto
 *  - grafoCategorias:     GrafoDirigido               patron global de gasto entre categorias
 */
public class SistemaMonedero {

    private final TablaHash<String, Cliente> clientes = new TablaHash<>();
    private final ArbolBinarioBusqueda<String, Integer> arbolPuntos = new ArbolBinarioBusqueda<>();
    private final ArbolAVL<ClavePuntos, Cliente> arbolRangos = new ArbolAVL<>();
    private final ColaPrioridad<TransaccionProgramada> programadas =
            new ColaPrioridad<>((a, b) -> {
                int c = a.getProximaFecha().compareTo(b.getProximaFecha());
                return c != 0 ? c : Integer.compare(a.getId(), b.getId());
            });
    private final Cola<Notificacion> pendientes = new Cola<>();
    private final GrafoDirigido<String> grafoTransferencias = new GrafoDirigido<>();
    private final GrafoDirigido<String> grafoCategorias = new GrafoDirigido<>();

    private LocalDate fechaActual;
    private int siguienteIdTransaccion = 1;
    private int siguienteIdProgramada = 1;

    public SistemaMonedero(LocalDate fechaInicial) { this.fechaActual = fechaInicial; }
    public SistemaMonedero() { this(LocalDate.now()); }

    // =====================================================================
    //  CLIENTES Y MONEDEROS
    // =====================================================================

    public Cliente registrarCliente(String id, String nombre) {
        id = texto(id, "El id");
        nombre = texto(nombre, "El nombre");
        if (clientes.contieneClave(id)) throw new OperacionInvalidaException("Ya existe un cliente con id " + id);
        Cliente c = new Cliente(id, nombre);
        clientes.poner(id, c);
        arbolPuntos.insertar(id, 0);
        arbolRangos.insertar(new ClavePuntos(0, id), c);
        grafoTransferencias.agregarVertice(id);
        notificar(c, TipoNotificacion.SISTEMA, "Bienvenido/a " + nombre + ". Tu rango inicial es Bronce.");
        despachar();
        return c;
    }

    public Cliente getCliente(String id) {
        Cliente c = id == null ? null : clientes.obtener(id);
        if (c == null) throw new OperacionInvalidaException("No existe el cliente " + id);
        return c;
    }

    public boolean existeCliente(String id) { return clientes.contieneClave(id); }

    /** Clientes ordenados por id: recorrido in-orden del ABB. */
    public ListaEnlazada<Cliente> listarClientes() {
        ListaEnlazada<Cliente> res = new ListaEnlazada<>();
        for (Par<String, Integer> p : arbolPuntos.inOrden()) res.agregarAlFinal(clientes.obtener(p.getClave()));
        return res;
    }

    public Monedero crearMonedero(String idCliente, String nombre) {
        Cliente c = getCliente(idCliente);
        nombre = texto(nombre, "El nombre del monedero");
        if (c.getMonedero(nombre) != null) throw new OperacionInvalidaException("Ya existe el monedero " + nombre);
        Monedero m = c.agregarMonedero(nombre);
        notificar(c, TipoNotificacion.SISTEMA, "Creaste el monedero '" + nombre + "'.");
        despachar();
        return m;
    }

    // =====================================================================
    //  OPERACIONES BASICAS
    // =====================================================================

    public Transaccion depositar(String idCliente, String monedero, double monto) {
        Cliente c = getCliente(idCliente);
        Monedero m = monedero(c, monedero);
        validarMonto(monto);
        Transaccion t = nueva(TipoTransaccion.DEPOSITO, monto, 0, c.getId(), null, null,
                c.getId(), m.getNombre(), null, "Deposito a " + m.getNombre());
        m.registrar(t, monto);
        registrarEnCliente(c, t, true);
        despachar();
        return t;
    }

    public Transaccion retirar(String idCliente, String monedero, double monto) {
        Cliente c = getCliente(idCliente);
        Monedero m = monedero(c, monedero);
        validarMonto(monto);
        double comision = c.tieneRetirosGratis(fechaActual) ? 0
                : Monedero.redondear(monto * c.getRango().getComisionRetiro());
        exigirSaldo(m, monto + comision);
        Transaccion t = nueva(TipoTransaccion.RETIRO, monto, comision, c.getId(), c.getId(), m.getNombre(),
                null, null, null, "Retiro de " + m.getNombre());
        m.registrar(t, -(monto + comision));
        registrarEnCliente(c, t, true);
        revisarSaldoBajo(c, m);
        despachar();
        return t;
    }

    public Transaccion transferir(String idOrigen, String monederoOrigen, String idDestino,
                                  String monederoDestino, double monto) {
        Cliente o = getCliente(idOrigen);
        Cliente d = getCliente(idDestino);
        if (o == d) throw new OperacionInvalidaException("Para mover dinero entre tus monederos usa 'Entre mis monederos'");
        Monedero mo = monedero(o, monederoOrigen);
        Monedero md = monedero(d, monederoDestino == null ? Cliente.MONEDERO_PRINCIPAL : monederoDestino);
        validarMonto(monto);

        double comision = monto * o.getRango().getComisionTransferencia();
        boolean conDescuento = o.getTransferenciasConDescuento() > 0;
        if (conDescuento) comision *= 0.9;          // beneficio canjeado: -10% de comision
        comision = Monedero.redondear(comision);
        exigirSaldo(mo, monto + comision);
        if (conDescuento) o.setTransferenciasConDescuento(o.getTransferenciasConDescuento() - 1);

        Transaccion t = nueva(TipoTransaccion.TRANSFERENCIA, monto, comision, o.getId(),
                o.getId(), mo.getNombre(), d.getId(), md.getNombre(), null,
                "Transferencia a " + d.getNombre());
        mo.registrar(t, -(monto + comision));
        md.registrar(t, monto);
        registrarEnCliente(o, t, true);
        d.getHistorial().agregarAlFinal(t);        // el destino tambien la ve en su historial
        grafoTransferencias.agregarArista(o.getId(), d.getId(), monto);
        notificar(d, TipoNotificacion.TRANSFERENCIA,
                String.format("Recibiste %.2f de %s en '%s'.", monto, o.getNombre(), md.getNombre()));
        revisarSaldoBajo(o, mo);
        despachar();
        return t;
    }

    /** Multimonedero: mover dinero entre dos monederos del MISMO cliente (sin comision ni puntos). */
    public Transaccion transferirEntreMonederos(String idCliente, String desde, String hacia, double monto) {
        Cliente c = getCliente(idCliente);
        Monedero m1 = monedero(c, desde);
        Monedero m2 = monedero(c, hacia);
        if (m1 == m2) throw new OperacionInvalidaException("Elige dos monederos distintos");
        validarMonto(monto);
        exigirSaldo(m1, monto);
        Transaccion t = nueva(TipoTransaccion.TRANSFERENCIA_INTERNA, monto, 0, c.getId(),
                c.getId(), m1.getNombre(), c.getId(), m2.getNombre(), null,
                m1.getNombre() + " -> " + m2.getNombre());
        m1.registrar(t, -monto);
        m2.registrar(t, monto);
        registrarEnCliente(c, t, true);
        c.getGrafoMonederos().agregarArista(m1.getNombre(), m2.getNombre(), monto);
        revisarSaldoBajo(c, m1);
        despachar();
        return t;
    }

    /** Un gasto es un retiro con categoria (Comida, Transporte...). Alimenta el analisis de patrones. */
    public Transaccion registrarGasto(String idCliente, String monedero, double monto, String categoria) {
        Cliente c = getCliente(idCliente);
        Monedero m = monedero(c, monedero);
        validarMonto(monto);
        categoria = texto(categoria, "La categoria");
        exigirSaldo(m, monto);
        Transaccion t = nueva(TipoTransaccion.GASTO, monto, 0, c.getId(), c.getId(), m.getNombre(),
                null, null, categoria, "Gasto en " + categoria);
        m.registrar(t, -monto);
        registrarEnCliente(c, t, true);
        // patron: categoria anterior -> categoria actual
        c.getGrafoCategorias().agregarVertice(categoria);
        grafoCategorias.agregarVertice(categoria);
        if (c.getUltimaCategoria() != null) {
            c.getGrafoCategorias().agregarArista(c.getUltimaCategoria(), categoria, monto);
            grafoCategorias.agregarArista(c.getUltimaCategoria(), categoria, monto);
        }
        c.setUltimaCategoria(categoria);
        revisarSaldoBajo(c, m);
        despachar();
        return t;
    }

    public double consultarSaldo(String idCliente, String monedero) {
        return monedero(getCliente(idCliente), monedero).getSaldo();
    }

    public double consultarSaldoTotal(String idCliente) { return getCliente(idCliente).saldoTotal(); }

    public ListaEnlazada<Transaccion> consultarHistorial(String idCliente) {
        return getCliente(idCliente).getHistorial();
    }

    // =====================================================================
    //  REVERSION (PILA) Y RECALCULO DE PUNTOS
    // =====================================================================

    /** Deshace la ultima transaccion reversible del cliente (la cima de su pila). */
    public Transaccion revertirUltima(String idCliente) {
        Cliente c = getCliente(idCliente);
        if (c.getReversibles().estaVacia()) throw new OperacionInvalidaException("No hay transacciones para revertir");
        Transaccion t = c.getReversibles().verCima();

        Monedero mOrigen = t.getClienteOrigen() == null ? null
                : getCliente(t.getClienteOrigen()).getMonedero(t.getMonederoOrigen());
        Cliente cDestino = t.getClienteDestino() == null ? null : getCliente(t.getClienteDestino());
        Monedero mDestino = cDestino == null ? null : cDestino.getMonedero(t.getMonederoDestino());

        // Primero comprobamos que se pueda: el destino debe tener todavia el dinero
        if (mDestino != null && mDestino.getSaldo() < t.getMonto())
            throw new OperacionInvalidaException("No se puede revertir: '" + mDestino.getNombre()
                    + "' de " + cDestino.getNombre() + " ya no tiene " + fmt(t.getMonto()));

        c.getReversibles().desapilar();
        Transaccion rev = nueva(TipoTransaccion.REVERSION, t.getMonto(), 0, c.getId(),
                t.getClienteDestino(), t.getMonederoDestino(), t.getClienteOrigen(), t.getMonederoOrigen(),
                t.getCategoria(), "Reversion de #" + t.getId());
        if (mDestino != null) mDestino.registrar(rev, -t.getMonto());
        if (mOrigen != null) mOrigen.registrar(rev, t.getTotalDebitado()); // devuelve tambien la comision
        t.marcarRevertida();
        c.getHistorial().agregarAlFinal(rev);
        if (cDestino != null && cDestino != c) {
            cDestino.getHistorial().agregarAlFinal(rev);
            notificar(cDestino, TipoNotificacion.TRANSFERENCIA,
                    "Se revirtio la transferencia #" + t.getId() + " de " + c.getNombre() + ".");
        }

        if (t.getTipo() == TipoTransaccion.TRANSFERENCIA)
            grafoTransferencias.reducirArista(t.getClienteOrigen(), t.getClienteDestino(), t.getMonto());
        if (t.getTipo() == TipoTransaccion.TRANSFERENCIA_INTERNA)
            c.getGrafoMonederos().reducirArista(t.getMonederoOrigen(), t.getMonederoDestino(), t.getMonto());

        // Los puntos se RECALCULAN desde el historial (requisito del enunciado)
        recalcularPuntos(c, "Reversion de #" + t.getId());
        notificar(c, TipoNotificacion.SISTEMA, "Revertiste la transaccion #" + t.getId()
                + " (" + t.getTipo() + " " + fmt(t.getMonto()) + ").");
        despachar();
        return rev;
    }

    /**
     * Recalcula los puntos acumulados sumando, de forma RECURSIVA, los puntos de
     * todas las transacciones NO revertidas del historial en las que el cliente es titular.
     */
    public int recalcularPuntos(Cliente c, String motivo) {
        int nuevos = sumarPuntosRecursivo(c.getHistorial().getCabeza(), c.getId());
        int diferencia = nuevos - c.getPuntosAcumulados();
        if (diferencia != 0) cambiarPuntos(c, diferencia, motivo);
        return nuevos;
    }

    static int sumarPuntosRecursivo(Nodo<Transaccion> nodo, String idCliente) {
        if (nodo == null) return 0;                         // caso base: fin de la lista
        Transaccion t = nodo.getDato();
        int aqui = (!t.isRevertida() && idCliente.equals(t.getTitular())) ? t.getPuntos() : 0;
        return aqui + sumarPuntosRecursivo(nodo.getSiguiente(), idCliente);
    }

    // =====================================================================
    //  PUNTOS, RANGOS Y CANJES
    // =====================================================================

    /** Puntos = (monto / 100) * factor del tipo, multiplicado por el bono del rango. */
    public static int calcularPuntos(TipoTransaccion tipo, double monto, Rango rango) {
        int base = (int) (monto / 100) * tipo.getPuntosPorCada100();
        return (int) Math.floor(base * rango.getMultiplicadorPuntos());
    }

    /**
     * Cambia los puntos de un cliente y mantiene sincronizadas las 3 estructuras:
     * TablaHash (via el objeto Cliente), ABB de puntos y AVL de rangos.
     * Para el AVL hay que sacar la clave vieja e insertar la nueva: O(log n).
     */
    private void cambiarPuntos(Cliente c, int delta, String motivo) {
        arbolRangos.eliminar(new ClavePuntos(c.getPuntosAcumulados(), c.getId()));
        c.setPuntosAcumulados(c.getPuntosAcumulados() + delta);
        arbolRangos.insertar(new ClavePuntos(c.getPuntosAcumulados(), c.getId()), c);
        arbolPuntos.insertar(c.getId(), c.getPuntosAcumulados());
        c.getHistorialPuntos().agregarAlFinal(
                new RegistroPuntos(fechaActual, delta, motivo, c.getPuntosAcumulados()));

        Rango nuevo = Rango.desdePuntos(c.getPuntosAcumulados());
        if (nuevo != c.getRango()) {
            boolean sube = nuevo.ordinal() > c.getRango().ordinal();
            c.setRango(nuevo);
            notificar(c, TipoNotificacion.RANGO, (sube ? "¡Subiste" : "Bajaste") + " al rango " + nuevo
                    + "! " + nuevo.descripcionBeneficios());
        }
    }

    public Transaccion canjear(String idCliente, Beneficio b) {
        Cliente c = getCliente(idCliente);
        if (c.getPuntosDisponibles() < b.getCosto())
            throw new OperacionInvalidaException("Necesitas " + b.getCosto() + " puntos disponibles y tienes "
                    + c.getPuntosDisponibles());
        c.sumarPuntosCanjeados(b.getCosto());
        c.getHistorialPuntos().agregarAlFinal(new RegistroPuntos(fechaActual, -b.getCosto(),
                "Canje: " + b.getDescripcion(), c.getPuntosAcumulados()));
        Transaccion bono = null;
        switch (b) {
            case DESCUENTO_TRANSFERENCIAS:
                c.setTransferenciasConDescuento(c.getTransferenciasConDescuento() + 10);
                break;
            case RETIROS_SIN_CARGO:
                LocalDate base = c.tieneRetirosGratis(fechaActual) ? c.getRetirosGratisHasta() : fechaActual;
                c.setRetirosGratisHasta(base.plusMonths(1));
                break;
            case BONO_SALDO:
                Monedero m = c.getMonedero(Cliente.MONEDERO_PRINCIPAL);
                bono = nueva(TipoTransaccion.BONO, 50, 0, c.getId(), null, null, c.getId(), m.getNombre(),
                        null, "Bono por canje de puntos");
                m.registrar(bono, 50);
                registrarEnCliente(c, bono, false); // un bono no se puede revertir
                break;
        }
        notificar(c, TipoNotificacion.PUNTOS, "Canjeaste " + b.getCosto() + " puntos: " + b.getDescripcion());
        despachar();
        return bono;
    }

    /** Ranking de mayor a menor puntaje (recorrido descendente del AVL). */
    public ListaEnlazada<Cliente> ranking() { return arbolRangos.valoresDescendente(); }

    /** Clientes de un rango: busqueda por intervalo en el AVL, O(log n + k). */
    public ListaEnlazada<Cliente> clientesDeRango(Rango r) {
        return arbolRangos.buscarEnRango(new ClavePuntos(r.getMinimo(), ""),
                new ClavePuntos(r.getMaximo(), "￿"));
    }

    /** Puntos por id usando el ABB. */
    public int puntosSegunArbol(String id) {
        Integer p = arbolPuntos.buscar(id);
        return p == null ? 0 : p;
    }

    // =====================================================================
    //  TRANSACCIONES PROGRAMADAS (COLA DE PRIORIDAD)
    // =====================================================================

    public TransaccionProgramada programarDeposito(String idCliente, String monedero, double monto,
                                                   LocalDate fecha, int cadaDias, int repeticiones) {
        Cliente c = getCliente(idCliente);
        monedero(c, monedero);
        return programar(new TransaccionProgramada(siguienteIdProgramada++, TipoTransaccion.DEPOSITO,
                c.getId(), null, c.getId(), monedero, monto, fecha, cadaDias, repeticiones));
    }

    public TransaccionProgramada programarTransferencia(String idCliente, String monederoOrigen, String idDestino,
                                                        String monederoDestino, double monto,
                                                        LocalDate fecha, int cadaDias, int repeticiones) {
        Cliente c = getCliente(idCliente);
        monedero(c, monederoOrigen);
        Cliente d = getCliente(idDestino);
        monedero(d, monederoDestino);
        if (c == d && monederoOrigen.equals(monederoDestino))
            throw new OperacionInvalidaException("Origen y destino son el mismo monedero");
        return programar(new TransaccionProgramada(siguienteIdProgramada++, TipoTransaccion.TRANSFERENCIA,
                c.getId(), monederoOrigen, d.getId(), monederoDestino, monto, fecha, cadaDias, repeticiones));
    }

    private TransaccionProgramada programar(TransaccionProgramada p) {
        validarMonto(p.getMonto());
        if (p.getProximaFecha().isBefore(fechaActual))
            throw new OperacionInvalidaException("La fecha no puede ser anterior a hoy (" + fechaActual + ")");
        programadas.insertar(p);
        notificar(getCliente(p.getIdCliente()), TipoNotificacion.PROGRAMADA,
                "Programaste: " + p.descripcion() + " por " + fmt(p.getMonto()) + " desde " + p.getProximaFecha());
        procesarProgramadas(); // por si es para hoy
        despachar();
        return p;
    }

    public boolean cancelarProgramada(int id) {
        for (TransaccionProgramada p : programadas.enOrden()) {
            if (p.getId() == id) {
                programadas.eliminar(p);
                notificar(getCliente(p.getIdCliente()), TipoNotificacion.PROGRAMADA,
                        "Cancelaste la programada #" + id + ".");
                despachar();
                return true;
            }
        }
        return false;
    }

    public ListaEnlazada<TransaccionProgramada> listarProgramadas() { return programadas.enOrden(); }

    public ListaEnlazada<TransaccionProgramada> listarProgramadas(String idCliente) {
        ListaEnlazada<TransaccionProgramada> res = new ListaEnlazada<>();
        for (TransaccionProgramada p : programadas.enOrden())
            if (p.getIdCliente().equals(idCliente)) res.agregarAlFinal(p);
        return res;
    }

    /** Simula el paso del tiempo: ejecuta lo que toque y envia recordatorios. */
    public int avanzarDias(int dias) {
        int ejecutadas = 0;
        for (int i = 0; i < dias; i++) {
            fechaActual = fechaActual.plusDays(1);
            ejecutadas += procesarProgramadas();
            enviarRecordatorios();
        }
        despachar();
        return ejecutadas;
    }

    /** Mientras la mas proxima ya este vencida: sacarla, ejecutarla y reprogramarla si se repite. */
    private int procesarProgramadas() {
        int n = 0;
        while (!programadas.estaVacia() && !programadas.verMinimo().getProximaFecha().isAfter(fechaActual)) {
            TransaccionProgramada p = programadas.extraerMinimo();
            Cliente c = getCliente(p.getIdCliente());
            try {
                if (p.getTipo() == TipoTransaccion.DEPOSITO) {
                    depositar(p.getIdCliente(), p.getMonederoDestino(), p.getMonto());
                } else if (p.getIdCliente().equals(p.getIdDestino())) {
                    transferirEntreMonederos(p.getIdCliente(), p.getMonederoOrigen(), p.getMonederoDestino(), p.getMonto());
                } else {
                    transferir(p.getIdCliente(), p.getMonederoOrigen(), p.getIdDestino(), p.getMonederoDestino(), p.getMonto());
                }
                n++;
                notificar(c, TipoNotificacion.PROGRAMADA, "Se ejecuto la programada #" + p.getId() + ": "
                        + p.descripcion() + " por " + fmt(p.getMonto()) + ".");
            } catch (OperacionInvalidaException e) {
                notificar(c, TipoNotificacion.PROGRAMADA, "No se pudo ejecutar la programada #" + p.getId()
                        + ": " + e.getMessage());
            }
            if (p.reprogramar()) programadas.insertar(p);
        }
        return n;
    }

    private void enviarRecordatorios() {
        LocalDate manana = fechaActual.plusDays(1);
        for (TransaccionProgramada p : programadas.enOrden()) {
            if (p.getProximaFecha().isAfter(manana)) break; // estan ordenadas: no hay mas para mañana
            if (p.getProximaFecha().equals(manana))
                notificar(getCliente(p.getIdCliente()), TipoNotificacion.RECORDATORIO,
                        "Mañana se ejecuta: " + p.descripcion() + " por " + fmt(p.getMonto()) + ".");
        }
    }

    // =====================================================================
    //  NOTIFICACIONES (COLA -> LISTA CIRCULAR)
    // =====================================================================

    private void notificar(Cliente c, TipoNotificacion tipo, String mensaje) {
        pendientes.encolar(new Notificacion(c.getId(), tipo, mensaje, fechaActual));
    }

    /** Entrega en orden de llegada (FIFO) cada notificacion a la bandeja circular de su cliente. */
    private void despachar() {
        while (!pendientes.estaVacia()) {
            Notificacion n = pendientes.desencolar();
            Cliente c = clientes.obtener(n.getIdCliente());
            if (c != null) c.getNotificaciones().agregar(n);
        }
    }

    private void revisarSaldoBajo(Cliente c, Monedero m) {
        if (m.getSaldo() < c.getUmbralSaldoBajo())
            notificar(c, TipoNotificacion.SALDO_BAJO, String.format("El monedero '%s' quedo con saldo bajo: %.2f",
                    m.getNombre(), m.getSaldo()));
    }

    public void cambiarUmbralSaldoBajo(String idCliente, double umbral) {
        if (umbral < 0) throw new OperacionInvalidaException("El umbral no puede ser negativo");
        getCliente(idCliente).setUmbralSaldoBajo(umbral);
    }

    // =====================================================================
    //  AUXILIARES
    // =====================================================================

    private Transaccion nueva(TipoTransaccion tipo, double monto, double comision, String titular,
                              String cOrigen, String mOrigen, String cDestino, String mDestino,
                              String categoria, String descripcion) {
        return new Transaccion(siguienteIdTransaccion++, tipo, Monedero.redondear(monto), comision, fechaActual,
                titular, cOrigen, mOrigen, cDestino, mDestino, categoria, descripcion);
    }

    /** Agrega al historial, a la pila de reversibles y suma los puntos. */
    private void registrarEnCliente(Cliente c, Transaccion t, boolean reversible) {
        t.setPuntos(calcularPuntos(t.getTipo(), t.getMonto(), c.getRango()));
        c.getHistorial().agregarAlFinal(t);
        if (reversible) c.getReversibles().apilar(t);
        if (t.getPuntos() > 0) {
            cambiarPuntos(c, t.getPuntos(), t.getTipo() + " #" + t.getId());
            notificar(c, TipoNotificacion.PUNTOS, "Ganaste " + t.getPuntos() + " puntos por " + t.getTipo()
                    + " de " + fmt(t.getMonto()) + ".");
        }
    }

    private Monedero monedero(Cliente c, String nombre) {
        Monedero m = c.getMonedero(nombre == null ? Cliente.MONEDERO_PRINCIPAL : nombre);
        if (m == null) throw new OperacionInvalidaException(c.getNombre() + " no tiene el monedero " + nombre);
        return m;
    }

    private void validarMonto(double monto) {
        if (!(monto > 0) || Double.isInfinite(monto)) throw new OperacionInvalidaException("El monto debe ser mayor que 0");
    }

    private void exigirSaldo(Monedero m, double necesario) {
        if (m.getSaldo() + 1e-9 < necesario)
            throw new OperacionInvalidaException(String.format("Saldo insuficiente en '%s': tienes %.2f y necesitas %.2f",
                    m.getNombre(), m.getSaldo(), necesario));
    }

    private static String texto(String s, String campo) {
        if (s == null || s.trim().isEmpty()) throw new OperacionInvalidaException(campo + " no puede estar vacio");
        return s.trim();
    }

    static String fmt(double v) { return String.format("%.2f", v); }

    // ---- getters para reportes e interfaz ----
    public LocalDate getFechaActual() { return fechaActual; }
    public GrafoDirigido<String> getGrafoTransferencias() { return grafoTransferencias; }
    public GrafoDirigido<String> getGrafoCategorias() { return grafoCategorias; }
    public ArbolAVL<ClavePuntos, Cliente> getArbolRangos() { return arbolRangos; }
    public ArbolBinarioBusqueda<String, Integer> getArbolPuntos() { return arbolPuntos; }
    public int cantidadClientes() { return clientes.tamanio(); }
}
