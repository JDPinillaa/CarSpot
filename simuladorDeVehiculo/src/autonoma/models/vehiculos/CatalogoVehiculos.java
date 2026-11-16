package autonoma.models.vehiculos;

import autonoma.models.Carro;
import java.util.List;

/** Catálogo de vehículos disponibles en el simulador. */
public final class CatalogoVehiculos {
    private CatalogoVehiculos() { }

    public static List<Carro> obtenerVehiculos() {
        return List.of(
                new GolfGtiMk85(),
                new SubaruBrzZd8()
        );
    }
}
