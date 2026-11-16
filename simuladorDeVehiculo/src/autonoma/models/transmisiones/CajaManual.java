package autonoma.models.transmisiones;

import autonoma.models.motores.Motor;

/** Transmisión cuyo cambio de relación queda bajo control del conductor. */
public final class CajaManual extends AbstractCajaCambios {
    public CajaManual() {
        super();
    }

    public CajaManual(double[] relaciones, double relacionFinal, double eficiencia) {
        super(relaciones, relacionFinal, eficiencia);
    }

    public CajaManual(double[] relaciones, double[] relacionesFinales, double eficiencia) {
        super(relaciones, relacionesFinales, eficiencia);
    }

    @Override
    public String getTipo() { return "Manual " + getNumeroMarchas() + " velocidades"; }

    @Override
    public boolean esManual() { return true; }

    @Override
    public void actualizar(double velocidadMps, Motor motor, double acelerador, double radioRuedaM,
            boolean permitirCambio) {
        revoluciones = calcularRevoluciones(velocidadMps, motor, radioRuedaM);
    }
}
