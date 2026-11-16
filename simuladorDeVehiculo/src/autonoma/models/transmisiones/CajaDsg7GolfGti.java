package autonoma.models.transmisiones;

import autonoma.models.motores.Motor;

/** DQ381-7F del Golf GTI; relaciones internas preliminares, aún estimadas. */
public final class CajaDsg7GolfGti extends AbstractCajaCambios {
    private static final double ACELERADOR_KICKDOWN = 0.80;
    private static final double ACELERADOR_PLENA_CARGA = 0.65;
    private static final double ACELERADOR_CARGA_MEDIA = 0.40;
    private static final double ACELERADOR_CARGA_BAJA = 0.10;
    private static final double FRACCION_RPM_KICKDOWN = 0.74;
    private static final int MARGEN_CAMBIO_PLENA_CARGA_RPM = 450;
    private static final int RPM_CAMBIO_CARGA_MEDIA = 4200;
    private static final int RPM_CAMBIO_CARGA_BAJA = 2600;
    private static final int RPM_CAMBIO_CRUCERO = 1600;

    public CajaDsg7GolfGti() {
        super(new double[]{3.40, 2.75, 1.767, 0.925, 0.795, 0.755, 0.635},
                new double[]{4.167, 3.125, 3.125, 4.167, 4.167, 3.125, 3.125}, 0.90);
    }

    /** Permite parametrizar cada relación final sin presentar datos de otra aplicación como los del GTI. */
    public CajaDsg7GolfGti(double[] relaciones, double[] relacionesFinales, double eficiencia) {
        super(relaciones, relacionesFinales, eficiencia);
    }

    @Override public String getTipo() { return "DSG 7 velocidades"; }
    @Override public boolean esManual() { return false; }

    @Override
    public void actualizar(double velocidadMps, Motor motor, double acelerador, double radioRuedaM,
            boolean permitirCambio) {
        revoluciones = calcularRevoluciones(velocidadMps, motor, radioRuedaM);
        if (!permitirCambio) return;

        int margenCambio = motor.getRevolucionesMaximas() - MARGEN_CAMBIO_PLENA_CARGA_RPM;
        int rpmObjetivo = margenCambio;
        if (acelerador > ACELERADOR_KICKDOWN
                && revoluciones < motor.getRevolucionesMaximas() * FRACCION_RPM_KICKDOWN) {
            while (marchaActual > 1) {
                int rpmAntes = revoluciones;
                cambiarMarcha(-1);
                int rpmCandidata = calcularRevoluciones(velocidadMps, motor, radioRuedaM);
                boolean sobrepasaRegimen = rpmCandidata >= motor.getRevolucionesMaximas() - 900;
                boolean acercaAlPico = Math.abs(rpmObjetivo - rpmCandidata)
                        < Math.abs(rpmObjetivo - rpmAntes);
                if (sobrepasaRegimen || !acercaAlPico) {
                    cambiarMarcha(1);
                    revoluciones = rpmAntes;
                    break;
                }
                revoluciones = rpmCandidata;
            }
            if (revoluciones >= motor.getRevolucionesMaximas() * FRACCION_RPM_KICKDOWN) return;
        }

        // A baja carga busca pronto la marcha larga; a fondo prioriza potencia.
        int umbralSubida = acelerador > ACELERADOR_PLENA_CARGA ? margenCambio
                : acelerador > ACELERADOR_CARGA_MEDIA ? RPM_CAMBIO_CARGA_MEDIA
                : acelerador > ACELERADOR_CARGA_BAJA ? RPM_CAMBIO_CARGA_BAJA : RPM_CAMBIO_CRUCERO;
        if (revoluciones >= umbralSubida && marchaActual < getNumeroMarchas()) cambiarMarcha(1);
        else if (acelerador > 0.55 && debeBajar(revoluciones)) cambiarMarcha(-1);
        revoluciones = calcularRevoluciones(velocidadMps, motor, radioRuedaM);
    }
}
