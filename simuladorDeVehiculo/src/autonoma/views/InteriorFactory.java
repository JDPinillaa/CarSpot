package autonoma.views;

import autonoma.models.Carro;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Construye el panel del tablero de instrumentos del vehículo. */
public final class InteriorFactory {
    private InteriorFactory() { }

    public static JPanel crear(Carro carro) {
        Color acento = new Color(255, 113, 49);
        int tamano = 258;
        JPanel tablero = new FondoTablero();
        tablero.setLayout(new GridBagLayout());
        tablero.setBorder(BorderFactory.createEmptyBorder(28, 32, 26, 32));

        JLabel titulo = new JLabel("TABLERO DE INSTRUMENTOS");
        titulo.setForeground(new Color(225, 230, 232));
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2; c.insets = new Insets(0, 0, 20, 0);
        tablero.add(titulo, c);

        IndicadorCircular velocidad = new IndicadorCircular("VELOCIDAD", "km/h", carro.getMotor().getVelocidadMaxima(), acento);
        IndicadorCircular rpm = new IndicadorCircular("REVOLUCIONES", "rpm", carro.getRevolucionesMaximas(), acento);
        Dimension dimension = new Dimension(tamano, tamano);
        velocidad.setPreferredSize(dimension);
        rpm.setPreferredSize(dimension);

        c = new GridBagConstraints(); c.gridx = 0; c.gridy = 1; c.insets = new Insets(0, 0, 0, 14);
        tablero.add(rpm, c);
        c = new GridBagConstraints(); c.gridx = 1; c.gridy = 1; c.insets = new Insets(0, 14, 0, 0);
        tablero.add(velocidad, c);

        JPanel datos = new JPanel();
        datos.setOpaque(false);
        JLabel transmision = new JLabel("Marcha: " + carro.getMarchaActual() + "   ·   " + carro.getTipoTransmision());
        transmision.setForeground(new Color(220, 226, 229));
        transmision.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        datos.add(transmision);
        c = new GridBagConstraints(); c.gridx = 0; c.gridy = 2; c.gridwidth = 2; c.insets = new Insets(18, 0, 0, 0);
        tablero.add(datos, c);
        tablero.putClientProperty("medidor.velocidad", velocidad);
        tablero.putClientProperty("medidor.rpm", rpm);
        tablero.putClientProperty("etiqueta.transmision", transmision);
        return tablero;
    }

    private static final class FondoTablero extends JPanel {
        private FondoTablero() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            Color arriba = new Color(51, 37, 35);
            Color abajo = new Color(18, 20, 24);
            g.setPaint(new GradientPaint(0, 0, arriba, 0, getHeight(), abajo));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);
            g.setColor(new Color(255, 113, 49, 110));
            g.fillRoundRect(0, 0, getWidth(), 7, 7, 7);
            g.dispose();
            super.paintComponent(graphics);
        }
    }
}
