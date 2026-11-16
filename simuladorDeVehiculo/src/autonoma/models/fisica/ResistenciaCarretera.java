package autonoma.models.fisica;

/** Fuerza de rodadura y aerodinámica, expresada como F0 + F1·v + F2·v². */
public final class ResistenciaCarretera {
    private static final double GRAVEDAD = 9.80665;
    private static final double DENSIDAD_AIRE_KG_M3 = 1.225;

    private final double fuerzaConstanteN;
    private final double fuerzaLinealNPorKmh;
    private final double fuerzaCuadraticaNPorKmh2;

    private ResistenciaCarretera(double constanteN, double linealNPorKmh,
            double cuadraticaNPorKmh2) {
        if (!Double.isFinite(constanteN) || !Double.isFinite(linealNPorKmh)
                || !Double.isFinite(cuadraticaNPorKmh2)
                || constanteN < 0 || linealNPorKmh < 0 || cuadraticaNPorKmh2 < 0) {
            throw new IllegalArgumentException("Los coeficientes de resistencia deben ser finitos y no negativos.");
        }
        fuerzaConstanteN = constanteN;
        fuerzaLinealNPorKmh = linealNPorKmh;
        fuerzaCuadraticaNPorKmh2 = cuadraticaNPorKmh2;
    }

    /** Construye la resistencia a partir de masa, CdA y coeficiente de rodadura. */
    public static ResistenciaCarretera desdeCda(double masaKg, double cdaM2,
            double coeficienteRodadura) {
        if (!Double.isFinite(masaKg) || !Double.isFinite(cdaM2)
                || !Double.isFinite(coeficienteRodadura)
                || masaKg <= 0 || cdaM2 <= 0 || coeficienteRodadura < 0) {
            throw new IllegalArgumentException("Masa, CdA y rodadura deben ser parámetros físicos válidos.");
        }
        double constante = masaKg * GRAVEDAD * coeficienteRodadura;
        double cuadratica = 0.5 * DENSIDAD_AIRE_KG_M3 * cdaM2 / (3.6 * 3.6);
        return new ResistenciaCarretera(constante, 0, cuadratica);
    }

    /** Construye la resistencia desde coeficientes totales con velocidad en km/h. */
    public static ResistenciaCarretera desdeCoeficientes(double constanteN,
            double linealNPorKmh, double cuadraticaNPorKmh2) {
        return new ResistenciaCarretera(constanteN, linealNPorKmh, cuadraticaNPorKmh2);
    }

    public double calcularFuerzaN(double velocidadMps) {
        if (!Double.isFinite(velocidadMps) || velocidadMps <= 0) return 0;
        double velocidadKmh = velocidadMps * 3.6;
        return fuerzaConstanteN + fuerzaLinealNPorKmh * velocidadKmh
                + fuerzaCuadraticaNPorKmh2 * velocidadKmh * velocidadKmh;
    }
}
