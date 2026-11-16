package autonoma.models.transmisiones;

import autonoma.models.motores.Motor;
import java.util.Arrays;

abstract class AbstractCajaCambios implements CajaCambios {
    private final double[] relaciones;
    private final double[] relacionesFinales;
    private final double eficiencia;
    protected int marchaActual = 1;
    protected int revoluciones;

    protected AbstractCajaCambios() {
        this(new double[]{3.50, 2.20, 1.50, 1.12, 0.88}, 3.90, 0.88);
    }

    protected AbstractCajaCambios(double[] relaciones, double relacionFinal, double eficiencia) {
        if (relaciones == null || relaciones.length == 0
                || !Double.isFinite(relacionFinal) || relacionFinal <= 0) {
            throw new IllegalArgumentException("Relaciones y eficiencia de transmisión inválidas.");
        }
        double[] finales = new double[relaciones.length];
        Arrays.fill(finales, relacionFinal);
        validarEficiencia(eficiencia);
        this.relacionesFinales = finales;
        this.eficiencia = eficiencia;
        this.relaciones = relaciones.clone();
        validarRelaciones();
    }

    protected AbstractCajaCambios(double[] relaciones, double[] relacionesFinales, double eficiencia) {
        if (relaciones == null || relaciones.length == 0 || relacionesFinales == null
                || relacionesFinales.length != relaciones.length) {
            throw new IllegalArgumentException("Debe existir una relación final para cada marcha.");
        }
        validarEficiencia(eficiencia);
        this.relaciones = relaciones.clone();
        this.relacionesFinales = relacionesFinales.clone();
        this.eficiencia = eficiencia;
        validarRelaciones();
        for (double relacionFinal : this.relacionesFinales) {
            if (!Double.isFinite(relacionFinal) || relacionFinal <= 0) {
                throw new IllegalArgumentException("Las relaciones finales deben ser finitas y positivas.");
            }
        }
    }

    private static void validarEficiencia(double eficiencia) {
        if (!Double.isFinite(eficiencia) || eficiencia <= 0 || eficiencia > 1) {
            throw new IllegalArgumentException("La eficiencia de transmisión debe estar entre 0 y 1.");
        }
    }

    private void validarRelaciones() {
        for (int i = 0; i < this.relaciones.length; i++) {
            if (!Double.isFinite(this.relaciones[i]) || this.relaciones[i] <= 0
                    || (i > 0 && this.relaciones[i] >= this.relaciones[i - 1])) {
                throw new IllegalArgumentException("Las relaciones deben ser positivas y estar en orden descendente.");
            }
        }
    }

    @Override
    public int getMarchaActual() { return marchaActual; }

    @Override
    public int getNumeroMarchas() { return relaciones.length; }

    @Override
    public int getRevoluciones() { return revoluciones; }

    @Override
    public void cambiarMarcha(int direccion) {
        if (direccion == 0) return;
        marchaActual = Math.max(1, Math.min(relaciones.length, marchaActual + Integer.signum(direccion)));
    }

    @Override public double getRelacionActual() { return relaciones[marchaActual - 1]; }
    @Override public double getRelacionFinal() { return relacionesFinales[marchaActual - 1]; }
    @Override public double getEficiencia() { return eficiencia; }

    protected int calcularRevoluciones(double velocidadMps, Motor motor, double radioRuedaM) {
        double rpm = velocidadMps * getRelacionActual() * getRelacionFinal()
                * 60.0 / (2.0 * Math.PI * radioRuedaM);
        return velocidadMps < 0.5 ? motor.getRevolucionesMinimas()
                : (int) Math.round(Math.min(motor.getRevolucionesMaximas(), rpm));
    }

    protected boolean debeBajar(int rpm) {
        return rpm <= 1800 && marchaActual > 1;
    }
}
