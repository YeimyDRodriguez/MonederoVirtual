package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/* 
JTable que no deja editar las celdas. 
*/
public class TablaSoloLectura extends JTable {
    private final DefaultTableModel modelo;

    public TablaSoloLectura(String... columnas) {
        modelo = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        setModel(modelo);
        Tema.estilizarTabla(this);
        setAutoCreateRowSorter(false);
        getTableHeader().setReorderingAllowed(false);
    }

    public void limpiar() { modelo.setRowCount(0); }
    public void agregarFila(Object... valores) { modelo.addRow(valores); }
    public JScrollPane conScroll() { return Tema.scroll(this); }
}
