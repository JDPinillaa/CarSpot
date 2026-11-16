package autonoma.models.fisica;

/** Límites longitudinales estimados por agarre, transferencia de carga y frenos. */
public final class ModeloAdherenciaLongitudinal {
    private static final double GRAVEDAD = 9.80665;

    private final double masaKg;
    private final double coeficienteAdherencia;
    private final double fraccionPesoDelantero;
    private final double alturaCentroGravedadM;
    private final double batallaM;
    private final double desaceleracionMaximaG;
    private final TipoTraccion tipoTraccion;

    public ModeloAdherenciaLongitudinal(double masaKg, double coeficienteAdherencia,
            double fraccionPesoDelantero, double alturaCentroGravedadM, double batallaM,
            double desaceleracionMaximaG) {
        this(masaKg, coeficienteAdherencia, fraccionPesoDelantero, alturaCentroGravedadM,
                batallaM, desaceleracionMaximaG, TipoTraccion.DELANTERA);
    }

    public ModeloAdherenciaLongitudinal(double masaKg, double coeficienteAdherencia,
            double fraccionPesoDelantero, double alturaCentroGravedadM, double batallaM,
            double desaceleracionMaximaG, TipoTraccion tipoTraccion) {
        if (!Double.isFinite(masaKg) || !Double.isFinite(coeficienteAdherencia)
                || !Double.isFinite(fraccionPesoDelantero) || !Double.isFinite(alturaCentroGravedadM)
                || !Double.isFinite(batallaM) || !Double.isFinite(desaceleracionMaximaG)
                || masaKg <= 0 || coeficienteAdherencia <= 0
                || fraccionPesoDelantero <= 0 || fraccionPesoDelantero >= 1
                || alturaCentroGravedadM <= 0 || batallaM <= 0 || desaceleracionMaximaG <= 0
                || tipoTraccion == null) {
            throw new IllegalArgumentException("Los parámetros de adherencia y frenado deben ser válidos.");
        }
        this.masaKg = masaKg;
        this.coeficienteAdherencia = coeficienteAdherencia;
        this.fraccionPesoDelantero = fraccionPesoDelantero;
        this.alturaCentroGravedadM = alturaCentroGravedadM;
        this.batallaM = batallaM;
        this.desaceleracionMaximaG = desaceleracionMaximaG;
        this.tipoTraccion = tipoTraccion;
    }

    public double limitarFuerzaTraccionN(double fuerzaRequeridaN) {
        double relacionAlturaBatalla = alturaCentroGravedadM / batallaM;
        double factorCargaMotriz = switch (tipoTraccion) {
            case DELANTERA -> coeficienteAdherencia * fraccionPesoDelantero
                    / (1.0 + coeficienteAdherencia * relacionAlturaBatalla);
            case TRASERA -> {
                double denominador = 1.0 - coeficienteAdherencia * relacionAlturaBatalla;
                yield denominador <= 0.05 ? coeficienteAdherencia
                        : coeficienteAdherencia * (1.0 - fraccionPesoDelantero) / denominador;
            }
            case INTEGRAL -> coeficienteAdherencia;
        };
        double fuerzaMaximaN = masaKg * GRAVEDAD * factorCargaMotriz;
        return Math.min(fuerzaRequeridaN, fuerzaMaximaN);
    }

    public double calcularFuerzaFrenadoN(double demandaFreno) {
        return demandaFreno * masaKg * GRAVEDAD * desaceleracionMaximaG;
    }
}
