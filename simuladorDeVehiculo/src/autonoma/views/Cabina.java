package autonoma.views;

import autonoma.exceptions.CarroAccidentadoException;
import autonoma.exceptions.CarroApagadoException;
import autonoma.exceptions.CarroDetenidoException;
import autonoma.exceptions.CarroHaPatinadoException;
import autonoma.exceptions.CarroYaApagadoException;
import autonoma.exceptions.CarroYaEncendidoException;
import autonoma.models.Carro;
import autonoma.models.PerfilVehiculo;
import autonoma.sounds.ReproductorAudio;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

/** Ventana de presentación y controles; no contiene reglas de simulación. */
public final class Cabina extends JFrame {
    private final PerfilVehiculo perfil;
    private final Carro carro;
    private final ReproductorAudio audio = new ReproductorAudio();
    private final JPanel interior;
    private final JLabel estado = new JLabel("MOTOR APAGADO", SwingConstants.CENTER);
    private final JLabel marcha = new JLabel("Marcha 1", SwingConstants.CENTER);
    private final JButton subirMarcha = new JButton("＋ Marcha");
    private final JButton bajarMarcha = new JButton("－ Marcha");

    public Cabina(PerfilVehiculo perfil) {
        this.perfil = perfil;
        this.carro = perfil.getCarro();
        setTitle("CarSpot · " + perfil.getNombre());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 690));
        setSize(1120, 780);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(new Color(16, 20, 23));
        root.setBorder(BorderFactory.createEmptyBorder(22, 28, 24, 28));
        root.add(crearCabecera(), BorderLayout.NORTH);
        interior = InteriorFactory.crear(perfil, carro);
        root.add(interior, BorderLayout.CENTER);
        root.add(crearControles(), BorderLayout.SOUTH);
        setContentPane(root);

        Timer refresco = new Timer(100, (ActionEvent e) -> actualizarTablero());
        refresco.start();
        actualizarTablero();
    }

    private JPanel crearCabecera() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel nombre = new JLabel(perfil.getNombre());
        nombre.setForeground(Color.WHITE);
        nombre.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        JLabel configuracion = new JLabel("Motor " + carro.getMotor().getVelocidadMaxima() + " km/h   ·   "
                + carro.getTipoTransmision() + "   ·   Interior " + perfil.getInterior());
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
        JButton acelerar = boton("Acelerar", new Color(51, 117, 171));
        JButton frenar = boton("Frenar", new Color(112, 117, 121));
        JButton informacion = boton("Estado", new Color(65, 75, 82));
        subirMarcha.setEnabled(carro.isManual());
        bajarMarcha.setEnabled(carro.isManual());
        estilizar(subirMarcha);
        estilizar(bajarMarcha);

        encender.addActionListener(e -> ejecutar(() -> { carro.encender(); audio.reproducir("startEngine.wav"); }));
        apagar.addActionListener(e -> ejecutar(() -> { carro.apagar(); audio.reproducir("stopEngine.wav"); }));
        acelerar.addActionListener(e -> pedirCantidad("¿Cuántos km/h desea acelerar?", cantidad -> {
            carro.acelerar(cantidad);
            audio.reproducir("engineAccelerating.wav");
        }));
        frenar.addActionListener(e -> pedirCantidad("¿Cuántos km/h desea frenar?", cantidad -> {
            try {
                carro.frenar(cantidad);
                audio.reproducir("softBrake.wav");
            } catch (CarroHaPatinadoException error) {
                audio.reproducir("brake.wav");
                throw error;
            }
        }));
        subirMarcha.addActionListener(e -> cambiarMarcha(1));
        bajarMarcha.addActionListener(e -> cambiarMarcha(-1));
        informacion.addActionListener(e -> mostrarEstado());
        botones.add(encender); botones.add(apagar); botones.add(acelerar); botones.add(frenar);
        if (carro.isManual()) { botones.add(bajarMarcha); botones.add(subirMarcha); }
        botones.add(informacion);
        contenedor.add(botones, BorderLayout.CENTER);
        return contenedor;
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

    private void cambiarMarcha(int direccion) {
        try {
            carro.cambiarMarcha(direccion);
            actualizarTablero();
        } catch (IllegalStateException error) {
            mostrarError(error.getMessage());
        }
    }

    private void pedirCantidad(String pregunta, AccionCantidad accion) {
        String valor = JOptionPane.showInputDialog(this, pregunta, "Control del vehículo", JOptionPane.QUESTION_MESSAGE);
        if (valor == null) return;
        try {
            accion.ejecutar(Integer.parseInt(valor.trim()));
            actualizarTablero();
        } catch (NumberFormatException error) {
            mostrarError("Ingrese una cantidad entera válida.");
        } catch (Exception error) {
            if (carro.seHaAccidentado()) audio.reproducir("accident.wav");
            mostrarError(error.getMessage());
        }
    }

    private void ejecutar(AccionVehiculo accion) {
        try {
            accion.ejecutar();
            actualizarTablero();
        } catch (Exception error) {
            if (carro.seHaAccidentado()) audio.reproducir("accident.wav");
            mostrarError(error.getMessage());
        }
    }

    private void actualizarTablero() {
        IndicadorCircular velocidad = (IndicadorCircular) interior.getClientProperty("medidor.velocidad");
        IndicadorCircular rpm = (IndicadorCircular) interior.getClientProperty("medidor.rpm");
        JLabel datosTransmision = (JLabel) interior.getClientProperty("etiqueta.transmision");
        velocidad.setValor(carro.getVelocidadActual());
        rpm.setValor(carro.getRevoluciones());
        estado.setText(carro.isEncendido() ? (carro.seHaAccidentado() ? "VEHÍCULO ACCIDENTADO" : "MOTOR ENCENDIDO") : "MOTOR APAGADO");
        estado.setForeground(carro.seHaAccidentado() ? new Color(255, 119, 102) : new Color(123, 212, 168));
        marcha.setText("Marcha " + carro.getMarchaActual() + "   ·   " + carro.getRevoluciones() + " rpm");
        datosTransmision.setText("Marcha " + carro.getMarchaActual() + "   ·   " + carro.getTipoTransmision());
    }

    private void mostrarEstado() {
        JOptionPane.showMessageDialog(this,
                "Vehículo: " + perfil.getNombre() + "\nMotor encendido: " + carro.isEncendido()
                + "\nVelocidad: " + carro.getVelocidadActual() + " km/h\nRevoluciones: " + carro.getRevoluciones()
                + " rpm\nTransmisión: " + carro.getTipoTransmision() + "\nMarcha: " + carro.getMarchaActual()
                + "\nPatinando: " + carro.haPatinado() + "\nAccidentado: " + carro.seHaAccidentado(),
                "Estado del vehículo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje == null ? "No se pudo realizar la acción." : mensaje,
                "Acción no disponible", JOptionPane.WARNING_MESSAGE);
    }

    @FunctionalInterface private interface AccionVehiculo { void ejecutar() throws Exception; }
    @FunctionalInterface private interface AccionCantidad { void ejecutar(int cantidad) throws Exception; }
}
