package autonoma.models.motores;

/** Configuración base de régimen, potencia y curva de par de un motor. */
public abstract class Motor {
    private final int velocidadMaxima;
    private final int potenciaMaximaKw;
    private final int revolucionesMinimas;
    private final int revolucionesMaximas;
    private final CurvaParMotor curvaPar;

    protected Motor(int velocidadMaxima, int potenciaMaximaKw,
            int revolucionesMinimas, int revolucionesMaximas,
            int[] rpmCurva, double[] parNmCurva) {
        if (velocidadMaxima <= 0 || potenciaMaximaKw <= 0
                || revolucionesMinimas <= 0 || revolucionesMaximas <= revolucionesMinimas) {
            throw new IllegalArgumentException("Parámetros de motor inválidos.");
        }
        this.velocidadMaxima = velocidadMaxima;
        this.potenciaMaximaKw = potenciaMaximaKw;
        this.revolucionesMinimas = revolucionesMinimas;
        this.revolucionesMaximas = revolucionesMaximas;
        this.curvaPar = new CurvaParMotor(rpmCurva, parNmCurva);
    }

    /** Velocidad máxima del vehículo (km/h); puede representar un limitador electrónico. */
    public int getVelocidadMaxima() { return velocidadMaxima; }

    public int getPotenciaMaximaKw() { return potenciaMaximaKw; }

    public int getRevolucionesMaximas() { return revolucionesMaximas; }

    public int getRevolucionesMinimas() { return revolucionesMinimas; }

    /** Interpola linealmente la curva de par publicada o calibrada para el motor. */
    public double getParNm(double rpm) {
        return curvaPar.obtenerParNm(rpm);
    }
}
