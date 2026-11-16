package autonoma.models.transmisiones;

import autonoma.models.motores.Motor;

/** Contrato intercambiable de transmisión para cada perfil de vehículo. */
public interface CajaCambios {
    String getTipo();
    boolean esManual();
    int getMarchaActual();
    int getNumeroMarchas();
    int getRevoluciones();
    double getRelacionActual();
    double getRelacionFinal();
    double getEficiencia();
    void cambiarMarcha(int direccion);
    void actualizar(double velocidadMps, Motor motor, double acelerador, double radioRuedaM,
            boolean permitirCambio);
}
