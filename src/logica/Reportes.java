package logica;

import estructuras.GrafoDirigido;
import estructuras.ListaEnlazada;
import estructuras.Par;
import estructuras.TablaHash;
import modelo.Cliente;
import modelo.Rango;
import modelo.TipoTransaccion;
import modelo.Transaccion;

public class Reportes {
    private Reportes() {}

    @SuppressWarnings("unchecked")
    public static Par<String, Double>[] gastosPorCategoria(Cliente c) {
        TablaHash<String, Double> suma = new TablaHash<>();
        for (Transaccion t : c.getHistorial())
            if (t.getTipo() == TipoTransaccion.GASTO && !t.isRevertida())
                suma.poner(t.getCategoria(), suma.obtenerPorDefecto(t.getCategoria(), 0.0) + t.getMonto());
        ListaEnlazada<String> cats = suma.claves();
        Par<String, Double>[] arr = new Par[cats.tamanio()];
        int i = 0;
        for (String cat : cats) arr[i++] = new Par<>(cat, suma.obtener(cat));
        Ordenamiento.mergeSort(arr, (a, b) -> Double.compare(b.getValor(), a.getValor()));
        return arr;
    }

    public static String reporteCliente(SistemaMonedero s, String idCliente) {
        Cliente c = s.getCliente(idCliente);
        StringBuilder sb = new StringBuilder();
        sb.append("=== REPORTE DE ").append(c.getNombre().toUpperCase()).append(" (").append(c.getId()).append(") ===\n");
        sb.append("Fecha: ").append(s.getFechaActual()).append("\n");
        sb.append("Rango: ").append(c.getRango()).append("  |  Puntos acumulados: ").append(c.getPuntosAcumulados())
                .append("  |  Disponibles: ").append(c.getPuntosDisponibles()).append("\n");
        sb.append("Saldo total: ").append(SistemaMonedero.fmt(c.saldoTotal())).append("\n\n");

        TablaHash<TipoTransaccion, Double> porTipo = new TablaHash<>();
        double comisiones = 0;
        int validas = 0;
        for (Transaccion t : c.getHistorial()) {
            if (t.isRevertida() || !c.getId().equals(t.getTitular())) continue;
            validas++;
            porTipo.poner(t.getTipo(), porTipo.obtenerPorDefecto(t.getTipo(), 0.0) + t.getMonto());
            comisiones += t.getComision();
        }
        sb.append("-- Movimientos realizados (").append(validas).append(") --\n");
        for (TipoTransaccion tipo : TipoTransaccion.values())
            if (porTipo.contieneClave(tipo))
                sb.append(String.format("  %-18s %10.2f%n", tipo, porTipo.obtener(tipo)));
        sb.append(String.format("  %-18s %10.2f%n", "Comisiones pagadas", comisiones));

        Par<String, Double>[] gastos = gastosPorCategoria(c);
        double totalGastos = 0;
        for (Par<String, Double> p : gastos) totalGastos += p.getValor();
        sb.append("\n-- Gastos por categoria (de mayor a menor) --\n");
        if (gastos.length == 0) sb.append("  Sin gastos registrados\n");
        for (Par<String, Double> p : gastos)
            sb.append(String.format("  %-15s %10.2f  (%5.1f%%) %s%n", p.getClave(), p.getValor(),
                    100 * p.getValor() / totalGastos, barra(p.getValor() / totalGastos)));

        sb.append("\n-- Patrones de gasto (despues de X, gasta en Y) --\n");
        sb.append(textoAristas(c.getGrafoCategorias(), "veces"));

        sb.append("\n-- Monederos --\n");
        for (String n : c.getNombresMonederos())
            sb.append(String.format("  %-15s %10.2f%n", n, c.getMonedero(n).getSaldo()));
        sb.append("Movimientos entre monederos (grafo dirigido):\n");
        sb.append(textoAristas(c.getGrafoMonederos(), "movimientos"));

        GrafoDirigido<String> g = s.getGrafoTransferencias();
        sb.append("\n-- Transferencias --\n");
        sb.append(String.format("  Enviado: %.2f   Recibido: %.2f%n", g.pesoSalida(c.getId()), g.pesoEntrada(c.getId())));
        for (GrafoDirigido.Arista<String> a : g.aristasDe(c.getId()))
            sb.append(String.format("  -> %-10s %10.2f en %d transferencia(s)%n", a.getDestino(), a.getPeso(), a.getCantidad()));
        ListaEnlazada<String> alcanzables = g.recorridoAnchura(c.getId());
        alcanzables.eliminarPrimero();
        sb.append("  Su dinero puede llegar (directa o indirectamente) a: ")
                .append(alcanzables.estaVacia() ? "nadie" : alcanzables.toString()).append("\n");
        return sb.toString();
    }

    public static String reporteGeneral(SistemaMonedero s) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== REPORTE GENERAL DEL SISTEMA (").append(s.getFechaActual()).append(") ===\n");
        sb.append("Clientes: ").append(s.cantidadClientes())
                .append("  |  Altura ABB de puntos: ").append(s.getArbolPuntos().altura())
                .append("  |  Altura AVL de rangos: ").append(s.getArbolRangos().altura())
                .append(s.getArbolRangos().estaBalanceado() ? " (balanceado)" : " (DESBALANCEADO)").append("\n\n");

        sb.append("-- Ranking por puntos --\n");
        int pos = 1;
        for (Cliente c : s.ranking())
            sb.append(String.format("  %2d. %-20s %6d pts  %s%n", pos++, c.getNombre(), c.getPuntosAcumulados(), c.getRango()));

        sb.append("\n-- Clientes por rango --\n");
        for (Rango r : Rango.values()) {
            ListaEnlazada<Cliente> l = s.clientesDeRango(r);
            sb.append(String.format("  %-8s (%d): ", r, l.tamanio()));
            for (Cliente c : l) sb.append(c.getNombre()).append("; ");
            sb.append("\n");
        }

        GrafoDirigido<String> g = s.getGrafoTransferencias();
        sb.append("\n-- Red de transferencias entre clientes (grafo dirigido) --\n");
        sb.append("  Vertices: ").append(g.cantidadVertices()).append("  Aristas: ").append(g.cantidadAristas()).append("\n");
        sb.append(textoAristas(g, "transferencias"));
        String mayorReceptor = null, mayorEmisor = null;
        double maxR = 0, maxE = 0;
        for (String v : g.vertices()) {
            if (g.pesoEntrada(v) > maxR) { maxR = g.pesoEntrada(v); mayorReceptor = v; }
            if (g.pesoSalida(v) > maxE) { maxE = g.pesoSalida(v); mayorEmisor = v; }
        }
        if (mayorEmisor != null) sb.append(String.format("  Mayor emisor: %s (%.2f)%n", mayorEmisor, maxE));
        if (mayorReceptor != null) sb.append(String.format("  Mayor receptor: %s (%.2f)%n", mayorReceptor, maxR));
        sb.append("  Relaciones mutuas: ");
        boolean alguna = false;
        for (String u : g.vertices())
            for (GrafoDirigido.Arista<String> a : g.aristasDe(u))
                if (u.compareTo(a.getDestino()) < 0 && g.obtenerArista(a.getDestino(), u) != null) {
                    sb.append(u).append(" <-> ").append(a.getDestino()).append("; ");
                    alguna = true;
                }
        sb.append(alguna ? "" : "ninguna").append("\n");
        sb.append("  Hay ciclos de dinero (A->B->...->A): ").append(g.tieneCiclo() ? "SI" : "NO").append("\n");

        sb.append("\n-- Patrones globales entre categorias de gasto --\n");
        sb.append(textoAristas(s.getGrafoCategorias(), "veces"));
        return sb.toString();
    }

    private static String textoAristas(GrafoDirigido<String> g, String unidad) {
        StringBuilder sb = new StringBuilder();
        for (String u : g.vertices())
            for (GrafoDirigido.Arista<String> a : g.aristasDe(u))
                sb.append(String.format("  %s -> %s : %.2f (%d %s)%n", u, a.getDestino(), a.getPeso(), a.getCantidad(), unidad));
        return sb.length() == 0 ? "  (sin datos)\n" : sb.toString();
    }

    private static String barra(double fraccion) {
        int n = (int) Math.round(fraccion * 20);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append('#');
        return sb.toString();
    }
}

