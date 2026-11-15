package autonoma.models;

/** Configuración ensamblada de un vehículo y su presentación. */
public final class PerfilVehiculo {
    private final String id;
    private final String nombre;
    private final String interior;
    private final Carro carro;

    public PerfilVehiculo(String id, String nombre, String interior, Motor motor, Llanta llantas,
            CajaCambios cajaCambios) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Falta el identificador del vehículo.");
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Falta el nombre del vehículo.");
        this.id = id;
        this.nombre = nombre;
        this.interior = normalizarInterior(interior);
        this.carro = new Carro(motor, llantas, cajaCambios);
    }

    private static String normalizarInterior(String interior) {
        String valor = interior == null ? "clasico" : interior.trim().toLowerCase();
        if (!valor.equals("clasico") && !valor.equals("deportivo")) {
            throw new IllegalArgumentException("Interior no reconocido: " + interior + ". Opciones: clasico, deportivo.");
        }
        return valor;
    }

    public String getNombre() { return nombre; }
    public String getId() { return id; }
    public String getInterior() { return interior; }
    public Carro getCarro() { return carro; }

    @Override
    public String toString() { return nombre; }
}
