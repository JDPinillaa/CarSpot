package autonoma.models;

/** Contrato intercambiable de transmisión para cada perfil de vehículo. */
public interface CajaCambios {
    String getTipo();
    boolean esManual();
    int getMarchaActual();
    int getNumeroMarchas();
    int getRevoluciones();
    int getLimiteVelocidadMarcha(Motor motor);
    void cambiarMarcha(int direccion);
    void actualizar(int velocidadKmh, Motor motor);
}
