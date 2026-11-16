package autonoma.models;

import autonoma.models.fisica.ModeloAdherenciaLongitudinal;
import autonoma.models.fisica.ResistenciaCarretera;
import autonoma.models.motores.Motor;
import autonoma.models.transmisiones.CajaCambios;

/** Dinámica longitudinal SI del vehículo; no depende de Swing ni de audio. */
public class Carro {
    private static final double RETARDO_CAMBIO_S = 0.16; // estimación de una DSG con doble embrague
    private static final double FRACCION_PAR_DURANTE_CAMBIO = 0.75; // transferencia de par estimada

    private final Motor motor;
    private final CajaCambios cajaCambios;
    private final double masaKg;
    private final ResistenciaCarretera resistenciaCarretera;
    private final ModeloAdherenciaLongitudinal modeloAdherencia;
    private final double radioRuedaM;
    private boolean encendido;
    private double velocidadMps;
    private double acelerador;
    private double freno;
    private double revoluciones;
    private double tiempoCambioRestante;

    public Carro(Motor motor, CajaCambios cajaCambios, double masaKg, double radioRuedaM,
            ResistenciaCarretera resistenciaCarretera,
            ModeloAdherenciaLongitudinal modeloAdherencia) {
        if (motor == null || cajaCambios == null
                || resistenciaCarretera == null || modeloAdherencia == null) {
            throw new IllegalArgumentException("El tren motriz y los modelos físicos son obligatorios.");
        }
        if (!Double.isFinite(masaKg) || masaKg <= 0 || !Double.isFinite(radioRuedaM) || radioRuedaM <= 0) {
            throw new IllegalArgumentException("La masa y el radio de rueda deben ser positivos y finitos.");
        }
        this.motor = motor;
        this.cajaCambios = cajaCambios;
        this.masaKg = masaKg;
        this.resistenciaCarretera = resistenciaCarretera;
        this.modeloAdherencia = modeloAdherencia;
        this.radioRuedaM = radioRuedaM;
        revoluciones = motor.getRevolucionesMinimas();
    }

    public void encender() {
        if (encendido) return;
        encendido = true;
        revoluciones = Math.max(revoluciones, motor.getRevolucionesMinimas());
    }

    public void apagar() {
        if (!encendido) return;
        encendido = false;
        acelerador = 0;
        revoluciones = 0;
    }

    public void setAcelerador(double valor) {
        acelerador = encendido ? limitarEntrada(valor) : 0;
    }

    public void setFreno(double valor) {
        freno = limitarEntrada(valor);
        if (freno > 0.05) acelerador = 0;
    }

    /** Avanza el modelo por un paso en segundos; dt se limita para evitar saltos al pausar la UI. */
    public void actualizar(double dt) {
        if (!Double.isFinite(dt) || dt <= 0) return;
        double paso = Math.min(dt, 0.05);
        int marchaAntes = cajaCambios.getMarchaActual();
        boolean permitirCambio = tiempoCambioRestante <= 0;
        if (encendido) {
            cajaCambios.actualizar(velocidadMps, motor, acelerador, radioRuedaM, permitirCambio);
            if (cajaCambios.getMarchaActual() != marchaAntes) {
                int saltos = Math.abs(cajaCambios.getMarchaActual() - marchaAntes);
                tiempoCambioRestante = RETARDO_CAMBIO_S * saltos;
            }
        }
        int rpmCalculadas = encendido ? cajaCambios.getRevoluciones() : 0;
        if (encendido && velocidadMps < 0.5) rpmCalculadas = motor.getRevolucionesMinimas();
        revoluciones = rpmCalculadas;

        double fuerzaMotorEnRuedas = motor.getParNm(revoluciones)
                * cajaCambios.getRelacionActual() * cajaCambios.getRelacionFinal()
                * cajaCambios.getEficiencia() / radioRuedaM;
        double fuerzaTraccion = acelerador * fuerzaMotorEnRuedas;
        fuerzaTraccion = modeloAdherencia.limitarFuerzaTraccionN(fuerzaTraccion);

        double fuerzaResistencia = resistenciaCarretera.calcularFuerzaN(velocidadMps);
        double fuerzaFrenado = modeloAdherencia.calcularFuerzaFrenadoN(freno);
        double fuerzaMotor = !encendido ? 0 : acelerador > 0 ? fuerzaTraccion : -calcularFrenoMotor();

        if (tiempoCambioRestante > 0) {
            fuerzaMotor *= FRACCION_PAR_DURANTE_CAMBIO;
            tiempoCambioRestante = Math.max(0, tiempoCambioRestante - paso);
        }
        double aceleracion = (fuerzaMotor - fuerzaFrenado - fuerzaResistencia) / masaKg;
        velocidadMps = Math.max(0, velocidadMps + aceleracion * paso);
        double velocidadMaxima = motor.getVelocidadMaxima() / 3.6;
        if (velocidadMps >= velocidadMaxima) {
            velocidadMps = velocidadMaxima; // limitador electrónico publicado para el GTI
            if (acelerador > 0) revoluciones = Math.min(revoluciones, motor.getRevolucionesMaximas());
        }
    }

    private double calcularFrenoMotor() {
        if (!encendido || velocidadMps < 0.5) return 0;
        return motor.getParNm(revoluciones) * 0.12 * cajaCambios.getRelacionActual()
                * cajaCambios.getRelacionFinal() / radioRuedaM;
    }

    private static double limitarEntrada(double valor) {
        if (!Double.isFinite(valor)) throw new IllegalArgumentException("El pedal debe ser un valor finito.");
        return Math.max(0, Math.min(1, valor));
    }

    public void cambiarMarcha(int direccion) {
        if (!cajaCambios.esManual()) return;
        int marchaAntes = cajaCambios.getMarchaActual();
        cajaCambios.cambiarMarcha(direccion);
        if (cajaCambios.getMarchaActual() != marchaAntes) {
            tiempoCambioRestante = RETARDO_CAMBIO_S
                    * Math.abs(cajaCambios.getMarchaActual() - marchaAntes);
        }
    }

    public int getVelocidadActual() { return (int) Math.round(velocidadMps * 3.6); }
    public double getVelocidadMps() { return velocidadMps; }
    public int getRevoluciones() { return encendido ? (int) Math.round(revoluciones) : 0; }
    public int getRevolucionesMaximas() { return motor.getRevolucionesMaximas(); }
    public int getMarchaActual() { return cajaCambios.getMarchaActual(); }
    public int getNumeroMarchas() { return cajaCambios.getNumeroMarchas(); }
    public String getTipoTransmision() { return cajaCambios.getTipo(); }
    public boolean isManual() { return cajaCambios.esManual(); }
    public boolean isEncendido() { return encendido; }
    public Motor getMotor() { return motor; }
    public CajaCambios getCajaCambios() { return cajaCambios; }
    public double getMasaKg() { return masaKg; }
}
