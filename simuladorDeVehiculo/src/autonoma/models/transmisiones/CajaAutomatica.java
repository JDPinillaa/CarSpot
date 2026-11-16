package autonoma.models.transmisiones;

import autonoma.models.motores.Motor;

/** Transmisión automática con cambios sencillos según el régimen del motor. */
public final class CajaAutomatica extends AbstractCajaCambios {
    @Override
    public String getTipo() { return "Automática"; }

    @Override
    public boolean esManual() { return false; }

    @Override
    public void actualizar(double velocidadMps, Motor motor, double acelerador, double radioRuedaM,
            boolean permitirCambio) {
        revoluciones = calcularRevoluciones(velocidadMps, motor, radioRuedaM);
        if (!permitirCambio) return;
        int umbralSubida = acelerador > 0.65 ? motor.getRevolucionesMaximas() - 120
                : acelerador > 0.1 ? 4200 : 3000;
        if (revoluciones >= umbralSubida && marchaActual < getNumeroMarchas()) cambiarMarcha(1);
        else if (acelerador > 0.55 && debeBajar(revoluciones)) cambiarMarcha(-1);
        revoluciones = calcularRevoluciones(velocidadMps, motor, radioRuedaM);
    }
}
