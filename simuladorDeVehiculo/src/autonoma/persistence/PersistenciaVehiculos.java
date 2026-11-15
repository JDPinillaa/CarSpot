package autonoma.persistence;

import autonoma.models.CajaAutomatica;
import autonoma.models.CajaCambios;
import autonoma.models.CajaManual;
import autonoma.models.Llanta;
import autonoma.models.LlantasBaratas;
import autonoma.models.LlantasBuenas;
import autonoma.models.LlantasBonitas;
import autonoma.models.Motor;
import autonoma.models.Motor1000;
import autonoma.models.Motor2000;
import autonoma.models.Motor3000;
import autonoma.models.PerfilVehiculo;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Lee perfiles de vehículos desde un archivo clave/valor extensible. */
public final class PersistenciaVehiculos {
    private static final Path CONFIGURACION = Path.of("data", "especificaciones.txt");

    private PersistenciaVehiculos() { }

    public static CatalogoVehiculos cargarCatalogo() throws IOException {
        Map<String, String> valores = leerValores();
        Map<String, Map<String, String>> perfiles = new LinkedHashMap<>();
        for (Map.Entry<String, String> valor : valores.entrySet()) {
            String[] partes = valor.getKey().split("\\.", 3);
            if (partes.length == 3 && partes[0].equals("perfil")) {
                perfiles.computeIfAbsent(partes[1], ignorado -> new LinkedHashMap<>())
                        .put(partes[2], valor.getValue());
            }
        }

        // Compatibilidad con el formato antiguo: motor/llantas en la raíz.
        if (perfiles.isEmpty()) perfiles.put("principal", valores);
        List<PerfilVehiculo> lista = new ArrayList<>();
        for (Map.Entry<String, Map<String, String>> perfil : perfiles.entrySet()) {
            Map<String, String> datos = perfil.getValue();
            Motor motor = crearMotor(requerido(datos, "motor", perfil.getKey()));
            Llanta llantas = crearLlantas(requerido(datos, "llantas", perfil.getKey()));
            CajaCambios transmision = crearTransmision(datos.getOrDefault("transmision", "automatica"));
            String nombre = datos.getOrDefault("nombre", perfil.getKey());
            String interior = datos.getOrDefault("interior", "clasico");
            lista.add(new PerfilVehiculo(perfil.getKey(), nombre, interior, motor, llantas, transmision));
        }
        return new CatalogoVehiculos(lista, valores.getOrDefault("perfilactivo", lista.get(0).getId()));
    }

    private static Map<String, String> leerValores() throws IOException {
        Map<String, String> valores = new LinkedHashMap<>();
        for (String linea : Files.readAllLines(CONFIGURACION)) {
            String limpia = linea.strip();
            if (limpia.isEmpty() || limpia.startsWith("#")) continue;
            int separador = limpia.indexOf('=');
            String clave;
            String valor;
            if (separador >= 0) {
                clave = limpia.substring(0, separador).trim();
                valor = limpia.substring(separador + 1).trim();
            } else {
                String[] partes = limpia.split("\\s+", 2);
                if (partes.length != 2) continue;
                clave = partes[0];
                valor = partes[1].trim();
            }
            valores.put(clave.toLowerCase(Locale.ROOT), valor);
        }
        return valores;
    }

    private static String requerido(Map<String, String> datos, String clave, String perfil) throws IOException {
        String valor = datos.get(clave);
        if (valor == null || valor.isBlank()) {
            throw new IOException("Falta '" + clave + "' en el perfil '" + perfil + "' de " + CONFIGURACION);
        }
        return valor;
    }

    private static Motor crearMotor(String tipo) throws IOException {
        return switch (tipo.trim()) {
            case "1000" -> new Motor1000();
            case "2000" -> new Motor2000();
            case "3000" -> new Motor3000();
            default -> throw new IOException("Motor no reconocido: " + tipo);
        };
    }

    private static Llanta crearLlantas(String tipo) throws IOException {
        return switch (tipo.toLowerCase(Locale.ROOT)) {
            case "baratas" -> new LlantasBaratas();
            case "buenas" -> new LlantasBuenas();
            case "bonitas" -> new LlantasBonitas();
            default -> throw new IOException("Tipo de llantas no reconocido: " + tipo);
        };
    }

    private static CajaCambios crearTransmision(String tipo) throws IOException {
        return switch (tipo.toLowerCase(Locale.ROOT)) {
            case "manual" -> new CajaManual();
            case "automatica", "automática" -> new CajaAutomatica();
            default -> throw new IOException("Transmisión no reconocida: " + tipo);
        };
    }
}
