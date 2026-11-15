package autonoma.models;

/** Transmisión cuyo cambio de relación queda bajo control del conductor. */
public final class CajaManual extends AbstractCajaCambios {
    @Override
    public String getTipo() { return "Manual"; }

    @Override
    public boolean esManual() { return true; }

    @Override
    public void actualizar(int velocidadKmh, Motor motor) {
        revoluciones = calcularRevoluciones(velocidadKmh, motor);
    }
}
