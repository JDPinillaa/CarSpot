package autonoma.views;

import autonoma.models.Carro;
import autonoma.models.PerfilVehiculo;
import java.awt.BorderLayout;
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

/** Selecciona la composición del tablero según el interior configurado. */
public final class InteriorFactory {
    private InteriorFactory() { }

    public static JPanel crear(PerfilVehiculo perfil, Carro carro) {
        boolean deportivo = perfil.getInterior().equals("deportivo");
        Color acento = deportivo ? new Color(255, 113, 49) : new Color(69, 190, 179);
        int tamano = deportivo ? 258 : 218;
        JPanel interior = new FondoInterior(deportivo);
        interior.setLayout(new GridBagLayout());
        interior.setBorder(BorderFactory.createEmptyBorder(28, 32, 26, 32));

        JLabel titulo = new JLabel(deportivo ? "PUESTO DE CONDUCCIÓN DEPORTIVO" : "CABINA CLÁSICA");
        titulo.setForeground(new Color(225, 230, 232));
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2; c.insets = new Insets(0, 0, 20, 0);
        interior.add(titulo, c);

        IndicadorCircular velocidad = new IndicadorCircular("VELOCIDAD", "km/h", carro.getMotor().getVelocidadMaxima(), acento);
        IndicadorCircular rpm = new IndicadorCircular("REVOLUCIONES", "rpm", carro.getRevolucionesMaximas(), acento);
        Dimension dimension = new Dimension(tamano, tamano);
        velocidad.setPreferredSize(dimension);
        rpm.setPreferredSize(dimension);
        if (deportivo) {
            c = new GridBagConstraints(); c.gridx = 0; c.gridy = 1; c.insets = new Insets(0, 0, 0, 14);
            interior.add(rpm, c);
            c = new GridBagConstraints(); c.gridx = 1; c.gridy = 1; c.insets = new Insets(0, 14, 0, 0);
            interior.add(velocidad, c);
        } else {
            c = new GridBagConstraints(); c.gridx = 0; c.gridy = 1; c.insets = new Insets(0, 0, 0, 24);
            interior.add(velocidad, c);
            c = new GridBagConstraints(); c.gridx = 1; c.gridy = 1; c.insets = new Insets(0, 24, 0, 0);
            interior.add(rpm, c);
        }
        JPanel datos = new JPanel();
        datos.setOpaque(false);
        JLabel transmision = new JLabel("Marcha: " + carro.getMarchaActual() + "   ·   " + carro.getTipoTransmision());
        transmision.setForeground(new Color(220, 226, 229));
        transmision.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        datos.add(transmision);
        c = new GridBagConstraints(); c.gridx = 0; c.gridy = 2; c.gridwidth = 2; c.insets = new Insets(18, 0, 0, 0);
        interior.add(datos, c);
        interior.putClientProperty("medidor.velocidad", velocidad);
        interior.putClientProperty("medidor.rpm", rpm);
        interior.putClientProperty("etiqueta.transmision", transmision);
        return interior;
    }

    private static final class FondoInterior extends JPanel {
        private final boolean deportivo;
        private FondoInterior(boolean deportivo) {
            this.deportivo = deportivo;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            Color arriba = deportivo ? new Color(51, 37, 35) : new Color(49, 57, 59);
            Color abajo = deportivo ? new Color(18, 20, 24) : new Color(24, 30, 32);
            g.setPaint(new GradientPaint(0, 0, arriba, 0, getHeight(), abajo));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);
            g.setColor(deportivo ? new Color(255, 113, 49, 110) : new Color(69, 190, 179, 80));
            g.fillRoundRect(0, 0, getWidth(), 7, 7, 7);
            g.dispose();
            super.paintComponent(graphics);
        }
    }
}
