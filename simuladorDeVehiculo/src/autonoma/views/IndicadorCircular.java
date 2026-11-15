package autonoma.views;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;

/** Medidor reutilizable para velocidad y revoluciones. */
public final class IndicadorCircular extends JComponent {
    private final String titulo;
    private final String unidad;
    private final int maximo;
    private final Color acento;
    private int valor;

    public IndicadorCircular(String titulo, String unidad, int maximo, Color acento) {
        this.titulo = titulo;
        this.unidad = unidad;
        this.maximo = maximo;
        this.acento = acento;
        setPreferredSize(new Dimension(220, 220));
        setOpaque(false);
    }

    public void setValor(int nuevoValor) {
        valor = Math.max(0, Math.min(maximo, nuevoValor));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int lado = Math.min(getWidth(), getHeight()) - 18;
        int x = (getWidth() - lado) / 2;
        int y = (getHeight() - lado) / 2;
        g.setColor(new Color(12, 16, 20, 235));
        g.fillOval(x, y, lado, lado);
        g.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(255, 255, 255, 35));
        g.drawArc(x + 13, y + 13, lado - 26, lado - 26, 225, -270);
        int barrido = (int) Math.round(-270.0 * valor / maximo);
        g.setColor(valor > maximo * 0.88 ? new Color(244, 76, 67) : acento);
        g.drawArc(x + 13, y + 13, lado - 26, lado - 26, 225, barrido);

        g.setColor(new Color(255, 255, 255, 170));
        g.setFont(getFont().deriveFont(Font.BOLD, 12f));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(titulo, (getWidth() - fm.stringWidth(titulo)) / 2, y + 43);
        g.setColor(Color.WHITE);
        g.setFont(getFont().deriveFont(Font.BOLD, lado * 0.20f));
        String lectura = Integer.toString(valor);
        fm = g.getFontMetrics();
        g.drawString(lectura, (getWidth() - fm.stringWidth(lectura)) / 2, y + lado / 2 + 12);
        g.setColor(new Color(255, 255, 255, 145));
        g.setFont(getFont().deriveFont(Font.PLAIN, 12f));
        fm = g.getFontMetrics();
        g.drawString(unidad, (getWidth() - fm.stringWidth(unidad)) / 2, y + lado / 2 + 35);
        g.dispose();
    }
}
