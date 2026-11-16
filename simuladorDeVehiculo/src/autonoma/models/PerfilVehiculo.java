package autonoma.models;

/** Configuración ensamblada de un vehículo identificable en el catálogo. */
public final class PerfilVehiculo {
    private final String id;
    private final String nombre;
    private final Carro carro;

    public PerfilVehiculo(String id, String nombre, Carro carro) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Falta el identificador del vehículo.");
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Falta el nombre del vehículo.");
        if (carro == null) throw new IllegalArgumentException("El vehículo ensamblado es obligatorio.");
        this.id = id;
        this.nombre = nombre;
        this.carro = carro;
    }

    public String getNombre() { return nombre; }
    public String getId() { return id; }
    public Carro getCarro() { return carro; }

    @Override
    public String toString() { return nombre; }
}
