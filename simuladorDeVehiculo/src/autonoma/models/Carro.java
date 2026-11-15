package autonoma.models;

import autonoma.exceptions.CarroAccidentadoException;
import autonoma.exceptions.CarroApagadoException;
import autonoma.exceptions.CarroDetenidoException;
import autonoma.exceptions.CarroHaPatinadoException;
import autonoma.exceptions.CarroYaApagadoException;
import autonoma.exceptions.CarroYaEncendidoException;

/** Estado y reglas del vehículo, sin dependencias de interfaz ni de audio. */
public class Carro {

    private final Motor motor;
    private final Llanta llantas;
    private final CajaCambios cajaCambios;
    private boolean encendido;
    private int velocidadActual;
    private boolean haPatinado;
    private boolean seHaAccidentado;

    public Carro(Motor motor, Llanta llantas) {
        this(motor, llantas, new CajaAutomatica());
    }

    public Carro(Motor motor, Llanta llantas, CajaCambios cajaCambios) {
        if (motor == null || llantas == null || cajaCambios == null) {
            throw new IllegalArgumentException("Motor, llantas y caja de cambios son obligatorios.");
        }
        this.motor = motor;
        this.llantas = llantas;
        this.cajaCambios = cajaCambios;
    }

    public void encender() throws CarroYaEncendidoException {
        if (encendido) throw new CarroYaEncendidoException("El vehículo ya está encendido.");
        if (seHaAccidentado) throw new IllegalStateException("El vehículo está accidentado.");
        encendido = true;
        cajaCambios.actualizar(velocidadActual, motor);
    }

    public void apagar() throws CarroYaApagadoException, CarroAccidentadoException {
        if (seHaAccidentado) throw new CarroAccidentadoException("El vehículo está accidentado.");
        if (!encendido) throw new CarroYaApagadoException("El vehículo ya está apagado.");
        if (velocidadActual > 60) {
            seHaAccidentado = true;
            encendido = false;
            velocidadActual = 0;
            throw new CarroAccidentadoException("¡Accidente! No se puede apagar a esa velocidad.");
        }
        encendido = false;
    }

    public void acelerar(int kmh) throws CarroApagadoException, CarroAccidentadoException {
        validarIncremento(kmh);
        validarDisponible();
        long nuevaVelocidad = (long) velocidadActual + kmh;
        if (cajaCambios.esManual() && nuevaVelocidad > cajaCambios.getLimiteVelocidadMarcha(motor)) {
            throw new IllegalStateException("El motor llegó al límite de esta marcha. Cambie a una marcha superior.");
        }
        if (nuevaVelocidad > motor.getVelocidadMaxima()) {
            accidentar("¡El motor falló! Aceleró más de lo permitido.");
        }
        velocidadActual = (int) nuevaVelocidad;
        cajaCambios.actualizar(velocidadActual, motor);
    }

    public void frenar(int kmh) throws CarroApagadoException, CarroDetenidoException,
            CarroHaPatinadoException, CarroAccidentadoException {
        validarIncremento(kmh);
        validarDisponible();
        if (kmh == 0) return;
        if (velocidadActual == 0) throw new CarroDetenidoException("El vehículo ya está detenido.");
        boolean frenadoBrusco = kmh > 30;
        if (!haPatinado && frenadoBrusco && velocidadActual > llantas.getLimitePatinaje()) {
            haPatinado = true;
            throw new CarroHaPatinadoException("¡El vehículo patinó al frenar bruscamente!");
        }
        if (!haPatinado && kmh > velocidadActual) {
            haPatinado = true;
            throw new CarroHaPatinadoException("El frenado solicitado supera la velocidad actual.");
        }

        // Un coche que ya patina aún puede reducir velocidad; de lo contrario
        // quedaba bloqueado y nunca podía recuperar el control.
        velocidadActual -= haPatinado ? Math.max(1, kmh / 2) : kmh;
        velocidadActual = Math.max(0, velocidadActual);
        if (velocidadActual == 0) haPatinado = false;
        cajaCambios.actualizar(velocidadActual, motor);
    }

    public void cambiarMarcha(int direccion) {
        if (!cajaCambios.esManual()) {
            throw new IllegalStateException("Este vehículo tiene transmisión automática.");
        }
        cajaCambios.cambiarMarcha(direccion);
        cajaCambios.actualizar(velocidadActual, motor);
    }

    private void validarDisponible() throws CarroApagadoException, CarroAccidentadoException {
        if (seHaAccidentado) throw new CarroAccidentadoException("El vehículo está accidentado.");
        if (!encendido) throw new CarroApagadoException("El vehículo está apagado.");
    }

    private void validarIncremento(int kmh) {
        if (kmh < 0) throw new IllegalArgumentException("La cantidad debe ser cero o positiva.");
    }

    private void accidentar(String mensaje) throws CarroAccidentadoException {
        seHaAccidentado = true;
        encendido = false;
        velocidadActual = 0;
        throw new CarroAccidentadoException(mensaje);
    }

    public int getVelocidadActual() { return velocidadActual; }
    public int getRevoluciones() { return encendido ? cajaCambios.getRevoluciones() : 0; }
    public int getRevolucionesMaximas() { return motor.getRevolucionesMaximas(); }
    public int getMarchaActual() { return cajaCambios.getMarchaActual(); }
    public int getNumeroMarchas() { return cajaCambios.getNumeroMarchas(); }
    public String getTipoTransmision() { return cajaCambios.getTipo(); }
    public boolean isManual() { return cajaCambios.esManual(); }
    public boolean isEncendido() { return encendido; }
    public boolean haPatinado() { return haPatinado; }
    public boolean seHaAccidentado() { return seHaAccidentado; }
    public Motor getMotor() { return motor; }
    public Llanta getLlantas() { return llantas; }
    public CajaCambios getCajaCambios() { return cajaCambios; }
}
