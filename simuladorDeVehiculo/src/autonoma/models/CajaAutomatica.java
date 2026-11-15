package autonoma.models;

/** Transmisión automática con cambios sencillos según el régimen del motor. */
public final class CajaAutomatica extends AbstractCajaCambios {
    @Override
    public String getTipo() { return "Automática"; }

    @Override
    public boolean esManual() { return false; }

    @Override
    public void actualizar(int velocidadKmh, Motor motor) {
        revoluciones = calcularRevoluciones(velocidadKmh, motor);
        while (debeSubir(revoluciones, motor)) {
            cambiarMarcha(1);
            revoluciones = calcularRevoluciones(velocidadKmh, motor);
        }
        while (debeBajar(revoluciones, motor)) {
            cambiarMarcha(-1);
            revoluciones = calcularRevoluciones(velocidadKmh, motor);
        }
    }
}
