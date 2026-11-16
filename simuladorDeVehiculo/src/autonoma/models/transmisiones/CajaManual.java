package autonoma.models.transmisiones;

import autonoma.models.motores.Motor;

/** Transmisión cuyo cambio de relación queda bajo control del conductor. */
public final class CajaManual extends AbstractCajaCambios {
    @Override
    public String getTipo() { return "Manual"; }

    @Override
    public boolean esManual() { return true; }

    @Override
    public void actualizar(double velocidadMps, Motor motor, double acelerador, double radioRuedaM,
            boolean permitirCambio) {
        revoluciones = calcularRevoluciones(velocidadMps, motor, radioRuedaM);
    }
}
