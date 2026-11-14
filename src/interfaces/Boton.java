package interfaces;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Boton plano con esquinas redondeadas. Se dibuja a mano en paintComponent.
 * Tres estilos: primario (relleno de color), secundario (blanco con borde) y peligro (rojo).
 */
public class Boton extends JButton {
    public enum Estilo { PRIMARIO, SECUNDARIO, PELIGRO }

    private Estilo estilo;
    private boolean encima; // el mouse esta sobre el boton

    public Boton(String texto) { this(texto, Estilo.PRIMARIO); }

    public Boton(String texto, Estilo estilo) {
        super(texto);
        this.estilo = estilo;
        setFont(Tema.NEGRITA);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        setForeground(estilo == Estilo.SECUNDARIO ? Tema.TEXTO : Color.WHITE);
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { encima = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { encima = false; repaint(); }
        });
    }

    public void setEstilo(Estilo e) {
        estilo = e;
        setForeground(e == Estilo.SECUNDARIO ? Tema.TEXTO : Color.WHITE);
        repaint();
    }

    public static Boton secundario(String texto) { return new Boton(texto, Estilo.SECUNDARIO); }
    public static Boton peligro(String texto) { return new Boton(texto, Estilo.PELIGRO); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color fondo, borde = null;
        switch (estilo) {
            case SECUNDARIO:
                fondo = encima ? Tema.FILA_ALTERNA : Color.WHITE;
                borde = Tema.BORDE;
                break;
            case PELIGRO:
                fondo = encima ? Tema.PELIGRO.darker() : Tema.PELIGRO;
                break;
            default:
                fondo = encima ? Tema.PRIMARIO_OSC : Tema.PRIMARIO;
        }
        if (!isEnabled()) fondo = Tema.BORDE;
        g2.setColor(fondo);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
        if (borde != null) {
            g2.setColor(borde);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
        }
        g2.dispose();
        super.paintComponent(g); // dibuja el texto encima
    }
}