package autonoma.models.motores;

/** Interpolación lineal de una curva de par definida por puntos RPM/Nm. */
public final class CurvaParMotor {
    private final int[] revolucionesRpm;
    private final double[] parNm;

    public CurvaParMotor(int[] revolucionesRpm, double[] parNm) {
        validar(revolucionesRpm, parNm);
        this.revolucionesRpm = revolucionesRpm.clone();
        this.parNm = parNm.clone();
    }

    public double obtenerParNm(double rpm) {
        if (rpm <= revolucionesRpm[0]) return parNm[0];
        for (int i = 1; i < revolucionesRpm.length; i++) {
            if (rpm <= revolucionesRpm[i]) {
                double proporcion = (rpm - revolucionesRpm[i - 1])
                        / (revolucionesRpm[i] - revolucionesRpm[i - 1]);
                return parNm[i - 1] + proporcion * (parNm[i] - parNm[i - 1]);
            }
        }
        return 0;
    }

    private static void validar(int[] rpm, double[] par) {
        if (rpm == null || par == null || rpm.length < 2 || rpm.length != par.length) {
            throw new IllegalArgumentException("La curva de par necesita pares de puntos RPM/Nm.");
        }
        if (rpm[0] < 0 || !Double.isFinite(par[0]) || par[0] < 0) {
            throw new IllegalArgumentException("La curva de par debe iniciar con valores no negativos.");
        }
        for (int i = 0; i < rpm.length; i++) {
            if (rpm[i] < 0 || !Double.isFinite(par[i]) || par[i] < 0
                    || (i > 0 && rpm[i] <= rpm[i - 1])) {
                throw new IllegalArgumentException(
                        "La curva de par debe estar ordenada y contener valores finitos no negativos.");
            }
        }
    }
}
