package autonoma.models.motores;

/**
 * Curva del bóxer atmosférico 2.4L FA24D del Subaru BRZ (ZD8) europeo de 234 PS (172 kW).
 */
public final class MotorSubaruBrzZd8 extends Motor {
    public MotorSubaruBrzZd8() {
        // Publicado por Subaru: 250 Nm a 3.700 rpm, 172 kW (234 PS) a 7.000 rpm,
        // corte a 7.400 rpm y velocidad máxima de 226 km/h (6MT).
        // A 7.000 rpm, 234,640 Nm se deriva de T = P / omega para coincidir con 172 kW.
        // Los puntos en ralentí (750 rpm), transición media y caída hacia el corte
        // son estimaciones sobre la curva característica del FA24D.
        super(
                226,
                172,
                750,
                7400,
                new int[]{750, 1500, 2500, 3700, 4500, 5200, 6000, 6500, 7000, 7400},
                new double[]{165, 205, 232, 250, 240, 242, 244, 241, 234.640, 218}
        );
    }
}
