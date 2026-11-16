package autonoma.views;

import autonoma.models.Carro;
import autonoma.sounds.ReproductorAudio;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.Timer;

/** Ventana de presentación y controles; no contiene reglas de simulación. */
public final class Cabina extends JFrame {
    private final Carro carro;
    private final ReproductorAudio audio = new ReproductorAudio();
    private final JPanel tablero;
    private final JLabel estado = new JLabel("MOTOR APAGADO", SwingConstants.CENTER);
    private final JLabel marcha = new JLabel("Marcha 1", SwingConstants.CENTER);
    private final JButton subirMarcha = new JButton("＋ Marcha");
    private final JButton bajarMarcha = new JButton("－ Marcha");

    public Cabina(Carro carro) {
        this.carro = carro;
        setTitle("CarSpot · " + carro.getNombre());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 690));
        setSize(1120, 780);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(new Color(16, 20, 23));
        root.setBorder(BorderFactory.createEmptyBorder(22, 28, 24, 28));
        root.add(crearCabecera(), BorderLayout.NORTH);
        tablero = InteriorFactory.crear(carro);
        root.add(tablero, BorderLayout.CENTER);
        root.add(crearControles(), BorderLayout.SOUTH);
        configurarAtajosTeclado(root);
        setContentPane(root);

        final long[] instanteAnterior = {System.nanoTime()};
        Timer refresco = new Timer(20, (ActionEvent e) -> {
            long ahora = System.nanoTime();
            double dt = (ahora - instanteAnterior[0]) / 1_000_000_000.0;
            instanteAnterior[0] = ahora;
            if (carro.isEncendido() || carro.getVelocidadMps() > 0.01) {
                carro.actualizar(dt);
            }
            actualizarTablero();
        });
        refresco.start();
        actualizarTablero();
    }

    private JPanel crearCabecera() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel nombre = new JLabel(carro.getNombre());
        nombre.setForeground(Color.WHITE);
        nombre.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        JLabel configuracion = new JLabel(carro.getMotor().getPotenciaMaximaKw() + " kW   ·   "
                + carro.getMotor().getVelocidadMaxima() + " km/h máx.   ·   "
                + carro.getTipoTransmision());
        configuracion.setForeground(new Color(163, 174, 179));
        configuracion.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        panel.add(nombre, BorderLayout.WEST);
        panel.add(configuracion, BorderLayout.SOUTH);
        estado.setForeground(new Color(123, 212, 168));
        estado.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        panel.add(estado, BorderLayout.EAST);
        return panel;
    }

    private JPanel crearControles() {
        JPanel contenedor = new JPanel(new BorderLayout(0, 10));
        contenedor.setOpaque(false);
        marcha.setForeground(Color.WHITE);
        marcha.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        contenedor.add(marcha, BorderLayout.NORTH);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        botones.setOpaque(false);
        JButton encender = boton("Encender", new Color(47, 154, 107));
        JButton apagar = boton("Apagar", new Color(174, 66, 60));
        JButton acelerar = boton("Acelerador · mantener", new Color(51, 117, 171));
        JButton frenar = boton("Freno · mantener", new Color(112, 117, 121));
        JButton informacion = boton("Estado", new Color(65, 75, 82));
        subirMarcha.setEnabled(carro.isManual());
        bajarMarcha.setEnabled(carro.isManual());
        estilizar(subirMarcha);
        estilizar(bajarMarcha);

        encender.addActionListener(e -> {
            if (!carro.isEncendido()) {
                carro.encender();
                audio.reproducir("startEngine.wav");
                actualizarTablero();
            }
        });
        apagar.addActionListener(e -> {
            if (carro.isEncendido()) {
                carro.apagar();
                audio.reproducir("stopEngine.wav");
                actualizarTablero();
            }
        });
        controlarPedal(acelerar, true);
        controlarPedal(frenar, false);
        subirMarcha.addActionListener(e -> cambiarMarcha(1));
        bajarMarcha.addActionListener(e -> cambiarMarcha(-1));
        informacion.addActionListener(e -> mostrarEstado());
        botones.add(encender); botones.add(apagar); botones.add(acelerar); botones.add(frenar);
        if (carro.isManual()) { botones.add(bajarMarcha); botones.add(subirMarcha); }
        botones.add(informacion);
        contenedor.add(botones, BorderLayout.CENTER);
        return contenedor;
    }

    private void configurarAtajosTeclado(JPanel root) {
        if (!carro.isManual()) return;
        InputMap entradas = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap acciones = root.getActionMap();

        entradas.put(KeyStroke.getKeyStroke('+'), "subirMarcha");
        entradas.put(KeyStroke.getKeyStroke('='), "subirMarcha");
        entradas.put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0), "subirMarcha");
        entradas.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0), "subirMarcha");

        entradas.put(KeyStroke.getKeyStroke('-'), "bajarMarcha");
        entradas.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), "bajarMarcha");
        entradas.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0), "bajarMarcha");

        acciones.put("subirMarcha", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cambiarMarcha(1);
            }
        });
        acciones.put("bajarMarcha", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cambiarMarcha(-1);
            }
        });
    }

    private void cambiarMarcha(int direccion) {
        carro.cambiarMarcha(direccion);
        actualizarTablero();
    }

    private JButton boton(String texto, Color fondo) {
        JButton boton = new JButton(texto);
        estilizar(boton);
        boton.setBackground(fondo);
        return boton;
    }

    private void estilizar(JButton boton) {
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        boton.setBorder(BorderFactory.createEmptyBorder(11, 18, 11, 18));
    }

    private void controlarPedal(JButton boton, boolean esAcelerador) {
        boton.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent evento) {
                if (esAcelerador) carro.setAcelerador(1);
                else carro.setFreno(1);
            }

            @Override public void mouseReleased(MouseEvent evento) { soltarPedal(esAcelerador); }
            @Override public void mouseExited(MouseEvent evento) {
                if ((evento.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0) soltarPedal(esAcelerador);
            }
        });
    }

    private void soltarPedal(boolean esAcelerador) {
        if (esAcelerador) carro.setAcelerador(0);
        else carro.setFreno(0);
    }

    private void actualizarTablero() {
        IndicadorCircular velocidad = (IndicadorCircular) tablero.getClientProperty("medidor.velocidad");
        IndicadorCircular rpm = (IndicadorCircular) tablero.getClientProperty("medidor.rpm");
        JLabel datosTransmision = (JLabel) tablero.getClientProperty("etiqueta.transmision");
        velocidad.setValor(carro.getVelocidadActual());
        rpm.setValor(carro.getRevoluciones());
        estado.setText(carro.isEncendido() ? "MOTOR ENCENDIDO" : "MOTOR APAGADO");
        estado.setForeground(carro.isEncendido() ? new Color(123, 212, 168) : new Color(163, 174, 179));
        marcha.setText("Marcha " + carro.getMarchaActual() + "   ·   " + carro.getRevoluciones() + " rpm");
        datosTransmision.setText("Marcha " + carro.getMarchaActual() + "   ·   " + carro.getTipoTransmision());
    }

    private void mostrarEstado() {
        JOptionPane.showMessageDialog(this,
                "Vehículo: " + carro.getNombre() + "\nMotor encendido: " + carro.isEncendido()
                + "\nVelocidad: " + carro.getVelocidadActual() + " km/h\nRevoluciones: " + carro.getRevoluciones()
                + " rpm\nTransmisión: " + carro.getTipoTransmision() + "\nMarcha: " + carro.getMarchaActual(),
                "Estado del vehículo", JOptionPane.INFORMATION_MESSAGE);
    }
}
