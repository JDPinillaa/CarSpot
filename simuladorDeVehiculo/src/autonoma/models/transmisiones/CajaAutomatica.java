package autonoma.models.transmisiones;

import autonoma.models.motores.Motor;

/** Transmisión automática con cambios sencillos según el régimen del motor. */
public final class CajaAutomatica extends AbstractCajaCambios {
    public CajaAutomatica() {
        super();
    }

    public CajaAutomatica(double[] relaciones, double relacionFinal, double eficiencia) {
        super(relaciones, relacionFinal, eficiencia);
    }

    public CajaAutomatica(double[] relaciones, double[] relacionesFinales, double eficiencia) {
        super(relaciones, relacionesFinales, eficiencia);
    }

    @Override
    public String getTipo() { return "Automática " + getNumeroMarchas() + " velocidades"; }

    @Override
    public boolean esManual() { return false; }

    @Override
    public void actualizar(double velocidadMps, Motor motor, double acelerador, double radioRuedaM,
            boolean permitirCambio) {
        revoluciones = calcularRevoluciones(velocidadMps, motor, radioRuedaM);
        if (!permitirCambio) return;
        int umbralSubida = acelerador > 0.65 ? motor.getRevolucionesMaximas() - 150
                : acelerador > 0.1 ? 4200 : 3000;
        if (revoluciones >= umbralSubida && marchaActual < getNumeroMarchas()) cambiarMarcha(1);
        else if (acelerador > 0.55 && debeBajar(revoluciones)) cambiarMarcha(-1);
        revoluciones = calcularRevoluciones(velocidadMps, motor, radioRuedaM);
    }
}
