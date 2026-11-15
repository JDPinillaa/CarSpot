package autonoma.persistence;

import autonoma.models.PerfilVehiculo;
import java.util.List;

/** Perfiles cargados desde configuración y perfil recomendado para iniciar. */
public final class CatalogoVehiculos {
    private final List<PerfilVehiculo> perfiles;
    private final String perfilInicialId;

    public CatalogoVehiculos(List<PerfilVehiculo> perfiles, String perfilInicialId) {
        if (perfiles == null || perfiles.isEmpty()) throw new IllegalArgumentException("El catálogo requiere al menos un vehículo.");
        this.perfiles = List.copyOf(perfiles);
        this.perfilInicialId = perfilInicialId;
    }

    public List<PerfilVehiculo> getPerfiles() { return perfiles; }

    public PerfilVehiculo getPerfilInicial() {
        return perfiles.stream().filter(perfil -> perfil.getId().equals(perfilInicialId))
                .findFirst().orElse(perfiles.get(0));
    }
}
