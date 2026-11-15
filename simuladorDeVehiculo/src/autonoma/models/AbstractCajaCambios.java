package autonoma.models;

abstract class AbstractCajaCambios implements CajaCambios {
    private static final double[] RELACIONES = {3.50, 2.20, 1.50, 1.12, 0.88};
    protected int marchaActual = 1;
    protected int revoluciones;

    @Override
    public int getMarchaActual() { return marchaActual; }

    @Override
    public int getNumeroMarchas() { return RELACIONES.length; }

    @Override
    public int getRevoluciones() { return revoluciones; }

    @Override
    public void cambiarMarcha(int direccion) {
        if (direccion == 0) return;
        marchaActual = Math.max(1, Math.min(RELACIONES.length, marchaActual + Integer.signum(direccion)));
    }

    protected int calcularRevoluciones(int velocidadKmh, Motor motor) {
        double relacionActual = RELACIONES[marchaActual - 1];
        double relacionFinal = RELACIONES[RELACIONES.length - 1];
        double velocidadEnMarcha = motor.getVelocidadMaxima() * relacionFinal / relacionActual;
        double fraccion = Math.min(1.0, velocidadKmh / Math.max(1.0, velocidadEnMarcha));
        int rango = motor.getRevolucionesMaximas() - motor.getRevolucionesMinimas();
        return motor.getRevolucionesMinimas() + (int) Math.round(rango * fraccion);
    }

    @Override
    public int getLimiteVelocidadMarcha(Motor motor) {
        double relacionActual = RELACIONES[marchaActual - 1];
        double relacionFinal = RELACIONES[RELACIONES.length - 1];
        return (int) Math.round(motor.getVelocidadMaxima() * relacionFinal / relacionActual);
    }

    protected boolean debeSubir(int rpm, Motor motor) {
        return rpm >= motor.getRevolucionesMaximas() * 0.86 && marchaActual < getNumeroMarchas();
    }

    protected boolean debeBajar(int rpm, Motor motor) {
        return rpm <= motor.getRevolucionesMinimas() + 900 && marchaActual > 1;
    }
}
