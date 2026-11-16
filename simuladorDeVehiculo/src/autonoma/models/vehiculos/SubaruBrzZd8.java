package autonoma.models.vehiculos;

import autonoma.models.Carro;
import autonoma.models.fisica.ModeloAdherenciaLongitudinal;
import autonoma.models.fisica.ResistenciaCarretera;
import autonoma.models.fisica.TipoTraccion;
import autonoma.models.motores.MotorSubaruBrzZd8;
import autonoma.models.transmisiones.CajaManual;

/**
 * Subaru BRZ (ZD8) 2.4 · 234 PS (Manual 6 velocidades TL70 · RWD).
 * Véase docs/referencia-subaru-brz-zd8.md para el detalle de fuentes y supuestos.
 */
public final class SubaruBrzZd8 extends Carro {
    // Datos oficiales Subaru Europa / Press Kit
    private static final double MASA_KG = 1275.0;
    private static final double BATALLA_M = 2.575;
    private static final double REPARTO_PESO_DELANTERO = 0.53;
    private static final double ALTURA_CENTRO_GRAVEDAD_M = 0.456;
    private static final double[] RELACIONES_MARCHA = {3.626, 2.188, 1.541, 1.213, 1.000, 0.767};
    private static final double RELACION_FINAL = 4.100;

    // Derivado de neumático de serie 215/40 R18
    private static final double RADIO_RUEDA_M = 0.3146;

    // Resistencia EPA 2025 Subaru BRZ 2.4L 6MT R18 (Target Coef A/B/C en N y km/h)
    private static final double RESISTENCIA_CONSTANTE_N = 139.1848;
    private static final double RESISTENCIA_LINEAL_N_POR_KMH = 1.135449;
    private static final double RESISTENCIA_CUADRATICA_N_POR_KMH2 = 0.0280121;

    // Parámetros longitudinales estimados/calibrados
    private static final double FACTOR_ENTREGA_PAR_EFECTIVO = 0.80;
    private static final double ADHERENCIA_LONGITUDINAL = 0.96;
    private static final double DESACELERACION_MAXIMA_FRENADO_G = 1.10;

    public SubaruBrzZd8() {
        super(
                "Subaru BRZ 2.4 · 234 PS (6MT)",
                new MotorSubaruBrzZd8(),
                new CajaManual(RELACIONES_MARCHA, RELACION_FINAL, FACTOR_ENTREGA_PAR_EFECTIVO),
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
                        TipoTraccion.TRASERA)
        );
    }
}
