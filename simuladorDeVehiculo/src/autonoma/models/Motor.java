/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package autonoma.models;

/**
 * Clase abstracta que representa un Motor.
 * Define la velocidad máxima que un motor puede alcanzar.
 * 
 * Esta clase debe ser extendida por clases concretas que representen tipos específicos de motores.
 * 
 * @author juand
 * @since 20250405
 * @version 1.0
 */
public abstract class Motor {

    /**
     * Velocidad máxima que el motor puede alcanzar, en km/h.
     */
    protected int velocidadMaxima;

    /** Régimen máximo y ralentí usados por el tacómetro. */
    protected int revolucionesMaximas = 6500;
    protected int revolucionesMinimas = 850;

    /**
     * Obtiene la velocidad máxima del motor.
     * 
     * @return La velocidad máxima en km/h.
     */
    public int getVelocidadMaxima() {
        return velocidadMaxima;
    }

    public int getRevolucionesMaximas() { return revolucionesMaximas; }

    public int getRevolucionesMinimas() { return revolucionesMinimas; }
}
