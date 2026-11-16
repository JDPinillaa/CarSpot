package autonoma.models.vehiculos;

import autonoma.models.Carro;
import autonoma.models.fisica.ModeloAdherenciaLongitudinal;
import autonoma.models.fisica.ResistenciaCarretera;
import autonoma.models.fisica.TipoTraccion;
import autonoma.models.motores.MotorGolfGtiMk85;
import autonoma.models.transmisiones.CajaDsg7GolfGti;

/**
 * Volkswagen Golf GTI Mk8.5 europeo (265 PS · DSG7 DQ381-7F · FWD).
 * Véase docs/referencia-golf-gti-mk8-5.md para el detalle de fuentes y supuestos.
 */
public final class GolfGtiMk85 extends Carro {
    // Datos oficiales Volkswagen Alemania
    private static final double MASA_KG = 1457.0;
    private static final double BATALLA_M = 2.627;

    // Derivado de neumático de serie 225/40 R18
    private static final double RADIO_RUEDA_M = 0.319;

    // Relaciones proxy de DQ381-7F (SEAT León 2.0 TSI 190 FWD + N/V EPA GTI en 7.ª)
    private static final double[] RELACIONES_MARCHA = {3.40, 2.75, 1.767, 0.925, 0.795, 0.755, 0.635};
    private static final double[] RELACIONES_FINALES = {4.167, 3.125, 3.125, 4.167, 4.167, 3.125, 3.125};
    private static final double FACTOR_ENTREGA_PAR_EFECTIVO = 0.81; // calibrado

    // Resistencia EPA 2025 Golf GTI EE. UU. (Set Coef A/B/C convertidos a N y km/h)
    private static final double RESISTENCIA_CONSTANTE_N = 62.3196;
    private static final double RESISTENCIA_LINEAL_N_POR_KMH = 0.330021;
    private static final double RESISTENCIA_CUADRATICA_N_POR_KMH2 = 0.0313953;

    // Parámetros longitudinales estimados/calibrados
    private static final double ADHERENCIA_LONGITUDINAL = 1.11;
    private static final double REPARTO_PESO_DELANTERO = 0.62;
    private static final double ALTURA_CENTRO_GRAVEDAD_M = 0.55;
    private static final double DESACELERACION_MAXIMA_FRENADO_G = 1.05;

    public GolfGtiMk85() {
        super(
                "Volkswagen Golf GTI Mk8.5 · 265 PS",
                new MotorGolfGtiMk85(),
                new CajaDsg7GolfGti(RELACIONES_MARCHA, RELACIONES_FINALES, FACTOR_ENTREGA_PAR_EFECTIVO),
                MASA_KG,
                RADIO_RUEDA_M,
                ResistenciaCarretera.desdeCoeficientes(
                        RESISTENCIA_CONSTANTE_N,
                        RESISTENCIA_LINEAL_N_POR_KMH,
                        RESISTENCIA_CUADRATICA_N_POR_KMH2),
                new ModeloAdherenciaLongitudinal(
                        MASA_KG,
                        ADHERENCIA_LONGITUDINAL,
                        REPARTO_PESO_DELANTERO,
                        ALTURA_CENTRO_GRAVEDAD_M,
                        BATALLA_M,
                        DESACELERACION_MAXIMA_FRENADO_G,
                        TipoTraccion.DELANTERA)
        );
    }
}
